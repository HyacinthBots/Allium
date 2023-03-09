package org.hyacinthbots.allium.database

import com.mongodb.client.model.Filters.eq
import dev.kord.common.entity.Snowflake
import org.bson.Document
import org.hyacinthbots.allium.utils.mongoClient

fun checkIfChannelIsInWhitelist(channelId: Snowflake): Boolean {
    val doc: Document? = mongoClient?.getDatabase("Allium")?.getCollection("LogWhitelist")?.find(eq("channelId", channelId))?.first()
    return doc != null
}

fun addChannelToWhitelist(guildId: Snowflake, channelId: Snowflake) {
    mongoClient?.getDatabase("Allium")?.getCollection("LogWhitelist")?.insertOne(Document().append("guildId", guildId).append("channelId", channelId))
}

fun removeChannelFromWhitelist(channelId: Snowflake) {
    mongoClient?.getDatabase("Allium")?.getCollection("LogWhitelist")?.deleteOne(eq("channelId", channelId))
}
