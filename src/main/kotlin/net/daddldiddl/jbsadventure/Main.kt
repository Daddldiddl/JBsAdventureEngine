package net.daddldiddl.jbsadventure

import net.daddldiddl.jbsadventure.tools.*
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset


/**
 * Application entry point for JB's Adventure Engine.
 *
 * Loads game data via [GameLoader], initializes the [Game], and runs the interactive
 * command loop until the player issues a quit command.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
fun main(args: Array<String>) {
    // Detect the terminal's native charset so input and output are encoded consistently.
    // System.console()?.charset() is the most reliable source (Java 17+); it reads the
    // OS/terminal setting and will return UTF-8 in WSL/Linux/macOS and the active code
    // page on Windows (e.g. Cp1252 or Cp437).  Fall back to UTF-8 when there is no
    // attached console (piped / redirected I/O).
    val terminalCharset: Charset = System.console()?.charset() ?: Charsets.UTF_8
    // Re-wrap System.out so that Kotlin's println() and ConsoleOutput both encode output
    // with the terminal's charset instead of the JVM default.
    System.setOut(PrintStream(System.out, true, terminalCharset))

    // need to show help?
    if(args.contains("--help") || args.contains("-h") || args.contains("-?")) {
        printCommandLineHelp()
        return
    }

    // load persisted config first; command-line arguments then override selected values.
    val effectiveConfig = Config.current.copy()
    if (args.contains("--consoleLog")) {
        effectiveConfig.writeLogToConsole = true
    }
    if (args.contains("--fileLog")) {
        effectiveConfig.writeFileLog = true
    }
    if (args.contains("--debug")) {
        effectiveConfig.logLevel = LogLevel.DEBUG
    }
    if (args.contains("--info")) {
        effectiveConfig.logLevel = LogLevel.INFO
    }
    if (args.contains("--warn")) {
        effectiveConfig.logLevel = LogLevel.WARN
    }
    if (args.contains("--noLog")) {
        effectiveConfig.writeFileLog = false
        effectiveConfig.writeLogToConsole = false
        effectiveConfig.logLevel = LogLevel.INFO
    }
    if (args.contains("--lang")) {
        effectiveConfig.languageCode = args.getOrNull(args.indexOf("--lang") + 1)?.takeIf { it.isNotBlank() }?.lowercase() ?: effectiveConfig.languageCode
    }
    val repairInput = args.contains("--repairInput")

    val dataFilePath = if (args.contains("--data")) args.getOrNull(args.indexOf("--data") + 1) else null

    // initialize global context
    val log = SimpleFileLog(
        consoleLogEnabled = effectiveConfig.writeLogToConsole,
        writeToFile = effectiveConfig.writeFileLog,
        logLevel = effectiveConfig.logLevel
    )
    GlobalContext.initLog(log)  // make LOG available before loadLanguageData uses it

    val console = ConsoleOutput()
    val lang = GameLoader.loadLanguageData(effectiveConfig.languageCode)

    GlobalContext.initialize(log, console, lang)

    Config.current = effectiveConfig
    Config.save()

    LOG.info("JB's Adventure Engine starting up (Console debug: ${effectiveConfig.writeLogToConsole}, Write to file: ${effectiveConfig.writeFileLog}, Log level: ${effectiveConfig.logLevel}, Language: ${effectiveConfig.languageCode})")
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
    GlobalContext.setGameData(gameData)
    LOG.info("Game data for ${gameData.title} loaded successfully, starting game loop")

    // initialize and run the game
    GlobalContext.repairInputDebug = repairInput
    val game = Game(gameData)
    game.printWelcome()
    val consoleReader = System.console()
    val fallbackReader = if (consoleReader == null) System.`in`.bufferedReader(terminalCharset) else null

    // main game loop
    while (game.isRunning()) {
        game.currentStateDebug()
        print("${ConsoleColor.LIGHTCYAN}> ${ConsoleColor.RESET}")
        System.out.flush()
        val line = if (consoleReader != null) consoleReader.readLine() else fallbackReader?.readLine() ?: break
        game.processCommand(line)
    }

    // game has ended
    LOG.info("Game finished, exiting")
    game.printGoodbye()
}

/**
 * Prints usage instructions for running the application from the command line.
 */
fun printCommandLineHelp() {
    val location = object {}.javaClass.protectionDomain
        .codeSource
        .location
        .toURI()
    val fileName = File(location).name
    val title = "JB's Adventure Engine - Command Line Usage"
 
    println("${ConsoleColor.WHITE}")
    println("${"=".repeat(title.length + 4)}")
    println("| $title |")
    println("${"=".repeat(title.length + 4)}")
    println()
    println("${ConsoleColor.WHITE}Usage: ${ConsoleColor.LIGHTCYAN}java -jar $fileName ${ConsoleColor.LIGHTYELLOW}[options]")
    println()
    println("${ConsoleColor.WHITE}Options:")
    println("  ${ConsoleColor.LIGHTYELLOW}--consoleLog     ${ConsoleColor.WHITE}Enable logging to console")
    println("  ${ConsoleColor.LIGHTYELLOW}--fileLog        ${ConsoleColor.WHITE}Enable logging to a file")
    println("  ${ConsoleColor.LIGHTYELLOW}--noLog          ${ConsoleColor.WHITE}Disable logging entirely (overrides other log options)")
    println("  ${ConsoleColor.LIGHTYELLOW}--debug          ${ConsoleColor.WHITE}Set logging to DEBUG level (most verbose)")
    println("  ${ConsoleColor.LIGHTYELLOW}--info           ${ConsoleColor.WHITE}Set logging to INFO level (default)")
    println("  ${ConsoleColor.LIGHTYELLOW}--warn           ${ConsoleColor.WHITE}Set logging to WARN level (issues only)")
    println("  ${ConsoleColor.LIGHTYELLOW}--data ${ConsoleColor.LIGHTCYAN}<path>    ${ConsoleColor.WHITE}Load game data from the specified JSON file instead of the bundled data.json")
    println("  ${ConsoleColor.LIGHTYELLOW}--lang ${ConsoleColor.LIGHTCYAN}<code>    ${ConsoleColor.WHITE}Load language and game data for the specific country code.")
    println("                   Supported codes: ${ConsoleColor.LIGHTCYAN}en${ConsoleColor.WHITE}, ${ConsoleColor.LIGHTCYAN}de${ConsoleColor.WHITE} (default: ${ConsoleColor.LIGHTCYAN}en${ConsoleColor.WHITE})")
    println("  ${ConsoleColor.LIGHTYELLOW}--repairInput    ${ConsoleColor.WHITE}Debug only: attempt mojibake repair for piped/non-interactive input")
    println("  ${ConsoleColor.LIGHTYELLOW}--help -h -?     ${ConsoleColor.WHITE}Show this help message")
    println("${ConsoleColor.RESET}")
}
