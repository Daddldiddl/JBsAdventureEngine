package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.lang.Keys
import net.daddldiddl.jbsadventure.lang.LanguageData
import net.daddldiddl.jbsadventure.model.actions.Action
import net.daddldiddl.jbsadventure.tools.serializers.ItemSerializer

/**
 * Represents an item that can exist within the game world.
 */
@Serializable(with = ItemSerializer::class)
open class Item(
    val id: Int,
    override var name: Name,
    override var description: String?,
    override val onExamine: List<Action> = emptyList(),
    val carriable: Boolean? = false,
    val driveable: Boolean? = false,
    val stateKey: String? = null,
    var usable: Boolean = true,
    var numberOfUses: Int? = null,
    var location: Int,
    val comment: String? = null,
    val usages: List<ItemUsage>? = emptyList(),
    val onUse: List<Action> = emptyList()
) : NamedEntity {

    override fun debugName(): String {
        return trimEmptySpaces(
            "'${name.name}' (id=$id${if (numberOfUses != null) ", uses left: $numberOfUses" else ""})"
        )
    }

    override fun toString(): String {
        return name.name
    }

    // Compatibility wrapper for older call sites.
    fun matchesName(lookupName: String): Boolean {
        return nameMatches(lookupName)
    }

    /** Returns a descriptive name including translated open/lock state. */
    override fun getDescriptiveName(definite: Boolean?): String {
        val lang = LanguageData.current
        val definiteArticle = definite == true
        var template = lang.getTemplate(Keys.Part.descriptiveName)
        val state = stateKey?.let { GameData.current.getStateByKey(it) }
        if (state != null) {
            template = template
                .replace(Keys.StandIn.state, state.currentValue)
                .replace(Keys.StandIn.name, name.name)
                .trim()
        } else {
            return super.getDescriptiveName(definiteArticle)
        }
        var article = getArticle(definiteArticle)
        if (!definiteArticle && lang.languageKey == Keys.languageKeyEn && !name.isPlural) {
            val nameWithoutArticle = template.replace(Keys.StandIn.article, "").trim()
            article = if (nameWithoutArticle.isNotEmpty() && nameWithoutArticle[0].lowercaseChar() in "aeiou") "an" else "a"
        }
        return trimEmptySpaces(template.replace(Keys.StandIn.article, article).trim())
    }

    fun getStateMessagePart(): String {
        val lang = LanguageData.current
        val state = stateKey?.let { GameData.current.getStateByKey(it) }
        if (state != null) {
            return lang.getTemplate(Keys.Part.state)
                .replace(Keys.StandIn.state, state.currentValue)
                .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
                .trim()
        }
        return ""
    }

    override fun getDetailedDescription(): String {
        val lang = LanguageData.current
        val stateMessagePart = getStateMessagePart()
        val template = if (description != null) {
            lang.getTemplate(Keys.Message.msgItemDetailedDescription)
        } else {
            lang.getTemplate(Keys.Message.msgItemDetailedDescriptionNoDescription)
        }
        return template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
            .replace(Keys.StandIn.description, description ?: "")
            .replace(Keys.StandIn.stateDescription, stateMessagePart)
            .replace(Keys.StandIn.state, stateMessagePart)
            .replace(Keys.StandIn.numberOfUses, numberOfUses?.toString() ?: "0")
            .replace(Keys.StandIn.numberOfUsesOrNo, if (numberOfUses == 0) getPronumGroup().none else numberOfUses?.toString() ?: "0")
            .trim()
    }
}
