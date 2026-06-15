package net.daddldiddl.jbsadventure.editor.io

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.json.Json
import net.daddldiddl.jbsadventure.editor.model.ActionDraft
import net.daddldiddl.jbsadventure.editor.model.EditorSession
import net.daddldiddl.jbsadventure.editor.model.ExitDraft
import net.daddldiddl.jbsadventure.editor.model.ItemDraft
import net.daddldiddl.jbsadventure.editor.model.ItemUsageDraft
import net.daddldiddl.jbsadventure.editor.model.NameDraft
import net.daddldiddl.jbsadventure.editor.model.PreconditionDraft
import net.daddldiddl.jbsadventure.editor.model.RoomDraft
import net.daddldiddl.jbsadventure.editor.model.StateDraft
import java.io.File

object EditorPersistence {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun saveSession(file: File, session: EditorSession) {
        val adventure = session.toAdventureFile()
        file.writeText(json.encodeToString(AdventureFile.serializer(), adventure))
    }

    fun loadSession(file: File): EditorSession {
        val text = file.readText()
        val adventure = json.decodeFromString(AdventureFile.serializer(), text)
        return adventure.toEditorSession()
    }
}

private fun EditorSession.toAdventureFile(): AdventureFile {
    val stateFiles = states.map { state ->
        val values = state.possibleValues.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(state.currentValue.ifBlank { "default" }) }

        StateFile(
            stateKey = state.key,
            currentValue = state.currentValue.ifBlank { values.first() },
            description = "",
            possibleValues = values
        )
    }

    return AdventureFile(
        title = metadata.title,
        introductionMessage = metadata.intro,
        exitMessage = metadata.outro,
        rooms = rooms.map { room ->
            RoomFile(
                id = room.id,
                name = room.nameDraft.toNameFile(),
                description = room.description,
                exits = room.exits.map { exit ->
                    ExitFile(
                        direction = exit.direction,
                        targetRoomId = exit.targetRoomId,
                        name = exit.nameDraft.toNameFileOrNull(),
                        supportsOpenClose = exit.supportsOpenClose,
                        supportsLockUnlock = exit.supportsLockUnlock,
                        open = exit.open,
                        locked = exit.locked,
                        visible = exit.visible,
                        blocked = exit.blocked,
                        keyId = if (exit.keyId > 0) exit.keyId else null,
                        consumeKeyOnLock = if (exit.consumeKeyOnLock) true else null,
                        consumeKeyOnUnlock = if (exit.consumeKeyOnUnlock) true else null,
                        onExamine = exit.onExamine.map { it.toTriggerActionFile() }.ifEmpty { null },
                        onOpen = exit.onOpen.map { it.toTriggerActionFile() }.ifEmpty { null },
                        onClose = exit.onClose.map { it.toTriggerActionFile() }.ifEmpty { null },
                        onLock = exit.onLock.map { it.toTriggerActionFile() }.ifEmpty { null },
                        onUnlock = exit.onUnlock.map { it.toTriggerActionFile() }.ifEmpty { null }
                    )
                },
                onExamine = room.onExamine.map { it.toTriggerActionFile() }.ifEmpty { null },
                itemUsages = room.itemUsages.map { usage ->
                    ItemUsageFile(
                        itemId = usage.itemId,
                        actions = usage.actions.map { it.toTriggerActionFile() },
                        becomesUsable = if (usage.becomesUsable) true else null,
                        consumeUsedItem = if (usage.consumeUsedItem) true else null
                    )
                }.ifEmpty { null }
            )
        },
        items = items.map { item ->
            ItemFile(
                id = item.id,
                name = item.nameDraft.toNameFile(),
                description = item.description,
                location = item.location,
                carriable = item.carriable,
                usable = item.usable,
                driveable = if (item.driveable) true else null,
                numberOfUses = if (item.numberOfUses > 0) item.numberOfUses else null,
                stateKey = item.stateKey.ifBlank { null },
                onExamine = item.onExamine.map { it.toTriggerActionFile() }.ifEmpty { null },
                onUse = item.onUse.map { it.toTriggerActionFile() }.ifEmpty { null }
            )
        },
        states = stateFiles,
        editorActions = actions.map { action ->
            ActionFile(
                type = action.type,
                description = action.description,
                bindingScope = action.bindingScope,
                bindingTargetId = action.bindingTargetId,
                bindingRoomId = action.bindingRoomId,
                bindingDirection = action.bindingDirection,
                changedStateKey = action.changedStateKey,
                newStateValue = action.newStateValue,
                moveToRoomId = action.moveToRoomId,
                affectedItemIdsCsv = action.affectedItemIdsCsv,
                moveToRoomIdForItems = action.moveToRoomIdForItems,
                transformsIntoItemIdsCsv = action.transformsIntoItemIdsCsv,
                modifyExitRoomId = action.modifyExitRoomId,
                modifyExitDirection = action.modifyExitDirection,
                modifyExitOpen = action.modifyExitOpen,
                modifyExitLocked = action.modifyExitLocked,
                modifyExitBlocked = action.modifyExitBlocked,
                modifyExitVisible = action.modifyExitVisible,
                modifyContainerId = action.modifyContainerId,
                modifyContainerOpen = action.modifyContainerOpen,
                modifyContainerLocked = action.modifyContainerLocked,
                preconditions = action.preconditions.map { it.toPreconditionFile() }
            )
        },
        // Legacy top-level preconditions kept for backward compatibility only.
        editorPreconditions = emptyList()
    )
}

