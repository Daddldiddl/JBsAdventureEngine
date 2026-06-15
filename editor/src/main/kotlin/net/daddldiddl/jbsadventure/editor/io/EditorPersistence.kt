package net.daddldiddl.jbsadventure.editor.io

import kotlinx.serialization.json.Json
import net.daddldiddl.jbsadventure.editor.model.EditorSession
import net.daddldiddl.jbsadventure.editor.model.ExitDraft
import net.daddldiddl.jbsadventure.editor.model.ItemDraft
import net.daddldiddl.jbsadventure.editor.model.RoomDraft
import net.daddldiddl.jbsadventure.editor.model.StateDraft
import java.io.File

object EditorPersistence {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun saveSession(file: File, session: EditorSession) {
        val adventure = session.toAdventureFile()
        file.writeText(json.encodeToString(AdventureFile.serializer(), adventure))
    }

    fun loadSession(file: File): EditorSession {
        val text = file.readText()
        val adventure = json.decodeFromString(AdventureFile.serializer(), text)
        return adventure.toEditorSession()
    }
}

private fun EditorSession.toAdventureFile(): AdventureFile {
    val stateFiles = states.map { state ->
        val values = state.possibleValues.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(state.currentValue.ifBlank { "default" }) }

        StateFile(
            stateKey = state.key,
            currentValue = state.currentValue.ifBlank { values.first() },
            description = "",
            possibleValues = values
        )
    }

    return AdventureFile(
        title = metadata.title,
        introductionMessage = metadata.intro,
        exitMessage = metadata.outro,
        rooms = rooms.map { room ->
            RoomFile(
                id = room.id,
                name = NameFile(name = room.name),
                description = room.description,
                exits = room.exits.map { exit ->
                    ExitFile(
                        direction = exit.direction,
                        targetRoomId = exit.targetRoomId,
                        supportsOpenClose = exit.supportsOpenClose,
                        supportsLockUnlock = exit.supportsLockUnlock,
                        open = exit.open,
                        locked = exit.locked,
                        visible = exit.visible
                    )
                }
            )
        },
        items = items.map { item ->
            ItemFile(
                id = item.id,
                name = NameFile(name = item.name),
                description = item.description,
                location = 0,
                carriable = true,
                usable = true
            )
        },
        states = stateFiles
    )
}

private fun AdventureFile.toEditorSession(): EditorSession {
    return EditorSession(
        metadata = net.daddldiddl.jbsadventure.editor.model.AdventureMetadata(
            title = title,
            intro = introductionMessage,
            outro = exitMessage
        ),
        rooms = rooms.map { room ->
            RoomDraft(
                id = room.id,
                name = room.name.name,
                description = room.description,
                exits = room.exits.map { exit ->
                    ExitDraft(
                        direction = exit.direction,
                        targetRoomId = exit.targetRoomId,
                        supportsOpenClose = exit.supportsOpenClose ?: false,
                        supportsLockUnlock = exit.supportsLockUnlock ?: false,
                        open = exit.open ?: true,
                        locked = exit.locked ?: false,
                        visible = exit.visible ?: true
                    )
                }.toMutableList()
            )
        }.toMutableList(),
        items = items.map { item ->
            ItemDraft(
                id = item.id,
                name = item.name.name,
                description = item.description
            )
        }.toMutableList(),
        states = states.map { state ->
            StateDraft(
                key = state.stateKey,
                currentValue = state.currentValue,
                possibleValues = state.possibleValues.joinToString(",")
            )
        }.toMutableList(),
        actions = mutableListOf(),
        preconditions = mutableListOf()
    )
}
