package com.dillon.orcharddex.diagnostics

import android.content.Context
import android.net.Uri
import com.dillon.orcharddex.BuildConfig
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DiagnosticRecord(
    val timestamp: Long,
    val kind: String,
    val category: String,
    val message: String,
    val attributes: Map<String, String> = emptyMap(),
    val stackTrace: String? = null
)

@Serializable
private data class DiagnosticsExport(
    val exportedAt: Long,
    val appVersion: String,
    val appVersionCode: Int,
    val records: List<DiagnosticRecord>
)

class LocalDiagnosticsStore(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val diagnosticsDirectory by lazy {
        File(appContext.filesDir, "diagnostics").apply { mkdirs() }
    }
    private val recordsFile by lazy {
        File(diagnosticsDirectory, "records.jsonl")
    }
    private val lock = Any()

    fun installCrashHandler() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                recordNow(
                    DiagnosticRecord(
                        timestamp = System.currentTimeMillis(),
                        kind = "crash",
                        category = "uncaught_exception",
                        message = throwable::class.java.name,
                        attributes = mapOf(
                            "thread" to thread.name,
                            "message" to (throwable.message ?: "")
                        ),
                        stackTrace = throwable.stackTraceToString()
                    )
                )
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    suspend fun recordBreadcrumb(
        category: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        runCatching {
            recordNow(
                DiagnosticRecord(
                    timestamp = System.currentTimeMillis(),
                    kind = "breadcrumb",
                    category = category,
                    message = message,
                    attributes = attributes
                )
            )
        }
    }

    suspend fun recordSlowPath(
        category: String,
        durationMs: Long,
        attributes: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        runCatching {
            recordNow(
                DiagnosticRecord(
                    timestamp = System.currentTimeMillis(),
                    kind = "slow_path",
                    category = category,
                    message = "$durationMs ms",
                    attributes = attributes + ("duration_ms" to durationMs.toString())
                )
            )
        }
    }

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val exportPayload = DiagnosticsExport(
            exportedAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE,
            records = readRecords()
        )
        val encoded = json.encodeToString(exportPayload)
        appContext.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(encoded)
        } ?: error("Unable to open output stream for diagnostics export.")
    }

    private fun readRecords(): List<DiagnosticRecord> = synchronized(lock) {
        if (!recordsFile.exists()) return emptyList()
        recordsFile.readLines()
            .asSequence()
            .mapNotNull { line -> runCatching { json.decodeFromString<DiagnosticRecord>(line) }.getOrNull() }
            .toList()
    }

    private fun recordNow(record: DiagnosticRecord) = synchronized(lock) {
        val encoded = json.encodeToString(record)
        diagnosticsDirectory.mkdirs()
        recordsFile.appendText("$encoded\n")
        trimIfNeeded()
    }

    private fun trimIfNeeded() {
        if (!recordsFile.exists()) return
        val lines = recordsFile.readLines()
        if (lines.size <= MAX_RECORDS && recordsFile.length() <= MAX_FILE_BYTES) return
        val trimmed = lines.takeLast(MAX_RECORDS)
        recordsFile.writeText(trimmed.joinToString(separator = "\n", postfix = "\n"))
    }

    companion object {
        private const val MAX_RECORDS = 250
        private const val MAX_FILE_BYTES = 256 * 1024L
    }
}
