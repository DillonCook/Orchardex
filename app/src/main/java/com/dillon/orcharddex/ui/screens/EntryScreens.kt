package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.RecurrenceType
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
import java.io.File

@Composable
fun EventFormScreen(
    viewModel: EventFormViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.state
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.update { copy(photoUri = uri) } }

    EntryFormLayout(title = "Log event") {
        SelectionField(
            label = "Tree",
            value = trees.selectedTreeLabel(state.treeId),
            options = trees.treeLabels(),
            onSelected = { label ->
                viewModel.update { copy(treeId = trees.find { it.selectorLabel() == label }?.id) }
            }
        )
        SelectionField(
            label = "Event type",
            value = state.eventType.eventLabel(),
            options = EventType.entries.map(EventType::eventLabel),
            onSelected = { label ->
                viewModel.update { copy(eventType = EventType.entries.first { it.eventLabel() == label }) }
            }
        )
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
            LocalPhotoStrip(existingPaths = emptyList(), newUris = listOf(it), onRemoveNew = { viewModel.update { copy(photoUri = null) } })
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = { viewModel.save(onSaved) },
            confirmLabel = "Save event"
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

    EntryFormLayout(title = "Log harvest") {
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
                    label = { Text("$rating★") }
                )
            }
        }
        FilterChip(
            selected = state.firstFruit,
            onClick = { viewModel.update { copy(firstFruit = !state.firstFruit) } },
            label = { Text("First fruit") }
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
            LocalPhotoStrip(existingPaths = emptyList(), newUris = listOf(it), onRemoveNew = { viewModel.update { copy(photoUri = null) } })
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
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            SectionCard("Privacy") {
                Text("OrchardDex stores orchard records only on this device.")
                Text("No account or login is required.")
                Text("No analytics, ads, crash reporting, or server sync are included.")
                Text("Backups are manual files that you control with the system file picker.")
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

private fun TreeEntity.selectorLabel(): String = "${displayName()} • ${orchardName.ifBlank { "No orchard" }}"
