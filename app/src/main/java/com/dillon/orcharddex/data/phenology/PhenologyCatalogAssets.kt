package com.dillon.orcharddex.data.phenology

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class PhenologyCatalogAsset(
    val speciesProfiles: List<SpeciesProfileAsset> = emptyList(),
    val cultivarProfiles: List<CultivarProfileAsset> = emptyList()
)

@Serializable
private data class SpeciesProfileAsset(
    val key: String,
    val aliases: Set<String> = emptySet(),
    val referenceZoneCode: String,
    val startMonth: Int,
    val startDay: Int,
    val durationDays: Long,
    val shiftDaysPerHalfZone: Long = 4,
    val defaultPhase: BloomPhase = BloomPhase.MID,
    val forecastBehavior: BloomForecastBehavior = BloomForecastBehavior.WINDOW,
    val pollinationRequirement: PollinationRequirement = PollinationRequirement.UNKNOWN,
    val catalogSpeciesLabel: String = key
) {
    fun toSpeciesBloomProfile(): SpeciesBloomProfile = SpeciesBloomProfile(
        key = key,
        aliases = aliases,
        referenceZoneCode = referenceZoneCode,
        startMonth = startMonth,
        startDay = startDay,
        durationDays = durationDays,
        shiftDaysPerHalfZone = shiftDaysPerHalfZone,
        defaultPhase = defaultPhase,
        forecastBehavior = forecastBehavior,
        pollinationRequirement = pollinationRequirement,
        catalogSpeciesLabel = catalogSpeciesLabel
    )
}

@Serializable
private data class CultivarProfileAsset(
    val speciesKey: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val phase: BloomPhase,
    val catalogSpeciesLabel: String = speciesKey,
    val pollinationRequirement: PollinationRequirement? = null
) {
    fun toCultivarBloomProfile(): CultivarBloomProfile = CultivarBloomProfile(
        speciesKey = speciesKey,
        cultivar = cultivar,
        aliases = aliases,
        phase = phase,
        catalogSpeciesLabel = catalogSpeciesLabel,
        pollinationRequirement = pollinationRequirement
    )
}

@Serializable
private data class PollinationCatalogAsset(
    val speciesOverrides: List<SpeciesPollinationAsset> = emptyList(),
    val cultivarOverrides: List<CultivarPollinationAsset> = emptyList()
)

@Serializable
private data class SpeciesPollinationAsset(
    val speciesKey: String,
    val pollinationRequirement: PollinationRequirement
)

@Serializable
private data class CultivarPollinationAsset(
    val speciesKey: String,
    val cultivar: String,
    val pollinationRequirement: PollinationRequirement
)

object PhenologyCatalogAssets {
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var phenologyCatalog = PhenologyCatalogAsset()

    @Volatile
    private var pollinationCatalog = PollinationCatalogAsset()

    fun initialize(context: Context) {
        phenologyCatalog = loadAsset(context, "phenology_profiles.json")
        pollinationCatalog = loadAsset(context, "pollination_profiles.json")
    }

    fun speciesProfiles(): List<SpeciesBloomProfile> =
        phenologyCatalog.speciesProfiles.map(SpeciesProfileAsset::toSpeciesBloomProfile)

    fun cultivarProfiles(): List<CultivarBloomProfile> =
        phenologyCatalog.cultivarProfiles.map(CultivarProfileAsset::toCultivarBloomProfile)

    fun speciesPollinationOverrides(): Map<String, PollinationRequirement> =
        pollinationCatalog.speciesOverrides.associate { it.speciesKey to it.pollinationRequirement }

    fun cultivarPollinationOverrides(): Map<Pair<String, String>, PollinationRequirement> =
        pollinationCatalog.cultivarOverrides.associate {
            (it.speciesKey to it.cultivar) to it.pollinationRequirement
        }

    private inline fun <reified T> loadAsset(context: Context, assetName: String): T {
        return runCatching {
            context.assets.open(assetName).bufferedReader().use { reader ->
                json.decodeFromString<T>(reader.readText())
            }
        }.getOrElse {
            when (T::class) {
                PhenologyCatalogAsset::class -> PhenologyCatalogAsset() as T
                PollinationCatalogAsset::class -> PollinationCatalogAsset() as T
                else -> error("Unsupported asset type for $assetName")
            }
        }
    }
}
