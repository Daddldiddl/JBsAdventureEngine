package net.daddldiddl.jbsadventure.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import net.daddldiddl.jbsadventure.model.SaveState
import net.daddldiddl.jbsadventure.model.ItemSaveState
import net.daddldiddl.jbsadventure.model.Room
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.State
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
     * Writes [state] to `savegame.json` in the working directory,
     * overwriting any previously saved game.
     *
     * @param gameData The current game state to persist.
     * @param currentRoom The current room the player is in.
     */
    fun save(gameData: GameData, currentRoom: Room) {
        val state = SaveState(
            currentRoomId = currentRoom.id,
            itemStates = gameData.getItemList().associate { it.id to ItemSaveState(it.id, it.location, it.usable, it.numberOfUses) },
            stateValues = gameData.getStateList().associate { it.stateKey to it.currentValue }
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
        state.itemStates.forEach { (itemId, itemState) ->
            gameData.setItemLocation(itemId, itemState.location)
            gameData.setItemUsable(itemId, itemState.usable)
            itemState.numberOfUses?.let { gameData.setItemNumberOfUses(itemId, it) }
        }
        state.stateValues.forEach { (stateKey, value) ->
            gameData.setCurrentStateValue(stateKey, value)
        }
        LOG.info("Loaded game state: currentRoomId=${state.currentRoomId}, number of itemSaveStates=${state.itemStates.size}, stateValues=${state.stateValues}")
        return state
    }

    /**
     * Returns `true` if a save file is present in the working directory.
     */
    fun hasSave(): Boolean = File(SAVE_FILE).exists()
}
