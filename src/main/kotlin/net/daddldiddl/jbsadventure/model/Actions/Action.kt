package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.tools.serializers.ActionSerializer
import java.lang.Thread.sleep

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
    /** Teleports the player to the room specified by [MoveToAction.moveToRoomId]. */
    MoveTo,
    /** Moves items to the room specified by [SetItemRoomAction.moveToRoomId]. */
    SetItemRoom,
    /** Changes a global game state. */
    ChangeState,
    /** Transforms one or multiple items into other items. */
    TransformIntoItem,
    /** Changes lock/open/blocked state of an exit. */
    ModifyExit
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
abstract class Action(
    val type: ActionType,
    val preconditions: List<Precondition> = emptyList(),
    val description: String? = null,
    val comment: String? = null,
    val actionDebug: String? = null,
    val delayInMillis: Long? = null
) {
    /**
     * Checks if the action's state conditions are met based on the provided game state.
     *
     * @param gameData The current game data containing the state information.
     * @return `true` if the action can be executed (i.e., conditions are met or no conditions specified); `false` otherwise.
     */
    fun canExecute(gameData: GameData): Boolean {
        if (preconditions.isEmpty()) {
            return true // No preconditions, action can be executed
        }
        return preconditions.all { precondition ->
            val state = gameData.getStateMap()[precondition.requiredStateKey] ?: return false
            precondition.requiredStateValues.contains(state.currentValue)
        }
    }

    abstract fun execute(gameData: GameData): Boolean

    fun delayIfRequired() {
        if (delayInMillis != null) {
            LOG.debug("Delaying $type Action by ${String.format("%.3f", delayInMillis / 1000f)} seconds...")
            sleep(delayInMillis)
        }
    }

    fun logActionExecution() {
        if (actionDebug != null) {
            LOG.debug("Executing action: ${actionDebug}")
        }
    }

    fun checkPreconditions(gameData: GameData): Boolean {
        for (precondition in preconditions) {
            val state = gameData.getStateByKey(precondition.requiredStateKey)
            if(state == null) {
                LOG.error("Precondition check failed: State with key '${precondition.requiredStateKey}' not found.")
                return false
            }
            if (!state.possibleValues.containsAll(precondition.requiredStateValues)) {
                LOG.warn("Precondition check warning: required state list contains invalid values: ${precondition.requiredStateValues.minus(state.possibleValues)}, allowed are ${state.possibleValues}")
            }
            if (!precondition.requiredStateValues.contains(state.currentValue)) {
                LOG.warn("Precondition check failed: current state is '${state.currentValue}' not in required states ${precondition.requiredStateValues}.")
                return false
            }
            LOG.debug("Precondition current state '${state.currentValue}' in ${precondition.requiredStateKey} satisfied.")
        }
        return true
    }
}

/**
 * Action representing a change in a global game state, such as 
 * 'the bomb is armed' or 'the hordes of doom have been unleashed'
 * - which then serves as a pre-requisite for other actions.
 */
data class ChangeStateAction(
    val changedStateKey: String,
    val newStateValue: String
) : Action(
    type = ActionType.ChangeState,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Changed state '$changedStateKey' to '$newStateValue'.",
    delayInMillis = null
){
    override fun execute(gameData: GameData): Boolean {
        val state = gameData.getStateByKey(changedStateKey)
        if (state != null && checkPreconditions(gameData)) {
            if (state.possibleValues.contains(newStateValue)) {
                delayIfRequired()
                gameData.setCurrentStateValue(changedStateKey, newStateValue)
                logActionExecution()
                return true
            } else {
                LOG.warn("Attempted to set state '$changedStateKey' to invalid value '$newStateValue'. Valid values are: ${state.possibleValues}")
                return false
            }
        } else {
            return false
        }
    }
}

/**
 * Action representing the movement of the player to a different room.
 */
