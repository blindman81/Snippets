package com.android.snippets.logic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.android.snippets.model.Photo
import com.ln.android.snippets.R

data class FinalizeBoxEmojiResult(
    val finalName: String,
    val updatedBoxes: List<String>,
    val updatedIcons: Map<String, String>,
    val updatedPhotos: List<Photo>,
    val photosChanged: Boolean,
    val oldIndex: Int
)

data class DeleteBoxResult(
    val updatedBoxes: List<String>,
    val updatedIcons: Map<String, String>,
    val updatedPhotos: List<Photo>,
    val photosChanged: Boolean,
    val updatedFilters: List<String>,
    val filtersChanged: Boolean
)

object BoxManager {

    fun getBoxDisplayName(box: String, userBoxes: List<String>): String {
        if (box == "Box" || box == "Library") return "Box"
        if (box == "Box 1" || box == "Favorites") return "Box 1"

        val match = Regex("^Box (\\d+)$", RegexOption.IGNORE_CASE).find(box.trim())
        if (match != null) {
            val num = match.groupValues[1]
            return "Box $num"
        }

        val idx = userBoxes.indexOf(box)
        return if (idx >= 0) "Box ${idx + 2}" else "Box 2"
    }

    fun generateNextBoxName(userBoxes: List<String>): String {
        val existingNumbers = mutableSetOf<Int>()
        // Favorites is Box 1
        existingNumbers.add(1)
        userBoxes.forEachIndexed { idx, name ->
            val match = Regex("^Box (\\d+)$", RegexOption.IGNORE_CASE).find(name.trim())
            if (match != null) {
                match.groupValues[1].toIntOrNull()?.let { existingNumbers.add(it) }
            } else {
                existingNumbers.add(idx + 2)
            }
        }
        var index = 2
        while (existingNumbers.contains(index)) {
            index++
        }
        return "Box $index"
    }

    fun generateNewPlaceholderName(userBoxes: List<String>): String {
        return generateNextBoxName(userBoxes)
    }

    fun finalizeBoxEmoji(
        userBoxes: List<String>,
        boxIcons: Map<String, String>,
        photos: List<Photo>,
        box: String,
        emoji: String
    ): FinalizeBoxEmojiResult? {
        val cleanEmoji = emoji.trim()
        if (cleanEmoji.isBlank()) return null

        if (box == "Box 1" || box == "Favorites") {
            return FinalizeBoxEmojiResult(
                finalName = "Box 1",
                updatedBoxes = userBoxes.filter { it != "Box 1" && it != "Favorites" },
                updatedIcons = boxIcons + ("Box 1" to cleanEmoji) + ("Favorites" to cleanEmoji),
                updatedPhotos = photos,
                photosChanged = false,
                oldIndex = -1
            )
        }

        val finalName = if (box.isBlank() || !box.matches(Regex("^Box \\d+$", RegexOption.IGNORE_CASE))) {
            val idx = userBoxes.indexOf(box)
            if (idx >= 0) "Box ${idx + 2}" else generateNextBoxName(userBoxes)
        } else {
            box
        }

        val filteredBoxes = userBoxes.filter { it != "Box 1" && it != "Favorites" }
        val oldIndex = filteredBoxes.indexOf(box)
        val updatedBoxes = if (oldIndex != -1) {
            val updated = filteredBoxes.toMutableList()
            updated[oldIndex] = finalName
            updated
        } else if (!filteredBoxes.contains(finalName)) {
            filteredBoxes + finalName
        } else {
            filteredBoxes
        }

        val updatedIcons = (boxIcons - box) + (finalName to cleanEmoji)

        var photosChanged = false
        val updatedPhotos = if (box.isNotBlank() && box != finalName) {
            photos.map { photo ->
                if (photo.collections.contains(box)) {
                    photosChanged = true
                    photo.copy(collections = photo.collections - box + finalName)
                } else photo
            }
        } else {
            photos
        }

        return FinalizeBoxEmojiResult(
            finalName = finalName,
            updatedBoxes = updatedBoxes,
            updatedIcons = updatedIcons,
            updatedPhotos = updatedPhotos,
            photosChanged = photosChanged,
            oldIndex = oldIndex
        )
    }

    fun deleteBox(
        userBoxes: List<String>,
        boxIcons: Map<String, String>,
        photos: List<Photo>,
        selectedFilterSnippets: List<String>,
        box: String
    ): DeleteBoxResult {
        val updatedBoxes = userBoxes.filter { it != box }
        val updatedIcons = boxIcons - box

        var photosChanged = false
        val updatedPhotos = photos.map { photo ->
            if (photo.collections.contains(box)) {
                photosChanged = true
                photo.copy(collections = photo.collections - box)
            } else photo
        }

        val filtersChanged = selectedFilterSnippets.contains(box)
        val updatedFilters = if (filtersChanged) {
            selectedFilterSnippets.filter { it != box }
        } else {
            selectedFilterSnippets
        }

        return DeleteBoxResult(
            updatedBoxes = updatedBoxes,
            updatedIcons = updatedIcons,
            updatedPhotos = updatedPhotos,
            photosChanged = photosChanged,
            updatedFilters = updatedFilters,
            filtersChanged = filtersChanged
        )
    }

    fun assignPhotosToBox(
        photos: List<Photo>,
        selectedPhotoIds: Set<String>,
        box: String,
        now: Long = System.currentTimeMillis()
    ): List<Photo> {
        return photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                if (box == "Box 1" || box == "Favorites") {
                    photo.copy(isFavorite = true)
                } else {
                    if (!photo.collections.contains(box)) {
                        photo.copy(
                            collections = photo.collections + box,
                            snippetsAddedTime = if (photo.snippetsAddedTime == 0L) now else photo.snippetsAddedTime
                        )
                    } else photo
                }
            } else photo
        }
    }

    fun removePhotosFromBox(
        photos: List<Photo>,
        selectedPhotoIds: Set<String>,
        box: String
    ): List<Photo> {
        return photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                if (box == "Box 1" || box == "Favorites") {
                    photo.copy(isFavorite = false)
                } else {
                    photo.copy(collections = photo.collections - box)
                }
            } else photo
        }
    }

    fun hasBoxEmoji(box: String, boxIcons: Map<String, String>): Boolean {
        if (box == "Box 1" || box == "Favorites") return true
        val iconName = boxIcons[box] ?: return false
        return iconName.isNotBlank() && iconName.none { it in 'a'..'z' || it in 'A'..'Z' }
    }

    fun getBoxIcon(
        box: String,
        editingBoxEmoji: String?,
        editingEmojiPreview: String?,
        boxIcons: Map<String, String>
    ): Any {
        if (box == "Box 1" || box == "Favorites") {
            if ((box == editingBoxEmoji || editingBoxEmoji == "Box 1" || editingBoxEmoji == "Favorites") && editingEmojiPreview != null) {
                if (editingEmojiPreview.isNotBlank() && editingEmojiPreview.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                    return editingEmojiPreview
                }
            }
            val custom = boxIcons["Box 1"] ?: boxIcons["Favorites"]
            return if (custom != null && custom.isNotBlank() && custom != "Box 1" && custom != "Favorites" && custom.none { it in 'a'..'z' || it in 'A'..'Z' }) custom else "❤️"
        }

        val iconName = if (box == editingBoxEmoji && editingEmojiPreview != null) {
            editingEmojiPreview
        } else {
            boxIcons[box] ?: ""
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
                R.drawable.ic_box
            } else if (iconName.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                iconName
            } else {
                R.drawable.ic_box
            }
        }
    }
}
