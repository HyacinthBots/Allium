package de.notjansel.sbbot.extensions

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@OptIn(KordPreview::class)
class CurrentMayor : Extension() {
    override val name = "current"

    override suspend fun setup() {
        publicSlashCommand() {
            name = "current"
            description = "Get the current Mayor"
            val kord = this@CurrentMayor.kord
            guild(TEST_SERVER_ID)
            action {
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hypixel.net/resources/skyblock/election"))
                    .build()
                val response = withContext(Dispatchers.IO) {
                    client.send(request, HttpResponse.BodyHandlers.ofString())
                }
                val gson: JsonObject = JsonParser.parseString(response.body().replace(Regex("§[0-9a-fA-Fk-oK-OrR]"), "")).asJsonObject
                val embed: EmbedBuilder = EmbedBuilder()
                embed.title = "Current mayor: ${gson["mayor"].asJsonObject.get("name").toString().replace("\"", "")}"
                embed.description = "The mayor's perks are listed below."
                val footer: EmbedBuilder.Footer = EmbedBuilder.Footer()
                footer.text = "Last Update: "
                embed.footer = footer
                embed.timestamp = Instant.fromEpochMilliseconds(gson["lastUpdated"].asLong)
                for (perk in gson["mayor"].asJsonObject.getAsJsonArray("perks")) {
                    embed.field(perk.asJsonObject.get("name").asString, true) { perk.asJsonObject.get("description").asString }
                }
                respond {
                    embeds.add(embed)
                }
            }
        }
    }
}
