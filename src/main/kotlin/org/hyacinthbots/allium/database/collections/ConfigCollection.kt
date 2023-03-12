package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.ConfigData
import org.koin.core.component.inject
import org.litote.kmongo.eq

class ConfigCollection : KordExKoinComponent {

    private val db: Database by inject()

    @PublishedApi
    internal val collection = db.mongo.getCollection<ConfigData>()

    suspend fun removeConfig(guildId: Snowflake) = collection.deleteOne(ConfigData::guildId eq guildId)
}
