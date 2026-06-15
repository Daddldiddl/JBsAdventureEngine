package net.daddldiddl.jbsadventure.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.daddldiddl.jbsadventure.editor.io.EditorPersistence
import net.daddldiddl.jbsadventure.editor.io.FileDialogs
import net.daddldiddl.jbsadventure.editor.i18n.Messages
import net.daddldiddl.jbsadventure.editor.model.ActionDraft
import net.daddldiddl.jbsadventure.editor.model.EditorSession
import net.daddldiddl.jbsadventure.editor.model.ExitDraft
import net.daddldiddl.jbsadventure.editor.model.ItemDraft
import net.daddldiddl.jbsadventure.editor.model.PreconditionDraft
import net.daddldiddl.jbsadventure.editor.model.RoomDraft
import net.daddldiddl.jbsadventure.editor.model.StateDraft
import java.util.Locale

private enum class Section {
    METADATA,
    ROOMS,
    ITEMS,
    EXITS,
    STATES,
    ACTIONS,
    PRECONDITIONS
}

private enum class ValidationSeverity {
    ERROR,
    WARNING
}

private data class ValidationIssue(
    val severity: ValidationSeverity,
    val messageKey: String,
    val messageArgs: List<Any> = emptyList(),
    val roomId: Int? = null
)

private val canonicalDirections = listOf("north", "south", "east", "west", "up", "down")

private val directionAliasesToCanonical = mapOf(
    // English aliases
    "n" to "north",
    "s" to "south",
    "e" to "east",
    "w" to "west",
    "u" to "up",
    "d" to "down",
    // German aliases
    "norden" to "north",
    "sueden" to "south",
    "süden" to "south",
    "osten" to "east",
    "westen" to "west",
    "oben" to "up",
    "hoch" to "up",
    "unten" to "down",
    "runter" to "down"
)

@Composable
fun EditorApp() {
    var currentLocale by remember { mutableStateOf(Locale.ENGLISH) }
    var section by remember { mutableStateOf(Section.METADATA) }
    var statusText by remember { mutableStateOf("Ready") }
    var currentFilePath by remember { mutableStateOf<String?>(null) }
    val session = remember {
        EditorSession(
            rooms = mutableListOf(RoomDraft(1, "Starting Room", "")),
            items = mutableListOf(ItemDraft(100, "Rusty Key", "")),
            states = mutableListOf(StateDraft("gate_state", "closed", "open,closed,locked")),
            actions = mutableListOf(ActionDraft("Message", "")),
            preconditions = mutableListOf(PreconditionDraft("PreconditionState", ""))
        )
    }

    fun t(key: String): String = Messages.text(currentLocale, key)
    fun tf(key: String, vararg args: Any): String = Messages.format(currentLocale, key, *args)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TopBar(
                    label = t("language.label"),
                    englishLabel = t("language.english"),
                    germanLabel = t("language.german"),
                    appTitle = t("app.title"),
                    newLabel = t("button.new"),
                    openLabel = t("button.open"),
                    saveLabel = t("button.save"),
                    onNew = {
                        session.metadata.title = ""
                        session.metadata.intro = ""
                        session.metadata.outro = ""
                        session.rooms.clear()
                        session.items.clear()
                        session.states.clear()
                        session.actions.clear()
                        session.preconditions.clear()
                        currentFilePath = null
                        statusText = t("status.newAdventure")
                    },
                    onOpen = {
                        val file = FileDialogs.chooseFileToOpen()
                        if (file != null) {
                            try {
                                val loadedSession = EditorPersistence.loadSession(file)
                                replaceSessionContent(session, loadedSession)
                                currentFilePath = file.absolutePath
                                statusText = tf("status.opened", file.name)
                            } catch (ex: Exception) {
                                statusText = tf("status.openFailed", ex.message ?: "unknown")
                            }
                        }
                    },
                    onSave = {
                        val validationIssues = collectValidationIssues(session)
                        val blockingErrors = validationIssues.filter { it.severity == ValidationSeverity.ERROR }
                        if (blockingErrors.isNotEmpty()) {
                            val firstError = blockingErrors.first()
                            statusText = tf(
                                "status.saveBlocked",
                                Messages.format(currentLocale, firstError.messageKey, *firstError.messageArgs.toTypedArray())
                            )
                            return@TopBar
                        }

                        val existingPath = currentFilePath
                        val file = if (existingPath != null) {
                            java.io.File(existingPath)
                        } else {
                            FileDialogs.chooseFileToSave()
                        }

                        if (file != null) {
                            try {
                                EditorPersistence.saveSession(file, session)
                                currentFilePath = file.absolutePath
                                statusText = tf("status.saved", file.name)
                            } catch (ex: Exception) {
                                statusText = tf("status.saveFailed", ex.message ?: "unknown")
                            }
                        }
                    },
                    onEnglish = { currentLocale = Locale.ENGLISH },
                    onGerman = { currentLocale = Locale.GERMAN }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationPanel(
                        title = t("navigation.title"),
                        section = section,
                        onSectionChanged = { section = it },
                        sections = listOf(
                            Section.METADATA to t("section.metadata"),
                            Section.ROOMS to t("section.rooms"),
                            Section.ITEMS to t("section.items"),
                            Section.EXITS to t("section.exits"),
                            Section.STATES to t("section.states"),
                            Section.ACTIONS to t("section.actions"),
                            Section.PRECONDITIONS to t("section.preconditions")
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    DetailsPanel(
                        title = t("details.title"),
                        section = section,
                        statusReady = t("status.ready"),
                        statusScope = t("status.scope"),
                        statusText = statusText,
                        validationIssues = collectValidationIssues(session),
                        locale = currentLocale,
                        currentFilePath = currentFilePath,
                        session = session
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    label: String,
    englishLabel: String,
    germanLabel: String,
    appTitle: String,
    newLabel: String,
    openLabel: String,
    saveLabel: String,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onEnglish: () -> Unit,
    onGerman: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = appTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onNew) { Text(text = newLabel) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onOpen) { Text(text = openLabel) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) { Text(text = saveLabel) }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "$label:")
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onEnglish) { Text(text = englishLabel) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onGerman) { Text(text = germanLabel) }
        }
    }
}

