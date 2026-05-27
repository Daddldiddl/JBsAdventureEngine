
package net.daddldiddl.jbsadventure.model

import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.model.lang.*

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ExitState (
    override var open: Boolean = true,
    override var locked: Boolean = false,
    val visible: Boolean = true
) : IOpenLockEnabled {

    fun getStateDescription(): String? {
        val stateDescriptions = mutableListOf<String>()
        if (isLocked()) {
            stateDescriptions.add(LANG.getStateValueFromKey(Keys.StateValues.locked))
        }
        else if (!isOpen()) {
            stateDescriptions.add(LANG.getStateValueFromKey(Keys.StateValues.closed))
        }
        return if (stateDescriptions.isNotEmpty()) {
            stateDescriptions.first() // For now, we return only the first applicable state. This can be expanded to combine states if needed.
        } else {
            null
        }
    }
}

@Serializable
data class ExitSurrogate (
    val direction: String,
    val targetRoomId: Int,
    val name: Name? = null,
    val exitState: ExitState = ExitState(),
    val description: String? = null,
    val itemUsages: List<ItemUsage>? = null
)

/**
 * Custom serializer for [Exit] that reads/writes the JSON array format
 * via [ExitSurrogate] while the runtime representation uses [MutableMap]s.
 */
object ExitSerializer : KSerializer<Exit>{
    override val descriptor: SerialDescriptor = ExitSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Exit {
        val surrogate = decoder.decodeSerializableValue(ExitSurrogate.serializer())
        return Exit(
            direction = surrogate.direction,
            targetRoomId = surrogate.targetRoomId,
            name = surrogate.name = "",
            exitState = surrogate.exitState,
            description = surrogate.description,
            itemUsages = surrogate.itemUsages)
    }

    override fun serialize(encoder: Encoder, value: Exit) {
        val surrogate = ExitSurrogate(
            direction = value.direction,
            targetRoomId = value.targetRoomId,
            name = value.name,
            exitState = value.exitState,
            description = value.description
            itemUsages = value.itemUsages
        )
        encoder.encodeSerializableValue(ExitSurrogate.serializer(), surrogate)
    }
}

/**
 * Represents an exit from a room in a specific direction, leading to another room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
*/
@Serializable(with = ExitSerializer::class)
class Exit (
    val direction: String,
    val targetRoomId: Int,
    val name: Name? = null,
    val exitState: ExitState = ExitState(),
    val description: String? = null,
    val itemUsages: List<ItemUsage>? = null
) {   

    /**
     * Returns a descriptive name for the exit, using the exit's name if available, or falling back to the direction's display name.
     * If [definite] is `true`, the name will be returned in its definite form (e.g., "the north exit"); otherwise, it will be returned in its indefinite form (e.g., "a north exit").
     */
    fun getDescriptiveName(definite: Boolean = false): String {
        var descriptiveName = direction
        if (name != null) {
            descriptiveName = if (definite) "${name.getDefiniteName()}" 
                              else "${name.getIndefiniteName()}"
        }
        return descriptiveName
    }

    fun isVisible(): Boolean { return exitState.visible }

    fun getDetailedDescription(): String {
        var descriptionText = ""
        if(description != null) {
            descriptionText = description
        }
        val template = when(name) {
            null -> LANG.getMessageTemplate(Keys.Messages.msgExitDetailedDescriptionNoName)
            else -> LANG.getMessageTemplate(Keys.Messages.msgExitDetailedDescription)
        }
        var message = template
        if(exitState.isLocked() || exitState.isClosed()) {
            val stateDescription = exitState.getStateDescription()
            if (stateDescription != null) {
                val stateTemplate = when(name) {
                    null -> LANG.getMessagePart(Keys.MessageParts.)
                    else -> LANG.getMessagePart(Keys.MessageParts.msgPartExitState)
                }
                message += " ${stateTemplate.replace(Keys.Placeholders.placeholderState, stateDescription)}"
            }
        }
        return message.replace(LangKey.Placeholders.placeholderExit, getDescriptiveName(definite = true))
                .replace(LangKey.Placeholders.placeholderDirection, direction.getDisplayName())
                .replace(LangKey.Placeholders.placeholderDescription, descriptionText)
                .trim()
    }
}
