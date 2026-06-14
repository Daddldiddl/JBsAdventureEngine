package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/** Enum representing different fixed location ids in the game. */
@Serializable
enum class FixedLocation(val value: Int) {

    /** Deserialization returned an invalid location id */
    INVALID(-666),
    /** item is currently not assigned a location */
    NOT_ASSIGNED(0),
    /** Item is in the players inventory */
    INVENTORY(-1),
    /** Item is in a container instead of a room or the inventory */
    CONTAINER(-2);

    companion object{

        fun valueOf(value: String): FixedLocation {
            return when (value) {
                "INVALID" -> INVALID
                "NOT_ASSIGNED" -> NOT_ASSIGNED
                "INVENTORY" -> INVENTORY
                "CONTAINER" -> CONTAINER
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }

        fun valueOf(value: Int): FixedLocation {
            return when (value) {
                INVALID.value -> INVALID
                NOT_ASSIGNED.value -> NOT_ASSIGNED
                INVENTORY.value -> INVENTORY
                CONTAINER.value -> CONTAINER
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }
    }
}

/** Enum representing different fixed item ids in the game. */
@Serializable
enum class FixedItem(val value: Int) {

    /** Deserialization returned an invalid item id */
    INVALID(-666);

    companion object{

        fun valueOf(value: String): FixedItem {
            return when (value) {
                "INVALID" -> INVALID
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }

        fun valueOf(value: Int): FixedItem {
            return when (value) {
                INVALID.value -> INVALID
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }
    }
}
