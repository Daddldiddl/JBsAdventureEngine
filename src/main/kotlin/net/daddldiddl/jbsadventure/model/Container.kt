package net.daddldiddl.jbsadventure.model
package net.daddldiddl.jbsadventure.model.lang

import kotlinx.serialization.Serializable

interface ContainerEntity : OpenLockEnabledNamedEntity{
    val containedItems: MutableList<Int>

    fun addItem(itemId: Int) {
        containedItems.add(itemId)
    }

    fun addItems(itemIds: List<Int>) {
        containedItems.addAll(itemIds)
    }

    fun removetem(itemId: Int) {
        containedItems.remove(itemId)
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

    fun containsItem(Item: Item): Boolean {
        return containsItem(Item.id)
    }

    fun getContainedItems(): List<Item> {
        return containedItems.toList()
    }

}


@Serializable(with = ItemSerializer::class)
data class Container : BaseItem, ContainerEntity {
    override val containedItems: MutableList<Int> = mutableListOf()

    constructor(
        id: Int,
        name: Name,
        description: String,
        alternateNames: List<String> = emptyList(),
        carriable: Boolean? = false,
        driveable: Boolean? = false,
        stateKey: String? = null,
        usable: Boolean = true,
        numberOfUses: Int? = null,
        location: Int,
        comment: String? = null,
        usages: List<ItemUsage>? = emptyList()
    ) : super(
        id, name, description, alternateNames, carriable, driveable, stateKey, usable, numberOfUses, location, comment, usages
    )

    override fun getDescriptiveName(definite: Boolean): String {
        return LANG.getMessagePart(Keys.MessageParts.msgPartDescriptiveName))
            .replace(Keys.Placeholders.article, LANG.getArticle(definite = definite))
            .replace(Keys.Placeholders.state, getOpenLockedState())
            .replace(Keys.Placeholders.name, name.name)
    }

    override fun getDescriptionWithState(gameData: GameData): String {
        var message = super.getDescriptionWithState(gameData)
        if(isOpen() && !isEmpty()) {
            val containedItemNames = getContainedItems().joinToString(", ") { it.getDescriptiveName() }
            message += "\n${LANG.getMessage(Keys.Messages.msgContainerContents).replace(Keys.Placeholders.placeholderItems, containedItemNames)}"
        } else if (isOpen() && isEmpty()) {
            message += "\n${replacePlaceholdersName(LANG.getMessage(Keys.Messages.msgContainerEmpty))}"
        } else {
            message += "\n${getMessagePartOpenLockedState()}"
        }
        return replacePlaceholdersSubject(replacePlaceholdersName(message))
    }
}