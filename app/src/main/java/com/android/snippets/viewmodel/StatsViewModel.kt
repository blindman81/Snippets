package com.android.snippets.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set

    var themePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set

    var useDynamicColors by mutableStateOf(true)
        private set

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
}
