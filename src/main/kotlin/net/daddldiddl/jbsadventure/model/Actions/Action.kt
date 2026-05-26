package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of actions that can be performed in the game.
 * 
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
enum class ActionType {
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
 * Represents an action that can be performed in the game, such as using an item or moving in a direction.
 *
 * Each action has a type (e.g., "MoveTo", "ChangeState", etc.) and may have associated parameters that define
 * the specifics of the action. Actions can also have conditions based on the game state that determine
 * whether they can be executed.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
open class Action(
    val type: ActionType,
    val preconditions: List<Precondition>? = null,
    val description: String = "",
    val actionDebug: String = "$type",
    val comment: String? = null
 ) {
    /**
     * Checks if the action's state conditions are met based on the provided game state.
     *
     * @param gameData The current game data containing the state information.
     * @return `true` if the action can be executed (i.e., conditions are met or no conditions specified); `false` otherwise.
     */
    fun canExecute(gameData: GameData): Boolean {
        if (preconditions.isNullOrEmpty()) {
            return true // No preconditions, action can be executed
        }
        return preconditions.all { precondition ->
            val state = gameData.getStateMap()[precondition.requiredStateKey] ?: return false
            state.currentValue == precondition.requiredStateValue
        }
    }

    public fun logActionExecution() {
        if (actionDebug != null) {
            LOG.debug("Executing action: $actionDebug")
        }
    }
}

/**
 * Action representing a change in the game state, such as unlocking a door or activating a mechanism.
 */
@Serializable
data class ChangeStateAction(
    val changedStateKey: String,
    val newStateValue: String
) : Action(
    type = ActionType.ChangeState,
    description: String = "",
    comment: String? = null,
    preconditions: List<Precondition>? = null,
    actionDebug = "Changed state '$changedStateKey' from '$oldStateValue' to '$newStateValue'."
)

/**
 * Action representing a transformation of one item into another, such as using a key to create an open door.
 */
@Serializable
data class MoveToAction(
    val moveToRoomId: Int
) : Action(
    type = ActionType.MoveTo,
    description: String = "",
    comment: String? = null,
    preconditions: List<Precondition>? = null,
    actionDebug = "Moved player to room with id $moveToRoomId."
)

/**
 * Action representing a change in the room location of an item, such as moving an item to a different room.
 */
@Serializable
data class SetItemRoomAction(
    val affectedItemIds : List<Int>,
    val moveToRoomId: Int
) : Action(
    type = ActionType.SetItemRoom,
    description: String = "",
    comment: String? = null,
    preconditions: List<Precondition>? = null,
    actionDebug = "Moved items ${affectedItemIds.joinToString(", ")} to room with id $moveToRoomId."
)

/**
 * Action representing a transformation of one item into another, such as using a key to create an open door.
 */
@Serializable
data class TransformIntoItemAction(
    val itemIds: List<Int>,
    val transformsIntoItemIds: List<Int>
) : Action(
    type = ActionType.TransformIntoItem,
    description: String = "",
    comment: String? = null,
    preconditions: List<Precondition>? = null,
    actionDebug = "Transformed items ${itemIds.joinToString(", ")} into items ${transformsIntoItemIds.joinToString(", ")}."
)



