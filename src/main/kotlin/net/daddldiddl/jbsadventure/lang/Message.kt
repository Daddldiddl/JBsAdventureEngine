package net.daddldiddl.jbsadventure.lang

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.*

**
 * Represents a message that can be displayed to the player, with support for placeholders and dynamic content.
 *
 * Each message has a key that corresponds to an entry in the language data, and can include placeholders
 * that are replaced with actual values when the message is generated.
 */
object Messages {

    fun getWelcomeMessage(gameData: GameData): String {
        return LANG.getMessage(Keys.msgWelcome).replace(Keys.Placeholders.Title, gameData.title)
    }

    fun getIntroMessage(gameData: GameData): String {
        return LANG.get(Keys.msgIntro)
    }

    fun getGoodbyeMessage(gameData: GameData): String {
        return LANG.getMessage(Keys.msgGoodbye).replace(Keys.Placeholders.Title, gameData.title)
    }

    fun getExitList(exits: List<Exit> = emptyList()): String {
        val filteredExits = exits.filter { it.exitState.isVisible()) }
        var template = LANG.getMessagePart(
            if(filteredExits.isEmpty()) Keys.MessageParts.noExits else Keys.MessageParts.exits
        )
        val exitNames = filteredExits
                .joinToString(", ") { it.getDescriptiveName() }
        return template.replace(Keys.Placeholders.exits, exitNames)
    }

    fun getShortExitList(exits: List<Exit> = emptyList()): String {
        val filteredExits = exits.filter { it.exitState.isVisible()) }
        var template = LANG.getMessagePart(
            if(filteredExits.isEmpty()) Keys.MessageParts.noExits else Keys.MessageParts.exits
        )
        val exitNames = filteredExits
                .joinToString(", ") { it.getDescriptiveName() }
        return template.replace(Keys.Placeholders.exits, exitNames)
    }

    fun getDirectionList(exits: List<Exit> = emptyList()): String {
        val filteredExits = exits.filter { it.exitState.isVisible() }
        val directions = filteredExits.joinToString(", ") { it.getDescriptiveName() }
        return LANG.getMessagePart(
            if(filteredExits.isEmpty()) Keys.MessageParts.noExits else Keys.MessageParts.exits
        ).replace(Keys.Placeholders.directions, directions)
    }

    fun getItemList(items: List<String> = emptyList()): String {
        var template = LANG.getMessagePart(
            if(items.isEmpty()) Keys.MessageParts.noItems else Keys.MessageParts.items
        )
        val itemNames = items.joinToString(", "){it.getDescriptiveName()}
        return template.replace(Keys.Placeholders.items, itemNames)
    }


}
