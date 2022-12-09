package org.hyacinthbots.allium.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hyacinthbots.allium.splashes
import org.hyacinthbots.allium.updatemessages
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

suspend inline fun webRequest(url: String): HttpResponse<String> {
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI(url))
        .build()
    val response = withContext(Dispatchers.IO) {
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
    return response
}

fun getRandomSplash(): String {
    val entries = splashes.count()
    val entry = (0 until entries).random()
    return splashes.get(entry).asString
}

fun getRandomUpdateMessage(): String {
    val entries = updatemessages.count()
    val entry = (0 until entries).random()
    return updatemessages.get(entry).asString
}

const val BUILD: String = "@version@"
