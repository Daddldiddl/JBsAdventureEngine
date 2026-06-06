package net.daddldiddl.jbsadventure.model.actions

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.CONSOLE
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.model.Container
import net.daddldiddl.jbsadventure.model.FixedLocation
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.Name
import net.daddldiddl.jbsadventure.tools.serializers.ActionSerializer
import java.lang.Thread.sleep

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
    ModifyExit,
    /** Changes lock/open/blocked state of an exit. */
    ModifyContainer
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
    fun checkPreconditions(gameData: GameData): Boolean {
        if (preconditions.isEmpty()) {
            return true // No preconditions, action can be executed
        }
        return preconditions.all { precondition ->
            precondition.isSatisfied(gameData)
        }
    }

    /** Executes the action against the given runtime game data. */
    abstract fun execute(gameData: GameData): Boolean

    /** Sleeps for [delayInMillis] if this action defines an execution delay. */
    fun delayIfRequired() {
        if (delayInMillis != null) {
            LOG.debug("Delaying $type Action by ${String.format("%.3f", delayInMillis / 1000f)} seconds...")
            sleep(delayInMillis)
        }
    }

    /** Writes optional debug info for action execution. */
    fun logActionExecution() {
        if (actionDebug != null) {
            LOG.debug("Executing action: ${actionDebug}")
        }
    }

    /** Validates the action's preconditions */
    fun validatePreconditions(gameData: GameData): Boolean {
        var valid: Boolean = true
        for(precondition in preconditions) {
            valid = precondition.validate(gameData) && valid
        }
        return valid
    }

    protected fun consoleOutput(message: String) {
        CONSOLE.print(message)
    }
}

/**
 * Action representing a change in a global game state, such as
 * 'the bomb is armed' or 'the hordes of doom have been unleashed'
 * - which then serves as a pre-requisite for other actions.
 */
data class ChangeStateAction(
    val changedStateKey: String,
    val newStateValue: String,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.ChangeState,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug ?: "Changed state '$changedStateKey' to '$newStateValue'.",
    delayInMillis = configuredDelayInMillis
) {
    /** Updates the configured state key with the new value when valid. */
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
 *
 */
/**
 * Action representing the movement of the player to a different room.
 */
data class MoveToAction(
    val moveToRoomId: Int,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.MoveTo,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug ?: "Moved player to room with id $moveToRoomId.",
    delayInMillis = configuredDelayInMillis
) {
    /** Moves the player to the configured target room when possible. */
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
    val affectedItemIds: List<Int>,
    val moveToRoomId: Int,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.SetItemRoom,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug
        ?: "Moved items ${affectedItemIds.joinToString(", ")} to room with id $moveToRoomId.",
    delayInMillis = configuredDelayInMillis
) {
    /** Relocates configured items to the configured destination room/location. */
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
    val transformsIntoItemIds: List<Int>,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.TransformIntoItem,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug ?: "Transformed items ${affectedItemIds.joinToString(", ")} into items ${
        transformsIntoItemIds.joinToString(
            ", "
        )
    }.",
    delayInMillis = configuredDelayInMillis
) {
    /** Replaces configured source items with target items at source locations. */
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
            sourceItem.location = FixedLocation.NOT_ASSIGNED.value
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
    val visible: Boolean?,
    val newName: Name?,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.ModifyExit,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug ?: "Changed Exit $roomId, $direction to: ${
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
        ).filter { !it.isBlank() }.joinToString(", ") { it }.trim().ifEmpty { "no changes" }
    }.",
    delayInMillis = configuredDelayInMillis
) {
    /** Applies selective open/lock/blocked/visibility changes to one room exit. */
    override fun execute(gameData: GameData): Boolean {
        if (!checkPreconditions(gameData)) {
            return false
        }
        delayIfRequired()
        val room = gameData.getRoomById(roomId) ?: return false
        val exit = room.exits?.get(direction) ?: return false

        if (open == null && locked == null && visible == null && blocked == null) {
            LOG.warn("The ModifyContainer action for ${exit.debugName()} has no values for locked, open, blocked or visible!")
            return false
        }

        if (open != null) exit.open = open
        if (locked != null) exit.locked = locked
        if (blocked != null) exit.blocked = blocked
        if (visible != null) exit.visible = visible
        logActionExecution()
        return true
    }
}

/**
 * Action representing a transformation of one item into another,
 * e.g. transforming an empty pot into a pot of hot coffee.
 */
data class ModifyContainerAction(
    val containerId: Int,
    val open: Boolean?,
    val locked: Boolean?,
    val configuredPreconditions: List<Precondition> = emptyList(),
    val configuredDescription: String? = null,
    val configuredComment: String? = null,
    val configuredActionDebug: String? = null,
    val configuredDelayInMillis: Long? = null
) : Action(
    type = ActionType.ModifyContainer,
    preconditions = configuredPreconditions,
    description = configuredDescription,
    comment = configuredComment,
    actionDebug = configuredActionDebug ?: "Changed container $containerId to: ${
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
            }
        ).filter { !it.isBlank() }.joinToString(", ") { it }.trim().ifEmpty { "no changes" }
    }.",
    delayInMillis = configuredDelayInMillis
) {
    /** Applies selective open/lock/blocked/visibility changes to one room exit. */
    override fun execute(gameData: GameData): Boolean {
        if (!checkPreconditions(gameData)) {
            return false
        }
        val item = gameData.getItemById(containerId) ?: return false
        if (item !is Container) {
            LOG.warn("Attempted to execute ModifyContainerAction on ${item.debugName()}, but it is not a container.")
            return false
        }
        val container = item
        if (!container.supportsOpenClose && open != null) {
            LOG.warn("${container.debugName()} does not support open/close, but action attempted to set open to $open.")
            return false
        }
        if (!container.supportsLockUnlock && locked != null) {
            LOG.warn("${container.debugName()} does not support lock/unlock, but action attempted to set locked to $locked.")
            return false
        }
        if (open == null && locked == null) {
            LOG.warn("The ModifyContainer action for ${container.debugName()} has no values for locked or open!")
            return false
        }

        delayIfRequired()

        if (open != null) container.open = open
        if (locked != null) container.locked = locked
        logActionExecution()
        return true
    }
}


