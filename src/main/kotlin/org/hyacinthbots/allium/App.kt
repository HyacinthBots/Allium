package org.hyacinthbots.allium

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.modules.extra.mappings.extMappings
import me.shedaniel.linkie.utils.readText
import org.hyacinthbots.allium.extensions.*
import org.hyacinthbots.allium.utils.ENVIRONMENT
import org.hyacinthbots.allium.utils.TOKEN
import org.hyacinthbots.docgenerator.docsGenerator
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import java.util.*
import kotlin.io.path.Path

var splashes = JsonArray()
var updatemessages = JsonArray()

suspend fun main() {
    splashes = JsonParser.parseString({}.javaClass.getResource("/splashes.json")?.readText()).asJsonArray
    updatemessages = JsonParser.parseString({}.javaClass.getResource("/updatemessage.json")?.readText()).asJsonArray
    val bot = ExtensibleBot(TOKEN) {
        extensions {
            add(::EventHooks)
            add(::Modrinth)
            add(::About)
            add(::PresenceUpdater)
            add(::StatusPing)
            add(::LogUploading)
            extMappings {  }
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
        }
    }
    bot.start()
}
