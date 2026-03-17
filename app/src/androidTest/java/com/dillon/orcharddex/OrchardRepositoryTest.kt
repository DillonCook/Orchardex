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
}
