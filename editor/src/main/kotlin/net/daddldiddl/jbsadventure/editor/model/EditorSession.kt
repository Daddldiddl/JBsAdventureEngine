package net.daddldiddl.jbsadventure.editor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class AdventureMetadata(
    title: String = "",
    intro: String = "",
    outro: String = ""
) {
    var title by mutableStateOf(title)
    var intro by mutableStateOf(intro)
    var outro by mutableStateOf(outro)
}

/**
 * All fields from the engine's Name class.
 * Only [name] is required; all others are optional and used for localisation /
 * language-specific inflection (e.g. German definite/indefinite forms).
 * [genderKey]: "male", "female" or "neuter".
 * [aliases]: comma-separated lookup names.
 */
class NameDraft(
    name: String = "",
    definiteName: String = "",
    indefiniteName: String = "",
    aliases: String = "",
    genderKey: String = "",
    isPlural: Boolean = false
) {
    var name by mutableStateOf(name)
    var definiteName by mutableStateOf(definiteName)
    var indefiniteName by mutableStateOf(indefiniteName)
    var aliases by mutableStateOf(aliases)
    var genderKey by mutableStateOf(genderKey)
    var isPlural by mutableStateOf(isPlural)
}

class RoomDraft(
    val id: Int,
    name: String,
    description: String,
    val exits: SnapshotStateList<ExitDraft> = mutableStateListOf(),
    val onExamine: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val itemUsages: SnapshotStateList<ItemUsageDraft> = mutableStateListOf()
) {
    val nameDraft: NameDraft = NameDraft(name = name)

    /** Convenience accessor so existing code `room.name = it` still compiles. */
    var name: String
        get() = nameDraft.name
        set(value) { nameDraft.name = value }

    var description by mutableStateOf(description)
}

class ExitDraft(
    direction: String,
    targetRoomId: Int,
    supportsOpenClose: Boolean = false,
    supportsLockUnlock: Boolean = false,
    open: Boolean = true,
    locked: Boolean = false,
    visible: Boolean = true,
    blocked: Boolean = false,
    keyId: Int = 0,
    consumeKeyOnLock: Boolean = false,
    consumeKeyOnUnlock: Boolean = false,
    val onExamine: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val onOpen: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val onClose: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val onLock: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val onUnlock: SnapshotStateList<ActionDraft> = mutableStateListOf()
) {
    /** Optional name – exits are usually identified by direction only. */
    val nameDraft: NameDraft = NameDraft()

    var direction by mutableStateOf(direction)
    var targetRoomId by mutableStateOf(targetRoomId)
    var supportsOpenClose by mutableStateOf(supportsOpenClose)
    var supportsLockUnlock by mutableStateOf(supportsLockUnlock)
    var open by mutableStateOf(open)
    var locked by mutableStateOf(locked)
    var visible by mutableStateOf(visible)
    var blocked by mutableStateOf(blocked)
    /** ID of the key item required to lock/unlock (0 = no key required). */
    var keyId by mutableStateOf(keyId)
    var consumeKeyOnLock by mutableStateOf(consumeKeyOnLock)
    var consumeKeyOnUnlock by mutableStateOf(consumeKeyOnUnlock)
}

class ItemDraft(
    val id: Int,
    name: String,
    description: String,
    carriable: Boolean = true,
    usable: Boolean = true,
    driveable: Boolean = false,
    location: Int = 0,
    numberOfUses: Int = 0,
    stateKey: String = "",
    val onExamine: SnapshotStateList<ActionDraft> = mutableStateListOf(),
    val onUse: SnapshotStateList<ActionDraft> = mutableStateListOf()
) {
    val nameDraft: NameDraft = NameDraft(name = name)

    var name: String
        get() = nameDraft.name
        set(value) { nameDraft.name = value }

    var description by mutableStateOf(description)
    var carriable by mutableStateOf(carriable)
    var usable by mutableStateOf(usable)
    var driveable by mutableStateOf(driveable)
    /** FixedLocation: >0 = room ID, -1 = inventory, -2 = container, 0 = not assigned. */
    var location by mutableStateOf(location)
    /** 0 = unlimited uses. */
    var numberOfUses by mutableStateOf(numberOfUses)
    /** Optional: links item description to a State for state-dependent display. */
    var stateKey by mutableStateOf(stateKey)
}

