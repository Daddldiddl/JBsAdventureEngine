package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.Exit
import net.daddldiddl.jbsadventure.model.ItemUsage
import net.daddldiddl.jbsadventure.model.Name

@Serializable
data class ExitSurrogate (
    val direction: String,
    val targetRoomId: Int,
    val name: Name? = null,
    val supportsOpenClose: Boolean? = null,
    val supportsLockUnlock: Boolean? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null,
    val visible: Boolean? = null,
    val description: String? = null,
    val blocked: Boolean? = null,
    val blockedDescription: String? = null,
    val itemUsages: List<ItemUsage>? = null
)

/**
 * Custom serializer for [Exit] that reads/writes the JSON array format
 * via [ExitSurrogate] while the runtime representation uses [MutableMap]s.
 */
object ExitSerializer : KSerializer<Exit>{
    override val descriptor: SerialDescriptor = ExitSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Exit {
        val surrogate = decoder.decodeSerializableValue(ExitSurrogate.serializer())
        return Exit(
            direction = surrogate.direction,
            targetRoomId = surrogate.targetRoomId,
            name = surrogate.name ?: Name(LANG.getDirectionAliasFromKey(surrogate.direction)),
            supportsOpenClose = surrogate.supportsOpenClose ?: false,
            supportsLockUnlock = surrogate.supportsLockUnlock ?: false,
            open = surrogate.open ?: true,
            locked = surrogate.locked ?: false,
            visible = surrogate.visible ?: true,
            description = surrogate.description,
            blocked = surrogate.blocked ?: false,
            blockedDescription = surrogate.blockedDescription,
            itemUsages = surrogate.itemUsages ?: emptyList()
        )
    }

    override fun serialize(encoder: Encoder, value: Exit) {
        val surrogate = ExitSurrogate(
            direction = value.direction,
            targetRoomId = value.targetRoomId,
            name = value.name,
            supportsOpenClose = value.supportsOpenClose,
            supportsLockUnlock = value.supportsLockUnlock,
            open = value.open,
            locked = value.locked,
            visible = value.visible,
            description = value.description,
            blocked = value.blocked,
            blockedDescription = value.blockedDescription,
            itemUsages = value.itemUsages
        )
        encoder.encodeSerializableValue(ExitSurrogate.serializer(), surrogate)
    }
}
