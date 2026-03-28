package com.dillon.orcharddex

import android.content.Context
import androidx.room.Room
import com.dillon.orcharddex.backup.BackupManager
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.remote.NasaPowerClimateService
import com.dillon.orcharddex.data.remote.OpenMeteoLocationSearchService
import com.dillon.orcharddex.data.repository.OrchardRepository
import com.dillon.orcharddex.data.repository.PhotoStorage
import com.dillon.orcharddex.notifications.ReminderScheduler
import com.dillon.orcharddex.sample.SampleDataSeeder

class OrchardDexContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: OrchardDexDatabase = Room.databaseBuilder(
        appContext,
        OrchardDexDatabase::class.java,
        OrchardDexDatabase.DB_NAME
    )
        // Fail fast on unsupported schemas rather than silently deleting orchard data.
        .addMigrations(*OrchardDexDatabase.ALL_MIGRATIONS)
        .build()

    val settingsRepository = SettingsRepository(appContext)
    val photoStorage = PhotoStorage(appContext)
    val reminderScheduler = ReminderScheduler(appContext)
    val sampleDataSeeder = SampleDataSeeder()
    val locationSearchService = OpenMeteoLocationSearchService()
    val climateFingerprintService = NasaPowerClimateService()
    val repository = OrchardRepository(
        database = database,
        settingsRepository = settingsRepository,
        photoStorage = photoStorage,
        reminderScheduler = reminderScheduler,
        sampleDataSeeder = sampleDataSeeder,
        locationSearchService = locationSearchService,
        climateFingerprintService = climateFingerprintService
    )
    val backupManager = BackupManager(
        context = appContext,
        database = database,
        settingsRepository = settingsRepository,
        photoStorage = photoStorage,
        reminderScheduler = reminderScheduler
    )
}
