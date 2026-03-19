package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.BuildConfig
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.model.DexCultivarEntry
import com.dillon.orcharddex.data.model.DexSpeciesGroup
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
import com.dillon.orcharddex.ui.components.ChoiceChipsRow
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.toDateTimeLabel
import com.dillon.orcharddex.ui.viewmodel.DexViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderListViewModel
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel
import java.io.File

private enum class DexPlantSortOption(val label: String) {
    UPDATED("Updated"),
    PLANTED("Planted"),
    SPECIES("Species"),
    CULTIVAR("Cultivar")
}

@Composable
fun DexScreen(
    viewModel: DexViewModel,
    onAddTree: () -> Unit,
    onTreeClick: (String) -> Unit
) {
    val dex by viewModel.dex.collectAsStateWithLifecycle()
    val plants by viewModel.trees.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var addMenuVisible by rememberSaveable { mutableStateOf(false) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    var speciesFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<TreeStatus?>(null) }
    var plantTypeFilter by rememberSaveable { mutableStateOf<PlantType?>(null) }
    var sort by rememberSaveable { mutableStateOf(DexPlantSortOption.UPDATED) }

    WishlistEntryDialog(viewModel)
    DexAddDialog(
        visible = addMenuVisible,
        onDismiss = { addMenuVisible = false },
        onAddTree = onAddTree,
        onAddWishlist = viewModel::showAddDialog
    )

    val speciesOptions = plants.map { it.tree.species }.distinct().sorted()
    val filteredPlants = remember(plants, search, speciesFilter, statusFilter, plantTypeFilter, sort) {
        plants.filter { item ->
            val tree = item.tree
            val query = search.trim().lowercase()
            val matchesQuery = query.isBlank() || listOf(
                tree.nickname.orEmpty(),
                tree.species,
                tree.cultivar,
                tree.tags,
                tree.notes,
                tree.sectionName
            ).any { it.lowercase().contains(query) }
            matchesQuery &&
                (speciesFilter == null || tree.species == speciesFilter) &&
                (statusFilter == null || tree.status == statusFilter) &&
                (plantTypeFilter == null || tree.plantType == plantTypeFilter)
        }.sortedWith(
            when (sort) {
                DexPlantSortOption.UPDATED -> compareByDescending { it.tree.updatedAt }
                DexPlantSortOption.PLANTED -> compareByDescending { it.tree.plantedDate }
                DexPlantSortOption.SPECIES -> compareBy({ it.tree.species.lowercase() }, { it.tree.cultivar.lowercase() })
                DexPlantSortOption.CULTIVAR -> compareBy({ it.tree.cultivar.lowercase() }, { it.tree.species.lowercase() })
            }
        )
    }
    val filteredGroups = remember(dex.ownedGroups, search, speciesFilter) {
        val query = search.trim().lowercase()
        dex.ownedGroups.mapNotNull { group ->
            if (speciesFilter != null && group.species != speciesFilter) return@mapNotNull null
            val cultivars = group.cultivars.filter { entry ->
                query.isBlank() || listOf(group.species, entry.species, entry.cultivar).any { it.lowercase().contains(query) }
            }
            if (cultivars.isEmpty()) null else DexSpeciesGroup(group.species, cultivars)
        }
    }

    if (filtersVisible) {
        AlertDialog(
            onDismissRequest = { filtersVisible = false },
            title = { Text("Filter plants") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChoiceChipsRow(speciesOptions.take(12), speciesFilter, onSelected = { speciesFilter = it })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("Any status") })
                        TreeStatus.entries.forEach { status ->
                            FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status.name.lowercase()) })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = plantTypeFilter == null, onClick = { plantTypeFilter = null }, label = { Text("Any planting") })
                        FilterChip(selected = plantTypeFilter == PlantType.IN_GROUND, onClick = { plantTypeFilter = PlantType.IN_GROUND }, label = { Text("In-ground") })
                        FilterChip(selected = plantTypeFilter == PlantType.CONTAINER, onClick = { plantTypeFilter = PlantType.CONTAINER }, label = { Text("Container") })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DexPlantSortOption.entries.forEach { option ->
                            FilterChip(selected = sort == option, onClick = { sort = option }, label = { Text(option.label) })
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { filtersVisible = false }) { Text("Done") } },
            dismissButton = {
                TextButton(onClick = {
                    speciesFilter = null
                    statusFilter = null
                    plantTypeFilter = null
                    sort = DexPlantSortOption.UPDATED
                }) { Text("Reset") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { addMenuVisible = true }, shape = RoundedCornerShape(16.dp), modifier = Modifier.testTag("add_tree")) {
                Icon(Icons.Outlined.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Plant library") {
                    Text("Search the orchard and open a plant for the full record, photos, reminders, and history.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Search plants or cultivars") }
                        )
                        OutlinedButton(onClick = { filtersVisible = true }) { Text("Filters") }
                    }
                    buildDexFilterSummary(speciesFilter, statusFilter, plantTypeFilter, sort)?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${filteredPlants.size} plants shown", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (filteredPlants.isEmpty()) {
                item { EmptyStateCard("No plants found", "Try a different search, clear filters, or add a plant.") }
            } else {
                items(filteredPlants, key = { it.tree.id }) { item ->
                    DexPlantCard(
                        item = item,
                        zoneCode = settings.usdaZone,
                        onClick = { onTreeClick(item.tree.id) }
                    )
                }
            }
            item {
                SectionCard("Variety board") {
                    if (filteredGroups.isEmpty()) {
                        Text("No varieties match the current search or filters.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            filteredGroups.forEach { group ->
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(group.species, style = MaterialTheme.typography.titleMedium)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        group.cultivars.forEach { entry ->
                                            DexCultivarCard(entry = entry, onClick = entry.linkedTreeId?.let { id -> { onTreeClick(id) } })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionCard("Wishlist") {
                    if (dex.wishlistEntries.isEmpty()) {
                        Text("No wishlist entries yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            dex.wishlistEntries.forEach { entry ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${entry.species} - ${entry.cultivar}", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            buildString {
                                                append(entry.priority.name.lowercase())
                                                if (entry.acquired) append(" - acquired")
                                                if (entry.notes.isNotBlank()) append(" - ${entry.notes}")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    TextButton(onClick = { viewModel.deleteWishlist(entry.id) }) { Text("Delete") }
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
private fun DexPlantCard(item: TreeListItem, zoneCode: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val pollination = BloomForecastEngine.pollinationRequirementFor(item.tree.species, item.tree.cultivar)
    val bloomWindow = BloomForecastEngine.bloomWindowLabelFor(item.tree.species, item.tree.cultivar, zoneCode)
    val title = item.tree.dexPrimaryTitle()
    val subtitle = item.tree.dexSecondaryTitle()
    val supportingLine = item.tree.dexSupportingLine()
    val thumbnailLabel = item.tree.cultivar.takeIf(String::isNotBlank)?.take(2)
        ?: item.tree.species.take(2)

    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                if (item.mainPhotoPath != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${item.mainPhotoPath}"),
                        contentDescription = "Plant photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Text(thumbnailLabel.uppercase())
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                supportingLine?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFact("Pollination", pollination.dexListLabel())
                    CompactFact("Bloom", bloomWindow.orEmpty())
                    CompactFact("Planting", item.tree.plantType.dexLabel())
                    CompactFact("Status", item.tree.status.dexBadgeLabel())
                }
            }
        }
    }
}

private fun TreeStatus.dexBadgeLabel(): String = when (this) {
    TreeStatus.ACTIVE -> ""
    else -> name.lowercase().replaceFirstChar(Char::uppercase)
}

private fun PlantType.dexLabel(): String = when (this) {
    PlantType.IN_GROUND -> "In-ground"
    PlantType.CONTAINER -> "Container"
}

private fun com.dillon.orcharddex.data.phenology.PollinationRequirement?.dexListLabel(): String = when (this) {
    null -> ""
    com.dillon.orcharddex.data.phenology.PollinationRequirement.SELF_FERTILE -> "Self-fertile"
    com.dillon.orcharddex.data.phenology.PollinationRequirement.NEEDS_CROSS_POLLINATION -> "Cross-pollination"
    com.dillon.orcharddex.data.phenology.PollinationRequirement.CROSS_POLLINATION_RECOMMENDED -> "Cross helpful"
    com.dillon.orcharddex.data.phenology.PollinationRequirement.POLLINATION_NOT_REQUIRED -> ""
    com.dillon.orcharddex.data.phenology.PollinationRequirement.UNKNOWN -> ""
}

private fun com.dillon.orcharddex.data.local.TreeEntity.dexPrimaryTitle(): String = when {
    cultivar.isNotBlank() -> cultivar.trim()
    !nickname.isNullOrBlank() -> nickname.trim()
    else -> species.trim()
}

private fun com.dillon.orcharddex.data.local.TreeEntity.dexSecondaryTitle(): String = when {
    species.isBlank() && cultivar.isBlank() -> displayName()
    species.isBlank() -> displayName()
    else -> species.trim()
}

private fun com.dillon.orcharddex.data.local.TreeEntity.dexSupportingLine(): String? = buildList {
    nickname?.trim()?.takeIf { it.isNotBlank() && !cultivar.equals(it, ignoreCase = true) }?.let(::add)
    sectionName.trim().takeIf { it.isNotBlank() }?.let(::add)
}.distinct().joinToString(" • ").takeIf(String::isNotBlank)

private enum class ReminderFilterTab(val label: String) {
    ALL("All"),
    DUE_SOON("Due soon"),
    OVERDUE("Overdue"),
    COMPLETED("Completed")
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = "New reminder")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Task board") {
                    Text("Scan overdue tasks first, then work through the next 7 days.")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("Open", reminders.count { it.reminder.completedAt == null && it.reminder.enabled }.toString())
                        StatCard("Overdue", reminders.count { it.reminder.completedAt == null && it.reminder.enabled && it.reminder.dueAt < now }.toString())
                        StatCard("Due in 7 days", reminders.count { it.reminder.completedAt == null && it.reminder.enabled && it.reminder.dueAt in now..dueSoonCutoff }.toString())
                        StatCard("Done or paused", reminders.count { it.reminder.completedAt != null || !it.reminder.enabled }.toString())
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
                    EmptyStateCard("No reminders found", "Add a reminder or change the filters to bring tasks back into view.")
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
fun SettingsScreen(viewModel: SettingsViewModel, onPrivacy: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var orchardNameDraft by rememberSaveable(settings.orchardName) { mutableStateOf(settings.orchardName) }
    val zoneOptions = remember { listOf("Not set") + BloomForecastEngine.supportedZoneLabels() }
    val supportedCultivarCatalog = remember {
        BloomForecastEngine.supportedCultivarCatalog()
            .groupBy { it.species }
            .toList()
            .sortedBy { it.first.lowercase() }
    }
    var usdaZoneDraft by rememberSaveable(settings.usdaZone) {
        mutableStateOf(settings.usdaZone.takeIf(String::isNotBlank)?.let(BloomForecastEngine::zoneLabelForCode) ?: "Not set")
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let(viewModel::exportBackup)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::validateImport)
    }

    viewModel.pendingImport?.let { validation ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPendingImport,
            title = { Text("Import backup?") },
            text = { Text("Archive from ${validation.appVersion}\n${validation.treeCount} trees, ${validation.reminderCount} reminders, ${validation.photoCount} photos.\nThis replaces current app data.") },
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

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SectionCard("Orchard") {
                OutlinedTextField(value = orchardNameDraft, onValueChange = { orchardNameDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Orchard name") })
                SelectionField(
                    label = "USDA zone",
                    value = usdaZoneDraft,
                    options = zoneOptions,
                    onSelected = { usdaZoneDraft = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Used for the dashboard bloom forecast calendar.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(
                    onClick = {
                        viewModel.updateOrchardProfile(
                            orchardNameDraft,
                            if (usdaZoneDraft == "Not set") "" else BloomForecastEngine.zoneCodeFromLabel(usdaZoneDraft)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save orchard details")
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
            SectionCard("Backups") {
                OutlinedButton(onClick = { exportLauncher.launch("orcharddex-${BuildConfig.VERSION_NAME}.orcharddex.zip") }, modifier = Modifier.fillMaxWidth().testTag("export_backup")) {
                    Text("Export backup")
                }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import backup")
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
            SectionCard("Bloom & pollination catalog") {
                Text("${supportedCultivarCatalog.sumOf { it.second.size }} cataloged cultivars currently supported.")
                Text("Species | cultivars", style = MaterialTheme.typography.bodySmall)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    supportedCultivarCatalog.forEach { (species, cultivars) ->
                        Text(
                            "$species | ${cultivars.joinToString(", ") { entry ->
                                val cultivarLabel = if (entry.aliases.isEmpty()) {
                                    entry.cultivar
                                } else {
                                    "${entry.cultivar} (${entry.aliases.joinToString("/")})"
                                }
                                val pollinationLabel = entry.pollinationRequirement?.label
                                if (pollinationLabel == null) {
                                    cultivarLabel
                                } else {
                                    "$cultivarLabel — $pollinationLabel"
                                }
                            }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            SectionCard("About") {
                Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Text("Local-only app. No account, analytics, ads, or cloud sync.")
                TextButton(onClick = onPrivacy) { Text("Privacy policy") }
                if (viewModel.busy) Text("Working...")
            }
        }
    }
}

@Composable
private fun WishlistEntryDialog(viewModel: DexViewModel) {
    if (!viewModel.addDialogVisible) return
    AlertDialog(
        onDismissRequest = viewModel::hideAddDialog,
        title = { Text("Add wishlist cultivar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = viewModel.addSpecies, onValueChange = { viewModel.addSpecies = it }, label = { Text("Species") })
                OutlinedTextField(value = viewModel.addCultivar, onValueChange = { viewModel.addCultivar = it }, label = { Text("Cultivar") })
                OutlinedTextField(value = viewModel.addNotes, onValueChange = { viewModel.addNotes = it }, label = { Text("Notes") })
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.dillon.orcharddex.data.model.WishlistPriority.entries.forEach { priority ->
                        FilterChip(selected = viewModel.addPriority == priority, onClick = { viewModel.addPriority = priority }, label = { Text(priority.name.lowercase()) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::saveWishlist) { Text("Save") } },
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

private fun buildDexFilterSummary(
    speciesFilter: String?,
    statusFilter: TreeStatus?,
    plantTypeFilter: PlantType?,
    sort: DexPlantSortOption
): String? {
    val parts = buildList {
        speciesFilter?.let { add("Species: $it") }
        statusFilter?.let { add("Status: ${it.name.lowercase()}") }
        plantTypeFilter?.let { add("Planting: ${it.name.replace("_", "-").lowercase()}") }
        add("Sort: ${sort.label.lowercase()}")
    }
    return parts.joinToString(" | ").takeIf(String::isNotBlank)
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
