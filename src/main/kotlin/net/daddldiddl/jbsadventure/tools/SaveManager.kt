package net.daddldiddl.jbsadventure.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import net.daddldiddl.jbsadventure.model.SaveState
import net.daddldiddl.jbsadventure.model.ItemSaveState
import net.daddldiddl.jbsadventure.model.ExitSaveState
import net.daddldiddl.jbsadventure.model.Room
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.Container
import net.daddldiddl.jbsadventure.model.FixedLocation
import net.daddldiddl.jbsadventure.LOG
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
                    containedItemIds = container?.getContainedItemIds()
                )
            },
            stateValues = gameData.getStateList().associate { it.stateKey to it.currentValue },
            exitStates = gameData.getRoomList().associate { room ->
                room.id to room.exits.orEmpty().mapValues { (_, exit) ->
                    ExitSaveState(
                        open = exit.open,
                        locked = exit.locked,
                        visible = exit.visible,
                        blocked = exit.blocked
                    )
                }
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
        gameData.getContainerList().forEach { container ->
            container.removeItems(container.getContainedItemIds())
        }

        state.itemStates.forEach { (itemId, itemState) ->
            gameData.setItemLocation(itemId, itemState.location)
            gameData.setItemUsable(itemId, itemState.usable)
            itemState.numberOfUses?.let { gameData.setItemNumberOfUses(itemId, it) }

            // Restore container-only open/lock flags if this item is a container.
            val container = gameData.getItemById(itemId) as? Container
            if (container != null) {
                itemState.open?.let { container.open = it }
                itemState.locked?.let { container.locked = it }
                itemState.containedItemIds?.let { container.addItems(it) }
            }
        }

        // Backward-compatible fallback: if contained-item lists are not present, use explicit item->container mapping.
        state.itemStates.forEach { (itemId, itemState) ->
            val containerId = itemState.currentContainerId ?: return@forEach
            val container = gameData.getItemById(containerId) as? Container ?: return@forEach
            if (!container.containsItem(itemId)) {
                container.addItem(itemId)
            }
        }

        // Ensure location flag and container lists are in sync after restore.
        gameData.getContainerList().forEach { container ->
            container.getContainedItemIds().forEach { containedItemId ->
                gameData.setItemLocation(containedItemId, FixedLocation.CONTAINER.value)
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
            }
        }

        LOG.info("Loaded game state: currentRoomId=${state.currentRoomId}, number of itemSaveStates=${state.itemStates.size}, stateValues=${state.stateValues}")
        return state
    }

    /**
     * Returns `true` if a save file is present in the working directory.
     */
    fun hasSave(): Boolean = File(SAVE_FILE).exists()
}
