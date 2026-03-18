package com.dillon.orcharddex

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.model.TreeInput
import com.dillon.orcharddex.data.model.WishlistPriority
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.repository.OrchardRepository
import com.dillon.orcharddex.data.repository.PhotoStorage
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.dillon.orcharddex.sample.SampleDataSeeder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchardRepositoryTest {
    private lateinit var database: OrchardDexDatabase
    private lateinit var repository: OrchardRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, OrchardDexDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OrchardRepository(
            database = database,
            settingsRepository = SettingsRepository(context),
            photoStorage = PhotoStorage(context),
            reminderScheduler = ReminderScheduler(context),
            sampleDataSeeder = SampleDataSeeder()
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
}
