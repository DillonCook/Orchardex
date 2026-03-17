package com.dillon.orcharddex.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.SettingsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "orcharddex_settings")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultLeadTimeMode: LeadTimeMode = LeadTimeMode.SAME_DAY,
    val defaultCustomLeadHours: Int = 6
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val defaultLeadTimeMode = stringPreferencesKey("default_lead_time_mode")
        val defaultCustomLeadHours = intPreferencesKey("default_custom_lead_hours")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map(::preferencesToSettings)

    suspend fun updateTheme(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun updateDefaultLeadTime(mode: LeadTimeMode, customHours: Int) {
        context.dataStore.edit {
            it[Keys.defaultLeadTimeMode] = mode.name
            it[Keys.defaultCustomLeadHours] = customHours
        }
    }

    suspend fun snapshot(): SettingsSnapshot {
        val current = settings.first()
        return SettingsSnapshot(
            themeMode = current.themeMode.name,
            dynamicColor = current.dynamicColor,
            defaultLeadTimeMode = current.defaultLeadTimeMode.name,
            defaultCustomLeadHours = current.defaultCustomLeadHours
        )
    }

    suspend fun restore(snapshot: SettingsSnapshot) {
        context.dataStore.edit {
            it[Keys.themeMode] = snapshot.themeMode
            it[Keys.dynamicColor] = snapshot.dynamicColor
            it[Keys.defaultLeadTimeMode] = snapshot.defaultLeadTimeMode
            it[Keys.defaultCustomLeadHours] = snapshot.defaultCustomLeadHours
        }
    }

    private fun preferencesToSettings(preferences: Preferences): AppSettings = AppSettings(
        themeMode = preferences[Keys.themeMode]?.let(AppThemeMode::valueOf) ?: AppThemeMode.SYSTEM,
        dynamicColor = preferences[Keys.dynamicColor] ?: true,
        defaultLeadTimeMode = preferences[Keys.defaultLeadTimeMode]?.let(LeadTimeMode::valueOf)
            ?: LeadTimeMode.SAME_DAY,
        defaultCustomLeadHours = preferences[Keys.defaultCustomLeadHours] ?: 6
    )
}