private fun AdventureFile.toEditorSession(): EditorSession {
    return EditorSession(
        metadata = net.daddldiddl.jbsadventure.editor.model.AdventureMetadata(
            title = title,
            intro = introductionMessage,
            outro = exitMessage
        ),
        rooms = rooms.map { room ->
            RoomDraft(
                id = room.id,
                name = room.name.name,
                description = room.description,
                exits = room.exits.map { exit ->
                    ExitDraft(
                        direction = exit.direction,
                        targetRoomId = exit.targetRoomId,
                        supportsOpenClose = exit.supportsOpenClose ?: false,
                        supportsLockUnlock = exit.supportsLockUnlock ?: false,
                        open = exit.open ?: true,
                        locked = exit.locked ?: false,
                        visible = exit.visible ?: true,
                        blocked = exit.blocked ?: false,
                        keyId = exit.keyId ?: 0,
                        consumeKeyOnLock = exit.consumeKeyOnLock ?: false,
                        consumeKeyOnUnlock = exit.consumeKeyOnUnlock ?: false,
                        onExamine = (exit.onExamine ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                        onOpen = (exit.onOpen ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                        onClose = (exit.onClose ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                        onLock = (exit.onLock ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                        onUnlock = (exit.onUnlock ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf())
                    ).also { d -> exit.name?.applyToNameDraft(d.nameDraft) }
                }.toCollection(mutableStateListOf()),
                onExamine = (room.onExamine ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                itemUsages = (room.itemUsages ?: emptyList()).map { usage ->
                    ItemUsageDraft(
                        itemId = usage.itemId,
                        actions = usage.actions.map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                        becomesUsable = usage.becomesUsable ?: false,
                        consumeUsedItem = usage.consumeUsedItem ?: false
                    )
                }.toCollection(mutableStateListOf())
            ).also { d -> room.name.applyToNameDraft(d.nameDraft) }
        }.toCollection(mutableStateListOf()),
        items = items.map { item ->
            ItemDraft(
                id = item.id,
                name = item.name.name,
                description = item.description,
                carriable = item.carriable ?: true,
                usable = item.usable ?: true,
                driveable = item.driveable ?: false,
                location = item.location,
                numberOfUses = item.numberOfUses ?: 0,
                stateKey = item.stateKey ?: "",
                onExamine = (item.onExamine ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf()),
                onUse = (item.onUse ?: emptyList()).map { it.toActionDraft() }.toCollection(mutableStateListOf())
            ).also { d -> item.name.applyToNameDraft(d.nameDraft) }
        }.toCollection(mutableStateListOf()),
        states = states.map { state ->
            StateDraft(
                key = state.stateKey,
                currentValue = state.currentValue,
                possibleValues = state.possibleValues.joinToString(",")
            )
        }.toCollection(mutableStateListOf()),
        actions = editorActions.map { action ->
            net.daddldiddl.jbsadventure.editor.model.ActionDraft(
                type = action.type,
                description = action.description
            ).also {
                it.bindingScope = action.bindingScope
                it.bindingTargetId = action.bindingTargetId
                it.bindingRoomId = action.bindingRoomId
                it.bindingDirection = action.bindingDirection
                it.changedStateKey = action.changedStateKey
                it.newStateValue = action.newStateValue
                it.moveToRoomId = action.moveToRoomId
                it.affectedItemIdsCsv = action.affectedItemIdsCsv
                it.moveToRoomIdForItems = action.moveToRoomIdForItems
                it.transformsIntoItemIdsCsv = action.transformsIntoItemIdsCsv
                it.modifyExitRoomId = action.modifyExitRoomId
                it.modifyExitDirection = action.modifyExitDirection
                it.modifyExitOpen = action.modifyExitOpen
                it.modifyExitLocked = action.modifyExitLocked
                it.modifyExitBlocked = action.modifyExitBlocked
                it.modifyExitVisible = action.modifyExitVisible
                it.modifyContainerId = action.modifyContainerId
                it.modifyContainerOpen = action.modifyContainerOpen
                it.modifyContainerLocked = action.modifyContainerLocked
                it.preconditions.clear()
                it.preconditions.addAll(action.preconditions.map { p -> p.toPreconditionDraft() })
            }
        }.toCollection(mutableStateListOf())
    )
}

// ─── NameDraft ↔ NameFile helpers ────────────────────────────────────────────

private fun NameDraft.toNameFile(): NameFile = NameFile(
    name = name,
    definiteName = definiteName.ifBlank { null },
    indefiniteName = indefiniteName.ifBlank { null },
    aliases = aliases.split(",").map { it.trim() }.filter { it.isNotBlank() },
    genderKey = genderKey.ifBlank { null },
    isPlural = if (isPlural) true else null
)

/** Returns null when the name draft is entirely empty (no meaningful name data). */
private fun NameDraft.toNameFileOrNull(): NameFile? {
    if (name.isBlank() && definiteName.isBlank() && indefiniteName.isBlank()
        && aliases.isBlank() && genderKey.isBlank() && !isPlural
    ) return null
    return toNameFile()
}

private fun NameFile.applyToNameDraft(draft: NameDraft) {
    draft.name = name
    draft.definiteName = definiteName ?: ""
    draft.indefiniteName = indefiniteName ?: ""
    draft.aliases = aliases.joinToString(",")
    draft.genderKey = genderKey ?: ""
    draft.isPlural = isPlural ?: false
}

// ─── ActionDraft ↔ TriggerActionFile converters ──────────────────────────────

private fun ActionDraft.toTriggerActionFile(): TriggerActionFile {
    val affectedIds = parsePersistCsvInts(affectedItemIdsCsv)
    val transformIds = parsePersistCsvInts(transformsIntoItemIdsCsv)
    return TriggerActionFile(
        type = type.trim(),
        description = description.ifBlank { null },
        moveToRoomId = if (type.trim() == "MoveTo") moveToRoomId else null,
        affectedItemIds = affectedIds.ifEmpty { null },
        moveToRoomIdForItems = if (type.trim() in listOf("SetItemRoom")) moveToRoomIdForItems else null,
        changedStateKey = changedStateKey.ifBlank { null },
        newStateValue = newStateValue.ifBlank { null },
        transformsIntoItemIds = transformIds.ifEmpty { null },
        roomId = if (type.trim() == "ModifyExit") modifyExitRoomId else null,
        direction = if (type.trim() == "ModifyExit") modifyExitDirection else null,
        open = parsePersistedBool(if (type.trim() == "ModifyContainer") modifyContainerOpen else modifyExitOpen),
        locked = parsePersistedBool(if (type.trim() == "ModifyContainer") modifyContainerLocked else modifyExitLocked),
        blocked = parsePersistedBool(modifyExitBlocked),
        visible = parsePersistedBool(modifyExitVisible),
        containerId = if (type.trim() == "ModifyContainer") modifyContainerId else null,
        preconditions = preconditions.map { it.toPreconditionFile() }.ifEmpty { null }
    )
}

private fun TriggerActionFile.toActionDraft(): ActionDraft =
    ActionDraft(type = type, description = description ?: "").also {
        it.moveToRoomId = moveToRoomId ?: 0
        it.affectedItemIdsCsv = affectedItemIds?.joinToString(",") ?: ""
        it.moveToRoomIdForItems = moveToRoomIdForItems ?: 0
        it.changedStateKey = changedStateKey ?: ""
        it.newStateValue = newStateValue ?: ""
        it.transformsIntoItemIdsCsv = transformsIntoItemIds?.joinToString(",") ?: ""
        it.modifyExitRoomId = roomId ?: 0
        it.modifyExitDirection = direction ?: "north"
        it.modifyExitOpen = open?.toString() ?: ""
        it.modifyExitLocked = locked?.toString() ?: ""
        it.modifyExitBlocked = blocked?.toString() ?: ""
        it.modifyExitVisible = visible?.toString() ?: ""
        it.modifyContainerId = containerId ?: 0
        if (containerId != null) {
            it.modifyContainerOpen = open?.toString() ?: ""
            it.modifyContainerLocked = locked?.toString() ?: ""
        }
        it.preconditions.clear()
        it.preconditions.addAll((preconditions ?: emptyList()).map { p -> p.toPreconditionDraft() })
    }

private fun PreconditionDraft.toPreconditionFile(): PreconditionFile = PreconditionFile(
    type = type,
    summary = summary,
    requiredStateKey = requiredStateKey,
    requiredStateValuesCsv = requiredStateValuesCsv,
    itemId = itemId,
    location = location,
    usable = usable,
    carriable = carriable,
    driveable = driveable,
    numberOfUses = numberOfUses,
    containerItemId = containerItemId,
    containerOpen = containerOpen,
    containerLocked = containerLocked,
    containerContainsItemsCsv = containerContainsItemsCsv,
    containerExcludesItemsCsv = containerExcludesItemsCsv,
    exitRoomId = exitRoomId,
    exitDirection = exitDirection,
    exitOpen = exitOpen,
    exitLocked = exitLocked,
    exitBlocked = exitBlocked,
    exitVisible = exitVisible,
    requiredItemsCsv = requiredItemsCsv,
    requiredRoomForItems = requiredRoomForItems,
    requiredContainerForItems = requiredContainerForItems,
    playerLocation = playerLocation,
    playerHasItemsCsv = playerHasItemsCsv,
    playerDoesntHaveItemsCsv = playerDoesntHaveItemsCsv
)

private fun PreconditionFile.toPreconditionDraft(): PreconditionDraft = PreconditionDraft(
    type = type,
    summary = summary
).also {
    it.requiredStateKey = requiredStateKey
    it.requiredStateValuesCsv = requiredStateValuesCsv
    it.itemId = itemId
    it.location = location
    it.usable = usable
    it.carriable = carriable
    it.driveable = driveable
    it.numberOfUses = numberOfUses
    it.containerItemId = containerItemId
    it.containerOpen = containerOpen
    it.containerLocked = containerLocked
    it.containerContainsItemsCsv = containerContainsItemsCsv
    it.containerExcludesItemsCsv = containerExcludesItemsCsv
    it.exitRoomId = exitRoomId
    it.exitDirection = exitDirection
    it.exitOpen = exitOpen
    it.exitLocked = exitLocked
    it.exitBlocked = exitBlocked
    it.exitVisible = exitVisible
    it.requiredItemsCsv = requiredItemsCsv
    it.requiredRoomForItems = requiredRoomForItems
    it.requiredContainerForItems = requiredContainerForItems
    it.playerLocation = playerLocation
    it.playerHasItemsCsv = playerHasItemsCsv
    it.playerDoesntHaveItemsCsv = playerDoesntHaveItemsCsv
}

private fun parsePersistCsvInts(input: String): List<Int> =
    input.split(",").map { it.trim() }.filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }

private fun parsePersistedBool(input: String): Boolean? = when (input.trim().lowercase()) {
    "true" -> true
    "false" -> false
    else -> null
}
