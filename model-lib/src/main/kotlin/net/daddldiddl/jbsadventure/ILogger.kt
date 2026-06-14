package net.daddldiddl.jbsadventure

/**
 * Logger interface for structured logging.
 *
 * Implemented by the engine's [net.daddldiddl.jbsadventure.tools.SimpleFileLog].
 * Defined here so that model classes can use [ILogger.current] for logging without
 * importing engine-specific types.
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

    companion object {
        /**
         * The active logger instance. Set at startup via [GlobalContext.initLog].
         * Defaults to [NoOpLogger] so model classes never throw on logging calls
         * even if the engine layer has not yet initialized.
         */
        @Volatile
        var current: ILogger = NoOpLogger
    }

    /** Silent fallback logger used before [GlobalContext] has been initialized. */
    object NoOpLogger : ILogger {
        override fun debug(msg: String) {}
        override fun info(msg: String) {}
        override fun warn(msg: String) {}
        override fun error(msg: String) {}
        override fun console(msg: String) {}
    }
}
