package com.android.snippets.logic

import com.android.snippets.model.Photo

data class SnippetUpdateResult(
    val updatedPhotos: List<Photo>,
    val updatedFirstSeen: Map<String, Long>,
    val isFirstTimeAdded: Boolean,
    val wasAppEmptyBefore: Boolean
)

data class SnippetRemoveResult(
    val updatedPhotos: List<Photo>,
    val updatedFirstSeen: Map<String, Long>,
    val updatedFilterSnippets: List<String>,
    val emptiedMemory: Boolean,
    val filtersChanged: Boolean
)

data class SnippetGlobalDeleteResult(
    val updatedPhotos: List<Photo>,
    val updatedFirstSeen: Map<String, Long>,
    val emptiedMemoryIds: List<String>
)

data class BulkAddSnippetResult(
    val updatedPhotos: List<Photo>,
    val updatedFirstSeen: Map<String, Long>,
    val skippedCount: Int,
    val addedCount: Int
)

data class BulkRemoveSnippetResult(
    val updatedPhotos: List<Photo>,
    val updatedFirstSeen: Map<String, Long>,
    val updatedFilterSnippets: List<String>,
    val emptiedMemoryIds: List<String>,
    val filtersChanged: Boolean
)

data class BulkClearSnippetResult(
    val updatedPhotos: List<Photo>,
    val emptiedMemoryIds: List<String>,
    val remainingSnippets: Set<String>
)

object SnippetManager {

    const val MAX_SNIPPETS_PER_PHOTO = 6
    const val SNACKBAR_NAME_PHOTO_FIRST = "Name the photo first!"
    const val SNACKBAR_ACTION_OPEN = "Open"

    fun validateAndExecuteAddSnippet(
        photo: Photo,
        showSnackbar: (message: String, actionLabel: String?, onAction: (() -> Unit)?) -> Unit,
        onShowNameModal: (Photo) -> Unit,
        onShowAddModal: (Photo) -> Unit
    ) {
        if (photo.name.isNullOrBlank()) {
            showSnackbar(
                SNACKBAR_NAME_PHOTO_FIRST,
                SNACKBAR_ACTION_OPEN
            ) {
                onShowNameModal(photo)
            }
        } else {
            onShowAddModal(photo)
        }
    }

    fun getMatchingSnippetsCount(photo: Photo, selectedFilterSnippets: List<String>): Int {
        return if (selectedFilterSnippets.isNotEmpty()) {
            photo.snippets.count { s -> selectedFilterSnippets.any { it.equals(s, ignoreCase = true) } }
        } else {
            photo.snippets.size
        }
    }

    fun updateSnippets(
        photos: List<Photo>,
        snippetFirstSeenTimes: Map<String, Long>,
        photoId: String,
        text: String,
        now: Long = System.currentTimeMillis()
    ): SnippetUpdateResult? {
        val targetPhoto = photos.find { it.id == photoId }
        if (targetPhoto != null && targetPhoto.snippets.size >= MAX_SNIPPETS_PER_PHOTO) {
            return null
        }

        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return null

        val newSnippets = listOf(trimmedText)
        var firstTimeSnippetsAdded = false
        val wasAppEmptyBefore = photos.all { it.snippets.isEmpty() }

        val newFirstSeen = if (!snippetFirstSeenTimes.containsKey(trimmedText)) {
            snippetFirstSeenTimes + (trimmedText to now)
        } else {
            snippetFirstSeenTimes
        }

        val updatedPhotos = photos.map {
            if (it.id == photoId) {
                val wasEmpty = it.snippets.isEmpty()
                if (wasEmpty && newSnippets.isNotEmpty()) {
                    firstTimeSnippetsAdded = true
                }
                val updatedSnippets = (it.snippets + newSnippets).distinct()
                it.copy(
                    snippets = updatedSnippets,
                    snippetsAddedTime = if (newSnippets.isNotEmpty()) now else it.snippetsAddedTime,
                    surfacedTime = 0L
                )
            } else it
        }

        return SnippetUpdateResult(
            updatedPhotos = updatedPhotos,
            updatedFirstSeen = newFirstSeen,
            isFirstTimeAdded = firstTimeSnippetsAdded,
            wasAppEmptyBefore = wasAppEmptyBefore
        )
    }

