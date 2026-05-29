package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.tools.serializers.*
impotrt net.daddldiddl.jbsadventure.LOG

@Serializable(with = GameDataSerializer::class)
data class GameData(
    val title: String,
    val introductionMessage: String,
    val exitMessage: String,
    private val Rooms: MutableMap<Int, Room>,
    private val Items: MutableMap<Int, Item>,
    val States: MutableMap<String, State>
) {
    /**
     * Returns a map of all rooms in the game, keyed by their ID.
     */
    fun getRoomMap(): Map<Int, Room> {
        return Rooms
    }

    /**
     * Returns a list of all rooms in the game.
     */
    fun getRoomList(): List<Room> {
        return Rooms.values.toList()
    }

    /**
     * Returns the room with the specified ID, or null if no such room exists.
     */
    fun getRoomById(id: Int): Room? {
        return Rooms[id]
    }

    /**
     * Returns a list of all visible exits in the specified room.
     */
    fun getVisibleExitsForRoom(roomId: Int): List<Exit> {
        return getRoomById(roomId)?.exits?.values?.filter { it.visible } ?: emptyList()
    }

    /**
     * Returns a list of all accessible (visible AND open) exits in the specified room.
     */
    fun getAccessibleExitsForRoom(roomId: Int): List<Exit> {
        return getVisibleExitsForRoom(roomId).filter { it.isOpen() }
    }

    /**
     * Returns a map of all items in the game, keyed by their ID.
     */
    fun getItemMap(): Map<Int, Item> {
        return Items
    }

    /**
     * Returns a list of all items in the game.
     */
    fun getItemList(): List<Item> {
        return Items.values.toList()
    }

    /**
     * Returns a list of all items in the specified room.
     */
    fun getItemsForRoom(roomId: Int): List<Item> {
        return Items.values.filter { it.location == roomId }
    }

    /**
     * Returns a list of all items in the player's inventory.
     */
    fun getInventoryItems(): List<Item> {
        return getItemsForRoom(FixedLocations.INVENTORY.value)
    }

    /**
     * Returns a list of all items in open containers in the specified room.
     */
    fun getOpenContainerItemsForRoom(roomId: Int): List<Item> {
        return getOpenContainersForRoom(roomId).flatMap { container -> container.getContainedItems() }
    }

    /**
     * Returns a list of all accessible items in the specified room, including items in the room, open containers, and the player's inventory.
     */
    fun getAllAccessibleItemsForRoom(roomId: Int): List<Item> {
        val roomItems = getItemsForRoom(roomId)
        val openContainerItems = getOpenContainerItemsForRoom(roomId)
        val inventoryItems = getInventoryItems()
        return roomItems + openContainerItems + inventoryItems
    }

    /**
     * Returns a list of all containers in the game.
     */
    fun getContainerList(): List<Container> {
        return Items.values.filter { it is Container }.map { it as Container }
    }

    /**
     * Returns a list of all open containers in the specified room.
     */
    fun getOpenContainersForRoom(roomId: Int): List<Container> {
        return Items.values.filter { it.location == roomId && it is Container && (it as Container).isOpen() }.map { it as Container }
    }

    /**
     * Returns the container that contains the specified item, or null if no such container exists.
     */
    fun getItemContainer(itemId: Int): Container? {
        val item = Items[itemId] ?: return null
        if(item.location == FixedLocations.CONTAINER.value) {
            return getContainerList().find { it.containsItem(itemId) }
        }
        return null
    }

    /**
     * Returns the item with the specified ID, or null if no such item exists.
     */
    fun getItemById(id: Int): Item? {
        return Items[id]
    }

    /**
     * Returns the item with the specified name in the specified room, or null if no such item exists in neither the room, open containers, nor the player's inventory.
     */
    fun getAccessibleItemByNameAndRoom(name: String, roomId: Int): Item? {
        return getAllAccessibleItemsForRoom(roomId).find {
            it.matchesName(name)
        }
    }

    /**
     * Sets the location of the item with the specified ID.
     */
    fun setItemLocation(itemId: Int, locationId: Int) {
        Items[itemId]?.location = locationId
    }

    /**
     * Sets whether the item with the specified ID is usable.
     */
    fun setItemUsable(itemId: Int, usable: Boolean) {
        Items[itemId]?.usable = usable
    }

    /**
     * Sets the number of uses for the item with the specified ID.
     */ 
    fun setItemNumberOfUses(itemId: Int, numberOfUses: Int) {
        Items[itemId]?.numberOfUses = numberOfUses
    }

    /**
     * Returns a map of all game states, keyed by their unique string key.
     */
    fun getStateMap(): Map<String, State> {
        return States
    }

    /**
     * Returns a list of all game states.
     */
    fun getStateList(): List<State> {
        return States.values.toList()
    }

    /**
     * Returns the game state with the specified key, or null if no such state exists.
     */
    fun getStateByKey(key: String): State? {
        return States[key]
    }

    /**
     * Returns true if the game state with the specified key has the expected value, or false otherwise.
     */
    fun isStateByKey(key: String, expectedState: String): Boolean {
        return States[key]?.currentValue == expectedState
    }

    /**
     * Returns true if the game state with the specified key has the expected value, or false otherwise.
     */
    fun isAllowedStateForKey(key: String, stateToValidate: String): Boolean {
        return States[key]?.possibleValues?.contains(stateToValidate) == true
    }

    /**
     * Sets the current value of the game state with the specified key.
     */
    fun setCurrentStateValue(key: String, newState: String) {
        if(isAllowedStateForKey(key, newState)) {
            States[key]?.currentValue = newState
        } else {
            LOG.warn("Attempted to set state '$key' to invalid value '$newState'. Valid values are: ${States[key]?.possibleValues}")
        }
    }
}
