package net.daddldiddl.jbsadventure.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.daddldiddl.jbsadventure.editor.io.EditorPersistence
import net.daddldiddl.jbsadventure.editor.io.FileDialogs
import net.daddldiddl.jbsadventure.editor.i18n.Messages
import net.daddldiddl.jbsadventure.editor.model.ActionDraft
import net.daddldiddl.jbsadventure.editor.model.EditorSession
import net.daddldiddl.jbsadventure.editor.model.ExitDraft
import net.daddldiddl.jbsadventure.editor.model.ItemDraft
import net.daddldiddl.jbsadventure.editor.model.NameDraft
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
    ACTIONS
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

private val canonicalDirections = listOf(
    "north", "south", "east", "west",
    "northeast", "northwest", "southeast", "southwest",
    "up", "down"
)

private val directionAliasesToCanonical = mapOf(
    // English cardinal
    "n" to "north",
    "s" to "south",
    "e" to "east",
    "w" to "west",
    "u" to "up",
    "d" to "down",
    // English intercardinal
    "ne" to "northeast",
    "nw" to "northwest",
    "se" to "southeast",
    "sw" to "southwest",
    // German cardinal
    "norden" to "north",
    "sueden" to "south",
    "süden" to "south",
    "osten" to "east",
    "westen" to "west",
    "oben" to "up",
    "hoch" to "up",
    "unten" to "down",
    "runter" to "down",
    // German intercardinal
    "nordost" to "northeast",
    "nordosten" to "northeast",
    "nordwest" to "northwest",
    "nordwesten" to "northwest",
    "suedost" to "southeast",
    "südost" to "southeast",
    "suedosten" to "southeast",
    "südosten" to "southeast",
    "suedwest" to "southwest",
    "südwest" to "southwest",
    "suedwesten" to "southwest",
    "südwesten" to "southwest"
)

private fun normalizeDirection(input: String): String {
    val key = input.trim().lowercase()
    if (key.isEmpty()) {
        return ""
    }
    return directionAliasesToCanonical[key] ?: key
}

private fun formatValidationIssue(locale: Locale, issue: ValidationIssue): String {
    return Messages.format(locale, issue.messageKey, *issue.messageArgs.toTypedArray())
}

