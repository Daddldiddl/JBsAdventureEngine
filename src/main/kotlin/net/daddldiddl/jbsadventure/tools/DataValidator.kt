package net.daddldiddl.jbsadventure.tools

import kotlin.enums.enumEntries
import net.daddldiddl.jbsadventure.ILogger
import net.daddldiddl.jbsadventure.model.Container
import net.daddldiddl.jbsadventure.model.FixedLocation
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.ItemUsage
import net.daddldiddl.jbsadventure.model.actions.*

/**
 * Provides validation checks for deserialized data.
 */
object DataValidator {

    private val fixedLocationValues: Set<Int> = enumEntries<FixedLocation>().map { it.value }.toSet()

    private fun isValidLocation(location: Int, gameData: GameData): Boolean {
        return location in fixedLocationValues || gameData.getRoomMap().containsKey(location)
    }

    /**
     * Performs validation checks on a [GameData] instance to identify potential issues such as:
     * 
     * - Room exits that point to non-existent room IDs
     * - Items that reference non-existent state keys or invalid room locations
     * - Item usages in rooms that reference non-existent state keys, invalid state values, or
     *   invalid target room IDs or item IDs
     * 
     * Logs warnings for any issues found, but does not throw exceptions or
     * prevent the game from loading, allowing for more leniency in data formatting while still
     * providing feedback on potential problems.
     * 
     * @param gameData The [GameData] instance to validate.
     * @return `true` if no issues were found, `false` if any warnings were logged during validation.
     */
    fun validate(gameData: GameData): Boolean {
        var isValid = true
        ILogger.current.debug("Validating game data...")

        // Check that all room exits point to valid room IDs
        for (room in gameData.getRoomList()) {
            for ((direction, exit) in room.exits.orEmpty()) {
                if (!gameData.getRoomMap().containsKey(exit.targetRoomId)) {
                    ILogger.current.warn(
                            "Room '${room.id}' has an exit '$direction' pointing to non-existent room ID '$exit.targetRoomId'"
                    )
                    isValid = false
                }
            }
        }

        // Check that all items reference valid states and locations
        for (item in gameData.getItemList()) {
            // Check that the item references a valid state key (if any)
            val stateKey = item.stateKey
            if (stateKey != null && !gameData.getStateMap().containsKey(stateKey)) {
                ILogger.current.warn("Item '${item.id}' has a usage referencing non-existent state '$stateKey'")
                isValid = false
            }
            // Check that the item is located in a valid room (if any)
            val location = item.location
            if (!isValidLocation(location, gameData)) {
                ILogger.current.warn(
                        "Item '${item.id}' has a usage referencing non-existent room ID '$location'"
                )
                isValid = false
            }
            // check that an Item with location CONTAINER has exactly one container containing it
            if (location == FixedLocation.CONTAINER.value) {
                val containers = gameData.Containers.values
                    .filter { it.containsItem(item.id) }
                    .map { it.id }
                if (containers.isEmpty()){
                    ILogger.current.warn("Item '${item.id}' has no container")
                    isValid = false
                } else if (containers.size > 1) {
                    ILogger.current.warn("Item '${item.id}' has more than one container (itemIds: ${containers.joinToString(", ")})")
                    isValid = false
                }
            }
        }

        // Check that all rooms with item usage have valid properties(e.g., target room IDs, item
        // IDs)
        for (room in gameData.getRoomList()) {
            for (usage: ItemUsage in room.itemUsages ?: emptyList()) {
                if (!gameData.getItemMap().containsKey(usage.itemId)) {
                    ILogger.current.warn(
                        "Room '${room.id}' has an item usage referencing non-existent itemId '${usage.itemId}'"
                    )
                    isValid = false
                }

                if (usage.actions.isEmpty()) {
                    ILogger.current.warn("Room '${room.id}' has an item usage (itemId ${usage.itemId}) without actions")
                    isValid = false
                }

                for (action in usage.actions) {
                    if(action.validatePreconditions(gameData)) {
                        ILogger.current.warn(
                            "Room '${room.id}' has an item usage (itemId ${usage.itemId}) with unsatisfied preconditions, which may prevent the action from executing as intended"
                        )
                        isValid = false
                    }

                    // Validate action-specific references and value constraints.
                    when (action) {
                        is ChangeStateAction -> {
                            val state = gameData.getStateByKey(action.changedStateKey)
                            if (state == null) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ChangeState action (itemId ${usage.itemId}) referencing non-existent state '${action.changedStateKey}'"
                                )
                                isValid = false
                            } else if (action.newStateValue !in state.possibleValues) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ChangeState action (itemId ${usage.itemId}) with invalid newStateValue '${action.newStateValue}' for state '${action.changedStateKey}'"
                                )
                                isValid = false
                            }
                        }

                        is MoveToAction -> {
                            if (!gameData.getRoomMap().containsKey(action.moveToRoomId)) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a MoveTo action (itemId ${usage.itemId}) referencing non-existent target room ID '${action.moveToRoomId}'"
                                )
                                isValid = false
                            }
                        }

                        is SetItemRoomAction -> {
                            if (!isValidLocation(action.moveToRoomId, gameData)) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a SetItemRoom action (itemId ${usage.itemId}) referencing invalid location '${action.moveToRoomId}'"
                                )
                                isValid = false
                            }
                            for (affectedItemId in action.affectedItemIds) {
                                if (!gameData.getItemMap().containsKey(affectedItemId)) {
                                    ILogger.current.warn(
                                        "Room '${room.id}' has a SetItemRoom action (itemId ${usage.itemId}) referencing non-existent affected item ID '$affectedItemId'"
                                    )
                                    isValid = false
                                }
                            }
                        }

                        is TransformIntoItemAction -> {
                            for (affectedItemId in action.affectedItemIds) {
                                if (!gameData.getItemMap().containsKey(affectedItemId)) {
                                    ILogger.current.warn(
                                        "Room '${room.id}' has a TransformIntoItem action (itemId ${usage.itemId}) referencing non-existent source item ID '$affectedItemId'"
                                    )
                                    isValid = false
                                }
                            }
                            for (transformedItemId in action.transformsIntoItemIds) {
                                if (!gameData.getItemMap().containsKey(transformedItemId)) {
                                    ILogger.current.warn(
                                        "Room '${room.id}' has a TransformIntoItem action (itemId ${usage.itemId}) referencing non-existent target item ID '$transformedItemId'"
                                    )
                                    isValid = false
                                }
                            }
                        }

                        is ModifyExitAction -> {
                            val targetRoom = gameData.getRoomById(action.roomId)
                            if (targetRoom == null) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ModifyExit action (itemId ${usage.itemId}) referencing non-existent room ID '${action.roomId}'"
                                )
                                isValid = false
                            } else if (!targetRoom.exits.orEmpty().containsKey(action.direction)) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ModifyExit action (itemId ${usage.itemId}) referencing unknown direction '${action.direction}' in room '${action.roomId}'"
                                )
                                isValid = false
                            }
                        }

                        is ModifyContainerAction -> {
                            val container = gameData.getItemById(action.containerId)
                            if (container == null) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ModifyContainer action (itemId ${usage.itemId}) referencing non-existent container ID '${action.containerId}'"
                                )
                                isValid = false
                            } else if (container !is Container) {
                                ILogger.current.warn(
                                    "Room '${room.id}' has a ModifyContainer action (itemId ${usage.itemId}) referencing item ID '${action.containerId}' which is not a container"
                                )
                                isValid = false
                            }
                        }

                        else -> {
                            ILogger.current.warn(
                                "Room '${room.id}' has an item usage (itemId ${usage.itemId}) with unsupported action type '${action.type}'"
                            )
                            isValid = false
                        }
                    }
                }
            }
        }
        ILogger.current.debug("Game data validation completed ${if(isValid) "successfully" else "with errors"}")
        return isValid
    }
}
