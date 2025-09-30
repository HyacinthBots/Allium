package org.hyacinthbots.allium.extensions

import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.i18n.Translations
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.CURSEFORGE_API_KEY
import java.util.*
import kotlin.time.ExperimentalTime

class CurseForge : Extension() {
	override val name = "CurseForge"

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json { ignoreUnknownKeys = true })
		}
		install(UserAgent) {
			agent = "hyacinthbots/allium/$BUILD (contact@jansel.dev)"
		}
	}

	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Curseforge.Command.name
			description = Translations.Curseforge.Command.description
			publicSubCommand(::CurseForgeSearchQuery) {
				name = Translations.Curseforge.Command.Search.name
				description = Translations.Curseforge.Command.Search.description
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

	@OptIn(ExperimentalTime::class)
	private fun EmbedBuilder.embedProject(data: Mod) {
		this.title = data.name
		this.url =
			URLBuilder(CURSEFORGE_FRONTEND_ENDPOINT).appendPathSegments("minecraft/mc-mods", data.slug).buildString()
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
			name = Translations.Curseforge.Arguments.Query.name
			description = Translations.Curseforge.Arguments.Query.description
		}
		val limit by defaultingInt {
			name = Translations.Curseforge.Arguments.Limit.name
			description = Translations.Curseforge.Arguments.Limit.description
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
