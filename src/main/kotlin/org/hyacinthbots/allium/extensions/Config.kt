package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

class Config : Extension() {
    override val name = "config"
    override suspend fun setup() {
        publicSlashCommand {
            name = "config"
            description = "Config Commands"
        }
    }
}
