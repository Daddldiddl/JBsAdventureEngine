package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.lang.LanguageData
import net.daddldiddl.jbsadventure.model.GameData

/**
 * Global context holder for engine-layer dependencies (logger and console).
 * Language and game data are stored in their own companion objects:
 * [LanguageData.current] and [GameData.current].
 *
 * Initialize once at application startup via [initialize].
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
object GlobalContext {
    private var _log: ILogger? = null
    private var _console: IConsole? = null

    /**
     * Structured logger for file and console output.
     * Throws IllegalStateException if accessed before initialization.
     */
    val log: ILogger
        get() = _log ?: throw IllegalStateException("GlobalContext.log not initialized. Call GlobalContext.initialize() first.")

    /**
     * Console output handler for player-visible text.
     * Throws IllegalStateException if accessed before initialization.
     */
    val console: IConsole
        get() = _console ?: throw IllegalStateException("GlobalContext.console not initialized. Call GlobalContext.initialize() first.")

    /**
     * Delegates to [LanguageData.current].
     */
    val lang: LanguageData
        get() = LanguageData.current

    /**
     * Delegates to [GameData.current].
     */
    val data: GameData
        get() = GameData.current

    /**
     * Initialize only the logger – call this before any code that uses [LOG]
     * (e.g. before [net.daddldiddl.jbsadventure.tools.GameLoader.loadLanguageData]).
     */
    fun initLog(log: ILogger) {
        _log = log
    }

    /**
     * Initialize the global context with required dependencies.
     * Must be called once at application startup before any model code runs.
     *
     * Also sets [LanguageData.current] so model classes can access it directly.
     */
    fun initialize(log: ILogger, console: IConsole, lang: LanguageData) {
        _log = log
        _console = console
        LanguageData.current = lang
    }

    /**
     * Set the game data. Called after loading game data from JSON.
     * Also sets [GameData.current] so model classes can access it directly.
     */
    fun setGameData(data: GameData) {
        GameData.current = data
    }

    /**
     * Switch to a different language at runtime.
     * Updates [LanguageData.current]; all subsequent formatting calls use the new language.
     */
    fun switchLanguage(lang: LanguageData) {
        LanguageData.current = lang
    }

    /**
     * Check if the context has been initialized.
     */
    fun isInitialized(): Boolean {
        return _log != null && _console != null
    }

    /**
     * Reset the context (primarily for testing).
     */
    fun reset() {
        _log = null
        _console = null
    }
}

// Convenience accessors – delegates to companion objects
val LOG: ILogger get() = GlobalContext.log
val CONSOLE: IConsole get() = GlobalContext.console
val LANG: LanguageData get() = LanguageData.current
val DATA: GameData get() = GameData.current
