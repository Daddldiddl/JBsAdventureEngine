package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.model.actions.*
import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.tools.serializers.RoomSerializer
import net.daddldiddl.jbsadventure.model.actions.*
import kotlin.collections.orEmpty


/**
 * Represents a room within the game world.
 *
 * A room has navigable [exits] to adjacent rooms and may contain items.
 * [itemUsages] defines which items can be used here and what effect each use triggers.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable(with = RoomSerializer::class)
data class Room(
    val id: Int,
    override var name: Name,
    override var description: String?,
    override val onExamine: List<Action> = emptyList(),
    val exits: Map<String, Exit>? = emptyMap(),
    val itemUsages: List<ItemUsage>? = emptyList()
) : NamedEntity
{
    /** Returns all exits currently marked as visible. */
    fun getVisibleExits(): List<Exit> {
        return exits.orEmpty().values.filter { it.visible }
    }

    /** Returns all items currently located in this room. */
    fun getItems(gameData: GameData): List<Item> {
        return gameData.getItemsForRoom(id)
    }

    /**
     * Returns the [ItemUsage] definition for the given item in this room, if one exists.
     *
     * @param itemId The ID of the item to look up.
     * @return The matching [ItemUsage], or `null` if no usage is defined for that item here.
     */
    fun getItemUsage(itemId: Int): ItemUsage? {
        return itemUsages?.find { it.itemId == itemId }
    }

    /** Resolves an exit by direction alias first, then by exit name/alias. */
    fun findExitByAlias(input: String): Exit? {
        val roomExits = exits.orEmpty()
        val directionKey = LANG.getDirectionKeyFromAlias(input)
        val byDirection = roomExits[directionKey]
        if (byDirection != null) {
            return byDirection
        }
        return roomExits.values.find { it.nameMatches(input) }
    }


    /**
     * Returns a debug-friendly name for the room, including its ID.
     */
    override fun debugName(): String {
        return "'${name.name}' (id=$id)"
    }    

    /** Returns the plain room name for compact debug output. */
    override fun toString(): String {
        return name.name
    }
}
