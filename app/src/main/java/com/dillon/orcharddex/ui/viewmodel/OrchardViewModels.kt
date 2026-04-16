package com.dillon.orcharddex.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dillon.orcharddex.OrchardDexApp
import com.dillon.orcharddex.backup.BackupManager
import com.dillon.orcharddex.backup.BackupValidation
import com.dillon.orcharddex.diagnostics.LocalDiagnosticsStore
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.BloomTimingMode
import com.dillon.orcharddex.data.model.BloomPatternType
import com.dillon.orcharddex.data.model.EventInput
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.GrowingLocationInput
import com.dillon.orcharddex.data.phenology.BloomForecastEngine
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.HarvestInput
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.LocationSearchResult
import com.dillon.orcharddex.data.model.NurseryStage
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.PollinationMode
import com.dillon.orcharddex.data.model.PropagationMethod
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.ReminderInput
import com.dillon.orcharddex.data.model.SaleChannel
import com.dillon.orcharddex.data.model.SaleInput
import com.dillon.orcharddex.data.model.SaleKind
import com.dillon.orcharddex.data.model.SelfCompatibility
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.TreeOriginType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistInput
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.data.repository.OrchardRepository
import com.dillon.orcharddex.time.OrchardTime
import com.dillon.orcharddex.ui.resolveExistingCultivarAutocomplete
import com.dillon.orcharddex.ui.epochToLocalDate
import com.dillon.orcharddex.ui.epochToLocalTime
import com.dillon.orcharddex.ui.localDateAtStartOfDay
import com.dillon.orcharddex.ui.localDateWithTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import kotlin.system.measureTimeMillis

private const val TREE_ID = "treeId"
private const val LOG_KIND = "logKind"
private const val REMINDER_ID = "reminderId"
private const val HISTORY_KIND = "kind"
private const val HISTORY_ENTRY_ID = "entryId"
private const val PARENT_TREE_ID = "parentTreeId"
private const val PROPAGATION_METHOD_ARG = "propagationMethod"

class DashboardViewModel(
    private val repository: OrchardRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val dashboard = repository.observeDashboard().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )
    val trees = repository.observeTrees().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val reminders = repository.observeReminders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val history = repository.observeHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val sales = repository.observeAllSales().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )
}

class TreesViewModel(
    repository: OrchardRepository
) : ViewModel() {
    val trees = repository.observeTrees().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val species = repository.observeSpeciesNames()
        .map { observedSpecies ->
            (observedSpecies + BloomForecastEngine.supportedSpeciesCatalog())
                .filter(String::isNotBlank)
                .distinctBy { it.trim().lowercase() }
                .sortedBy(String::lowercase)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BloomForecastEngine.supportedSpeciesCatalog()
        )
    val cultivars = repository.observeCultivarNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val orchards = repository.observeGrowingLocations()
        .map { locations ->
            locations
                .map(GrowingLocationEntity::name)
                .filter(String::isNotBlank)
                .distinctBy(String::lowercase)
                .sortedBy(String::lowercase)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
}

class HistoryViewModel(
    repository: OrchardRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val history = repository.observeHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val sales = repository.observeAllSales().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )
}

class HistoryDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val kind = savedStateHandle.get<String>(HISTORY_KIND)
        ?.uppercase()
        ?.let { value -> runCatching { ActivityKind.valueOf(value) }.getOrNull() }
    private val entryId: String? = savedStateHandle[HISTORY_ENTRY_ID]

    var isLoading by mutableStateOf(true)
        private set

    var detail by mutableStateOf<HistoryEntryModel?>(null)
        private set

    var saleBusy by mutableStateOf(false)
        private set

    var saleErrorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            detail = if (kind != null && entryId != null) {
                repository.getHistoryEntry(kind, entryId)
            } else {
                null
            }
            isLoading = false
        }
    }

    fun recordHarvestSale(
        soldDate: LocalDate,
        quantityValue: Double,
        quantityUnit: String,
        unitPrice: Double,
        saleChannel: SaleChannel,
        notes: String,
        onSaved: () -> Unit = {}
    ) {
        val current = detail
        if (current == null || current.kind != ActivityKind.HARVEST) {
            saleErrorMessage = "Harvest sale details are unavailable."
            return
        }
        viewModelScope.launch {
            saleBusy = true
            saleErrorMessage = null
            runCatching {
                repository.recordSale(
                    SaleInput(
                        treeId = current.treeId,
                        saleKind = SaleKind.HARVEST,
                        linkedHarvestId = current.id,
                        soldAt = localDateAtStartOfDay(soldDate),
                        quantityValue = quantityValue,
                        quantityUnit = quantityUnit,
                        unitPrice = unitPrice,
                        saleChannel = saleChannel,
                        notes = notes
                    )
                )
            }.onFailure { throwable ->
                saleErrorMessage = throwable.message ?: "Unable to save the harvest sale."
            }.onSuccess {
                onSaved()
            }
            detail = if (kind != null && entryId != null) {
                repository.getHistoryEntry(kind, entryId)
            } else {
                null
            }
            saleBusy = false
        }
    }
}

