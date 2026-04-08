package com.dillon.orcharddex.ui.viewmodel

import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.NurseryStage
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PollinationProfile
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption
import com.dillon.orcharddex.ui.autocompleteSpeciesOptions
import com.dillon.orcharddex.ui.cultivarDisplayOptions
import com.dillon.orcharddex.ui.existingCultivarAutocompleteOptions
import com.dillon.orcharddex.ui.normalizeAutocomplete
import com.dillon.orcharddex.ui.resolveExistingCultivarAutocomplete
import com.dillon.orcharddex.ui.resolveStableSpeciesSelection

enum class TreeFormSearchField {
    SPECIES,
    CULTIVAR
}

data class TreeFormAutocompleteUiState(
    val stableSpeciesSelection: String? = null,
    val speciesSuggestions: List<String> = emptyList(),
    val cultivarSuggestions: List<CultivarAutocompleteOption> = emptyList(),
    val cultivarSuggestionsTitle: String = "Cultivar matches",
    val matchedCultivarOption: CultivarAutocompleteOption? = null,
    val cultivarAliasOptions: List<String> = emptyList(),
    val catalogSpeciesSelection: String? = null,
    val catalogCultivarSelection: String = "",
    val pollinationProfile: PollinationProfile? = null,
    val autoBloomTimingLabel: String? = null
)

internal fun buildTreeFormAutocompleteUiState(
    state: TreeFormState,
    knownTrees: List<TreeEntity>,
    supportedSpecies: List<String>,
    activeSearchField: TreeFormSearchField?,
    locationProfile: ForecastLocationProfile = ForecastLocationProfile(),
    orchardRegionCode: String? = null
): TreeFormAutocompleteUiState {
    val speciesCatalog = (knownTrees.map(TreeEntity::species) + supportedSpecies)
        .filter(String::isNotBlank)
        .distinctBy(::normalizeAutocomplete)
        .sortedBy(String::lowercase)
    val normalizedSpeciesQuery = normalizeAutocomplete(state.species)
    val stableSpeciesSelection = resolveStableSpeciesSelection(state.species, speciesCatalog)
    val speciesSuggestions = if (
        activeSearchField == TreeFormSearchField.SPECIES &&
        normalizedSpeciesQuery.length >= 2
    ) {
        (
            BloomForecastEngine.speciesAutocompleteOptions(state.species) +
                autocompleteSpeciesOptions(state.species, speciesCatalog)
            )
            .distinctBy(::normalizeAutocomplete)
            .take(8)
    } else {
        emptyList()
    }

    val cultivarSuggestions = when {
        activeSearchField != TreeFormSearchField.CULTIVAR -> emptyList()
        state.cultivar.isBlank() && stableSpeciesSelection != null -> {
            (
                BloomForecastEngine.cultivarAutocompleteOptions(
                    query = "",
                    speciesQuery = stableSpeciesSelection
                ) + existingCultivarAutocompleteOptions(
                    query = "",
                    speciesQuery = stableSpeciesSelection,
                    trees = knownTrees
                )
                )
                .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
                .take(8)
        }
        state.cultivar.isNotBlank() -> {
            val speciesQuery = stableSpeciesSelection ?: state.species
            (
                BloomForecastEngine.cultivarAutocompleteOptions(
                    query = state.cultivar,
                    speciesQuery = speciesQuery
                ) + existingCultivarAutocompleteOptions(
                    query = state.cultivar,
                    speciesQuery = speciesQuery,
                    trees = knownTrees
                )
                )
                .distinctBy { normalizeAutocomplete("${it.species}|${it.cultivar}") }
                .take(8)
        }
        else -> emptyList()
    }

    val matchedCultivarOption = if (state.cultivar.isBlank()) {
        null
    } else {
        BloomForecastEngine.resolveCultivarAutocomplete(state.cultivar, stableSpeciesSelection ?: state.species)
            ?: resolveExistingCultivarAutocomplete(state.cultivar, knownTrees)
    }
    val catalogSpeciesSelection = matchedCultivarOption?.species ?: stableSpeciesSelection
    val catalogCultivarSelection = matchedCultivarOption?.cultivar ?: state.cultivar

    return TreeFormAutocompleteUiState(
        stableSpeciesSelection = stableSpeciesSelection,
        speciesSuggestions = speciesSuggestions,
        cultivarSuggestions = cultivarSuggestions,
        cultivarSuggestionsTitle = if (state.cultivar.isBlank() && stableSpeciesSelection != null) {
            "Known ${stableSpeciesSelection.trim()} cultivars"
        } else {
            "Cultivar matches"
        },
        matchedCultivarOption = matchedCultivarOption,
        cultivarAliasOptions = cultivarDisplayOptions(matchedCultivarOption),
        catalogSpeciesSelection = catalogSpeciesSelection,
        catalogCultivarSelection = catalogCultivarSelection,
        pollinationProfile = catalogSpeciesSelection?.let { species ->
            BloomForecastEngine.pollinationProfileFor(species, catalogCultivarSelection)
        },
        autoBloomTimingLabel = catalogSpeciesSelection?.let { species ->
            BloomForecastEngine.autoBloomTimingLabelFor(
                speciesInput = species,
                cultivarInput = catalogCultivarSelection,
                locationProfile = locationProfile,
                orchardRegionCode = orchardRegionCode
            )
        }
    )
}

