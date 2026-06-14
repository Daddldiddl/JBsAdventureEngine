package net.daddldiddl.jbsadventure.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.daddldiddl.jbsadventure.LOG
import java.io.File

/**
 * Persisted engine configuration loaded from and saved to `config.json`.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
@Serializable
data class Config(
    var writeFileLog: Boolean = false,
    var writeLogToConsole: Boolean = false,
    var logLevel: LogLevel = LogLevel.INFO,
    var ignoreActionDelays: Boolean = false,
    var languageCode: String = "en"
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val CONFIG_PATH = "./config.json"
        private var _current: Config? = null

        /** Writes a debug log line if logging is already initialized. */
        private fun safeDebug(message: String) {
            runCatching { LOG.debug(message) }
        }

        /** Writes an info log line if logging is already initialized. */
        private fun safeInfo(message: String) {
            runCatching { LOG.info(message) }
        }

        // Backing field avoids recursive self-access in getter/setter.
        var current: Config
            get() {
                if (_current == null) {
                    _current = load() ?: Config()
                }
                return _current!!
            }
            set(value) {
                _current = value
            }

        /** Saves the current in-memory config to `config.json`. */
        fun save() {
            safeDebug("Attempting to save config data to $CONFIG_PATH")
            val jsonString = json.encodeToString(current)
            File(CONFIG_PATH).bufferedWriter().use { writer ->
                writer.write(jsonString)
            }
            safeInfo("Saved config data to $CONFIG_PATH")
        }

        /** Loads config from `config.json`, or returns null if the file does not exist. */
        fun load(): Config? {
            safeDebug("Attempting to load config data from $CONFIG_PATH")
            val localFile = File(CONFIG_PATH)
            if (!localFile.exists()) {
                safeInfo("No config file found at $CONFIG_PATH, using defaults")
                return null
            }

            return localFile.bufferedReader(Charsets.UTF_8).use { reader ->
                val text = reader.readText()
                safeInfo("Loaded config data from $CONFIG_PATH")
                val newConfig: Config = json.decodeFromString(text)
                safeInfo(
                    "Deserialized config data: writeFileLog=${newConfig.writeFileLog}, writeLogToConsole=${newConfig.writeLogToConsole}, logLevel=${newConfig.logLevel}, ignoreActionDelays=${newConfig.ignoreActionDelays}, languageCode=${newConfig.languageCode}"
                )
                newConfig
            }
        }
    }
}