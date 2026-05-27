package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.model.lang.*

@Serializable
data class ItemSurrogate(
    val id: Int,
    val name: Name,
    val description: String,
    val alternateNames: List<Name>,
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList()
)

object ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = ItemSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Item {
        val surrogate = decoder.decodeSerializableValue(ItemSurrogate.serializer())
        return Item(
            id = surrogate.id,
            name = surrogate.name,
            description = surrogate.description,
            alternateNames = surrogate.alternateNames,
            carriable = surrogate.carriable,
            stateKey = surrogate.stateKey,
            usable = surrogate.usable,
            numberOfUses = surrogate.numberOfUses,
            location = surrogate.location,
            comment = surrogate.comment,
            usages = surrogate.usages
        )
    }

    override fun serialize(encoder: Encoder, value: Item) {
        val surrogate = ItemSurrogate(
            id = value.id,
            name =value.name,
            description = value.description,
            alternateNames = value.alternateNames,
            carriable = value.carriable,
            stateKey = value.stateKey,
            usable = value.usable,
            numberOfUses = value.numberOfUses,
            location = value.location,
            comment = value.comment,
            usages = value.usages
        )
        encoder.encodeSerializableValue(RoomSurrogate.serializer(), surrogate)
    }

}

/**
 * Represents an item that can exist within the game world.
 *
 * Items reside in a room and can be examined or used by the player.
 * 
 * [usable] indicates whether the item can currently be used by the player;
 * [carriable] indicates whether the player can pick up the item;
 * [driveable] indicates whether using the item can cause it to relocate with the player.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable(with = ItemSerializer::class)
data class Item(
    val id: Int,
    val name: Name,
    val description: String,
    val alternateNames: List<Name> = emptyList(),
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList(),
    val container: Container? = null
) {
    /**
     * Checks if the given name matches this item, considering both the main name and any alternate names.
     *
     * @param name The name to check against this item.
     * @return `true` if the name matches either the main name or any alternate names; `false` otherwise.
     */
    fun matchesName(lookupName: String): Boolean {
        val lowerName = lookupName.lowercase()
        return lowerName == this.name.name.lowercase() ||
                alternateNames.any { it.lowercase() == lowerName }
    }

    fun descriptionWithState(gameData: GameData): String {
        val state = stateKey?.let { gameData.getStateMap()[it] }
        val usedescription = numberOfUses?.let { "${description.replace("<numberOfUses>", numberOfUses.toString())}" } 
                ?: "${description}"
        return if (state != null) "$usedescription\n${state.getDescriptionWithCurrentValue()}" else "$usedescription"

    }
    /**
     * Returns a debug-friendly name for the item, including its ID.
     */
    fun debugName(): String {
        return "$name (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
    }
}
