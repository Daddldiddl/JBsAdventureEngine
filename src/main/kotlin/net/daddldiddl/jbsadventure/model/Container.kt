package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.model.actions.Action

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
    onExamine: List<Action> = emptyList(),
    carriable: Boolean? = false,
    driveable: Boolean? = false,
    stateKey: String? = null,
    usable: Boolean = true,
    numberOfUses: Int? = null,
    location: Int,
    comment: String? = null,
    usages: List<ItemUsage>? = emptyList(),
    override var open: Boolean = false,
    override var locked: Boolean = false,
    override val keyId: Int? = null,
    val containedItems: MutableList<Int> = mutableListOf(),
) : Item(
    id = id,
    name = name,
    description = description,
    onExamine = onExamine,
    carriable = carriable,
    driveable = driveable,
    stateKey = stateKey,
    usable = usable,
    numberOfUses = numberOfUses,
    location = location,
    comment = comment,
    usages = usages,
), OpenLockEnabledNamedEntity {

    /** Returns a descriptive display name including open/lock state text. */
    override fun getDescriptiveName(definite: Boolean?): String {
        return super<OpenLockEnabledNamedEntity>.getDescriptiveName(definite)
    }

    /** Returns base description and, if open, details about contained items. */
    override fun getDetailedDescription(): String {
        val base = super<Item>.getDetailedDescription()
        if (!isOpen()) {
            return base
        }

        val containedItemNames = getContainedItems(DATA).joinToString(", ") { it.getDescriptiveName() }
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

    /** Resolves and returns currently contained item objects from global game data. */
    fun getContainedItems(gameData: GameData): List<Item> {
        return gameData.getContainerItems(id)
    }

    override val supportsOpenClose: Boolean
        get() = true
    override val supportsLockUnlock: Boolean
        get() = false

    /** Adds one item ID to this container. */
    fun addItem(itemId: Int, gameData: GameData) {
        addItems(listOf(itemId), gameData)
    }

    /** Adds multiple item IDs to this container. */
    fun addItems(itemIds: List<Int>, gameData: GameData) {
        val items: List<Item> = gameData.getItemList().filter { it.id in itemIds }
        containedItems.addAll(itemIds)
        for (item in items) {
            item.location = FixedLocation.CONTAINER.value
        }
    }

    /** Removes one item ID from this container. */
    fun removeItem(itemId: Int, gameData: GameData) {
        removeItems(listOf(itemId), gameData)
    }

    /** Removes multiple item IDs from this container. */
    fun removeItems(itemIds: List<Int>, gameData: GameData) {
        val items: List<Item> = gameData.getItemList().filter { it.id in itemIds }
        containedItems.removeAll(itemIds)
        for (item in items) {
            item.location = FixedLocation.NOT_ASSIGNED.value
        }
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
}