package com.dillon.orcharddex.data.repository

import androidx.room.withTransaction
import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.TreeWithPhotos
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.DashboardDetailItem
import com.dillon.orcharddex.data.model.DashboardModel
import com.dillon.orcharddex.data.model.DexCultivarEntry
import com.dillon.orcharddex.data.model.DexModel
import com.dillon.orcharddex.data.model.DexSpeciesGroup
import com.dillon.orcharddex.data.model.EventInput
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.HarvestInput
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.RecentActivityItem
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.ReminderInput
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.TreeDetailModel
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistInput
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.dillon.orcharddex.sample.SampleDataSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class OrchardRepository(
    private val database: OrchardDexDatabase,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: PhotoStorage,
    private val reminderScheduler: ReminderScheduler,
    private val sampleDataSeeder: SampleDataSeeder
) {
    private val treeDao = database.treeDao()
    private val treePhotoDao = database.treePhotoDao()
    private val eventDao = database.eventDao()
    private val harvestDao = database.harvestDao()
    private val reminderDao = database.reminderDao()
    private val wishlistDao = database.wishlistDao()

    fun observeTrees(): Flow<List<TreeListItem>> = treeDao.observeTreesWithPhotos().map { trees ->
        trees.map { item ->
            TreeListItem(
                tree = item.tree,
                mainPhotoPath = item.photos.sortedBy(TreePhotoEntity::sortOrder).firstOrNull()?.relativePath
            )
        }
    }

    fun observeTreeDetail(treeId: String): Flow<TreeDetailModel?> = combine(
        treeDao.observeTreeWithPhotos(treeId),
        eventDao.observeEventsForTree(treeId),
        harvestDao.observeHarvestsForTree(treeId),
        reminderDao.observeRemindersForTree(treeId)
    ) { treeWithPhotos, events, harvests, reminders ->
        treeWithPhotos?.let {
            TreeDetailModel(
                tree = it.tree,
                photos = it.photos.sortedBy(TreePhotoEntity::sortOrder),
                events = events,
                harvests = harvests,
                reminders = reminders
            )
        }
    }

    fun observeDashboard(): Flow<DashboardModel> = combine(
        treeDao.observeTreesWithPhotos(),
        eventDao.observeAllEvents(),
        harvestDao.observeAllHarvests(),
        reminderDao.observeAllReminders(),
        wishlistDao.observeAll()
    ) { trees, events, harvests, reminders, wishlist ->
        val now = System.currentTimeMillis()
        val in7 = now + 7L * 24 * 60 * 60 * 1000
        val in30 = now + 30L * 24 * 60 * 60 * 1000
        val activeTrees = trees.map(TreeWithPhotos::tree).filter { it.status == TreeStatus.ACTIVE }
        val treesById = trees.map(TreeWithPhotos::tree).associateBy(TreeEntity::id)
        val fruitedTreeIds = fruitingTreeIds(activeTrees, harvests)
        val awaitingFirstFruitTrees = activeTrees
            .filterNot { it.id in fruitedTreeIds }
            .sortedBy(TreeEntity::plantedDate)
        val upcoming7Items = reminders
            .filter { it.enabled && it.completedAt == null && it.dueAt in now..in7 }
            .sortedBy(ReminderEntity::dueAt)
            .map { reminder ->
                val tree = reminder.treeId?.let(treesById::get)
                DashboardDetailItem(
                    id = reminder.id,
                    title = reminder.title,
                    subtitle = tree?.displayName() ?: "General orchard",
                    date = reminder.dueAt,
                    treeId = reminder.treeId
                )
            }
        val treeItems = trees
            .map(TreeWithPhotos::tree)
            .sortedBy { it.displayName().lowercase() }
            .map { tree ->
                DashboardDetailItem(
                    id = tree.id,
                    title = tree.displayName(),
                    subtitle = tree.status.label(),
                    date = tree.plantedDate,
                    treeId = tree.id
                )
            }
        val cultivarItems = activeTrees
            .groupBy { it.species.normalized() to it.cultivar.normalized() }
            .values
            .sortedBy { cultivarTrees -> cultivarTrees.first().cultivar.lowercase() }
            .map { cultivarTrees ->
                val tree = cultivarTrees.first()
                DashboardDetailItem(
                    id = "${tree.species.normalized()}::${tree.cultivar.normalized()}",
                    title = listOf(tree.cultivar, tree.species).filter(String::isNotBlank).joinToString(" "),
                    subtitle = "${cultivarTrees.size} active plant${if (cultivarTrees.size == 1) "" else "s"}"
                )
            }
        val speciesItems = trees
            .map(TreeWithPhotos::tree)
            .groupBy { it.species.normalized() }
            .values
            .sortedBy { speciesTrees -> speciesTrees.first().species.lowercase() }
            .map { speciesTrees ->
                val tree = speciesTrees.first()
                DashboardDetailItem(
                    id = tree.species.normalized(),
                    title = tree.species,
                    subtitle = "${speciesTrees.size} plant${if (speciesTrees.size == 1) "" else "s"}"
                )
            }
        val wishlistItems = wishlist
            .filterNot(WishlistCultivarEntity::acquired)
            .map { entry ->
                DashboardDetailItem(
                    id = entry.id,
                    title = "${entry.species} - ${entry.cultivar}",
                    subtitle = buildString {
                        append(entry.priority.name.lowercase())
                        if (entry.notes.isNotBlank()) {
                            append(" - ")
                            append(entry.notes)
                        }
                    },
                    treeId = entry.linkedTreeId
                )
            }
        val awaitingFirstFruitItems = awaitingFirstFruitTrees
            .map { tree ->
                DashboardDetailItem(
                    id = tree.id,
                    title = tree.displayName(),
                    subtitle = tree.species,
                    date = tree.plantedDate,
                    treeId = tree.id
                )
            }
        val recentActivity = (
            events.map { event ->
                val tree = trees.firstOrNull { it.tree.id == event.treeId }?.tree
                RecentActivityItem(
                    id = event.id,
                    title = event.eventType.displayLabel(),
                    subtitle = tree?.displayName() ?: "Orchard task",
                    date = event.eventDate,
                    kind = ActivityKind.EVENT,
                    treeId = event.treeId
                )
            } + harvests.map { harvest ->
                val tree = trees.firstOrNull { it.tree.id == harvest.treeId }?.tree
                RecentActivityItem(
                    id = harvest.id,
                    title = "Harvest",
                    subtitle = "${tree?.displayName() ?: "Tree"} • ${harvest.quantityValue.trimmed()} ${harvest.quantityUnit}",
                    date = harvest.harvestDate,
                    kind = ActivityKind.HARVEST,
                    treeId = harvest.treeId
                )
            }
        ).sortedByDescending(RecentActivityItem::date).take(8)

        DashboardModel(
            totalTreeCount = trees.size,
            cultivarCount = activeTrees.distinctBy { it.species.normalized() to it.cultivar.normalized() }.size,
            upcoming7Count = reminders.count { it.enabled && it.completedAt == null && it.dueAt in now..in7 },
            upcoming30Count = reminders.count { it.enabled && it.completedAt == null && it.dueAt in now..in30 },
            speciesCount = trees.map(TreeWithPhotos::tree).distinctBy { it.species.normalized() }.size,
            wishlistCount = wishlist.count { !it.acquired },
            awaitingFirstFruitCount = awaitingFirstFruitTrees.size,
            recentActivity = recentActivity,
            recentHarvests = harvests.sortedByDescending(HarvestEntity::harvestDate).take(5),
            treeItems = treeItems,
            cultivarItems = cultivarItems,
            speciesItems = speciesItems,
            wishlistItems = wishlistItems,
            awaitingFirstFruitItems = awaitingFirstFruitItems,
            upcoming7Items = upcoming7Items
        )
    }

    fun observeDex(): Flow<DexModel> = combine(
        treeDao.observeTrees(),
        harvestDao.observeAllHarvests(),
        wishlistDao.observeAll()
    ) { trees, harvests, wishlist ->
        val fruitedTreeIds = fruitingTreeIds(trees, harvests)
        val groupedTrees = trees.groupBy { it.species.trim() to it.cultivar.trim() }
        val speciesGroups = groupedTrees.entries
            .sortedBy { it.key.first.lowercase() }
            .groupBy { it.key.first.trim() }
            .map { (species, cultivarEntries) ->
                DexSpeciesGroup(
                    species = species,
                    cultivars = cultivarEntries.map { (key, cultivarTrees) ->
                        val activeCount = cultivarTrees.count { it.status == TreeStatus.ACTIVE }
                        val linkedTreeId = cultivarTrees.firstOrNull { it.status == TreeStatus.ACTIVE }?.id
                        val wishlistMatch = wishlist.any {
                            !it.acquired &&
                                it.species.normalized() == key.first.normalized() &&
                                it.cultivar.normalized() == key.second.normalized()
                        }
                        DexCultivarEntry(
                            species = key.first,
                            cultivar = key.second,
                            activeTreeCount = activeCount,
                            inactiveTreeCount = cultivarTrees.size - activeCount,
                            firstFruitAchieved = cultivarTrees.any { it.id in fruitedTreeIds },
                            wishlist = wishlistMatch,
                            linkedTreeId = linkedTreeId
                        )
                    }.sortedBy { it.cultivar.lowercase() }
                )
            }

        DexModel(
            ownedGroups = speciesGroups,
            wishlistEntries = wishlist.sortedWith(compareBy(WishlistCultivarEntity::acquired, WishlistCultivarEntity::species, WishlistCultivarEntity::cultivar)),
            ownedCultivarCount = groupedTrees.size,
            wishlistCount = wishlist.count { !it.acquired },
            firstFruitCount = groupedTrees.values.count { cultivarTrees ->
                cultivarTrees.any { it.id in fruitedTreeIds }
            }
        )
    }

    fun observeReminders(): Flow<List<ReminderListItem>> = combine(
        reminderDao.observeAllReminders(),
        treeDao.observeTrees()
    ) { reminders, trees ->
        reminders.map { reminder ->
            val tree = trees.firstOrNull { it.id == reminder.treeId }
            ReminderListItem(
                reminder = reminder,
                treeLabel = tree?.displayName(),
                species = tree?.species
            )
        }
    }

    fun observeHistory(): Flow<List<HistoryEntryModel>> = combine(
        treeDao.observeTrees(),
        eventDao.observeAllEvents(),
        harvestDao.observeAllHarvests()
    ) { trees, events, harvests ->
        buildHistoryEntries(trees, events, harvests)
    }

    fun observeTreeNames(): Flow<List<TreeEntity>> = treeDao.observeTrees()

    fun observeOrchardNames(): Flow<List<String>> = treeDao.observeOrchardNames()

    fun observeSpeciesNames(): Flow<List<String>> = treeDao.observeSpeciesNames()

    fun observeCultivarNames(): Flow<List<String>> = treeDao.observeCultivarNames().map { cultivars ->
        cultivars.filter(String::isNotBlank)
    }

    suspend fun getTree(treeId: String): TreeEntity? = treeDao.getTree(treeId)

    suspend fun getTreeDetailSnapshot(treeId: String): TreeDetailModel? = withContext(Dispatchers.IO) {
        val tree = treeDao.getTreeWithPhotos(treeId) ?: return@withContext null
        TreeDetailModel(
            tree = tree.tree,
            photos = tree.photos.sortedBy(TreePhotoEntity::sortOrder),
            events = eventDao.getAllEvents().filter { it.treeId == treeId },
            harvests = harvestDao.getAllHarvests().filter { it.treeId == treeId },
            reminders = reminderDao.getAllReminders().filter { it.treeId == treeId }
        )
    }

    suspend fun getHistoryEntry(kind: ActivityKind, entryId: String): HistoryEntryModel? = withContext(Dispatchers.IO) {
        buildHistoryEntries(
            trees = treeDao.getAllTrees(),
            events = eventDao.getAllEvents(),
            harvests = harvestDao.getAllHarvests()
        ).firstOrNull { it.kind == kind && it.id == entryId }
    }

    suspend fun getReminder(reminderId: String): ReminderEntity? = reminderDao.getReminder(reminderId)

    suspend fun saveTree(input: TreeInput): String = withContext(Dispatchers.IO) {
        require(input.species.isNotBlank()) { "Species is required." }
        val quantity = input.quantity.coerceAtLeast(1)
        val now = System.currentTimeMillis()
        val existing = input.id?.let { treeDao.getTree(it) }
        val globalOrchardName = settingsRepository.snapshot().orchardName.trim()

        if (input.id != null || quantity == 1) {
            val treeId = input.id ?: UUID.randomUUID().toString()
            database.withTransaction {
                val entity = buildTreeEntity(
                    input = input,
                    treeId = treeId,
                    globalOrchardName = globalOrchardName,
                    existing = existing,
                    timestamp = now
                )
                treeDao.insert(entity)

                if (input.removedPhotoIds.isNotEmpty()) {
                    val photos = treePhotoDao.getPhotosByIds(input.removedPhotoIds)
                    photos.forEach { photoStorage.deletePhoto(it.relativePath) }
                    treePhotoDao.deleteByIds(input.removedPhotoIds)
                }

                val existingCount = treePhotoDao.getPhotosForTree(treeId).size
                input.newPhotoUris.forEachIndexed { index, uri ->
                    val relativePath = photoStorage.importPhoto(uri, PhotoStorage.Category.TREE)
                    treePhotoDao.insert(
                        TreePhotoEntity(
                            id = UUID.randomUUID().toString(),
                            treeId = treeId,
                            relativePath = relativePath,
                            caption = null,
                            createdAt = now,
                            sortOrder = existingCount + index
                        )
                    )
                }

                markWishlistAcquired(entity.species, entity.cultivar, treeId)
            }
            return@withContext treeId
        }

        val firstTreeId = UUID.randomUUID().toString()
        val treeIds = listOf(firstTreeId) + List(quantity - 1) { UUID.randomUUID().toString() }
        database.withTransaction {
            val trees = treeIds.mapIndexed { index, treeId ->
                buildTreeEntity(
                    input = input,
                    treeId = treeId,
                    globalOrchardName = globalOrchardName,
                    existing = null,
                    timestamp = now + index
                )
            }
            treeDao.insertAll(trees)

            treeIds.forEach { treeId ->
                input.newPhotoUris.forEachIndexed { index, uri ->
                    val relativePath = photoStorage.importPhoto(uri, PhotoStorage.Category.TREE)
                    treePhotoDao.insert(
                        TreePhotoEntity(
                            id = UUID.randomUUID().toString(),
                            treeId = treeId,
                            relativePath = relativePath,
                            caption = null,
                            createdAt = now,
                            sortOrder = index
                        )
                    )
                }
            }

            val firstTree = trees.first()
            markWishlistAcquired(firstTree.species, firstTree.cultivar, firstTree.id)
        }
        firstTreeId
    }

    suspend fun deleteTree(treeId: String) = withContext(Dispatchers.IO) {
        val photos = treePhotoDao.getPhotosForTree(treeId)
        photos.forEach { photoStorage.deletePhoto(it.relativePath) }
        treeDao.delete(treeId)
    }

    suspend fun addTreePhotos(treeId: String, uris: List<android.net.Uri>) = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext
        val now = System.currentTimeMillis()
        val existingCount = treePhotoDao.getPhotosForTree(treeId).size
        uris.forEachIndexed { index, uri ->
            val relativePath = photoStorage.importPhoto(uri, PhotoStorage.Category.TREE)
            treePhotoDao.insert(
                TreePhotoEntity(
                    id = UUID.randomUUID().toString(),
                    treeId = treeId,
                    relativePath = relativePath,
                    caption = null,
                    createdAt = now,
                    sortOrder = existingCount + index
                )
            )
        }
    }

    suspend fun addEvent(input: EventInput) = addEvents(listOf(input))

    suspend fun addEvents(inputs: List<EventInput>) = withContext(Dispatchers.IO) {
        if (inputs.isEmpty()) return@withContext

        val sharedPhotoUri = inputs.first().photoUri
        val sharedPhotoPath = if (
            sharedPhotoUri != null &&
            inputs.all { it.photoUri == sharedPhotoUri }
        ) {
            photoStorage.importPhoto(sharedPhotoUri, PhotoStorage.Category.EVENT)
        } else {
            null
        }
        val now = System.currentTimeMillis()

        eventDao.insertAll(
            inputs.map { input ->
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    treeId = input.treeId,
                    eventType = input.eventType,
                    eventDate = input.eventDate,
                    notes = input.notes.trim(),
                    cost = input.cost,
                    quantityValue = input.quantityValue,
                    quantityUnit = input.quantityUnit.trim().takeIf(String::isNotBlank),
                    photoPath = sharedPhotoPath ?: input.photoUri?.let {
                        photoStorage.importPhoto(it, PhotoStorage.Category.EVENT)
                    },
                    createdAt = now
                )
            }
        )
    }

    suspend fun addHarvest(input: HarvestInput) = withContext(Dispatchers.IO) {
        val photoPath = input.photoUri?.let { photoStorage.importPhoto(it, PhotoStorage.Category.HARVEST) }
        harvestDao.insert(
            HarvestEntity(
                id = UUID.randomUUID().toString(),
                treeId = input.treeId,
                harvestDate = input.harvestDate,
                quantityValue = input.quantityValue,
                quantityUnit = input.quantityUnit.trim(),
                qualityRating = input.qualityRating,
                firstFruit = input.firstFruit,
                notes = input.notes.trim(),
                photoPath = photoPath,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveReminder(input: ReminderInput): String = withContext(Dispatchers.IO) {
        require(input.title.isNotBlank()) { "Reminder title is required." }
        val now = System.currentTimeMillis()
        val existing = input.id?.let { reminderDao.getReminder(it) }
        val reminder = ReminderEntity(
            id = input.id ?: UUID.randomUUID().toString(),
            treeId = input.treeId,
            title = input.title.trim(),
            notes = input.notes.trim(),
            dueAt = input.dueAt,
            hasTime = input.hasTime,
            recurrenceType = input.recurrenceType,
            recurrenceIntervalDays = input.recurrenceIntervalDays,
            enabled = input.enabled,
            completedAt = existing?.completedAt,
            leadTimeMode = input.leadTimeMode,
            customLeadTimeHours = input.customLeadTimeHours,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        reminderDao.insert(reminder)
        if (reminder.enabled) reminderScheduler.schedule(reminder) else reminderScheduler.cancel(reminder.id)
        reminder.id
    }

    suspend fun deleteReminder(reminderId: String) = withContext(Dispatchers.IO) {
        reminderScheduler.cancel(reminderId)
        reminderDao.delete(reminderId)
    }

    suspend fun markReminderDone(reminderId: String, createLinkedEvent: Boolean) = withContext(Dispatchers.IO) {
        val reminder = reminderDao.getReminder(reminderId) ?: return@withContext
        val now = System.currentTimeMillis()
        if (createLinkedEvent && reminder.treeId != null) {
            eventDao.insert(
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    treeId = reminder.treeId,
                    eventType = reminder.title.toEventType(),
                    eventDate = now,
                    notes = reminder.notes.ifBlank { "Completed reminder: ${reminder.title}" },
                    cost = null,
                    quantityValue = null,
                    quantityUnit = null,
                    photoPath = null,
                    createdAt = now
                )
            )
        }

        val updated = reminder.nextDueAt()?.let { nextDue ->
            reminder.copy(
                dueAt = nextDue,
                completedAt = null,
                enabled = true,
                updatedAt = now
            )
        } ?: reminder.copy(
            enabled = false,
            completedAt = now,
            updatedAt = now
        )
        reminderDao.update(updated)
        if (updated.enabled) reminderScheduler.schedule(updated) else reminderScheduler.cancel(updated.id)
    }

    suspend fun addWishlist(input: WishlistInput) = withContext(Dispatchers.IO) {
        val existing = input.id?.let { null } ?: wishlistDao.findBySpeciesAndCultivar(input.species, input.cultivar)
        wishlistDao.insert(
            WishlistCultivarEntity(
                id = input.id ?: existing?.id ?: UUID.randomUUID().toString(),
                species = input.species.trim(),
                cultivar = input.cultivar.trim(),
                priority = input.priority,
                notes = input.notes.trim(),
                acquired = existing?.acquired ?: false,
                linkedTreeId = existing?.linkedTreeId,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteWishlist(wishlistId: String) = withContext(Dispatchers.IO) {
        wishlistDao.delete(wishlistId)
    }

    suspend fun loadSampleDataReplaceAll() = withContext(Dispatchers.IO) {
        clearAllDataInternal()
        val sample = sampleDataSeeder.build()
        database.withTransaction {
            treeDao.insertAll(sample.trees)
            eventDao.insertAll(sample.events)
            harvestDao.insertAll(sample.harvests)
            reminderDao.insertAll(sample.reminders)
            wishlistDao.insertAll(sample.wishlist)
        }
        val orchardName = settingsRepository.snapshot().orchardName.trim()
        if (orchardName.isNotBlank()) {
            treeDao.updateOrchardNameForAll(orchardName)
        }
        sample.reminders.forEach(reminderScheduler::schedule)
    }

    suspend fun syncOrchardName(name: String) = withContext(Dispatchers.IO) {
        treeDao.updateOrchardNameForAll(name.trim())
    }

    suspend fun currentSettingsSnapshot() = settingsRepository.snapshot()

    private fun buildTreeEntity(
        input: TreeInput,
        treeId: String,
        globalOrchardName: String,
        existing: TreeEntity?,
        timestamp: Long
    ): TreeEntity = TreeEntity(
        id = treeId,
        orchardName = globalOrchardName.ifBlank { existing?.orchardName ?: input.orchardName.trim() },
        sectionName = input.sectionName.trim(),
        nickname = input.nickname.trim().takeIf(String::isNotBlank),
        species = input.species.trim(),
        cultivar = input.cultivar.trim(),
        rootstock = input.rootstock.trim().takeIf(String::isNotBlank),
        source = input.source.trim().takeIf(String::isNotBlank),
        purchaseDate = input.purchaseDate,
        plantedDate = input.plantedDate,
        plantType = input.plantType,
        containerSize = input.containerSize.trim().takeIf(String::isNotBlank),
        sunExposure = input.sunExposure.trim().takeIf(String::isNotBlank),
        frostSensitivity = input.frostSensitivity,
        frostSensitivityNote = input.frostSensitivityNote.trim().takeIf(String::isNotBlank),
        irrigationNote = input.irrigationNote.trim().takeIf(String::isNotBlank),
        status = input.status,
        hasFruitedBefore = input.hasFruitedBefore,
        notes = input.notes.trim(),
        tags = input.tags.trim(),
        createdAt = existing?.createdAt ?: timestamp,
        updatedAt = timestamp
    )

    private suspend fun markWishlistAcquired(species: String, cultivar: String, treeId: String) {
        if (cultivar.isBlank()) return
        val match = wishlistDao.findBySpeciesAndCultivar(species, cultivar) ?: return
        wishlistDao.insert(match.copy(acquired = true, linkedTreeId = treeId))
    }

    private suspend fun clearAllDataInternal() {
        reminderDao.getActiveReminders().forEach { reminderScheduler.cancel(it.id) }
        database.withTransaction {
            eventDao.clearAll()
            harvestDao.clearAll()
            reminderDao.clearAll()
            treePhotoDao.clearAll()
            treeDao.clearAll()
            wishlistDao.clearAll()
        }
        photoStorage.clearAll()
    }

    private fun buildHistoryEntries(
        trees: List<TreeEntity>,
        events: List<EventEntity>,
        harvests: List<HarvestEntity>
    ): List<HistoryEntryModel> {
        val treesById = trees.associateBy(TreeEntity::id)
        return (
            events.map { event ->
                val tree = treesById[event.treeId]
                HistoryEntryModel(
                    id = event.id,
                    kind = ActivityKind.EVENT,
                    treeId = event.treeId,
                    treeLabel = tree?.displayName() ?: "Unknown tree",
                    orchardName = tree?.orchardName.orEmpty(),
                    species = tree?.species.orEmpty(),
                    cultivar = tree?.cultivar.orEmpty(),
                    date = event.eventDate,
                    createdAt = event.createdAt,
                    title = event.eventType.displayLabel(),
                    preview = buildEventPreview(event),
                    notes = event.notes,
                    eventType = event.eventType,
                    quantityValue = event.quantityValue,
                    quantityUnit = event.quantityUnit,
                    cost = event.cost,
                    photoPath = event.photoPath
                )
            } +
                harvests.map { harvest ->
                    val tree = treesById[harvest.treeId]
                    HistoryEntryModel(
                        id = harvest.id,
                        kind = ActivityKind.HARVEST,
                        treeId = harvest.treeId,
                        treeLabel = tree?.displayName() ?: "Unknown tree",
                        orchardName = tree?.orchardName.orEmpty(),
                        species = tree?.species.orEmpty(),
                        cultivar = tree?.cultivar.orEmpty(),
                        date = harvest.harvestDate,
                        createdAt = harvest.createdAt,
                        title = if (harvest.firstFruit) "First fruit harvest" else "Harvest",
                        preview = buildHarvestPreview(harvest),
                        notes = harvest.notes,
                        quantityValue = harvest.quantityValue,
                        quantityUnit = harvest.quantityUnit,
                        qualityRating = harvest.qualityRating,
                        firstFruit = harvest.firstFruit,
                        photoPath = harvest.photoPath
                    )
                }
            ).sortedWith(
            compareByDescending<HistoryEntryModel> { it.date }
                .thenByDescending { it.createdAt }
        )
    }
}

fun TreeEntity.displayName(): String = when {
    !nickname.isNullOrBlank() && cultivar.isNotBlank() -> "$nickname ($cultivar)"
    !nickname.isNullOrBlank() -> nickname
    cultivar.isNotBlank() -> "$cultivar ${species.trim()}"
    else -> species.trim()
}.trim()

fun TreeEntity.speciesCultivarLabel(): String = speciesCultivarLabel(species, cultivar)

fun speciesCultivarLabel(species: String, cultivar: String): String = when {
    species.isBlank() -> cultivar.trim()
    cultivar.isBlank() -> species.trim()
    else -> "${species.trim()} • ${cultivar.trim()}"
}

private fun String.normalized(): String = trim().lowercase()

private fun Double.trimmed(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

private fun TreeStatus.label(): String = name.lowercase().replaceFirstChar(Char::uppercase)

private fun buildEventPreview(event: EventEntity): String = listOfNotNull(
    event.quantityValue?.let { value ->
        listOf(value.trimmed(), event.quantityUnit.orEmpty()).joinToString(" ").trim()
            .takeIf(String::isNotBlank)
    },
    event.cost?.let { "Cost \$${it.trimmed()}" },
    event.notes.takeIf(String::isNotBlank)
).joinToString(" - ")

private fun buildHarvestPreview(harvest: HarvestEntity): String = listOfNotNull(
    "${harvest.quantityValue.trimmed()} ${harvest.quantityUnit}".trim(),
    "Quality ${harvest.qualityRating}/5",
    "First fruit".takeIf { harvest.firstFruit },
    harvest.notes.takeIf(String::isNotBlank)
).joinToString(" - ")

private fun fruitingTreeIds(trees: List<TreeEntity>, harvests: List<HarvestEntity>): Set<String> =
    harvests.map(HarvestEntity::treeId).toSet() + trees.filter(TreeEntity::hasFruitedBefore).map(TreeEntity::id)

private fun EventType.displayLabel(): String = when (this) {
    EventType.PLANTED -> "Planted"
    EventType.REPOTTED -> "Repotted"
    EventType.PRUNED -> "Pruned"
    EventType.FERTILIZED -> "Fertilized"
    EventType.SPRAYED -> "Sprayed"
    EventType.BLOOM -> "Bloom"
    EventType.FRUIT_SET -> "Fruit set"
    EventType.HARVEST -> "Harvest"
    EventType.PEST_OBSERVED -> "Pest observed"
    EventType.DISEASE_OBSERVED -> "Disease observed"
    EventType.FROST_DAMAGE -> "Frost damage"
    EventType.HEAT_STRESS -> "Heat stress"
    EventType.GRAFTED -> "Grafted"
    EventType.WATERED -> "Watered"
    EventType.NOTE -> "Note"
}

private fun String.toEventType(): EventType {
    val normalized = lowercase()
    return when {
        normalized.contains("fertil") -> EventType.FERTILIZED
        normalized.contains("prun") -> EventType.PRUNED
        normalized.contains("spray") -> EventType.SPRAYED
        normalized.contains("harvest") -> EventType.HARVEST
        normalized.contains("repot") -> EventType.REPOTTED
        normalized.contains("bloom") -> EventType.BLOOM
        normalized.contains("water") || normalized.contains("moisture") -> EventType.WATERED
        else -> EventType.NOTE
    }
}

private fun ReminderEntity.nextDueAt(): Long? {
    val zone = ZoneId.systemDefault()
    val current = Instant.ofEpochMilli(dueAt).atZone(zone)
    return when (recurrenceType) {
        RecurrenceType.NONE -> null
        RecurrenceType.DAILY -> current.plusDays(1).toInstant().toEpochMilli()
        RecurrenceType.WEEKLY -> current.plusWeeks(1).toInstant().toEpochMilli()
        RecurrenceType.MONTHLY -> current.plusMonths(1).toInstant().toEpochMilli()
        RecurrenceType.EVERY_X_DAYS -> current.plusDays((recurrenceIntervalDays ?: 1).toLong()).toInstant().toEpochMilli()
    }
}
