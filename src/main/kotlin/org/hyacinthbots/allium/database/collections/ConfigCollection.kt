package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.ConfigData
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class ConfigCollection : KordExKoinComponent {

    private val db: Database by inject()

    @PublishedApi
    internal val collection = db.mongo.getCollection<ConfigData>()

    suspend fun updateConfig(guildId: Snowflake, logUploadingType: String) {
        val coll = collection.findOne(ConfigData::guildId eq guildId)
        if (coll != null) {
            collection.updateOne(ConfigData::guildId eq guildId, setValue(ConfigData::logUploadingType, logUploadingType))
        } else {
            collection.insertOne(ConfigData(guildId, logUploadingType))
        }
    }

    suspend fun logUploadingType(guildId: Snowflake): String {
        val coll = collection.findOne(ConfigData::guildId eq guildId)
        return coll?.logUploadingType ?: "whitelist"
    }

    suspend fun removeConfig(guildId: Snowflake) = collection.deleteOne(ConfigData::guildId eq guildId)
}
