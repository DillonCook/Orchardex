package com.dillon.orcharddex.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dillon.orcharddex.OrchardDexApp
import com.dillon.orcharddex.ui.navigation.BottomDestination
import com.dillon.orcharddex.ui.navigation.OrchardRoutes
import com.dillon.orcharddex.ui.screens.DashboardScreen
import com.dillon.orcharddex.ui.screens.DexScreen
import com.dillon.orcharddex.ui.screens.EventFormScreen
import com.dillon.orcharddex.ui.screens.HarvestFormScreen
import com.dillon.orcharddex.ui.screens.PrivacyScreen
import com.dillon.orcharddex.ui.screens.ReminderFormScreen
import com.dillon.orcharddex.ui.screens.SettingsScreen
import com.dillon.orcharddex.ui.screens.TasksScreen
import com.dillon.orcharddex.ui.screens.TreeDetailScreen
import com.dillon.orcharddex.ui.screens.TreeFormScreen
import com.dillon.orcharddex.ui.screens.TreesHoldingScreen
import com.dillon.orcharddex.ui.viewmodel.OrchardViewModelProvider
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrchardDexRoot(app: OrchardDexApp) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = OrchardViewModelProvider.Factory)
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    var setupOrchardName by rememberSaveable(settings.onboardingComplete) {
        mutableStateOf(settings.orchardName)
    }
    val bottomDestinations = listOf(
        BottomDestination.Dashboard,
        BottomDestination.Trees,
        BottomDestination.Dex,
        BottomDestination.Tasks,
        BottomDestination.Settings
    )
    val showBottomBar = bottomDestinations.any { currentRoute.startsWith(it.route) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    if (!settings.onboardingComplete) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Set up OrchardDex") },
            text = {
                OutlinedTextField(
                    value = setupOrchardName,
                    onValueChange = { setupOrchardName = it },
                    label = { Text("Orchard name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { settingsViewModel.completeOnboarding(setupOrchardName) },
                    enabled = setupOrchardName.isNotBlank()
                ) {
                    Text("Save setup")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            val isRootDestination = bottomDestinations.any { currentRoute.startsWith(it.route) }
            TopAppBar(
                title = { Text(currentRoute.titleForRoute(settings.orchardName)) },
                navigationIcon = {
                    if (!isRootDestination) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute.startsWith(destination.route),
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomDestination.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onAddTree = { navController.navigate(OrchardRoutes.treeForm()) },
                    onAddEvent = { navController.navigate(OrchardRoutes.eventForm()) },
                    onAddHarvest = { navController.navigate(OrchardRoutes.harvestForm()) },
                    onAddReminder = { navController.navigate(OrchardRoutes.reminderForm()) },
                    onViewTree = { treeId -> navController.navigate(OrchardRoutes.treeDetail(treeId)) }
                )
            }
            composable(BottomDestination.Trees.route) {
                TreesHoldingScreen(
                    onOpenDex = { navController.navigate(BottomDestination.Dex.route) }
                )
            }
            composable(BottomDestination.Dex.route) {
                DexScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onAddTree = { navController.navigate(OrchardRoutes.treeForm()) },
                    onTreeClick = { treeId -> navController.navigate(OrchardRoutes.treeDetail(treeId)) }
                )
            }
            composable(BottomDestination.Tasks.route) {
                TasksScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onAddReminder = { navController.navigate(OrchardRoutes.reminderForm()) },
                    onEditReminder = { reminderId ->
                        navController.navigate(OrchardRoutes.reminderForm(reminderId = reminderId))
                    }
                )
            }
            composable(BottomDestination.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onPrivacy = { navController.navigate(OrchardRoutes.PRIVACY) }
                )
            }
            composable(
                route = OrchardRoutes.TREE_DETAIL,
                arguments = listOf(navArgument(OrchardRoutes.TREE_ID_ARG) { type = NavType.StringType })
            ) {
                TreeDetailScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onBack = { navController.popBackStack() },
                    onEditTree = { treeId -> navController.navigate(OrchardRoutes.treeForm(treeId)) },
                    onAddEvent = { treeId -> navController.navigate(OrchardRoutes.eventForm(treeId)) },
                    onAddHarvest = { treeId -> navController.navigate(OrchardRoutes.harvestForm(treeId)) },
                    onAddReminder = { treeId -> navController.navigate(OrchardRoutes.reminderForm(treeId = treeId)) }
                )
            }
            composable(
                route = OrchardRoutes.TREE_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                TreeFormScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onSaved = { treeId ->
                        navController.popBackStack()
                        navController.navigate(OrchardRoutes.treeDetail(treeId))
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = OrchardRoutes.EVENT_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                EventFormScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onSaved = { treeId ->
                        navController.popBackStack()
                        treeId.takeIf(String::isNotBlank)?.let {
                            navController.navigate(OrchardRoutes.treeDetail(it))
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = OrchardRoutes.HARVEST_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                HarvestFormScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onSaved = { treeId ->
                        navController.popBackStack()
                        treeId.takeIf(String::isNotBlank)?.let {
                            navController.navigate(OrchardRoutes.treeDetail(it))
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = OrchardRoutes.REMINDER_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(OrchardRoutes.REMINDER_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                ReminderFormScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    requestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(OrchardRoutes.PRIVACY) {
                PrivacyScreen()
            }
        }
    }
}

private fun String.titleForRoute(orchardName: String): String = when {
    startsWith(BottomDestination.Dashboard.route) -> orchardName.ifBlank { "Dashboard" }
    startsWith(BottomDestination.Trees.route) -> "Trees"
    startsWith(BottomDestination.Dex.route) -> "Dex"
    startsWith(BottomDestination.Tasks.route) -> "Tasks"
    startsWith(BottomDestination.Settings.route) -> "Settings"
    startsWith("treeDetail") -> "Tree details"
    startsWith("treeForm") -> "Tree"
    startsWith("eventForm") -> "Log event"
    startsWith("harvestForm") -> "Log harvest"
    startsWith("reminderForm") -> "Reminder"
    startsWith("privacy") -> "Privacy"
    else -> "OrchardDex"
}
