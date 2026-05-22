package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a room within the game world.
 *
 * A room has navigable [exits] to adjacent rooms and may contain items.
 * [itemUsages] defines which items can be used here and what effect each use triggers.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class Room(
    val id: Int,
    val name: String,
    val description: String,
    val exits: Map<String, Int>,
    @SerialName("ItemUsages")
    val itemUsages: List<ItemUsage>? = emptyList()
) {
    /**
     * Returns the [ItemUsage] definition for the given item in this room, if one exists.
     *
     * @param itemId The ID of the item to look up.
     * @return The matching [ItemUsage], or `null` if no usage is defined for that item here.
     */
    fun getItemUsage(itemId: Int): ItemUsage? {
        return itemUsages?.find { it.itemId == itemId }
    }

    /**
     * Returns a debug-friendly name for the room, including its ID.
     */
    fun debugName(): String {
        return "$name (id=$id)"
    }    
}
