package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CurrentElection : Extension() {
    override val name = "mayor"
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
                val embed: EmbedBuilder = EmbedBuilder()
                embed.title = "Current Election"
                embed.description = "Here are the current Election Candidates and their Perks:"
                embed.field("Mayor 1", true) { "List Perks of Mayor 1 here" }
                embed.field("Mayor 2", true) { "List Perks of Mayor 2 here" }
                embed.field("Mayor 3", true) { "List Perks of Mayor 3 here" }
                embed.field("Mayor 4", true) { "List Perks of Mayor 4 here" }
                embed.field("Mayor 5", true) { "List Perks of Mayor 5 here" }
                // first tries of a paginator, need to read docs for this.
                var paginatorBuilder: PaginatorBuilder = PaginatorBuilder()
                val mayor1: Page = Page("", (suspend { EmbedBuilder })
                paginatorBuilder.pages.addPage(mayor1)
                respond {
                    embeds.add(embed)
                }
            }
        }
    }
}
