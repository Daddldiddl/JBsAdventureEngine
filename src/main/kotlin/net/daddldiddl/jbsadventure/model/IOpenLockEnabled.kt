package net.daddldiddl.jbsadventure.model

interface IOpenLockEnabled {
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
        if(!open && !locked) {
            open = true
            return isOpen()
        }
        return false
     }

    fun lock() : Boolean{
        if(!locked) {
            locked = true
            return isLocked()
        }
        return false
    }

    fun unlock() : Boolean{
        if(locked) {
            locked = !isLocked()
            return !isLocked()
        }
        return false
    }

    fun close(): Boolean {
        if(!open && !locked) {
            open = false
            return !isOpen()
        }
        return false
    }
}