package com.dillon.orcharddex.data.phenology

import android.content.Context
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.PhenologyModelType
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@Serializable
internal data class PhenologyCatalogAsset(
    val speciesProfiles: List<SpeciesProfileAsset> = emptyList(),
    val cultivarProfiles: List<CultivarProfileAsset> = emptyList(),
    val catalogOnlyCultivars: List<CatalogOnlyCultivarAsset> = emptyList()
)

@Serializable
internal data class SpeciesProfileAsset(
    val key: String,
    val aliases: Set<String> = emptySet(),
    val referenceZoneCode: String,
    val startMonth: Int,
    val startDay: Int,
    val durationDays: Long,
    val shiftDaysPerHalfZone: Long = 4,
    val defaultPhase: BloomPhase = BloomPhase.MID,
    val forecastBehavior: BloomForecastBehavior = BloomForecastBehavior.WINDOW,
    val patternType: BloomPatternType? = null,
    val modelType: PhenologyModelType? = null,
    val reviewTier: SpeciesReviewTier = SpeciesReviewTier.BASELINE,
    val pollinationRequirement: PollinationRequirement = PollinationRequirement.UNKNOWN,
    val catalogSpeciesLabel: String = key,
    val uncertaintyNote: String? = null
) {
    companion object {
        fun fromSpeciesBloomProfile(profile: SpeciesBloomProfile): SpeciesProfileAsset = SpeciesProfileAsset(
            key = profile.key,
            aliases = profile.aliases,
            referenceZoneCode = profile.referenceZoneCode,
            startMonth = profile.startMonth,
            startDay = profile.startDay,
            durationDays = profile.durationDays,
            shiftDaysPerHalfZone = profile.shiftDaysPerHalfZone,
            defaultPhase = profile.defaultPhase,
            forecastBehavior = profile.forecastBehavior,
            patternType = profile.patternType,
            modelType = profile.modelType,
            reviewTier = profile.reviewTier,
            pollinationRequirement = profile.pollinationRequirement,
            catalogSpeciesLabel = profile.catalogSpeciesLabel,
            uncertaintyNote = profile.uncertaintyNote
        )
    }
}

internal fun SpeciesProfileAsset.toSpeciesBloomProfile(): SpeciesBloomProfile = SpeciesBloomProfile(
    key = key,
    aliases = aliases,
    referenceZoneCode = referenceZoneCode,
    startMonth = startMonth,
    startDay = startDay,
    durationDays = durationDays,
    shiftDaysPerHalfZone = shiftDaysPerHalfZone,
    defaultPhase = defaultPhase,
    forecastBehavior = forecastBehavior,
    patternType = patternType ?: when (forecastBehavior) {
        BloomForecastBehavior.WINDOW -> BloomPatternType.SINGLE_ANNUAL
        BloomForecastBehavior.MANUAL_ONLY -> BloomPatternType.CONTINUOUS
        BloomForecastBehavior.SUPPRESSED -> BloomPatternType.SUPPRESSED
    },
    modelType = modelType ?: when (forecastBehavior) {
        BloomForecastBehavior.WINDOW -> PhenologyModelType.CLIMATE_WINDOW
        BloomForecastBehavior.MANUAL_ONLY -> PhenologyModelType.MANUAL_ONLY
        BloomForecastBehavior.SUPPRESSED -> PhenologyModelType.MANUAL_ONLY
    },
    reviewTier = reviewTier,
    pollinationRequirement = pollinationRequirement,
    catalogSpeciesLabel = catalogSpeciesLabel,
    uncertaintyNote = uncertaintyNote
)

@Serializable
internal data class CultivarProfileAsset(
    val speciesKey: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val phase: BloomPhase,
    val catalogSpeciesLabel: String = speciesKey,
    val pollinationRequirement: PollinationRequirement? = null
) {
    companion object {
        fun fromCultivarBloomProfile(profile: CultivarBloomProfile): CultivarProfileAsset = CultivarProfileAsset(
            speciesKey = profile.speciesKey,
            cultivar = profile.cultivar,
            aliases = profile.aliases,
            phase = profile.phase,
            catalogSpeciesLabel = profile.catalogSpeciesLabel,
            pollinationRequirement = profile.pollinationRequirement
        )
    }
}

internal fun CultivarProfileAsset.toCultivarBloomProfile(): CultivarBloomProfile = CultivarBloomProfile(
    speciesKey = speciesKey,
    cultivar = cultivar,
    aliases = aliases,
    phase = phase,
    catalogSpeciesLabel = catalogSpeciesLabel,
    pollinationRequirement = pollinationRequirement
)

@Serializable
internal data class CatalogOnlyCultivarAsset(
    val species: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val pollinationRequirement: PollinationRequirement? = null
) {
    companion object {
        fun fromCultivarAutocompleteOption(option: CultivarAutocompleteOption): CatalogOnlyCultivarAsset = CatalogOnlyCultivarAsset(
            species = option.species,
            cultivar = option.cultivar,
            aliases = option.aliases.toSet(),
            pollinationRequirement = option.pollinationRequirement
        )
    }
}

internal fun CatalogOnlyCultivarAsset.toCultivarAutocompleteOption(): CultivarAutocompleteOption = CultivarAutocompleteOption(
    species = species,
    cultivar = cultivar,
    aliases = aliases.sortedBy(String::lowercase),
    pollinationRequirement = pollinationRequirement
)

