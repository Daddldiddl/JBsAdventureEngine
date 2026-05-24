
package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

@Serializable
data class ExitState(
    val closed: Boolean = false,
    val locked: Boolean = false,
    val blocked: Boolean = false,
    val visible: Boolean = true
){
    fun getStateDescription(): String? {
        val stateDescriptions = mutableListOf<String>()
        if (blocked) {
            stateDescriptions.add(MESSAGES.getMessage(LangKeys.StateValues.exitBlocked))
        }
        if (locked) {
            stateDescriptions.add(MESSAGES.getMessage(LangKeys.StateValues.exitLocked))
        }
        if (closed) {
            stateDescriptions.add(MESSAGES.getMessage(LangKeys.StateValues.exitClosed))
        }
        return if (stateDescriptions.isNotEmpty()) {
            stateDescriptions.first() // For now, we return only the first applicable state. This can be expanded to combine states if needed.
        } else {
            null
        }
    }
}

/**
 * Represents an exit from a room in a specific direction, leading to another room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
*/
@Serializable
class Exit(
    val direction: Direction,
    val targetRoomId: Int,
    val exitState: ExitState = ExitState(),
    val name: Name? = null,
    val description: String? = null,
    val itemUsages: List<ItemUsage>? = null
) {   

    /**
     * Returns a descriptive name for the exit, using the exit's name if available, or falling back to the direction's display name.
     * If [definite] is `true`, the name will be returned in its definite form (e.g., "the north exit"); otherwise, it will be returned in its indefinite form (e.g., "a north exit").
     */
    fun getDescriptiveName(definite: Boolean = false): String {
        var descriptiveName = direction.getDisplayName()
        if (name != null) {
            descriptiveName = if (definite) name.getDefiniteName() else name.getIndefiniteName()
        }
        return descriptiveName
    }

    fun getDetailedDescription(): String {
        var descriptionText = ""
        if(description != null) {
            descriptionText = description
        }
        val template = when(name) {
            null -> MESSAGES.getMessage(LangKeys.Messages.msgExitDetailedDescriptionNoName)
            else -> MESSAGES.getMessage(LangKeys.Messages.msgExitDetailedDescription)
        }
        if(name != null) {
            val template = lang.messages[key]
        } else i
        var message = template
        message = message
        if(exit.exitState.blocked || exit.exitState.locked || exit.exitState.closed) {
            val stateDescription = exit.exitState.getStateDescription()
            if (stateDescription != null) {
                val stateTemplate = when(name) {
                    null -> MESSAGES.getMessagePart(LangKeys.MessageParts.msgPartExitStateNoName)
                    else -> MESSAGES.getMessagePart(LangKeys.MessageParts.msgPartExitState)
                }
                message += " ${stateTemplate.replace(LangKeys.Placeholders.placeholderState, stateDescription)}"
            }
        }
        return message.replace(LangKey.Placeholders.placeholderExit, getDescriptiveName(definite = true))
                .replace(LangKey.Placeholders.placeholderDirection, direction.getDisplayName())
                .replace(LangKey.Placeholders.placeholderDescription, descriptionText)
                .trim()
    }
}