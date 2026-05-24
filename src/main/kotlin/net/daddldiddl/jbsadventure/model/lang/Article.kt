package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable
/**
 * Represents the articles (definite and indefinite) associated with a name in the game.
 *
 * Each article can be defined explicitly, or if not provided, default articles will be used based on the language settings.
 * The getIndefinite and getDefinite methods return the appropriate article combined with the name, ensuring proper grammar in messages.
 * 
 * If no specific article is defined for a name, a warning is logged, and the default article from the language data is used as a fallback.
 * This allows for flexibility in language customization while maintaining a consistent user experience even when specific articles are not defined for certain names.
 * 
 * Copyright (c) 2026 by Jochen Brinkmann
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */


@Serializable
data class Article (
    val indefinte: String? = null,
    val definite: String? = null
){
    fun getIndefinite(name: String): String {
        return "${indefinte ?: getDefaultIndefiniteArticle(name)} $name"
    }

    fun getDefinite(name: String): String {
        return "${definite ?: getDefaultDefiniteArticle(name)} $name"
    }

    private fun getDefaultIndefiniteArticle(name: String): String {
        LOG.warn("Warning: No indefinite article defined for '$name', using default article.")
        return LangKey.DefaultValues.defaultIndefiniteArticle
    }

    private fun getDefaultDefiniteArticle(name: String): String {
        LOG.warn("Warning: No definite article defined for '$name', using default article.")
        return LangKey.DefaultValues.defaultDefiniteArticle
}