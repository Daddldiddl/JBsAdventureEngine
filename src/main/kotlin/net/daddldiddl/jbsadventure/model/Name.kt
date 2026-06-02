package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys

/**
 * Localizable name metadata for entities.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class Name(
    val name: String,
    val aliases: List<String> = emptyList(),
    val genderKey: String = LANG.defaultPronoun.genderKey,
    val isPlural: Boolean = false
) {
    constructor(name: String, aliases: List<String>) : this(name, aliases, LANG.defaultPronoun.genderKey, false)
}

/**
 * Base interface for named, describable game entities with language-aware helpers.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface NamedEntity {
    var name: Name
    var description: String?

    private val regexVocalStart: Regex
        get() = "^[aeouiAEOUI]".toRegex()

    /**
     * Returns the appropriate article based on definiteness, plurality, and gender.
     */
    fun getArticle(definite: Boolean = false): String {
        return when (definite) {
            true -> LANG.pronounGroups[name.genderKey]?.definiteArticle ?: LANG.defaultPronoun.definiteArticle
            else -> LANG.pronounGroups[name.genderKey]?.indefiniteArticle ?: LANG.defaultPronoun.indefiniteArticle
        }
    }


    /** Replaces `<name>` placeholders with entity-specific values. */
    fun replacePlaceholdersName(msg: String): String {
        return msg.replace(Keys.StandIn.name, name.name)
        .replace(Keys.StandIn.indefiniteName, getIndefiniteName())
        .replace(Keys.StandIn.definiteName, getDefiniteName())
    }

    /** Replaces subject-pronoun placeholder in a message template. */
    fun replacePlaceholderSubjectPronoun(msg: String): String {
        return msg.replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
    }

    /** Replaces object-pronoun placeholder in a message template. */
    fun replacePlaceholderObjectPronoun(msg: String): String {
        return msg.replace(Keys.StandIn.pronounObject, getPronounObject() ?: "")
    }

    /**
     * Returns the name of the entity in its indefinite form, including the appropriate article based on the language's grammar rules.
     */
    fun getIndefiniteName(): String {
        val article: String = getArticle(definite=false)
        return trimEmptySpaces("$article ${name.name}".trim())
    }

    /**
     * Returns the name of the entity in its definite form, including the appropriate article based on the language's grammar rules.
     */
    fun getDefiniteName(): String {
        return trimEmptySpaces("${getArticle(definite=true)} ${name.name}".trim())
    }

    /**
     * Returns the subject pronoun for the entity, based on its gender and plurality.
     */
    fun getPronounSubject(): String? {
        return LANG.getPronounSubject(genderKey = name.genderKey).trim()
    }

    /**
     * Returns the object pronoun for the entity, based on its gender and plurality.
     */
    fun getPronounObject(): String? {
        return LANG.getPronounObject(genderKey = name.genderKey).trim()
    }

    /**
     * Returns the possessive adjective for the entity, based on its gender and plurality.
     */
    fun getPossessiveAdjective(): String? {
        return LANG.getPossessiveAdjective(genderKey = name.genderKey).trim()
    }

    /**
     * Returns the possessive noun for the entity, based on its gender and plurality.
     */
    fun getPossessiveNoun(): String? {
        return LANG.getPossessiveNoun(genderKey = name.genderKey).trim()
    }

    /**
     * Checks if the provided name matches the entity's name or any of its aliases (non-case-sensitive!).
     */
    fun nameMatches(lookupName: String): Boolean {
        val lowerName = lookupName.lowercase()
        return lowerName == this.name.name.lowercase() ||
                name.aliases.any { it.lowercase() == lowerName }
    }

    /** Builds a localized message part describing the current state value. */
    fun getStateMessage(stateValue: String): String {
        val msgPart = LANG.getTemplate(if (name.isPlural) Keys.Part.msgPartStatePlural else Keys.Part.msgPartState)
            .replace(Keys.StandIn.state, stateValue)
            .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "").trim()

        return startUpperCase(msgPart)
    }

    /** Normalizes and collapses whitespace in localized output text. */
    fun trimEmptySpaces(input: String): String {
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    /** Returns text with an uppercased first character after trimming. */
    fun startUpperCase(input: String) : String {
        val inputTrimmed = trimEmptySpaces(input)
        if(inputTrimmed.isEmpty()) return inputTrimmed
        else return inputTrimmed.replaceFirstChar { it.uppercase() }
    }

    /** Returns either definite or indefinite descriptive entity name. */
    fun getDescriptiveName(definite: Boolean? = false): String {
        return if (definite == true) getDefiniteName() else getIndefiniteName()
    }

    /** Returns a formatted, sentence-cased detailed description. */
    fun getDetailedDescription(): String {
        return startUpperCase(description  ?: "")
    }
}