package net.daddldiddl.jbsadventure.model.lang

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.lang.Keys
import net.daddldiddl.jbsadventure.model.Exit
import net.daddldiddl.jbsadventure.model.Room

/**
 * Represents a message that can be displayed to the player, with support for placeholders and dynamic content.
 *
 * Each message has a key that corresponds to an entry in the language data, and can include placeholders
 * that are replaced with actual values when the message is generated.
 */
object Messages {

    fun getExitList(exits: List<Exit>): String {
        key
        val template = LANG.getMessageTemplate(key)
        var message = template
        val exitNames = exits.filter { it.exitState.visible }
                .joinToString(", ") { it.getDescriptiveName() }
        return message.replace(Keys.Placeholders.exits, exitNames)
    }

    fun
}