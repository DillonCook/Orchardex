package com.dillon.orcharddex.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dillon.orcharddex.OrchardDexApp
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.ui.navigation.BottomDestination
import com.dillon.orcharddex.ui.navigation.OrchardRoutes
import com.dillon.orcharddex.ui.components.SelectionField
import com.dillon.orcharddex.ui.components.TimezonePickerField
import com.dillon.orcharddex.ui.screens.CatalogScreen
import com.dillon.orcharddex.ui.screens.DashboardScreen
import com.dillon.orcharddex.ui.screens.DexScreen
import com.dillon.orcharddex.ui.screens.HistoryDetailScreen
import com.dillon.orcharddex.ui.screens.HistoryScreen
import com.dillon.orcharddex.ui.screens.LogFormScreen
import com.dillon.orcharddex.ui.screens.PrivacyScreen
import com.dillon.orcharddex.ui.screens.ReminderFormScreen
import com.dillon.orcharddex.ui.screens.SettingsScreen
import com.dillon.orcharddex.ui.screens.TasksScreen
import com.dillon.orcharddex.ui.screens.TreeDetailScreen
import com.dillon.orcharddex.ui.screens.TreeFormScreen
import com.dillon.orcharddex.ui.viewmodel.OrchardViewModelProvider
import com.dillon.orcharddex.ui.viewmodel.SettingsViewModel
import androidx.compose.material3.surfaceColorAtElevation

