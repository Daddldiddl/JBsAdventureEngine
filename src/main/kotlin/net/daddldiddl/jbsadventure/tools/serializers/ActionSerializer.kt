package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.model.actions.*

@Serializable
private data class ActionSurrogate(
    val type: ActionType,
    val preconditions: List<Precondition>? = null,
    val description: String? = null,
    val comment: String? = null,
    val actionDebug: String? = null,
    val delayInMillis: Long? = null,
    // for change state actions:
    val changedStateKey: String? = null,
    val newStateValue: String? = null,
    // for move to actions:
    val moveToRoomId: Int? = null,
    // for set item room actions:
    val moveToRoomIdForItems: Int? = null,
    // for transform into item actions:
    val transformsIntoItemIds: List<Int>? = null,
    // for ModifyExit actions
    val roomId: Int? = null,
    val direction: String? = null,
    val blocked: Boolean? = null,
    val visible: Boolean? = null,
    val locked: Boolean? = null,
    val open: Boolean? = null,
    // for ModifyContainer actions:
    val containerId: Int? = null,
    // for set room and transform actions:
    val affectedItemIds: List<Int>? = null,
    // for modify actions
    val newName: Name? = null
)

object ActionSerializer : KSerializer<Action> {
    override val descriptor: SerialDescriptor = ActionSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Action {
        val surrogate = decoder.decodeSerializableValue(ActionSurrogate.serializer())
        // Keep deserialization tolerant while old and new JSON fields may coexist.
        return when (surrogate.type) {
            ActionType.ChangeState -> ChangeStateAction(
                changedStateKey = surrogate.changedStateKey!!,
                newStateValue = surrogate.newStateValue!!,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis
            )
            ActionType.MoveTo -> MoveToAction(
                moveToRoomId = surrogate.moveToRoomId!!,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis
            )
            ActionType.SetItemRoom -> SetItemRoomAction(
                affectedItemIds = surrogate.affectedItemIds!!,
                // Prefer the new field, but accept the old moveToRoomId for compatibility.
                moveToRoomId = surrogate.moveToRoomIdForItems ?: surrogate.moveToRoomId!!,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis
            )
            ActionType.TransformIntoItem -> TransformIntoItemAction(
                affectedItemIds = surrogate.affectedItemIds!!,
                transformsIntoItemIds = surrogate.transformsIntoItemIds!!,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis
            )
            ActionType.ModifyExit -> ModifyExitAction(
                roomId = surrogate.roomId!!,
                direction = surrogate.direction!!,
                blocked = surrogate.blocked,
                visible = surrogate.visible,
                locked = surrogate.locked,
                open = surrogate.open,
                newName = surrogate.newName,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis,
            )
            ActionType.ModifyContainer -> ModifyContainerAction(
                containerId = surrogate.containerId!!,
                open = surrogate.open,
                locked = surrogate.locked,
                configuredPreconditions = surrogate.preconditions ?: emptyList(),
                configuredDescription = surrogate.description,
                configuredComment = surrogate.comment,
                configuredActionDebug = surrogate.actionDebug,
                configuredDelayInMillis = surrogate.delayInMillis
            )
        }
    }

    override fun serialize(encoder: Encoder, value: Action) {
        val surrogate = when (value) {
            is ChangeStateAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                changedStateKey = value.changedStateKey,
                newStateValue = value.newStateValue
            )
            is MoveToAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                moveToRoomId = value.moveToRoomId
            )
            is SetItemRoomAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                affectedItemIds = value.affectedItemIds,
                moveToRoomIdForItems = value.moveToRoomId
            )
            is TransformIntoItemAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                affectedItemIds = value.affectedItemIds,
                transformsIntoItemIds = value.transformsIntoItemIds
            )
            is ModifyExitAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                roomId = value.roomId,
                direction = value.direction,
                blocked = value.blocked,
                visible = value.visible,
                locked = value.locked,
                open = value.open,
                newName = value.newName
            )
            is ModifyContainerAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                delayInMillis = value.delayInMillis,
                containerId = value.containerId,
                locked = value.locked,
                open = value.open
            )
            else -> throw IllegalArgumentException("Unsupported action type '${value.type}' encountered during serialization.")
        }
        encoder.encodeSerializableValue(ActionSurrogate.serializer(), surrogate)
    }
}
