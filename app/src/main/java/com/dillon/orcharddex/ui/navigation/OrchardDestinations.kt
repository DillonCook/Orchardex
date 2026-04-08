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
    data object Dex : BottomDestination("dex", "Orchard", Icons.Outlined.CollectionsBookmark)
    data object Tasks : BottomDestination("tasks", "Tasks", Icons.Outlined.TaskAlt)
    data object Settings : BottomDestination("settings", "Settings", Icons.Outlined.Settings)
}

object OrchardRoutes {
    const val TREE_ID_ARG = "treeId"
    const val PARENT_TREE_ID_ARG = "parentTreeId"
    const val PROPAGATION_METHOD_ARG = "propagationMethod"
    const val LOG_KIND_ARG = "logKind"
    const val REMINDER_ID_ARG = "reminderId"
    const val HISTORY_KIND_ARG = "kind"
    const val HISTORY_ENTRY_ID_ARG = "entryId"

    const val TREE_DETAIL = "treeDetail/{$TREE_ID_ARG}"
    const val TREE_FORM =
        "treeForm?$TREE_ID_ARG={$TREE_ID_ARG}&$PARENT_TREE_ID_ARG={$PARENT_TREE_ID_ARG}&$PROPAGATION_METHOD_ARG={$PROPAGATION_METHOD_ARG}"
    const val LOG_FORM = "logForm?$TREE_ID_ARG={$TREE_ID_ARG}&$LOG_KIND_ARG={$LOG_KIND_ARG}"
    const val EVENT_FORM = "eventForm?$TREE_ID_ARG={$TREE_ID_ARG}"
    const val HARVEST_FORM = "harvestForm?$TREE_ID_ARG={$TREE_ID_ARG}"
    const val REMINDER_FORM = "reminderForm?$TREE_ID_ARG={$TREE_ID_ARG}&$REMINDER_ID_ARG={$REMINDER_ID_ARG}"
    const val HISTORY_DETAIL = "historyDetail/{$HISTORY_KIND_ARG}/{$HISTORY_ENTRY_ID_ARG}"
    const val PRIVACY = "privacy"
    const val CATALOG = "catalog"

    fun treeDetail(treeId: String) = "treeDetail/$treeId"
    fun treeForm(
        treeId: String? = null,
        parentTreeId: String? = null,
        propagationMethod: String? = null
    ) = buildString {
        append("treeForm?$TREE_ID_ARG=${treeId.orEmpty()}")
        append("&$PARENT_TREE_ID_ARG=${parentTreeId.orEmpty()}")
        append("&$PROPAGATION_METHOD_ARG=${propagationMethod.orEmpty()}")
    }
    fun logForm(treeId: String? = null, kind: ActivityKind? = null) =
        "logForm?$TREE_ID_ARG=${treeId.orEmpty()}&$LOG_KIND_ARG=${kind?.name?.lowercase().orEmpty()}"
    fun eventForm(treeId: String? = null) = "eventForm?$TREE_ID_ARG=${treeId.orEmpty()}"
    fun harvestForm(treeId: String? = null) = "harvestForm?$TREE_ID_ARG=${treeId.orEmpty()}"
    fun reminderForm(treeId: String? = null, reminderId: String? = null) =
        "reminderForm?$TREE_ID_ARG=${treeId.orEmpty()}&$REMINDER_ID_ARG=${reminderId.orEmpty()}"
    fun historyDetail(kind: ActivityKind, entryId: String) =
        "historyDetail/${kind.name.lowercase()}/$entryId"
}
