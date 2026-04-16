package com.dillon.orcharddex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.DashboardDetailItem
import com.dillon.orcharddex.data.model.DashboardModel
import com.dillon.orcharddex.data.model.ForecastConfidence
import com.dillon.orcharddex.data.model.ForecastSource
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.local.SaleEntity
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.BloomPhase
import com.dillon.orcharddex.data.phenology.EverbearingPlant
import com.dillon.orcharddex.data.phenology.PredictedBloomWindow
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.OrchardDexHeroBanner
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.displayAmount
import com.dillon.orcharddex.ui.epochToLocalDate
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.DashboardViewModel
import java.io.File
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

private enum class DashboardStat(val label: String) {
    TREES("Trees"),
    CULTIVARS("Cultivars"),
    SPECIES("Species"),
    WISHLIST("Wishlist"),
    AWAITING_FIRST_FRUIT("Awaiting first fruit"),
    DUE_SOON("Due in 7 days")
}

private enum class DashboardCalendarKind(val label: String, val priority: Int) {
    REMINDER("Task", 0),
    EVENT("Event", 1),
    HARVEST("Harvest", 2),
    BLOOM_FORECAST("Bloom forecast", 3)
}

private data class DashboardCalendarItem(
    val id: String,
    val kind: DashboardCalendarKind,
    val title: String,
    val subtitle: String,
    val detail: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate = startDate,
    val treeId: String? = null
) {
    fun occursOn(day: LocalDate): Boolean = !startDate.isAfter(day) && !endDate.isBefore(day)
}

private data class DashboardCalendarState(
    val agendaItems: List<DashboardCalendarItem> = emptyList(),
    val activeTreeCount: Int = 0,
    val forecastedTreeCount: Int = 0,
    val everbearingPlants: List<EverbearingPlant> = emptyList()
)

private data class DashboardBloomWatchItem(
    val treeId: String,
    val primaryLabel: String,
    val secondaryLabel: String = "",
    val expectedBloomLabel: String,
    val expectedFruitLabel: String,
    val sortDate: LocalDate,
    val infoLines: List<String> = emptyList()
)

private data class DashboardFruitTimingInsight(
    val label: String,
    val nextDate: LocalDate,
    val seasonCount: Int,
    val sourceLabel: String,
    val detailLine: String
)

private data class DashboardDaySummary(
    val reminderCount: Int = 0,
    val logCount: Int = 0,
    val bloomCount: Int = 0
)

private enum class DashboardMonthSection(val label: String) {
    BLOOM_NOW("Bloom now"),
    FRUIT_LIKELY("Fruit likely"),
    REPEAT_BLOOMERS("Repeat bloomers"),
    RECOVERY_WATCH("Recovery watch")
}

private data class DashboardMonthSectionItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val detail: String = "",
    val treeId: String? = null
)

private data class DashboardMonthSectionState(
    val bloomNow: List<DashboardMonthSectionItem> = emptyList(),
    val fruitLikely: List<DashboardMonthSectionItem> = emptyList(),
    val repeatBloomers: List<DashboardMonthSectionItem> = emptyList(),
    val recoveryWatch: List<DashboardMonthSectionItem> = emptyList()
) {
    fun itemsFor(section: DashboardMonthSection): List<DashboardMonthSectionItem> = when (section) {
        DashboardMonthSection.BLOOM_NOW -> bloomNow
        DashboardMonthSection.FRUIT_LIKELY -> fruitLikely
        DashboardMonthSection.REPEAT_BLOOMERS -> repeatBloomers
        DashboardMonthSection.RECOVERY_WATCH -> recoveryWatch
    }
}

