package net.daddldiddl.jbsadventure.model

/** Enum representing different fixed locations in the game. */
public enum class Locations(val value: Int) {
    /** Location not assigned */
    NOT_ASSIGNED(0),
    /** Player's inventory */
    INVENTORY(-1),
    /** Location for when items are stored inside a container */
    CONTAINER(-2);
}