private data class LaunchWalkthroughPage(
    val title: String,
    val message: String,
    val highlightedTabs: Set<String>,
    val actionHint: String? = null,
    val climateHints: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrchardDexRoot(
    app: OrchardDexApp,
    settings: AppSettings
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = OrchardViewModelProvider.Factory)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    var setupOrchardName by rememberSaveable(settings.onboardingComplete) {
        mutableStateOf(settings.orchardName)
    }
    val zoneOptions = remember { listOf("Not set") + BloomForecastEngine.supportedZoneLabels() }
    var setupUsdaZone by rememberSaveable(settings.onboardingComplete, settings.usdaZone) {
        mutableStateOf(settings.usdaZone.takeIf(String::isNotBlank)?.let(BloomForecastEngine::zoneLabelForCode) ?: "Not set")
    }
    var setupTimezoneId by rememberSaveable(settings.onboardingComplete, settings.timezoneId) {
        mutableStateOf(settings.timezoneId)
    }
    val hemisphereOptions = remember { Hemisphere.entries.map(Hemisphere::label) }
    var setupHemisphere by rememberSaveable(settings.onboardingComplete, settings.hemisphere) {
        mutableStateOf(settings.hemisphere.label)
    }
    var walkthroughPageIndex by rememberSaveable(
        settings.onboardingComplete,
        settings.walkthroughComplete
    ) {
        mutableStateOf(0)
    }
    val walkthroughPages = remember {
        listOf(
            LaunchWalkthroughPage(
                title = "Welcome to Dashboard",
                message = "Use Dashboard to see your agenda, bloom timing, and quick orchard stats in one place.",
                highlightedTabs = setOf(BottomDestination.Dashboard.route)
            ),
            LaunchWalkthroughPage(
                title = "Add your first tree",
                message = "Open Orchard, then tap Add plant to create your first tree with species, cultivar, and location details.",
                highlightedTabs = setOf(BottomDestination.Dex.route),
                actionHint = "Add plant"
            ),
            LaunchWalkthroughPage(
                title = "Track work and harvests",
                message = "Use History for events and harvests, and Tasks for reminders you want to stay on top of.",
                highlightedTabs = setOf(
                    BottomDestination.Trees.route,
                    BottomDestination.Tasks.route
                )
            ),
            LaunchWalkthroughPage(
                title = "Finish climate settings",
                message = "Open Settings > Default orchard to fill coordinates, elevation, and chill hours. Those details make bloom timing more accurate for your location.",
                highlightedTabs = setOf(BottomDestination.Settings.route),
                climateHints = listOf("Coordinates", "Elevation", "Chill hours")
            )
        )
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

    LaunchedEffect(currentRoute) {
        app.container.diagnosticsStore.recordBreadcrumb(
            category = "navigation",
            message = "route_changed",
            attributes = mapOf("route" to currentRoute.ifBlank { "unknown" })
        )
    }

    fun navigateToRoot(destination: BottomDestination) {
        navController.navigate(destination.route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    if (!settings.onboardingComplete) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Set up your orchard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Name your orchard to get started.")
                    OutlinedTextField(
                        value = setupOrchardName,
                        onValueChange = { setupOrchardName = it },
                        label = { Text("Orchard Name") },
                        modifier = Modifier.testTag("setup_orchard_name")
                    )
                    TimezonePickerField(
                        label = "Timezone",
                        value = setupTimezoneId,
                        onSelected = { setupTimezoneId = it },
                        supportingText = "This sets local dates and reminder timing."
                    )
                    SelectionField(
                        label = "Hemisphere",
                        value = setupHemisphere,
                        options = hemisphereOptions,
                        onSelected = { setupHemisphere = it }
                    )
                    SelectionField(
                        label = "USDA zone",
                        value = setupUsdaZone,
                        options = zoneOptions,
                        onSelected = { setupUsdaZone = it }
                    )
                    Text("You can finish your orchard location later in Settings under Default orchard.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.completeOnboarding(
                            setupOrchardName,
                            ForecastLocationProfile(
                                name = setupOrchardName,
                                countryCode = "",
                                timezoneId = setupTimezoneId,
                                hemisphere = Hemisphere.entries.first { it.label == setupHemisphere },
                                usdaZoneCode = if (setupUsdaZone == "Not set") null else BloomForecastEngine.zoneCodeFromLabel(setupUsdaZone)
                            )
                        )
                    },
                    enabled = setupOrchardName.isNotBlank() && setupTimezoneId.isNotBlank(),
                    modifier = Modifier.testTag("setup_save_orchard")
                ) {
                    Text("Save orchard")
                }
            }
        )
    }

    if (settings.onboardingComplete && !settings.walkthroughComplete) {
        val page = walkthroughPages[walkthroughPageIndex.coerceIn(0, walkthroughPages.lastIndex)]
        AlertDialog(
            onDismissRequest = {},
            title = { Text(page.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Step ${walkthroughPageIndex + 1} of ${walkthroughPages.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(page.message)
                    WalkthroughTabsPreview(
                        destinations = bottomDestinations,
                        highlightedRoutes = page.highlightedTabs
                    )
                    page.actionHint?.let { actionLabel ->
                        WalkthroughActionPreview(actionLabel)
                    }
                    if (page.climateHints.isNotEmpty()) {
                        WalkthroughClimatePreview(page.climateHints)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (walkthroughPageIndex == walkthroughPages.lastIndex) {
                            settingsViewModel.completeWalkthrough()
                        } else {
                            walkthroughPageIndex += 1
                        }
                    }
                ) {
                    Text(if (walkthroughPageIndex == walkthroughPages.lastIndex) "Done" else "Next")
                }
            },
            dismissButton = {
                Row {
                    if (walkthroughPageIndex > 0) {
                        TextButton(onClick = { walkthroughPageIndex -= 1 }) {
                            Text("Back")
                        }
                    }
                    TextButton(onClick = settingsViewModel::completeWalkthrough) {
                        Text("Skip")
                    }
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
                            onClick = { navigateToRoot(destination) },
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
                    onAddEvent = { navController.navigate(OrchardRoutes.logForm(kind = ActivityKind.EVENT)) },
                    onAddHarvest = { navController.navigate(OrchardRoutes.logForm(kind = ActivityKind.HARVEST)) },
                    onAddReminder = { navController.navigate(OrchardRoutes.reminderForm()) },
                    onOpenOrchard = { navigateToRoot(BottomDestination.Dex) },
                    onOpenSettings = { navigateToRoot(BottomDestination.Settings) },
                    onViewTree = { treeId -> navController.navigate(OrchardRoutes.treeDetail(treeId)) }
                )
            }
            composable(BottomDestination.Trees.route) {
                HistoryScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    onEntryClick = { kind, entryId ->
                        navController.navigate(OrchardRoutes.historyDetail(kind, entryId))
                    },
                    onAddLog = { navController.navigate(OrchardRoutes.logForm()) },
                    onAddEvent = { navController.navigate(OrchardRoutes.logForm(kind = ActivityKind.EVENT)) },
                    onAddHarvest = { navController.navigate(OrchardRoutes.logForm(kind = ActivityKind.HARVEST)) },
                    onAddPlant = { navController.navigate(OrchardRoutes.treeForm()) }
                )
            }
            composable(BottomDestination.Dex.route) {
                DexScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    settings = settings,
                    onAddTree = { navController.navigate(OrchardRoutes.treeForm()) },
                    onTreeClick = { treeId -> navController.navigate(OrchardRoutes.treeDetail(treeId)) },
                    onQuickLog = { treeId -> navController.navigate(OrchardRoutes.logForm(treeId)) }
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
                    settings = settings,
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
                    settings = settings,
                    onBack = { navController.popBackStack() },
                    onEditTree = { treeId -> navController.navigate(OrchardRoutes.treeForm(treeId)) },
                    onPropagate = { treeId, propagationMethod ->
                        navController.navigate(
                            OrchardRoutes.treeForm(
                                parentTreeId = treeId,
                                propagationMethod = propagationMethod
                            )
                        )
                    },
                    onAddLog = { treeId -> navController.navigate(OrchardRoutes.logForm(treeId)) },
                    onAddReminder = { treeId -> navController.navigate(OrchardRoutes.reminderForm(treeId = treeId)) },
                    onOpenLog = { kind, entryId -> navController.navigate(OrchardRoutes.historyDetail(kind, entryId)) }
                )
            }
            composable(
                route = OrchardRoutes.TREE_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(OrchardRoutes.PARENT_TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(OrchardRoutes.PROPAGATION_METHOD_ARG) {
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
                route = OrchardRoutes.LOG_FORM,
                arguments = listOf(
                    navArgument(OrchardRoutes.TREE_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(OrchardRoutes.LOG_KIND_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                LogFormScreen(
                    viewModel = viewModel(factory = OrchardViewModelProvider.Factory),
                    settings = settings,
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
                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
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

@Composable
private fun WalkthroughTabsPreview(
    destinations: List<BottomDestination>,
    highlightedRoutes: Set<String>
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        destinations.forEach { destination ->
            val highlighted = destination.route in highlightedRoutes
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
                contentColor = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun WalkthroughActionPreview(
    actionLabel: String
) {
    OutlinedCard {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "When you are ready to start:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = {}, enabled = false) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WalkthroughClimatePreview(
    hints: List<String>
) {
    OutlinedCard {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Default orchard details to finish:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hints.forEach { hint ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = hint,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

private fun String.titleForRoute(orchardName: String): String = when {
    startsWith(BottomDestination.Dashboard.route) -> orchardName.ifBlank { "Dashboard" }
    startsWith(BottomDestination.Trees.route) -> "History"
    startsWith(BottomDestination.Dex.route) -> "Orchard"
    startsWith(BottomDestination.Tasks.route) -> "Tasks"
    startsWith(BottomDestination.Settings.route) -> "Settings"
    startsWith("historyDetail") -> "Log details"
    startsWith("treeDetail") -> "Tree details"
    startsWith("treeForm") -> "Tree"
    startsWith("logForm") -> "New activity"
    startsWith("eventForm") -> "Add event"
    startsWith("harvestForm") -> "Add harvest"
    startsWith("reminderForm") -> "Reminder"
    startsWith("privacy") -> "Privacy"
    startsWith("catalog") -> "Plant Catalog"
    else -> "OrcharDex"
}
