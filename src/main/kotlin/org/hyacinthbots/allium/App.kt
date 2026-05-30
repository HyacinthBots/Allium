package org.hyacinthbots.allium

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.component.section
import dev.kord.rest.builder.component.textDisplay
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.container
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.messageFlags
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
					val kord = getKoin().get<Kord>()
					messageFlags {
						+MessageFlag.IsComponentsV2
					}
					container {
						textDisplay("""# Allium
							|Hello! I am Allium, a Minecraft modding focused bot.
						""".trimMargin())
						separator(SeparatorSpacingSize.Large)
						textDisplay("""## General info
							|- Birthday (initial date of idea): September 6th, 2022
							|- Main Developers: Jansel
							|
							|Contributors:
							|[Jansel](https://github.com/NotJansel)
                       		|[TemperΘΔ](https://github.com/StonkDragon)
                       		|[triphora](https://github.com/triphora)
                       		|[NoComment](https://github.com/NoComment1105)
                       		|[Scrumplex](https://github.com/Scrumplex)
					  		|[gdude](https://github.com/gdude2002)
						""".trimMargin())
						separator(SeparatorSpacingSize.Large)
						textDisplay("""## Bot & Build info
							|Guilds: ${kord.guilds.count()}
							|Build: $BUILD
							|Built on: <t:$BUILDTIME>
							|Next Update: ${getRandomUpdateMessage()}
						""".trimMargin())
						separator(SeparatorSpacingSize.Large)
						actionRow {
							linkButton("https://github.com/HyacinthBots/Allium") {
								label = "Source Code"
							}
							linkButton("https://discord.com/api/oauth2/authorize?client_id=1013045351852298280&permissions=347136&scope=bot%20applications.commands") {
								label = "Add me to your Server"
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
			add(::LinkListener)
			extMappings {}
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
