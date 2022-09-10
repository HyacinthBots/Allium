package de.notjansel.sbbot.extensions

import com.google.gson.JsonParser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.newbazaar
import de.notjansel.sbbot.oldbazaar
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class BazaarChecker : Extension() {
    override val name = "BazzarChecker"
    override suspend fun setup() {
        publicSlashCommand {
            name = "bazaarchecker"
            description = "Checks every 2 Minutes if Bazaar is enabled, and sends a message if yes or not."
            action {
                val kord = de.notjansel.sbbot.kord
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hypixel.net/resources/skyblock/election"))
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                oldbazaar = newbazaar
                newbazaar = JsonParser.parseString(response.body()).asJsonObject.getAsJsonObject("products").asJsonObject
                var responsemessage: String = ""
                responsemessage = if (newbazaar == oldbazaar) {
                    "Bazaar is the same as before. **Bazaar Disabled**"
                } else {
                    "Bazaar is not the same as before. **Bazaar Enabled**"
                }
                respond {
                    content = responsemessage
                }
            }
        }
    }
}