enum class DexPlantSortOption(val label: String) {
    UPDATED("Updated"),
    PLANTED("Planted"),
    SPECIES("Species"),
    CULTIVAR("Cultivar")
}

data class DexBlockInventoryCardModel(
    val id: String,
    val locationName: String,
    val blockName: String,
    val plantCount: Int,
    val dueTaskCount: Int,
    val saleReadyCount: Int,
    val rootingCount: Int,
    val plants: List<TreeListItem>
) {
    val label: String
        get() = blockName

    val isUnassigned: Boolean
        get() = blockName == "Unassigned"
}

data class DexBrowserUiState(
    val search: String = "",
    val speciesFilter: String? = null,
    val statusFilter: TreeStatus? = null,
    val plantTypeFilter: PlantType? = null,
    val nurseryStageFilter: NurseryStage? = null,
    val sort: DexPlantSortOption = DexPlantSortOption.UPDATED,
    val blockView: Boolean = false,
    val selectedBlockId: String? = null,
    val speciesOptions: List<String> = emptyList(),
    val filteredPlants: List<TreeListItem> = emptyList(),
    val groupedBlocks: List<DexBlockInventoryCardModel> = emptyList(),
    val observationsByTreeId: Map<String, List<PhenologyObservation>> = emptyMap()
)

internal fun buildDexBrowserUiState(
    state: DexBrowserUiState,
    plants: List<TreeListItem>,
    history: List<HistoryEntryModel>,
    remindersByTreeId: Map<String, List<com.dillon.orcharddex.data.model.ReminderListItem>>
): DexBrowserUiState {
    val query = state.search.trim().lowercase()
    val observationsByTreeId = history
        .mapNotNull(::historyEntryToPhenologyObservation)
        .groupBy(PhenologyObservation::treeId)
    val filteredPlants = plants
        .filter { item ->
            val tree = item.tree
            val matchesQuery = query.isBlank() || listOf(
                tree.nickname.orEmpty(),
                tree.species,
                tree.cultivar,
                tree.tags,
                tree.notes,
                tree.sectionName
            ).any { it.lowercase().contains(query) }
            matchesQuery &&
                (state.speciesFilter == null || tree.species == state.speciesFilter) &&
                (state.statusFilter == null || tree.status == state.statusFilter) &&
                (state.plantTypeFilter == null || tree.plantType == state.plantTypeFilter) &&
                (state.nurseryStageFilter == null || tree.nurseryStage == state.nurseryStageFilter)
        }
        .sortedWith(
            when (state.sort) {
                DexPlantSortOption.UPDATED -> compareByDescending { it.tree.updatedAt }
                DexPlantSortOption.PLANTED -> compareByDescending { it.tree.plantedDate }
                DexPlantSortOption.SPECIES -> compareBy({ it.tree.species.lowercase() }, { it.tree.cultivar.lowercase() })
                DexPlantSortOption.CULTIVAR -> compareBy({ it.tree.cultivar.lowercase() }, { it.tree.species.lowercase() })
            }
        )
    val groupedBlocks = filteredPlants
        .groupBy { item ->
            val blockName = item.tree.sectionName.trim().ifBlank { "Unassigned" }
            val locationName = item.location?.name?.trim()?.takeUnless { it.isBlank() }
                ?: item.tree.orchardName.trim().ifBlank { "Growing location" }
            locationName to blockName
        }
        .map { (groupKey, items) ->
            val (locationName, blockName) = groupKey
            val saleReadyCount = items.count { it.tree.nurseryStage == NurseryStage.SALE_READY }
            val rootingCount = items.count {
                it.tree.nurseryStage == NurseryStage.ROOTING || it.tree.nurseryStage == NurseryStage.PROPAGATING
            }
            val dueTaskCount = items.sumOf { item ->
                remindersByTreeId[item.tree.id].orEmpty().count { reminderItem ->
                    reminderItem.reminder.completedAt == null && reminderItem.reminder.enabled
                }
            }
            DexBlockInventoryCardModel(
                id = normalizeAutocomplete("$locationName|$blockName"),
                locationName = locationName,
                blockName = blockName,
                plantCount = items.size,
                dueTaskCount = dueTaskCount,
                saleReadyCount = saleReadyCount,
                rootingCount = rootingCount,
                plants = items
            )
        }
        .sortedWith(
            compareBy<DexBlockInventoryCardModel>(
                { it.locationName.lowercase() },
                { it.isUnassigned },
                { it.blockName.lowercase() }
            )
        )
    val selectedBlockId = when {
        groupedBlocks.isEmpty() -> null
        state.selectedBlockId != null && groupedBlocks.any { it.id == state.selectedBlockId } -> state.selectedBlockId
        else -> groupedBlocks.first().id
    }

    return state.copy(
        speciesOptions = plants.map { it.tree.species }.distinct().sorted(),
        filteredPlants = filteredPlants,
        groupedBlocks = groupedBlocks,
        selectedBlockId = selectedBlockId,
        observationsByTreeId = observationsByTreeId
    )
}

private fun historyEntryToPhenologyObservation(entry: HistoryEntryModel): PhenologyObservation? = when {
    entry.kind == com.dillon.orcharddex.data.model.ActivityKind.HARVEST -> PhenologyObservation(
        treeId = entry.treeId,
        dateMillis = entry.date,
        isHarvest = true
    )
    entry.eventType != null -> PhenologyObservation(
        treeId = entry.treeId,
        dateMillis = entry.date,
        eventType = entry.eventType
    )
    else -> null
}
