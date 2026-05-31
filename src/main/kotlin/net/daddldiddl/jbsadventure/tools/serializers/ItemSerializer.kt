package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.model.*

@Serializable
data class ItemSurrogate(
    val id: Int,
    val name: Name,
    val description: String? = null,
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList(),
    val isContainer: Boolean? = false,
    val containedItems: List<Int>? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null
)

object ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = ItemSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Item {
        val surrogate = decoder.decodeSerializableValue(ItemSurrogate.serializer())
        // Item and Container share one array in JSON; discriminator is `isContainer`.
        if(surrogate.isContainer == true) {
            val container = Container(
                id = surrogate.id,
                name = surrogate.name,
                description = surrogate.description,
                carriable = surrogate.carriable,
                driveable = surrogate.driveable,
                stateKey = surrogate.stateKey,
                usable = surrogate.usable,
                numberOfUses = surrogate.numberOfUses,
                location = surrogate.location,
                comment = surrogate.comment,
                usages = surrogate.usages,
                open = surrogate.open ?: false,
                locked = surrogate.locked ?: false,
            )
            if(surrogate.containedItems != null) {
                container.addItems(surrogate.containedItems)
            }
            return container
        }

        return Item(
            id = surrogate.id,
            name = surrogate.name,
            description = surrogate.description,
            carriable = surrogate.carriable,
            driveable = surrogate.driveable,
            stateKey = surrogate.stateKey,
            usable = surrogate.usable,
            numberOfUses = surrogate.numberOfUses,
            location = surrogate.location,
            comment = surrogate.comment,
            usages = surrogate.usages
        )
    }

    override fun serialize(encoder: Encoder, value: Item) {
        // Always emit the discriminator fields so runtime type can be reconstructed.
        val surrogate = ItemSurrogate(
            id = value.id,
            name =value.name,
            description = value.description,
            carriable = value.carriable,
            driveable = value.driveable,
            stateKey = value.stateKey,
            usable = value.usable,
            numberOfUses = value.numberOfUses,
            location = value.location,
            comment = value.comment,
            isContainer = value is Container,
            containedItems = if (value is ContainerEntity) value.getContainedItemIds() else null,
            open = if (value is ContainerEntity) value.open else null,
            locked = if (value is ContainerEntity) value.locked else null,
            usages = value.usages
        )
        encoder.encodeSerializableValue(ItemSurrogate.serializer(), surrogate)
    }

}
