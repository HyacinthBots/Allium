package org.hyacinthbots.allium.extensions

import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.stringChoice
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import org.hyacinthbots.allium.database.collections.ConfigCollection
import org.hyacinthbots.allium.i18n.Translations

class Config : Extension() {
	override val name = "config"
	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Config.Command.name
			description = Translations.Config.Command.description

			publicSubCommand(::setConfig) {
				name = Translations.Config.Command.Set.name
				description = Translations.Config.Command.Set.description
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
				name = Translations.Config.Command.Get.name
				description = Translations.Config.Command.Get.description
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
			name = Translations.Config.Arguments.Type.name
			description = Translations.Config.Arguments.Type.description
			choice(Translations.Config.Arguments.Type.Choice.w, "whitelist")
			choice(Translations.Config.Arguments.Type.Choice.b, "blacklist")
		}
	}
}
