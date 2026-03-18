package org.hyacinthbots.allium.database.collections

import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.LinkListenerData
import org.koin.core.component.inject
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class LinkListenerCollection : KordExKoinComponent {
	private val db: Database by inject()
	@PublishedApi
	internal val linkListenerCollection = db.mongo.getCollection<LinkListenerData>()

	suspend inline fun checkIfChannelIsInWhitelist(guildId: Snowflake, channelId: Snowflake): Boolean {
		val coll = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			)
		)
		return coll?.channels?.contains(channelId) == true
	}

	suspend inline fun checkIfChannelIsInBlacklist(guildId: Snowflake, channelId: Snowflake): Boolean {
		val coll = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			)
		)
		return coll?.channels?.contains(channelId) == true
	}

	suspend fun addChannelToWhitelist(guildId: Snowflake, channelId: Snowflake) {
		var list = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			)
		)?.channels
		if (list != null) {
			list.add(channelId)
			linkListenerCollection.updateOne(
				and(
					LinkListenerData::guildId eq guildId,
					LinkListenerData::type eq "whitelist"
				),
				setValue(LinkListenerData::channels, list)
			)
		} else {
			list = mutableListOf(channelId)
			linkListenerCollection.insertOne(LinkListenerData(guildId, list, "whitelist"))
		}
	}

	suspend fun addChannelToBlacklist(guildId: Snowflake, channelId: Snowflake) {
		var list = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			)
		)?.channels
		if (list != null) {
			list.add(channelId)
			linkListenerCollection.updateOne(
				and(
					LinkListenerData::guildId eq guildId,
					LinkListenerData::type eq "blacklist"
				),
				setValue(LinkListenerData::channels, list)
			)
		} else {
			list = mutableListOf(channelId)
			linkListenerCollection.insertOne(LinkListenerData(guildId, list, "blacklist"))
		}
	}

	suspend fun getWhitelist(guildId: Snowflake): MutableList<Snowflake>? =
		linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			)
		)?.channels

	suspend fun getBlacklist(guildId: Snowflake): MutableList<Snowflake>? =
		linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			)
		)?.channels

	suspend fun removeChannelFromWhitelist(guildId: Snowflake, channelId: Snowflake) {
		val list = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			)
		)?.channels
		list?.remove(channelId)
		linkListenerCollection.updateOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			),
			setValue(LinkListenerData::channels, list)
		)
	}

	suspend fun removeChannelFromBlacklist(guildId: Snowflake, channelId: Snowflake) {
		val list = linkListenerCollection.findOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			)
		)?.channels
		list?.remove(channelId)
		linkListenerCollection.updateOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			),
			setValue(LinkListenerData::channels, list)
		)
	}

	suspend fun removeWhitelist(guildId: Snowflake) =
		linkListenerCollection.deleteOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "whitelist"
			)
		)

	suspend fun removeBlacklist(guildId: Snowflake) =
		linkListenerCollection.deleteOne(
			and(
				LinkListenerData::guildId eq guildId,
				LinkListenerData::type eq "blacklist"
			)
		)
}
