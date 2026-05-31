package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys

/**
 * Capability interface for container-like entities that can hold other item IDs.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface ContainerEntity : OpenLockEnabledNamedEntity {
    override val supportsOpenClose: Boolean
        get() = true
    override val supportsLockUnlock: Boolean
        get() = false

    val containedItems: MutableList<Int>

    /** Adds one item ID to this container. */
    fun addItem(itemId: Int) {
        containedItems.add(itemId)
    }

    /** Adds multiple item IDs to this container. */
    fun addItems(itemIds: List<Int>) {
        containedItems.addAll(itemIds)
    }

    /** Removes one item ID from this container. */
    fun removeItem(itemId: Int) {
        containedItems.remove(itemId)
    }

    // Backward-compatible alias for the old typoed method name.
    fun removetem(itemId: Int) {
        removeItem(itemId)
    }

    /** Removes multiple item IDs from this container. */
    fun removeItems(itemIds: List<Int>) {
        containedItems.removeAll(itemIds)
    }

    /** Returns whether this container currently holds the given item ID. */
    fun containsItem(itemId: Int): Boolean {
        return containedItems.contains(itemId)
    }

    /** Returns whether this container currently holds no items. */
    fun isEmpty(): Boolean {
        return containedItems.isEmpty()
    }

    /** Returns a snapshot of contained item IDs. */
    fun getContainedItemIds(): List<Int> {
        return containedItems.toList()
    }

    /** Returns whether this container currently holds the given [Item]. */
    fun containsItem(item: Item): Boolean {
        return containsItem(item.id)
    }

    /** Resolves and returns currently contained item objects from global game data. */
    fun getContainedItemObjects(): List<Item> {
        return containedItems.mapNotNull { DATA.getItemById(it) }
    }

    override fun getDescriptiveName(definite: Boolean?): String {
        return super.getDescriptiveName(definite)
    }
}

/**
 * Runtime container item implementation.
 *
 * Behaves like an item plus open/close and contained-item handling.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
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

    /** Returns a descriptive display name including open/lock state text. */
    override fun getDescriptiveName(definite: Boolean?): String {
        return super<ContainerEntity>.getDescriptiveName(definite)
    }

    /** Returns base description and, if open, details about contained items. */
    override fun getDetailedDescription(): String {
        val base = super<Item>.getDetailedDescription()
        if (!isOpen()) {
            return base
        }

        val containedItemNames = getContainedItemObjects().joinToString(", ") { it.getDescriptiveName() }
        val details = if (containedItemNames.isBlank()) {
            LANG.getTemplate(Keys.Message.msgContainerEmpty)
                .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
        } else {
            LANG.getTemplate(Keys.Message.msgContainerContent)
                .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
                .replace(Keys.StandIn.items, containedItemNames)
        }

        return "$base\n$details".trim()
    }
}