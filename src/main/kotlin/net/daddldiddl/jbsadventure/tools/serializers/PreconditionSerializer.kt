package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import net.daddldiddl.jbsadventure.model.FixedItem
import net.daddldiddl.jbsadventure.model.FixedLocation
import net.daddldiddl.jbsadventure.model.actions.*


@Serializable
data class PreconditionSurrogate(
    val type: PreconditionType ?= PreconditionType.PreconditionState,
    // for state precondition
    val requiredStateKey: String? = null,
    val requiredStateValues: List<String>? = null,
    // for items location precondition
    val requiredItems: List<Int>? = null,
    val requiredRoomForItems: Int? = null,
    val requiredContainerForItems: Int? = null,
    // for exit precondition
    val roomId: Int? = null,
    val direction: String? = null,
    val blocked: Boolean? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null,
    val visible: Boolean? = null,
    // for item precondition
    val itemId: Int? = null,
    val location: Int? = null,
    val numberOfUses: Int? = null,
    val usable: Boolean? = null,
    val carriable: Boolean? = null,
    val driveable: Boolean? = null,
    // for container precondition
    val containsItems: List<Int>? = null,
    val excludesItems: List<Int>? = null
    // for player p
)

object PreconditionSerializer : KSerializer<Precondition> {
    override val descriptor: SerialDescriptor
        get() = PreconditionSurrogate.serializer().descriptor

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Precondition {
        val surrogate = decoder.decodeSerializableValue(PreconditionSurrogate.serializer())
        return when (surrogate.type) {
            PreconditionType.PreconditionItemsLocation -> PreconditionItemsLocation(
                requiredItems = surrogate.requiredItems ?: emptyList(),
                requiredRoomForItems = surrogate.requiredRoomForItems,
                requiredContainerForItems = surrogate.requiredContainerForItems
            )
            PreconditionType.PreconditionState -> PreconditionState(
                requiredStateKey = surrogate.requiredStateKey ?: "",
                requiredStateValues = surrogate.requiredStateValues?.toSet() ?: emptySet()
            )
            PreconditionType.PreconditionExit -> PreconditionExit(
                roomId = surrogate.roomId ?: FixedLocation.INVALID.value,
                direction = surrogate.direction ?: "",
                blocked = surrogate.blocked,
                open = surrogate.open,
                locked = surrogate.locked,
                visible = surrogate.visible
            )
            PreconditionType.PreconditionItem -> PreconditionItem(
                itemId = surrogate.itemId ?: FixedItem.INVALID.value,
                location = surrogate.location,
                numberOfUses = surrogate.numberOfUses,
                usable = surrogate.usable,
                carriable = surrogate.carriable,
                driveable = surrogate.driveable
            )
            PreconditionType.PreconditionContainer -> PreconditionContainer(
                itemId = surrogate.itemId ?: FixedItem.INVALID.value,
                location = surrogate.location,
                carriable = surrogate.carriable,
                containsItems = surrogate.containsItems ?: emptyList(),
                excludesItems = surrogate.excludesItems ?: emptyList(),
                open = surrogate.open,
                locked = surrogate.locked
            )
            else -> throw IllegalArgumentException("Unsupported precondition type")
        }
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Precondition) {
        val surrogate = when(value) {
            is PreconditionState -> PreconditionSurrogate(
                type = PreconditionType.PreconditionState,
                requiredStateKey = value.requiredStateKey,
                requiredStateValues = value.requiredStateValues.toList()
            )
            is PreconditionItemsLocation -> PreconditionSurrogate(
                type = PreconditionType.PreconditionItemsLocation,
                requiredItems = value.requiredItems,
                requiredRoomForItems = value.requiredRoomForItems,
                requiredContainerForItems = value.requiredContainerForItems
            )
            is PreconditionExit -> PreconditionSurrogate(
                type = PreconditionType.PreconditionExit,
                roomId = value.roomId,
                direction = value.direction,
                blocked = value.blocked,
                open = value.open,
                locked = value.locked,
                visible = value.visible
            )
            is PreconditionItem -> PreconditionSurrogate(
                type = PreconditionType.PreconditionItem,
                itemId = value.itemId,
                location = value.location,
                numberOfUses = value.numberOfUses,
                usable = value.usable,
                carriable = value.carriable,
                driveable = value.driveable
            )
            is PreconditionContainer -> PreconditionSurrogate(
                type = PreconditionType.PreconditionContainer,
                itemId = value.itemId,
                location = value.location,
                carriable = value.carriable,
                containsItems = value.containsItems,
                excludesItems = value.excludesItems,
                open = value.open,
                locked = value.locked
            )
            else -> throw IllegalArgumentException("Unsupported precondition type")
        }
        encoder.encodeSerializableValue(PreconditionSurrogate.serializer(), surrogate)
    }
}

