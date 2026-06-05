package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.model.actions.ActionType
import net.daddldiddl.jbsadventure.tools.ConsoleColor
import net.daddldiddl.jbsadventure.tools.SaveManager

/**
 * Main game controller that manages state, processes player commands, and drives the game loop.
 *
 * On construction the game loads all rooms and items from the supplied [GameData]. The caller
 * should invoke [printWelcome] once, then repeatedly call [processCommand] with each line of player input
 * until [isRunning] returns `false`.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class Game(private val gameData: GameData) {

    private var running = true

    /** Logs current room and room items for debugging purposes. */
    fun currentStateDebug() {
        LOG.debug("Current room: ${DATA.currentRoom.name} (id=${DATA.currentRoom.id}), containing items: ${gameData.getItemsForRoom(DATA.currentRoom.id).joinToString(", ") { "${it.name} (id=${it.id})" }}")
    }

    /**
     * Prints the welcome banner and describes the starting room. Must be called once before the
     * first [processCommand] invocation.
     */
    fun printWelcome() {
        val welcome = LANG.getTemplate(Keys.Message.msgWelcome).replace(Keys.StandIn.title, gameData.title)
        CONSOLE.print("=".repeat(welcome.length), ConsoleColor.LIGHTGREEN)
        CONSOLE.print(welcome, ConsoleColor.LIGHTGREEN)
        CONSOLE.print("=".repeat(welcome.length), ConsoleColor.LIGHTGREEN)
        CONSOLE.print()
        CONSOLE.print(gameData.introductionMessage, ConsoleColor.WHITE)
        CONSOLE.print()
        CONSOLE.print(LANG.getTemplate(Keys.Message.msgIntroHelp), ConsoleColor.LIGHTYELLOW)
        CONSOLE.print()
        describeRoom()
        CONSOLE.print()
    }

    /**
     * Prints the goodbye message when the player exits the game.
     */
    fun printGoodbye() {
        CONSOLE.print(
            LANG.getTemplate(Keys.Message.msgGoodbye)
                .replace(Keys.StandIn.title, gameData.title),
            ConsoleColor.LIGHTGREEN
        )
    }

    /**
     * Cleans up and filters the input string by removing unwanted characters and splitting into tokens.
     *
     * @param input The raw input string entered by the player.
     * @return A list of cleaned and filtered tokens.
     */
    private fun cleanupAndFilterInput(input: String): List<String> {
        val rgxUnwantedChars = "[^\\w\\s]+".toRegex()
        return input.trim().lowercase().replace(rgxUnwantedChars, " ").split("\\s+".toRegex())
            .filter { it !in LANG.partsToIgnore }
            .filter { !it.isBlank() }
    }

    /**
     * Parses and dispatches a single line of player input to the appropriate handler.
     *
     * Recognised top-level commands include movement (`go`/direction aliases),
     * item interaction (`examine`, `use`, `take`, `drop`, `open`, `close`),
     * utility commands (`inventory`, `help`, `save`, `load`) and `quit`.
     *
     * @param input The raw input string entered by the player.
     */
    fun processCommand(input: String) {
        CONSOLE.print()
        val parts = cleanupAndFilterInput(input)
        if(parts.isEmpty()) {
            return
        }
        LOG.debug("Processing input string: \"$input\", filtered to: $parts")
        when (parts[0]) {
            in LANG.getAllDirectionAliases().union(LANG.getCommandAliases(Keys.Command.go)) -> {
                handleMove(parts)
            }
            in LANG.getCommandAliases(Keys.Command.look) -> {
                describeRoom()
            }
            in LANG.getCommandAliases(Keys.Command.examine) -> {
                handleExamine(parts)
            }
            in LANG.getCommandAliases(Keys.Command.use) -> {
                handleUse(parts)
            }
            in LANG.getCommandAliases(Keys.Command.take) -> {
                handlePickup(parts)
            }
            in LANG.getCommandAliases(Keys.Command.drop) -> {
                handleDrop(parts)
            }
            in LANG.getCommandAliases(Keys.Command.open)
                    + LANG.getCommandAliases(Keys.Command.close) -> {
                handleOpenClose(parts)
            }
            in LANG.getCommandAliases(Keys.Command.lock)
                    + LANG.getCommandAliases(Keys.Command.unlock) -> {
                handleLockUnlock(parts)
            }
            in LANG.getCommandAliases(Keys.Command.inventory) -> {
                handleInventory()
            }
            in LANG.getCommandAliases(Keys.Command.help) -> {
                printHelp()
            }
            in LANG.getCommandAliases(Keys.Command.quit) -> {
                LOG.debug("Handling quit command, exiting game loop")
                CONSOLE.print()
                CONSOLE.print(gameData.exitMessage)
                running = false
            }
            in LANG.getCommandAliases(Keys.Command.save) -> {
                saveGame()
            }
            in LANG.getCommandAliases(Keys.Command.load) -> {
                loadGame()
            }
            else -> {
                if(parts.size == 1 && parts[0].isBlank()) {
                    return
                } else {
                    CONSOLE.warn(
                        LANG.getTemplate(Keys.Message.msgUnknownCommandWithHelp)
                            .replace(Keys.StandIn.command, parts.first())
                    )
                }

            }
        }
        CONSOLE.print()
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
        if (parts.size == 1) {
            if (part0 in LANG.getAllDirectionAliases()) {
                move(LANG.getDirectionKeyFromAlias(part0) ?: "")
            } else {
                CONSOLE.print(LANG.getTemplate(Keys.Message.msgGoWhere))
            }
        } else {
            val part1 = parts[1].lowercase()
            if (part1 in LANG.getAllDirectionAliases()) {
                move(LANG.getDirectionKeyFromAlias(part1) ?: part1)
            } else {
                CONSOLE.print(LANG.getTemplate(Keys.Message.msgGoWhere))
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

        var namedEntity: NamedEntity? = null
        var itemName: String? = null
        var item: Item? = null
        var exit: Exit? = null
        if(parts.size > 1) {
            itemName = parts.drop(1).joinToString(" ")
            item = gameData.getAllAccessibleItemsForRoom(DATA.currentRoom.id).find { it.matchesName(itemName) }
            exit = gameData.currentRoom.exits?.values?.filter { it.nameMatches(itemName) }?.firstOrNull()
            if (item == null && exit == null) {
                CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoItemFound).replace(Keys.StandIn.name, itemName))
                return
            }
        }

        namedEntity = item ?: exit ?: gameData.currentRoom
        CONSOLE.print(namedEntity.getDetailedDescription())

        if(namedEntity.onExamine.isNotEmpty()) {
            namedEntity.onExamine.forEach { action ->
                if (action.checkPreconditions(gameData)) {
                    val executed = action.execute(gameData)
                    LOG.debug("Executed onExamine action '${action.type}' for '${namedEntity.debugName()}', success=$executed, descriptionPresent=${!action.description.isNullOrBlank()}")
                    if (executed && !action.description.isNullOrBlank()) {
                        CONSOLE.print(action.description)
                    }
                } else {
                    LOG.debug("Skipping onExamine action '${action.type}' for '${namedEntity.debugName()}' because preconditions are not met")
                }
            }
        }
    }

    /**
     * Handles opening or closing a door or container, depending on the command alias used.
     * Validates that the target entity supports the requested action, is present in the current room,
     * and is not already in the desired state.
     * Also checks for locked or blocked states that would prevent opening/closing.
     */
    fun handleOpenClose(parts: List<String>) {
        LOG.debug("Handling open/close command with parts: $parts")
        val part0: String = parts[0].lowercase()
        val cmd = LANG.getCommandFromAlias(part0) ?: ""
        val itemName  = parts.drop(0).joinToString { " " }.trim()
        if(cmd.isEmpty() || itemName.isEmpty() || cmd !in listOf(Keys.Command.open, Keys.Command.close)) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgCommandWhat)
                .replace(Keys.StandIn.command, LANG.getCommandAlias(cmd)))
            return
        }
        val isOpen = cmd == Keys.Command.open
        val entity = gameData.getAllAccessibleOpenLockEntitiesForRoom(gameData.currentRoom.id)
            .filter { it.nameMatches(itemName) }
            .firstOrNull()
        if(entity == null) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoDoorOrContainerFound)
                .replace(Keys.StandIn.name, itemName))
        } else if(entity is Exit && entity.blocked){
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgExitIsBlocked)
            ))
        } else if (entity.supportsOpenClose != true){
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgDoesNotSupportOpenClose)
            ))
        } else if(isOpen && entity.isOpen()) {
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityAlreadyOpen)
            ))
        } else if (!isOpen && !entity.isOpen()) {
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityAlreadyClosed)
            ))
        } else if(entity.isLocked()){
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityLocked)
            ))
        } else if (isOpen) {
            entity.open()
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityOpened)
            ))
        } else {
            entity.close()
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityClosed)
            ))
        }
    }

    fun handleLockUnlock(parts: List<String>) {
        LOG.debug("Handling (un)lock command with parts: $parts")
        val part0: String = parts[0].lowercase()
        val cmd = LANG.getCommandFromAlias(part0) ?: ""
        val itemName  = parts.drop(0).joinToString { " " }.trim()
        if(cmd.isEmpty() || itemName.isEmpty() || cmd !in listOf(Keys.Command.lock, Keys.Command.unlock)) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgCommandWhat)
                .replace(Keys.StandIn.command, LANG.getCommandAlias(cmd)))
            return
        }
        val isLock = cmd == Keys.Command.lock
        val entity = gameData.getAllAccessibleOpenLockEntitiesForRoom(gameData.currentRoom.id)
            .filter { it.nameMatches(itemName) }
            .firstOrNull()
        // pre-check: ensure entity exists, supports lock/unlock and isn't blocked
        if(entity == null) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoDoorOrContainerFound)
                .replace(Keys.StandIn.name, itemName))
            return
        } else if(entity is Exit && entity.blocked){
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgExitIsBlocked)
            ))
            return
        } else if (entity.supportsLockUnlock != true){
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgDoesNotSupportLockUnlock)
            ))
            return
        }
        // check for key
        val key : Item? = gameData.getAllAccessibleItemsForRoom(gameData.currentRoom.id)
                .find { it.id == entity.keyId }
        if ( entity.keyId == null) {
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(if (isLock) Keys.Message.msgLockRequiresNoKey else Keys.Message.msgUnlockRequiresNoKey)
            ))
        } else if(key == null) {
            CONSOLE.print(
                entity.replacePlaceholdersName(
                    LANG.getTemplate(if (isLock) Keys.Message.msgLockKeyMissing else Keys.Message.msgUnlockKeyMissing)
                )
            )
        } else if (isLock && entity.isLocked()) {
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityAlreadyLocked)
            ))
        } else if (!isLock && !entity.isLocked()) {
        CONSOLE.print(
            entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityAlreadyUnlocked)
            ))
        } else if(entity.isLocked()){
            entity.unlock()
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityUnlocked)
            ))
        } else {
            entity.lock()
            CONSOLE.print(entity.replacePlaceholdersName(
                LANG.getTemplate(Keys.Message.msgEntityLocked)
            ))
        }
    }

    /**
     * Handles the `use` command, applying the item's [ItemUsage] actions if one is defined for the
     * current room.
     *
     * @param parts The tokenised command; words after the verb are joined to form the item name.
     */
    fun handleUse(parts: List<String>) {
        LOG.debug("Handling use command with parts: $parts")
        if (parts.size < 2) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgCommandWhat)
                .replace(Keys.StandIn.command, parts[0]))
            return
        }

        val itemName = parts.drop(1).joinToString(" ")
        val item = gameData.getAllAccessibleItemsForRoom(DATA.currentRoom.id).find { it.matchesName(itemName) }
        if (item == null) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoItemFound).replace(Keys.StandIn.name, itemName))
            return
        }
        if (item.usable == false) {
            CONSOLE.print(item.replacePlaceholdersName(LANG.getTemplate(Keys.Message.msgItemNotUsable)))
            return
        }

        val usage = DATA.currentRoom.getItemUsage(item.id)
        if (usage == null) {
            CONSOLE.print(item.replacePlaceholdersName(LANG.getTemplate(Keys.Message.msgItemNotUsable)))
            return
        }

        var itemUsed = false
        var movedToAnotherRoom = false

        for (action in usage.actions) {
            if (!action.checkPreconditions(gameData)) {
                LOG.debug("Skipping action '${action.type}' for item '${item.debugName()}' because preconditions are not met")
                continue
            }

            val executed = action.execute(gameData)
            if (!executed) {
                LOG.debug("Action '${action.type}' for item '${item.debugName()}' failed during execution")
                continue
            }

            itemUsed = true
            if (action.type == ActionType.MoveTo) {
                movedToAnotherRoom = true
                if (item.driveable == true) {
                    LOG.debug("Keeping driveable item '${item.debugName()}' with player in room '${gameData.currentRoom.debugName()}'")
                    gameData.setItemLocation(item.id, gameData.currentRoom.id)
                }
            }

            LOG.debug(
                "Executed action '${action.type}' for item '${item.debugName()}'; descriptionPresent=${!action.description.isNullOrBlank()}"
            )
            if (!action.description.isNullOrBlank()) {
                CONSOLE.print(action.description)
            }
        }

        if (!itemUsed) {
            CONSOLE.print(item.replacePlaceholdersName(LANG.getTemplate(Keys.Message.msgItemNotUsable)))
            return
        }

        if (usage.becomesUsable == true) {
            item.usable = true
        }
        if (usage.consumeUsedItem == true) {
            if (item.numberOfUses != null) {
                item.numberOfUses = item.numberOfUses!! - 1
                LOG.debug("Decrementing numberOfUses of item '${item.debugName()}' by one due to use action")
            }
            if (item.numberOfUses == null || item.numberOfUses!! <= 0) {
                LOG.debug("Removing item '${item.debugName()}' due to being consumed by use action")
                item.location = FixedLocation.NOT_ASSIGNED.value
            }
        }
        if (movedToAnotherRoom) {
            describeRoom()
        }
    }

    /** Handles taking an accessible item from the current room into the inventory. */
    private fun handlePickup(parts: List<String>) {
        LOG.debug("Handling pickup request")
        if (parts.size < 2) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgCommandWhat)
                .replace(Keys.StandIn.command, parts[0]))
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getAccessibleItemByNameAndRoom(itemName, DATA.currentRoom.id)
            if (item == null) {
                CONSOLE.print(
                    LANG.getTemplate(Keys.Message.msgNoItemFound)
                        .replace(Keys.StandIn.name, itemName)
                )
            } else if (item.carriable == false) {
                CONSOLE.print(
                    LANG.getTemplate(Keys.Message.msgItemNotCarriable)
                        .replace(Keys.StandIn.definiteName, item.getDescriptiveName(definite = true))
                )
            } else {
                LOG.debug("Setting location of item '${item.debugName()}' to INVENTORY_LOCATION due to pickup")
                gameData.setItemLocation(item.id, FixedLocation.INVENTORY.value)
                CONSOLE.print(
                    LANG.getTemplate(Keys.Message.msgItemTaken)
                        .replace(Keys.StandIn.definiteName, item.getDescriptiveName(definite = true))
                )
            }
        }
    }

    /** Handles dropping an inventory item into the current room. */
    private fun handleDrop(parts: List<String>) {
        LOG.debug("Handling drop request")
        if (parts.size < 2) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgCommandWhat)
                .replace(Keys.StandIn.command, parts[0]))
        } else {
            val itemName = parts.drop(1).joinToString(" ")
            val item = gameData.getAccessibleItemByNameAndRoom(itemName, FixedLocation.INVENTORY.value)
            if (item == null) {
                CONSOLE.print(
                    LANG.getTemplate(Keys.Message.msgNoInventoryItem)
                        .replace(Keys.StandIn.indefiniteName, itemName)
                )
            } else {
                LOG.debug("Setting location of item '${item.debugName()}' to '${DATA.currentRoom.debugName()}' due to drop")
                gameData.setItemLocation(item.id, DATA.currentRoom.id)
                CONSOLE.print(
                    LANG.getTemplate(Keys.Message.msgItemDropped)
                        .replace(Keys.StandIn.definiteName, item.getDescriptiveName(definite = true))
                )
            }
        }
    }

    /** Prints the player's inventory using localized templates. */
    private fun handleInventory() {
        LOG.debug("Handling inventory request")
        val itemList = gameData.getItemsForRoom(FixedLocation.INVENTORY.value)
            .joinToString(", ") { it.getDescriptiveName(definite = false) }
        if(itemList.isEmpty()) {
            CONSOLE.print(LANG.getTemplate(Keys.Part.noInventory), ConsoleColor.CYAN)
        } else {
            CONSOLE.print(
                LANG.getTemplate(Keys.Part.inventory).replace(Keys.StandIn.items, itemList),
                ConsoleColor.CYAN
            )
        }
    }


    /** Saves the current game state (player location and all item locations) to `savegame.json`. */
    private fun saveGame() {
        LOG.info("Saving game state...")
        SaveManager.save(gameData, DATA.currentRoom)
        CONSOLE.print(LANG.getTemplate(Keys.Message.msgGameSaved))
    }

    /**
     * Loads a previously saved game state from `savegame.json`, restoring the player's room and all
     * item locations. Prints an error if no save file exists.
     */
    private fun loadGame() {
        LOG.info("Loading game state...")
        val state = SaveManager.load(gameData)
        if (state == null) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoSavedGameFound))
            return
        }
        DATA.currentRoom =
                gameData.getRoomById(state.currentRoomId)
                        ?: error("Saved room id=${state.currentRoomId} not found in data.json")
        CONSOLE.print(LANG.getTemplate(Keys.Message.msgGameLoaded))
        describeRoom()
    }

    /**
     * Attempts to move the player in the given direction.
     *
     * @param direction The direction to travel (e.g. `"north"`, `"up"`).
     */
    private fun move(direction: String) {
        LOG.debug("Attempting to move from '${DATA.currentRoom.debugName()}' in direction '$direction'")
        val exits = DATA.currentRoom.exits ?: emptyMap()
        if (exits.isEmpty()) {
            CONSOLE.print(LANG.getTemplate(Keys.Message.msgNoExitsFromRoom))
            return
        }
        if (!exits.containsKey(direction)) {
            CONSOLE.print(
                LANG.getTemplate(Keys.Message.msgNoExitsFromRoom)
                    .replace(Keys.StandIn.direction, LANG.getDirectionAliasFromKey(direction))
            )
            return
        }
        val exit = exits[direction]
        if (exit == null || !exit.visible) {
            CONSOLE.print(
                LANG.getTemplate(Keys.Message.msgNoExitInDirection)
                    .replace(Keys.StandIn.direction, LANG.getDirectionAliasFromKey(direction))
            )
            return
        }
        if (!exit.isOpen()) {
            val message = if (exit.blocked) {
                exit.blockedDescription ?: LANG.getTemplate(Keys.Message.msgExitIsBlocked)
            } else if (exit.isLocked()) {
                LANG.getTemplate(Keys.Message.msgEntityLocked)
            } else {
                LANG.getTemplate(Keys.Message.msgEntityClosed)
            }
            CONSOLE.print(exit.replacePlaceholdersName(message))
            return
        }
        val nextRoomId = exit.targetRoomId
        if (!gameData.getRoomMap().containsKey(nextRoomId)) {
            CONSOLE.print(
                LANG.getTemplate(Keys.Message.msgNoExitInDirection)
                    .replace(Keys.StandIn.direction, LANG.getDirectionAliasFromKey(direction))
            )
            LOG.error("The exit in direction $direction for room ${gameData.currentRoom.debugName()} links to an invalid roomId=$nextRoomId!")
            return
        }
        DATA.currentRoom =
                gameData.getRoomById(nextRoomId)
                        ?: error("Room with id $nextRoomId not found in data.json")
        describeRoom()
    }

    /**
     * Prints the name, description, available exits, and visible items of the current room to
     * stdout.
     */
    private fun describeRoom() {
        LOG.debug("Describing room '${DATA.currentRoom.debugName()}' with id ${DATA.currentRoom.id}")
        CONSOLE.print("-".repeat(DATA.currentRoom.name.name.length), ConsoleColor.LIGHTCYAN)
        CONSOLE.print(DATA.currentRoom.name.name, ConsoleColor.LIGHTCYAN)
        CONSOLE.print("-".repeat(DATA.currentRoom.name.name.length), ConsoleColor.LIGHTCYAN)
        CONSOLE.print(DATA.currentRoom.description, ConsoleColor.WHITE)
        val exitList = DATA.getVisibleExitsForRoom(DATA.currentRoom.id)
            .map { it.getDescriptiveName() }.joinToString( ", " )
        if(exitList.isNotEmpty()) {
            CONSOLE.print(
                LANG.getTemplate(Keys.Part.exits)
                    .replace(Keys.StandIn.exits, exitList),
                ConsoleColor.LIGHTYELLOW
            )
        } else {
            CONSOLE.print(LANG.getTemplate(Keys.Part.noExits), ConsoleColor.LIGHTGREEN)
        }
        val itemList = gameData.getItemsForRoom(DATA.currentRoom.id)
            .joinToString(", ") { it.getDescriptiveName(definite = false) }
        if (itemList.isNotEmpty()) {
            CONSOLE.print(
                LANG.getTemplate(Keys.Part.items).replace(Keys.StandIn.items, itemList),
                ConsoleColor.LIGHTGREEN
            )
        }
    }

    /** Prints a summary of all available player commands to stdout. */
    private fun printHelp() {
        LOG.debug("Handling help request, printing help message")
        CONSOLE.print()
        val sb = StringBuilder()
        sb.append("${ConsoleColor.CYAN}${LANG.headings[Keys.Headings.commandsHeading] ?: Keys.Headings.commandsHeading}:\n")
        val commandKeys = listOf(
            Keys.Command.help,
            Keys.Command.look,
            Keys.Command.examine,
            Keys.Command.use,
            Keys.Command.take,
            Keys.Command.drop,
            Keys.Command.open,
            Keys.Command.close,
            Keys.Command.inventory,
            Keys.Command.go,
            Keys.Command.save,
            Keys.Command.load,
            Keys.Command.quit,
        )
        for (key in commandKeys) {
            sb.append("${ConsoleColor.LIGHTYELLOW}- ${LANG.getCommandAliases(key).joinToString(", ")}: ${ConsoleColor.WHITE}${LANG.commands[key]?.description ?: ""}\n")
        }
        sb.append("\n${ConsoleColor.CYAN}${LANG.headings[Keys.Headings.directionsHeading] ?: Keys.Headings.directionsHeading}:\n${ConsoleColor.LIGHTYELLOW}")
        sb.append(LANG.getAllDirectionAliases().joinToString(", "))
        CONSOLE.print(sb.toString())
    }
}
