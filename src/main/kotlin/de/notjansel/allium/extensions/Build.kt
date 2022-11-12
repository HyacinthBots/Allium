package de.notjansel.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.allium.utils.*
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
            action {
                respond {
                    embed {
                        title = "General Information"
                        field {
                            name = "Dependencies"
                            inline = false
                            value = """
                                [KordEx](https://github.com/Kord-Extensions/kord-extensions): 1.5.5-20221107.093708-35
                                [KordEx Mappings Extension](https://github.com/Kord-Extensions/kord-extensions/tree/develop/extra-modules/extra-mappings): 1.5.5-20221107.093708-35
                                [Gson](https://github.com/google/gson): 2.10
                                [jansi](https://github.com/fusesource/jansi): 2.4.0
                                [kotlin-logging](https://github.com/MicroUtils/kotlin-logging): 2.1.23
                                [logback-classic](https://github.com/qos-ch/logback): 1.2.8
                                [kotlinx-serialization-core](https://github.com/Kotlin/kotlinx.serialization): 1.4.1
                                [detekt](https://github.com/detekt/detekt): 1.21.0
                                [Kotlin](https://github.com/JetBrains/kotlin): 1.7.21
                                [groovy](https://github.com/apache/groovy): 3.0.8
                            """.trimIndent()
                        }
                        field {
                            name = "Build"
                            inline = false
                            value = BUILD
                        }
                        field {
                            name = "Other Infos"
                            inline = false
                            value = """
                                |Language: Kotlin
                                |Buildsystem: Gradle
                                |My mental issues because of this: alot
                                |Some dependencies cannot be updated yet due to logging issues
                            """.trimMargin()
                        }
                    }
                }
            }
        }
    }
}
