package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.tools.serializers.ItemSerializer

/**
 * Represents an item that can exist within the game world.
 */
@Serializable(with = ItemSerializer::class)
open class Item(
    val id: Int,
    override val name: Name,
    override val description: String?,
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList(),
) : NamedEntity {

    fun debugName(): String {
        return replacePlaceholdersName(
            "<name> (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
        )
    }

    override fun toString(): String {
        return name.name
    }

    override fun getDescriptiveName(definite: Boolean): String {
        val state = stateKey?.let { DATA.getStateByKey(it) }
        if (state != null) {
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
        if (state != null) {
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
            LANG.getMessageTemplate("msgItemDetailedDescription")
        } else {
            LANG.getMessageTemplate("msgItemDetailedDescriptionNoDescription")
        }
        return template
            .replace(Keys.Placeholders.definiteName, getDescriptiveName(definite = true))
            .replace(Keys.Placeholders.description, description ?: "")
            .replace(Keys.Placeholders.stateDescription, stateMessagePart)
            .replace(Keys.Placeholders.state, stateMessagePart)
            .trim()
    }
}
