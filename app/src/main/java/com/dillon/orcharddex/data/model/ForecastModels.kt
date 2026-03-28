package com.dillon.orcharddex.data.model

import kotlinx.serialization.Serializable
import java.time.ZoneId

@Serializable
enum class Hemisphere(val label: String) {
    NORTHERN("Northern hemisphere"),
    SOUTHERN("Southern hemisphere"),
    EQUATORIAL("Equatorial")
}

@Serializable
enum class ChillHoursBand(val label: String) {
    UNKNOWN("Unknown"),
    UNDER_100("<100 hours"),
    H100_300("100-300 hours"),
    H300_600("300-600 hours"),
    H600_900("600-900 hours"),
    H900_PLUS("900+ hours")
}

@Serializable
enum class MicroclimateFlag(val label: String) {
    GREENHOUSE("Greenhouse"),
    COASTAL("Coastal"),
    URBAN_HEAT("Urban heat"),
    WARM_WALL("Warm wall"),
    FROST_POCKET("Frost pocket"),
    EXPOSED_WIND("Exposed / windy"),
    SHADED_COOL("Shaded / cool")
}

@Serializable
enum class ForecastSource(val label: String) {
    CULTIVAR_ADJUSTED("cultivar-adjusted"),
    SPECIES_BASELINE("species baseline"),
    HEMISPHERE_SHIFTED("hemisphere-shifted"),
    CLIMATE_BAND("climate band"),
    CUSTOM("custom"),
    HISTORY_LEARNED("history-learned")
}

@Serializable
enum class ForecastConfidence(val label: String) {
    HIGH("high confidence"),
    MEDIUM("medium confidence"),
    LOW("low confidence")
}

@Serializable
enum class SelfCompatibility(val label: String) {
    SELF_FERTILE("Self-fertile"),
    PARTIAL("Partial"),
    SELF_STERILE("Self-sterile"),
    UNKNOWN("Unknown")
}

@Serializable
enum class PollinationMode(val label: String) {
    SELF_POLLINATING("Self-pollinating"),
    INSECT("Insect pollination"),
    HAND_HELPFUL("Hand pollination helps"),
    HAND_REQUIRED("Hand pollination required"),
    UNKNOWN("Unknown")
}

@Serializable
data class PollinationProfile(
    val selfCompatibility: SelfCompatibility = SelfCompatibility.UNKNOWN,
    val pollinationMode: PollinationMode = PollinationMode.UNKNOWN
) {
    val summaryLabel: String
        get() = listOfNotNull(
            selfCompatibility.takeUnless { it == SelfCompatibility.UNKNOWN }?.label,
            pollinationMode.takeUnless { it == PollinationMode.UNKNOWN }?.label
        ).joinToString(" · ").ifBlank { "Unknown" }
}

enum class ClimateBand(val label: String) {
    EQUATORIAL("0-12°"),
    TROPICAL("12-23.5°"),
    SUBTROPICAL("23.5-35°"),
    TEMPERATE("35-45°"),
    COOL("45°+")
}

@Serializable
data class LocationSearchResult(
    val name: String,
    val countryCode: String = "",
    val country: String = "",
    val admin1: String = "",
    val admin2: String = "",
    val timezoneId: String = "",
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val elevationM: Double? = null
) {
    val displayLabel: String
        get() = listOfNotNull(
            name.takeIf(String::isNotBlank),
            admin1.takeIf(String::isNotBlank),
            country.takeIf(String::isNotBlank)
        ).joinToString(", ")
}

