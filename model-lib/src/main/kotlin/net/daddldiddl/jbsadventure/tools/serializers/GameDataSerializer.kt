package net.daddldiddl.jbsadventure.tools.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.model.Item
import net.daddldiddl.jbsadventure.model.Room
import net.daddldiddl.jbsadventure.model.State


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
object GameDataSerializer : KSerializer<GameData> {
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