@Composable
private fun NavigationPanel(
    title: String,
    section: Section,
    onSectionChanged: (Section) -> Unit,
    sections: List<Pair<Section, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFFEFEFEF))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        sections.forEach { entry ->
            val bgColor = if (entry.first == section) Color(0xFFDCE9FF) else Color.Transparent
            Text(
                text = entry.second,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .clickable { onSectionChanged(entry.first) }
                    .padding(vertical = 6.dp, horizontal = 6.dp)
            )
        }
    }
}

@Composable
private fun DetailsPanel(
    title: String,
    section: Section,
    statusReady: String,
    statusScope: String,
    statusText: String,
    validationIssues: List<ValidationIssue>,
    locale: Locale,
    currentFilePath: String?,
    session: EditorSession
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FF))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        when (section) {
            Section.METADATA -> MetadataEditor(session, locale)
            Section.ROOMS -> RoomsEditor(session, locale)
            Section.ITEMS -> ItemsEditor(session, locale)
            Section.EXITS -> ExitsEditor(session, validationIssues, locale)
            Section.STATES -> StatesEditor(session, locale)
            Section.ACTIONS -> ActionsEditor(session, locale)
            Section.PRECONDITIONS -> PreconditionsEditor(session, locale)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = statusReady)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = statusScope)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = statusText)
        val validationErrors = validationIssues.filter { it.severity == ValidationSeverity.ERROR }
        val validationWarnings = validationIssues.filter { it.severity == ValidationSeverity.WARNING }
        if (validationErrors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = Messages.text(locale, "status.validationErrors"), color = Color(0xFFB00020), fontWeight = FontWeight.SemiBold)
            validationErrors.forEach { error ->
                Text(text = "- ${formatValidationIssue(locale, error)}", color = Color(0xFFB00020))
            }
        }
        if (validationWarnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = Messages.text(locale, "status.validationWarnings"), color = Color(0xFF8A6D00), fontWeight = FontWeight.SemiBold)
            validationWarnings.forEach { warning ->
                Text(text = "- ${formatValidationIssue(locale, warning)}", color = Color(0xFF8A6D00))
            }
        }
        if (currentFilePath != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = Messages.format(locale, "status.file", currentFilePath))
        }
    }
}

