package org.hyacinthbots.allium.utils

import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

/** Get the test server ID from the env vars or a .env file. */
val TEST_SERVER_ID = Snowflake(env("TEST_SERVER"))

/** Get the test channel ID from the env vars or a .env file. */
val TEST_SERVER_CHANNEL_ID = Snowflake(env("TEST_CHANNEL"))

/** Get the bots token from the env vars or a .env file. */
val TOKEN = env("TOKEN")

/** Get the time the bot was built on. */
const val BUILDTIME = "@buildTime@"

/** Get the build revision. */
const val BUILD = "@version@"

val ENVIRONMENT = env("ENVIRONMENT")
