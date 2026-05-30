package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/**
 * Persisted mutable snapshot of one item.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class ItemSaveState(
    val itemId: Int,
    val location: Int,
    val usable: Boolean,
    val numberOfUses: Int? = null,
    /** If set, item is currently stored in this container ID. */
    val currentContainerId: Int? = null,
    /** Open flag for container items; null for normal items. */
    val open: Boolean? = null,
    /** Locked flag for container items; null for normal items. */
    val locked: Boolean? = null,
    /** Explicit container item list for container items; null for normal items. */
    val containedItemIds: List<Int>? = null
)

/** Mutable runtime snapshot for one room exit. */
@Serializable
data class ExitSaveState(
    val open: Boolean,
    val locked: Boolean,
    val visible: Boolean,
    val blocked: Boolean
)

/**
 * Represents a snapshot of all mutable game states that must persist across sessions.
 *
 * Contains only the data that diverges from the fixed initial state in `data.json`:
 * the player's current room, the current state of every item in the world, 
 * and the current values of all game states.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class SaveState(
    /** ID of the room the player was in when the game was saved. */
    val currentRoomId: Int,
    /** Maps each item's ID to its current state. */
    val itemStates: Map<Int, ItemSaveState>,
    /** Maps each state key to its current value. */
    val stateValues: Map<String, String>,
    /** Mutable runtime flags per room and direction for exits. */
    val exitStates: Map<Int, Map<String, ExitSaveState>> = emptyMap()
)
