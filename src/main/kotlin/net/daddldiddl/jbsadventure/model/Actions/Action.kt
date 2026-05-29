package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.tools.serializers.*

@Serializable
data class Precondition(
    val requiredStateKey: String,
    val requiredStateValues: Set<String> = emptySet()
)
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
@Serializable(with = ActionSerializer::class)
open class Action(
    val type: ActionType,
    val preconditions: List<Precondition> = emptyList(),
    val description: String? = null,
    val comment: String? = null,
    val actionDebug: String? = null) {
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
            precondition.requiredStateValues.contains(state.currentValue)
        }
    }

    public fun logActionExecution() {
        if (actionDebug != null) {
            LOG.debug("Executing action: ${actionDebug}")
        }
    }
}

/**
 * Action representing a change in a global game state, such as 
 * 'the bomb is armed' or 'the hordes of doom have been unleashed'
 * - which then serves as a pre-requisite for other actions.
 */
@Serializable(with = ActionSerializer::class)
data class ChangeStateAction(
    val changedStateKey: String,
    val newStateValue: String
) : Action(
    type = ActionType.ChangeState,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Changed state '$changedStateKey' to '$newStateValue'."
)

/**
 * Action representing the movement of the player to a different room.
 */
@Serializable(with = ActionSerializer::class)
data class MoveToAction(
    val moveToRoomId: Int
) : Action(
    type = ActionType.MoveTo,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Moved player to room with id $moveToRoomId."
)

/**
 * Action representing a change in the room location of an item,
 * such as moving some items to a different room, a container, or the inventory.
 */
@Serializable(with = ActionSerializer::class)
data class SetItemRoomAction(
    val affectedItemIds : List<Int>,
    val moveToRoomId: Int
) : Action(
    type = ActionType.SetItemRoom,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Moved items ${affectedItemIds.joinToString(", ")} to room with id $moveToRoomId."
)

/**
 * Action representing a transformation of one item into another,
 * e.g. transforming an empty pot into a pot of hot coffee.
 */
@Serializable(with = ActionSerializer::class)
data class TransformIntoItemAction(
    val affectedItemIds: List<Int>,
    val transformsIntoItemIds: List<Int>
) : Action(
    type = ActionType.TransformIntoItem,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Transformed items ${affectedItemIds.joinToString(", ")} into items ${transformsIntoItemIds.joinToString(", ")}."
)



