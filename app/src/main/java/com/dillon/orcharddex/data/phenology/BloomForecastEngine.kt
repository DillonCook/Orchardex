package com.dillon.orcharddex.data.phenology

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.ClimateBand
import com.dillon.orcharddex.data.model.ForecastConfidence
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.ForecastSource
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.MicroclimateFlag
import com.dillon.orcharddex.data.model.PhenologyModelType
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.PollinationProfile
import com.dillon.orcharddex.data.model.SelfCompatibility
import com.dillon.orcharddex.data.model.bloomMonthLabels
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
import com.dillon.orcharddex.time.OrchardTime
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.serialization.Serializable

enum class BloomPhase(val label: String, val startOffsetDays: Int) {
    EARLY("Early", -10),
    EARLY_MID("Early-mid", -5),
    MID("Mid", 0),
    MID_LATE("Mid-late", 5),
    LATE("Late", 10)
}

enum class BloomForecastBehavior {
    WINDOW,
    MANUAL_ONLY,
    SUPPRESSED
}

enum class PollinationRequirement(val label: String) {
    SELF_FERTILE("Self-fertile"),
    SELF_FERTILE_CROSS_BENEFITS("Self-fertile, cross-pollination helps"),
    NEEDS_CROSS_POLLINATION("Needs cross-pollination"),
    CROSS_POLLINATION_RECOMMENDED("Cross-pollination recommended"),
    PARTIAL_SELF_INCOMPATIBILITY("Partial self-incompatibility"),
    POLLINATION_NOT_REQUIRED("Pollination not required"),
    UNKNOWN("Unknown")
}

@Serializable
enum class SpeciesReviewTier {
    BASELINE,
    REVIEWED,
    VALIDATED
}

data class SpeciesBloomProfile(
    val key: String,
    val aliases: Set<String>,
    val referenceZoneCode: String,
    val startMonth: Int,
    val startDay: Int,
    val durationDays: Long,
    val shiftDaysPerHalfZone: Long = 4,
    val defaultPhase: BloomPhase = BloomPhase.MID,
    val forecastBehavior: BloomForecastBehavior = BloomForecastBehavior.WINDOW,
    val patternType: BloomPatternType = when (forecastBehavior) {
        BloomForecastBehavior.WINDOW -> BloomPatternType.SINGLE_ANNUAL
        BloomForecastBehavior.MANUAL_ONLY -> BloomPatternType.CONTINUOUS
        BloomForecastBehavior.SUPPRESSED -> BloomPatternType.SUPPRESSED
    },
    val modelType: PhenologyModelType = when (forecastBehavior) {
        BloomForecastBehavior.WINDOW -> PhenologyModelType.CLIMATE_WINDOW
        BloomForecastBehavior.MANUAL_ONLY -> PhenologyModelType.MANUAL_ONLY
        BloomForecastBehavior.SUPPRESSED -> PhenologyModelType.MANUAL_ONLY
    },
    val reviewTier: SpeciesReviewTier = SpeciesReviewTier.BASELINE,
    val pollinationRequirement: PollinationRequirement = PollinationRequirement.UNKNOWN,
    val catalogSpeciesLabel: String = key,
    val uncertaintyNote: String? = null
)

data class RegionalBloomOverride(
    val speciesKey: String,
    val orchardRegion: OrchardRegion,
    val referenceZoneCode: String,
    val startMonth: Int,
    val startDay: Int,
    val durationDays: Long,
    val shiftDaysPerHalfZone: Long = 4
)

data class CultivarBloomProfile(
    val speciesKey: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val phase: BloomPhase,
    val catalogSpeciesLabel: String = speciesKey,
    val pollinationRequirement: PollinationRequirement? = null
)

data class PredictedBloomWindow(
    val treeId: String,
    val treeLabel: String,
    val speciesLabel: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val phase: BloomPhase,
    val patternType: BloomPatternType,
    val source: ForecastSource,
    val confidence: ForecastConfidence,
    val sourceLabel: String = source.label,
    val confidenceLabel: String = confidence.label
)

data class BloomForecastSummary(
    val headline: String,
    val supportingLine: String,
    val source: ForecastSource,
    val confidence: ForecastConfidence,
    val patternType: BloomPatternType = BloomPatternType.SINGLE_ANNUAL,
    val patternLabel: String = "",
    val timingLabel: String? = null,
    val exactCountdownAllowed: Boolean = patternType == BloomPatternType.SINGLE_ANNUAL,
    val daysUntilStart: Long? = null,
    val isCurrentWindow: Boolean = false
)

data class SupportedCultivarCatalogEntry(
    val species: String,
    val cultivar: String,
    val aliases: List<String> = emptyList(),
    val pollinationRequirement: PollinationRequirement? = null
)

data class CatalogCultivarReferenceEntry(
    val cultivar: String,
    val aliases: List<String> = emptyList(),
    val fertilityLabel: String? = null
)

data class CatalogUsdaBloomTimingEntry(
    val zoneCode: String,
    val zoneLabel: String,
    val timingLabel: String
)

data class CatalogSpeciesReferenceEntry(
    val species: String,
    val aliases: List<String> = emptyList(),
    val referenceBloomTimingLabel: String,
    val zoneBloomTimings: List<CatalogUsdaBloomTimingEntry> = emptyList(),
    val fertilityLabel: String,
    val cultivars: List<CatalogCultivarReferenceEntry> = emptyList()
)

data class CultivarAutocompleteOption(
    val species: String,
    val cultivar: String,
    val aliases: List<String> = emptyList(),
    val pollinationRequirement: PollinationRequirement? = null
)

data class SpeciesAutocompleteOption(
    val species: String,
    val aliases: List<String> = emptyList()
)

data class EverbearingPlant(
    val treeId: String,
    val treeLabel: String,
    val speciesLabel: String,
    val detailLabel: String
)

object BloomForecastEngine {
    private val catalogMonthDayFormatter = DateTimeFormatter.ofPattern("MMM d")
    private const val catalogSeparator = " - "
    private val hemisphereShiftUnsafeKeys = setOf("dragon fruit")
    private val winterBloomKeys = setOf("loquat")
    private val chillSensitiveKeys = setOf(
        "apple",
        "pear",
        "european pear",
        "asian pear",
        "peach",
        "nectarine",
        "plum",
        "apricot",
        "sweet cherry",
        "sour cherry",
        "blueberry"
    )

    private fun formatCatalogRange(startDate: LocalDate, endDate: LocalDate): String =
        "${startDate.format(catalogMonthDayFormatter)} - ${endDate.format(catalogMonthDayFormatter)}"

    private val baseSpeciesProfiles = listOf(
        SpeciesBloomProfile(
            "peach",
            setOf("peach"),
            "7a",
            3,
            20,
            12,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Peach bloom is strongly affected by chill completion and frost timing, so the catalog window is best treated as a seasonal expectation, not a guarantee."
        ),
        SpeciesBloomProfile(
            "sweet cherry",
            setOf("sweet cherry", "cherry"),
            "7a",
            3,
            24,
            10,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Most sweet cherries need pollinizers, but self-fertile exceptions and regional chill differences make the generic bloom lane intentionally conservative."
        ),
        SpeciesBloomProfile(
            "sour cherry",
            setOf("sour cherry", "tart cherry"),
            "7a",
            3,
            28,
            10,
            defaultPhase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Sour cherries are commonly self-fruitful, but spring weather and nearby pollinizers can still shift the real crop quality away from the nominal bloom lane."
        ),
        SpeciesBloomProfile(
            "blueberry",
            setOf("blueberry"),
            "7a",
            3,
            29,
            18,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Rabbiteye and highbush bloom overlap is not uniform, so cross-pollination guidance is stronger than the generic species timing."
        ),
        SpeciesBloomProfile(
            "grape",
            setOf("grape", "grapes", "grape vine"),
            "7a",
            5,
            20,
            12,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "The baseline mostly fits bunch grapes, while muscadine and wild-grape fertility behavior can differ enough that the species row stays intentionally broad."
        ),
        SpeciesBloomProfile(
            "kiwiberry",
            setOf(
                "kiwiberry",
                "kiwi berry",
                "hardy kiwi",
                "hardy kiwifruit",
                "baby kiwi",
                "cocktail kiwi",
                "actinidia arguta"
            ),
            "6a",
            5,
            25,
            10,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Most kiwiberry vines need a compatible male pollinizer, while partially self-fertile exceptions like Issai still crop better when cross-pollinated."
        ),
        SpeciesBloomProfile(
            "strawberry",
            setOf("strawberry"),
            "7a",
            4,
            10,
            25,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Cultivated strawberries are self-fertile, but pollinator activity still affects berry fullness enough that the seasonal window should stay coarse."
        ),
        SpeciesBloomProfile(
            "persimmon",
            setOf("persimmon"),
            "7b",
            5,
            15,
            14,
            uncertaintyNote = "American and Oriental persimmons do not share one fertility rule, so the generic persimmon row should stay cautious about both timing and pollination."
        ),
        SpeciesBloomProfile(
            "mango",
            setOf("mango", "mangifera indica"),
            "10b",
            12,
            1,
            150,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Mango usually blooms in one main annual flush, but bloom strength and set still move with weather, pollinator activity, and local cultivar behavior."
        ),
        SpeciesBloomProfile(
            "lychee",
            setOf("lychee"),
            "10a",
            2,
            20,
            20,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Bloom induction depends heavily on local cool-season conditions, and cultivar fruit set can vary even when flowering looks strong."
        ),
        SpeciesBloomProfile(
            "longan",
            setOf(
                "longan",
                "lungan",
                "dragon eye",
                "longana",
                "mamoncillo chino",
                "dimocarpus longan",
                "nephelium longan",
                "euphoria longana"
            ),
            "10b",
            2,
            25,
            70,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Longan flowering depends heavily on cool, dry-season conditions, so the species baseline should stay seasonal but not overly precise."
        ),
        SpeciesBloomProfile(
            "mamoncillo",
            setOf(
                "mamoncillo",
                "genip",
                "ginep",
                "guenepa",
                "guinep",
                "quenepa",
                "quenepe",
                "spanish lime",
                "melicoccus bijugatus",
                "melicocca bijuga"
            ),
            "10b",
            4,
            1,
            61,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Often treated as functionally dioecious in backyard use; avoid assuming a lone tree will crop well."
        ),
        SpeciesBloomProfile(
            "atemoya",
            setOf(
                "atemoya",
                "annona atemoya",
                "annona x atemoya",
                "annona cherimola x annona squamosa",
                "annona squamosa x annona cherimola"
            ),
            "10b",
            4,
            1,
            105,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Natural set can be weak. Hand pollination or strong beetle activity often improves crops."
        ),
        SpeciesBloomProfile(
            "soursop",
            setOf(
                "soursop",
                "guanabana",
                "guanábana",
                "graviola",
                "annona muricata",
                "guayabano",
                "guyabano"
            ),
            "10b",
            4,
            1,
            90,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Soursop flowering is warm-season driven, but pollination detail and flush strength still vary enough that the generic window should stay conservative."
        ),
        SpeciesBloomProfile(
            "abiu",
            setOf(
                "abiu",
                "abio",
                "abieiro",
                "caimito amarillo",
                "caimo",
                "madura verde",
                "luma",
                "pouteria caimito",
                "lucuma caimito",
                "achras caimito"
            ),
            "10b",
            5,
            1,
            92,
            pollinationRequirement = PollinationRequirement.UNKNOWN,
            catalogSpeciesLabel = "Abiu",
            uncertaintyNote = "Florida flowering behavior is documented better than pollination biology; keep the fertility default conservative."
        ),
        SpeciesBloomProfile(
            "ambarella",
            setOf(
                "ambarella",
                "june plum",
                "juneplum",
                "otaheite apple",
                "spondias dulcis",
                "spondias cytherea"
            ),
            "10b",
            4,
            1,
            92,
            pollinationRequirement = PollinationRequirement.UNKNOWN,
            catalogSpeciesLabel = "Ambarella (June plum)",
            uncertaintyNote = "Ambarella seasonality is clearer than its backyard fertility behavior, so the app should keep the pollination default conservative."
        ),
        SpeciesBloomProfile(
            "cashew",
            setOf(
                "cashew",
                "cashew apple",
                "cashewapple",
                "anacardium occidentale"
            ),
            "10b",
            4,
            1,
            60,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            catalogSpeciesLabel = "Cashew (cashew apple)",
            uncertaintyNote = "Cashew bloom timing is usable as a seasonal cue, but cultivar fertility behavior is mixed enough that the app should keep both pollination and countdown precision conservative."
        ),
        SpeciesBloomProfile(
            "caimito",
            setOf(
                "caimito",
                "star apple",
                "starapple",
                "cainito",
                "golden-leaf tree",
                "golden leaf tree",
                "chrysophyllum cainito"
            ),
            "10b",
            8,
            1,
            92,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            catalogSpeciesLabel = "Caimito (star apple)",
            uncertaintyNote = "Some cultivars can set alone, while others benefit from or require cross-pollination."
        ),
        SpeciesBloomProfile(
            "coconut",
            setOf(
                "coconut",
                "coconuts",
                "coconut palm",
                "coconut tree",
                "cocos nucifera"
            ),
            "10b",
            1,
            1,
            365,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            pollinationRequirement = PollinationRequirement.UNKNOWN,
            uncertaintyNote = "Coconut flowering is effectively continuous in warm climates and fertility varies by dwarf, tall, and hybrid types, so exact timing and a single species-wide pollination rule would both be misleading."
        ),
        SpeciesBloomProfile(
            "star fruit",
            setOf("star fruit", "starfruit", "carambola", "averrhoa carambola"),
            "10b",
            4,
            15,
            50,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Star fruit often flowers in repeated flushes, but cultivar fertility and flush strength vary enough that local observations should outrank the baseline."
        ),
        SpeciesBloomProfile(
            "sugar apple",
            setOf("sugar apple", "sweetsop", "sweet sop", "annona squamosa", "sitaphal"),
            "10b",
            3,
            15,
            95,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Natural set can be weak and hand pollination often changes outcomes, so the species window is more useful as a watch period than a precise forecast."
        ),
        SpeciesBloomProfile(
            "jackfruit",
            setOf("jackfruit", "jack fruit", "jack", "artocarpus heterophyllus", "kathal", "panas", "nangka", "nagka"),
            "10b",
            1,
            20,
            55,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Warm-climate bloom can stretch in multiple flushes, and pollination biology is documented less cleanly than the cultivar list."
        ),
        SpeciesBloomProfile(
            "tamarind",
            setOf("tamarind", "tamarindo", "tamarindus indica", "imli", "ambli", "chinch", "sampalok", "makham", "makham waan"),
            "10b",
            5,
            15,
            100,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION,
            uncertaintyNote = "Pollination guidance is stronger than the exact bloom calendar here; warm-season timing can still drift by climate and tree vigor."
        ),
        SpeciesBloomProfile(
            "pineapple",
            setOf("pineapple", "piña", "pina", "ananas", "ananas comosus"),
            "10b",
            2,
            15,
            75,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Pineapple flowering can be induced and staggered by stress or management, so the catalog window is only a broad seasonal cue."
        ),
        SpeciesBloomProfile(
            "barbados cherry",
            setOf(
                "barbados cherry",
                "acerola",
                "west indian cherry",
                "acerola cherry",
                "malpighia emarginata",
                "malpighia glabra",
                "malpighia punicifolia"
            ),
            "10b",
            4,
            1,
            210,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Acerola can flower in repeated warm-season flushes, so the model is more useful for watch windows than exact dates."
        ),
        SpeciesBloomProfile(
            "jamaican cherry",
            setOf(
                "jamaican cherry",
                "jamaica cherry",
                "panama berry",
                "strawberry tree",
                "cotton candy berry",
                "singapore cherry",
                "jam fruit tree",
                "calabura",
                "muntingia calabura"
            ),
            "10b",
            3,
            1,
            240,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            patternType = BloomPatternType.CONTINUOUS,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Jamaican cherry can flower almost continuously in warm climates, so exact countdowns would be fake precision."
        ),
        SpeciesBloomProfile(
            "coffee",
            setOf(
                "coffee",
                "coffee tree",
                "arabica coffee",
                "coffee arabica",
                "coffea arabica"
            ),
            "10b",
            3,
            1,
            200,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "The app models the common Arabica-type home-coffee lane here; bloom often comes in rain-triggered flushes, so local moisture patterns should outrank the generic warm-season watch window."
        ),
        SpeciesBloomProfile(
            "passionfruit",
            setOf(
                "passionfruit",
                "passion fruit",
                "lilikoi",
                "lilikoʻi",
                "maracuja azedo",
                "maracujá azedo",
                "sour passion fruit",
                "passiflora edulis"
            ),
            "10b",
            3,
            15,
            260,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Purple, yellow, and hybrid passionfruit lines do not share one fertility rule, and pruning plus climate can shift repeat flush timing."
        ),
        SpeciesBloomProfile(
            "dragon fruit",
            setOf("dragon fruit", "dragonfruit", "pitaya"),
            "10a",
            6,
            1,
            70,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.WARM_SEASON_PHOTOPERIOD,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Pollination coverage is strong, but cultivar bloom timing still varies by climate and repeat-wave behavior; local history should outrank the catalog window."
        ),
        SpeciesBloomProfile(
            "mamey sapote",
            setOf(
                "mamey sapote",
                "mamey colorado",
                "zapote colorado",
                "zapote mamey",
                "pouteria sapota",
                "calocarpum sapota",
                "calocarpum mammosum",
                "lucuma mammosa"
            ),
            "10b",
            6,
            1,
            275,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Mamey sapote bloom and fruiting cycles are long and climate-sensitive, so local observation history should outrank the generic season."
        ),
        SpeciesBloomProfile(
            "canistel",
            setOf(
                "canistel",
                "eggfruit",
                "egg fruit",
                "egg-fruit",
                "yellow sapote",
                "zapote amarillo",
                "pouteria campechiana",
                "lucuma campechiana",
                "lucuma nervosa",
                "pouteria campechiana var nervosa",
                "pouteria campechiana var palmeri"
            ),
            "10b",
            1,
            15,
            165,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS,
            uncertaintyNote = "Canistel bloom timing is seasonally useful, but set and flowering intensity still vary enough that the baseline should stay coarse."
        ),
        SpeciesBloomProfile(
            "black sapote",
            setOf(
                "black sapote",
                "black persimmon",
                "zapote negro",
                "sapote negro",
                "zapote prieto",
                "diospyros digyna",
                "diospyros nigra",
                "diospyros obtusifolia",
                "sapota nigra"
            ),
            "10b",
            3,
            15,
            170,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Black sapote fertility and flowering behavior vary by cultivar, so both pollination guidance and timing should stay conservative."
        ),
        SpeciesBloomProfile(
            "green sapote",
            setOf(
                "green sapote",
                "pouteria viridis",
                "calocarpum viride",
                "achradelpha viridis",
                "injerto",
                "injerto verde",
                "raxtul",
                "zapote injerto",
                "white faisan",
                "red faisan"
            ),
            "10b",
            2,
            15,
            75,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE,
            uncertaintyNote = "Green sapote is less consistently documented than white or mamey sapote, so the catalog season should be treated as a coarse baseline."
        ),
        SpeciesBloomProfile(
            "white sapote",
            setOf("white sapote", "casimiroa edulis", "zapote blanco", "casimiroa"),
            "10b",
            11,
            15,
            170,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED,
            uncertaintyNote = "Pollination is cultivar-dependent and sources conflict, so the long bloom season should be treated as a coarse watch window."
        ),
        SpeciesBloomProfile(
            "jaboticaba",
            setOf("jaboticaba"),
            "10b",
            3,
            15,
            60,
            patternType = BloomPatternType.MULTI_WAVE,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            uncertaintyNote = "Jaboticaba is better modeled as a repeat bloomer than a single seasonal tree, but species-level fertility guidance is still too thin for a stronger claim."
        ),
        SpeciesBloomProfile(
            "saccharum spp.",
            setOf(
                "sugar cane",
                "sugarcane",
                "cane",
                "sugarcane (cultivated hybrid complex)",
                "saccharum spp",
                "saccharum officinarum"
            ),
            "10b",
            1,
            1,
            30,
            forecastBehavior = BloomForecastBehavior.SUPPRESSED,
            pollinationRequirement = PollinationRequirement.UNKNOWN,
            catalogSpeciesLabel = "Sugarcane (cultivated hybrid complex)",
            uncertaintyNote = "Cultivated sugar cane is usually grown vegetatively and flowering is often suppressed, so bloom forecasting is intentionally de-emphasized here."
        ),
        SpeciesBloomProfile(
            "papaya",
            setOf("papaya", "carica papaya", "mamão", "mamao", "lechosa"),
            "10b",
            4,
            1,
            90,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            patternType = BloomPatternType.CONTINUOUS,
            modelType = PhenologyModelType.TROPICAL_REPEAT,
            pollinationRequirement = PollinationRequirement.UNKNOWN,
            uncertaintyNote = "Sex expression and planting type drive papaya fruit set more than cultivar name alone, so the species model should stay coarse and history-led."
        )
    ) +
        AppleBloomCatalog.speciesProfiles +
        PearBloomCatalog.speciesProfiles +
        CaneberryBloomCatalog.speciesProfiles +
        PomegranateBloomCatalog.speciesProfiles +
        FigBloomCatalog.speciesProfiles +
        MulberryBloomCatalog.speciesProfiles +
        PlumBloomCatalog.speciesProfiles +
        AvocadoBloomCatalog.speciesProfiles +
        StoneFruitBloomCatalog.speciesProfiles +
        KiwiBloomCatalog.speciesProfiles +
        BlueberryBloomCatalog.speciesProfiles +
        BerryBloomCatalog.speciesProfiles +
        BerryMelonBloomCatalog.speciesProfiles +
        MediterraneanBloomCatalog.speciesProfiles +
        NutBloomCatalog.speciesProfiles +
        SpecialtyTreeBloomCatalog.speciesProfiles +
        WarmClimateTreeBloomCatalog.speciesProfiles +
        PawpawBloomCatalog.speciesProfiles +
        BananaBloomCatalog.speciesProfiles +
        CitrusBloomCatalog.speciesProfiles

