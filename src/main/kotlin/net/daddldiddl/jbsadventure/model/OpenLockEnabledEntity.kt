package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys

/**
 * Mixin for named entities that expose open/lock state text in descriptions.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface OpenLockEnabledNamedEntity : NamedEntity, OpenLockEnabledEntity {
    /** Returns a language-aware message part for the current open/lock state. */
    fun getMessagePartOpenLockedState(): String {
        return getStateMessage(getOpenLockState())
    }

    /** Returns a descriptive name including translated open/lock state. */
    override fun getDescriptiveName(definite: Boolean?): String {
        val definiteArticle = definite == true
        var template = LANG.getTemplate(Keys.Part.descriptiveName)
        template =  trimEmptySpaces(template
            .replace(Keys.StandIn.state, getOpenLockState())
            .replace(Keys.StandIn.name, name.name)
            .trim())
        var article = getArticle(definiteArticle)
        if(!definiteArticle && LANG.languageKey == Keys.languageKeyEn && !name.isPlural) {
            val nameWithoutArticle = template.replace(Keys.StandIn.article, "").trim()
            // English has the special rule of using "an" instead of "a" before vowel sounds, so we handle this as a special case.
            // Note that this is a very simplified rule and does not cover all cases (e.g., "a university" vs. "an hour"), but it should work for most common cases in a text adventure game.
            article = if (nameWithoutArticle.subSequence(0,0).matches(Regex("[aeiouAEIOU]"))) "an" else "a"
        }
        return template.replace(Keys.StandIn.article, article).trim()
    }
}

/**
 * Capability interface for entities that can be opened/closed and optionally locked.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface OpenLockEnabledEntity {
    // Defaults for entities that do not support these interactions.
    val supportsOpenClose: Boolean
        get() = false
    val supportsLockUnlock: Boolean
        get() = false

    var open: Boolean
    var locked: Boolean
    val keyId: Int?

    /** Returns whether the entity is currently open. */
    fun isOpen(): Boolean {
        return open
    }

    /** Returns whether the entity is currently closed. */
    fun isClosed(): Boolean {
        return !open
    }

    /** Returns whether the entity is currently locked. */
    fun isLocked(): Boolean {
        return locked
    }

    /** Attempts to open the entity and returns success state. */
    fun open() : Boolean{
        if(!isOpen() && !isLocked()) {
            open = true
            return isOpen()
        }
        return false
     }

    /** Attempts to close the entity and returns success state. */
    fun close(): Boolean {
        if(isOpen() && !isLocked()) {
            open = false
            return !isOpen()
        }
        return false
    }
    
    /** Attempts to lock the entity and returns success state. */
    fun lock() : Boolean{
        if(!isLocked()) {
            locked = true
            return isLocked()
        }
        return false
    }

    /** Attempts to unlock the entity and returns success state. */
    fun unlock() : Boolean{
        if(isLocked()) {
            locked = !isLocked()
            return !isLocked()
        }
        return false
    }

    /** Returns the localized state label for open/closed/locked combinations. */
    fun getOpenLockState(): String {
        return when {
            supportsLockUnlock && isLocked() && isClosed() -> LANG.getStateValueFromKey(Keys.StateValue.lockedClosed)
            supportsLockUnlock && isLocked() && isOpen() -> LANG.getStateValueFromKey(Keys.StateValue.lockedOpen)
            supportsOpenClose && isClosed() -> LANG.getStateValueFromKey(Keys.StateValue.closed)
            supportsOpenClose -> LANG.getStateValueFromKey(Keys.StateValue.open)
            else -> ""
        }
    }

}