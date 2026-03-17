package com.dillon.orcharddex.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PhotoStorage(private val context: Context) {
    enum class Category(val folder: String) {
        TREE("trees"),
        EVENT("events"),
        HARVEST("harvests")
    }

    private val photosRoot: File by lazy {
        File(context.filesDir, "photos").apply { mkdirs() }
    }

    suspend fun importPhoto(uri: Uri, category: Category): String = withContext(Dispatchers.IO) {
        val extension = context.contentResolver.getType(uri)
            ?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
            ?: "jpg"
        val relativePath = "${category.folder}/${UUID.randomUUID()}.$extension"
        val destination = File(photosRoot, relativePath).apply { parentFile?.mkdirs() }
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to open image stream for $uri")
        relativePath
    }

    suspend fun deletePhoto(relativePath: String?) = withContext(Dispatchers.IO) {
        relativePath?.let { File(photosRoot, it).delete() }
    }

    fun resolve(relativePath: String?): File? = relativePath?.let { File(photosRoot, it) }

    suspend fun restorePhoto(relativePath: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val destination = File(photosRoot, relativePath).apply { parentFile?.mkdirs() }
        destination.writeBytes(bytes)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        if (photosRoot.exists()) {
            photosRoot.deleteRecursively()
        }
        photosRoot.mkdirs()
    }
}
