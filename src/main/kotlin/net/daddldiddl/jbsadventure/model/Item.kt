package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*
import net.daddldiddl.jbsadventure.tools.serializers.*



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
@Serializable(with = ItemSerializer::class)
open class BaseItem(
    val id: Int,
    override val name: Name,
    override val description: String?,
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    val usable: Boolean = true,
    val numberOfUses: Int? = null,
    val location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList(),
) : NamedEntity
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
        val state = stateKey?.let { DATA.getStateByKey(it) }
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
        val state = stateKey?.let { DATA.getStateByKey(it) }
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