    // The first catalog is phase-based so it can scale to thousands of cultivars by adding rows,
    // without changing the date engine itself.
    private val baseCultivarProfiles = listOf(
        CultivarBloomProfile("peach", "Early Redhaven", phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "Eva's Pride", phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "Florida Prince", aliases = setOf("Flordaprince", "Florda Prince", "UF Prince", "USF Prince"), phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "UFSun", aliases = setOf("UF Sun"), phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "UFBest", aliases = setOf("UF Best"), phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "Flordabest", aliases = setOf("Florda Best"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "TropicBeauty", aliases = setOf("Tropic Beauty"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "UFGem", aliases = setOf("UF Gem"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "UFGold", aliases = setOf("UF Gold"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "Florida Grande", aliases = setOf("Florda Grande", "UF Grande", "USF Grande"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "UFBeauty", aliases = setOf("UF Beauty"), phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "UFOne", aliases = setOf("UF One"), phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "UFO", phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "Elberta", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "4th of July", aliases = setOf("Fourth of July"), phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "UF2000", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "UFBlaze", aliases = setOf("UF Blaze"), phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "Flordadawn", aliases = setOf("Florida Dawn", "Florda Dawn"), phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "UFGlo", aliases = setOf("UF Glo", "Florida Glo", "Florda Glo"), phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "Belle of Georgia", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "Reliance", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "Gulfking", aliases = setOf("Gulf King"), phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "UFSharp", aliases = setOf("UF Sharp"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Gulfcrimson", aliases = setOf("Gulf Crimson"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Gulfprince", aliases = setOf("Gulf Prince"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Gulfsnow", aliases = setOf("Gulf Snow"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Gulfatlas", aliases = setOf("Gulf Atlas"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Gulfcrest", aliases = setOf("Gulf Crest"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Flordaking", aliases = setOf("Florida King", "Florda King"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Flordacrest", aliases = setOf("Florida Crest", "Florda Crest"), phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "TropicSnow", aliases = setOf("Tropic Snow"), phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "Contender", phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Redhaven", phase = BloomPhase.LATE),
        CultivarBloomProfile(
            "sweet cherry",
            "Black Tartarian",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "sweet cherry",
            "Lapins",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "sweet cherry",
            "Bing",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "sweet cherry",
            "Rainier",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "sweet cherry",
            "Stella",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile("sour cherry", "Montmorency", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile(
            "lychee",
            "Mauritius",
            aliases = setOf("Tai So"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "lychee",
            "Fei Zi Xiao",
            aliases = setOf("Fay Zee Siu", "Feizixiao"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY
        ),
        CultivarBloomProfile(
            "lychee",
            "Sweetheart",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Kohala",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Edau",
            aliases = setOf("Daw"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Biew Kiew",
            aliases = setOf("Beow Keow"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Chompoo I",
            aliases = setOf("Chompoo"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Haew",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "longan",
            "Diamond River",
            aliases = setOf("Petch Sakorn"),
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "mamoncillo",
            "Montgomery",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "mamoncillo",
            "Jose Pabon",
            aliases = setOf("José Pabón"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "mamoncillo",
            "Large",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "atemoya",
            "Gefner",
            aliases = setOf("Geffner"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "atemoya",
            "Page",
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "atemoya",
            "African Pride",
            aliases = setOf("Kaller"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "atemoya",
            "Bradley",
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "atemoya",
            "Pink Mammoth",
            aliases = setOf("Mammoth", "Pinks Mammoth"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "atemoya",
            "Priestly",
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "soursop",
            "Sweet",
            aliases = setOf("Sweet soursop", "Sweet guanabana", "Guanabana dulce"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "soursop",
            "Bennett",
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "soursop",
            "Cuban Fiberless",
            aliases = setOf("Cuban Fibreless", "Guanabana sin fibre"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "soursop",
            "Whitman Fiberless",
            aliases = setOf("Whitman's Fiberless", "Whitman"),
            phase = BloomPhase.MID
        ),
        abiu("Gray"),
        abiu("Z-2", aliases = setOf("Z2")),
        cashew(
            "Gigante / Tardio",
            aliases = setOf("Gigante/Tardio", "Gigante", "Tardio")
        ),
        cashew(
            "Anão / Precoce",
            aliases = setOf("Anao / Precoce", "Anao/Precoce", "Anão/Precoce", "Anao", "Precoce")
        ),
        caimito(
            "Haitian Star",
            aliases = setOf("Haitian Star Apple"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        caimito(
            "Blanco Star",
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        coconut(
            "Jamaican Tall",
            aliases = setOf("Atlantic Tall", "Jamaica Tall"),
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        coconut(
            "Panama Tall",
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        coconut(
            "Malayan Dwarf",
            aliases = setOf("Dwarf Malayan"),
            pollinationRequirement = PollinationRequirement.UNKNOWN
        ),
        coconut("Green Malayan Dwarf", aliases = setOf("Malayan Green Dwarf")),
        coconut(
            "Yellow Malayan Dwarf",
            aliases = setOf("Malayan Yellow Dwarf"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        coconut(
            "Golden Malayan Dwarf",
            aliases = setOf("Malayan Golden Dwarf", "Golden Malayan"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        coconut(
            "Red Malayan Dwarf",
            aliases = setOf("Malayan Red Dwarf"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        coconut("Maypan", pollinationRequirement = PollinationRequirement.UNKNOWN),
        CultivarBloomProfile(
            "star fruit",
            "Arkin",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "star fruit",
            "Golden Star",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "star fruit",
            "Fwang Tung",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "star fruit",
            "Kary",
            aliases = setOf("Kari"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "star fruit",
            "Kajang",
            aliases = setOf("Kaiang"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "star fruit",
            "Sri Kembangan",
            aliases = setOf("Sri Kambangan"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("star fruit", "Lara", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "star fruit",
            "B-10",
            aliases = setOf("B10"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "star fruit",
            "B-17",
            aliases = setOf("B17", "Belimbing Madu"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "star fruit",
            "B-16",
            aliases = setOf("B16"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "star fruit",
            "B-2",
            aliases = setOf("B2"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "star fruit",
            "B-1",
            aliases = setOf("B1"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "star fruit",
            "B-11",
            aliases = setOf("B11", "Chan Yong I"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "star fruit",
            "Hew-1",
            aliases = setOf("Hew #1"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("star fruit", "Demak", phase = BloomPhase.MID),
        CultivarBloomProfile("star fruit", "Dah Pon", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "star fruit",
            "Tean Ma",
            aliases = setOf("Team Ma", "Tean-Ma"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile(
            "star fruit",
            "Mih Tao",
            aliases = setOf("Mei Tao", "Mih-Tao"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY
        ),
        CultivarBloomProfile(
            "star fruit",
            "Cheng Chui",
            aliases = setOf("Cheng Tsey", "Chun Choi"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("star fruit", "Newcomb", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "star fruit",
            "Star King",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        CultivarBloomProfile("star fruit", "Thayer", phase = BloomPhase.MID),
        CultivarBloomProfile("star fruit", "Maha", phase = BloomPhase.MID),
        CultivarBloomProfile("star fruit", "Thai Knight", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "star fruit",
            "Wheeler",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "sugar apple",
            "Lessard Thai",
            aliases = setOf("Thai Lessard"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Kampong Mauve", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Purple or Red", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "Cuban Seedless",
            aliases = setOf("Seedless Cuban"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "sugar apple",
            "Brazilian Seedless",
            aliases = setOf("Brazilian seedless"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile("sugar apple", "Thai Seedless", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "LeahReese", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Na Dai", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "Balanagar",
            aliases = setOf("Balangar"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Kakarlapahad", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Washington", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "Mammoth",
            aliases = setOf("A squamosa var mammoth"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Red", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "Red-speckled",
            aliases = setOf("Red Speckled"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Crimson", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Yellow", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "White-stemmed",
            aliases = setOf("White Stemmed"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Barbados", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "British Guiana", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Red Sitaphal", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Raidurg", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "APK-1",
            aliases = setOf("APK1"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Barbados Seedlings", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Washington 07005", phase = BloomPhase.MID),
        CultivarBloomProfile("sugar apple", "Washington 98797", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "NMK-1 Golden",
            aliases = setOf("NMK-1"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("sugar apple", "Beni Mazar", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "sugar apple",
            "Abd El Razik",
            aliases = setOf("Abd E1 Razik"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "Black Gold", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Dang Rasimi", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "Golden Nugget",
            aliases = setOf("Gold Nugget"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "Honey Gold", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "J-30", aliases = setOf("J30"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "J-31", aliases = setOf("J31"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "NS1", aliases = setOf("NS-1"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Tabouey", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Cochin", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "Chompa Gob",
            aliases = setOf("Chompa Grob", "Champa Gob"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "Kun Wi Chan", aliases = setOf("Thai Globe"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Lemon Gold", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "Golden Pillow",
            aliases = setOf("Mong Tong"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "Fairchild First", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Sweet Fairchild", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Mia 1", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Leung Bang", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Bosworth No. 3", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Galaxy", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Alba", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Hew", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "N.A.N.S.I.", aliases = setOf("Nansi"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Reliance", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Tree Farm", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Yullatin", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Ziemen", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Velipala", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "Singapore",
            aliases = setOf("Ceylon Jack", "Singapore Jack"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "Panruti Selection", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Thanjavur Jack", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Burliar 1", aliases = setOf("Burliar-1"), phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "PLR 1",
            aliases = setOf("PLR.1", "Palur 1", "Palur.1 Jack"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("jackfruit", "PPI 1", aliases = setOf("PPI.1"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "PLR (J) 2", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Gulabi", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Rudrakshi", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Hazari", phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Champa", aliases = setOf("Champaka", "Champa Jack"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Siddu", aliases = setOf("Siddujack"), phase = BloomPhase.MID),
        CultivarBloomProfile("jackfruit", "Shankara", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jackfruit",
            "Muttom Varikka",
            aliases = setOf("Muttam Varikka", "Muttomvarikka"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "jackfruit",
            "Sindhoor",
            aliases = setOf("Sindoor", "Sindhoora Varikka"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Manila Sweet", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "Makham Waan", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "tamarind",
            "PKM-1",
            aliases = setOf("PKM 1", "Periyakulam 1"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Urigam", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "tamarind",
            "Prathisthan",
            aliases = setOf("Pratishthan"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Goma Prateek", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "Ajanta", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "tamarind",
            "T-263",
            aliases = setOf("Tamarind 263", "T 263"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Hasanur", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "Tumkur", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "DTS-1", aliases = setOf("DTS 1"), phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "Yogeshwari", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "tamarind",
            "Sichomphu",
            aliases = setOf("Si Chomphu", "Sri Chompoo", "Si Chompoo"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Khandee",
            aliases = setOf("Khantee", "Kantee"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Prakai Thong",
            aliases = setOf("Prakaithong"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Fak Dap", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "Wan Lon", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "tamarind",
            "Sithong",
            aliases = setOf("Si Thong"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Sithong Bao",
            aliases = setOf("Si Thong Bao"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Nam Phueng",
            aliases = setOf("Nam Pheung", "Namphueng"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Inthaphalam",
            aliases = setOf("Intapalum", "Intapalam"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Mun Jong",
            aliases = setOf("Muen Chong", "Munjong"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "tamarind",
            "Saeng Athit",
            aliases = setOf("Saengathit"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("tamarind", "Aglibut Sweet", phase = BloomPhase.MID),
        CultivarBloomProfile("tamarind", "PSAU Sour 2", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Smooth Cayenne", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Kew", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Giant Kew", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Red Spanish", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Green Spanish", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Singapore Spanish", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Queen", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Natal Queen", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Ripley Queen", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "MacGregor", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "pineapple",
            "Victoria",
            aliases = setOf("Queen Victoria"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "pineapple",
            "Mauritius",
            aliases = setOf("Red Ceylon", "Moris", "Morris"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "pineapple",
            "Sugarloaf",
            aliases = setOf("White Sugarloaf", "Kona Sugar Loaf", "Kona Sugarloaf"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("pineapple", "Pernambuco", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Perolera", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Manzana", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "pineapple",
            "MD-2",
            aliases = setOf("MD2", "Del Monte Gold", "Gold Extra Sweet", "Extra Sweet"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("pineapple", "N36", aliases = setOf("N-36"), phase = BloomPhase.MID),
        CultivarBloomProfile(
            "pineapple",
            "Josapine",
            aliases = setOf("Josephine", "Josaphine"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("pineapple", "Gandul", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "pineapple",
            "BRS Imperial",
            aliases = setOf("Imperial"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "pineapple",
            "BRS Vitória",
            aliases = setOf("BRS Vitoria"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("pineapple", "Tainung 17", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Tainung 21", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "N67-10", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Soft Touch", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "Gold Barrel", phase = BloomPhase.MID),
        CultivarBloomProfile("pineapple", "White Jade", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "pineapple",
            "Amritha",
            aliases = setOf("Amrutha"),
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile("barbados cherry", "Florida Sweet", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "B-17", aliases = setOf("B17"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "J.H. Beaumont", aliases = setOf("Beaumont"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "F. Haley", aliases = setOf("Haley"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Hawaiian Queen", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Maunawili", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Tropical Ruby", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "C.F. Rehnborg", aliases = setOf("Rehnborg"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Manoa Sweet", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Red Jumbo", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Flor Branca", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Junko", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS Sertaneja", aliases = setOf("Sertaneja"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Costa Rica", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Okinawa", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Nikki", phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Coopama Nº 1", aliases = setOf("Coopama No. 1"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS Cabocla", aliases = setOf("Cabocla"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS 235 Apodi", aliases = setOf("Apodi"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS 236 Cereja", aliases = setOf("Cereja"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS 237 Roxinha", aliases = setOf("Roxinha"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS 238 Frutacor", aliases = setOf("Frutacor"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "BRS 366 Jaburu", aliases = setOf("Jaburu"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "Rubra", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "barbados cherry",
            "Olivier",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile("barbados cherry", "Waldy-CATI 30", aliases = setOf("Waldy CATI 30"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "UEL 3 - Dominga", aliases = setOf("UEL3 Dominga"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "UEL 4 - Ligia", aliases = setOf("UEL4 Ligia"), phase = BloomPhase.MID),
        CultivarBloomProfile("barbados cherry", "UEL 5 - Natalia", aliases = setOf("UEL5 Natalia"), phase = BloomPhase.MID),
        CultivarBloomProfile(
            "jamaican cherry",
            "Standard red-fruited type",
            phase = BloomPhase.MID
        ),
        CultivarBloomProfile(
            "jamaican cherry",
            "Yellow-fruited form",
            aliases = setOf(
                "Yellow Jamaica Cherry",
                "Yellow Jamaican Cherry",
                "Yellow Panama Berry",
                "Yellow Strawberry Tree"
            ),
            phase = BloomPhase.MID
        ),
        passionFruit(
            "Possum Purple",
            aliases = setOf("Purple Possum", "Purple Possom", "Possom Purple"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        kiwiberry(
            "Issai",
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        kiwiberry(
            "Ananasnaya",
            aliases = setOf("Anna"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        kiwiberry(
            "Ken's Red",
            aliases = setOf("Kens Red"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        kiwiberry(
            "Jumbo",
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        kiwiberry(
            "Krupnoplodnaya",
            aliases = setOf("Krupnopladnaya"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("Panama Red", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        passionFruit("Sweet Sunrise", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit(
            "Whitman Yellow",
            aliases = setOf("Whitman"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("Australian Purple", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Nellie Kelly", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Don's Choice", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Frederick", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Misty Gem", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Sweetheart", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Tango", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit(
            "Flamenco",
            aliases = setOf("Red Flamenco", "Red Flemenco"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        passionFruit("Red Rover", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Tas Black", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit(
            "Toms Special",
            aliases = setOf("Tom's Special"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        passionFruit("Waimanalo Selection", pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS),
        passionFruit("Kapoho Selection", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("Mike's Choice", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("Panama Gold", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("Sevcik Selection", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("University Round Selection", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit(
            "University Selection No. B-74",
            aliases = setOf("B-74"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("Yee Selection", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("Pandora", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit(
            "McGuffies Red",
            aliases = setOf("McGuffy"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "Noel's Special",
            aliases = setOf("Noels Special"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("IAC-Paulista", pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        passionFruit(
            "BRS Gigante Amarelo",
            aliases = setOf("BRS GA1"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "BRS Sol do Cerrado",
            aliases = setOf("BRS SC1"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "BRS Rubi do Cerrado",
            aliases = setOf("BRS RC"),
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        passionFruit(
            "BRS Ouro Vermelho",
            aliases = setOf("BRS OV1"),
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        passionFruit(
            "IAC 275 - Wonder",
            aliases = setOf("IAC 275 Wonder"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "IAC 273 - Monte Alegre",
            aliases = setOf("IAC 273 Monte Alegre"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "IAC 277 - Jewelry",
            aliases = setOf("IAC 277 Jewelry"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("FB 200 Yellow Master", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit("FB 300 Araguari", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit(
            "SCS437 Catarina",
            aliases = setOf("SCS437 Catherine"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit(
            "UENF Rio Dourado",
            aliases = setOf("UENF Golden River"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        passionFruit("Solar", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        passionFruit(
            "Round Yellow",
            aliases = setOf("Redondo Amarelo"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        papaya("Kapoho Solo", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya(
            "Sunrise Solo",
            aliases = setOf("Sunrise"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        papaya(
            "Improved Sunrise Solo 72/12",
            aliases = setOf("Sunrise Solo 72/12"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        papaya("Waimanalo", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Sunset", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Rainbow", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("SunUp", aliases = setOf("UH SunUp"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya(
            "Tainung No. 1",
            aliases = setOf("Tainung 1", "Tainung No.1"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        papaya(
            "Tainung No. 2",
            aliases = setOf("Tainung 2", "Tainung No.2"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        papaya(
            "Known You No. 1",
            aliases = setOf("Known You 1", "Known-You 1")
        ),
        papaya(
            "Known You No. 2",
            aliases = setOf("Known You 2", "Known-You 2")
        ),
        papaya(
            "Red Lady 786",
            aliases = setOf("Red Lady", "786"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        papaya("Maradol", aliases = setOf("Maradol Roja")),
        papaya("Caribbean Red"),
        papaya("Eksotika", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Pusa Delicious", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Pusa Majesty", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Pusa Nanha", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("Pusa Dwarf", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("Ranchi Dwarf", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("Washington", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("Honey Dew", aliases = setOf("Honeydew"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Coorg Honey Dew", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("CO.2", aliases = setOf("CO 2"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("CO.3", aliases = setOf("CO 3"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("CO.5", aliases = setOf("CO 5"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("CO.6", aliases = setOf("CO 6"), pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        papaya("CO.7", aliases = setOf("CO 7"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("CO.8", aliases = setOf("CO 8"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Arka Surya", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Arka Prabhath", aliases = setOf("Arka Prabhat"), pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        papaya("Golden"),
        papaya("Calimosa"),
        papaya("UENF/Caliman 01", aliases = setOf("Caliman 01", "UENF-Caliman 01")),
        mameySapote("Pantin", aliases = setOf("Key West")),
        mameySapote("Magana", aliases = setOf("Magaña")),
        mameySapote("Pace"),
        mameySapote("Tazumal"),
        mameySapote("Mayapan", aliases = setOf("AREC No. 2")),
        mameySapote("Copan", aliases = setOf("AREC No. 1")),
        mameySapote("Lara"),
        mameySapote("Florida"),
        mameySapote("Piloto"),
        mameySapote("Chenox"),
        mameySapote("Abuelo"),
        mameySapote("Francisco Fernandez", aliases = setOf("Francisco Fernancez")),
        mameySapote("Flores"),
        mameySapote("Viejo"),
        mameySapote("Lorito"),
        mameySapote("Cepeda Especial", aliases = setOf("Cepeda Special")),
        mameySapote("Akil Especial", aliases = setOf("Akil Special")),
        mameySapote("AREC No. 3"),
        canistel("Bruce"),
        canistel("Fairchild #1", aliases = setOf("Fairchild 1")),
        canistel("Fairchild #2", aliases = setOf("Fairchild 2")),
        canistel("Fitzpatrick"),
        canistel("Keisau"),
        canistel("Oro"),
        canistel("Trompo"),
        canistel("TREC 9680", aliases = setOf("TREC9680")),
        canistel("TREC 9681", aliases = setOf("TREC9681")),
        blackSapote("Merida", aliases = setOf("Reineke", "Reinecke")),
        blackSapote("Bernicker", aliases = setOf("Bernecker")),
        blackSapote("Mossman"),
        blackSapote("Maher"),
        blackSapote("Ricks Late", aliases = setOf("Rick's Late")),
        blackSapote("Superb"),
        blackSapote("Cocktail"),
        greenSapote("UF/TREC selection"),
        greenSapote("Whitman"),
        whiteSapote(
            "Blumenthal",
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        whiteSapote("Dade"),
        whiteSapote("Denzler", aliases = setOf("Densler")),
        whiteSapote("Golden", aliases = setOf("Max Golden")),
        whiteSapote("Homestead"),
        whiteSapote(
            "Lemon Gold",
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        whiteSapote("McDill"),
        whiteSapote("Pike"),
        whiteSapote(
            "Reinecke Commercial",
            aliases = setOf("Reinekie", "Reinikie", "Reinikie Commercial", "Reineke Commercial"),
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        whiteSapote("Smathers"),
        whiteSapote(
            "Suebelle",
            aliases = setOf("Hubbell"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        whiteSapote("Vernon", pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        whiteSapote("Yellow", pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION),
        sugarCaneGroup("Chewing cane"),
        sugarCaneGroup("Syrup cane"),
        sugarCaneGroup(
            "Crystal / commercial cane",
            aliases = setOf("Commercial cane", "Crystal cane")
        ),
        sugarCane("Yellow Gal", aliases = setOf("F31-407", "F 31-407")),
        sugarCane("White Transparent"),
        sugarCane("Georgia Red"),
        sugarCane("Home Green"),
        sugarCane("Louisiana Ribbon"),
        sugarCane("Louisiana Purple"),
        sugarCane("Louisiana Striped"),
        sugarCane("Green German"),
        sugarCane("CP 96-1252", aliases = setOf("CP96-1252")),
        sugarCane("CP 01-1372", aliases = setOf("CP01-1372")),
        sugarCane("CP 00-1101", aliases = setOf("CP00-1101")),
        sugarCane("CP 89-2143", aliases = setOf("CP89-2143"))
    ) +
        AppleBloomCatalog.cultivarProfiles +
        PearBloomCatalog.cultivarProfiles +
        CaneberryBloomCatalog.cultivarProfiles +
        PomegranateBloomCatalog.cultivarProfiles +
        FigBloomCatalog.cultivarProfiles +
        MulberryBloomCatalog.cultivarProfiles +
        PlumBloomCatalog.cultivarProfiles +
        AvocadoBloomCatalog.cultivarProfiles +
        StoneFruitBloomCatalog.cultivarProfiles +
        KiwiBloomCatalog.cultivarProfiles +
        BlueberryBloomCatalog.cultivarProfiles +
        BerryBloomCatalog.cultivarProfiles +
        BerryMelonBloomCatalog.cultivarProfiles +
        MediterraneanBloomCatalog.cultivarProfiles +
        NutBloomCatalog.cultivarProfiles +
        SpecialtyTreeBloomCatalog.cultivarProfiles +
        WarmClimateTreeBloomCatalog.cultivarProfiles +
        PawpawBloomCatalog.cultivarProfiles +
        DragonFruitCatalog.cultivarProfiles +
        BananaBloomCatalog.cultivarProfiles +
        CitrusBloomCatalog.cultivarProfiles

    private val speciesProfiles: List<SpeciesBloomProfile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val assetProfiles = PhenologyCatalogAssets.speciesProfiles()
        val baseProfiles = if (assetProfiles.isNotEmpty()) assetProfiles else baseSpeciesProfiles
        mergeSpeciesProfiles(
            base = baseProfiles,
            overlay = emptyList(),
            pollinationOverrides = PhenologyCatalogAssets.speciesPollinationOverrides()
        )
    }

    private val cultivarProfiles: List<CultivarBloomProfile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val assetProfiles = PhenologyCatalogAssets.cultivarProfiles()
        val baseProfiles = if (assetProfiles.isNotEmpty()) assetProfiles else baseCultivarProfiles
        mergeCultivarProfiles(
            base = baseProfiles,
            overlay = emptyList(),
            pollinationOverrides = PhenologyCatalogAssets.cultivarPollinationOverrides()
        )
    }

    private val baseCatalogOnlyCultivars = listOf(
        mangoCultivar("Rosigold"),
        mangoCultivar("Angie"),
        mangoCultivar("Florigon"),
        mangoCultivar("Saigon"),
        mangoCultivar("Zill"),
        mangoCultivar("Edward"),
        mangoCultivar("Vallenato"),
        mangoCultivar("Cogshall"),
        mangoCultivar("Glenn"),
        mangoCultivar("Nam Doc Mai"),
        mangoCultivar("Haden"),
        mangoCultivar("Irwin"),
        mangoCultivar("Carrie"),
        mangoCultivar("Julie"),
        mangoCultivar("Van Dyke"),
        mangoCultivar("Tommy Atkins"),
        mangoCultivar("Lippens"),
        mangoCultivar("Mallika"),
        mangoCultivar("Phimsen Mun"),
        mangoCultivar("Graham"),
        mangoCultivar("Dot"),
        mangoCultivar("Parvin"),
        mangoCultivar("Duncan"),
        mangoCultivar("Ruby"),
        mangoCultivar("Kent"),
        mangoCultivar("Palmer"),
        mangoCultivar("Valencia Pride"),
        mangoCultivar("Sensation"),
        mangoCultivar("Rapoza"),
        mangoCultivar("Carabao"),
        mangoCultivar("Fairchild"),
        mangoCultivar("Kyo Savoy"),
        mangoCultivar("Ice Cream"),
        mangoCultivar("Keitt"),
        mangoCultivar("Neelum"),
        mangoCultivar("Sweet Tart"),
        mangoCultivar("Pickering"),
        mangoCultivar("Coconut Cream"),
        mangoCultivar("Lemon Zest"),
        mangoCultivar("Fruit Punch"),
        mangoCultivar("Orange Sherbet"),
        mangoCultivar("Maha Chanok"),
        mangoCultivar("Beverly"),
        mangoCultivar("Bailey's Marvel"),
        mangoCultivar("Honey Kiss"),
        mangoCultivar("Pina Colada"),
        mangoCultivar("Venus"),
        mangoCultivar("M-4"),
        mangoCultivar("Okrung"),
        mangoCultivar(
            "Ataulfo",
            aliases = setOf("Honey", "Honey mango", "Champagne", "Champagne mango")
        ),
        mangoCultivar("Southern Blush"),
        coffeeCultivar("Typica", aliases = setOf("Kona Typica")),
        coffeeCultivar("Bourbon"),
        coffeeCultivar("Caturra"),
        coffeeCultivar("Yellow Caturra"),
        coffeeCultivar("Catuai", aliases = setOf("Catuaí")),
        coffeeCultivar("Red Catuai", aliases = setOf("Red Catuaí")),
        coffeeCultivar("Yellow Catuai", aliases = setOf("Yellow Catuaí")),
        coffeeCultivar("Mundo Novo"),
        coffeeCultivar("Geisha", aliases = setOf("Gesha")),
        coffeeCultivar("Pacas"),
        coffeeCultivar("Villa Sarchi"),
        grapeCultivar("Concord"),
        grapeCultivar("Niagara"),
        grapeCultivar("Himrod"),
        grapeCultivar("Interlaken"),
        grapeCultivar("Canadice"),
        grapeCultivar("Reliance"),
        grapeCultivar("Vanessa"),
        grapeCultivar("Jupiter"),
        grapeCultivar("Mars"),
        grapeCultivar("Price"),
        grapeCultivar("New York Muscat", aliases = setOf("NY Muscat")),
        grapeCultivar("Lakemont"),
        grapeCultivar("Edelweiss"),
        grapeCultivar("Marquette"),
        grapeCultivar("Frontenac"),
        grapeCultivar("La Crescent"),
        grapeCultivar("Swenson Red"),
        jaboticabaCultivar("Sabara", aliases = setOf("Sabara", "Sabará")),
        jaboticabaCultivar("Paulista"),
        jaboticabaCultivar("Grimal"),
        jaboticabaCultivar("Red Hybrid"),
        jaboticabaCultivar("Escarlate", aliases = setOf("Scarlet")),
        jaboticabaCultivar("Otto Andersen", aliases = setOf("Otto Anderson")),
        jaboticabaCultivar("Esalq", aliases = setOf("ESALQ")),
        jaboticabaCultivar("Pingo de Mel", aliases = setOf("Honey Drop")),
        jaboticabaCultivar("White Jaboticaba", aliases = setOf("Branca"))
    )

    private val cultivarCatalogOnlyOptions: List<CultivarAutocompleteOption> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val assetOptions = PhenologyCatalogAssets.catalogOnlyCultivars()
        if (assetOptions.isNotEmpty()) assetOptions else baseCatalogOnlyCultivars
    }

    private fun mangoCultivar(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarAutocompleteOption(
        species = "Mango",
        cultivar = cultivar,
        aliases = aliases
            .filterNot { normalize(it) == normalize(cultivar) }
            .sortedBy(String::lowercase),
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )

    private fun coffeeCultivar(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarAutocompleteOption(
        species = "Coffee",
        cultivar = cultivar,
        aliases = aliases
            .filterNot { normalize(it) == normalize(cultivar) }
            .sortedBy(String::lowercase),
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )

    private fun grapeCultivar(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarAutocompleteOption(
        species = "Grape",
        cultivar = cultivar,
        aliases = aliases
            .filterNot { normalize(it) == normalize(cultivar) }
            .sortedBy(String::lowercase),
        pollinationRequirement = PollinationRequirement.SELF_FERTILE
    )

    private fun jaboticabaCultivar(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarAutocompleteOption(
        species = "Jaboticaba",
        cultivar = cultivar,
        aliases = aliases
            .filterNot { normalize(it) == normalize(cultivar) }
            .sortedBy(String::lowercase)
    )

    private fun kiwiberry(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "kiwiberry",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun passionFruit(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "passionfruit",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Passion Fruit",
        pollinationRequirement = pollinationRequirement
    )

    private fun papaya(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "papaya",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun mameySapote(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "mamey sapote",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun canistel(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "canistel",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun blackSapote(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "black sapote",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun greenSapote(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "green sapote",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun whiteSapote(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "white sapote",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        pollinationRequirement = pollinationRequirement
    )

    private fun abiu(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "abiu",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Abiu",
        pollinationRequirement = pollinationRequirement
    )

    private fun caimito(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "caimito",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Caimito (star apple)",
        pollinationRequirement = pollinationRequirement
    )

    private fun cashew(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "cashew",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Cashew (cashew apple)",
        pollinationRequirement = pollinationRequirement
    )

    private fun coconut(
        cultivar: String,
        aliases: Set<String> = emptySet(),
        pollinationRequirement: PollinationRequirement? = null
    ) = CultivarBloomProfile(
        speciesKey = "coconut",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Coconut",
        pollinationRequirement = pollinationRequirement
    )

    private fun sugarCaneGroup(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarBloomProfile(
        speciesKey = "saccharum spp.",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Sugarcane (cultivated hybrid complex)"
    )

    private fun sugarCane(
        cultivar: String,
        aliases: Set<String> = emptySet()
    ) = CultivarBloomProfile(
        speciesKey = "saccharum spp.",
        cultivar = cultivar,
        aliases = aliases,
        phase = BloomPhase.MID,
        catalogSpeciesLabel = "Sugarcane (cultivated hybrid complex)"
    )

    private val regionalBloomOverrides = listOf(
        RegionalBloomOverride(
            speciesKey = "lychee",
            orchardRegion = OrchardRegion.SOUTH_FLORIDA,
            referenceZoneCode = "10b",
            startMonth = 2,
            startDay = 5,
            durationDays = 55
        ),
        RegionalBloomOverride(
            speciesKey = "lychee",
            orchardRegion = OrchardRegion.HAWAII,
            referenceZoneCode = "11a",
            startMonth = 2,
            startDay = 1,
            durationDays = 65
        ),
        RegionalBloomOverride(
            speciesKey = "lychee",
            orchardRegion = OrchardRegion.CALIFORNIA,
            referenceZoneCode = "10a",
            startMonth = 3,
            startDay = 15,
            durationDays = 45
        )
    )

    private data class ProfileMatch(
        val profile: SpeciesBloomProfile,
        val phase: BloomPhase,
        val cultivarMatched: Boolean
    )

    private data class ScoredCultivarOption(
        val option: CultivarAutocompleteOption,
        val score: Int,
        val speciesScore: Int
    )

    private data class SpeciesMatchContext(
        val normalizedQuery: String,
        val queryProfile: SpeciesBloomProfile?
    )

    private data class ScoredSpeciesOption(
        val option: SpeciesAutocompleteOption,
        val score: Int
    )

    private fun mergeSpeciesProfiles(
        base: List<SpeciesBloomProfile>,
        overlay: List<SpeciesBloomProfile>,
        pollinationOverrides: Map<String, PollinationRequirement>
    ): List<SpeciesBloomProfile> {
        val mergedByKey = linkedMapOf<String, SpeciesBloomProfile>()
        base.forEach { profile -> mergedByKey[profile.key] = profile }
        overlay.forEach { profile -> mergedByKey[profile.key] = profile }
        return mergedByKey.values.map { profile ->
            pollinationOverrides.entries
                .firstOrNull { (speciesKey, _) -> normalize(speciesKey) == normalize(profile.key) }
                ?.value
                ?.let { overrideRequirement -> profile.copy(pollinationRequirement = overrideRequirement) }
                ?: profile
        }
    }

    private fun mergeCultivarProfiles(
        base: List<CultivarBloomProfile>,
        overlay: List<CultivarBloomProfile>,
        pollinationOverrides: Map<Pair<String, String>, PollinationRequirement>
    ): List<CultivarBloomProfile> {
        val mergedByKey = linkedMapOf<String, CultivarBloomProfile>()
        base.forEach { profile ->
            mergedByKey[normalize("${profile.speciesKey}|${profile.cultivar}")] = profile
        }
        overlay.forEach { profile ->
            mergedByKey[normalize("${profile.speciesKey}|${profile.cultivar}")] = profile
        }
        return mergedByKey.values.map { profile ->
            pollinationOverrides.entries
                .firstOrNull { (key, _) ->
                    normalize(key.first) == normalize(profile.speciesKey) &&
                        normalize(key.second) == normalize(profile.cultivar)
                }
                ?.value
                ?.let { overrideRequirement -> profile.copy(pollinationRequirement = overrideRequirement) }
                ?: profile
        }
    }

    private val citrusSpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(CitrusBloomCatalog.subgroupSpeciesKeys)
    }

    private val pearSpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(PearBloomCatalog.subgroupSpeciesKeys)
    }

    private val raspberrySpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(CaneberryBloomCatalog.subgroupSpeciesKeys)
    }

    private val blueberrySpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(BlueberryBloomCatalog.subgroupSpeciesKeys)
    }

    private val plumSpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(PlumBloomCatalog.subgroupSpeciesKeys)
    }

    private val mulberrySpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(MulberryBloomCatalog.subgroupSpeciesKeys)
    }

    private val figSpeciesKeys: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map(SpeciesBloomProfile::key)
            .toSet()
            .intersect(FigBloomCatalog.subgroupSpeciesKeys)
    }

    private val speciesByKey: Map<String, SpeciesBloomProfile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles.associateBy(SpeciesBloomProfile::key)
    }

    private val speciesByAlias: Map<String, SpeciesBloomProfile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles.flatMap { profile ->
            (profile.aliases + profile.key + profile.catalogSpeciesLabel.toCatalogDisplayLabel())
                .map { alias -> normalize(alias) to profile }
        }.toMap()
    }

    private val speciesAutocompleteCatalog: List<SpeciesAutocompleteOption> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        speciesProfiles
            .map { profile ->
                val species = profile.catalogSpeciesLabel.toCatalogDisplayLabel()
                SpeciesAutocompleteOption(
                    species = species,
                    aliases = (profile.aliases + profile.key + profile.catalogSpeciesLabel)
                        .filterNot { normalize(it) == normalize(species) }
                        .distinctBy(::normalize)
                        .sortedBy(String::lowercase)
                )
            }
            .distinctBy { option -> normalize(option.species) }
    }

    private val cultivarAutocompleteCatalog: List<CultivarAutocompleteOption> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        (
            cultivarProfiles.map { profile ->
                val speciesPollination = speciesByKey[profile.speciesKey]
                    ?.pollinationRequirement
                    ?.takeUnless { it == PollinationRequirement.UNKNOWN }
                CultivarAutocompleteOption(
                    species = profile.catalogSpeciesLabel.toCatalogDisplayLabel(),
                    cultivar = profile.cultivar,
                    aliases = profile.aliases
                        .filterNot { normalize(it) == normalize(profile.cultivar) }
                        .sortedBy(String::lowercase),
                    pollinationRequirement = profile.pollinationRequirement ?: speciesPollination
                )
            } + cultivarCatalogOnlyOptions
            )
            .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }
    }

    private val cultivarsBySpeciesLabel: Map<String, List<CultivarAutocompleteOption>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        cultivarAutocompleteCatalog
            .groupBy { normalize(it.species) }
            .mapValues { (_, options) ->
                options.sortedWith(compareBy(CultivarAutocompleteOption::cultivar, CultivarAutocompleteOption::species))
            }
    }

    fun supportedZoneLabels(): List<String> = UsdaZoneCatalog.zones.map(UsdaZoneDefinition::label)

    fun speciesAutocompleteOptions(
        query: String,
        limit: Int = 8
    ): List<String> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()
        return speciesAutocompleteCatalog
            .mapNotNull { option ->
                val score = sequenceOf(option.species, *option.aliases.toTypedArray())
                    .map { matchScore(normalizedQuery, normalize(it)) }
                    .filterNotNull()
                    .maxOrNull()
                    ?: return@mapNotNull null
                ScoredSpeciesOption(option = option, score = score)
            }
            .sortedWith(
                compareByDescending<ScoredSpeciesOption> { it.score }
                    .thenBy { it.option.species.lowercase() }
            )
            .map { it.option.species }
            .distinctBy(::normalize)
            .take(limit)
    }

    fun resolveSpeciesAutocomplete(query: String): String? {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return null
        val exactMatches = speciesProfiles.filter { profile ->
            normalize(profile.catalogSpeciesLabel.toCatalogDisplayLabel()) == normalizedQuery ||
                normalize(profile.key) == normalizedQuery ||
                profile.aliases.any { normalize(it) == normalizedQuery }
        }
        if (exactMatches.isEmpty()) return null
        val resolvedSpecies = exactMatches
            .map { it.catalogSpeciesLabel.toCatalogDisplayLabel() }
            .distinctBy(::normalize)
            .singleOrNull()
            ?: return null
        val resolvedProfile = exactMatches.first()
        val exactCitrusLabel = normalizedQuery == normalize(resolvedProfile.key) ||
            normalizedQuery == normalize(resolvedProfile.catalogSpeciesLabel.toCatalogDisplayLabel())
        if (exactCitrusLabel && (resolvedProfile.key == "citrus" || resolvedProfile.key in citrusSpeciesKeys)) {
            return null
        }
        return resolvedSpecies
    }

    fun zoneCodeFromLabel(label: String): String = UsdaZoneCatalog.zones
        .firstOrNull { it.label == label }
        ?.code
        ?: label.substringBefore(' ').trim().lowercase()

    fun zoneLabelForCode(code: String?): String = UsdaZoneCatalog.resolve(code).label

    fun effectiveZoneCode(code: String?): String = UsdaZoneCatalog.resolve(code).code

    fun supportedSpeciesCatalog(): List<String> = (
        speciesAutocompleteCatalog.map(SpeciesAutocompleteOption::species) + cultivarAutocompleteCatalog.map(CultivarAutocompleteOption::species)
        )
        .distinctBy(::normalize)
        .sortedBy(String::lowercase)

    fun supportedCultivarCatalog(): List<SupportedCultivarCatalogEntry> = cultivarAutocompleteCatalog
        .map {
            SupportedCultivarCatalogEntry(
                species = it.species,
                cultivar = it.cultivar,
                aliases = it.aliases,
                pollinationRequirement = it.pollinationRequirement
            )
        }
        .sortedWith(compareBy({ it.species.lowercase() }, { it.cultivar.lowercase() }))

    internal fun catalogSpeciesProfilesForExport(): List<SpeciesBloomProfile> = baseSpeciesProfiles

    internal fun catalogCultivarProfilesForExport(): List<CultivarBloomProfile> = baseCultivarProfiles

    internal fun catalogOnlyCultivarsForExport(): List<CultivarAutocompleteOption> = baseCatalogOnlyCultivars

    fun supportedSpeciesReferenceCatalog(): List<CatalogSpeciesReferenceEntry> = speciesProfiles
        .map { profile ->
            val speciesLabel = profile.catalogSpeciesLabel.toCatalogDisplayLabel()
            CatalogSpeciesReferenceEntry(
                species = speciesLabel,
                aliases = (profile.aliases + profile.key + profile.catalogSpeciesLabel)
                    .filterNot { normalize(it) == normalize(speciesLabel) }
                    .distinctBy(::normalize)
                    .sortedBy(String::lowercase),
                referenceBloomTimingLabel = profile.catalogUsdaBloomTimingLabel(),
                zoneBloomTimings = profile.catalogZoneBloomTimingEntries(),
                fertilityLabel = profile.pollinationRequirement.label,
                cultivars = cultivarAutocompleteCatalog
                    .filter { normalize(it.species) == normalize(speciesLabel) }
                    .sortedBy(CultivarAutocompleteOption::cultivar)
                    .map { cultivar ->
                        CatalogCultivarReferenceEntry(
                            cultivar = cultivar.cultivar,
                            aliases = cultivar.aliases.sortedBy(String::lowercase),
                            fertilityLabel = cultivar.pollinationRequirement?.label
                        )
                    }
            )
        }
        .distinctBy { normalize(it.species) }
        .sortedBy { it.species.lowercase() }

    fun catalogBloomTimingLabelFor(
        speciesInput: String,
        cultivarInput: String = ""
    ): String? {
        val speciesProfile = resolveSpeciesProfile(speciesInput) ?: return null
        val cultivarMatch = matchCultivarProfile(speciesProfile, cultivarInput)
        val phase = cultivarMatch?.phase ?: speciesProfile.defaultPhase
        return when (speciesProfile.forecastBehavior) {
            BloomForecastBehavior.WINDOW -> {
                val startDate = LocalDate.of(2026, speciesProfile.startMonth, speciesProfile.startDay)
                    .plusDays(phase.startOffsetDays.toLong())
                val endDate = startDate.plusDays(speciesProfile.durationDays)
                listOf(
                    "Catalog default",
                    "USDA ${speciesProfile.referenceZoneCode.uppercase()}",
                    formatCatalogRange(startDate, endDate)
                ).joinToString(catalogSeparator)
            }
            BloomForecastBehavior.MANUAL_ONLY -> "Catalog default - Continuous / repeat-bearing"
            BloomForecastBehavior.SUPPRESSED -> "Catalog default - No automatic bloom-season forecast"
        }
    }

    fun autoBloomTimingLabelFor(
        speciesInput: String,
        cultivarInput: String = "",
        locationProfile: ForecastLocationProfile,
        orchardRegionCode: String? = null
    ): String? {
        val speciesProfile = resolveSpeciesProfile(speciesInput) ?: return null
        val adjustedProfile = speciesProfile.withRegionalOverride(orchardRegionCode)
        val cultivarMatch = matchCultivarProfile(speciesProfile, cultivarInput)
        val phase = cultivarMatch?.phase ?: adjustedProfile.defaultPhase
        val orchardAwarePrefix = if (locationProfile.hasForecastSignals()) {
            "This orchard"
        } else {
            "Reference season"
        }
        return when (adjustedProfile.forecastBehavior) {
            BloomForecastBehavior.WINDOW -> {
                if (adjustedProfile.key == "dragon fruit") {
                    val season = dragonFruitSeasonForYear(
                        year = 2026,
                        locationProfile = locationProfile
                    )
                    return "$orchardAwarePrefix$catalogSeparator${formatCatalogRange(season.startDate, season.endDate)}"
                }
                if (adjustedProfile.modelType == PhenologyModelType.TROPICAL_REPEAT) {
                    val season = tropicalRepeatSeasonForYear(
                        profile = adjustedProfile,
                        year = 2026,
                        locationProfile = locationProfile
                    )
                    return "$orchardAwarePrefix$catalogSeparator" +
                        "Active season ${formatCatalogRange(season.startDate, season.endDate)}"
                }
                val rotatedProfile = if (
                    locationProfile.hemisphere == Hemisphere.SOUTHERN &&
                    adjustedProfile.key !in hemisphereShiftUnsafeKeys
                ) {
                    adjustedProfile.rotatedSixMonths()
                } else {
                    adjustedProfile
                }
                val climateShiftDays = climateShiftDaysFor(rotatedProfile, locationProfile)
                val startDate = LocalDate.of(2026, rotatedProfile.startMonth, rotatedProfile.startDay)
                    .plusDays(phase.startOffsetDays.toLong())
                    .plusDays(climateShiftDays)
                val endDate = startDate.plusDays(rotatedProfile.durationDays)
                "$orchardAwarePrefix$catalogSeparator${formatCatalogRange(startDate, endDate)}"
            }
            BloomForecastBehavior.MANUAL_ONLY ->
                "$orchardAwarePrefix$catalogSeparator${manualOnlySeasonLabelFor(adjustedProfile, locationProfile)}"
            BloomForecastBehavior.SUPPRESSED -> "No automatic bloom-season forecast"
        }
    }

    fun customBloomTimingSummaryLabel(tree: TreeEntity): String? {
        tree.exactCustomBloomWindowForYear(2026)?.let { window ->
            return formatCatalogRange(window.startDate, window.endDate)
        }
        val manualProfile = tree.effectiveManualBloomProfile()
        if (manualProfile.none { it > 0 }) return null
        val activeMonths = manualProfile
            .normalizedMonthlyIntensity()
            .mapIndexedNotNull { index, value -> bloomMonthLabels[index].takeIf { value > 0 } }
        return when (val patternType = tree.bloomPatternOverride ?: inferPatternFromMonthlyProfile(manualProfile)) {
            BloomPatternType.CONTINUOUS -> "Continuous / repeat"
            BloomPatternType.MULTI_WAVE -> activeMonths.joinToString(", ").ifBlank { "Repeat bloomer" }
            BloomPatternType.ALTERNATE_YEAR -> "Alternate-year pattern"
            BloomPatternType.MANUAL_ONLY -> "Manual bloom profile"
            BloomPatternType.SUPPRESSED -> "Suppressed"
            BloomPatternType.SINGLE_ANNUAL -> activeMonths.joinToString(", ")
        }
    }

    fun pollinationRequirementFor(
        speciesInput: String,
        cultivarInput: String = ""
    ): PollinationRequirement? {
        val speciesProfile = speciesByAlias[normalize(speciesInput)] ?: return null
        val cultivarRequirement = matchCultivarProfile(speciesProfile, cultivarInput)?.pollinationRequirement
        val requirement = cultivarRequirement ?: speciesProfile.pollinationRequirement
        return requirement.takeUnless { it == PollinationRequirement.UNKNOWN }
    }

    fun pollinationProfileFor(
        speciesInput: String,
        cultivarInput: String = ""
    ): PollinationProfile? = pollinationRequirementFor(speciesInput, cultivarInput)
        ?.toPollinationProfile()

    fun cultivarAutocompleteOptions(
        query: String,
        speciesQuery: String? = null,
        limit: Int = 8
    ): List<CultivarAutocompleteOption> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) {
            resolveScopedSpeciesLabel(speciesQuery)?.let { scopedSpecies ->
                val exactScopedOptions = cultivarsBySpeciesLabel[normalize(scopedSpecies)].orEmpty()
                val speciesProfile = resolveSpeciesProfile(speciesQuery)
                if (
                    exactScopedOptions.isNotEmpty() &&
                    speciesProfile != null &&
                    speciesProfile.supportsFamilyScopedAutocompleteExpansion()
                ) {
                    val familyOptions = cultivarAutocompleteCatalog
                        .filter { option ->
                            resolveSpeciesProfile(option.species)?.let { optionSpecies ->
                                speciesCompatible(speciesProfile, optionSpecies.key)
                            } == true
                        }
                        .sortedWith(
                            compareBy<CultivarAutocompleteOption> { it.species.lowercase() }
                                .thenBy { it.cultivar.lowercase() }
                        )
                    return (exactScopedOptions + familyOptions)
                        .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }
                        .take(limit)
                }
                if (exactScopedOptions.isNotEmpty()) {
                    return exactScopedOptions.take(limit)
                }
            }

            val speciesContext = speciesMatchContext(speciesQuery)
            val scopedOptions = cultivarAutocompleteCatalog
                .mapNotNull { option ->
                    val speciesScore = speciesMatchScore(speciesContext, option.species)
                    if (speciesScore <= 0) return@mapNotNull null
                    ScoredCultivarOption(
                        option = option,
                        score = 0,
                        speciesScore = speciesScore
                    )
                }
                .sortedWith(
                    compareByDescending<ScoredCultivarOption> { it.speciesScore }
                        .thenBy { it.option.species.lowercase() }
                        .thenBy { it.option.cultivar.lowercase() }
                )
                .map(ScoredCultivarOption::option)
                .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }
                .take(limit)
            return scopedOptions
        }
        val speciesContext = speciesMatchContext(speciesQuery)
        return cultivarAutocompleteCatalog
            .mapNotNull { option ->
                val score = sequenceOf(option.cultivar, *option.aliases.toTypedArray())
                    .map { matchScore(normalizedQuery, normalize(it)) }
                    .filterNotNull()
                    .maxOrNull()
                    ?: return@mapNotNull null
                ScoredCultivarOption(
                    option = option,
                    score = score,
                    speciesScore = speciesMatchScore(speciesContext, option.species)
                )
            }
            .sortedWith(
                compareByDescending<ScoredCultivarOption> { it.score }
                    .thenByDescending { it.speciesScore }
                    .thenBy { it.option.species.lowercase() }
                    .thenBy { it.option.cultivar.lowercase() }
            )
            .map(ScoredCultivarOption::option)
            .take(limit)
    }

    fun resolveCultivarAutocomplete(
        query: String,
        speciesQuery: String? = null
    ): CultivarAutocompleteOption? {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return null
        val exactMatches = cultivarAutocompleteCatalog
            .filter { option ->
                normalize(option.cultivar) == normalizedQuery ||
                    option.aliases.any { normalize(it) == normalizedQuery }
            }
            .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }
        if (exactMatches.isEmpty()) return null

        val speciesProfile = resolveSpeciesProfile(speciesQuery)
        if (speciesProfile != null) {
            val scopedMatches = exactMatches.filter { option ->
                resolveSpeciesProfile(option.species)?.let { optionSpecies ->
                    speciesCompatible(speciesProfile, optionSpecies.key)
                } == true
            }
            if (scopedMatches.size == 1) {
                return scopedMatches.single()
            }
        }

        return exactMatches.singleOrNull()
    }

    fun everbearingPlants(
        trees: List<TreeEntity>,
        locationProfilesByTreeId: Map<String, ForecastLocationProfile> = emptyMap()
    ): List<EverbearingPlant> = trees.mapNotNull { tree ->
        val patternType = when {
            tree.bloomTimingMode == BloomTimingMode.CUSTOM -> tree.bloomPatternOverride
                ?: inferPatternFromMonthlyProfile(tree.effectiveManualBloomProfile())
            else -> tree.resolveProfileMatch()?.profile?.patternType
        } ?: return@mapNotNull null
        if (patternType !in setOf(BloomPatternType.MULTI_WAVE, BloomPatternType.CONTINUOUS, BloomPatternType.MANUAL_ONLY)) {
            return@mapNotNull null
        }
        EverbearingPlant(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            detailLabel = tree.everbearingDetailLabel(
                locationProfile = locationProfilesByTreeId[tree.id] ?: ForecastLocationProfile()
            )
        )
    }.sortedWith(compareBy({ it.speciesLabel.lowercase() }, { it.treeLabel.lowercase() }))

    fun nextBloomSummary(
        tree: TreeEntity,
        locationProfile: ForecastLocationProfile,
        observations: List<PhenologyObservation> = emptyList()
    ): BloomForecastSummary? {
        val today = OrchardTime.today()
        val nextWindow = nextBloomWindow(
            tree = tree,
            locationProfile = locationProfile,
            observations = observations
        )

        if (nextWindow != null) {
            return nextWindow.toSummary(today)
        }

        if (tree.bloomTimingMode == BloomTimingMode.CUSTOM) {
            val manualPattern = tree.bloomPatternOverride ?: inferPatternFromMonthlyProfile(tree.effectiveManualBloomProfile())
            return BloomForecastSummary(
                headline = when (manualPattern) {
                    BloomPatternType.MULTI_WAVE -> "Watch"
                    BloomPatternType.CONTINUOUS -> "Repeat"
                    BloomPatternType.ALTERNATE_YEAR -> "Watch"
                    BloomPatternType.MANUAL_ONLY -> "Manual"
                    BloomPatternType.SUPPRESSED -> "Unknown"
                    BloomPatternType.SINGLE_ANNUAL -> "Unknown"
                },
                supportingLine = when (manualPattern) {
                    BloomPatternType.MULTI_WAVE -> "Repeat bloomer | custom | high confidence"
                    BloomPatternType.CONTINUOUS -> "Active season varies | custom | high confidence"
                    BloomPatternType.ALTERNATE_YEAR -> "Alternate-year pattern | custom | high confidence"
                    BloomPatternType.MANUAL_ONLY -> "Manual bloom profile | custom | high confidence"
                    BloomPatternType.SUPPRESSED -> "Bloom suppressed manually"
                    BloomPatternType.SINGLE_ANNUAL -> "Custom bloom window"
                },
                source = ForecastSource.CUSTOM,
                confidence = ForecastConfidence.HIGH,
                patternType = manualPattern,
                patternLabel = manualPattern.detailPatternLabel(),
                timingLabel = null,
                exactCountdownAllowed = false,
                daysUntilStart = null,
                isCurrentWindow = false
            )
        }

        val profileMatch = tree.resolveProfileMatch() ?: return null
        return when (profileMatch.profile.forecastBehavior) {
            BloomForecastBehavior.SUPPRESSED -> null
            BloomForecastBehavior.MANUAL_ONLY -> BloomForecastSummary(
                headline = "Repeat",
                supportingLine = buildString {
                    append(tree.everbearingDetailLabel(locationProfile))
                    append(" | ")
                    append(ForecastSource.CLIMATE_BAND.label)
                    append(" | ")
                    append(ForecastConfidence.MEDIUM.label)
                },
                source = ForecastSource.SPECIES_BASELINE,
                confidence = ForecastConfidence.MEDIUM,
                patternType = profileMatch.profile.patternType,
                patternLabel = profileMatch.profile.patternType.detailPatternLabel(),
                timingLabel = tree.everbearingDetailLabel(locationProfile)
                    .removePrefix("Active season ")
                    .trim()
                    .takeIf { it != tree.everbearingDetailLabel(locationProfile).trim() && it.isNotBlank() },
                exactCountdownAllowed = false,
                daysUntilStart = 0,
                isCurrentWindow = true
            )
            BloomForecastBehavior.WINDOW -> BloomForecastSummary(
                headline = "Unknown",
                supportingLine = "Catalog window unavailable | low confidence",
                source = ForecastSource.SPECIES_BASELINE,
                confidence = ForecastConfidence.LOW,
                patternType = profileMatch.profile.patternType,
                patternLabel = profileMatch.profile.patternType.detailPatternLabel(),
                timingLabel = null,
                exactCountdownAllowed = false,
                daysUntilStart = null,
                isCurrentWindow = false
            )
        }
    }

    fun plantBloomCountdownLabel(tree: TreeEntity, locationProfile: ForecastLocationProfile): String? =
        nextBloomSummary(tree, locationProfile)?.headline

    fun plantBloomCountdownLabel(tree: TreeEntity, zoneCode: String?): String? =
        plantBloomCountdownLabel(
            tree = tree,
            locationProfile = ForecastLocationProfile(usdaZoneCode = zoneCode?.takeIf(String::isNotBlank))
        )

    fun predictMonth(
        trees: List<TreeEntity>,
        yearMonth: YearMonth,
        locationProfile: ForecastLocationProfile,
        orchardRegionCode: String? = null,
        observations: List<PhenologyObservation> = emptyList()
    ): List<PredictedBloomWindow> {
        val zoneId = runCatching { ZoneId.of(locationProfile.timezoneId) }.getOrElse { OrchardTime.zoneId() }
        return trees.mapNotNull { tree ->
            tree.customBloomWindowForMonth(yearMonth)?.let { return@mapNotNull it }
            learnedBloomWindowForMonth(
                tree = tree,
                yearMonth = yearMonth,
                zoneId = zoneId,
                observations = observations
            )?.let { return@mapNotNull it }

            val profileMatch = tree.resolveProfileMatch() ?: return@mapNotNull null
            val speciesProfile = profileMatch.profile.withRegionalOverride(orchardRegionCode)
            if (speciesProfile.forecastBehavior != BloomForecastBehavior.WINDOW) {
                return@mapNotNull null
            }
            if (speciesProfile.key == "dragon fruit") {
                return@mapNotNull dragonFruitWindowForMonth(
                    tree = tree,
                    yearMonth = yearMonth,
                    locationProfile = locationProfile
                )
            }
            if (speciesProfile.modelType == PhenologyModelType.TROPICAL_REPEAT) {
                return@mapNotNull sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
                    .map { year ->
                        tropicalRepeatWindowForYear(
                            tree = tree,
                            profile = speciesProfile,
                            year = year,
                            locationProfile = locationProfile
                        )
                    }
                    .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }
            }
            val phase = profileMatch.phase
            val rotatedProfile = if (
                locationProfile.hemisphere == Hemisphere.SOUTHERN &&
                speciesProfile.key !in hemisphereShiftUnsafeKeys
            ) {
                speciesProfile.rotatedSixMonths()
                } else {
                    speciesProfile
            }
            val climateShiftDays = climateShiftDaysFor(rotatedProfile, locationProfile)
            val source = when {
                locationProfile.hemisphere == Hemisphere.SOUTHERN &&
                    speciesProfile.key !in hemisphereShiftUnsafeKeys -> ForecastSource.HEMISPHERE_SHIFTED
                climateShiftDays != 0L -> ForecastSource.CLIMATE_BAND
                profileMatch.cultivarMatched -> ForecastSource.CULTIVAR_ADJUSTED
                else -> ForecastSource.SPECIES_BASELINE
            }
            val confidence = forecastConfidenceFor(
                profile = speciesProfile,
                locationProfile = locationProfile,
                cultivarMatched = profileMatch.cultivarMatched,
                source = source
            )
            sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
                .map { year ->
                    bloomWindowForYear(
                        tree = tree,
                        profile = rotatedProfile,
                        phase = phase,
                        shiftDays = climateShiftDays,
                        source = source,
                        confidence = confidence,
                        year = year
                    )
                }
                .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }
        }.sortedBy(PredictedBloomWindow::startDate)
    }

    fun predictMonth(
        trees: List<TreeEntity>,
        yearMonth: YearMonth,
        zoneCode: String?,
        orchardRegionCode: String? = null
    ): List<PredictedBloomWindow> = predictMonth(
        trees = trees,
        yearMonth = yearMonth,
        locationProfile = ForecastLocationProfile(usdaZoneCode = zoneCode?.takeIf(String::isNotBlank)),
        orchardRegionCode = orchardRegionCode
    )

    private data class LearnedBloomSignal(
        val inferredDate: LocalDate,
        val directBloom: Boolean
    )

    private data class LearnedBloomProfile(
        val monthlyIntensity: List<Int>,
        val patternType: BloomPatternType,
        val peakDayOfYear: Int? = null,
        val durationDays: Long? = null,
        val confidence: ForecastConfidence,
        val alternateYearAnchor: Int? = null
    )

    private fun SpeciesBloomProfile.catalogUsdaBloomTimingLabel(): String {
        val zoneCode = referenceZoneCode.uppercase()
        return when (forecastBehavior) {
            BloomForecastBehavior.WINDOW -> {
                val startDate = LocalDate.of(2026, startMonth, startDay)
                val endDate = startDate.plusDays(durationDays)
                "USDA $zoneCode - ${formatCatalogRange(startDate, endDate)}"
            }
            BloomForecastBehavior.MANUAL_ONLY -> "USDA $zoneCode - Continuous / repeat-bearing"
            BloomForecastBehavior.SUPPRESSED -> "USDA $zoneCode - No automatic bloom-season forecast"
        }
    }

    private fun SpeciesBloomProfile.catalogZoneBloomTimingEntries(): List<CatalogUsdaBloomTimingEntry> = when (forecastBehavior) {
        BloomForecastBehavior.WINDOW -> {
            val referenceZone = UsdaZoneCatalog.resolve(referenceZoneCode)
            UsdaZoneCatalog.zones.map { zone ->
                val shiftDays = (referenceZone.index - zone.index) * shiftDaysPerHalfZone
                val startDate = LocalDate.of(2026, startMonth, startDay).plusDays(shiftDays)
                val endDate = startDate.plusDays(durationDays)
                CatalogUsdaBloomTimingEntry(
                    zoneCode = zone.code,
                    zoneLabel = "USDA ${zone.code.uppercase()}",
                    timingLabel = formatCatalogRange(startDate, endDate)
                )
            }
        }
        BloomForecastBehavior.MANUAL_ONLY -> listOf(
            CatalogUsdaBloomTimingEntry(
                zoneCode = "all",
                zoneLabel = "All USDA zones",
                timingLabel = "Continuous / repeat-bearing"
            )
        )
        BloomForecastBehavior.SUPPRESSED -> listOf(
            CatalogUsdaBloomTimingEntry(
                zoneCode = "all",
                zoneLabel = "All USDA zones",
                timingLabel = "No automatic bloom-season forecast"
            )
        )
    }

    private fun SpeciesBloomProfile.withRegionalOverride(orchardRegionCode: String?): SpeciesBloomProfile {
        val orchardRegion = OrchardRegionCatalog.resolve(orchardRegionCode)
        val override = regionalBloomOverrides.firstOrNull {
            it.speciesKey == key && it.orchardRegion == orchardRegion
        } ?: return this
        return copy(
            referenceZoneCode = override.referenceZoneCode,
            startMonth = override.startMonth,
            startDay = override.startDay,
            durationDays = override.durationDays,
            shiftDaysPerHalfZone = override.shiftDaysPerHalfZone
        )
    }

    private fun bloomWindowForYear(
        tree: TreeEntity,
        profile: SpeciesBloomProfile,
        phase: BloomPhase,
        shiftDays: Long,
        source: ForecastSource,
        confidence: ForecastConfidence,
        year: Int
    ): PredictedBloomWindow {
        val startDate = LocalDate.of(year, profile.startMonth, profile.startDay)
            .plusDays(phase.startOffsetDays.toLong())
            .plusDays(shiftDays)
        val endDate = startDate.plusDays(profile.durationDays)
        return PredictedBloomWindow(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            startDate = startDate,
            endDate = endDate,
            phase = phase,
            patternType = profile.patternType,
            source = source,
            confidence = confidence
        )
    }

    private fun TreeEntity.customBloomWindowForMonth(yearMonth: YearMonth): PredictedBloomWindow? {
        exactCustomBloomWindowForMonth(yearMonth)?.let { return it }
        if (bloomTimingMode != BloomTimingMode.CUSTOM) return null
        val manualProfile = effectiveManualBloomProfile()
        if (manualProfile.none { it > 0 }) return null
        return patternWindowForMonth(
            tree = this,
            yearMonth = yearMonth,
            monthlyIntensity = manualProfile,
            patternType = bloomPatternOverride ?: inferPatternFromMonthlyProfile(manualProfile),
            source = ForecastSource.CUSTOM,
            confidence = ForecastConfidence.HIGH,
            alternateYearAnchor = alternateYearAnchor
        )
    }

    private fun TreeEntity.exactCustomBloomWindowForMonth(yearMonth: YearMonth): PredictedBloomWindow? =
        sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
            .mapNotNull { year -> exactCustomBloomWindowForYear(year) }
            .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }

    private fun TreeEntity.exactCustomBloomWindowForYear(year: Int): PredictedBloomWindow? {
        if (bloomTimingMode != BloomTimingMode.CUSTOM) return null
        val month = customBloomStartMonth ?: return null
        val day = customBloomStartDay ?: return null
        val duration = customBloomDurationDays?.takeIf { it > 0 } ?: return null
        val startDate = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return null
        return PredictedBloomWindow(
            treeId = id,
            treeLabel = displayName(),
            speciesLabel = speciesCultivarLabel(species, cultivar),
            startDate = startDate,
            endDate = startDate.plusDays(duration.toLong()),
            phase = BloomPhase.MID,
            patternType = bloomPatternOverride ?: BloomPatternType.SINGLE_ANNUAL,
            source = ForecastSource.CUSTOM,
            confidence = ForecastConfidence.HIGH
        )
    }

    private fun dragonFruitWindowForMonth(
        tree: TreeEntity,
        yearMonth: YearMonth,
        locationProfile: ForecastLocationProfile
    ): PredictedBloomWindow? = sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
        .map { year -> dragonFruitWindowForYear(tree, year, locationProfile) }
        .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }

    private data class DragonFruitSeason(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val source: ForecastSource,
        val confidence: ForecastConfidence
    )

    private data class SeasonalDateRange(
        val startDate: LocalDate,
        val endDate: LocalDate
    )

    private fun dragonFruitWindowForYear(
        tree: TreeEntity,
        year: Int,
        locationProfile: ForecastLocationProfile
    ): PredictedBloomWindow {
        val season = dragonFruitSeasonForYear(year, locationProfile)
        return PredictedBloomWindow(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            startDate = season.startDate,
            endDate = season.endDate,
            phase = BloomPhase.MID,
            patternType = BloomPatternType.MULTI_WAVE,
            source = season.source,
            confidence = season.confidence
        )
    }

    private fun tropicalRepeatWindowForYear(
        tree: TreeEntity,
        profile: SpeciesBloomProfile,
        year: Int,
        locationProfile: ForecastLocationProfile
    ): PredictedBloomWindow {
        val season = tropicalRepeatSeasonForYear(
            profile = profile,
            year = year,
            locationProfile = locationProfile
        )
        return PredictedBloomWindow(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            startDate = season.startDate,
            endDate = season.endDate,
            phase = BloomPhase.MID,
            patternType = profile.patternType,
            source = if (locationProfile.hasForecastSignals()) {
                ForecastSource.CLIMATE_BAND
            } else {
                ForecastSource.SPECIES_BASELINE
            },
            confidence = if (locationProfile.hasForecastSignals()) {
                ForecastConfidence.MEDIUM
            } else {
                ForecastConfidence.LOW
            }
        )
    }

    private fun dragonFruitSeasonForYear(
        year: Int,
        locationProfile: ForecastLocationProfile
    ): DragonFruitSeason {
        val climateBand = effectiveClimateBand(locationProfile)
        val warmSeasonWindow = locationProfile.climateFingerprint?.warmSeasonWindow(
            minMeanTempC = 20.0,
            minMaxTempC = 28.0
        )
        val baselineSeason = dragonFruitBaselineSeason(
            year = year,
            hemisphere = locationProfile.hemisphere,
            climateBand = climateBand
        )
        val climateSeason = warmSeasonWindow?.let { window ->
            SeasonalDateRange(
                startDate = LocalDate.of(year, window.startMonth, 1),
                endDate = LocalDate.of(year, window.startMonth, 1)
                    .plusMonths(window.monthCount.toLong())
                    .minusDays(1)
            )
        }
        val effectiveSeason = climateSeason
            ?.let { season -> clampSeasonRange(season, baselineSeason) }
            ?: baselineSeason
        val hasMicroclimateRisk = locationProfile.microclimateFlags.any {
            it == MicroclimateFlag.GREENHOUSE || it == MicroclimateFlag.FROST_POCKET
        }
        return DragonFruitSeason(
            startDate = effectiveSeason.startDate,
            endDate = effectiveSeason.endDate,
            source = if (
                warmSeasonWindow != null ||
                locationProfile.latitudeDeg != null ||
                !locationProfile.usdaZoneCode.isNullOrBlank()
            ) {
                ForecastSource.CLIMATE_BAND
            } else {
                ForecastSource.SPECIES_BASELINE
            },
            confidence = when {
                hasMicroclimateRisk -> ForecastConfidence.LOW
                warmSeasonWindow != null ||
                    locationProfile.latitudeDeg != null ||
                    !locationProfile.usdaZoneCode.isNullOrBlank() -> ForecastConfidence.MEDIUM
                else -> ForecastConfidence.LOW
            }
        )
    }

    private fun dragonFruitBaselineSeason(
        year: Int,
        hemisphere: Hemisphere,
        climateBand: ClimateBand?
    ): SeasonalDateRange {
        val northernRange = when (climateBand) {
            ClimateBand.EQUATORIAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 4, 1),
                endDate = LocalDate.of(year, 11, 30)
            )
            ClimateBand.TROPICAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 4, 1),
                endDate = LocalDate.of(year, 11, 15)
            )
            ClimateBand.SUBTROPICAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 5, 1),
                endDate = LocalDate.of(year, 10, 31)
            )
            ClimateBand.TEMPERATE -> SeasonalDateRange(
                startDate = LocalDate.of(year, 6, 1),
                endDate = LocalDate.of(year, 9, 30)
            )
            ClimateBand.COOL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 6, 15),
                endDate = LocalDate.of(year, 9, 15)
            )
            null -> SeasonalDateRange(
                startDate = LocalDate.of(year, 5, 1),
                endDate = LocalDate.of(year, 10, 31)
            )
        }
        return when (hemisphere) {
            Hemisphere.SOUTHERN -> SeasonalDateRange(
                startDate = northernRange.startDate.plusMonths(6),
                endDate = northernRange.endDate.plusMonths(6)
            )
            Hemisphere.EQUATORIAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 4, 1),
                endDate = LocalDate.of(year, 11, 30)
            )
            Hemisphere.NORTHERN -> northernRange
        }
    }

    fun nextBloomWindow(
        tree: TreeEntity,
        locationProfile: ForecastLocationProfile,
        observations: List<PhenologyObservation> = emptyList()
    ): PredictedBloomWindow? {
        val today = OrchardTime.today()
        val currentMonth = YearMonth.from(today)
        return generateSequence(0L) { it + 1 }
            .take(18)
            .mapNotNull { offset ->
                predictMonth(
                    trees = listOf(tree),
                    yearMonth = currentMonth.plusMonths(offset),
                    locationProfile = locationProfile,
                    observations = observations
                ).firstOrNull()
            }
            .firstOrNull { window -> !window.endDate.isBefore(today) }
    }

    private fun clampSeasonRange(
        climateSeason: SeasonalDateRange,
        baselineSeason: SeasonalDateRange
    ): SeasonalDateRange {
        val clampedStart = maxOf(climateSeason.startDate, baselineSeason.startDate)
        val clampedEnd = minOf(climateSeason.endDate, baselineSeason.endDate)
        return if (clampedEnd.isBefore(clampedStart)) baselineSeason else SeasonalDateRange(
            startDate = clampedStart,
            endDate = clampedEnd
        )
    }

    private fun tropicalRepeatSeasonForYear(
        profile: SpeciesBloomProfile,
        year: Int,
        locationProfile: ForecastLocationProfile
    ): SeasonalDateRange {
        val rotatedProfile = if (
            locationProfile.hemisphere == Hemisphere.SOUTHERN &&
            profile.key !in hemisphereShiftUnsafeKeys
        ) {
            profile.rotatedSixMonths()
        } else {
            profile
        }
        val baselineSeason = SeasonalDateRange(
            startDate = LocalDate.of(year, rotatedProfile.startMonth, rotatedProfile.startDay),
            endDate = LocalDate.of(year, rotatedProfile.startMonth, rotatedProfile.startDay)
                .plusDays(rotatedProfile.durationDays)
        )
        val climateSeason = warmSeasonDateRange(
            year = year,
            locationProfile = locationProfile,
            minMeanTempC = 18.0,
            minMaxTempC = 26.0
        ) ?: climateBandSeasonForYear(year, locationProfile)
            ?: baselineSeason
        val startDate = maxOf(baselineSeason.startDate, climateSeason.startDate)
        val endDate = climateSeason.endDate
        return if (endDate.isBefore(startDate)) baselineSeason else SeasonalDateRange(
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun climateBandSeasonForYear(
        year: Int,
        locationProfile: ForecastLocationProfile
    ): SeasonalDateRange? {
        val climateBand = effectiveClimateBand(locationProfile)
            ?: return null
        return climateBandSeasonForBand(year, climateBand, locationProfile.hemisphere)
    }

    private fun warmSeasonDateRange(
        year: Int,
        locationProfile: ForecastLocationProfile,
        minMeanTempC: Double,
        minMaxTempC: Double
    ): SeasonalDateRange? {
        val warmSeasonWindow = locationProfile.climateFingerprint?.warmSeasonWindow(
            minMeanTempC = minMeanTempC,
            minMaxTempC = minMaxTempC
        ) ?: return null
        val startDate = LocalDate.of(year, warmSeasonWindow.startMonth, 1)
        val endDate = startDate.plusMonths(warmSeasonWindow.monthCount.toLong()).minusDays(1)
        return SeasonalDateRange(startDate = startDate, endDate = endDate)
    }

    private fun manualOnlySeasonLabelFor(
        profile: SpeciesBloomProfile,
        locationProfile: ForecastLocationProfile
    ): String {
        if (!locationProfile.hasForecastSignals()) {
            return manualOnlyPatternLabel(profile.patternType)
        }
        val seasonRange = when (profile.modelType) {
            PhenologyModelType.TROPICAL_REPEAT ->
                tropicalRepeatSeasonForYear(profile, 2026, locationProfile)
            PhenologyModelType.WARM_SEASON_PHOTOPERIOD ->
                dragonFruitSeasonForYear(2026, locationProfile).let { season ->
                    SeasonalDateRange(season.startDate, season.endDate)
                }
            else -> warmSeasonDateRange(
                year = 2026,
                locationProfile = locationProfile,
                minMeanTempC = 20.0,
                minMaxTempC = 28.0
            ) ?: climateBandSeasonForYear(2026, locationProfile)
        }
        return seasonRange?.let { range ->
            "Active season ${formatCatalogRange(range.startDate, range.endDate)}"
        } ?: manualOnlyPatternLabel(profile.patternType)
    }

    private fun manualOnlyPatternLabel(patternType: BloomPatternType): String = when (patternType) {
        BloomPatternType.CONTINUOUS -> "Continuous / repeat-bearing"
        BloomPatternType.MULTI_WAVE -> "Repeat bloomer"
        BloomPatternType.MANUAL_ONLY -> "Watch for buds manually"
        BloomPatternType.ALTERNATE_YEAR -> "Alternate-year bloom pattern"
        BloomPatternType.SUPPRESSED -> "No automatic bloom-season forecast"
        BloomPatternType.SINGLE_ANNUAL -> "Manual bloom profile"
    }

    private fun TreeEntity.everbearingDetailLabel(locationProfile: ForecastLocationProfile): String {
        if (bloomTimingMode == BloomTimingMode.CUSTOM) {
            return when (val patternType = bloomPatternOverride ?: inferPatternFromMonthlyProfile(effectiveManualBloomProfile())) {
                BloomPatternType.CONTINUOUS -> "Continuous / repeat-bearing"
                BloomPatternType.MULTI_WAVE -> "Repeat bloomer"
                BloomPatternType.MANUAL_ONLY -> "Watch for buds manually"
                BloomPatternType.ALTERNATE_YEAR -> "Alternate-year bloom pattern"
                BloomPatternType.SUPPRESSED -> "No automatic bloom-season forecast"
                BloomPatternType.SINGLE_ANNUAL -> patternType.label
            }
        }
        val profile = resolveProfileMatch()?.profile ?: return "Repeat bloomer"
        return manualOnlySeasonLabelFor(profile, locationProfile)
    }

    private fun learnedBloomWindowForMonth(
        tree: TreeEntity,
        yearMonth: YearMonth,
        zoneId: ZoneId,
        observations: List<PhenologyObservation>
    ): PredictedBloomWindow? {
        val learnedProfile = learnedBloomProfile(tree, zoneId, observations) ?: return null
        if (learnedProfile.patternType == BloomPatternType.SINGLE_ANNUAL &&
            learnedProfile.peakDayOfYear != null &&
            learnedProfile.durationDays != null
        ) {
            return sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
                .mapNotNull { year ->
                    val peakDate = dayOfYearToDate(year, learnedProfile.peakDayOfYear)
                    val startDate = peakDate.minusDays(learnedProfile.durationDays / 2)
                    val endDate = startDate.plusDays(learnedProfile.durationDays)
                    PredictedBloomWindow(
                        treeId = tree.id,
                        treeLabel = tree.displayName(),
                        speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
                        startDate = startDate,
                        endDate = endDate,
                        phase = BloomPhase.MID,
                        patternType = learnedProfile.patternType,
                        source = ForecastSource.HISTORY_LEARNED,
                        confidence = learnedProfile.confidence
                    )
                }
                .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }
        }
        return patternWindowForMonth(
            tree = tree,
            yearMonth = yearMonth,
            monthlyIntensity = learnedProfile.monthlyIntensity,
            patternType = learnedProfile.patternType,
            source = ForecastSource.HISTORY_LEARNED,
            confidence = learnedProfile.confidence,
            alternateYearAnchor = learnedProfile.alternateYearAnchor
        )
    }

    private fun learnedBloomProfile(
        tree: TreeEntity,
        zoneId: ZoneId,
        observations: List<PhenologyObservation>
    ): LearnedBloomProfile? {
        val signals = observations
            .asSequence()
            .filter { it.treeId == tree.id }
            .mapNotNull { it.toLearnedBloomSignal(zoneId) }
            .toList()
        if (signals.isEmpty()) return null

        val bloomSignals = signals.filter(LearnedBloomSignal::directBloom)
        val weightedMonths = MutableList(12) { 0.0 }
        signals.forEach { signal ->
            val monthIndex = signal.inferredDate.monthValue - 1
            weightedMonths[monthIndex] += if (signal.directBloom) 3.0 else 1.5
        }
        val maxWeight = weightedMonths.maxOrNull() ?: 0.0
        val monthlyIntensity = if (maxWeight <= 0.0) {
            List(12) { 0 }
        } else {
            weightedMonths.map { weight ->
                when {
                    weight <= 0.0 -> 0
                    weight / maxWeight >= 0.75 -> 3
                    weight / maxWeight >= 0.45 -> 2
                    else -> 1
                }
            }
        }
        val activeYears = signals.map { it.inferredDate.year }.distinct().sorted()
        val alternateYearAnchor = activeYears.firstOrNull()
            ?.takeIf {
                activeYears.size >= 2 &&
                    activeYears.map { year -> year % 2 }.distinct().size == 1 &&
                    (activeYears.last() - activeYears.first()) >= 2
            }
        val inferredPattern = when {
            alternateYearAnchor != null -> BloomPatternType.ALTERNATE_YEAR
            else -> inferPatternFromMonthlyProfile(monthlyIntensity)
        }
        val confidence = when {
            activeYears.size >= 2 && (bloomSignals.isNotEmpty() || signals.size >= 3) -> ForecastConfidence.HIGH
            activeYears.size >= 1 && (bloomSignals.isNotEmpty() || signals.size >= 2) -> ForecastConfidence.MEDIUM
            bloomSignals.size >= 2 || signals.size >= 3 -> ForecastConfidence.MEDIUM
            else -> ForecastConfidence.LOW
        }
        val peakDay = circularMeanDayOfYear(signals.map { it.inferredDate.dayOfYear })
        val spreadDays = circularSpreadDays(signals.map { it.inferredDate.dayOfYear }, peakDay)
        val durationDays = when {
            inferredPattern != BloomPatternType.SINGLE_ANNUAL -> null
            bloomSignals.size >= 3 && spreadDays <= 9 -> 21L
            bloomSignals.size >= 2 && spreadDays <= 16 -> 28L
            bloomSignals.isEmpty() -> 40L
            spreadDays >= 24 -> 42L
            else -> 32L
        }
        return LearnedBloomProfile(
            monthlyIntensity = monthlyIntensity,
            patternType = inferredPattern,
            peakDayOfYear = peakDay.takeIf { inferredPattern == BloomPatternType.SINGLE_ANNUAL },
            durationDays = durationDays,
            confidence = confidence,
            alternateYearAnchor = alternateYearAnchor
        )
    }

    private fun TreeEntity.effectiveManualBloomProfile(): List<Int> {
        val explicitProfile = manualBloomProfile.normalizedMonthlyIntensity()
        if (explicitProfile.any { it > 0 }) return explicitProfile
        return legacyManualBloomProfile()
    }

    private fun TreeEntity.legacyManualBloomProfile(): List<Int> {
        if (bloomTimingMode != BloomTimingMode.CUSTOM) return List(12) { 0 }
        val month = customBloomStartMonth?.takeIf { it in 1..12 } ?: return List(12) { 0 }
        val duration = customBloomDurationDays?.takeIf { it > 0 } ?: return List(12) { 0 }
        val monthCount = when {
            duration >= 180 -> 12
            duration >= 90 -> 3
            duration >= 45 -> 2
            else -> 1
        }
        return List(12) { index ->
            val monthValue = index + 1
            if ((0 until monthCount).any { offset -> ((month - 1 + offset) % 12) + 1 == monthValue }) {
                if (monthCount >= 3) 2 else 3
            } else {
                0
            }
        }
    }

    private fun patternWindowForMonth(
        tree: TreeEntity,
        yearMonth: YearMonth,
        monthlyIntensity: List<Int>,
        patternType: BloomPatternType,
        source: ForecastSource,
        confidence: ForecastConfidence,
        alternateYearAnchor: Int? = null
    ): PredictedBloomWindow? {
        val normalizedProfile = monthlyIntensity.normalizedMonthlyIntensity()
        if (patternType == BloomPatternType.SUPPRESSED) return null
        if (patternType == BloomPatternType.ALTERNATE_YEAR &&
            alternateYearAnchor != null &&
            (yearMonth.year - alternateYearAnchor) % 2 != 0
        ) {
            return null
        }
        val activeThisMonth = when (patternType) {
            BloomPatternType.CONTINUOUS -> true
            BloomPatternType.MANUAL_ONLY -> normalizedProfile.any { it > 0 }
            else -> normalizedProfile.getOrElse(yearMonth.monthValue - 1) { 0 } > 0
        }
        if (!activeThisMonth) return null
        return PredictedBloomWindow(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            startDate = yearMonth.atDay(1),
            endDate = yearMonth.atEndOfMonth(),
            phase = BloomPhase.MID,
            patternType = patternType,
            source = source,
            confidence = confidence
        )
    }

    private fun PhenologyObservation.toLearnedBloomSignal(zoneId: ZoneId): LearnedBloomSignal? {
        val observedDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
        val inferredDate = when (eventType) {
            com.dillon.orcharddex.data.model.EventType.BLOOM -> observedDate
            com.dillon.orcharddex.data.model.EventType.BUD -> observedDate.plusDays(10)
            com.dillon.orcharddex.data.model.EventType.FRUIT_SET -> observedDate.minusDays(14)
            else -> null
        } ?: return null
        return LearnedBloomSignal(
            inferredDate = inferredDate,
            directBloom = eventType == com.dillon.orcharddex.data.model.EventType.BLOOM
        )
    }

    private fun circularMeanDayOfYear(days: List<Int>): Int {
        if (days.isEmpty()) return 1
        val yearLength = 366.0
        val angleFactor = (2 * PI) / yearLength
        val x = days.sumOf { day -> cos(day * angleFactor) }
        val y = days.sumOf { day -> sin(day * angleFactor) }
        var angle = atan2(y, x)
        if (angle < 0) angle += 2 * PI
        return ((angle / (2 * PI)) * yearLength).roundToInt().coerceIn(1, 366)
    }

    private fun circularSpreadDays(days: List<Int>, centerDay: Int): Int {
        if (days.isEmpty()) return 0
        return days
            .map { day -> circularDistance(day, centerDay) }
            .average()
            .roundToInt()
    }

    private fun circularDistance(left: Int, right: Int, yearLength: Int = 366): Int {
        val delta = abs(left - right)
        return min(delta, yearLength - delta)
    }

    private fun List<Int>.normalizedMonthlyIntensity(): List<Int> {
        val normalized = take(12).map { it.coerceIn(0, 3) }
        return if (normalized.size == 12) normalized else normalized + List(12 - normalized.size) { 0 }
    }

    private fun inferPatternFromMonthlyProfile(monthlyIntensity: List<Int>): BloomPatternType {
        val normalizedProfile = monthlyIntensity.normalizedMonthlyIntensity()
        val activeMonths = normalizedProfile.count { it > 0 }
        if (activeMonths == 0) return BloomPatternType.SUPPRESSED
        if (activeMonths >= 8) return BloomPatternType.CONTINUOUS
        val doubled = normalizedProfile + normalizedProfile
        var spanCount = 0
        var inSpan = false
        for (index in doubled.indices) {
            val active = doubled[index] > 0
            if (active && !inSpan) {
                spanCount += 1
                inSpan = true
            } else if (!active) {
                inSpan = false
            }
        }
        return when {
            activeMonths >= 4 -> BloomPatternType.MULTI_WAVE
            spanCount >= 3 -> BloomPatternType.MULTI_WAVE
            else -> BloomPatternType.SINGLE_ANNUAL
        }
    }

    private fun dayOfYearToDate(year: Int, dayOfYear: Int): LocalDate {
        val maxDay = if (java.time.Year.isLeap(year.toLong())) 366 else 365
        return LocalDate.ofYearDay(year, dayOfYear.coerceIn(1, maxDay))
    }

    private fun TreeEntity.resolveProfileMatch(): ProfileMatch? {
        val speciesMatch = speciesProfile()
        val cultivarMatch = speciesMatch?.let { matchCultivarProfile(it, cultivar) }
        if (cultivarMatch != null) {
            val profile = speciesProfiles.firstOrNull { it.key == cultivarMatch.speciesKey }
            if (profile != null) {
                return ProfileMatch(profile = profile, phase = cultivarMatch.phase, cultivarMatched = true)
            }
        }
        val profile = speciesMatch ?: return null
        return ProfileMatch(profile = profile, phase = profile.defaultPhase, cultivarMatched = false)
    }

    private fun matchCultivarProfile(
        speciesProfile: SpeciesBloomProfile,
        cultivarInput: String
    ): CultivarBloomProfile? {
        val cultivarKey = normalize(cultivarInput)
        if (cultivarKey.isBlank()) return null
        return cultivarProfiles.firstOrNull { cultivarProfile ->
            speciesCompatible(speciesProfile, cultivarProfile.speciesKey) &&
                (
                    cultivarKey == normalize(cultivarProfile.cultivar) ||
                        cultivarProfile.aliases.any { normalize(it) == cultivarKey }
                    )
        }
    }

    private fun TreeEntity.speciesProfile(): SpeciesBloomProfile? = resolveSpeciesProfile(species)

    private fun resolveSpeciesProfile(speciesInput: String?): SpeciesBloomProfile? {
        val normalizedSpecies = normalize(speciesInput.orEmpty())
        if (normalizedSpecies.isBlank()) return null
        return speciesByAlias[normalizedSpecies]
            ?: speciesByAlias.entries.firstOrNull { normalizedSpecies.contains(it.key) }?.value
    }

    private fun SpeciesBloomProfile.rotatedSixMonths(): SpeciesBloomProfile {
        val shiftedStart = LocalDate.of(2026, startMonth, startDay).plusMonths(6)
        return copy(
            startMonth = shiftedStart.monthValue,
            startDay = shiftedStart.dayOfMonth
        )
    }

    private fun climateShiftDaysFor(
        profile: SpeciesBloomProfile,
        locationProfile: ForecastLocationProfile
    ): Long {
        val referenceBand = referenceClimateBandFor(profile.referenceZoneCode)
        val referenceStartDate = LocalDate.of(2026, profile.startMonth, profile.startDay)
        val bandShiftDays = climateAlignedStartDateForYear(
            profile = profile,
            year = referenceStartDate.year,
            locationProfile = locationProfile
        )?.let { alignedStartDate ->
            ChronoUnit.DAYS.between(referenceStartDate, alignedStartDate)
        } ?: effectiveClimateBand(locationProfile)?.let { climateBand ->
            (climateBand.order - referenceBand.order) * 14L
        } ?: 0L
        val elevationShiftDays = when {
            locationProfile.elevationM == null -> 0L
            locationProfile.elevationM >= 1800.0 -> 21L
            locationProfile.elevationM >= 1200.0 -> 14L
            locationProfile.elevationM >= 600.0 -> 7L
            else -> 0L
        }
        return bandShiftDays + elevationShiftDays + chillTimingAdjustmentDays(profile, locationProfile)
    }

    private fun referenceClimateBandFor(zoneCode: String): ClimateBand {
        val zoneNumber = zoneCode.trim().takeWhile(Char::isDigit).toIntOrNull() ?: 7
        return when {
            zoneNumber >= 11 -> ClimateBand.TROPICAL
            zoneNumber >= 9 -> ClimateBand.SUBTROPICAL
            zoneNumber >= 6 -> ClimateBand.TEMPERATE
            else -> ClimateBand.COOL
        }
    }

    private val ClimateBand.order: Int
        get() = when (this) {
            ClimateBand.EQUATORIAL -> 0
            ClimateBand.TROPICAL -> 1
            ClimateBand.SUBTROPICAL -> 2
            ClimateBand.TEMPERATE -> 3
            ClimateBand.COOL -> 4
        }

    private val ChillHoursBand.order: Int
        get() = when (this) {
            ChillHoursBand.UNKNOWN -> -1
            ChillHoursBand.UNDER_100 -> 0
            ChillHoursBand.H100_300 -> 1
            ChillHoursBand.H300_600 -> 2
            ChillHoursBand.H600_900 -> 3
            ChillHoursBand.H900_PLUS -> 4
        }

    private fun effectiveClimateBand(locationProfile: ForecastLocationProfile): ClimateBand? {
        val derivedClimateBand = locationProfile.climateBand()
        val zoneClimateBand = locationProfile.usdaZoneCode
            ?.takeIf(String::isNotBlank)
            ?.let(::referenceClimateBandFor)
        return listOfNotNull(derivedClimateBand, zoneClimateBand)
            .maxByOrNull { band -> band.order }
    }

    private fun climateBandSeasonForBand(
        year: Int,
        climateBand: ClimateBand,
        hemisphere: Hemisphere
    ): SeasonalDateRange {
        val northernRange = when (climateBand) {
            ClimateBand.EQUATORIAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 2, 1),
                endDate = LocalDate.of(year, 12, 31)
            )
            ClimateBand.TROPICAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 3, 1),
                endDate = LocalDate.of(year, 11, 30)
            )
            ClimateBand.SUBTROPICAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 4, 1),
                endDate = LocalDate.of(year, 10, 31)
            )
            ClimateBand.TEMPERATE -> SeasonalDateRange(
                startDate = LocalDate.of(year, 5, 1),
                endDate = LocalDate.of(year, 9, 30)
            )
            ClimateBand.COOL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 6, 1),
                endDate = LocalDate.of(year, 8, 31)
            )
        }
        return when (hemisphere) {
            Hemisphere.SOUTHERN -> SeasonalDateRange(
                startDate = northernRange.startDate.plusMonths(6),
                endDate = northernRange.endDate.plusMonths(6)
            )
            Hemisphere.EQUATORIAL -> SeasonalDateRange(
                startDate = LocalDate.of(year, 2, 1),
                endDate = LocalDate.of(year, 12, 31)
            )
            Hemisphere.NORTHERN -> northernRange
        }
    }

    private fun climateAlignedStartDateForYear(
        profile: SpeciesBloomProfile,
        year: Int,
        locationProfile: ForecastLocationProfile
    ): LocalDate? {
        val referenceBand = referenceClimateBandFor(profile.referenceZoneCode)
        val localOnsetDate = climateOnsetDateForYear(profile, year, locationProfile) ?: return null
        val referenceOnsetDate = if (profile.key in winterBloomKeys) {
            referenceCoolingOnsetDateForBand(year, referenceBand, locationProfile.hemisphere)
        } else {
            referenceOnsetDateForBand(year, referenceBand, locationProfile.hemisphere)
        }
        val referenceStartDate = LocalDate.of(year, profile.startMonth, profile.startDay)
        val offsetFromOnset = ChronoUnit.DAYS.between(referenceOnsetDate, referenceStartDate)
        return localOnsetDate.plusDays(offsetFromOnset)
    }

    private fun climateOnsetDateForYear(
        profile: SpeciesBloomProfile,
        year: Int,
        locationProfile: ForecastLocationProfile
    ): LocalDate? {
        val referenceBand = referenceClimateBandFor(profile.referenceZoneCode)
        if (profile.key in winterBloomKeys) {
            val (maxMeanTempC, maxMaxTempC) = coolingThresholdsFor(profile, referenceBand)
            return coolingTriggeredOnsetDateForYear(
                year = year,
                locationProfile = locationProfile,
                maxMeanTempC = maxMeanTempC,
                maxMaxTempC = maxMaxTempC
            ) ?: effectiveClimateBand(locationProfile)?.let { climateBand ->
                referenceCoolingOnsetDateForBand(year, climateBand, locationProfile.hemisphere)
            }
        }
        val (minMeanTempC, minMaxTempC) = onsetThresholdsFor(profile, referenceBand)
        return temperatureTriggeredOnsetDateForYear(
            year = year,
            locationProfile = locationProfile,
            minMeanTempC = minMeanTempC,
            minMaxTempC = minMaxTempC
        ) ?: effectiveClimateBand(locationProfile)?.let { climateBand ->
            referenceOnsetDateForBand(year, climateBand, locationProfile.hemisphere)
        }
    }

    private fun coolingThresholdsFor(
        profile: SpeciesBloomProfile,
        referenceBand: ClimateBand
    ): Pair<Double, Double> = when {
        profile.key == "loquat" -> 18.0 to 26.0
        referenceBand == ClimateBand.TROPICAL -> 20.0 to 28.0
        referenceBand == ClimateBand.SUBTROPICAL -> 18.0 to 26.0
        else -> 16.0 to 24.0
    }

    private fun onsetThresholdsFor(
        profile: SpeciesBloomProfile,
        referenceBand: ClimateBand
    ): Pair<Double, Double> = when {
        profile.key in chillSensitiveKeys -> 8.0 to 15.0
        referenceBand == ClimateBand.COOL -> 7.0 to 13.0
        referenceBand == ClimateBand.TEMPERATE -> 10.0 to 16.0
        referenceBand == ClimateBand.SUBTROPICAL -> 14.0 to 22.0
        else -> 18.0 to 26.0
    }

    private fun temperatureTriggeredOnsetDateForYear(
        year: Int,
        locationProfile: ForecastLocationProfile,
        minMeanTempC: Double,
        minMaxTempC: Double
    ): LocalDate? {
        val climateFingerprint = locationProfile.climateFingerprint
            ?.takeIf(LocationClimateFingerprint::isComplete)
            ?: return null
        val coldestMonthIndex = climateFingerprint.meanMonthlyTempC
            .indices
            .minByOrNull { index -> climateFingerprint.meanMonthlyTempC[index] }
            ?: return null
        for (offset in 1..12) {
            val monthIndex = (coldestMonthIndex + offset) % 12
            if (
                climateFingerprint.meanMonthlyTempC[monthIndex] >= minMeanTempC ||
                climateFingerprint.meanMonthlyMaxTempC[monthIndex] >= minMaxTempC
            ) {
                return LocalDate.of(year, monthIndex + 1, 1)
            }
        }
        return null
    }

    private fun coolingTriggeredOnsetDateForYear(
        year: Int,
        locationProfile: ForecastLocationProfile,
        maxMeanTempC: Double,
        maxMaxTempC: Double
    ): LocalDate? {
        val climateFingerprint = locationProfile.climateFingerprint
            ?.takeIf(LocationClimateFingerprint::isComplete)
            ?: return null
        val warmestMonthIndex = climateFingerprint.meanMonthlyTempC
            .indices
            .maxByOrNull { index -> climateFingerprint.meanMonthlyTempC[index] }
            ?: return null
        for (offset in 1..12) {
            val monthIndex = (warmestMonthIndex + offset) % 12
            if (
                climateFingerprint.meanMonthlyTempC[monthIndex] <= maxMeanTempC ||
                climateFingerprint.meanMonthlyMaxTempC[monthIndex] <= maxMaxTempC
            ) {
                return LocalDate.of(year, monthIndex + 1, 1)
            }
        }
        return null
    }

    private fun referenceOnsetDateForBand(
        year: Int,
        climateBand: ClimateBand,
        hemisphere: Hemisphere
    ): LocalDate {
        val northernDate = when (climateBand) {
            ClimateBand.EQUATORIAL -> LocalDate.of(year, 1, 15)
            ClimateBand.TROPICAL -> LocalDate.of(year, 2, 15)
            ClimateBand.SUBTROPICAL -> LocalDate.of(year, 3, 1)
            ClimateBand.TEMPERATE -> LocalDate.of(year, 3, 15)
            ClimateBand.COOL -> LocalDate.of(year, 4, 15)
        }
        return when (hemisphere) {
            Hemisphere.SOUTHERN -> northernDate.plusMonths(6)
            Hemisphere.EQUATORIAL -> LocalDate.of(year, 1, 15)
            Hemisphere.NORTHERN -> northernDate
        }
    }

    private fun referenceCoolingOnsetDateForBand(
        year: Int,
        climateBand: ClimateBand,
        hemisphere: Hemisphere
    ): LocalDate {
        val northernDate = when (climateBand) {
            ClimateBand.EQUATORIAL -> LocalDate.of(year, 11, 1)
            ClimateBand.TROPICAL -> LocalDate.of(year, 11, 1)
            ClimateBand.SUBTROPICAL -> LocalDate.of(year, 11, 1)
            ClimateBand.TEMPERATE -> LocalDate.of(year, 10, 1)
            ClimateBand.COOL -> LocalDate.of(year, 9, 15)
        }
        return when (hemisphere) {
            Hemisphere.SOUTHERN -> northernDate.plusMonths(6)
            Hemisphere.EQUATORIAL -> LocalDate.of(year, 11, 1)
            Hemisphere.NORTHERN -> northernDate
        }
    }

    private fun chillTimingAdjustmentDays(
        profile: SpeciesBloomProfile,
        locationProfile: ForecastLocationProfile
    ): Long {
        if (profile.key !in chillSensitiveKeys) return 0L
        val actualChillBand = locationProfile.effectiveChillHoursBand()
        if (actualChillBand == ChillHoursBand.UNKNOWN) return 0L
        val referenceChillBand = referenceChillHoursBandFor(profile.referenceZoneCode)
        val delta = actualChillBand.order - referenceChillBand.order
        return when {
            delta < 0 -> (-delta) * 10L
            delta > 0 -> -min(delta * 4L, 12L)
            else -> 0L
        }
    }

    private fun referenceChillHoursBandFor(zoneCode: String): ChillHoursBand {
        val zoneNumber = zoneCode.trim().takeWhile(Char::isDigit).toIntOrNull() ?: 7
        return when {
            zoneNumber >= 10 -> ChillHoursBand.UNDER_100
            zoneNumber >= 9 -> ChillHoursBand.H100_300
            zoneNumber >= 8 -> ChillHoursBand.H300_600
            zoneNumber >= 7 -> ChillHoursBand.H600_900
            else -> ChillHoursBand.H900_PLUS
        }
    }

    private fun forecastConfidenceFor(
        profile: SpeciesBloomProfile,
        locationProfile: ForecastLocationProfile,
        cultivarMatched: Boolean,
        source: ForecastSource
    ): ForecastConfidence {
        if (source == ForecastSource.CUSTOM) {
            return ForecastConfidence.HIGH
        }

        val hasClimateFingerprint = locationProfile.climateFingerprint?.isComplete() == true
        var confidence = when {
            cultivarMatched && (hasClimateFingerprint || !locationProfile.usdaZoneCode.isNullOrBlank()) -> ForecastConfidence.HIGH
            hasClimateFingerprint -> ForecastConfidence.MEDIUM
            else -> ForecastConfidence.MEDIUM
        }

        if (locationProfile.hemisphere == Hemisphere.SOUTHERN && !hasClimateFingerprint && locationProfile.latitudeDeg == null) {
            confidence = ForecastConfidence.LOW
        }
        if (profile.key in hemisphereShiftUnsafeKeys && !hasClimateFingerprint) {
            confidence = ForecastConfidence.LOW
        }
        if (locationProfile.microclimateFlags.any { it == MicroclimateFlag.GREENHOUSE || it == MicroclimateFlag.FROST_POCKET }) {
            confidence = ForecastConfidence.LOW
        }
        if (profile.key in chillSensitiveKeys) {
            confidence = when (locationProfile.effectiveChillHoursBand()) {
                ChillHoursBand.UNKNOWN -> ForecastConfidence.LOW
                ChillHoursBand.UNDER_100 -> ForecastConfidence.LOW
                else -> confidence
            }
        }
        if (locationProfile.latitudeDeg == null && locationProfile.usdaZoneCode.isNullOrBlank() && !hasClimateFingerprint) {
            confidence = ForecastConfidence.LOW
        }
        return confidence
    }

    private fun speciesCompatible(speciesProfile: SpeciesBloomProfile?, cultivarSpeciesKey: String): Boolean = when {
        speciesProfile == null -> true
        speciesProfile.key == cultivarSpeciesKey -> true
        speciesProfile.key == "citrus" && cultivarSpeciesKey in citrusSpeciesKeys -> true
        speciesProfile.key == "pear" && cultivarSpeciesKey in pearSpeciesKeys -> true
        speciesProfile.key == "raspberry" && cultivarSpeciesKey in raspberrySpeciesKeys -> true
        speciesProfile.key == "blueberry" && cultivarSpeciesKey in blueberrySpeciesKeys -> true
        speciesProfile.key == "fig" && cultivarSpeciesKey in figSpeciesKeys -> true
        speciesProfile.key == "mulberry" && cultivarSpeciesKey in mulberrySpeciesKeys -> true
        speciesProfile.key == "plum" && cultivarSpeciesKey in plumSpeciesKeys -> true
        else -> false
    }

    private fun overlaps(start: LocalDate, end: LocalDate, yearMonth: YearMonth): Boolean {
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        return !end.isBefore(monthStart) && !start.isAfter(monthEnd)
    }

    private fun speciesMatchContext(query: String?): SpeciesMatchContext {
        val normalizedQuery = normalize(query.orEmpty())
        return SpeciesMatchContext(
            normalizedQuery = normalizedQuery,
            queryProfile = normalizedQuery.takeIf(String::isNotBlank)?.let { resolveSpeciesProfile(query) }
        )
    }

    private fun resolveScopedSpeciesLabel(query: String?): String? {
        val normalizedQuery = normalize(query.orEmpty())
        if (normalizedQuery.isBlank()) return null
        resolveSpeciesAutocomplete(query.orEmpty())?.let { return it }
        return speciesAutocompleteCatalog
            .firstOrNull { normalize(it.species) == normalizedQuery }
            ?.species
    }

    private fun speciesMatchScore(context: SpeciesMatchContext, species: String): Int {
        if (context.normalizedQuery.isBlank()) return 0

        val speciesProfile = resolveSpeciesProfile(species)
        if (context.queryProfile != null && speciesProfile != null && speciesCompatible(context.queryProfile, speciesProfile.key)) {
            return 4
        }

        val normalizedSpecies = normalize(species)
        if (context.queryProfile?.key == "blueberry" && normalizedSpecies.endsWith("blueberry")) {
            return 4
        }
        if (context.queryProfile?.key == "fig" && normalizedSpecies.endsWith("fig")) {
            return 4
        }
        if (context.queryProfile?.key == "mulberry" && normalizedSpecies.endsWith("mulberry")) {
            return 4
        }
        if (context.queryProfile?.key == "plum" && normalizedSpecies.endsWith("plum")) {
            return 4
        }
        return when {
            normalizedSpecies == context.normalizedQuery -> 3
            normalizedSpecies.startsWith(context.normalizedQuery) -> 2
            normalizedSpecies.contains(context.normalizedQuery) -> 1
            else -> 0
        }
    }

    private fun matchScore(query: String, candidate: String): Int? = when {
        candidate == query -> 500
        candidate.startsWith(query) -> 400
        candidate.split(' ').any { it.startsWith(query) } -> 320
        candidate.contains(query) -> 220
        else -> null
    }

    private fun normalize(value: String): String = value
        .trim()
        .lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun PredictedBloomWindow.toSummary(today: LocalDate): BloomForecastSummary {
        val coarseWindow = coarseWindowLabel()
        val isCurrentWindow = !today.isBefore(startDate) && !today.isAfter(endDate)
        val daysUntilStart = if (isCurrentWindow) 0L else ChronoUnit.DAYS.between(today, startDate)
        val exactCountdownAllowed = patternType == BloomPatternType.SINGLE_ANNUAL && confidence != ForecastConfidence.LOW
        val headline = when (patternType) {
            BloomPatternType.CONTINUOUS -> if (isCurrentWindow) "Repeat" else "Watch"
            BloomPatternType.MULTI_WAVE -> if (isCurrentWindow) "Repeat" else "Watch"
            BloomPatternType.ALTERNATE_YEAR -> if (isCurrentWindow) "Active year" else "Watch"
            BloomPatternType.MANUAL_ONLY -> "Manual"
            BloomPatternType.SUPPRESSED -> "Unknown"
            BloomPatternType.SINGLE_ANNUAL -> when {
                isCurrentWindow && confidence == ForecastConfidence.LOW -> "Likely now"
                isCurrentWindow -> "Now"
                confidence == ForecastConfidence.LOW -> "Likely $coarseWindow"
                else -> "${daysUntilStart}d"
            }
        }
        val supportingLine = if (patternType != BloomPatternType.SINGLE_ANNUAL) {
            buildString {
                append(
                    when (patternType) {
                        BloomPatternType.CONTINUOUS -> "Active season now"
                        BloomPatternType.MULTI_WAVE -> "Repeat bloomer"
                        BloomPatternType.ALTERNATE_YEAR -> "Alternate-year bloom"
                        BloomPatternType.MANUAL_ONLY -> "Watch for buds"
                        BloomPatternType.SUPPRESSED -> "No automatic forecast"
                        BloomPatternType.SINGLE_ANNUAL -> coarseWindow
                    }
                )
                append(" | ")
                append(sourceLabel)
                append(" | ")
                append(confidenceLabel)
            }
        } else buildString {
            append(
                if (confidence == ForecastConfidence.LOW) {
                    coarseWindow
                } else {
                    formatCatalogRange(startDate, endDate)
                }
            )
            append(" | ")
            append(sourceLabel)
            append(" | ")
            append(confidenceLabel)
        }
        return BloomForecastSummary(
            headline = headline,
            supportingLine = supportingLine,
            source = source,
            confidence = confidence,
            patternType = patternType,
            patternLabel = patternType.detailPatternLabel(),
            timingLabel = if (patternType == BloomPatternType.SINGLE_ANNUAL) {
                if (confidence == ForecastConfidence.LOW) coarseWindow else formatCatalogRange(startDate, endDate)
            } else {
                formatCatalogRange(startDate, endDate)
            },
            exactCountdownAllowed = exactCountdownAllowed,
            daysUntilStart = daysUntilStart,
            isCurrentWindow = isCurrentWindow
        )
    }

    private fun BloomPatternType.detailPatternLabel(): String = when (this) {
        BloomPatternType.SINGLE_ANNUAL -> "Annual bloomer"
        BloomPatternType.MULTI_WAVE -> "Repeat bloomer"
        BloomPatternType.CONTINUOUS -> "Continuous bloomer"
        BloomPatternType.ALTERNATE_YEAR -> "Alternate-year bloomer"
        BloomPatternType.MANUAL_ONLY -> "Manual bloomer"
        BloomPatternType.SUPPRESSED -> "No automatic bloom forecast"
    }

    private fun PredictedBloomWindow.coarseWindowLabel(): String {
        val startMonth = startDate.format(DateTimeFormatter.ofPattern("MMM"))
        val endMonth = endDate.format(DateTimeFormatter.ofPattern("MMM"))
        return if (startMonth == endMonth) startMonth else "$startMonth-$endMonth"
    }

    private fun PollinationRequirement.toPollinationProfile(): PollinationProfile = when (this) {
        PollinationRequirement.SELF_FERTILE -> PollinationProfile(
            selfCompatibility = SelfCompatibility.SELF_FERTILE,
            pollinationMode = PollinationMode.SELF_POLLINATING
        )
        PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS -> PollinationProfile(
            selfCompatibility = SelfCompatibility.SELF_FERTILE,
            pollinationMode = PollinationMode.INSECT
        )
        PollinationRequirement.NEEDS_CROSS_POLLINATION -> PollinationProfile(
            selfCompatibility = SelfCompatibility.SELF_STERILE,
            pollinationMode = PollinationMode.HAND_HELPFUL
        )
        PollinationRequirement.CROSS_POLLINATION_RECOMMENDED -> PollinationProfile(
            selfCompatibility = SelfCompatibility.PARTIAL,
            pollinationMode = PollinationMode.INSECT
        )
        PollinationRequirement.PARTIAL_SELF_INCOMPATIBILITY -> PollinationProfile(
            selfCompatibility = SelfCompatibility.PARTIAL,
            pollinationMode = PollinationMode.HAND_HELPFUL
        )
        PollinationRequirement.POLLINATION_NOT_REQUIRED -> PollinationProfile(
            selfCompatibility = SelfCompatibility.UNKNOWN,
            pollinationMode = PollinationMode.UNKNOWN
        )
        PollinationRequirement.UNKNOWN -> PollinationProfile()
    }

    private fun SpeciesBloomProfile.supportsFamilyScopedAutocompleteExpansion(): Boolean = key in setOf(
        "citrus",
        "pear",
        "raspberry",
        "blueberry",
        "fig",
        "plum",
        "mulberry"
    )

    private fun String.toDisplayLabel(): String = split(' ')
        .filter(String::isNotBlank)
        .joinToString(" ") { part -> part.replaceFirstChar(Char::uppercase) }

    private fun String.toCatalogDisplayLabel(): String = if (
        any(Char::isUpperCase) || any { !it.isLetterOrDigit() && !it.isWhitespace() }
    ) {
        this
    } else {
        toDisplayLabel()
    }
}


