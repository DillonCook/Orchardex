package com.dillon.orcharddex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TreeEntity::class,
        TreePhotoEntity::class,
        EventEntity::class,
        HarvestEntity::class,
        ReminderEntity::class,
        WishlistCultivarEntity::class
    ],
    version = 1,
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
        const val DB_NAME = "orcharddex.db"
    }
}
