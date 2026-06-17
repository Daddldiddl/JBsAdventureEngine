package net.daddldiddl.jbsadventure.editor.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureFile(
    val title: String = "",
    val introductionMessage: String = "",
    val exitMessage: String = "",
    @SerialName("Rooms")
    val rooms: List<RoomFile> = emptyList(),
    @SerialName("Items")
    val items: List<ItemFile> = emptyList(),
    @SerialName("States")
    val states: List<StateFile> = emptyList(),
    @SerialName("EditorActions")
    val editorActions: List<ActionFile> = emptyList(),
    @SerialName("EditorPreconditions")
    val editorPreconditions: List<PreconditionFile> = emptyList()
)

@Serializable
data class RoomFile(
    val id: Int,
    val name: NameFile,
    val description: String = "",
    val exits: List<ExitFile> = emptyList(),
    val onExamine: List<TriggerActionFile>? = null,
    val itemUsages: List<ItemUsageFile>? = null
)

@Serializable
data class ItemUsageFile(
    val itemId: Int,
    val actions: List<TriggerActionFile> = emptyList(),
    val becomesUsable: Boolean? = null,
    val consumeUsedItem: Boolean? = null
)

/**
 * Full Name class – only [name] is required.
 * [definiteName]/[indefiniteName] enable language-specific article forms (e.g. German).
 * [genderKey]: "male", "female" or "neuter".
 */
@Serializable
data class NameFile(
    val name: String,
    val definiteName: String? = null,
    val indefiniteName: String? = null,
    val aliases: List<String> = emptyList(),
    val genderKey: String? = null,
    val isPlural: Boolean? = null
)

@Serializable
data class ExitFile(
    val direction: String,
    val targetRoomId: Int,
    /** Optional name – leave null to use direction as display label. */
    val name: NameFile? = null,
    val supportsOpenClose: Boolean? = null,
    val supportsLockUnlock: Boolean? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null,
    val visible: Boolean? = null,
    val blocked: Boolean? = null,
    /** Item ID of the required key (null = no key needed). */
    val keyId: Int? = null,
    val consumeKeyOnLock: Boolean? = null,
    val consumeKeyOnUnlock: Boolean? = null,
    val onExamine: List<TriggerActionFile>? = null,
    val onOpen: List<TriggerActionFile>? = null,
    val onClose: List<TriggerActionFile>? = null,
    val onLock: List<TriggerActionFile>? = null,
    val onUnlock: List<TriggerActionFile>? = null
)

@Serializable
data class ItemFile(
    val id: Int,
    val name: NameFile,
    val description: String = "",
    val location: Int = 0,
    val carriable: Boolean? = null,
    val usable: Boolean? = null,
    val driveable: Boolean? = null,
    val numberOfUses: Int? = null,
    /** Optional: links item to a State key for state-dependent description display. */
    val stateKey: String? = null,
    val onExamine: List<TriggerActionFile>? = null,
    val onUse: List<TriggerActionFile>? = null
)

/**
 * Engine-compatible action representation for inline trigger lists
 * (onExamine, onUse, onOpen, onClose, onLock, onUnlock).
 *
 * Uses engine field names directly – [affectedItemIds] and [transformsIntoItemIds]
 * are Int lists, NOT CSV strings.
 */
@Serializable
data class TriggerActionFile(
    val type: String,
    val description: String? = null,
    val comment: String? = null,
    val delayInMillis: Long? = null,
    // MoveTo
    val moveToRoomId: Int? = null,
    // SetItemRoom
    val affectedItemIds: List<Int>? = null,
    val moveToRoomIdForItems: Int? = null,
    // ChangeState
    val changedStateKey: String? = null,
    val newStateValue: String? = null,
    // TransformIntoItem
    val transformsIntoItemIds: List<Int>? = null,
    // ModifyExit
    val roomId: Int? = null,
    val direction: String? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null,
    val blocked: Boolean? = null,
    val visible: Boolean? = null,
    val newName: String? = null,
    // ModifyContainer
    val containerId: Int? = null,
    // Action preconditions
    val preconditions: List<PreconditionFile>? = null
)

@Serializable
data class StateFile(
    val stateKey: String,
    val currentValue: String,
    val description: String = "",
    val possibleValues: List<String> = emptyList()
)

@Serializable
data class ActionFile(
    val type: String,
    val description: String,
    val bindingScope: String = "Global",
    val bindingTargetId: Int = 0,
    val bindingRoomId: Int = 0,
    val bindingDirection: String = "north",
    val changedStateKey: String = "",
    val newStateValue: String = "",
    val moveToRoomId: Int = 0,
    val affectedItemIdsCsv: String = "",
    val moveToRoomIdForItems: Int = 0,
    val transformsIntoItemIdsCsv: String = "",
    val modifyExitRoomId: Int = 0,
    val modifyExitDirection: String = "north",
    val modifyExitOpen: String = "",
    val modifyExitLocked: String = "",
    val modifyExitBlocked: String = "",
    val modifyExitVisible: String = "",
    val modifyContainerId: Int = 0,
    val modifyContainerOpen: String = "",
    val modifyContainerLocked: String = "",
    val preconditions: List<PreconditionFile> = emptyList()
)

@Serializable
data class PreconditionFile(
    val type: String,
    val summary: String,
    val requiredStateKey: String = "",
    val requiredStateValuesCsv: String = "",
    val itemId: Int = 0,
    val location: String = "",
    val usable: String = "",
    val carriable: String = "",
    val driveable: String = "",
    val numberOfUses: String = "",
    val containerItemId: Int = 0,
    val containerOpen: String = "",
    val containerLocked: String = "",
    val containerContainsItemsCsv: String = "",
    val containerExcludesItemsCsv: String = "",
    val exitRoomId: Int = 0,
    val exitDirection: String = "north",
    val exitOpen: String = "",
    val exitLocked: String = "",
    val exitBlocked: String = "",
    val exitVisible: String = "",
    val requiredItemsCsv: String = "",
    val requiredRoomForItems: String = "",
    val requiredContainerForItems: String = "",
    val playerLocation: String = "",
    val playerHasItemsCsv: String = "",
    val playerDoesntHaveItemsCsv: String = ""
)
