package org.hyacinthbots.allium.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Instant
import org.hyacinthbots.allium.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Modrinth Commands. Written in pure pain.
 * @author NotJansel
 * @since 0.1.3
 */

@OptIn(KordPreview::class)
class Modrinth : Extension() {
    override val name = "modrinth"
    override suspend fun setup() {
        publicSlashCommand {
            name = "modrinth"
            description = "What is Modrinth?"
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
                        "https://api.modrinth.com/v2/search?limit=${arguments.limit}&query=${arguments.query}"
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
                        val versionsreq = webRequest("https://api.modrinth.com/v2/project/${hit["slug"].asString}/versions")
                        val versionsres = JsonParser.parseString(versionsreq.body()).asJsonObject.asJsonArray
                        var m: MutableSet<String> = HashSet()
                        for ((index, vers_hit) in versionsres.withIndex()) {
                            index.toString() // leave this as else this doesn't work (please I don't want to count up manually)
                            var loaders = ArrayList<String>()
                            for (loader in vers_hit.asJsonObject.getAsJsonArray("loaders")) {
                                loaders.add(loader.asString)
                            }
                            m.addAll(loaders)
                        }
                        var strLoaders = ""
                        m.forEach {
                            strLoaders += this.toString() + "\n"
                        }
                        strLoaders.dropLast(2)
                        respond {
                            embed {
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
                        }
                        return@action
                    }
                    respondingPaginator {
                        for ((i, _) in hits.withIndex()) {
                            val hit: JsonObject = hits.get(i).asJsonObject
                            val versionsreq = webRequest("https://api.modrinth.com/v2/project/${hit["slug"].asString}/versions")
                            val versionsres = JsonParser.parseString(versionsreq.body()).asJsonObject.asJsonArray
                            var m: MutableSet<String> = HashSet()
                            for ((index, vers_hit) in versionsres.withIndex()) {
                                index.toString() // leave this as else this doesn't work (please I don't want to count up manually)
                                var loaders = ArrayList<String>()
                                for (loader in vers_hit.asJsonObject.getAsJsonArray("loaders")) {
                                    loaders.add(loader.asString)
                                }
                                m.addAll(loaders)
                            }
                            var strLoaders = ""
                            m.forEach {
                                strLoaders += this.toString() + "\n"
                            }
                            strLoaders.dropLast(2)
                            page {
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
                        }
                        timeoutSeconds = 60
                        locale = Locale.ENGLISH
                    }.send()
                }
            }
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

    inner class UserSearchQuery : Arguments() {
        val query by string {
            name = "query"
            description = "User to search up"
            require(true)
        }
    }
}
