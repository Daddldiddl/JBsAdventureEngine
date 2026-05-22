package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Root data container deserialized from `data.json`.
 *
 * Holds the complete set of [Room]s and [Item]s that make up the game world
 * and provides convenience accessors consumed by [net.daddldiddl.jbsadventure.Game].
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */

/**
 * Private surrogate that mirrors the JSON structure (arrays).
 * Used only by [GameDataSerializer] and never exposed outside this file.
 */
@Serializable
private data class GameDataSurrogate(
    val title: String? = null,
    val introductionMessage: String? = null,
    val exitMessage: String? = null,
    val Rooms: List<Room>,
    val Items: List<Item>,
    val States: List<State>
)

/**
 * Custom serializer for [GameData] that reads/writes the JSON array format
 * via [GameDataSurrogate] while the runtime representation uses [MutableMap]s.
 */
private object GameDataSerializer : KSerializer<GameData> {
    override val descriptor: SerialDescriptor = GameDataSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): GameData {
        val surrogate = decoder.decodeSerializableValue(GameDataSurrogate.serializer())
        return GameData(
            title = surrogate.title ?: "a JB's Adventure Engine game",
            introductionMessage = surrogate.introductionMessage ?: "Enjoy exploring the world and discovering its secrets!\nUse commands like 'look', 'go north', 'take item', and 'use item' to interact with the environment. Type 'help' for a list of commands.",
            exitMessage = surrogate.exitMessage ?: "Thank you for playing ${surrogate.title ?: "a JB's Adventure Engine game"}! We hope you enjoyed your time with the game. See you next time!",
            Rooms = surrogate.Rooms.associateBy { it.id }.toMutableMap(),
            Items = surrogate.Items.associateBy { it.id }.toMutableMap(),
            States = surrogate.States.associateBy { it.stateKey }.toMutableMap()
        )
    }

    override fun serialize(encoder: Encoder, value: GameData) {
        val surrogate = GameDataSurrogate(
            title = value.title,
            introductionMessage = value.introductionMessage,
            exitMessage = value.exitMessage,
            Rooms = value.getRoomList(),
            Items = value.getItemList(),
            States = value.getStateList()
        )
        encoder.encodeSerializableValue(GameDataSurrogate.serializer(), surrogate)
    }
}

@Serializable(with = GameDataSerializer::class)
data class GameData(
    val title: String,
    val introductionMessage: String,
    val exitMessage: String,
    private val Rooms: MutableMap<Int, Room>,
    private val Items: MutableMap<Int, Item>,
    private val States: MutableMap<String, State>
) {
    fun getRoomMap(): Map<Int, Room> {
        return Rooms
    }

    fun getRoomList(): List<Room> {
        return Rooms.values.toList()
    }

    fun getRoomById(id: Int): Room? {
        return Rooms[id]
    }

    fun setRoomById(id: Int, room: Room) {
        Rooms.remove(id)
        Rooms.put(room.id, room)
    }

    fun getItemMap(): Map<Int, Item> {
        return Items
    }

    fun getItemList(): List<Item> {
        return Items.values.toList()
    }

    fun getItemsForRoom(roomId: Int): List<Item> {
        return Items.values.filter { it.location == roomId }
    }

    fun getItemById(id: Int): Item? {
        return Items[id]
    }

    fun getItemByNameAndRoom(name: String, roomId: Int): Item? {
        return getItemsForRoom(roomId).find {
            it.matchesName(name)
        }
    }

    fun setItemById(id: Int, item: Item) {
        Items.remove(id)
        Items.put(item.id, item)
    }

    fun setItemLocation(itemId: Int, locationId: Int) {
        Items[itemId]?.location = locationId
    }

    fun setItemUsable(itemId: Int, usable: Boolean) {
        Items[itemId]?.usable = usable
    }

    fun setItemNumberOfUses(itemId: Int, numberOfUses: Int) {
        Items[itemId]?.numberOfUses = numberOfUses
    }

    fun getStateMap(): Map<String, State> {
        return States
    }

    fun getStateList(): List<State> {
        return States.values.toList()
    }

    fun getStateByKey(key: String): State? {
        return States[key]
    }

    fun setCurrentStateValue(key: String, state: String) {
        if(States[key]?.possibleValues?.contains(state) == true) {
            States[key]?.currentValue = state
        }
    }
}
