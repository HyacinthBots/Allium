package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import de.notjansel.sbbot.utils.*

class About : Extension() {
    override val name = "about"
    override suspend fun setup() {
        publicSlashCommand {
            name = "about"
            description = "what is this bot?"
            action {
                respond {
                    embed {
                        title = "Skyblock Bot"
                        field {
                            name = "General Information"
                            inline = false
                            value = """
                                Hello! I am SkyblockBot, a bot developed as a fun project by Jansel.
                                I am open Source available on GitHub, to show complete transparency.
                                I was done as a Project to replace the mayor feature from SkyHelper,
                                which has some locked behind a requirement (100 Members respectively).
                                Jansel didn't want to have this requirementwall to prevent him from 
                                using that feature, so he coded his own. I also have some utility-
                                commands, such as the modrinth commandgroup, which lets you search
                                for projects published on modrinth.
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
                            value = "https://github.com/NotJansel/SkyblockBot"
                        }
                        field {
                            name = "Build"
                            value = getBuild()
                        }
                    }
                }
            }
        }
    }
}
