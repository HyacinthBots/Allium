package org.hyacinthbots.allium.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class MetaData(
    val version: Int,
    val id: String = "meta"
)
