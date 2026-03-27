package com.dillon.orcharddex

import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dillon.orcharddex.data.local.ActivityPhotoEntity
import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.backup.BackupManager
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.ChillHoursBand
import com.dillon.orcharddex.data.model.EventType
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.repository.PhotoStorage
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupManagerTest {
    private lateinit var context: android.content.Context
    private lateinit var database: OrchardDexDatabase
    private lateinit var backupManager: BackupManager
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, OrchardDexDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsRepository = SettingsRepository(context)
        backupManager = BackupManager(
            context = context,
            database = database,
            settingsRepository = settingsRepository,
            photoStorage = PhotoStorage(context),
            reminderScheduler = ReminderScheduler(context)
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportThenImport_restoresStructuredData() = runBlocking {
        database.growingLocationDao().insert(
            GrowingLocationEntity(
                id = "location-1",
                name = "Home orchard",
                countryCode = "US",
                timezoneId = "America/New_York",
                hemisphere = Hemisphere.NORTHERN,
                latitudeDeg = 26.1,
                longitudeDeg = null,
                elevationM = 5.0,
                usdaZoneCode = "10b",
                chillHoursBand = ChillHoursBand.UNKNOWN,
                microclimateFlags = emptySet(),
                notes = "",
                createdAt = 1L,
                updatedAt = 1L
            )
        )
        settingsRepository.updateDefaultLocationId("location-1")
        database.treeDao().insert(
            TreeEntity(
                id = "tree-1",
                locationId = "location-1",
                orchardName = "Home",
                sectionName = "South row",
                nickname = null,
                species = "Guava",
                cultivar = "Ruby Supreme",
                rootstock = null,
                source = null,
                purchaseDate = null,
                plantedDate = 1_700_000_000_000,
                plantType = PlantType.IN_GROUND,
                containerSize = null,
                sunExposure = "Full sun",
                frostSensitivity = FrostSensitivityLevel.MEDIUM,
                frostSensitivityNote = null,
                irrigationNote = null,
                status = TreeStatus.ACTIVE,
                notes = "",
                tags = "",
                createdAt = 1L,
                updatedAt = 1L
            )
        )

        val file = File(context.cacheDir, "backup-test.orcharddex.zip").apply {
            delete()
            createNewFile()
        }

        backupManager.exportTo(file.toUri())
        val validation = backupManager.validateImport(file.toUri())
        backupManager.clearAllData()

        assertThat(database.treeDao().getAllTrees()).isEmpty()
        assertThat(validation.locationCount).isEqualTo(1)

        backupManager.importReplaceAll(file.toUri())

        assertThat(database.treeDao().getAllTrees()).hasSize(1)
        assertThat(database.growingLocationDao().getAllLocations()).hasSize(1)
        assertThat(database.treeDao().getAllTrees().single().cultivar).isEqualTo("Ruby Supreme")
        assertThat(database.treeDao().getAllTrees().single().locationId).isEqualTo("location-1")
    }

    @Test
    fun exportThenImport_restoresActivityPhotoAttachments() = runBlocking {
        database.treeDao().insert(
            TreeEntity(
                id = "tree-1",
                orchardName = "Home",
                sectionName = "South row",
                nickname = null,
                species = "Guava",
                cultivar = "Ruby Supreme",
                rootstock = null,
                source = null,
                purchaseDate = null,
                plantedDate = 1_700_000_000_000,
                plantType = PlantType.IN_GROUND,
                containerSize = null,
                sunExposure = "Full sun",
                frostSensitivity = FrostSensitivityLevel.MEDIUM,
                frostSensitivityNote = null,
                irrigationNote = null,
                status = TreeStatus.ACTIVE,
                notes = "",
                tags = "",
                createdAt = 1L,
                updatedAt = 1L
            )
        )
        database.eventDao().insert(
            EventEntity(
                id = "event-1",
                treeId = "tree-1",
                eventType = EventType.BLOOM,
                eventDate = 1_710_000_000_000,
                notes = "Peak bloom",
                cost = null,
                quantityValue = null,
                quantityUnit = null,
                photoPath = "events/cover.jpg",
                createdAt = 1_710_000_000_000
            )
        )
        database.harvestDao().insert(
            HarvestEntity(
                id = "harvest-1",
                treeId = "tree-1",
                harvestDate = 1_720_000_000_000,
                quantityValue = 12.0,
                quantityUnit = "fruit",
                qualityRating = 4,
                firstFruit = false,
                verified = true,
                notes = "Good crop",
                photoPath = "harvests/cover.jpg",
                createdAt = 1_720_000_000_000
            )
        )
        database.activityPhotoDao().insertAll(
            listOf(
                ActivityPhotoEntity(
                    id = "activity-1",
                    ownerKind = "EVENT",
                    ownerId = "event-1",
                    relativePath = "events/cover.jpg",
                    caption = null,
                    createdAt = 1_710_000_000_000,
                    sortOrder = 0
                ),
                ActivityPhotoEntity(
                    id = "activity-2",
                    ownerKind = "EVENT",
                    ownerId = "event-1",
                    relativePath = "events/detail.jpg",
                    caption = null,
                    createdAt = 1_710_000_000_001,
                    sortOrder = 1
                ),
                ActivityPhotoEntity(
                    id = "activity-3",
                    ownerKind = "HARVEST",
                    ownerId = "harvest-1",
                    relativePath = "harvests/cover.jpg",
                    caption = null,
                    createdAt = 1_720_000_000_000,
                    sortOrder = 0
                )
            )
        )

        val file = File(context.cacheDir, "backup-activity-photo-test.orcharddex.zip").apply {
            delete()
            createNewFile()
        }

        backupManager.exportTo(file.toUri())
        backupManager.clearAllData()
        backupManager.importReplaceAll(file.toUri())

        val restoredPhotos = database.activityPhotoDao().getAllPhotos()

        assertThat(restoredPhotos).hasSize(3)
        assertThat(restoredPhotos.map { it.ownerKind }).containsExactly("EVENT", "EVENT", "HARVEST")
        assertThat(restoredPhotos.filter { it.ownerId == "event-1" }.map { it.sortOrder }).containsExactly(0, 1)
    }
}
