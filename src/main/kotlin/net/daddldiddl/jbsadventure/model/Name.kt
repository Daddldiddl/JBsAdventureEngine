package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.lang.*
import net.daddldiddl.jbsadventure.model.actions.Action
import java.text.Normalizer

/**
 * Localizable name metadata for entities.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class Name(
    val name: String,
    val aliases: List<String> = emptyList(),
    val genderKey: String = Keys.Pronouns.defaultDefaultPronounGroupKey,
    val isPlural: Boolean = false
)

/**
 * Base interface for named, describable game entities with language-aware helpers.
 * All formatting methods use [LanguageData.current] – set via GlobalContext.initialize().
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface NamedEntity {
    var name: Name
    var description: String?
    val onExamine: List<Action>

    fun debugName(): String

    /** Returns the appropriate article based on definiteness and gender. */
    fun getArticle(definite: Boolean = false): String {
        val lang = LanguageData.current
        return when (definite) {
            true -> lang.pronounGroups[name.genderKey]?.definiteArticle ?: lang.defaultPronoun.definiteArticle
            else -> lang.pronounGroups[name.genderKey]?.indefiniteArticle ?: lang.defaultPronoun.indefiniteArticle
        }
    }

    /** Replaces `<name>` placeholders with entity-specific values. */
    fun replacePlaceholdersName(msg: String): String {
        return msg.replace(Keys.StandIn.name, name.name)
            .replace(Keys.StandIn.indefiniteName, getIndefiniteName())
            .replace(Keys.StandIn.definiteName, getDefiniteName())
    }

    /** Replaces `<nameTarget>` placeholders with entity-specific values. */
    fun replacePlaceholdersTargetName(msg: String): String {
        return msg.replace(Keys.StandIn.nameTarget, name.name)
            .replace(Keys.StandIn.indefiniteNameTarget, getIndefiniteName())
            .replace(Keys.StandIn.definiteNameTarget, getDefiniteName())
    }

    /** Replaces subject-pronoun placeholder in a message template. */
    fun replacePlaceholderSubjectPronoun(msg: String): String {
        return msg.replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
    }

    /** Replaces object-pronoun placeholder in a message template. */
    fun replacePlaceholderObjectPronoun(msg: String): String {
        return msg.replace(Keys.StandIn.pronounObject, getPronounObject() ?: "")
    }

    /** Returns the name in its indefinite form with the appropriate article. */
    fun getIndefiniteName(): String {
        return trimEmptySpaces("${getArticle(definite = false)} ${name.name}".trim())
    }

    /** Returns the name in its definite form with the appropriate article. */
    fun getDefiniteName(): String {
        return trimEmptySpaces("${getArticle(definite = true)} ${name.name}".trim())
    }

    /** Returns the subject pronoun based on gender. */
    fun getPronounSubject(): String? {
        return LanguageData.current.getPronounSubject(genderKey = name.genderKey).trim()
    }

    /** Returns the object pronoun based on gender. */
    fun getPronounObject(): String? {
        return LanguageData.current.getPronounObject(genderKey = name.genderKey).trim()
    }

    /** Returns the possessive adjective based on gender. */
    fun getPossessiveAdjective(): String? {
        return LanguageData.current.getPossessiveAdjective(genderKey = name.genderKey).trim()
    }

    /** Returns the possessive noun based on gender. */
    fun getPossessiveNoun(): String? {
        return LanguageData.current.getPossessiveNoun(genderKey = name.genderKey).trim()
    }

    /** Checks if the provided name matches the entity's name or any alias (case-insensitive). */
    fun nameMatches(lookupName: String): Boolean {
        val lookupVariants = normalizedNameVariants(lookupName)
        if (lookupVariants.isEmpty()) {
            return false
        }

        val allNames = sequenceOf(this.name.name) + name.aliases.asSequence()
        return allNames.any { candidate ->
            val candidateVariants = normalizedNameVariants(candidate)
            lookupVariants.any { it in candidateVariants }
        }
    }

    private fun normalizedNameVariants(value: String): Set<String> {
        val lower = value.lowercase().trim()
        if (lower.isBlank()) {
            return emptySet()
        }

        val collapsedWhitespace = lower.replace("\\s+".toRegex(), " ")
        val lettersDigitsOnly = collapsedWhitespace.replace("[^\\p{L}\\p{Nd}]".toRegex(), "")
        val noSpaces = collapsedWhitespace.replace(" ", "")

        val nfd = Normalizer.normalize(lettersDigitsOnly, Normalizer.Form.NFD)
        val withoutDiacritics = nfd.replace("\\p{M}+".toRegex(), "")

        val umlautExpanded = lettersDigitsOnly
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
        val umlautFlattened = lettersDigitsOnly
            .replace("ä", "a")
            .replace("ö", "o")
            .replace("ü", "u")
            .replace("ß", "ss")
        val umlautDropped = lettersDigitsOnly
            .replace("ä", "")
            .replace("ö", "")
            .replace("ü", "")
            .replace("ß", "ss")

        return setOf(
            lower,
            collapsedWhitespace,
            noSpaces,
            lettersDigitsOnly,
            withoutDiacritics,
            umlautExpanded,
            umlautFlattened,
            umlautDropped
        ).filter { it.isNotBlank() }.toSet()
    }

    /** Builds a localized message part describing the current state value. */
    fun getStateMessage(stateValue: String): String {
        val lang = LanguageData.current
        val msgPart = lang.getTemplate(if (name.isPlural) Keys.Part.statePlural else Keys.Part.state)
            .replace(Keys.StandIn.state, stateValue)
            .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "").trim()
        return startUpperCase(msgPart)
    }

    /** Normalizes and collapses whitespace in localized output text. */
    fun trimEmptySpaces(input: String): String {
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    /** Returns text with an uppercased first character after trimming. */
    fun startUpperCase(input: String): String {
        val inputTrimmed = trimEmptySpaces(input)
        if (inputTrimmed.isEmpty()) return inputTrimmed
        else return inputTrimmed.replaceFirstChar { it.uppercase() }
    }

    /** Returns either definite or indefinite descriptive entity name. */
    fun getDescriptiveName(definite: Boolean? = false): String {
        return if (definite == true) getDefiniteName() else getIndefiniteName()
    }

    /** Returns a formatted, sentence-cased detailed description. */
    fun getDetailedDescription(): String {
        return startUpperCase(description ?: "")
    }

    fun getPronumGroup(): PronounGroup {
        val lang = LanguageData.current
        return lang.pronounGroups[name.genderKey] ?: lang.defaultPronoun
    }
}