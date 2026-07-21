package com.android.snippets.logic

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private companion object {
        const val TAG = "AutoBackupWorker"
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
        val schedule = prefs.getString("auto_backup_schedule", "Disabled") ?: "Disabled"
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
            performBackup(schedule.lowercase())
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto backup failed", e)
            Result.retry()
        }
    }

    private fun performBackup(suffix: String) {
        val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SnippetsBackups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val backupFile = File(backupDir, "snippets_backup_auto_$suffix.json")
        Log.d(TAG, "Creating auto backup file: ${backupFile.absolutePath}")

        val gson = Gson()
        val prettyGson = GsonBuilder().setPrettyPrinting().create()

        val backupJsonObj = JsonObject().apply {
            addProperty("version", 2)
            addProperty("exportedAt", System.currentTimeMillis())

            fun addJsonFileIfExists(key: String, fileName: String) {
                val file = File(applicationContext.filesDir, fileName)
                if (file.exists()) {
                    try {
                        val elem = gson.fromJson(file.readText(), JsonElement::class.java)
                        add(key, elem)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading $fileName for auto backup", e)
                    }
                }
            }

            addJsonFileIfExists("photos", "photos_v2.json")
            addJsonFileIfExists("collections", "collections_v2.json")
            addJsonFileIfExists("collectionIcons", "collection_icons_v2.json")
            addJsonFileIfExists("snippetColors", "snippet_colors_v2.json")
            addJsonFileIfExists("snippetStyles", "snippet_styles_v2.json")
            addJsonFileIfExists("snippetFirstSeenTimes", "snippet_first_seen_v1.json")

            val prefs = applicationContext.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
            val prefsObj = JsonObject().apply {
                prefs.all.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> addProperty(key, value)
                        is Number -> addProperty(key, value)
                        is String -> addProperty(key, value)
                        is Set<*> -> {
                            val arr = JsonArray()
                            value.forEach { item -> if (item is String) arr.add(item) }
                            add(key, arr)
                        }
                    }
                }
            }
            add("settings", prefsObj)
        }

        FileOutputStream(backupFile).use { outputStream ->
            outputStream.write(prettyGson.toJson(backupJsonObj).toByteArray(Charsets.UTF_8))
        }
        Log.d(TAG, "Auto backup completed successfully.")
    }
}
