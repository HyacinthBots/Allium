package de.notjansel.sbbot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import de.notjansel.sbbot.TEST_SERVER_ID
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent

@OptIn(KordPreview::class)
class Startup : Extension() {
    override val name = "startup"
    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val onlineLog = kord.getGuild(TEST_SERVER_ID)?.getChannelOf<TextChannel>(Snowflake(1013046925051834458))
                onlineLog?.createMessage("Bot Online")
            }
        }
    }
}
