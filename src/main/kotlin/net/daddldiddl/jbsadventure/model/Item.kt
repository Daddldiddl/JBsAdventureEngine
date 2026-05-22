package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/**
 * Represents an item that can exist within the game world.
 *
 * Items reside in a room and can be examined or used by the player.
 * 
 * [usable] indicates whether the item can currently be used by the player;
 * [carriable] indicates whether the player can pick up the item;
 * [driveable] indicates whether using the item can cause it to relocate with the player.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class Item(
    val id: Int,
    val name: String,
    val description: String,
    val alternateNames: List<String>,
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null
) {
    object Constants {
        public val INVENTORY_LOCATION :Int = -1
        public val NOTASSIGNED_LOCATION :Int = 0
    }

    fun getArticle(): String {
        val firstChar = name.firstOrNull()?.lowercaseChar() ?: return "a"
        return if (firstChar in listOf('a', 'e', 'i', 'o', 'u')) "an" else "a"
    }

    /**
     * Checks if the given name matches this item, considering both the main name and any alternate names.
     *
     * @param name The name to check against this item.
     * @return `true` if the name matches either the main name or any alternate names; `false` otherwise.
     */
    fun matchesName(name: String): Boolean {
        val lowerName = name.lowercase()
        return lowerName == this.name.lowercase() ||
                alternateNames.any { it.lowercase() == lowerName }
    }

    fun descriptionWithState(gameData: GameData): String {
        val state = stateKey?.let { gameData.getStateMap()[it] }
        val usedescription = numberOfUses?.let { "${description.replace("<numberOfUses>", numberOfUses.toString())}" } 
                ?: "${description}"
        return if (state != null) "$usedescription\n${state.getDescriptionWithCurrentValue()}" else "$usedescription"

    }
    /**
     * Returns a debug-friendly name for the item, including its ID.
     */
    fun debugName(): String {
        return "$name (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
    }
}
