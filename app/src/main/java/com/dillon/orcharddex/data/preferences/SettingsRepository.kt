package com.dillon.orcharddex.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
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
    val showSalesTools: Boolean = false,
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
    val climateSource: String = "",
    val climateFetchedAt: Long? = null,
    val climateMeanMonthlyTempC: List<Double> = emptyList(),
    val climateMeanMonthlyMinTempC: List<Double> = emptyList(),
    val climateMeanMonthlyMaxTempC: List<Double> = emptyList(),
    val defaultLocationId: String = "",
    val orchardRegion: String = "",
    val onboardingComplete: Boolean = false,
    val walkthroughComplete: Boolean = false
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
    microclimateFlags = microclimateFlags,
    climateFingerprint = climateSource.takeIf(String::isNotBlank)?.let { source ->
        LocationClimateFingerprint(
            source = source,
            fetchedAt = climateFetchedAt ?: 0L,
            meanMonthlyTempC = climateMeanMonthlyTempC,
            meanMonthlyMinTempC = climateMeanMonthlyMinTempC,
            meanMonthlyMaxTempC = climateMeanMonthlyMaxTempC
        )
    }
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val showSalesTools = booleanPreferencesKey("show_sales_tools")
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
        val climateSource = stringPreferencesKey("climate_source")
        val climateFetchedAt = longPreferencesKey("climate_fetched_at")
        val climateMeanMonthlyTempC = stringPreferencesKey("climate_mean_monthly_temp_c")
        val climateMeanMonthlyMinTempC = stringPreferencesKey("climate_mean_monthly_min_temp_c")
        val climateMeanMonthlyMaxTempC = stringPreferencesKey("climate_mean_monthly_max_temp_c")
        val defaultLocationId = stringPreferencesKey("default_location_id")
        val orchardRegion = stringPreferencesKey("orchard_region")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val walkthroughComplete = booleanPreferencesKey("walkthrough_complete")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map(::preferencesToSettings)

    suspend fun updateTheme(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun updateShowSalesTools(enabled: Boolean) {
        context.dataStore.edit { it[Keys.showSalesTools] = enabled }
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
            val climateFingerprint = profile.climateFingerprint
            preferences[Keys.climateSource] = climateFingerprint?.source.orEmpty()
            if (climateFingerprint == null) {
                preferences.remove(Keys.climateFetchedAt)
            } else {
                preferences[Keys.climateFetchedAt] = climateFingerprint.fetchedAt
            }
            preferences[Keys.climateMeanMonthlyTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyTempC.orEmpty())
            preferences[Keys.climateMeanMonthlyMinTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyMinTempC.orEmpty())
            preferences[Keys.climateMeanMonthlyMaxTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyMaxTempC.orEmpty())
        }
    }

    suspend fun updateDefaultLocationId(locationId: String) {
        context.dataStore.edit { it[Keys.defaultLocationId] = locationId.trim() }
    }

    suspend fun updateOrchardRegion(regionCode: String) {
        context.dataStore.edit { it[Keys.orchardRegion] = regionCode.trim().lowercase() }
    }

    suspend fun markWalkthroughComplete() {
        context.dataStore.edit { it[Keys.walkthroughComplete] = true }
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
            val climateFingerprint = locationProfile.climateFingerprint
            it[Keys.climateSource] = climateFingerprint?.source.orEmpty()
            if (climateFingerprint == null) {
                it.remove(Keys.climateFetchedAt)
            } else {
                it[Keys.climateFetchedAt] = climateFingerprint.fetchedAt
            }
            it[Keys.climateMeanMonthlyTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyTempC.orEmpty())
            it[Keys.climateMeanMonthlyMinTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyMinTempC.orEmpty())
            it[Keys.climateMeanMonthlyMaxTempC] = encodeDoubleList(climateFingerprint?.meanMonthlyMaxTempC.orEmpty())
            it[Keys.orchardRegion] = orchardRegion.trim().lowercase()
            it[Keys.onboardingComplete] = true
            it[Keys.walkthroughComplete] = false
        }
    }

    suspend fun snapshot(): SettingsSnapshot {
        val current = settings.first()
        return SettingsSnapshot(
            themeMode = current.themeMode.name,
            dynamicColor = current.dynamicColor,
            showSalesTools = current.showSalesTools,
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
            climateSource = current.climateSource,
            climateFetchedAt = current.climateFetchedAt,
            climateMeanMonthlyTempC = current.climateMeanMonthlyTempC,
            climateMeanMonthlyMinTempC = current.climateMeanMonthlyMinTempC,
            climateMeanMonthlyMaxTempC = current.climateMeanMonthlyMaxTempC,
            defaultLocationId = current.defaultLocationId,
            orchardRegion = current.orchardRegion,
            onboardingComplete = current.onboardingComplete,
            walkthroughComplete = current.walkthroughComplete
        )
    }

    suspend fun restore(snapshot: SettingsSnapshot) {
        context.dataStore.edit {
            it[Keys.themeMode] = snapshot.themeMode
            it[Keys.dynamicColor] = snapshot.dynamicColor
            it[Keys.showSalesTools] = snapshot.showSalesTools
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
            it[Keys.climateSource] = snapshot.climateSource
            if (snapshot.climateFetchedAt == null) {
                it.remove(Keys.climateFetchedAt)
            } else {
                it[Keys.climateFetchedAt] = snapshot.climateFetchedAt
            }
            it[Keys.climateMeanMonthlyTempC] = encodeDoubleList(snapshot.climateMeanMonthlyTempC)
            it[Keys.climateMeanMonthlyMinTempC] = encodeDoubleList(snapshot.climateMeanMonthlyMinTempC)
            it[Keys.climateMeanMonthlyMaxTempC] = encodeDoubleList(snapshot.climateMeanMonthlyMaxTempC)
            it[Keys.defaultLocationId] = snapshot.defaultLocationId
            it[Keys.orchardRegion] = snapshot.orchardRegion
            it[Keys.onboardingComplete] = snapshot.onboardingComplete
            it[Keys.walkthroughComplete] = snapshot.walkthroughComplete
        }
    }

    private fun preferencesToSettings(preferences: Preferences): AppSettings = AppSettings(
        themeMode = preferences[Keys.themeMode]?.let(AppThemeMode::valueOf) ?: AppThemeMode.SYSTEM,
        dynamicColor = preferences[Keys.dynamicColor] ?: true,
        showSalesTools = preferences[Keys.showSalesTools] ?: false,
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
        climateSource = preferences[Keys.climateSource].orEmpty(),
        climateFetchedAt = preferences[Keys.climateFetchedAt],
        climateMeanMonthlyTempC = decodeDoubleList(preferences[Keys.climateMeanMonthlyTempC]),
        climateMeanMonthlyMinTempC = decodeDoubleList(preferences[Keys.climateMeanMonthlyMinTempC]),
        climateMeanMonthlyMaxTempC = decodeDoubleList(preferences[Keys.climateMeanMonthlyMaxTempC]),
        defaultLocationId = preferences[Keys.defaultLocationId].orEmpty(),
        orchardRegion = preferences[Keys.orchardRegion].orEmpty(),
        onboardingComplete = preferences[Keys.onboardingComplete] ?: false,
        walkthroughComplete = preferences[Keys.walkthroughComplete] ?: false
    )
}

private fun encodeDoubleList(values: List<Double>): String = values.joinToString(",")

private fun decodeDoubleList(serialized: String?): List<Double> = serialized
    .orEmpty()
    .split(',')
    .mapNotNull { token -> token.trim().takeIf(String::isNotBlank)?.toDoubleOrNull() }

private fun normalizeTimezoneId(value: String): String = runCatching {
    ZoneId.of(value.trim()).id
}.getOrElse { ZoneId.systemDefault().id }
