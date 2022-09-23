package de.notjansel.sbbot.extensions

import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.notjansel.sbbot.TEST_SERVER_ID
import de.notjansel.sbbot.utils.*
import dev.kord.rest.builder.message.create.embed

class Modrinth : Extension() {
    override val name = "modrinth"
    override suspend fun setup() {
        publicSlashCommand {
            name = "modrinth"
            description = "modrinth related commands"
            guild(TEST_SERVER_ID)
            publicSubCommand(::ModrinthSearchQuery) {
                name = "user"
                description = "Search for a User"
                action {
                    val url: String = "https://api.modrinth.com/v2/user/${arguments.query}"
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
                action {
                    val url: String = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:mod%22]]&query=${arguments.query}"
                    if (arguments.query == "") {
                        respond { content = "No query was given, aborting search." }
                        return@action
                    }
                    respondEphemeral { content = "Not implemented yet." }
                }
            }
            publicSubCommand(::ModrinthSearchQuery) {
                name = "resourcepack"
                description = "Search for a resource pack"
                action {
                    val url: String = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:resourcepack%22]]&query=${arguments.query}"
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
                action {
                    val url: String = "https://api.modrinth.com/v2/search?limit=${arguments.limit}&facets=[[%22project_type:modpack%22]]&query=${arguments.query}"
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
            require(true)
        }
        val limit by defaultingInt {
            name = "limit"
            description = "limit search results"
            defaultValue = 5
            require(false)
        }
    }
}
