package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.CONSOLE
import net.daddldiddl.jbsadventure.tools.ConsoleColor

object LanguageKeys {
    // directions
    public val north = "north"
    public val south = "south"   
    public val east = "east"
    public val west = "west"
    public val up = "up"
    public val down = "down"
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
    // placeholder
    public val placeholderDirection = "<direction>"
    public val placeholderDirections = "<directions>"
    public val placeholderExits = "<exits>"
    public val placeholderItem = "<item>"
    public val placeholderItems = "<items>"
}


@Serializable
data class CommandData(
    val aliases: List<String>,
    val description: String
)

@Serializable
data class LanguageData(
    val useArticlesFromItemData: Boolean = true,
    val directions: Map<String, Set<String>>,
    val commands: Map<String, CommandData>,
    val partsToIgnore: Set<String>,
    public val directionsHeading: String,
    public val commandsHeading: String,
    public val descriptionExits: String,
    public val descriptionNoExits: String,
    public val descriptionItems: String,
    public val descriptionInventory: String,
    public val descriptionNoInventory: String
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

    fun isCommand(command: String): Boolean {
        for(cmd in commands.values) {
            if(cmd.aliases.contains(command)) {
                return true
            }
        }
        return commands.containsKey(command) && commands[command]?.aliases?.contains("go") == true
    }

    fun getAllDirectionAliases(): List<String> {
        val allDirections = mutableListOf<String>()
        for (aliases in directions.values) {
            allDirections.addAll(aliases)
        }
        return allDirections.distinct()
    }

    fun getInternalDirection(input: String): String {
        for ((internal, aliases) in directions) {
            if (aliases.contains(input)) {
                return internal
            }
        }
        return "<unknown direction>"
    }

    fun getDirectionFromInternal(input: String): String {
        return directions[input]?.firstOrNull { it == input } ?: input
    }

}
