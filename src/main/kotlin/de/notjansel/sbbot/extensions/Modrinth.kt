package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import java.net.URL

class Modrinth : Extension() {
    override val name = "modrinth"
    override suspend fun setup() {
        publicSlashCommand {
            name = "modrinth"
            description = "modrinth related commands"
            publicSubCommand {
                name = "user"
                description = "Search for a User"
                action {
                    val query: String // get the query arg
                    val url: URL = URL("https://api.modrinth.com/")
                }
            }
            publicSubCommand {
                name = "mod"
                description = "Search for a mod"
            }
            publicSubCommand {
                name = "plugin"
                description = "Search for a plugin"
            }
            publicSubCommand {
                name = "resourcepack"
                description = "Search for a resource pack"
            }
            publicSubCommand {
                name = "modpack"
                description = "Search for a Modpack"
            }
        }
    }
}
