package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import de.notjansel.sbbot.TEST_SERVER_CHANNEL_ID
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent

@OptIn(KordPreview::class)
class Startup : Extension() {
    override val name = "startup"
    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val onlineLog = kord.getGuild(TEST_SERVER_ID)?.getChannelOf<TextChannel>(TEST_SERVER_CHANNEL_ID)
                onlineLog?.createMessage("Bot Online")
                kord.editPresence {
                    status = PresenceStatus.Online
                    streaming("some skyblock shenanigans", "https://twitch.tv/notjansel")
                }
            }
        }
    }
}
