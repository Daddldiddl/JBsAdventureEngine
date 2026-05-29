package net.daddldiddl.jbsadventure.lang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.model.GameData

@Serializable
data class LanguageDataSurrogate(
    val languageKey: String?,
    val defaultPronounGroupKey: String?,
    @SerialName("PronounGroups")
    val pronounGroups: List<PronounGroup>,
    @SerialName("Directions")
    val directions: Map<String, Set<String>>,
    @SerialName("Commands")
    val commands: Map<String, CommandData>,
    @SerialName("Messages")
    val messages: Map<String, String>,
    @SerialName("Headings")
    val headings: Map<String, String>,
    @SerialName("StateValues")
    val stateValues: Map<String, String>,
    @SerialName("MessageParts")
    val messageParts: Map<String, String>,
    @SerialName("PartsToIgnore")
    val partsToIgnore: Set<String>
)

/**
 * Custom serializer for [GameData] that reads/writes the JSON array format
 * via [LanguageDataSurrogate] while the runtime representation uses [MutableMap]s.
 */

object LanguageDataSerializer : KSerializer<LanguageData> {
    override val descriptor: SerialDescriptor = LanguageDataSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): LanguageData {
        val surrogate = decoder.decodeSerializableValue(LanguageDataSurrogate.serializer())
        return LanguageData(
            languageKey = surrogate.languageKey ?: "en",
            defaultPronounGroupKey = surrogate.defaultPronounGroupKey ?: Keys.Pronouns.defaultDefaultPronounGroupKey,
            pronounGroups = surrogate.pronounGroups.associateBy { it.genderKey }.toMutableMap(),
            directions = surrogate.directions,
            commands = surrogate.commands,
            headings = surrogate.headings,
            stateValues = surrogate.stateValues,
            messageParts = surrogate.messageParts,
            messages = surrogate.messages,
            partsToIgnore = surrogate.partsToIgnore
        )
    }

    override fun serialize(encoder: Encoder, value: LanguageData) {
        val surrogate = LanguageDataSurrogate(
            languageKey = value.languageKey,
            defaultPronounGroupKey = value.defaultPronounGroupKey,
            pronounGroups = value.pronounGroups.values.toList(),
            directions = value.directions,
            commands = value.commands,
            headings = value.headings,
            stateValues = value.stateValues,
            messageParts = value.messageParts,
            partsToIgnore = value.partsToIgnore,
            messages = value.messages,
        )
        encoder.encodeSerializableValue(LanguageDataSurrogate.serializer(), surrogate)
    }

}