package com.dillon.orcharddex.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import com.dillon.orcharddex.OrchardDexApp
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.ui.navigation.BottomDestination
import com.dillon.orcharddex.ui.navigation.OrchardRoutes
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.screens.CatalogScreen
import com.dillon.orcharddex.ui.screens.DashboardScreen
import com.dillon.orcharddex.ui.screens.DexScreen
import com.dillon.orcharddex.ui.screens.EventFormScreen
import com.dillon.orcharddex.ui.screens.HarvestFormScreen
import com.dillon.orcharddex.ui.screens.HistoryDetailScreen
import com.dillon.orcharddex.ui.screens.HistoryScreen
import com.dillon.orcharddex.ui.screens.PrivacyScreen
import com.dillon.orcharddex.ui.screens.ReminderFormScreen
import com.dillon.orcharddex.ui.screens.SettingsScreen
import com.dillon.orcharddex.ui.screens.TasksScreen
import com.dillon.orcharddex.ui.screens.TreeDetailScreen
import com.dillon.orcharddex.ui.screens.TreeFormScreen
import com.dillon.orcharddex.ui.viewmodel.OrchardViewModelProvider
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel
import androidx.compose.material3.surfaceColorAtElevation

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
    val zoneOptions = remember { listOf("Not set") + BloomForecastEngine.supportedZoneLabels() }
    var setupUsdaZone by rememberSaveable(settings.onboardingComplete, settings.usdaZone) {
        mutableStateOf(settings.usdaZone.takeIf(String::isNotBlank)?.let(BloomForecastEngine::zoneLabelForCode) ?: "Not set")
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
            title = { Text("Set up OrcharDex") },
            text = {
                androidx.compose.foundation.layout.Column {
                    OutlinedTextField(
                        value = setupOrchardName,
                        onValueChange = { setupOrchardName = it },
                        label = { Text("Orchard name") }
                    )
                    SelectionField(
                        label = "USDA zone",
                        value = setupUsdaZone,
                        options = zoneOptions,
                        onSelected = { setupUsdaZone = it }
                    )
                    Text("USDA zone drives bloom timing across the app. You can update it later in Settings.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.completeOnboarding(
                            setupOrchardName,
                            if (setupUsdaZone == "Not set") "" else BloomForecastEngine.zoneCodeFromLabel(setupUsdaZone)
                        )
                    },
                    enabled = setupOrchardName.isNotBlank()
                ) {
                    Text("Save setup")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val isRootDestination = bottomDestinations.any { currentRoute.startsWith(it.route) }
            val isDarkPalette = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val topBarSurface = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            val headerWash = if (isDarkPalette) {
                listOf(
                    Color(0xFF2D2147).copy(alpha = 0.38f),
                    Color.Transparent,
                    Color(0xFF5C471E).copy(alpha = 0.30f)
                )
            } else {
                listOf(
                    Color(0xFFD6E7FF).copy(alpha = 0.86f),
                    Color.Transparent,
                    Color(0xFFFFE3A9).copy(alpha = 0.76f)
                )
            }
            val headerAccent = if (isDarkPalette) {
                listOf(
                    Color(0xFF84A37E).copy(alpha = 0.74f),
                    Color(0xFF8B78E6).copy(alpha = 0.62f),
                    Color(0xFFF0B54B).copy(alpha = 0.82f)
                )
            } else {
                listOf(
                    Color(0xFF7B984B).copy(alpha = 0.76f),
                    Color(0xFF7390C8).copy(alpha = 0.62f),
                    Color(0xFFC4892B).copy(alpha = 0.80f)
                )
            }
            val headerSheen = if (isDarkPalette) {
                listOf(
                    Color.White.copy(alpha = 0.035f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.08f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color.Transparent,
                    Color(0xFFC4892B).copy(alpha = 0.06f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarSurface)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = headerWash
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = headerSheen
                        )
                    )
            ) {
                TopAppBar(
                    title = { Text(currentRoute.titleForRoute(settings.orchardName)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        if (!isRootDestination) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = headerAccent
                            )
                        )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 100)) },
            popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 100)) }
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
                HistoryScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onEntryClick = { kind, entryId ->
                        navController.navigate(OrchardRoutes.historyDetail(kind, entryId))
                    },
                    onAddEvent = { navController.navigate(OrchardRoutes.eventForm()) },
                    onAddHarvest = { navController.navigate(OrchardRoutes.harvestForm()) }
                )
            }
            composable(BottomDestination.Dex.route) {
                DexScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    usdaZone = settings.usdaZone,
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
                    onPrivacy = { navController.navigate(OrchardRoutes.PRIVACY) },
                    onOpenCatalog = { navController.navigate(OrchardRoutes.CATALOG) }
                )
            }
            composable(
                route = OrchardRoutes.HISTORY_DETAIL,
                arguments = listOf(
                    navArgument(OrchardRoutes.HISTORY_KIND_ARG) { type = NavType.StringType },
                    navArgument(OrchardRoutes.HISTORY_ENTRY_ID_ARG) { type = NavType.StringType }
                )
            ) {
                HistoryDetailScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onBack = { navController.popBackStack() },
                    onOpenTree = { treeId -> navController.navigate(OrchardRoutes.treeDetail(treeId)) }
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
            composable(OrchardRoutes.CATALOG) {
                CatalogScreen(usdaZone = settings.usdaZone)
            }
        }
    }
}

private fun String.titleForRoute(orchardName: String): String = when {
    startsWith(BottomDestination.Dashboard.route) -> orchardName.ifBlank { "Dashboard" }
    startsWith(BottomDestination.Trees.route) -> "History"
    startsWith(BottomDestination.Dex.route) -> "Plants"
    startsWith(BottomDestination.Tasks.route) -> "Tasks"
    startsWith(BottomDestination.Settings.route) -> "Settings"
    startsWith("historyDetail") -> "Log details"
    startsWith("treeDetail") -> "Tree details"
    startsWith("treeForm") -> "Tree"
    startsWith("eventForm") -> "Add event"
    startsWith("harvestForm") -> "Add harvest"
    startsWith("reminderForm") -> "Reminder"
    startsWith("privacy") -> "Privacy"
    startsWith("catalog") -> "Plant Catalog"
    else -> "OrcharDex"
}
