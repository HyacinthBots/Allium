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
                                [KordEx](https://github.com/Kord-Extensions/kord-extensions): latest snapshot
                                [Gson](https://github.com/google/gson): latest
                                [jansi](https://github.com/fusesource/jansi): latest
                                [kotlin-logging](https://github.com/MicroUtils/kotlin-logging): latest
                                [logback-classic](https://github.com/qos-ch/logback): latest
                                [kotlinx-serialization-core](https://github.com/Kotlin/kotlinx.serialization): latest
                                [detekt](https://github.com/detekt/detekt): latest
                                [Kotlin](https://github.com/JetBrains/kotlin): latest
                            """.trimIndent()
                        }
                        field {
                            name = "Build"
                            inline = false
                            value = "@version@"
                        }
                        field {
                            name = "Other Infos"
                            inline = false
                            value = """
                                |Language: Kotlin
                                |Buildsystem: Gradle
                                |My mental issues because of this: alot
                            """.trimMargin()
                        }
                    }
                }
            }
        }
    }
}
