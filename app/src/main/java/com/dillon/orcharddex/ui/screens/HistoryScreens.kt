package com.dillon.orcharddex.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.LocalPhotoStrip
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.HistoryDetailViewModel
import com.dillon.orcharddex.ui.viewmodel.HistoryViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val historyMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val historyDayFormatter = DateTimeFormatter.ofPattern("d")
private val historyMonthShortFormatter = DateTimeFormatter.ofPattern("MMM")

private enum class HistoryQuickFilter(val label: String) {
    ALL("All"),
    EVENTS("Events"),
    HARVESTS("Harvests"),
    SEASONAL("Seasonal"),
    ISSUES("Issues")
}

private enum class HarvestTracker(val label: String) {
    CURRENT_YEAR("Current year"),
    TOTAL("Total harvests")
}

private data class HarvestSpeciesCount(
    val species: String,
    val harvestCount: Int
)

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEntryClick: (ActivityKind, String) -> Unit,
    onAddEvent: () -> Unit,
    onAddHarvest: () -> Unit
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var quickFilter by rememberSaveable { mutableStateOf(HistoryQuickFilter.ALL) }
    var addMenuVisible by rememberSaveable { mutableStateOf(false) }
    var selectedTracker by rememberSaveable { mutableStateOf<HarvestTracker?>(null) }

    val filteredHistory = remember(history, search, quickFilter) {
        val query = search.trim().lowercase()
        history.filter { entry ->
            entry.matchesSearch(query) && quickFilter.matches(entry)
        }
    }
    val groupedHistory = remember(filteredHistory) {
        filteredHistory.groupBy { it.date.monthBucketLabel() }.toList()
    }
    val currentYear = remember { java.time.Year.now().value }
    val harvestHistory = remember(history) {
        history.filter { entry -> entry.kind == ActivityKind.HARVEST }
    }
    val currentYearHarvests = remember(harvestHistory, currentYear) {
        harvestHistory.filter { entry ->
            Instant.ofEpochMilli(entry.date).atZone(ZoneId.systemDefault()).year == currentYear
        }
    }
    val trackerEntries = selectedTracker?.let { tracker ->
        when (tracker) {
            HarvestTracker.CURRENT_YEAR -> currentYearHarvests
            HarvestTracker.TOTAL -> harvestHistory
        }
    }.orEmpty()
    val trackerSpeciesCounts = remember(trackerEntries) {
        trackerEntries
            .groupingBy { it.species.ifBlank { "Unknown species" } }
            .eachCount()
            .map { (species, count) -> HarvestSpeciesCount(species = species, harvestCount = count) }
            .sortedWith(compareByDescending<HarvestSpeciesCount> { it.harvestCount }.thenBy { it.species.lowercase() })
    }

    if (addMenuVisible) {
        AlertDialog(
            onDismissRequest = { addMenuVisible = false },
            title = { Text("Add to history") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            addMenuVisible = false
                            onAddEvent()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add event")
                    }
                    Button(
                        onClick = {
                            addMenuVisible = false
                            onAddHarvest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add harvest")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { addMenuVisible = false }) {
                    Text("Close")
                }
            }
        )
    }

    selectedTracker?.let { tracker ->
        HarvestTrackerDialog(
            title = if (tracker == HarvestTracker.CURRENT_YEAR) "$currentYear harvests" else tracker.label,
            speciesCounts = trackerSpeciesCounts,
            totalHarvests = trackerEntries.size,
            onDismiss = { selectedTracker = null }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { addMenuVisible = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("history_new")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "New")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard("Harvest trackers") {
                    Text(
                        text = "Tap a tracker to review harvest counts by species.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        com.dillon.orcharddex.ui.components.StatCard(
                            modifier = Modifier.weight(1f),
                            label = "$currentYear harvests",
                            value = currentYearHarvests.size.toString(),
                            minWidth = 0.dp,
                            onClick = { selectedTracker = HarvestTracker.CURRENT_YEAR }
                        )
                        com.dillon.orcharddex.ui.components.StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Total harvests",
                            value = harvestHistory.size.toString(),
                            minWidth = 0.dp,
                            onClick = { selectedTracker = HarvestTracker.TOTAL }
                        )
                    }
                }
            }
            item {
                SectionCard("Search") {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search history") }
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HistoryQuickFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = quickFilter == filter,
                                onClick = { quickFilter = filter },
                                label = { Text(filter.label) }
                            )
                        }
                    }
                    Text(
                        text = "${filteredHistory.size} entries shown",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            when {
                history.isEmpty() -> item {
                    EmptyStateCard(
                        title = "No history yet",
                        message = "Add an event or harvest to start building your orchard timeline."
                    )
                }

                filteredHistory.isEmpty() -> item {
                    EmptyStateCard(
                        title = "No logs match these filters",
                        message = "Try a different search or switch the quick filter."
                    )
                }

                else -> {
                    groupedHistory.forEach { (monthLabel, entries) ->
                        item(key = monthLabel) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                                Text(
                                text = "${entries.size} entr${if (entries.size == 1) "y" else "ies"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        items(entries, key = { "${it.kind.name}-${it.id}" }) { entry ->
                            HistoryRow(
                                entry = entry,
                                onClick = { onEntryClick(entry.kind, entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel,
    onBack: () -> Unit,
    onOpenTree: (String) -> Unit
) {
    val entry = viewModel.detail
    val context = LocalContext.current

    when {
        viewModel.isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading log...")
        }

        entry == null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Entry not found.")
        }

        else -> {
            val historyEntry = requireNotNull(entry)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionCard(historyEntry.title) {
                        Text(historyEntry.treeLabel, style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompactFact("Logged", historyEntry.date.toDateLabel())
                            CompactFact("Kind", historyEntry.kind.detailLabel())
                            historyEntry.eventType?.let { CompactFact("Event", it.detailLabel()) }
                            historyEntry.quantityValue?.let { value ->
                                CompactFact(
                                    "Qty",
                                    listOf(value.displayAmount(), historyEntry.quantityUnit.orEmpty())
                                        .joinToString(" ")
                                        .trim()
                                )
                            }
                            historyEntry.cost?.let { CompactFact("Cost", "$${it.displayAmount()}") }
                            historyEntry.qualityRating?.let { CompactFact("Quality", "$it/5") }
                            if (historyEntry.kind == ActivityKind.HARVEST) {
                                CompactFact("Verified", if (historyEntry.verified) "Yes" else "No")
                            }
                            CompactFact("Species", historyEntry.species)
                            CompactFact("Cultivar", historyEntry.cultivar)
                            if (historyEntry.firstFruit) {
                                CompactFact("Flag", "First fruit")
                            }
                        }
                    }
                }
                historyEntry.photoPath?.let { photoPath ->
                    item {
                        SectionCard("Photo") {
                            LocalPhotoStrip(
                                existingPaths = listOf(
                                    historyEntry.id to File(context.filesDir, "photos/$photoPath").absolutePath
                                )
                            )
                        }
                    }
                }
                item {
                    SectionCard("Notes") {
                        Text(historyEntry.notes.ifBlank { "No notes for this log." })
                    }
                }
                if (historyEntry.preview.isNotBlank() && historyEntry.preview != historyEntry.notes) {
                    item {
                        SectionCard("Summary") {
                            Text(historyEntry.preview)
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { onOpenTree(historyEntry.treeId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open plant")
                        }
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: HistoryEntryModel,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.widthIn(min = 54.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.date.dayNumberLabel(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = entry.date.monthShortLabel(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = entry.treeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.rowMetaLine(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.preview.isNotBlank()) {
                    Text(
                        text = entry.preview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HarvestTrackerDialog(
    title: String,
    speciesCounts: List<HarvestSpeciesCount>,
    totalHarvests: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (speciesCounts.isEmpty()) {
                Text("No harvests logged yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "$totalHarvests harvests across ${speciesCounts.size} species.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(speciesCounts, key = { it.species }) { item ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.species,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.harvestCount.toString(),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
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

private fun HistoryQuickFilter.matches(entry: HistoryEntryModel): Boolean = when (this) {
    HistoryQuickFilter.ALL -> true
    HistoryQuickFilter.EVENTS -> entry.kind == ActivityKind.EVENT
    HistoryQuickFilter.HARVESTS -> entry.kind == ActivityKind.HARVEST
    HistoryQuickFilter.SEASONAL -> entry.eventType in setOf(EventType.BLOOM, EventType.FRUIT_SET)
    HistoryQuickFilter.ISSUES -> entry.eventType in setOf(
        EventType.PEST_OBSERVED,
        EventType.DISEASE_OBSERVED,
        EventType.FROST_DAMAGE,
        EventType.HEAT_STRESS
    )
}

private fun HistoryEntryModel.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    return listOf(
        title,
        preview,
        notes,
        treeLabel,
        species,
        cultivar,
        kind.detailLabel(),
        eventType?.detailLabel().orEmpty()
    ).any { value ->
        value.lowercase().contains(query)
    }
}

private fun HistoryEntryModel.rowMetaLine(): String = buildString {
    append(kind.detailLabel())
    eventType?.let {
        append(" - ")
        append(it.detailLabel())
    }
    quantityValue?.let { value ->
        append(" - ")
        append(value.displayAmount())
        quantityUnit?.takeIf(String::isNotBlank)?.let { unit ->
            append(" ")
            append(unit)
        }
    }
    qualityRating?.let {
        append(" - quality ")
        append(it)
        append("/5")
    }
    if (firstFruit) {
        append(" - first fruit")
    }
    if (kind == ActivityKind.HARVEST) {
        append(if (verified) " - verified" else " - unverified")
    }
}

private fun HistoryEntryModel.isIssueLog(): Boolean = eventType in setOf(
    EventType.PEST_OBSERVED,
    EventType.DISEASE_OBSERVED,
    EventType.FROST_DAMAGE,
    EventType.HEAT_STRESS
)

private fun ActivityKind.detailLabel(): String = when (this) {
    ActivityKind.EVENT -> "Event"
    ActivityKind.HARVEST -> "Harvest"
}

private fun EventType.detailLabel(): String = name.lowercase()
    .replace("_", " ")
    .replaceFirstChar(Char::uppercase)

private fun Long.monthBucketLabel(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(historyMonthFormatter)

private fun Long.dayNumberLabel(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(historyDayFormatter)

private fun Long.monthShortLabel(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(historyMonthShortFormatter)
