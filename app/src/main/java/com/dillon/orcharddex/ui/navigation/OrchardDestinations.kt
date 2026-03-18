package com.dillon.orcharddex.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.graphics.vector.ImageVector
import com.dillon.orcharddex.data.model.ActivityKind

sealed class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomDestination("dashboard", "Dashboard", Icons.Outlined.Dashboard)
    data object Trees : BottomDestination("trees", "History", Icons.Outlined.Yard)
    data object Dex : BottomDestination("dex", "Dex", Icons.Outlined.CollectionsBookmark)
    data object Tasks : BottomDestination("tasks", "Tasks", Icons.Outlined.TaskAlt)
    data object Settings : BottomDestination("settings", "Settings", Icons.Outlined.Settings)
}

object OrchardRoutes {
    const val TREE_ID_ARG = "treeId"
    const val REMINDER_ID_ARG = "reminderId"
    const val HISTORY_KIND_ARG = "kind"
    const val HISTORY_ENTRY_ID_ARG = "entryId"

    const val TREE_DETAIL = "treeDetail/{$TREE_ID_ARG}"
    const val TREE_FORM = "treeForm?$TREE_ID_ARG={$TREE_ID_ARG}"
    const val EVENT_FORM = "eventForm?$TREE_ID_ARG={$TREE_ID_ARG}"
    const val HARVEST_FORM = "harvestForm?$TREE_ID_ARG={$TREE_ID_ARG}"
    const val REMINDER_FORM = "reminderForm?$TREE_ID_ARG={$TREE_ID_ARG}&$REMINDER_ID_ARG={$REMINDER_ID_ARG}"
    const val HISTORY_DETAIL = "historyDetail/{$HISTORY_KIND_ARG}/{$HISTORY_ENTRY_ID_ARG}"
    const val PRIVACY = "privacy"

    fun treeDetail(treeId: String) = "treeDetail/$treeId"
    fun treeForm(treeId: String? = null) = "treeForm?$TREE_ID_ARG=${treeId.orEmpty()}"
    fun eventForm(treeId: String? = null) = "eventForm?$TREE_ID_ARG=${treeId.orEmpty()}"
    fun harvestForm(treeId: String? = null) = "harvestForm?$TREE_ID_ARG=${treeId.orEmpty()}"
    fun reminderForm(treeId: String? = null, reminderId: String? = null) =
        "reminderForm?$TREE_ID_ARG=${treeId.orEmpty()}&$REMINDER_ID_ARG=${reminderId.orEmpty()}"
    fun historyDetail(kind: ActivityKind, entryId: String) =
        "historyDetail/${kind.name.lowercase()}/$entryId"
}
