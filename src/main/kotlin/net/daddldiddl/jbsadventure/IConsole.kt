package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.tools.ConsoleColor

/**
 * Console output interface for displaying player-visible text.
 *
 * Implemented by the engine's [net.daddldiddl.jbsadventure.tools.ConsoleOutput].
 * Defined in the model package so that model classes can depend on this interface
 * without importing engine-specific types.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface IConsole {
    /** Prints a message in the default color. */
    fun print(message: String?)

    /** Prints a message in the specified color. */
    fun print(message: String?, color: ConsoleColor)

    /** Prints an empty line. */
    fun print()

    /** Prints a warning-styled message. */
    fun warn(message: String?)
}