class StateDraft(
    key: String,
    currentValue: String,
    possibleValues: String
) {
    var key by mutableStateOf(key)
    var currentValue by mutableStateOf(currentValue)
    var possibleValues by mutableStateOf(possibleValues)
}

class ActionDraft(
    type: String,
    description: String
) {
    var type by mutableStateOf(type)
    var description by mutableStateOf(description)
    var bindingScope by mutableStateOf("Global")
    var bindingTargetId by mutableStateOf(0)
    var bindingRoomId by mutableStateOf(0)
    var bindingDirection by mutableStateOf("north")

    var changedStateKey by mutableStateOf("")
    var newStateValue by mutableStateOf("")
    var moveToRoomId by mutableStateOf(0)
    var affectedItemIdsCsv by mutableStateOf("")
    var moveToRoomIdForItems by mutableStateOf(0)
    var transformsIntoItemIdsCsv by mutableStateOf("")

    var modifyExitRoomId by mutableStateOf(0)
    var modifyExitDirection by mutableStateOf("north")
    var modifyExitOpen by mutableStateOf("")
    var modifyExitLocked by mutableStateOf("")
    var modifyExitBlocked by mutableStateOf("")
    var modifyExitVisible by mutableStateOf("")

    var modifyContainerId by mutableStateOf(0)
    var modifyContainerOpen by mutableStateOf("")
    var modifyContainerLocked by mutableStateOf("")

    // Preconditions belong to the action itself (engine-compatible structure).
    val preconditions: SnapshotStateList<PreconditionDraft> = mutableStateListOf()
}

class ItemUsageDraft(
    itemId: Int = 0,
    becomesUsable: Boolean = false,
    consumeUsedItem: Boolean = false,
    val actions: SnapshotStateList<ActionDraft> = mutableStateListOf()
) {
    var itemId by mutableStateOf(itemId)
    var becomesUsable by mutableStateOf(becomesUsable)
    var consumeUsedItem by mutableStateOf(consumeUsedItem)
}

class PreconditionDraft(
    type: String,
    summary: String
) {
    var type by mutableStateOf(type)
    var summary by mutableStateOf(summary)

    var requiredStateKey by mutableStateOf("")
    var requiredStateValuesCsv by mutableStateOf("")

    var itemId by mutableStateOf(0)
    var location by mutableStateOf("")
    var usable by mutableStateOf("")
    var carriable by mutableStateOf("")
    var driveable by mutableStateOf("")
    var numberOfUses by mutableStateOf("")

    var containerItemId by mutableStateOf(0)
    var containerOpen by mutableStateOf("")
    var containerLocked by mutableStateOf("")
    var containerContainsItemsCsv by mutableStateOf("")
    var containerExcludesItemsCsv by mutableStateOf("")

    var exitRoomId by mutableStateOf(0)
    var exitDirection by mutableStateOf("north")
    var exitOpen by mutableStateOf("")
    var exitLocked by mutableStateOf("")
    var exitBlocked by mutableStateOf("")
    var exitVisible by mutableStateOf("")

    var requiredItemsCsv by mutableStateOf("")
    var requiredRoomForItems by mutableStateOf("")
    var requiredContainerForItems by mutableStateOf("")

    var playerLocation by mutableStateOf("")
    var playerHasItemsCsv by mutableStateOf("")
    var playerDoesntHaveItemsCsv by mutableStateOf("")
}

class EditorSession(
    val metadata: AdventureMetadata = AdventureMetadata(),
    val rooms: SnapshotStateList<RoomDraft> = mutableStateListOf(),
    val items: SnapshotStateList<ItemDraft> = mutableStateListOf(),
    val states: SnapshotStateList<StateDraft> = mutableStateListOf(),
    val actions: SnapshotStateList<ActionDraft> = mutableStateListOf()
)
