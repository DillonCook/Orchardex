package com.dillon.orcharddex.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TreeEntity::class,
        TreePhotoEntity::class,
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
    abstract fun treeDao(): TreeDao
    abstract fun treePhotoDao(): TreePhotoDao
    abstract fun eventDao(): EventDao
    abstract fun harvestDao(): HarvestDao
    abstract fun reminderDao(): ReminderDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        const val DB_VERSION = 4
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
    }
}
