package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.ConfigData
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class ConfigCollection : KordExKoinComponent {

    private val db: Database by inject()

    @PublishedApi
    internal val collection = db.mongo.getCollection<ConfigData>()

    suspend fun updateConfig(guildId: Snowflake, moderatorRole: Snowflake, logUploadingType: String) {
        val coll = collection.findOne(ConfigData::guildId eq guildId)
        if (coll != null) {
            collection.updateOne(ConfigData::guildId eq guildId, setValue(ConfigData::logUploadingType, logUploadingType))
            collection.updateOne(ConfigData::guildId eq guildId, setValue(ConfigData::moderatorRole, moderatorRole))
        } else {
            collection.insertOne(ConfigData(guildId, logUploadingType, moderatorRole))
        }
    }

    suspend fun hasModeratorRole(guildId: Snowflake, user: User): Boolean {
        val coll = collection.findOne(ConfigData::guildId eq guildId) ?: return false
        val moderatorRole = coll.moderatorRole
        return user.asMember(guildId).roleIds.contains(moderatorRole)
    }

    suspend fun moderatorRole(guildId: Snowflake): Snowflake? {
        val coll = collection.findOne(ConfigData::guildId eq guildId)
        return coll?.moderatorRole
    }

    suspend fun logUploadingType(guildId: Snowflake): String {
        val coll = collection.findOne(ConfigData::guildId eq guildId)
        return coll?.logUploadingType ?: "whitelist"
    }

    suspend fun removeConfig(guildId: Snowflake) = collection.deleteOne(ConfigData::guildId eq guildId)
}
