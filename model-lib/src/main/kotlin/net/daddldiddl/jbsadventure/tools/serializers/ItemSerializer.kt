package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.model.Container
import net.daddldiddl.jbsadventure.model.Item
import net.daddldiddl.jbsadventure.model.ItemUsage
import net.daddldiddl.jbsadventure.model.Name
import net.daddldiddl.jbsadventure.model.actions.Action

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
    val locked: Boolean? = null,
    val keyId: Int? = null,
    val consumeKeyOnLock: Boolean? = null,
    val consumeKeyOnUnlock: Boolean? = null,
    val supportsLockUnlock: Boolean? = null,
    val onExamine: List<Action>? = null,
    val onOpen: List<Action>? = null,
    val onClose: List<Action>? = null,
    val onLock: List<Action>? = null,
    val onUnlock: List<Action>? = null,
    val onUse: List<Action>? = null,
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
                onExamine = surrogate.onExamine ?: mutableListOf(),
                carriable = surrogate.carriable,
                driveable = surrogate.driveable,
                stateKey = surrogate.stateKey,
                usable = surrogate.usable,
                numberOfUses = surrogate.numberOfUses,
                location = surrogate.location,
                comment = surrogate.comment,
                usages = surrogate.usages,
                onUse = surrogate.onUse ?: emptyList(),
                open = surrogate.open ?: false,
                locked = surrogate.locked ?: false,
                keyId = surrogate.keyId,
                consumeKeyOnLock = surrogate.consumeKeyOnLock ?: false,
                consumeKeyOnUnlock = surrogate.consumeKeyOnUnlock ?: false,
                onOpen = surrogate.onOpen ?: emptyList(),
                onClose = surrogate.onClose ?: emptyList(),
                onLock = surrogate.onLock ?: emptyList(),
                onUnlock = surrogate.onUnlock ?: emptyList(),
                configuredSupportsLockUnlock = surrogate.supportsLockUnlock ?: false,
            )
            if(surrogate.containedItems != null) {
                container.containedItems.addAll(surrogate.containedItems)
            }
            return container
        }

        return Item(
            id = surrogate.id,
            name = surrogate.name,
            description = surrogate.description,
            onExamine = surrogate.onExamine ?: emptyList(),
            carriable = surrogate.carriable,
            driveable = surrogate.driveable,
            stateKey = surrogate.stateKey,
            usable = surrogate.usable,
            numberOfUses = surrogate.numberOfUses,
            location = surrogate.location,
            comment = surrogate.comment,
            usages = surrogate.usages,
            onUse = surrogate.onUse ?: emptyList()
        )
    }

    override fun serialize(encoder: Encoder, value: Item) {
        val isContainer = value is Container
        // Always emit the discriminator fields so runtime type can be reconstructed.
        val surrogate = ItemSurrogate(
            id = value.id,
            name =value.name,
            description = value.description,
            onExamine = value.onExamine,
            carriable = value.carriable,
            driveable = value.driveable,
            stateKey = value.stateKey,
            usable = value.usable,
            numberOfUses = value.numberOfUses,
            location = value.location,
            comment = value.comment,
            isContainer = isContainer,
            containedItems = if (isContainer) value.getContainedItemIds() else null,
            open = if (isContainer) value.open else null,
            locked = if (isContainer) value.locked else null,
            keyId = if (isContainer) value.keyId else null,
            consumeKeyOnLock = if(isContainer) value.consumeKeyOnLock else null,
            consumeKeyOnUnlock = if(isContainer) value.consumeKeyOnUnlock else null,
            supportsLockUnlock = if (isContainer) value.supportsLockUnlock else null,
            onOpen = if (isContainer) value.onOpen else null,
            onClose = if (isContainer) value.onClose else null,
            onLock = if (isContainer) value.onLock else null,
            onUnlock = if (isContainer) value.onUnlock else null,
            onUse = value.onUse,
            usages = value.usages
        )
        encoder.encodeSerializableValue(ItemSurrogate.serializer(), surrogate)
    }

}
