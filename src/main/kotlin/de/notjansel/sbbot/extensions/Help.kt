package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respondingPaginator

/**
 * What the fuck did I do here.
 * @author NotJansel
 * @since 0.1.3
 * */

class Help : Extension() {
    override val name = "help"
    override suspend fun setup() {
        publicSlashCommand {
            name = "help"
            description = "Get help to commands"
            action {
                val commands = applicationCommandRegistry.bot.extensions
                respondingPaginator {
                    page {
                        title = "Help Menu"
                        description = """
                            This Menu shows every single command in this Bot, that it can support.
                            The Paginator is there to easily go through all of them.
                        """.trimIndent()
                    }
                    for (s in commands.values) {
                         for (c in s.slashCommands) {
                             for (sb in c.subCommands) {
                                 page {
                                     title = c.name + " " + sb.name
                                     description = sb.description
                                     field {
                                         name = "Arguments"
                                         inline = true
                                         value = sb.arguments.toString()
                                     }
                                 }
                             }
                             for (g in c.groups.values) {
                                 for (sb in g.subCommands) {
                                     page {
                                         title = c.name + " " + g.name + " " + sb.name
                                         description = sb.description
                                         field {
                                             name = "Arguments"
                                             inline = true
                                             value = sb.arguments.toString()
                                         }
                                     }
                                 }
                             }
                             if (c.groups.values == null || c.subCommands == null) {
                                 page {
                                     title = c.name
                                     description = c.description
                                     field {
                                         name = "Arguments"
                                         inline = true
                                         value = c.arguments.toString()
                                     }
                                 }
                             }
                         }
                    }
                }.send()
            }
        }
    }
}
