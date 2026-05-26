package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG

@Serializable
class Name(
    val name: String,
    val genderKey: String ?= null
) {

    private val regexVocalStart = "^[aeouiAEOUI]".toRegex()

    fun getIndefiniteName(): String {
        val srticle = "${LANG.getArticle(genderKey = genderKey)} $name".trim()
        return (LANG.languageKey == "en" && name.matches(regex = regexVocalStart)
    }

    fun getDefiniteName(): String {
        return "${LANG.getArticle(true, genderKey = genderKey)} $name".trim()
    }

    fun getPossesiveName(): String {
        return "${LANG.getPossessivePronoun(genderKey = genderKey)} $name".trim()
    }

    override fun toString(): String {
        return name
    }
}
