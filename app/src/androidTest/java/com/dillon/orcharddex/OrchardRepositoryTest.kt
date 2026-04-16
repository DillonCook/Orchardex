package com.dillon.orcharddex

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.model.ForecastLocationProfile
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
import com.dillon.orcharddex.data.model.Hemisphere
import com.dillon.orcharddex.data.model.LocationClimateFingerprint
import com.dillon.orcharddex.data.model.LocationSearchResult
import com.dillon.orcharddex.data.model.PlantType
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.TreeStatus
import com.dillon.orcharddex.data.model.WishlistPriority
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.remote.ClimateFingerprintService
import com.dillon.orcharddex.data.remote.LocationSearchService
import com.dillon.orcharddex.data.repository.OrchardRepository
import com.dillon.orcharddex.data.repository.PhotoStorage
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.dillon.orcharddex.sample.SampleDataSeeder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchardRepositoryTest {
    private lateinit var database: OrchardDexDatabase
    private lateinit var repository: OrchardRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, OrchardDexDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsRepository = SettingsRepository(context)
        repository = OrchardRepository(
            database = database,
            settingsRepository = settingsRepository,
            photoStorage = PhotoStorage(context),
            reminderScheduler = ReminderScheduler(context),
            sampleDataSeeder = SampleDataSeeder(),
            locationSearchService = object : LocationSearchService {
                override suspend fun search(query: String): List<LocationSearchResult> = emptyList()
            },
            climateFingerprintService = object : ClimateFingerprintService {
                override suspend fun fetch(latitudeDeg: Double, longitudeDeg: Double): LocationClimateFingerprint? = null
            }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveTree_marksMatchingWishlistCultivarAcquired() = runBlocking {
        database.wishlistDao().insert(
            WishlistCultivarEntity(
                id = "wish-1",
                species = "Mango",
                cultivar = "Sweet Tart",
                priority = WishlistPriority.HIGH,
                notes = "",
                acquired = false,
                linkedTreeId = null,
                createdAt = 1L
            )
        )

        val savedTreeId = repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Fence row",
                species = "Mango",
                cultivar = "Sweet Tart",
                plantedDate = 1_700_000_000_000
            )
        )

        val savedWishlist = database.wishlistDao().getAll().single()
        assertThat(savedWishlist.acquired).isTrue()
        assertThat(savedWishlist.linkedTreeId).isEqualTo(savedTreeId)
    }

    @Test
    fun saveTree_withQuantity_createsMultiplePlantRecords() = runBlocking {
        repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Block A",
                species = "Grapefruit",
                cultivar = "Ruby Red",
                plantedDate = 1_700_000_000_000,
                quantity = 3
            )
        )

        val savedTrees = database.treeDao().getAllTrees()
        assertThat(savedTrees).hasSize(3)
        assertThat(savedTrees.map { it.species }.distinct()).containsExactly("Grapefruit")
        assertThat(savedTrees.map { it.cultivar }.distinct()).containsExactly("Ruby Red")
        assertThat(savedTrees.map { it.sectionName }.distinct()).containsExactly("Block A")
    }

    @Test
    fun saveTree_withQuantity_autoNumbersDuplicateNicknames() = runBlocking {
        repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Block A",
                species = "Grapefruit",
                cultivar = "Ruby Red",
                plantedDate = 1_700_000_000_000,
                quantity = 3
            )
        )

        val nicknames = database.treeDao().getAllTrees().mapNotNull { it.nickname }.sorted()
        assertThat(nicknames).containsExactly("Plant 1", "Plant 2", "Plant 3").inOrder()
    }

    @Test
    fun saveTree_newDuplicateWithoutNickname_usesNextAutoNumber() = runBlocking {
        repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Block A",
                species = "Grapefruit",
                cultivar = "Ruby Red",
                plantedDate = 1_700_000_000_000
            )
        )

        repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Block A",
                species = "Grapefruit",
                cultivar = "Ruby Red",
                plantedDate = 1_700_000_000_000
            )
        )

        val savedTrees = database.treeDao().getAllTrees()
        assertThat(savedTrees.map { it.nickname }).contains("Plant 2")
    }

    @Test
    fun saveTree_updatingExistingTree_preservesExistingPhotos() = runBlocking {
        val savedTreeId = repository.saveTree(
            TreeInput(
                orchardName = "Home",
                sectionName = "Block A",
                species = "Mango",
                cultivar = "Sweet Tart",
                plantedDate = 1_700_000_000_000
            )
        )
        database.treePhotoDao().insert(
            TreePhotoEntity(
                id = "tree-photo-1",
                treeId = savedTreeId,
                relativePath = "trees/existing.jpg",
                caption = "Existing photo",
                createdAt = 1_700_000_000_001,
                sortOrder = 0,
                isHero = true
            )
        )

        repository.saveTree(
            TreeInput(
                id = savedTreeId,
                orchardName = "Home",
                sectionName = "Updated section",
                species = "Mango",
                cultivar = "Sweet Tart",
                plantedDate = 1_700_000_000_000
            )
        )

        val treePhotos = database.treePhotoDao().getPhotosForTree(savedTreeId)
        val savedTree = database.treeDao().getTree(savedTreeId)

        assertThat(savedTree).isNotNull()
        assertThat(savedTree?.sectionName).isEqualTo("Updated section")
        assertThat(treePhotos).hasSize(1)
        assertThat(treePhotos.single().relativePath).isEqualTo("trees/existing.jpg")
        assertThat(treePhotos.single().isHero).isTrue()
    }

    @Test
    fun ensureGrowingLocations_backfillsDefaultLocationAndAssignsLegacyTrees() = runBlocking {
        settingsRepository.completeOnboarding(
            orchardName = "Home orchard",
            locationProfile = ForecastLocationProfile(
                name = "Home orchard",
                countryCode = "US",
                timezoneId = "America/Los_Angeles",
                hemisphere = Hemisphere.NORTHERN,
                usdaZoneCode = "10a"
            ),
            orchardRegion = ""
        )
        database.treeDao().insert(
            TreeEntity(
                id = "legacy-tree",
                orchardName = "Legacy orchard",
                sectionName = "South row",
                nickname = null,
                species = "Mango",
                cultivar = "Carrie",
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

        repository.ensureGrowingLocations(settingsRepository.settings.first())

        val location = database.growingLocationDao().getAllLocations().single()
        val savedTree = database.treeDao().getAllTrees().single()

        assertThat(location.name).isEqualTo("Home orchard")
        assertThat(savedTree.locationId).isEqualTo(location.id)
        assertThat(savedTree.orchardName).isEqualTo("Legacy orchard")
    }
}
