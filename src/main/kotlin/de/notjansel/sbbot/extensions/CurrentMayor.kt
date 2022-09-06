package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.common.annotation.KordPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@OptIn(KordPreview::class)
class CurrentMayor : Extension() {
    override val name = "mayor"

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
                val json = Json.parseToJsonElement(response.body())
                println(json)
                respond {
                    content = json.jsonObject["mayor"]?.jsonObject?.get("name")?.toString()?.replace("\"", "")
                }
            }
        }
    }
}
