package org.hyacinthbots.allium.database.collections

import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.ConfigData
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class ConfigCollection : KordExKoinComponent {

	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mongo.getCollection<ConfigData>()

	suspend fun updateConfig(guildId: Snowflake, logUploadingType: String? = null, linkListenerType: String? = null) {
		val coll = collection.findOne(ConfigData::guildId eq guildId)
		if (coll != null) {
			if (logUploadingType != null) {
				collection.updateOne(
					ConfigData::guildId eq guildId,
					setValue(ConfigData::logUploadingType, logUploadingType)
				)
			}
			if (linkListenerType != null) {
				collection.updateOne(
					ConfigData::guildId eq guildId,
					setValue(ConfigData::linkListenerType, linkListenerType)
				)
			}
		} else {
			collection.insertOne(
				ConfigData(
					guildId,
					logUploadingType ?: "whitelist",
					linkListenerType ?: "blacklist"
				)
			)
		}
	}

	suspend fun logUploadingType(guildId: Snowflake): String {
		val coll = collection.findOne(ConfigData::guildId eq guildId)
		return coll?.logUploadingType ?: "whitelist"
	}

	suspend fun linkListenerType(guildId: Snowflake): String {
		val coll = collection.findOne(ConfigData::guildId eq guildId)
		return coll?.linkListenerType ?: "blacklist"
	}

	suspend fun removeConfig(guildId: Snowflake) = collection.deleteOne(ConfigData::guildId eq guildId)
}
