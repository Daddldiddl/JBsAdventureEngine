package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG
import kotlin.plus

@Serializable
class Name(
    val name: String,
    val genderKey: String ?= null,
    val isPlural: Boolean = false
) {

    private val regexVocalStart by lazy { "^[aeouiAEOUI]".toRegex() }

    fun getIndefiniteName(): String {
        val englishException = (LANG.languageKey == Keys.languageKeyEn && name.matches(regex = regexVocalStart))
        val article: String = if (!isPlural){
            if(englishException) "an" else LANG.getArticle(definite=false, genderKey = genderKey)
        }
        else {
            return "${LANG.getArticle(definite=false, plural=isPlural, genderKey = genderKey)} $name".trim()
        }
        return "$article $name".trim()
    }

    fun getDefiniteName(): String {
        return "${LANG.getArticle(definite=true, plural=isPlural, genderKey = genderKey)} $name".trim()
    }

    override fun toString(): String {
        return name
    }
}
