package org.hyacinthbots.allium.utils

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
