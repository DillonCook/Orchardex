package com.dillon.orcharddex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.dillon.orcharddex.BuildConfig
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.ui.components.ChoiceChipsRow
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.DexViewModel
import com.dillon.orcharddex.ui.viewmodel.ReminderListViewModel
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel

private enum class ReminderFilterTab(val label: String) {
    ALL("All"),
    DUE_SOON("Due soon"),
    OVERDUE("Overdue"),
    COMPLETED("Completed")
}

@Composable
fun DexScreen(
    viewModel: DexViewModel,
    onTreeClick: (String) -> Unit
) {
    val dex by viewModel.dex.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var speciesFilter by rememberSaveable { mutableStateOf<String?>(null) }

    if (viewModel.addDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::hideAddDialog,
            title = { Text("Add wishlist cultivar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.addSpecies,
                        onValueChange = { viewModel.addSpecies = it },
                        label = { Text("Species") }
                    )
                    OutlinedTextField(
                        value = viewModel.addCultivar,
                        onValueChange = { viewModel.addCultivar = it },
                        label = { Text("Cultivar") }
                    )
                    OutlinedTextField(
                        value = viewModel.addNotes,
                        onValueChange = { viewModel.addNotes = it },
                        label = { Text("Notes") }
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.dillon.orcharddex.data.model.WishlistPriority.entries.forEach { priority ->
                            FilterChip(
                                selected = viewModel.addPriority == priority,
                                onClick = { viewModel.addPriority = priority },
                                label = { Text(priority.name.lowercase()) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveWishlist) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideAddDialog) { Text("Cancel") }
            }
        )
    }

    val speciesOptions = dex.ownedGroups.map { it.species }.distinct()
    val filteredGroups = dex.ownedGroups
        .filter { speciesFilter == null || it.species == speciesFilter }
        .map { group ->
            group.copy(
                cultivars = group.cultivars.filter { cultivar ->
                    search.isBlank() ||
                        cultivar.cultivar.contains(search, ignoreCase = true) ||
                        cultivar.species.contains(search, ignoreCase = true)
                }
            )
        }.filter { it.cultivars.isNotEmpty() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Collection progress") {
                Text("Owned cultivars: ${dex.ownedCultivarCount}")
                Text("Wishlist: ${dex.wishlistCount}")
                Text("First fruit achieved: ${dex.firstFruitCount}")
                OutlinedButton(onClick = viewModel::showAddDialog) {
                    Text("Add wishlist cultivar")
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search cultivars") }
            )
        }
        item {
            ChoiceChipsRow(speciesOptions, speciesFilter, onSelected = { speciesFilter = it })
        }
        if (filteredGroups.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No cultivars yet",
                    message = "Add trees or wishlist entries to build your orchard Dex."
                )
            }
        }
        items(filteredGroups, key = { it.species }) { group ->
            SectionCard(group.species) {
                group.cultivars.forEach { cultivar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = cultivar.linkedTreeId != null) {
                                cultivar.linkedTreeId?.let(onTreeClick)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(cultivar.cultivar, style = MaterialTheme.typography.titleMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = true, onClick = {}, label = { Text("Owned ${cultivar.activeTreeCount}") })
                                if (cultivar.wishlist) {
                                    FilterChip(selected = true, onClick = {}, label = { Text("Wishlist") })
                                }
                                if (cultivar.firstFruitAchieved) {
                                    FilterChip(selected = true, onClick = {}, label = { Text("First fruit") })
                                }
                                if (cultivar.inactiveTreeCount > 0) {
                                    FilterChip(selected = false, onClick = {}, label = { Text("Inactive ${cultivar.inactiveTreeCount}") })
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
                    dex.wishlistEntries.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${entry.species} • ${entry.cultivar}")
                                Text(
                                    buildString {
                                        append(entry.priority.name.lowercase())
                                        if (entry.acquired) append(" • acquired")
                                        if (entry.notes.isNotBlank()) append(" • ${entry.notes}")
                                    }
                                )
                            }
                            TextButton(onClick = { viewModel.deleteWishlist(entry.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
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
    val treeOptions = reminders.mapNotNull { it.treeLabel }.distinct()
    val speciesOptions = reminders.mapNotNull { it.species }.distinct()
    val filtered = reminders.filter { item ->
        val reminder = item.reminder
        val query = search.trim().lowercase()
        val matchesQuery = query.isBlank() || listOf(
            reminder.title,
            reminder.notes,
            item.treeLabel.orEmpty(),
            item.species.orEmpty()
        ).any { it.lowercase().contains(query) }
        val matchesTab = when (filter) {
            ReminderFilterTab.ALL -> true
            ReminderFilterTab.DUE_SOON -> reminder.completedAt == null && reminder.dueAt in now..dueSoonCutoff
            ReminderFilterTab.OVERDUE -> reminder.completedAt == null && reminder.dueAt < now
            ReminderFilterTab.COMPLETED -> reminder.completedAt != null || !reminder.enabled
        }
        matchesQuery &&
            matchesTab &&
            (treeFilter == null || item.treeLabel == treeFilter) &&
            (speciesFilter == null || item.species == speciesFilter)
    }

    if (completeReminderId != null) {
        AlertDialog(
            onDismissRequest = { completeReminderId = null },
            title = { Text("Complete reminder") },
            text = { Text("Also create a linked event log?") },
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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedButton(onClick = onAddReminder, modifier = Modifier.fillMaxWidth()) {
                Text("Add reminder")
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search reminders") }
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderFilterTab.entries.forEach { tab ->
                    FilterChip(selected = filter == tab, onClick = { filter = tab }, label = { Text(tab.label) })
                }
            }
        }
        item {
            ChoiceChipsRow(treeOptions, treeFilter, onSelected = { treeFilter = it })
            ChoiceChipsRow(speciesOptions, speciesFilter, onSelected = { speciesFilter = it })
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No reminders found",
                    message = "Add a local reminder for orchard care tasks."
                )
            }
        }
        items(filtered, key = { it.reminder.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(item.reminder.title, style = MaterialTheme.typography.titleMedium)
                    Text("${item.treeLabel ?: "General orchard"} • ${item.reminder.dueAt.toDateLabel()}")
                    if (item.reminder.notes.isNotBlank()) {
                        Text(item.reminder.notes)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onEditReminder(item.reminder.id) }) { Text("Edit") }
                        if (item.reminder.completedAt == null && item.reminder.enabled) {
                            TextButton(onClick = { completeReminderId = item.reminder.id }) { Text("Mark done") }
                        }
                        TextButton(onClick = { viewModel.deleteReminder(item.reminder.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onPrivacy: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let(viewModel::exportBackup) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::validateImport) }

    viewModel.pendingImport?.let { validation ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPendingImport,
            title = { Text("Import backup?") },
            text = {
                Text(
                    "Archive from ${validation.appVersion}\n" +
                        "${validation.treeCount} trees, ${validation.reminderCount} reminders, ${validation.photoCount} photos.\n" +
                        "This replaces current app data."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::importReplaceAll) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPendingImport) { Text("Cancel") }
            }
        )
    }

    if (viewModel.confirmClearAll) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAll,
            title = { Text("Clear all data?") },
            text = { Text("This permanently removes all local trees, photos, reminders, and wishlist entries.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearAllData) { Text("Clear all") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearAll) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Appearance") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateTheme(mode) },
                            label = { Text(mode.name.lowercase()) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dynamic color")
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = viewModel::updateDynamicColor
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
                if (settings.defaultLeadTimeMode == LeadTimeMode.CUSTOM_HOURS) {
                    Text("Custom lead time: ${settings.defaultCustomLeadHours} hours")
                }
            }
        }
        item {
            SectionCard("Backups") {
                OutlinedButton(
                    onClick = { exportLauncher.launch("orcharddex-${BuildConfig.VERSION_NAME}.orcharddex.zip") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup")
                ) {
                    Text("Export backup")
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import backup")
                }
            }
        }
        item {
            SectionCard("Data") {
                OutlinedButton(onClick = viewModel::loadSampleData, modifier = Modifier.fillMaxWidth()) {
                    Text("Load sample orchard data")
                }
                OutlinedButton(onClick = viewModel::requestClearAll, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear all data")
                }
            }
        }
        item {
            SectionCard("Privacy") {
                Text("All orchard data stays on device.")
                Text("No account, analytics, ads, or server sync.")
                TextButton(onClick = onPrivacy) { Text("Open privacy statement") }
            }
        }
        item {
            SectionCard("About") {
                Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                if (viewModel.busy) {
                    Text("Working…")
                }
            }
        }
    }
}
