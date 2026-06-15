package net.daddldiddl.jbsadventure.editor.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureFile(
    val title: String = "",
    val introductionMessage: String = "",
    val exitMessage: String = "",
    @SerialName("Rooms")
    val rooms: List<RoomFile> = emptyList(),
    @SerialName("Items")
    val items: List<ItemFile> = emptyList(),
    @SerialName("States")
    val states: List<StateFile> = emptyList()
)

@Serializable
data class RoomFile(
    val id: Int,
    val name: NameFile,
    val description: String = "",
    val exits: List<ExitFile> = emptyList()
)

@Serializable
data class NameFile(
    val name: String,
    val aliases: List<String> = emptyList()
)

@Serializable
data class ExitFile(
    val direction: String,
    val targetRoomId: Int,
    val supportsOpenClose: Boolean? = null,
    val supportsLockUnlock: Boolean? = null,
    val open: Boolean? = null,
    val locked: Boolean? = null,
    val visible: Boolean? = null
)

@Serializable
data class ItemFile(
    val id: Int,
    val name: NameFile,
    val description: String = "",
    val location: Int = 0,
    val carriable: Boolean? = true,
    val usable: Boolean? = true
)

@Serializable
data class StateFile(
    val stateKey: String,
    val currentValue: String,
    val description: String = "",
    val possibleValues: List<String> = emptyList()
)
