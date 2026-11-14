package com.android.snippets.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.logic.SnippetManager
import com.android.snippets.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set

    var themePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set

    var useDynamicColors by mutableStateOf(true)
        private set

    var snippetFirstSeenTimes by mutableStateOf<Map<String, Long>>(emptyMap())
        private set

    private var storedFirstSeenJson: String = "{}"

    init {
        viewModelScope.launch {
            photoRepository.allPhotosFlow.collectLatest {
                photos = it
                updateSnippetFirstSeen()
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
                storedFirstSeenJson = prefs.snippetFirstSeenJson
                updateSnippetFirstSeen()
            }
        }
    }

    private fun updateSnippetFirstSeen() {
        val currentPhotos = photos
        val jsonStr = storedFirstSeenJson
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val loadedFirstSeen = if (jsonStr.isNotBlank() && jsonStr != "{}") {
                try {
                    json.decodeFromString<Map<String, Long>>(jsonStr)
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
            val rebuiltFirstSeen = SnippetManager.rebuildSnippetFirstSeenTimes(currentPhotos)
            val combined = rebuiltFirstSeen.mapValues { (snippet, rebuiltTime) ->
                loadedFirstSeen[snippet] ?: rebuiltTime
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                snippetFirstSeenTimes = combined
            }
        }
    }
}