@Composable
fun EditorApp() {
    var currentLocale by remember { mutableStateOf(Locale.ENGLISH) }
    var section by remember { mutableStateOf(Section.METADATA) }
    var statusText by remember { mutableStateOf("Ready") }
    var currentFilePath by remember { mutableStateOf<String?>(null) }
    val session = remember {
        EditorSession(
            rooms = mutableStateListOf(RoomDraft(1, "Starting Room", "")),
            items = mutableStateListOf(ItemDraft(100, "Rusty Key", "")),
            states = mutableStateListOf(StateDraft("gate_state", "closed", "open,closed,locked")),
            actions = mutableStateListOf(ActionDraft("Message", ""))
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
                            Section.ACTIONS to t("section.actions")
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = appTitle,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(8.dp))
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
                        NameEditor(room.nameDraft, locale, nameRequired = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = room.description,
                            onValueChange = { room.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoomItemUsagesEditor(room, session, locale)
                        Spacer(modifier = Modifier.height(8.dp))
                        TriggerListEditor(t(locale, "trigger.onExamine"), room.onExamine, locale)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomItemUsagesEditor(room: RoomDraft, session: EditorSession, locale: Locale) {
    var expanded by remember { mutableStateOf(false) }
    Button(onClick = { expanded = !expanded }) {
        Text(t(locale, "section.itemUsages") + " (${room.itemUsages.size})")
    }

    if (expanded) {
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = {
            val firstItemId = session.items.firstOrNull()?.id ?: 0
            room.itemUsages.add(net.daddldiddl.jbsadventure.editor.model.ItemUsageDraft(itemId = firstItemId))
        }) { Text(t(locale, "button.addItemUsage")) }

        room.itemUsages.forEachIndexed { index, usage ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${index + 1}", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Button(onClick = { room.itemUsages.removeAt(index) }) { Text("✕") }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = usage.itemId.toString(),
                        onValueChange = { usage.itemId = it.toIntOrNull() ?: 0 },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t(locale, "field.itemId")) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Button(onClick = { usage.becomesUsable = !usage.becomesUsable }) {
                            Text(tf(locale, "button.toggleBecomesUsable", usage.becomesUsable))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { usage.consumeUsedItem = !usage.consumeUsedItem }) {
                            Text(tf(locale, "button.toggleConsumeUsedItem", usage.consumeUsedItem))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    TriggerListEditor(t(locale, "section.actions"), usage.actions, locale)
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
                        NameEditor(item.nameDraft, locale, nameRequired = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { item.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = item.location.toString(),
                            onValueChange = { item.location = it.toIntOrNull() ?: 0 },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.itemLocation")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = item.stateKey,
                            onValueChange = { item.stateKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.stateKey")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = item.numberOfUses.toString(),
                            onValueChange = { item.numberOfUses = it.toIntOrNull() ?: 0 },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.numberOfUses")) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = { item.carriable = !item.carriable }) {
                                Text(tf(locale, "button.toggleCarriable", item.carriable))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { item.usable = !item.usable }) {
                                Text(tf(locale, "button.toggleUsable", item.usable))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { item.driveable = !item.driveable }) {
                                Text(tf(locale, "button.toggleDriveable", item.driveable))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TriggerListEditor(t(locale, "trigger.onExamine"), item.onExamine, locale)
                        Spacer(modifier = Modifier.height(4.dp))
                        TriggerListEditor(t(locale, "trigger.onUse"), item.onUse, locale)
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
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { exit.blocked = !exit.blocked }) { Text(tf(locale, "button.toggleBlocked", exit.blocked)) }
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

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = if (exit.keyId > 0) exit.keyId.toString() else "",
                onValueChange = { exit.keyId = it.toIntOrNull() ?: 0 },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t(locale, "field.exitKeyId")) }
            )
            if (exit.supportsLockUnlock) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Button(onClick = { exit.consumeKeyOnLock = !exit.consumeKeyOnLock }) {
                        Text(tf(locale, "button.toggleConsumeKeyOnLock", exit.consumeKeyOnLock))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { exit.consumeKeyOnUnlock = !exit.consumeKeyOnUnlock }) {
                        Text(tf(locale, "button.toggleConsumeKeyOnUnlock", exit.consumeKeyOnUnlock))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            NameEditor(exit.nameDraft, locale, nameRequired = false)

            Spacer(modifier = Modifier.height(8.dp))
            TriggerListEditor(t(locale, "trigger.onExamine"), exit.onExamine, locale)
            Spacer(modifier = Modifier.height(4.dp))
            TriggerListEditor(t(locale, "trigger.onOpen"), exit.onOpen, locale)
            Spacer(modifier = Modifier.height(4.dp))
            TriggerListEditor(t(locale, "trigger.onClose"), exit.onClose, locale)
            Spacer(modifier = Modifier.height(4.dp))
            TriggerListEditor(t(locale, "trigger.onLock"), exit.onLock, locale)
            Spacer(modifier = Modifier.height(4.dp))
            TriggerListEditor(t(locale, "trigger.onUnlock"), exit.onUnlock, locale)

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
}

private fun collectValidationIssues(session: EditorSession): List<ValidationIssue> {
    val issues = mutableListOf<ValidationIssue>()
    val roomIds = session.rooms.map { it.id }.toSet()
    val itemIds = session.items.map { it.id }.toSet()
    val stateKeys = session.states.map { it.key }.toSet()

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

        room.itemUsages.forEachIndexed { usageIndex, usage ->
            val usageLabel = "room ${room.id} itemUsage #${usageIndex + 1}"
            if (!itemIds.contains(usage.itemId)) {
                issues.add(
                    ValidationIssue(
                        ValidationSeverity.ERROR,
                        "validation.preconditionItemUnknown",
                        listOf(usageLabel, usage.itemId)
                    )
                )
            }
            usage.actions.forEachIndexed { actionIndex, action ->
                validateAction(
                    issues,
                    action,
                    "$usageLabel action #${actionIndex + 1}",
                    roomIds,
                    itemIds,
                    stateKeys,
                    session,
                    checkBinding = false
                )
            }
        }

        room.onExamine.forEachIndexed { index, action ->
            validateAction(issues, action, "room ${room.id} onExamine #${index + 1}", roomIds, itemIds, stateKeys, session, false)
        }
    }

    session.items.forEach { item ->
        item.onExamine.forEachIndexed { index, action ->
            validateAction(issues, action, "item ${item.id} onExamine #${index + 1}", roomIds, itemIds, stateKeys, session, false)
        }
        item.onUse.forEachIndexed { index, action ->
            validateAction(issues, action, "item ${item.id} onUse #${index + 1}", roomIds, itemIds, stateKeys, session, false)
        }
    }

    session.rooms.forEach { room ->
        room.exits.forEach { exit ->
            val exitLabel = "room ${room.id} exit ${normalizeDirection(exit.direction)}"
            exit.onExamine.forEachIndexed { index, action ->
                validateAction(issues, action, "$exitLabel onExamine #${index + 1}", roomIds, itemIds, stateKeys, session, false)
            }
            exit.onOpen.forEachIndexed { index, action ->
                validateAction(issues, action, "$exitLabel onOpen #${index + 1}", roomIds, itemIds, stateKeys, session, false)
            }
            exit.onClose.forEachIndexed { index, action ->
                validateAction(issues, action, "$exitLabel onClose #${index + 1}", roomIds, itemIds, stateKeys, session, false)
            }
            exit.onLock.forEachIndexed { index, action ->
                validateAction(issues, action, "$exitLabel onLock #${index + 1}", roomIds, itemIds, stateKeys, session, false)
            }
            exit.onUnlock.forEachIndexed { index, action ->
                validateAction(issues, action, "$exitLabel onUnlock #${index + 1}", roomIds, itemIds, stateKeys, session, false)
            }
        }
    }

    session.actions.forEachIndexed { index, action ->
        validateAction(
            issues = issues,
            action = action,
            indexLabel = index + 1,
            roomIds = roomIds,
            itemIds = itemIds,
            stateKeys = stateKeys,
            session = session,
            checkBinding = true
        )
    }

    return issues.distinctBy { "${it.severity}:${it.roomId}:${it.messageKey}:${it.messageArgs.joinToString("|")}" }
}

private fun validateAction(
    issues: MutableList<ValidationIssue>,
    action: ActionDraft,
    indexLabel: Any,
    roomIds: Set<Int>,
    itemIds: Set<Int>,
    stateKeys: Set<String>,
    session: EditorSession,
    checkBinding: Boolean
) {
    if (action.type.isBlank()) {
        issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionMissingType", listOf(indexLabel)))
    }

    if (checkBinding) {
        when (action.bindingScope) {
            "Room" -> if (!roomIds.contains(action.bindingTargetId)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionBindingRoomMissing", listOf(indexLabel, action.bindingTargetId)))
            }
            "Item", "Container" -> if (!itemIds.contains(action.bindingTargetId)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionBindingItemMissing", listOf(indexLabel, action.bindingTargetId)))
            }
            "Exit" -> {
                val room = session.rooms.firstOrNull { it.id == action.bindingRoomId }
                if (room == null) {
                    issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionBindingExitRoomMissing", listOf(indexLabel, action.bindingRoomId)))
                } else if (room.exits.none { normalizeDirection(it.direction) == normalizeDirection(action.bindingDirection) }) {
                    issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionBindingExitMissing", listOf(indexLabel, action.bindingDirection, action.bindingRoomId)))
                }
            }
        }
        if (action.bindingScope.isNotBlank() && action.bindingScope !in setOf("Global", "Room", "Item", "Container", "Exit")) {
            issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionBindingScopeUnknown", listOf(indexLabel, action.bindingScope)))
        }
    }

    when (action.type.trim()) {
        "ChangeState" -> {
            if (action.changedStateKey.isBlank()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionChangeStateKeyMissing", listOf(indexLabel)))
            } else if (!stateKeys.contains(action.changedStateKey)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionChangeStateKeyUnknown", listOf(indexLabel, action.changedStateKey)))
            }
        }
        "MoveTo" -> if (!roomIds.contains(action.moveToRoomId)) {
            issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionMoveRoomUnknown", listOf(indexLabel, action.moveToRoomId)))
        }
        "SetItemRoom" -> {
            val affected = parseCsvInts(action.affectedItemIdsCsv)
            if (affected.isEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionAffectedItemsMissing", listOf(indexLabel)))
            }
            val unknown = affected.filterNot { itemIds.contains(it) }
            if (unknown.isNotEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionAffectedItemsUnknown", listOf(indexLabel, unknown.joinToString(","))))
            }
            if (!roomIds.contains(action.moveToRoomIdForItems)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionMoveItemsRoomUnknown", listOf(indexLabel, action.moveToRoomIdForItems)))
            }
        }
        "TransformIntoItem" -> {
            val affected = parseCsvInts(action.affectedItemIdsCsv)
            val target = parseCsvInts(action.transformsIntoItemIdsCsv)
            if (affected.isEmpty() || target.isEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionTransformListsMissing", listOf(indexLabel)))
            }
            val unknown = (affected + target).filterNot { itemIds.contains(it) }
            if (unknown.isNotEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionTransformItemsUnknown", listOf(indexLabel, unknown.joinToString(","))))
            }
        }
        "ModifyExit" -> {
            val room = session.rooms.firstOrNull { it.id == action.modifyExitRoomId }
            if (room == null) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionModifyExitRoomUnknown", listOf(indexLabel, action.modifyExitRoomId)))
            } else if (room.exits.none { normalizeDirection(it.direction) == normalizeDirection(action.modifyExitDirection) }) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionModifyExitUnknown", listOf(indexLabel, action.modifyExitDirection, action.modifyExitRoomId)))
            }
            if (!isOptionalBooleanText(action.modifyExitOpen)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyExitOpen", action.modifyExitOpen)))
            }
            if (!isOptionalBooleanText(action.modifyExitLocked)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyExitLocked", action.modifyExitLocked)))
            }
            if (!isOptionalBooleanText(action.modifyExitBlocked)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyExitBlocked", action.modifyExitBlocked)))
            }
            if (!isOptionalBooleanText(action.modifyExitVisible)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyExitVisible", action.modifyExitVisible)))
            }
        }
        "ModifyContainer" -> {
            if (!itemIds.contains(action.modifyContainerId)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.actionModifyContainerUnknown", listOf(indexLabel, action.modifyContainerId)))
            }
            if (!isOptionalBooleanText(action.modifyContainerOpen)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyContainerOpen", action.modifyContainerOpen)))
            }
            if (!isOptionalBooleanText(action.modifyContainerLocked)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionInvalidTriStateBoolean", listOf(indexLabel, "modifyContainerLocked", action.modifyContainerLocked)))
            }
        }
    }
    if (action.type.isNotBlank() && action.type.trim() !in setOf("MoveTo", "SetItemRoom", "ChangeState", "TransformIntoItem", "ModifyExit", "ModifyContainer", "Message")) {
        issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.actionUnknownType", listOf(indexLabel, action.type.trim())))
    }

    action.preconditions.forEachIndexed { preIndex, precondition ->
        validatePrecondition(
            issues,
            precondition,
            "$indexLabel precondition #${preIndex + 1}",
            roomIds,
            itemIds,
            stateKeys,
            session
        )
    }
}

private fun validatePrecondition(
    issues: MutableList<ValidationIssue>,
    precondition: PreconditionDraft,
    indexLabel: Any,
    roomIds: Set<Int>,
    itemIds: Set<Int>,
    stateKeys: Set<String>,
    session: EditorSession
) {
    when (precondition.type.trim()) {
        "PreconditionState" -> {
            if (precondition.requiredStateKey.isBlank()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionStateKeyMissing", listOf(indexLabel)))
            } else if (!stateKeys.contains(precondition.requiredStateKey)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionStateKeyUnknown", listOf(indexLabel, precondition.requiredStateKey)))
            }
        }
        "PreconditionItem" -> if (!itemIds.contains(precondition.itemId)) {
            issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionItemUnknown", listOf(indexLabel, precondition.itemId)))
        }
        "PreconditionContainer" -> {
            if (!itemIds.contains(precondition.containerItemId)) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionContainerUnknown", listOf(indexLabel, precondition.containerItemId)))
            }
            if (!isOptionalBooleanText(precondition.containerOpen)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidTriStateBoolean", listOf(indexLabel, "containerOpen", precondition.containerOpen)))
            }
            if (!isOptionalBooleanText(precondition.containerLocked)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidTriStateBoolean", listOf(indexLabel, "containerLocked", precondition.containerLocked)))
            }
        }
        "PreconditionExit" -> {
            val room = session.rooms.firstOrNull { it.id == precondition.exitRoomId }
            if (room == null) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionExitRoomUnknown", listOf(indexLabel, precondition.exitRoomId)))
            } else if (room.exits.none { normalizeDirection(it.direction) == normalizeDirection(precondition.exitDirection) }) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionExitUnknown", listOf(indexLabel, precondition.exitDirection, precondition.exitRoomId)))
            }
            if (!isOptionalBooleanText(precondition.exitOpen)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidTriStateBoolean", listOf(indexLabel, "exitOpen", precondition.exitOpen)))
            }
        }
        "PreconditionItemsLocation" -> {
            val required = parseCsvInts(precondition.requiredItemsCsv)
            if (required.isEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionItemsLocationMissingItems", listOf(indexLabel)))
            }
            val unknown = required.filterNot { itemIds.contains(it) }
            if (unknown.isNotEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionItemsLocationUnknownItems", listOf(indexLabel, unknown.joinToString(","))))
            }
            if (!isOptionalIntegerText(precondition.requiredRoomForItems)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidOptionalInteger", listOf(indexLabel, "requiredRoomForItems", precondition.requiredRoomForItems)))
            }
            if (!isOptionalIntegerText(precondition.requiredContainerForItems)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidOptionalInteger", listOf(indexLabel, "requiredContainerForItems", precondition.requiredContainerForItems)))
            }
        }
        "PreconditionPlayer" -> {
            val has = parseCsvInts(precondition.playerHasItemsCsv)
            val not = parseCsvInts(precondition.playerDoesntHaveItemsCsv)
            val unknown = (has + not).filterNot { itemIds.contains(it) }
            if (unknown.isNotEmpty()) {
                issues.add(ValidationIssue(ValidationSeverity.ERROR, "validation.preconditionPlayerUnknownItems", listOf(indexLabel, unknown.joinToString(","))))
            }
            if (!isOptionalIntegerText(precondition.playerLocation)) {
                issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidOptionalInteger", listOf(indexLabel, "playerLocation", precondition.playerLocation)))
            }
        }
    }

    if (precondition.type.trim() == "PreconditionItem") {
        if (!isOptionalIntegerText(precondition.location)) {
            issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidOptionalInteger", listOf(indexLabel, "location", precondition.location)))
        }
        if (!isOptionalBooleanText(precondition.usable)) {
            issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidTriStateBoolean", listOf(indexLabel, "usable", precondition.usable)))
        }
        if (!isOptionalBooleanText(precondition.carriable)) {
            issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionInvalidTriStateBoolean", listOf(indexLabel, "carriable", precondition.carriable)))
        }
    }
    if (precondition.type.isNotBlank() && precondition.type.trim() !in setOf("PreconditionState", "PreconditionItem", "PreconditionContainer", "PreconditionExit", "PreconditionItemsLocation", "PreconditionPlayer")) {
        issues.add(ValidationIssue(ValidationSeverity.WARNING, "validation.preconditionUnknownType", listOf(indexLabel, precondition.type.trim())))
    }
}

