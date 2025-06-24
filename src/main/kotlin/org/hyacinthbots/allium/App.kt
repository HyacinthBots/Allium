package org.hyacinthbots.allium

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.modules.func.mappings.extMappings
import kotlinx.coroutines.flow.count
import org.hyacinthbots.allium.extensions.*
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.BUILDTIME
import org.hyacinthbots.allium.utils.TOKEN
import org.hyacinthbots.allium.utils.database
import org.hyacinthbots.allium.utils.getRandomUpdateMessage
import org.slf4j.LoggerFactory
import java.util.*

var updatemessages = JsonArray()

@OptIn(PrivilegedIntent::class)
suspend fun main() {
	val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
	val rootLogger = loggerContext.getLogger("org.mongodb.driver")
	rootLogger.level = Level.OFF
	updatemessages = JsonParser.parseString({}.javaClass.getResource("/updatemessage.json")?.readText()).asJsonArray
	val bot = ExtensibleBot(TOKEN) {
		database(true)
		about {
			ephemeral = false
			general {
				message {
					embed {
						description = "Hello! I am Allium, a Minecraft modding focused bot."
						title = "Allium"

						field {
							name = "Birthday (initial date of idea)"
							value = "September 6th, 2022"
						}
						field {
							name = " Main Developers"
							value = "Jansel"
						}
						field {
							name = "Contributors"
							value = """[Jansel](https://github.com/NotJansel)
                          	|[TemperΘΔ](https://github.com/StonkDragon)
                          	|[triphora](https://github.com/triphora)
                          	|[NoComment](https://github.com/NoComment1105)
                          	|[Scrumplex](https://github.com/Scrumplex)
						  	|[gdude](https://github.com/gdude2002)
                			""".trimMargin()
						}
						field {
							val kord = getKoin().get<Kord>()
							name = "Guilds"
							value = kord.guilds.count().toString()
						}
						field {
							name = "Build"
							value = BUILD.toString()
						}
						field {
							name = "This build was created on:"
							value = "<t:$BUILDTIME>"
						}
						field {
							name = "Next update?"
							value = getRandomUpdateMessage()
						}
					}
					actionRow {
						linkButton("https://github.com/HyacinthBots/Allium") {
							label = "Source Code"
						}
						linkButton("https://discord.com/api/oauth2/authorize?client_id=1013045351852298280&permissions=347136&scope=bot%20applications.commands") {
							label = "Invite"
						}
						linkButton("https://github.com/HyacinthBots/.github/blob/main/terms-of-service.md") {
							label = "Terms of Service"
						}
						linkButton("https://github.com/HyacinthBots/Allium/tree/root/docs/privacy-policy.md") {
							label = "Privacy Policy"
						}
					}
				}
			}
		}

		extensions {
			add(::EventHooks)
			add(::CurseForge)
			add(::Modrinth)
			add(::PresenceUpdater)
			add(::ClientJarUpdater)
			add(::StatusPing)
			add(::LogUploading)
			add(::Config)
			extMappings { }
		}
		intents {
			+Intent.MessageContent
		}
		i18n {
			applicationCommandLocale(SupportedLocales.ENGLISH)
		}
		/*
		docsGenerator {
			enabled = true
			fileFormat = SupportedFileFormat.MARKDOWN
			filePath = Path("./docs/commands.md")
			environment = ENVIRONMENT
			useBuiltinCommandList = true
			commandTypes = CommandTypes.ALL
			botName = "Allium"
		}
		 */
	}
	bot.start()
}