data class MoveToAction(
    val moveToRoomId: Int,
) : Action(
    type = ActionType.MoveTo,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Moved player to room with id $moveToRoomId.",
    delayInMillis = null
){
    override fun execute(gameData: GameData): Boolean {
        val room = gameData.getRoomById(moveToRoomId)
        if (room != null && checkPreconditions(gameData)) {
            delayIfRequired()
            gameData.currentRoom = room
            logActionExecution()
            return true
        } else {
            return false
        }
    }
}

/**
 * Action representing a change in the room location of an item,
 * such as moving some items to a different room, a container, or the inventory.
 */
data class SetItemRoomAction(
    val affectedItemIds : List<Int>,
    val moveToRoomId: Int
) : Action(
    type = ActionType.SetItemRoom,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Moved items ${affectedItemIds.joinToString(", ")} to room with id $moveToRoomId.",
    delayInMillis = null
){
    override fun execute(gameData: GameData): Boolean {
        if (!checkPreconditions(gameData)) {
            return false
        }
        delayIfRequired()
        for (itemId in affectedItemIds) {
            gameData.setItemLocation(itemId, moveToRoomId)
        }
        logActionExecution()
        return true
    }
}

/**
 * Action representing a transformation of one item into another,
 * e.g. transforming an empty pot into a pot of hot coffee.
 */
data class TransformIntoItemAction(
    val affectedItemIds: List<Int>,
    val transformsIntoItemIds: List<Int>
) : Action(
    type = ActionType.TransformIntoItem,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Transformed items ${affectedItemIds.joinToString(", ")} into items ${transformsIntoItemIds.joinToString(", ")}.",
    delayInMillis = null
){
    override fun execute(gameData: GameData): Boolean {
        if (!checkPreconditions(gameData)) {
            return false
        }
        delayIfRequired()
        // Transform by moving source items out of play and placing target items at source locations.
        val pairCount = minOf(affectedItemIds.size, transformsIntoItemIds.size)
        for (idx in 0 until pairCount) {
            val sourceItem = gameData.getItemById(affectedItemIds[idx]) ?: continue
            val targetItem = gameData.getItemById(transformsIntoItemIds[idx]) ?: continue
            val sourceLocation = sourceItem.location
            sourceItem.location = Item.Constants.NOTASSIGNED_LOCATION
            targetItem.location = sourceLocation
        }
        logActionExecution()
        return true
    }
}

/**
 * Action representing a transformation of one item into another,
 * e.g. transforming an empty pot into a pot of hot coffee.
 */
data class ModifyExitAction(
    val roomId: Int,
    val direction: String,
    val open: Boolean?,
    val locked: Boolean?,
    val blocked: Boolean?,
    val visible: Boolean?
) : Action(
    type = ActionType.ModifyExit,
    preconditions = emptyList(),
    description = "",
    comment = null,
    actionDebug = "Changed Exit $roomId, $direction to: ${
        listOf(
            when (open) {
                true -> ("open")
                false -> "closed"
                null -> ""
            },
            when (locked) {
                true -> ("locked")
                false -> "unlocked"
                null -> ""
            },
            when (blocked) {
                true -> ("blocked")
                false -> "unblocked"
                null -> ""
            },
            when (visible) {
                true -> ("visible")
                false -> "invisible"
                null -> ""
            }
        ).filter{!it.isBlank()}.joinToString(", ") { it }.trim().ifEmpty { "no changes" }
    }.",
    delayInMillis = null
){
    override fun execute(gameData: GameData): Boolean {
        if (!checkPreconditions(gameData)) {
            return false
        }
        delayIfRequired()
        val room = gameData.getRoomById(roomId) ?: return false
        val exit = room.exits?.get(direction) ?: return false

        if (open != null) exit.open = open
        if (locked != null) exit.locked = locked
        if (blocked != null) exit.blocked = blocked
        if (visible != null) exit.visible = visible
        logActionExecution()
        return true
    }
}



