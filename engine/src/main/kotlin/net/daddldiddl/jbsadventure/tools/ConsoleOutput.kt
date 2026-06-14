package net.daddldiddl.jbsadventure.tools

import net.daddldiddl.jbsadventure.IConsole
import net.daddldiddl.jbsadventure.ILogger

/**
 * Utility class for console output. Provides a simple interface to print messages
 * to the console while also mirroring them to the active [ILogger] instance.
 *
 * Copyright (c) 2026. This file is part of JB's Adventure Engine, licensed under the MIT License (MIT).
 * See LICENSE file in the project root for full license information.
 */
class ConsoleOutput : IConsole {
    /** Prints a message in default color and mirrors it to the log. */
    override fun print(message: String?) {
        print(message, ConsoleColor.WHITE)
    }

    /** Prints a message in the specified color and mirrors it to the log. */
    override fun print(message: String?, color: ConsoleColor) {
        println("$color${wrapText(message, 80)}${ConsoleColor.RESET}")
        ILogger.current.console(message ?: "")
    }

    /** Prints an empty line to console and log. */
    override fun print() {
        println()
        ILogger.current.console("")
    }

    /** Prints a warning-styled message. */
    override fun warn(message: String?) {
        print(message, ConsoleColor.LIGHTRED)
    }

    /** Prints a log line using a level-dependent color without writing file logs. */
    fun printLog(message: String, level: LogLevel) {
        val colorCode = when (level) {
            LogLevel.DEBUG -> "${ConsoleColor.LIGHTGRAY}"
            LogLevel.ERROR -> "${ConsoleColor.RED}"
            LogLevel.WARN -> "${ConsoleColor.YELLOW}"
            LogLevel.INFO -> "${ConsoleColor.GREEN}"
            else -> "${ConsoleColor.RESET}"
        }
        println("$colorCode${wrapText(message, 120)}${ConsoleColor.RESET}")
    }

    private fun wrapText(text: String?, width: Int): String {
        if (text == null || text.isEmpty()) return ""
        val words = text.split(Regex("\\s+"))
        val builder = StringBuilder()
        var lineLength = 0
        var firstWord = true
        for (word in words) {
            if ((lineLength + word.length + if (lineLength > 0) 1 else 0) > width && !firstWord) {
                builder.append(System.lineSeparator())
                lineLength = 0
                firstWord = false
            }
            if (lineLength > 0) {
                builder.append(" ")
                lineLength++
            }
            builder.append(word)
            lineLength += word.length
        }
        return builder.toString()
    }
}
