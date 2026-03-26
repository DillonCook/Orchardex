package com.dillon.orcharddex.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            Text(item.tree.sectionName.ifBlank { "No section" })
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text(item.tree.species) })
                                AssistChip(onClick = {}, label = { Text(item.tree.plantType.name.replace("_", "-").lowercase()) })
                                if (item.tree.status != TreeStatus.ACTIVE) {
                                    AssistChip(onClick = {}, label = { Text(item.tree.status.name.lowercase()) })
                                }
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
    val knownTrees by viewModel.knownTrees.collectAsStateWithLifecycle()
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris -> viewModel.addPhotos(uris) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.addPhotos(listOf(it)) }
        } else {
            pendingCameraUri?.path?.let(::File)?.delete()
        }
        pendingCameraUri = null
    }
    var showRootstockField by rememberSaveable(state.id, state.rootstock.isNotBlank()) {
        mutableStateOf(state.rootstock.isNotBlank())
    }
    var showAdvancedFields by rememberSaveable(state.id) {
        mutableStateOf(state.hasAdvancedFieldValues())
    }
    var suppressSpeciesAutocomplete by rememberSaveable(state.id) {
        mutableStateOf(false)
    }
    var suppressCultivarAutocomplete by rememberSaveable(state.id) {
        mutableStateOf(false)
    }
    val heroExistingPath = state.existingPhotos.heroOrLatestPhoto()?.relativePath
    val heroNewUri = state.newPhotoUris.firstOrNull()
    val supportedSpecies = remember { BloomForecastEngine.supportedSpeciesCatalog() }
    val speciesCatalog = remember(knownTrees, supportedSpecies) {
        (knownTrees.map(TreeEntity::species) + supportedSpecies)
            .filter(String::isNotBlank)
            .distinctBy(::normalizeAutocomplete)
            .sortedBy(String::lowercase)
    }
    val builtInSpeciesSuggestions = remember(state.species) {
        BloomForecastEngine.speciesAutocompleteOptions(state.species)
    }
    val orchardSpeciesSuggestions = remember(state.species, speciesCatalog) {
        autocompleteSpeciesOptions(state.species, speciesCatalog)
    }
    val speciesSuggestions = remember(builtInSpeciesSuggestions, orchardSpeciesSuggestions) {
        (builtInSpeciesSuggestions + orchardSpeciesSuggestions)
            .distinctBy(::normalizeAutocomplete)
            .take(8)
    }
    val builtInCultivarSuggestions = remember(state.cultivar, state.species) {
        BloomForecastEngine.cultivarAutocompleteOptions(state.cultivar, state.species)
    }
    val orchardCultivarSuggestions = remember(state.cultivar, state.species, knownTrees) {
        existingCultivarAutocompleteOptions(state.cultivar, state.species, knownTrees)
    }
    val cultivarSuggestions = remember(builtInCultivarSuggestions, orchardCultivarSuggestions) {
        (builtInCultivarSuggestions + orchardCultivarSuggestions)
            .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
            .take(8)
    }
    val pollinationRequirement = remember(state.species, state.cultivar) {
        BloomForecastEngine.pollinationRequirementFor(state.species, state.cultivar)
    }
    val autoBloomTimingLabel = remember(state.species, state.cultivar) {
        BloomForecastEngine.catalogBloomTimingLabelFor(state.species, state.cultivar)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TreeHeroPhotoHeader(
                existingPhotoPath = heroExistingPath?.let { File(context.filesDir, "photos/$it").absolutePath },
                newPhotoUri = heroNewUri,
                onImportPhoto = {
                    photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onTakePhoto = {
                    val outputUri = createTreeCaptureUri(context)
                    pendingCameraUri = outputUri
                    cameraLauncher.launch(outputUri)
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
        item {
            SectionCard(if (state.id == null) "Add tree" else "Edit tree") {
                OutlinedTextField(
                    value = state.species,
                    onValueChange = { input ->
                        suppressSpeciesAutocomplete = false
                        val exactSpecies = BloomForecastEngine.resolveSpeciesAutocomplete(input)
                            ?: speciesCatalog.firstOrNull {
                                normalizeAutocomplete(it) == normalizeAutocomplete(input)
                            }
                        viewModel.update { copy(species = exactSpecies ?: input) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tree_species"),
                    label = { Text("Species") }
                )
                if (!suppressSpeciesAutocomplete) {
                    SpeciesAutocompleteCard(
                        query = state.species,
                        suggestions = speciesSuggestions,
                        onSelected = { suggestion ->
                            suppressSpeciesAutocomplete = true
                            viewModel.update { copy(species = suggestion) }
                        }
                    )
                }
                OutlinedTextField(
                    value = state.cultivar,
                    onValueChange = { input ->
                        suppressCultivarAutocomplete = false
                        val exactMatch = BloomForecastEngine.resolveCultivarAutocomplete(input, state.species)
                            ?: resolveExistingCultivarAutocomplete(input, knownTrees)
                        if (exactMatch != null) {
                            suppressSpeciesAutocomplete = true
                            suppressCultivarAutocomplete = true
                        }
                        viewModel.update {
                            if (exactMatch != null) {
                                copy(species = exactMatch.species, cultivar = exactMatch.cultivar)
                            } else {
                                copy(cultivar = input)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tree_cultivar"),
                    label = { Text("Cultivar (optional)") }
                )
                if (!suppressCultivarAutocomplete) {
                    CultivarAutocompleteCard(
                        query = state.cultivar,
                        suggestions = cultivarSuggestions,
                        onSelected = { suggestion ->
                            suppressSpeciesAutocomplete = true
                            suppressCultivarAutocomplete = true
                            viewModel.update {
                                copy(species = suggestion.species, cultivar = suggestion.cultivar)
                            }
                        }
                    )
                }
                pollinationRequirement?.let { requirement ->
                    Text(
                        text = "Known pollination: ${requirement.label}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.id == null) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = { input ->
                            if (input.all(Char::isDigit)) {
                                viewModel.update { copy(quantity = input) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tree_quantity"),
                        label = { Text("How many plants?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(
                        text = "This creates separate plant records with the same details and auto-numbers duplicate nicknames when needed.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.update { copy(hasFruitedBefore = !hasFruitedBefore) } },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Has fruited before")
                        Text(
                            text = "Use this for older plants so first-fruit tracking stays accurate without adding old harvest logs.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Checkbox(
                        checked = state.hasFruitedBefore,
                        onCheckedChange = { checked -> viewModel.update { copy(hasFruitedBefore = checked) } }
                    )
                }
            }
        }
        item {
            SectionCard("Advanced") {
                Text(
                    text = "Optional details for labeling, dates, placement, care, and notes.",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = { showAdvancedFields = !showAdvancedFields },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showAdvancedFields) "Hide advanced fields" else "Show advanced fields")
                }
                if (showAdvancedFields) {
                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = { viewModel.update { copy(nickname = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nickname / label (optional)") }
                    )
                    OutlinedTextField(
                        value = state.source,
                        onValueChange = { viewModel.update { copy(source = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Source / nursery (optional)") }
                    )
                    if (showRootstockField) {
                        OutlinedTextField(
                            value = state.rootstock,
                            onValueChange = { viewModel.update { copy(rootstock = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Rootstock") }
                        )
                        TextButton(
                            onClick = {
                                viewModel.update { copy(rootstock = "") }
                                showRootstockField = false
                            }
                        ) {
                            Text("Hide rootstock")
                        }
                    } else {
                        TextButton(onClick = { showRootstockField = true }) {
                            Text("Add rootstock")
                        }
                    }
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
                    Text(
                        text = "Bloom timing",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Auto uses cultivar dates if known, otherwise species defaults. Custom stays on this plant only.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.bloomTimingMode == BloomTimingMode.AUTO,
                            onClick = {
                                viewModel.update {
                                    copy(
                                        bloomTimingMode = BloomTimingMode.AUTO,
                                        customBloomStartMonth = "",
                                        customBloomStartDay = "",
                                        customBloomDurationDays = ""
                                    )
                                }
                            },
                            label = { Text("Auto") }
                        )
                        FilterChip(
                            selected = state.bloomTimingMode == BloomTimingMode.CUSTOM,
                            onClick = { viewModel.update { copy(bloomTimingMode = BloomTimingMode.CUSTOM) } },
                            label = { Text("Custom") }
                        )
                    }
                    if (state.bloomTimingMode == BloomTimingMode.AUTO) {
                        Text(
                            text = autoBloomTimingLabel ?: "No catalog bloom timing found yet. Switch to Custom if this plant needs a local override.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.customBloomStartMonth,
                                onValueChange = { input ->
                                    if (input.all(Char::isDigit)) {
                                        viewModel.update { copy(customBloomStartMonth = input) }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Start month") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = state.customBloomStartDay,
                                onValueChange = { input ->
                                    if (input.all(Char::isDigit)) {
                                        viewModel.update { copy(customBloomStartDay = input) }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Start day") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        OutlinedTextField(
                            value = state.customBloomDurationDays,
                            onValueChange = { input ->
                                if (input.all(Char::isDigit)) {
                                    viewModel.update { copy(customBloomDurationDays = input) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Duration days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(
                            text = "Use this when the catalog timing is off locally or this cultivar is missing from the catalog.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.update {
                                    copy(
                                        bloomTimingMode = BloomTimingMode.AUTO,
                                        customBloomStartMonth = "",
                                        customBloomStartDay = "",
                                        customBloomDurationDays = ""
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset to catalog")
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
                    Text(
                        when {
                            state.id != null -> "Update tree"
                            state.quantity.toIntOrNull()?.let { it > 1 } == true -> "Save trees"
                            else -> "Save tree"
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SpeciesAutocompleteCard(
    query: String,
    suggestions: List<String>,
    onSelected: (String) -> Unit
) {
    if (query.isBlank() || suggestions.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Species matches", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    AssistChip(onClick = { onSelected(suggestion) }, label = { Text(suggestion) })
                }
            }
        }
    }
}

@Composable
internal fun CultivarAutocompleteCard(
    query: String,
    suggestions: List<CultivarAutocompleteOption>,
    onSelected: (CultivarAutocompleteOption) -> Unit
) {
    if (query.isBlank() || suggestions.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cultivar matches", style = MaterialTheme.typography.labelMedium)
            suggestions.forEach { suggestion ->
                TextButton(
                    onClick = { onSelected(suggestion) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(suggestion.cultivar, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            buildString {
                                append(suggestion.species)
                                val aliasPreview = previewCultivarAliases(query, suggestion.aliases)
                                if (aliasPreview.isNotEmpty()) {
                                    append(" | also known as ")
                                    append(aliasPreview.joinToString(", "))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeHeroPhotoHeader(
    existingPhotoPath: String?,
    newPhotoUri: Uri?,
    onImportPhoto: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            when {
                newPhotoUri != null -> {
                    AsyncImage(
                        model = newPhotoUri,
                        contentDescription = "Tree hero photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                existingPhotoPath != null -> {
                    AsyncImage(
                        model = File(existingPhotoPath),
                        contentDescription = "Tree hero photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Text("Add a hero photo")
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onImportPhoto, modifier = Modifier.weight(1f)) {
            Text("Import photo")
        }
        Button(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
            Text("Take photo")
        }
    }
}

private fun createTreeCaptureUri(context: android.content.Context): Uri {
    val directory = File(context.cacheDir, "camera-captures").apply { mkdirs() }
    val imageFile = File(directory, "tree-${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

internal fun autocompleteSpeciesOptions(
    query: String,
    options: List<String>,
    limit: Int = 8
): List<String> {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return emptyList()
    return options
        .mapNotNull { option ->
            autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(option))?.let { score -> option to score }
        }
        .sortedWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first.lowercase() }
        )
        .map(Pair<String, Int>::first)
        .distinctBy(::normalizeAutocomplete)
        .take(limit)
}

internal fun existingCultivarAutocompleteOptions(
    query: String,
    speciesQuery: String,
    trees: List<TreeEntity>,
    limit: Int = 8
): List<CultivarAutocompleteOption> {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return emptyList()
    return trees
        .filter { it.cultivar.isNotBlank() }
        .mapNotNull { tree ->
            val score = autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(tree.cultivar))
                ?: return@mapNotNull null
            CultivarAutocompleteOption(species = tree.species, cultivar = tree.cultivar) to score
        }
        .sortedWith(
            compareByDescending<Pair<CultivarAutocompleteOption, Int>> { it.second }
                .thenByDescending {
                    speciesAutocompleteScore(speciesQuery, it.first.species)
                }
                .thenBy { it.first.species.lowercase() }
                .thenBy { it.first.cultivar.lowercase() }
        )
        .map(Pair<CultivarAutocompleteOption, Int>::first)
        .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
        .take(limit)
}

internal fun resolveExistingCultivarAutocomplete(
    query: String,
    trees: List<TreeEntity>
): CultivarAutocompleteOption? {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return null
    val matches = trees
        .filter { normalizeAutocomplete(it.cultivar) == normalizedQuery }
        .map { CultivarAutocompleteOption(species = it.species, cultivar = it.cultivar) }
        .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
    if (matches.isEmpty()) return null
    return matches.singleOrNull()
}

private fun speciesAutocompleteScore(query: String, species: String): Int {
    val normalizedQuery = normalizeAutocomplete(query)
    if (normalizedQuery.isBlank()) return 0
    val normalizedSpecies = normalizeAutocomplete(species)
    return when {
        normalizedSpecies == normalizedQuery -> 3
        normalizedSpecies.startsWith(normalizedQuery) -> 2
        normalizedSpecies.contains(normalizedQuery) -> 1
        else -> 0
    }
}

private fun autocompleteMatchScore(query: String, candidate: String): Int? = when {
    candidate == query -> 500
    candidate.startsWith(query) -> 400
    candidate.split(' ').any { it.startsWith(query) } -> 320
    candidate.contains(query) -> 220
    else -> null
}

private fun com.dillon.orcharddex.ui.viewmodel.TreeFormState.hasAdvancedFieldValues(): Boolean =
    nickname.isNotBlank() ||
        source.isNotBlank() ||
        rootstock.isNotBlank() ||
        purchaseDate != null ||
        plantType != PlantType.IN_GROUND ||
        containerSize.isNotBlank() ||
        sunExposure.isNotBlank() ||
        frostSensitivity != FrostSensitivityLevel.MEDIUM ||
        frostSensitivityNote.isNotBlank() ||
        irrigationNote.isNotBlank() ||
        status != TreeStatus.ACTIVE ||
        tags.isNotBlank() ||
        notes.isNotBlank() ||
        bloomTimingMode != com.dillon.orcharddex.data.model.BloomTimingMode.AUTO ||
        customBloomStartMonth.isNotBlank() ||
        customBloomStartDay.isNotBlank() ||
        customBloomDurationDays.isNotBlank()

internal fun previewCultivarAliases(
    query: String,
    aliases: List<String>,
    limit: Int = 2
): List<String> {
    val normalizedQuery = normalizeAutocomplete(query)
    val distinctAliases = aliases.distinctBy(::normalizeAutocomplete)
    if (normalizedQuery.isBlank()) return distinctAliases.take(limit)
    return distinctAliases
        .sortedWith(
            compareByDescending<String> {
                autocompleteMatchScore(normalizedQuery, normalizeAutocomplete(it)) ?: 0
            }.thenBy { it.lowercase() }
        )
        .take(limit)
}

internal fun normalizeAutocomplete(value: String): String = value
    .trim()
    .lowercase()
    .replace("&", "and")
    .replace(Regex("[^a-z0-9]+"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

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
    val pollinationRequirement = remember(item.tree.species, item.tree.cultivar) {
        BloomForecastEngine.pollinationRequirementFor(item.tree.species, item.tree.cultivar)
    }
    val customBloomTimingSummary = remember(item.tree) {
        BloomForecastEngine.customBloomTimingSummaryLabel(item.tree)
    }
    val heroPhoto = remember(item.photos) { item.photos.heroOrLatestPhoto() }
    val harvestSummary = remember(item.harvests) { item.harvests.toHarvestSummary() }

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

    val historyByYear = remember(item.events, item.harvests) {
        (
            item.events.map { event ->
                TreeHistoryEntry(
                    date = event.eventDate,
                    label = buildString {
                        append(event.eventType.name.lowercase().replace("_", " "))
                        if (event.notes.isNotBlank()) append(" • ${event.notes}")
                    },
                    eventType = event.eventType,
                    isHarvest = false
                )
            } +
                item.harvests.map { harvest ->
                    TreeHistoryEntry(
                        date = harvest.harvestDate,
                        label = buildString {
                            append("harvest • ${harvest.quantityValue.displayAmount()} ${harvest.quantityUnit}")
                            if (harvest.firstFruit) append(" • First fruit")
                            if (harvest.notes.isNotBlank()) append(" • ${harvest.notes}")
                        },
                        eventType = null,
                        isHarvest = true
                    )
                }
            ).groupBy { entry ->
            Instant.ofEpochMilli(entry.date).atZone(ZoneId.systemDefault()).year
        }.toSortedMap(compareByDescending { it })
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TreeActionButton(label = "Add event", onClick = { onAddEvent(item.tree.id) })
                TreeActionButton(
                    label = "Add harvest",
                    onClick = { onAddHarvest(item.tree.id) },
                    modifier = Modifier.testTag("add_harvest")
                )
                TreeActionButton(label = "Add reminder", onClick = { onAddReminder(item.tree.id) })
                TreeActionButton(
                    label = "Add photo",
                    onClick = {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                TreeActionButton(label = "Edit plant", onClick = { onEditTree(item.tree.id) })
            }
        }
        item {
            SectionCard(item.tree.displayName()) {
                Text(item.tree.speciesCultivarLabel())
                Text(item.tree.sectionName.ifBlank { "No section assigned" })
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFact("Planting", item.tree.plantType.name.replace("_", "-").lowercase())
                    if (item.tree.status != TreeStatus.ACTIVE) {
                        CompactFact("Status", item.tree.status.name.lowercase())
                    }
                    CompactFact("Planted", item.tree.plantedDate.toDateLabel())
                    CompactFact("Fruited", "yes".takeIf { item.tree.hasFruitedBefore || item.harvests.isNotEmpty() }.orEmpty())
                    CompactFact("Sun", item.tree.sunExposure.orEmpty())
                    CompactFact("Source", item.tree.source.orEmpty())
                    CompactFact("Pollination", pollinationRequirement?.label.orEmpty())
                    CompactFact("Bloom dates", "Custom".takeIf { customBloomTimingSummary != null }.orEmpty())
                }
                customBloomTimingSummary?.let {
                    Text(
                        text = "Custom bloom window: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                heroPhoto?.let { photo ->
                    Card(shape = RoundedCornerShape(24.dp)) {
                        AsyncImage(
                            model = File(context.filesDir, "photos/${photo.relativePath}"),
                            contentDescription = "Tree hero photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                    Text(
                        text = "Hero photo • ${photo.createdAt.toDateLabel()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (item.photos.isNotEmpty()) {
                    Text(
                        text = "Tap any photo below to make it the hero image.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LocalPhotoStrip(
                    existingPaths = item.photos.map { photo ->
                        photo.id to File(context.filesDir, "photos/${photo.relativePath}").absolutePath
                    },
                    selectedExistingId = heroPhoto?.id,
                    onSelectExisting = viewModel::setHeroPhoto
                )
            }
        }
        item {
            SectionCard("Notes") {
                Text(item.tree.notes.ifBlank { "No notes yet." })
            }
        }
        item {
            SectionCard("Harvest summary") {
                if (harvestSummary.totalHarvests == 0 && !item.tree.hasFruitedBefore) {
                    Text("No harvests logged yet.")
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactFact("Total harvests", harvestSummary.totalHarvests.toString())
                        harvestSummary.latestHarvestDate?.let { CompactFact("Latest", it.toDateLabel()) }
                        harvestSummary.firstFruitDate?.let { CompactFact("First fruit", it.toDateLabel()) }
                        CompactFact("Verified", harvestSummary.verifiedHarvestCount.toString())
                        if (item.tree.hasFruitedBefore && harvestSummary.firstFruitDate == null) {
                            CompactFact("History", "Previously fruited")
                        }
                    }
                    if (harvestSummary.totalsByUnit.isNotEmpty()) {
                        Text("Logged totals", style = MaterialTheme.typography.labelLarge)
                        harvestSummary.totalsByUnit.forEach { total ->
                            Text("${total.quantity.displayAmount()} ${total.unit}")
                        }
                    }
                }
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
            SectionCard("Season history") {
                if (historyByYear.isEmpty()) {
                    Text("No bloom, fruit set, or harvest history yet.")
                } else {
                    historyByYear.forEach { (year, entries) ->
                        val bloomCount = entries.count { it.eventType == EventType.BLOOM }
                        val fruitSetCount = entries.count { it.eventType == EventType.FRUIT_SET }
                        val harvestCount = entries.count(TreeHistoryEntry::isHarvest)
                        Text(year.toString(), style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$bloomCount blooms • $fruitSetCount fruit sets • $harvestCount harvests",
                            style = MaterialTheme.typography.bodySmall
                        )
                        entries.sortedByDescending(TreeHistoryEntry::date).forEach { entry ->
                            Text("${entry.date.toDateLabel()} • ${entry.label}")
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Harvest history") {
                if (item.harvests.isEmpty()) {
                    Text("No harvests yet.")
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
            SectionCard("Activity log") {
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
}

@Composable
private fun TreeActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private data class TreeHistoryEntry(
    val date: Long,
    val label: String,
    val eventType: EventType?,
    val isHarvest: Boolean
)

private data class HarvestUnitTotal(
    val unit: String,
    val quantity: Double
)

private data class TreeHarvestSummary(
    val totalHarvests: Int,
    val latestHarvestDate: Long?,
    val firstFruitDate: Long?,
    val verifiedHarvestCount: Int,
    val totalsByUnit: List<HarvestUnitTotal>
)

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

private fun List<TreePhotoEntity>.heroOrLatestPhoto(): TreePhotoEntity? =
    firstOrNull(TreePhotoEntity::isHero)
        ?: maxWithOrNull(compareBy<TreePhotoEntity> { it.createdAt }.thenBy { it.sortOrder })

private fun List<HarvestEntity>.toHarvestSummary(): TreeHarvestSummary {
    val orderedHarvests = sortedByDescending(HarvestEntity::harvestDate)
    return TreeHarvestSummary(
        totalHarvests = size,
        latestHarvestDate = orderedHarvests.firstOrNull()?.harvestDate,
        firstFruitDate = filter(HarvestEntity::firstFruit).minByOrNull(HarvestEntity::harvestDate)?.harvestDate,
        verifiedHarvestCount = count(HarvestEntity::verified),
        totalsByUnit = groupBy { harvest ->
            harvest.quantityUnit.trim().ifBlank { "unit" }
        }.map { (unit, harvests) ->
            HarvestUnitTotal(
                unit = unit,
                quantity = harvests.sumOf(HarvestEntity::quantityValue)
            )
        }.sortedByDescending(HarvestUnitTotal::quantity)
    )
}
