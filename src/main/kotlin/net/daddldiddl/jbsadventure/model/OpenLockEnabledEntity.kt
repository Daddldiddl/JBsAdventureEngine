package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys

interface OpenLockEnabledNamedEntity : NamedEntity, OpenLockEnabledEntity {
    fun getMessagePartOpenLockedState(): String {
        return getMessagePartState(getOpenLockState())
    }

    override fun getDescriptiveName(definite: Boolean?): String {
        val template = LANG.getMessage(Keys.Part.msgPartDescriptiveName)
        return trimEmptySpaces(template
            .replace(Keys.StandIn.state, getOpenLockState())
            .replace(Keys.StandIn.article, LANG.getArticle(definite = false))
            .replace(Keys.StandIn.name, name.name)
            .trim())
    }
}

interface OpenLockEnabledEntity {
    var open: Boolean
    var locked: Boolean

    fun isOpen(): Boolean {
        return open
    }

    fun isClosed(): Boolean {
        return !open
    }

    fun isLocked(): Boolean {
        return locked
    }

    fun open() : Boolean{
        if(!isOpen() && !isLocked()) {
            open = true
            return isOpen()
        }
        return false
     }

    fun close(): Boolean {
        if(isOpen() && !isLocked()) {
            open = false
            return !isOpen()
        }
        return false
    }
    
    fun lock() : Boolean{
        if(!isLocked()) {
            locked = true
            return isLocked()
        }
        return false
    }

    fun unlock() : Boolean{
        if(isLocked()) {
            locked = !isLocked()
            return !isLocked()
        }
        return false
    }

    fun getOpenLockState(): String {
        return when {
            isLocked() && isClosed() -> LANG.getStateValueFromKey(Keys.StateValue.lockedClosed)
            isLocked() && isOpen() -> LANG.getStateValueFromKey(Keys.StateValue.lockedOpen)
            isClosed() -> LANG.getStateValueFromKey(Keys.StateValue.closed)
            else -> LANG.getStateValueFromKey(Keys.StateValue.open)
        }
    }

}