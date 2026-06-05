package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable
import net.daddldiddl.jbsadventure.DATA
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.lang.Keys
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
        val definiteArticle = definite == true
        var template = LANG.getTemplate(Keys.Part.descriptiveName)
        val state = stateKey?.let { DATA.getStateByKey(it) }
        if(state != null) {
            template = template
                    .replace(Keys.StandIn.state, state.currentValue)
                    .replace(Keys.StandIn.name, name.name)
                    .trim()
        } else {
            return super.getDescriptiveName(definiteArticle)
        }
        var article = getArticle(definiteArticle)
        if(!definiteArticle && LANG.languageKey == Keys.languageKeyEn && !name.isPlural) {
            val nameWithoutArticle = template.replace(Keys.StandIn.article, "").trim()
            // English has the special rule of using "an" instead of "a" before vowel sounds, so we handle this as a special case.
            // Note that this is a very simplified rule and does not cover all cases (e.g., "a university" vs. "an hour"), but it should work for most common cases in a text adventure game.
            article = if (nameWithoutArticle.subSequence(0,0).matches(Regex("[aeiouAEIOU]"))) "an" else "a"
        }
        return trimEmptySpaces(template.replace(Keys.StandIn.article, article).trim())
    }

    fun getStateMessagePart(): String {
        val state = stateKey?.let { DATA.getStateByKey(it) }
        if (state != null) {
            return LANG.getTemplate(Keys.Part.state)
                .replace(Keys.StandIn.state, state.currentValue)
                .replace(Keys.StandIn.pronounSubject, getPronounSubject() ?: "")
                .trim()
        }
        return ""
    }

    override fun getDetailedDescription(): String {
        val stateMessagePart = getStateMessagePart()
        val template = if (description != null) {
            LANG.getTemplate(Keys.Message.msgItemDetailedDescription)
        } else {
            LANG.getTemplate(Keys.Message.msgItemDetailedDescriptionNoDescription)
        }
        return template
            .replace(Keys.StandIn.definiteName, getDescriptiveName(definite = true))
            .replace(Keys.StandIn.description, description ?: "")
            .replace(Keys.StandIn.stateDescription, stateMessagePart)
            .replace(Keys.StandIn.state, stateMessagePart)
            .trim()
    }
}
