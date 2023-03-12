package org.hyacinthbots.allium.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class LogUploadingWhitelistData(
    val guildId: Snowflake,
    val channels: MutableList<Snowflake>
)
