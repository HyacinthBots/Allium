package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.*
import dev.kord.core.kordLogger
import kotlinx.coroutines.flow.count
import org.hyacinthbots.allium.TEST_SERVER_CHANNEL_ID
import org.hyacinthbots.allium.TEST_SERVER_ID
import org.hyacinthbots.allium.utils.*

/**
 * Startup Functions.
 * @author NotJansel
 * @since 0.1.2
 */

@OptIn(KordPreview::class)
class Startup : Extension() {
    override val name = "startup"
    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val onlineLog = kord.getGuildOrNull(TEST_SERVER_ID)?.getChannelOf<TextChannel>(TEST_SERVER_CHANNEL_ID)
                onlineLog?.createMessage("Bot Online, current version: $BUILD")
                kord.editPresence {
                    status = PresenceStatus.Online
                    watching("${kord.guilds.count()} Servers")
                }
            }
        }
        event<DisconnectEvent> {
            action { kordLogger.info("Bot Disconnected.") }
        }
    }
}
