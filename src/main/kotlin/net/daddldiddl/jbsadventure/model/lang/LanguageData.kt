package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.model.GameData
import kotlin.collections.List

/**
 * Represents the data for a command, including its aliases and description.
 */
@Serializable
data class CommandData(
    val aliases: List<String>,
    val description: String
)

@Serializable
data class PronounGroup(
    val key: String,
    val definiteArticle: String,
    val indefiniteArticle: String,
    val pronoun: String,
    val pluralPronoun: String,
    val possessivePronoun: String,
    val possessivePluralPronoun: String,
    val isDefault: Boolean ?= false
)

@Serializable
data class LanguageDataSurrogate(
    val languageKey: String?,
    val pronounGroups: List<PronounGroup>,
    val directions: Map<String, Set<String>>,
    val commands: Map<String, CommandData>,
    val messages: Map<String, String>,
    val headings: Map<String, String>,
    val stateValues: Map<String, String>,
    val messageParts: Map<String, String>,
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
            pronounGroups = surrogate.pronounGroups.associateBy { it.key }.toMutableMap(),
            directions = surrogate.directions,
            commands = surrogate.commands,
            headings = surrogate.headings,
            stateValues = surrogate.stateValues,
            messageParts = surrogate.messageParts,
            messages = surrogate.messages,
            partsToIgnore = surrogate.partsToIgnore,
            languageKey = "en"
        )
    }

    override fun serialize(encoder: Encoder, value: LanguageData) {
        val surrogate = LanguageDataSurrogate(
            languageKey = value.languageKey,
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
/**
 * Represents the data for the language, including directions, commands, messages, and other game-related text.
 */
@Serializable(with = LanguageDataSerializer::class)
data class LanguageData(
    val languageKey: String,
    val pronounGroups: MutableMap<String, PronounGroup> = emptyMap<String, PronounGroup>().toMutableMap(),
    val directions: Map<String, Set<String>> = emptyMap(),
    val commands: Map<String, CommandData> = emptyMap(),
    val messages: Map<String, String> = emptyMap(),
    val headings: Map<String, String> = emptyMap(),
    val stateValues: Map<String, String> = emptyMap(),
    val messageParts: Map<String, String> = emptyMap(),
    val partsToIgnore: Set<String> = emptySet()
) {
    val defaultPronoun: PronounGroup = pronounGroups[Keys.Pronouns.keyDefaultGroup]
        ?:  if(pronounGroups.isNotEmpty()) pronounGroups.values.first() // no default value
            else PronounGroup(
            Keys.Pronouns.keyDefaultGroup,
            definiteArticle = "the",
            indefiniteArticle = "a",
            pronoun = "it",
            pluralPronoun = "they",
            possessivePronoun = "its",
            possessivePluralPronoun = "their"
        ) // no values at all

    init {
        if(pronounGroups.isEmpty()){
            pronounGroups.set(
                key = Keys.Pronouns.keyDefaultGroup, value = defaultPronoun
            )
        }
    }

    fun getArticle(definite: Boolean = false, genderKey: String ?= defaultPronoun.key): String {
        return when(definite){
            true -> pronounGroups[genderKey]?.definiteArticle ?: defaultPronoun.definiteArticle
            else -> pronounGroups[genderKey]?.indefiniteArticle ?: defaultPronoun.indefiniteArticle
        }
    }

    fun getPronoun(singular: Boolean = false, genderKey: String ?= defaultPronoun.key): String {
        return when(singular){
            true -> pronounGroups[genderKey]?.pronoun ?: defaultPronoun.pronoun
            false -> pronounGroups[genderKey]?.pluralPronoun ?: defaultPronoun.pluralPronoun
        }
    }

    fun getPossessivePronoun(singular: Boolean = false, genderKey: String ?= defaultPronoun.key): String {
        return when(singular){
            true -> pronounGroups[genderKey]?.possessivePronoun ?: defaultPronoun.possessivePronoun
            false -> pronounGroups[genderKey]?.possessivePluralPronoun ?: defaultPronoun.possessivePluralPronoun
        }
    }

    /**
     * Returns the matching message template - may contain placeholders.
     */
    fun getMessageTemplate(key: String): String {
        return messages[key] ?: run {
            LOG.warn("Warning: No message found for key '$key', returning key as message.")
            return "Unknown message key: $key"
        }
    }

    /**
     * Returns the matching messagePart template - may contain placeholders.
     */
    fun getMessagePart(key: String): String {
        return messageParts[key] ?: run {
            LOG.warn("Warning: No message part found for key '$key', returning key as message part.")
            return "Unknown message part key: $key"
        }
    }

    /**
     * Returns the requested state value
     */
    fun getStateValue(key: String): String {
        return stateValues[key] ?: run {
            LOG.warn("Warning: No state value found for key '$key', returning key as value.")
            return "Unknown state value key: $key"
        }
    }

    /**
     * Returns the list of aliases for the given command, or an empty list if the command is not defined.
     */
    fun getCommandAliases(commandKey: String): List<String> {
        return commands[commandKey]?.aliases ?: emptyList()
    }

    /**
     * Returns the description for the given command, or null if the command is not defined.
     */
    fun getCommandDescription(commandKey: String): String? {
        return commands[commandKey]?.description
    }

    /**
     * Returns the list of direction aliases for the given direction, or an empty list if the direction is not defined.
     */
    fun getDirectionAliases(direction: String): List<String> {
        return directions[direction]?.toList() ?: emptyList()
    }

    /**
     * Checks if the given string is a valid command (or direction in place of a GO command).
     */
    fun isCommand(input: String): Boolean {
        for(cmd in commands.values) {
            if(cmd.aliases.contains(input)) {
                return true
            }
        }
        return isMoveCommand(input)
    }

    /**
     * Returns whether the command is either an alias for the GO command or a direction
     */
    fun isMoveCommand(input: String): Boolean {
        if(commands[Keys.Commands.go]?.aliases?.contains(input) ?: false) {
            return true
        }
        if (getAllDirectionAliases().contains(input)) {
            return true
        }
        return false
    }

    /**
     * Returns a list of all direction aliases.
     */
    fun getAllDirectionAliases(): List<String> {
        val allDirections = mutableListOf<String>()
        for (aliases in directions.values) {
            allDirections.addAll(aliases)
        }
        return allDirections.distinct()
    }

    /**
     * Returns the internal representation of a direction based on its alias.
     */
    fun getInternalDirection(input: String): String {
        for ((internal, aliases) in directions) {
            if (aliases.contains(input)) {
                return internal
            }
        }
        return "<unknown direction: '$input'>"
    }

    /**
     * Returns the alias of a direction based on its internal representation.
     */
    fun getDirectionFromInternal(directionKey: String): String {
        val  firstOrNull = directions[directionKey]?.firstOrNull { it == directionKey }
        return firstOrNull ?: "<unknown direction key: '$directionKey'>"
    }

}
