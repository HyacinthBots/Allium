package org.hyacinthbots.allium.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.hyacinthbots.allium.utils.MONGO_URI
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
    private val settings = MongoClientSettings
        .builder()
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .applyConnectionString(ConnectionString(MONGO_URI))
        .build()

    private val client = KMongo.createClient(settings).coroutine

    val mongo get() = client.getDatabase("Allium")

    suspend fun migrate() {
        Migrator.migrateMain()
    }
}
