package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CurrentElection : Extension() {
    override val name = "election"

    override suspend fun setup() {
        publicSlashCommand() {
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
                val json = Json.parseToJsonElement(response.body())
                if (json.jsonObject["current"] == null) {
                    respond {
                        content = "There is no Election running."
                    }
                    return@action
                }
                val formattedjson = Json.parseToJsonElement(json.toString().replace(Regex("§[0-9a-fA-Fk-oK-OrR]"), ""))
                println(formattedjson)
                val embed: EmbedBuilder = EmbedBuilder()
                embed.title = "Current Election"
                embed.description = "Here are the current Election Candidates and their Perks:"
                embed.field("Mayor 1", true) { "List Perks of Mayor 1 here" }
                embed.field("Mayor 2", true) { "List Perks of Mayor 2 here" }
                embed.field("Mayor 3", true) { "List Perks of Mayor 3 here" }
                embed.field("Mayor 4", true) { "List Perks of Mayor 4 here" }
                embed.field("Mayor 5", true) { "List Perks of Mayor 5 here" }
                respond {
                    embeds.add(embed)
                }
            }
        }
    }
}
