package com.dillon.orcharddex

import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dillon.orcharddex.backup.BackupManager
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.model.FrostSensitivityLevel
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

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, OrchardDexDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupManager = BackupManager(
            context = context,
            database = database,
            settingsRepository = SettingsRepository(context),
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

        val file = File(context.cacheDir, "backup-test.orcharddex.zip").apply {
            delete()
            createNewFile()
        }

        backupManager.exportTo(file.toUri())
        backupManager.clearAllData()

        assertThat(database.treeDao().getAllTrees()).isEmpty()

        backupManager.importReplaceAll(file.toUri())

        assertThat(database.treeDao().getAllTrees()).hasSize(1)
        assertThat(database.treeDao().getAllTrees().single().cultivar).isEqualTo("Ruby Supreme")
    }
}
