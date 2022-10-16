package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respondingPaginator

class Help : Extension() {
    override val name = "help"
    override suspend fun setup() {
        publicSlashCommand {
            name = "help"
            description = "Get help to commands"
            action {
                val commands = slashCommands
                respondingPaginator {
                    for (s in commands) {
                         page {
                             title = ""
                         }
                    }
                }
            }
        }
    }
}