data class TreeFormState(
    val id: String? = null,
    val locationId: String? = null,
    val orchardName: String = "",
    val sectionName: String = "",
    val nickname: String = "",
    val species: String = "",
    val cultivar: String = "",
    val rootstock: String = "",
    val source: String = "",
    val purchaseDate: LocalDate? = null,
    val plantedDate: LocalDate = OrchardTime.today(),
    val plantType: PlantType = PlantType.IN_GROUND,
    val containerSize: String = "",
    val sunExposure: String = "",
    val frostSensitivity: FrostSensitivityLevel = FrostSensitivityLevel.MEDIUM,
    val frostSensitivityNote: String = "",
    val irrigationNote: String = "",
    val status: TreeStatus = TreeStatus.ACTIVE,
    val hasFruitedBefore: Boolean = false,
    val notes: String = "",
    val tags: String = "",
    val bloomTimingMode: BloomTimingMode = BloomTimingMode.AUTO,
    val bloomPatternOverride: BloomPatternType? = null,
    val manualBloomProfile: List<Int> = List(12) { 0 },
    val alternateYearAnchor: String = "",
    val customBloomStartMonth: String = "",
    val customBloomStartDay: String = "",
    val customBloomDurationDays: String = "",
    val selfCompatibilityOverride: SelfCompatibility? = null,
    val pollinationModeOverride: PollinationMode? = null,
    val pollinationOverrideNote: String = "",
    val nurseryStage: NurseryStage = NurseryStage.NONE,
    val parentTreeId: String? = null,
    val originType: TreeOriginType = TreeOriginType.UNKNOWN,
    val propagationMethod: PropagationMethod? = null,
    val propagationDate: LocalDate? = null,
    val quantity: String = "1",
    val existingPhotos: List<TreePhotoEntity> = emptyList(),
    val newPhotoUris: List<Uri> = emptyList(),
    val removedPhotoIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

internal fun applyPropagationParentState(
    currentState: TreeFormState,
    parentTree: TreeEntity?,
    parentOrchardName: String?,
    defaultLocationId: String?,
    fallbackOrchardName: String,
    propagationMethod: PropagationMethod?,
    today: LocalDate = OrchardTime.today()
): TreeFormState {
    val normalizedManualBloomProfile = parentTree?.manualBloomProfile
        ?.takeIf { profile -> profile.size == 12 }
        ?: currentState.manualBloomProfile
    return currentState.copy(
        locationId = parentTree?.locationId ?: defaultLocationId,
        orchardName = parentOrchardName
            ?: parentTree?.orchardName?.takeIf(String::isNotBlank)
            ?: fallbackOrchardName,
        sectionName = parentTree?.sectionName.orEmpty(),
        species = parentTree?.species.orEmpty(),
        cultivar = parentTree?.cultivar.orEmpty(),
        rootstock = parentTree?.rootstock.orEmpty(),
        source = parentTree?.source.orEmpty(),
        plantType = parentTree?.plantType ?: currentState.plantType,
        containerSize = parentTree?.containerSize.orEmpty(),
        sunExposure = parentTree?.sunExposure.orEmpty(),
        frostSensitivity = parentTree?.frostSensitivity ?: currentState.frostSensitivity,
        frostSensitivityNote = parentTree?.frostSensitivityNote.orEmpty(),
        irrigationNote = parentTree?.irrigationNote.orEmpty(),
        bloomTimingMode = parentTree?.bloomTimingMode ?: currentState.bloomTimingMode,
        bloomPatternOverride = parentTree?.bloomPatternOverride,
        manualBloomProfile = normalizedManualBloomProfile,
        alternateYearAnchor = parentTree?.alternateYearAnchor?.toString().orEmpty(),
        customBloomStartMonth = parentTree?.customBloomStartMonth?.toString().orEmpty(),
        customBloomStartDay = parentTree?.customBloomStartDay?.toString().orEmpty(),
        customBloomDurationDays = parentTree?.customBloomDurationDays?.toString().orEmpty(),
        selfCompatibilityOverride = parentTree?.selfCompatibilityOverride,
        pollinationModeOverride = parentTree?.pollinationModeOverride,
        pollinationOverrideNote = parentTree?.pollinationOverrideNote.orEmpty(),
        nurseryStage = if (parentTree != null) NurseryStage.PROPAGATING else NurseryStage.NONE,
        parentTreeId = parentTree?.id,
        originType = if (parentTree != null) TreeOriginType.PROPAGATED else TreeOriginType.UNKNOWN,
        propagationMethod = if (parentTree != null) propagationMethod else null,
        propagationDate = if (parentTree != null) today else null
    )
}

class TreeFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository,
    private val settingsRepository: SettingsRepository,
    private val diagnosticsStore: LocalDiagnosticsStore
) : ViewModel() {
    private val treeId: String? = savedStateHandle[TREE_ID]
    private val parentTreeId: String? = savedStateHandle[PARENT_TREE_ID]
    private val propagationMethodArg: PropagationMethod? = savedStateHandle.get<String>(PROPAGATION_METHOD_ARG)
        ?.uppercase()
        ?.let { value -> runCatching { PropagationMethod.valueOf(value) }.getOrNull() }
    val knownTrees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val locations = repository.observeGrowingLocations().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    private val supportedSpeciesCatalog = BloomForecastEngine.supportedSpeciesCatalog()
    private var knownTreeSnapshot: List<TreeEntity> = emptyList()
    private var locationSnapshot: List<GrowingLocationEntity> = emptyList()
    private var settingsSnapshot: AppSettings = AppSettings()
    private var activeSearchField: TreeFormSearchField? = null
    private var autocompleteJob: Job? = null
    var state by mutableStateOf(TreeFormState(isLoading = treeId != null))
        private set
    var autocompleteState by mutableStateOf(TreeFormAutocompleteUiState())
        private set

    init {
        viewModelScope.launch {
            knownTrees.collect { trees ->
                knownTreeSnapshot = trees
                scheduleAutocompleteRefresh()
            }
        }
        viewModelScope.launch {
            locations.collect { locations ->
                locationSnapshot = locations
                scheduleAutocompleteRefresh()
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                settingsSnapshot = settings
                scheduleAutocompleteRefresh()
            }
        }
        if (treeId != null) {
            viewModelScope.launch {
                val detail = repository.getTreeDetailSnapshot(treeId)
                setTreeFormState(
                    detail?.let {
                        TreeFormState(
                            id = it.tree.id,
                            locationId = it.tree.locationId,
                            orchardName = it.tree.orchardName,
                            sectionName = it.tree.sectionName,
                            nickname = it.tree.nickname.orEmpty(),
                            species = it.tree.species,
                            cultivar = it.tree.cultivar,
                            rootstock = it.tree.rootstock.orEmpty(),
                            source = it.tree.source.orEmpty(),
                            purchaseDate = it.tree.purchaseDate?.let(::epochToLocalDate),
                            plantedDate = epochToLocalDate(it.tree.plantedDate),
                            plantType = it.tree.plantType,
                            containerSize = it.tree.containerSize.orEmpty(),
                            sunExposure = it.tree.sunExposure.orEmpty(),
                            frostSensitivity = it.tree.frostSensitivity,
                            frostSensitivityNote = it.tree.frostSensitivityNote.orEmpty(),
                            irrigationNote = it.tree.irrigationNote.orEmpty(),
                            status = it.tree.status,
                            hasFruitedBefore = it.tree.hasFruitedBefore,
                            notes = it.tree.notes,
                            tags = it.tree.tags,
                            bloomTimingMode = it.tree.bloomTimingMode,
                            bloomPatternOverride = it.tree.bloomPatternOverride,
                            manualBloomProfile = it.tree.manualBloomProfile.takeIf { profile -> profile.size == 12 }
                                ?: List(12) { 0 },
                            alternateYearAnchor = it.tree.alternateYearAnchor?.toString().orEmpty(),
                            customBloomStartMonth = it.tree.customBloomStartMonth?.toString().orEmpty(),
                            customBloomStartDay = it.tree.customBloomStartDay?.toString().orEmpty(),
                            customBloomDurationDays = it.tree.customBloomDurationDays?.toString().orEmpty(),
                            selfCompatibilityOverride = it.tree.selfCompatibilityOverride,
                            pollinationModeOverride = it.tree.pollinationModeOverride,
                            pollinationOverrideNote = it.tree.pollinationOverrideNote.orEmpty(),
                            nurseryStage = it.tree.nurseryStage,
                            parentTreeId = it.tree.parentTreeId,
                            originType = it.tree.originType,
                            propagationMethod = it.tree.propagationMethod,
                            propagationDate = it.tree.propagationDate?.let(::epochToLocalDate),
                            existingPhotos = it.photos,
                            isLoading = false
                        )
                    } ?: TreeFormState(isLoading = false)
                )
            }
        } else {
            viewModelScope.launch {
                val settings = settingsRepository.snapshot()
                val defaultLocationId = settings.defaultLocationId.takeIf(String::isNotBlank)
                val defaultLocation = defaultLocationId?.let { locationId ->
                    repository.getGrowingLocation(locationId)
                }
                val parent = parentTreeId?.let { propagationParentId ->
                    repository.getTreeDetailSnapshot(propagationParentId)
                }
                setTreeFormState(
                    applyPropagationParentState(
                        currentState = state,
                        parentTree = parent?.tree,
                        parentOrchardName = parent?.location?.name,
                        defaultLocationId = defaultLocation?.id ?: defaultLocationId,
                        fallbackOrchardName = defaultLocation?.name ?: settings.orchardName,
                        propagationMethod = propagationMethodArg
                    )
                )
            }
        }
        scheduleAutocompleteRefresh()
    }

    fun update(update: TreeFormState.() -> TreeFormState) {
        setTreeFormState(state.update().copy(errorMessage = null))
    }

    fun setActiveSearchField(field: TreeFormSearchField?) {
        if (activeSearchField == field) return
        activeSearchField = field
        scheduleAutocompleteRefresh()
    }

    fun updateSpeciesInput(input: String) {
        activeSearchField = TreeFormSearchField.SPECIES
        setTreeFormState(state.copy(species = input, errorMessage = null))
    }

    fun updateCultivarInput(input: String) {
        activeSearchField = TreeFormSearchField.CULTIVAR
        val matchedCultivar = resolveMatchedCultivarOption(
            cultivarQuery = input,
            speciesQuery = state.species
        )
        setTreeFormState(
            state.copy(
                species = if (state.species.isBlank()) matchedCultivar?.species ?: state.species else state.species,
                cultivar = input,
                errorMessage = null
            )
        )
    }

    fun selectSpeciesSuggestion(suggestion: String) {
        activeSearchField = null
        setTreeFormState(state.copy(species = suggestion, errorMessage = null))
        viewModelScope.launch {
            diagnosticsStore.recordBreadcrumb(
                category = "tree_form",
                message = "species_selected",
                attributes = mapOf("species_length" to suggestion.length.toString())
            )
        }
    }

    fun selectCultivarSuggestion(option: com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption) {
        activeSearchField = null
        setTreeFormState(
            state.copy(
                species = option.species,
                cultivar = option.cultivar,
                errorMessage = null
            )
        )
        viewModelScope.launch {
            diagnosticsStore.recordBreadcrumb(
                category = "tree_form",
                message = "cultivar_selected",
                attributes = mapOf(
                    "species_length" to option.species.length.toString(),
                    "cultivar_length" to option.cultivar.length.toString()
                )
            )
        }
    }

    fun addPhotos(uris: List<Uri>) {
        setTreeFormState(state.copy(newPhotoUris = state.newPhotoUris + uris))
    }

    fun removeNewPhoto(uri: Uri) {
        setTreeFormState(state.copy(newPhotoUris = state.newPhotoUris - uri))
    }

    fun removeExistingPhoto(photoId: String) {
        setTreeFormState(state.copy(
            existingPhotos = state.existingPhotos.filterNot { it.id == photoId },
            removedPhotoIds = state.removedPhotoIds + photoId
        ))
    }

    fun save(onSaved: (String) -> Unit) {
        val matchedCultivar = resolveMatchedCultivarOption(
            cultivarQuery = state.cultivar,
            speciesQuery = state.species
        )
        val normalizedSpecies = BloomForecastEngine.resolveSpeciesAutocomplete(state.species)
            ?.takeIf(String::isNotBlank)
            ?: matchedCultivar?.species
            ?: state.species.trim()
        if (normalizedSpecies.isBlank()) {
            setTreeFormState(state.copy(errorMessage = "Species is required."))
            return
        }
        val quantity = if (state.id == null) {
            state.quantity.toIntOrNull()?.takeIf { it > 0 }
        } else {
            1
        }
        if (quantity == null) {
            setTreeFormState(state.copy(errorMessage = "Quantity must be at least 1."))
            return
        }
        val customBloomStartMonth = state.customBloomStartMonth.toIntOrNull()
        val customBloomStartDay = state.customBloomStartDay.toIntOrNull()
        val customBloomDurationDays = state.customBloomDurationDays.toIntOrNull()
        val alternateYearAnchor = state.alternateYearAnchor.toIntOrNull()
        if (state.bloomTimingMode == BloomTimingMode.CUSTOM) {
            val manualProfileValid = state.manualBloomProfile.size == 12 && state.manualBloomProfile.any { it > 0 }
            if (!manualProfileValid) {
                setTreeFormState(state.copy(errorMessage = "Custom bloom timing needs at least one active month."))
                return
            }
        }
        if (state.bloomPatternOverride == BloomPatternType.ALTERNATE_YEAR && alternateYearAnchor == null) {
            setTreeFormState(state.copy(errorMessage = "Alternate-year bloom needs a reference year."))
            return
        }
        viewModelScope.launch {
            setTreeFormState(state.copy(isSaving = true, errorMessage = null))
            try {
                val savedTreeId = repository.saveTree(
                    TreeInput(
                        id = state.id,
                        locationId = state.locationId,
                        orchardName = state.orchardName,
                        sectionName = state.sectionName,
                        nickname = state.nickname,
                        species = normalizedSpecies,
                        cultivar = state.cultivar,
                        rootstock = state.rootstock,
                        source = state.source,
                        purchaseDate = state.purchaseDate?.let(::localDateAtStartOfDay),
                        plantedDate = localDateAtStartOfDay(state.plantedDate),
                        plantType = state.plantType,
                        containerSize = state.containerSize,
                        sunExposure = state.sunExposure,
                        frostSensitivity = state.frostSensitivity,
                        frostSensitivityNote = state.frostSensitivityNote,
                        irrigationNote = state.irrigationNote,
                        status = state.status,
                        hasFruitedBefore = state.hasFruitedBefore,
                        notes = state.notes,
                        tags = state.tags,
                        bloomTimingMode = state.bloomTimingMode,
                        bloomPatternOverride = state.bloomPatternOverride,
                        manualBloomProfile = state.manualBloomProfile,
                        alternateYearAnchor = alternateYearAnchor,
                        customBloomStartMonth = customBloomStartMonth,
                        customBloomStartDay = customBloomStartDay,
                        customBloomDurationDays = customBloomDurationDays,
                        selfCompatibilityOverride = state.selfCompatibilityOverride,
                        pollinationModeOverride = state.pollinationModeOverride,
                        pollinationOverrideNote = state.pollinationOverrideNote,
                        nurseryStage = state.nurseryStage,
                        parentTreeId = state.parentTreeId,
                        originType = state.originType,
                        propagationMethod = state.propagationMethod,
                        propagationDate = state.propagationDate?.let(::localDateAtStartOfDay),
                        quantity = quantity,
                        newPhotoUris = state.newPhotoUris,
                        removedPhotoIds = state.removedPhotoIds.toList()
                    )
                )
                setTreeFormState(state.copy(isSaving = false))
                onSaved(savedTreeId)
            } catch (throwable: Exception) {
                val message = when {
                    throwable is SecurityException -> "A selected photo or file couldn't be accessed. Try picking it again."
                    throwable.message?.contains("image stream", ignoreCase = true) == true ->
                        "A selected photo couldn't be imported. Try picking it again."
                    else -> throwable.message?.takeIf(String::isNotBlank) ?: "Couldn't save this plant right now."
                }
                diagnosticsStore.recordBreadcrumb(
                    category = "tree_form",
                    message = "save_failed",
                    attributes = mapOf(
                        "type" to throwable::class.java.simpleName,
                        "species_length" to normalizedSpecies.length.toString(),
                        "cultivar_length" to state.cultivar.length.toString()
                    )
                )
                setTreeFormState(state.copy(
                    isSaving = false,
                    errorMessage = message
                ))
            }
        }
    }

    private fun setTreeFormState(nextState: TreeFormState) {
        val shouldRefreshAutocomplete = state.id != nextState.id ||
            state.locationId != nextState.locationId ||
            state.species != nextState.species ||
            state.cultivar != nextState.cultivar
        state = nextState
        if (shouldRefreshAutocomplete) {
            scheduleAutocompleteRefresh()
        }
    }

    private fun scheduleAutocompleteRefresh() {
        val stateSnapshot = state
        val treesSnapshot = knownTreeSnapshot
        val locationProfileSnapshot = currentLocationProfile(stateSnapshot)
        val orchardRegionSnapshot = settingsSnapshot.orchardRegion.takeIf(String::isNotBlank)
        val searchFieldSnapshot = activeSearchField
        autocompleteJob?.cancel()
        autocompleteJob = viewModelScope.launch {
            if (
                searchFieldSnapshot == TreeFormSearchField.SPECIES ||
                (searchFieldSnapshot == TreeFormSearchField.CULTIVAR && stateSnapshot.cultivar.isNotBlank())
            ) {
                delay(90)
            }
            var nextAutocompleteState = TreeFormAutocompleteUiState()
            val elapsed = measureTimeMillis {
                nextAutocompleteState = withContext(Dispatchers.Default) {
                    buildTreeFormAutocompleteUiState(
                        state = stateSnapshot,
                        knownTrees = treesSnapshot,
                        supportedSpecies = supportedSpeciesCatalog,
                        activeSearchField = searchFieldSnapshot,
                        locationProfile = locationProfileSnapshot,
                        orchardRegionCode = orchardRegionSnapshot
                    )
                }
            }
            autocompleteState = nextAutocompleteState
            if (elapsed >= 48L) {
                diagnosticsStore.recordSlowPath(
                    category = "tree_form_autocomplete",
                    durationMs = elapsed,
                    attributes = mapOf(
                        "field" to (searchFieldSnapshot?.name ?: "NONE"),
                        "species_length" to stateSnapshot.species.length.toString(),
                        "cultivar_length" to stateSnapshot.cultivar.length.toString(),
                        "known_tree_count" to treesSnapshot.size.toString()
                    )
                )
            }
        }
    }

    private fun currentLocationProfile(stateSnapshot: TreeFormState): ForecastLocationProfile =
        stateSnapshot.locationId
            ?.let { locationId -> locationSnapshot.firstOrNull { location -> location.id == locationId } }
            ?.toForecastLocationProfile()
            ?: settingsSnapshot.forecastLocationProfile()

    private fun resolveMatchedCultivarOption(
        cultivarQuery: String,
        speciesQuery: String
    ): com.dillon.orcharddex.data.phenology.CultivarAutocompleteOption? =
        BloomForecastEngine.resolveCultivarAutocomplete(cultivarQuery, speciesQuery)
            ?: resolveExistingCultivarAutocomplete(cultivarQuery, knownTreeSnapshot)
}

class TreeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val treeId: String = checkNotNull(savedStateHandle[TREE_ID])
    val detail = repository.observeTreeDetail(treeId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    var confirmDelete by mutableStateOf(false)
        private set

    var saleBusy by mutableStateOf(false)
        private set

    var saleErrorMessage by mutableStateOf<String?>(null)
        private set

    fun requestDeleteConfirmation() {
        confirmDelete = true
    }

    fun dismissDeleteConfirmation() {
        confirmDelete = false
    }

    fun deleteTree(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTree(treeId)
            confirmDelete = false
            onDeleted()
        }
    }

    fun addPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            repository.addTreePhotos(treeId, uris)
        }
    }

    fun setHeroPhoto(photoId: String) {
        viewModelScope.launch {
            repository.setTreeHeroPhoto(treeId, photoId)
        }
    }

    fun recordTreeSale(
        soldDate: LocalDate,
        salePrice: Double,
        saleChannel: SaleChannel,
        notes: String,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            saleBusy = true
            saleErrorMessage = null
            runCatching {
                repository.recordSale(
                    SaleInput(
                        treeId = treeId,
                        saleKind = SaleKind.TREE,
                        soldAt = localDateAtStartOfDay(soldDate),
                        quantityValue = 1.0,
                        quantityUnit = "plant",
                        unitPrice = salePrice,
                        saleChannel = saleChannel,
                        notes = notes
                    )
                )
            }.onSuccess {
                onSaved()
            }.onFailure { throwable ->
                saleErrorMessage = throwable.message ?: "Unable to save the plant sale."
            }
            saleBusy = false
        }
    }
}

