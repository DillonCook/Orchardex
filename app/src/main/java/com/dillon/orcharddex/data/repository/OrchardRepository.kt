package com.dillon.orcharddex.data.repository

import com.dillon.orcharddex.data.local.ActivityPhotoEntity
import androidx.room.withTransaction
import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.TreeWithPhotos
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.local.toForecastLocationProfile
import com.dillon.orcharddex.data.model.ActivityKind
import com.dillon.orcharddex.data.model.DashboardDetailItem
import com.dillon.orcharddex.data.model.DashboardModel
import com.dillon.orcharddex.data.model.DexCultivarEntry
import com.dillon.orcharddex.data.model.DexModel
import com.dillon.orcharddex.data.model.DexSpeciesGroup
import com.dillon.orcharddex.data.model.EventInput
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.GrowingLocationInput
import com.dillon.orcharddex.data.model.HarvestInput
import com.dillon.orcharddex.data.model.HistoryEntryModel
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.LocationSearchResult
import com.dillon.orcharddex.data.model.normalizedName
import com.dillon.orcharddex.data.model.RecentActivityItem
import com.dillon.orcharddex.data.model.RecurrenceType
import com.dillon.orcharddex.data.model.ReminderInput
import com.dillon.orcharddex.data.model.ReminderListItem
import com.dillon.orcharddex.data.model.TreeDetailModel
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.TreeListItem
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.PhenologyObservation
import com.dillon.orcharddex.data.model.WishlistInput
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.preferences.forecastLocationProfile
import com.dillon.orcharddex.data.remote.ClimateFingerprintService
import com.dillon.orcharddex.data.remote.LocationSearchService
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.dillon.orcharddex.sample.SampleDataSeeder
import com.dillon.orcharddex.time.OrchardTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

private const val EVENT_OWNER_KIND = "EVENT"
private const val HARVEST_OWNER_KIND = "HARVEST"

