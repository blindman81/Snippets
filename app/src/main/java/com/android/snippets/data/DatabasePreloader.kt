package com.android.snippets.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.snippets.data.repository.CollectionRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

object DatabasePreloader {
    private const val TAG = "DatabasePreloader"
    private const val PREFS_NAME = "snippets_prefs"
    private const val KEY_ROOM_PRELOAD_DONE = "room_preload_completed_v1"

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun preloadIfNecessary(
        context: Context,
        photoRepository: PhotoRepository,
        collectionRepository: CollectionRepository
    ) = withContext(Dispatchers.IO) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAlreadyPreloaded = prefs.getBoolean(KEY_ROOM_PRELOAD_DONE, false)

        val existingPhotoCount = photoRepository.getPhotoCount()
        val existingCollectionCount = collectionRepository.getCollectionCount()

        // If already marked as preloaded and has data, skip
        if (isAlreadyPreloaded && (existingPhotoCount > 0 || existingCollectionCount > 0)) {
            Log.d(TAG, "Database already preloaded with $existingPhotoCount photos and $existingCollectionCount collections.")
            return@withContext
        }

        val filesDir = context.filesDir

        // 1. Photos Migration
        if (existingPhotoCount == 0) {
            val photosFile = File(filesDir, "photos_v2.json")
            val photosList = mutableListOf<Photo>()

            if (photosFile.exists()) {
                try {
                    val content = photosFile.readText()
                    val decoded: List<Photo> = jsonSerializer.decodeFromString(content)
                    photosList.addAll(decoded)
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading photos_v2.json", e)
                }
            } else if (prefs.contains("photos_json")) {
                try {
                    val content = prefs.getString("photos_json", null)
                    if (!content.isNullOrEmpty()) {
                        val decoded: List<Photo> = jsonSerializer.decodeFromString(content)
                        photosList.addAll(decoded)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading photos_json from SharedPreferences", e)
                }
            }

            if (photosList.isNotEmpty()) {
                photoRepository.insertPhotos(photosList)
                Log.d(TAG, "Successfully migrated ${photosList.size} photos to Room database.")
            }
        }

        // 2. Collections Migration
        if (existingCollectionCount == 0) {
            val collectionsFile = File(filesDir, "collections_v2.json")
            val collectionIconsFile = File(filesDir, "collection_icons_v2.json")

            var loadedCollections: List<String> = emptyList()
            if (collectionsFile.exists()) {
                try {
                    val collJson = collectionsFile.readText()
                    loadedCollections = jsonSerializer.decodeFromString(collJson)
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading collections_v2.json", e)
                }
            } else if (prefs.contains("collections_json")) {
                try {
                    val collJson = prefs.getString("collections_json", null)
                    if (!collJson.isNullOrEmpty()) {
                        loadedCollections = jsonSerializer.decodeFromString(collJson)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading collections_json from SharedPreferences", e)
                }
            }

            val loadedIcons: Map<String, String> = if (collectionIconsFile.exists()) {
                try {
                    val iconJson = collectionIconsFile.readText()
                    jsonSerializer.decodeFromString(iconJson)
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading collection_icons_v2.json", e)
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val pinnedCollections: Set<String> = prefs.getStringSet("pinned_collections", emptySet()) ?: emptySet()

            if (loadedCollections.isNotEmpty()) {
                val collectionEntities = loadedCollections.mapIndexed { index, name ->
                    CollectionEntity(
                        name = name,
                        icon = loadedIcons[name],
                        orderIndex = index,
                        isPinned = pinnedCollections.contains(name)
                    )
                }
                collectionRepository.insertCollections(collectionEntities)
                Log.d(TAG, "Successfully migrated ${collectionEntities.size} collections to Room database.")
            }
        }

        prefs.edit().putBoolean(KEY_ROOM_PRELOAD_DONE, true).apply()
    }
}