data class LogFormState(
    val kind: ActivityKind = ActivityKind.EVENT,
    val applyToAllActive: Boolean = false,
    val selectedTreeIds: Set<String> = emptySet(),
    val eventType: EventType = EventType.NOTE,
    val logDate: LocalDate = OrchardTime.today(),
    val notes: String = "",
    val cost: String = "",
    val quantityValue: String = "",
    val quantityUnit: String = "",
    val qualityRating: Int = 3,
    val firstFruit: Boolean = false,
    val verified: Boolean = true,
    val recordSaleNow: Boolean = false,
    val saleQuantityValue: String = "",
    val saleQuantityUnit: String = "",
    val saleUnitPrice: String = "",
    val saleChannel: SaleChannel = SaleChannel.DIRECT,
    val saleNotes: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class LogFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val initialTreeId: String? = savedStateHandle.get<String>(TREE_ID)?.takeIf(String::isNotBlank)
    private val initialKind: ActivityKind = savedStateHandle.get<String>(LOG_KIND)
        ?.uppercase()
        ?.let { value -> runCatching { ActivityKind.valueOf(value) }.getOrNull() }
        ?: ActivityKind.EVENT

    var state by mutableStateOf(
        LogFormState(
            kind = initialKind,
            selectedTreeIds = initialTreeId?.let(::setOf) ?: emptySet(),
            quantityUnit = if (initialKind == ActivityKind.HARVEST) "fruit" else ""
        )
    )
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val harvests = repository.observeAllHarvests().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun update(update: LogFormState.() -> LogFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun setKind(kind: ActivityKind) {
        state = when (kind) {
            ActivityKind.EVENT -> state.copy(
                kind = kind,
                recordSaleNow = false,
                saleQuantityValue = "",
                saleQuantityUnit = "",
                saleUnitPrice = "",
                saleNotes = "",
                errorMessage = null
            )
            ActivityKind.HARVEST -> state.copy(
                kind = kind,
                applyToAllActive = false,
                quantityUnit = state.quantityUnit.ifBlank { "fruit" },
                saleQuantityUnit = state.saleQuantityUnit.ifBlank { state.quantityUnit.ifBlank { "fruit" } },
                errorMessage = null
            )
        }
    }

    fun setApplyToAllActive(enabled: Boolean) {
        state = state.copy(applyToAllActive = enabled, errorMessage = null)
    }

    fun applyLastHarvest(harvest: HarvestEntity) {
        state = state.copy(
            quantityValue = harvest.quantityValue.toString().removeSuffix(".0"),
            quantityUnit = harvest.quantityUnit,
            saleQuantityValue = if (state.recordSaleNow) harvest.quantityValue.toString().removeSuffix(".0") else state.saleQuantityValue,
            saleQuantityUnit = if (state.recordSaleNow) harvest.quantityUnit else state.saleQuantityUnit,
            qualityRating = harvest.qualityRating,
            errorMessage = null
        )
    }

    fun setRecordSaleNow(enabled: Boolean) {
        state = state.copy(
            recordSaleNow = enabled,
            saleQuantityValue = if (enabled) state.saleQuantityValue.ifBlank { state.quantityValue } else "",
            saleQuantityUnit = if (enabled) state.saleQuantityUnit.ifBlank { state.quantityUnit } else "",
            saleUnitPrice = if (enabled) state.saleUnitPrice else "",
            saleNotes = if (enabled) state.saleNotes else "",
            errorMessage = null
        )
    }

    fun toggleTreeSelection(treeId: String) {
        val selected = state.selectedTreeIds
        state = state.copy(
            selectedTreeIds = if (treeId in selected) selected - treeId else selected + treeId,
            errorMessage = null
        )
    }

    fun selectTreeIds(treeIds: Collection<String>) {
        state = state.copy(selectedTreeIds = state.selectedTreeIds + treeIds, errorMessage = null)
    }

    fun toggleTreeGroupSelection(treeIds: Collection<String>) {
        val group = treeIds.toSet()
        if (group.isEmpty()) return
        val selected = state.selectedTreeIds
        val updated = if (group.all { it in selected }) {
            selected - group
        } else {
            selected + group
        }
        state = state.copy(selectedTreeIds = updated, errorMessage = null)
    }

    fun clearTreeSelection() {
        state = state.copy(selectedTreeIds = emptySet(), errorMessage = null)
    }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        state = state.copy(photoUris = (state.photoUris + uris).distinct(), errorMessage = null)
    }

    fun removePhoto(uri: Uri) {
        state = state.copy(photoUris = state.photoUris - uri, errorMessage = null)
    }

    fun save(onSaved: (String) -> Unit) {
        when (state.kind) {
            ActivityKind.EVENT -> saveEvent(onSaved)
            ActivityKind.HARVEST -> saveHarvest(onSaved)
        }
    }

    private fun saveEvent(onSaved: (String) -> Unit) {
        val targetTreeIds = if (state.applyToAllActive) {
            trees.value.filter { it.status == TreeStatus.ACTIVE }.map(TreeEntity::id)
        } else {
            state.selectedTreeIds.toList()
        }.distinct()

        if (targetTreeIds.isEmpty()) {
            state = state.copy(
                errorMessage = if (state.applyToAllActive) {
                    "No active plants are available."
                } else {
                    "Select at least one plant."
                }
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true)
            repository.addEvents(
                targetTreeIds.map { treeId ->
                    EventInput(
                        treeId = treeId,
                        eventType = state.eventType,
                        eventDate = localDateAtStartOfDay(state.logDate),
                        notes = state.notes,
                        cost = state.cost.toDoubleOrNull(),
                        quantityValue = state.quantityValue.toDoubleOrNull(),
                        quantityUnit = state.quantityUnit,
                        photoUris = state.photoUris
                    )
                }
            )
            state = state.copy(isSaving = false)
            onSaved(targetTreeIds.singleOrNull().orEmpty())
        }
    }

    private fun saveHarvest(onSaved: (String) -> Unit) {
        val targetTreeIds = state.selectedTreeIds.toList()
        val targetTrees = trees.value.filter { tree -> tree.id in targetTreeIds }
        val quantity = state.quantityValue.toDoubleOrNull()
        if (targetTreeIds.isEmpty() || quantity == null) {
            state = state.copy(errorMessage = "Pick at least one plant and enter a quantity.")
            return
        }
        val saleQuantity = state.saleQuantityValue.toDoubleOrNull()
        val saleUnitPrice = state.saleUnitPrice.toDoubleOrNull()
        if (state.recordSaleNow) {
            if (!supportsInlineHarvestSale(targetTrees)) {
                state = state.copy(errorMessage = "Sell-now is available when the selected plants are the same species or cultivar.")
                return
            }
            if (saleQuantity == null || saleQuantity <= 0.0 || saleUnitPrice == null || saleUnitPrice < 0.0) {
                state = state.copy(errorMessage = "Enter a valid sale quantity and price.")
                return
            }
            val maxBatchQuantity = quantity * targetTreeIds.size
            if (saleQuantity > maxBatchQuantity + 0.0001) {
                state = state.copy(errorMessage = "Sale quantity exceeds the total harvested amount for this batch.")
                return
            }
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val savedHarvests = repository.addHarvests(
                targetTreeIds.map { treeId ->
                    HarvestInput(
                        treeId = treeId,
                        harvestDate = localDateAtStartOfDay(state.logDate),
                        quantityValue = quantity,
                        quantityUnit = state.quantityUnit,
                        qualityRating = state.qualityRating,
                        firstFruit = state.firstFruit,
                        verified = state.verified,
                        notes = state.notes,
                        photoUris = state.photoUris
                    )
                }
            )
            if (state.recordSaleNow) {
                var remainingSaleQuantity = checkNotNull(saleQuantity)
                savedHarvests.forEach { savedHarvest ->
                    if (remainingSaleQuantity <= 0.0001) return@forEach
                    val allocatedQuantity = minOf(savedHarvest.quantityValue, remainingSaleQuantity)
                    if (allocatedQuantity > 0.0) {
                        repository.recordSale(
                            SaleInput(
                                treeId = savedHarvest.treeId,
                                saleKind = SaleKind.HARVEST,
                                linkedHarvestId = savedHarvest.id,
                                soldAt = localDateAtStartOfDay(state.logDate),
                                quantityValue = allocatedQuantity,
                                quantityUnit = state.saleQuantityUnit.ifBlank { state.quantityUnit },
                                unitPrice = checkNotNull(saleUnitPrice),
                                saleChannel = state.saleChannel,
                                notes = state.saleNotes
                            )
                        )
                        remainingSaleQuantity -= allocatedQuantity
                    }
                }
                if (remainingSaleQuantity > 0.0001) {
                    state = state.copy(
                        isSaving = false,
                        errorMessage = "Not enough harvested quantity was available to allocate this sale."
                    )
                    return@launch
                }
            }
            state = state.copy(isSaving = false)
            onSaved(targetTreeIds.singleOrNull().orEmpty())
        }
    }
}

