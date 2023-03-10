package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import org.hyacinthbots.allium.database.collections.LogUploadingCollection
import org.hyacinthbots.allium.utils.BUILD
import org.hyacinthbots.allium.utils.TEST_SERVER_CHANNEL_ID
import org.hyacinthbots.allium.utils.TEST_SERVER_ID
import org.hyacinthbots.allium.utils.getRandomSplash

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

                kord.editPresence {
                    playing(getRandomSplash())
                }
            }
        }
        event<DisconnectEvent> {
            action { /**/ }
        }
        event<GuildDeleteEvent> {
            action {
                LogUploadingCollection().removeWhitelist(event.guildId)
            }
        }
    }
}
