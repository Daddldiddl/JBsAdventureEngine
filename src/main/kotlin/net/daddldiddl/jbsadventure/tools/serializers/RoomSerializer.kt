package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*

@Serializable
private data class RoomSurrogate(
    val id: Int,
    val name: Name,
    val description: String,
    val exits: List<Exit> = emptyList(),
    val itemUsages: List<ItemUsage>? = emptyList()
)

/**
 * Custom serializer for [Room] that reads/writes the JSON array format
 * via [RoomSurrogate] while the runtime representation uses [MutableMap]s.
 */
object RoomSerializer : KSerializer<Room> {
    override val descriptor: SerialDescriptor = RoomSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Room {
        val surrogate = decoder.decodeSerializableValue(RoomSurrogate.serializer())
        return Room(
            id = surrogate.id,
            name = surrogate.name,
            description = surrogate.description,
            exits = surrogate.exits?.associateBy { it.direction } ?: emptyMap(),
            itemUsages = surrogate.itemUsages ?: emptyList()
        )
    }

    override fun serialize(encoder: Encoder, value: Room) {
        val surrogate = RoomSurrogate(
            id = value.id,
            name = value.name,
            description = value.description,
            exits = value.exits?.values?.toList() ?: emptyList(),
            itemUsages = value.itemUsages
        )
        encoder.encodeSerializableValue(RoomSurrogate.serializer(), surrogate)
    }
}
