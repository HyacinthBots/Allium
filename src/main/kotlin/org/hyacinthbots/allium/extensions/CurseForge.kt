package org.hyacinthbots.allium.extensions

import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.component.section
import dev.kord.rest.builder.component.textDisplay
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.container
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.messageFlags
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.utils.respond
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.request
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.database.collections.ConfigCollection
import org.hyacinthbots.allium.database.collections.LinkListenerCollection
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

		event<MessageCreateEvent> {
			check {
				anyGuild()
				if (ConfigCollection().linkListenerType(event.guildId!!) == "whitelist") {
					failIfNot {
						LinkListenerCollection().checkIfChannelIsInWhitelist(
							event.message.getGuild().id,
							event.message.channelId
						)
					}
				} else if (ConfigCollection().linkListenerType(event.guildId!!) == "blacklist") {
					failIf {
						LinkListenerCollection().checkIfChannelIsInBlacklist(
							event.message.getGuild().id,
							event.message.channelId
						)
					}
				}
			}
			action {
				val message = event.message.content
				val regex = Regex("https?://(?:www\\.)?curseforge\\.com/minecraft/(?:mc-mods|modpacks|shaders|bukkit-plugins|mc-addons|worlds|texture-packs|customization|data-packs)/([^/\\s]+)")
				val match = regex.find(message)
				if (match != null) {
					val slug = match.groupValues[1]
					val response = searchCurseForge(slug, 1)
					val project = response.data.firstOrNull { it.slug == slug } ?: return@action

					event.message.respond {
						messageFlags {
							+MessageFlag.IsComponentsV2
						}
						container {
							section {
								textDisplay("# ${project.name}")
								thumbnailAccessory {
									url = project.logo.url
									description = "${project.name} logo"
								}
								textDisplay(project.summary)
							}
							separator(SeparatorSpacingSize.Large)
							textDisplay("""Downloads: ${project.downloadCount}
								|Latest supported Minecraft version: N/A due to bad sorting on Curseforge's End
								|Authors: ${project.authors.joinToString(", ") { it.name }}
								|Last Update: <t:${Instant.parse(project.dateModified).epochSeconds}>
							""".trimMargin())
							separator(SeparatorSpacingSize.Large)
							actionRow {
								if (!project.links.sourceUrl.isNullOrBlank()) {
									linkButton(project.links.sourceUrl) {
										label = "Project Source Code"
									}
								}
								if (!project.links.wikiUrl.isNullOrBlank()) {
									linkButton(project.links.wikiUrl) {
										label = "Project Wiki"
									}
								}
								if (!project.links.issuesUrl.isNullOrBlank()) {
									linkButton(project.links.issuesUrl) {
										label = "Project Issues"
									}
								}
								linkButton(project.links.websiteUrl) {
									label = "CurseForge Page"
								}
							}
						}
					}
				}
			}
		}
	}

	private suspend fun searchCurseForge(query: String, limit: Int): SearchResponse {
		val a = client.get(CURSEFORGE_ENDPOINT) {
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
		}
		KotlinLogging.logger("DEBUG").info { a.request.url.toString() }
		return a.body()
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
		val data: List<Mod>,
		val pagination: Pagination
	)

	@Serializable
	data class Mod(
		val id: Int,
		val name: String,
		val slug: String,
		val links: ModLinks,
		val summary: String,
		val downloadCount: Long,
		val authors: List<ModAuthor>,
		val logo: ModAsset,
		val dateModified: String,
		val latestFiles: List<ModFile> = emptyList()
	)

	@Serializable
	data class ModLinks(
		val websiteUrl: String,
		val wikiUrl: String? = null,
		val issuesUrl: String? = null,
		val sourceUrl: String? = null
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
	data class ModFile(
		val id: Int,
		val displayName: String,
		val fileName: String,
		val fileDate: String,
		val gameVersions: List<String>
	)

	@Serializable
	data class Pagination(
		val index: Int,
		val pageSize: Int,
		val resultCount: Int,
		val totalCount: Int
	)
}
