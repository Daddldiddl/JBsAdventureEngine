package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

/** Enum representing different fixed locations in the game. */
@Serializable
enum class FixedLocation(val value: Int) {

    NOT_ASSIGNED(0),
    INVENTORY(-1),
    CONTAINER(-2);

    companion object{

        fun valueOf(value: String): FixedLocation {
            return when (value) {
                "NOT_ASSIGNED" -> NOT_ASSIGNED
                "INVENTORY" -> INVENTORY
                "CONTAINER" -> CONTAINER
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }

        fun valueOf(value: Int): FixedLocation {
            return when (value) {
                NOT_ASSIGNED.value -> NOT_ASSIGNED
                INVENTORY.value -> INVENTORY
                CONTAINER.value -> CONTAINER
                else -> throw IllegalArgumentException("No enum value net.daddldiddl.jbsadventure.model.FixedLocations.$value")
            }
        }
    }
}
