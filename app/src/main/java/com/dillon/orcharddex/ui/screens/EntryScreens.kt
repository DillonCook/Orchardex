package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.ui.components.DateField
import com.dillon.orcharddex.ui.components.LocalPhotoStrip
import com.dillon.orcharddex.ui.components.PhotoAddCard
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.components.TimeField
import com.dillon.orcharddex.ui.viewmodel.EventFormViewModel
import com.dillon.orcharddex.ui.viewmodel.HarvestFormViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderFormViewModel

@Composable
fun EventFormScreen(
    viewModel: EventFormViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.state
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val activeTrees = remember(trees) { trees.filter { it.status == TreeStatus.ACTIVE } }
    val selectedTrees = remember(trees, state.selectedTreeIds) {
        trees.filter { it.id in state.selectedTreeIds }
    }
    var selectionDialogVisible by rememberSaveable { mutableStateOf(false) }
    var plantSearch by rememberSaveable { mutableStateOf("") }
    val visibleTrees = remember(trees, plantSearch) {
        val query = plantSearch.trim().lowercase()
        if (query.isBlank()) {
            trees
        } else {
            trees.filter { tree -> tree.matchesPlantSearch(query) }
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.update { copy(photoUri = uri) } }

    if (selectionDialogVisible) {
        AlertDialog(
            onDismissRequest = { selectionDialogVisible = false },
            title = { Text("Choose plants") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = plantSearch,
                        onValueChange = { plantSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search plants") }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${state.selectedTreeIds.size} selected")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.selectTreeIds(visibleTrees.map(TreeEntity::id)) }) {
                                Text("Select visible")
                            }
                            TextButton(onClick = viewModel::clearTreeSelection) {
                                Text("Clear")
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleTrees, key = TreeEntity::id) { tree ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleTreeSelection(tree.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = tree.id in state.selectedTreeIds,
                                        onCheckedChange = { _ -> viewModel.toggleTreeSelection(tree.id) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tree.displayName())
                                        Text(
                                            tree.selectorSubtitle(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectionDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }

    EntryFormLayout(title = "Add event") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !state.applyToAllActive,
                onClick = { viewModel.setApplyToAllActive(false) },
                label = { Text("Selected plants") }
            )
            FilterChip(
                selected = state.applyToAllActive,
                onClick = { viewModel.setApplyToAllActive(true) },
                label = { Text("All active plants") }
            )
        }
        if (state.applyToAllActive) {
            Text("${activeTrees.size} active plants will receive this event.")
        } else {
            OutlinedButton(
                onClick = { selectionDialogVisible = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (selectedTrees.isEmpty()) {
                        "Choose plants"
                    } else {
                        "Edit ${selectedTrees.size} selected plants"
                    }
                )
            }
            if (selectedTrees.isEmpty()) {
                Text("No plants selected yet.")
            } else {
                selectedTrees.take(4).forEach { tree ->
                    Text(tree.selectorLabel(), style = MaterialTheme.typography.bodySmall)
                }
                if (selectedTrees.size > 4) {
                    Text("+${selectedTrees.size - 4} more plants", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Event type",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventType.entries.forEach { eventType ->
                    FilterChip(
                        selected = state.eventType == eventType,
                        onClick = { viewModel.update { copy(eventType = eventType) } },
                        label = { Text(eventType.eventLabel()) }
                    )
                }
            }
        }
        DateField(
            label = "Event date",
            value = state.eventDate,
            onDateSelected = { viewModel.update { copy(eventDate = it) } }
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.update { copy(notes = it) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes") },
            minLines = 4
        )
        OutlinedTextField(
            value = state.cost,
            onValueChange = { viewModel.update { copy(cost = it) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cost") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.quantityValue,
                onValueChange = { viewModel.update { copy(quantityValue = it) } },
                modifier = Modifier.weight(1f),
                label = { Text("Qty") }
            )
            OutlinedTextField(
                value = state.quantityUnit,
                onValueChange = { viewModel.update { copy(quantityUnit = it) } },
                modifier = Modifier.weight(1f),
                label = { Text("Unit") }
            )
        }
        PhotoAddCard(
            onClick = {
                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
        state.photoUri?.let {
            LocalPhotoStrip(
                existingPaths = emptyList(),
                newUris = listOf(it),
                onRemoveNew = { viewModel.update { copy(photoUri = null) } }
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = { viewModel.save(onSaved) },
            confirmLabel = if (state.applyToAllActive || state.selectedTreeIds.size > 1) {
                "Apply event"
            } else {
                "Save event"
            }
        )
    }
}

@Composable
fun HarvestFormScreen(
    viewModel: HarvestFormViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.state
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.update { copy(photoUri = uri) } }

    EntryFormLayout(title = "Add harvest") {
        SelectionField(
            label = "Tree",
            value = trees.selectedTreeLabel(state.treeId),
            options = trees.treeLabels(),
            onSelected = { label ->
                viewModel.update { copy(treeId = trees.find { it.selectorLabel() == label }?.id) }
            }
        )
        DateField(
            label = "Harvest date",
            value = state.harvestDate,
            onDateSelected = { viewModel.update { copy(harvestDate = it) } }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.quantityValue,
                onValueChange = { viewModel.update { copy(quantityValue = it) } },
                modifier = Modifier
                    .weight(1f)
                    .testTag("harvest_quantity"),
                label = { Text("Quantity") }
            )
            OutlinedTextField(
                value = state.quantityUnit,
                onValueChange = { viewModel.update { copy(quantityUnit = it) } },
                modifier = Modifier.weight(1f),
                label = { Text("Unit") }
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { rating ->
                FilterChip(
                    selected = state.qualityRating == rating,
                    onClick = { viewModel.update { copy(qualityRating = rating) } },
                    label = { Text("$rating star") }
                )
            }
        }
        FilterChip(
            selected = state.firstFruit,
            onClick = { viewModel.update { copy(firstFruit = !state.firstFruit) } },
            label = { Text("First fruit") }
        )
        FilterChip(
            selected = state.verified,
            onClick = { viewModel.update { copy(verified = !state.verified) } },
            label = { Text("Harvest received / verified") }
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.update { copy(notes = it) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes") },
            minLines = 4
        )
        PhotoAddCard(
            onClick = {
                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
        state.photoUri?.let {
            LocalPhotoStrip(
                existingPaths = emptyList(),
                newUris = listOf(it),
                onRemoveNew = { viewModel.update { copy(photoUri = null) } }
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = { viewModel.save(onSaved) },
            confirmLabel = "Save harvest",
            confirmModifier = Modifier.testTag("harvest_save")
        )
    }
}

@Composable
fun ReminderFormScreen(
    viewModel: ReminderFormViewModel,
    requestNotificationPermission: () -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.state
    val trees by viewModel.trees.collectAsStateWithLifecycle()

    EntryFormLayout(title = if (state.id == null) "Add reminder" else "Edit reminder") {
        SelectionField(
            label = "Tree (optional)",
            value = trees.selectedTreeLabel(state.treeId, fallback = "Any tree"),
            options = listOf("Any tree") + trees.treeLabels(),
            onSelected = { label ->
                val treeId = trees.find { it.selectorLabel() == label }?.id
                viewModel.update { copy(treeId = treeId) }
            }
        )
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.update { copy(title = it) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") }
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.update { copy(notes = it) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes") },
            minLines = 3
        )
        DateField(
            label = "Due date",
            value = state.dueDate,
            onDateSelected = { viewModel.update { copy(dueDate = it) } }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Include time")
            Switch(
                checked = state.hasTime,
                onCheckedChange = { checked -> viewModel.update { copy(hasTime = checked) } }
            )
        }
        if (state.hasTime) {
            TimeField(
                label = "Due time",
                value = state.dueTime,
                onTimeSelected = { viewModel.update { copy(dueTime = it) } }
            )
        }
        SelectionField(
            label = "Recurrence",
            value = state.recurrenceType.reminderLabel(),
            options = RecurrenceType.entries.map(RecurrenceType::reminderLabel),
            onSelected = { label ->
                viewModel.update {
                    copy(recurrenceType = RecurrenceType.entries.first { it.reminderLabel() == label })
                }
            }
        )
        if (state.recurrenceType == RecurrenceType.EVERY_X_DAYS) {
            OutlinedTextField(
                value = state.recurrenceIntervalDays,
                onValueChange = { viewModel.update { copy(recurrenceIntervalDays = it) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Every X days") }
            )
        }
        SelectionField(
            label = "Lead time",
            value = state.leadTimeMode.leadTimeLabel(),
            options = LeadTimeMode.entries.map(LeadTimeMode::leadTimeLabel),
            onSelected = { label ->
                viewModel.update {
                    copy(leadTimeMode = LeadTimeMode.entries.first { it.leadTimeLabel() == label })
                }
            }
        )
        if (state.leadTimeMode == LeadTimeMode.CUSTOM_HOURS) {
            OutlinedTextField(
                value = state.customLeadTimeHours,
                onValueChange = { viewModel.update { copy(customLeadTimeHours = it) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Hours before") }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reminder enabled")
            Switch(
                checked = state.enabled,
                onCheckedChange = { checked -> viewModel.update { copy(enabled = checked) } }
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = {
                if (state.enabled) requestNotificationPermission()
                viewModel.save(onSaved)
            },
            confirmLabel = "Save reminder"
        )
    }
}

@Composable
fun PrivacyScreen() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Privacy policy") {
                Text("Last updated: Mar 18, 2026")
                Text("OrcharDex is an offline-first Android app for orchard records, reminders, photos, harvest logs, and manual backups.")
            }
        }
        item {
            SectionCard("Summary") {
                Text("OrcharDex stores orchard records only on this device.")
                Text("No account or login is required.")
                Text("No analytics, ads, crash reporting, or server sync are included.")
                Text("OrcharDex does not sell, share, or transmit your orchard data to the developer or third parties.")
            }
        }
        item {
            SectionCard("Data OrcharDex stores") {
                Text("Plant records, notes, tags, orchard details, event logs, harvest logs, reminders, and app settings.")
                Text("Photos you choose are copied into app-private storage inside OrcharDex.")
                Text("Manual backup files are created or imported only when you choose to export or import them.")
            }
        }
        item {
            SectionCard("How data is used") {
                Text("Your data is used only to power app features on your device.")
                Text("Notifications are generated locally for reminders you create.")
                Text("Selected photos are attached to your orchard records and stored inside the app sandbox.")
                Text("Backups are written only to the location you choose with the system file picker.")
            }
        }
        item {
            SectionCard("Permissions") {
                Text("Notification permission is requested only when you enable reminders on Android 13 and newer.")
                Text("OrcharDex uses the Android photo picker and does not request broad media-library permissions.")
            }
        }
        item {
            SectionCard("Deletion and control") {
                Text("You can delete local app data at any time from Settings > Data > Clear all data or by uninstalling the app.")
                Text("Any backup files you export remain under your control and can be deleted separately.")
            }
        }
        item {
            SectionCard("Contact") {
                Text("For privacy questions, use the contact method listed in the OrcharDex Google Play listing.")
            }
        }
    }
}

@Composable
private fun EntryFormLayout(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(title, content = content)
        }
    }
}

@Composable
private fun FormActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    confirmModifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text("Cancel")
        }
        Button(onClick = onConfirm, modifier = Modifier.weight(1f).then(confirmModifier)) {
            Text(confirmLabel)
        }
    }
}

private fun EventType.eventLabel(): String = name.lowercase().replace("_", " ")
    .replaceFirstChar(Char::uppercase)

private fun RecurrenceType.reminderLabel(): String = when (this) {
    RecurrenceType.NONE -> "None"
    RecurrenceType.DAILY -> "Daily"
    RecurrenceType.WEEKLY -> "Weekly"
    RecurrenceType.MONTHLY -> "Monthly"
    RecurrenceType.EVERY_X_DAYS -> "Every X days"
}

private fun LeadTimeMode.leadTimeLabel(): String = when (this) {
    LeadTimeMode.SAME_DAY -> "Same day"
    LeadTimeMode.ONE_DAY_BEFORE -> "1 day before"
    LeadTimeMode.CUSTOM_HOURS -> "Custom hours"
}

private fun List<TreeEntity>.treeLabels(): List<String> = map(TreeEntity::selectorLabel)

private fun List<TreeEntity>.selectedTreeLabel(treeId: String?, fallback: String = ""): String =
    firstOrNull { it.id == treeId }?.selectorLabel() ?: fallback

private fun TreeEntity.selectorLabel(): String = buildString {
    append(displayName())
    if (sectionName.isNotBlank()) {
        append(" - ")
        append(sectionName)
    }
}

private fun TreeEntity.selectorSubtitle(): String = buildString {
    append(species)
    if (cultivar.isNotBlank()) append(" - $cultivar")
    if (sectionName.isNotBlank()) {
        append(" - ")
        append(sectionName)
    }
    if (status != TreeStatus.ACTIVE) {
        append(" - ${status.name.lowercase()}")
    }
}

private fun TreeEntity.matchesPlantSearch(query: String): Boolean = listOf(
    displayName(),
    species,
    cultivar,
    sectionName,
    notes,
    tags
).any { value -> value.lowercase().contains(query) }
