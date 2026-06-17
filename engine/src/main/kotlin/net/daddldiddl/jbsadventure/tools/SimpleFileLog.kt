package net.daddldiddl.jbsadventure.tools

import net.daddldiddl.jbsadventure.ILogger
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LogLevel(val priority: Int) {
    ERROR(5), WARN(4), INFO(3), CONSOLE(2), DEBUG(1)
}

/**
 * Simple logging utility class to log messages. The console output mode can be enabled or disabled
 * via the constructor parameter. When enabled, calls to one of the log methods will print the provided message
 * prefixed with the appropriate log level tag. When disabled, they will only be written to the log file.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
class SimpleFileLog(consoleLogEnabled: Boolean, writeToFile: Boolean, logLevel: LogLevel) : ILogger {
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

    private fun removeColorCodes(message: String): String {
        return message.replace(Regex("\\u001B\\[[;\\d]*m"), "")
    }

    private fun log(message: String, level: LogLevel) {
        if (level.priority < logLevel.priority) {
            return
        }

        val timestamp = LocalDateTime.now().format(dateFormatter)
        val logMsg = removeColorCodes(when (level) {
            LogLevel.DEBUG -> "$timestamp $DEBUG_TAG $message"
            LogLevel.ERROR -> "$timestamp $ERROR_TAG $message"
            LogLevel.WARN -> "$timestamp $WARN_TAG $message"
            LogLevel.INFO -> "$timestamp $INFO_TAG $message"
            LogLevel.CONSOLE -> "$timestamp $CONSOLE_TAG $message"
        })

        // Print to console with inline ANSI color coding (no CONSOLE dependency)
        if (consoleLogEnabled && level != LogLevel.CONSOLE) {
            val colorCode = when (level) {
                LogLevel.DEBUG -> ConsoleColor.LIGHTGRAY   // light gray
                LogLevel.ERROR -> ConsoleColor.RED   // red
                LogLevel.WARN  -> ConsoleColor.LIGHTYELLOW   // yellow
                LogLevel.INFO  -> ConsoleColor.LIGHTGREEN   // green
                // CONSOLE not required as its already excluded above!
            }
            println("$colorCode$logMsg${ConsoleColor.RESET}")
        }

        if (writeToFile) {
            try {
                FileWriter(logFile, true).use { writer ->
                    writer.write("$logMsg\n")
                }
            } catch (e: IOException) {
                println("[ERROR] Failed to write to log file: ${e.message}")
            }
        }
    }

    fun isConsoleLogEnabled(): Boolean = consoleLogEnabled
    fun getLogLevel(): LogLevel = logLevel

    override fun debug(msg: String) { log(msg, LogLevel.DEBUG) }
    override fun error(msg: String) { log(msg, LogLevel.ERROR) }
    override fun warn(msg: String)  { log(msg, LogLevel.WARN) }
    override fun info(msg: String)  { log(msg, LogLevel.INFO) }
    override fun console(msg: String) { log(msg, LogLevel.CONSOLE) }
}
