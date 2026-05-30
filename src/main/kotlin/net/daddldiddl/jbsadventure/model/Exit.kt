
package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*
import net.daddldiddl.jbsadventure.tools.serializers.*

import kotlinx.serialization.Serializable

/**
 * Represents an exit from a room in a specific direction, leading to another room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
*/
@Serializable(with = ExitSerializer::class)
class Exit (
    val direction: String,
    val targetRoomId: Int,
    override val name: Name = Name(LANG.getDirectionAliasFromKey(direction)),
    override var open: Boolean = true,
    override var locked: Boolean = false,
    override val description: String? = null,
    var blocked: Boolean = false,
    var blockedDescription: String? = null,
    val itemUsages: List<ItemUsage>? = null,
    val visible: Boolean = true
) : OpenLockEnabledNamedEntity {
    /**
     * Returns a descriptive name for the exit, using the exit's name if available, or falling back to the direction's display name.
     * If [definite] is `true`, the name will be returned in its definite form (e.g., "the north exit"); otherwise, it will be returned in its indefinite form (e.g., "a north exit").
     */
    override fun getDescriptiveName(definite: Boolean?): String {
        var descriptiveName = direction
        if (name != null) {
            descriptiveName = super.getDescriptiveName(definite)
        }
        return descriptiveName
    }

    override fun getDetailedDescription(): String {
        val desc = if(blocked) blockedDescription else description
        val template = when (desc){
            null -> when(name) {
                null -> LANG.getMessage(Keys.Message.msgExitDetailedNoDescriptionNoName)
                else -> LANG.getMessage(Keys.Message.msgExitDetailedNoDescription)
            }
            else -> when(name) {
                null -> LANG.getMessage(Keys.Message.msgExitDetailedDescriptionNoName)
                else -> LANG.getMessage(Keys.Message.msgExitDetailedDescription)
            }
        }
        var message = template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = false))
            .replace(Keys.StandIn.direction, LANG.getDirectionAliasFromKey(direction))
            .replace(Keys.StandIn.description, desc ?: "")
            .trim()
        if(blocked){
            message = LANG.getMessagePart(Keys.Part.msgPartState)
                .replace(Keys.StandIn.state, LANG.getStateValueFromKey(Keys.StateValue.blocked))
                .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
                .trim()
        }
        else if(isLocked() || isClosed()) {
           message = "$message ${getMessagePartOpenLockedState()}"
        }
        return replacePlaceholdersName(message).trim()
    }

    override fun isOpen(): Boolean {
        return open && !blocked
    }

    override fun toString(): String {
        return getDescriptiveName()
    }
}
