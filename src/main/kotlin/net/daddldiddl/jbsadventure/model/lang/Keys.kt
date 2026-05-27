package net.daddldiddl.jbsadventure.model.lang

/**
 * Defines the keys for accessing specific language data entries from the maps in [LanguageData],
 * such as directions, commands, messages, and placeholders.
 */
object Keys {

    const val languageKey = "languageKey"
    const val languageKeyEn = "en"

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
    object Commands {
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
    }

    /**
     * Defines the placeholders used in messages and their corresponding keys for language data.
     * Placeholders are used for dynamic content in messages.
     */
    object Placeholders {    
        const val article = "<article>"
        const val specificArticle = "<specificArticle>"
        const val direction = "<direction>"
        const val directions = "<directions>"
        const val exits = "<exits>"
        const val item = "<item>"
        const val items = "<items>"
        const val affectedItem = "<affectedItem>"
        const val title = "<title>"
        const val room = "<room>"
        const val state = "<state>"
        const val command = "<command>"
        const val numberOfUses = "<numberOfUses>"
        const val placeholderExit = "<exit>"
        const val detailedExit = "<detailedExit>"
    }

    object StateValues {
        const val locked = "stateExitLocked"
        const val open = "stateOpen"
        const val closed = "stateExitClosed"
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
    object MessageParts {
        const val exits = "msgPartExits"
        const val noExits = "msgPartNoExits"
        const val exitState = "msgPartExitState"
        const val items = "msgPartItems"
        const val noItems = "msgPartNoItems"
        const val inventory = "msgPartInventory"
        const val noInventory = "msgPartNoInventory"
    }

    /**
     * Defines the messages used in the game and their corresponding keys for language data.
     * Messages are the main text displayed to the player during the game.
     */
    object Messages {
        const val msgUnknownCommand = "msgUnknownCommand"
        const val msgNoExit = "msgNoExit"
        const val msgNoItem = "msgNoItem"
        const val msgNoInventoryItem = "msgNoInventoryItem"
        const val msgItemTaken = "msgItemTaken"
        const val msgItemDropped = "msgItemDropped"
        const val msgItemNotUsable = "msgItemNotUsable"
        const val msgWelcome = "msgWelcome"
        const val msgIntroHelp = "msgIntroHelp"
        const val msgGoodbye = "msgGoodbye"
        const val msgExitDefaultDescription = "msgExitDefaultDescription"
        const val msgExitDescription = "msgExitDescription"
        const val msgExitDetailedDescription = "msgExitDetailedDescription"
        const val msgExitDetailedDescriptionNoName = "msgExitDetailedDescriptionNoName"
    }
}
