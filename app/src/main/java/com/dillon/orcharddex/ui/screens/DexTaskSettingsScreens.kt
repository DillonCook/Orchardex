package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.BuildConfig
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.model.DexCultivarEntry
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.phenology.CatalogSpeciesReferenceEntry
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.preferences.AppThemeMode
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
import androidx.compose.material3.surfaceColorAtElevation
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
    usdaZone: String,
    onAddTree: () -> Unit,
    onTreeClick: (String) -> Unit
) {
    val dex by viewModel.dex.collectAsStateWithLifecycle()
    val plants by viewModel.trees.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var wishlistVisible by rememberSaveable { mutableStateOf(false) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }
    var speciesFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<TreeStatus?>(null) }
    var plantTypeFilter by rememberSaveable { mutableStateOf<PlantType?>(null) }
    var sort by rememberSaveable { mutableStateOf(DexPlantSortOption.UPDATED) }

    WishlistEntryDialog(viewModel)
    if (wishlistVisible) {
        WishlistEntriesDialog(
            entries = dex.wishlistEntries,
            onDismiss = { wishlistVisible = false },
            onDelete = viewModel::deleteWishlist
        )
    }

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
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Search plants or cultivars") }
                        )
                        OutlinedButton(onClick = { filtersVisible = true }) { Text("Filters") }
                    }
                }
            }
            if (filteredPlants.isEmpty()) {
                item { EmptyStateCard("No plants found", "Try a different search, clear filters, or add a plant.") }
            } else {
                items(filteredPlants, key = { it.tree.id }) { item ->
                    DexPlantCard(
                        item = item,
                        usdaZone = usdaZone,
                        onClick = { onTreeClick(item.tree.id) }
                    )
                }
            }

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
    usdaZone: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val title = item.tree.dexPrimaryTitle()
    val subtitle = item.tree.dexSecondaryTitle()
    val supportingLine = item.tree.dexSupportingLine()
    val thumbnailLabel = item.tree.cultivar.takeIf(String::isNotBlank)?.take(2)
        ?: item.tree.species.take(2)
    val nextBloomLabel = remember(item.tree, usdaZone) {
        BloomForecastEngine.plantBloomCountdownLabel(item.tree, usdaZone)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.mainPhotoPath != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${item.mainPhotoPath}"),
                        contentDescription = "Plant photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(84.dp),
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = thumbnailLabel.uppercase(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                supportingLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            nextBloomLabel?.let { label ->
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Next bloom",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun TreeEntity.dexSupportingLine(): String? = listOfNotNull(
    sectionName.takeIf(String::isNotBlank),
    when (status) {
        TreeStatus.ACTIVE -> null
        else -> status.name.lowercase().replace('_', ' ')
    }
).joinToString(" • ").takeIf(String::isNotBlank)

private fun com.dillon.orcharddex.data.local.TreeEntity.dexPrimaryTitle(): String = cultivar
    .trim()
    .takeIf(String::isNotBlank)
    ?: species.trim()

private fun com.dillon.orcharddex.data.local.TreeEntity.dexSecondaryTitle(): String? = species
    .trim()
    .takeIf { cultivar.isNotBlank() && it.isNotBlank() }

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
    var orchardNameDraft by rememberSaveable(settings.orchardName) { mutableStateOf(settings.orchardName) }
    val zoneOptions = remember { listOf("Not set") + BloomForecastEngine.supportedZoneLabels() }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                Text("USDA zone drives bloom timing across the orchard experience.", style = MaterialTheme.typography.bodySmall)
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
                OutlinedButton(onClick = { exportLauncher.launch("orchardex-${BuildConfig.VERSION_NAME}.orchardex.zip") }, modifier = Modifier.fillMaxWidth().testTag("export_backup")) {
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
                Text("Local-only app. No account, analytics, ads, or cloud sync.")
                TextButton(onClick = onPrivacy) { Text("Privacy policy") }
                if (viewModel.busy) Text("Working...")
            }
        }
    }
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
                    text = "${filteredCatalog.size} species • ${filteredCatalog.sumOf { it.cultivars.size }} cultivars shown",
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

    AlertDialog(
        onDismissRequest = viewModel::hideAddDialog,
        title = { Text("Add wishlist plant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.addSpecies,
                    onValueChange = { input ->
                        suppressSpeciesAutocomplete = false
                        val exactSpecies = BloomForecastEngine.resolveSpeciesAutocomplete(input)
                            ?: speciesCatalog.firstOrNull {
                                normalizeAutocomplete(it) == normalizeAutocomplete(input)
                            }
                        viewModel.addSpecies = exactSpecies ?: input
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
                OutlinedTextField(
                    value = viewModel.addCultivar,
                    onValueChange = { input ->
                        suppressCultivarAutocomplete = false
                        val exactMatch = BloomForecastEngine.resolveCultivarAutocomplete(input, viewModel.addSpecies)
                            ?: resolveExistingCultivarAutocomplete(input, treeEntities)
                        if (exactMatch != null) {
                            suppressSpeciesAutocomplete = true
                            suppressCultivarAutocomplete = true
                            viewModel.addSpecies = exactMatch.species
                            viewModel.addCultivar = exactMatch.cultivar
                        } else {
                            viewModel.addCultivar = input
                        }
                    },
                    label = { Text("Cultivar (optional)") }
                )
                if (!suppressCultivarAutocomplete) {
                    CultivarAutocompleteCard(
                        query = viewModel.addCultivar,
                        suggestions = cultivarSuggestions,
                        onSelected = { suggestion ->
                            suppressSpeciesAutocomplete = true
                            suppressCultivarAutocomplete = true
                            viewModel.addSpecies = suggestion.species
                            viewModel.addCultivar = suggestion.cultivar
                        }
                    )
                }
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
