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
    val genderKey: String,
    val definiteArticle: String,
    val indefiniteArticle: String,
    val pronounSubject: String,
    val pronounObject: String,
    val possessiveAdjective: String,
    val possessiveNoun: String,
    val definiteArticlePlural: String,
    val indefiniteArticlePlural: String,
    val pronounSubjectPlural: String,
    val pronounObjectPlural: String,
    val possessiveAdjectivePlural: String,
    val possessiveNounPlural: String
)

@Serializable
data class LanguageDataSurrogate(
    val languageKey: String?,
    val defaultPronounGroupKey: String?,
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
/**
 * Represents the data for the language, including directions, commands, messages, and other game-related text.
 */
@Serializable(with = LanguageDataSerializer::class)
data class LanguageData(
    /** The key for the language, e.g., "en" for English */
    val languageKey: String,
    /** The key for the default pronoun group */
    val defaultPronounGroupKey: String = Keys.Pronouns.defaultDefaultPronounGroupKey,
    /** initial mutable for default pronoun group */
    val pronounGroups: MutableMap<String, PronounGroup> = emptyMap<String, PronounGroup>().toMutableMap(),
    /** fixed map of direction aliases */
    val directions: Map<String, Set<String>> = emptyMap(),
    /** fixed map of command data */
    val commands: Map<String, CommandData> = emptyMap(),
    /** fixed map of message templates */
    val messages: Map<String, String> = emptyMap(),
    /** fixed map of headings (for texts like the help screen) */
    val headings: Map<String, String> = emptyMap(),
    /** fixed map of state value translations for item/exit states like closed/locked */
    val stateValues: Map<String, String> = emptyMap(),
    /** fixed map of message parts for constructing dynamic messages */
    val messageParts: Map<String, String> = emptyMap(),
    /** fixed set of input parts to ignore (e.g., articles, pronouns) */
    val partsToIgnore: Set<String> = emptySet()
) {
    /** if no default PronounGroup for  pronoun values and articles is provided,
     * we use this default one, which is basically just the English "it" pronoun group,
     * but with the genderKey of the default group, so it can be used as a fallback for
     * missing values in the language data. */
    val defaultPronoun: PronounGroup = pronounGroups[defaultPronounGroupKey]
        ?:  if(!pronounGroups.isEmpty()) pronounGroups.values.iterator().next() // just take the first one if there is no default group key, but there are groups at all
            else PronounGroup(
                genderKey = defaultPronounGroupKey,
                definiteArticle = "the",
                indefiniteArticle = "a",
                possessiveNoun = "its",
                pronounSubject = "it",
                pronounObject = "it",
                possessiveAdjective = "its",
                definiteArticlePlural = "the",
                indefiniteArticlePlural = "",
                pronounSubjectPlural = "they",
                pronounObjectPlural = "them",
                possessiveAdjectivePlural = "their",
                possessiveNounPlural = "theirs"
            ) // no values at all

    init {
        // we created a new PronounGroup as none were provided
        if(pronounGroups.isEmpty()) {
            pronounGroups.put(
                key = defaultPronounGroupKey, value = defaultPronoun
            )
        }
        // there were pronoun groups, but no default one, so we add the default one as fallback with the expected default key if it doesn't match the provided default key
        else if(defaultPronounGroupKey != defaultPronoun.genderKey) {
            pronounGroups.put(
                key = defaultPronounGroupKey, value = defaultPronoun
            )
        }
    }

    /**
     * Returns the appropriate article based on definiteness, plurality, and gender.
     */
    fun getArticle(definite: Boolean = false, plural:Boolean? = false, genderKey: String ?= defaultPronoun.genderKey): String {
        return when(plural?:false) {
            false -> when (definite) {
                true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.definiteArticle ?: defaultPronoun.definiteArticle
                else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.indefiniteArticle ?: defaultPronoun.indefiniteArticle
            }
            true -> when (definite) {
                true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.definiteArticlePlural?: defaultPronoun.definiteArticlePlural
                else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.indefiniteArticlePlural ?: defaultPronoun.indefiniteArticlePlural
            }
        }
    }

    /**
     * Returns the appropriate subject pronoun based on plurality and gender.
     */
    fun getPronounSubject(plural:Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return when(plural) {
            true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounSubjectPlural?: defaultPronoun.pronounSubjectPlural
            else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounSubject?: defaultPronoun.pronounSubject
        }
    }

    /**
     * Returns the appropriate object pronoun based on plurality and gender.
     */
    fun getPronounObject(plural :Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return when(plural) {
            true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounObjectPlural?: defaultPronoun.pronounObjectPlural
            else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounObject?: defaultPronoun.pronounObject
        }
    }

    /**
     * Returns the appropriate possessive adjective based on plurality and gender.
     */
    fun getPossessiveAdjective(plural :Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return when(plural) {
            true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveAdjectivePlural?: defaultPronoun.possessiveAdjectivePlural
            else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveAdjective?: defaultPronoun.possessiveAdjective
        }
    }

    /**
     * Returns the appropriate possessive noun based on plurality and gender.
     */
    fun getPossessiveNoun(plural :Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return when(plural) {
            true -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveNounPlural?: defaultPronoun.possessiveNounPlural
            else -> pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveNoun?: defaultPronoun.possessiveNoun
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
    fun getDirectionAliasesForKey(directionKey: String): List<String> {
        return directions[directionKey]?.toList() ?: emptyList()
    }

    /**
     * Checks if the given string is a valid command (or direction in place of a GO command).
     */
    fun isCommandAlias(input: String): Boolean {
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
    fun getDirectionKeyFromAlias(input: String): String {
        for ((directionKey, aliases) in directions) {
            if (aliases.contains(input)) {
                return directionKey
            }
        }
        return "<unknown direction: '$input'>"
    }

    /**
     * Returns the alias of a direction based on its internal representation.
     */
    fun getDirectionAliasFromKey(directionKey: String): String {
        val  firstOrNull = directions[directionKey]?.firstOrNull { it == directionKey }
        return firstOrNull ?: "<unknown direction key: '$directionKey'>"
    }
    
    /**
     * Returns the translation of a state value based on its key.
     */
    fun getStateValueFromKey(stateValueKey: String): String {
        return stateValues[stateValueKey] ?: "<unknown state value key: '$stateValueKey'>"
}
