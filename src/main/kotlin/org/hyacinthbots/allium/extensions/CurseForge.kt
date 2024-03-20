package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.CURSEFORGE_API_KEY
import java.util.*

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
                    arguments.query.replace(" ", "%20")
                    val response = searchCurseForge(arguments.query, arguments.limit)
                    when (response.pagination.totalCount) {
                        1 -> respond {
                                embed {
                                    embedProject(response.data[0])
                                }
                        }

                        0 -> respond { content = "No results found." }

                        else -> respondingPaginator {
                            for ((i, _) in response.data.withIndex()) {
                                page {
                                    embedProject(response.data[i])
                                }
                            }
                            owner = user
                            timeoutSeconds = 180
                            locale = Locale.ENGLISH
                        }.send()
                    }
                }
            }
        }
    }

    private suspend fun searchCurseForge(query: String, limit: Int): SearchResponse {
        return client.get(CURSEFORGE_ENDPOINT) {
            url {
                path("v1/mods/search")
                parameter("gameId", 432)
                parameter("classId", 6)
                parameter("searchFilter", query)
                parameter("pageSize", limit)
                parameter("sortField", 2)
                parameter("sortOrder", "desc")
            }
            headers {
                append("x-api-key", CURSEFORGE_API_KEY)
                append("Accept", "application/json")
            }
        }.body()
    }

    private fun EmbedBuilder.embedProject(data: Mod) {
        this.title = data.name
        this.url = URLBuilder(CURSEFORGE_FRONTEND_ENDPOINT).appendPathSegments("minecraft/mc-mods", data.slug).buildString()
        thumbnail {
            this.url = data.logo.url
        }
        this.description = data.summary
        field("Downloads", true) { data.downloadCount.toString() }
        field("Author", true) { data.authors.first().name }
        field(
            "Last Update",
            true
        ) { "<t:${Instant.parse(data.dateModified).epochSeconds}>" }
        footer {
            this.text = "CurseForge | ${data.authors.first().name}"
        }
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
        val limit by defaultingInt {
            name = "limit"
            description = "Set the Limit of Search results"
            maxValue = 50
            defaultValue = 5
            minValue = 1
        }
    }

    @Serializable
    data class SearchResponse(
        val data: MutableList<Mod>,
        val pagination: Pagination
    )

    @Serializable
    data class Mod(
        val id: Int,
        val name: String,
        val slug: String,
        val summary: String,
        val downloadCount: Long,
        val authors: MutableList<ModAuthor>,
        val logo: ModAsset,
        val dateModified: String,
        val allowModDistribution: Boolean?
    )

    @Serializable
    data class ModAuthor(
        val id: Int,
        val name: String,
        val url: String
    )

    @Serializable
    data class ModAsset(
        val id: Int,
        val modId: Int,
        val title: String,
        val description: String,
        val thumbnailUrl: String,
        val url: String
    )

    @Serializable
    data class Pagination(
        val index: Int,
        val pageSize: Int,
        val resultCount: Int,
        val totalCount: Int
    )
}
