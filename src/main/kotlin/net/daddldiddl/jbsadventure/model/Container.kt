package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

@Serializable
data class Container (val containedItems: MutableList<Int>, override var open: Boolean, override var locked: Boolean) : IOpenLockEnabled
{
    getItems(){

    }
}

