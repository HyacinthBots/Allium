package org.hyacinthbots.allium.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    val guildId: Snowflake,
    val logUploadingType: String
)