private fun supportsInlineHarvestSale(trees: List<TreeEntity>): Boolean {
    if (trees.isEmpty()) return false
    if (trees.size == 1) return true
    val sameSpecies = trees.map { it.species.trim().lowercase() }
        .filter(String::isNotBlank)
        .distinct()
        .size == 1
    val sameCultivar = trees.map { it.cultivar.trim().lowercase() }
        .filter(String::isNotBlank)
        .distinct()
        .size == 1
    return sameSpecies || sameCultivar
}

data class EventFormState(
    val applyToAllActive: Boolean = false,
    val selectedTreeIds: Set<String> = emptySet(),
    val eventType: EventType = EventType.NOTE,
    val eventDate: LocalDate = OrchardTime.today(),
    val notes: String = "",
    val cost: String = "",
    val quantityValue: String = "",
    val quantityUnit: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class EventFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val initialTreeId: String? = savedStateHandle.get<String>(TREE_ID)?.takeIf(String::isNotBlank)
    var state by mutableStateOf(
        EventFormState(
            selectedTreeIds = initialTreeId?.let(::setOf) ?: emptySet()
        )
    )
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun update(update: EventFormState.() -> EventFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun setApplyToAllActive(enabled: Boolean) {
        state = state.copy(applyToAllActive = enabled, errorMessage = null)
    }

    fun toggleTreeSelection(treeId: String) {
        val selected = state.selectedTreeIds
        state = state.copy(
            selectedTreeIds = if (treeId in selected) selected - treeId else selected + treeId,
            errorMessage = null
        )
    }

    fun selectTreeIds(treeIds: Collection<String>) {
        state = state.copy(selectedTreeIds = state.selectedTreeIds + treeIds, errorMessage = null)
    }

    fun toggleTreeGroupSelection(treeIds: Collection<String>) {
        val group = treeIds.toSet()
        if (group.isEmpty()) return
        val selected = state.selectedTreeIds
        val updated = if (group.all { it in selected }) {
            selected - group
        } else {
            selected + group
        }
        state = state.copy(selectedTreeIds = updated, errorMessage = null)
    }

    fun clearTreeSelection() {
        state = state.copy(selectedTreeIds = emptySet(), errorMessage = null)
    }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        state = state.copy(photoUris = (state.photoUris + uris).distinct(), errorMessage = null)
    }

    fun removePhoto(uri: Uri) {
        state = state.copy(photoUris = state.photoUris - uri, errorMessage = null)
    }

    fun save(onSaved: (String) -> Unit) {
        val targetTreeIds = if (state.applyToAllActive) {
            trees.value.filter { it.status == TreeStatus.ACTIVE }.map(TreeEntity::id)
        } else {
            state.selectedTreeIds.toList()
        }.distinct()

        if (targetTreeIds.isEmpty()) {
            state = state.copy(
                errorMessage = if (state.applyToAllActive) {
                    "No active plants are available."
                } else {
                    "Select at least one plant."
                }
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isSaving = true)
            repository.addEvents(
                targetTreeIds.map { treeId ->
                    EventInput(
                        treeId = treeId,
                        eventType = state.eventType,
                        eventDate = localDateAtStartOfDay(state.eventDate),
                        notes = state.notes,
                        cost = state.cost.toDoubleOrNull(),
                        quantityValue = state.quantityValue.toDoubleOrNull(),
                        quantityUnit = state.quantityUnit,
                        photoUris = state.photoUris
                    )
                }
            )
            state = state.copy(isSaving = false)
            onSaved(targetTreeIds.singleOrNull().orEmpty())
        }
    }
}

