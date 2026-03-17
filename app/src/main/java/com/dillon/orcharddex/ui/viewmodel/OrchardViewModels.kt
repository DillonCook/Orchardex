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
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.model.CommonSpeciesSuggestions
import com.dillon.orcharddex.data.model.EventInput
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.HarvestInput
import com.dillon.orcharddex.data.model.LeadTimeMode
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.ReminderInput
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistInput
import com.dillon.orcharddex.data.model.WishlistPriority
import com.dillon.orcharddex.data.preferences.AppSettings
import com.dillon.orcharddex.data.preferences.AppThemeMode
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.repository.OrchardRepository
import com.dillon.orcharddex.ui.epochToLocalDate
import com.dillon.orcharddex.ui.epochToLocalTime
import com.dillon.orcharddex.ui.localDateAtStartOfDay
import com.dillon.orcharddex.ui.localDateWithTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

private const val TREE_ID = "treeId"
private const val REMINDER_ID = "reminderId"

class DashboardViewModel(
    private val repository: OrchardRepository
) : ViewModel() {
    val dashboard = repository.observeDashboard().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.dillon.orcharddex.data.model.DashboardModel()
    )

    var confirmLoadSample by mutableStateOf(false)
        private set

    var busy by mutableStateOf(false)
        private set

    fun requestLoadSampleConfirmation() {
        confirmLoadSample = true
    }

    fun dismissLoadSampleConfirmation() {
        confirmLoadSample = false
    }

    fun loadSampleData() {
        viewModelScope.launch {
            busy = true
            confirmLoadSample = false
            repository.loadSampleDataReplaceAll()
            busy = false
        }
    }
}

class TreesViewModel(
    repository: OrchardRepository
) : ViewModel() {
    val trees = repository.observeTrees().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val species = repository.observeSpeciesNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CommonSpeciesSuggestions
    )
    val cultivars = repository.observeCultivarNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val orchards = repository.observeOrchardNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}

