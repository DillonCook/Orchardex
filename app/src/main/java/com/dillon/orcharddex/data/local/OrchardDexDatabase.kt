package com.dillon.orcharddex.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GrowingLocationEntity::class,
        TreeEntity::class,
        TreePhotoEntity::class,
        ActivityPhotoEntity::class,
        EventEntity::class,
        HarvestEntity::class,
        ReminderEntity::class,
        WishlistCultivarEntity::class
    ],
    version = OrchardDexDatabase.DB_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OrchardDexDatabase : RoomDatabase() {
    abstract fun growingLocationDao(): GrowingLocationDao
    abstract fun treeDao(): TreeDao
    abstract fun treePhotoDao(): TreePhotoDao
    abstract fun activityPhotoDao(): ActivityPhotoDao
    abstract fun eventDao(): EventDao
    abstract fun harvestDao(): HarvestDao
    abstract fun reminderDao(): ReminderDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        const val DB_VERSION = 8
        const val DB_NAME = "orcharddex.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN hasFruitedBefore INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE harvests
                    ADD COLUMN verified INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN bloomTimingMode TEXT NOT NULL DEFAULT 'AUTO'
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN customBloomStartMonth INTEGER
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN customBloomStartDay INTEGER
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN customBloomDurationDays INTEGER
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE tree_photos
                    ADD COLUMN isHero INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    UPDATE tree_photos
                    SET isHero = CASE
                        WHEN id = (
                            SELECT candidate.id
                            FROM tree_photos AS candidate
                            WHERE candidate.treeId = tree_photos.treeId
                            ORDER BY candidate.createdAt DESC, candidate.sortOrder DESC
                            LIMIT 1
                        ) THEN 1
                        ELSE 0
                    END
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN selfCompatibilityOverride TEXT
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN pollinationModeOverride TEXT
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE trees
                    ADD COLUMN pollinationOverrideNote TEXT
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `growing_locations` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `countryCode` TEXT NOT NULL,
                        `timezoneId` TEXT NOT NULL,
                        `hemisphere` TEXT NOT NULL,
                        `latitudeDeg` REAL,
                        `longitudeDeg` REAL,
                        `elevationM` REAL,
                        `usdaZoneCode` TEXT,
                        `chillHoursBand` TEXT NOT NULL,
                        `microclimateFlags` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_growing_locations_name` ON `growing_locations` (`name`)")
                database.execSQL("PRAGMA foreign_keys=OFF")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trees_new` (
                        `id` TEXT NOT NULL,
                        `locationId` TEXT,
                        `orchardName` TEXT NOT NULL,
                        `sectionName` TEXT NOT NULL,
                        `nickname` TEXT,
                        `species` TEXT NOT NULL,
                        `cultivar` TEXT NOT NULL,
                        `rootstock` TEXT,
                        `source` TEXT,
                        `purchaseDate` INTEGER,
                        `plantedDate` INTEGER NOT NULL,
                        `plantType` TEXT NOT NULL,
                        `containerSize` TEXT,
                        `sunExposure` TEXT,
                        `frostSensitivity` TEXT NOT NULL,
                        `frostSensitivityNote` TEXT,
                        `irrigationNote` TEXT,
                        `status` TEXT NOT NULL,
                        `hasFruitedBefore` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `bloomTimingMode` TEXT NOT NULL,
                        `customBloomStartMonth` INTEGER,
                        `customBloomStartDay` INTEGER,
                        `customBloomDurationDays` INTEGER,
                        `selfCompatibilityOverride` TEXT,
                        `pollinationModeOverride` TEXT,
                        `pollinationOverrideNote` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`locationId`) REFERENCES `growing_locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `trees_new` (
                        `id`, `locationId`, `orchardName`, `sectionName`, `nickname`, `species`, `cultivar`,
                        `rootstock`, `source`, `purchaseDate`, `plantedDate`, `plantType`, `containerSize`,
                        `sunExposure`, `frostSensitivity`, `frostSensitivityNote`, `irrigationNote`, `status`,
                        `hasFruitedBefore`, `notes`, `tags`, `bloomTimingMode`, `customBloomStartMonth`,
                        `customBloomStartDay`, `customBloomDurationDays`, `selfCompatibilityOverride`,
                        `pollinationModeOverride`, `pollinationOverrideNote`, `createdAt`, `updatedAt`
                    )
                    SELECT
                        `id`, NULL, `orchardName`, `sectionName`, `nickname`, `species`, `cultivar`,
                        `rootstock`, `source`, `purchaseDate`, `plantedDate`, `plantType`, `containerSize`,
                        `sunExposure`, `frostSensitivity`, `frostSensitivityNote`, `irrigationNote`, `status`,
                        `hasFruitedBefore`, `notes`, `tags`, `bloomTimingMode`, `customBloomStartMonth`,
                        `customBloomStartDay`, `customBloomDurationDays`, `selfCompatibilityOverride`,
                        `pollinationModeOverride`, `pollinationOverrideNote`, `createdAt`, `updatedAt`
                    FROM `trees`
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE `trees`")
                database.execSQL("ALTER TABLE `trees_new` RENAME TO `trees`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_trees_species` ON `trees` (`species`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_trees_cultivar` ON `trees` (`cultivar`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_trees_orchardName` ON `trees` (`orchardName`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_trees_status` ON `trees` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_trees_locationId` ON `trees` (`locationId`)")
                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `activity_photos` (
                        `id` TEXT NOT NULL,
                        `ownerKind` TEXT NOT NULL,
                        `ownerId` TEXT NOT NULL,
                        `relativePath` TEXT NOT NULL,
                        `caption` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_activity_photos_ownerKind_ownerId`
                    ON `activity_photos` (`ownerKind`, `ownerId`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_activity_photos_relativePath`
                    ON `activity_photos` (`relativePath`)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `activity_photos` (`id`, `ownerKind`, `ownerId`, `relativePath`, `caption`, `createdAt`, `sortOrder`)
                    SELECT 'event:' || `id`, 'EVENT', `id`, `photoPath`, NULL, `createdAt`, 0
                    FROM `events`
                    WHERE `photoPath` IS NOT NULL AND TRIM(`photoPath`) != ''
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `activity_photos` (`id`, `ownerKind`, `ownerId`, `relativePath`, `caption`, `createdAt`, `sortOrder`)
                    SELECT 'harvest:' || `id`, 'HARVEST', `id`, `photoPath`, NULL, `createdAt`, 0
                    FROM `harvests`
                    WHERE `photoPath` IS NOT NULL AND TRIM(`photoPath`) != ''
                    """.trimIndent()
                )
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )
    }
}
