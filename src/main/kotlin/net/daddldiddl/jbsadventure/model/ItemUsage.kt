package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/**
 * Defines the possible actions triggered when a player uses an item in a room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
enum class ItemAction {
    /** Teleports the player to the room specified by [ItemUsage.moveToRoomId]. */
    MoveTo,
    /** Moves the item to the room specified by [ItemUsage.moveToRoomId]. */
    SetItemRoom,
    /** Changes the state of the item specified by [ItemUsage.itemId]. */
    ChangeState,
    /** Transforms the item specified by [ItemUsage.itemId] into another item specified by [ItemUsage.transformsIntoItemId]. */
    TransformIntoItem
}

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
    val action: Action,
    val becomesUsable: Boolean? = null,
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

    fun getAffectedItem(itemMap: Map<Int, Item>): Item? {
        val affectedId = affectedItemId ?: return null
        return itemMap[affectedId]
    }

    fun getState(StateMap: Map<String, State>): State? {
        val key = stateKey ?: return null
        return StateMap[key]
    }

    fun isStateChangeValid(StateMap: Map<String, State>): Boolean {
        val State = getState(StateMap) ?: return false
        return State.currentValue == oldStateValue
    }
}
