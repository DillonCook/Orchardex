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
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.EverbearingPlant
import com.dillon.orcharddex.data.phenology.PredictedBloomWindow
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.epochToLocalDate
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

private val dashboardMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dashboardDayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTree: () -> Unit,
    onAddEvent: () -> Unit,
    onAddHarvest: () -> Unit,
    onAddReminder: () -> Unit,
    onViewTree: (String) -> Unit
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val trees by viewModel.trees.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedStat by rememberSaveable { mutableStateOf<DashboardStat?>(null) }
    var showEverbearingDialog by rememberSaveable { mutableStateOf(false) }
    var visibleMonthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedDateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val visibleMonth = remember(visibleMonthText) { YearMonth.parse(visibleMonthText) }
    val selectedDate = remember(selectedDateText) { LocalDate.parse(selectedDateText) }
    val calendarState = remember(trees, reminders, history, settings.usdaZone, visibleMonth) {
        buildDashboardCalendarState(
            settings = settings,
            visibleMonth = visibleMonth,
            reminders = reminders,
            history = history,
            activeTrees = trees.filter { it.status == TreeStatus.ACTIVE }
        )
    }
    val itemsForSelectedDay = remember(calendarState.items, selectedDate) {
        calendarState.items
            .filter { it.occursOn(selectedDate) }
            .sortedWith(compareBy(DashboardCalendarItem::startDate, DashboardCalendarItem::kind, DashboardCalendarItem::title))
    }

    fun jumpMonth(offset: Long) {
        val updatedMonth = visibleMonth.plusMonths(offset)
        visibleMonthText = updatedMonth.toString()
        selectedDateText = defaultSelectedDateForMonth(updatedMonth).toString()
    }

    selectedStat?.let { stat ->
        DashboardDetailDialog(
            stat = stat,
            dashboard = dashboard,
            onDismiss = { selectedStat = null },
            onViewTree = onViewTree
        )
    }
    if (showEverbearingDialog && calendarState.everbearingPlants.isNotEmpty()) {
        EverbearingFruitDialog(
            plants = calendarState.everbearingPlants,
            onDismiss = { showEverbearingDialog = false },
            onViewTree = onViewTree
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dashboard.totalTreeCount == 0) {
            item {
                EmptyStateCard(
                    title = "Start with your first tree",
                    message = "Add a plant, then use reminders, events, harvests, and bloom forecasts to run the orchard from one place."
                )
            }
        }
        item {
            SectionCard("Orchard pulse") {
                Text(
                    text = if (dashboard.totalTreeCount == 0) {
                        "The dashboard becomes your quick review board once plants and reminders are in place."
                    } else {
                        "Scan the collection, spot due work, and jump into the right plant or reminder list quickly."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Trees", dashboard.totalTreeCount.toString()) {
                        selectedStat = DashboardStat.TREES
                    }
                    StatCard("Cultivars", dashboard.cultivarCount.toString()) {
                        selectedStat = DashboardStat.CULTIVARS
                    }
                    StatCard("Species", dashboard.speciesCount.toString()) {
                        selectedStat = DashboardStat.SPECIES
                    }
                    StatCard("Wishlist", dashboard.wishlistCount.toString()) {
                        selectedStat = DashboardStat.WISHLIST
                    }
                    StatCard("Awaiting first fruit", dashboard.awaitingFirstFruitCount.toString()) {
                        selectedStat = DashboardStat.AWAITING_FIRST_FRUIT
                    }
                    StatCard("Due in 7 days", dashboard.upcoming7Count.toString()) {
                        selectedStat = DashboardStat.DUE_SOON
                    }
                }
                Text(
                    text = "Tap any count to inspect the underlying list.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            SectionCard("Quick actions") {
                Text(
                    text = "Capture orchard work from the dashboard without hunting through other screens.",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                itemsForSelectedDay = itemsForSelectedDay,
                onPreviousMonth = { jumpMonth(-1) },
                onNextMonth = { jumpMonth(1) },
                onShowEverbearing = { showEverbearingDialog = true },
                onSelectDay = { selectedDateText = it.toString() },
                onViewTree = onViewTree
            )
        }
    }
}

@Composable
private fun DashboardCalendarSection(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    settings: AppSettings,
    calendarState: DashboardCalendarState,
    itemsForSelectedDay: List<DashboardCalendarItem>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowEverbearing: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onViewTree: (String) -> Unit
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
            settings.usdaZone.isBlank() -> {
                Text("Set your USDA zone in Settings to tune bloom timing.", style = MaterialTheme.typography.bodySmall)
            }
            calendarState.forecastedTreeCount in 1 until calendarState.activeTreeCount -> {
                Text(
                    text = "Bloom estimates cover ${calendarState.forecastedTreeCount} of ${calendarState.activeTreeCount} active ${"plant".pluralize(calendarState.activeTreeCount)}.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (calendarState.everbearingPlants.isNotEmpty()) {
            OutlinedButton(onClick = onShowEverbearing) {
                Text(
                    "${calendarState.everbearingPlants.size} everbearing ${"fruit".pluralize(calendarState.everbearingPlants.size)}"
                )
            }
            Text(
                text = "Open the badge to see the tracked fruits that bloom opportunistically instead of on a fixed monthly window.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        DashboardMonthGrid(
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            items = calendarState.items,
            onSelectDay = onSelectDay
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = selectedDate.format(dashboardDayFormatter),
                style = MaterialTheme.typography.titleMedium
            )
            if (itemsForSelectedDay.isEmpty()) {
                Text(
                    text = "No blooms, reminders, or history recorded for this day.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                itemsForSelectedDay.forEach { item ->
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
                            today = day == LocalDate.now(),
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
            text = buildString {
                append(item.kind.label)
                if (item.subtitle.isNotBlank()) {
                    append(" - ")
                    append(item.subtitle)
                }
                if (item.detail.isNotBlank()) {
                    append(" - ")
                    append(item.detail)
                }
            },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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

private fun buildDashboardCalendarState(
    settings: AppSettings,
    visibleMonth: YearMonth,
    reminders: List<ReminderListItem>,
    history: List<HistoryEntryModel>,
    activeTrees: List<com.dillon.orcharddex.data.local.TreeEntity>
): DashboardCalendarState {
    val bloomWindows = BloomForecastEngine.predictMonth(
        trees = activeTrees,
        yearMonth = visibleMonth,
        zoneCode = settings.usdaZone
    )
    val everbearingPlants = BloomForecastEngine.everbearingPlants(activeTrees)
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
    detail = "${phase.label} estimate",
    startDate = startDate,
    endDate = endDate,
    treeId = treeId
)

private fun List<DashboardCalendarItem>.summaryFor(day: LocalDate): DashboardDaySummary {
    val itemsForDay = filter { it.occursOn(day) }
    return DashboardDaySummary(
        reminderCount = itemsForDay.count { it.kind == DashboardCalendarKind.REMINDER },
        logCount = itemsForDay.count { it.kind == DashboardCalendarKind.EVENT || it.kind == DashboardCalendarKind.HARVEST },
        bloomCount = itemsForDay.count { it.kind == DashboardCalendarKind.BLOOM_FORECAST }
    )
}

private fun defaultSelectedDateForMonth(yearMonth: YearMonth): LocalDate =
    if (yearMonth == YearMonth.now()) LocalDate.now() else yearMonth.atDay(1)

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

private fun DashboardDetailItem.supportingLine(): String? = buildString {
    if (subtitle.isNotBlank()) {
        append(subtitle)
    }
    date?.let {
        if (isNotBlank()) append(" - ")
        append(it.toDateLabel())
    }
}.takeIf(String::isNotBlank)