private fun parseCsvInts(input: String): List<Int> {
    return input.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .mapNotNull { it.toIntOrNull() }
}

private fun isOptionalBooleanText(value: String): Boolean {
    if (value.isBlank()) {
        return true
    }
    return value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)
}

private fun isOptionalIntegerText(value: String): Boolean {
    if (value.isBlank()) {
        return true
    }
    return value.toIntOrNull() != null
}

@Composable
private fun NameEditor(draft: NameDraft, locale: Locale, nameRequired: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    if (nameRequired) {
        OutlinedTextField(
            value = draft.name,
            onValueChange = { draft.name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.name")) }
        )
        Spacer(modifier = Modifier.height(4.dp))
    }

    Button(onClick = { expanded = !expanded }) {
        Text(
            if (expanded) t(locale, "section.nameAdvancedCollapse")
            else tf(locale, "section.nameAdvanced", if (!nameRequired && draft.name.isNotBlank()) draft.name else "")
        )
    }

    if (expanded) {
        Spacer(modifier = Modifier.height(6.dp))
        if (!nameRequired) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft.name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t(locale, "field.name")) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = draft.definiteName,
            onValueChange = { draft.definiteName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.definiteName")) }
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = draft.indefiniteName,
            onValueChange = { draft.indefiniteName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.indefiniteName")) }
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = draft.aliases,
            onValueChange = { draft.aliases = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.aliases")) }
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = draft.genderKey,
            onValueChange = { draft.genderKey = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(t(locale, "field.genderKey")) }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { draft.isPlural = !draft.isPlural }) {
                Text(tf(locale, "field.isPlural", draft.isPlural))
            }
        }
    }
}

@Composable
private fun TriggerListEditor(
    label: String,
    actions: androidx.compose.runtime.snapshots.SnapshotStateList<ActionDraft>,
    locale: Locale
) {
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = { expanded = !expanded }) {
        Text("$label (${actions.size})")
    }

    if (expanded) {
        Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
            Button(onClick = { actions.add(ActionDraft("Message", "")) }) {
                Text(t(locale, "button.addAction"))
            }
            Spacer(modifier = Modifier.height(4.dp))
            actions.forEachIndexed { index, action ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Button(onClick = { actions.removeAt(index) }) { Text("✕") }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        InlineTriggerActionEditor(action, locale)
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineTriggerActionEditor(action: ActionDraft, locale: Locale) {
    val allowedTypes = listOf("MoveTo", "SetItemRoom", "ChangeState", "TransformIntoItem", "ModifyExit", "ModifyContainer", "Message")
    var typeExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { typeExpanded = true }) {
            Text(action.type.ifBlank { t(locale, "field.actionType") })
        }
        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
            allowedTypes.forEach { at ->
                DropdownMenuItem(text = { Text(at) }, onClick = {
                    action.type = at
                    typeExpanded = false
                })
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = action.description,
        onValueChange = { action.description = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(t(locale, "field.description")) }
    )

    when (action.type.trim()) {
        "ChangeState" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.changedStateKey, onValueChange = { action.changedStateKey = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.changedStateKey")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.newStateValue, onValueChange = { action.newStateValue = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.newStateValue")) })
        }
        "MoveTo" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.moveToRoomId.toString(), onValueChange = { action.moveToRoomId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.moveToRoomId")) })
        }
        "SetItemRoom" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.affectedItemIdsCsv, onValueChange = { action.affectedItemIdsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.affectedItemIdsCsv")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.moveToRoomIdForItems.toString(), onValueChange = { action.moveToRoomIdForItems = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.moveToRoomIdForItems")) })
        }
        "TransformIntoItem" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.affectedItemIdsCsv, onValueChange = { action.affectedItemIdsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.affectedItemIdsCsv")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.transformsIntoItemIdsCsv, onValueChange = { action.transformsIntoItemIdsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.transformsIntoItemIdsCsv")) })
        }
        "ModifyExit" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitRoomId.toString(), onValueChange = { action.modifyExitRoomId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.exitRoomId")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitDirection, onValueChange = { action.modifyExitDirection = normalizeDirection(it) }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.exitDirection")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitOpen, onValueChange = { action.modifyExitOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalOpen")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitLocked, onValueChange = { action.modifyExitLocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalLocked")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitBlocked, onValueChange = { action.modifyExitBlocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalBlocked")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyExitVisible, onValueChange = { action.modifyExitVisible = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalVisible")) })
        }
        "ModifyContainer" -> {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyContainerId.toString(), onValueChange = { action.modifyContainerId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.containerId")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyContainerOpen, onValueChange = { action.modifyContainerOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalOpen")) })
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = action.modifyContainerLocked, onValueChange = { action.modifyContainerLocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.booleanOptionalLocked")) })
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    InlinePreconditionsEditor(action.preconditions, locale)
}

