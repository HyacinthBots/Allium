package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import org.hyacinthbots.allium.utils.getRandomSplash
import kotlin.time.Duration.Companion.minutes

class PresenceUpdater : Extension() {
    override val name = "presence-updater"

    private val scheduler = Scheduler()

    private lateinit var task: Task

    override suspend fun setup() {
        task = scheduler.schedule(10.minutes, repeat = true, callback = ::updatePresence)
    }

    private suspend fun updatePresence() {
        kord.editPresence {
            playing(getRandomSplash())
        }
    }
}
