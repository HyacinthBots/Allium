package org.hyacinthbots.allium.extensions

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import org.hyacinthbots.allium.database.collections.ConfigCollection

class Config : Extension() {
    override val name = "config"
    override suspend fun setup() {
        publicSlashCommand {
            name = "config"
            description = "Config Commands"

            publicSubCommand(::setConfig) {
                name = "set"
                description = "Set the Config"
                check {
                    anyGuild()
                    hasPermission(Permission.ManageGuild)
                }
                action {
                    ConfigCollection().updateConfig(guild!!.id, arguments.logUploadingType)
                    respond {
                        content = "The Config was successfully set!"
                    }
                }
            }
            publicSubCommand {
                name = "get"
                description = "Set the Config"
                check {
                    anyGuild()
                    hasPermission(Permission.ManageGuild)
                }
                action {
                    respond {
                        embed(fun EmbedBuilder.() {
                            title = "Config for Guild ${getGuild()!!.asGuild().name}"
                            field {
                                name = "Log Uploading type"
                                value = ConfigCollection().logUploadingType(guild!!.id)
                                inline = true
                            }
                        })
                    }
                }
            }
        }
    }

    inner class setConfig : Arguments() {
        val logUploadingType by stringChoice {
            name = "type"
            description = "What Listtype should be used."
            choice("whitelist", "whitelist")
            choice("blacklist", "blacklist")
        }
    }
}
