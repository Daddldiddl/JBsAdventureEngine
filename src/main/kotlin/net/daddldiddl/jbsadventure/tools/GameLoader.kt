package net.daddldiddl.jbsadventure.tools

import kotlinx.serialization.json.Json
import net.daddldiddl.jbsadventure.LOG
import net.daddldiddl.jbsadventure.lang.LanguageData
import net.daddldiddl.jbsadventure.model.GameData
import net.daddldiddl.jbsadventure.LANG
import net.daddldiddl.jbsadventure.GlobalContext
import java.io.File

/**
 * Loads and deserializes game data from the bundled classpath resource `data.json`.
 *
 * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
 */
object GameLoader {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadGameData(path: String): GameData {
        LOG.debug("Attempting to load game data from $path")
        val localFile = File(path)
        if (localFile.exists()) {
            localFile.bufferedReader(Charsets.UTF_8).use { reader ->
                val text = reader.readText()
                LOG.info("Loaded game data from $path")
                val gameData = json.decodeFromString<GameData>(text)
                LOG.info(
                        "Deserialized game data for ${gameData.title}: ${gameData.getRoomList().size} rooms, ${gameData.getItemList().size} items, ${gameData.getStateList().size} states"
                )
                if (!DataValidator.validate(gameData)) {
                    LOG.warn(
                            "Game data validation failed, but loading will continue. Please check the warnings above for potential issues in your data.json."
                    )
                }
                return gameData
            }
        } else {
            LOG.error("Game data file not found at $path")
            throw IllegalArgumentException("Game data file not found at $path")
        }
    }

    /**
     * Reads `data.json` from the classpath, deserializes it, and returns the resulting [GameData].
     *
     * @return A fully populated [GameData] instance.
     * @throws IllegalStateException if `data.json` cannot be found on the classpath.
     */
    fun loadGameData(): GameData {
        LOG.debug("Attempting to load game data from data.json on classpath")
        val text =
                GameLoader::class
                        .java
                        .getResourceAsStream("/lang/${GlobalContext.lang.languageKey}/data.json")
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.readText()
                        ?: error("data.json not found in classpath")
        LOG.info("Loaded game data from data.json")
        val gameData = json.decodeFromString<GameData>(text)
        LOG.info(
                "Deserialized game data: ${gameData.getRoomList().size} rooms, ${gameData.getItemList().size} items, ${gameData.getStateList().size} states"
        )
        if (!DataValidator.validate(gameData)) {
            LOG.warn(
                    "Game data validation failed, but loading will continue. Please check the warnings above for potential issues in your data.json."
            )
        }
        return gameData
    }

    /**
     * Reads `lang/<langCode>.json` from the classpath, deserializes it, and returns the resulting [LanguageData].
     *
     * @param langCode The language code for the desired language data.
     * @return A fully populated [LanguageData] instance.
     * @throws IllegalStateException if the language data file cannot be found on the classpath.
     */
    fun loadLanguageData(langCode: String): LanguageData {
        LOG.debug("Attempting to load language data for language code '$langCode'")
        val localCandidates: List<File> = listOf(
            File("./lang.json"),
            File("./$langCode.json"),
            File("./$langCode/lang.json")
        )
        var localFile: File? = null
        for (candidate in localCandidates) {
            if (candidate.exists()) {
                localFile = candidate
                break
            }
        }

        if (localFile != null) {
            val text = localFile.readText(Charsets.UTF_8)
            LOG.info("Loaded language data for language code '$langCode' from file system (${localFile.path})")
            return json.decodeFromString<LanguageData>(text)
        }

        val resourcePath = "/lang/$langCode/lang.json"
        val resourceStream = GameLoader::class.java.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Language data file not found for language code '$langCode' at path '$resourcePath'")
        val text = resourceStream.bufferedReader(Charsets.UTF_8).readText()
        LOG.info("Loaded language data for language code '$langCode'")
        return json.decodeFromString<LanguageData>(text)
    }
}
