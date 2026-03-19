package com.dillon.orcharddex

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.PollinationRequirement
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth
import org.junit.Test

class BloomForecastEngineTest {
    @Test
    fun supportedCultivarCatalog_includesCommonBananas() {
        val bananaCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Banana" }
            .map { it.cultivar }

        assertThat(bananaCultivars).containsAtLeast(
            "Dwarf Cavendish",
            "Blue Java",
            "Goldfinger",
            "Mona Lisa",
            "Rajapuri",
            "Sweetheart"
        )
    }

    @Test
    fun supportedCultivarCatalog_includesDragonFruitAndPollinationMetadata() {
        val dragonFruitCultivars = BloomForecastEngine.supportedCultivarCatalog()
            .filter { it.species == "Dragon Fruit" }
            .associateBy { it.cultivar }

        assertThat(dragonFruitCultivars.keys).containsAtLeast(
            "American Beauty",
            "Asunta 6",
            "AX",
            "Cosmic Charlie",
            "Edgar's Baby",
            "Fruit Punch",
            "Medusa",
            "Sugar Dragon",
            "Thai Dragon",
            "Townsend Pink",
            "Tricia",
            "Vietnamese White",
            "Voodoo Child"
        )
        assertThat(dragonFruitCultivars.getValue("American Beauty").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("AX").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Townsend Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(dragonFruitCultivars.getValue("Tricia").pollinationRequirement)
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(dragonFruitCultivars.getValue("Fruit Punch").pollinationRequirement)
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(dragonFruitCultivars.getValue("Dennis Pale Pink").pollinationRequirement)
            .isEqualTo(PollinationRequirement.UNKNOWN)
    }

    @Test
    fun resolveCultivarAutocomplete_matchesBananaAliases() {
        val match = BloomForecastEngine.resolveCultivarAutocomplete("Ice Cream", "Banana")

        assertThat(match).isNotNull()
        assertThat(match?.species).isEqualTo("Banana")
        assertThat(match?.cultivar).isEqualTo("Blue Java")
    }

    @Test
    fun resolveCultivarAutocomplete_matchesDragonFruitAliases() {
        val cometMatch = BloomForecastEngine.resolveCultivarAutocomplete("Haley's Comet", "Dragon fruit")
        val thaiMatch = BloomForecastEngine.resolveCultivarAutocomplete("Thai Red", "Dragon fruit")
        val asuntaMatch = BloomForecastEngine.resolveCultivarAutocomplete("Asunta 6 (Paco)", "Dragon fruit")

        assertThat(cometMatch?.cultivar).isEqualTo("Halley's Comet")
        assertThat(thaiMatch?.cultivar).isEqualTo("Thai Dragon")
        assertThat(asuntaMatch?.cultivar).isEqualTo("Asunta 6")
    }

    @Test
    fun pollinationRequirementFor_resolvesCultivarAndSpeciesDefaults() {
        assertThat(BloomForecastEngine.pollinationRequirementFor("Banana", "Goldfinger"))
            .isEqualTo(PollinationRequirement.POLLINATION_NOT_REQUIRED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Sugar Dragon"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "AX"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Townsend Pink"))
            .isEqualTo(PollinationRequirement.SELF_FERTILE)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Fruit Punch"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Dragon Fruit", "Dennis Pale Pink"))
            .isNull()
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Golden Delicious"))
            .isEqualTo(PollinationRequirement.CROSS_POLLINATION_RECOMMENDED)
        assertThat(BloomForecastEngine.pollinationRequirementFor("Apple", "Honeycrisp"))
            .isEqualTo(PollinationRequirement.NEEDS_CROSS_POLLINATION)
    }

    @Test
    fun predictMonth_skipsAutomaticBananaForecasts() {
        val bananaTree = TreeEntity(
            id = "banana-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = null,
            species = "Banana",
            cultivar = "Goldfinger",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )

        val windows = BloomForecastEngine.predictMonth(
            trees = listOf(bananaTree),
            yearMonth = YearMonth.of(2026, 6),
            zoneCode = "10b"
        )

        assertThat(windows).isEmpty()
    }

    @Test
    fun everbearingPlants_returnsTrackedBananasForSeparateDashboardListing() {
        val bananaTree = TreeEntity(
            id = "banana-1",
            orchardName = "Home",
            sectionName = "Tropics",
            nickname = "Plant 2",
            species = "Banana",
            cultivar = "Goldfinger",
            rootstock = null,
            source = null,
            purchaseDate = null,
            plantedDate = 1_700_000_000_000,
            plantType = PlantType.IN_GROUND,
            containerSize = null,
            sunExposure = null,
            frostSensitivity = FrostSensitivityLevel.MEDIUM,
            frostSensitivityNote = null,
            irrigationNote = null,
            status = TreeStatus.ACTIVE,
            hasFruitedBefore = false,
            notes = "",
            tags = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val appleTree = bananaTree.copy(
            id = "apple-1",
            species = "Apple",
            cultivar = "Anna",
            nickname = "Back row"
        )

        val everbearing = BloomForecastEngine.everbearingPlants(listOf(bananaTree, appleTree))

        assertThat(everbearing).hasSize(1)
        assertThat(everbearing.single().treeId).isEqualTo("banana-1")
        assertThat(everbearing.single().treeLabel).isEqualTo("Plant 2 (Goldfinger)")
        assertThat(everbearing.single().speciesLabel).isEqualTo("Banana • Goldfinger")
    }
}
