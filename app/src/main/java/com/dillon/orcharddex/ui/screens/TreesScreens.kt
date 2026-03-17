package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.CommonSpeciesSuggestions
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.ui.components.ChoiceChipsRow
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.DateField
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.LocalPhotoStrip
import com.dillon.orcharddex.ui.components.PhotoAddCard
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.TreeDetailViewModel
import com.dillon.orcharddex.ui.viewmodel.TreeFormViewModel
import com.dillon.orcharddex.ui.viewmodel.TreesViewModel
import java.io.File
import java.time.LocalDate

private enum class TreeSortOption(val label: String) {
    UPDATED("Updated"),
    PLANTED("Planted"),
    SPECIES("Species"),
    CULTIVAR("Cultivar")
}

@Composable
fun TreesScreen(
    viewModel: TreesViewModel,
    onAddTree: () -> Unit,
    onTreeClick: (String) -> Unit
) {
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val speciesOptions by viewModel.species.collectAsStateWithLifecycle()
    val cultivarOptions by viewModel.cultivars.collectAsStateWithLifecycle()
    val orchardOptions by viewModel.orchards.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var speciesFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var cultivarFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var orchardFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<TreeStatus?>(null) }
    var plantTypeFilter by rememberSaveable { mutableStateOf<PlantType?>(null) }
    var sort by rememberSaveable { mutableStateOf(TreeSortOption.UPDATED) }

    val filteredTrees = remember(
        trees,
        search,
        speciesFilter,
        cultivarFilter,
        orchardFilter,
        statusFilter,
        plantTypeFilter,
        sort
    ) {
        trees.filter { item ->
            val tree = item.tree
            val query = search.trim().lowercase()
            val matchesQuery = query.isBlank() || listOf(
                tree.orchardName,
                tree.sectionName,
                tree.nickname.orEmpty(),
                tree.species,
                tree.cultivar,
                tree.tags
            ).any { it.lowercase().contains(query) }
            matchesQuery &&
                (speciesFilter == null || tree.species == speciesFilter) &&
                (cultivarFilter == null || tree.cultivar == cultivarFilter) &&
                (orchardFilter == null || tree.orchardName == orchardFilter) &&
                (statusFilter == null || tree.status == statusFilter) &&
                (plantTypeFilter == null || tree.plantType == plantTypeFilter)
        }.sortedWith(
            when (sort) {
                TreeSortOption.UPDATED -> compareByDescending { it.tree.updatedAt }
                TreeSortOption.PLANTED -> compareByDescending { it.tree.plantedDate }
                TreeSortOption.SPECIES -> compareBy({ it.tree.species.lowercase() }, { it.tree.cultivar.lowercase() })
                TreeSortOption.CULTIVAR -> compareBy({ it.tree.cultivar.lowercase() }, { it.tree.species.lowercase() })
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTree, modifier = Modifier.testTag("add_tree")) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search trees") }
                )
            }
            item {
                SectionCard("Filters") {
                    ChoiceChipsRow(speciesOptions.take(10), speciesFilter, onSelected = { speciesFilter = it })
                    ChoiceChipsRow(cultivarOptions.take(10), cultivarFilter, onSelected = { cultivarFilter = it })
                    ChoiceChipsRow(orchardOptions.take(8), orchardFilter, onSelected = { orchardFilter = it })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("Any status") })
                        TreeStatus.entries.forEach { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { statusFilter = status },
                                label = { Text(status.name.lowercase().replaceFirstChar(Char::uppercase)) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = plantTypeFilter == null, onClick = { plantTypeFilter = null }, label = { Text("Any planting") })
                        FilterChip(selected = plantTypeFilter == PlantType.IN_GROUND, onClick = { plantTypeFilter = PlantType.IN_GROUND }, label = { Text("In-ground") })
                        FilterChip(selected = plantTypeFilter == PlantType.CONTAINER, onClick = { plantTypeFilter = PlantType.CONTAINER }, label = { Text("Container") })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TreeSortOption.entries.forEach { option ->
                            FilterChip(selected = sort == option, onClick = { sort = option }, label = { Text(option.label) })
                        }
                    }
                }
            }
            if (filteredTrees.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No trees match these filters",
                        message = "Clear a filter or add a new tree."
                    )
                }
            }
            items(filteredTrees, key = { it.tree.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTreeClick(item.tree.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TreeThumbnail(item.mainPhotoPath)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.tree.displayName(), style = MaterialTheme.typography.titleMedium)
                            Text("${item.tree.orchardName.ifBlank { "No orchard" }} • ${item.tree.sectionName.ifBlank { "No section" }}")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text(item.tree.species) })
                                AssistChip(onClick = {}, label = { Text(item.tree.plantType.name.replace("_", "-").lowercase()) })
                                AssistChip(onClick = {}, label = { Text(item.tree.status.name.lowercase()) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TreeFormScreen(
    viewModel: TreeFormViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    val orchardNames by viewModel.orchardNames.collectAsStateWithLifecycle()
    val speciesNames by viewModel.speciesNames.collectAsStateWithLifecycle()
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }
    val matchingSpecies = remember(state.species, speciesNames) {
        if (state.species.isBlank()) CommonSpeciesSuggestions else {
            speciesNames.filter { it.contains(state.species, ignoreCase = true) }.take(6)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(if (state.id == null) "Add tree" else "Edit tree") {
                OutlinedTextField(
                    value = state.orchardName,
                    onValueChange = { viewModel.update { copy(orchardName = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Orchard name") }
                )
                if (orchardNames.isNotEmpty()) {
                    ChoiceChipsRow(orchardNames, state.orchardName.takeIf(String::isNotBlank), onSelected = {
                        viewModel.update { copy(orchardName = it.orEmpty()) }
                    })
                }
                OutlinedTextField(
                    value = state.sectionName,
                    onValueChange = { viewModel.update { copy(sectionName = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Section / location note") }
                )
                OutlinedTextField(
                    value = state.nickname,
                    onValueChange = { viewModel.update { copy(nickname = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nickname / label") }
                )
                OutlinedTextField(
                    value = state.species,
                    onValueChange = { viewModel.update { copy(species = it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tree_species"),
                    label = { Text("Species") }
                )
                ChoiceChipsRow(matchingSpecies, state.species.takeIf(String::isNotBlank), onSelected = {
                    viewModel.update { copy(species = it.orEmpty()) }
                })
                OutlinedTextField(
                    value = state.cultivar,
                    onValueChange = { viewModel.update { copy(cultivar = it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tree_cultivar"),
                    label = { Text("Cultivar") }
                )
                OutlinedTextField(
                    value = state.rootstock,
                    onValueChange = { viewModel.update { copy(rootstock = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Rootstock") }
                )
                OutlinedTextField(
                    value = state.source,
                    onValueChange = { viewModel.update { copy(source = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Source / nursery") }
                )
            }
        }
        item {
            SectionCard("Dates & placement") {
                if (state.purchaseDate != null) {
                    DateField(
                        label = "Purchase date",
                        value = state.purchaseDate,
                        onDateSelected = { viewModel.update { copy(purchaseDate = it) } }
                    )
                    OutlinedButton(
                        onClick = { viewModel.update { copy(purchaseDate = null) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear purchase date")
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.update { copy(purchaseDate = LocalDate.now()) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set purchase date")
                    }
                }
                DateField(
                    label = "Planted date",
                    value = state.plantedDate,
                    onDateSelected = { viewModel.update { copy(plantedDate = it) } }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.plantType == PlantType.IN_GROUND,
                        onClick = { viewModel.update { copy(plantType = PlantType.IN_GROUND) } },
                        label = { Text("In-ground") }
                    )
                    FilterChip(
                        selected = state.plantType == PlantType.CONTAINER,
                        onClick = { viewModel.update { copy(plantType = PlantType.CONTAINER) } },
                        label = { Text("Container") }
                    )
                }
                if (state.plantType == PlantType.CONTAINER) {
                    OutlinedTextField(
                        value = state.containerSize,
                        onValueChange = { viewModel.update { copy(containerSize = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Container size") }
                    )
                }
                OutlinedTextField(
                    value = state.sunExposure,
                    onValueChange = { viewModel.update { copy(sunExposure = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sun exposure") }
                )
            }
        }
        item {
            SectionCard("Care & notes") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrostSensitivityLevel.entries.forEach { level ->
                        FilterChip(
                            selected = state.frostSensitivity == level,
                            onClick = { viewModel.update { copy(frostSensitivity = level) } },
                            label = { Text(level.name.lowercase()) }
                        )
                    }
                }
                if (state.frostSensitivity == FrostSensitivityLevel.CUSTOM) {
                    OutlinedTextField(
                        value = state.frostSensitivityNote,
                        onValueChange = { viewModel.update { copy(frostSensitivityNote = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Frost sensitivity note") }
                    )
                }
                OutlinedTextField(
                    value = state.irrigationNote,
                    onValueChange = { viewModel.update { copy(irrigationNote = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Irrigation note") }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TreeStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.status == status,
                            onClick = { viewModel.update { copy(status = status) } },
                            label = { Text(status.name.lowercase()) }
                        )
                    }
                }
                OutlinedTextField(
                    value = state.tags,
                    onValueChange = { viewModel.update { copy(tags = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tags") }
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.update { copy(notes = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
                    minLines = 4
                )
            }
        }
        item {
            SectionCard("Photos") {
                PhotoAddCard(
                    onClick = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                LocalPhotoStrip(
                    existingPaths = state.existingPhotos.map { photo ->
                        photo.id to File(context.filesDir, "photos/${photo.relativePath}").absolutePath
                    },
                    newUris = state.newPhotoUris,
                    onRemoveExisting = viewModel::removeExistingPhoto,
                    onRemoveNew = viewModel::removeNewPhoto
                )
            }
        }
        item {
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.save(onSaved) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tree_save")
                ) {
                    Text(if (state.id == null) "Save tree" else "Update tree")
                }
            }
        }
    }
}

@Composable
fun TreeDetailScreen(
    viewModel: TreeDetailViewModel,
    onBack: () -> Unit,
    onEditTree: (String) -> Unit,
    onAddEvent: (String) -> Unit,
    onAddHarvest: (String) -> Unit,
    onAddReminder: (String) -> Unit
) {
    val context = LocalContext.current
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }
    val item = detail ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tree not found.")
    }

    if (viewModel.confirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            title = { Text("Delete this tree?") },
            text = { Text("This removes the tree, its photos, events, harvests, and reminders.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTree(onBack) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(item.tree.displayName()) {
                Text("${item.tree.species} • ${item.tree.cultivar}")
                Text("${item.tree.orchardName.ifBlank { "No orchard" }} • ${item.tree.sectionName.ifBlank { "No section" }}")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFact("Planting", item.tree.plantType.name.replace("_", "-").lowercase())
                    CompactFact("Status", item.tree.status.name.lowercase())
                    CompactFact("Planted", item.tree.plantedDate.toDateLabel())
                    CompactFact("Sun", item.tree.sunExposure.orEmpty())
                }
                LocalPhotoStrip(
                    existingPaths = item.photos.map { photo ->
                        photo.id to File(context.filesDir, "photos/${photo.relativePath}").absolutePath
                    }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onAddEvent(item.tree.id) }) { Text("Log Event") }
                    OutlinedButton(
                        onClick = { onAddHarvest(item.tree.id) },
                        modifier = Modifier.testTag("add_harvest")
                    ) { Text("Log Harvest") }
                    OutlinedButton(onClick = { onAddReminder(item.tree.id) }) { Text("Add Reminder") }
                    OutlinedButton(
                        onClick = {
                            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) { Text("Add Photo") }
                    OutlinedButton(onClick = { onEditTree(item.tree.id) }) { Text("Edit Tree") }
                }
            }
        }
        item {
            SectionCard("Notes") {
                Text(item.tree.notes.ifBlank { "No notes yet." })
            }
        }
        item {
            SectionCard("Upcoming reminders") {
                val reminders = item.reminders.filter { it.completedAt == null }
                if (reminders.isEmpty()) {
                    Text("No reminders linked to this tree.")
                } else {
                    reminders.forEach { reminder ->
                        Text("${reminder.title} • ${reminder.dueAt.toDateLabel()}")
                    }
                }
            }
        }
        item {
            SectionCard("Harvest history") {
                if (item.harvests.isEmpty()) {
                    Text("No harvests logged yet.")
                } else {
                    item.harvests.forEach { harvest ->
                        Text(
                            "${harvest.harvestDate.toDateLabel()} • ${harvest.quantityValue.displayAmount()} ${harvest.quantityUnit}" +
                                if (harvest.firstFruit) " • First fruit" else ""
                        )
                    }
                }
            }
        }
        item {
            SectionCard("Timeline") {
                val timeline = (
                    item.events.map { it.eventDate to "${it.eventType.name.lowercase().replace("_", " ")} • ${it.notes}" } +
                        item.harvests.map { it.harvestDate to "harvest • ${it.quantityValue.displayAmount()} ${it.quantityUnit}" }
                    ).sortedByDescending { it.first }
                if (timeline.isEmpty()) {
                    Text("No activity on this tree yet.")
                } else {
                    timeline.forEach { entry ->
                        Text("${entry.first.toDateLabel()} • ${entry.second}")
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = viewModel::requestDeleteConfirmation, modifier = Modifier.fillMaxWidth()) {
                Text("Delete tree")
            }
        }
    }
}

@Composable
private fun TreeThumbnail(relativePath: String?) {
    val context = LocalContext.current
    Card(modifier = Modifier.size(88.dp)) {
        if (relativePath != null) {
            AsyncImage(
                model = File(context.filesDir, "photos/$relativePath"),
                contentDescription = "Tree photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No photo")
            }
        }
    }
}
