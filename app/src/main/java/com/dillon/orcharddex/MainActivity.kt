package com.dillon.orcharddex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.OrchardDexRoot
import com.dillon.orcharddex.ui.theme.OrchardDexTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val keepSplashVisible = mutableStateOf(true)
        installSplashScreen().setKeepOnScreenCondition { keepSplashVisible.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as OrchardDexApp
        setContent {
            val settings by app.container.settingsRepository.settings
                .map<AppSettings, AppSettings?> { it }
                .collectAsStateWithLifecycle(
                    initialValue = null
                )
            LaunchedEffect(settings) {
                settings?.let { loadedSettings ->
                    app.container.repository.ensureGrowingLocations(loadedSettings)
                    keepSplashVisible.value = false
                }
            }
            settings?.let { loadedSettings ->
                OrchardTime.updateTimezoneId(loadedSettings.timezoneId)
                OrchardDexTheme(
                    themeMode = loadedSettings.themeMode,
                    dynamicColor = loadedSettings.dynamicColor
                ) {
                    OrchardDexRoot(
                        app = app,
                        settings = loadedSettings
                    )
                }
            }
        }
    }
}
