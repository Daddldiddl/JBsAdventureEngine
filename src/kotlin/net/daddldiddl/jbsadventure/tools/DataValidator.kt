package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.Item
import net.daddldiddl.jbsadventure.model.ItemAction
import net.daddldiddl.jbsadventure.model.ItemUsage

/**
 * Provides validation checks for deserialized data.
 */
object DataValidator {

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
        LOG.debug("Validating game data...")
        // Check that all room exits point to valid room IDs
        for (room in gameData.getRoomList()) {
            for ((direction, targetRoomId) in room.exits) {
                if (!gameData.getRoomMap().containsKey(targetRoomId)) {
                    LOG.warn(
                            "Room '${room.id}' has an exit '$direction' pointing to non-existent room ID '$targetRoomId'"
                    )
                    isValid = false
                }
            }
        }
        // Check that all items reference valid states
        for (item in gameData.getItemList()) {
            // Check if the item references a valid state key (if any)
            val stateKey = item.stateKey
            if (stateKey != null && !gameData.getStateMap().containsKey(stateKey)) {
                LOG.warn("Item '${item.id}' has a usage referencing non-existent state '$stateKey'")
                isValid = false
            }
            // Check if the item is located in a valid room (if any)
            val location = item.location
            if (location != Item.Constants.INVENTORY_LOCATION &&
                            location != Item.Constants.NOTASSIGNED_LOCATION &&
                            !gameData.getRoomMap().containsKey(location)
            ) {
                LOG.warn(
                        "Item '${item.id}' has a usage referencing non-existent room ID '$location'"
                )
                isValid = false
            }
        }
        // Check that all rooms with item usage have valid properties(e.g., target room IDs, item
        // IDs)
        for (room in gameData.getRoomList()) {
            for (usage: ItemUsage in room.itemUsages ?: emptyList()) {
                // Check if the item usages reference valid state keys (if any)
                val stateKey = usage.stateKey
                if (stateKey != null) {
                    if (!gameData.getStateMap().containsKey(stateKey)) {
                        LOG.warn(
                                "Room '${room.id}' has an item usage (itemId ${usage.itemId}) referencing non-existent state '$stateKey'"
                        )
                        isValid = false
                    } else {
                        val state = gameData.getStateByKey(stateKey)
                        if (state != null) {
                            // If the usage specifies old/new state values, check that they are
                            // valid for the referenced state
                            if (usage.oldStateValue != null &&
                                            !state.possibleValues.contains(usage.oldStateValue)
                            ) {
                                LOG.warn(
                                        "Room '${room.id}' has an item usage (itemId ${usage.itemId}) with invalid oldStateValue '${usage.oldStateValue}' for state '$stateKey'"
                                )
                                isValid = false
                            }
                            if (usage.newStateValue != null &&
                                            !state.possibleValues.contains(usage.newStateValue)
                            ) {
                                LOG.warn(
                                        "Room '${room.id}' has an item usage (itemId ${usage.itemId}) with invalid newStateValue '${usage.newStateValue}' for state '$stateKey'"
                                )
                                isValid = false
                            }
                        }
                    }
                }
                // If the usage involves moving to another room or setting an item's location, check
                // that the target room ID exists
                if (usage.action == ItemAction.MoveTo || usage.action == ItemAction.SetItemRoom) {
                    val targetRoomId = usage.moveToRoomId
                    if (targetRoomId != null && !gameData.getRoomMap().containsKey(targetRoomId)) {
                        LOG.warn(
                                "Room '${room.id}' has an item usage with action '${usage.action}' referencing non-existent target room ID '$targetRoomId'"
                        )
                        isValid = false
                    }
                }
                // If the usage involves changing an item's state, check that the affected item ID
                // exists
                if (usage.action == ItemAction.ChangeState) {
                    val affectedItemId = usage.affectedItemId
                    if (affectedItemId != null && !gameData.getItemMap().containsKey(affectedItemId)
                    ) {
                        LOG.warn(
                                "Room '${room.id}' has an item usage with action 'ChangeState' referencing non-existent affected item ID '$affectedItemId'"
                        )
                        isValid = false
                    }
                }
            }
        }
        LOG.debug("Game data validation completed ${if(isValid) "successfully" else "with errors"}")
        return isValid
    }
}
