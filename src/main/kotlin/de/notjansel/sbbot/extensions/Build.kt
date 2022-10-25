package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import de.notjansel.sbbot.utils.*
import dev.kord.rest.builder.message.create.embed

/**
 * Build Command, to show my monkebrain what build the bot is on and more
 * @author NotJansel
 * @since 0.1.3
 */
class Build : Extension() {
    override val name = "build"
    override suspend fun setup() {
        publicSlashCommand {
            name = "build"
            description = "Displays what build the bot is on."
            allowByDefault = false
            allowInDms = false

            guild(TEST_SERVER_ID)
            action {
                respond {
                    embed {
                        title = "General Information"
                        field {
                            name = "Dependencies"
                            inline = false
                            value = """
                                KordEx: @kordex@
                                Gson: @gson@
                                Jansi: @jansi@
                                kotlin-logging: @logging@
                                logback-classic: @logback@
                                kotlinx-serialization-core: @kx-ser@
                                groovy: @groovy@
                                detekt: @detekt@
                                Kotlin: @kotlin@
                            """.trimIndent()
                        }
                        field {
                            name = "Build"
                            inline = false
                            value = "@version@"
                        }
                    }
                }
            }
        }
    }
}
