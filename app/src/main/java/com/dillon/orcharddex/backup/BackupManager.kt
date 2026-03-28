package com.dillon.orcharddex.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.dillon.orcharddex.data.local.ActivityPhotoEntity
import com.dillon.orcharddex.BuildConfig
import com.dillon.orcharddex.data.local.EventEntity
import com.dillon.orcharddex.data.local.GrowingLocationEntity
import com.dillon.orcharddex.data.local.HarvestEntity
import com.dillon.orcharddex.data.local.OrchardDexDatabase
import com.dillon.orcharddex.data.local.ReminderEntity
import com.dillon.orcharddex.data.local.TreeEntity
import com.dillon.orcharddex.data.local.TreePhotoEntity
import com.dillon.orcharddex.data.local.WishlistCultivarEntity
import com.dillon.orcharddex.data.model.SettingsSnapshot
import com.dillon.orcharddex.data.preferences.SettingsRepository
import com.dillon.orcharddex.data.repository.PhotoStorage
import com.dillon.orcharddex.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupValidation(
    val appVersion: String,
    val schemaVersion: Int,
    val locationCount: Int,
    val treeCount: Int,
    val eventCount: Int,
    val harvestCount: Int,
    val reminderCount: Int,
    val wishlistCount: Int,
    val photoCount: Int
)

@Serializable
data class BackupManifest(
    val archiveVersion: Int = 2,
    val appVersion: String,
    val schemaVersion: Int,
    val exportedAt: Long
)

