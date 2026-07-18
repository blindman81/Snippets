package com.android.snippets.logic

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.android.snippets.model.Photo
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

        val backupFile = File(backupDir, "snippets_backup_auto_$suffix.zip")
        Log.d(TAG, "Creating auto backup file: ${backupFile.absolutePath}")

        FileOutputStream(backupFile).use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                // 1. Pack JSON files
                val filesToBackup = listOf(
                    "photos_v2.json",
                    "collections_v2.json",
                    "collection_icons_v2.json",
                    "snippet_colors_v2.json",
                    "snippet_styles_v2.json",
                    "snippet_first_seen_v1.json"
                )
                for (fileName in filesToBackup) {
                    val file = File(applicationContext.filesDir, fileName)
                    if (file.exists()) {
                        zipOut.putNextEntry(ZipEntry(fileName))
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }

                // 2. Pack shared preferences
                val sharedPrefsFile = File(applicationContext.dataDir, "shared_prefs/snippets_prefs.xml")
                if (sharedPrefsFile.exists()) {
                    zipOut.putNextEntry(ZipEntry("shared_prefs/snippets_prefs.xml"))
                    sharedPrefsFile.inputStream().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }

                // Get active photo names from photos_v2.json
                val activePhotoNames = mutableSetOf<String>()
                val photosFile = File(applicationContext.filesDir, "photos_v2.json")
                if (photosFile.exists()) {
                    try {
                        val json = photosFile.readText()
                        val type = object : TypeToken<List<Photo>>() {}.type
                        val photos: List<Photo> = Gson().fromJson(json, type) ?: emptyList()
                        photos.forEach { photo ->
                            val uriStr = photo.uriString
                            val fileName = uriStr.substringAfterLast('/')
                            if (fileName.isNotEmpty()) {
                                activePhotoNames.add(fileName)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing photos_v2.json for active photo filtering in auto backup", e)
                    }
                }

                // 3. Pack photo files
                val photosDir = File(applicationContext.filesDir, "photos")
                if (photosDir.exists() && photosDir.isDirectory) {
                    photosDir.listFiles()?.forEach { photoFile ->
                        if (photoFile.isFile) {
                            if (activePhotoNames.contains(photoFile.name)) {
                                zipOut.putNextEntry(ZipEntry("photos/${photoFile.name}"))
                                photoFile.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            } else {
                                Log.d(TAG, "Auto backup: Skipping orphaned/deleted photo file: ${photoFile.name}")
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "Auto backup completed successfully.")
    }
}