data class HarvestFormState(
    val selectedTreeIds: Set<String> = emptySet(),
    val harvestDate: LocalDate = OrchardTime.today(),
    val quantityValue: String = "",
    val quantityUnit: String = "fruit",
    val qualityRating: Int = 3,
    val firstFruit: Boolean = false,
    val verified: Boolean = true,
    val notes: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class HarvestFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val initialTreeId: String? = savedStateHandle[TREE_ID]
    var state by mutableStateOf(
        HarvestFormState(
            selectedTreeIds = initialTreeId?.takeIf(String::isNotBlank)?.let(::setOf) ?: emptySet()
        )
    )
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val harvests = repository.observeAllHarvests().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun update(update: HarvestFormState.() -> HarvestFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun applyLastHarvest(harvest: HarvestEntity) {
        state = state.copy(
            quantityValue = harvest.quantityValue.toString().removeSuffix(".0"),
            quantityUnit = harvest.quantityUnit,
            qualityRating = harvest.qualityRating,
            errorMessage = null
        )
    }

    fun toggleTreeSelection(treeId: String) {
        val selected = state.selectedTreeIds
        state = state.copy(
            selectedTreeIds = if (treeId in selected) selected - treeId else selected + treeId,
            errorMessage = null
        )
    }

    fun selectTreeIds(treeIds: Collection<String>) {
        state = state.copy(selectedTreeIds = state.selectedTreeIds + treeIds, errorMessage = null)
    }

    fun toggleTreeGroupSelection(treeIds: Collection<String>) {
        val group = treeIds.toSet()
        if (group.isEmpty()) return
        val selected = state.selectedTreeIds
        val updated = if (group.all { it in selected }) {
            selected - group
        } else {
            selected + group
        }
        state = state.copy(selectedTreeIds = updated, errorMessage = null)
    }

    fun clearTreeSelection() {
        state = state.copy(selectedTreeIds = emptySet(), errorMessage = null)
    }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        state = state.copy(photoUris = (state.photoUris + uris).distinct(), errorMessage = null)
    }

    fun removePhoto(uri: Uri) {
        state = state.copy(photoUris = state.photoUris - uri, errorMessage = null)
    }

    fun save(onSaved: (String) -> Unit) {
        val targetTreeIds = state.selectedTreeIds.toList()
        val quantity = state.quantityValue.toDoubleOrNull()
        if (targetTreeIds.isEmpty() || quantity == null) {
            state = state.copy(errorMessage = "Pick at least one plant and enter a quantity.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            repository.addHarvests(
                targetTreeIds.map { treeId ->
                    HarvestInput(
                        treeId = treeId,
                        harvestDate = localDateAtStartOfDay(state.harvestDate),
                        quantityValue = quantity,
                        quantityUnit = state.quantityUnit,
                        qualityRating = state.qualityRating,
                        firstFruit = state.firstFruit,
                        verified = state.verified,
                        notes = state.notes,
                        photoUris = state.photoUris
                    )
                }
            )
            state = state.copy(isSaving = false)
            onSaved(targetTreeIds.singleOrNull().orEmpty())
        }
    }
}

class DexViewModel(
    private val repository: OrchardRepository,
    private val diagnosticsStore: LocalDiagnosticsStore
) : ViewModel() {
    val trees = repository.observeTrees().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val history = repository.observeHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val reminders = repository.observeReminders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val dex = repository.observeDex().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.dillon.orcharddex.data.model.DexModel()
    )
    private var treeSnapshot: List<com.dillon.orcharddex.data.model.TreeListItem> = emptyList()
    private var historySnapshot: List<HistoryEntryModel> = emptyList()
    private var reminderSnapshot: List<com.dillon.orcharddex.data.model.ReminderListItem> = emptyList()
    private var browserJob: Job? = null

    var addDialogVisible by mutableStateOf(false)
        private set
    var browserState by mutableStateOf(DexBrowserUiState())
        private set

    var addSpecies by mutableStateOf("")
    var addCultivar by mutableStateOf("")
    var addNotes by mutableStateOf("")

    init {
        viewModelScope.launch {
            trees.collect { items ->
                treeSnapshot = items
                scheduleBrowserRefresh()
            }
        }
        viewModelScope.launch {
            history.collect { entries ->
                historySnapshot = entries
                scheduleBrowserRefresh()
            }
        }
        viewModelScope.launch {
            reminders.collect { items ->
                reminderSnapshot = items
                scheduleBrowserRefresh()
            }
        }
    }

    fun showAddDialog() {
        addDialogVisible = true
    }

    fun hideAddDialog() {
        addDialogVisible = false
        addSpecies = ""
        addCultivar = ""
        addNotes = ""
    }

    fun saveWishlist() {
        val normalizedSpecies = BloomForecastEngine.resolveSpeciesAutocomplete(addSpecies)
            ?.takeIf(String::isNotBlank)
            ?: addSpecies.trim()
        if (normalizedSpecies.isBlank()) return
        viewModelScope.launch {
            repository.addWishlist(
                WishlistInput(
                    species = normalizedSpecies,
                    cultivar = addCultivar,
                    notes = addNotes
                )
            )
            hideAddDialog()
        }
    }

    fun deleteWishlist(id: String) {
        viewModelScope.launch {
            repository.deleteWishlist(id)
        }
    }

    fun updateSearch(query: String) {
        browserState = browserState.copy(search = query)
        scheduleBrowserRefresh(debounceMs = 90)
    }

    fun updateSpeciesFilter(species: String?) {
        browserState = browserState.copy(speciesFilter = species)
        scheduleBrowserRefresh()
    }

    fun updateStatusFilter(status: TreeStatus?) {
        browserState = browserState.copy(statusFilter = status)
        scheduleBrowserRefresh()
    }

    fun updatePlantTypeFilter(plantType: PlantType?) {
        browserState = browserState.copy(plantTypeFilter = plantType)
        scheduleBrowserRefresh()
    }

    fun updateNurseryStageFilter(stage: NurseryStage?) {
        browserState = browserState.copy(nurseryStageFilter = stage)
        scheduleBrowserRefresh()
    }

    fun updateSort(sort: DexPlantSortOption) {
        browserState = browserState.copy(sort = sort)
        scheduleBrowserRefresh()
    }

    fun setBlockView(enabled: Boolean) {
        browserState = browserState.copy(blockView = enabled)
        if (enabled) {
            scheduleBrowserRefresh()
        }
    }

    fun setSelectedBlock(blockId: String) {
        browserState = browserState.copy(selectedBlockId = blockId)
    }

    fun resetBrowserFilters() {
        browserState = browserState.copy(
            speciesFilter = null,
            statusFilter = null,
            plantTypeFilter = null,
            nurseryStageFilter = null,
            sort = DexPlantSortOption.UPDATED
        )
        scheduleBrowserRefresh()
    }

    private fun scheduleBrowserRefresh(debounceMs: Long = 0L) {
        val stateSnapshot = browserState
        val treesSnapshot = treeSnapshot
        val historyEntries = historySnapshot
        val remindersByTreeId = reminderSnapshot.groupBy { it.reminder.treeId.orEmpty() }
        browserJob?.cancel()
        browserJob = viewModelScope.launch {
            if (debounceMs > 0L) delay(debounceMs)
            var nextBrowserState = stateSnapshot
            val elapsed = measureTimeMillis {
                nextBrowserState = withContext(Dispatchers.Default) {
                    buildDexBrowserUiState(
                        state = stateSnapshot,
                        plants = treesSnapshot,
                        history = historyEntries,
                        remindersByTreeId = remindersByTreeId
                    )
                }
            }
            browserState = nextBrowserState
            if (elapsed >= 48L) {
                diagnosticsStore.recordSlowPath(
                    category = "dex_browser",
                    durationMs = elapsed,
                    attributes = mapOf(
                        "search_length" to stateSnapshot.search.length.toString(),
                        "plant_count" to treesSnapshot.size.toString(),
                        "history_count" to historyEntries.size.toString()
                    )
                )
            }
        }
    }
}

