package com.android.snippets.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.android.snippets.data.SnippetsDatabase
import com.android.snippets.data.CollectionEntity
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.toEntity
import com.android.snippets.model.Photo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnippetsBackupManager @Inject constructor(
    private val database: SnippetsDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    suspend fun generateBackupJson(
        photos: List<Photo>,
        userCollections: List<String>,
        collectionIcons: Map<String, String>,
        snippetFirstSeenTimes: Map<String, Long>
    ): String {
        val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
        val backupJsonObj = buildJsonObject {
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("photos", json.encodeToJsonElement(photos))
            put("collections", json.encodeToJsonElement(userCollections))
            put("collectionIcons", json.encodeToJsonElement(collectionIcons))
            put("snippetFirstSeenTimes", json.encodeToJsonElement(snippetFirstSeenTimes))

            putJsonObject("settings") {
                put("theme_preference", userPrefs.themePreference)
                put("use_dynamic_colors", userPrefs.useDynamicColors)
                put("expand_label_icon", userPrefs.expandLabelIcon)
                put("expand_calendar_icon", userPrefs.expandCalendarIcon)
                put("expand_location_icon", userPrefs.expandLocationIcon)
                put("auto_backup_schedule", userPrefs.autoBackupSchedule)
                put("notification_reminder_enabled", userPrefs.notificationReminderEnabled)
                put("notification_reminder_hour", userPrefs.notificationReminderHour)
                put("notification_reminder_minute", userPrefs.notificationReminderMinute)
                put("snippet_sort_type", userPrefs.snippetSortType)
                put("is_sort_ascending", userPrefs.isSortAscending)
                put("selected_filter_snippets", json.encodeToJsonElement(userPrefs.selectedFilterSnippets.sorted()))
                put("display_mode", userPrefs.displayMode)
                put("collection_display_modes_json", userPrefs.collectionDisplayModesJson)
                put("photo_sort_type", userPrefs.photoSortType)
                put("collection_photo_sort_types_json", userPrefs.collectionPhotoSortTypesJson)
                put("last_seen_yearly_recap_year", userPrefs.lastSeenYearlyRecapYear)
                put("last_seen_monthly_recap_period", userPrefs.lastSeenMonthlyRecapPeriod)
            }
        }
        return json.encodeToString(backupJsonObj)
    }

    private fun normalizePhotoUri(context: Context, photo: Photo): Photo {
        val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
        val uriStr = photo.uriString

        // 1. If it's a file path or contains /photos/
        if (uriStr.startsWith("file://") || uriStr.startsWith("/") || uriStr.contains("/photos/")) {
            val rawPath = if (uriStr.startsWith("file://")) uriStr.substring(7) else uriStr
            val directFile = File(rawPath)
            val fileName = directFile.name
            val inPhotosDir = File(photosDir, fileName)

            // Fast path: check inPhotosDir first since extracted files are stored there
            if (fileName.isNotEmpty() && inPhotosDir.exists()) {
                val (w, h) = if (photo.widthPx > 0 && photo.heightPx > 0) photo.widthPx to photo.heightPx else extractDimensions(inPhotosDir)
                return photo.copy(
                    uriString = "file://${inPhotosDir.absolutePath}",
                    widthPx = w,
                    heightPx = h
                )
            }

            if (directFile.exists()) {
                val (w, h) = if (photo.widthPx > 0 && photo.heightPx > 0) photo.widthPx to photo.heightPx else extractDimensions(directFile)
                return photo.copy(
                    uriString = "file://${directFile.absolutePath}",
                    widthPx = w,
                    heightPx = h
                )
            }

            if (fileName.isNotEmpty()) {
                // Check context.filesDir/fileName (moved directly from root of zip)
                val inFilesDir = File(context.filesDir, fileName)
                if (inFilesDir.exists()) {
                    inFilesDir.copyTo(inPhotosDir, overwrite = true)
                    inFilesDir.delete()
                    val (w, h) = if (photo.widthPx > 0 && photo.heightPx > 0) photo.widthPx to photo.heightPx else extractDimensions(inPhotosDir)
                    return photo.copy(
                        uriString = "file://${inPhotosDir.absolutePath}",
                        widthPx = w,
                        heightPx = h
                    )
                }

                // Check context.filesDir/files/photos/fileName
                val inSubDir = File(context.filesDir, "files/photos/$fileName")
                if (inSubDir.exists()) {
                    inSubDir.copyTo(inPhotosDir, overwrite = true)
                    inSubDir.delete()
                    val (w, h) = if (photo.widthPx > 0 && photo.heightPx > 0) photo.widthPx to photo.heightPx else extractDimensions(inPhotosDir)
                    return photo.copy(
                        uriString = "file://${inPhotosDir.absolutePath}",
                        widthPx = w,
                        heightPx = h
                    )
                }
            }
        }

        // 2. If it's a content URI, attempt to save locally to internal storage
        if (uriStr.startsWith("content://")) {
            try {
                val parsedUri = Uri.parse(uriStr)
                val fileName = "photo_${photo.date}.jpg"
                val destFile = File(photosDir, fileName)
                context.contentResolver.openInputStream(parsedUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (destFile.exists() && destFile.length() > 0) {
                    val (w, h) = if (photo.widthPx > 0 && photo.heightPx > 0) photo.widthPx to photo.heightPx else extractDimensions(destFile)
                    return photo.copy(
                        uriString = "file://${destFile.absolutePath}",
                        widthPx = w,
                        heightPx = h
                    )
                }
            } catch (_: Exception) {
            }
        }

        return photo
    }

    private fun extractDimensions(file: File): Pair<Int, Int> {
        return try {
            val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
            val w = options.outWidth
            val h = options.outHeight
            if (w > 0 && h > 0) w to h else 0 to 0
        } catch (_: Exception) {
            0 to 0
        }
    }

    suspend fun restoreFromJsonString(
        jsonString: String,
        context: Context? = null,
        onProgress: ((current: Int, total: Int, message: String) -> Unit)? = null
    ) {
        val rootElement = json.parseToJsonElement(jsonString)
        if (rootElement is JsonObject) {
            if ("photos" in rootElement) {
                val photosElem = rootElement["photos"] ?: JsonArray(emptyList())
                val importedPhotos: List<Photo> = json.decodeFromJsonElement(photosElem)
                val totalPhotos = importedPhotos.size
                onProgress?.invoke(0, totalPhotos, "Restoring $totalPhotos photos...")
                val normalizedPhotos = if (context != null) {
                    importedPhotos.map { normalizePhotoUri(context, it) }
                } else {
                    importedPhotos
                }
                database.withTransaction {
                    database.photoDao().deleteAllPhotos()
                    if (normalizedPhotos.isNotEmpty()) {
                        database.photoDao().insertPhotos(normalizedPhotos.map { it.toEntity() })
                    }
                }
                onProgress?.invoke(totalPhotos, totalPhotos, "Restored $totalPhotos photos")
            }

            val importedIcons: Map<String, String> = if ("collectionIcons" in rootElement) {
                try {
                    json.decodeFromJsonElement(rootElement["collectionIcons"]!!)
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            if ("collections" in rootElement) {
                val collElem = rootElement["collections"] ?: JsonArray(emptyList())
                val importedCollections: List<String> = json.decodeFromJsonElement(collElem)
                database.withTransaction {
                    database.collectionDao().deleteAllCollections()
                    if (importedCollections.isNotEmpty()) {
                        database.collectionDao().insertCollections(
                            importedCollections.mapIndexed { idx, name ->
                                CollectionEntity(
                                    name = name,
                                    icon = importedIcons[name],
                                    orderIndex = idx
                                )
                            }
                        )
                    }
                }
            }

            if ("snippetFirstSeenTimes" in rootElement) {
                try {
                    val firstSeenMap: Map<String, Long> = json.decodeFromJsonElement(rootElement["snippetFirstSeenTimes"]!!)
                    userPreferencesRepository.updateSnippetFirstSeenJson(json.encodeToString(firstSeenMap))
                } catch (_: Exception) {
                }
            }

            if ("settings" in rootElement && rootElement["settings"] is JsonObject) {
                val settingsObj = rootElement["settings"]!!.jsonObject
                val settingsValues = settingsObj.mapNotNull { (key, elem) ->
                    if (elem is JsonPrimitive) {
                        elem.booleanOrNull?.let { key to it }
                            ?: elem.intOrNull?.let { key to it }
                            ?: elem.longOrNull?.let { key to it }
                            ?: elem.floatOrNull?.let { key to it }
                            ?: (key to elem.content)
                    } else if (elem is JsonArray) {
                        key to elem.mapNotNull { (it as? JsonPrimitive)?.content }.toSet()
                    } else {
                        null
                    }
                }.toMap()
                userPreferencesRepository.restorePreferences(settingsValues)
            }
        } else if (rootElement is JsonArray) {
            val importedPhotos: List<Photo> = json.decodeFromJsonElement(rootElement)
            val totalPhotos = importedPhotos.size
            onProgress?.invoke(0, totalPhotos, "Restoring $totalPhotos photos...")
            val normalizedPhotos = if (context != null) {
                importedPhotos.map { normalizePhotoUri(context, it) }
            } else {
                importedPhotos
            }
            database.withTransaction {
                database.photoDao().deleteAllPhotos()
                if (normalizedPhotos.isNotEmpty()) {
                    database.photoDao().insertPhotos(normalizedPhotos.map { it.toEntity() })
                }
            }
            onProgress?.invoke(totalPhotos, totalPhotos, "Restored $totalPhotos photos")
        } else {
            throw IOException("Backup must contain a JSON object or photo list")
        }
    }

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        photos: List<Photo>,
        userCollections: List<String>,
        collectionIcons: Map<String, String>,
        snippetFirstSeenTimes: Map<String, Long>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val jsonString = generateBackupJson(
                photos = photos,
                userCollections = userCollections,
                collectionIcons = collectionIcons,
                snippetFirstSeenTimes = snippetFirstSeenTimes
            )

            ZipOutputStream(outputStream.buffered(65536)).use { zipOut ->
                zipOut.setLevel(java.util.zip.Deflater.BEST_SPEED)
                // 1. Write JSON metadata
                zipOut.putNextEntry(ZipEntry("backup.json"))
                zipOut.write(jsonString.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // 2. Bundle all photo files from internal storage
                val addedFileNames = mutableSetOf<String>()
                for (photo in photos) {
                    val uriStr = photo.uriString
                    val file = when {
                        uriStr.startsWith("file://") -> File(uriStr.substring(7))
                        uriStr.startsWith("/") -> File(uriStr)
                        else -> null
                    }
                    if (file != null && file.exists() && file.isFile && addedFileNames.add(file.name)) {
                        try {
                            zipOut.putNextEntry(ZipEntry("photos/${file.name}"))
                            file.inputStream().buffered(65536).use { input ->
                                input.copyTo(zipOut, bufferSize = 65536)
                            }
                            zipOut.closeEntry()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } ?: throw IOException("Unable to open output stream")
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        onProgress: ((current: Int, total: Int, message: String) -> Unit)? = null
    ) {
        val rawInput = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream")

        val bufferedInput = BufferedInputStream(rawInput, 65536)
        bufferedInput.mark(4)
        val header = ByteArray(4)
        val readCount = bufferedInput.read(header, 0, 4)
        bufferedInput.reset()

        val isZip = readCount >= 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()

        if (isZip) {
            val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
            var totalEntries = 0
            var photoCount = 0
            if (onProgress != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { countStream ->
                        ZipInputStream(BufferedInputStream(countStream)).use { zipCountIn ->
                            var entry = zipCountIn.nextEntry
                            while (entry != null) {
                                if (!entry.isDirectory) {
                                    totalEntries++
                                    val lower = entry.name.lowercase()
                                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                                        lower.endsWith(".webp") || lower.endsWith(".heic") || lower.endsWith(".heif") ||
                                        entry.name.startsWith("photos/")) {
                                        photoCount++
                                    }
                                }
                                zipCountIn.closeEntry()
                                entry = zipCountIn.nextEntry
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            val maxProgress = if (photoCount > 0) photoCount else totalEntries.coerceAtLeast(1)
            var currentProgress = 0
            onProgress?.invoke(0, maxProgress, if (photoCount > 0) "Restoring photos (0/$maxProgress)..." else "Extracting backup...")

            val extractedJsonFiles = mutableListOf<File>()

            ZipInputStream(bufferedInput).use { zipIn ->
                var currEntry: ZipEntry? = zipIn.nextEntry
                while (currEntry != null) {
                    val name = currEntry.name
                    val lower = name.lowercase()
                    if (!currEntry.isDirectory) {
                        val isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                                lower.endsWith(".webp") || lower.endsWith(".heic") || lower.endsWith(".heif")
                        val isJson = lower.endsWith(".json")
                        val isPref = name.startsWith("shared_prefs/")

                        val outputFile = when {
                            isImage -> {
                                File(photosDir, File(name).name)
                            }
                            isPref -> {
                                val rel = name.removePrefix("shared_prefs/")
                                val prefDir = File(context.dataDir, "shared_prefs").apply { if (!exists()) mkdirs() }
                                File(prefDir, rel)
                            }
                            isJson -> {
                                val jsonFile = File(context.filesDir, File(name).name)
                                extractedJsonFiles.add(jsonFile)
                                jsonFile
                            }
                            else -> {
                                File(context.filesDir, name)
                            }
                        }

                        outputFile.parentFile?.mkdirs()
                        FileOutputStream(outputFile).buffered(65536).use { output ->
                            zipIn.copyTo(output, bufferSize = 65536)
                        }

                        if (isImage || name.startsWith("photos/")) {
                            currentProgress++
                            onProgress?.invoke(currentProgress, maxProgress, "Restoring photo $currentProgress of $maxProgress...")
                        } else if (photoCount == 0) {
                            currentProgress++
                            onProgress?.invoke(currentProgress, maxProgress, "Extracting file $currentProgress of $maxProgress...")
                        }
                    }
                    zipIn.closeEntry()
                    currEntry = zipIn.nextEntry
                }
            }

            onProgress?.invoke(maxProgress, maxProgress, "Restoring database entries...")

            var databaseRestored = false

            // Try candidate JSON backup files in order of likelihood
            val priorityNames = listOf("photos_v2.json", "snippets_backup.json", "backup.json")
            val sortedJsonFiles = extractedJsonFiles.sortedBy { f ->
                val idx = priorityNames.indexOf(f.name.lowercase())
                if (idx != -1) idx else 99
            }

            for (jsonFile in sortedJsonFiles) {
                if (jsonFile.exists()) {
                    try {
                        val jsonText = jsonFile.readText()
                        restoreFromJsonString(jsonText, context, onProgress)
                        databaseRestored = true
                        break
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Also check collections_v2.json if collections were not restored
            val collectionsFile = File(context.filesDir, "collections_v2.json")
            if (collectionsFile.exists()) {
                try {
                    val jsonStr = collectionsFile.readText()
                    val unzippedCols: List<String>? = json.decodeFromString(jsonStr)
                    if (!unzippedCols.isNullOrEmpty()) {
                        database.withTransaction {
                            database.collectionDao().deleteAllCollections()
                            database.collectionDao().insertCollections(
                                unzippedCols.mapIndexed { idx, colName ->
                                    CollectionEntity(name = colName, orderIndex = idx)
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            onProgress?.invoke(maxProgress, maxProgress, "Restore complete")
        } else {
            val jsonString = bufferedInput.bufferedReader(Charsets.UTF_8).use { it.readText() }
            restoreFromJsonString(jsonString, context, onProgress)
        }
    }
}
