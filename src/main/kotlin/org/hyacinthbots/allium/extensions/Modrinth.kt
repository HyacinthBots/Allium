package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralStringSelectMenu
import com.kotlindiscord.kord.extensions.components.menus.string.EphemeralStringSelectMenuContext
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.utils.*
import java.util.*

/**
 * Modrinth Commands. Written in pure pain.
 * @author NotJansel
 * @since 0.1.3
 */
class Modrinth : Extension() {
    override val name = "modrinth"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(UserAgent) {
            agent = "hyacinthbots/allium/$BUILD (github@notjansel.de)"
        }
    }

    override suspend fun setup() {
            publicSlashCommand {
            name = "modrinth"
            description = "What is Modrinth?"
            publicSubCommand(::UserSearchQuery) {
                name = "user"
                description = "Search for a User"
                action {
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
                    lateinit var response: UserData
                    try {
                        response = searchModrinthUser(arguments)
                    } catch (e: NoTransformationFoundException) {
                        respond {
                            content = "No User found or bad response."
                        }
                        return@action
                    }
                    respond {
                        embed(fun EmbedBuilder.() {
                            embedUser(response)
                        })
                    }
                }
            }

            publicSubCommand(::ModrinthSlugQuery) {
                name = "project"
                description = "Get a Project by it's slug"
                action {
                    val response = getProject(arguments.slug)
                    respond {
                        embed {
                            embedDirectProject(response, arguments.slug)
                        }
                    }
                }
            }

            publicSubCommand(::ModrinthSearchQuery) {
                name = "search"
                description = "Search for a mod/plugin"
                action {
                    arguments.query.replace(" ", "%20")
                    val response = searchModrinth(arguments.query, arguments.limit)
                    if (response.hits.count() == 1) {
                        respond {
                            embed {
                                embedProject(response.hits[0])
                            }
                        }
                        return@action
                    } else {
                        respondingPaginator {
                            for ((i, _) in response.hits.withIndex()) {
                                page {
                                    embedProject(response.hits[i])
                                }
                            }
                            owner = user
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
                    arguments.query.replace(" ", "%20")
                    var searchFilters = SearchData(arguments.query, mutableMapOf(Pair("", "")))

                    respond {
                        content = "Use the menu below to narrow your search"
                        components {
                            ephemeralStringSelectMenu(0) {
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

                                action {
                                    when (this.selected[0]) {
                                        "category" -> searchFilters = createFilterMenu(
                                                "category",
                                                getModCategories().toMutableList(),
                                                searchFilters
                                        )

                                        "environment" -> searchFilters = createFilterMenu(
                                                "environment",
                                                mutableListOf("server", "client"),
                                                searchFilters
                                        )

                                        "loader" -> searchFilters = createFilterMenu(
                                                "loader",
                                                getModLoaders().toMutableList(),
                                                searchFilters
                                        )

                                        "version" -> searchFilters = createFilterMenu(
                                                "version",
                                                // only use 25 results due to limit of select menus
                                                getMinecraftVersions().take(25).toMutableList(),
                                                searchFilters
                                        )

                                        "license" -> searchFilters = createFilterMenu(
                                                "license",
                                                getLicenses().toMutableList(),
                                                searchFilters
                                        )
                                    }
                                }
                            }
                            publicButton(1) {
                                label = "Search with your selections"
                                action {
                                    val results = searchModrinthAdvanced(searchFilters)
                                    edit {
                                        content = "Here is your Problem"
                                    }
                                    editingPaginator {
                                        for (data in results.hits) {
                                            page {
                                                embedProject(data)
                                            }
                                        }
                                    }.send()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun EmbedBuilder.embedUser(user: UserData) {
        this.title = user.name ?: user.username
        this.description = user.bio ?: "No bio set."
        this.url = URLBuilder(MODRINTH_FRONTEND_ENDPOINT + "user/" + user.username).buildString()
        this.thumbnail {
            this.url = user.avatarUrl
        }
    }

    private suspend fun EmbedBuilder.embedProject(data: ProjectData) {
        this.title = data.title
        this.url = URLBuilder(MODRINTH_FRONTEND_ENDPOINT).appendPathSegments("project", data.slug).buildString()
        thumbnail {
            this.url = data.iconURL.toString()
        }
        this.description = data.description
        field("Latest Version", true) { data.latestVersion }
        field(
                "Client/Server Side",
                true
        ) { "Client: ${data.clientSide}\nServer: ${data.serverSide}" }
        field("Downloads", true) { data.downloads.toString() }
        field("Author", true) { data.author }
        field(
                "Last Update",
                true
        ) { "<t:${Instant.parse(data.dateModified).epochSeconds}>" }
        field("License", true) { data.license.toString() }
        field("Loaders", true) { getProjectLoaders(data.slug).joinToString("\n") }
        footer {
            this.text = "Modrinth | ${data.author}"
        }
    }

    private suspend fun EmbedBuilder.embedDirectProject(data: DirectProjectData, slug: String) {
        this.title = data.title
        this.url = URLBuilder(MODRINTH_FRONTEND_ENDPOINT).appendPathSegments("project", data.slug).buildString()
        thumbnail {
            this.url = data.iconURL.toString()
        }
        this.description = data.description
        field(
            "Client/Server Side",
            true
        ) { "Client: ${data.clientSide}\nServer: ${data.serverSide}" }
        field("Downloads", true) { data.downloads.toString() }
        field(
            "Last Update",
            true
        ) { "<t:${Instant.parse(data.updated).epochSeconds}>" }
        field("License", true) { data.license.id }
        field("Loaders", true) { getProjectLoaders(data.slug).joinToString("\n") }
        footer {
            this.text = "Modrinth"
        }
    }

    private suspend fun getLicenses(): List<String> {
        val response = client.get(MODRINTH_ENDPOINT) { url { path("v2/tag/license") } }
        val licenses: List<LicenseData> = response.body()
        return licenses.stream().map { it.short }.toList()
    }

    private suspend fun getModCategories(): List<String> {
        val response = client.get(MODRINTH_ENDPOINT) { url { path("v2/tag/category") } }
        val categories: List<CategoryData> = response.body()
        return categories.stream().filter { it.projectType == "mod" }.map { it.name }.toList()
    }

    private suspend fun getModLoaders(): List<String> {
        val response = client.get(MODRINTH_ENDPOINT) { url { path("v2/tag/loader") } }
        val loaders: List<LoaderData> = response.body()
        return loaders.stream().filter { "mod" in it.supportedProjectTypes }.map { it.name }.toList()
    }

    private suspend fun getMinecraftVersions(): List<String> {
        val response = client.get(MODRINTH_ENDPOINT) { url { path("v2/tag/game_version") } }
        val versions: List<VersionData> = response.body()
        return versions.stream().filter { it.versionType == "release" }.map { it.version }.toList()
    }

    private suspend inline fun getProjectLoaders(name: String): List<String> {
        val response = client.get(MODRINTH_ENDPOINT) { url { appendPathSegments("v2", "project", name, "version") } }
        val versions: List<ProjectVersionData> = response.body()
        return versions.stream()
                .flatMap { it.loaders.stream() }.distinct()  // collect all loaders distinctly
                .map { it[0].uppercase() + it.drop(1) }      // Make first character uppercase
                .toList()
    }

    private suspend fun searchModrinthUser(arguments: UserSearchQuery): UserData {
        return client.get(MODRINTH_ENDPOINT) {
            url {
                path("v2/user/${arguments.query}")
            }
        }.body()
    }

    private suspend fun searchModrinth(query: String, limit: Int): SearchResponseData {
        return client.get(MODRINTH_ENDPOINT) {
            url {
                path("v2/search")
                parameter("query", query)
                parameter("limit", limit)
            }
        }.body()
    }

    private suspend fun getProject(slug: String): DirectProjectData {
        return client.get(MODRINTH_ENDPOINT) {
            url {
                path("v2/project/$slug")
            }
        }.body()
    }

    private suspend fun searchModrinthAdvanced(currentFilter: SearchData): SearchResponseData {
        val groups = mutableListOf<List<Facet>>()
        println(currentFilter.facets)
        groups.add(currentFilter.facets.filterValues { it == "version" }.map { Facet("versions", it.key) })
        groups.add(currentFilter.facets.filterValues { it == "license" }.map { Facet("license", it.key) })
        groups.add(
                currentFilter.facets.filterValues { it == "environment" }.map {
                    if (it.key == "client") {
                        Facet("client_side", "required")
                    } else {
                        Facet("server_side", "required")
                    }
                }
        )
        groups.add(
                currentFilter.facets.filterValues { it == "category" || it == "loader" }
                        .map { Facet("categories", it.key) }
        )

        // ugly facet string builder
        val facetString = "[${                  // outer groups begin
            groups
                    .filter { it.isNotEmpty() }     // get rid of empty facet groups
                    .joinToString(",") { group ->   // join facet groups with ,
                        "[${                        // inner group facets begin
                            group
                                    .joinToString(",")  // join facets inside of groups with ,
                        }]"                         // inner group facets end
                    }
        }]"                                     // outer groups end

        return client.get(MODRINTH_ENDPOINT) {
            url {
                path("v2/search")
                parameter("query", currentFilter.query)
                parameter("limit", 5)
                encodedParameters.append("facets", facetString)
            }
        }.body()
    }

    private suspend fun EphemeralStringSelectMenuContext<*>.createFilterMenu(
            filterType: String,
            filterOptions: MutableList<String>,
            currentFilter: SearchData
    ): SearchData {
        respond {
            components {
                ephemeralStringSelectMenu {
                    maximumChoices = filterOptions.size
                    placeholder = "Filter by $filterType"
                    filterOptions.forEach {
                        option(it, it)
                    }
                    action {
                        this.selected.forEach {
                            currentFilter.facets[it] = filterType
                        }
                        respond {
                            content = "Filter adjusted."
                        }
                    }
                }
            }
        }
        return currentFilter // return it so any other functions called can access it
    }

    companion object {
        const val MODRINTH_ENDPOINT = "https://api.modrinth.com/"
        const val MODRINTH_FRONTEND_ENDPOINT = "https://modrinth.com/"
    }

    inner class ModrinthSlugQuery : Arguments() {
        val slug by string {
            name = "slug"
            description = "the slug of the project you want to look up"
        }
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
            val author: String,
            @SerialName("client_side") val clientSide: String,
            @SerialName("server_side") val serverSide: String,
            @SerialName("source_url") val sourceURL: String? = null,
            @SerialName("discord_url") val discordURL: String? = null,
            @SerialName("project_type") val projectType: String,
            @SerialName("latest_version") val latestVersion: String,
            @SerialName("date_modified") val dateModified: String,
            val downloads: Int,
            @SerialName("icon_url") val iconURL: String?,
            val license: String?
    )

    @Serializable
    data class DirectProjectData(
        val slug: String,
        val title: String,
        val description: String,
        val categories: MutableList<String>,
        @SerialName("client_side") val clientSide: String,
        @SerialName("server_side") val serverSide: String,
        @SerialName("source_url") val sourceURL: String? = null,
        @SerialName("discord_url") val discordURL: String? = null,
        @SerialName("project_type") val projectType: String,
        val updated: String,
        val downloads: Int,
        @SerialName("icon_url") val iconURL: String?,
        val license: DirectLicenseData
    )

    @Serializable
    data class DirectLicenseData(
        val id: String,
        val name: String,
        val url: String
    )

    @Serializable
    data class UserData(
            val username: String,
            val name: String?,
            val email: String?,
            val bio: String?,
            val id: String,
            @SerialName("github_id") val githubId: Int?,
            @SerialName("avatar_url") val avatarUrl: String,
            val created: String,
            val role: String
    )

    data class TeamData(
        val role: String,
        @SerialName("team_id") val teamId: String,
        val user: UserData
    )

    @Serializable
    data class ProjectVersionData(
            val name: String,
            val loaders: List<String>
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

    data class Facet(
            val key: String,
            val value: String
    ) {
        override fun toString(): String = "%22$key:$value%22"  // I am so sorry about this
    }
}
