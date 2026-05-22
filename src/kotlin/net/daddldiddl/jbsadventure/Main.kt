package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.tools.*
import net.daddldiddl.jbsadventure.model.LanguageData
import net.daddldiddl.jbsadventure.tools.ConsoleColor
import java.io.File

public lateinit var LOG: SimpleFileLog
public lateinit var CONSOLE: ConsoleOutput
public lateinit var LANGUAGE_DATA: LanguageData

/**
 * Application entry point for JB's Adventure Engine.
 *
 * Loads game data via [GameLoader], initializes the [Game], and runs the interactive
 * command loop until the player issues a quit command.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
fun main(args: Array<String>) {
    // need to show hel?
    if(args.contains("--help") || args.contains("-h") || args.contains("-?")) {
        printCommandLineHelp()
        return
    }

    // read other command line parameters   
    val debugMode = args.contains("--consoleDebug")
    val writeToFile = args.contains("--log")    
    val logLevel = if(debugMode) LogLevel.DEBUG 
            else LogLevel.INFO
    val langCode = if (args.contains("--lang")) args.getOrNull(args.indexOf("--lang") + 1) ?: "en" else "en"
    val dataFilePath = if (args.contains("--data")) args.getOrNull(args.indexOf("--data") + 1) else null

    // initialize global variables
    LOG = SimpleFileLog(consoleLogEnabled = debugMode, writeToFile = writeToFile, logLevel = logLevel)
    CONSOLE = ConsoleOutput()
    LANGUAGE_DATA = GameLoader.loadLanguageData(langCode)

    LOG.info("JB's Adventure Engine starting up (Debug mode: $debugMode, Write to file: $writeToFile, Log level: $logLevel, Language: $langCode)")
    LOG.debug("Command line arguments: ${args.joinToString(" ")}")

    // load game data
    val gameData = when {
        dataFilePath != null && !File(dataFilePath).exists() -> {
            LOG.error("External data file not found at '$dataFilePath'")
            return
        }
        dataFilePath != null -> GameLoader.loadGameData(dataFilePath)
        else -> GameLoader.loadGameData()
    }
    LOG.info("Game data for ${gameData.title} loaded successfully, starting game loop")

    // initialize and run the game
    val game = Game(gameData)
    game.outputWelcome()
    val reader = System.`in`.bufferedReader()

    // main game loop
    while (game.isRunning()) {
        game.currentStateDebug()
        print("> ")
        System.out.flush()
        val line = reader.readLine() ?: break
        game.processCommand(line)
    }

    // game has ended
    LOG.info("Game finished, exiting")
    CONSOLE.print("Thanks for playing! Goodbye.")
}

/**
 * Prints usage instructions for running the application from the command line.
 */
fun printCommandLineHelp() {
    // Get the name of the currently running JAR file for display in the usage instructions
    val location = object {}.javaClass.protectionDomain
        .codeSource
        .location
        .toURI()
    val fileName = File(location).name
    val title = "JB's Adventure Engine - Command Line Usage"
 
    // Print usage instructions
    println("${ConsoleColor.WHITE}")
    println("${"=".repeat(title.length + 4)}")
    println("| $title |")
    println("${"=".repeat(title.length + 4)}")
    println()
    println("${ConsoleColor.WHITE}Usage: ${ConsoleColor.LIGHTCYAN}java -jar $fileName ${ConsoleColor.LIGHTYELLOW}[options]")
    println()
    println("${ConsoleColor.WHITE}Options:")
    println("  ${ConsoleColor.LIGHTYELLOW}--consoleDebug   ${ConsoleColor.WHITE}Enable DEBUG level logging to console")
    println("  ${ConsoleColor.LIGHTYELLOW}--log            ${ConsoleColor.WHITE}Enable INFO level file logging")
    println("  ${ConsoleColor.LIGHTYELLOW}--logDebug       ${ConsoleColor.WHITE}Enable DEBUG level file logging")
    println("  ${ConsoleColor.LIGHTYELLOW}--data ${ConsoleColor.LIGHTCYAN}<path>    ${ConsoleColor.WHITE}Load game data from the specified JSON file instead of the bundled data.json")
    println("  ${ConsoleColor.LIGHTYELLOW}--lang ${ConsoleColor.LIGHTCYAN}<code>    ${ConsoleColor.WHITE}Load language data for the specific country code.")
    println("                   Supported codes: ${ConsoleColor.LIGHTCYAN}en${ConsoleColor.WHITE}, ${ConsoleColor.LIGHTCYAN}de${ConsoleColor.WHITE} (default: ${ConsoleColor.LIGHTCYAN}en${ConsoleColor.WHITE})")
    println("  ${ConsoleColor.LIGHTYELLOW}--help -h -?     ${ConsoleColor.WHITE}Show this help message")
    println("${ConsoleColor.RESET}")
}