class OrchardRepository(
    private val database: OrchardDexDatabase,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: PhotoStorage,
    private val reminderScheduler: ReminderScheduler,
    private val sampleDataSeeder: SampleDataSeeder,
    private val locationSearchService: LocationSearchService,
    private val climateFingerprintService: ClimateFingerprintService
) {
    private val growingLocationDao = database.growingLocationDao()
    private val treeDao = database.treeDao()
    private val treePhotoDao = database.treePhotoDao()
    private val activityPhotoDao = database.activityPhotoDao()
    private val eventDao = database.eventDao()
    private val harvestDao = database.harvestDao()
    private val reminderDao = database.reminderDao()
    private val wishlistDao = database.wishlistDao()

    fun observeTrees(): Flow<List<TreeListItem>> = combine(
        treeDao.observeTreesWithPhotos(),
        growingLocationDao.observeAllLocations()
    ) { trees, locations ->
        val locationsById = locations.associateBy(GrowingLocationEntity::id)
        trees.map { item ->
            TreeListItem(
                tree = item.tree,
                mainPhotoPath = item.photos.heroOrLatestPhoto()?.relativePath,
                location = item.tree.locationId?.let(locationsById::get)
            )
        }
    }

    fun observeTreeDetail(treeId: String): Flow<TreeDetailModel?> = combine(
        treeDao.observeTreeWithPhotos(treeId),
        eventDao.observeEventsForTree(treeId),
        harvestDao.observeHarvestsForTree(treeId),
        reminderDao.observeRemindersForTree(treeId),
        growingLocationDao.observeAllLocations(),
        activityPhotoDao.observeAllPhotos()
    ) { values ->
        val treeWithPhotos = values[0] as TreeWithPhotos?
        val events = values[1] as List<EventEntity>
        val harvests = values[2] as List<HarvestEntity>
        val reminders = values[3] as List<ReminderEntity>
        val locations = values[4] as List<GrowingLocationEntity>
        val activityPhotos = values[5] as List<ActivityPhotoEntity>
        val locationsById = locations.associateBy(GrowingLocationEntity::id)
        treeWithPhotos?.let {
            TreeDetailModel(
                tree = it.tree,
                location = it.tree.locationId?.let(locationsById::get),
                photos = it.photos.sortedWith(
                    compareByDescending<TreePhotoEntity> { photo -> photo.createdAt }
                        .thenByDescending(TreePhotoEntity::sortOrder)
                ),
                activityPhotos = activityPhotos.filter { photo ->
                    (photo.ownerKind == EVENT_OWNER_KIND && events.any { it.id == photo.ownerId }) ||
                        (photo.ownerKind == HARVEST_OWNER_KIND && harvests.any { it.id == photo.ownerId })
                },
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

    fun observeAllHarvests(): Flow<List<HarvestEntity>> = harvestDao.observeAllHarvests()

    fun observeHistory(): Flow<List<HistoryEntryModel>> = combine(
        treeDao.observeTrees(),
        eventDao.observeAllEvents(),
        harvestDao.observeAllHarvests(),
        activityPhotoDao.observeAllPhotos()
    ) { trees, events, harvests, activityPhotos ->
        buildHistoryEntries(trees, events, harvests, activityPhotos)
    }

    fun observeTreeNames(): Flow<List<TreeEntity>> = treeDao.observeTrees()

    fun observeGrowingLocations(): Flow<List<GrowingLocationEntity>> = growingLocationDao.observeAllLocations()

    fun observeOrchardNames(): Flow<List<String>> = treeDao.observeOrchardNames()

    fun observeSpeciesNames(): Flow<List<String>> = treeDao.observeSpeciesNames()

    fun observeCultivarNames(): Flow<List<String>> = treeDao.observeCultivarNames().map { cultivars ->
        cultivars.filter(String::isNotBlank)
    }

    suspend fun getTree(treeId: String): TreeEntity? = treeDao.getTree(treeId)

    suspend fun getTreeDetailSnapshot(treeId: String): TreeDetailModel? = withContext(Dispatchers.IO) {
        val tree = treeDao.getTreeWithPhotos(treeId) ?: return@withContext null
        val location = tree.tree.locationId?.let { locationId -> growingLocationDao.getLocation(locationId) }
        val events = eventDao.getAllEvents().filter { it.treeId == treeId }
        val harvests = harvestDao.getAllHarvests().filter { it.treeId == treeId }
        val eventIds = events.map(EventEntity::id).toSet()
        val harvestIds = harvests.map(HarvestEntity::id).toSet()
        val activityPhotos = activityPhotoDao.getAllPhotos()
        TreeDetailModel(
            tree = tree.tree,
            location = location,
            photos = tree.photos.sortedWith(
                compareByDescending<TreePhotoEntity> { photo -> photo.createdAt }
                    .thenByDescending(TreePhotoEntity::sortOrder)
            ),
            activityPhotos = activityPhotos.filter { photo ->
                (photo.ownerKind == EVENT_OWNER_KIND && photo.ownerId in eventIds) ||
                    (photo.ownerKind == HARVEST_OWNER_KIND && photo.ownerId in harvestIds)
            },
            events = events,
            harvests = harvests,
            reminders = reminderDao.getAllReminders().filter { it.treeId == treeId }
        )
    }

    suspend fun getHistoryEntry(kind: ActivityKind, entryId: String): HistoryEntryModel? = withContext(Dispatchers.IO) {
        buildHistoryEntries(
            trees = treeDao.getAllTrees(),
            events = eventDao.getAllEvents(),
            harvests = harvestDao.getAllHarvests(),
            activityPhotos = activityPhotoDao.getAllPhotos()
        ).firstOrNull { it.kind == kind && it.id == entryId }
    }

    suspend fun getReminder(reminderId: String): ReminderEntity? = reminderDao.getReminder(reminderId)

    suspend fun saveTree(input: TreeInput): String = withContext(Dispatchers.IO) {
        require(input.species.isNotBlank()) { "Species is required." }
        val quantity = input.quantity.coerceAtLeast(1)
        val now = System.currentTimeMillis()
        val existing = input.id?.let { treeDao.getTree(it) }
        val settingsSnapshot = settingsRepository.settings.first()
        val defaultLocation = ensureDefaultGrowingLocation(settingsSnapshot, now)
        val selectedLocation = resolveTreeLocation(
            locationId = input.locationId,
            existingLocationId = existing?.locationId,
            fallbackLocation = defaultLocation
        )
        val existingDuplicateCount = if (input.id == null) {
            countMatchingTrees(input.species, input.cultivar)
        } else {
            0
        }
        val plannedNicknames = plannedTreeNicknames(input.nickname, quantity, existingDuplicateCount)

        if (input.id != null || quantity == 1) {
            val treeId = input.id ?: UUID.randomUUID().toString()
            database.withTransaction {
                val entity = buildTreeEntity(
                    input = input,
                    treeId = treeId,
                    location = selectedLocation,
                    fallbackOrchardName = settingsSnapshot.orchardName.trim(),
                    existing = existing,
                    timestamp = now,
                    nickname = plannedNicknames.firstOrNull()
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
                            sortOrder = existingCount + index,
                            isHero = false
                        )
                    )
                }

                ensureHeroPhotoSelected(treeId)

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
                    location = selectedLocation,
                    fallbackOrchardName = settingsSnapshot.orchardName.trim(),
                    existing = null,
                    timestamp = now + index,
                    nickname = plannedNicknames.getOrNull(index)
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
                            sortOrder = index,
                            isHero = false
                        )
                    )
                }
                ensureHeroPhotoSelected(treeId)
            }

            val firstTree = trees.first()
            markWishlistAcquired(firstTree.species, firstTree.cultivar, firstTree.id)
        }
        firstTreeId
    }

    suspend fun deleteTree(treeId: String) = withContext(Dispatchers.IO) {
        val eventIds = eventDao.getAllEvents()
            .filter { it.treeId == treeId }
            .map(EventEntity::id)
        val harvestIds = harvestDao.getAllHarvests()
            .filter { it.treeId == treeId }
            .map(HarvestEntity::id)
        val photos = treePhotoDao.getPhotosForTree(treeId)
        val allActivityPhotos = activityPhotoDao.getAllPhotos()
        val activityPhotoIds = allActivityPhotos
            .filter { photo ->
                (photo.ownerKind == EVENT_OWNER_KIND && photo.ownerId in eventIds) ||
                    (photo.ownerKind == HARVEST_OWNER_KIND && photo.ownerId in harvestIds)
            }
            .map(ActivityPhotoEntity::id)
        photos.forEach { photoStorage.deletePhoto(it.relativePath) }
        if (activityPhotoIds.isNotEmpty()) {
            val deletedPhotos = activityPhotoDao.getPhotosByIds(activityPhotoIds)
            val retainedPaths = allActivityPhotos
                .filterNot { it.id in activityPhotoIds }
                .map(ActivityPhotoEntity::relativePath)
                .toSet()
            deletedPhotos
                .map(ActivityPhotoEntity::relativePath)
                .distinct()
                .filterNot(retainedPaths::contains)
                .forEach { relativePath -> photoStorage.deletePhoto(relativePath) }
            activityPhotoDao.deleteByIds(activityPhotoIds)
        }
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
                    sortOrder = existingCount + index,
                    isHero = false
                )
            )
        }
        ensureHeroPhotoSelected(treeId)
    }

    suspend fun setTreeHeroPhoto(treeId: String, photoId: String) = withContext(Dispatchers.IO) {
        val photo = treePhotoDao.getPhotosByIds(listOf(photoId)).firstOrNull() ?: return@withContext
        if (photo.treeId != treeId) return@withContext
        treePhotoDao.setHeroPhoto(treeId, photoId)
    }

    suspend fun addEvent(input: EventInput) = addEvents(listOf(input))

    suspend fun addEvents(inputs: List<EventInput>) = withContext(Dispatchers.IO) {
        if (inputs.isEmpty()) return@withContext

        val now = System.currentTimeMillis()
        val sharedPhotoUris = inputs.first().photoUris
        val sharedPhotoPaths = if (
            sharedPhotoUris.isNotEmpty() &&
            inputs.all { it.photoUris == sharedPhotoUris }
        ) {
            sharedPhotoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.EVENT) }
        } else {
            emptyList()
        }
        val events = inputs.mapIndexed { index, input ->
            val photoPaths = sharedPhotoPaths.ifEmpty {
                input.photoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.EVENT) }
            }
            EventEntity(
                id = UUID.randomUUID().toString(),
                treeId = input.treeId,
                eventType = input.eventType,
                eventDate = input.eventDate,
                notes = input.notes.trim(),
                cost = input.cost,
                quantityValue = input.quantityValue,
                quantityUnit = input.quantityUnit.trim().takeIf(String::isNotBlank),
                photoPath = photoPaths.firstOrNull(),
                createdAt = now + index
            )
        }
        eventDao.insertAll(events)
        activityPhotoDao.insertAll(
            events.flatMapIndexed { index, event ->
                val photoPaths = sharedPhotoPaths.ifEmpty {
                    inputs[index].photoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.EVENT) }
                }
                photoPaths.mapIndexed { sortOrder, relativePath ->
                    ActivityPhotoEntity(
                        id = UUID.randomUUID().toString(),
                        ownerKind = EVENT_OWNER_KIND,
                        ownerId = event.id,
                        relativePath = relativePath,
                        caption = null,
                        createdAt = event.createdAt + sortOrder,
                        sortOrder = sortOrder
                    )
                }
            }
        )
    }

    suspend fun addHarvest(input: HarvestInput) = addHarvests(listOf(input))

    suspend fun addHarvests(inputs: List<HarvestInput>) = withContext(Dispatchers.IO) {
        if (inputs.isEmpty()) return@withContext

        val fruitingTreeIds = (
            harvestDao.getAllHarvests().map(HarvestEntity::treeId) +
                treeDao.getAllTrees().filter(TreeEntity::hasFruitedBefore).map(TreeEntity::id)
            ).toMutableSet()
        val now = System.currentTimeMillis()
        val sharedPhotoUris = inputs.first().photoUris
        val sharedPhotoPaths = if (
            sharedPhotoUris.isNotEmpty() &&
            inputs.all { it.photoUris == sharedPhotoUris }
        ) {
            sharedPhotoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.HARVEST) }
        } else {
            emptyList()
        }
        val harvests = inputs.mapIndexed { index, input ->
            val firstFruit = input.firstFruit || fruitingTreeIds.add(input.treeId)
            val photoPaths = sharedPhotoPaths.ifEmpty {
                input.photoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.HARVEST) }
            }
            HarvestEntity(
                id = UUID.randomUUID().toString(),
                treeId = input.treeId,
                harvestDate = input.harvestDate,
                quantityValue = input.quantityValue,
                quantityUnit = input.quantityUnit.trim(),
                qualityRating = input.qualityRating,
                firstFruit = firstFruit,
                verified = input.verified,
                notes = input.notes.trim(),
                photoPath = photoPaths.firstOrNull(),
                createdAt = now + index
            )
        }
        harvestDao.insertAll(harvests)
        activityPhotoDao.insertAll(
            harvests.flatMapIndexed { index, harvest ->
                val photoPaths = sharedPhotoPaths.ifEmpty {
                    inputs[index].photoUris.map { uri -> photoStorage.importPhoto(uri, PhotoStorage.Category.HARVEST) }
                }
                photoPaths.mapIndexed { sortOrder, relativePath ->
                    ActivityPhotoEntity(
                        id = UUID.randomUUID().toString(),
                        ownerKind = HARVEST_OWNER_KIND,
                        ownerId = harvest.id,
                        relativePath = relativePath,
                        caption = null,
                        createdAt = harvest.createdAt + sortOrder,
                        sortOrder = sortOrder
                    )
                }
            }
        )
    }

    suspend fun saveReminder(input: ReminderInput): String = saveReminders(listOf(input)).single()

    suspend fun saveReminders(inputs: List<ReminderInput>): List<String> = withContext(Dispatchers.IO) {
        require(inputs.isNotEmpty()) { "At least one reminder is required." }
        val now = System.currentTimeMillis()
        val reminders = inputs.map { input ->
            require(input.title.isNotBlank()) { "Reminder title is required." }
            val existing = input.id?.let { reminderDao.getReminder(it) }
            ReminderEntity(
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
        }
        reminders.forEach { reminder ->
            reminderDao.insert(reminder)
            if (reminder.enabled) reminderScheduler.schedule(reminder) else reminderScheduler.cancel(reminder.id)
        }
        reminders.map(ReminderEntity::id)
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
        val species = input.species.trim()
        val cultivar = input.cultivar.trim()
        val existing = input.id?.let { null } ?: wishlistDao.findBySpeciesAndCultivar(species, cultivar)
        wishlistDao.insert(
            WishlistCultivarEntity(
                id = input.id ?: existing?.id ?: UUID.randomUUID().toString(),
                species = species,
                cultivar = cultivar,
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

    suspend fun searchGrowingLocations(query: String): List<LocationSearchResult> = withContext(Dispatchers.IO) {
        locationSearchService.search(query)
    }

    suspend fun saveGrowingLocation(input: GrowingLocationInput): GrowingLocationEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = input.id?.let { locationId -> growingLocationDao.getLocation(locationId) }
        val coordinatesChanged = existing?.latitudeDeg != input.latitudeDeg || existing?.longitudeDeg != input.longitudeDeg
        var entity = GrowingLocationEntity(
            id = input.id ?: UUID.randomUUID().toString(),
            name = input.name.trim().ifBlank { "Growing location" },
            countryCode = input.countryCode.trim(),
            timezoneId = input.timezoneId.trim(),
            hemisphere = input.hemisphere,
            latitudeDeg = input.latitudeDeg,
            longitudeDeg = input.longitudeDeg,
            elevationM = input.elevationM,
            usdaZoneCode = input.usdaZoneCode?.trim()?.ifBlank { null },
            chillHoursBand = input.chillHoursBand,
            microclimateFlags = input.microclimateFlags,
            climateSource = if (input.latitudeDeg == null || input.longitudeDeg == null) null else existing?.climateSource,
            climateFetchedAt = if (input.latitudeDeg == null || input.longitudeDeg == null) null else existing?.climateFetchedAt,
            climateMeanMonthlyTempC = if (input.latitudeDeg == null || input.longitudeDeg == null) emptyList() else existing?.climateMeanMonthlyTempC.orEmpty(),
            climateMeanMonthlyMinTempC = if (input.latitudeDeg == null || input.longitudeDeg == null) emptyList() else existing?.climateMeanMonthlyMinTempC.orEmpty(),
            climateMeanMonthlyMaxTempC = if (input.latitudeDeg == null || input.longitudeDeg == null) emptyList() else existing?.climateMeanMonthlyMaxTempC.orEmpty(),
            notes = input.notes.trim(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        if (input.latitudeDeg != null && input.longitudeDeg != null && (coordinatesChanged || existing?.climateFetchedAt == null)) {
            fetchLocationClimate(input.latitudeDeg, input.longitudeDeg)?.let { climate ->
                entity = entity.withClimate(climate)
            }
        }
        growingLocationDao.insert(entity)
        treeDao.updateOrchardNameForLocation(entity.id, entity.name)
        entity
    }

    suspend fun getGrowingLocation(locationId: String): GrowingLocationEntity? = withContext(Dispatchers.IO) {
        growingLocationDao.getLocation(locationId)
    }

    suspend fun ensureGrowingLocations(settings: com.dillon.orcharddex.data.preferences.AppSettings) = withContext(Dispatchers.IO) {
        val defaultLocation = ensureDefaultGrowingLocation(settings, System.currentTimeMillis()) ?: return@withContext
        val needsDefaultIdUpdate = settings.defaultLocationId != defaultLocation.id
        val needsSettingsSync = settings.orchardName.trim() != defaultLocation.name ||
            settings.countryCode != defaultLocation.countryCode ||
            settings.timezoneId != defaultLocation.timezoneId ||
            settings.hemisphere != defaultLocation.hemisphere ||
            settings.latitudeDeg != defaultLocation.latitudeDeg ||
            settings.longitudeDeg != defaultLocation.longitudeDeg ||
            settings.elevationM != defaultLocation.elevationM ||
            settings.usdaZone != defaultLocation.usdaZoneCode.orEmpty() ||
            settings.chillHoursBand != defaultLocation.chillHoursBand ||
            settings.microclimateFlags != defaultLocation.microclimateFlags
        if (needsDefaultIdUpdate) {
            settingsRepository.updateDefaultLocationId(defaultLocation.id)
        }
        if (needsSettingsSync) {
            settingsRepository.updateOrchardName(defaultLocation.name)
            settingsRepository.updateForecastLocation(defaultLocation.toForecastLocationProfile())
        }
        treeDao.assignLocationToTreesWithoutLocation(defaultLocation.id)
    }

    suspend fun refreshLocationClimate(locationId: String): GrowingLocationEntity? = withContext(Dispatchers.IO) {
        val location = growingLocationDao.getLocation(locationId) ?: return@withContext null
        val latitude = location.latitudeDeg ?: return@withContext location
        val longitude = location.longitudeDeg ?: return@withContext location
        val fingerprint = fetchLocationClimate(latitude, longitude) ?: return@withContext location
        val updated = location.withClimate(fingerprint).copy(updatedAt = System.currentTimeMillis())
        growingLocationDao.insert(updated)
        updated
    }

    suspend fun loadSampleDataReplaceAll() = withContext(Dispatchers.IO) {
        clearAllDataInternal()
        val sample = sampleDataSeeder.build()
        database.withTransaction {
            growingLocationDao.insertAll(sample.locations)
            treeDao.insertAll(sample.trees)
            eventDao.insertAll(sample.events)
            harvestDao.insertAll(sample.harvests)
            reminderDao.insertAll(sample.reminders)
            wishlistDao.insertAll(sample.wishlist)
        }
        val defaultLocation = sample.locations.firstOrNull()
        if (defaultLocation != null) {
            settingsRepository.updateDefaultLocationId(defaultLocation.id)
            settingsRepository.updateOrchardName(defaultLocation.name)
            settingsRepository.updateForecastLocation(defaultLocation.toForecastLocationProfile())
        }
        sample.reminders.forEach(reminderScheduler::schedule)
    }

    suspend fun syncOrchardName(name: String) = withContext(Dispatchers.IO) {
        val settings = settingsRepository.snapshot()
        val defaultLocationId = settings.defaultLocationId.takeIf(String::isNotBlank)
        if (defaultLocationId != null) {
            val location = growingLocationDao.getLocation(defaultLocationId)
            if (location != null) {
                val updated = location.copy(name = name.trim().ifBlank { location.name }, updatedAt = System.currentTimeMillis())
                growingLocationDao.insert(updated)
                treeDao.updateOrchardNameForLocation(updated.id, updated.name)
                settingsRepository.updateDefaultLocationId(updated.id)
                settingsRepository.updateForecastLocation(updated.toForecastLocationProfile())
                settingsRepository.updateOrchardName(updated.name)
                return@withContext
            }
        }
        treeDao.updateOrchardNameForAll(name.trim())
    }

    suspend fun currentSettingsSnapshot() = settingsRepository.snapshot()

    private fun buildTreeEntity(
        input: TreeInput,
        treeId: String,
        location: GrowingLocationEntity?,
        fallbackOrchardName: String,
        existing: TreeEntity?,
        timestamp: Long,
        nickname: String? = input.nickname
    ): TreeEntity = TreeEntity(
        id = treeId,
        locationId = location?.id ?: existing?.locationId,
        orchardName = location?.name
            ?: fallbackOrchardName.ifBlank { existing?.orchardName ?: input.orchardName.trim() },
        sectionName = input.sectionName.trim(),
        nickname = nickname?.trim()?.takeIf(String::isNotBlank),
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
        bloomTimingMode = input.bloomTimingMode,
        customBloomStartMonth = input.customBloomStartMonth,
        customBloomStartDay = input.customBloomStartDay,
        customBloomDurationDays = input.customBloomDurationDays,
        selfCompatibilityOverride = input.selfCompatibilityOverride,
        pollinationModeOverride = input.pollinationModeOverride,
        pollinationOverrideNote = input.pollinationOverrideNote.trim().takeIf(String::isNotBlank),
        createdAt = existing?.createdAt ?: timestamp,
        updatedAt = timestamp
    )

    private suspend fun ensureDefaultGrowingLocation(
        settings: com.dillon.orcharddex.data.preferences.AppSettings,
        timestamp: Long
    ): GrowingLocationEntity? {
        val existingLocations = growingLocationDao.getAllLocations()
        val configuredDefault = settings.defaultLocationId
            .takeIf(String::isNotBlank)
            ?.let { locationId -> growingLocationDao.getLocation(locationId) }
        if (configuredDefault != null) {
            return configuredDefault
        }
        if (existingLocations.isNotEmpty()) {
            return existingLocations.first()
        }
        val fallbackName = settings.forecastLocationProfile().normalizedName(
            fallback = settings.orchardName.trim().ifBlank { "Growing location" }
        )
        val created = GrowingLocationEntity(
            id = UUID.randomUUID().toString(),
            name = fallbackName,
            countryCode = settings.countryCode,
            timezoneId = settings.timezoneId,
            hemisphere = settings.hemisphere,
            latitudeDeg = settings.latitudeDeg,
            longitudeDeg = settings.longitudeDeg,
            elevationM = settings.elevationM,
            usdaZoneCode = settings.usdaZone.takeIf(String::isNotBlank),
            chillHoursBand = settings.chillHoursBand,
            microclimateFlags = settings.microclimateFlags,
            climateSource = null,
            climateFetchedAt = null,
            climateMeanMonthlyTempC = emptyList(),
            climateMeanMonthlyMinTempC = emptyList(),
            climateMeanMonthlyMaxTempC = emptyList(),
            notes = "",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        growingLocationDao.insert(created)
        settingsRepository.updateDefaultLocationId(created.id)
        return created
    }

    private suspend fun resolveTreeLocation(
        locationId: String?,
        existingLocationId: String?,
        fallbackLocation: GrowingLocationEntity?
    ): GrowingLocationEntity? {
        val requestedId = locationId?.takeIf(String::isNotBlank) ?: existingLocationId
        return requestedId?.let { requestedLocationId ->
            growingLocationDao.getLocation(requestedLocationId)
        } ?: fallbackLocation
    }

    private suspend fun fetchLocationClimate(
        latitudeDeg: Double,
        longitudeDeg: Double
    ): LocationClimateFingerprint? = runCatching {
        climateFingerprintService.fetch(latitudeDeg, longitudeDeg)
    }.getOrNull()

    private suspend fun countMatchingTrees(species: String, cultivar: String): Int = treeDao.getAllTrees().count {
        it.species.normalized() == species.normalized() &&
            it.cultivar.normalized() == cultivar.normalized()
    }

    private fun plannedTreeNicknames(
        inputNickname: String,
        quantity: Int,
        existingDuplicateCount: Int
    ): List<String?> {
        val trimmedNickname = inputNickname.trim()
        val shouldAutoNumber = quantity > 1 || (existingDuplicateCount > 0 && trimmedNickname.isBlank())
        if (!shouldAutoNumber) {
            return List(quantity) { trimmedNickname.takeIf(String::isNotBlank) }
        }
        val startingOrdinal = existingDuplicateCount + 1
        return List(quantity) { index ->
            val ordinal = startingOrdinal + index
            if (trimmedNickname.isNotBlank()) {
                "$trimmedNickname $ordinal"
            } else {
                "Plant $ordinal"
            }
        }
    }

    private suspend fun markWishlistAcquired(species: String, cultivar: String, treeId: String) {
        if (cultivar.isBlank()) return
        val match = wishlistDao.findBySpeciesAndCultivar(species, cultivar) ?: return
        wishlistDao.insert(match.copy(acquired = true, linkedTreeId = treeId))
    }

    private suspend fun ensureHeroPhotoSelected(treeId: String) {
        val photos = treePhotoDao.getPhotosForTree(treeId)
        if (photos.isEmpty() || photos.any(TreePhotoEntity::isHero)) return
        photos.heroOrLatestPhoto()?.let { heroPhoto ->
            treePhotoDao.setHeroPhoto(treeId, heroPhoto.id)
        }
    }

    private suspend fun clearAllDataInternal() {
        reminderDao.getActiveReminders().forEach { reminderScheduler.cancel(it.id) }
        database.withTransaction {
            activityPhotoDao.clearAll()
            eventDao.clearAll()
            harvestDao.clearAll()
            reminderDao.clearAll()
            treePhotoDao.clearAll()
            treeDao.clearAll()
            growingLocationDao.clearAll()
            wishlistDao.clearAll()
        }
        photoStorage.clearAll()
    }

    private fun buildHistoryEntries(
        trees: List<TreeEntity>,
        events: List<EventEntity>,
        harvests: List<HarvestEntity>,
        activityPhotos: List<ActivityPhotoEntity>
    ): List<HistoryEntryModel> {
        val treesById = trees.associateBy(TreeEntity::id)
        val photosByOwner = activityPhotos.groupBy { it.ownerKind to it.ownerId }
        return (
            events.map { event ->
                val tree = treesById[event.treeId]
                val photoPaths = photosByOwner[EVENT_OWNER_KIND to event.id]
                    .orEmpty()
                    .sortedBy(ActivityPhotoEntity::sortOrder)
                    .map(ActivityPhotoEntity::relativePath)
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
                    photoPath = photoPaths.firstOrNull() ?: event.photoPath,
                    photoPaths = photoPaths.ifEmpty { listOfNotNull(event.photoPath) }
                )
            } +
                harvests.map { harvest ->
                    val tree = treesById[harvest.treeId]
                    val photoPaths = photosByOwner[HARVEST_OWNER_KIND to harvest.id]
                        .orEmpty()
                        .sortedBy(ActivityPhotoEntity::sortOrder)
                        .map(ActivityPhotoEntity::relativePath)
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
                        verified = harvest.verified,
                        photoPath = photoPaths.firstOrNull() ?: harvest.photoPath,
                        photoPaths = photoPaths.ifEmpty { listOfNotNull(harvest.photoPath) }
                    )
                }
            ).sortedWith(
            compareByDescending<HistoryEntryModel> { it.date }
                .thenByDescending { it.createdAt }
        )
    }
}

private fun GrowingLocationEntity.withClimate(climate: LocationClimateFingerprint): GrowingLocationEntity = copy(
    climateSource = climate.source.ifBlank { climateSource },
    climateFetchedAt = climate.fetchedAt,
    climateMeanMonthlyTempC = climate.meanMonthlyTempC,
    climateMeanMonthlyMinTempC = climate.meanMonthlyMinTempC,
    climateMeanMonthlyMaxTempC = climate.meanMonthlyMaxTempC
)

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
    "Awaiting verification".takeIf { !harvest.verified },
    harvest.notes.takeIf(String::isNotBlank)
).joinToString(" - ")

private fun fruitingTreeIds(trees: List<TreeEntity>, harvests: List<HarvestEntity>): Set<String> =
    harvests.map(HarvestEntity::treeId).toSet() + trees.filter(TreeEntity::hasFruitedBefore).map(TreeEntity::id)

private fun List<TreePhotoEntity>.heroOrLatestPhoto(): TreePhotoEntity? =
    firstOrNull(TreePhotoEntity::isHero)
        ?: maxWithOrNull(compareBy<TreePhotoEntity> { it.createdAt }.thenBy { it.sortOrder })

private fun EventType.displayLabel(): String = when (this) {
    EventType.PLANTED -> "Planted"
    EventType.REPOTTED -> "Repotted"
    EventType.PRUNED -> "Pruned"
    EventType.FERTILIZED -> "Fertilized"
    EventType.SPRAYED -> "Sprayed"
    EventType.BUD -> "Bud"
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
        normalized.contains("bud") -> EventType.BUD
        normalized.contains("bloom") -> EventType.BLOOM
        normalized.contains("water") || normalized.contains("moisture") -> EventType.WATERED
        else -> EventType.NOTE
    }
}

private fun ReminderEntity.nextDueAt(): Long? {
    val zone = OrchardTime.zoneId()
    val current = Instant.ofEpochMilli(dueAt).atZone(zone)
    return when (recurrenceType) {
        RecurrenceType.NONE -> null
        RecurrenceType.DAILY -> current.plusDays(1).toInstant().toEpochMilli()
        RecurrenceType.WEEKLY -> current.plusWeeks(1).toInstant().toEpochMilli()
        RecurrenceType.MONTHLY -> current.plusMonths(1).toInstant().toEpochMilli()
        RecurrenceType.EVERY_X_DAYS -> current.plusDays((recurrenceIntervalDays ?: 1).toLong()).toInstant().toEpochMilli()
    }
}
