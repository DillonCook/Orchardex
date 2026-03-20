package com.dillon.orcharddex.data.phenology

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.repository.displayName
import com.dillon.orcharddex.data.repository.speciesCultivarLabel
import java.time.LocalDate
import java.time.YearMonth

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
    val pollinationRequirement: PollinationRequirement = PollinationRequirement.UNKNOWN,
    val catalogSpeciesLabel: String = key
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
    val sourceLabel: String
)

data class SupportedCultivarCatalogEntry(
    val species: String,
    val cultivar: String,
    val aliases: List<String> = emptyList(),
    val pollinationRequirement: PollinationRequirement? = null
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
    private val speciesProfiles = listOf(
        SpeciesBloomProfile(
            "apple",
            setOf("apple", "malus", "malus domestica"),
            "7a",
            4,
            5,
            12,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        SpeciesBloomProfile(
            "pear",
            setOf("pear", "european pear"),
            "7a",
            3,
            30,
            10,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        SpeciesBloomProfile(
            "asian pear",
            setOf("asian pear", "nashi"),
            "7a",
            3,
            27,
            10,
            defaultPhase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        SpeciesBloomProfile("peach", setOf("peach"), "7a", 3, 20, 12, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("nectarine", setOf("nectarine"), "7a", 3, 18, 12, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("plum", setOf("plum", "japanese plum", "european plum"), "7a", 3, 18, 12),
        SpeciesBloomProfile(
            "apricot",
            setOf("apricot"),
            "7a",
            3,
            8,
            10,
            defaultPhase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        SpeciesBloomProfile(
            "sweet cherry",
            setOf("sweet cherry", "cherry"),
            "7a",
            3,
            24,
            10,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        SpeciesBloomProfile(
            "sour cherry",
            setOf("sour cherry", "tart cherry"),
            "7a",
            3,
            28,
            10,
            defaultPhase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        SpeciesBloomProfile("blueberry", setOf("blueberry"), "7a", 3, 29, 18, pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        SpeciesBloomProfile("grape", setOf("grape", "grapes", "grape vine"), "7a", 5, 20, 12, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("raspberry", setOf("raspberry"), "7a", 5, 10, 18, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("blackberry", setOf("blackberry"), "7a", 5, 12, 20, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("strawberry", setOf("strawberry"), "7a", 4, 10, 25, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("fig", setOf("fig"), "8a", 5, 10, 20, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("persimmon", setOf("persimmon"), "7b", 5, 15, 14),
        SpeciesBloomProfile("mulberry", setOf("mulberry"), "7a", 4, 15, 14),
        SpeciesBloomProfile("pomegranate", setOf("pomegranate"), "8b", 5, 8, 18, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile("avocado", setOf("avocado"), "10a", 3, 1, 45, pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED),
        SpeciesBloomProfile("mango", setOf("mango"), "10b", 2, 10, 35, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
        SpeciesBloomProfile(
            "lychee",
            setOf("lychee"),
            "10a",
            2,
            20,
            20,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
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
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
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
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
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
            catalogSpeciesLabel = "Abiu"
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
            catalogSpeciesLabel = "Caimito (star apple)"
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
            pollinationRequirement = PollinationRequirement.UNKNOWN
        ),
        SpeciesBloomProfile(
            "star fruit",
            setOf("star fruit", "starfruit", "carambola", "averrhoa carambola"),
            "10b",
            4,
            15,
            50,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        SpeciesBloomProfile(
            "sugar apple",
            setOf("sugar apple", "sweetsop", "sweet sop", "annona squamosa", "sitaphal"),
            "10b",
            3,
            15,
            95,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        SpeciesBloomProfile(
            "jackfruit",
            setOf("jackfruit", "jack fruit", "jack", "artocarpus heterophyllus", "kathal", "panas", "nangka", "nagka"),
            "10b",
            1,
            20,
            55,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
        ),
        SpeciesBloomProfile(
            "tamarind",
            setOf("tamarind", "tamarindo", "tamarindus indica", "imli", "ambli", "chinch", "sampalok", "makham", "makham waan"),
            "10b",
            5,
            15,
            100,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        SpeciesBloomProfile(
            "pineapple",
            setOf("pineapple", "piña", "pina", "ananas", "ananas comosus"),
            "10b",
            2,
            15,
            75,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        SpeciesBloomProfile("loquat", setOf("loquat"), "9b", 11, 20, 45),
        SpeciesBloomProfile("guava", setOf("guava"), "10a", 4, 20, 30, pollinationRequirement = PollinationRequirement.SELF_FERTILE),
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
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        SpeciesBloomProfile(
            "dragon fruit",
            setOf("dragon fruit", "dragonfruit", "pitaya"),
            "10a",
            6,
            1,
            70,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        SpeciesBloomProfile("sapodilla", setOf("sapodilla"), "10b", 4, 1, 45),
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
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
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        SpeciesBloomProfile(
            "white sapote",
            setOf("white sapote", "casimiroa edulis", "zapote blanco", "casimiroa"),
            "10b",
            11,
            15,
            170,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        SpeciesBloomProfile("jaboticaba", setOf("jaboticaba"), "10b", 3, 15, 60),
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
            catalogSpeciesLabel = "Sugarcane (cultivated hybrid complex)"
        ),
        SpeciesBloomProfile(
            "papaya",
            setOf("papaya", "carica papaya", "mamão", "mamao", "lechosa"),
            "10b",
            4,
            1,
            90,
            forecastBehavior = BloomForecastBehavior.MANUAL_ONLY,
            pollinationRequirement = PollinationRequirement.UNKNOWN
        )
    ) + BananaBloomCatalog.speciesProfiles + CitrusBloomCatalog.speciesProfiles

    // The first catalog is phase-based so it can scale to thousands of cultivars by adding rows,
    // without changing the date engine itself.
    private val cultivarProfiles = listOf(
        CultivarBloomProfile("apple", "Anna", phase = BloomPhase.EARLY),
        CultivarBloomProfile("apple", "Gala", aliases = setOf("Royal Gala"), phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Fuji", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "McIntosh", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("apple", "Ambrosia", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Braeburn", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Cortland", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Empire", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Ginger Gold", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Honeycrisp", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Liberty", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Granny Smith", phase = BloomPhase.MID),
        CultivarBloomProfile(
            "apple",
            "Golden Delicious",
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile("apple", "Pink Lady", aliases = setOf("Cripps Pink"), phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Pristine", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Red Delicious", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Jonagold", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Mutsu", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Winesap", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Zestar!", aliases = setOf("Zestar"), phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Arkansas Black", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Enterprise", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Grimes Golden", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Macoun", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Rome Beauty", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "SunCrisp", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Sweet Sixteen", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("pear", "Hosui", phase = BloomPhase.EARLY),
        CultivarBloomProfile("pear", "Bartlett", aliases = setOf("Max-Red Bartlett"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Anjou", aliases = setOf("Red Anjou"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Bosc", aliases = setOf("Beurre Bosc"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Comice", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("pear", "Kieffer", phase = BloomPhase.LATE),
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
            "plum",
            "Methley",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "plum",
            "Shiro",
            phase = BloomPhase.EARLY,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "plum",
            "Gulfbeauty",
            aliases = setOf("Gulf Beauty"),
            phase = BloomPhase.EARLY_MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "plum",
            "Gulfblaze",
            aliases = setOf("Gulf Blaze"),
            phase = BloomPhase.MID,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "plum",
            "Gulfrose",
            aliases = setOf("Gulf Rose"),
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.NEEDS_CROSS_POLLINATION
        ),
        CultivarBloomProfile(
            "plum",
            "Santa Rosa",
            phase = BloomPhase.MID_LATE,
            pollinationRequirement = PollinationRequirement.CROSS_POLLINATION_RECOMMENDED
        ),
        CultivarBloomProfile(
            "plum",
            "Stanley",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile(
            "plum",
            "Damson",
            phase = BloomPhase.LATE,
            pollinationRequirement = PollinationRequirement.SELF_FERTILE
        ),
        CultivarBloomProfile("apricot", "Blenheim", phase = BloomPhase.EARLY),
        CultivarBloomProfile("apricot", "Goldcot", phase = BloomPhase.MID),
        CultivarBloomProfile("apricot", "Moorpark", phase = BloomPhase.MID),
        CultivarBloomProfile("apricot", "Harcot", phase = BloomPhase.LATE),
        CultivarBloomProfile("nectarine", "Early Glo", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("nectarine", "Sunbest", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("nectarine", "UFRoyal", aliases = setOf("UF Royal"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("nectarine", "Fantasia", phase = BloomPhase.MID),
        CultivarBloomProfile("nectarine", "UFQueen", aliases = setOf("UF Queen"), phase = BloomPhase.MID),
        CultivarBloomProfile("nectarine", "Sunraycer", phase = BloomPhase.MID),
        CultivarBloomProfile("nectarine", "Sunmist", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("nectarine", "Suncoast", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("nectarine", "Snow Queen", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("nectarine", "Redgold", aliases = setOf("RedGold"), phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Patriot", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Beckyblue", aliases = setOf("Becky Blue"), phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Bonita", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Climax", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Snowchaser", aliases = setOf("Snow Chaser"), phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Arcadia", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Chickadee", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Kestrel", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Optimus", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Bluecrop", aliases = setOf("Blue Crop"), phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Chandler", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Emerald", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("blueberry", "Farthing", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("blueberry", "Jewel", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("blueberry", "Springhigh", aliases = setOf("Spring High"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("blueberry", "Star", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("blueberry", "Biloxi", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Legacy", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Misty", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "O'Neal", aliases = setOf("ONeal"), phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Sharpblue", aliases = setOf("Sharp Blue"), phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Rubel", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Sweetcrisp", aliases = setOf("Sweet Crisp"), phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Windsor", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Alapaha", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Austin", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Duke", phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Brightwell", phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Elliott", aliases = setOf("Elliot"), phase = BloomPhase.LATE)
        ,
        CultivarBloomProfile("blueberry", "Powderblue", aliases = setOf("Powder Blue"), phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Premier", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Tifblue", aliases = setOf("Tif Blue"), phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Vernon", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Woodard", phase = BloomPhase.LATE),
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
        coconut("Malayan Dwarf", aliases = setOf("Dwarf Malayan")),
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
        coconut("Maypan"),
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
            aliases = setOf("Purple Possum"),
            pollinationRequirement = PollinationRequirement.SELF_FERTILE_CROSS_BENEFITS
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
    ) + DragonFruitCatalog.cultivarProfiles + BananaBloomCatalog.cultivarProfiles + CitrusBloomCatalog.cultivarProfiles

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

    private data class ScoredSpeciesOption(
        val option: SpeciesAutocompleteOption,
        val score: Int
    )

    private val citrusSpeciesKeys = CitrusBloomCatalog.speciesProfiles
        .map(SpeciesBloomProfile::key)
        .toSet() - "citrus"

    private val speciesByKey = speciesProfiles.associateBy(SpeciesBloomProfile::key)

    private val speciesByAlias = speciesProfiles.flatMap { profile ->
        (profile.aliases + profile.key + profile.catalogSpeciesLabel.toCatalogDisplayLabel())
            .map { alias -> normalize(alias) to profile }
    }.toMap()

    private val speciesAutocompleteCatalog = speciesProfiles
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

    private val cultivarAutocompleteCatalog = cultivarProfiles
        .map { profile ->
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
        }
        .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }

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
        val exactMatches = speciesAutocompleteCatalog.filter { option ->
            normalize(option.species) == normalizedQuery ||
                option.aliases.any { normalize(it) == normalizedQuery }
        }
        if (exactMatches.isEmpty()) return null
        return exactMatches
            .map(SpeciesAutocompleteOption::species)
            .distinctBy(::normalize)
            .singleOrNull()
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

    fun pollinationRequirementFor(
        speciesInput: String,
        cultivarInput: String = ""
    ): PollinationRequirement? {
        val speciesProfile = speciesByAlias[normalize(speciesInput)] ?: return null
        val cultivarRequirement = matchCultivarProfile(speciesProfile, cultivarInput)?.pollinationRequirement
        val requirement = cultivarRequirement ?: speciesProfile.pollinationRequirement
        return requirement.takeUnless { it == PollinationRequirement.UNKNOWN }
    }

    fun cultivarAutocompleteOptions(
        query: String,
        speciesQuery: String? = null,
        limit: Int = 8
    ): List<CultivarAutocompleteOption> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()
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
                    speciesScore = speciesMatchScore(speciesQuery, option.species)
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

    fun everbearingPlants(trees: List<TreeEntity>): List<EverbearingPlant> = trees.mapNotNull { tree ->
        val profileMatch = tree.resolveProfileMatch() ?: return@mapNotNull null
        if (profileMatch.profile.forecastBehavior != BloomForecastBehavior.MANUAL_ONLY) {
            return@mapNotNull null
        }
        EverbearingPlant(
            treeId = tree.id,
            treeLabel = tree.displayName(),
            speciesLabel = speciesCultivarLabel(tree.species, tree.cultivar),
            detailLabel = "Continuous / repeat-bearing"
        )
    }.sortedWith(compareBy({ it.speciesLabel.lowercase() }, { it.treeLabel.lowercase() }))

    fun predictMonth(
        trees: List<TreeEntity>,
        yearMonth: YearMonth,
        zoneCode: String?,
        orchardRegionCode: String? = null
    ): List<PredictedBloomWindow> {
        val targetZone = UsdaZoneCatalog.resolve(zoneCode)
        return trees.mapNotNull { tree ->
            val profileMatch = tree.resolveProfileMatch() ?: return@mapNotNull null
            val speciesProfile = profileMatch.profile.withRegionalOverride(orchardRegionCode)
            if (speciesProfile.forecastBehavior != BloomForecastBehavior.WINDOW) {
                return@mapNotNull null
            }
            val phase = profileMatch.phase
            val referenceZone = UsdaZoneCatalog.resolve(speciesProfile.referenceZoneCode)
            val shiftDays = (referenceZone.index - targetZone.index) * speciesProfile.shiftDaysPerHalfZone
            sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
                .map { bloomWindowForYear(tree, speciesProfile, phase, shiftDays, profileMatch.cultivarMatched, it) }
                .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }
        }.sortedBy(PredictedBloomWindow::startDate)
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
        cultivarMatched: Boolean,
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
            sourceLabel = if (cultivarMatched) "cultivar-adjusted" else "species baseline"
        )
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

    private fun speciesCompatible(speciesProfile: SpeciesBloomProfile?, cultivarSpeciesKey: String): Boolean = when {
        speciesProfile == null -> true
        speciesProfile.key == cultivarSpeciesKey -> true
        speciesProfile.key == "citrus" && cultivarSpeciesKey in citrusSpeciesKeys -> true
        else -> false
    }

    private fun overlaps(start: LocalDate, end: LocalDate, yearMonth: YearMonth): Boolean {
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        return !end.isBefore(monthStart) && !start.isAfter(monthEnd)
    }

    private fun speciesMatchScore(query: String?, species: String): Int {
        val normalizedQuery = normalize(query.orEmpty())
        if (normalizedQuery.isBlank()) return 0

        val queryProfile = resolveSpeciesProfile(query)
        val speciesProfile = resolveSpeciesProfile(species)
        if (queryProfile != null && speciesProfile != null && speciesCompatible(queryProfile, speciesProfile.key)) {
            return 4
        }

        val normalizedSpecies = normalize(species)
        return when {
            normalizedSpecies == normalizedQuery -> 3
            normalizedSpecies.startsWith(normalizedQuery) -> 2
            normalizedSpecies.contains(normalizedQuery) -> 1
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
