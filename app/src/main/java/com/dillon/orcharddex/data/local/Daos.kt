package com.dillon.orcharddex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tree: TreeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trees: List<TreeEntity>)

    @Query("DELETE FROM trees WHERE id = :treeId")
    suspend fun delete(treeId: String)

    @Query("SELECT * FROM trees WHERE id = :treeId")
    suspend fun getTree(treeId: String): TreeEntity?

    @Query("SELECT * FROM trees ORDER BY updatedAt DESC")
    fun observeTrees(): Flow<List<TreeEntity>>

    @Query("SELECT * FROM trees ORDER BY updatedAt DESC")
    suspend fun getAllTrees(): List<TreeEntity>

    @Transaction
    @Query("SELECT * FROM trees ORDER BY updatedAt DESC")
    fun observeTreesWithPhotos(): Flow<List<TreeWithPhotos>>

    @Transaction
    @Query("SELECT * FROM trees WHERE id = :treeId")
    fun observeTreeWithPhotos(treeId: String): Flow<TreeWithPhotos?>

    @Transaction
    @Query("SELECT * FROM trees WHERE id = :treeId")
    suspend fun getTreeWithPhotos(treeId: String): TreeWithPhotos?

    @Query("SELECT DISTINCT orchardName FROM trees WHERE orchardName != '' ORDER BY orchardName")
    fun observeOrchardNames(): Flow<List<String>>

    @Query("SELECT DISTINCT species FROM trees ORDER BY species")
    fun observeSpeciesNames(): Flow<List<String>>

    @Query("SELECT DISTINCT cultivar FROM trees ORDER BY cultivar")
    fun observeCultivarNames(): Flow<List<String>>

    @Query("DELETE FROM trees")
    suspend fun clearAll()
}

@Dao
interface TreePhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: TreePhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<TreePhotoEntity>)

    @Query("SELECT * FROM tree_photos WHERE treeId = :treeId ORDER BY sortOrder, createdAt")
    suspend fun getPhotosForTree(treeId: String): List<TreePhotoEntity>

    @Query("SELECT * FROM tree_photos ORDER BY createdAt")
    suspend fun getAllPhotos(): List<TreePhotoEntity>

    @Query("SELECT * FROM tree_photos WHERE id IN (:photoIds)")
    suspend fun getPhotosByIds(photoIds: List<String>): List<TreePhotoEntity>

    @Query("DELETE FROM tree_photos WHERE id IN (:photoIds)")
    suspend fun deleteByIds(photoIds: List<String>)

    @Query("DELETE FROM tree_photos")
    suspend fun clearAll()
}

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events WHERE treeId = :treeId ORDER BY eventDate DESC, createdAt DESC")
    fun observeEventsForTree(treeId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY eventDate DESC, createdAt DESC")
    fun observeAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY eventDate DESC, createdAt DESC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("DELETE FROM events")
    suspend fun clearAll()
}

@Dao
interface HarvestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(harvest: HarvestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(harvests: List<HarvestEntity>)

    @Query("SELECT * FROM harvests WHERE treeId = :treeId ORDER BY harvestDate DESC, createdAt DESC")
    fun observeHarvestsForTree(treeId: String): Flow<List<HarvestEntity>>

    @Query("SELECT * FROM harvests ORDER BY harvestDate DESC, createdAt DESC")
    fun observeAllHarvests(): Flow<List<HarvestEntity>>

    @Query("SELECT * FROM harvests ORDER BY harvestDate DESC, createdAt DESC")
    suspend fun getAllHarvests(): List<HarvestEntity>

    @Query("DELETE FROM harvests")
    suspend fun clearAll()
}

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminder(reminderId: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE treeId = :treeId ORDER BY dueAt ASC")
    fun observeRemindersForTree(treeId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY dueAt ASC")
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY dueAt ASC")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE enabled = 1 AND completedAt IS NULL")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun delete(reminderId: String)

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistCultivarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WishlistCultivarEntity>)

    @Query("SELECT * FROM wishlist_cultivars ORDER BY acquired ASC, species ASC, cultivar ASC")
    fun observeAll(): Flow<List<WishlistCultivarEntity>>

    @Query("SELECT * FROM wishlist_cultivars ORDER BY acquired ASC, species ASC, cultivar ASC")
    suspend fun getAll(): List<WishlistCultivarEntity>

    @Query(
        """
        SELECT * FROM wishlist_cultivars
        WHERE LOWER(species) = LOWER(:species) AND LOWER(cultivar) = LOWER(:cultivar)
        LIMIT 1
        """
    )
    suspend fun findBySpeciesAndCultivar(species: String, cultivar: String): WishlistCultivarEntity?

    @Query("DELETE FROM wishlist_cultivars WHERE id = :wishlistId")
    suspend fun delete(wishlistId: String)

    @Query("DELETE FROM wishlist_cultivars")
    suspend fun clearAll()
}
