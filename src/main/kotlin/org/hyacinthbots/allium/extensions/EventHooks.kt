package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.*
import dev.kord.core.event.guild.*
import org.hyacinthbots.allium.TEST_SERVER_CHANNEL_ID
import org.hyacinthbots.allium.TEST_SERVER_ID
import org.hyacinthbots.allium.splashes
import org.hyacinthbots.allium.utils.*

/**
 * Startup Functions.
 * @author NotJansel
 * @since 0.1.2
 */

@OptIn(KordPreview::class)
class EventHooks : Extension() {
    override val name = "startup"
    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val onlineLog = kord.getGuildOrNull(TEST_SERVER_ID)?.getChannelOf<TextChannel>(TEST_SERVER_CHANNEL_ID)
                onlineLog?.createMessage("Bot Online, current version: $BUILD")
                val entries = splashes.count()
                val entry = (0 until entries).random()
                kord.editPresence {
                    playing(splashes.get(entry).asString)
                }
            }
        }
        event<DisconnectEvent> {
            action { /**/ }
        }
    }
}
