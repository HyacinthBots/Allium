package org.hyacinthbots.allium.database.migrations

import com.mongodb.MongoNamespace
import org.hyacinthbots.allium.database.entities.LogUploadingWhitelistData
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
    db.getCollection<LogUploadingWhitelistData>("logUploadingData")
        .renameCollection(MongoNamespace("Allium.logUploadingWhitelistData"))
}
