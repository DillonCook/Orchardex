package com.dillon.orcharddex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.OrchardDexRoot
import com.dillon.orcharddex.ui.theme.OrchardDexTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as OrchardDexApp
        setContent {
            val settingsFlow = remember(app) {
                app.container.settingsRepository.settings.map<AppSettings, AppSettings?> { it }
            }
            val settings by settingsFlow
                .collectAsStateWithLifecycle(
                    initialValue = null
                )
            LaunchedEffect(settings) {
                settings?.let { loadedSettings ->
                    app.container.repository.ensureGrowingLocations(loadedSettings)
                }
            }
            val loadedSettings = settings
            OrchardDexTheme(
                themeMode = loadedSettings?.themeMode ?: AppThemeMode.SYSTEM,
                dynamicColor = loadedSettings?.dynamicColor ?: true
            ) {
                if (loadedSettings != null) {
                    OrchardTime.updateTimezoneId(loadedSettings.timezoneId)
                    OrchardDexRoot(
                        app = app,
                        settings = loadedSettings
                    )
                } else {
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
                }
            }
        }
    }
}
