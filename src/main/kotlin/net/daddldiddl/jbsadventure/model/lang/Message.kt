package net.daddldiddl.jbsadventure.model.lang

/**
 * Represents a message that can be displayed to the player, with support for placeholders and dynamic content.
 *
 * Each message has a key that corresponds to an entry in the language data, and can include placeholders
 * that are replaced with actual values when the message is generated.
 */
class Messages(private val lang: LanguageData) {
    fun getTemplate(key: String): String {
        return lang.messages[key] ?: run {
            LOG.warn("Warning: No message found for key '$key', returning key as message.")
            return "Unknown message key: $key"
        }
    }

    fun getStateValue(key: String): String {
        return lang.stateValues[key] ?: run {
            LOG.warn("Warning: No state value found for key '$key', returning key as value.")
            return "Unknown state value key: $key"
        }
    }

    fun getMessagePart(key: String): String {
        return lang.messageParts[key] ?: run {
            LOG.warn("Warning: No message part found for key '$key', returning key as message part.")
            return "Unknown message part key: $key"
        }
    }

    fun getExitMessage(key: String, exit: Exit): String {
        val template = getTemplate(key)
        var message = template
        message = message.replace(LangKey.Placeholders.placeholderExit, exit.getDescriptiveName())
        message = message.replace(LangKey.Placeholders.placeholderDetailedExit, exit.getDetailedDescription())
        return message
    }

    fun getExitList(key: String, exits: List<Exit>): String {
        val template = getTemplate(key)
        var message = template
        val exitNames = exits.filter { it.exitState.visible }
                .joinToString(", ") { it.getDescriptiveName() }
        message = message.replace(LangKey.Placeholders.placeholderExits, exitNames)
        return message
    }
}