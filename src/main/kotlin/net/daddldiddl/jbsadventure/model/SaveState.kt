package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemSaveState(
    val itemId: Int,
    val location: Int,
    val usable: Boolean,
    val numberOfUses: Int? = null
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
    val stateValues: Map<String, String>
)
