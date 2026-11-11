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
import kotlinx.coroutines.withContext
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
    var activePhotoId by mutableStateOf<String?>(null)

    val currentPhoto: Photo? by derivedStateOf {
        val id = activePhotoId
        if (id != null) {
            photos.find { it.id == id } ?: activeMemoriesSnapshot.firstOrNull { it.id == id }
        } else {
            activeMemoriesSnapshot.firstOrNull()
                ?: curatedMemories.getOrNull(currentMemoryIndex)
                ?: photos.firstOrNull()
        }
    }

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
        val target = memories.getOrNull(index) ?: memories.firstOrNull() ?: return
        activePhotoId = target.id
        activeMemoriesSnapshot = listOf(target)
        currentMemoryIndex = 0
        MemoryWorker.isInMemoryScreen = true
        MemoryWorker.cancelSummaryNotification(getApplication())
    }

    fun openMemory(index: Int) {
        val target = curatedMemories.getOrNull(index) ?: photos.getOrNull(index)
        if (target != null) {
            openMemory(listOf(target), 0)
        }
    }

    fun openMemoryForPhoto(photoId: String) {
        activePhotoId = photoId
        val targetPhoto = photos.find { it.id == photoId }
        if (targetPhoto != null) {
            activeMemoriesSnapshot = listOf(targetPhoto)
            currentMemoryIndex = 0
        }
        MemoryWorker.isInMemoryScreen = true
        MemoryWorker.cancelSummaryNotification(getApplication())
    }

    fun onMemoryViewed(index: Int) {
        val photo = currentPhoto ?: return
        markAsViewed(photo.id)
    }

    fun onMemoryViewed(photoId: String) {
        markAsViewed(photoId)
    }

    private fun markAsViewed(id: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable).launch {
            val now = System.currentTimeMillis()
            val existing = photos.find { it.id == id } ?: photoRepository.getPhotoById(id) ?: return@launch
            val safeLastViewed = maxOf(now, existing.snippetsAddedTime)

            if (existing.isViewed && existing.lastViewedTime >= safeLastViewed) {
                return@launch
            }

            val updatedPhoto = existing.copy(isViewed = true, lastViewedTime = safeLastViewed)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                photos = photos.map { if (it.id == id) updatedPhoto else it }
                if (activeMemoriesSnapshot.any { it.id == id }) {
                    activeMemoriesSnapshot = activeMemoriesSnapshot.map { if (it.id == id) updatedPhoto else it }
                }
            }

            photoRepository.updatePhoto(updatedPhoto)
            MemoryWorker.cancelPostedNotification(getApplication(), id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        MemoryWorker.isInMemoryScreen = false
    }
}
