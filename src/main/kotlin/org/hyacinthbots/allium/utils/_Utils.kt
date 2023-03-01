package org.hyacinthbots.allium.utils

import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.NewsChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import org.hyacinthbots.allium.splashes
import org.hyacinthbots.allium.updatemessages

fun getRandomSplash(): String {
    val entries = splashes.count()
    val entry = (0 until entries).random()
    return splashes.get(entry).asString
}

fun getRandomUpdateMessage(): String {
    val entries = updatemessages.count()
    val entry = (0 until entries).random()
    return updatemessages.get(entry).asString
}

suspend inline fun CheckContext<*>.botHasChannelPerms(permissions: Permissions) {
    if (!passed) {
        return
    }

    val eventChannel = channelFor(event)?.asChannelOrNull() ?: return

    val permissionsSet: MutableSet<String> = mutableSetOf()
    var count = 0
    permissions.values.forEach { _ ->
        permissionsSet.add(
            permissions.values.toString()
                .split(",")[count]
                .split(".")[4]
                .split("$")[1]
                .split("@")[0]
                .replace("[", "`")
                .replace("]", "`")
        )
        count++
    }

    /* Use `TextChannel` when the channel is a Text channel */
    if (eventChannel is TextChannel) {
        if (eventChannel.asChannelOfOrNull<TextChannel>()?.getEffectivePermissions(event.kord.selfId)
                ?.contains(Permissions(permissions)) == true
        ) {
            pass()
        } else {
            fail(
                "Incorrect permissions!\nI do not have the $permissionsSet permissions for ${eventChannel.mention}"
            )
        }
    } else if (eventChannel is NewsChannel) {
        if (eventChannel.asChannelOfOrNull<NewsChannel>()?.getEffectivePermissions(event.kord.selfId)
                ?.contains(Permissions(permissions)) == true
        ) {
            pass()
        } else {
            fail(
                "Incorrect permissions!\nI do not have the $permissionsSet permissions for ${eventChannel.mention}"
            )
        }
    } else if (eventChannel is ThreadChannel) {
        if (eventChannel.asChannelOfOrNull<ThreadChannel>()?.getParent()?.getEffectivePermissions(event.kord.selfId)
                ?.contains(Permissions(permissions)) == true
        ) {
            pass()
        } else {
            fail(
                "Incorrect permissions!\nI do not have the $permissionsSet permissions for ${eventChannel.mention}"
            )
        }
    } else {
        fail("Unable to get permissions for channel! Please report this to the developers!")
    }
}
