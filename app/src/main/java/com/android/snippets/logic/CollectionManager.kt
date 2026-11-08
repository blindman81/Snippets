package com.android.snippets.logic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.android.snippets.model.Photo

data class FinalizeEmojiResult(
    val finalName: String,
    val updatedCollections: List<String>,
    val updatedIcons: Map<String, String>,
    val updatedPhotos: List<Photo>,
    val photosChanged: Boolean,
    val oldIndex: Int
)

data class DeleteCollectionResult(
    val updatedCollections: List<String>,
    val updatedIcons: Map<String, String>,
    val updatedPhotos: List<Photo>,
    val photosChanged: Boolean,
    val updatedFilters: List<String>,
    val filtersChanged: Boolean
)

object CollectionManager {

    fun generateNewPlaceholderName(userCollections: List<String>): String {
        var newName = ""
        var counter = 1
        while (userCollections.contains(newName)) {
            newName = " ".repeat(counter)
            counter++
        }
        return newName
    }

    fun finalizeCollectionEmoji(
        userCollections: List<String>,
        collectionIcons: Map<String, String>,
        photos: List<Photo>,
        collection: String,
        emoji: String
    ): FinalizeEmojiResult? {
        val cleanEmoji = emoji.trim()
        if (cleanEmoji.isBlank()) return null

        var finalName = cleanEmoji
        var counter = 2
        while (userCollections.any { it != collection && it.equals(finalName, ignoreCase = true) }) {
            finalName = "$cleanEmoji $counter"
            counter++
        }

        val oldIndex = userCollections.indexOf(collection)
        val updatedCollections = if (oldIndex != -1) {
            val updated = userCollections.toMutableList()
            updated[oldIndex] = finalName
            updated
        } else if (!userCollections.contains(finalName)) {
            userCollections + finalName
        } else {
            userCollections
        }

        val updatedIcons = (collectionIcons - collection) + (finalName to cleanEmoji)

        var photosChanged = false
        val updatedPhotos = if (collection.isNotBlank() && collection != finalName) {
            photos.map { photo ->
                if (photo.collections.contains(collection)) {
                    photosChanged = true
                    photo.copy(collections = photo.collections - collection + finalName)
                } else photo
            }
        } else {
            photos
        }

        return FinalizeEmojiResult(
            finalName = finalName,
            updatedCollections = updatedCollections,
            updatedIcons = updatedIcons,
            updatedPhotos = updatedPhotos,
            photosChanged = photosChanged,
            oldIndex = oldIndex
        )
    }

    fun deleteCollection(
        userCollections: List<String>,
        collectionIcons: Map<String, String>,
        photos: List<Photo>,
        selectedFilterSnippets: List<String>,
        collection: String
    ): DeleteCollectionResult {
        val updatedCollections = userCollections.filter { it != collection }
        val updatedIcons = collectionIcons - collection

        var photosChanged = false
        val updatedPhotos = photos.map { photo ->
            if (photo.collections.contains(collection)) {
                photosChanged = true
                photo.copy(collections = photo.collections - collection)
            } else photo
        }

        val filtersChanged = selectedFilterSnippets.contains(collection)
        val updatedFilters = if (filtersChanged) {
            selectedFilterSnippets.filter { it != collection }
        } else {
            selectedFilterSnippets
        }

        return DeleteCollectionResult(
            updatedCollections = updatedCollections,
            updatedIcons = updatedIcons,
            updatedPhotos = updatedPhotos,
            photosChanged = photosChanged,
            updatedFilters = updatedFilters,
            filtersChanged = filtersChanged
        )
    }

    fun assignPhotosToCollection(
        photos: List<Photo>,
        selectedPhotoIds: Set<String>,
        collection: String,
        now: Long = System.currentTimeMillis()
    ): List<Photo> {
        return photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                if (collection == "Favorites") {
                    photo.copy(isFavorite = true)
                } else {
                    if (!photo.collections.contains(collection)) {
                        photo.copy(
                            collections = photo.collections + collection,
                            snippetsAddedTime = if (photo.snippetsAddedTime == 0L) now else photo.snippetsAddedTime
                        )
                    } else photo
                }
            } else photo
        }
    }

    fun removePhotosFromCollection(
        photos: List<Photo>,
        selectedPhotoIds: Set<String>,
        collection: String
    ): List<Photo> {
        return photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                if (collection == "Favorites") {
                    photo.copy(isFavorite = false)
                } else {
                    photo.copy(collections = photo.collections - collection)
                }
            } else photo
        }
    }

    fun getCollectionIcon(
        collection: String,
        editingCollectionEmoji: String?,
        editingEmojiPreview: String?,
        collectionIcons: Map<String, String>
    ): Any {
        if (collection == "Favorites") return Icons.Rounded.Favorite
        val iconName = if (collection == editingCollectionEmoji && editingEmojiPreview != null) {
            editingEmojiPreview
        } else {
            collectionIcons[collection] ?: collection
        }

        return when (iconName) {
            "Landscape" -> Icons.Rounded.Landscape
            "Nature" -> Icons.Rounded.Nature
            "Person" -> Icons.Rounded.Person
            "Group" -> Icons.Rounded.Groups
            "LocalDrink" -> Icons.Rounded.LocalDrink
            "Cookie" -> Icons.Rounded.Cookie
            "Pets" -> Icons.Rounded.Pets
            "Travel" -> Icons.Rounded.Flight
            "Waves" -> Icons.Rounded.Waves
            "Apartment" -> Icons.Rounded.Apartment
            else -> if (iconName.isBlank()) {
                Icons.Rounded.Add
            } else if (iconName.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                iconName
            } else {
                Icons.Rounded.Folder
            }
        }
    }
}
