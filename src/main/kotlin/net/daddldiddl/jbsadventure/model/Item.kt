package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*
import net.daddldiddl.jbsadventure.tools.serializers.*

@Serializable(with = ItemSerializer::class)
data class Item : BaseItem {
    constructor(
        id: Int,
        name: Name,
        description: String,
        carriable: Boolean? = false,
        driveable: Boolean? = false,
        stateKey: String? = null,
        usable: Boolean = true,
        numberOfUses: Int? = null,
        location: Int,
        comment: String? = null,
        usages: List<ItemUsage>? = emptyList(),
    ) : super(
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
        usages = usages
    )
}

/**
 * Represents an item that can exist within the game world.
 *
 * Items reside in a room and can be examined or used by the player.
 * 
 * [usable] indicates whether the item can currently be used by the player;
 * [carriable] indicates whether the player can pick up the item;
 * [driveable] indicates whether using the item can cause it to relocate with the player.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
open class BaseItem : NamedEntity (
    // The unique identifier for this item.
    val id: Int,
    // The primary name of the item, used for display and matching player input.
    override val name: Name,
    // A description of the item, shown to the player when they examine it.
    override val description: String ?= null,
    // Indicates whether the player can pick up and carry this item in their inventory.
    val carriable: Boolean? = false,
    // Indicates whether using this item can cause it to move with the player (e.g., a vehicle).
    val driveable: Boolean? = false,
    // An optional key that links this item to a game state, allowing its description or behavior to change based on the current value of that state.
    val stateKey: String? = null,
    // Indicates whether the item can currently be used by the player.
    var usable: Boolean = true,
    // The number of times the item can be used before it becomes unusable.
    var numberOfUses: Int? = null,
    // The ID of the room where the item is currently located.
    var location: Int = FixedLocations.NOT_ASSIGNED.value,
    // An optional comment or note about the item, not shown to the player.
    val comment: String? = null,
    // A list of usages that define what happens when the player uses this item in different rooms or contexts.
    val usages: List<ItemUsage>? = emptyList(),
) : NamedItemState
{
    /**
     * Returns a debug-friendly name for the item, including its ID.
     */
    fun debugName(): String {
        return replacePlaceholdersName("<name> (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
    }

    override fun toString(): String {
        return name.name
    }

    override fun getDescriptiveName(definite: Boolean): String {
        val state = stateKey?.let { DATA.getStateForKey(it) }
        if(state != null ) {
            return LANG.getMessagePart(Keys.MessageParts.msgPartDescriptiveName)
                .replace(Keys.Placeholders.article, LANG.getArticle(definite = definite))
                .replace(Keys.Placeholders.state, state.currentValue)
                .replace(Keys.Placeholders.name, name.name)
                .trim()
        }
        return super.getDescriptiveName(definite)
    }

    fun getStateMessagePart(): String {
        val state = stateKey?.let { DATA.getStateForKey(it) }
        if(state != null ) {
            return LANG.getMessagePart(Keys.MessageParts.msgPartState)
                .replace(Keys.Placeholders.state, state.currentValue)
                .replace(Keys.Placeholders.pronounSubject, getPronounSubject() ?: "")
                .trim()
        }
        return ""
    }

    override fun getDetailedDescription(): String {
        val stateMessagePart = getStateMessagePart()
        val template = if (description != null) {
            LANG.getMessage(Keys.Messages.msgItemDetailedDescription)
        } else {
            LANG.getMessage(Keys.Messages.msgItemDetailedDescriptionNoDescription)
        }
        return template
            .replace(Keys.Placeholders.definiteName, getDescriptiveName(definite = true))
            .replace(Keys.Placeholders.description, description ?: "")
            .replace(Keys.Placeholders.state, stateMessagePart)
            .trim()
    }}


