package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.tools.*
import java.io.File
import com.sun.tools.attach.AgentLoadException

public lateinit var LOG: SimpleFileLog
public lateinit var CONSOLE: ConsoleOutput

/**
 * Application entry point for JB's Big Adventure.
 *
 * Loads game data via [GameLoader], initializes the [Game], and runs the interactive
 * command loop until the player issues a quit command.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
fun main(args: Array<String>) {
    if(args.contains("--help") || args.contains("-h") || args.contains("-?")) {
        val location = object {}.javaClass.protectionDomain
            .codeSource
            .location
            .toURI()

        // Convert to File and extract only the file name
        val fileName = File(location).name
        println("Usage: java -jar $fileName [options]")
        println("Options:")
        println("  --consoleDebug   Enable debug logging to console (and file if not disabled by --nolog)")
        println("  --nolog          Disable all file logging (console logging will still work if enabled)")
        println("  --help           how this help message")
        println("  --data <path>  Load game data from the specified JSON file instead of the bundled data.json")
        return
    }
    // Set to true to enable console logging, false to disable    
    val debugMode = args.contains("--consoleDebug")
    val writeToFile = !args.contains("--nolog")    
    val logLevel = if(debugMode) SimpleFileLog.LogLevel.DEBUG 
            else SimpleFileLog.LogLevel.INFO
    LOG = SimpleFileLog(consoleLogEnabled = debugMode, writeToFile = writeToFile, logLevel = logLevel)
    CONSOLE = ConsoleOutput()
    LOG.info("JB's Adventure Engine starting up (Debug mode: $debugMode, Write to file: $writeToFile, Log level: $logLevel)")
    val dataArgIndex = args.indexOf("--data")
    val dataFilePath = if (dataArgIndex >= 0) args.getOrNull(dataArgIndex + 1) else null
    val gameData = when {
        dataArgIndex >= 0 && dataFilePath == null -> {
            LOG.error("--data flag provided but no path argument followed it")
            throw IllegalArgumentException("--data requires a file path argument")
        }
        dataFilePath != null && !File(dataFilePath).exists() -> {
            LOG.error("External data file not found at '$dataFilePath'")
            throw IllegalArgumentException("Data file not found: $dataFilePath")
        }
        dataFilePath != null -> GameLoader.loadGameData(dataFilePath)
        else -> GameLoader.loadGameData()
    }
    LOG.info("Game data for ${gameData.title} loaded successfully, starting game loop")
    val game = Game(gameData)
    game.outputWelcome()
    val reader = System.`in`.bufferedReader()
    while (game.isRunning()) {
        game.currentStateDebug()
        print("> ")
        System.out.flush()
        val line = reader.readLine() ?: break
        game.processCommand(line)
    }
    LOG.info("Game finished, exiting")
}
