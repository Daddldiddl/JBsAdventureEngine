package net.daddldiddl.jbsadventure.tools

import net.daddldiddl.jbsadventure.CONSOLE
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public enum class LogLevel(val priority: Int) {
    ERROR(5), WARN(4), INFO(3), CONSOLE(2), DEBUG(1)
}

/**
 * Simple logging utility class to log messages. The console output mode can be enabled or disabled
 * via the constructor parameter. When enabled, calls to one of the log methods will print the provided message 
 * prefixed with the appropriate log level tag. When disabled, they will only be written to the log file.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class SimpleFileLog(consoleLogEnabled: Boolean, writeToFile :Boolean,logLevel: LogLevel) {
    private val consoleLogEnabled: Boolean = consoleLogEnabled
    private val writeToFile: Boolean = writeToFile
    private val logLevel: LogLevel = logLevel
    private val logFile = File("JBsBigAdventure_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}.log")
    private val DEBUG_TAG = "[DEBUG]"
    private val ERROR_TAG = "[ERROR]"
    private val WARN_TAG = "[WARN] "
    private val INFO_TAG = "[INFO] "
    private val CONSOLE_TAG = "[TEXT] "
    private val dateFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss-SSS")

    /**
     * Removes ANSI color codes from a string.
     *
     * @param message The message from which to remove color codes.
     * @return The message without color codes.
     */
    private fun removeColorCodes(message: String): String {
        return message.replace(Regex("\\u001B\\[[;\\d]*m"), "")
    }

    /**
     * Logs a message with the specified log level. The message is prefixed with a timestamp and the log level tag.
     * If console logging is enabled, the message is also printed to the console with color coding based on the log level.
     *
     * @param message The message to log.
     * @param level The log level of the message.
     */
    private fun log(message: String, level: LogLevel) {
        // Only process if loglevel is appropriate
        if(level.priority < logLevel.priority) {
            return
        }

        // Format the log message with a timestamp and log level tag, and remove color codes
        val timestamp = LocalDateTime.now().format(dateFormatter)
        val logMsg = removeColorCodes( when (level) {
            LogLevel.DEBUG -> "$timestamp $DEBUG_TAG $message"
            LogLevel.ERROR -> "$timestamp $ERROR_TAG $message"
            LogLevel.WARN -> "$timestamp $WARN_TAG $message"
            LogLevel.INFO -> "$timestamp $INFO_TAG $message"
            LogLevel.CONSOLE -> "$timestamp $CONSOLE_TAG $message"
        })

        // Print to console with color coding if console logging is enabled
        if (consoleLogEnabled && level != LogLevel.CONSOLE) {
            CONSOLE.printLog(logMsg, level)
        } 

        // Append the log message to the log file if the log level is appropriate
        if(writeToFile) {
            // Append the log message to the log file
            try {
                FileWriter(logFile, true).use { writer ->
                    writer.write("$logMsg\n")
                }
            } catch (e: IOException) {
                println("[ERROR] Failed to write to log file: ${e.message}")
            }
        }
    }

    /**
     * Checks if the console log mode is enabled.
     * @return true if console log mode is enabled, false otherwise.
     */
    fun isConsoleLogEnabled(): Boolean = consoleLogEnabled

    /**
     * Returns the configured log level.
     * @return The configured log level.
     */
    fun getLogLevel(): LogLevel = logLevel

    /**
     * Logs a debug message.
     * @param message The message to log.
     */
    fun debug(message: String) {
        log(message, LogLevel.DEBUG)
    }

    /**
     * Logs an error message.
     * @param message The message to log.
     */
    fun error(message: String) {
        log(message, LogLevel.ERROR)
    }

    /**
     * Logs a warning message.
     * @param message The message to log.
     */
    fun warn(message: String) {
        log(message, LogLevel.WARN)
    }

    /**
     * Logs an informational message.
     * @param message The message to log.
     */
    fun info(message: String) {
        log(message, LogLevel.INFO)
    }

    /**
     * Logs a message intended for console output.
     * @param message The message to log.
     */
    fun console(message: String) {
        log(message, LogLevel.CONSOLE)
    }
}
