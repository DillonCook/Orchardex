package com.dillon.orcharddex

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val testDbName = "orcharddex-migration-test"
    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OrchardDexDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    fun tearDown() {
        context.deleteDatabase(testDbName)
    }

    @Test
    fun migrate10To11_preservesTreesLogsPhotosAndNotes() = runBlocking {
        helper.createDatabase(testDbName, 10).apply {
            execSQL(
                """
                INSERT INTO growing_locations (
                    id, name, countryCode, timezoneId, hemisphere, latitudeDeg, longitudeDeg,
                    elevationM, usdaZoneCode, chillHoursBand, microclimateFlags, climateSource,
                    climateFetchedAt, climateMeanMonthlyTempC, climateMeanMonthlyMinTempC,
                    climateMeanMonthlyMaxTempC, notes, createdAt, updatedAt
                ) VALUES (
                    'location-1', 'Home orchard', 'US', 'America/New_York', 'NORTHERN',
                    26.1, -80.2, 5.0, '10b', 'UNKNOWN', '', NULL, NULL, '', '', '',
                    'Location notes', 1, 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO trees (
                    id, locationId, orchardName, sectionName, nickname, species, cultivar, rootstock,
                    source, purchaseDate, plantedDate, plantType, containerSize, sunExposure,
                    frostSensitivity, frostSensitivityNote, irrigationNote, status, hasFruitedBefore,
                    notes, tags, bloomTimingMode, bloomPatternOverride, manualBloomProfile,
                    alternateYearAnchor, customBloomStartMonth, customBloomStartDay,
                    customBloomDurationDays, selfCompatibilityOverride, pollinationModeOverride,
                    pollinationOverrideNote, nurseryStage, parentTreeId, originType,
                    propagationMethod, propagationDate, createdAt, updatedAt
                ) VALUES (
                    'tree-1', 'location-1', 'Home orchard', 'South row', 'Favorite', 'Mango',
                    'Sweet Tart', 'Turpentine', 'Local nursery', NULL, 1700000000000, 'IN_GROUND',
                    NULL, 'Full sun', 'MEDIUM', NULL, NULL, 'ACTIVE', 1, 'Tree notes', 'tag-1',
                    'AUTO', NULL, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'NONE',
                    NULL, 'PURCHASED', NULL, NULL, 1, 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO tree_photos (id, treeId, relativePath, caption, createdAt, sortOrder, isHero)
                VALUES ('tree-photo-1', 'tree-1', 'trees/hero.jpg', 'Hero', 1700000000001, 0, 1)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO events (
                    id, treeId, eventType, eventDate, notes, cost, quantityValue, quantityUnit, photoPath, createdAt
                ) VALUES (
                    'event-1', 'tree-1', 'BLOOM', 1710000000000, 'Event notes', 4.5, 2.0, 'count',
                    'events/bloom.jpg', 1710000000000
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO activity_photos (id, ownerKind, ownerId, relativePath, caption, createdAt, sortOrder)
                VALUES ('activity-photo-1', 'EVENT', 'event-1', 'events/bloom.jpg', 'Bloom photo', 1710000000001, 0)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO harvests (
                    id, treeId, harvestDate, quantityValue, quantityUnit, qualityRating, firstFruit,
                    verified, notes, photoPath, createdAt
                ) VALUES (
                    'harvest-1', 'tree-1', 1720000000000, 12.0, 'fruit', 4, 1, 1, 'Harvest notes',
                    'harvests/fruit.jpg', 1720000000000
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO reminders (
                    id, treeId, title, notes, dueAt, hasTime, recurrenceType, recurrenceIntervalDays,
                    enabled, completedAt, leadTimeMode, customLeadTimeHours, createdAt, updatedAt
                ) VALUES (
                    'reminder-1', 'tree-1', 'Feed tree', 'Reminder notes', 1730000000000, 0,
                    'NONE', NULL, 1, NULL, 'SAME_DAY', NULL, 1730000000000, 1730000000000
                )
                """.trimIndent()
            )
            close()
        }

        val migrated = Room.databaseBuilder(context, OrchardDexDatabase::class.java, testDbName)
            .addMigrations(*OrchardDexDatabase.ALL_MIGRATIONS)
            .build()

        migrated.openHelper.writableDatabase.close()

        val trees = migrated.treeDao().getAllTrees()
        val treePhotos = migrated.treePhotoDao().getAllPhotos()
        val events = migrated.eventDao().getAllEvents()
        val harvests = migrated.harvestDao().getAllHarvests()
        val reminders = migrated.reminderDao().getAllReminders()
        val activityPhotos = migrated.activityPhotoDao().getAllPhotos()
        val sales = migrated.saleDao().getAllSales()

        assertThat(trees).hasSize(1)
        assertThat(trees.single().species).isEqualTo("Mango")
        assertThat(trees.single().cultivar).isEqualTo("Sweet Tart")
        assertThat(trees.single().notes).isEqualTo("Tree notes")
        assertThat(treePhotos).hasSize(1)
        assertThat(treePhotos.single().relativePath).isEqualTo("trees/hero.jpg")
        assertThat(events).hasSize(1)
        assertThat(events.single().notes).isEqualTo("Event notes")
        assertThat(events.single().photoPath).isEqualTo("events/bloom.jpg")
        assertThat(harvests).hasSize(1)
        assertThat(harvests.single().notes).isEqualTo("Harvest notes")
        assertThat(harvests.single().photoPath).isEqualTo("harvests/fruit.jpg")
        assertThat(reminders).hasSize(1)
        assertThat(reminders.single().notes).isEqualTo("Reminder notes")
        assertThat(activityPhotos).hasSize(1)
        assertThat(activityPhotos.single().relativePath).isEqualTo("events/bloom.jpg")
        assertThat(sales).isEmpty()

        migrated.close()
    }
}
