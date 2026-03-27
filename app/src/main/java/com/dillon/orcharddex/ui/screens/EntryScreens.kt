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
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.ActivityKind
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
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.EventFormViewModel
import com.dillon.orcharddex.ui.viewmodel.HarvestFormViewModel
import com.dillon.orcharddex.ui.viewmodel.LogFormViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderFormViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderFormState
import com.dillon.orcharddex.ui.viewmodel.ReminderTargetMode

@Composable
fun LogFormScreen(
    viewModel: LogFormViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.state
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val harvests by viewModel.harvests.collectAsStateWithLifecycle()
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
    val recentHarvests = remember(harvests, state.selectedTreeIds) {
        harvests
            .filter { harvest -> harvest.treeId in state.selectedTreeIds }
            .sortedByDescending(HarvestEntity::harvestDate)
    }
    val recentValueHarvests = remember(recentHarvests) { recentHarvests.take(3) }
    val unitSuggestions = remember(recentHarvests, state.quantityUnit, state.selectedTreeIds, state.kind) {
        if (state.kind != ActivityKind.HARVEST || state.selectedTreeIds.isEmpty()) {
            emptyList()
        } else {
            harvestUnitSuggestions(recentHarvests, state.quantityUnit)
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }
    val confirmLabel = when (state.kind) {
        ActivityKind.EVENT -> if (state.applyToAllActive || state.selectedTreeIds.size > 1) {
            "Apply event"
        } else {
            "Save event"
        }
        ActivityKind.HARVEST -> if (state.selectedTreeIds.size > 1) {
            "Apply harvest"
        } else {
            "Save harvest"
        }
    }
    val confirmModifier = if (state.kind == ActivityKind.HARVEST) {
        Modifier.testTag("harvest_save")
    } else {
        Modifier
    }

    if (selectionDialogVisible) {
        PlantSelectionDialog(
            search = plantSearch,
            onSearchChange = { plantSearch = it },
            visibleTrees = visibleTrees,
            selectedTreeIds = state.selectedTreeIds,
            onToggleTree = viewModel::toggleTreeSelection,
            onToggleSpeciesGroup = viewModel::toggleTreeGroupSelection,
            onSelectVisibleTrees = { viewModel.selectTreeIds(visibleTrees.map(TreeEntity::id)) },
            onClear = viewModel::clearTreeSelection,
            onDismiss = { selectionDialogVisible = false }
        )
    }

    EntryFormLayout(title = "Details") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Type",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.kind == ActivityKind.EVENT,
                    onClick = { viewModel.setKind(ActivityKind.EVENT) },
                    label = { Text("Event") }
                )
                FilterChip(
                    selected = state.kind == ActivityKind.HARVEST,
                    onClick = { viewModel.setKind(ActivityKind.HARVEST) },
                    label = { Text("Harvest") }
                )
            }
        }

        if (state.kind == ActivityKind.EVENT) {
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
        }

        if (state.kind == ActivityKind.EVENT && state.applyToAllActive) {
            Text("${activeTrees.size} active plants will receive this event.")
        } else {
            OutlinedButton(
                onClick = { selectionDialogVisible = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (selectedTrees.isEmpty()) {
                        "Choose plants or species"
                    } else {
                        "Edit ${selectedTrees.size} selected plants"
                    }
                )
            }
            if (selectedTrees.isEmpty()) {
                Text("No plants selected yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                selectedTrees.take(4).forEach { tree ->
                    Text(tree.selectorLabel(), style = MaterialTheme.typography.bodySmall)
                }
                if (selectedTrees.size > 4) {
                    Text("+${selectedTrees.size - 4} more plants", style = MaterialTheme.typography.bodySmall)
                }
                if (state.kind == ActivityKind.HARVEST && selectedTrees.size > 1) {
                    Text(
                        text = "This harvest entry will be copied to ${selectedTrees.size} plants.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (state.kind == ActivityKind.EVENT) {
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
        }

        DateField(
            label = if (state.kind == ActivityKind.EVENT) "Event date" else "Harvest date",
            value = state.logDate,
            onDateSelected = { viewModel.update { copy(logDate = it) } }
        )

        if (state.kind == ActivityKind.HARVEST) {
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
            if (state.selectedTreeIds.isNotEmpty()) {
                if (recentValueHarvests.isEmpty()) {
                    Text(
                        text = if (selectedTrees.size > 1) {
                            "Log the first harvest for these plants to unlock quick-fill values here."
                        } else {
                            "Log the first harvest for this plant to unlock quick-fill values here."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Recent harvest values",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = if (selectedTrees.size > 1) {
                                "Tap a past harvest from this selection to reuse quantity, unit, and quality."
                            } else {
                                "Tap a past harvest to reuse quantity, unit, and quality."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentValueHarvests.forEach { harvest ->
                                FilterChip(
                                    selected = state.quantityValue == harvest.quantityValue.displayAmount() &&
                                        state.quantityUnit.equals(harvest.quantityUnit, ignoreCase = true),
                                    onClick = { viewModel.applyLastHarvest(harvest) },
                                    label = {
                                        Text(
                                            "${harvest.quantityValue.displayAmount()} ${harvest.quantityUnit} - ${harvest.harvestDate.toDateLabel()}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = if (selectedTrees.size > 1) {
                    "First fruit is tagged automatically for each plant the first time a harvest is logged."
                } else {
                    "First fruit is tagged automatically the first time a harvest is logged for this plant."
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (unitSuggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Common units",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unitSuggestions.forEach { unit ->
                            FilterChip(
                                selected = state.quantityUnit.equals(unit, ignoreCase = true),
                                onClick = { viewModel.update { copy(quantityUnit = unit) } },
                                label = { Text(unit) }
                            )
                        }
                    }
                }
            }
            Text(
                text = "Harvest quality",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { rating ->
                    FilterChip(
                        selected = state.qualityRating == rating,
                        onClick = { viewModel.update { copy(qualityRating = rating) } },
                        label = { Text("$rating star") }
                    )
                }
            }
        } else {
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
        }

        if (state.kind == ActivityKind.HARVEST) {
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.update { copy(notes = it) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 4
            )
        }

        PhotoAddCard(
            onClick = {
                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
        if (state.photoUris.isNotEmpty()) {
            LocalPhotoStrip(
                existingPaths = emptyList(),
                newUris = state.photoUris,
                onRemoveNew = viewModel::removePhoto
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = { viewModel.save(onSaved) },
            confirmLabel = confirmLabel,
            confirmModifier = confirmModifier
        )
    }
}

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
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }

    if (selectionDialogVisible) {
        PlantSelectionDialog(
            search = plantSearch,
            onSearchChange = { plantSearch = it },
            visibleTrees = visibleTrees,
            selectedTreeIds = state.selectedTreeIds,
            onToggleTree = viewModel::toggleTreeSelection,
            onToggleSpeciesGroup = viewModel::toggleTreeGroupSelection,
            onSelectVisibleTrees = { viewModel.selectTreeIds(visibleTrees.map(TreeEntity::id)) },
            onClear = viewModel::clearTreeSelection,
            onDismiss = { selectionDialogVisible = false }
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
                        "Choose plants or species"
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
        if (state.photoUris.isNotEmpty()) {
            LocalPhotoStrip(
                existingPaths = emptyList(),
                newUris = state.photoUris,
                onRemoveNew = viewModel::removePhoto
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
    val harvests by viewModel.harvests.collectAsStateWithLifecycle()
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
    val recentHarvests = remember(harvests, state.selectedTreeIds) {
        harvests
            .filter { harvest -> harvest.treeId in state.selectedTreeIds }
            .sortedByDescending(HarvestEntity::harvestDate)
    }
    val recentValueHarvests = remember(recentHarvests) { recentHarvests.take(3) }
    val unitSuggestions = remember(recentHarvests, state.quantityUnit, state.selectedTreeIds) {
        if (state.selectedTreeIds.isEmpty()) {
            emptyList()
        } else {
            harvestUnitSuggestions(recentHarvests, state.quantityUnit)
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }

    if (selectionDialogVisible) {
        PlantSelectionDialog(
            search = plantSearch,
            onSearchChange = { plantSearch = it },
            visibleTrees = visibleTrees,
            selectedTreeIds = state.selectedTreeIds,
            onToggleTree = viewModel::toggleTreeSelection,
            onToggleSpeciesGroup = viewModel::toggleTreeGroupSelection,
            onSelectVisibleTrees = { viewModel.selectTreeIds(visibleTrees.map(TreeEntity::id)) },
            onClear = viewModel::clearTreeSelection,
            onDismiss = { selectionDialogVisible = false }
        )
    }

    EntryFormLayout(title = "Add harvest") {
        OutlinedButton(
            onClick = { selectionDialogVisible = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (selectedTrees.isEmpty()) {
                    "Choose plants or species"
                } else {
                    "Edit ${selectedTrees.size} selected plants"
                }
            )
        }
        if (selectedTrees.isEmpty()) {
            Text("No plants selected yet.", style = MaterialTheme.typography.bodySmall)
        } else {
            selectedTrees.take(4).forEach { tree ->
                Text(tree.selectorLabel(), style = MaterialTheme.typography.bodySmall)
            }
            if (selectedTrees.size > 4) {
                Text("+${selectedTrees.size - 4} more plants", style = MaterialTheme.typography.bodySmall)
            }
            if (selectedTrees.size > 1) {
                Text(
                    text = "This harvest entry will be copied to ${selectedTrees.size} plants.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
        if (state.selectedTreeIds.isNotEmpty()) {
            if (recentValueHarvests.isEmpty()) {
                Text(
                    text = if (selectedTrees.size > 1) {
                        "Log the first harvest for these plants to unlock quick-fill values here."
                    } else {
                        "Log the first harvest for this plant to unlock quick-fill values here."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recent harvest values",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = if (selectedTrees.size > 1) {
                            "Tap a past harvest from this selection to reuse quantity, unit, and quality."
                        } else {
                            "Tap a past harvest to reuse quantity, unit, and quality."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentValueHarvests.forEach { harvest ->
                            FilterChip(
                                selected = state.quantityValue == harvest.quantityValue.displayAmount() &&
                                    state.quantityUnit.equals(harvest.quantityUnit, ignoreCase = true),
                                onClick = { viewModel.applyLastHarvest(harvest) },
                                label = {
                                    Text(
                                        "${harvest.quantityValue.displayAmount()} ${harvest.quantityUnit} - ${harvest.harvestDate.toDateLabel()}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = if (selectedTrees.size > 1) {
                "First fruit is tagged automatically for each plant the first time a harvest is logged."
            } else {
                "First fruit is tagged automatically the first time a harvest is logged for this plant."
            },
            style = MaterialTheme.typography.bodySmall
        )
        if (unitSuggestions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Common units",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unitSuggestions.forEach { unit ->
                        FilterChip(
                            selected = state.quantityUnit.equals(unit, ignoreCase = true),
                            onClick = { viewModel.update { copy(quantityUnit = unit) } },
                            label = { Text(unit) }
                        )
                    }
                }
            }
        }
        Text(
            text = "Harvest quality",
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { rating ->
                FilterChip(
                    selected = state.qualityRating == rating,
                    onClick = { viewModel.update { copy(qualityRating = rating) } },
                    label = { Text("$rating star") }
                )
            }
        }
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
        if (state.photoUris.isNotEmpty()) {
            LocalPhotoStrip(
                existingPaths = emptyList(),
                newUris = state.photoUris,
                onRemoveNew = viewModel::removePhoto
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormActions(
            onCancel = onCancel,
            onConfirm = { viewModel.save(onSaved) },
            confirmLabel = if (state.selectedTreeIds.size > 1) "Apply harvest" else "Save harvest",
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
    val activeTrees = remember(trees) { trees.filter { it.status == TreeStatus.ACTIVE } }
    val selectedTrees = remember(trees, state.selectedTreeIds) {
        trees.filter { it.id in state.selectedTreeIds }
    }
    val focusTree = remember(trees, state.treeId, selectedTrees) {
        when {
            state.treeId != null -> trees.firstOrNull { it.id == state.treeId }
            selectedTrees.isEmpty() -> null
            selectedTrees.size == 1 -> selectedTrees.first()
            selectedTrees.sameSpeciesSelection() -> selectedTrees.first()
            else -> null
        }
    }
    val careTemplates = remember(focusTree) { careTemplatesFor(focusTree) }
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

    if (selectionDialogVisible && state.id == null) {
        PlantSelectionDialog(
            search = plantSearch,
            onSearchChange = { plantSearch = it },
            visibleTrees = visibleTrees,
            selectedTreeIds = state.selectedTreeIds,
            onToggleTree = viewModel::toggleTreeSelection,
            onToggleSpeciesGroup = viewModel::toggleTreeGroupSelection,
            onSelectVisibleTrees = { viewModel.selectTreeIds(visibleTrees.map(TreeEntity::id)) },
            onClear = viewModel::clearTreeSelection,
            onDismiss = { selectionDialogVisible = false }
        )
    }

    EntryFormLayout(title = if (state.id == null) "Add reminder" else "Edit reminder") {
        if (state.id == null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Reminder target",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.targetMode == ReminderTargetMode.GENERAL,
                        onClick = { viewModel.setTargetMode(ReminderTargetMode.GENERAL) },
                        label = { Text("General orchard") }
                    )
                    FilterChip(
                        selected = state.targetMode == ReminderTargetMode.SELECTED,
                        onClick = { viewModel.setTargetMode(ReminderTargetMode.SELECTED) },
                        label = { Text("Selected plants") }
                    )
                    FilterChip(
                        selected = state.targetMode == ReminderTargetMode.ALL_ACTIVE,
                        onClick = { viewModel.setTargetMode(ReminderTargetMode.ALL_ACTIVE) },
                        label = { Text("All active plants") }
                    )
                }
            }
            when (state.targetMode) {
                ReminderTargetMode.GENERAL -> {
                    Text(
                        text = "This reminder is not tied to a single plant and will stay orchard-wide.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                ReminderTargetMode.SELECTED -> {
                    OutlinedButton(
                        onClick = { selectionDialogVisible = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedTrees.isEmpty()) {
                                "Choose plants or species"
                            } else {
                                "Edit ${selectedTrees.size} selected plants"
                            }
                        )
                    }
                    if (selectedTrees.isEmpty()) {
                        Text("No plants selected yet.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        selectedTrees.take(4).forEach { tree ->
                            Text(tree.selectorLabel(), style = MaterialTheme.typography.bodySmall)
                        }
                        if (selectedTrees.size > 4) {
                            Text(
                                text = "+${selectedTrees.size - 4} more plants",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                ReminderTargetMode.ALL_ACTIVE -> {
                    Text(
                        text = "${activeTrees.size} active plants will receive this reminder.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            SelectionField(
                label = "Tree (optional)",
                value = trees.selectedTreeLabel(state.treeId, fallback = "Any tree"),
                options = listOf("Any tree") + trees.treeLabels(),
                onSelected = { label ->
                    val treeId = trees.find { it.selectorLabel() == label }?.id
                    viewModel.update { copy(treeId = treeId) }
                }
            )
        }
        if (careTemplates.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Care templates",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Apply a starter title, notes, and cadence, then fine-tune it below.",
                    style = MaterialTheme.typography.bodySmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    careTemplates.forEach { template ->
                        FilterChip(
                            selected = state.title == template.title,
                            onClick = { viewModel.update { applyTemplate(template) } },
                            label = { Text(template.label) }
                        )
                    }
                }
            }
        }
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

private data class SpeciesSelectionGroup(
    val id: String,
    val species: String,
    val treeIds: List<String>,
    val subtitle: String
)

@Composable
private fun PlantSelectionDialog(
    search: String,
    onSearchChange: (String) -> Unit,
    visibleTrees: List<TreeEntity>,
    selectedTreeIds: Set<String>,
    onToggleTree: (String) -> Unit,
    onToggleSpeciesGroup: (Collection<String>) -> Unit,
    onSelectVisibleTrees: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val speciesGroups = remember(visibleTrees) { visibleTrees.speciesSelectionGroups() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose plants or species") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search plants or species") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedTreeIds.size} selected")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onSelectVisibleTrees) {
                            Text("Select visible plants")
                        }
                        TextButton(onClick = onClear) {
                            Text("Clear")
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (speciesGroups.isNotEmpty()) {
                        item {
                            Text("Species", style = MaterialTheme.typography.labelLarge)
                        }
                        items(speciesGroups, key = SpeciesSelectionGroup::id) { group ->
                            val selectedCount = group.treeIds.count { it in selectedTreeIds }
                            val toggleState = when {
                                selectedCount == 0 -> ToggleableState.Off
                                selectedCount == group.treeIds.size -> ToggleableState.On
                                else -> ToggleableState.Indeterminate
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSpeciesGroup(group.treeIds) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TriStateCheckbox(
                                        state = toggleState,
                                        onClick = { onToggleSpeciesGroup(group.treeIds) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(group.species)
                                        Text(
                                            group.subtitle,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (visibleTrees.isNotEmpty()) {
                        item {
                            Text("Plants", style = MaterialTheme.typography.labelLarge)
                        }
                        items(visibleTrees, key = TreeEntity::id) { tree ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleTree(tree.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = tree.id in selectedTreeIds,
                                        onCheckedChange = { _ -> onToggleTree(tree.id) }
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
                    if (speciesGroups.isEmpty() && visibleTrees.isEmpty()) {
                        item {
                            Text(
                                text = "No matching plants.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
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

private fun List<TreeEntity>.speciesSelectionGroups(): List<SpeciesSelectionGroup> =
    filter { it.species.isNotBlank() }
        .groupBy { it.species.selectionKey() }
        .values
        .sortedBy { group -> group.first().species.lowercase() }
        .map { group ->
            val cultivars = group
                .map(TreeEntity::cultivar)
                .filter(String::isNotBlank)
                .distinctBy(String::lowercase)
                .sortedBy(String::lowercase)
            SpeciesSelectionGroup(
                id = group.first().species.selectionKey(),
                species = group.first().species,
                treeIds = group.map(TreeEntity::id),
                subtitle = buildString {
                    append("${group.size} plant")
                    if (group.size != 1) append("s")
                    if (cultivars.isNotEmpty()) {
                        append(" - ")
                        append(cultivars.take(2).joinToString(", "))
                        if (cultivars.size > 2) {
                            append(" +${cultivars.size - 2} more")
                        }
                    }
                }
            )
        }

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

private fun List<TreeEntity>.sameSpeciesSelection(): Boolean =
    map(TreeEntity::species)
        .filter(String::isNotBlank)
        .map(String::selectionKey)
        .distinct()
        .size == 1

private fun String.selectionKey(): String = trim().lowercase()

private data class CareTemplate(
    val label: String,
    val title: String,
    val notes: String,
    val recurrenceType: RecurrenceType,
    val recurrenceIntervalDays: Int? = null,
    val leadTimeMode: LeadTimeMode = LeadTimeMode.SAME_DAY,
    val customLeadTimeHours: Int? = null
)

private fun ReminderFormState.applyTemplate(template: CareTemplate): ReminderFormState = copy(
    title = template.title,
    notes = template.notes,
    recurrenceType = template.recurrenceType,
    recurrenceIntervalDays = template.recurrenceIntervalDays?.toString() ?: recurrenceIntervalDays,
    leadTimeMode = template.leadTimeMode,
    customLeadTimeHours = template.customLeadTimeHours?.toString() ?: customLeadTimeHours,
    enabled = true
)

private fun careTemplatesFor(tree: TreeEntity?): List<CareTemplate> {
    val plantLabel = tree?.displayName()?.lowercase() ?: "the orchard"
    val bloomLabel = tree?.species?.ifBlank { null } ?: "this planting"
    return buildList {
        add(
            CareTemplate(
                label = "Fertilize",
                title = "Fertilize",
                notes = "Feed $plantLabel and log the product or rate used.",
                recurrenceType = RecurrenceType.MONTHLY,
                leadTimeMode = LeadTimeMode.ONE_DAY_BEFORE
            )
        )
        add(
            CareTemplate(
                label = "Pest check",
                title = "Pest check",
                notes = "Inspect $plantLabel for pests, damage, and disease pressure.",
                recurrenceType = RecurrenceType.WEEKLY
            )
        )
        add(
            CareTemplate(
                label = "Irrigation",
                title = "Irrigation check",
                notes = "Check soil moisture and irrigation coverage for $plantLabel.",
                recurrenceType = RecurrenceType.EVERY_X_DAYS,
                recurrenceIntervalDays = 3,
                leadTimeMode = LeadTimeMode.CUSTOM_HOURS,
                customLeadTimeHours = 12
            )
        )
        add(
            CareTemplate(
                label = "Prune",
                title = "Prune",
                notes = "Review $plantLabel for cleanup cuts, shaping, or thinning.",
                recurrenceType = RecurrenceType.NONE,
                leadTimeMode = LeadTimeMode.ONE_DAY_BEFORE
            )
        )
        add(
            CareTemplate(
                label = "Bloom watch",
                title = "Bloom watch",
                notes = "Watch $bloomLabel bloom progress and note pollination conditions.",
                recurrenceType = RecurrenceType.WEEKLY
            )
        )
        add(
            CareTemplate(
                label = "Harvest check",
                title = "Harvest check",
                notes = "Inspect $plantLabel for ripeness and harvest readiness.",
                recurrenceType = RecurrenceType.WEEKLY,
                leadTimeMode = LeadTimeMode.CUSTOM_HOURS,
                customLeadTimeHours = 6
            )
        )
    }
}

private fun harvestUnitSuggestions(
    harvests: List<HarvestEntity>,
    currentUnit: String
): List<String> = buildList {
    currentUnit.trim().takeIf(String::isNotBlank)?.let(::add)
    harvests
        .map(HarvestEntity::quantityUnit)
        .filter(String::isNotBlank)
        .distinctBy(String::lowercase)
        .take(4)
        .forEach(::add)
    listOf("fruit", "lb", "kg", "basket")
        .forEach(::add)
}.distinctBy(String::lowercase)
