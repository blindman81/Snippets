package com.android.snippets.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.logic.MemoryManager
import com.android.snippets.logic.MemoryWorker
import com.android.snippets.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    application: Application,
    private val photoRepository: PhotoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set

    var themePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set

    var useDynamicColors by mutableStateOf(true)
        private set

    var currentMemoryIndex by mutableStateOf(0)
    var activeMemoriesSnapshot by mutableStateOf<List<Photo>>(emptyList())

    val curatedMemories: List<Photo> by derivedStateOf {
        MemoryManager.getCuratedMemories(photos)
    }

    val historyMemories: List<Photo> by derivedStateOf {
        MemoryManager.getHistoryMemories(photos)
    }

    val hasUnviewedMemories: Boolean by derivedStateOf {
        MemoryManager.hasUnviewedMemories(curatedMemories)
    }

    init {
        viewModelScope.launch {
            photoRepository.allPhotosFlow.collectLatest {
                photos = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collectLatest { prefs ->
                themePreference = when (prefs.themePreference.uppercase()) {
                    "LIGHT" -> ThemePreference.LIGHT
                    "DARK" -> ThemePreference.DARK
                    else -> ThemePreference.SYSTEM
                }
                useDynamicColors = prefs.useDynamicColors
            }
        }
    }

    fun openMemory(memories: List<Photo>, index: Int) {
        if (memories.isEmpty()) return
        activeMemoriesSnapshot = memories
        currentMemoryIndex = index.coerceIn(0, activeMemoriesSnapshot.size - 1)
        MemoryWorker.isInMemoryScreen = true
        MemoryWorker.cancelSummaryNotification(getApplication())
    }

    fun openMemory(index: Int) {
        openMemory(curatedMemories, index)
    }

    fun openMemoryForPhoto(photoId: String) {
        val targetPhoto = photos.find { it.id == photoId } ?: return
        val index = curatedMemories.indexOfFirst { it.id == photoId }
        if (index != -1) {
            openMemory(index)
            return
        }
        val combined = (listOf(targetPhoto) + curatedMemories).sortedByDescending { it.date }
        val newIndex = combined.indexOfFirst { it.id == photoId }.coerceAtLeast(0)
        openMemory(combined, newIndex)
    }

    fun onMemoryViewed(index: Int) {
        val list = activeMemoriesSnapshot.ifEmpty { curatedMemories }
        if (list.isEmpty()) return
        currentMemoryIndex = index.coerceIn(0, list.size - 1)
        list.getOrNull(currentMemoryIndex)?.let { markAsViewed(it.id) }
    }

    fun onMemoryViewed(photoId: String) {
        val list = activeMemoriesSnapshot.ifEmpty { curatedMemories }
        val index = list.indexOfFirst { it.id == photoId }
        if (index != -1) {
            currentMemoryIndex = index
        }
        markAsViewed(photoId)
    }

    private fun markAsViewed(id: String) {
        val now = System.currentTimeMillis()
        val photo = photos.find { it.id == id } ?: return

        if (photo.isViewed && photo.snippetsAddedTime <= photo.lastViewedTime) {
            return
        }

        val updatedPhoto = photo.copy(isViewed = true, lastViewedTime = now)
        photos = photos.map { if (it.id == id) updatedPhoto else it }
        viewModelScope.launch {
            photoRepository.updatePhoto(updatedPhoto)
        }

        MemoryWorker.cancelPostedNotification(getApplication(), id)
    }
}
