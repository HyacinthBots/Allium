package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.LogUploadingBlacklistData
import org.hyacinthbots.allium.database.entities.LogUploadingWhitelistData
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class LogUploadingCollection : KordExKoinComponent {
    private val db: Database by inject()

    @PublishedApi
    internal val whitelistCollection = db.mongo.getCollection<LogUploadingWhitelistData>()

    @PublishedApi
    internal val blacklistCollection = db.mongo.getCollection<LogUploadingBlacklistData>()

    suspend inline fun checkIfChannelIsInWhitelist(guildId: Snowflake, channelId: Snowflake): Boolean {
        val coll = whitelistCollection.findOne(LogUploadingWhitelistData::guildId eq guildId)
        return coll?.channels?.contains(channelId) == true
    }

    suspend inline fun checkIfChannelIsInBlacklist(guildId: Snowflake, channelId: Snowflake): Boolean {
        val coll = blacklistCollection.findOne(LogUploadingBlacklistData::guildId eq guildId)
        return coll?.channels?.contains(channelId) == true
    }

    suspend fun addChannelToWhitelist(guildId: Snowflake, channelId: Snowflake) {
        var list = whitelistCollection.findOne(LogUploadingWhitelistData::guildId eq guildId)?.channels
        if (list != null) {
            list.add(channelId)
            whitelistCollection.updateOne(LogUploadingWhitelistData::guildId eq guildId, setValue(LogUploadingWhitelistData::channels, list))
        } else {
            list = mutableListOf(channelId)
            whitelistCollection.insertOne(LogUploadingWhitelistData(guildId, list))
        }
    }

    suspend fun addChannelToBlacklist(guildId: Snowflake, channelId: Snowflake) {
        var list = blacklistCollection.findOne(LogUploadingBlacklistData::guildId eq guildId)?.channels
        if (list != null) {
            list.add(channelId)
            blacklistCollection.updateOne(LogUploadingBlacklistData::guildId eq guildId, setValue(LogUploadingBlacklistData::channels, list))
        } else {
            list = mutableListOf(channelId)
            blacklistCollection.insertOne(LogUploadingBlacklistData(guildId, list))
        }
    }

    suspend fun removeChannelFromWhitelist(guildId: Snowflake, channelId: Snowflake) {
        val list = whitelistCollection.findOne(LogUploadingWhitelistData::guildId eq guildId)?.channels
        list?.remove(channelId)
        whitelistCollection.updateOne(LogUploadingWhitelistData::guildId eq guildId, setValue(LogUploadingWhitelistData::channels, list))
    }

    suspend fun removeChannelFromBlacklist(guildId: Snowflake, channelId: Snowflake) {
        val list = blacklistCollection.findOne(LogUploadingBlacklistData::guildId eq guildId)?.channels
        list?.remove(channelId)
        blacklistCollection.updateOne(LogUploadingBlacklistData::guildId eq guildId, setValue(LogUploadingBlacklistData::channels, list))
    }

    suspend fun removeWhitelist(guildId: Snowflake) = whitelistCollection.deleteOne(LogUploadingWhitelistData::guildId eq guildId)
    suspend fun removeBlacklist(
        guildId: Snowflake
    ) = blacklistCollection.deleteOne(LogUploadingBlacklistData::guildId eq guildId)
}
