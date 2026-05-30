package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys

interface ContainerEntity : OpenLockEnabledNamedEntity {
    override val supportsOpenClose: Boolean
        get() = true
    override val supportsLockUnlock: Boolean
        get() = false

    val containedItems: MutableList<Int>

    fun addItem(itemId: Int) {
        containedItems.add(itemId)
    }

    fun addItems(itemIds: List<Int>) {
        containedItems.addAll(itemIds)
    }

    fun removeItem(itemId: Int) {
        containedItems.remove(itemId)
    }

    // Backward-compatible alias for the old typoed method name.
    fun removetem(itemId: Int) {
        removeItem(itemId)
    }

    fun removeItems(itemIds: List<Int>) {
        containedItems.removeAll(itemIds)
    }

    fun containsItem(itemId: Int): Boolean {
        return containedItems.contains(itemId)
    }

    fun isEmpty(): Boolean {
        return containedItems.isEmpty()
    }

    fun getContainedItemIds(): List<Int> {
        return containedItems.toList()
    }

    fun containsItem(item: Item): Boolean {
        return containsItem(item.id)
    }

    fun getContainedItemObjects(): List<Item> {
        return containedItems.mapNotNull { DATA.getItemById(it) }
    }

    override fun getDescriptiveName(definite: Boolean?): String {
        return super.getDescriptiveName(definite)
    }
}

class Container(
    id: Int,
    name: Name,
    description: String?,
    carriable: Boolean? = false,
    driveable: Boolean? = false,
    stateKey: String? = null,
    usable: Boolean = true,
    numberOfUses: Int? = null,
    location: Int,
    comment: String? = null,
    usages: List<ItemUsage>? = emptyList(),
    override val containedItems: MutableList<Int> = mutableListOf(),
    override var open: Boolean = false,
    override var locked: Boolean = false,
) : Item(
    id = id,
    name = name,
    description = description,
    carriable = carriable,
    driveable = driveable,
    stateKey = stateKey,
    usable = usable,
    numberOfUses = numberOfUses,
    location = location,
    comment = comment,
    usages = usages,
), ContainerEntity {

    override fun getDescriptiveName(definite: Boolean?): String {
        return super<ContainerEntity>.getDescriptiveName(definite)
    }

    override fun getDetailedDescription(): String {
        val base = super<Item>.getDetailedDescription()
        if (!isOpen()) {
            return base
        }

        val containedItemNames = getContainedItemObjects().joinToString(", ") { it.getDescriptiveName() }
        val details = if (containedItemNames.isBlank()) {
            LANG.getMessage(Keys.Message.msgContainerEmpty)
                .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
        } else {
            LANG.getMessage(Keys.Message.msgContainerContent)
                .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
                .replace(Keys.StandIn.items, containedItemNames)
        }

        return "$base\n$details".trim()
    }
}