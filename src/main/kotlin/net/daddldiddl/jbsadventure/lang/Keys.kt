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
        /** The defaultPronounGroupKey used for the default pronoun group */
        const val defaultDefaultPronounGroupKey = "defaultPronounGroupKey"
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
        const val exits = "<exits>"
        const val item = "<item>"
        const val items = "<items>"
        const val title = "<title>"
        const val room = "<room>"
        const val command = "<command>"
        const val numberOfUses = "<numberOfUses>"
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
        const val descriptiveName = "msgPartDescriptiveName"
        const val state = "msgPartState"
        const val statePlural = "msgPartStatePlural"
        const val items = "msgPartItems"
        const val inventory = "msgPartInventory"
        const val noInventory = "msgPartNoInventory"
    }

    /**
     * Defines the messages used in the game and their corresponding keys for language data.
     * Messages are the main text displayed to the player during the game.
     */
    object Message {
        const val msgUnknownCommandWithHelp = "msgUnknownCommandWithHelp"
        const val msgCommandWhat = "msgCommandWhat"
        const val msgGoWhere = "msgGoWhere"
        const val msgNoItemFound = "msgNoItemFound"
        const val msgNoInventoryItem = "msgNoInventoryItem"
        const val msgItemNotCarriable = "msgItemNotCarriable"
        const val msgContainerEmpty = "msgContainerEmpty"
        const val msgContainerContent = "msgContainerContent"
        const val msgItemTaken = "msgItemTaken"
        const val msgItemDropped = "msgItemDropped"
        const val msgItemNotUsable = "msgItemNotUsable"
        const val msgItemDetailedDescription = "msgItemDetailedDescription"
        const val msgItemDetailedDescriptionNoDescription = "msgItemDetailedDescriptionNoDescription"
        const val msgGameSaved = "msgGameSaved"
        const val msgNoSavedGameFound = "msgNoSavedGameFound"
        const val msgGameLoaded = "msgGameLoaded"
        const val msgNoExitsFromRoom = "msgNoExitsFromRoom"
        const val msgNoExitInDirection = "msgNoExitInDirection"
        const val msgWelcome = "msgWelcome"
        const val msgIntroHelp = "msgIntroHelp"
        const val msgGoodbye = "msgGoodbye"
        const val msgExitDetailedNoDescription = "msgExitDetailedNoDescription"
        const val msgExitDetailedDescription = "msgExitDetailedDescription"
        const val msgExitDetailedNoDescriptionNoName = "msgExitDetailedNoDescriptionNoName"
        const val msgExitDetailedDescriptionNoName = "msgExitDetailedDescriptionNoName"
        const val msgDoesNotSupportOpenClose = "msgDoesNotSupportOpenClose"
        const val msgDoesNotSupportLockUnlock = "msgDoesNotSupportLockUnlock"
        const val msgLockRequiresNoKey = "msgLockRequiresNoKey"
        const val msgUnlockRequiresNoKey = "msgUnlockRequiresNoKey"
        const val msgEntityAlreadyOpen = "msgAlreadyOpen"
        const val msgEntityAlreadyClosed = "msgAlreadyClosed"
        const val msgEntityAlreadyUnlocked = "msgAlreadyUnlocked"
        const val msgEntityAlreadyLocked = "msgAlreadyLocked"
        const val msgEntityOpened = "msgOpenSuccess"
        const val msgEntityClosed = "msgCloseSuccess"
        const val msgEntityLocked = "msgLockSuccess"
        const val msgEntityUnlocked = "msgUnlockSuccess"
        const val msgNoDoorOrContainerFound = "msgNoDoorOrContainerFound"
        const val msgLockKeyMissing = "msgLockKeyMissing"
        const val msgUnlockKeyMissing = "msgLockKeyMissing"
        const val msgExitIsBlocked = "msgExitBlocked"
    }
}
