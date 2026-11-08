package com.android.snippets.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.backup.SnippetsBackupManager
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.CollectionRepository
import com.android.snippets.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: SnippetsBackupManager
) : AndroidViewModel(application) {

    var themePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set
    var useDynamicColors by mutableStateOf(true)
        private set
    var expandLabelIcon by mutableStateOf(false)
        private set
    var expandCalendarIcon by mutableStateOf(false)
        private set
    var expandLocationIcon by mutableStateOf(false)
        private set
    var autoBackupSchedule by mutableStateOf("Disabled")
        private set

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collectLatest { prefs ->
                themePreference = when (prefs.themePreference.uppercase()) {
                    "LIGHT" -> ThemePreference.LIGHT
                    "DARK" -> ThemePreference.DARK
                    else -> ThemePreference.SYSTEM
                }
                useDynamicColors = prefs.useDynamicColors
                expandLabelIcon = prefs.expandLabelIcon
                expandCalendarIcon = prefs.expandCalendarIcon
                expandLocationIcon = prefs.expandLocationIcon
                autoBackupSchedule = prefs.autoBackupSchedule
            }
        }
    }

    fun updateThemePreference(pref: ThemePreference) {
        themePreference = pref
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(pref.name)
        }
    }

    fun updateExpandLabelIcon(expand: Boolean) {
        expandLabelIcon = expand
        viewModelScope.launch {
            userPreferencesRepository.updateExpandLabelIcon(expand)
        }
    }

    fun updateExpandCalendarIcon(expand: Boolean) {
        expandCalendarIcon = expand
        viewModelScope.launch {
            userPreferencesRepository.updateExpandCalendarIcon(expand)
        }
    }

    fun updateExpandLocationIcon(expand: Boolean) {
        expandLocationIcon = expand
        viewModelScope.launch {
            userPreferencesRepository.updateExpandLocationIcon(expand)
        }
    }

    fun updateAutoBackupSchedule(schedule: String) {
        autoBackupSchedule = schedule
        viewModelScope.launch {
            userPreferencesRepository.updateAutoBackupSchedule(schedule)
            com.android.snippets.logic.AutoBackupWorker.schedule(getApplication(), schedule)
        }
    }

    fun exportBackupToFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val photos = photoRepository.getAllPhotos()
                val collections = collectionRepository.getAllCollections()
                val userCollections = collections.map { it.name }
                val collectionIcons = collections.associate { it.name to (it.icon ?: "") }
                val snippetFirstSeenTimes = SnippetsViewModel.rebuildSnippetFirstSeenTimes(photos)

                backupManager.exportBackup(
                    context = context,
                    uri = uri,
                    photos = photos,
                    userCollections = userCollections,
                    collectionIcons = collectionIcons,
                    snippetFirstSeenTimes = snippetFirstSeenTimes
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup file exported successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun restoreBackupFromFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                backupManager.importBackup(context, uri)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importBackupFromFile(context: Context, uri: Uri) = restoreBackupFromFile(context, uri)
}
