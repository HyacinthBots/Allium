package de.notjansel.sbbot.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import de.notjansel.sbbot.TEST_SERVER_ID
import de.notjansel.sbbot.utils.*
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Instant

@OptIn(KordPreview::class)
class Modrinth : Extension() {
    override val name = "modrinth"
    override suspend fun setup() {
        publicSlashCommand {
            name = "modrinth"
            description = "What is Modrinth?"
            guild(TEST_SERVER_ID)
            publicSubCommand(::UserSearchQuery) {
                name = "user"
                description = "Search for a User"
                guild(TEST_SERVER_ID)
                action {
                    val url = "https://api.modrinth.com/v2/user/${arguments.query}"
                    if (arguments.query ==  "") {
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
                            title = user["name"].asString
                            description = user["bio"].asString
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
                guild(TEST_SERVER_ID)
                action {
                    val url = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:mod%22]]&query=${arguments.query}"
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
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
                        respond {
                            embed {
                                this.title = hit["title"].asString
                                this.url = "https://modrinth.com/mod/${hit["slug"]}"
                                thumbnail {
                                    this.url = hit["icon_url"].asString
                                }
                                this.description = hit["description"].asString
                                field("Latest Version", true) { hit["latest_version"].asString }
                                field("Client/Server Side", true) { "Client: ${hit["client_side"].asString}\nServer: ${hit["server_side"].asString}" }
                                field("Downloads", true) { hit["downloads"].asString }
                                field("Author", true) { hit["author"].asString }
                                field("Last Update", true) { "<t:${Instant.parse(hit["date_modified"].asString).epochSeconds}>" }
                                field("License", true) { hit["license"].asString }
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
                            page {
                                this.title = hit["title"].asString
                                this.url = "https://modrinth.com/mod/${hit["slug"]}"
                                thumbnail {
                                    this.url = hit["icon_url"].asString
                                }
                                this.description = hit["description"].asString
                                field("Latest Version", true) { hit["latest_version"].asString }
                                field("Client/Server Side", true) { "Client: ${hit["client_side"].asString}\nServer: ${hit["server_side"].asString}" }
                                field("Downloads", true) { hit["downloads"].asString }
                                field("Author", true) { hit["author"].asString }
                                field("Last Update", true) { "<t:${Instant.parse(hit["date_modified"].asString).epochSeconds}>" }
                                field("License", true) { hit["license"].asString }
                                footer {
                                    this.text = "Modrinth | ${hit["author"].asString}"
                                }
                            }
                        }
                    }
                }
            }
            publicSubCommand(::ModrinthSearchQuery) {
                name = "resourcepack"
                description = "Search for a resource pack"
                guild(TEST_SERVER_ID)
                action {
                    val url = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:resourcepack%22]]&query=${arguments.query}"
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
                    respondEphemeral { content = "Not implemented yet." }
                }
            }
            publicSubCommand(::ModrinthSearchQuery) {
                name = "modpack"
                description = "Search for a Modpack"
                guild(TEST_SERVER_ID)
                action {
                    val url = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:modpack%22]]&query=${arguments.query}"
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
                    respondEphemeral { content = "Not implemented yet." }
                }
            }
        }
    }
    inner class ModrinthSearchQuery : Arguments() {
        val query by defaultingString {
            name = "query"
            description = "Query to search"
            defaultValue = ""
        }
        val limit by defaultingInt {
            name = "limit"
            description = "limit search results"
            defaultValue = 5
        }
    }
    inner class UserSearchQuery : Arguments() {
        val query by defaultingString {
            name = "query"
            description = "User to search up"
            defaultValue = ""
            require(true)
        }
    }
}
