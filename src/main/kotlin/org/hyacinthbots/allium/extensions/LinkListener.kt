package org.hyacinthbots.allium.extensions

import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.embed
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.channel
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import org.hyacinthbots.allium.database.collections.LinkListenerCollection
import org.hyacinthbots.allium.i18n.Translations

class LinkListener : Extension() {
	override val name = "link-listener"

	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Linklistener.Command.Whitelist.name
			description = Translations.Linklistener.Command.Whitelist.description
			ephemeralSubCommand(::LinkListenerArgs) {
				name = Translations.Linklistener.Command.Whitelist.Add.name
				description = Translations.Linklistener.Command.Whitelist.Add.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					if (LinkListenerCollection().checkIfChannelIsInWhitelist(guild!!.id, arguments.channel.id)) {
						respond { content = "Channel already in whitelist!" }
						return@action
					}
					LinkListenerCollection().addChannelToWhitelist(guild!!.id, arguments.channel.id)
					respond { content = "Channel added to Whitelist" }
				}
			}
			ephemeralSubCommand(::LinkListenerArgs) {
				name = Translations.Linklistener.Command.Whitelist.Remove.name
				description = Translations.Linklistener.Command.Whitelist.Remove.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					if (LinkListenerCollection().checkIfChannelIsInWhitelist(guild!!.id, arguments.channel.id)) {
						LinkListenerCollection().removeChannelFromWhitelist(guild!!.id, arguments.channel.id)
						respond { content = "Channel removed from Whitelist" }
						return@action
					} else {
						respond { content = "Channel is not in the Whitelist" }
					}
				}
			}
			ephemeralSubCommand {
				name = Translations.Linklistener.Command.Whitelist.List.name
				description = Translations.Linklistener.Command.Whitelist.List.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					val list = LinkListenerCollection().getWhitelist(this.guild!!.id)
					respond {
						embed {
							var channelsList = ""
							title = "Link Listener Whitelist"
							if (list != null) {
								for (channel in list) {
									channelsList += "<#$channel>\n"
								}
							}
							description = channelsList
						}
					}
				}
			}
		}
		publicSlashCommand {
			name = Translations.Linklistener.Command.Blacklist.name
			description = Translations.Linklistener.Command.Blacklist.description
			ephemeralSubCommand(::LinkListenerArgs) {
				name = Translations.Linklistener.Command.Blacklist.Add.name
				description = Translations.Linklistener.Command.Blacklist.Add.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					if (LinkListenerCollection().checkIfChannelIsInBlacklist(guild!!.id, arguments.channel.id)) {
						respond { content = "Channel already in blacklist!" }
						return@action
					}
					LinkListenerCollection().addChannelToBlacklist(guild!!.id, arguments.channel.id)
					respond { content = "Channel added to blacklist" }
				}
			}
			ephemeralSubCommand(::LinkListenerArgs) {
				name = Translations.Linklistener.Command.Blacklist.Remove.name
				description = Translations.Linklistener.Command.Blacklist.Remove.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					if (LinkListenerCollection().checkIfChannelIsInBlacklist(guild!!.id, arguments.channel.id)) {
						LinkListenerCollection().removeChannelFromBlacklist(guild!!.id, arguments.channel.id)
						respond { content = "Channel removed from blacklist" }
						return@action
					} else {
						respond { content = "Channel is not in the blacklist" }
					}
				}
			}
			ephemeralSubCommand {
				name = Translations.Linklistener.Command.Blacklist.List.name
				description = Translations.Linklistener.Command.Blacklist.List.description
				check {
					anyGuild()
					hasPermission(Permission.ManageChannels)
				}
				action {
					val list = LinkListenerCollection().getBlacklist(this.guild!!.id)
					respond {
						embed {
							var channelsList = ""
							title = "Link Listener Blacklist"
							if (list != null) {
								for (channel in list) {
									channelsList += "<#$channel>\n"
								}
							}
							description = channelsList
						}
					}
				}
			}
		}
	}

	inner class LinkListenerArgs : Arguments() {
		val channel by channel {
			name = Translations.Linklistener.Arguments.Channel.name
			description = Translations.Linklistener.Arguments.Channel.description
		}
	}
}
