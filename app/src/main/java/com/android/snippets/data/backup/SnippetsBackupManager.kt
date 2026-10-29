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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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

    suspend fun restoreFromJsonString(jsonString: String) {
        val rootElement = json.parseToJsonElement(jsonString)
        if (rootElement is JsonObject) {
            if ("photos" in rootElement) {
                val photosElem = rootElement["photos"] ?: JsonArray(emptyList())
                val importedPhotos: List<Photo> = json.decodeFromJsonElement(photosElem)
                database.withTransaction {
                    database.photoDao().deleteAllPhotos()
                    if (importedPhotos.isNotEmpty()) {
                        database.photoDao().insertPhotos(importedPhotos.map { it.toEntity() })
                    }
                }
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
            database.withTransaction {
                database.photoDao().deleteAllPhotos()
                if (importedPhotos.isNotEmpty()) {
                    database.photoDao().insertPhotos(importedPhotos.map { it.toEntity() })
                }
            }
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
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: throw IOException("Unable to open output stream")
    }

    suspend fun importBackup(context: Context, uri: Uri) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to open input stream")

        var isZip = false
        try {
            ZipInputStream(bytes.inputStream()).use { zipIn ->
                val entry = zipIn.nextEntry
                if (entry != null) {
                    isZip = true
                    var currEntry: ZipEntry? = entry
                    while (currEntry != null) {
                        val name = currEntry.name
                        if (!currEntry.isDirectory) {
                            val targetRoot: File = when {
                                name.startsWith("photos/") -> context.filesDir
                                name.startsWith("shared_prefs/") -> File(context.dataDir, "shared_prefs")
                                else -> context.filesDir
                            }
                            val relativePath = when {
                                name.startsWith("shared_prefs/") -> name.removePrefix("shared_prefs/")
                                else -> name
                            }

                            val root = targetRoot.canonicalFile
                            val outputFile = File(root, relativePath).canonicalFile
                            val rootPrefix = root.path.trimEnd(File.separatorChar) + File.separator
                            if (!outputFile.path.startsWith(rootPrefix)) {
                                throw IOException("Backup entry escapes its target directory")
                            }

                            outputFile.parentFile?.mkdirs()
                            FileOutputStream(outputFile).use { output -> zipIn.copyTo(output) }
                        }
                        zipIn.closeEntry()
                        currEntry = zipIn.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            if (isZip) throw e
            isZip = false
        }

        if (!isZip) {
            val jsonString = String(bytes, Charsets.UTF_8)
            restoreFromJsonString(jsonString)
        } else {
            val photosFile = File(context.filesDir, "photos_v2.json")
            if (photosFile.exists()) {
                try {
                    val jsonStr = photosFile.readText()
                    val unzippedPhotos: List<Photo>? = json.decodeFromString(jsonStr)
                    if (!unzippedPhotos.isNullOrEmpty()) {
                        database.withTransaction {
                            database.photoDao().deleteAllPhotos()
                            database.photoDao().insertPhotos(unzippedPhotos.map { it.toEntity() })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val collectionsFile = File(context.filesDir, "collections_v2.json")
            if (collectionsFile.exists()) {
                try {
                    val jsonStr = collectionsFile.readText()
                    val unzippedCols: List<String>? = json.decodeFromString(jsonStr)
                    if (!unzippedCols.isNullOrEmpty()) {
                        database.withTransaction {
                            database.collectionDao().deleteAllCollections()
                            database.collectionDao().insertCollections(
                                unzippedCols.mapIndexed { idx, name ->
                                    CollectionEntity(name = name, orderIndex = idx)
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