class BackupManager(
    private val context: Context,
    private val database: OrchardDexDatabase,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: PhotoStorage,
    private val reminderScheduler: ReminderScheduler
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val trees = database.treeDao().getAllTrees()
        val locations = database.growingLocationDao().getAllLocations()
        val treePhotos = database.treePhotoDao().getAllPhotos()
        val activityPhotos = database.activityPhotoDao().getAllPhotos()
        val events = database.eventDao().getAllEvents()
        val harvests = database.harvestDao().getAllHarvests()
        val reminders = database.reminderDao().getAllReminders()
        val wishlist = database.wishlistDao().getAll()
        val settings = settingsRepository.snapshot()
        val photoPaths = (
            treePhotos.map(TreePhotoEntity::relativePath) +
                activityPhotos.map(ActivityPhotoEntity::relativePath) +
                events.mapNotNull(EventEntity::photoPath) +
                harvests.mapNotNull(HarvestEntity::photoPath)
            ).distinct()

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            ZipOutputStream(stream).use { zip ->
                writeJson(
                    zip,
                    "manifest.json",
                    BackupManifest(
                        appVersion = BuildConfig.VERSION_NAME,
                        schemaVersion = OrchardDexDatabase.DB_VERSION,
                        exportedAt = System.currentTimeMillis()
                    )
                )
                writeJson(zip, "growing_locations.json", locations)
                writeJson(zip, "trees.json", trees)
                writeJson(zip, "tree_photos.json", treePhotos)
                writeJson(zip, "activity_photos.json", activityPhotos)
                writeJson(zip, "events.json", events)
                writeJson(zip, "harvests.json", harvests)
                writeJson(zip, "reminders.json", reminders)
                writeJson(zip, "wishlist.json", wishlist)
                writeJson(zip, "settings.json", settings)
                photoPaths.forEach { relativePath ->
                    val file = photoStorage.resolve(relativePath)
                    if (file?.exists() == true) {
                        zip.putNextEntry(ZipEntry("photos/$relativePath"))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        } ?: error("Unable to open output stream for export.")
    }

    suspend fun validateImport(uri: Uri): BackupValidation = withContext(Dispatchers.IO) {
        val entries = readArchive(uri)
        val manifest = json.decodeFromString<BackupManifest>(entries.getRequiredText("manifest.json"))
        val locations = entries["growing_locations.json"]
            ?.toString(Charsets.UTF_8)
            ?.let { json.decodeFromString<List<GrowingLocationEntity>>(it) }
            .orEmpty()
        val trees = json.decodeFromString<List<TreeEntity>>(entries.getRequiredText("trees.json"))
        val events = json.decodeFromString<List<EventEntity>>(entries.getRequiredText("events.json"))
        val harvests = json.decodeFromString<List<HarvestEntity>>(entries.getRequiredText("harvests.json"))
        val reminders = json.decodeFromString<List<ReminderEntity>>(entries.getRequiredText("reminders.json"))
        val wishlist = json.decodeFromString<List<WishlistCultivarEntity>>(entries.getRequiredText("wishlist.json"))
        BackupValidation(
            appVersion = manifest.appVersion,
            schemaVersion = manifest.schemaVersion,
            locationCount = locations.size,
            treeCount = trees.size,
            eventCount = events.size,
            harvestCount = harvests.size,
            reminderCount = reminders.size,
            wishlistCount = wishlist.size,
            photoCount = entries.keys.count { it.startsWith("photos/") }
        )
    }

    suspend fun importReplaceAll(uri: Uri) = withContext(Dispatchers.IO) {
        val entries = readArchive(uri)
        val locations = entries["growing_locations.json"]
            ?.toString(Charsets.UTF_8)
            ?.let { json.decodeFromString<List<GrowingLocationEntity>>(it) }
            .orEmpty()
        val trees = json.decodeFromString<List<TreeEntity>>(entries.getRequiredText("trees.json"))
        val treePhotos = json.decodeFromString<List<TreePhotoEntity>>(entries.getRequiredText("tree_photos.json"))
        val events = json.decodeFromString<List<EventEntity>>(entries.getRequiredText("events.json"))
        val harvests = json.decodeFromString<List<HarvestEntity>>(entries.getRequiredText("harvests.json"))
        val activityPhotos = entries["activity_photos.json"]
            ?.toString(Charsets.UTF_8)
            ?.let { json.decodeFromString<List<ActivityPhotoEntity>>(it) }
            ?: legacyActivityPhotos(events, harvests)
        val reminders = json.decodeFromString<List<ReminderEntity>>(entries.getRequiredText("reminders.json"))
        val wishlist = json.decodeFromString<List<WishlistCultivarEntity>>(entries.getRequiredText("wishlist.json"))
        val settings = json.decodeFromString<SettingsSnapshot>(entries.getRequiredText("settings.json"))

        database.reminderDao().getActiveReminders().forEach { reminderScheduler.cancel(it.id) }
        photoStorage.clearAll()

        database.withTransaction {
            database.activityPhotoDao().clearAll()
            database.eventDao().clearAll()
            database.harvestDao().clearAll()
            database.reminderDao().clearAll()
            database.treePhotoDao().clearAll()
            database.treeDao().clearAll()
            database.growingLocationDao().clearAll()
            database.wishlistDao().clearAll()

            database.growingLocationDao().insertAll(locations)
            database.treeDao().insertAll(trees)
            database.treePhotoDao().insertAll(treePhotos)
            database.activityPhotoDao().insertAll(activityPhotos)
            database.eventDao().insertAll(events)
            database.harvestDao().insertAll(harvests)
            database.reminderDao().insertAll(reminders)
            database.wishlistDao().insertAll(wishlist)
        }

        entries.filterKeys { it.startsWith("photos/") }.forEach { (entryName, bytes) ->
            photoStorage.restorePhoto(entryName.removePrefix("photos/"), bytes)
        }

        settingsRepository.restore(settings)
        reminders.filter { it.enabled && it.completedAt == null }.forEach(reminderScheduler::schedule)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.reminderDao().getActiveReminders().forEach { reminderScheduler.cancel(it.id) }
        database.withTransaction {
            database.activityPhotoDao().clearAll()
            database.eventDao().clearAll()
            database.harvestDao().clearAll()
            database.reminderDao().clearAll()
            database.treePhotoDao().clearAll()
            database.treeDao().clearAll()
            database.growingLocationDao().clearAll()
            database.wishlistDao().clearAll()
        }
        photoStorage.clearAll()
    }

    private inline fun <reified T> writeJson(zip: ZipOutputStream, entryName: String, payload: T) {
        zip.putNextEntry(ZipEntry(entryName))
        zip.write(json.encodeToString(payload).toByteArray())
        zip.closeEntry()
    }

    private fun readArchive(uri: Uri): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val bytes = ByteArrayOutputStream().use { buffer ->
                            zip.copyTo(buffer)
                            buffer.toByteArray()
                        }
                        entries[entry.name] = bytes
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("Unable to open import file.")
        return entries
    }

    private fun Map<String, ByteArray>.getRequiredText(name: String): String =
        this[name]?.toString(Charsets.UTF_8) ?: error("Missing backup entry: $name")

    private fun legacyActivityPhotos(
        events: List<EventEntity>,
        harvests: List<HarvestEntity>
    ): List<ActivityPhotoEntity> = buildList {
        events.forEach { event ->
            event.photoPath?.takeIf(String::isNotBlank)?.let { relativePath ->
                add(
                    ActivityPhotoEntity(
                        id = "event:${event.id}",
                        ownerKind = "EVENT",
                        ownerId = event.id,
                        relativePath = relativePath,
                        caption = null,
                        createdAt = event.createdAt,
                        sortOrder = 0
                    )
                )
            }
        }
        harvests.forEach { harvest ->
            harvest.photoPath?.takeIf(String::isNotBlank)?.let { relativePath ->
                add(
                    ActivityPhotoEntity(
                        id = "harvest:${harvest.id}",
                        ownerKind = "HARVEST",
                        ownerId = harvest.id,
                        relativePath = relativePath,
                        caption = null,
                        createdAt = harvest.createdAt,
                        sortOrder = 0
                    )
                )
            }
        }
    }
}
