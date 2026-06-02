package net.daddldiddl.jbsadventure.lang

/**
 * Defines the keys for accessing specific language data entries from the maps in [LanguageData],
 * such as directions, commands, messages, and placeholders.
 */
object Keys {

    const val languageKey = "languageKey"
    const val languageKeyEn = "en"

    /**
     * Key constants for pronoun-group configuration and defaults.
     *
     * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
     */
    object Pronouns {
        /** The key for the defaultPronounGroupKey - used to decide the default pronoun group from the language data */
        const val defaultPronounGroupKey = "defaultPronounGroupKey"
        /** if no defaultPronounGroupKey is provided in the language file, this one is used */
        const val defaultDefaultPronounGroupKey = "neutral"
        /** default genderKeys for pronoun groups - you can use whatever you want,
         * these are just examples and NOT actually used in the code.
         * The only relevant relation is between the language file and
         * the data.json, so make sure the genderKeys used in both match! */
        const val keyNeutral = "neutral"
        const val keyMale = "male"
        const val keyFemale = "female"
    }
    /**
     * Defines the standard directions used in the game and their corresponding keys for language data.
     * Each direction can have multiple aliases defined in the language data.
     */
    object Directions {
        const val north = "north"
        const val south = "south"
        const val east = "east"
        const val west = "west"
        const val up = "up"
        const val down = "down"
        const val northeast = "northeast"
        const val northwest = "northwest"
        const val southeast = "southeast"
        const val southwest = "southwest"
    }

    /**
     * Defines the standard commands used in the game and their corresponding keys for language data.
     * Each command can have multiple aliases defined in the language data.
     */
    object Command {
        // general commands
        const val help = "help"
        const val quit = "quit"
        const val save = "save"
        const val load = "load"

        // action commands
        const val look = "look"
        const val inventory = "inventory"
        const val go = "go"
        const val take = "take"
        const val use = "use"
        const val drop = "drop"
        const val examine = "examine"
        const val open = "open"
        const val close = "close"
        const val lock = "lock"
        const val unlock = "unlock"
    }

    /**
     * Defines the placeholders used in messages and their corresponding keys for language data.
     * Placeholders are used for dynamic content in messages.
     */
    object StandIn {
        const val name = "<name>"
        const val article = "<article>"
        const val definiteName = "<definiteName>"
        const val indefiniteName = "<indefiniteName>"
        const val description = "<description>"
        const val stateDescription = "<stateDescription>"
        const val state = "<state>"
        const val direction = "<direction>"
        const val directions = "<directions>"
        const val exits = "<exits>"
        const val item = "<item>"
        const val items = "<items>"
        const val affectedItem = "<affectedItem>"
        const val title = "<title>"
        const val room = "<room>"
        const val command = "<command>"
        const val numberOfUses = "<numberOfUses>"
        const val placeholderExit = "<exit>"
        const val detailedExit = "<detailedExit>"
        const val pronounSubject = "<pronounSubject>"
        const val pronounObject = "<pronounObject>"
    }

    /**
     * State-value key constants for open/closed/locked text mappings.
     *
     * Copyright (c) 2026 Jochen Brinkmann. Licensed under the MIT License.
     */
    object StateValue {
        const val locked = "stateLocked"
        const val open = "stateOpen"
        const val closed = "stateClosed"
        const val lockedClosed = "stateLockedClosed"
        const val lockedOpen = "stateLockedOpen"
        const val blocked = "stateBlocked"
    }

    /**
     * Defines the headings used in messages and their corresponding keys for language data.
     */
    object Headings {
        const val directionsHeading = "directionsHeading"
        const val commandsHeading = "commandsHeading"
    }

    /**
     * Defines the message parts used in messages and their corresponding keys for language data.
     * Message parts are reusable components of messages that can be combined to create full messages.
     */
    object Part {
        const val exits = "msgPartExits"
        const val noExits = "msgPartNoExits"
        const val msgPartState = "msgPartState"
        const val msgPartDescriptiveName = "msgPartDescriptiveName"
        const val msgPartStatePlural = "msgPartStatePlural"
        const val items = "msgPartItems"
        const val noItems = "msgPartNoItems"
        const val inventory = "msgPartInventory"
        const val noInventory = "msgPartNoInventory"
    }

    /**
     * Defines the messages used in the game and their corresponding keys for language data.
     * Messages are the main text displayed to the player during the game.
     */
    object Message {
        const val msgUnknownCommand = "msgUnknownCommand"
        const val msgUnknownCommandWithHelp = "msgUnknownCommandWithHelp"
        const val msgNoExit = "msgNoExit"
        const val msgGoWhere = "msgGoWhere"
        const val msgExamineWhat = "msgExamineWhat"
        const val msgUseWhat = "msgUseWhat"
        const val msgTakeWhat = "msgTakeWhat"
        const val msgDropWhat = "msgDropWhat"
        const val msgNoItemFound = "msgNoItemFound"
        const val msgItemNotVisible = "msgItemNotVisible"
        const val msgNoInventoryItem = "msgNoInventoryItem"
        const val msgItemNotCarriable = "msgItemNotCarriable"
        const val msgContainerEmpty = "msgContainerEmpty"
        const val msgContainerContent = "msgContainerContent"
        const val msgItemTaken = "msgItemTaken"
        const val msgItemDropped = "msgItemDropped"
        const val msgItemNotUsable = "msgItemNotUsable"
        const val msgGameSaved = "msgGameSaved"
        const val msgNoSavedGameFound = "msgNoSavedGameFound"
        const val msgGameLoaded = "msgGameLoaded"
        const val msgNoExitsFromRoom = "msgNoExitsFromRoom"
        const val msgExitTargetRoomMissing = "msgExitTargetRoomMissing"
        const val msgWelcome = "msgWelcome"
        const val msgIntroHelp = "msgIntroHelp"
        const val msgGoodbye = "msgGoodbye"
        const val msgExitDetailedNoDescription = "msgExitDetailedNoDescription"
        const val msgExitDetailedDescription = "msgExitDetailedDescription"
        const val msgExitDetailedNoDescriptionNoName = "msgExitDetailedNoDescriptionNoName"
        const val msgExitDetailedDescriptionNoName = "msgExitDetailedDescriptionNoName"
        const val msgOpenCloseWhat = "msgOpenCloseWhat"
        const val msgDoesNotSupportOpenClose = "msgDoesNotSupportOpenClose"
        const val msgAlreadyOpen = "msgAlreadyOpen"
        const val msgAlreadyClosed = "msgAlreadyClosed"
        const val msgTargetLocked = "msgTargetLocked"
        const val msgOpenSuccess = "msgOpenSuccess"
        const val msgCloseSuccess = "msgCloseSuccess"
        const val msgOpenFailed = "msgOpenFailed"
        const val msgCloseFailed = "msgCloseFailed"
        const val msgNoTargetToOpenClose = "msgNoTargetToOpenClose"
        const val msgNoDoorOrContainerFound = "msgNoDoorOrContainerFound"
        const val msgExitBlocked = "msgExitBlocked"
        const val msgExitLocked = "msgExitLocked"
        const val msgExitClosed = "msgExitClosed"
    }
}
