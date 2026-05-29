package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.*

@Serializable
data class Name(
    val name: String,
    val aliases: List<String> = emptyList(),
    val genderKey: String = LANG.defaultPronoun.genderKey,
    val isPlural: Boolean = false
) {
    constructor(name: String, aliases: List<String>) : this(name, aliases, LANG.defaultPronoun.genderKey, false)
}

interface NamedEntity {
    val name: Name
    val description: String?

    private val regexVocalStart: Regex
        get() = "^[aeouiAEOUI]".toRegex()

    fun replacePlaceholdersName(msg: String): String {
        return msg.replace(Keys.Placeholders.name, name.name)
        .replace(Keys.Placeholders.indefiniteName, getIndefiniteName())
        .replace(Keys.Placeholders.definiteName, getDefiniteName())
    }

    fun replacePlaceholderSubjectPronoun(msg: String): String {
        return msg.replace(Keys.Placeholders.pronounSubject, getPronounSubject() ?: "")
    }

    fun replacePlaceholderObjectPronoun(msg: String): String {
        return msg.replace(Keys.Placeholders.pronounObject, getPronounObject() ?: "")
    }

    /**
     * Returns the name of the entity in its indefinite form, including the appropriate article based on the language's grammar rules.
     */
    fun getIndefiniteName(): String {
        val englishException = (!name.isPlural && LANG.languageKey == Keys.languageKeyEn && name.name.matches(regex = regexVocalStart))
        val article: String = if (englishException) "an" else LANG.getArticle(definite=false, genderKey = name.genderKey)
        return trimEmptySpaces("$article ${name.name}".trim())
    }

    /**
     * Returns the name of the entity in its definite form, including the appropriate article based on the language's grammar rules.
     */
    fun getDefiniteName(): String {
        return trimEmptySpaces("${LANG.getArticle(definite=true, genderKey = name.genderKey)} ${name.name}".trim())
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

    fun getMessagePartState(stateValue: String): String {
        val msgPart = LANG.getMessagePart(if (name.isPlural) Keys.MessageParts.msgPartStatePlural else Keys.MessageParts.msgPartState)
            .replace(Keys.Placeholders.state, stateValue)
            .replace(Keys.Placeholders.pronounSubject, getPronounSubject() ?: "").trim()

        return startUpperCase(msgPart)
    }

    fun trimEmptySpaces(input: String): String {
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    fun startUpperCase(input: String) : String {
        val inputTrimmed = trimEmptySpaces(input)
        if(inputTrimmed.isEmpty()) return inputTrimmed
        else return inputTrimmed.replaceFirstChar { it.uppercase() }
    }

    fun getDescriptiveName(definite:Boolean = false): String {
        return if (definite) getDefiniteName() else getIndefiniteName()
    }

    fun getDetailedDescription(): String {
        return startUpperCase(description  ?: "")
    }
}