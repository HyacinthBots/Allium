package org.hyacinthbots.allium.database.migrations

import com.mongodb.MongoNamespace
import org.hyacinthbots.allium.database.entities.LogUploadingBlacklistData
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
    with(db.getCollection<LogUploadingBlacklistData>("logUploadingData")) {
        collection.renameCollection(MongoNamespace("logUploadingWhitelistData"))
    }
}
