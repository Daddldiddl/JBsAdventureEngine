package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable

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
    val defaultDefiniteArticle: String = "a",
    val defaultIndefiniteArticle: String = "an",
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
