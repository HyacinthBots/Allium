package org.hyacinthbots.allium.extensions

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.Permission
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.container
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.messageFlags
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.extensions.event
import org.hyacinthbots.allium.database.collections.ConfigCollection
import org.hyacinthbots.allium.i18n.Translations

class Config : Extension() {
	override val name = "config"

	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Config.Command.name
			description = Translations.Config.Command.description

			ephemeralSubCommand {
				name = Translations.Config.Command.Set.name
				description = Translations.Config.Command.Set.description
				check {
					anyGuild()
					hasPermission(Permission.ManageGuild)
				}
				action {
					respond {
						messageFlags {
							+MessageFlag.IsComponentsV2
						}
						container {
							textDisplay("# The Allium Config")
							separator(SeparatorSpacingSize.Large)
							textDisplay("""How to use:
								|By clicking the button below, a modal will pop up with Selectors.
								|Use these selectors to set the Listener-types for each Listener.
								|Explanation is a follows:
								|- whitelist: the Listener will explicitly only trigger on messages in the channels, that are in the whitelist.
								|- blacklist: the exact opposite: it will trigger in any channel **except** those in the blacklist.
								|
								|This is just a basic implementation right now, the following will be added here later:
								|- setting channels of the respective listeners.
								|- migration of these channels into a single collection, for easier access internally. (no more searching 3 seperate collections)
							""".trimMargin())
							actionRow {
								interactionButton(ButtonStyle.Primary, "config_set") {
									label = "Open config setter"
								}
							}
						}

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
							field {
								name = "Link Listener type"
								value = ConfigCollection().linkListenerType(guild!!.id)
								inline = true
							}
						})
					}
				}
			}
		}

		event<ButtonInteractionCreateEvent> {
			action {
				if (event.interaction.componentId == "config_set") {
					event.interaction.modal("Config Editor", "configeditor") {
						label("Log Uploading Type") {
							stringSelect("logup_type") {
								options = listOf(SelectOptionBuilder("whitelist", "whitelist"), SelectOptionBuilder("blacklist", "blacklist")).toMutableList()
								placeholder = "Select if the Listener should use a white- or blacklist."
							}
						}
						label("Link Listener Type") {
							stringSelect("link_listener_type") {
								options = listOf(SelectOptionBuilder("whitelist", "whitelist"), SelectOptionBuilder("blacklist", "blacklist")).toMutableList()
								placeholder = "Select if the Listener should use a white- or blacklist."
							}
						}
					}
				}
			}
		}

		event<ModalSubmitInteractionCreateEvent> {
			action {
				ConfigCollection().updateConfig(event.interaction.data.guildId.value!!,
					event.interaction.stringSelects["logup_type"]?.values?.first(),
					event.interaction.stringSelects["link_listener_type"]?.values?.first()
				)
				event.interaction.respondEphemeral {
					content = "Successfully submitted!"
				}
			}
		}
	}
}
