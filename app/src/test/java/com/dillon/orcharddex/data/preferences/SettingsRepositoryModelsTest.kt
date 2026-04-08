package com.dillon.orcharddex.data.preferences

import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.SettingsSnapshot
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlinx.serialization.json.Json

class SettingsRepositoryModelsTest {
    @Test
    fun forecastLocationProfile_preservesClimateFingerprint() {
        val settings = AppSettings(
            orchardName = "Backyard",
            hemisphere = Hemisphere.NORTHERN,
            usdaZone = "10a",
            latitudeDeg = 33.7,
            longitudeDeg = -117.8,
            climateSource = "NASA POWER",
            climateFetchedAt = 42L,
            climateMeanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
            climateMeanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
            climateMeanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
        )

        val profile = settings.forecastLocationProfile()

        assertThat(profile.usdaZoneCode).isEqualTo("10a")
        assertThat(profile.climateFingerprint).isEqualTo(
            LocationClimateFingerprint(
                source = "NASA POWER",
                fetchedAt = 42L,
                meanMonthlyTempC = settings.climateMeanMonthlyTempC,
                meanMonthlyMinTempC = settings.climateMeanMonthlyMinTempC,
                meanMonthlyMaxTempC = settings.climateMeanMonthlyMaxTempC
            )
        )
    }

    @Test
    fun settingsSnapshot_defaultsWalkthroughCompleteWhenOlderBackupOmitsIt() {
        val snapshot = Json.decodeFromString<SettingsSnapshot>(
            """
            {
              "themeMode": "SYSTEM",
              "dynamicColor": true,
              "showSalesTools": false,
              "defaultLeadTimeMode": "SAME_DAY",
              "defaultCustomLeadHours": 6,
              "orchardName": "Backyard",
              "usdaZone": "10a",
              "countryCode": "US",
              "timezoneId": "America/New_York",
              "hemisphere": "NORTHERN",
              "defaultLocationId": "",
              "orchardRegion": "",
              "onboardingComplete": true
            }
            """.trimIndent()
        )

        assertThat(snapshot.walkthroughComplete).isFalse()
    }
}
