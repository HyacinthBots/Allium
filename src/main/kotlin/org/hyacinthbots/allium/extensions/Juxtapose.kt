package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.attachment
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.utils.BUILD

class Juxtapose : Extension() {
    val allowedContentTypes: Array<String> = arrayOf("image/jpeg", "image/png", "image/webp")

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(UserAgent) {
            agent = "hyacinthbots/allium/$BUILD (github@notjansel.de)"
        }
    }

    override val name = "juxtapose"
    override suspend fun setup() {
        publicSlashCommand {
            name = "juxtapose"
            publicSubCommand(::uploadArgs) {
                name = "upload"
                description = "Create a juxtapose by uploading the Images directly"
                action {
                    if (!allowedContentTypes.contains(arguments.leftImage.contentType) || !allowedContentTypes.contains(
                            arguments.rightImage.contentType
                        )
                    ) {
                        respondEphemeral {
                            content = "One of the Attachments does not match the needed content types."
                        }
                        return@action
                    }
                    client.post("https://juxtapose.knightlab.com/juxtapose/create") {
                        this.setBody {
                            "{images:[{\"src\":\"${arguments.leftImage.url}\", },{}]}"
                            attributes
                        }
                    }
                }
            }
        }
    }

    inner class uploadArgs : Arguments() {
        val leftImage by attachment {
            name = "Left image"
            description = "Left image of the juxtapose"
        }
        val rightImage by attachment {
            name = "Right image"
            description = "Right image of the juxtapose"
        }
        val leftLabel by optionalString {
            name = "Left label"
            description = "The label to show on the left image"
        }
        val rightLabel by optionalString {
            name = "Right label"
            description = "The label to show on the right image"
        }
        val isVertical by optionalBoolean {
            name = "isVertical"
            description = ""
        }
    }

    @Serializable
    data class postjson(
        val images: MutableList<image>
    )

    @Serializable
    data class image(
        val src: String,
        val label: String,
        val credit: String = ""
    )
}
