package com.dillon.orcharddex.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.MicroclimateFlag
import com.dillon.orcharddex.data.model.SettingsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.ZoneId

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
    val defaultCustomLeadHours: Int = 6,
    val orchardName: String = "",
    val usdaZone: String = "",
    val countryCode: String = "",
    val timezoneId: String = ZoneId.systemDefault().id,
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    val latitudeDeg: Double? = null,
    val longitudeDeg: Double? = null,
    val elevationM: Double? = null,
    val chillHoursBand: ChillHoursBand = ChillHoursBand.UNKNOWN,
    val microclimateFlags: Set<MicroclimateFlag> = emptySet(),
    val defaultLocationId: String = "",
    val orchardRegion: String = "",
    val onboardingComplete: Boolean = false
)

fun AppSettings.forecastLocationProfile(): ForecastLocationProfile = ForecastLocationProfile(
    name = orchardName,
    countryCode = countryCode,
    timezoneId = normalizeTimezoneId(timezoneId),
    hemisphere = hemisphere,
    latitudeDeg = latitudeDeg,
    longitudeDeg = longitudeDeg,
    elevationM = elevationM,
    usdaZoneCode = usdaZone.takeIf(String::isNotBlank),
    chillHoursBand = chillHoursBand,
    microclimateFlags = microclimateFlags
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val defaultLeadTimeMode = stringPreferencesKey("default_lead_time_mode")
        val defaultCustomLeadHours = intPreferencesKey("default_custom_lead_hours")
        val orchardName = stringPreferencesKey("orchard_name")
        val usdaZone = stringPreferencesKey("usda_zone")
        val countryCode = stringPreferencesKey("country_code")
        val timezoneId = stringPreferencesKey("timezone_id")
        val hemisphere = stringPreferencesKey("hemisphere")
        val latitudeDeg = stringPreferencesKey("latitude_deg")
        val longitudeDeg = stringPreferencesKey("longitude_deg")
        val elevationM = stringPreferencesKey("elevation_m")
        val chillHoursBand = stringPreferencesKey("chill_hours_band")
        val microclimateFlags = stringSetPreferencesKey("microclimate_flags")
        val defaultLocationId = stringPreferencesKey("default_location_id")
        val orchardRegion = stringPreferencesKey("orchard_region")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
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

    suspend fun updateOrchardName(name: String) {
        context.dataStore.edit { it[Keys.orchardName] = name.trim() }
    }

    suspend fun updateUsdaZone(zoneCode: String) {
        context.dataStore.edit { it[Keys.usdaZone] = zoneCode.trim().lowercase() }
    }

    suspend fun updateForecastLocation(profile: ForecastLocationProfile) {
        context.dataStore.edit { preferences ->
            preferences[Keys.countryCode] = profile.countryCode.trim()
            preferences[Keys.timezoneId] = normalizeTimezoneId(profile.timezoneId)
            preferences[Keys.hemisphere] = profile.hemisphere.name
            preferences[Keys.latitudeDeg] = profile.latitudeDeg?.toString().orEmpty()
            preferences[Keys.longitudeDeg] = profile.longitudeDeg?.toString().orEmpty()
            preferences[Keys.elevationM] = profile.elevationM?.toString().orEmpty()
            preferences[Keys.usdaZone] = profile.usdaZoneCode.orEmpty().trim().lowercase()
            preferences[Keys.chillHoursBand] = profile.chillHoursBand.name
            preferences[Keys.microclimateFlags] = profile.microclimateFlags.mapTo(linkedSetOf(), MicroclimateFlag::name)
        }
    }

    suspend fun updateDefaultLocationId(locationId: String) {
        context.dataStore.edit { it[Keys.defaultLocationId] = locationId.trim() }
    }

    suspend fun updateOrchardRegion(regionCode: String) {
        context.dataStore.edit { it[Keys.orchardRegion] = regionCode.trim().lowercase() }
    }

    suspend fun completeOnboarding(
        orchardName: String,
        locationProfile: ForecastLocationProfile,
        orchardRegion: String = ""
    ) {
        context.dataStore.edit {
            it[Keys.orchardName] = orchardName.trim()
            it[Keys.usdaZone] = locationProfile.usdaZoneCode.orEmpty().trim().lowercase()
            it[Keys.countryCode] = locationProfile.countryCode.trim()
            it[Keys.timezoneId] = normalizeTimezoneId(locationProfile.timezoneId)
            it[Keys.hemisphere] = locationProfile.hemisphere.name
            it[Keys.latitudeDeg] = locationProfile.latitudeDeg?.toString().orEmpty()
            it[Keys.longitudeDeg] = locationProfile.longitudeDeg?.toString().orEmpty()
            it[Keys.elevationM] = locationProfile.elevationM?.toString().orEmpty()
            it[Keys.chillHoursBand] = locationProfile.chillHoursBand.name
            it[Keys.microclimateFlags] = locationProfile.microclimateFlags.mapTo(linkedSetOf(), MicroclimateFlag::name)
            it[Keys.orchardRegion] = orchardRegion.trim().lowercase()
            it[Keys.onboardingComplete] = true
        }
    }

    suspend fun snapshot(): SettingsSnapshot {
        val current = settings.first()
        return SettingsSnapshot(
            themeMode = current.themeMode.name,
            dynamicColor = current.dynamicColor,
            defaultLeadTimeMode = current.defaultLeadTimeMode.name,
            defaultCustomLeadHours = current.defaultCustomLeadHours,
            orchardName = current.orchardName,
            usdaZone = current.usdaZone,
            countryCode = current.countryCode,
            timezoneId = current.timezoneId,
            hemisphere = current.hemisphere,
            latitudeDeg = current.latitudeDeg,
            longitudeDeg = current.longitudeDeg,
            elevationM = current.elevationM,
            chillHoursBand = current.chillHoursBand,
            microclimateFlags = current.microclimateFlags,
            defaultLocationId = current.defaultLocationId,
            orchardRegion = current.orchardRegion,
            onboardingComplete = current.onboardingComplete
        )
    }

    suspend fun restore(snapshot: SettingsSnapshot) {
        context.dataStore.edit {
            it[Keys.themeMode] = snapshot.themeMode
            it[Keys.dynamicColor] = snapshot.dynamicColor
            it[Keys.defaultLeadTimeMode] = snapshot.defaultLeadTimeMode
            it[Keys.defaultCustomLeadHours] = snapshot.defaultCustomLeadHours
            it[Keys.orchardName] = snapshot.orchardName
            it[Keys.usdaZone] = snapshot.usdaZone
            it[Keys.countryCode] = snapshot.countryCode
            it[Keys.timezoneId] = normalizeTimezoneId(snapshot.timezoneId)
            it[Keys.hemisphere] = snapshot.hemisphere.name
            it[Keys.latitudeDeg] = snapshot.latitudeDeg?.toString().orEmpty()
            it[Keys.longitudeDeg] = snapshot.longitudeDeg?.toString().orEmpty()
            it[Keys.elevationM] = snapshot.elevationM?.toString().orEmpty()
            it[Keys.chillHoursBand] = snapshot.chillHoursBand.name
            it[Keys.microclimateFlags] = snapshot.microclimateFlags.mapTo(linkedSetOf(), MicroclimateFlag::name)
            it[Keys.defaultLocationId] = snapshot.defaultLocationId
            it[Keys.orchardRegion] = snapshot.orchardRegion
            it[Keys.onboardingComplete] = snapshot.onboardingComplete
        }
    }

    private fun preferencesToSettings(preferences: Preferences): AppSettings = AppSettings(
        themeMode = preferences[Keys.themeMode]?.let(AppThemeMode::valueOf) ?: AppThemeMode.SYSTEM,
        dynamicColor = preferences[Keys.dynamicColor] ?: true,
        defaultLeadTimeMode = preferences[Keys.defaultLeadTimeMode]?.let(LeadTimeMode::valueOf)
            ?: LeadTimeMode.SAME_DAY,
        defaultCustomLeadHours = preferences[Keys.defaultCustomLeadHours] ?: 6,
        orchardName = preferences[Keys.orchardName].orEmpty(),
        usdaZone = preferences[Keys.usdaZone].orEmpty(),
        countryCode = preferences[Keys.countryCode].orEmpty(),
        timezoneId = normalizeTimezoneId(preferences[Keys.timezoneId].orEmpty()),
        hemisphere = preferences[Keys.hemisphere]?.let(Hemisphere::valueOf) ?: Hemisphere.NORTHERN,
        latitudeDeg = preferences[Keys.latitudeDeg]?.toDoubleOrNull(),
        longitudeDeg = preferences[Keys.longitudeDeg]?.toDoubleOrNull(),
        elevationM = preferences[Keys.elevationM]?.toDoubleOrNull(),
        chillHoursBand = preferences[Keys.chillHoursBand]?.let(ChillHoursBand::valueOf) ?: ChillHoursBand.UNKNOWN,
        microclimateFlags = preferences[Keys.microclimateFlags]
            ?.mapNotNull { value -> runCatching { MicroclimateFlag.valueOf(value) }.getOrNull() }
            ?.toSet()
            .orEmpty(),
        defaultLocationId = preferences[Keys.defaultLocationId].orEmpty(),
        orchardRegion = preferences[Keys.orchardRegion].orEmpty(),
        onboardingComplete = preferences[Keys.onboardingComplete] ?: false
    )
}

private fun normalizeTimezoneId(value: String): String = runCatching {
    ZoneId.of(value.trim()).id
}.getOrElse { ZoneId.systemDefault().id }
