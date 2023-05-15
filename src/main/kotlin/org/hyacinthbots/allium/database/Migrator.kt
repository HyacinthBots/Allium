package org.hyacinthbots.allium.database

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import mu.KotlinLogging
import org.hyacinthbots.allium.database.collections.MetaCollection
import org.hyacinthbots.allium.database.entities.MetaData
import org.hyacinthbots.allium.database.migrations.v1
import org.koin.core.component.inject

object Migrator : KordExKoinComponent {
    private val logger = KotlinLogging.logger("Migrator Logger")

    private val db: Database by inject()
    private val mainMetaCollection: MetaCollection by inject()

    suspend fun migrate() {
        logger.info { "Starting main database migration" }

        var meta = mainMetaCollection.get()

        if (meta == null) {
            meta = MetaData(0)

            mainMetaCollection.set(meta)
        }

        var currentVersion = meta.version

        logger.info { "Current main database version: v$currentVersion" }

        while (true) {
            val nextVersion = currentVersion + 1

            @Suppress("TooGenericExceptionCaught", "UseIfInsteadOfWhen")
            try {
                when (nextVersion) {
                    1 -> ::v1
                    else -> break
                }(db.mongo)

                logger.info { "Migrated database to version $nextVersion." }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to migrate database to version $nextVersion." }

                throw t
            }

            currentVersion = nextVersion
        }

        if (currentVersion != meta.version) {
            meta = meta.copy(version = currentVersion)

            mainMetaCollection.update(meta)

            logger.info { "Finished main database migrations." }
        }
    }
}
