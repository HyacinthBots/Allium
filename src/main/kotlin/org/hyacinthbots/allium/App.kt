package org.hyacinthbots.allium

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.modules.extra.mappings.extMappings
import me.shedaniel.linkie.utils.readText
import org.hyacinthbots.allium.extensions.*
import org.hyacinthbots.allium.utils.TOKEN
import java.util.*

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
            add(::Juxtapose)
            extMappings {  }
        }
        i18n {
            defaultLocale = Locale.ENGLISH
        }
    }
    bot.start()
}
