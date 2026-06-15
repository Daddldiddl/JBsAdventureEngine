package net.daddldiddl.jbsadventure.editor.i18n

import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

object Messages {
    fun text(locale: Locale, key: String): String {
        return try {
            val bundle = ResourceBundle.getBundle("i18n.messages", locale)
            bundle.getString(key)
        } catch (_: MissingResourceException) {
            key
        }
    }

    fun format(locale: Locale, key: String, vararg args: Any): String {
        val pattern = text(locale, key)
        return MessageFormat(pattern, locale).format(args)
    }
}
