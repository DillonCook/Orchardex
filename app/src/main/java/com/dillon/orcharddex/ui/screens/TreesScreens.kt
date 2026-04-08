package com.dillon.orcharddex.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
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
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.SaleEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.NurseryStage
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.PollinationProfile
import com.dillon.orcharddex.data.model.PropagationMethod
import com.dillon.orcharddex.data.model.SaleKind
import com.dillon.orcharddex.data.model.SelfCompatibility
import com.dillon.orcharddex.data.model.TreeOriginType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.bloomMonthLabels
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
import com.dillon.orcharddex.ui.previewCultivarAliases
import com.dillon.orcharddex.ui.components.ChoiceChipsRow
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.DateField
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.LocalPhotoStrip
import com.dillon.orcharddex.ui.components.PhotoAddCard
import com.dillon.orcharddex.ui.components.SaleDialog
import com.dillon.orcharddex.ui.components.SaleDraftState
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.viewmodel.TreeFormSearchField
import com.dillon.orcharddex.ui.viewmodel.TreeDetailViewModel
import com.dillon.orcharddex.ui.viewmodel.TreeFormViewModel
import com.dillon.orcharddex.ui.viewmodel.TreesViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch

private enum class TreeSortOption(val label: String) {
    UPDATED("Updated"),
    PLANTED("Planted"),
    SPECIES("Species"),
    CULTIVAR("Cultivar")
}

