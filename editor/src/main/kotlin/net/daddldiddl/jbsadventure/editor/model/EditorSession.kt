package net.daddldiddl.jbsadventure.editor.model

data class AdventureMetadata(
    var title: String = "",
    var intro: String = "",
    var outro: String = ""
)

data class RoomDraft(
    val id: Int,
    var name: String,
    var description: String,
    val exits: MutableList<ExitDraft> = mutableListOf()
)

data class ExitDraft(
    var direction: String,
    var targetRoomId: Int,
    var supportsOpenClose: Boolean = false,
    var supportsLockUnlock: Boolean = false,
    var open: Boolean = true,
    var locked: Boolean = false,
    var visible: Boolean = true
)

data class ItemDraft(
    val id: Int,
    var name: String,
    var description: String
)

data class StateDraft(
    var key: String,
    var currentValue: String,
    var possibleValues: String
)

data class ActionDraft(
    var type: String,
    var description: String
)

data class PreconditionDraft(
    var type: String,
    var summary: String
)

data class EditorSession(
    val metadata: AdventureMetadata = AdventureMetadata(),
    val rooms: MutableList<RoomDraft> = mutableListOf(),
    val items: MutableList<ItemDraft> = mutableListOf(),
    val states: MutableList<StateDraft> = mutableListOf(),
    val actions: MutableList<ActionDraft> = mutableListOf(),
    val preconditions: MutableList<PreconditionDraft> = mutableListOf()
)
