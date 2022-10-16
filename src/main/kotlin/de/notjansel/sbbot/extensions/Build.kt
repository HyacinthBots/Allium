package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.notjansel.sbbot.TEST_SERVER_ID
import de.notjansel.sbbot.utils.*

/**
 * Build Command, to show my monkebrain what build the bot is on.
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
                    content = "The Bot is on build $BUILD"
                }
            }
        }
    }
}
