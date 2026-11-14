package com.android.snippets.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.logic.ExportBackupWorker
import com.android.snippets.logic.RestoreBackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
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
        val appContext = context.applicationContext
        appContext.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val request = OneTimeWorkRequestBuilder<ExportBackupWorker>()
            .setInputData(workDataOf(ExportBackupWorker.OUTPUT_URI to uri.toString()))
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            ExportBackupWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    fun restoreBackupFromFile(context: Context, uri: Uri) {
        val appContext = context.applicationContext
        appContext.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val request = OneTimeWorkRequestBuilder<RestoreBackupWorker>()
            .setInputData(workDataOf(RestoreBackupWorker.INPUT_URI to uri.toString()))
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            RestoreBackupWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    fun importBackupFromFile(context: Context, uri: Uri) = restoreBackupFromFile(context, uri)
}
