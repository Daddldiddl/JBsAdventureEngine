package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/**
 * Describes how a specific item can be used within a room and what effect that has.
 *
 * An [ItemUsage] is attached to a [Room] and links an item (by [itemId]) to an [action]
 * and the [description] shown to the player upon use.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class ItemUsage(
    val itemId: Int,
    val actions: List<Action>,
    val becomesUsable: Boolean? = false,
    val consumeUsedItem: Boolean? = false
) {
    /**
     * Resolves the [Item] associated with this usage from the global item list.
     *
     * @param itemMap The complete list of items in the game world.
     * @return The [Item] whose ID matches [itemId], or `null` if not found.
     */
    fun getUsageItem(itemMap: Map<Int, Item>): Item? {
        return itemMap[itemId]
    }
}
