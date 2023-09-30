package org.hyacinthbots.allium.utils

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.collections.LogUploadingCollection
import org.hyacinthbots.allium.database.collections.MetaCollection
import org.hyacinthbots.allium.updatemessages
import org.koin.dsl.bind
import java.io.File

private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(UserAgent) {
        agent = "hyacinthbots/allium/$BUILD (github@notjansel.de)"
    }
}

fun getRandomSplash(): String {
    Runtime.getRuntime().exec("jar xf client.jar assets/minecraft/texts/splashes.txt")
    val entries = File("./assets/minecraft/texts/splashes.txt").readLines().count()
    val entry = (0 until entries).random()
    return File("./assets/minecraft/texts/splashes.txt").readLines()[entry]
}

suspend inline fun ExtensibleBotBuilder.database(migrate: Boolean) {
    val db = Database()

    hooks {
        beforeKoinSetup {
            loadModule {
                single { db } bind Database::class
            }

            loadModule {
                single { LogUploadingCollection() } bind LogUploadingCollection::class
                single { MetaCollection() } bind MetaCollection::class
            }

            if (migrate) {
                runBlocking { db.migrate() }
            }
        }
    }
}

fun getRandomUpdateMessage(): String {
    val entries = updatemessages.count()
    val entry = (0 until entries).random()
    return updatemessages.get(entry).asString
}

suspend fun downloadLatestClientJar() {
    val response = client.get("https://meta.prismlauncher.org/v1/net.minecraft/")
    val metaResponse: MetaResponse = response.body()
    val latestVersion: Version = metaResponse.versions.first()
    val version = client.get("https://meta.prismlauncher.org/v1/net.minecraft/${latestVersion.version}.json")
    val versionResponse: VersionData = version.body()
    val outputFile = File("./client.jar")
    val out: ByteReadChannel = client.get {
        url(versionResponse.mainJar.downloads.artifact.url)
        method = HttpMethod.Get
    }.bodyAsChannel()
    out.copyAndClose(outputFile.writeChannel())
}

@Serializable
data class MetaResponse(
        val versions: MutableList<Version>
)

@Serializable
data class Version(
        val version: String
)

@Serializable
data class VersionData(
        val mainJar: MainJar
)

@Serializable
data class MainJar(
        val downloads: Downloads
)

@Serializable
data class Downloads(
        val artifact: Artifact
)

@Serializable
data class Artifact(
        val url: String
)
