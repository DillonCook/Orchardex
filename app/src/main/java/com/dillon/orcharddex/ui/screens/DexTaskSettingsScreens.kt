package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.BuildConfig
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.DexCultivarEntry
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.GrowingLocationInput
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.LocationSearchResult
import com.dillon.orcharddex.data.model.MicroclimateFlag
import com.dillon.orcharddex.data.model.NurseryStage
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.phenology.CatalogSpeciesReferenceEntry
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.ui.autocompleteSpeciesOptions
import com.dillon.orcharddex.ui.existingCultivarAutocompleteOptions
import com.dillon.orcharddex.ui.normalizeAutocomplete
import com.dillon.orcharddex.ui.components.ChoiceChipsRow
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.components.TimezonePickerField
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.toDateTimeLabel
import com.dillon.orcharddex.ui.viewmodel.DexBlockInventoryCardModel
import com.dillon.orcharddex.ui.viewmodel.DexPlantSortOption
import com.dillon.orcharddex.ui.viewmodel.DexViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderListViewModel
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel
import androidx.compose.material3.surfaceColorAtElevation
import java.io.File

@Composable
fun DexScreen(
    viewModel: DexViewModel,
    settings: AppSettings,
    onAddTree: () -> Unit,
    onTreeClick: (String) -> Unit,
    onQuickLog: (String) -> Unit
) {
    val dex by viewModel.dex.collectAsStateWithLifecycle()
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val browserState = viewModel.browserState
    val defaultLocationProfile = remember(settings) { settings.forecastLocationProfile() }
    var wishlistVisible by rememberSaveable { mutableStateOf(false) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    val hasPlantFilters = browserState.search.isNotBlank() ||
        browserState.speciesFilter != null ||
        browserState.statusFilter != null ||
        browserState.plantTypeFilter != null ||
        browserState.nurseryStageFilter != null

    WishlistEntryDialog(viewModel)
    if (wishlistVisible) {
        WishlistEntriesDialog(
            entries = dex.wishlistEntries,
            onDismiss = { wishlistVisible = false },
            onDelete = viewModel::deleteWishlist
        )
    }
    if (filtersVisible) {
        AlertDialog(
            onDismissRequest = { filtersVisible = false },
            title = { Text("Filter plants") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChoiceChipsRow(browserState.speciesOptions.take(12), browserState.speciesFilter, onSelected = viewModel::updateSpeciesFilter)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = browserState.statusFilter == null, onClick = { viewModel.updateStatusFilter(null) }, label = { Text("Any status") })
                        TreeStatus.entries.forEach { status ->
                            FilterChip(
                                selected = browserState.statusFilter == status,
                                onClick = { viewModel.updateStatusFilter(status) },
                                label = { Text(status.name.lowercase()) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = browserState.plantTypeFilter == null, onClick = { viewModel.updatePlantTypeFilter(null) }, label = { Text("Any planting") })
                        FilterChip(selected = browserState.plantTypeFilter == PlantType.IN_GROUND, onClick = { viewModel.updatePlantTypeFilter(PlantType.IN_GROUND) }, label = { Text("In-ground") })
                        FilterChip(selected = browserState.plantTypeFilter == PlantType.CONTAINER, onClick = { viewModel.updatePlantTypeFilter(PlantType.CONTAINER) }, label = { Text("Container") })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = browserState.nurseryStageFilter == null, onClick = { viewModel.updateNurseryStageFilter(null) }, label = { Text("Any stage") })
                        NurseryStage.entries.filterNot { it == NurseryStage.NONE }.forEach { stage ->
                            FilterChip(
                                selected = browserState.nurseryStageFilter == stage,
                                onClick = { viewModel.updateNurseryStageFilter(stage) },
                                label = { Text(stage.label) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DexPlantSortOption.entries.forEach { option ->
                            FilterChip(
                                selected = browserState.sort == option,
                                onClick = { viewModel.updateSort(option) },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { filtersVisible = false }) { Text("Done") } },
            dismissButton = {
                TextButton(onClick = viewModel::resetBrowserFilters) { Text("Reset") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTree, shape = RoundedCornerShape(16.dp), modifier = Modifier.testTag("add_tree")) {
                Icon(Icons.Outlined.Add, contentDescription = "Add plant")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = viewModel::showAddDialog) { Text("Add to wishlist") }
                        OutlinedButton(onClick = { wishlistVisible = true }) {
                            Text("View wishlist (${dex.wishlistEntries.size})")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = browserState.search,
                            onValueChange = viewModel::updateSearch,
                            modifier = Modifier.weight(1f),
                            label = { Text("Search plants or cultivars") }
                        )
                        OutlinedButton(onClick = { filtersVisible = true }) { Text("Filters") }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !browserState.blockView, onClick = { viewModel.setBlockView(false) }, label = { Text("Plants") })
                        FilterChip(selected = browserState.blockView, onClick = { viewModel.setBlockView(true) }, label = { Text("Blocks") })
                    }
                }
            }
            if (browserState.filteredPlants.isEmpty()) {
                item {
                    if (trees.isEmpty()) {
                        EmptyStateCard(
                            title = "Start your orchard",
                            message = "Add your first plant to start tracking cultivars, reminders, and harvests.",
                            primaryActionLabel = "Add first plant",
                            onPrimaryAction = onAddTree
                        )
                    } else {
                        EmptyStateCard(
                            title = "No plants found",
                            message = "Try a different search, clear filters, or add a plant.",
                            primaryActionLabel = if (hasPlantFilters) "Clear filters" else "Add plant",
                            onPrimaryAction = if (hasPlantFilters) viewModel::resetBrowserFilters else onAddTree,
                            secondaryActionLabel = if (hasPlantFilters) "Add plant" else null,
                            onSecondaryAction = if (hasPlantFilters) onAddTree else null
                        )
                    }
                }
            } else if (browserState.blockView) {
                val selectedBlock = browserState.groupedBlocks.firstOrNull { it.id == browserState.selectedBlockId }
                item {
                    BlockMapSection(
                        blocks = browserState.groupedBlocks,
                        selectedBlockId = browserState.selectedBlockId,
                        onSelectBlock = viewModel::setSelectedBlock
                    )
                }
                selectedBlock?.let { block ->
                    item(key = block.id) {
                        BlockInventoryCard(
                            block = block,
                            onTreeClick = onTreeClick,
                            onQuickLog = onQuickLog
                        )
                    }
                }
            } else {
                items(browserState.filteredPlants, key = { it.tree.id }) { item ->
                    DexPlantCard(
                        item = item,
                        onClick = { onTreeClick(item.tree.id) },
                        onQuickAdd = { onQuickLog(item.tree.id) }
                    )
                }
            }

        }
    }
}

@Composable
private fun BlockMapSection(
    blocks: List<DexBlockInventoryCardModel>,
    selectedBlockId: String?,
    onSelectBlock: (String) -> Unit
) {
    val assignedBlockCount = remember(blocks) { blocks.count { !it.isUnassigned } }
    val unassignedPlantCount = remember(blocks) {
        blocks.filter(DexBlockInventoryCardModel::isUnassigned).sumOf(DexBlockInventoryCardModel::plantCount)
    }
    SectionCard("Block map") {
        if (assignedBlockCount == 0 && unassignedPlantCount > 0) {
            Text(
                text = "Everything is still unassigned. Add an Area / Section / Block on a plant to turn this into a real orchard section view.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Pick a block to focus the orchard tab on one section at a time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactFact("Blocks", assignedBlockCount.toString())
            if (unassignedPlantCount > 0) {
                CompactFact("Unassigned plants", unassignedPlantCount.toString())
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            blocks.forEach { block ->
                BlockMapTile(
                    block = block,
                    selected = block.id == selectedBlockId,
                    onClick = { onSelectBlock(block.id) }
                )
            }
        }
    }
}

@Composable
private fun BlockMapTile(
    block: DexBlockInventoryCardModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedContainer = MaterialTheme.colorScheme.primaryContainer
    val selectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContainer = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    val unselectedContent = MaterialTheme.colorScheme.onSurface
    OutlinedCard(
        modifier = Modifier
            .widthIn(min = 164.dp, max = 220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) selectedContainer else unselectedContainer,
            contentColor = if (selected) selectedContent else unselectedContent
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = block.blockName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = block.locationName,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) selectedContent.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${block.plantCount} plants",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = listOfNotNull(
                    "${block.dueTaskCount} due".takeIf { block.dueTaskCount > 0 },
                    "${block.saleReadyCount} sale-ready".takeIf { block.saleReadyCount > 0 },
                    "${block.rootingCount} rooting".takeIf { block.rootingCount > 0 }
                ).joinToString(" • ").ifBlank { "No active alerts" },
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) selectedContent.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WishlistEntriesDialog(
    entries: List<com.dillon.orcharddex.data.local.WishlistCultivarEntity>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wishlist") },
        text = {
            if (entries.isEmpty()) {
                Text("No wishlist entries yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = entry.cultivar.takeIf(String::isNotBlank)?.let { "${entry.species} - $it" } ?: entry.species,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val detailLine = buildString {
                                        if (entry.acquired) append("Acquired")
                                        if (entry.notes.isNotBlank()) {
                                            if (isNotEmpty()) append(" - ")
                                            append(entry.notes)
                                        }
                                    }
                                    if (detailLine.isNotBlank()) {
                                        Text(
                                            text = detailLine,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                TextButton(onClick = { onDelete(entry.id) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun DexCultivarCard(entry: DexCultivarEntry, onClick: (() -> Unit)?) {
    OutlinedCard(
        modifier = Modifier.widthIn(min = 160.dp).let { if (onClick == null) it else it.clickable(onClick = onClick) },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entry.cultivar.ifBlank { "Unknown cultivar" }, style = MaterialTheme.typography.titleMedium)
            Text(entry.species, style = MaterialTheme.typography.bodySmall)
            Text(
                buildString {
                    append("${entry.activeTreeCount} active")
                    if (entry.inactiveTreeCount > 0) append(" - ${entry.inactiveTreeCount} inactive")
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (entry.firstFruitAchieved) Text("First fruit reached", style = MaterialTheme.typography.bodySmall)
            if (entry.wishlist) Text("Wishlist match tracked", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DexPlantCard(
    item: TreeListItem,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val context = LocalContext.current
    val thumbnailLabel = item.tree.cultivar.takeIf(String::isNotBlank)?.take(2)
        ?: item.tree.species.take(2)
    val cultivarLabel = item.tree.dexCultivarLabel()
    val speciesLabel = item.tree.dexSpeciesLabel()
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp)
    ) {
        BoxWithConstraints {
            val stackedLayout = maxWidth < 560.dp
            val thumbnailWidth = if (stackedLayout) 104.dp else 118.dp

            @Composable
            fun Thumbnail(modifier: Modifier = Modifier) {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    if (item.mainPhotoPath != null) {
                        AsyncImage(
                            model = File(context.filesDir, "photos/${item.mainPhotoPath}"),
                            contentDescription = "Plant photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = thumbnailLabel.uppercase(),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }

            @Composable
            fun PlantLabels(modifier: Modifier = Modifier) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cultivarLabel?.let { cultivar ->
                        Text(
                            text = cultivar,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = speciesLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            @Composable
            fun QuickAddButton(size: androidx.compose.ui.unit.Dp) {
                FilledTonalIconButton(
                    onClick = onQuickAdd,
                    modifier = Modifier
                        .size(size)
                        .testTag("dex_quick_add_${item.tree.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add activity"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .heightIn(min = thumbnailWidth)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(if (stackedLayout) 12.dp else 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Thumbnail(
                    modifier = Modifier
                        .width(thumbnailWidth)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    PlantLabels()
                }
                QuickAddButton(size = if (stackedLayout) 38.dp else 40.dp)
            }
        }
    }
}

private fun TreeEntity.dexCultivarLabel(): String? = cultivar.trim().takeIf(String::isNotBlank)

private fun TreeEntity.dexSpeciesLabel(): String = species.trim()

@Composable
private fun BlockInventoryCard(
    block: DexBlockInventoryCardModel,
    onTreeClick: (String) -> Unit,
    onQuickLog: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(block.blockName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = block.locationName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactFact("Plants", block.plantCount.toString())
                if (block.dueTaskCount > 0) {
                    CompactFact("Due tasks", block.dueTaskCount.toString())
                }
                if (block.saleReadyCount > 0) {
                    CompactFact("Sale-ready", block.saleReadyCount.toString())
                }
                if (block.rootingCount > 0) {
                    CompactFact("Rooting", block.rootingCount.toString())
                }
            }
            Text(
                text = "Plants in this section",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            block.plants.forEach { plant ->
                DexPlantCard(
                    item = plant,
                    onClick = { onTreeClick(plant.tree.id) },
                    onQuickAdd = { onQuickLog(plant.tree.id) }
                )
            }
        }
    }
}

private enum class ReminderFilterTab(val label: String) {
    ALL("All"),
    DUE_SOON("Due soon"),
    OVERDUE("Overdue"),
    COMPLETED("Completed")
}

private enum class TaskBoardStat(val label: String) {
    OPEN("Open"),
    OVERDUE("Overdue"),
    DUE_IN_7_DAYS("Due in 7 days"),
    DONE_OR_PAUSED("Done or paused")
}

@Composable
fun TasksScreen(
    viewModel: ReminderListViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (String) -> Unit
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ReminderFilterTab.ALL) }
    var treeFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var speciesFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var completeReminderId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTaskBoardStat by rememberSaveable { mutableStateOf<TaskBoardStat?>(null) }

    val now = System.currentTimeMillis()
    val dueSoonCutoff = now + 7L * 24 * 60 * 60 * 1000
    val treeOptions = reminders.mapNotNull { it.treeLabel }.distinct().sorted()
    val speciesOptions = reminders.mapNotNull { it.species }.distinct().sorted()
    val filtered = reminders.filter { item ->
        val reminder = item.reminder
        val query = search.trim().lowercase()
        val matchesQuery = query.isBlank() || listOf(reminder.title, reminder.notes, item.treeLabel.orEmpty(), item.species.orEmpty()).any {
            it.lowercase().contains(query)
        }
        val matchesTab = when (filter) {
            ReminderFilterTab.ALL -> true
            ReminderFilterTab.DUE_SOON -> reminder.completedAt == null && reminder.enabled && reminder.dueAt in now..dueSoonCutoff
            ReminderFilterTab.OVERDUE -> reminder.completedAt == null && reminder.enabled && reminder.dueAt < now
            ReminderFilterTab.COMPLETED -> reminder.completedAt != null || !reminder.enabled
        }
        matchesQuery && matchesTab && (treeFilter == null || item.treeLabel == treeFilter) && (speciesFilter == null || item.species == speciesFilter)
    }

    if (completeReminderId != null) {
        AlertDialog(
            onDismissRequest = { completeReminderId = null },
            title = { Text("Complete reminder") },
            text = { Text("Also add a matching event?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markDone(completeReminderId!!, true)
                    completeReminderId = null
                }) { Text("Done + event") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.markDone(completeReminderId!!, false)
                        completeReminderId = null
                    }) { Text("Done only") }
                    TextButton(onClick = { completeReminderId = null }) { Text("Cancel") }
                }
            }
        )
    }

    selectedTaskBoardStat?.let { stat ->
        TaskBoardDetailDialog(
            stat = stat,
            reminders = reminders,
            now = now,
            dueSoonCutoff = dueSoonCutoff,
            onDismiss = { selectedTaskBoardStat = null },
            onOpenReminder = { reminderId ->
                selectedTaskBoardStat = null
                onEditReminder(reminderId)
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = "New reminder")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Task board") {
                    Text("Scan overdue tasks first, then work through the next 7 days.")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Open",
                                value = reminders.count { it.reminder.completedAt == null && it.reminder.enabled }.toString(),
                                minWidth = 0.dp,
                                onClick = { selectedTaskBoardStat = TaskBoardStat.OPEN }
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Overdue",
                                value = reminders.count { it.reminder.completedAt == null && it.reminder.enabled && it.reminder.dueAt < now }.toString(),
                                minWidth = 0.dp,
                                onClick = { selectedTaskBoardStat = TaskBoardStat.OVERDUE }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Due in 7 days",
                                value = reminders.count { it.reminder.completedAt == null && it.reminder.enabled && it.reminder.dueAt in now..dueSoonCutoff }.toString(),
                                minWidth = 0.dp,
                                onClick = { selectedTaskBoardStat = TaskBoardStat.DUE_IN_7_DAYS }
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Done or paused",
                                value = reminders.count { it.reminder.completedAt != null || !it.reminder.enabled }.toString(),
                                minWidth = 0.dp,
                                onClick = { selectedTaskBoardStat = TaskBoardStat.DONE_OR_PAUSED }
                            )
                        }
                    }
                }
            }
            item {
                SectionCard("Find reminders") {
                    OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search reminders") })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderFilterTab.entries.forEach { tab ->
                            FilterChip(selected = filter == tab, onClick = { filter = tab }, label = { Text(tab.label) })
                        }
                    }
                    if (treeOptions.isNotEmpty()) ChoiceChipsRow(treeOptions, treeFilter, onSelected = { treeFilter = it })
                    if (speciesOptions.isNotEmpty()) ChoiceChipsRow(speciesOptions, speciesFilter, onSelected = { speciesFilter = it })
                    Text("${filtered.size} reminders shown", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (filtered.isEmpty()) {
                item {
                    if (reminders.isEmpty()) {
                        EmptyStateCard(
                            title = "No reminders yet",
                            message = "Add your first reminder to keep watering, fertilizing, and harvest checks on track.",
                            primaryActionLabel = "Add first reminder",
                            onPrimaryAction = onAddReminder
                        )
                    } else {
                        EmptyStateCard(
                            title = "No reminders found",
                            message = "Add a reminder or change the filters to bring tasks back into view."
                        )
                    }
                }
            } else {
                items(filtered, key = { it.reminder.id }) { item ->
                    ReminderCard(
                        item = item,
                        now = now,
                        dueSoonCutoff = dueSoonCutoff,
                        onEdit = { onEditReminder(item.reminder.id) },
                        onComplete = { completeReminderId = item.reminder.id },
                        onDelete = { viewModel.deleteReminder(item.reminder.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskBoardDetailDialog(
    stat: TaskBoardStat,
    reminders: List<com.dillon.orcharddex.data.model.ReminderListItem>,
    now: Long,
    dueSoonCutoff: Long,
    onDismiss: () -> Unit,
    onOpenReminder: (String) -> Unit
) {
    val matchingItems = reminders.filter { stat.matches(it.reminder, now, dueSoonCutoff) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stat.label) },
        text = {
            if (matchingItems.isEmpty()) {
                Text(stat.emptyMessage())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matchingItems, key = { it.reminder.id }) { item ->
                        TaskBoardDetailRow(item = item, onClick = { onOpenReminder(item.reminder.id) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun TaskBoardDetailRow(
    item: com.dillon.orcharddex.data.model.ReminderListItem,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(item.reminder.title, style = MaterialTheme.typography.titleMedium)
            Text(item.treeLabel ?: "General orchard", style = MaterialTheme.typography.bodySmall)
            Text(item.reminder.dueLine(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun TaskBoardStat.matches(reminder: ReminderEntity, now: Long, dueSoonCutoff: Long): Boolean = when (this) {
    TaskBoardStat.OPEN -> reminder.completedAt == null && reminder.enabled
    TaskBoardStat.OVERDUE -> reminder.completedAt == null && reminder.enabled && reminder.dueAt < now
    TaskBoardStat.DUE_IN_7_DAYS -> reminder.completedAt == null && reminder.enabled && reminder.dueAt in now..dueSoonCutoff
    TaskBoardStat.DONE_OR_PAUSED -> reminder.completedAt != null || !reminder.enabled
}

private fun TaskBoardStat.emptyMessage(): String = when (this) {
    TaskBoardStat.OPEN -> "No open tasks right now."
    TaskBoardStat.OVERDUE -> "No overdue tasks right now."
    TaskBoardStat.DUE_IN_7_DAYS -> "Nothing due in the next 7 days."
    TaskBoardStat.DONE_OR_PAUSED -> "No done or paused tasks yet."
}

@Composable
private fun ReminderCard(
    item: com.dillon.orcharddex.data.model.ReminderListItem,
    now: Long,
    dueSoonCutoff: Long,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val reminder = item.reminder
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
            Text(item.treeLabel ?: "General orchard", style = MaterialTheme.typography.bodyMedium)
            Text(reminder.dueLine(), style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactFact("Status", reminder.statusLabel(now, dueSoonCutoff))
                item.species?.let { CompactFact("Species", it) }
                reminder.recurrenceLabel()?.let { CompactFact("Repeats", it) }
            }
            if (reminder.notes.isNotBlank()) {
                Text(reminder.notes, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                if (reminder.completedAt == null && reminder.enabled) Button(onClick = onComplete) { Text("Mark done") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onPrivacy: () -> Unit,
    onOpenCatalog: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    var editingLocation by remember { mutableStateOf<GrowingLocationEditorState?>(null) }
    var deleteLocation by remember { mutableStateOf<GrowingLocationEntity?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let(viewModel::exportBackup)
    }
    val diagnosticsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::exportDiagnostics)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::validateImport)
    }
    val defaultLocationId = settings.defaultLocationId.takeIf(String::isNotBlank)
    val currentDefaultLocation = remember(locations, defaultLocationId) {
        defaultLocationId?.let { id -> locations.firstOrNull { it.id == id } }
    }
    val additionalLocations = remember(locations, defaultLocationId) {
        locations.filterNot { it.id == defaultLocationId }
    }

    editingLocation?.let { draft ->
        GrowingLocationEditorDialog(
            state = draft,
            searchQuery = viewModel.locationSearchQuery,
            searchResults = viewModel.locationSearchResults,
            searchBusy = viewModel.locationSearchBusy,
            searchError = viewModel.locationSearchError,
            onStateChange = { editingLocation = it },
            onSearchQueryChange = viewModel::updateLocationSearchQuery,
            onApplySearchResult = { result ->
                editingLocation = (editingLocation ?: draft).applySearchResult(result)
                viewModel.clearLocationSearch()
            },
            onDismiss = {
                editingLocation = null
                viewModel.clearLocationSearch()
            },
            onSave = {
                val timezoneId = draft.timezoneId.trim().ifBlank { settings.timezoneId }
                viewModel.saveGrowingLocation(
                    draft.toInput(
                        fallbackName = draft.name.ifBlank { "Orchard" },
                        fallbackTimezoneId = timezoneId
                    )
                )
                editingLocation = null
                viewModel.clearLocationSearch()
            }
        )
    }
    deleteLocation?.let { location ->
        AlertDialog(
            onDismissRequest = { deleteLocation = null },
            title = { Text("Delete ${location.name}?") },
            text = {
                Text(
                    "Trees already assigned to this orchard keep their orchard name text, but the linked location/climate profile will be removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrowingLocation(location.id)
                        deleteLocation = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteLocation = null }) { Text("Cancel") }
            }
        )
    }

    viewModel.pendingImport?.let { validation ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPendingImport,
            title = { Text("Import backup?") },
            text = {
                Text(
                    "Archive from ${validation.appVersion}\n" +
                        "${validation.treeCount} trees, ${validation.locationCount} locations, " +
                        "${validation.reminderCount} reminders, ${validation.photoCount} photos.\n" +
                        "This replaces current app data."
                )
            },
            confirmButton = { TextButton(onClick = viewModel::importReplaceAll) { Text("Import") } },
            dismissButton = { TextButton(onClick = viewModel::dismissPendingImport) { Text("Cancel") } }
        )
    }
    if (viewModel.confirmClearAll) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAll,
            title = { Text("Clear all data?") },
            text = { Text("This permanently removes all local trees, photos, reminders, and wishlist entries.") },
            confirmButton = { TextButton(onClick = viewModel::clearAllData) { Text("Clear all") } },
            dismissButton = { TextButton(onClick = viewModel::dismissClearAll) { Text("Cancel") } }
        )
    }
    if (BuildConfig.DEBUG && viewModel.confirmLoadSample) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLoadSample,
            title = { Text("Load sample orchard data?") },
            text = { Text("This replaces current app data with the built-in sample orchard.") },
            confirmButton = { TextButton(onClick = viewModel::loadSampleData) { Text("Replace data") } },
            dismissButton = { TextButton(onClick = viewModel::dismissLoadSample) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Default orchard") {
                Text(
                    "Trees without their own assigned location use this orchard by default. Give it your own orchard name, then search a nearby place to fill timezone, coordinates, and elevation.",
                    style = MaterialTheme.typography.bodySmall
                )
                val defaultLocation = currentDefaultLocation
                if (defaultLocation == null) {
                    Text(
                        "No default orchard is saved yet. Create one first so bloom timing, reminders, and date formatting share one location profile.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(defaultLocation.name, style = MaterialTheme.typography.titleMedium)
                    listOfNotNull(
                        defaultLocation.timezoneId.takeIf(String::isNotBlank),
                        defaultLocation.usdaZoneCode?.let(BloomForecastEngine::zoneLabelForCode)
                    ).takeIf(List<String>::isNotEmpty)?.let { details ->
                        Text(
                            details.joinToString(" - "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactFact("Hemisphere", defaultLocation.hemisphere.label)
                        defaultLocation.latitudeDeg?.let { CompactFact("Latitude", it.toString()) }
                        defaultLocation.elevationM?.let { CompactFact("Elevation", "${it.toInt()} m") }
                        CompactFact("Climate", defaultLocation.climateSource ?: "Not cached")
                    }
                }
                Text(
                    "Search only fills place data. Your orchard name stays custom. Latitude, longitude, elevation, and chill hours are required. USDA stays optional.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearLocationSearch()
                            editingLocation = GrowingLocationEditorState.from(currentDefaultLocation, settings)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (currentDefaultLocation == null) "Create orchard" else "Edit orchard")
                    }
                }
                OutlinedButton(
                    onClick = {
                        viewModel.clearLocationSearch()
                        editingLocation = GrowingLocationEditorState.from(
                            null,
                            settings
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add new orchard / growing location")
                }
            }
        }
        if (additionalLocations.isNotEmpty()) {
            item {
                SectionCard("Additional orchards / locations") {
                    additionalLocations.forEach { location ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(location.name, style = MaterialTheme.typography.titleMedium)
                                listOfNotNull(
                                    location.timezoneId.takeIf(String::isNotBlank),
                                    location.usdaZoneCode?.let(BloomForecastEngine::zoneLabelForCode)
                                ).takeIf(List<String>::isNotEmpty)?.let { details ->
                                    Text(
                                        details.joinToString(" - "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CompactFact("Hemisphere", location.hemisphere.label)
                                    location.latitudeDeg?.let { CompactFact("Latitude", it.toString()) }
                                    location.elevationM?.let { CompactFact("Elevation", "${it.toInt()} m") }
                                    CompactFact("Climate", location.climateSource ?: "Not cached")
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.clearLocationSearch()
                                            editingLocation = GrowingLocationEditorState.from(location)
                                        },
                                        modifier = Modifier.widthIn(min = 112.dp)
                                    ) {
                                        Text("Edit")
                                    }
                                    OutlinedButton(
                                        onClick = { deleteLocation = location },
                                        modifier = Modifier.widthIn(min = 112.dp),
                                        enabled = !viewModel.busy
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Backups") {
                Text(
                    "Export a backup before device changes, reinstalls, or big orchard cleanups.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(onClick = { exportLauncher.launch("orchardex-${BuildConfig.VERSION_NAME}.orchardex.zip") }, modifier = Modifier.fillMaxWidth().testTag("export_backup")) {
                    Text("Export backup")
                }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import backup")
                }
            }
        }
        item {
            SectionCard("Appearance") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(selected = settings.themeMode == mode, onClick = { viewModel.updateTheme(mode) }, label = { Text(mode.name.lowercase()) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dynamic color")
                    Switch(checked = settings.dynamicColor, onCheckedChange = viewModel::updateDynamicColor)
                }
            }
        }
        item {
            SectionCard("Optional tools") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Show sales and money tools")
                        Text(
                            "Reveal sell buttons, revenue cards, and inline harvest-sale fields.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.showSalesTools,
                        onCheckedChange = viewModel::updateShowSalesTools
                    )
                }
            }
        }
        item {
            SectionCard("Default reminder behavior") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeadTimeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.defaultLeadTimeMode == mode,
                            onClick = { viewModel.updateDefaultReminder(mode, settings.defaultCustomLeadHours) },
                            label = { Text(mode.name.lowercase().replace("_", " ")) }
                        )
                    }
                }
                if (settings.defaultLeadTimeMode == LeadTimeMode.CUSTOM_HOURS) Text("Custom lead time: ${settings.defaultCustomLeadHours} hours")
            }
        }
        item {
            SectionCard("Diagnostics") {
                Text(
                    "Create a local diagnostics report with recent crashes, screen breadcrumbs, and slow-path timings. Nothing is uploaded automatically.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = { diagnosticsLauncher.launch("orchardex-diagnostics-${BuildConfig.VERSION_NAME}.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export diagnostics")
                }
            }
        }
        item {
            SectionCard("Data") {
                if (BuildConfig.DEBUG) {
                    OutlinedButton(onClick = viewModel::requestLoadSample, modifier = Modifier.fillMaxWidth()) { Text("Load sample orchard data") }
                }
                OutlinedButton(onClick = viewModel::requestClearAll, modifier = Modifier.fillMaxWidth()) { Text("Clear all data") }
            }
        }
        item {
            SectionCard("Catalog") {
                Text("Open the reference-grade plant catalog for bloom timing, fertility grading, and cultivar notes.")
                OutlinedButton(
                    onClick = onOpenCatalog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open plant catalog")
                }
            }
        }
        item {
            SectionCard("About") {
                Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Text("Offline-first app. No account, analytics, or cloud sync. Optional online location search and climate lookup are used only when you trigger them.")
                TextButton(onClick = onPrivacy) { Text("Privacy policy") }
                if (viewModel.busy) Text("Working...")
            }
        }
    }
}

private data class GrowingLocationEditorState(
    val id: String? = null,
    val name: String = "",
    val searchedPlaceLabel: String = "",
    val countryCode: String = "",
    val timezoneId: String = "",
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    val latitudeDeg: String = "",
    val longitudeDeg: String = "",
    val elevationM: String = "",
    val usdaZoneLabel: String = "Not set",
    val chillHoursBand: ChillHoursBand = ChillHoursBand.UNKNOWN,
    val microclimateFlags: Set<MicroclimateFlag> = emptySet(),
    val notes: String = ""
) {
    fun toInput(fallbackName: String, fallbackTimezoneId: String): GrowingLocationInput = GrowingLocationInput(
        id = id,
        name = name.trim().ifBlank { fallbackName },
        countryCode = countryCode.trim(),
        timezoneId = timezoneId.trim().ifBlank { fallbackTimezoneId },
        hemisphere = hemisphere,
        latitudeDeg = latitudeDeg.toDoubleOrNull(),
        longitudeDeg = longitudeDeg.toDoubleOrNull(),
        elevationM = elevationM.toDoubleOrNull(),
        usdaZoneCode = if (usdaZoneLabel == "Not set") null else BloomForecastEngine.zoneCodeFromLabel(usdaZoneLabel),
        chillHoursBand = chillHoursBand,
        microclimateFlags = microclimateFlags,
        notes = notes.trim()
    )

    fun applySearchResult(result: LocationSearchResult): GrowingLocationEditorState = copy(
        searchedPlaceLabel = result.displayLabel.ifBlank { result.name },
        countryCode = result.countryCode.ifBlank { countryCode },
        timezoneId = result.timezoneId.ifBlank { timezoneId },
        hemisphere = hemisphereForLatitude(result.latitudeDeg),
        latitudeDeg = result.latitudeDeg.toString(),
        longitudeDeg = result.longitudeDeg.toString(),
        elevationM = result.elevationM?.toString() ?: elevationM
    )

    companion object {
        fun from(location: GrowingLocationEntity?, settings: AppSettings? = null): GrowingLocationEditorState {
            val fallbackProfile = settings?.forecastLocationProfile()
            return GrowingLocationEditorState(
                id = location?.id,
                name = location?.name ?: fallbackProfile?.name.orEmpty(),
                searchedPlaceLabel = "",
                countryCode = location?.countryCode ?: fallbackProfile?.countryCode.orEmpty(),
                timezoneId = location?.timezoneId ?: fallbackProfile?.timezoneId.orEmpty(),
                hemisphere = location?.hemisphere ?: fallbackProfile?.hemisphere ?: Hemisphere.NORTHERN,
                latitudeDeg = location?.latitudeDeg?.toString() ?: fallbackProfile?.latitudeDeg?.toString().orEmpty(),
                longitudeDeg = location?.longitudeDeg?.toString() ?: fallbackProfile?.longitudeDeg?.toString().orEmpty(),
                elevationM = location?.elevationM?.toString() ?: fallbackProfile?.elevationM?.toString().orEmpty(),
                usdaZoneLabel = location?.usdaZoneCode?.let(BloomForecastEngine::zoneLabelForCode)
                    ?: fallbackProfile?.usdaZoneCode?.let(BloomForecastEngine::zoneLabelForCode)
                    ?: "Not set",
                chillHoursBand = location?.chillHoursBand ?: fallbackProfile?.chillHoursBand ?: ChillHoursBand.UNKNOWN,
                microclimateFlags = location?.microclimateFlags ?: fallbackProfile?.microclimateFlags.orEmpty(),
                notes = location?.notes.orEmpty()
            )
        }
    }
}

@Composable
private fun LocationSearchSheet(
    searchQuery: String,
    searchResults: List<LocationSearchResult>,
    searchBusy: Boolean,
    searchError: String?,
    onSearchQueryChange: (String) -> Unit,
    onApplySearchResult: (LocationSearchResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Find a place",
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search place") },
            supportingText = {
                Text("Type a city or place name and pick a result to fill timezone, coordinates, and elevation. It does not rename your growing location.")
            }
        )
        when {
            searchBusy -> {
                Text(
                    text = "Searching...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            searchResults.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplySearchResult(result) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = result.displayLabel,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = buildString {
                                        append("${result.latitudeDeg}, ${result.longitudeDeg}")
                                        result.elevationM?.let { append(" | ${it.toInt()} m") }
                                        if (result.timezoneId.isNotBlank()) append(" | ${result.timezoneId}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            searchError != null -> {
                Text(
                    text = searchError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Text(
                    text = "Start typing to search for a city, region, or place name.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrowingLocationEditorDialog(
    state: GrowingLocationEditorState,
    searchQuery: String,
    searchResults: List<LocationSearchResult>,
    searchBusy: Boolean,
    searchError: String?,
    onStateChange: (GrowingLocationEditorState) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onApplySearchResult: (LocationSearchResult) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val zoneOptions = remember { listOf("Not set") + BloomForecastEngine.supportedZoneLabels() }
    val hemisphereOptions = remember { Hemisphere.entries.map(Hemisphere::label) }
    val chillOptions = remember { ChillHoursBand.entries.map(ChillHoursBand::label) }
    var showSearchPicker by rememberSaveable(state.id) { mutableStateOf(false) }
    val canSave = remember(state) {
        state.name.isNotBlank() &&
            state.timezoneId.isNotBlank() &&
            state.latitudeDeg.toDoubleOrNull() != null &&
            state.longitudeDeg.toDoubleOrNull() != null &&
            state.elevationM.toDoubleOrNull() != null &&
            state.chillHoursBand != ChillHoursBand.UNKNOWN
    }
    if (showSearchPicker) {
        ModalBottomSheet(
            onDismissRequest = { showSearchPicker = false }
        ) {
            LocationSearchSheet(
                searchQuery = searchQuery,
                searchResults = searchResults,
                searchBusy = searchBusy,
                searchError = searchError,
                onSearchQueryChange = onSearchQueryChange,
                onApplySearchResult = {
                    onApplySearchResult(it)
                    showSearchPicker = false
                }
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.id == null) "Add orchard / growing location" else "Edit orchard / growing location") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "1. Name this orchard",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Use the custom orchard name you want when adding plants, like Backyard Orchard, Greenhouse, or Front Bed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { onStateChange(state.copy(name = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Orchard Name") },
                        supportingText = {
                            Text("This is your custom orchard name shown when you add trees.")
                        }
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "2. Search a nearby place to fill climate details",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Search fills timezone, coordinates, and elevation. You can adjust them manually afterward.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { showSearchPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                state.latitudeDeg.isNotBlank() && state.longitudeDeg.isNotBlank() ->
                                    "Search place or replace current coordinates"
                                else -> "Search place"
                            }
                        )
                    }
                }
                if (state.searchedPlaceLabel.isNotBlank()) {
                    item {
                        Text(
                            text = "Using place data from ${state.searchedPlaceLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Text(
                        text = "3. Confirm timezone, coordinates, elevation, and chill hours. USDA is optional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    TimezonePickerField(
                        label = "Timezone",
                        value = state.timezoneId,
                        onSelected = { onStateChange(state.copy(timezoneId = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = "Required for local dates and seasonal context."
                    )
                }
                item {
                    SelectionField(
                        label = "Hemisphere",
                        value = state.hemisphere.label,
                        options = hemisphereOptions,
                        onSelected = { selected ->
                            onStateChange(
                                state.copy(
                                    hemisphere = Hemisphere.entries.first { it.label == selected }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.latitudeDeg,
                            onValueChange = { onStateChange(state.copy(latitudeDeg = it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Latitude") }
                        )
                        OutlinedTextField(
                            value = state.longitudeDeg,
                            onValueChange = { onStateChange(state.copy(longitudeDeg = it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Longitude") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.elevationM,
                        onValueChange = { onStateChange(state.copy(elevationM = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Elevation (m)") }
                    )
                }
                item {
                    SelectionField(
                        label = "USDA zone",
                        value = state.usdaZoneLabel,
                        options = zoneOptions,
                        onSelected = { onStateChange(state.copy(usdaZoneLabel = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SelectionField(
                        label = "Chill hours",
                        value = state.chillHoursBand.label,
                        options = chillOptions,
                        onSelected = { selected ->
                            onStateChange(
                                state.copy(
                                    chillHoursBand = ChillHoursBand.entries.first { it.label == selected }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Microclimate flags nudge timing and confidence for special spots around the same coordinates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MicroclimateFlag.entries.forEach { flag ->
                            val selected = flag in state.microclimateFlags
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onStateChange(
                                        state.copy(
                                            microclimateFlags = if (selected) {
                                                state.microclimateFlags - flag
                                            } else {
                                                state.microclimateFlags + flag
                                            }
                                        )
                                    )
                                },
                                label = { Text(flag.label) }
                            )
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MicroclimateFlag.entries.forEach { flag ->
                            Text(
                                text = "${flag.label}: ${flag.explanation()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (flag in state.microclimateFlags) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                if (state.latitudeDeg.isNotBlank() || state.longitudeDeg.isNotBlank()) {
                    item {
                        Text(
                            text = "Picked coordinates: ${state.latitudeDeg.ifBlank { "?" }}, ${state.longitudeDeg.ifBlank { "?" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun hemisphereForLatitude(latitudeDeg: Double): Hemisphere = when {
    latitudeDeg > 12.0 -> Hemisphere.NORTHERN
    latitudeDeg < -12.0 -> Hemisphere.SOUTHERN
    else -> Hemisphere.EQUATORIAL
}

private fun MicroclimateFlag.explanation(): String = when (this) {
    MicroclimateFlag.GREENHOUSE -> "Treats the plant as protected, so outdoor timing becomes less dependable."
    MicroclimateFlag.COASTAL -> "Softens seasonal temperature swings and can stretch the active season."
    MicroclimateFlag.URBAN_HEAT -> "Warmer nights and surrounding surfaces can nudge bloom earlier."
    MicroclimateFlag.WARM_WALL -> "A sheltered warm wall can advance growth and protect tender flushes."
    MicroclimateFlag.FROST_POCKET -> "Cold air settles here, which can delay bloom or increase damage risk."
    MicroclimateFlag.EXPOSED_WIND -> "Wind stress can reduce flower quality and lower set confidence."
    MicroclimateFlag.SHADED_COOL -> "Lower light or a cool pocket can slow the season."
}

@Composable
fun CatalogScreen(usdaZone: String) {
    val catalog = remember { BloomForecastEngine.supportedSpeciesReferenceCatalog() }
    var search by rememberSaveable { mutableStateOf("") }
    val filteredCatalog = remember(catalog, search) {
        val query = search.trim().lowercase()
        if (query.isBlank()) {
            catalog
        } else {
            catalog.filter { entry ->
                listOf(
                    entry.species,
                    entry.fertilityLabel,
                    entry.referenceBloomTimingLabel,
                    entry.zoneBloomTimings.joinToString(" ") { timing ->
                        "${timing.zoneLabel} ${timing.timingLabel}"
                    },
                    entry.aliases.joinToString(" "),
                    entry.cultivars.joinToString(" ") { cultivar ->
                        buildString {
                            append(cultivar.cultivar)
                            if (cultivar.aliases.isNotEmpty()) append(" ${cultivar.aliases.joinToString(" ")}")
                            cultivar.fertilityLabel?.let { append(" $it") }
                        }
                    }
                ).any { value -> value.lowercase().contains(query) }
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Plant catalog") {
                Text("Species-first reference cards for USDA bloom timing, fertility grading, cultivars, and aliases.")
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search species or cultivars") }
                )
                Text(
                    text = "${filteredCatalog.size} species â€¢ ${filteredCatalog.sumOf { it.cultivars.size }} cultivars shown",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (filteredCatalog.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No catalog matches",
                    message = "Try a different species, cultivar, alias, or fertility term."
                )
            }
        } else {
            items(filteredCatalog, key = { it.species }) { entry ->
                CatalogSpeciesCard(entry = entry, usdaZone = usdaZone)
            }
        }
    }
}

@Composable
private fun CatalogDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CatalogSpeciesCard(
    entry: CatalogSpeciesReferenceEntry,
    usdaZone: String
) {
    var showAllZoneTimings by rememberSaveable(entry.species) { mutableStateOf(false) }
    val selectedZoneCode = remember(usdaZone) {
        usdaZone.takeIf(String::isNotBlank)?.let(BloomForecastEngine::effectiveZoneCode)
    }
    val selectedZoneTiming = remember(entry.zoneBloomTimings, selectedZoneCode) {
        selectedZoneCode?.let { zoneCode ->
            entry.zoneBloomTimings.firstOrNull { it.zoneCode == zoneCode }
        }
    }
    val primaryTiming = selectedZoneTiming ?: entry.zoneBloomTimings.firstOrNull()
    val visibleZoneTimings = remember(entry.zoneBloomTimings, showAllZoneTimings, selectedZoneCode) {
        val allZoneEntries = entry.zoneBloomTimings.filterNot { it.zoneCode == "all" }
        if (showAllZoneTimings || allZoneEntries.size <= 8) {
            allZoneEntries
        } else {
            val selectedEntry = selectedZoneCode?.let { zoneCode ->
                allZoneEntries.firstOrNull { it.zoneCode == zoneCode }
            }
            buildList {
                selectedEntry?.let(::add)
                allZoneEntries
                    .filterNot { it.zoneCode == selectedZoneCode }
                    .take(7)
                    .forEach(::add)
            }
        }
    }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(entry.species, style = MaterialTheme.typography.titleLarge)
                if (entry.aliases.isNotEmpty()) {
                    Text(
                        text = "Aliases: ${entry.aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CatalogDetailRow(
                        label = when {
                            primaryTiming == null -> "Reference bloom timing"
                            primaryTiming.zoneCode == "all" -> "Bloom timing"
                            selectedZoneCode != null -> "Bloom timing (${primaryTiming.zoneLabel})"
                            else -> "Reference bloom timing"
                        },
                        value = primaryTiming?.timingLabel ?: entry.referenceBloomTimingLabel
                    )
                    CatalogDetailRow(label = "Fertility", value = entry.fertilityLabel)
                    CatalogDetailRow(label = "Cultivars", value = entry.cultivars.size.toString())
                }
            }
            Text(
                text = "Bloom timing here is species-based. Cultivar timing can shift earlier or later where modeled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.zoneBloomTimings.isEmpty()) {
                Text(
                    text = "No zone-specific bloom windows are modeled for this species yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (selectedZoneCode == null && primaryTiming?.zoneCode != "all") {
                    Text(
                        text = "Set your USDA zone in Settings to make this card default to your orchard zone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.zoneBloomTimings.any { it.zoneCode != "all" }) {
                    OutlinedButton(
                        onClick = { showAllZoneTimings = !showAllZoneTimings },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showAllZoneTimings) {
                                "Hide all USDA zones"
                            } else {
                                "Show all USDA zones"
                            }
                        )
                    }
                }
                if (showAllZoneTimings && visibleZoneTimings.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            visibleZoneTimings.forEach { timing ->
                                CatalogDetailRow(
                                    label = timing.zoneLabel,
                                    value = timing.timingLabel
                                )
                            }
                        }
                    }
                }
            }
            if (entry.cultivars.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cultivars", style = MaterialTheme.typography.titleMedium)
                    entry.cultivars.forEach { cultivar ->
                        OutlinedCard(shape = RoundedCornerShape(18.dp)) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(cultivar.cultivar, style = MaterialTheme.typography.titleSmall)
                                cultivar.fertilityLabel?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                                if (cultivar.aliases.isNotEmpty()) {
                                    Text(
                                        text = "Aliases: ${cultivar.aliases.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun WishlistEntryDialog(viewModel: DexViewModel) {
    if (!viewModel.addDialogVisible) return

    val knownTrees by viewModel.trees.collectAsStateWithLifecycle()
    var suppressSpeciesAutocomplete by remember { mutableStateOf(false) }
    var suppressCultivarAutocomplete by remember { mutableStateOf(false) }

    val treeEntities = remember(knownTrees) { knownTrees.map { it.tree } }
    val supportedSpecies = remember { BloomForecastEngine.supportedSpeciesCatalog() }
    val speciesCatalog = remember(treeEntities, supportedSpecies) {
        (treeEntities.map(TreeEntity::species) + supportedSpecies)
            .filter(String::isNotBlank)
            .distinctBy(::normalizeAutocomplete)
            .sortedBy(String::lowercase)
    }
    val builtInSpeciesSuggestions = remember(viewModel.addSpecies) {
        BloomForecastEngine.speciesAutocompleteOptions(viewModel.addSpecies)
    }
    val orchardSpeciesSuggestions = remember(viewModel.addSpecies, speciesCatalog) {
        autocompleteSpeciesOptions(viewModel.addSpecies, speciesCatalog)
    }
    val speciesSuggestions = remember(builtInSpeciesSuggestions, orchardSpeciesSuggestions) {
        (builtInSpeciesSuggestions + orchardSpeciesSuggestions)
            .distinctBy(::normalizeAutocomplete)
            .take(8)
    }
    val builtInCultivarSuggestions = remember(viewModel.addCultivar, viewModel.addSpecies) {
        BloomForecastEngine.cultivarAutocompleteOptions(viewModel.addCultivar, viewModel.addSpecies)
    }
    val orchardCultivarSuggestions = remember(viewModel.addCultivar, viewModel.addSpecies, treeEntities) {
        existingCultivarAutocompleteOptions(viewModel.addCultivar, viewModel.addSpecies, treeEntities)
    }
    val cultivarSuggestions = remember(builtInCultivarSuggestions, orchardCultivarSuggestions) {
        (builtInCultivarSuggestions + orchardCultivarSuggestions)
            .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
            .take(8)
    }
    val cultivarSuggestionsTitle = remember(viewModel.addCultivar, viewModel.addSpecies) {
        if (viewModel.addCultivar.isBlank() && viewModel.addSpecies.isNotBlank()) {
            "Known ${viewModel.addSpecies.trim()} cultivars"
        } else {
            "Cultivar matches"
        }
    }

    AlertDialog(
        onDismissRequest = viewModel::hideAddDialog,
        title = { Text("Add wishlist plant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.addSpecies,
                    onValueChange = { input ->
                        suppressSpeciesAutocomplete = false
                        viewModel.addSpecies = input
                    },
                    label = { Text("Species") }
                )
                if (!suppressSpeciesAutocomplete) {
                    SpeciesAutocompleteCard(
                        query = viewModel.addSpecies,
                        suggestions = speciesSuggestions,
                        onSelected = { suggestion ->
                            suppressSpeciesAutocomplete = true
                            viewModel.addSpecies = suggestion
                        }
                    )
                }
                if (!suppressCultivarAutocomplete) {
                    CultivarAutocompleteCard(
                        query = viewModel.addCultivar,
                        suggestions = cultivarSuggestions,
                        title = cultivarSuggestionsTitle,
                        allowBlankQuery = viewModel.addCultivar.isBlank() && viewModel.addSpecies.isNotBlank(),
                        onSelected = { suggestion ->
                            suppressSpeciesAutocomplete = true
                            suppressCultivarAutocomplete = true
                            viewModel.addSpecies = suggestion.species
                            viewModel.addCultivar = suggestion.cultivar
                        }
                    )
                }
                OutlinedTextField(
                    value = viewModel.addCultivar,
                    onValueChange = { input ->
                        suppressCultivarAutocomplete = false
                        viewModel.addCultivar = input
                    },
                    label = { Text("Cultivar (optional)") }
                )
                OutlinedTextField(value = viewModel.addNotes, onValueChange = { viewModel.addNotes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::saveWishlist,
                enabled = viewModel.addSpecies.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = viewModel::hideAddDialog) { Text("Cancel") } }
    )
}

@Composable
private fun DexAddDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddTree: () -> Unit,
    onAddWishlist: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Dex") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDismiss(); onAddTree() }, modifier = Modifier.fillMaxWidth().testTag("add_tree")) { Text("Add plant") }
                Button(onClick = { onDismiss(); onAddWishlist() }, modifier = Modifier.fillMaxWidth()) { Text("Add wishlist") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun ReminderEntity.statusLabel(now: Long, dueSoonCutoff: Long): String = when {
    completedAt != null -> "Completed"
    !enabled -> "Paused"
    dueAt < now -> "Overdue"
    dueAt in now..dueSoonCutoff -> "Due soon"
    else -> "Scheduled"
}

private fun ReminderEntity.dueLine(): String = if (hasTime) dueAt.toDateTimeLabel() else dueAt.toDateLabel()

private fun ReminderEntity.recurrenceLabel(): String? = when (recurrenceType) {
    RecurrenceType.NONE -> null
    RecurrenceType.DAILY -> "Daily"
    RecurrenceType.WEEKLY -> "Weekly"
    RecurrenceType.MONTHLY -> "Monthly"
    RecurrenceType.EVERY_X_DAYS -> "Every ${recurrenceIntervalDays ?: 7} days"
}


