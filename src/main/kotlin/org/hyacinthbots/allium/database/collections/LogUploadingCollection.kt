package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.LogUploadingData
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class LogUploadingCollection : KordExKoinComponent {
    private val db: Database by inject()

    @PublishedApi
    internal val collection = db.mongo.getCollection<LogUploadingData>()

    suspend inline fun checkIfChannelIsInWhitelist(guildId: Snowflake, channelId: Snowflake): Boolean {
        val coll = collection.findOne(LogUploadingData::guildId eq guildId)
        return coll?.channels?.contains(channelId) == true
    }

    suspend fun addChannelToWhitelist(guildId: Snowflake, channelId: Snowflake) {
        var list = collection.findOne(LogUploadingData::guildId eq guildId)?.channels
        if (list != null) {
            list.add(channelId)
            collection.updateOne(LogUploadingData::guildId eq guildId, setValue(LogUploadingData::channels, list))
        } else {
            list = mutableListOf(channelId)
            collection.insertOne(LogUploadingData(guildId, list))
        }
    }

    suspend fun removeChannelFromWhitelist(guildId: Snowflake, channelId: Snowflake) {
        val list = collection.findOne(LogUploadingData::guildId eq guildId)?.channels
        list?.remove(channelId)
        collection.updateOne(LogUploadingData::guildId eq guildId, setValue(LogUploadingData::channels, list))
    }

    suspend fun removeWhitelist(guildId: Snowflake) = collection.deleteOne(LogUploadingData::guildId eq guildId)
}
