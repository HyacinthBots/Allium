package org.hyacinthbots.allium.extensions

import dev.kordex.core.extensions.Extension
import dev.kordex.core.utils.envOrNull
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import kotlin.time.Duration.Companion.seconds

class StatusPing : Extension() {
	override val name = "status-ping"

	private val scheduler = Scheduler()

	private lateinit var task: Task

	private val client = HttpClient {}

	private val env = envOrNull("STATUS_URL")
	private val env2 = envOrNull("STATUS_PRIV_URL")

	private val logger = KotlinLogging.logger("Status ping")

	override suspend fun setup() {
		if (env != null) {
			task = scheduler.schedule(30.seconds, repeat = true, callback = ::post)
		}
	}

	private suspend fun post() {
		logger.debug { "Pinging!" }
		client.post(env!!)
		client.post(env2!!)
	}
}