    fun removeSnippet(
        photos: List<Photo>,
        snippetFirstSeenTimes: Map<String, Long>,
        selectedFilterSnippets: List<String>,
        photoId: String,
        snippet: String
    ): SnippetRemoveResult {
        var emptiedMemory = false
        val updatedPhotos = photos.map {
            if (it.id == photoId) {
                val updatedSnippets = it.snippets.filter { s -> s != snippet }
                emptiedMemory = it.snippets.isNotEmpty() && updatedSnippets.isEmpty()
                it.copy(
                    snippets = updatedSnippets,
                    snippetsAddedTime = if (updatedSnippets.isEmpty()) 0L else it.snippetsAddedTime
                )
            } else it
        }

        val updatedFirstSeen = if (updatedPhotos.none { photo -> photo.snippets.contains(snippet) }) {
            snippetFirstSeenTimes - snippet
        } else {
            snippetFirstSeenTimes
        }

        val filtersChanged = selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
        val updatedFilters = if (filtersChanged) {
            selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
        } else {
            selectedFilterSnippets
        }

        return SnippetRemoveResult(
            updatedPhotos = updatedPhotos,
            updatedFirstSeen = updatedFirstSeen,
            updatedFilterSnippets = updatedFilters,
            emptiedMemory = emptiedMemory,
            filtersChanged = filtersChanged
        )
    }

    fun deleteSnippetGlobally(
        photos: List<Photo>,
        snippetFirstSeenTimes: Map<String, Long>,
        snippet: String
    ): SnippetGlobalDeleteResult {
        val emptiedMemoryIds = mutableListOf<String>()
        val updatedPhotos = photos.map {
            val updatedSnippets = it.snippets.filter { s -> s != snippet }
            if (it.snippets.isNotEmpty() && updatedSnippets.isEmpty()) {
                emptiedMemoryIds.add(it.id)
            }
            it.copy(
                snippets = updatedSnippets,
                snippetsAddedTime = if (updatedSnippets.isEmpty()) 0L else it.snippetsAddedTime
            )
        }

        val updatedFirstSeen = snippetFirstSeenTimes - snippet
        return SnippetGlobalDeleteResult(
            updatedPhotos = updatedPhotos,
            updatedFirstSeen = updatedFirstSeen,
            emptiedMemoryIds = emptiedMemoryIds
        )
    }

    fun bulkAddSnippet(
        photos: List<Photo>,
        snippetFirstSeenTimes: Map<String, Long>,
        photoIds: Set<String>,
        text: String,
        now: Long = System.currentTimeMillis()
    ): BulkAddSnippetResult? {
        val trimmedText = text.trim()
        if (trimmedText.isBlank() || photoIds.isEmpty()) return null

        val updatedFirstSeen = if (!snippetFirstSeenTimes.containsKey(trimmedText)) {
            snippetFirstSeenTimes + (trimmedText to now)
        } else {
            snippetFirstSeenTimes
        }

        var skippedCount = 0
        val updatedPhotos = photos.map { photo ->
            if (photoIds.contains(photo.id)) {
                if (photo.snippets.size >= MAX_SNIPPETS_PER_PHOTO && !photo.snippets.contains(trimmedText)) {
                    skippedCount++
                    photo
                } else {
                    val updatedSnippets = (photo.snippets + trimmedText).distinct()
                    photo.copy(
                        snippets = updatedSnippets,
                        snippetsAddedTime = now,
                        surfacedTime = 0L
                    )
                }
            } else photo
        }

        return BulkAddSnippetResult(
            updatedPhotos = updatedPhotos,
            updatedFirstSeen = updatedFirstSeen,
            skippedCount = skippedCount,
            addedCount = photoIds.size
        )
    }