@Composable
private fun InlinePreconditionsEditor(
    preconditions: androidx.compose.runtime.snapshots.SnapshotStateList<PreconditionDraft>,
    locale: Locale
) {
    val allowedTypes = listOf("PreconditionState", "PreconditionItem", "PreconditionContainer", "PreconditionExit", "PreconditionItemsLocation", "PreconditionPlayer")
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = { expanded = !expanded }) {
        Text(t(locale, "section.preconditions") + " (${preconditions.size})")
    }

    if (expanded) {
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = { preconditions.add(PreconditionDraft("PreconditionState", "")) }) {
            Text(t(locale, "button.addPrecondition"))
        }
        preconditions.forEachIndexed { index, precondition ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${index + 1}", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Button(onClick = { preconditions.removeAt(index) }) { Text("✕") }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    var typeExpanded by remember { mutableStateOf(false) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { typeExpanded = true }) {
                            Text(precondition.type.ifBlank { t(locale, "field.preconditionType") })
                        }
                        DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            allowedTypes.forEach { pt ->
                                DropdownMenuItem(text = { Text(pt) }, onClick = {
                                    precondition.type = pt
                                    typeExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = precondition.summary,
                        onValueChange = { precondition.summary = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t(locale, "field.summary")) }
                    )

                    when (precondition.type.trim()) {
                        "PreconditionState" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.requiredStateKey, onValueChange = { precondition.requiredStateKey = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.requiredStateKey")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.requiredStateValuesCsv, onValueChange = { precondition.requiredStateValuesCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.requiredStateValuesCsv")) })
                        }
                        "PreconditionItem" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.itemId.toString(), onValueChange = { precondition.itemId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.itemId")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.location, onValueChange = { precondition.location = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.locationOptional")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.usable, onValueChange = { precondition.usable = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.usableTriState")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.carriable, onValueChange = { precondition.carriable = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.carriableTriState")) })
                        }
                        "PreconditionContainer" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.containerItemId.toString(), onValueChange = { precondition.containerItemId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.containerItemId")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.containerOpen, onValueChange = { precondition.containerOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.openTriState")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.containerLocked, onValueChange = { precondition.containerLocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.lockedTriState")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.containerContainsItemsCsv, onValueChange = { precondition.containerContainsItemsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.containsItemsCsv")) })
                        }
                        "PreconditionExit" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.exitRoomId.toString(), onValueChange = { precondition.exitRoomId = it.toIntOrNull() ?: 0 }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.exitRoomId")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.exitDirection, onValueChange = { precondition.exitDirection = normalizeDirection(it) }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.exitDirection")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.exitOpen, onValueChange = { precondition.exitOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.openTriState")) })
                        }
                        "PreconditionItemsLocation" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.requiredItemsCsv, onValueChange = { precondition.requiredItemsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.requiredItemIdsCsv")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.requiredRoomForItems, onValueChange = { precondition.requiredRoomForItems = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.requiredRoomIdOptional")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.requiredContainerForItems, onValueChange = { precondition.requiredContainerForItems = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.requiredContainerIdOptional")) })
                        }
                        "PreconditionPlayer" -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.playerLocation, onValueChange = { precondition.playerLocation = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.playerLocationOptional")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.playerHasItemsCsv, onValueChange = { precondition.playerHasItemsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.playerHasItemsCsv")) })
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = precondition.playerDoesntHaveItemsCsv, onValueChange = { precondition.playerDoesntHaveItemsCsv = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.playerDoesntHaveItemsCsv")) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatesEditor(session: EditorSession, locale: Locale) {    Column {
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
    val bindingScopes = listOf("Global", "Room", "Item", "Container", "Exit")

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
                            value = action.bindingScope,
                            onValueChange = { action.bindingScope = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.bindingScope")) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (action.bindingScope == "Exit") {
                            OutlinedTextField(
                                value = action.bindingRoomId.toString(),
                                onValueChange = { action.bindingRoomId = it.toIntOrNull() ?: 0 },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(t(locale, "field.bindingRoomId")) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = action.bindingDirection,
                                onValueChange = { action.bindingDirection = normalizeDirection(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(t(locale, "field.bindingExitDirection")) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            OutlinedTextField(
                                value = action.bindingTargetId.toString(),
                                onValueChange = { action.bindingTargetId = it.toIntOrNull() ?: 0 },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(t(locale, "field.bindingTargetId")) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        OutlinedTextField(
                            value = action.description,
                            onValueChange = { action.description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(t(locale, "field.description")) }
                        )

                        when (action.type.trim()) {
                            "ChangeState" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.changedStateKey,
                                    onValueChange = { action.changedStateKey = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.changedStateKey")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.newStateValue,
                                    onValueChange = { action.newStateValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.newStateValue")) }
                                )
                            }
                            "MoveTo" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.moveToRoomId.toString(),
                                    onValueChange = { action.moveToRoomId = it.toIntOrNull() ?: 0 },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.moveToRoomId")) }
                                )
                            }
                            "SetItemRoom" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.affectedItemIdsCsv,
                                    onValueChange = { action.affectedItemIdsCsv = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.affectedItemIdsCsv")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.moveToRoomIdForItems.toString(),
                                    onValueChange = { action.moveToRoomIdForItems = it.toIntOrNull() ?: 0 },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.moveToRoomIdForItems")) }
                                )
                            }
                            "TransformIntoItem" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.affectedItemIdsCsv,
                                    onValueChange = { action.affectedItemIdsCsv = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.affectedItemIdsCsv")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.transformsIntoItemIdsCsv,
                                    onValueChange = { action.transformsIntoItemIdsCsv = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.transformsIntoItemIdsCsv")) }
                                )
                            }
                            "ModifyExit" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.modifyExitRoomId.toString(),
                                    onValueChange = { action.modifyExitRoomId = it.toIntOrNull() ?: 0 },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.exitRoomId")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.modifyExitDirection,
                                    onValueChange = { action.modifyExitDirection = normalizeDirection(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.exitDirection")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyExitOpen, onValueChange = { action.modifyExitOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.openTriState")) })
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyExitLocked, onValueChange = { action.modifyExitLocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.lockedTriState")) })
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyExitBlocked, onValueChange = { action.modifyExitBlocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.blockedTriState")) })
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyExitVisible, onValueChange = { action.modifyExitVisible = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.visibleTriState")) })
                            }
                            "ModifyContainer" -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = action.modifyContainerId.toString(),
                                    onValueChange = { action.modifyContainerId = it.toIntOrNull() ?: 0 },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(t(locale, "field.containerId")) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyContainerOpen, onValueChange = { action.modifyContainerOpen = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.openTriState")) })
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = action.modifyContainerLocked, onValueChange = { action.modifyContainerLocked = it }, modifier = Modifier.fillMaxWidth(), label = { Text(t(locale, "field.lockedTriState")) })
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        InlinePreconditionsEditor(action.preconditions, locale)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tf(locale, "ui.validationKey", "types=${allowedTypes.joinToString()} | scopes=${bindingScopes.joinToString()}"))
                    }
                }
            }
        }
    }
}

private fun t(locale: Locale, key: String): String = Messages.text(locale, key)

private fun tf(locale: Locale, key: String, vararg args: Any): String =
    Messages.format(locale, key, *args)
