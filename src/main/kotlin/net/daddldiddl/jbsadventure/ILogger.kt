package net.daddldiddl.jbsadventure

/**
 * Logger interface for structured logging.
 *
 * Implemented by the engine's [net.daddldiddl.jbsadventure.tools.SimpleFileLog].
 * Defined in the model package so that model classes can depend on this interface
 * without importing engine-specific types.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
interface ILogger {
    /** Logs a debug-level message. */
    fun debug(msg: String)

    /** Logs an info-level message. */
    fun info(msg: String)

    /** Logs a warning-level message. */
    fun warn(msg: String)

    /** Logs an error-level message. */
    fun error(msg: String)

    /** Logs a player-visible console text message (for file-mirroring). */
    fun console(msg: String)
}

