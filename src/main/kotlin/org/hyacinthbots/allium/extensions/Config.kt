package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.entity.Permission

class Config : Extension() {
    override val name = "config"
    override suspend fun setup() {
        publicSlashCommand {
            name = "config"
            description = "Config Commands"

            publicSubCommand(::setConfig) {
                name = "set"
                description = "Set the Config"
                requirePermission(Permission.ModerateMembers)
                failIf {

                }
                action {

                }
            }
        }
    }

    inner class setConfig : Arguments() {
        val moderatorRole by role {
            name = "modRole"
            description = "Moderator Role"
        }
        val logUploadingType by stringChoice {
            name = "logUploadingType"
            description = "What Listtype should be used."
            choice("whitelist", "whitelist")
            choice("blacklist", "blacklist")
        }
    }
}
