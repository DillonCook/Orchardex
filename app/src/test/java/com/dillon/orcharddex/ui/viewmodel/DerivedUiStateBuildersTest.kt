package com.dillon.orcharddex.ui.viewmodel

import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DerivedUiStateBuildersTest {
    @Test
    fun buildTreeFormAutocompleteUiState_blankCultivarUsesStableSpeciesLane() {
        val autocompleteState = buildTreeFormAutocompleteUiState(
            state = TreeFormState(species = "Mango", cultivar = ""),
            knownTrees = listOf(
                testTree(id = "1", species = "Mangifera indica", cultivar = "Carrie"),
                testTree(id = "2", species = "Guava", cultivar = "Ruby Supreme")
            ),
            supportedSpecies = listOf("Mango", "Guava"),
            activeSearchField = TreeFormSearchField.CULTIVAR
        )

        assertThat(autocompleteState.stableSpeciesSelection).isEqualTo("Mango")
        assertThat(autocompleteState.cultivarSuggestionsTitle).isEqualTo("Known Mango cultivars")
        assertThat(autocompleteState.cultivarSuggestions.map { it.cultivar }).contains("Carrie")
        assertThat(autocompleteState.cultivarSuggestions.map { it.species }.distinct()).contains("Mango")
    }

    @Test
    fun buildTreeFormAutocompleteUiState_usesOrchardAdjustedAutoBloomLabel() {
        val autocompleteState = buildTreeFormAutocompleteUiState(
            state = TreeFormState(species = "Dragon fruit", cultivar = ""),
            knownTrees = emptyList(),
            supportedSpecies = listOf("Dragon fruit"),
            activeSearchField = null,
            locationProfile = ForecastLocationProfile(
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a",
                climateFingerprint = LocationClimateFingerprint(
                    source = "NASA POWER",
                    fetchedAt = 1L,
                    meanMonthlyTempC = listOf(15.0, 15.0, 17.0, 19.0, 22.0, 25.0, 28.0, 28.0, 26.0, 22.0, 18.0, 15.0),
                    meanMonthlyMinTempC = listOf(9.0, 9.0, 11.0, 13.0, 16.0, 19.0, 22.0, 22.0, 20.0, 16.0, 12.0, 9.0),
                    meanMonthlyMaxTempC = listOf(22.0, 23.0, 25.0, 27.0, 30.0, 34.0, 37.0, 37.0, 34.0, 30.0, 26.0, 22.0)
                )
            )
        )

        assertThat(autocompleteState.autoBloomTimingLabel).isEqualTo("This orchard - May 1 - Oct 31")
    }

    @Test
    fun buildDexBrowserUiState_filtersPlantsAndGroupsBlocks() {
        val browserState = buildDexBrowserUiState(
            state = DexBrowserUiState(
                search = "pick",
                speciesFilter = "Mango"
            ),
            plants = listOf(
                testTreeListItem(id = "1", species = "Mango", cultivar = "Carrie", sectionName = "North"),
                testTreeListItem(
                    id = "2",
                    species = "Mango",
                    cultivar = "Pickering",
                    sectionName = "North",
                    nurseryStage = com.dillon.orcharddex.data.model.NurseryStage.SALE_READY
                ),
                testTreeListItem(id = "3", species = "Guava", cultivar = "Ruby Supreme", sectionName = "South")
            ),
            history = listOf(
                HistoryEntryModel(
                    id = "h1",
                    kind = ActivityKind.HARVEST,
                    treeId = "2",
                    treeLabel = "Pickering Mango",
                    orchardName = "Test Orchard",
                    species = "Mango",
                    cultivar = "Pickering",
                    date = 100L,
                    createdAt = 100L,
                    title = "Harvest",
                    preview = "",
                    notes = ""
                ),
                HistoryEntryModel(
                    id = "e1",
                    kind = ActivityKind.EVENT,
                    treeId = "1",
                    treeLabel = "Carrie Mango",
                    orchardName = "Test Orchard",
                    species = "Mango",
                    cultivar = "Carrie",
                    date = 80L,
                    createdAt = 80L,
                    title = "Bloom observed",
                    preview = "",
                    notes = "",
                    eventType = EventType.BLOOM
                )
            ),
            remindersByTreeId = mapOf(
                "2" to listOf(
                    ReminderListItem(
                        reminder = ReminderEntity(
                            id = "r1",
                            treeId = "2",
                            title = "Feed tree",
                            notes = "",
                            dueAt = 1000L,
                            hasTime = false,
                            recurrenceType = RecurrenceType.NONE,
                            recurrenceIntervalDays = null,
                            enabled = true,
                            completedAt = null,
                            leadTimeMode = LeadTimeMode.SAME_DAY,
                            customLeadTimeHours = null,
                            createdAt = 0L,
                            updatedAt = 0L
                        ),
                        treeLabel = "Pickering Mango",
                        species = "Mango"
                    )
                )
            )
        )

        assertThat(browserState.speciesOptions).containsExactly("Guava", "Mango")
        assertThat(browserState.filteredPlants.map { it.tree.id }).containsExactly("2")
        assertThat(browserState.groupedBlocks).hasSize(1)
        assertThat(browserState.groupedBlocks.first().saleReadyCount).isEqualTo(1)
        assertThat(browserState.groupedBlocks.first().dueTaskCount).isEqualTo(1)
        assertThat(browserState.observationsByTreeId.getValue("2").single().isHarvest).isTrue()
    }

    private fun testTree(
        id: String,
        species: String,
        cultivar: String,
        sectionName: String = "",
        nurseryStage: com.dillon.orcharddex.data.model.NurseryStage = com.dillon.orcharddex.data.model.NurseryStage.NONE
    ) = TreeEntity(
        id = id,
        orchardName = "Test Orchard",
        sectionName = sectionName,
        nickname = null,
        species = species,
        cultivar = cultivar,
        rootstock = null,
        source = null,
        purchaseDate = null,
        plantedDate = 0L,
        plantType = PlantType.IN_GROUND,
        containerSize = null,
        sunExposure = null,
        frostSensitivity = FrostSensitivityLevel.MEDIUM,
        frostSensitivityNote = null,
        irrigationNote = null,
        status = TreeStatus.ACTIVE,
        nurseryStage = nurseryStage,
        notes = "",
        tags = "",
        bloomTimingMode = BloomTimingMode.AUTO,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun testTreeListItem(
        id: String,
        species: String,
        cultivar: String,
        sectionName: String = "",
        nurseryStage: com.dillon.orcharddex.data.model.NurseryStage = com.dillon.orcharddex.data.model.NurseryStage.NONE
    ) = TreeListItem(
        tree = testTree(
            id = id,
            species = species,
            cultivar = cultivar,
            sectionName = sectionName,
            nurseryStage = nurseryStage
        ),
        mainPhotoPath = null,
        location = null
    )
}
