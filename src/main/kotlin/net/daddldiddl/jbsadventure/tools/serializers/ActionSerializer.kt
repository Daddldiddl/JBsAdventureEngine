package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*

@Serializable
private data class ActionSurrogate(
    val type: ActionType,
    val preconditions: List<Precondition>? = null,
    val description: String? = null,
    val comment: String? = null,
    val actionDebug: String? = null,
    // for change state actions:
    val changedStateKey: String? = null,
    val newStateValue: String? = null,
    // for move to actions:
    val moveToRoomId: Int? = null,
    // for set item room actions:
    val moveToRoomIdForItems: Int? = null,
    // for transform into item actions:
    val transformsIntoItemIds: List<Int>? = null,
    // for set room and transform actions:
    val affectedItemIds: List<Int>? = null,

)

object ActionSerializer : KSerializer<Action> {
    override val descriptor: SerialDescriptor = ActionSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Action {
        val surrogate = decoder.decodeSerializableValue(ActionSurrogate.serializer())
        return when (surrogate.type) {
            ActionType.ChangeState -> ChangeStateAction(
                changedStateKey = surrogate.changedStateKey!!,
                newStateValue = surrogate.newStateValue!!,
                preconditions = surrogate.preconditions,
                description = surrogate.description,
                comment = surrogate.comment,
                actionDebug = surrogate.actionDebug
            )
            ActionType.MoveTo -> MoveToAction(
                moveToRoomId = surrogate.moveToRoomId!!,
                preconditions = surrogate.preconditions,
                description = surrogate.description,
                comment = surrogate.comment,
                actionDebug = surrogate.actionDebug
            )
            ActionType.SetItemRoom -> SetItemRoomAction(
                affectedItemIds = surrogate.affectedItemIds!!,
                moveToRoomId = surrogate.moveToRoomIdForItems!!,
                preconditions = surrogate.preconditions,
                description = surrogate.description,
                comment = surrogate.comment,
                actionDebug = surrogate.actionDebug
            )
            ActionType.TransformIntoItem -> TransformIntoItemAction(
                affectedItemIds = surrogate.affectedItemIds!!,
                transformsIntoItemIds = surrogate.transformsIntoItemIds!!,
                preconditions = surrogate.preconditions,
                description = surrogate.description,
                comment = surrogate.comment,
                actionDebug = surrogate.actionDebug
            )
            else -> {
                LOG.error("Unknown action type '${surrogate.type}' encountered during deserialization. Returning a default no-op action.")
                return Action(
                    type = surrogate.type,
                    preconditions = surrogate.preconditions,
                    description = surrogate.description,
                    comment = surrogate.comment,
                    actionDebug = surrogate.actionDebug
                )
            }
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
                changedStateKey = value.changedStateKey,
                newStateValue = value.newStateValue
            )
            is MoveToAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                moveToRoomId = value.moveToRoomId
            )
            is SetItemRoomAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                affectedItemIds = value.affectedItemIds,
                moveToRoomIdForItems = value.moveToRoomId
            )
            is TransformIntoItemAction -> ActionSurrogate(
                type = value.type,
                preconditions = value.preconditions,
                description = value.description,
                comment = value.comment,
                actionDebug = value.actionDebug,
                affectedItemIds = value.affectedItemIds,
                transformsIntoItemIds = value.transformsIntoItemIds
            )
            else -> throw IllegalArgumentException("Unsupported action type '${value.type}' encountered during serialization.")
        }
        encoder.encodeSerializableValue(ActionSurrogate.serializer(), surrogate)
    }
}
