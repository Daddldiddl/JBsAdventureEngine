package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable

@Serializable
class Name(
    val name: String,
    val article: Article = Article()
) {
    fun getIndefiniteName(): String {
        return article.getIndefinite(name)
    }

    fun getDefiniteName(): String {
        return article.getDefinite(name)
    }
}
