
package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.model.actions.Action
import net.daddldiddl.jbsadventure.tools.serializers.ExitSerializer

/**
 * Represents an exit from a room in a specific direction, leading to another room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
*/
@Serializable(with = ExitSerializer::class)
class Exit (
    val direction: String,
    val targetRoomId: Int,
    override var name: Name = Name(LANG.getDirectionAliasFromKey(direction)),
    override var description: String? = null,
    override val onExamine: List<Action> = emptyList(),
    override val supportsOpenClose: Boolean = false,
    override val supportsLockUnlock: Boolean = false,
    override var open: Boolean = true,
    override var locked: Boolean = false,
    override val keyId: Int?,
    var visible: Boolean = true,
    var blocked: Boolean = false,
    var blockedDescription: String? = null,
    val itemUsages: List<ItemUsage>? = null
) : OpenLockEnabledNamedEntity {
    /**
     * Returns a descriptive name for the exit, using the exit's name if available, or falling back to the direction's display name.
     * If [definite] is `true`, the name will be returned in its definite form (e.g., "the north exit"); otherwise, it will be returned in its indefinite form (e.g., "a north exit").
     */
    override fun getDescriptiveName(definite: Boolean?): String {
        return if(direction == name.name) direction else "${super.getDescriptiveName(definite)} ($direction)"
    }

    override fun getDetailedDescription(): String {
        val desc = if(blocked) blockedDescription else description
        val template = when (desc) {
            null -> LANG.getTemplate(Keys.Message.msgExitDetailedNoDescription)
            else -> LANG.getTemplate(Keys.Message.msgExitDetailedDescription)
        }
        var message = template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = false))
            .replace(Keys.StandIn.direction, LANG.getDirectionAliasFromKey(direction))
            .replace(Keys.StandIn.description, desc ?: "")
            .trim()
        if(blocked){
            message = LANG.getTemplate(Keys.Part.msgPartState)
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

    override fun debugName(): String {
        return if(!name.name.equals(direction)) "'${name.name}' ($direction)" else "'$direction'"
    }

}
