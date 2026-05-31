package net.daddldiddl.jbsadventure.tools

import net.daddldiddl.jbsadventure.LOG

/**
 * Enum representing console colors for text output.
 * Each color is associated with an ANSI escape code.
 * 
 * Copyright (c) 2026. This file is part of JB's Adventure Engine, licensed under the MIT License (MIT).
 * See LICENSE file in the project root for full license information.
 */
public enum class ConsoleColor(private val code: String) {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    LIGHTGRAY("\u001B[37m"),
    DARKGRAY("\u001B[90m"),
    LIGHTRED("\u001B[91m"),
    LIGHTGREEN("\u001B[92m"),
    LIGHTYELLOW("\u001B[93m"),
    LIGHTBLUE("\u001B[94m"),
    LIGHTMAGENTA("\u001B[95m"),
    LIGHTCYAN("\u001B[96m"),
    WHITE("\u001B[97m");

    override fun toString(): String {
        return code
    }
}

/**
 * Utility class for console output. Provides a simple interface to print messages 
 * to the console while also logging them via the global [LOG] instance.
 * 
 * Copyright (c) 2026. This file is part of JB's Adventure Engine, licensed under the MIT License (MIT).
 * See LICENSE file in the project root for full license information.
 */
class ConsoleOutput {
    /** Prints a message in default color and mirrors it to the log. */
    public fun print(message: String?) {
        print(message, ConsoleColor.WHITE)
    }

    /** Prints a message in the specified color and mirrors it to the log. */
    public fun print(message: String?, color: ConsoleColor) {
        println("$color$message${ConsoleColor.RESET}")
        LOG.console(message ?: "")
    }

    /** Prints an empty line to console and log. */
    public fun print() {
        println()
        LOG.console("")
    }

    /** Prints a warning-styled message. */
    public fun warn(message: String?) {
        print(message, ConsoleColor.LIGHTRED)
    }

    /** Prints a log line using a level-dependent color without writing file logs. */
    public fun printLog(message: String, level: LogLevel) {
            val colorCode = when (level) {
                LogLevel.DEBUG -> "${ConsoleColor.BLUE}" // Blue
                LogLevel.ERROR -> "${ConsoleColor.RED}" // Red
                LogLevel.WARN -> "${ConsoleColor.YELLOW}"  // Yellow
                LogLevel.INFO -> "${ConsoleColor.GREEN}"  // Green
                else -> "${ConsoleColor.RESET}" // Reset
            }
            println("$colorCode$message${ConsoleColor.RESET}") // Reset color after printing
    }
}