data class TreeFormState(
    val id: String? = null,
    val orchardName: String = "",
    val sectionName: String = "",
    val nickname: String = "",
    val species: String = "",
    val cultivar: String = "",
    val rootstock: String = "",
    val source: String = "",
    val purchaseDate: LocalDate? = null,
    val plantedDate: LocalDate = LocalDate.now(),
    val plantType: PlantType = PlantType.IN_GROUND,
    val containerSize: String = "",
    val sunExposure: String = "",
    val frostSensitivity: FrostSensitivityLevel = FrostSensitivityLevel.MEDIUM,
    val frostSensitivityNote: String = "",
    val irrigationNote: String = "",
    val status: TreeStatus = TreeStatus.ACTIVE,
    val notes: String = "",
    val tags: String = "",
    val existingPhotos: List<TreePhotoEntity> = emptyList(),
    val newPhotoUris: List<Uri> = emptyList(),
    val removedPhotoIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class TreeFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val treeId: String? = savedStateHandle[TREE_ID]
    var state by mutableStateOf(TreeFormState(isLoading = treeId != null))
        private set

    val orchardNames = repository.observeOrchardNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val speciesNames = repository.observeSpeciesNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CommonSpeciesSuggestions
    )

    init {
        if (treeId != null) {
            viewModelScope.launch {
                val detail = repository.getTreeDetailSnapshot(treeId)
                state = detail?.let {
                    TreeFormState(
                        id = it.tree.id,
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
                        notes = it.tree.notes,
                        tags = it.tree.tags,
                        existingPhotos = it.photos,
                        isLoading = false
                    )
                } ?: TreeFormState(isLoading = false)
            }
        }
    }

    fun update(update: TreeFormState.() -> TreeFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun addPhotos(uris: List<Uri>) {
        state = state.copy(newPhotoUris = state.newPhotoUris + uris)
    }

    fun removeNewPhoto(uri: Uri) {
        state = state.copy(newPhotoUris = state.newPhotoUris - uri)
    }

    fun removeExistingPhoto(photoId: String) {
        state = state.copy(
            existingPhotos = state.existingPhotos.filterNot { it.id == photoId },
            removedPhotoIds = state.removedPhotoIds + photoId
        )
    }

    fun save(onSaved: (String) -> Unit) {
        if (state.species.isBlank() || state.cultivar.isBlank()) {
            state = state.copy(errorMessage = "Species and cultivar are required.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true, errorMessage = null)
            val savedTreeId = repository.saveTree(
                TreeInput(
                    id = state.id,
                    orchardName = state.orchardName,
                    sectionName = state.sectionName,
                    nickname = state.nickname,
                    species = state.species,
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
                    notes = state.notes,
                    tags = state.tags,
                    newPhotoUris = state.newPhotoUris,
                    removedPhotoIds = state.removedPhotoIds.toList()
                )
            )
            state = state.copy(isSaving = false)
            onSaved(savedTreeId)
        }
    }
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
}

data class EventFormState(
    val treeId: String? = null,
    val eventType: EventType = EventType.NOTE,
    val eventDate: LocalDate = LocalDate.now(),
    val notes: String = "",
    val cost: String = "",
    val quantityValue: String = "",
    val quantityUnit: String = "",
    val photoUri: Uri? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class EventFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val initialTreeId: String? = savedStateHandle[TREE_ID]
    var state by mutableStateOf(EventFormState(treeId = initialTreeId))
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun update(update: EventFormState.() -> EventFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun save(onSaved: (String) -> Unit) {
        val treeId = state.treeId
        if (treeId.isNullOrBlank()) {
            state = state.copy(errorMessage = "Select a tree.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            repository.addEvent(
                EventInput(
                    treeId = treeId,
                    eventType = state.eventType,
                    eventDate = localDateAtStartOfDay(state.eventDate),
                    notes = state.notes,
                    cost = state.cost.toDoubleOrNull(),
                    quantityValue = state.quantityValue.toDoubleOrNull(),
                    quantityUnit = state.quantityUnit,
                    photoUri = state.photoUri
                )
            )
            state = state.copy(isSaving = false)
            onSaved(treeId)
        }
    }
}

data class HarvestFormState(
    val treeId: String? = null,
    val harvestDate: LocalDate = LocalDate.now(),
    val quantityValue: String = "",
    val quantityUnit: String = "fruit",
    val qualityRating: Int = 3,
    val firstFruit: Boolean = false,
    val notes: String = "",
    val photoUri: Uri? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class HarvestFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: OrchardRepository
) : ViewModel() {
    private val initialTreeId: String? = savedStateHandle[TREE_ID]
    var state by mutableStateOf(HarvestFormState(treeId = initialTreeId))
        private set

    val trees = repository.observeTreeNames().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun update(update: HarvestFormState.() -> HarvestFormState) {
        state = state.update().copy(errorMessage = null)
    }

    fun save(onSaved: (String) -> Unit) {
        val treeId = state.treeId
        val quantity = state.quantityValue.toDoubleOrNull()
        if (treeId.isNullOrBlank() || quantity == null) {
            state = state.copy(errorMessage = "Pick a tree and enter a quantity.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            repository.addHarvest(
                HarvestInput(
                    treeId = treeId,
                    harvestDate = localDateAtStartOfDay(state.harvestDate),
                    quantityValue = quantity,
                    quantityUnit = state.quantityUnit,
                    qualityRating = state.qualityRating,
                    firstFruit = state.firstFruit,
                    notes = state.notes,
                    photoUri = state.photoUri
                )
            )
            state = state.copy(isSaving = false)
            onSaved(treeId)
        }
    }
}

class DexViewModel(
    private val repository: OrchardRepository
) : ViewModel() {
    val dex = repository.observeDex().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.dillon.orcharddex.data.model.DexModel()
    )

    var addDialogVisible by mutableStateOf(false)
        private set

    var addSpecies by mutableStateOf("")
    var addCultivar by mutableStateOf("")
    var addPriority by mutableStateOf(WishlistPriority.MEDIUM)
    var addNotes by mutableStateOf("")

    fun showAddDialog() {
        addDialogVisible = true
    }

    fun hideAddDialog() {
        addDialogVisible = false
        addSpecies = ""
        addCultivar = ""
        addPriority = WishlistPriority.MEDIUM
        addNotes = ""
    }

    fun saveWishlist() {
        if (addSpecies.isBlank() || addCultivar.isBlank()) return
        viewModelScope.launch {
            repository.addWishlist(
                WishlistInput(
                    species = addSpecies,
                    cultivar = addCultivar,
                    priority = addPriority,
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

data class ReminderFormState(
    val id: String? = null,
    val treeId: String? = null,
    val title: String = "",
    val notes: String = "",
    val dueDate: LocalDate = LocalDate.now(),
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
    var state by mutableStateOf(ReminderFormState(treeId = treeIdArg, isLoading = reminderId != null))
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

    fun save(onSaved: () -> Unit) {
        if (state.title.isBlank()) {
            state = state.copy(errorMessage = "Title is required.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isSaving = true)
            val dueAt = if (state.hasTime) {
                localDateWithTime(state.dueDate, state.dueTime)
            } else {
                localDateWithTime(state.dueDate, LocalTime.of(8, 0))
            }
            repository.saveReminder(
                ReminderInput(
                    id = state.id,
                    treeId = state.treeId,
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
            )
            state = state.copy(isSaving = false)
            onSaved()
        }
    }
}

class SettingsViewModel(
    private val repository: OrchardRepository,
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {
    val settings = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )

    var busy by mutableStateOf(false)
        private set

    var pendingImport by mutableStateOf<BackupValidation?>(null)
        private set

    var pendingImportUri by mutableStateOf<Uri?>(null)
        private set

    var confirmClearAll by mutableStateOf(false)
        private set

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

    fun updateDefaultReminder(mode: LeadTimeMode, customHours: Int) {
        viewModelScope.launch {
            settingsRepository.updateDefaultLeadTime(mode, customHours)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            busy = true
            backupManager.exportTo(uri)
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
            repository.loadSampleDataReplaceAll()
            busy = false
        }
    }
}

object OrchardViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DashboardViewModel(orchardDexApplication().container.repository) }
        initializer { TreesViewModel(orchardDexApplication().container.repository) }
        initializer { TreeFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { TreeDetailViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { EventFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { HarvestFormViewModel(this.createSavedStateHandle(), orchardDexApplication().container.repository) }
        initializer { DexViewModel(orchardDexApplication().container.repository) }
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
                orchardDexApplication().container.backupManager
            )
        }
    }
}

private fun CreationExtras.orchardDexApplication(): OrchardDexApp =
    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as OrchardDexApp
