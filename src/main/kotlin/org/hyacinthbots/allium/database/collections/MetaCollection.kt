package org.hyacinthbots.allium.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.hyacinthbots.allium.database.Database
import org.hyacinthbots.allium.database.entities.MetaData
import org.koin.core.component.inject
import org.litote.kmongo.eq

class MetaCollection : KordExKoinComponent {
    private val db: Database by inject()

    @PublishedApi
    internal val collection = db.mongo.getCollection<MetaData>()

    suspend fun get(): MetaData? =
        collection.findOne()

    suspend fun set(meta: MetaData) =
        collection.insertOne(meta)

    suspend fun update(meta: MetaData) =
        collection.findOneAndReplace(
            MetaData::id eq "meta",
            meta
        )
}
