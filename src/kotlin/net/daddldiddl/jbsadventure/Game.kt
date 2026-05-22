package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.tools.*

/**
 * Main game controller that manages state, processes player commands, and drives the game loop.
 *
 * On construction the game loads all rooms and items from the supplied [GameData]. The caller
 * should invoke [start] once, then repeatedly call [processCommand] with each line of player input
 * until [isRunning] returns `false`.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class Game(private val gameData: GameData) {
    private var currentRoom: Room =
            gameData.getRoomById(1) ?: error("Starting room (id=1) not found in data.json")
    private var running = true

    fun currentStateDebug() {
        val sb = StringBuilder()
        sb.append(
                "Current room: ${currentRoom.name} (id=${currentRoom.id}), containing items: ${gameData.getItemsForRoom(currentRoom.id).joinToString(", ") { "${it.name} (id=${it.id})" }}"
        )
        LOG.debug(sb.toString())
    }

    /**
     * Prints the welcome banner and describes the starting room. Must be called once before the
     * first [processCommand] invocation.
     */
    fun outputWelcome() {
        CONSOLE.print("=".repeat(50))
        CONSOLE.print("Welcome to ${gameData.title}!")
        CONSOLE.print("=".repeat(50))
        CONSOLE.print(gameData.introductionMessage)
        CONSOLE.print()
        CONSOLE.print(
                "Use commands like 'look', 'go north', 'take item', and 'use item' to interact with the environment.\nType 'help' for a list of commands."
        )
        CONSOLE.print()
        describeRoom()
    }

    /**
     * Parses and dispatches a single line of player input to the appropriate handler.
     *
     * Recognised top-level commands: `go`/`move`/direction shortcuts, `look`, `examine`, `use`,
     * `help`, and `quit`.
     *
     * @param input The raw input string entered by the player.
     */
    fun processCommand(input: String) {
        val parts = input.trim().lowercase().split("\\s+".toRegex())
        when (parts[0]) {
            "go",
            "move",
            "e",
            "w",
            "n",
            "s",
            "u",
            "d",
            "east",
            "west",
            "north",
            "south",
            "up",
            "down" -> {
                handleMove(parts)
            }
            "look", "l" -> {                
                describeRoom()
            }
            "examine", "x" -> {
                handleExamine(parts)
            }
            "use" -> {
                handleUse(parts)
            }
            "pickup", "take", "t" -> {
                handlePickup(parts)
            }
            "drop" -> {
                handleDrop(parts)
            }
            "inventory", "i" -> {
                handleInventory()
            }
            "help", "?" -> {
                printHelp()
            }
            "quit", "exit", "q" -> {
                LOG.debug("Handling quit command, exiting game loop")
                CONSOLE.print()
                CONSOLE.print(gameData.exitMessage)
                running = false
            }
            "save" -> {
                saveGame()
            }
            "load" -> {
                loadGame()
            }
            "" -> {
                Unit
            }

            // ignore blank input
            else -> {
                CONSOLE.print("Unknown command '${parts[0]}'. Type 'help' for a list of commands.")
            }
        }
    }

    /**
     * Returns whether the game loop should continue running.
     *
     * @return `true` until the player issues a quit command.
     */
    fun isRunning(): Boolean = running

    /**
     * Handles movement commands, resolving both shorthand (e.g. `n`) and full direction names.
     *
     * @param parts The tokenised command; `parts[0]` is the verb or a bare direction,
     * ```
     *              `parts[1]` (when present) is the explicit direction to travel.
     * ```
     */
    fun handleMove(parts: List<String>) {
        LOG.debug("Handling move command with parts: $parts")
        val part0: String = parts[0].lowercase()
        val dirMap =
                mapOf(
                        "e" to "east",
                        "w" to "west",
                        "n" to "north",
                        "s" to "south",
                        "u" to "up",
                        "d" to "down"
                )
        if (parts.size == 1) {
            if (part0 in dirMap.keys) {
                move(dirMap[part0] ?: "")
            } else if (part0 in dirMap.values) {
                move(part0)
            } else {
                CONSOLE.print("Go where? e.g. 'go north'")
            }
        } else {
            val part1 = parts[1].lowercase()
            if (part1 in dirMap.keys) {
                move(dirMap[part1] ?: "")
            } else if (part1 in dirMap.values) {
                move(part1)
            } else {
                CONSOLE.print("Go where? e.g. 'go north'")
            }
        }
    }

    /**
     * Handles the `examine` command, printing the description of the named item if it is present in
     * the current room.
     *
     * @param parts The tokenised command; words after the verb are joined to form the item name.
     */
    fun handleExamine(parts: List<String>) {
        LOG.debug("Handling examine command with parts: $parts")
        if (parts.size < 2) {
            CONSOLE.print("Examine what? e.g. 'examine key'")
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getItemByNameAndRoom(itemName, currentRoom.id) 
                    ?: gameData.getItemByNameAndRoom(itemName, Item.Constants.INVENTORY_LOCATION)
            if (item == null) {
                CONSOLE.print("There is no '$itemName' here to examine.")
            } else if (item.location != currentRoom.id && item.location != Item.Constants.INVENTORY_LOCATION) {
                CONSOLE.print("You don't see ${item.getArticle()} ${item.name} here to examine.")
            } else {
                if (item.usable == false) {
                    CONSOLE.print(
                            "${item.descriptionWithState(gameData)} It doesn't seem like you can use it right now."
                    )
                    return
                }
                CONSOLE.print(item.descriptionWithState(gameData))
            }
        }
    }

    /**
     * Handles the `use` command, applying the item's [ItemUsage] action if one is defined for the
     * current room.
     *
     * @param parts The tokenised command; words after the verb are joined to form the item name.
     */
    fun handleUse(parts: List<String>) {
        LOG.debug("Handling use command with parts: $parts")
        if (parts.size < 2) {
            CONSOLE.print("Use what? e.g. 'use key'")
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getItemByNameAndRoom(itemName, currentRoom.id) 
                    ?: gameData.getItemByNameAndRoom(itemName, Item.Constants.INVENTORY_LOCATION)
            if (item == null) {
                CONSOLE.print("There is no '${itemName}' here to use.")
            } else if (item.usable == false) {
                CONSOLE.print("You can't use the ${item.name} right now.")
            } else {
                val usage = currentRoom.getItemUsage(item.id)
                if (usage == null) {
                    CONSOLE.print("You can't use the ${item.name} here.")
                } else {
                    var itemUsed :Boolean = false
                    when (usage.action) {
                        ItemAction.MoveTo -> {
                            val targetRoomId =
                                    usage.moveToRoomId
                                            ?: error("moveTo action requires moveToRoomId")
                            LOG.debug(
                                    "Moving player from room '${currentRoom.debugName()}' to room '${gameData.getRoomById(targetRoomId)?.debugName()}' due to use of item '${item.debugName()}'"
                            )
                            currentRoom =
                                    gameData.getRoomById(targetRoomId)
                                            ?: error(
                                                    "Target room $targetRoomId not found in data.json"
                                            )
                            if (item.driveable == true) {
                                // If the item is driveable, we assume it moves with the player to
                                // the new room.
                                LOG.debug(
                                        "Setting item '${item.debugName()}' location to '${gameData.getRoomById(targetRoomId)?.debugName()}' due to being used to move the player"
                                )
                                gameData.setItemLocation(item.id, targetRoomId)
                            }
                            itemUsed = true
                        }
                        ItemAction.SetItemRoom -> {
                            val targetRoomId: Int =
                                    usage.moveToRoomId
                                            ?: error("SetItemRoom action requires moveToRoomId")
                            val itemToMoveId: Int =
                                    usage.affectedItemId
                                            ?: error("SetItemRoom action requires affectedItemId")
                            val itemToMove =
                                    gameData.getItemById(itemToMoveId)
                                            ?: error(
                                                    "Item with id $itemToMoveId not found in data.json"
                                            )
                            LOG.debug(
                                    "Setting item '${itemToMove.debugName()}' location to '${gameData.getRoomById(targetRoomId)?.debugName()}' due to use of item '${item.debugName()}'"
                            )
                            gameData.setItemLocation(itemToMoveId, targetRoomId)
                            itemUsed = true
                        }
                        ItemAction.ChangeState -> {
                            val stateKey: String =
                                    usage.stateKey ?: error("ChangeState action requires stateKey")
                            val newStateValue: String =
                                    usage.newStateValue
                                            ?: error("ChangeState action requires newStateValue")
                            val oldStateValue: String =
                                    usage.oldStateValue
                                            ?: error("ChangeState action requires oldStateValue")
                            val affectedItemId: Int =
                                    usage.affectedItemId
                                            ?: error("ChangeState action requires affectedItemId")
                            val state: State =
                                    usage.getState(gameData.getStateMap())
                                            ?: error(
                                                    "State with key '$stateKey' not found in data.json"
                                            )
                            val affectedItem =
                                    usage.getAffectedItem(gameData.getItemMap())
                                            ?: error(
                                                    "Affected item with id $affectedItemId not found in data.json"
                                            )
                            if (usage.isStateChangeValid(gameData.getStateMap())) {
                                LOG.debug(
                                        "Changing state '$stateKey' of item '${affectedItem.debugName()}' to '$newStateValue' due to use of item '${item.debugName()}'"
                                )
                                state.currentValue = newStateValue
                                if (usage.becomesUsable == true) {
                                    LOG.debug(
                                            "Setting item '${affectedItem.debugName()}' usable=true due to state change from using item '${item.debugName()}'"
                                    )
                                    affectedItem.usable = true
                                }
                                itemUsed = true
                            } else {
                                CONSOLE.print("The ${affectedItem.name} is not ${oldStateValue}.")
                            }
                        }
                        else -> {
                            CONSOLE.print("Unknown action '${usage.action}' for item usage.")
                        }
                    }
                    if (itemUsed){
                        CONSOLE.print(usage.description)
                        if(usage.consumeUsedItem == true) {
                            if(item.numberOfUses != null) {
                                item.numberOfUses = item.numberOfUses!! - 1
                                LOG.debug("Decrementing numberOfUses of item '${item.debugName()}'} by one due to use action)")
                            }
                            if(item.numberOfUses == null || item.numberOfUses!! <= 0) {
                                LOG.debug("Removing item '${item.debugName()}' due to being consumed by use action)")
                                item.location = Item.Constants.NOTASSIGNED_LOCATION
                            }
                        }
                        if(usage.action == ItemAction.MoveTo) {
                            describeRoom()
                        }
                    }
                }
            }
        }
    }

    private fun handlePickup(parts: List<String>) {
        LOG.debug("Handling pickup request")
        if (parts.size < 2) {
            CONSOLE.print("Pickup what? e.g. 'pickup key'")
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getItemByNameAndRoom(itemName, currentRoom.id)
            if (item == null) {
                CONSOLE.print("There is no '${itemName}' here to pickup.")
            } else if (item.carriable == false) {
                CONSOLE.print("You can't pickup the ${item.name}.")
            } else {
                LOG.debug("Setting location of item '${item.debugName()}' to INVENTORY_LOCATION due to pickup")
                gameData.setItemLocation(item.id, Item.Constants.INVENTORY_LOCATION)
                CONSOLE.print("You picked up the ${item.name}.")
            }
        }
    }

    private fun handleDrop(parts: List<String>) {
        LOG.debug("Handling drop request")
        if (parts.size < 2) {
            CONSOLE.print("Drop what? e.g. 'drop key'")
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getItemByNameAndRoom(itemName, Item.Constants.INVENTORY_LOCATION)
            if (item == null) {
                CONSOLE.print("You are not carrying a '$itemName'.")
            } else {
                LOG.debug("Setting location of item '${item.debugName()}' to '${currentRoom.debugName()}' due to drop")
                gameData.setItemLocation(item.id, currentRoom.id)
                CONSOLE.print("You dropped the ${item.name}.")
            }
        }
    }

    private fun handleInventory() {
        LOG.debug("Handling inventory request")
        var inventoryItems = gameData.getItemList().filter { it.location == Item.Constants.INVENTORY_LOCATION } 
        if(inventoryItems.isEmpty()) {
            CONSOLE.print("You are not carrying anything.")
        } else {
            CONSOLE.print("You are carrying: ${inventoryItems.joinToString(", ") { "${it.getArticle()} ${it.name}" }}")
        }
    }


    /** Saves the current game state (player location and all item locations) to `savegame.json`. */
    private fun saveGame() {
        LOG.info("Saving game state...")
        SaveManager.save(gameData, currentRoom)
        CONSOLE.print("Game saved.")
    }

    /**
     * Loads a previously saved game state from `savegame.json`, restoring the player's room and all
     * item locations. Prints an error if no save file exists.
     */
    private fun loadGame() {
        LOG.info("Loading game state...")
        val state = SaveManager.load(gameData)
        if (state == null) {
            CONSOLE.print("No saved game found.")
            return
        }
        currentRoom =
                gameData.getRoomById(state.currentRoomId)
                        ?: error("Saved room id=${state.currentRoomId} not found in data.json")
        CONSOLE.print("Game loaded.")
        describeRoom()
    }

    /**
     * Attempts to move the player in the given direction.
     *
     * @param direction The direction to travel (e.g. `"north"`, `"up"`).
     */
    private fun move(direction: String) {
        LOG.debug("Attempting to move from '${currentRoom.debugName()}' in direction '$direction'")
        if (currentRoom.exits.isEmpty()) {
            CONSOLE.print("There are no exits from this room.")
            return
        }
        if (!currentRoom.exits.containsKey(direction)) {
            CONSOLE.print("You can't go $direction from here.")
            return
        }
        val nextRoomId = currentRoom.exits[direction]
        if (nextRoomId == null || !gameData.getRoomMap().containsKey(nextRoomId)) {
            CONSOLE.print("You can't go $direction from here (Room $nextRoomId not found).")
            return
        }
        currentRoom =
                gameData.getRoomById(nextRoomId)
                        ?: error("Room with id $nextRoomId not found in data.json")
        describeRoom()
    }

    /**
     * Prints the name, description, available exits, and visible items of the current room to
     * stdout.
     */
    private fun describeRoom() {
        LOG.debug("Describing room '${currentRoom.debugName()}' with id ${currentRoom.id}")
        CONSOLE.print()
        CONSOLE.print(currentRoom.description)
        val exitList = currentRoom.exits.keys.joinToString(", ")
        CONSOLE.print("Exits: " + if (exitList.isNotEmpty()) "$exitList" else "none")
        val itemList = gameData.getItemsForRoom(currentRoom.id).joinToString(", ") { "${it.getArticle()} ${it.name}" }
        if (itemList.isNotEmpty()) {
            CONSOLE.print("There is: $itemList")
        }
        CONSOLE.print()
    }

    /** Prints a summary of all available player commands to stdout. */
    private fun printHelp() {
        LOG.debug("Handling help request, printing help message")
        CONSOLE.print()
        CONSOLE.print("Available commands:")
        CONSOLE.print(
                "  go <direction>  - Move in a direction (north, south, east, west, up, down)."
        )
        CONSOLE.print(
                "                    You can also use direction shortcuts: 'n', 's', 'e', 'w', 'u', 'd'"
        )
        CONSOLE.print("  look            - Describe your current surroundings (shortcut: 'l').")
        CONSOLE.print(
                "  examine <item>  - Examine an item in the room, e.g. 'examine key' (shortcut: 'x')."
        )
        CONSOLE.print("  use <item>      - Use an item in the room, e.g. 'use key'.")
        CONSOLE.print(
                "  pickup <item>   - Pick up a carriable item in the room, e.g. 'pickup key' (shortcut: 't')."
        )
        CONSOLE.print(
                "  drop <item>     - Drop an item from your inventory, e.g. 'drop key'."
        )
        CONSOLE.print(
                "  inventory       - Show the items you are currently carrying (shortcut: 'i')."
        )
        CONSOLE.print("  help            - Show this help message (shortcut: '?').")
        CONSOLE.print("  save            - Save the current game state to savegame.json.")
        CONSOLE.print("  load            - Load a previously saved game state.")
        CONSOLE.print("  quit            - Exit the game (shortcut: 'q').")
        CONSOLE.print()
    }
}
