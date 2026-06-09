package net.daddldiddl.jbsadventure.tools

/**
 * Enum representing console colors for text output.
 * Each color is associated with an ANSI escape code.
 *
 * Copyright (c) 2026. This file is part of JB's Adventure Engine, licensed under the MIT License (MIT).
 * See LICENSE file in the project root for full license information.
 */
enum class ConsoleColor(private val code: String) {
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

