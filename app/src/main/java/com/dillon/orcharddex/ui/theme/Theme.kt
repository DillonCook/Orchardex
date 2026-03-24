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
    primary = Color(0xFF5D7F2E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE8BC),
    onPrimaryContainer = Color(0xFF1F2A0B),
    secondary = Color(0xFFC4892B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E1B4),
    onSecondaryContainer = Color(0xFF3F2500),
    tertiary = Color(0xFF7290C6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD9E4F8),
    onTertiaryContainer = Color(0xFF162640),
    background = Color(0xFFF7F2E5),
    onBackground = Color(0xFF241F16),
    surface = Color(0xFFFFFBF3),
    onSurface = Color(0xFF241E15),
    surfaceVariant = Color(0xFFE7E1CF),
    onSurfaceVariant = Color(0xFF595241),
    outline = Color(0xFF847B66),
    outlineVariant = Color(0xFFD0C7B3)
)

private val OrchardDarkScheme = darkColorScheme(
    primary = Color(0xFF8B78E6),
    onPrimary = Color(0xFF1E1539),
    primaryContainer = Color(0xFF33285A),
    onPrimaryContainer = Color(0xFFE8E0FF),
    secondary = Color(0xFFF0B54B),
    onSecondary = Color(0xFF3E2800),
    secondaryContainer = Color(0xFF594019),
    onSecondaryContainer = Color(0xFFFFE9C2),
    tertiary = Color(0xFF84A37E),
    onTertiary = Color(0xFF132013),
    tertiaryContainer = Color(0xFF324232),
    onTertiaryContainer = Color(0xFFD7E8D1),
    background = Color(0xFF121814),
    onBackground = Color(0xFFE6ECE3),
    surface = Color(0xFF1A231D),
    onSurface = Color(0xFFE6ECE3),
    surfaceVariant = Color(0xFF243127),
    onSurfaceVariant = Color(0xFFAAB5AA),
    outline = Color(0xFF435245),
    outlineVariant = Color(0xFF334034)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
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
