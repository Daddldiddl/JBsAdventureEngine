package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.tools.serializers.ItemSerializer

interface ContainerEntity : OpenLockEnabledNamedEntity {
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

    fun getContainedItems(): List<Item> {
        return containedItems.mapNotNull { DATA.getItemById(it) }
    }
}

@Serializable(with = ItemSerializer::class)
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

    override fun getDescriptiveName() {
        super<ContainerEntity>.getDescriptiveName()
    }

    override fun getDetailedDescription(): String {
        val base = super<Item>.getDetailedDescription()
        if (!isOpen()) {
            return base
        }

        val containedItemNames = getContainedItems().joinToString(", ") { it.getDescriptiveName() }
        val details = if (containedItemNames.isBlank()) {
            LANG.getMessageTemplate("msgContainerEmpty")
                .replace(Keys.Placeholders.definiteName, getDescriptiveName(definite = true))
        } else {
            LANG.getMessageTemplate("msgContainerContents")
                .replace(Keys.Placeholders.definiteName, getDescriptiveName(definite = true))
                .replace(Keys.Placeholders.items, containedItemNames)
        }

        return "$base\n$details".trim()
    }
}