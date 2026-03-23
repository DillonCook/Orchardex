package com.dillon.orcharddex.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dillon.orcharddex.data.preferences.AppThemeMode

private val OrchardLightScheme = lightColorScheme(
    primary = Color(0xFF35543A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E9D6),
    onPrimaryContainer = Color(0xFF142414),
    secondary = Color(0xFFB9852B),
    onSecondary = Color(0xFF2B1A00),
    secondaryContainer = Color(0xFFF8E4BD),
    onSecondaryContainer = Color(0xFF3A2703),
    tertiary = Color(0xFF607B6C),
    background = Color(0xFFF8F5EF),
    surface = Color(0xFFFFFBF4),
    surfaceVariant = Color(0xFFE6E0D7),
    outline = Color(0xFF726B61)
)

private val OrchardDarkScheme = darkColorScheme(
    primary = Color(0xFFA8CAA6),
    onPrimary = Color(0xFF0F1A0F),
    primaryContainer = Color(0xFF1F3322),
    onPrimaryContainer = Color(0xFFD6E9D6),
    secondary = Color(0xFFF0C98A),
    onSecondary = Color(0xFF392400),
    secondaryContainer = Color(0xFF614013),
    onSecondaryContainer = Color(0xFFF8E4BD),
    tertiary = Color(0xFFB7CCBF),
    background = Color(0xFF0E1110),
    surface = Color(0xFF181D1A),
    surfaceVariant = Color(0xFF2A322D),
    outline = Color(0xFF8C938B)
)

@Composable
fun OrchardDexTheme(
    themeMode: AppThemeMode,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> OrchardDarkScheme
        else -> OrchardLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OrchardTypography,
        content = content
    )
}
