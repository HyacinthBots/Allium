package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import org.hyacinthbots.allium.splashes
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.TEST_SERVER_CHANNEL_ID
import org.hyacinthbots.allium.utils.TEST_SERVER_ID

/**
 * Startup Functions.
 * @author NotJansel
 * @since 0.1.2
 */
class EventHooks : Extension() {
    override val name = "startup"
    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val onlineLog =
                    kord.getGuildOrNull(TEST_SERVER_ID)?.getChannelOf<GuildMessageChannel>(TEST_SERVER_CHANNEL_ID)
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
