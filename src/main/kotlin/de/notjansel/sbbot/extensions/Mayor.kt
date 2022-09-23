package de.notjansel.sbbot.extensions

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.math.roundToInt

class Mayor : Extension() {
    override val name = "mayor"
    override suspend fun setup() {
        publicSlashCommand {
            name = "mayor"
            description = "mayor related commands!"
            guild(TEST_SERVER_ID)

            publicSubCommand {
                name = "current"
                description = "Get the current Mayor"
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

            publicSubCommand {
                name = "election"
                description = "Shows the current election"
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
                    if (!gson["success"].asBoolean) {
                        respond {
                            content = "There was a Hypixel API Error. Please try again later."
                        }
                        return@action
                    }
                    if (gson["current"] == null) {
                        val embed: EmbedBuilder = EmbedBuilder()
                        embed.title = "Current Election"
                        embed.description = "There is no Election running."
                        val footer: EmbedBuilder.Footer = EmbedBuilder.Footer()
                        footer.text = "Last Update: "
                        embed.footer = footer
                        embed.timestamp = Instant.fromEpochMilliseconds(gson["lastUpdated"].asLong)
                        respond {
                            embeds.add(embed)
                        }
                        return@action
                    }
                    val embed: EmbedBuilder = EmbedBuilder()
                    embed.title = "Current Election"
                    var totalvotes = 0
                    embed.description = "Here are the current Election Candidates for year ${gson["current"].asJsonObject.get("year").asInt} and their Perks:"
                    for (mayor in gson["current"].asJsonObject.getAsJsonArray("candidates")) {
                        totalvotes += mayor.asJsonObject["votes"].asInt
                    }
                    for (mayor in gson["current"].asJsonObject.getAsJsonArray("candidates")) {
                        var perks: String = ""
                        for (perk in mayor.asJsonObject.get("perks").asJsonArray) {
                            perks += perk.asJsonObject["name"].asString + "\n"
                            perks += "*" + perk.asJsonObject["description"].asString + "*\n\n"
                        }
                        perks += "\n**Votes**: " + mayor.asJsonObject["votes"].asInt.toString() + " (${(mayor.asJsonObject["votes"].asInt.toDouble() / totalvotes.toDouble() * 100).roundToInt()}%)"
                        embed.field(mayor.asJsonObject.get("name").asString, true) { perks }
                    }
                    val footer: EmbedBuilder.Footer = EmbedBuilder.Footer()
                    footer.text = "Last Update: "
                    embed.footer = footer
                    embed.timestamp = Instant.fromEpochMilliseconds(gson["lastUpdated"].asLong)
                    respond {
                        embeds.add(embed)
                    }
                }
            }
        }
    }
}