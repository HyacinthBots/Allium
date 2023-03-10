package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.DISCORD_PINK
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.download
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hyacinthbots.allium.database.collections.LogUploadingCollection
import org.hyacinthbots.allium.utils.botHasChannelPerms
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

class LogUploading : Extension() {

    override val name = "log-uploading"

    /** The file extensions that will be read and decoded by this system. */
    private val logFileExtensions = setOf("log", "gz", "txt")

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                anyGuild()
                failIf {
                    event.message.author.isNullOrBot()
                    event.message.getChannelOrNull() !is MessageChannel
                }
                LogUploadingCollection().checkIfChannelIsInWhitelist(event.guildId!!, event.message.channelId)
                // I hate NullPointerExceptions. This is to prevent a null pointer exception if the message is a Pk one.
                if (channelFor(event) == null) return@check
                botHasChannelPerms(Permissions(Permission.SendMessages, Permission.EmbedLinks))
            }
            action {
                val eventMessage = event.message.asMessageOrNull() // Get the message
                val uploadChannel = eventMessage.channel.asChannelOrNull()
                val eventMember = event.member

                eventMessage.attachments.forEach { attachment ->
                    val attachmentFileName = attachment.filename
                    val attachmentFileExtension = attachmentFileName.substring(
                        attachmentFileName.lastIndexOf(".") + 1
                    )

                    if (attachmentFileExtension in logFileExtensions) {
                        val logBytes = attachment.download()

                        val logContent: String = if (attachmentFileExtension != "gz") {
                            // If the file is not a gz log, we just decode it
                            logBytes.decodeToString()
                        } else {
                            // If the file is a gz log, we convert it to a byte array,
                            // and unzip it
                            val bis = ByteArrayInputStream(logBytes)
                            val gis = GZIPInputStream(bis)

                            gis.readAllBytes().decodeToString()
                        }

                        // Ask the user to remove NEC to ease the debugging on mobile users and others
                        val necText = "at Not Enough Crashes"
                        val indexOfNECText = logContent.indexOf(necText)
                        if (indexOfNECText != -1) {
                            uploadChannel?.createEmbed {
                                title = "Not Enough Crashes detected in logs"
                                description = "Not Enough Crashes (NEC) is well known to cause issues and often " +
                                        "makes the debugging process more difficult. " +
                                        "Please remove NEC, recreate the issue, and resend the relevant files " +
                                        "(i.e. log or crash report) if the issue persists."
                                footer {
                                    text = eventMessage.author?.tag ?: ""
                                    icon = eventMessage.author?.avatar?.url
                                }
                                color = DISCORD_PINK
                            }
                        } else {
                            // Ask the user if they're ok with uploading their log to a paste site
                            var confirmationMessage: Message? = null

                            confirmationMessage = uploadChannel?.createMessage {
                                embed {
                                    title = "Do you want to upload this file to mclo.gs?"
                                    description =
                                        "mclo.gs is a website that allows users to share minecraft logs " +
                                                "through public posts.\nIt's easier for the mobile users to view " +
                                                "the file on mclo.gs, do you want it to be uploaded?"
                                    footer {
                                        text =
                                            "Uploaded by ${eventMessage.author?.tag ?: eventMember?.asUserOrNull()?.tag}"
                                        icon =
                                            eventMessage.author?.avatar?.url ?: eventMember?.asUserOrNull()?.avatar?.url
                                    }
                                    color = DISCORD_PINK
                                }

                                components {
                                    ephemeralButton(row = 0) {
                                        label = "Yes"
                                        style = ButtonStyle.Success

                                        action {
                                            // Make sure only the log uploader can confirm this
                                            if (event.interaction.user.id == eventMember!!.id) {
                                                // Delete the confirmation and proceed to upload
                                                confirmationMessage!!.delete()

                                                val uploadMessage = uploadChannel.createEmbed {
                                                    title = "Uploading `$attachmentFileName` to mclo.gs..."
                                                    footer {
                                                        text =
                                                            "Uploaded by ${eventMessage.author?.tag ?: eventMember.asUserOrNull().tag}"
                                                        icon = eventMessage.author?.avatar?.url
                                                            ?: eventMember.asUserOrNull().avatar?.url
                                                    }
                                                    timestamp = Clock.System.now()
                                                    color = DISCORD_PINK
                                                }

                                                try {
                                                    val response = postToMCLogs(logContent)

                                                    uploadMessage.edit {
                                                        embed {
                                                            title = "`$attachmentFileName` uploaded to mclo.gs"
                                                            footer {
                                                                text =
                                                                    "Uploaded by ${eventMessage.author?.tag ?: eventMember.asUserOrNull().tag}"
                                                                icon = eventMessage.author?.avatar?.url
                                                                    ?: eventMember.asUserOrNull().avatar?.url
                                                            }
                                                            timestamp = Clock.System.now()
                                                            color = DISCORD_PINK
                                                        }

                                                        actionRow {
                                                            linkButton(response) {
                                                                label = "Click here to view"
                                                            }
                                                        }
                                                    }
                                                } catch (e: IOException) {
                                                    // If the upload fails, we'll just show the error
                                                    uploadMessage.edit {
                                                        embed {
                                                            title =
                                                                "Failed to upload `$attachmentFileName` to mclo.gs"
                                                            description = "Error: $e"
                                                            footer {
                                                                text =
                                                                    "Uploaded by ${eventMessage.author?.tag ?: eventMember.asUserOrNull().tag}"
                                                                icon = eventMessage.author?.avatar?.url
                                                                    ?: eventMember.asUserOrNull().avatar?.url
                                                            }
                                                            timestamp = Clock.System.now()
                                                            color = DISCORD_RED
                                                        }
                                                    }
                                                    // Capture Exception to Sentry
                                                    sentry.captureException(e) {
                                                        tag("log_file_name", attachmentFileName)
                                                        tag("extension", extension.name)
                                                        tag("id", eventMessage.id.toString())
                                                    }
                                                    e.printStackTrace()
                                                }
                                            } else {
                                                respond { content = "Only the uploader can use this menu." }
                                            }
                                        }
                                    }

                                    ephemeralButton(row = 0) {
                                        label = "No"
                                        style = ButtonStyle.Danger

                                        action {
                                            if (event.interaction.user.id == eventMember!!.id) {
                                                confirmationMessage!!.delete()
                                            } else {
                                                respond { content = "Only the uploader can use this menu." }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        publicSlashCommand {
            name = "log-whitelist"
            description = "Commands related to the log-uploading whitelist"
            ephemeralSubCommand(::Whitelist) {
                name = "add-to-whitelist"
                description = "Add a channel to the log-uploading whitelist"
                check {
                    anyGuild()
                    hasPermission(Permission.ManageChannels)
                }
                action {
                    if (LogUploadingCollection().checkIfChannelIsInWhitelist(guild!!.id, arguments.channel.id)) {
                        respond {
                            content = "Channel already in whitelist!"
                        }
                        return@action
                    }
                    this.guild?.id?.let { LogUploadingCollection().addChannelToWhitelist(it, arguments.channel.id) }
                    respond { content = "Channel added to Whitelist" }
                }
            }
            ephemeralSubCommand(::Whitelist) {
                name = "remove-whitelist"
                description = "Add a channel to the log-uploading whitelist"
                check {
                    anyGuild()
                    hasPermission(Permission.ManageChannels)
                }
                action {
                    if (LogUploadingCollection().checkIfChannelIsInWhitelist(guild!!.id, arguments.channel.id)) {
                        respond {
                            content = "Channel already in whitelist!"
                        }
                        return@action
                    }
                    this.guild?.id?.let { LogUploadingCollection().removeChannelFromWhitelist(it, arguments.channel.id) }
                    respond { content = "Channel added to Whitelist" }
                }
            }
        }
    }

    inner class Whitelist : Arguments() {
        val channel by channel {
            name = "channel"
            description = "Channel to add to the Whitelist"
        }
    }

    @Serializable
    data class LogData(val success: Boolean, val id: String? = null, val error: String? = null)
    private suspend fun postToMCLogs(text: String): String {
        val client = HttpClient()
        val cleanText = text.replace("\r\n", "\n", true).replace("\r", "\n", true)
        val response = client.post("https://api.mclo.gs/1/log") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("content", cleanText)
                    }
                )
            )
        }.readBytes().decodeToString()
        client.close()
        val json = Json { ignoreUnknownKeys = true } // to avoid causing any errors due to missing values in the JSON
        val log = json.decodeFromString<LogData>(response)
        if (log.success) {
            return "https://mclo.gs/" + log.id
        } else {
            throw IOException("Failed to upload log: " + log.error)
        }
    }
}