@Composable
private fun MetadataEditor(session: EditorSession, locale: Locale) {
    Column {
        OutlinedTextField(
            value = session.metadata.title,
            onValueChange = { session.metadata.title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.title")) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = session.metadata.intro,
            onValueChange = { session.metadata.intro = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.intro")) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = session.metadata.outro,
            onValueChange = { session.metadata.outro = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.outro")) }
        )
    }
}

@Composable
private fun RoomsEditor(session: EditorSession, locale: Locale) {
    var nextRoomId by remember { mutableStateOf((session.rooms.maxOfOrNull { it.id } ?: 0) + 1) }

    Column {
        Button(onClick = {
            session.rooms.add(RoomDraft(nextRoomId, "Room $nextRoomId", ""))
            nextRoomId += 1
        }) {
            Text(t(locale, "button.addRoom"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(session.rooms) { room ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(tf(locale, "ui.roomTitle", room.id), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = room.name,
                            onValueChange = { room.name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.name")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = room.description,
                            onValueChange = { room.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsEditor(session: EditorSession, locale: Locale) {
    var nextItemId by remember { mutableStateOf((session.items.maxOfOrNull { it.id } ?: 99) + 1) }

    Column {
        Button(onClick = {
            session.items.add(ItemDraft(nextItemId, "Item $nextItemId", ""))
            nextItemId += 1
        }) {
            Text(t(locale, "button.addItem"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(session.items) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(tf(locale, "ui.itemTitle", item.id), fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { item.name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.name")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { item.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExitsEditor(session: EditorSession, validationIssues: List<ValidationIssue>, locale: Locale) {
    if (session.rooms.isEmpty()) {
        Text(t(locale, "ui.noRooms"))
        return
    }

    val allRoomIds = session.rooms.map { it.id }

    LazyColumn {
        items(session.rooms) { room ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(tf(locale, "ui.roomHeader", room.id, room.name), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(onClick = {
                        val fallbackTarget = session.rooms.firstOrNull { it.id != room.id }?.id ?: room.id
                        room.exits.add(
                            ExitDraft(
                                direction = "north",
                                targetRoomId = fallbackTarget
                            )
                        )
                    }) {
                        Text(t(locale, "button.addExit"))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (room.exits.isEmpty()) {
                        Text(t(locale, "ui.noExits"))
                    }

                    room.exits.forEachIndexed { index, exit ->
                        ExitRow(room, exit, index, allRoomIds, locale)
                    }

                    val roomIssues = validationIssues.filter { it.roomId == room.id }
                    if (roomIssues.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        roomIssues.forEach { issue ->
                            val issueColor = if (issue.severity == ValidationSeverity.ERROR) Color(0xFFB00020) else Color(0xFF8A6D00)
                            Text(text = formatValidationIssue(locale, issue), color = issueColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExitRow(room: RoomDraft, exit: ExitDraft, index: Int, roomIds: List<Int>, locale: Locale) {
    var targetExpanded by remember { mutableStateOf(false) }
    var directionExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(tf(locale, "ui.exitTitle", index + 1))

            Button(onClick = { directionExpanded = true }) {
                Text(tf(locale, "button.direction", exit.direction.ifBlank { t(locale, "placeholder.empty") }))
            }
            DropdownMenu(expanded = directionExpanded, onDismissRequest = { directionExpanded = false }) {
                canonicalDirections.forEach { direction ->
                    DropdownMenuItem(
                        text = { Text(direction) },
                        onClick = {
                            exit.direction = direction
                            directionExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = exit.direction,
                onValueChange = { exit.direction = normalizeDirection(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t(locale, "field.direction")) }
            )
            Spacer(modifier = Modifier.height(6.dp))

            Button(onClick = { targetExpanded = true }) {
                Text(tf(locale, "button.targetRoom", exit.targetRoomId))
            }
            DropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                roomIds.forEach { roomId ->
                    DropdownMenuItem(
                        text = { Text(tf(locale, "ui.roomTitle", roomId)) },
                        onClick = {
                            exit.targetRoomId = roomId
                            targetExpanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(onClick = { exit.visible = !exit.visible }) { Text(tf(locale, "button.toggleVisible", exit.visible)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { exit.open = !exit.open }) { Text(tf(locale, "button.toggleOpen", exit.open)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { exit.locked = !exit.locked }) { Text(tf(locale, "button.toggleLocked", exit.locked)) }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { exit.supportsOpenClose = !exit.supportsOpenClose }) {
                    Text(tf(locale, "button.toggleSupportsOpenClose", exit.supportsOpenClose))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { exit.supportsLockUnlock = !exit.supportsLockUnlock }) {
                    Text(tf(locale, "button.toggleSupportsLockUnlock", exit.supportsLockUnlock))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (index in room.exits.indices) {
                    room.exits.removeAt(index)
                }
            }) {
                Text(t(locale, "button.removeExit"))
            }
        }
    }
}

private fun replaceSessionContent(target: EditorSession, source: EditorSession) {
    target.metadata.title = source.metadata.title
    target.metadata.intro = source.metadata.intro
    target.metadata.outro = source.metadata.outro

    target.rooms.clear()
    target.rooms.addAll(source.rooms)

    target.items.clear()
    target.items.addAll(source.items)

    target.states.clear()
    target.states.addAll(source.states)

    target.actions.clear()
    target.actions.addAll(source.actions)

    target.preconditions.clear()
    target.preconditions.addAll(source.preconditions)
}

private fun collectValidationIssues(session: EditorSession): List<ValidationIssue> {
    val issues = mutableListOf<ValidationIssue>()
    val roomIds = session.rooms.map { it.id }.toSet()

    session.rooms.forEach { room ->
        val directionGroups = room.exits.groupBy { normalizeDirection(it.direction) }
        directionGroups
            .filter { entry -> entry.key.isNotBlank() && entry.value.size > 1 }
            .forEach { entry ->
                issues.add(
                    ValidationIssue(
                        severity = ValidationSeverity.ERROR,
                        roomId = room.id,
                        messageKey = "validation.roomDuplicateDirection",
                        messageArgs = listOf(room.id, entry.key)
                    )
                )
            }

        room.exits.forEach { exit ->
            val direction = normalizeDirection(exit.direction)
            if (direction.isBlank()) {
                issues.add(
                    ValidationIssue(
                        severity = ValidationSeverity.ERROR,
                        roomId = room.id,
                        messageKey = "validation.roomEmptyDirection",
                        messageArgs = listOf(room.id)
                    )
                )
            } else if (direction !in canonicalDirections) {
                issues.add(
                    ValidationIssue(
                        severity = ValidationSeverity.WARNING,
                        roomId = room.id,
                        messageKey = "validation.roomNonStandardDirection",
                        messageArgs = listOf(room.id, exit.direction)
                    )
                )
            }
            if (!roomIds.contains(exit.targetRoomId)) {
                issues.add(
                    ValidationIssue(
                        severity = ValidationSeverity.ERROR,
                        roomId = room.id,
                        messageKey = "validation.roomUnknownTarget",
                        messageArgs = listOf(room.id, exit.direction, exit.targetRoomId)
                    )
                )
            }
            if (exit.locked && !exit.supportsLockUnlock) {
                issues.add(
                    ValidationIssue(
                        severity = ValidationSeverity.WARNING,
                        roomId = room.id,
                        messageKey = "validation.roomLockedWithoutSupport",
                        messageArgs = listOf(room.id, exit.direction)
                    )
                )
            }
        }
    }

    return issues.distinctBy { "${it.severity}:${it.roomId}:${it.messageKey}:${it.messageArgs.joinToString("|")}" }
}

private fun normalizeDirection(raw: String): String {
    val normalized = raw.trim().lowercase()
    if (normalized.isBlank()) {
        return ""
    }
    return directionAliasesToCanonical[normalized] ?: normalized
}

private fun formatValidationIssue(locale: Locale, issue: ValidationIssue): String {
    return Messages.format(locale, issue.messageKey, *issue.messageArgs.toTypedArray())
}

@Composable
private fun StatesEditor(session: EditorSession, locale: Locale) {
    Column {
        Button(onClick = {
            session.states.add(StateDraft("state_${session.states.size + 1}", "", ""))
        }) {
            Text(t(locale, "button.addState"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(session.states) { state ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = state.key,
                            onValueChange = { state.key = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.stateKey")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.currentValue,
                            onValueChange = { state.currentValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.currentValue")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.possibleValues,
                            onValueChange = { state.possibleValues = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.possibleValues")) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsEditor(session: EditorSession, locale: Locale) {
    val allowedTypes = listOf("MoveTo", "SetItemRoom", "ChangeState", "TransformIntoItem", "ModifyExit", "ModifyContainer", "Message")

    Column {
        Button(onClick = { session.actions.add(ActionDraft("Message", "")) }) {
            Text(t(locale, "button.addAction"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(session.actions) { action ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = action.type,
                            onValueChange = { action.type = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.actionType")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = action.description,
                            onValueChange = { action.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tf(locale, "ui.validationKey", allowedTypes.joinToString()))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreconditionsEditor(session: EditorSession, locale: Locale) {
    val allowedTypes = listOf(
        "PreconditionState",
        "PreconditionItem",
        "PreconditionContainer",
        "PreconditionExit",
        "PreconditionItemsLocation",
        "PreconditionPlayer"
    )

    Column {
        Button(onClick = { session.preconditions.add(PreconditionDraft("PreconditionState", "")) }) {
            Text(t(locale, "button.addPrecondition"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(session.preconditions) { precondition ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = precondition.type,
                            onValueChange = { precondition.type = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.preconditionType")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = precondition.summary,
                            onValueChange = { precondition.summary = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.summary")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tf(locale, "ui.validationKey", allowedTypes.joinToString()))
                    }
                }
            }
        }
    }
}

private fun t(locale: Locale, key: String): String = Messages.text(locale, key)

private fun tf(locale: Locale, key: String, vararg args: Any): String =
    Messages.format(locale, key, *args)
