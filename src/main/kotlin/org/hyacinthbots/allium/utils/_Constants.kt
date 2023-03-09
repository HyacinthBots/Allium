package org.hyacinthbots.allium.utils

import com.kotlindiscord.kord.extensions.utils.env
import com.mongodb.client.MongoClient
import dev.kord.common.entity.Snowflake

/** Get the test server ID from the env vars or a .env file. */
val TEST_SERVER_ID = Snowflake(env("TEST_SERVER"))

/** Get the test channel ID from the env vars or a .env file. */
val TEST_SERVER_CHANNEL_ID = Snowflake(env("TEST_CHANNEL"))

/** Get the bots token from the env vars or a .env file. */
val TOKEN = env("TOKEN")

/** Get the time the bot was built on. */
const val BUILDTIME = "@buildTime@"

/** Create a nulled MongoCLient for later initialization. */
var mongoClient: MongoClient? = null

/** Get the Mongo URI for the Client to connect to. */
val MONGO_URI = env("MONGO_URI")

/** Get the build revision. */
const val BUILD = "@version@"

val ENVIRONMENT = env("ENVIRONMENT")
