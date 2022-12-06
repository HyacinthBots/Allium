package org.hyacinthbots.allium.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenuContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Modrinth Commands. Written in pure pain.
 * @author NotJansel
 * @since 0.1.3
 */
class Modrinth : Extension() {
    override val name = "modrinth"
    override suspend fun setup() {
        publicSlashCommand {
            name = "modrinth"
            description = "What is Modrinth?"
            searchModrinth("", 0) // detekt cries
            publicSubCommand(::UserSearchQuery) {
                name = "user"
                description = "Search for a User"
                action {
                    val url = "https://api.modrinth.com/v2/user/${arguments.query}"
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
                    val request = webRequest(url)
                    if (request.statusCode() == 404) {
                        respond { content = "No user found under query ${arguments.query}." }
                    }
                    val user = JsonParser.parseString(request.body()).asJsonObject
                    respond {
                        embed {
                            title = user["username"].asString
                            description = (
                                if (user["bio"] != JsonNull.INSTANCE) {
                                user["bio"].asString
                                } else {
                                "No bio set."
                                }
                            ).toString()
                            thumbnail {
                                this.url = user["avatar_url"].asString
                            }
                        }
                    }
                }
            }

            publicSubCommand(::ModrinthSearchQuery) {
                name = "project"
                description = "Search for a mod/plugin"
                action {
                    val url =
                        "https://api.modrinth.com/v2/search?limit=${arguments.limit}&query=${
                            arguments.query.replace(" ", "%20")
                        }"
                    val request = webRequest(url)
                    val response = JsonParser.parseString(request.body()).asJsonObject
                    val hits: JsonArray = response["hits"].asJsonArray
                    if (hits.isEmpty) {
                        respond {
                            content = "No results found."
                        }
                        return@action
                    }
                    if (response["total_hits"].asInt == 1) {
                        val hit = hits.get(0).asJsonObject

                        val strLoaders = getProjectLoaders(hit["slug"].asString)

                        respond {
                            embed {
                                embedContents(hit, strLoaders)
                            }
                        }
                        return@action
                    } else {
                        respondingPaginator {
                            for ((i, _) in hits.withIndex()) {
                                val hit: JsonObject = hits.get(i).asJsonObject

                                val strLoaders = getProjectLoaders(hit["slug"].asString)

                                page {
                                    embedContents(hit, strLoaders)
                                }
                            }
                            timeoutSeconds = 180
                            locale = Locale.ENGLISH
                        }.send()
                    }
                }
            }

            publicSubCommand(::AdvancedSearchQuery) {
                name = "advanced"
                description = "Advanced search"
                action {
                    var searchFilters = Modrinth.SearchData(arguments.query, mutableMapOf(Pair("", "")))

                    respond {
                        content = "Use the menu below to narrow your search"
                        components {
                            ephemeralSelectMenu(0) {
                                placeholder = "Adjust your search parameters"
                                maximumChoices = 1
                                option("Edit category filter", "category") {
                                    description = "Change which categories you want to limit your search to"
                                }
                                option("Edit environment filter", "environment") {
                                    description = "Change which environment(s) you want to limit your search to"
                                }
                                option("Edit loader filter", "loader") {
                                    description = "Change which mod loader(s) you want to limit your search to"
                                }
                                option("Edit version filter", "version") {
                                    description = "Change which version(s) you want to limit your search to"
                                }
                                option("Edit license filter", "license") {
                                    description = "Change which license(s) you want to limit your search to"
                                }
                                option("Edit project type filter", "type") {
                                    description = "Change which project type(s) you want to limit your search to"
                                }

                                action {
                                    when (this.selected[0]) {
                                        "category" -> searchFilters = createFilterMenu(
                                            "category",
                                            getModCategories(),
                                            searchFilters
                                        )
                                        "environment" -> searchFilters = createFilterMenu(
                                            "environment",
                                            mutableListOf("server", "client"),
                                            searchFilters
                                        )
                                        "loader" -> searchFilters = createFilterMenu(
                                            "loader",
                                            getModLoaders(),
                                            searchFilters
                                        )
                                        "version" -> searchFilters = createFilterMenu(
                                            "version",
                                            // only use 25 results due to limit of select menus
                                            getMinecraftVersions().subList(0, 24),
                                            searchFilters
                                        )
                                        "license" -> searchFilters = createFilterMenu(
                                            "license",
                                            getLicenses(),
                                            searchFilters
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend inline fun getProjectLoaders(name: String): String {
        val versionsreq = webRequest("https://api.modrinth.com/v2/project/$name/version")
        val versionsres = JsonParser.parseString(versionsreq.body()).asJsonArray
        val m: MutableSet<String> = HashSet()
        for ((index, vers_hit) in versionsres.withIndex()) {
            index.toString() // leave this as else this doesn't work (please I don't want to count up manually)
            val loaders = ArrayList<String>()
            for (loader in vers_hit.asJsonObject.getAsJsonArray("loaders")) {
                loaders.add(loader.asString)
            }
            m.addAll(loaders)
        }
        var strLoaders = ""
        for (entry in m) {
            val formattedEntry = entry[0].uppercase() + entry.drop(1)
            strLoaders += formattedEntry + "\n"
        }
        strLoaders.dropLast(2)

        return strLoaders
    }

    private fun EmbedBuilder.embedContents(hit: JsonObject, strLoaders: String) {
        this.title = hit["title"].asString
        this.url = "https://modrinth.com/project/${hit["slug"].asString}"
        thumbnail {
            this.url = hit["icon_url"].asString
        }
        this.description = hit["description"].asString
        field("Latest Version", true) { hit["latest_version"].asString }
        field(
            "Client/Server Side",
            true
        ) { "Client: ${hit["client_side"].asString}\nServer: ${hit["server_side"].asString}" }
        field("Downloads", true) { hit["downloads"].asString }
        field("Author", true) { hit["author"].asString }
        field(
            "Last Update",
            true
        ) { "<t:${Instant.parse(hit["date_modified"].asString).epochSeconds}>" }
        field("License", true) { hit["license"].asString }
        field("Loaders", true) { strLoaders }
        footer {
            this.text = "Modrinth | ${hit["author"].asString}"
        }
    }

    private suspend fun getLicenses(): MutableList<String> {
        val client = HttpClient()
        val response = client.request("https://api.modrinth.com/v2/tag/license")
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        val stringResponseArray = json.decodeFromString<List<LicenseData>>(response)

        val licenses = mutableListOf<String>()
        stringResponseArray.forEach {
            licenses.add(it.short)
            println(it.short)
        }
        return licenses
    }

    private suspend fun searchModrinth(query: String, limit: Int): SearchResponseData {
        val route = "https://modrinth.com/v2/search?query=$query&limit=$limit"

        val client = HttpClient()
        val response = client.request(route)
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(response)
    }

    private suspend fun searchModrinthAdvanced(currentFilter: SearchData): SearchResponseData {
        val route = "https://api.modrinth.com/v2/search?limit=5"

        val client = HttpClient()
        val response = client.request(route)
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        val decodedResponse = json.decodeFromString<SearchResponseData>(response)

        // for detekt
        println(decodedResponse)
        println(currentFilter)
        return decodedResponse
    }

    private suspend fun EphemeralSelectMenuContext.createFilterMenu(
        filterType: String,
        filterOptions: MutableList<String>,
        currentFilter: SearchData
    ): SearchData {
        respond {
            components {
                ephemeralSelectMenu {
                    maximumChoices = filterOptions.size
                    placeholder = "Filter by $filterType"
                    filterOptions.forEach {
                        option(it, it)
                    }
                    action {
                        this.selected.forEach {
                            currentFilter.facets[it] = filterType
                        }
                        val data = searchModrinthAdvanced(currentFilter)
                        respond {
                            editingPaginator {
                                for (hit in data.hits) {
                                    page {
                                        this.title = hit.title
                                        this.description = hit.description
                                        thumbnail {
                                            this.url = hit.iconURL.toString()
                                        }
                                        field {
                                            this.name = "Loaders"
                                            this.value = getProjectLoaders(hit.slug)
                                        }
                                        field {
                                            this.name = ""
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentFilter // return it so any other functions called can access it
    }

    private suspend fun getModCategories(): MutableList<String> {
        val client = HttpClient()
        val response = client.request("https://api.modrinth.com/v2/tag/category")
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        val stringResponseArray = json.decodeFromString<List<CategoryData>>(response)

        val modCategories = mutableListOf<String>()
        stringResponseArray.forEach {
            if (it.projectType == "mod") {
                modCategories.add(it.name)
            }
        }
        return modCategories
    }

    private suspend fun getModLoaders(): MutableList<String> {
        val client = HttpClient()
        val response = client.request("https://api.modrinth.com/v2/tag/loader")
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        val stringResponseArray = json.decodeFromString<List<LoaderData>>(response)

        val modLoaders = mutableListOf<String>()
        stringResponseArray.forEach {
            if ("mod" in it.supportedProjectTypes) {
                modLoaders.add(it.name)
            }
        }
        return modLoaders
    }

    private suspend fun getMinecraftVersions(): MutableList<String> {
        val client = HttpClient()
        val response = client.request("https://api.modrinth.com/v2/tag/game_version")
            .readBytes().decodeToString()
        client.close()

        val json = Json { ignoreUnknownKeys = true }
        val stringResponseArray = json.decodeFromString<List<VersionData>>(response)

        val minecraftVersions = mutableListOf<String>()
        stringResponseArray.forEach {
            if (it.versionType == "release") {
                minecraftVersions.add(it.version)
            }
        }
        return minecraftVersions
    }

    inner class ModrinthSearchQuery : Arguments() {
        val query by string {
            name = "query"
            description = "Query to search"
        }
        val limit by defaultingInt {
            name = "limit"
            description = "limit search results"
            defaultValue = 5
        }
    }

    inner class AdvancedSearchQuery : Arguments() {
        val query by string {
            name = "string"
            description = "Query to search"
        }
    }

    inner class UserSearchQuery : Arguments() {
        val query by string {
            name = "query"
            description = "User to search up"
        }
    }

    @Serializable
    data class SearchData(
        val query: String,
        val facets: MutableMap<String, String>, // the key is the facet and the value is the facet type
    )

    @Serializable
    data class SearchResponseData(
        val hits: List<ProjectData>,
        val offset: Int,
        val limit: Int,
        @SerialName("total_hits") val totalHits: Int
    )

    @Serializable
    data class ProjectData(
        val slug: String,
        val title: String,
        val description: String,
        val categories: MutableList<String>,
        @SerialName("client_side") val clientSide: String,
        @SerialName("server_side") val serverSide: String,
        @SerialName("source_url") val sourceURL: String? = null,
        @SerialName("discord_url") val discordURL: String? = null,
        @SerialName("project_type") val projectType: String,
        val downloads: Int,
        @SerialName("icon_url") val iconURL: String?,
        val license: String?
    )

    @Serializable
    data class CategoryData(
        val icon: String,
        val name: String,
        @SerialName("project_type") val projectType: String
    )

    @Serializable
    data class LoaderData(
        val icon: String,
        val name: String,
        @SerialName("supported_project_types") val supportedProjectTypes: MutableList<String>
    )

    @Serializable
    data class VersionData(
        val version: String,
        @SerialName("version_type") val versionType: String,
        val date: String,
        val major: Boolean
    )

    @Serializable
    data class LicenseData(
        val short: String,
        val name: String
    )
}
