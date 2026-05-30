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

    companion object Constants {
        const val NOTASSIGNED_LOCATION: Int = 0
    }

    fun debugName(): String {
        return replacePlaceholdersName(
            "<name> (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
        )
    }

    override fun toString(): String {
        return name.name
    }

    // Compatibility wrapper for older call sites.
    fun matchesName(lookupName: String): Boolean {
        return nameMatches(lookupName)
    }

    // Compatibility wrapper for older call sites.
    fun getArticle(definite: Boolean? = false): String {
        return LANG.getArticle(definite = definite == true, genderKey = name.genderKey)
    }

    override fun getDescriptiveName(definite: Boolean?): String {
        val state = stateKey?.let { DATA.getStateByKey(it) }
        if (state != null) {
            return LANG.getMessagePart(Keys.Part.msgPartDescriptiveName)
                .replace(Keys.StandIn.article, LANG.getArticle(definite = definite == true))
                .replace(Keys.StandIn.state, state.currentValue)
                .replace(Keys.StandIn.name, name.name)
                .trim()
        }
        return super.getDescriptiveName(definite)
    }

    fun getStateMessagePart(): String {
        val state = stateKey?.let { DATA.getStateByKey(it) }
        if (state != null) {
            return LANG.getMessagePart(Keys.Part.msgPartState)
                .replace(Keys.StandIn.state, state.currentValue)
                .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
                .trim()
        }
        return ""
    }

    // Compatibility wrapper for older call sites.
    fun descriptionWithState(gameData: GameData): String {
        return getDetailedDescription()
    }

    override fun getDetailedDescription(): String {
        val stateMessagePart = getStateMessagePart()
        val template = if (description != null) {
            LANG.getMessage("msgItemDetailedDescription")
        } else {
            LANG.getMessage("msgItemDetailedDescriptionNoDescription")
        }
        return template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
            .replace(Keys.StandIn.description, description ?: "")
            .replace(Keys.StandIn.stateDescription, stateMessagePart)
            .replace(Keys.StandIn.state, stateMessagePart)
            .trim()
    }
}