@Serializable
internal data class PollinationCatalogAsset(
    val speciesOverrides: List<SpeciesPollinationAsset> = emptyList(),
    val cultivarOverrides: List<CultivarPollinationAsset> = emptyList()
)

@Serializable
internal data class SpeciesPollinationAsset(
    val speciesKey: String,
    val pollinationRequirement: PollinationRequirement
)

@Serializable
internal data class CultivarPollinationAsset(
    val speciesKey: String,
    val cultivar: String,
    val pollinationRequirement: PollinationRequirement
)

object PhenologyCatalogAssets {
    private val json = Json { ignoreUnknownKeys = true }
    @OptIn(ExperimentalSerializationApi::class)
    private val prettyJson = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    @Volatile
    private var phenologyCatalog = PhenologyCatalogAsset()

    @Volatile
    private var pollinationCatalog = PollinationCatalogAsset()

    fun initialize(context: Context) {
        phenologyCatalog = loadAsset(context, "phenology_profiles.json")
        pollinationCatalog = loadAsset(context, "pollination_profiles.json")
    }

    fun speciesProfiles(): List<SpeciesBloomProfile> {
        ensureLoaded()
        return phenologyCatalog.speciesProfiles.map(SpeciesProfileAsset::toSpeciesBloomProfile)
    }

    fun cultivarProfiles(): List<CultivarBloomProfile> {
        ensureLoaded()
        return phenologyCatalog.cultivarProfiles.map(CultivarProfileAsset::toCultivarBloomProfile)
    }

    fun catalogOnlyCultivars(): List<CultivarAutocompleteOption> {
        ensureLoaded()
        return phenologyCatalog.catalogOnlyCultivars.map(CatalogOnlyCultivarAsset::toCultivarAutocompleteOption)
    }

    fun speciesPollinationOverrides(): Map<String, PollinationRequirement> {
        ensureLoaded()
        return pollinationCatalog.speciesOverrides.associate { it.speciesKey to it.pollinationRequirement }
    }

    fun cultivarPollinationOverrides(): Map<Pair<String, String>, PollinationRequirement> {
        ensureLoaded()
        return pollinationCatalog.cultivarOverrides.associate {
            (it.speciesKey to it.cultivar) to it.pollinationRequirement
        }
    }

    internal fun serializePhenologyCatalog(
        speciesProfiles: List<SpeciesBloomProfile>,
        cultivarProfiles: List<CultivarBloomProfile>,
        catalogOnlyCultivars: List<CultivarAutocompleteOption> = emptyList()
    ): String = prettyJson.encodeToString(
        PhenologyCatalogAsset(
            speciesProfiles = speciesProfiles
                .sortedBy(SpeciesBloomProfile::key)
                .map(SpeciesProfileAsset.Companion::fromSpeciesBloomProfile),
            cultivarProfiles = cultivarProfiles
                .sortedWith(compareBy(CultivarBloomProfile::speciesKey, CultivarBloomProfile::cultivar))
                .map(CultivarProfileAsset.Companion::fromCultivarBloomProfile),
            catalogOnlyCultivars = catalogOnlyCultivars
                .sortedWith(compareBy(CultivarAutocompleteOption::species, CultivarAutocompleteOption::cultivar))
                .map(CatalogOnlyCultivarAsset.Companion::fromCultivarAutocompleteOption)
        )
    )

    internal fun serializePollinationCatalog(
        speciesOverrides: List<SpeciesPollinationAsset> = emptyList(),
        cultivarOverrides: List<CultivarPollinationAsset> = emptyList()
    ): String = prettyJson.encodeToString(
        PollinationCatalogAsset(
            speciesOverrides = speciesOverrides.sortedBy(SpeciesPollinationAsset::speciesKey),
            cultivarOverrides = cultivarOverrides.sortedWith(compareBy(CultivarPollinationAsset::speciesKey, CultivarPollinationAsset::cultivar))
        )
    )

    private fun ensureLoaded() {
        if (
            phenologyCatalog.speciesProfiles.isNotEmpty() ||
            phenologyCatalog.cultivarProfiles.isNotEmpty() ||
            phenologyCatalog.catalogOnlyCultivars.isNotEmpty()
        ) return
        synchronized(this) {
            if (
                phenologyCatalog.speciesProfiles.isNotEmpty() ||
                phenologyCatalog.cultivarProfiles.isNotEmpty() ||
                phenologyCatalog.catalogOnlyCultivars.isNotEmpty()
            ) return
            phenologyCatalog = loadJvmAsset("phenology_profiles.json", PhenologyCatalogAsset())
            pollinationCatalog = loadJvmAsset("pollination_profiles.json", PollinationCatalogAsset())
        }
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

    private inline fun <reified T> loadJvmAsset(assetName: String, fallback: T): T {
        val assetText = loadJvmAssetText(assetName) ?: return fallback
        return runCatching { json.decodeFromString<T>(assetText) }.getOrDefault(fallback)
    }

    private fun loadJvmAssetText(assetName: String): String? {
        val classpathText = runCatching {
            PhenologyCatalogAssets::class.java.classLoader
                ?.getResourceAsStream(assetName)
                ?.bufferedReader()
                ?.use { it.readText() }
        }.getOrNull()
        if (!classpathText.isNullOrBlank()) return classpathText
        return runCatching {
            File("app/src/main/assets/$assetName")
                .takeIf(File::exists)
                ?.readText()
        }.getOrNull()
    }
}
