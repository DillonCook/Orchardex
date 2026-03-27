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
    val notes: String = ""
) {
    fun climateBand(): ClimateBand? {
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

    fun hasForecastSignals(): Boolean =
        !usdaZoneCode.isNullOrBlank() ||
            latitudeDeg != null ||
            elevationM != null ||
            chillHoursBand != ChillHoursBand.UNKNOWN ||
            microclimateFlags.isNotEmpty()
}

fun ForecastLocationProfile.normalizedName(fallback: String = "Primary orchard"): String =
    name.trim().ifBlank { fallback }

data class PhenologyObservation(
    val treeId: String,
    val dateMillis: Long,
    val eventType: EventType? = null,
    val isHarvest: Boolean = false
)
