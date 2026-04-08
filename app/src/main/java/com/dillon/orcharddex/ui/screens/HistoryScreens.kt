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
import com.dillon.orcharddex.data.model.SaleKind
import com.dillon.orcharddex.data.local.SaleEntity
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.ui.components.CompactFact
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.LocalPhotoStrip
import com.dillon.orcharddex.ui.components.SaleDialog
import com.dillon.orcharddex.ui.components.SaleDraftState
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.epochToLocalDate
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.viewmodel.HistoryDetailViewModel
import com.dillon.orcharddex.ui.viewmodel.HistoryViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
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

private data class SeasonalSummary(
    val year: Int,
    val harvestCount: Int,
    val firstFruitCount: Int,
    val speciesCount: Int,
    val topSpecies: String?,
    val peakMonth: String?
)

private data class SalesSummary(
    val saleCount: Int = 0,
    val treeSaleCount: Int = 0,
    val harvestSaleCount: Int = 0,
    val currentMonthRevenue: Double = 0.0,
    val totalRevenue: Double = 0.0
)

private data class RecentSaleSummary(
    val id: String,
    val title: String,
    val subtitle: String,
    val detail: String
)

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEntryClick: (ActivityKind, String) -> Unit,
    onAddLog: () -> Unit,
    onAddEvent: () -> Unit,
    onAddHarvest: () -> Unit,
    onAddPlant: () -> Unit
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    var quickFilter by rememberSaveable { mutableStateOf(HistoryQuickFilter.ALL) }
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
    val currentYear = remember { OrchardTime.currentYear() }
    val harvestHistory = remember(history) {
        history.filter { entry -> entry.kind == ActivityKind.HARVEST }
    }
    val currentYearHarvests = remember(harvestHistory, currentYear) {
        harvestHistory.filter { entry ->
            Instant.ofEpochMilli(entry.date).atZone(OrchardTime.zoneId()).year == currentYear
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
    val seasonalSummaries = remember(harvestHistory) {
        harvestHistory
            .groupBy { entry ->
                Instant.ofEpochMilli(entry.date).atZone(OrchardTime.zoneId()).year
            }
            .map { (year, entries) ->
                val topSpecies = entries
                    .groupingBy { it.species.ifBlank { "Unknown species" } }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
                val peakMonth = entries
                    .groupingBy(HistoryEntryModel::monthShortLabel)
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
                SeasonalSummary(
                    year = year,
                    harvestCount = entries.size,
                    firstFruitCount = entries.count(HistoryEntryModel::firstFruit),
                    speciesCount = entries.map { it.species.ifBlank { "Unknown species" } }.distinct().size,
                    topSpecies = topSpecies,
                    peakMonth = peakMonth
                )
            }
            .sortedByDescending(SeasonalSummary::year)
    }
    val currentSeasonSummary = seasonalSummaries.firstOrNull { it.year == currentYear }
        ?: seasonalSummaries.firstOrNull()
    val treeLabels = remember(trees) {
        trees.associate { tree -> tree.id to tree.displayName() }
    }
    val salesSummary = remember(sales) {
        val currentMonth = OrchardTime.currentYearMonth()
        SalesSummary(
            saleCount = sales.size,
            treeSaleCount = sales.count { sale -> sale.saleKind == SaleKind.TREE },
            harvestSaleCount = sales.count { sale -> sale.saleKind == SaleKind.HARVEST },
            currentMonthRevenue = sales
                .filter { sale -> YearMonth.from(epochToLocalDate(sale.soldAt)) == currentMonth }
                .sumOf(SaleEntity::totalPrice),
            totalRevenue = sales.sumOf(SaleEntity::totalPrice)
        )
    }
    val recentSales = remember(sales, treeLabels) {
        sales.take(5).map { sale ->
            val treeLabel = treeLabels[sale.treeId].orEmpty()
            RecentSaleSummary(
                id = sale.id,
                title = treeLabel.ifBlank { sale.saleKind.historyLabel() },
                subtitle = listOf(sale.saleKind.historyLabel(), sale.saleChannel.label)
                    .joinToString(" - "),
                detail = "${sale.soldAt.toDateLabel()} - $${sale.totalPrice.displayAmount()}"
            )
        }
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
                onClick = onAddLog,
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
                SectionCard("Season analytics") {
                    if (seasonalSummaries.isEmpty()) {
                        Text("Add harvests to compare seasons, first fruit, and busiest months.")
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            com.dillon.orcharddex.ui.components.StatCard(
                                modifier = Modifier.weight(1f),
                                label = "${currentSeasonSummary?.year ?: currentYear} harvests",
                                value = (currentSeasonSummary?.harvestCount ?: 0).toString(),
                                minWidth = 0.dp
                            )
                            com.dillon.orcharddex.ui.components.StatCard(
                                modifier = Modifier.weight(1f),
                                label = "First fruit logs",
                                value = (currentSeasonSummary?.firstFruitCount ?: 0).toString(),
                                minWidth = 0.dp
                            )
                        }
                        com.dillon.orcharddex.ui.components.StatCard(
                            label = "Harvested species",
                            value = (currentSeasonSummary?.speciesCount ?: 0).toString(),
                            minWidth = 0.dp
                        )
                        seasonalSummaries.take(3).forEach { summary ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${summary.year} season",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${summary.harvestCount} harvests • ${summary.speciesCount} species • ${summary.firstFruitCount} first-fruit logs",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    summary.topSpecies?.let {
                                        Text(
                                            text = "Top species: $it",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    summary.peakMonth?.let {
                                        Text(
                                            text = "Peak month: $it",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (settings.showSalesTools) {
                item {
                    SectionCard("Sales") {
                        if (salesSummary.saleCount == 0) {
                            Text("No sales recorded yet.")
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                com.dillon.orcharddex.ui.components.StatCard(
                                    modifier = Modifier.weight(1f),
                                    label = "This month",
                                    value = "$${salesSummary.currentMonthRevenue.displayAmount()}",
                                    minWidth = 0.dp
                                )
                                com.dillon.orcharddex.ui.components.StatCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Total revenue",
                                    value = "$${salesSummary.totalRevenue.displayAmount()}",
                                    minWidth = 0.dp
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactFact("Transactions", salesSummary.saleCount.toString())
                                CompactFact("Plant sales", salesSummary.treeSaleCount.toString())
                                CompactFact("Harvest sales", salesSummary.harvestSaleCount.toString())
                            }
                            Text("Recent sales", style = MaterialTheme.typography.labelLarge)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                recentSales.forEach { sale ->
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = sale.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = sale.subtitle,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = sale.detail,
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
                    if (trees.isEmpty()) {
                        EmptyStateCard(
                            title = "No history yet",
                            message = "Add your first plant before you start logging orchard events and harvests.",
                            primaryActionLabel = "Add first plant",
                            onPrimaryAction = onAddPlant
                        )
                    } else {
                        EmptyStateCard(
                            title = "No history yet",
                            message = "Log your first event or harvest to start building your orchard timeline.",
                            primaryActionLabel = "Log first event",
                            onPrimaryAction = onAddEvent,
                            secondaryActionLabel = "Log first harvest",
                            onSecondaryAction = onAddHarvest
                        )
                    }
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
    settings: AppSettings,
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
            var showSaleDialog by rememberSaveable(historyEntry.id) { mutableStateOf(false) }
            var saleDraft by remember(historyEntry.id, historyEntry.saleCount) {
                mutableStateOf(defaultHarvestSaleDraft(historyEntry))
            }
            var saleDraftError by remember(historyEntry.id, historyEntry.saleCount) {
                mutableStateOf<String?>(null)
            }
            val remainingQuantity = historyEntry.remainingQuantity ?: historyEntry.quantityValue ?: 0.0

            if (showSaleDialog) {
                SaleDialog(
                    title = "Record harvest sale",
                    confirmLabel = if (viewModel.saleBusy) "Saving..." else "Record sale",
                    state = saleDraft,
                    onStateChange = {
                        saleDraft = it
                        saleDraftError = null
                    },
                    onDismiss = {
                        showSaleDialog = false
                        saleDraftError = null
                    },
                    onConfirm = {
                        val quantity = saleDraft.quantityValue.toDoubleOrNull()
                        val unitPrice = saleDraft.unitPrice.toDoubleOrNull()
                        when {
                            quantity == null || quantity <= 0.0 -> {
                                saleDraftError = "Enter a valid quantity."
                            }
                            quantity > remainingQuantity + 0.0001 -> {
                                saleDraftError = "Sale quantity exceeds the remaining logged harvest."
                            }
                            unitPrice == null || unitPrice < 0.0 -> {
                                saleDraftError = "Enter a valid unit price."
                            }
                            else -> {
                                saleDraftError = null
                                viewModel.recordHarvestSale(
                                    soldDate = saleDraft.soldDate,
                                    quantityValue = quantity,
                                    quantityUnit = saleDraft.quantityUnit,
                                    unitPrice = unitPrice,
                                    saleChannel = saleDraft.saleChannel,
                                    notes = saleDraft.notes
                                ) {
                                    showSaleDialog = false
                                }
                            }
                        }
                    },
                    remainingLabel = "Remaining to sell: ${remainingQuantity.displayAmount()} ${historyEntry.quantityUnit.orEmpty()}",
                    errorMessage = saleDraftError ?: viewModel.saleErrorMessage,
                    saving = viewModel.saleBusy
                )
            }
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
                            if (historyEntry.kind == ActivityKind.HARVEST && !historyEntry.verified) {
                                CompactFact("Verified", "No")
                            }
                            CompactFact("Species", historyEntry.species)
                            CompactFact("Cultivar", historyEntry.cultivar)
                            if (historyEntry.firstFruit) {
                                CompactFact("Flag", "First fruit")
                            }
                            if (settings.showSalesTools && historyEntry.saleCount > 0) {
                                CompactFact("Sales", historyEntry.saleCount.toString())
                            }
                            if (settings.showSalesTools) {
                                historyEntry.revenue?.let { CompactFact("Revenue", "$${it.displayAmount()}") }
                            }
                        }
                    }
                }
                val photoPaths = historyEntry.photoPaths.ifEmpty { listOfNotNull(historyEntry.photoPath) }
                if (photoPaths.isNotEmpty()) {
                    item {
                        SectionCard(if (photoPaths.size == 1) "Photo" else "Photos") {
                            LocalPhotoStrip(
                                existingPaths = photoPaths.mapIndexed { index, photoPath ->
                                    "${historyEntry.id}:$index" to File(context.filesDir, "photos/$photoPath").absolutePath
                                }
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
                if (settings.showSalesTools && historyEntry.kind == ActivityKind.HARVEST) {
                    item {
                        SectionCard("Sales") {
                            if (historyEntry.saleCount == 0) {
                                Text("No sales recorded for this harvest yet.")
                            } else {
                                historyEntry.soldQuantity?.let { soldQuantity ->
                                    Text(
                                        "Sold ${soldQuantity.displayAmount()} ${historyEntry.quantityUnit.orEmpty()} for $${historyEntry.revenue?.displayAmount() ?: "0"}."
                                    )
                                }
                                historyEntry.remainingQuantity?.let { remaining ->
                                    Text(
                                        text = "Remaining logged harvest: ${remaining.displayAmount()} ${historyEntry.quantityUnit.orEmpty()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (settings.showSalesTools && historyEntry.kind == ActivityKind.HARVEST && remainingQuantity > 0.0) {
                            OutlinedButton(
                                onClick = {
                                    saleDraft = defaultHarvestSaleDraft(historyEntry)
                                    saleDraftError = null
                                    showSaleDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Record sale")
                            }
                        }
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
    HistoryQuickFilter.SEASONAL -> entry.kind == ActivityKind.HARVEST ||
        entry.eventType in setOf(EventType.BUD, EventType.BLOOM, EventType.FRUIT_SET)
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

private fun SaleKind.historyLabel(): String = when (this) {
    SaleKind.TREE -> "Plant sale"
    SaleKind.HARVEST -> "Harvest sale"
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
    if (kind == ActivityKind.HARVEST && !verified) {
        append(" - unverified")
    }
    if (photoPaths.size > 1) {
        append(" - ")
        append(photoPaths.size)
        append(" photos")
    } else if (photoPath != null) {
        append(" - photo")
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
    .atZone(OrchardTime.zoneId())
    .toLocalDate()
    .format(historyMonthFormatter)

private fun Long.dayNumberLabel(): String = Instant.ofEpochMilli(this)
    .atZone(OrchardTime.zoneId())
    .toLocalDate()
    .format(historyDayFormatter)

private fun Long.monthShortLabel(): String = Instant.ofEpochMilli(this)
    .atZone(OrchardTime.zoneId())
    .toLocalDate()
    .format(historyMonthShortFormatter)

private fun HistoryEntryModel.monthShortLabel(): String = date.monthShortLabel()

private fun defaultHarvestSaleDraft(entry: HistoryEntryModel): SaleDraftState {
    val defaultQuantity = entry.remainingQuantity ?: entry.quantityValue ?: 0.0
    return SaleDraftState(
        soldDate = OrchardTime.today(),
        quantityValue = if (defaultQuantity > 0.0) defaultQuantity.displayAmount() else "",
        quantityUnit = entry.quantityUnit.orEmpty()
    )
}