    fun bulkRemoveSnippet(
        photos: List<Photo>,
        snippetFirstSeenTimes: Map<String, Long>,
        selectedFilterSnippets: List<String>,
        photoIds: Set<String>,
        snippet: String
    ): BulkRemoveSnippetResult? {
        if (photoIds.isEmpty() || snippet.isBlank()) return null
        val trimmedSnippet = snippet.trim()

        val emptiedMemoryIds = mutableListOf<String>()
        val updatedPhotos = photos.map { photo ->
            if (photoIds.contains(photo.id)) {
                val updatedSnippets = photo.snippets.filter { s -> s != trimmedSnippet }
                if (photo.snippets.isNotEmpty() && updatedSnippets.isEmpty()) {
                    emptiedMemoryIds.add(photo.id)
                }
                photo.copy(
                    snippets = updatedSnippets,
                    snippetsAddedTime = if (updatedSnippets.isEmpty()) 0L else photo.snippetsAddedTime
                )
            } else photo
        }

        val updatedFirstSeen = if (updatedPhotos.none { photo -> photo.snippets.contains(trimmedSnippet) }) {
            snippetFirstSeenTimes - trimmedSnippet
        } else {
            snippetFirstSeenTimes
        }

        val filtersChanged = selectedFilterSnippets.any { it.equals(trimmedSnippet, ignoreCase = true) }
        val updatedFilters = if (filtersChanged) {
            selectedFilterSnippets.filter { !it.equals(trimmedSnippet, ignoreCase = true) }
        } else {
            selectedFilterSnippets
        }

        return BulkRemoveSnippetResult(
            updatedPhotos = updatedPhotos,
            updatedFirstSeen = updatedFirstSeen,
            updatedFilterSnippets = updatedFilters,
            emptiedMemoryIds = emptiedMemoryIds,
            filtersChanged = filtersChanged
        )
    }

    fun bulkClearSnippets(
        photos: List<Photo>,
        photoIds: Set<String>
    ): BulkClearSnippetResult {
        val emptiedMemoryIds = mutableListOf<String>()
        val updatedPhotos = photos.map { photo ->
            if (photoIds.contains(photo.id)) {
                emptiedMemoryIds.add(photo.id)
                photo.copy(
                    snippets = emptyList(),
                    snippetsAddedTime = 0L
                )
            } else photo
        }

        val remainingSnippets = updatedPhotos.flatMap { it.snippets }.toSet()

        return BulkClearSnippetResult(
            updatedPhotos = updatedPhotos,
            emptiedMemoryIds = emptiedMemoryIds,
            remainingSnippets = remainingSnippets
        )
    }

    fun extractFavoritesUniqueSnippets(photos: List<Photo>): List<String> {
        val set = LinkedHashSet<String>()
        for (i in photos.indices) {
            val p = photos[i]
            if (p.isFavorite) {
                val snips = p.snippets
                for (j in snips.indices) set.add(snips[j])
            }
        }
        return set.toList()
    }

    fun extractAllUniqueSnippets(
        photos: List<Photo>,
        snippetSortType: com.android.snippets.viewmodel.SnippetSortType,
        snippetFirstSeenTimes: Map<String, Long>
    ): List<String> {
        val baseSnippets = LinkedHashSet<String>()
        for (i in photos.indices) {
            val snips = photos[i].snippets
            for (j in snips.indices) baseSnippets.add(snips[j])
        }

        val filtered = when (snippetSortType) {
            com.android.snippets.viewmodel.SnippetSortType.Emoji -> baseSnippets.filter { SnippetStyleHelper.containsEmoji(it) }
            com.android.snippets.viewmodel.SnippetSortType.Emoticons -> baseSnippets.filter { SnippetStyleHelper.containsEmoticon(it) }
            else -> baseSnippets.toList()
        }

        return when (snippetSortType) {
            com.android.snippets.viewmodel.SnippetSortType.New -> filtered.sortedByDescending { snippetFirstSeenTimes[it] ?: 0L }
            com.android.snippets.viewmodel.SnippetSortType.Old -> filtered.sortedBy { snippetFirstSeenTimes[it] ?: 0L }
            com.android.snippets.viewmodel.SnippetSortType.AZ -> filtered.sortedWith(String.CASE_INSENSITIVE_ORDER)
            com.android.snippets.viewmodel.SnippetSortType.Emoji, com.android.snippets.viewmodel.SnippetSortType.Emoticons -> filtered.sorted()
        }
    }

    fun rebuildSnippetFirstSeenTimes(source: List<Photo>): Map<String, Long> {
        val rebuilt = mutableMapOf<String, Long>()
        source.forEach { photo ->
            val time = if (photo.snippetsAddedTime != 0L) photo.snippetsAddedTime else photo.date
            photo.snippets.forEach { snippet ->
                val existing = rebuilt[snippet]
                if (existing == null || time < existing) {
                    rebuilt[snippet] = time
                }
            }
        }
        return rebuilt
    }
}
