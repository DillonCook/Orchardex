package com.dillon.orcharddex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.ui.components.EmptyStateCard
import com.dillon.orcharddex.ui.components.SectionCard
import com.dillon.orcharddex.ui.components.StatCard
import com.dillon.orcharddex.ui.toDateLabel
import com.dillon.orcharddex.ui.viewmodel.DashboardViewModel

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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (dashboard.totalTreeCount == 0) {
                EmptyStateCard(
                    title = "Start with your first tree",
                    message = "Add a plant, then use orchard-wide events and reminders to track the season."
                )
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Trees", dashboard.totalTreeCount.toString())
                StatCard("Active cultivars", dashboard.activeCultivarCount.toString())
                StatCard("Species", dashboard.speciesCount.toString())
                StatCard("Wishlist", dashboard.wishlistCount.toString())
                StatCard("First fruit", dashboard.firstFruitCount.toString())
                StatCard("Due in 7 days", dashboard.upcoming7Count.toString())
            }
        }
        item {
            SectionCard("Quick actions") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onAddTree) { Text("Add plant") }
                    OutlinedButton(onClick = onAddEvent) { Text("Apply event") }
                    OutlinedButton(onClick = onAddHarvest) { Text("Add harvest") }
                    OutlinedButton(onClick = onAddReminder) { Text("Add reminder") }
                }
            }
        }
        item {
            SectionCard("Upcoming work") {
                Text("Next 7 days: ${dashboard.upcoming7Count}")
                Text("Next 30 days: ${dashboard.upcoming30Count}")
            }
        }
        item {
            SectionCard("Recent activity") {
                if (dashboard.recentActivity.isEmpty()) {
                    Text("No activity logged yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dashboard.recentActivity.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = item.treeId != null) {
                                        item.treeId?.let(onViewTree)
                                    }
                            ) {
                                Text(item.title)
                                Text("${item.subtitle} - ${item.date.toDateLabel()}")
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Recent harvests") {
                if (dashboard.recentHarvests.isEmpty()) {
                    Text("No harvests logged yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dashboard.recentHarvests.forEach { harvest ->
                            Text("${harvest.quantityValue} ${harvest.quantityUnit} - ${harvest.harvestDate.toDateLabel()}")
                        }
                    }
                }
            }
        }
    }
}
