package com.dillon.orcharddex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.model.DashboardDetailItem
import com.dillon.orcharddex.data.model.DashboardModel
import com.dillon.orcharddex.data.model.ForecastConfidence
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.BloomPhase
import com.dillon.orcharddex.data.phenology.EverbearingPlant
import com.dillon.orcharddex.data.phenology.PredictedBloomWindow
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.OrchardDexHeroBanner
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.epochToLocalDate
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.DashboardViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
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
    val items: List<DashboardCalendarItem> = emptyList(),
    val activeTreeCount: Int = 0,
    val forecastedTreeCount: Int = 0,
    val everbearingPlants: List<EverbearingPlant> = emptyList()
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
    onViewTree: (String) -> Unit
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle(initialValue = null)
    val dashboardModel = dashboard ?: DashboardModel()
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedStat by rememberSaveable { mutableStateOf<DashboardStat?>(null) }
    var selectedMonthSection by rememberSaveable { mutableStateOf<DashboardMonthSection?>(null) }
    var showSelectedDayDialog by rememberSaveable { mutableStateOf(false) }
    var visibleMonthText by rememberSaveable { mutableStateOf(OrchardTime.currentYearMonth().toString()) }
    var selectedDateText by rememberSaveable { mutableStateOf(OrchardTime.today().toString()) }
    val visibleMonth = remember(visibleMonthText) { YearMonth.parse(visibleMonthText) }
    val selectedDate = remember(selectedDateText) { LocalDate.parse(selectedDateText) }
    val locationProfile = remember(settings) { settings.forecastLocationProfile() }
    val calendarState = remember(trees, reminders, history, locationProfile, settings.orchardRegion, visibleMonth) {
        buildDashboardCalendarState(
            defaultLocationProfile = locationProfile,
            orchardRegion = settings.orchardRegion,
            visibleMonth = visibleMonth,
            reminders = reminders,
            history = history,
            activeTrees = trees.filter { it.tree.status == TreeStatus.ACTIVE }
        )
    }
    val itemsForSelectedDay = remember(calendarState.items, selectedDate) {
        calendarState.items
            .filter { it.occursOn(selectedDate) }
            .sortedWith(compareBy(DashboardCalendarItem::startDate, DashboardCalendarItem::kind, DashboardCalendarItem::title))
    }
    val monthSectionState = remember(calendarState, reminders, history, visibleMonth) {
        buildDashboardMonthSectionState(
            visibleMonth = visibleMonth,
            calendarState = calendarState,
            reminders = reminders,
            history = history
        )
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
        selectedDateText = defaultSelectedDateForMonth(updatedMonth).toString()
    }

    selectedStat?.let { stat ->
        DashboardDetailDialog(
            stat = stat,
            dashboard = dashboardModel,
            onDismiss = { selectedStat = null },
            onViewTree = onViewTree
        )
    }
    selectedMonthSection?.let { section ->
        DashboardMonthSectionDialog(
            section = section,
            visibleMonth = visibleMonth,
            items = monthSectionState.itemsFor(section),
            onDismiss = { selectedMonthSection = null },
            onViewTree = onViewTree
        )
    }
    if (showSelectedDayDialog) {
        DashboardDayDialog(
            date = selectedDate,
            items = itemsForSelectedDay,
            onDismiss = { showSelectedDayDialog = false },
            onViewTree = onViewTree
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dashboard != null && dashboardModel.totalTreeCount == 0) {
            item {
                EmptyStateCard(
                    title = "Start with your first tree",
                    message = "Add a plant, then use reminders, events, harvests, and bloom forecasts to run the orchard from one place."
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
            DashboardCalendarSection(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                settings = settings,
                calendarState = calendarState,
                monthSectionState = monthSectionState,
                onPreviousMonth = { jumpMonth(-1) },
                onNextMonth = { jumpMonth(1) },
                onSelectDay = {
                    selectedDateText = it.toString()
                    showSelectedDayDialog = true
                },
                onOpenMonthSection = { selectedMonthSection = it }
            )
        }
        item {
            SectionCard("Orchard pulse") {
                Text(
                    text = if (dashboardModel.totalTreeCount == 0) {
                        "The dashboard becomes your quick review board once plants and reminders are in place."
                    } else {
                        "Scan the collection, spot due work, and jump into the right plant or reminder list quickly."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    orchardPulseStats.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (stat, value) ->
                                StatCard(
                                    label = stat.label,
                                    value = value.toString(),
                                    modifier = Modifier.weight(1f),
                                    minWidth = 0.dp
                                ) {
                                    selectedStat = stat
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "Tap any count to inspect the underlying list.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DashboardCalendarSection(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    settings: AppSettings,
    calendarState: DashboardCalendarState,
    monthSectionState: DashboardMonthSectionState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onOpenMonthSection: (DashboardMonthSection) -> Unit
) {
    SectionCard("Calendar") {
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
        when {
            calendarState.activeTreeCount == 0 -> {
                Text("Add an active plant to see bloom timing here.", style = MaterialTheme.typography.bodySmall)
            }
            !settings.forecastLocationProfile().hasForecastSignals() -> {
                Text(
                    "Add latitude, elevation, chill hours, or a USDA zone in Settings to tune bloom timing.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            calendarState.forecastedTreeCount in 1 until calendarState.activeTreeCount -> {
                Text(
                    text = "Bloom estimates cover ${calendarState.forecastedTreeCount} of ${calendarState.activeTreeCount} active ${"plant".pluralize(calendarState.activeTreeCount)}.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        DashboardMonthGrid(
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            items = calendarState.items,
            onSelectDay = onSelectDay
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap a day to open that date in a modal. Month sections below stay focused on ${visibleMonth.format(dashboardMonthFormatter)}.",
            style = MaterialTheme.typography.bodySmall
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "This month",
                style = MaterialTheme.typography.titleMedium
            )
            DashboardMonthSectionCard(
                section = DashboardMonthSection.BLOOM_NOW,
                count = monthSectionState.bloomNow.size,
                subtitle = "Predicted bloom windows in ${visibleMonth.format(dashboardMonthFormatter)}",
                onClick = { onOpenMonthSection(DashboardMonthSection.BLOOM_NOW) }
            )
            DashboardMonthSectionCard(
                section = DashboardMonthSection.FRUIT_LIKELY,
                count = monthSectionState.fruitLikely.size,
                subtitle = "Month-matched harvest activity from logged history",
                onClick = { onOpenMonthSection(DashboardMonthSection.FRUIT_LIKELY) }
            )
            DashboardMonthSectionCard(
                section = DashboardMonthSection.REPEAT_BLOOMERS,
                count = monthSectionState.repeatBloomers.size,
                subtitle = "Repeat-bearing plants tracked outside a tight month window",
                onClick = { onOpenMonthSection(DashboardMonthSection.REPEAT_BLOOMERS) }
            )
            DashboardMonthSectionCard(
                section = DashboardMonthSection.RECOVERY_WATCH,
                count = monthSectionState.recoveryWatch.size,
                subtitle = "Tree-linked reminders due in ${visibleMonth.format(dashboardMonthFormatter)}",
                onClick = { onOpenMonthSection(DashboardMonthSection.RECOVERY_WATCH) }
            )
        }
    }
}

@Composable
private fun DashboardMonthGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    items: List<DashboardCalendarItem>,
    onSelectDay: (LocalDate) -> Unit
) {
    val firstDayOffset = visibleMonth.atDay(1).dayOfWeek.value % 7
    val totalDays = visibleMonth.lengthOfMonth()
    val cells = buildList<LocalDate?> {
        repeat(firstDayOffset) { add(null) }
        repeat(totalDays) { add(visibleMonth.atDay(it + 1)) }
        while (size % DayOfWeek.values().size != 0) add(null)
    }
    val weekdayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        cells.chunked(DayOfWeek.values().size).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        DashboardDayCell(
                            modifier = Modifier.weight(1f),
                            day = day,
                            summary = items.summaryFor(day),
                            selected = day == selectedDate,
                            today = day == OrchardTime.today(),
                            onClick = { onSelectDay(day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardDayCell(
    modifier: Modifier = Modifier,
    day: LocalDate,
    summary: DashboardDaySummary,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        today -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        today -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    OutlinedCard(
        modifier = modifier
            .height(94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected || today) FontWeight.SemiBold else FontWeight.Normal
            )
            if (summary.reminderCount > 0) {
                Text(
                    text = "${summary.reminderCount} ${"task".pluralize(summary.reminderCount)}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (summary.logCount > 0) {
                Text(
                    text = "${summary.logCount} ${"log".pluralize(summary.logCount)}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (summary.bloomCount > 0) {
                Text(
                    text = "${summary.bloomCount} ${"bloom".pluralize(summary.bloomCount)}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

private fun buildDashboardMonthSectionState(
    visibleMonth: YearMonth,
    calendarState: DashboardCalendarState,
    reminders: List<ReminderListItem>,
    history: List<HistoryEntryModel>
): DashboardMonthSectionState {
    val bloomNow = calendarState.items
        .filter { it.kind == DashboardCalendarKind.BLOOM_FORECAST }
        .map { item ->
            DashboardMonthSectionItem(
                id = item.id,
                title = item.title,
                subtitle = item.subtitle,
                detail = listOf(
                    "${item.startDate.format(dashboardCompactDateFormatter)} - ${item.endDate.format(dashboardCompactDateFormatter)}",
                    item.detail.takeIf(String::isNotBlank)
                ).joinToString(" - "),
                treeId = item.treeId
            )
        }
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
    val everbearingPlants = BloomForecastEngine.everbearingPlants(activeTrees.map(TreeListItem::tree))
    val bloomItems = bloomWindows.map(PredictedBloomWindow::toCalendarItem)
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
    val allItems = (bloomItems + reminderItems + historyItems).sortedWith(
        compareBy<DashboardCalendarItem>({ it.startDate }, { it.kind.priority }, { it.title.lowercase() })
    )
    return DashboardCalendarState(
        items = allItems,
        activeTreeCount = activeTrees.size,
        forecastedTreeCount = bloomWindows.map(PredictedBloomWindow::treeId).distinct().size,
        everbearingPlants = everbearingPlants
    )
}

private fun PredictedBloomWindow.toCalendarItem(): DashboardCalendarItem = DashboardCalendarItem(
    id = "bloom:$treeId:${startDate}",
    kind = DashboardCalendarKind.BLOOM_FORECAST,
    title = treeLabel,
    subtitle = speciesLabel,
    detail = buildString {
        if (confidence == ForecastConfidence.LOW) {
            append("Likely ${startDate.format(dashboardCompactDateFormatter)} - ${endDate.format(dashboardCompactDateFormatter)}")
        } else {
            append(phase.dashboardDetailLabel())
        }
        append(" - ")
        append(sourceLabel)
        append(" - ")
        append(confidenceLabel)
    },
    startDate = startDate,
    endDate = endDate,
    treeId = treeId
)

private fun BloomPhase.dashboardDetailLabel(): String = when (this) {
    BloomPhase.MID -> "Peak bloom estimate"
    else -> "${label} bloom estimate"
}

private fun DashboardCalendarItem.supportingLine(): String = buildString {
    append(kind.label)
    subtitle.takeUnless { it.isRedundantWith(title) }?.takeIf(String::isNotBlank)?.let {
        append(" - ")
        append(it)
    }
    if (detail.isNotBlank()) {
        append(" - ")
        append(detail)
    }
}

private fun String.isRedundantWith(other: String): Boolean {
    val words = normalizedWords()
    val otherWords = other.normalizedWords()
    return words.isNotEmpty() && otherWords.containsAll(words)
}

private fun String.normalizedWords(): Set<String> =
    lowercase()
        .split(Regex("[^a-z0-9]+"))
        .filter(String::isNotBlank)
        .toSet()

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