@Serializable
data class LocationClimateFingerprint(
    val source: String = "",
    val fetchedAt: Long = 0L,
    val meanMonthlyTempC: List<Double> = emptyList(),
    val meanMonthlyMinTempC: List<Double> = emptyList(),
    val meanMonthlyMaxTempC: List<Double> = emptyList()
) {
    fun isComplete(): Boolean =
        meanMonthlyTempC.size == 12 &&
            meanMonthlyMinTempC.size == 12 &&
            meanMonthlyMaxTempC.size == 12

    fun annualMeanTempC(): Double? =
        meanMonthlyTempC.takeIf { it.size == 12 }?.average()

    fun annualTempRangeC(): Double? {
        val monthly = meanMonthlyTempC.takeIf { it.size == 12 } ?: return null
        return monthly.maxOrNull()?.minus(monthly.minOrNull() ?: return null)
    }

    fun coldestMonthMeanTempC(): Double? =
        meanMonthlyTempC.takeIf { it.size == 12 }?.minOrNull()

    fun derivedClimateBand(): ClimateBand? {
        val coldestMonth = coldestMonthMeanTempC() ?: return null
        return when {
            coldestMonth >= 24.0 -> ClimateBand.EQUATORIAL
            coldestMonth >= 18.0 -> ClimateBand.TROPICAL
            coldestMonth >= 10.0 -> ClimateBand.SUBTROPICAL
            coldestMonth >= 2.0 -> ClimateBand.TEMPERATE
            else -> ClimateBand.COOL
        }
    }

    fun estimatedChillHoursBand(): ChillHoursBand? {
        val coldestMonth = coldestMonthMeanTempC() ?: return null
        return when {
            coldestMonth >= 18.0 -> ChillHoursBand.UNDER_100
            coldestMonth >= 14.0 -> ChillHoursBand.H100_300
            coldestMonth >= 10.0 -> ChillHoursBand.H300_600
            coldestMonth >= 6.0 -> ChillHoursBand.H600_900
            else -> ChillHoursBand.H900_PLUS
        }
    }

    fun warmSeasonWindow(
        minMeanTempC: Double = 18.0,
        minMaxTempC: Double = 24.0
    ): WarmSeasonWindow? {
        if (!isComplete()) return null
        val warmMonths = meanMonthlyTempC.indices.filter { index ->
            meanMonthlyTempC[index] >= minMeanTempC || meanMonthlyMaxTempC[index] >= minMaxTempC
        }
        if (warmMonths.isEmpty()) return null

        val doubled = (warmMonths + warmMonths.map { it + 12 }).sorted()
        var bestStart = doubled.first()
        var bestLength = 1
        var currentStart = doubled.first()
        var currentLength = 1
        for (index in 1 until doubled.size) {
            if (doubled[index] == doubled[index - 1] + 1) {
                currentLength += 1
            } else {
                if (currentLength > bestLength) {
                    bestStart = currentStart
                    bestLength = currentLength
                }
                currentStart = doubled[index]
                currentLength = 1
            }
        }
        if (currentLength > bestLength) {
            bestStart = currentStart
            bestLength = currentLength
        }
        return WarmSeasonWindow(
            startMonth = (bestStart % 12) + 1,
            monthCount = bestLength.coerceAtMost(12)
        )
    }
}

data class WarmSeasonWindow(
    val startMonth: Int,
    val monthCount: Int
)

@Serializable
data class ForecastLocationProfile(
    val name: String = "",
    val countryCode: String = "",
    val timezoneId: String = ZoneId.systemDefault().id,
    val hemisphere: Hemisphere = Hemisphere.NORTHERN,
    val latitudeDeg: Double? = null,
    val longitudeDeg: Double? = null,
    val elevationM: Double? = null,
    val usdaZoneCode: String? = null,
    val chillHoursBand: ChillHoursBand = ChillHoursBand.UNKNOWN,
    val microclimateFlags: Set<MicroclimateFlag> = emptySet(),
    val climateFingerprint: LocationClimateFingerprint? = null,
    val notes: String = ""
) {
    fun climateBand(): ClimateBand? {
        climateFingerprint?.derivedClimateBand()?.let { return it }
        val latitude = latitudeDeg ?: return null
        val absoluteLatitude = kotlin.math.abs(latitude)
        return when {
            absoluteLatitude < 12.0 -> ClimateBand.EQUATORIAL
            absoluteLatitude < 23.5 -> ClimateBand.TROPICAL
            absoluteLatitude < 35.0 -> ClimateBand.SUBTROPICAL
            absoluteLatitude < 45.0 -> ClimateBand.TEMPERATE
            else -> ClimateBand.COOL
        }
    }

    fun effectiveChillHoursBand(): ChillHoursBand =
        chillHoursBand.takeUnless { it == ChillHoursBand.UNKNOWN }
            ?: climateFingerprint?.estimatedChillHoursBand()
            ?: ChillHoursBand.UNKNOWN

    fun warmSeasonWindow(): WarmSeasonWindow? = climateFingerprint?.warmSeasonWindow()

    fun hasForecastSignals(): Boolean =
        !usdaZoneCode.isNullOrBlank() ||
            latitudeDeg != null ||
            elevationM != null ||
            climateFingerprint?.isComplete() == true ||
            chillHoursBand != ChillHoursBand.UNKNOWN ||
            microclimateFlags.isNotEmpty()
}

fun ForecastLocationProfile.normalizedName(fallback: String = "Growing location"): String =
    name.trim().ifBlank { fallback }

data class PhenologyObservation(
    val treeId: String,
    val dateMillis: Long,
    val eventType: EventType? = null,
    val isHarvest: Boolean = false
)
