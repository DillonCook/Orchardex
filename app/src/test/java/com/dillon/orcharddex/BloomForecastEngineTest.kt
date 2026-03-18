package com.dillon.orcharddex

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
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
    fun resolveCultivarAutocomplete_matchesBananaAliases() {
        val match = BloomForecastEngine.resolveCultivarAutocomplete("Ice Cream", "Banana")

        assertThat(match).isNotNull()
        assertThat(match?.species).isEqualTo("Banana")
        assertThat(match?.cultivar).isEqualTo("Blue Java")
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
}
