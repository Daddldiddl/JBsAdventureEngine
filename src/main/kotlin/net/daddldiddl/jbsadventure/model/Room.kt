package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import net.daddldiddl.jbsadventure.model.lang.*

@Serializable
data class RoomSurrogate(
    val id: Int,
    val name: String,
    val description: String,
    @SerialName("Exits")
    val exits: List<Exit>? = emptyList(),
    @SerialName("ItemUsages")
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
            name = Name(surrogate.name),
            description = surrogate.description,
            exits = surrogate.exits?.associateBy { it.direction } ?: emptyMap(),
            itemUsages = surrogate.itemUsages
        )
    }

    override fun serialize(encoder: Encoder, value: Room) {
        val surrogate = RoomSurrogate(
            id = value.id,
            name = value.name.name,
            description = value.description,
            exits = value.exits?.values?.toList() ?: emptyList(),
            itemUsages = value.itemUsages
        )
        encoder.encodeSerializableValue(RoomSurrogate.serializer(), surrogate)
    }

}


/**
 * Represents a room within the game world.
 *
 * A room has navigable [exits] to adjacent rooms and may contain items.
 * [itemUsages] defines which items can be used here and what effect each use triggers.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable(with = RoomSerializer::class)
data class Room(
    val id: Int,
    val name: Name,
    val description: String,
    @SerialName("Exits")
    val exits: Map<String, Exit>? = emptyMap(),
    @SerialName("ItemUsages")
    val itemUsages: List<ItemUsage>? = emptyList()
) {
    fun getVisibleExits(): List<Exit> {
        exits?.values?.filter { it.isVisible() }
    }

    /**
     * Returns the [ItemUsage] definition for the given item in this room, if one exists.
     *
     * @param itemId The ID of the item to look up.
     * @return The matching [ItemUsage], or `null` if no usage is defined for that item here.
     */
    fun getItemUsage(itemId: Int): ItemUsage? {
        return itemUsages?.find { it.itemId == itemId }
    }

    /**
     * Returns a debug-friendly name for the room, including its ID.
     */
    fun debugName(): String {
        return "$name (id=$id)"
    }    
}
