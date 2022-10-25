package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import de.notjansel.sbbot.utils.*
import dev.kord.rest.builder.message.create.embed

/**
 * Build Command, to show my monkebrain what build the bot is on and more.
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
                                [KordEx](https://github.com/KordExtensions/kord-extensions): latest
                                Gson: latest
                                Jansi: latest
                                kotlin-logging: latest
                                logback-classic: latest
                                kotlinx-serialization-core: latest
                                groovy: latest
                                detekt: latest
                                Kotlin: latest
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