private enum class TreeDetailSection(val label: String) {
    SNAPSHOT("Snapshot"),
    PRODUCTION("Production"),
    TIMELINE("Timeline")
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
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val autocompleteState = viewModel.autocompleteState
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
    var showAdvancedFields by rememberSaveable(state.id) {
        mutableStateOf(shouldAutoExpandAdvancedFields(state))
    }
    var advancedFieldsAutoExpanded by rememberSaveable(state.id) { mutableStateOf(false) }
    val shouldAutoExpandAdvanced = remember(state) { shouldAutoExpandAdvancedFields(state) }
    LaunchedEffect(state.id, shouldAutoExpandAdvanced) {
        if (!advancedFieldsAutoExpanded && shouldAutoExpandAdvanced) {
            showAdvancedFields = true
            advancedFieldsAutoExpanded = true
        }
    }
    val heroExistingPath = state.existingPhotos.heroOrLatestPhoto()?.relativePath
    val heroNewUri = state.newPhotoUris.firstOrNull()
    val locationOptions = remember(locations) { locations.map(::treeLocationLabel) }
    val selectedLocationLabel = remember(locations, state.locationId, state.orchardName) {
        locations.firstOrNull { it.id == state.locationId }?.let(::treeLocationLabel)
            ?: state.orchardName.ifBlank { locations.firstOrNull()?.let(::treeLocationLabel) ?: "Orchard" }
    }
    val speciesError = state.errorMessage == "Species is required."
    val quantityError = state.errorMessage == "Quantity must be at least 1."
    val photoError = remember(state.errorMessage) {
        state.errorMessage?.let { message ->
            message.contains("photo", ignoreCase = true) ||
                message.contains("image", ignoreCase = true) ||
                message.contains("file", ignoreCase = true)
        } == true
    }
    val generalError = state.errorMessage.takeUnless {
        speciesError || quantityError || photoError
    }
    val showInlineSpeciesSuggestions = autocompleteState.speciesSuggestions.isNotEmpty()
    val showInlineCultivarSuggestions = autocompleteState.cultivarSuggestions.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("tree_form_list"),
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
                if (photoError) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
                    if (locationOptions.isNotEmpty()) {
                        SelectionField(
                            label = "Orchard Name",
                            value = selectedLocationLabel,
                            options = locationOptions,
                            onSelected = { selected ->
                                locations.firstOrNull { treeLocationLabel(it) == selected }?.let { location ->
                                    viewModel.update {
                                        copy(
                                            locationId = location.id,
                                            orchardName = location.name
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = state.cultivar,
                        onValueChange = viewModel::updateCultivarInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    viewModel.setActiveSearchField(TreeFormSearchField.CULTIVAR)
                                } else {
                                    viewModel.setActiveSearchField(null)
                                }
                            }
                            .testTag("tree_cultivar"),
                        label = { Text("Cultivar (optional)") }
                    )
                    if (showInlineCultivarSuggestions) {
                        CultivarAutocompleteCard(
                            query = if (state.cultivar.isBlank()) autocompleteState.stableSpeciesSelection.orEmpty() else state.cultivar,
                            suggestions = autocompleteState.cultivarSuggestions,
                            title = autocompleteState.cultivarSuggestionsTitle,
                            allowBlankQuery = true,
                            modifier = Modifier.testTag("species_scoped_cultivars"),
                            onSelected = viewModel::selectCultivarSuggestion
                        )
                    }
                    OutlinedTextField(
                        value = state.species,
                        onValueChange = viewModel::updateSpeciesInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    viewModel.setActiveSearchField(TreeFormSearchField.SPECIES)
                                } else {
                                    viewModel.setActiveSearchField(null)
                                }
                            }
                            .testTag("tree_species"),
                        label = { Text("Species") },
                        isError = speciesError,
                        supportingText = if (speciesError) {
                            { Text("Species is required.") }
                        } else {
                            null
                        }
                    )
                    if (showInlineSpeciesSuggestions) {
                        SpeciesAutocompleteCard(
                            query = state.species,
                            suggestions = autocompleteState.speciesSuggestions,
                            modifier = Modifier.testTag("species_autocomplete"),
                            onSelected = viewModel::selectSpeciesSuggestion
                        )
                    }
                    if (autocompleteState.cultivarAliasOptions.size > 1) {
                        SelectionField(
                            label = "Display cultivar as",
                            value = state.cultivar.takeIf(String::isNotBlank) ?: autocompleteState.cultivarAliasOptions.first(),
                            options = autocompleteState.cultivarAliasOptions,
                            onSelected = { selected ->
                                autocompleteState.matchedCultivarOption?.let { option ->
                                    viewModel.update {
                                        copy(species = option.species, cultivar = selected)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    autocompleteState.pollinationProfile?.let { profile ->
                        Text(
                            text = "Catalog pollination: ${profile.summaryLabel}",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = quantityError,
                            supportingText = if (quantityError) {
                                { Text("Enter at least 1 plant.") }
                            } else {
                                null
                            }
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
                                text = "Turn this on when you know this plant has already produced fruit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            label = { Text("Source / nursery (optional)") },
                            supportingText = {
                                Text("Track where this plant, cutting, or scion came from.")
                            }
                        )
                        OutlinedTextField(
                            value = state.rootstock,
                            onValueChange = { viewModel.update { copy(rootstock = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Rootstock (optional)") },
                            supportingText = {
                                Text("Useful for grafted trees and compatibility or vigor notes.")
                            }
                        )
                        OutlinedTextField(
                            value = state.sectionName,
                            onValueChange = { viewModel.update { copy(sectionName = it) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Area / Section / Block (optional)") },
                            supportingText = {
                                Text("Use this to group plants by bed, row, greenhouse, or orchard block.")
                            }
                        )
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
                            onClick = { viewModel.update { copy(purchaseDate = OrchardTime.today()) } },
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
                        text = "Nursery and origin",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NurseryStage.entries.forEach { stage ->
                            FilterChip(
                                selected = state.nurseryStage == stage,
                                onClick = { viewModel.update { copy(nurseryStage = stage) } },
                                label = { Text(stage.label) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TreeOriginType.entries.forEach { origin ->
                            FilterChip(
                                selected = state.originType == origin,
                                onClick = { viewModel.update { copy(originType = origin) } },
                                label = { Text(origin.label) }
                            )
                        }
                    }
                    if (state.parentTreeId != null || state.originType == TreeOriginType.PROPAGATED) {
                        Text(
                            text = "Propagation",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "This child plant starts with the parent's orchard profile and cultivar details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PropagationMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = state.propagationMethod == method,
                                    onClick = { viewModel.update { copy(propagationMethod = method) } },
                                    label = { Text(method.label) }
                                )
                            }
                        }
                        if (state.propagationDate != null) {
                            DateField(
                                label = "Propagation date",
                                value = state.propagationDate,
                                onDateSelected = { viewModel.update { copy(propagationDate = it) } }
                            )
                            OutlinedButton(
                                onClick = { viewModel.update { copy(propagationDate = null) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear propagation date")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.update { copy(propagationDate = OrchardTime.today()) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Set propagation date")
                            }
                        }
                    }
                    Text(
                        text = "Bloom pattern",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Auto uses the catalog and learned logs. Custom lets you shape this plant with a month-by-month bloom profile.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.bloomTimingMode == BloomTimingMode.AUTO,
                            onClick = {
                                viewModel.update {
                                    copy(
                                        bloomTimingMode = BloomTimingMode.AUTO,
                                        bloomPatternOverride = null,
                                        manualBloomProfile = List(12) { 0 },
                                        alternateYearAnchor = "",
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
                            text = autocompleteState.autoBloomTimingLabel ?: "No catalog bloom timing found yet. Switch to Custom if this plant needs a local override.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        val selectedPattern = state.bloomPatternOverride ?: BloomPatternType.SINGLE_ANNUAL
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                BloomPatternType.SINGLE_ANNUAL,
                                BloomPatternType.MULTI_WAVE,
                                BloomPatternType.CONTINUOUS,
                                BloomPatternType.ALTERNATE_YEAR,
                                BloomPatternType.MANUAL_ONLY
                            ).forEach { pattern ->
                                FilterChip(
                                    selected = selectedPattern == pattern,
                                    onClick = { viewModel.update { copy(bloomPatternOverride = pattern) } },
                                    label = { Text(pattern.label) }
                                )
                            }
                        }
                        if (selectedPattern == BloomPatternType.ALTERNATE_YEAR) {
                            OutlinedTextField(
                                value = state.alternateYearAnchor,
                                onValueChange = { input ->
                                    if (input.all(Char::isDigit)) {
                                        viewModel.update { copy(alternateYearAnchor = input) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Active bloom year") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Text(
                            text = "Tap months to cycle Off → Watch → Peak → Strong.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bloomMonthLabels.forEachIndexed { index, monthLabel ->
                                BloomIntensityChip(
                                    monthLabel = monthLabel,
                                    intensity = state.manualBloomProfile.getOrElse(index) { 0 },
                                    onClick = {
                                        val updatedProfile = state.manualBloomProfile.toMutableList().let { profile ->
                                            val normalized = if (profile.size == 12) {
                                                profile
                                            } else {
                                                MutableList(12) { monthIndex -> profile.getOrElse(monthIndex) { 0 } }
                                            }
                                            normalized[index] = (normalized[index] + 1) % 4
                                            normalized
                                        }
                                        viewModel.update { copy(manualBloomProfile = updatedProfile) }
                                    }
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.update {
                                    copy(
                                        bloomTimingMode = BloomTimingMode.AUTO,
                                        bloomPatternOverride = null,
                                        manualBloomProfile = List(12) { 0 },
                                        alternateYearAnchor = "",
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
                    Text(
                        text = "Pollination override",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Keep these on Catalog unless this plant behaves differently in your orchard.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.selfCompatibilityOverride == null,
                            onClick = { viewModel.update { copy(selfCompatibilityOverride = null) } },
                            label = { Text("Catalog") }
                        )
                        SelfCompatibility.entries.forEach { value ->
                            FilterChip(
                                selected = state.selfCompatibilityOverride == value,
                                onClick = { viewModel.update { copy(selfCompatibilityOverride = value) } },
                                label = { Text(value.label) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.pollinationModeOverride == null,
                            onClick = { viewModel.update { copy(pollinationModeOverride = null) } },
                            label = { Text("Catalog") }
                        )
                        PollinationMode.entries.forEach { value ->
                            FilterChip(
                                selected = state.pollinationModeOverride == value,
                                onClick = { viewModel.update { copy(pollinationModeOverride = value) } },
                                label = { Text(value.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.pollinationOverrideNote,
                        onValueChange = { viewModel.update { copy(pollinationOverrideNote = it) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pollination override note") }
                    )
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
                generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tree_save")
                    ) {
                        Text(
                            when {
                                state.isSaving -> "Saving..."
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
}

@Composable
internal fun SpeciesAutocompleteCard(
    query: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    if (query.isBlank() || suggestions.isEmpty()) return
    Card(modifier = modifier.fillMaxWidth()) {
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
    title: String = "Cultivar matches",
    allowBlankQuery: Boolean = false,
    modifier: Modifier = Modifier,
    onSelected: (CultivarAutocompleteOption) -> Unit
) {
    if ((!allowBlankQuery && query.isBlank()) || suggestions.isEmpty()) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
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

internal fun com.dillon.orcharddex.ui.viewmodel.TreeFormState.hasAdvancedFieldValues(): Boolean =
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
        customBloomDurationDays.isNotBlank() ||
        selfCompatibilityOverride != null ||
        pollinationModeOverride != null ||
        pollinationOverrideNote.isNotBlank()

internal fun shouldAutoExpandAdvancedFields(
    state: com.dillon.orcharddex.ui.viewmodel.TreeFormState
): Boolean = state.hasAdvancedFieldValues() ||
    state.parentTreeId != null ||
    state.originType == TreeOriginType.PROPAGATED

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeDetailScreen(
    viewModel: TreeDetailViewModel,
    settings: AppSettings,
    onBack: () -> Unit,
    onEditTree: (String) -> Unit,
    onPropagate: (String, String) -> Unit,
    onAddLog: (String) -> Unit,
    onAddReminder: (String) -> Unit,
    onOpenLog: (ActivityKind, String) -> Unit
) {
    val context = LocalContext.current
    val detail by viewModel.detail.collectAsStateWithLifecycle()
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
    val item = detail ?: return Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tree not found.")
    }
    val locationProfile = remember(item.location, settings) {
        item.location?.toForecastLocationProfile() ?: settings.forecastLocationProfile()
    }
    val catalogPollination = remember(item.tree.species, item.tree.cultivar) {
        BloomForecastEngine.pollinationProfileFor(item.tree.species, item.tree.cultivar)
    }
    val effectivePollination = remember(item.tree, catalogPollination) {
        PollinationProfile(
            selfCompatibility = item.tree.selfCompatibilityOverride
                ?: catalogPollination?.selfCompatibility
                ?: SelfCompatibility.UNKNOWN,
            pollinationMode = item.tree.pollinationModeOverride
                ?: catalogPollination?.pollinationMode
                ?: PollinationMode.UNKNOWN
        )
    }
    val customBloomTimingSummary = remember(item.tree) {
        BloomForecastEngine.customBloomTimingSummaryLabel(item.tree)
    }
    val activityPhotoPathsByOwner = remember(item.activityPhotos) {
        item.activityPhotos
            .groupBy { it.ownerKind to it.ownerId }
            .mapValues { (_, photos) ->
                photos.sortedBy { photo -> photo.sortOrder }.map { photo -> photo.relativePath }
            }
    }
    val phenologyObservations = remember(item.events, item.harvests) {
        item.events.map { event ->
            PhenologyObservation(
                treeId = item.tree.id,
                dateMillis = event.eventDate,
                eventType = event.eventType
            )
        } + item.harvests.map { harvest ->
            PhenologyObservation(
                treeId = item.tree.id,
                dateMillis = harvest.harvestDate,
                isHarvest = true
            )
        }
    }
    val forecastSummary = remember(item.tree, locationProfile, phenologyObservations) {
        BloomForecastEngine.nextBloomSummary(item.tree, locationProfile, phenologyObservations)
    }
    val detailListState = rememberLazyListState()
    val detailScope = rememberCoroutineScope()
    val heroPhoto = remember(item.photos) { item.photos.heroOrLatestPhoto() }
    val harvestSummary = remember(item.harvests) { item.harvests.toHarvestSummary() }
    var timelineFilter by rememberSaveable(item.tree.id) { mutableStateOf(TreeTimelineFilter.ALL) }
    var pendingPropagationMethod by rememberSaveable(item.tree.id) { mutableStateOf<PropagationMethod?>(null) }
    var showPhotoActionDialog by rememberSaveable(item.tree.id) { mutableStateOf(false) }
    var showTreeSaleDialog by rememberSaveable(item.tree.id) { mutableStateOf(false) }
    var treeSaleDraft by remember(item.tree.id) { mutableStateOf(defaultTreeSaleDraft()) }
    var treeSaleDraftError by remember(item.tree.id) { mutableStateOf<String?>(null) }
    val directTreeSale = remember(item.sales) {
        item.sales.firstOrNull { sale -> sale.saleKind == SaleKind.TREE }
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
    if (showPhotoActionDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoActionDialog = false },
            title = { Text("Add plant photos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Add a new plant photo from the camera or import one from your library.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            showPhotoActionDialog = false
                            val outputUri = createTreeCaptureUri(context)
                            pendingCameraUri = outputUri
                            cameraLauncher.launch(outputUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take photo")
                    }
                    OutlinedButton(
                        onClick = {
                            showPhotoActionDialog = false
                            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import photos")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (settings.showSalesTools && showTreeSaleDialog) {
        SaleDialog(
            title = "Sell this plant",
            confirmLabel = if (viewModel.saleBusy) "Saving..." else "Record sale",
            state = treeSaleDraft,
            onStateChange = {
                treeSaleDraft = it
                treeSaleDraftError = null
            },
            onDismiss = {
                showTreeSaleDialog = false
                treeSaleDraftError = null
            },
            onConfirm = {
                val salePrice = treeSaleDraft.unitPrice.toDoubleOrNull()
                if (salePrice == null || salePrice < 0.0) {
                    treeSaleDraftError = "Enter a valid sale price."
                } else {
                    treeSaleDraftError = null
                    viewModel.recordTreeSale(
                        soldDate = treeSaleDraft.soldDate,
                        salePrice = salePrice,
                        saleChannel = treeSaleDraft.saleChannel,
                        notes = treeSaleDraft.notes
                    ) {
                        showTreeSaleDialog = false
                    }
                }
            },
            quantityEnabled = false,
            unitEnabled = false,
            unitPriceLabel = "Sale price",
            errorMessage = treeSaleDraftError ?: viewModel.saleErrorMessage,
            saving = viewModel.saleBusy
        )
    }
    pendingPropagationMethod?.let { selectedMethod ->
        AlertDialog(
            onDismissRequest = { pendingPropagationMethod = null },
            title = { Text("Propagate this plant") },
            text = { Text("Create a new child plant prefilled from this mother plant using ${selectedMethod.label.lowercase()}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPropagate(item.tree.id, selectedMethod.name.lowercase())
                        pendingPropagationMethod = null
                    }
                ) {
                    Text("Create child")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPropagationMethod = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val timelineEntries = remember(item.events, item.harvests, activityPhotoPathsByOwner) {
        (
            item.events.map { event ->
                val photoPaths = activityPhotoPathsByOwner["EVENT" to event.id].orEmpty()
                TreeTimelineEntry(
                    id = event.id,
                    kind = ActivityKind.EVENT,
                    date = event.eventDate,
                    title = event.eventType.treeLabel(),
                    summary = buildEventTimelineSummary(event),
                    eventType = event.eventType,
                    photoPaths = photoPaths.ifEmpty { listOfNotNull(event.photoPath) }
                )
            } +
                item.harvests.map { harvest ->
                    val photoPaths = activityPhotoPathsByOwner["HARVEST" to harvest.id].orEmpty()
                    TreeTimelineEntry(
                        id = harvest.id,
                        kind = ActivityKind.HARVEST,
                        date = harvest.harvestDate,
                        title = if (harvest.firstFruit) "First fruit harvest" else "Harvest",
                        summary = buildHarvestTimelineSummary(harvest),
                        photoPaths = photoPaths.ifEmpty { listOfNotNull(harvest.photoPath) },
                        isHarvest = true
                    )
                }
            ).sortedByDescending(TreeTimelineEntry::date)
    }
    val seasonRollups = remember(timelineEntries) {
        timelineEntries
            .groupBy { Instant.ofEpochMilli(it.date).atZone(OrchardTime.zoneId()).year }
            .map { (year, entries) ->
                TreeSeasonRollup(
                    year = year,
                    budCount = entries.count { it.eventType == EventType.BUD },
                    bloomCount = entries.count { it.eventType == EventType.BLOOM },
                    fruitSetCount = entries.count { it.eventType == EventType.FRUIT_SET },
                    harvestCount = entries.count(TreeTimelineEntry::isHarvest),
                    photoCount = entries.count { it.photoPaths.isNotEmpty() }
                )
            }
            .sortedByDescending(TreeSeasonRollup::year)
    }
    val filteredTimeline = remember(timelineEntries, timelineFilter) {
        timelineEntries.filter { timelineFilter.matches(it) }
    }
    val filteredTimelineByYear = remember(filteredTimeline) {
        filteredTimeline
            .groupBy { Instant.ofEpochMilli(it.date).atZone(OrchardTime.zoneId()).year }
            .toSortedMap(compareByDescending { it })
    }
    val snapshotSectionIndex = 3
    val productionSectionIndex = 4
    val timelineSectionIndex = if (settings.showSalesTools) 7 else 6
    val activeDetailSection = when {
        detailListState.firstVisibleItemIndex >= timelineSectionIndex -> TreeDetailSection.TIMELINE
        detailListState.firstVisibleItemIndex >= productionSectionIndex -> TreeDetailSection.PRODUCTION
        else -> TreeDetailSection.SNAPSHOT
    }

    Scaffold { innerPadding ->
        LazyColumn(
            state = detailListState,
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.tree.cultivar.takeIf(String::isNotBlank)?.let { cultivar ->
                        Text(
                            text = cultivar,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = item.tree.species.ifBlank { item.tree.displayName() },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    item.tree.nickname?.takeIf(String::isNotBlank)?.let { nickname ->
                        Text(
                            text = nickname,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                heroPhoto?.let { photo ->
                    Card(shape = RoundedCornerShape(24.dp)) {
                        AsyncImage(
                            model = File(context.filesDir, "photos/${photo.relativePath}"),
                            contentDescription = "Tree hero photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    }
                } ?: OutlinedCard(shape = RoundedCornerShape(24.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add a photo to this plant")
                    }
                }
                if (item.photos.isNotEmpty()) {
                    LocalPhotoStrip(
                        existingPaths = item.photos.map { photo ->
                            photo.id to File(context.filesDir, "photos/${photo.relativePath}").absolutePath
                        },
                        selectedExistingId = heroPhoto?.id,
                        onSelectExisting = viewModel::setHeroPhoto
                    )
                    if (item.photos.size > 1) {
                        Text(
                            text = "Tap a thumbnail to make it the hero photo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            val actionItems = buildList {
                add(
                    "Add activity" to {
                        onAddLog(item.tree.id)
                    }
                )
                add(
                    "Reminder" to {
                        onAddReminder(item.tree.id)
                    }
                )
                add(
                    "Photo" to {
                        showPhotoActionDialog = true
                    }
                )
                if (settings.showSalesTools && directTreeSale == null) {
                    add(
                        "Sell" to {
                            treeSaleDraft = defaultTreeSaleDraft()
                            treeSaleDraftError = null
                            showTreeSaleDialog = true
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actionItems.forEachIndexed { index, (label, onClick) ->
                    TreeActionButton(
                        label = label,
                        onClick = onClick,
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (index == 0) Modifier.testTag("add_harvest") else Modifier
                            )
                    )
                }
            }
        }
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TreeDetailSection.entries.forEach { section ->
                            FilterChip(
                                selected = activeDetailSection == section,
                                onClick = {
                                    val targetIndex = when (section) {
                                        TreeDetailSection.SNAPSHOT -> snapshotSectionIndex
                                        TreeDetailSection.PRODUCTION -> productionSectionIndex
                                        TreeDetailSection.TIMELINE -> timelineSectionIndex
                                    }
                                    detailScope.launch {
                                        detailListState.animateScrollToItem(targetIndex)
                                    }
                                },
                                label = { Text(section.label) }
                            )
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Plant snapshot") {
                listOfNotNull(
                    (item.location?.name ?: item.tree.orchardName).takeIf(String::isNotBlank),
                    item.tree.sectionName.takeIf(String::isNotBlank)
                ).takeIf(List<String>::isNotEmpty)?.let { placement ->
                    Text(
                        text = placement.joinToString(" - "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFact("Planting", item.tree.plantType.name.replace("_", "-").lowercase())
                    if (item.tree.status != TreeStatus.ACTIVE) {
                        CompactFact("Status", item.tree.status.name.lowercase())
                    }
                    CompactFact("Planted", item.tree.plantedDate.toDateLabel())
                    CompactFact("Stage", item.tree.nurseryStage.label.takeIf { item.tree.nurseryStage != NurseryStage.NONE }.orEmpty())
                    CompactFact("Origin", item.tree.originType.label.takeIf { item.tree.originType != TreeOriginType.UNKNOWN }.orEmpty())
                    CompactFact("Propagation", item.tree.propagationMethod?.label.orEmpty())
                    CompactFact("Custom bloom", "Yes".takeIf { customBloomTimingSummary != null }.orEmpty())
                    CompactFact("Rootstock", item.tree.rootstock.orEmpty())
                    CompactFact("Source", item.tree.source.orEmpty())
                    CompactFact("History", "Previously fruited".takeIf { item.tree.hasFruitedBefore && item.harvests.isEmpty() }.orEmpty())
                }
                forecastSummary?.let { summary ->
                    Text(
                        text = "Bloom forecast: ${summary.patternLabel.ifBlank { summary.headline }}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    summary.timingLabel?.takeIf(String::isNotBlank)?.let { timingLabel ->
                        Text(
                            text = "Estimated bloom: $timingLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                customBloomTimingSummary?.let {
                    Text(
                        text = "Custom bloom window: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (
                    effectivePollination.selfCompatibility != SelfCompatibility.UNKNOWN ||
                    effectivePollination.pollinationMode != PollinationMode.UNKNOWN
                ) {
                    Text(
                        text = listOf(
                            effectivePollination.selfCompatibility.label.takeIf {
                                effectivePollination.selfCompatibility != SelfCompatibility.UNKNOWN
                            },
                            effectivePollination.pollinationMode.label.takeIf {
                                effectivePollination.pollinationMode != PollinationMode.UNKNOWN
                            }
                        ).filterNotNull().joinToString(" - "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                heroPhoto?.let { photo -> if (false) {
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
                if (false && item.photos.size > 1) {
                        Text(
                            text = "Tap any photo below to make it the hero image.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LocalPhotoStrip(
                            existingPaths = item.photos.map { photo ->
                                photo.id to File(context.filesDir, "photos/${photo.relativePath}").absolutePath
                            },
                            selectedExistingId = heroPhoto?.id,
                            onSelectExisting = viewModel::setHeroPhoto
                        )
                } }
                item.tree.pollinationOverrideNote?.takeIf(String::isNotBlank)?.let { note ->
                    Text(text = "Pollination note: $note", style = MaterialTheme.typography.bodySmall)
                }
                item.tree.notes.takeIf(String::isNotBlank)?.let { notes ->
                    Text(text = notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            SectionCard("Production summary") {
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
                    if (seasonRollups.isNotEmpty()) {
                        Text("Recent seasons", style = MaterialTheme.typography.labelLarge)
                        seasonRollups.take(3).forEach { rollup ->
                            Text(
                                "${rollup.year} - ${rollup.budCount} buds - ${rollup.bloomCount} blooms - " +
                                    "${rollup.fruitSetCount} fruit sets - ${rollup.harvestCount} harvests" +
                                    if (rollup.photoCount > 0) " - ${rollup.photoCount} photos" else ""
                            )
                        }
                    }
                }
            }
        }
        if (settings.showSalesTools) {
            item {
                SectionCard("Revenue") {
                    val revenueSummary = item.revenueSummary
                    if (revenueSummary.totalRevenue <= 0.0) {
                        Text("No plant or harvest sales logged yet.")
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompactFact("Direct", "$${revenueSummary.directRevenue.displayAmount()}")
                            CompactFact("Lineage", "$${revenueSummary.lineageRevenue.displayAmount()}")
                            CompactFact("Total", "$${revenueSummary.totalRevenue.displayAmount()}")
                            CompactFact("Plant sales", revenueSummary.treeSaleCount.toString())
                            CompactFact("Harvest sales", revenueSummary.harvestSaleCount.toString())
                            if (revenueSummary.lineageSaleCount > 0) {
                                CompactFact("Descendant sales", revenueSummary.lineageSaleCount.toString())
                            }
                            revenueSummary.lastSaleDate?.let { CompactFact("Last sale", it.toDateLabel()) }
                        }
                        if (revenueSummary.directPlantRevenue > 0.0) {
                            Text(
                                text = "Plants sold: $${revenueSummary.directPlantRevenue.displayAmount()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (revenueSummary.directHarvestRevenue > 0.0) {
                            Text(
                                text = "Harvest sold: $${revenueSummary.directHarvestRevenue.displayAmount()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (item.sales.isNotEmpty()) {
                            Text("Recent direct sales", style = MaterialTheme.typography.labelLarge)
                            item.sales
                                .sortedByDescending(SaleEntity::soldAt)
                                .take(3)
                                .forEach { sale ->
                                    Text(sale.revenueLine())
                                }
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
            SectionCard("Activity timeline") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreeTimelineFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = timelineFilter == filter,
                            onClick = { timelineFilter = filter },
                            label = { Text(filter.label) }
                        )
                    }
                }
                if (filteredTimelineByYear.isEmpty()) {
                    Text("No activity on this tree yet.")
                } else {
                    filteredTimelineByYear.forEach { (year, entries) ->
                        val rollup = seasonRollups.firstOrNull { it.year == year }
                        Text(year.toString(), style = MaterialTheme.typography.titleSmall)
                        rollup?.let {
                            Text(
                                "${it.budCount} buds - ${it.bloomCount} blooms - ${it.fruitSetCount} fruit sets - ${it.harvestCount} harvests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        entries.forEach { entry ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenLog(entry.kind, entry.id) },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = entry.date.toDateLabel(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = entry.summary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    entry.photoPath?.let { photoPath ->
                                        Box {
                                            Card(
                                                modifier = Modifier.size(72.dp),
                                                shape = RoundedCornerShape(18.dp)
                                            ) {
                                                AsyncImage(
                                                    model = File(context.filesDir, "photos/$photoPath"),
                                                    contentDescription = "Log photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            if (entry.photoPaths.size > 1) {
                                                Text(
                                                    text = "+${entry.photoPaths.size - 1}",
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(6.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Propagate from this plant") {
                Text(
                    "Choose a method below to create a linked child plant from this mother plant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PropagationMethod.entries.forEach { method ->
                        FilterChip(
                            selected = false,
                            onClick = { pendingPropagationMethod = method },
                            label = { Text(method.label) }
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditTree(item.tree.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit plant")
                }
                OutlinedButton(
                    onClick = viewModel::requestDeleteConfirmation,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete tree")
                }
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

private enum class TreeTimelineFilter(val label: String) {
    ALL("All"),
    SEASONAL("Seasonal"),
    HARVESTS("Harvests"),
    ISSUES("Issues"),
    CARE("Care"),
    OBSERVATIONS("Observations")
}

private data class TreeTimelineEntry(
    val id: String,
    val kind: ActivityKind,
    val date: Long,
    val title: String,
    val summary: String,
    val eventType: EventType? = null,
    val photoPaths: List<String> = emptyList(),
    val photoPath: String? = photoPaths.firstOrNull(),
    val isHarvest: Boolean = false
)

private data class TreeSeasonRollup(
    val year: Int,
    val budCount: Int,
    val bloomCount: Int,
    val fruitSetCount: Int,
    val harvestCount: Int,
    val photoCount: Int
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

private fun treeLocationLabel(location: GrowingLocationEntity): String = location.name

@Composable
private fun BloomIntensityChip(
    monthLabel: String,
    intensity: Int,
    onClick: () -> Unit
) {
    val (label, selected) = when (intensity.coerceIn(0, 3)) {
        0 -> "$monthLabel Off" to false
        1 -> "$monthLabel Watch" to true
        2 -> "$monthLabel Peak" to true
        else -> "$monthLabel Strong" to true
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
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

private fun TreeTimelineFilter.matches(entry: TreeTimelineEntry): Boolean = when (this) {
    TreeTimelineFilter.ALL -> true
    TreeTimelineFilter.SEASONAL -> entry.isHarvest || entry.eventType in setOf(EventType.BUD, EventType.BLOOM, EventType.FRUIT_SET)
    TreeTimelineFilter.HARVESTS -> entry.isHarvest
    TreeTimelineFilter.ISSUES -> entry.eventType in setOf(
        EventType.PEST_OBSERVED,
        EventType.DISEASE_OBSERVED,
        EventType.FROST_DAMAGE,
        EventType.HEAT_STRESS
    )
    TreeTimelineFilter.CARE -> entry.eventType in setOf(
        EventType.PLANTED,
        EventType.REPOTTED,
        EventType.PRUNED,
        EventType.FERTILIZED,
        EventType.SPRAYED,
        EventType.GRAFTED,
        EventType.WATERED
    )
    TreeTimelineFilter.OBSERVATIONS -> entry.eventType == EventType.NOTE
}

private fun EventType.treeLabel(): String = name.lowercase()
    .replace("_", " ")
    .replaceFirstChar(Char::uppercase)

private fun buildEventTimelineSummary(event: com.dillon.orcharddex.data.local.EventEntity): String = listOfNotNull(
    event.quantityValue?.let { quantity ->
        listOf(quantity.displayAmount(), event.quantityUnit.orEmpty()).joinToString(" ").trim().takeIf(String::isNotBlank)
    },
    event.cost?.let { "Cost $${it.displayAmount()}" },
    event.notes.takeIf(String::isNotBlank)
).joinToString(" - ").ifBlank { "No extra details." }

private fun buildHarvestTimelineSummary(harvest: HarvestEntity): String = listOfNotNull(
    "${harvest.quantityValue.displayAmount()} ${harvest.quantityUnit}".trim(),
    "Quality ${harvest.qualityRating}/5",
    "First fruit".takeIf { harvest.firstFruit },
    harvest.notes.takeIf(String::isNotBlank)
).joinToString(" - ")

private fun defaultTreeSaleDraft(): SaleDraftState = SaleDraftState(
    quantityValue = "1",
    quantityUnit = "plant"
)

private fun SaleEntity.revenueLine(): String = buildString {
    append(
        when (saleKind) {
            SaleKind.TREE -> "Plant sale"
            SaleKind.HARVEST -> "Harvest sale"
        }
    )
    append(" • ")
    append(soldAt.toDateLabel())
    append(" • $")
    append(totalPrice.displayAmount())
    append(" • ")
    append(saleChannel.label)
}