private val dashboardMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dashboardDayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
private val dashboardCompactDateFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTree: () -> Unit,
    onAddEvent: () -> Unit,
    onAddHarvest: () -> Unit,
    onAddReminder: () -> Unit,
    onOpenOrchard: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewTree: (String) -> Unit
) {
    val context = LocalContext.current
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle(initialValue = null)
    val dashboardModel = dashboard ?: DashboardModel()
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedStat by rememberSaveable { mutableStateOf<DashboardStat?>(null) }
    var selectedBloomWatch by rememberSaveable { mutableStateOf<String?>(null) }
    var visibleMonthText by rememberSaveable { mutableStateOf(OrchardTime.currentYearMonth().toString()) }
    val visibleMonth = remember(visibleMonthText) { YearMonth.parse(visibleMonthText) }
    val locationProfile = remember(settings) { settings.forecastLocationProfile() }
    val activeTrees = remember(trees) { trees.filter { it.tree.status == TreeStatus.ACTIVE } }
    val calendarState = remember(trees, reminders, history, locationProfile, settings.orchardRegion, visibleMonth) {
        buildDashboardCalendarState(
            defaultLocationProfile = locationProfile,
            orchardRegion = settings.orchardRegion,
            visibleMonth = visibleMonth,
            reminders = reminders,
            history = history,
            activeTrees = activeTrees
        )
    }
    val dueThisWeek = remember(reminders) {
        val now = System.currentTimeMillis()
        val cutoff = now + 7L * 24 * 60 * 60 * 1000
        reminders
            .filter { it.reminder.completedAt == null && it.reminder.enabled && it.reminder.dueAt in now..cutoff }
            .sortedBy { it.reminder.dueAt }
            .take(6)
    }
    val bloomWatchItems = remember(activeTrees, history, locationProfile, settings.orchardRegion) {
        buildDashboardBloomWatchItems(
            defaultLocationProfile = locationProfile,
            orchardRegion = settings.orchardRegion,
            activeTrees = activeTrees,
            history = history
        )
    }
    val seasonHarvestTotals = remember(history) {
        val currentYear = OrchardTime.today().year
        history
            .filter { it.kind == ActivityKind.HARVEST && epochToLocalDate(it.date).year == currentYear }
            .groupBy { it.quantityUnit.orEmpty().ifBlank { "unit" } }
            .mapValues { (_, entries) -> entries.sumOf { it.quantityValue ?: 0.0 } }
            .toList()
            .sortedByDescending { it.second }
    }
    val salesThisMonthRevenue = remember(sales) {
        val currentMonth = OrchardTime.currentYearMonth()
        sales
            .filter { sale -> YearMonth.from(epochToLocalDate(sale.soldAt)) == currentMonth }
            .sumOf(SaleEntity::totalPrice)
    }
    val photoTimeline = remember(history) {
        history
            .filter { it.photoPaths.isNotEmpty() }
            .sortedByDescending(HistoryEntryModel::date)
            .take(8)
    }
    val orchardPulseStats = listOf(
        DashboardStat.TREES to dashboardModel.totalTreeCount,
        DashboardStat.CULTIVARS to dashboardModel.cultivarCount,
        DashboardStat.SPECIES to dashboardModel.speciesCount,
        DashboardStat.WISHLIST to dashboardModel.wishlistCount,
        DashboardStat.AWAITING_FIRST_FRUIT to dashboardModel.awaitingFirstFruitCount,
        DashboardStat.DUE_SOON to dashboardModel.upcoming7Count
    )

    fun jumpMonth(offset: Long) {
        val updatedMonth = visibleMonth.plusMonths(offset)
        visibleMonthText = updatedMonth.toString()
    }

    selectedStat?.let { stat ->
        DashboardDetailDialog(
            stat = stat,
            dashboard = dashboardModel,
            onDismiss = { selectedStat = null },
            onViewTree = onViewTree
        )
    }
    bloomWatchItems.firstOrNull { it.treeId == selectedBloomWatch }?.let { item ->
        DashboardBloomWatchDialog(
            item = item,
            onDismiss = { selectedBloomWatch = null }
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dashboard != null && dashboardModel.totalTreeCount == 0) {
            item {
                EmptyStateCard(
                    title = "Start with your first plant",
                    message = "Add a plant, then use reminders, logs, and bloom timing to track the orchard from one place.",
                    primaryActionLabel = "Add first plant",
                    onPrimaryAction = onAddTree,
                    secondaryActionLabel = "Open Orchard",
                    onSecondaryAction = onOpenOrchard
                )
            }
        }
        item {
            SectionCard("") {
                OrchardDexHeroBanner()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onAddTree) { Text("Add plant") }
                    OutlinedButton(onClick = onAddEvent) { Text("Add event") }
                    OutlinedButton(onClick = onAddHarvest) { Text("Add harvest") }
                    OutlinedButton(onClick = onAddReminder) { Text("Add reminder") }
                }
            }
        }
        item {
            SectionCard("At a glance") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (settings.showSalesTools) {
                            DashboardMiniStatCard(
                                label = "Sales this month",
                                value = "$${salesThisMonthRevenue.displayAmount()}"
                            )
                        }
                        orchardPulseStats.forEach { (stat, value) ->
                            DashboardMiniStatCard(
                                label = stat.label,
                                value = value.toString(),
                                onClick = { selectedStat = stat }
                            )
                        }
                    }
                    if (seasonHarvestTotals.isEmpty()) {
                        Text("No harvests logged this season.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Season harvest total", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            seasonHarvestTotals.forEach { (unit, total) ->
                                DashboardMiniStatCard(
                                    label = unit,
                                    value = total.displayAmount()
                                )
                            }
                        }
                    }
                }
            }
        }
        if (dashboardModel.totalTreeCount > 0 && settings.needsClimateProfileCompletionPrompt()) {
            item {
                EmptyStateCard(
                    title = "Finish your climate profile",
                    message = "Open Settings > Default orchard to add coordinates, elevation, and chill hours so bloom timing stays more accurate.",
                    primaryActionLabel = "Open Settings",
                    onPrimaryAction = onOpenSettings
                )
            }
        }
        item {
            DashboardCalendarSection(
                visibleMonth = visibleMonth,
                calendarState = calendarState,
                onPreviousMonth = { jumpMonth(-1) },
                onNextMonth = { jumpMonth(1) },
                onViewTree = onViewTree
            )
        }
        item {
            SectionCard("Due this week") {
                if (dueThisWeek.isEmpty()) {
                    Text("Nothing due in the next 7 days.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        dueThisWeek.forEach { item ->
                            DashboardActionRow(
                                title = item.reminder.title,
                                subtitle = item.treeLabel ?: "General orchard",
                                detail = item.reminder.dueAt.toDateLabel(),
                                onClick = item.reminder.treeId?.let { treeId -> { onViewTree(treeId) } }
                            )
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Bloom & fruit timing") {
                if (bloomWatchItems.isEmpty()) {
                    Text("No near-term bloom or fruit timing is learned right now.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        bloomWatchItems.forEach { item ->
                            DashboardBloomWatchRow(
                                item = item,
                                onClick = { onViewTree(item.treeId) },
                                onShowInfo = item.infoLines.takeIf { lines -> lines.isNotEmpty() }?.let {
                                    { selectedBloomWatch = item.treeId }
                                }
                            )
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Photo timeline") {
                if (photoTimeline.isEmpty()) {
                    Text("Add photos to harvests or events to build the timeline.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(photoTimeline, key = { "${it.kind.name}-${it.id}" }) { entry ->
                            DashboardPhotoCard(
                                entry = entry,
                                photoPath = entry.photoPaths.firstOrNull(),
                                filesDir = context.filesDir,
                                onClick = { onViewTree(entry.treeId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCalendarSection(
    visibleMonth: YearMonth,
    calendarState: DashboardCalendarState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onViewTree: (String) -> Unit
) {
    SectionCard("Agenda") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onPreviousMonth) { Text("<") }
            Text(
                text = visibleMonth.format(dashboardMonthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(onClick = onNextMonth) { Text(">") }
        }
        val agendaItems = calendarState.agendaItems
            .filter { item -> YearMonth.from(item.startDate) == visibleMonth || YearMonth.from(item.endDate) == visibleMonth }
            .sortedWith(compareBy(DashboardCalendarItem::startDate, DashboardCalendarItem::kind, DashboardCalendarItem::title))
            .take(20)
        if (agendaItems.isEmpty()) {
            Text("No tasks, events, or harvests scheduled in this month.", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                agendaItems.forEach { item ->
                    DashboardCalendarRow(
                        item = item,
                        onClick = item.treeId?.let { treeId -> { onViewTree(treeId) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCalendarRow(
    item: DashboardCalendarItem,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick == null) base else base.clickable(onClick = onClick)
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = item.supportingLine(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashboardMiniStatCard(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier
            .heightIn(min = 84.dp)
            .let { base -> if (onClick == null) base else base.clickable(onClick = onClick) },
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DashboardPhotoCard(
    entry: HistoryEntryModel,
    photoPath: String?,
    filesDir: File,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .size(width = 180.dp, height = 208.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (photoPath != null) {
                AsyncImage(
                    model = File(filesDir, "photos/$photoPath"),
                    contentDescription = entry.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.treeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.date.toDateLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DashboardActionRow(
    title: String,
    subtitle: String,
    detail: String,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { base -> if (onClick == null) base else base.clickable(onClick = onClick) },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        if (detail.isNotBlank()) {
            Text(detail, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DashboardBloomWatchRow(
    item: DashboardBloomWatchItem,
    onClick: () -> Unit,
    onShowInfo: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(item.primaryLabel, style = MaterialTheme.typography.titleMedium)
                if (item.secondaryLabel.isNotBlank()) {
                    Text(
                        text = item.secondaryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            onShowInfo?.let { showInfo ->
                TextButton(onClick = showInfo) {
                    Text("Why?")
                }
            }
        }
        Text(
            text = "Expected bloom: ${item.expectedBloomLabel}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Expected fruit: ${item.expectedFruitLabel}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DashboardBloomWatchDialog(
    item: DashboardBloomWatchItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.primaryLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (item.secondaryLabel.isNotBlank()) {
                    Text(item.secondaryLabel, style = MaterialTheme.typography.bodySmall)
                }
                Text("Expected bloom: ${item.expectedBloomLabel}", style = MaterialTheme.typography.bodyMedium)
                Text("Expected fruit: ${item.expectedFruitLabel}", style = MaterialTheme.typography.bodyMedium)
                item.infoLines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DashboardIndicatorDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .size(6.dp)
            .background(color = color, shape = RoundedCornerShape(999.dp))
    )
}

@Composable
private fun DashboardMonthSectionCard(
    section: DashboardMonthSection,
    count: Int,
    subtitle: String,
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
            Text(section.label, style = MaterialTheme.typography.titleMedium)
            Text("${count} ${"item".pluralize(count)}", style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DashboardDayDialog(
    date: LocalDate,
    items: List<DashboardCalendarItem>,
    onDismiss: () -> Unit,
    onViewTree: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(dashboardDayFormatter)) },
        text = {
            if (items.isEmpty()) {
                Text("No blooms, reminders, or history recorded for this day.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = DashboardCalendarItem::id) { item ->
                        DashboardCalendarRow(
                            item = item,
                            onClick = item.treeId?.let { treeId ->
                                {
                                    onDismiss()
                                    onViewTree(treeId)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DashboardMonthSectionDialog(
    section: DashboardMonthSection,
    visibleMonth: YearMonth,
    items: List<DashboardMonthSectionItem>,
    onDismiss: () -> Unit,
    onViewTree: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${section.label} · ${visibleMonth.format(dashboardMonthFormatter)}") },
        text = {
            if (items.isEmpty()) {
                Text(section.emptyMessage(visibleMonth))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = DashboardMonthSectionItem::id) { item ->
                        DashboardMonthSectionRow(
                            item = item,
                            onClick = item.treeId?.let { treeId ->
                                {
                                    onDismiss()
                                    onViewTree(treeId)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DashboardMonthSectionRow(
    item: DashboardMonthSectionItem,
    onClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { base -> if (onClick == null) base else base.clickable(onClick = onClick) },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        if (item.subtitle.isNotBlank()) {
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
        }
        if (item.detail.isNotBlank()) {
            Text(item.detail, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DashboardDetailDialog(
    stat: DashboardStat,
    dashboard: DashboardModel,
    onDismiss: () -> Unit,
    onViewTree: (String) -> Unit
) {
    val detailItems = stat.detailItems(dashboard)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stat.label) },
        text = {
            if (detailItems.isEmpty()) {
                Text(stat.emptyMessage())
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detailItems, key = DashboardDetailItem::id) { item ->
                        DashboardDetailRow(
                            item = item,
                            onClick = {
                                val treeId = item.treeId ?: return@DashboardDetailRow
                                onDismiss()
                                onViewTree(treeId)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EverbearingFruitDialog(
    plants: List<EverbearingPlant>,
    onDismiss: () -> Unit,
    onViewTree: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Everbearing fruits") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "These fruits are tracked separately because they can bloom across much of the year instead of following a tight month window.",
                    style = MaterialTheme.typography.bodySmall
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plants, key = EverbearingPlant::treeId) { plant ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onViewTree(plant.treeId)
                                },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(plant.treeLabel, style = MaterialTheme.typography.titleMedium)
                            Text(plant.speciesLabel, style = MaterialTheme.typography.bodySmall)
                            Text(plant.detailLabel, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DashboardDetailRow(
    item: DashboardDetailItem,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick == null) base else base.clickable(onClick = onClick)
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        item.supportingLine()?.let { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildDashboardBloomWatchItems(
    defaultLocationProfile: com.dillon.orcharddex.data.model.ForecastLocationProfile,
    orchardRegion: String,
    activeTrees: List<TreeListItem>,
    history: List<HistoryEntryModel>
): List<DashboardBloomWatchItem> {
    val today = OrchardTime.today()
    val observationsByTreeId = historyPhenologyObservationsByTreeId(history)
    val harvestHistory = history.filter { it.kind == ActivityKind.HARVEST }
    return activeTrees.mapNotNull { item ->
        val tree = item.tree
        val treeLocationProfile = item.location?.toForecastLocationProfile() ?: defaultLocationProfile
        val observations = observationsByTreeId[tree.id].orEmpty()
        val learnedWindow = BloomForecastEngine.nextBloomWindow(
            tree = tree,
            locationProfile = treeLocationProfile,
            observations = observations
        )
        val baselineWindow = observations.takeIf { it.isNotEmpty() }?.let {
            BloomForecastEngine.nextBloomWindow(
                tree = tree,
                locationProfile = treeLocationProfile,
                observations = emptyList()
            )
        }
        val fruitInsight = learnedFruitTimingInsight(
            tree = tree,
            history = harvestHistory,
            today = today
        )
        val isBloomCurrent = learnedWindow?.let { !today.isBefore(it.startDate) && !today.isAfter(it.endDate) } == true
        val isBloomSoon = learnedWindow?.startDate?.let { !it.isAfter(today.plusDays(45)) } == true
        val isFruitSoon = fruitInsight?.nextDate?.let { !it.isAfter(today.plusDays(60)) } == true
        if (!isBloomCurrent && !isBloomSoon && !isFruitSoon) return@mapNotNull null

        val bloomSeasonCount = observedSeasonCount(
            observations = observations,
            timezoneId = treeLocationProfile.timezoneId
        )
        val infoLines = buildList {
            if (learnedWindow?.source == ForecastSource.HISTORY_LEARNED && baselineWindow != null) {
                val shiftDays = ChronoUnit.DAYS.between(baselineWindow.startDate, learnedWindow.startDate)
                if (kotlin.math.abs(shiftDays) >= 3L) {
                    add(
                        "Adjusted from your logs: usually ${kotlin.math.abs(shiftDays)} days " +
                            if (shiftDays > 0) "later here." else "earlier here."
                    )
                }
            }
            if (bloomSeasonCount > 0) {
                add("Confidence increased from $bloomSeasonCount prior ${"season".pluralize(bloomSeasonCount)}.")
            }
            fruitInsight?.detailLine?.let(::add)
        }

        DashboardBloomWatchItem(
            treeId = tree.id,
            primaryLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            secondaryLabel = tree.nickname.orEmpty().trim(),
            expectedBloomLabel = learnedWindow?.let { it.expectedTimingLabel(today) }
                ?: BloomForecastEngine.nextBloomSummary(
                    tree = tree,
                    locationProfile = treeLocationProfile,
                    observations = observations
                )?.timingLabel
                ?: "Log bloom events to learn this here",
            expectedFruitLabel = fruitInsight?.label
                ?: fallbackFruitTimingLabel(learnedWindow),
            sortDate = listOfNotNull(
                learnedWindow?.startDate?.takeIf { !today.isAfter(learnedWindow.endDate) }?.let {
                    if (it.isBefore(today)) today else it
                },
                fruitInsight?.nextDate
            ).minOrNull() ?: today,
            infoLines = infoLines
        )
    }.sortedWith(
        compareBy<DashboardBloomWatchItem>({ it.sortDate }, { it.primaryLabel.lowercase() })
    ).take(6)
}

private fun historyPhenologyObservationsByTreeId(
    history: List<HistoryEntryModel>
): Map<String, List<PhenologyObservation>> = history.mapNotNull { entry ->
    when {
        entry.kind == ActivityKind.HARVEST -> PhenologyObservation(
            treeId = entry.treeId,
            dateMillis = entry.date,
            isHarvest = true
        )
        entry.eventType != null -> PhenologyObservation(
            treeId = entry.treeId,
            dateMillis = entry.date,
            eventType = entry.eventType
        )
        else -> null
    }
}.groupBy(PhenologyObservation::treeId)

private fun observedSeasonCount(
    observations: List<PhenologyObservation>,
    timezoneId: String
): Int {
    val zoneId = runCatching { ZoneId.of(timezoneId) }.getOrElse { OrchardTime.zoneId() }
    return observations
        .filter { observation ->
            observation.eventType in setOf(
                com.dillon.orcharddex.data.model.EventType.BUD,
                com.dillon.orcharddex.data.model.EventType.BLOOM,
                com.dillon.orcharddex.data.model.EventType.FRUIT_SET
            )
        }
        .map { observation ->
            Instant.ofEpochMilli(observation.dateMillis).atZone(zoneId).toLocalDate().year
        }
        .distinct()
        .size
}

private fun learnedFruitTimingInsight(
    tree: com.dillon.orcharddex.data.local.TreeEntity,
    history: List<HistoryEntryModel>,
    today: LocalDate
): DashboardFruitTimingInsight? {
    val treeMatches = history.filter { it.treeId == tree.id }
    buildFruitTimingInsight(treeMatches, today, "this tree")?.let { return it }

    val cultivar = tree.cultivar.normalizedDashboardKey()
    if (cultivar.isNotBlank()) {
        val cultivarMatches = history.filter {
            it.treeId != tree.id &&
                it.species.normalizedDashboardKey() == tree.species.normalizedDashboardKey() &&
                it.cultivar.normalizedDashboardKey() == cultivar
        }
        buildFruitTimingInsight(cultivarMatches, today, "this cultivar in your orchard")?.let { return it }
    }

    val speciesMatches = history.filter {
        it.treeId != tree.id &&
            it.species.normalizedDashboardKey() == tree.species.normalizedDashboardKey()
    }
    return buildFruitTimingInsight(speciesMatches, today, "this species in your orchard")
}

private fun buildFruitTimingInsight(
    history: List<HistoryEntryModel>,
    today: LocalDate,
    sourceLabel: String
): DashboardFruitTimingInsight? {
    if (history.isEmpty()) return null
    val dates = history.map { epochToLocalDate(it.date) }.sorted()
    val years = dates.map(LocalDate::getYear).distinct().sorted()
    val dayOfYearValues = dates.map(LocalDate::getDayOfYear).sorted()
    val representativeDayOfYear = dayOfYearValues[dayOfYearValues.size / 2]
    val spreadDays = dayOfYearValues.last() - dayOfYearValues.first()
    val activeMonths = dates.map(LocalDate::getMonthValue).distinct().sorted()
    val monthSpan = activeMonths.last() - activeMonths.first()
    val nextDate = nextOccurrenceForDayOfYear(representativeDayOfYear, today)
    val startLabel = coarseTimingLabel(nextOccurrenceForDayOfYear(dayOfYearValues.first(), today))
    val endLabel = coarseTimingLabel(nextOccurrenceForDayOfYear(dayOfYearValues.last(), today))
    val label = when {
        activeMonths.size >= 6 || spreadDays >= 150 -> "active season, $startLabel - $endLabel"
        activeMonths.size >= 3 || spreadDays >= 60 -> "repeat waves, $startLabel - $endLabel"
        activeMonths.size == 2 || monthSpan >= 1 -> "$startLabel - $endLabel"
        else -> coarseTimingLabel(nextDate)
    }
    return DashboardFruitTimingInsight(
        label = label,
        nextDate = nextDate,
        seasonCount = years.size,
        sourceLabel = sourceLabel,
        detailLine = "Expected fruit learned from ${years.size} ${"season".pluralize(years.size)} of harvest logs for $sourceLabel."
    )
}

private fun PredictedBloomWindow.expectedTimingLabel(today: LocalDate): String = when {
    !today.isBefore(startDate) && !today.isAfter(endDate) -> "now"
    patternType == com.dillon.orcharddex.data.model.BloomPatternType.SINGLE_ANNUAL -> {
        val midpoint = startDate.plusDays(ChronoUnit.DAYS.between(startDate, endDate) / 2)
        coarseTimingLabel(midpoint)
    }
    patternType == com.dillon.orcharddex.data.model.BloomPatternType.MULTI_WAVE ->
        "repeat waves, ${coarseTimingLabel(startDate)} - ${coarseTimingLabel(endDate)}"
    patternType == com.dillon.orcharddex.data.model.BloomPatternType.CONTINUOUS ->
        "active season, ${coarseTimingLabel(startDate)} - ${coarseTimingLabel(endDate)}"
    else -> "${coarseTimingLabel(startDate)} - ${coarseTimingLabel(endDate)}"
}

private fun fallbackFruitTimingLabel(window: PredictedBloomWindow?): String = when (window?.patternType) {
    com.dillon.orcharddex.data.model.BloomPatternType.MULTI_WAVE -> "repeat waves after bloom"
    com.dillon.orcharddex.data.model.BloomPatternType.CONTINUOUS -> "active season varies"
    com.dillon.orcharddex.data.model.BloomPatternType.ALTERNATE_YEAR -> "varies in active years"
    com.dillon.orcharddex.data.model.BloomPatternType.SINGLE_ANNUAL -> "after bloom"
    com.dillon.orcharddex.data.model.BloomPatternType.MANUAL_ONLY -> "learn from harvest logs"
    com.dillon.orcharddex.data.model.BloomPatternType.SUPPRESSED, null -> "Log harvests to learn this here"
}

private fun coarseTimingLabel(date: LocalDate): String = when (date.dayOfMonth) {
    in 1..10 -> "early ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)}"
    in 11..20 -> "mid ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)}"
    else -> "late ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)}"
}

private fun nextOccurrenceForDayOfYear(dayOfYear: Int, today: LocalDate): LocalDate {
    val currentYearDate = LocalDate.ofYearDay(
        today.year,
        dayOfYear.coerceAtMost(YearMonth.of(today.year, 12).atEndOfMonth().dayOfYear)
    )
    return if (!currentYearDate.isBefore(today)) {
        currentYearDate
    } else {
        LocalDate.ofYearDay(
            today.year + 1,
            dayOfYear.coerceAtMost(YearMonth.of(today.year + 1, 12).atEndOfMonth().dayOfYear)
        )
    }
}

private fun String.normalizedDashboardKey(): String = trim().lowercase()

private fun buildDashboardMonthSectionState(
    visibleMonth: YearMonth,
    calendarState: DashboardCalendarState,
    reminders: List<ReminderListItem>,
    history: List<HistoryEntryModel>
): DashboardMonthSectionState {
    val bloomNow = emptyList<DashboardMonthSectionItem>()
    val fruitLikely = history
        .filter { it.kind == com.dillon.orcharddex.data.model.ActivityKind.HARVEST }
        .filter { YearMonth.from(epochToLocalDate(it.date)) == visibleMonth }
        .sortedByDescending(HistoryEntryModel::date)
        .map { entry ->
            DashboardMonthSectionItem(
                id = entry.id,
                title = entry.treeLabel,
                subtitle = entry.species,
                detail = entry.harvestSummary(),
                treeId = entry.treeId
            )
        }
    val repeatBloomers = calendarState.everbearingPlants.map { plant ->
        DashboardMonthSectionItem(
            id = plant.treeId,
            title = plant.treeLabel,
            subtitle = plant.speciesLabel,
            detail = plant.detailLabel,
            treeId = plant.treeId
        )
    }
    val recoveryWatch = reminders
        .filter { it.reminder.treeId != null && it.reminder.enabled && it.reminder.completedAt == null }
        .filter { YearMonth.from(epochToLocalDate(it.reminder.dueAt)) == visibleMonth }
        .sortedBy { it.reminder.dueAt }
        .map { item ->
            DashboardMonthSectionItem(
                id = item.reminder.id,
                title = item.treeLabel ?: item.reminder.title,
                subtitle = item.reminder.title,
                detail = item.reminder.dueAt.toDateLabel(),
                treeId = item.reminder.treeId
            )
        }
    return DashboardMonthSectionState(
        bloomNow = bloomNow,
        fruitLikely = fruitLikely,
        repeatBloomers = repeatBloomers,
        recoveryWatch = recoveryWatch
    )
}

private fun buildDashboardCalendarState(
    defaultLocationProfile: com.dillon.orcharddex.data.model.ForecastLocationProfile,
    orchardRegion: String,
    visibleMonth: YearMonth,
    reminders: List<ReminderListItem>,
    history: List<HistoryEntryModel>,
    activeTrees: List<TreeListItem>
): DashboardCalendarState {
    val observationsByTreeId = history
        .mapNotNull { entry ->
            when {
                entry.kind == com.dillon.orcharddex.data.model.ActivityKind.HARVEST -> PhenologyObservation(
                    treeId = entry.treeId,
                    dateMillis = entry.date,
                    isHarvest = true
                )
                entry.eventType != null -> PhenologyObservation(
                    treeId = entry.treeId,
                    dateMillis = entry.date,
                    eventType = entry.eventType
                )
                else -> null
            }
        }
        .groupBy(PhenologyObservation::treeId)
    val bloomWindows = activeTrees.flatMap { item ->
        BloomForecastEngine.predictMonth(
            trees = listOf(item.tree),
            yearMonth = visibleMonth,
            locationProfile = item.location?.toForecastLocationProfile() ?: defaultLocationProfile,
            orchardRegionCode = orchardRegion.takeIf(String::isNotBlank),
            observations = observationsByTreeId[item.tree.id].orEmpty()
        )
    }
    val everbearingPlants = BloomForecastEngine.everbearingPlants(
        trees = activeTrees.map(TreeListItem::tree),
        locationProfilesByTreeId = activeTrees.associate { item ->
            item.tree.id to (item.location?.toForecastLocationProfile() ?: defaultLocationProfile)
        }
    )
    val reminderItems = reminders.mapNotNull { reminderItem ->
        val reminder = reminderItem.reminder
        val dueDate = epochToLocalDate(reminder.dueAt)
        if (!reminder.enabled || reminder.completedAt != null || YearMonth.from(dueDate) != visibleMonth) {
            return@mapNotNull null
        }
        DashboardCalendarItem(
            id = "reminder:${reminder.id}",
            kind = DashboardCalendarKind.REMINDER,
            title = reminder.title,
            subtitle = reminderItem.treeLabel ?: "General orchard",
            detail = reminder.notes.trim().takeIf(String::isNotBlank).orEmpty(),
            startDate = dueDate,
            treeId = reminder.treeId
        )
    }
    val historyItems = history.mapNotNull { entry ->
        val entryDate = epochToLocalDate(entry.date)
        if (YearMonth.from(entryDate) != visibleMonth) return@mapNotNull null
        DashboardCalendarItem(
            id = "${entry.kind.name.lowercase()}:${entry.id}",
            kind = if (entry.kind == com.dillon.orcharddex.data.model.ActivityKind.HARVEST) {
                DashboardCalendarKind.HARVEST
            } else {
                DashboardCalendarKind.EVENT
            },
            title = entry.title,
            subtitle = entry.treeLabel,
            detail = entry.preview.trim().take(72),
            startDate = entryDate,
            treeId = entry.treeId
        )
    }
    val allItems = (reminderItems + historyItems).sortedWith(
        compareBy<DashboardCalendarItem>({ it.startDate }, { it.kind.priority }, { it.title.lowercase() })
    )
    return DashboardCalendarState(
        agendaItems = allItems,
        activeTreeCount = activeTrees.size,
        forecastedTreeCount = bloomWindows.map(PredictedBloomWindow::treeId).distinct().size,
        everbearingPlants = everbearingPlants
    )
}

private fun PredictedBloomWindow.toCalendarItem(): DashboardCalendarItem = DashboardCalendarItem(
    id = "bloom:$treeId:${startDate}",
    kind = DashboardCalendarKind.BLOOM_FORECAST,
    title = treeLabel,
    subtitle = "",
    detail = "${startDate.format(dashboardCompactDateFormatter)} - ${endDate.format(dashboardCompactDateFormatter)}",
    startDate = startDate,
    endDate = endDate,
    treeId = treeId
)

private fun DashboardCalendarItem.supportingLine(): String = buildString {
    append(kind.label)
    if (detail.isNotBlank()) {
        append(" - ")
        append(detail)
    }
}

private fun AppSettings.needsClimateProfileCompletionPrompt(): Boolean {
    if (defaultLocationId.isBlank()) return true
    val profile = forecastLocationProfile()
    val missingCoordinates = profile.latitudeDeg == null || profile.longitudeDeg == null
    val missingElevation = profile.elevationM == null
    val missingChillBand = profile.effectiveChillHoursBand() == ChillHoursBand.UNKNOWN
    return missingCoordinates || missingElevation || missingChillBand
}

private fun List<DashboardCalendarItem>.summaryFor(day: LocalDate): DashboardDaySummary {
    val itemsForDay = filter { it.occursOn(day) }
    return DashboardDaySummary(
        reminderCount = itemsForDay.count { it.kind == DashboardCalendarKind.REMINDER },
        logCount = itemsForDay.count { it.kind == DashboardCalendarKind.EVENT || it.kind == DashboardCalendarKind.HARVEST },
        bloomCount = itemsForDay.count { it.kind == DashboardCalendarKind.BLOOM_FORECAST }
    )
}

private fun defaultSelectedDateForMonth(yearMonth: YearMonth): LocalDate =
    if (yearMonth == OrchardTime.currentYearMonth()) OrchardTime.today() else yearMonth.atDay(1)

private fun String.pluralize(count: Int): String = if (count == 1) this else "${this}s"

private fun DashboardStat.detailItems(dashboard: DashboardModel): List<DashboardDetailItem> = when (this) {
    DashboardStat.TREES -> dashboard.treeItems
    DashboardStat.CULTIVARS -> dashboard.cultivarItems
    DashboardStat.SPECIES -> dashboard.speciesItems
    DashboardStat.WISHLIST -> dashboard.wishlistItems
    DashboardStat.AWAITING_FIRST_FRUIT -> dashboard.awaitingFirstFruitItems
    DashboardStat.DUE_SOON -> dashboard.upcoming7Items
}

private fun DashboardStat.emptyMessage(): String = when (this) {
    DashboardStat.TREES -> "No plants tracked yet."
    DashboardStat.CULTIVARS -> "No cultivars tracked yet."
    DashboardStat.SPECIES -> "No species tracked yet."
    DashboardStat.WISHLIST -> "No wishlist items yet."
    DashboardStat.AWAITING_FIRST_FRUIT -> "All active trees have reached first fruit."
    DashboardStat.DUE_SOON -> "Nothing is due in the next 7 days."
}

private fun DashboardMonthSection.emptyMessage(visibleMonth: YearMonth): String = when (this) {
    DashboardMonthSection.BLOOM_NOW -> "No predicted bloom windows in ${visibleMonth.format(dashboardMonthFormatter)}."
    DashboardMonthSection.FRUIT_LIKELY -> "No logged harvest activity matched ${visibleMonth.format(dashboardMonthFormatter)}."
    DashboardMonthSection.REPEAT_BLOOMERS -> "No repeat bloomers are tracked separately right now."
    DashboardMonthSection.RECOVERY_WATCH -> "No active tree-linked reminders are due in ${visibleMonth.format(dashboardMonthFormatter)}."
}

private fun HistoryEntryModel.harvestSummary(): String = buildString {
    val quantityLine = when {
        quantityValue != null && !quantityUnit.isNullOrBlank() -> {
            val normalized = if (quantityValue % 1.0 == 0.0) quantityValue.toInt().toString() else quantityValue.toString()
            "$normalized $quantityUnit"
        }
        preview.isNotBlank() -> preview.trim().take(72)
        else -> "Harvest logged"
    }
    append(quantityLine)
    append(" • ")
    append(date.toDateLabel())
}

private fun DashboardDetailItem.supportingLine(): String? = buildString {
    if (subtitle.isNotBlank()) {
        append(subtitle)
    }
    date?.let {
        if (isNotBlank()) append(" - ")
        append(it.toDateLabel())
    }
}.takeIf(String::isNotBlank)
