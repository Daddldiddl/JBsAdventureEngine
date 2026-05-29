package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.Keys

interface OpenLockEnabledNamedEntity : NamedEntity, OpenLockEnabledEntity {
    fun getMessagePartOpenLockedState(): String {
        return getMessagePartState(getOpenLockState())
    }

    fun getDescriptiveName(definite: Boolean = false): String {
        var string = ""
        return get
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
            isLocked() && isClosed() -> LANG.getStateValueFromKey(Keys.StateValues.lockedClosed)
            isLocked() && isOpen() -> LANG.getStateValueFromKey(Keys.StateValues.lockedOpen)
            isClosed() -> LANG.getStateValueFromKey(Keys.StateValues.closed)
            else -> LANG.getStateValueFromKey(Keys.StateValues.open)
        }
    }

}