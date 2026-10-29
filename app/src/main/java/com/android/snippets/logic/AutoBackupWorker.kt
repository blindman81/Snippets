package com.android.snippets.logic

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.snippets.data.toPhoto
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.model.Photo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val TAG = "AutoBackupWorker"

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun hasNewChanges(existing: JsonObject, current: JsonObject): Boolean {
            val allKeys = (existing.keys + current.keys) - "exportedAt"
            for (key in allKeys) {
                val existingElem = existing[key]
                val currentElem = current[key]
                if (existingElem != currentElem) {
                    return true
                }
            }
            return false
        }

        fun getReferenceBackupFile(backupDir: File, suffix: String): File? {
            val backupFile = File(backupDir, "snippets_backup_auto_$suffix.json")
            if (backupFile.exists()) return backupFile

            val otherFiles = backupDir.listFiles { _, name ->
                name.startsWith("snippets_backup_auto_") && name.endsWith(".json")
            } ?: return null
            return otherFiles.filter { it.isFile }.maxByOrNull { it.lastModified() }
        }

        suspend fun buildBackupData(context: Context): JsonObject {
            val db = com.android.snippets.data.SnippetsDatabase.getInstance(context)
            val roomPhotos = try {
                db.photoDao().getAllPhotosSync().map { it.toPhoto() }
            } catch (e: Exception) {
                emptyList()
            }
            val roomCollections = try {
                db.collectionDao().getAllCollectionsSync()
            } catch (e: Exception) {
                emptyList()
            }

            val collectionNames = roomCollections.map { it.name }
            val collectionIcons = roomCollections.mapNotNull { it.icon?.let { icon -> it.name to icon } }.toMap()

            val prefs = UserPreferencesRepository(context).userPreferencesFlow.first()
            val snippetFirstSeenTimes: Map<String, Long> = try {
                json.decodeFromString(prefs.snippetFirstSeenJson)
            } catch (_: Exception) {
                emptyMap()
            }

            return buildJsonObject {
                put("version", 2)
                put("photos", json.encodeToJsonElement(roomPhotos))
                put("collections", json.encodeToJsonElement(collectionNames))
                put("collectionIcons", json.encodeToJsonElement(collectionIcons))
                put("snippetFirstSeenTimes", json.encodeToJsonElement(snippetFirstSeenTimes))

                putJsonObject("settings") {
                    put("theme_preference", prefs.themePreference)
                    put("use_dynamic_colors", prefs.useDynamicColors)
                    put("expand_label_icon", prefs.expandLabelIcon)
                    put("expand_calendar_icon", prefs.expandCalendarIcon)
                    put("expand_location_icon", prefs.expandLocationIcon)
                    put("auto_backup_schedule", prefs.autoBackupSchedule)
                    put("notification_reminder_enabled", prefs.notificationReminderEnabled)
                    put("notification_reminder_hour", prefs.notificationReminderHour)
                    put("notification_reminder_minute", prefs.notificationReminderMinute)
                    put("snippet_sort_type", prefs.snippetSortType)
                    put("is_sort_ascending", prefs.isSortAscending)
                    put("selected_filter_snippets", json.encodeToJsonElement(prefs.selectedFilterSnippets.sorted()))
                    put("display_mode", prefs.displayMode)
                    put("collection_display_modes_json", prefs.collectionDisplayModesJson)
                    put("photo_sort_type", prefs.photoSortType)
                    put("collection_photo_sort_types_json", prefs.collectionPhotoSortTypesJson)
                    put("last_seen_yearly_recap_year", prefs.lastSeenYearlyRecapYear)
                    put("last_seen_monthly_recap_period", prefs.lastSeenMonthlyRecapPeriod)
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        val schedule = UserPreferencesRepository(applicationContext)
            .userPreferencesFlow.first().autoBackupSchedule
        Log.d(TAG, "AutoBackupWorker triggered. Schedule: $schedule")

        if (schedule == "Disabled") {
            return Result.success()
        }

        val calendar = Calendar.getInstance()
        val shouldBackup = when (schedule) {
            "Daily" -> true
            "Weekly" -> calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            "Monthly" -> calendar.get(Calendar.DAY_OF_MONTH) == 1
            else -> false
        }

        if (!shouldBackup) {
            Log.d(TAG, "Backup not due today.")
            return Result.success()
        }

        return try {
            val performed = performBackup(schedule.lowercase())
            if (performed) {
                Log.d(TAG, "Auto backup completed successfully.")
            } else {
                Log.d(TAG, "Auto backup skipped: no new changes.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto backup failed", e)
            Result.retry()
        }
    }

    private suspend fun performBackup(suffix: String): Boolean {
        val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SnippetsBackups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val backupFile = File(backupDir, "snippets_backup_auto_$suffix.json")
        val currentBackupData = buildBackupData(applicationContext)

        val referenceFile = getReferenceBackupFile(backupDir, suffix)
        if (referenceFile != null && referenceFile.exists()) {
            try {
                val existingJson: JsonObject = json.decodeFromString(referenceFile.readText())
                if (!hasNewChanges(existingJson, currentBackupData)) {
                    Log.d(TAG, "No new changes detected compared to ${referenceFile.name}. Skipping auto backup.")
                    return false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read or parse existing backup file ${referenceFile.name}, continuing with backup", e)
            }
        }

        Log.d(TAG, "New changes detected or no existing backup. Creating auto backup file: ${backupFile.absolutePath}")
        val backupJsonObj = buildJsonObject {
            currentBackupData.forEach { (k, v) -> put(k, v) }
            put("exportedAt", System.currentTimeMillis())
        }

        FileOutputStream(backupFile).use { outputStream ->
            outputStream.write(json.encodeToString(backupJsonObj).toByteArray(Charsets.UTF_8))
        }
        Log.d(TAG, "Auto backup completed successfully.")
        return true
    }
}
