package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import org.hyacinthbots.allium.utils.*

/**
 * About command.
 * Written in pure boredom in school.
 * @author NotJansel
 * @since 0.1.3
 */
class About : Extension() {
    override val name = "about"
    override suspend fun setup() {
        publicSlashCommand {
            name = "about"
            description = "what is this bot?"
            action {
                respond {
                    embed {
                        title = "Allium"
                        field {
                            name = "General Information"
                            inline = false
                            value = """
                                Hello! I am Allium, a Minecraft modding focused bot.
                            """.trimIndent()
                        }
                        field {
                            name = "Birthday"
                            value = "September 6th, 2022"
                        }
                        field {
                            name = "Developers"
                            value = "Jansel, StonkDragon (mental help)"
                        }
                        field {
                            name = "Source code"
                            value = "[click me](https://github.com/HyacinthBots/Allium)"
                        }
                        field {
                            name = "Build"
                            value = BUILD
                        }
                    }
                }
            }
        }
    }
}