class ReminderListViewModel(
    private val repository: OrchardRepository
) : ViewModel() {
    val reminders = repository.observeReminders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun markDone(id: String, createLinkedEvent: Boolean) {
        viewModelScope.launch {
            repository.markReminderDone(id, createLinkedEvent)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }
}

enum class ReminderTargetMode {
    GENERAL,
    SELECTED,
    ALL_ACTIVE
}

data class ReminderFormState(
    val id: String? = null,
    val treeId: String? = null,
    val targetMode: ReminderTargetMode = ReminderTargetMode.GENERAL,
    val selectedTreeIds: Set<String> = emptySet(),
    val title: String = "",
    val notes: String = "",
    val dueDate: LocalDate = OrchardTime.today(),
    val dueTime: LocalTime = LocalTime.of(8, 0),
    val hasTime: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceIntervalDays: String = "7",
    val enabled: Boolean = true,
    val leadTimeMode: LeadTimeMode = LeadTimeMode.SAME_DAY,
    val customLeadTimeHours: String = "6",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class ReminderFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    private val reminderId: String? = savedStateHandle[REMINDER_ID]
    private val treeIdArg: String? = savedStateHandle[TREE_ID]
    var state by mutableStateOf(
        ReminderFormState(
            treeId = treeIdArg,
            targetMode = if (treeIdArg.isNullOrBlank()) ReminderTargetMode.GENERAL else ReminderTargetMode.SELECTED,
            selectedTreeIds = treeIdArg?.takeIf(String::isNotBlank)?.let(::setOf) ?: emptySet(),
            isLoading = reminderId != null
        )
    )
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )
    val reminders = repository.observeReminders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            val defaults = settings.value
            state = state.copy(
                leadTimeMode = defaults.defaultLeadTimeMode,
                customLeadTimeHours = defaults.defaultCustomLeadHours.toString()
            )
            if (reminderId != null) {
                val reminder = repository.getReminder(reminderId)
                state = reminder?.let {
                    ReminderFormState(
                        id = it.id,
                        treeId = it.treeId,
                        targetMode = if (it.treeId == null) ReminderTargetMode.GENERAL else ReminderTargetMode.SELECTED,
                        selectedTreeIds = it.treeId?.let(::setOf) ?: emptySet(),
                        title = it.title,
                        notes = it.notes,
                        dueDate = epochToLocalDate(it.dueAt),
                        dueTime = epochToLocalTime(it.dueAt),
                        hasTime = it.hasTime,
                        recurrenceType = it.recurrenceType,
                        recurrenceIntervalDays = (it.recurrenceIntervalDays ?: 7).toString(),
                        enabled = it.enabled,
                        leadTimeMode = it.leadTimeMode,
                        customLeadTimeHours = (it.customLeadTimeHours ?: defaults.defaultCustomLeadHours).toString(),
                        isLoading = false
                    )
                } ?: state.copy(isLoading = false)
            } else {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun update(update: ReminderFormState.() -> ReminderFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun setTargetMode(mode: ReminderTargetMode) {
        state = when (mode) {
            ReminderTargetMode.GENERAL -> state.copy(
                targetMode = mode,
                treeId = null,
                selectedTreeIds = emptySet(),
                errorMessage = null
            )
            ReminderTargetMode.SELECTED -> state.copy(
                targetMode = mode,
                treeId = state.selectedTreeIds.singleOrNull(),
                errorMessage = null
            )
            ReminderTargetMode.ALL_ACTIVE -> state.copy(
                targetMode = mode,
                treeId = null,
                errorMessage = null
            )
        }
    }

    fun toggleTreeSelection(treeId: String) {
        val selected = state.selectedTreeIds
        val updated = if (treeId in selected) selected - treeId else selected + treeId
        state = state.copy(
            selectedTreeIds = updated,
            treeId = updated.singleOrNull(),
            targetMode = ReminderTargetMode.SELECTED,
            errorMessage = null
        )
    }

    fun selectTreeIds(treeIds: Collection<String>) {
        val updated = state.selectedTreeIds + treeIds
        state = state.copy(
            selectedTreeIds = updated,
            treeId = updated.singleOrNull(),
            targetMode = ReminderTargetMode.SELECTED,
            errorMessage = null
        )
    }

    fun toggleTreeGroupSelection(treeIds: Collection<String>) {
        val group = treeIds.toSet()
        if (group.isEmpty()) return
        val selected = state.selectedTreeIds
        val updated = if (group.all { it in selected }) {
            selected - group
        } else {
            selected + group
        }
        state = state.copy(
            selectedTreeIds = updated,
            treeId = updated.singleOrNull(),
            targetMode = ReminderTargetMode.SELECTED,
            errorMessage = null
        )
    }

    fun clearTreeSelection() {
        state = state.copy(
            selectedTreeIds = emptySet(),
            treeId = null,
            errorMessage = null
        )
    }

    fun save(onSaved: (requestNotificationPermission: Boolean) -> Unit) {
        if (state.title.isBlank()) {
            state = state.copy(errorMessage = "Title is required.")
            return
        }
        val targetTreeIds = if (state.id != null) {
            listOf(state.treeId)
        } else {
            when (state.targetMode) {
                ReminderTargetMode.GENERAL -> listOf(null)
                ReminderTargetMode.SELECTED -> state.selectedTreeIds.toList()
                ReminderTargetMode.ALL_ACTIVE -> trees.value
                    .filter { it.status == TreeStatus.ACTIVE }
                    .map(TreeEntity::id)
            }
        }
        if (state.id == null && state.targetMode == ReminderTargetMode.SELECTED && targetTreeIds.isEmpty()) {
            state = state.copy(errorMessage = "Select at least one plant.")
            return
        }
        if (state.id == null && state.targetMode == ReminderTargetMode.ALL_ACTIVE && targetTreeIds.isEmpty()) {
            state = state.copy(errorMessage = "No active plants are available.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val shouldRequestNotificationPermission = state.enabled &&
                reminders.value.none { reminderItem ->
                    reminderItem.reminder.enabled &&
                        reminderItem.reminder.id != state.id
                }
            val dueAt = if (state.hasTime) {
                localDateWithTime(state.dueDate, state.dueTime)
            } else {
                localDateWithTime(state.dueDate, LocalTime.of(8, 0))
            }
            repository.saveReminders(
                targetTreeIds.map { targetTreeId ->
                    ReminderInput(
                        id = state.id,
                        treeId = if (state.id != null) state.treeId else targetTreeId,
                        title = state.title,
                        notes = state.notes,
                        dueAt = dueAt,
                        hasTime = state.hasTime,
                        recurrenceType = state.recurrenceType,
                        recurrenceIntervalDays = state.recurrenceIntervalDays.toIntOrNull(),
                        enabled = state.enabled,
                        leadTimeMode = state.leadTimeMode,
                        customLeadTimeHours = state.customLeadTimeHours.toIntOrNull()
                    )
                }
            )
            state = state.copy(isSaving = false)
            onSaved(shouldRequestNotificationPermission)
        }
    }
}

class SettingsViewModel(
    private val repository: OrchardRepository,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val diagnosticsStore: LocalDiagnosticsStore
) : ViewModel() {
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )
    val locations = repository.observeGrowingLocations().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    var busy by mutableStateOf(false)
        private set

    var pendingImport by mutableStateOf<BackupValidation?>(null)
        private set

    var pendingImportUri by mutableStateOf<Uri?>(null)
        private set

    var confirmClearAll by mutableStateOf(false)
        private set

    var confirmLoadSample by mutableStateOf(false)
        private set

    var locationSearchQuery by mutableStateOf("")
        private set

    var locationSearchResults by mutableStateOf<List<LocationSearchResult>>(emptyList())
        private set

    var locationSearchBusy by mutableStateOf(false)
        private set

    var locationSearchError by mutableStateOf<String?>(null)
        private set

    private var locationSearchJob: Job? = null

    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateTheme(mode)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDynamicColor(enabled)
        }
    }

    fun updateShowSalesTools(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowSalesTools(enabled)
        }
    }

    fun updateOrchardProfile(name: String, locationProfile: ForecastLocationProfile) {
        viewModelScope.launch {
            busy = true
            try {
                val snapshot = settingsRepository.snapshot()
                val existingDefaultLocation = snapshot.defaultLocationId
                    .takeIf(String::isNotBlank)
                    ?.let { locationId -> repository.getGrowingLocation(locationId) }
                val savedLocation = repository.saveGrowingLocation(
                    GrowingLocationInput(
                        id = snapshot.defaultLocationId.takeIf(String::isNotBlank),
                        name = name.trim(),
                        countryCode = locationProfile.countryCode,
                        timezoneId = locationProfile.timezoneId,
                        hemisphere = locationProfile.hemisphere,
                        latitudeDeg = locationProfile.latitudeDeg,
                        longitudeDeg = locationProfile.longitudeDeg,
                        elevationM = locationProfile.elevationM,
                        usdaZoneCode = locationProfile.usdaZoneCode,
                        chillHoursBand = locationProfile.chillHoursBand,
                        microclimateFlags = locationProfile.microclimateFlags,
                        notes = existingDefaultLocation?.notes ?: locationProfile.notes
                    )
                )
                settingsRepository.updateDefaultLocationId(savedLocation.id)
                settingsRepository.updateOrchardName(savedLocation.name)
                settingsRepository.updateForecastLocation(savedLocation.toForecastLocationProfile())
                settingsRepository.updateOrchardRegion("")
            } finally {
                busy = false
            }
        }
    }

    fun updateDefaultReminder(mode: LeadTimeMode, customHours: Int) {
        viewModelScope.launch {
            settingsRepository.updateDefaultLeadTime(mode, customHours)
        }
    }

    fun completeOnboarding(
        orchardName: String,
        locationProfile: ForecastLocationProfile
    ) {
        viewModelScope.launch {
            val trimmed = orchardName.trim()
            settingsRepository.completeOnboarding(trimmed, locationProfile, "")
            repository.ensureGrowingLocations(settingsRepository.settings.first())
        }
    }

    fun completeWalkthrough() {
        viewModelScope.launch {
            settingsRepository.markWalkthroughComplete()
        }
    }

    fun saveGrowingLocation(input: GrowingLocationInput) {
        viewModelScope.launch {
            busy = true
            try {
                val savedLocation = repository.saveGrowingLocation(input)
                val snapshot = settingsRepository.snapshot()
                if (snapshot.defaultLocationId.isBlank() || snapshot.defaultLocationId == savedLocation.id) {
                    settingsRepository.updateDefaultLocationId(savedLocation.id)
                    settingsRepository.updateOrchardName(savedLocation.name)
                    settingsRepository.updateForecastLocation(savedLocation.toForecastLocationProfile())
                    settingsRepository.updateOrchardRegion("")
                }
            } finally {
                busy = false
            }
        }
    }

    fun setDefaultGrowingLocation(locationId: String) {
        viewModelScope.launch {
            busy = true
            try {
                val location = repository.getGrowingLocation(locationId) ?: return@launch
                settingsRepository.updateDefaultLocationId(location.id)
                settingsRepository.updateOrchardName(location.name)
                settingsRepository.updateForecastLocation(location.toForecastLocationProfile())
                settingsRepository.updateOrchardRegion("")
            } finally {
                busy = false
            }
        }
    }

    fun deleteGrowingLocation(locationId: String) {
        viewModelScope.launch {
            busy = true
            try {
                val snapshot = settingsRepository.snapshot()
                if (snapshot.defaultLocationId == locationId) return@launch
                repository.deleteGrowingLocation(locationId)
            } finally {
                busy = false
            }
        }
    }

    fun updateLocationSearchQuery(query: String) {
        locationSearchQuery = query
        locationSearchError = null
        val trimmed = query.trim()
        locationSearchJob?.cancel()
        if (trimmed.length < 2) {
            locationSearchResults = emptyList()
            locationSearchBusy = false
            return
        }
        locationSearchJob = viewModelScope.launch {
            delay(250)
            val activeQuery = locationSearchQuery.trim()
            if (activeQuery.length < 2) {
                locationSearchBusy = false
                locationSearchResults = emptyList()
                return@launch
            }
            locationSearchBusy = true
            try {
                val results = repository.searchGrowingLocations(activeQuery)
                if (activeQuery != locationSearchQuery.trim()) return@launch
                locationSearchResults = results
                locationSearchError = if (results.isEmpty()) "No matches found." else null
            } catch (_: Exception) {
                if (activeQuery != locationSearchQuery.trim()) return@launch
                locationSearchResults = emptyList()
                locationSearchError = "Location search is unavailable right now."
            } finally {
                if (activeQuery == locationSearchQuery.trim()) {
                    locationSearchBusy = false
                }
            }
        }
    }

    fun clearLocationSearch() {
        locationSearchJob?.cancel()
        locationSearchQuery = ""
        locationSearchResults = emptyList()
        locationSearchError = null
        locationSearchBusy = false
    }

    fun searchLocationMatches(query: String = locationSearchQuery) {
        viewModelScope.launch {
            val trimmed = query.trim()
            locationSearchQuery = query
            if (trimmed.length < 2) {
                locationSearchResults = emptyList()
                locationSearchError = "Enter at least 2 characters."
                return@launch
            }
            locationSearchBusy = true
            locationSearchError = null
            try {
                locationSearchResults = repository.searchGrowingLocations(trimmed)
                if (locationSearchResults.isEmpty()) {
                    locationSearchError = "No matches found."
                }
            } catch (_: Exception) {
                locationSearchResults = emptyList()
                locationSearchError = "Location search is unavailable right now."
            } finally {
                locationSearchBusy = false
            }
        }
    }

    fun refreshLocationClimate(locationId: String) {
        viewModelScope.launch {
            busy = true
            try {
                val refreshed = repository.refreshLocationClimate(locationId) ?: return@launch
                val snapshot = settingsRepository.snapshot()
                if (snapshot.defaultLocationId == refreshed.id) {
                    settingsRepository.updateForecastLocation(refreshed.toForecastLocationProfile())
                }
            } finally {
                busy = false
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            busy = true
            backupManager.exportTo(uri)
            busy = false
        }
    }

    fun exportDiagnostics(uri: Uri) {
        viewModelScope.launch {
            busy = true
            diagnosticsStore.exportTo(uri)
            busy = false
        }
    }

    fun validateImport(uri: Uri) {
        viewModelScope.launch {
            busy = true
            pendingImport = backupManager.validateImport(uri)
            pendingImportUri = uri
            busy = false
        }
    }

    fun dismissPendingImport() {
        pendingImport = null
        pendingImportUri = null
    }

    fun importReplaceAll() {
        val uri = pendingImportUri ?: return
        viewModelScope.launch {
            busy = true
            backupManager.importReplaceAll(uri)
            repository.ensureGrowingLocations(settingsRepository.settings.first())
            busy = false
            dismissPendingImport()
        }
    }

    fun requestClearAll() {
        confirmClearAll = true
    }

    fun dismissClearAll() {
        confirmClearAll = false
    }

    fun requestLoadSample() {
        confirmLoadSample = true
    }

    fun dismissLoadSample() {
        confirmLoadSample = false
    }

    fun clearAllData() {
        viewModelScope.launch {
            busy = true
            backupManager.clearAllData()
            confirmClearAll = false
            busy = false
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            busy = true
            confirmLoadSample = false
            repository.loadSampleDataReplaceAll()
            repository.ensureGrowingLocations(settingsRepository.settings.first())
            busy = false
        }
    }
}

object OrchardViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                orchardDexApplication().container.repository,
                orchardDexApplication().container.settingsRepository
            )
        }
        initializer { TreesViewModel(orchardDexApplication().container.repository) }
        initializer {
            HistoryViewModel(
                orchardDexApplication().container.repository,
                orchardDexApplication().container.settingsRepository
            )
        }
        initializer { HistoryDetailViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer {
            TreeFormViewModel(
                this.createSavedStateHandle(),
                orchardDexApplication().container.repository,
                orchardDexApplication().container.settingsRepository,
                orchardDexApplication().container.diagnosticsStore
            )
        }
        initializer { TreeDetailViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { LogFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { EventFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { HarvestFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer {
            DexViewModel(
                orchardDexApplication().container.repository,
                orchardDexApplication().container.diagnosticsStore
            )
        }
        initializer { ReminderListViewModel(orchardDexApplication().container.repository) }
        initializer {
            ReminderFormViewModel(
                this.createSavedStateHandle(),
                orchardDexApplication().container.repository,
                orchardDexApplication().container.settingsRepository
            )
        }
        initializer {
            SettingsViewModel(
                orchardDexApplication().container.repository,
                orchardDexApplication().container.settingsRepository,
                orchardDexApplication().container.backupManager,
                orchardDexApplication().container.diagnosticsStore
            )
        }
    }
}

private fun CreationExtras.orchardDexApplication(): OrchardDexApp =
    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as OrchardDexApp
