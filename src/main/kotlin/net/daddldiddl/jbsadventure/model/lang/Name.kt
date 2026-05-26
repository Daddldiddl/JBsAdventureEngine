package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG

@Serializable
class Name(
    val name: String,
    val genderKey: String ?= null,
    val isPlural: Boolean = false
) {

    private val regexVocalStart by lazy { "^[aeouiAEOUI]".toRegex() }

    fun getIndefiniteName(): String {
        val englishException = (LANG.languageKey == "en" && name.matches(regex = regexVocalStart))
        val article: String = if (!isPlural){
            if(englishException) "an" else LANG.getArticle(definite=false, genderKey = genderKey)
        }
        else {
            return "${LANG.getArticle(true, genderKey = genderKey)} $name".trim()
        }
        return "$article $name".trim()
    }

    fun getDefiniteName(): String {
        return "${LANG.getArticle(true, genderKey = genderKey)} $name".trim()
    }

    fun getIndefinitePluralName() {

    }

    override fun toString(): String {
        return name
    }
}
