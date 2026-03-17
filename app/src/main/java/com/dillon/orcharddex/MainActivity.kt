package com.dillon.orcharddex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.ui.OrchardDexRoot
import com.dillon.orcharddex.ui.theme.OrchardDexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as OrchardDexApp
        setContent {
            val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = AppSettings()
            )
            OrchardDexTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                OrchardDexRoot(app = app)
            }
        }
    }
}
