package net.daddldiddl.jbsadventure.model.actions

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.ILogger
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.tools.serializers.PreconditionSerializer

@Serializable
enum class PreconditionType {
    PreconditionState,
    PreconditionItemsLocation,
    PreconditionExit,
    PreconditionItem,
    PreconditionContainer,
    PreconditionPlayer
}


@Serializable(PreconditionSerializer::class)
abstract class Precondition {
    abstract fun isSatisfied(gameData: GameData): Boolean
    abstract fun validate(gameData: GameData): Boolean
}

/**
 * Preconditions that must be fulfilled before an action is allowed to execute.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class PreconditionState(
    val requiredStateKey: String,
    val requiredStateValues: Set<String> = emptySet()
) : Precondition(){

    override fun isSatisfied(gameData: GameData): Boolean{
        if(!validate(gameData)) {
            return false
        }
        val state: State = gameData.States[requiredStateKey]!!
        return requiredStateValues.contains(state.currentValue)
    }

    override fun validate(gameData: GameData): Boolean {
        val state = gameData.getStateByKey(requiredStateKey)
        if (state == null) {
            ILogger.current.warn(
                "Invalid precondition referencing non-existent state '${requiredStateKey}'"
            )
            return false
        }

        val invalidValues = requiredStateValues.filter { it !in state.possibleValues }
        if (invalidValues.isNotEmpty()) {
            ILogger.current.warn(
                "Invalid precondition values ${invalidValues.joinToString(", ")} for state '${requiredStateKey}'"
            )
            return false
        }
        return true
    }
}

open class PreconditionItem(
    val itemId: Int,
    val location: Int?,
    val numberOfUses: Int?,
    val usable: Boolean?,
    val carriable: Boolean?,
    val driveable: Boolean?
) : Precondition(){

    override fun isSatisfied(gameData: GameData): Boolean {
        val item = gameData.getItemById(itemId) ?: return false
        if(location != null && location != item.location) return false
        if(numberOfUses != null && (item.numberOfUses == null || numberOfUses >= item.numberOfUses!!)) return false
        if(usable != null && usable != item.usable) return false
        if(carriable != null && carriable != item.carriable) return false
        if (driveable != null && driveable != item.driveable) return false
        return true
    }

    override fun validate(gameData: GameData): Boolean {
        if (gameData.getItemById(itemId) == null) return false
        return location != null || numberOfUses != null || usable != null || carriable != null || driveable != null
    }
}

class PreconditionPlayer(
    val location: Int?,
    val hasItems: List<Int> = emptyList(),
    val doesntHaveItems: List<Int> = emptyList()
) : Precondition() {
    override fun isSatisfied(gameData: GameData): Boolean {
        return (location == null || location == gameData.currentRoom.id)
            && (hasItems.isEmpty() || gameData.getItemsForRoom(FixedLocation.INVENTORY.value)
                .map{it.id}.containsAll(hasItems))
            && (doesntHaveItems.isEmpty() || gameData.getItemsForRoom(FixedLocation.INVENTORY.value)
                .map { it.id }.none { it in doesntHaveItems })
            && (doesntHaveItems.isNotEmpty() || hasItems.isNotEmpty() || location != null)
    }

    override fun validate(gameData: GameData): Boolean {
        return (doesntHaveItems.isNotEmpty() || hasItems.isNotEmpty() || location != null)
                && (location == null || gameData.getRoomById(location) != null)
                && (hasItems.isEmpty() || hasItems.all { gameData.getItemById(it) != null })
                && (doesntHaveItems.isEmpty() || doesntHaveItems.all { gameData.getItemById(it) != null })
    }
}

class PreconditionContainer(
    val itemId: Int,
    val location: Int?,
    val carriable: Boolean?,
    val containsItems: List<Int> = emptyList(),
    val excludesItems: List<Int> = emptyList(),
    val open: Boolean?,
    val locked: Boolean?
) : Precondition(){

    override fun isSatisfied(gameData: GameData): Boolean {
        val item = gameData.getContainerById(itemId) ?: return false
        if(location != null && location != item.location) return false
        if(carriable != null && carriable != item.carriable) return false
        if(open != null && open != item.open) return false
        if(locked != null && locked != item.locked) return false
        if(containsItems.isNotEmpty() && !gameData.getItemMap().keys.containsAll(containsItems)) return false
        if(excludesItems.isNotEmpty() && !gameData.getItemMap().keys.containsAll(excludesItems)) return false
        return true
    }

    override fun validate(gameData: GameData): Boolean {
        val item = gameData.getItemById(itemId)
        if (item == null || item !is Container) return false
        return location != null || carriable != null || open != null || locked != null || containsItems.isNotEmpty() || excludesItems.isNotEmpty()
    }
}

class PreconditionExit (
    val roomId: Int,
    val direction: String,
    val blocked: Boolean?,
    val open: Boolean?,
    val locked: Boolean?,
    val visible: Boolean?
) : Precondition(){

    override fun isSatisfied(gameData: GameData): Boolean {
        if(!validate(gameData)){
            return false
        }
        var result = true
        val room: Room = gameData.getRoomById(roomId)!!
        val exit: Exit = room.exits!![direction]!!
        if(blocked != null && exit.blocked != blocked){
            result = false
        }
        if(open != null && exit.open != open){
            result = false
        }
        if(locked != null && exit.locked != locked){
            result = false
        }
        if(visible != null && exit.visible != visible){
            result = false
        }
        return result
    }

    override fun validate(gameData: GameData): Boolean {
        val room = gameData.getRoomById(roomId)
        if(room == null) {
            return false
        }
        if(room.exits == null || room.exits[direction] == null){
            return false
        }
        // At least one property must be specified for the precondition to be meaningful
        if(blocked == null && open == null && locked == null && visible == null){
            return false
        }
        return true
    }
}

/**
 * Preconditions that must be fulfilled before an action is allowed to execute.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class PreconditionItemsLocation(
    val requiredItems: List<Int> = emptyList(),
    val requiredRoomForItems: Int? = null,
    val requiredContainerForItems: Int? = null
): Precondition() {

    override fun isSatisfied(gameData: GameData): Boolean{
        if(!validate(gameData)) {
            return false
        }
        for (itemId in requiredItems) {
            val item = gameData.getItemById(itemId)!!
            val roomSatisfied = requiredRoomForItems != null && item.location == requiredRoomForItems
            val containerSatisfied = requiredContainerForItems != null && item.location == FixedLocation.CONTAINER.value
                && gameData.getItemContainer(item.id)?.id == requiredContainerForItems
            if(!roomSatisfied && !containerSatisfied)
                return false
        }
        return true
    }

    override fun validate(gameData: GameData): Boolean {
        if (requiredItems.isEmpty()){
            ILogger.current.error("PreconditionItemLocations check failed: empty items list.")
            return false
        }
        if(requiredRoomForItems == null && requiredContainerForItems == null) {
            ILogger.current.error("PreconditionItemLocations check failed: neither required roomId nor required containerId provided!.")
            return false
        }
        for (itemId in requiredItems) {
            val item = gameData.getItemById(itemId)
            if (item == null) {
                ILogger.current.error("PreconditionItemLocations check failed: Item with id '$itemId' not found.")
                return false
            }
        }
        // Only validate requiredRoomForItems if it's a positive room ID (not special location values like 0, -1, -2)
        if (requiredRoomForItems != null && requiredRoomForItems > 0 && gameData.getRoomById(requiredRoomForItems) == null) {
            ILogger.current.error("PreconditionItemLocations check failed: required roomId '$requiredRoomForItems' not found.")
            return false
        }
        if (requiredContainerForItems != null && gameData.getItemById(requiredContainerForItems) == null) {
            ILogger.current.error("PreconditionItemLocations check failed: required containerId '$requiredContainerForItems' not found.")
            return false
        }
        return true
    }
}
