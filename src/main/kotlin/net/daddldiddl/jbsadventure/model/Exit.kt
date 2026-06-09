package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.lang.LanguageData
import net.daddldiddl.jbsadventure.model.actions.Action
import net.daddldiddl.jbsadventure.tools.serializers.ExitSerializer

/**
 * Represents an exit from a room in a specific direction, leading to another room.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable(with = ExitSerializer::class)
class Exit(
    val direction: String,
    val targetRoomId: Int,
    override var name: Name = Name(direction),
    override var description: String? = null,
    override val onExamine: List<Action> = emptyList(),
    override val supportsOpenClose: Boolean = false,
    override val supportsLockUnlock: Boolean = false,
    override val onOpen: List<Action> = emptyList(),
    override val onClose: List<Action> = emptyList(),
    override val onLock: List<Action> = emptyList(),
    override val onUnlock: List<Action> = emptyList(),
    override var open: Boolean = true,
    override var locked: Boolean = false,
    override val keyId: Int?,
    override val consumeKeyOnLock: Boolean = false,
    override val consumeKeyOnUnlock: Boolean = false,
    var visible: Boolean = true,
    var blocked: Boolean = false,
    var blockedDescription: String? = null,
    val itemUsages: List<ItemUsage>? = null,
) : OpenLockEnabledNamedEntity {

    override fun getDescriptiveName(definite: Boolean?): String {
        return if (direction == name.name) direction else "${super.getDescriptiveName(definite)} ($direction)"
    }

    override fun getDetailedDescription(): String {
        val lang = LanguageData.current
        val desc = if (blocked) blockedDescription else description
        val template = when (name.name) {
            in lang.getAllDirectionAliases() -> {
                when (desc) {
                    null -> lang.getTemplate(Keys.Message.msgExitDetailedNoDescriptionNoName)
                    else -> lang.getTemplate(Keys.Message.msgExitDetailedDescriptionNoName)
                }
            }
            else -> {
                when (desc) {
                    null -> lang.getTemplate(Keys.Message.msgExitDetailedNoDescription)
                    else -> lang.getTemplate(Keys.Message.msgExitDetailedDescription)
                }
            }
        }
        var message = template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = false))
            .replace(Keys.StandIn.direction, lang.getDirectionAliasFromKey(direction))
            .replace(Keys.StandIn.description, desc ?: "")
            .trim()
        if (blocked) {
            message = lang.getTemplate(Keys.Part.state)
                .replace(Keys.StandIn.state, lang.getStateValueFromKey(Keys.StateValue.blocked))
                .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
                .trim()
        } else if (isLocked() || isClosed()) {
            message = "$message ${getMessagePartOpenLockedState()}"
        }
        return replacePlaceholdersName(message).trim()
    }

    override fun nameMatches(lookupName: String): Boolean {
        return super.nameMatches(lookupName) || direction.equals(lookupName, ignoreCase = true)
    }

    override fun isOpen(): Boolean = open && !blocked

    override fun toString(): String {
        return if (direction == name.name) direction else "${name.name} ($direction)"
    }

    override fun debugName(): String {
        return if (!name.name.equals(direction)) "'${name.name}' ($direction)" else "'$direction'"
    }
}
