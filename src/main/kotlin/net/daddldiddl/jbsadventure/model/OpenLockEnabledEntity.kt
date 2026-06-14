package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.lang.LanguageData
import net.daddldiddl.jbsadventure.model.actions.Action

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
        val lang = LanguageData.current
        val definiteArticle = definite == true
        val descriptiveBaseName = if (definiteArticle) name.definiteBaseName() else name.indefiniteBaseName()
        var template = lang.getTemplate(Keys.Part.descriptiveName)
        template = trimEmptySpaces(
            template
                .replace(Keys.StandIn.state, getOpenLockState())
                .replace(Keys.StandIn.name, descriptiveBaseName)
                .trim()
        )
        var article = getArticle(definiteArticle)
        if (!definiteArticle && lang.languageKey == Keys.languageKeyEn && !name.isPlural) {
            val nameWithoutArticle = template.replace(Keys.StandIn.article, "").trim()
            article = if (nameWithoutArticle.isNotEmpty() && nameWithoutArticle[0].lowercaseChar() in "aeiou") "an" else "a"
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
    val supportsOpenClose: Boolean get() = false
    val supportsLockUnlock: Boolean get() = false
    val onOpen: List<Action> get() = emptyList()
    val onClose: List<Action> get() = emptyList()
    val onLock: List<Action> get() = emptyList()
    val onUnlock: List<Action> get() = emptyList()

    var open: Boolean
    var locked: Boolean
    val keyId: Int?
    val consumeKeyOnUnlock: Boolean
    val consumeKeyOnLock: Boolean

    fun isOpen(): Boolean = open
    fun isClosed(): Boolean = !open
    fun isLocked(): Boolean = locked

    /** Attempts to open the entity and returns success state. */
    fun open(): Boolean {
        if (!isOpen() && !isLocked()) {
            open = true
            if (onOpen.isNotEmpty()) onOpen.forEach { it.execute(GameData.current) }
            return isOpen()
        }
        return false
    }

    /** Attempts to close the entity and returns success state. */
    fun close(): Boolean {
        if (isOpen() && !isLocked()) {
            open = false
            if (onClose.isNotEmpty()) onClose.forEach { it.execute(GameData.current) }
            return !isOpen()
        }
        return false
    }

    /** Attempts to lock the entity and returns success state. */
    fun lock(): Boolean {
        if (!isLocked()) {
            locked = true
            if (onLock.isNotEmpty()) onLock.forEach { if (it.checkPreconditions(GameData.current)) it.execute(GameData.current) }
            return isLocked()
        }
        return false
    }

    /** Attempts to unlock the entity and returns success state. */
    fun unlock(): Boolean {
        if (isLocked()) {
            locked = !isLocked()
            if (onUnlock.isNotEmpty()) onUnlock.forEach { if (it.checkPreconditions(GameData.current)) it.execute(GameData.current) }
            return !isLocked()
        }
        return false
    }

    /** Returns the localized state label for open/closed/locked combinations. */
    fun getOpenLockState(): String {
        val lang = LanguageData.current
        return when {
            supportsLockUnlock && isLocked() && isClosed() -> lang.getStateValueFromKey(Keys.StateValue.lockedClosed)
            supportsLockUnlock && isLocked() && isOpen()   -> lang.getStateValueFromKey(Keys.StateValue.lockedOpen)
            supportsOpenClose && isClosed()                -> lang.getStateValueFromKey(Keys.StateValue.closed)
            supportsOpenClose                              -> lang.getStateValueFromKey(Keys.StateValue.open)
            else -> ""
        }
    }

}