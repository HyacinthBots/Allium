package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.CURSEFORGE_API_TOKEN

class CurseForge : Extension() {
    override val name = "CurseForge"

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
            name = "curseforge"
            description = "Search for mods on CurseForge"
            publicSubCommand(::CurseForgeSearchQuery) {
                name = "search"
                description = "Search for mods on CurseForge"
                action {
                    respond { searchCurseForge("sodium") }
                }
            }
        }
    }

    private suspend fun searchCurseForge(query: String): String {
        return client.get(CURSEFORGE_ENDPOINT) {
            url {
                path("v1/search")
                parameters.append("gameId", "432")
                parameters.append("classId", "6")
                parameters.append("slug", query)
                parameters.append("pageSize", "5")
                parameters.append("sortField", "2")
            }
            headers {
                append("x-api-key", CURSEFORGE_API_TOKEN)
            }
        }.body()
    }

    companion object {
        const val CURSEFORGE_ENDPOINT = "https://api.curseforge.com/"
        const val CURSEFORGE_FRONTEND_ENDPOINT = "https://www.curseforge.com/"
    }

    inner class CurseForgeSearchQuery : Arguments() {
        val query by string {
            name = "query"
            description = "The query to search for"
        }
    }
}
