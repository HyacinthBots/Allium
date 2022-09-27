package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed

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
                    }
                    embed {
                        field {
                            name = "About Jansel himself"
                            value = """
                                Jansel is a developer, who codes when he has time and taught everything
                                mostly himself. Beginning in Spring 2020, when the lockdowns started to
                                hit Germany, he decided to learn something he may need in the future: Coding.
                                With this mindset he began to learn coding and found a first usecase:
                                Using Methods to get unsupported games running on NVIDIA GeForce NOW.
                                As a Early Access User of GNF, another Exploit, he quickly learned how
                                the exploiting was done, and even what dangers it can bring. With that
                                he continued his path for small over a year and a half, until he got a better GPU.
                                And when that day came, he decided to turn his back to exploiting after
                                one of his discord accounts was terminated in Winter 2020. Since he turned
                                his back to exploiting, he did go on another path of things he liked:
                                Minecraft. He played it alot, and I mean ALOT when his mental state was
                                at the lowest of his lifetime and as time flew by, he eventually got back to 
                                coding, but not exploiting. He realized what he could do with coding and decided
                                to learn more languages than just C#. He turned his way to Java and Minecraft
                                Plugin coding. That time was going on for about half a year, until he discovered
                                Kotlin, the language he is now mainly using and in which this Bot is coded.
                            """.trimIndent()
                        }
                    }
                }
            }
        }
    }
}
