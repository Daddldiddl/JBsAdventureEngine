package net.daddldiddl.jbsadventure.lang

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.tools.serializers.LanguageDataSerializer

/**
 * Represents the data for a command, including its aliases and description.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class CommandData(
    val aliases: List<String>,
    val description: String,
    val adjective: String? = null,
    val verb: String? = null
) {
    fun verb(): String = verb ?: aliases.first()
    fun adjective(): String = adjective ?: aliases.first()
}

/**
 * Defines one pronoun group used for localized naming and placeholder replacement.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class PronounGroup(
    val genderKey: String,
    val definiteArticle: String,
    val indefiniteArticle: String,
    val pronounSubject: String,
    val pronounObject: String,
    val possessiveAdjective: String,
    val possessiveNoun: String,
    val none: String
)


/**
 * Represents the data for the language, including directions, commands, messages, and other game-related text.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
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
                none = "no"
            ) // no values at all

    init {
        // we created a new PronounGroup as none were provided
        if(pronounGroups.isEmpty()) {
            pronounGroups[defaultPronounGroupKey] = defaultPronoun
        }
        // there were pronoun groups, but no default one, so we add the default one as fallback with the expected default key if it doesn't match the provided default key
        else if(defaultPronounGroupKey != defaultPronoun.genderKey) {
            pronounGroups[defaultPronounGroupKey] = defaultPronoun
        }
    }

    /**
     * Returns the appropriate subject pronoun based on plurality and gender.
     */
    fun getPronounSubject(plural:Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounSubject?: defaultPronoun.pronounSubject
    }

    /**
     * Returns the appropriate object pronoun based on plurality and gender.
     */
    fun getPronounObject(plural :Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return pronounGroups[genderKey ?: defaultPronoun.genderKey]?.pronounObject?: defaultPronoun.pronounObject
    }

    /**
     * Returns the appropriate possessive adjective based on plurality and gender.
     */
    fun getPossessiveAdjective(plural :Boolean ?= false, genderKey :String ?= defaultPronoun.genderKey): String {
        return pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveAdjective?: defaultPronoun.possessiveAdjective
    }

    /**
     * Returns the appropriate possessive noun based on plurality and gender.
     */
    fun getPossessiveNoun(genderKey :String ?= defaultPronoun.genderKey): String {
        return pronounGroups[genderKey ?: defaultPronoun.genderKey]?.possessiveNoun?: defaultPronoun.possessiveNoun
    }

    /**
     * Returns the matching template for a message key.
     *
     * Keys starting with `msgPart` are resolved from `messageParts`.
     * All other keys are resolved from `messages`.
     */
    fun getTemplate(key: String): String {
        if (key.startsWith("msgPart")) {
            return messageParts[key] ?: run {
                LOG.warn("Warning: No message part found for key '$key', returning key as message.")
                return "Unknown message part key: $key"
            }
        }
        return messages[key]  ?: run {
            LOG.warn("Warning: No message found for key '$key', returning key as message.")
            return "Unknown message key: $key"
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
     * Checks if the given string is a valid command (or direction in place of a GO command).
     */
    fun getCommandFromAlias(input: String): String? {
        for(key in commands.keys) {
            if(commands[key]?.aliases?.contains(input) ?: false) {
                return key
            }
        }
        if(LANG.getAllDirectionAliases().contains(input)) {;
            return Keys.Command.go
        }
        return ""
    }

    /**
     * Checks if the given string is a valid command (or direction in place of a GO command).
     */
    fun getCommandAlias(key: String): String {
        return commands[key]?.aliases?.first() ?: "'$key'"
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
    fun getDirectionKeyFromAlias(input: String): String? {
        for ((directionKey, aliases) in directions) {
            if (aliases.contains(input)) {
                return directionKey
            }
        }
        return null
    }

    /**
     * Returns the alias of a direction based on its internal representation.
     */
    fun getDirectionAliasFromKey(directionKey: String): String {
        val  firstOrNull = directions[directionKey]?.firstOrNull { it == directionKey }
        return firstOrNull ?: "<unknown direction key: '$directionKey'>"
    }
    
    /**
     * Returns the translated state value for the given key, or the key itself if no translation is found.
     */
    fun getStateValueFromKey(key: String): String {
        return stateValues[key] ?: run {
            LOG.warn("Warning: No state value found for key '$key', returning key as value.")
            return "Unknown state value key: $key"
        }
    }
}
