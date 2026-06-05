package net.daddldiddl.jbsadventure.tools

import kotlinx.serialization.json.Json
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.model.*
import java.io.File

/**
 * Handles persistence of [SaveState] to and from a JSON save file on disk.
 *
 * The save file is written to the working directory as `savegame.json`,
 * which is the same directory the JAR is run from.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
object SaveManager {

    private const val SAVE_FILE = "savegame.json"
    private val json = Json { prettyPrint = true }

    /**
     * Writes a [SaveState] to `savegame.json` in the working directory,
     * overwriting any previously saved game.
     *
     * @param gameData The current game state to persist.
     * @param currentRoom The current room the player is in.
     */
    fun save(gameData: GameData, currentRoom: Room) {
        val state = SaveState(
            currentRoomId = currentRoom.id,
            itemStates = gameData.getItemList().associate { item ->
                val container = item as? Container
                item.id to ItemSaveState(
                    itemId = item.id,
                    location = item.location,
                    usable = item.usable,
                    numberOfUses = item.numberOfUses,
                    currentContainerId = gameData.getItemContainer(item.id)?.id,
                    open = container?.open,
                    locked = container?.locked,
                    containedItemIds = container?.getContainedItemIds(),
                    name = item.name,
                    description = item.description
                )
            },
            stateValues = gameData.getStateList().associate { it.stateKey to it.currentValue },
            exitStates = gameData.getRoomList().associate { room ->
                room.id to room.exits.orEmpty().mapValues { (_, exit) ->
                    ExitSaveState(
                        open = exit.open,
                        locked = exit.locked,
                        visible = exit.visible,
                        blocked = exit.blocked,
                        name = exit.name,
                        description = exit.description
                    )
                }
            },
            roomStates = gameData.getRoomList().associate { room ->
                room.id to RoomSaveState(
                    roomId = room.id,
                    name = room.name,
                    description = room.description
                )
            }
        )
        LOG.debug("Saving game state: currentRoomId=${state.currentRoomId}, number of itemSaveStates=${state.itemStates.size}, stateValues=${state.stateValues}")    
        File(SAVE_FILE).writeText(json.encodeToString(state))
    }

    /**
     * Reads and deserializes a [SaveState] from `savegame.json`.
     *
     * @return The restored [SaveState], or `null` if no save file exists.
     * @throws IllegalStateException if the save file exists but cannot be parsed.
     */
    fun load(gameData: GameData): SaveState? {
        val file = File(SAVE_FILE)
        if (!file.exists()) return null
        val state = json.decodeFromString<SaveState>(file.readText())

        // Reset container memberships before rebuilding them from the save snapshot.
        gameData.Containers.values.forEach { container ->
            container.removeItems(container.getContainedItemIds(), gameData)
        }

        state.itemStates.forEach { (itemId, itemState) ->
            val item = gameData.getItemById(itemId)
            if (item != null) {
                // Restore basic item state
                gameData.setItemLocation(itemId, itemState.location)
                gameData.setItemUsable(itemId, itemState.usable)
                itemState.numberOfUses?.let { gameData.setItemNumberOfUses(itemId, it) }

                // Restore mutable name and description
                itemState.name?.let { item.name = it }
                itemState.description?.let { item.description = it }

                // Restore container-only open/lock flags if this item is a container.
                val container = item as? Container
                if (container != null) {
                    itemState.open?.let { container.open = it }
                    itemState.locked?.let { container.locked = it }
                    itemState.containedItemIds?.let { container.addItems(it, gameData) }
                }
            }
        }

        // Backward-compatible fallback: if contained-item lists are not present, use explicit item->container mapping.
        state.itemStates.forEach { (itemId, itemState) ->
            val containerId = itemState.currentContainerId ?: return@forEach
            val container = gameData.getItemById(containerId) as? Container ?: return@forEach
            if (!container.containsItem(itemId)) {
                container.addItem(itemId, gameData)
            }
        }

        // Ensure location flag and container lists are in sync after restore,
        // but do not override explicit non-container locations from itemStates.
        gameData.Containers.values.forEach { container ->
            container.getContainedItemIds().forEach { containedItemId ->
                val explicitLocation = state.itemStates[containedItemId]?.location
                if (explicitLocation == FixedLocation.CONTAINER.value) {
                    gameData.setItemLocation(containedItemId, FixedLocation.CONTAINER.value)
                    if (!container.containsItem(containedItemId)) {
                        container.addItem(containedItemId, gameData)
                    }
                } else if (explicitLocation != null && explicitLocation != FixedLocation.CONTAINER.value) {
                    container.removeItem(containedItemId, gameData)
                }
            }
        }

        state.stateValues.forEach { (stateKey, value) ->
            gameData.setCurrentStateValue(stateKey, value)
        }

        state.exitStates.forEach { (roomId, roomExitStates) ->
            val room = gameData.getRoomById(roomId) ?: return@forEach
            roomExitStates.forEach { (direction, exitState) ->
                val exit = room.exits?.get(direction) ?: return@forEach
                exit.open = exitState.open
                exit.locked = exitState.locked
                exit.visible = exitState.visible
                exit.blocked = exitState.blocked
                exitState.name?.let { exit.name = it }
                exitState.description?.let { exit.description = it }
            }
        }

        state.roomStates.forEach { (roomId, roomState) ->
            val room = gameData.getRoomById(roomId) ?: return@forEach
            roomState.name?.let { room.name = it }
            roomState.description?.let { room.description = it }
        }

        LOG.info("Loaded game state: currentRoomId=${state.currentRoomId}, number of itemSaveStates=${state.itemStates.size}, stateValues=${state.stateValues}")
        return state
    }

    /**
     * Returns `true` if a save file is present in the working directory.
     */
    fun hasSave(): Boolean = File(SAVE_FILE).exists()
}
