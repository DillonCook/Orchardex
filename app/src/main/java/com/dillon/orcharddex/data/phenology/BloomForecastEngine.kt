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

data class SpeciesBloomProfile(
    val key: String,
    val aliases: Set<String>,
    val referenceZoneCode: String,
    val startMonth: Int,
    val startDay: Int,
    val durationDays: Long,
    val shiftDaysPerHalfZone: Long = 4,
    val defaultPhase: BloomPhase = BloomPhase.MID
)

data class CultivarBloomProfile(
    val speciesKey: String,
    val cultivar: String,
    val aliases: Set<String> = emptySet(),
    val phase: BloomPhase
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
        SpeciesBloomProfile("citrus", setOf("citrus"), "9b", 3, 5, 25),
        SpeciesBloomProfile("lemon", setOf("lemon", "meyer lemon", "eureka lemon"), "9b", 3, 1, 25),
        SpeciesBloomProfile("orange", setOf("orange", "navel orange", "valencia orange"), "9b", 3, 10, 20),
        SpeciesBloomProfile("mandarin", setOf("mandarin", "tangerine", "clementine"), "9b", 3, 5, 20),
        SpeciesBloomProfile("grapefruit", setOf("grapefruit"), "9b", 3, 8, 20),
        SpeciesBloomProfile("avocado", setOf("avocado"), "10a", 3, 1, 45),
        SpeciesBloomProfile("mango", setOf("mango"), "10b", 2, 10, 35),
        SpeciesBloomProfile("lychee", setOf("lychee"), "10a", 2, 20, 20),
        SpeciesBloomProfile("loquat", setOf("loquat"), "9b", 11, 20, 45),
        SpeciesBloomProfile("guava", setOf("guava"), "10a", 4, 20, 30),
        SpeciesBloomProfile("passionfruit", setOf("passionfruit", "passion fruit"), "10a", 4, 15, 40),
        SpeciesBloomProfile("dragon fruit", setOf("dragon fruit", "dragonfruit", "pitaya"), "10a", 6, 1, 70),
        SpeciesBloomProfile("sapodilla", setOf("sapodilla"), "10b", 4, 1, 45),
        SpeciesBloomProfile("jaboticaba", setOf("jaboticaba"), "10b", 3, 15, 60),
        SpeciesBloomProfile("banana", setOf("banana"), "10b", 5, 1, 90),
        SpeciesBloomProfile("papaya", setOf("papaya"), "10b", 4, 1, 90)
    )

    // The first catalog is phase-based so it can scale to thousands of cultivars by adding rows,
    // without changing the date engine itself.
    private val cultivarProfiles = listOf(
        CultivarBloomProfile("apple", "Anna", phase = BloomPhase.EARLY),
        CultivarBloomProfile("apple", "Gala", aliases = setOf("Royal Gala"), phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Fuji", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "McIntosh", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("apple", "Honeycrisp", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("apple", "Granny Smith", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Golden Delicious", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Pink Lady", aliases = setOf("Cripps Pink"), phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Red Delicious", phase = BloomPhase.MID),
        CultivarBloomProfile("apple", "Jonagold", phase = BloomPhase.MID),
        CultivarBloomProfile("pear", "Hosui", phase = BloomPhase.EARLY),
        CultivarBloomProfile("pear", "Bartlett", aliases = setOf("Max-Red Bartlett"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Anjou", aliases = setOf("Red Anjou"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Bosc", aliases = setOf("Beurre Bosc"), phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("pear", "Comice", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("pear", "Kieffer", phase = BloomPhase.LATE),
        CultivarBloomProfile("peach", "Early Redhaven", phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "Eva's Pride", phase = BloomPhase.EARLY),
        CultivarBloomProfile("peach", "Elberta", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("peach", "4th of July", aliases = setOf("Fourth of July"), phase = BloomPhase.MID),
        CultivarBloomProfile("peach", "Belle of Georgia", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("peach", "Reliance", phase = BloomPhase.MID_LATE),
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
        CultivarBloomProfile("plum", "Santa Rosa", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("plum", "Stanley", phase = BloomPhase.LATE),
        CultivarBloomProfile("plum", "Damson", phase = BloomPhase.LATE),
        CultivarBloomProfile("apricot", "Blenheim", phase = BloomPhase.EARLY),
        CultivarBloomProfile("apricot", "Goldcot", phase = BloomPhase.MID),
        CultivarBloomProfile("apricot", "Moorpark", phase = BloomPhase.MID),
        CultivarBloomProfile("apricot", "Harcot", phase = BloomPhase.LATE),
        CultivarBloomProfile("nectarine", "Early Glo", phase = BloomPhase.EARLY_MID),
        CultivarBloomProfile("nectarine", "Fantasia", phase = BloomPhase.MID),
        CultivarBloomProfile("nectarine", "Snow Queen", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("nectarine", "Redgold", aliases = setOf("RedGold"), phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Patriot", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Climax", phase = BloomPhase.EARLY),
        CultivarBloomProfile("blueberry", "Bluecrop", aliases = setOf("Blue Crop"), phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Chandler", phase = BloomPhase.MID),
        CultivarBloomProfile("blueberry", "Rubel", phase = BloomPhase.MID_LATE),
        CultivarBloomProfile("blueberry", "Duke", phase = BloomPhase.LATE),
        CultivarBloomProfile("blueberry", "Elliott", aliases = setOf("Elliot"), phase = BloomPhase.LATE)
    )

    private val speciesByAlias = speciesProfiles.flatMap { profile ->
        (profile.aliases + profile.key).map { alias -> normalize(alias) to profile }
    }.toMap()

    fun supportedZoneLabels(): List<String> = UsdaZoneCatalog.zones.map(UsdaZoneDefinition::label)

    fun zoneCodeFromLabel(label: String): String = UsdaZoneCatalog.zones
        .firstOrNull { it.label == label }
        ?.code
        ?: label.substringBefore(' ').trim().lowercase()

    fun zoneLabelForCode(code: String?): String = UsdaZoneCatalog.resolve(code).label

    fun effectiveZoneCode(code: String?): String = UsdaZoneCatalog.resolve(code).code

    fun predictMonth(
        trees: List<TreeEntity>,
        yearMonth: YearMonth,
        zoneCode: String?
    ): List<PredictedBloomWindow> {
        val targetZone = UsdaZoneCatalog.resolve(zoneCode)
        return trees.mapNotNull { tree ->
            val profileMatch = tree.resolveProfileMatch() ?: return@mapNotNull null
            val speciesProfile = profileMatch.first
            val phase = profileMatch.second
            val referenceZone = UsdaZoneCatalog.resolve(speciesProfile.referenceZoneCode)
            val shiftDays = (referenceZone.index - targetZone.index) * speciesProfile.shiftDaysPerHalfZone
            sequenceOf(yearMonth.year - 1, yearMonth.year, yearMonth.year + 1)
                .map { bloomWindowForYear(tree, speciesProfile, phase, shiftDays, it) }
                .firstOrNull { window -> overlaps(window.startDate, window.endDate, yearMonth) }
        }.sortedBy(PredictedBloomWindow::startDate)
    }

    private fun bloomWindowForYear(
        tree: TreeEntity,
        profile: SpeciesBloomProfile,
        phase: BloomPhase,
        shiftDays: Long,
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
            sourceLabel = if (tree.cultivar.isBlank()) "species baseline" else "cultivar-adjusted"
        )
    }

    private fun TreeEntity.resolveProfileMatch(): Pair<SpeciesBloomProfile, BloomPhase>? {
        val cultivarMatch = cultivarProfiles.firstOrNull { cultivarProfile ->
            val cultivarKey = normalize(cultivar)
            cultivarKey.isNotBlank() && (
                cultivarKey == normalize(cultivarProfile.cultivar) ||
                    cultivarProfile.aliases.any { normalize(it) == cultivarKey }
                )
        }
        if (cultivarMatch != null) {
            val profile = speciesProfiles.firstOrNull { it.key == cultivarMatch.speciesKey }
            if (profile != null) {
                return profile to cultivarMatch.phase
            }
        }
        val profile = speciesProfile() ?: return null
        return profile to profile.defaultPhase
    }

    private fun TreeEntity.speciesProfile(): SpeciesBloomProfile? {
        val normalizedSpecies = normalize(species)
        return speciesByAlias[normalizedSpecies]
            ?: speciesByAlias.entries.firstOrNull { normalizedSpecies.contains(it.key) }?.value
    }

    private fun overlaps(start: LocalDate, end: LocalDate, yearMonth: YearMonth): Boolean {
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        return !end.isBefore(monthStart) && !start.isAfter(monthEnd)
    }

    private fun normalize(value: String): String = value
        .trim()
        .lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
