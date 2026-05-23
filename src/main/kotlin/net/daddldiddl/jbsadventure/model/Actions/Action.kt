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
@Serializable
open data class Action(
    val type: ActionType,
    val requiredStateKey: String? = null,
    val requiredStateValue: String? = null,
    val description: String? = null,
    val comment: String? = null,
    @Transient val actionDebug: String? = null,
) {
    /**
     * Checks if the action's state conditions are met based on the provided game state.
     *
     * @param gameData The current game data containing the state information.
     * @return `true` if the action can be executed (i.e., conditions are met or no conditions specified); `false` otherwise.
     */
    fun canExecute(gameData: GameData): Boolean {
        if (requiredStateKey == null || requiredStateValue == null) {
            return true // No state requirement, action can be executed
        }
        val state = gameData.getStateMap()[requiredStateKey] ?: return false
        return state.currentValue == requiredStateValue
    }
}

data class ChangeStateAction(
    val changedStateKey: String,
    val oldStateValue: String,
    val newStateValue: String
) : Action(
    type = ActionType.ChangeState,
    description :String? = null,
    comment: String? = null,
    requiredStateKey: String? = null,
    requiredStateValue: String? = null,
    actionDebug = "Changed state '$changedStateKey' from '$oldStateValue' to '$newStateValue'."
)

data class MoveToAction(
    val moveToRoomId: Int
) : Action(
    type = ActionType.MoveTo,
    description :String? = null,
    comment: String? = null,
    requiredStateKey: String? = null,
    requiredStateValue: String? = null,
    actionDebug = "Move player to room with id $moveToRoomId."
)


data class SetItemRoomAction(
    val affectedItemIds : List<Int>,
    val moveToRoomId: Int
) : Action(
    type = ActionType.SetItemRoom,
    description :String? = null,
    comment: String? = null,
    requiredStateKey: String? = null,
    requiredStateValue: String? = null,
    actionDebug = "Move items ${affectedItemIds.joinToString(", ")} to room with id $moveToRoomId."
)

data class SetItemRoomAction(
    val itemIds: List<Int>,
    val moveToRoomId: Int
) : Action(
    type = ActionType.SetItemRoom,
    description :String? = null,
    comment: String? = null,
    requiredStateKey: String? = null,
    requiredStateValue: String? = null,
    actionDebug = "Move items ${itemIds.joinToString(", ")} to room with id $moveToRoomId."
)

data class TransformIntoItemsAction(
    val itemIds: List<Int>,
    val transformsIntoItemIds: List<Int>
) : Action(
    type = ActionType.TransformIntoItem,
    description :String? = null,
    comment: String? = null,
    requiredStateKey: String? = null,
    requiredStateValue: String? = null,
    actionDebug = "Transform items ${itemIds.joinToString(", ")} into items ${transformsIntoItemIds.joinToString(", ")}."
)



