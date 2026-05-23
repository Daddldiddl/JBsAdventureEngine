package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.CONSOLE
import net.daddldiddl.jbsadventure.tools.ConsoleColor

/**
 * Defines the keys for accessing specific language data entries, 
 * such as directions, commands, messages, and placeholders.
 */
object LangKey {

    /**
     * Defines the standard directions used in the game and their corresponding keys for language data.
     * Each direction can have multiple aliases defined in the language data.
     */
    object Directions {
        public val north = "north"
        public val south = "south"   
        public val east = "east"
        public val west = "west"
        public val up = "up"
        public val down = "down"
    }

    /**
     * Defines the standard commands used in the game and their corresponding keys for language data.
     * Each command can have multiple aliases defined in the language data.
     */
    object Commands {
        // general commands
        public val help = "help"
        public val quit = "quit"
        public val save = "save"
        public val load = "load"

        // action commands
        public val look = "look"
        public val inventory = "inventory"
        public val go = "go"
        public val take = "take"
        public val use = "use"
        public val drop = "drop"
        public val examine = "examine"
    }

    /**
     * Defines the default values used in the game and their corresponding keys for language data.
     */
    object DefaultValues {
        public val defaultArticle = "defaultArticle"
    }

    /**
     * Defines the placeholders used in messages and their corresponding keys for language data.
     * Placeholders are used for dynamic content in messages.
     */
    object Placeholders {    
        public val article = "<article>"
        public val specificArticle = "<specificArticle>"
        public val placeholderDirection = "<direction>"
        public val placeholderDirections = "<directions>"
        public val placeholderExits = "<exits>"
        public val placeholderItem = "<item>"
        public val placeholderItems = "<items>"
        public val placeholderAffectedItem = "<affectedItem>"
        public val placeholderTitle = "<title>"
        public val placeholderRoom = "<room>"
        public val placeholderState = "<state>"
        public val placeholderCommand = "<command>"
        public val placeholderNumberOfUses = "<numberOfUses>"
    }

    /**
     * Defines the headings used in messages and their corresponding keys for language data.
     */
    object Headings {
        public val directionsHeading = "directionsHeading"
        public val commandsHeading = "commandsHeading"
    }

    /**
     * Defines the message parts used in messages and their corresponding keys for language data.
     * Message parts are reusable components of messages that can be combined to create full messages.
     */
    object MessageParts {
        public val msgPartExits = "msgPartExits"
        public val msgPartNoExits = "msgPartNoExits"
        public val msgPartItems = "msgPartItems"
        public val msgPartNoItems = "msgPartNoItems"
        public val msgPartInventory = "msgPartInventory"
        public val msgPartNoInventory = "msgPartNoInventory"
    }

    /**
     * Defines the messages used in the game and their corresponding keys for language data.
     * Messages are the main text displayed to the player during the game.
     */
    object Messages {
        public val msgUnknownCommand = "msgUnknownCommand"
        public val msgNoExit = "msgNoExit"
        public val msgNoItem = "msgNoItem"
        public val msgNoInventoryItem = "msgNoInventoryItem"
        public val msgItemTaken = "msgItemTaken"
        public val msgItemDropped = "msgItemDropped"
        public val msgItemNotUsable = "msgItemNotUsable"
        public val msgWelcome = "msgWelcome"
        public val msgGoodbye = "msgGoodbye"
    }
}

/**
 * Represents the data for a command, including its aliases and description.
 */
@Serializable
data class CommandData(
    val aliases: List<String>,
    val description: String
)

/**
 * Represents the data for the language, including directions, commands, messages, and other game-related text.
 */
@Serializable
data class LanguageData(
    val useArticlesFromItemData: Boolean = true,
    val defaultArticle: String = "a",
    val directions: Map<String, Set<String>>,
    val commands: Map<String, CommandData>,
    val messages: Map<String, String>,
    val headings: Map<String, String>,
    val messageParts: Map<String, String>,
    val partsToIgnore: Set<String>,
) {
    /**
     * Returns the list of aliases for the given command, or an empty list if the command is not defined.
     */
    fun getCommandAliases(command: String): List<String> {
        return commands[command]?.aliases ?: emptyList()
    }

    /**
     * Returns the description for the given command, or null if the command is not defined.
     */
    fun getCommandDescription(command: String): String? {
        return commands[command]?.description
    }

    /**
     * Returns the list of direction aliases for the given direction, or an empty list if the direction is not defined.
     */
    fun getDirectionAliases(direction: String): List<String> {
        return directions[direction]?.toList() ?: emptyList()
    }

    /**
     * Checks if the given string is a valid command.
     */
    fun isCommand(command: String): Boolean {
        for(cmd in commands.values) {
            if(cmd.aliases.contains(command)) {
                return true
            }
        }
        return commands.containsKey(command) && commands[command]?.aliases?.contains("go") == true
    }

    /**
     * Returns a list of all direction aliases.
     */
    fun getAllDirectionAliases(): List<String> {
        val allDirections = mutableListOf<String>()
        for (aliases in directions.values) {
            allDirections.addAll(aliases)
        }
        return allDirections.distinct()
    }

    /**
     * Returns the internal representation of a direction based on its alias.
     */
    fun getInternalDirection(input: String): String {
        for ((internal, aliases) in directions) {
            if (aliases.contains(input)) {
                return internal
            }
        }
        return "<unknown direction>"
    }

    /**
     * Returns the alias of a direction based on its internal representation.
     */
    fun getDirectionFromInternal(input: String): String {
        return directions[input]?.firstOrNull { it == input } ?: input
    }

}
