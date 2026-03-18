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
    MANUAL_ONLY
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
    val forecastBehavior: BloomForecastBehavior = BloomForecastBehavior.WINDOW
)

data class CultivarBloomProfile(
    val speciesKey: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val phase: BloomPhase,
    val catalogSpeciesLabel: String = speciesKey
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
    val aliases: List<String> = emptyList()
)

data class CultivarAutocompleteOption(
    val species: String,
    val cultivar: String,
    val aliases: List<String> = emptyList()
)

object BloomForecastEngine {
    private val speciesProfiles = listOf(
        SpeciesBloomProfile("apple", setOf("apple", "malus", "malus domestica"), "7a", 4, 5, 12),
        SpeciesBloomProfile("pear", setOf("pear", "european pear"), "7a", 3, 30, 10),
        SpeciesBloomProfile("asian pear", setOf("asian pear", "nashi"), "7a", 3, 27, 10, defaultPhase = BloomPhase.EARLY),
        SpeciesBloomProfile("peach", setOf("peach"), "7a", 3, 20, 12),
        SpeciesBloomProfile("nectarine", setOf("nectarine"), "7a", 3, 18, 12),
        SpeciesBloomProfile("plum", setOf("plum", "japanese plum", "european plum"), "7a", 3, 18, 12),
        SpeciesBloomProfile("apricot", setOf("apricot"), "7a", 3, 8, 10, defaultPhase = BloomPhase.EARLY),
        SpeciesBloomProfile("sweet cherry", setOf("sweet cherry", "cherry"), "7a", 3, 24, 10),
        SpeciesBloomProfile("sour cherry", setOf("sour cherry", "tart cherry"), "7a", 3, 28, 10, defaultPhase = BloomPhase.MID_LATE),
        SpeciesBloomProfile("blueberry", setOf("blueberry"), "7a", 3, 29, 18),
        SpeciesBloomProfile("grape", setOf("grape", "grapes", "grape vine"), "7a", 5, 20, 12),
        SpeciesBloomProfile("raspberry", setOf("raspberry"), "7a", 5, 10, 18),
        SpeciesBloomProfile("blackberry", setOf("blackberry"), "7a", 5, 12, 20),
        SpeciesBloomProfile("strawberry", setOf("strawberry"), "7a", 4, 10, 25),
        SpeciesBloomProfile("fig", setOf("fig"), "8a", 5, 10, 20),
        SpeciesBloomProfile("persimmon", setOf("persimmon"), "7b", 5, 15, 14),
        SpeciesBloomProfile("mulberry", setOf("mulberry"), "7a", 4, 15, 14),
        SpeciesBloomProfile("pomegranate", setOf("pomegranate"), "8b", 5, 8, 18),
        SpeciesBloomProfile("avocado", setOf("avocado"), "10a", 3, 1, 45),
        SpeciesBloomProfile("mango", setOf("mango"), "10b", 2, 10, 35),
        SpeciesBloomProfile("lychee", setOf("lychee"), "10a", 2, 20, 20),
        SpeciesBloomProfile("loquat", setOf("loquat"), "9b", 11, 20, 45),
        SpeciesBloomProfile("guava", setOf("guava"), "10a", 4, 20, 30),
        SpeciesBloomProfile("passionfruit", setOf("passionfruit", "passion fruit"), "10a", 4, 15, 40),
        SpeciesBloomProfile("dragon fruit", setOf("dragon fruit", "dragonfruit", "pitaya"), "10a", 6, 1, 70),
        SpeciesBloomProfile("sapodilla", setOf("sapodilla"), "10b", 4, 1, 45),
        SpeciesBloomProfile("jaboticaba", setOf("jaboticaba"), "10b", 3, 15, 60),
        SpeciesBloomProfile("papaya", setOf("papaya"), "10b", 4, 1, 90)
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
        CultivarBloomProfile("apple", "Golden Delicious", phase = BloomPhase.MID),
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
        CultivarBloomProfile("sweet cherry", "Black Tartarian", phase = BloomPhase.EARLY),
        CultivarBloomProfile("sweet cherry", "Lapins", phase = BloomPhase.EARLY),
        CultivarBloomProfile("sweet cherry", "Bing", phase = BloomPhase.MID),
        CultivarBloomProfile("sweet cherry", "Rainier", phase = BloomPhase.MID),
        CultivarBloomProfile("sweet cherry", "Stella", phase = BloomPhase.MID),
        CultivarBloomProfile("sour cherry", "Montmorency", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("plum", "Methley", phase = BloomPhase.EARLY),
        CultivarBloomProfile("plum", "Shiro", phase = BloomPhase.EARLY),
        CultivarBloomProfile("plum", "Gulfbeauty", aliases = setOf("Gulf Beauty"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("plum", "Gulfblaze", aliases = setOf("Gulf Blaze"), phase = BloomPhase.MID),
        CultivarBloomProfile("plum", "Gulfrose", aliases = setOf("Gulf Rose"), phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("plum", "Santa Rosa", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("plum", "Stanley", phase = BloomPhase.LATE),
        CultivarBloomProfile("plum", "Damson", phase = BloomPhase.LATE),
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
        CultivarBloomProfile("blueberry", "Woodard", phase = BloomPhase.LATE)
    ) + BananaBloomCatalog.cultivarProfiles + CitrusBloomCatalog.cultivarProfiles

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

    private val citrusSpeciesKeys = CitrusBloomCatalog.speciesProfiles
        .map(SpeciesBloomProfile::key)
        .toSet() - "citrus"

    private val speciesByAlias = speciesProfiles.flatMap { profile ->
        (profile.aliases + profile.key).map { alias -> normalize(alias) to profile }
    }.toMap()

    private val cultivarAutocompleteCatalog = cultivarProfiles
        .map { profile ->
            CultivarAutocompleteOption(
                species = profile.catalogSpeciesLabel.toDisplayLabel(),
                cultivar = profile.cultivar,
                aliases = profile.aliases
                    .filterNot { normalize(it) == normalize(profile.cultivar) }
                    .sortedBy(String::lowercase)
            )
        }
        .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }

    fun supportedZoneLabels(): List<String> = UsdaZoneCatalog.zones.map(UsdaZoneDefinition::label)

    fun zoneCodeFromLabel(label: String): String = UsdaZoneCatalog.zones
        .firstOrNull { it.label == label }
        ?.code
        ?: label.substringBefore(' ').trim().lowercase()

    fun zoneLabelForCode(code: String?): String = UsdaZoneCatalog.resolve(code).label

    fun effectiveZoneCode(code: String?): String = UsdaZoneCatalog.resolve(code).code

    fun supportedSpeciesCatalog(): List<String> = (
        speciesProfiles.map { it.key.toDisplayLabel() } + cultivarAutocompleteCatalog.map(CultivarAutocompleteOption::species)
        )
        .distinctBy(::normalize)
        .sortedBy(String::lowercase)

    fun supportedCultivarCatalog(): List<SupportedCultivarCatalogEntry> = cultivarAutocompleteCatalog
        .map { SupportedCultivarCatalogEntry(species = it.species, cultivar = it.cultivar, aliases = it.aliases) }
        .sortedWith(compareBy({ it.species.lowercase() }, { it.cultivar.lowercase() }))

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
        val exactMatches = cultivarAutocompleteCatalog.filter { option ->
            normalize(option.cultivar) == normalizedQuery ||
                option.aliases.any { normalize(it) == normalizedQuery }
        }
        if (exactMatches.isEmpty()) return null
        return exactMatches
            .distinctBy { option -> normalize("${option.species}|${option.cultivar}") }
            .singleOrNull()
    }

    fun predictMonth(
        trees: List<TreeEntity>,
        yearMonth: YearMonth,
        zoneCode: String?
    ): List<PredictedBloomWindow> {
        val targetZone = UsdaZoneCatalog.resolve(zoneCode)
        return trees.mapNotNull { tree ->
            val profileMatch = tree.resolveProfileMatch() ?: return@mapNotNull null
            val speciesProfile = profileMatch.profile
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
        val cultivarKey = normalize(cultivar)
        val cultivarMatch = cultivarProfiles.firstOrNull { cultivarProfile ->
            cultivarKey.isNotBlank() &&
                speciesCompatible(speciesMatch, cultivarProfile.speciesKey) &&
                (
                    cultivarKey == normalize(cultivarProfile.cultivar) ||
                        cultivarProfile.aliases.any { normalize(it) == cultivarKey }
                    )
        }
        if (cultivarMatch != null) {
            val profile = speciesProfiles.firstOrNull { it.key == cultivarMatch.speciesKey }
            if (profile != null) {
                return ProfileMatch(profile = profile, phase = cultivarMatch.phase, cultivarMatched = true)
            }
        }
        val profile = speciesMatch ?: return null
        return ProfileMatch(profile = profile, phase = profile.defaultPhase, cultivarMatched = false)
    }

    private fun TreeEntity.speciesProfile(): SpeciesBloomProfile? {
        val normalizedSpecies = normalize(species)
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
}
