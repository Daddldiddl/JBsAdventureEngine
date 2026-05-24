package net.daddldiddl.jbsadventure.model.lang

/**
 * Defines the keys for accessing specific language data entries, 
 * such as directions, commands, messages, and placeholders.
 */
object LangKey {

    /**
     * Defines the standard directions used in the game and their corresponding keys for language data.
     * Each direction can have multiple aliases defined in the language data.
     */
    object Directions {
        public val north = "north"
        public val south = "south"   
        public val east = "east"
        public val west = "west"
        public val up = "up"
        public val down = "down"
        public val northeast = "northeast"
        public val northwest = "northwest"
        public val southeast = "southeast"
        public val southwest = "southwest"
    }

    /**
     * Defines the standard commands used in the game and their corresponding keys for language data.
     * Each command can have multiple aliases defined in the language data.
     */
    object Commands {
        // general commands
        public val help = "help"
        public val quit = "quit"
        public val save = "save"
        public val load = "load"

        // action commands
        public val look = "look"
        public val inventory = "inventory"
        public val go = "go"
        public val take = "take"
        public val use = "use"
        public val drop = "drop"
        public val examine = "examine"
    }

    /**
     * Defines the default values used in the game and their corresponding keys for language data.
     */
    object DefaultValues {
        public val defaultIndefiniteArticle = "defaultIndefiniteArticle"
        public val defaultDefiniteArticle = "defaultDefiniteArticle"
    }

    /**
     * Defines the placeholders used in messages and their corresponding keys for language data.
     * Placeholders are used for dynamic content in messages.
     */
    object Placeholders {    
        public val article = "<article>"
        public val specificArticle = "<specificArticle>"
        public val placeholderDirection = "<direction>"
        public val placeholderDirections = "<directions>"
        public val placeholderExits = "<exits>"
        public val placeholderItem = "<item>"
        public val placeholderItems = "<items>"
        public val placeholderAffectedItem = "<affectedItem>"
        public val placeholderTitle = "<title>"
        public val placeholderRoom = "<room>"
        public val placeholderState = "<state>"
        public val placeholderCommand = "<command>"
        public val placeholderNumberOfUses = "<numberOfUses>"
        public val placeholderExit = "<exit>"
        public val placeholderDetailedExit = "<detailedExit>"
    }

    object StateValues {
        public val exitLocked = "stateExitLocked"
        public val exitClosed = "stateExitClosed"
        public val exitBlocked = "stateExitBlocked"
        public val exitVisible = "stateExitVisible"

    }

    /**
     * Defines the headings used in messages and their corresponding keys for language data.
     */
    object Headings {
        public val directionsHeading = "directionsHeading"
        public val commandsHeading = "commandsHeading"
    }

    /**
     * Defines the message parts used in messages and their corresponding keys for language data.
     * Message parts are reusable components of messages that can be combined to create full messages.
     */
    object MessageParts {
        public val msgPartExits = "msgPartExits"
        public val msgPartNoExits = "msgPartNoExits"
        public val msgPartExitState = "msgPartExitState"
        public val msgPartItems = "msgPartItems"
        public val msgPartNoItems = "msgPartNoItems"
        public val msgPartInventory = "msgPartInventory"
        public val msgPartNoInventory = "msgPartNoInventory"
    }

    /**
     * Defines the messages used in the game and their corresponding keys for language data.
     * Messages are the main text displayed to the player during the game.
     */
    object Messages {
        public val msgUnknownCommand = "msgUnknownCommand"
        public val msgNoExit = "msgNoExit"
        public val msgNoItem = "msgNoItem"
        public val msgNoInventoryItem = "msgNoInventoryItem"
        public val msgItemTaken = "msgItemTaken"
        public val msgItemDropped = "msgItemDropped"
        public val msgItemNotUsable = "msgItemNotUsable"
        public val msgWelcome = "msgWelcome"
        public val msgGoodbye = "msgGoodbye"
        public val msgExitDefaultDescription = "msgExitDefaultDescription"
        public val msgExitDescription = "msgExitDescription"
        public val msgExitDetailedDescription = "msgExitDetailedDescription"
        public val msgExitDetailedDescriptionNoName = "msgExitDetailedDescriptionNoName"
    }
}