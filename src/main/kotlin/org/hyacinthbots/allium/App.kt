package org.hyacinthbots.allium

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.modules.extra.mappings.extMappings
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.shedaniel.linkie.utils.readText
import org.hyacinthbots.allium.extensions.*
import org.hyacinthbots.allium.utils.ENVIRONMENT
import org.hyacinthbots.allium.utils.TOKEN
import org.hyacinthbots.allium.utils.database
import org.hyacinthbots.docgenerator.docsGenerator
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import java.util.*
import kotlin.io.path.Path

var splashes = JsonArray()
var updatemessages = JsonArray()

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    updatemessages = JsonParser.parseString({}.javaClass.getResource("/updatemessage.json")?.readText()).asJsonArray
    val bot = ExtensibleBot(TOKEN) {
        database(true)
        extensions {
            add(::EventHooks)
            add(::CurseForge)
            add(::Modrinth)
            add(::About)
            add(::PresenceUpdater)
            add(::ClientJarUpdater)
            add(::StatusPing)
            add(::LogUploading)
            add(::Config)
            extMappings {  }
        }
        intents {
            +Intent.MessageContent
        }
        i18n {
            defaultLocale = Locale.ENGLISH
        }
        docsGenerator {
            enabled = true
            fileFormat = SupportedFileFormat.MARKDOWN
            filePath = Path("./docs/commands.md")
            environment = ENVIRONMENT
            useBuiltinCommandList = true
            commandTypes = CommandTypes.ALL
            botName = "Allium"
        }
    }
    bot.start()
}
