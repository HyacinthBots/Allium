package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import org.hyacinthbots.allium.splashes
import kotlin.time.Duration.Companion.minutes

class PresenceUpdater : Extension() {
    override val name = "presence-updater"

    private val scheduler = Scheduler()

    private lateinit var task: Task

    override suspend fun setup() {
        task = scheduler.schedule(10.minutes, repeat = true, callback = ::updatePresence)
    }

    private suspend fun updatePresence() {
        val entries = splashes.count()
        val entry = (0 until entries).random()
        kord.editPresence {
            playing(splashes.get(entry).asString)
        }
    }
}
