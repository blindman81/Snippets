package com.android.snippets.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val USER_PREFERENCES_NAME = "snippets_prefs"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, USER_PREFERENCES_NAME),
            SharedPreferencesMigration(context, "memory_notification_state")
        )
    }
)

/**
 * Data class representing the full set of user preferences for Snippets.
 */
data class UserPreferences(
    val themePreference: String = "SYSTEM",
    val useDynamicColors: Boolean = true,
    val expandLabelIcon: Boolean = false,
    val expandCalendarIcon: Boolean = false,
    val expandLocationIcon: Boolean = false,
    val autoBackupSchedule: String = "Disabled",
    val notificationReminderEnabled: Boolean = false,
    val notificationReminderHour: Int = 9,
    val notificationReminderMinute: Int = 0,
    val snippetSortType: String = "New",
    val isSortAscending: Boolean = true,
    val selectedFilterSnippets: Set<String> = emptySet(),
    val displayMode: String = "Day",
    val collectionDisplayModesJson: String = "{}",
    val photoSortType: String = "DateNewest",
    val collectionPhotoSortTypesJson: String = "{}",
    val lastSeenYearlyRecapYear: Int = -1,
    val lastSeenMonthlyRecapPeriod: String = "",
    val snippetFirstSeenJson: String = "{}",
    val memoryPostedNotificationIds: Set<String> = emptySet()
)

/**
 * Repository that manages user preferences using Jetpack Preferences DataStore.
 * Migrates existing data from SharedPreferences ("snippets_prefs") automatically.
 */
class UserPreferencesRepository(private val context: Context) {

    val dataStore: DataStore<Preferences> = context.dataStore

    object PreferencesKeys {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val EXPAND_LABEL_ICON = booleanPreferencesKey("expand_label_icon")
        val EXPAND_PHOTO_NAME_ICON = booleanPreferencesKey("expand_photo_name_icon")
        val EXPAND_CALENDAR_ICON = booleanPreferencesKey("expand_calendar_icon")
        val EXPAND_DAY_AND_DATE_ICON = booleanPreferencesKey("expand_day_and_date_icon")
        val EXPAND_LOCATION_ICON = booleanPreferencesKey("expand_location_icon")
        val AUTO_BACKUP_SCHEDULE = stringPreferencesKey("auto_backup_schedule")
        val NOTIFICATION_REMINDER_ENABLED = booleanPreferencesKey("notification_reminder_enabled")
        val NOTIFICATION_REMINDER_HOUR = intPreferencesKey("notification_reminder_hour")
        val NOTIFICATION_REMINDER_MINUTE = intPreferencesKey("notification_reminder_minute")
        val SNIPPET_SORT_TYPE = stringPreferencesKey("snippet_sort_type")
        val IS_SORT_ASCENDING = booleanPreferencesKey("is_sort_ascending")
        val SELECTED_FILTER_SNIPPETS = stringSetPreferencesKey("selected_filter_snippets")
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
        val COLLECTION_DISPLAY_MODES_JSON = stringPreferencesKey("collection_display_modes_json")
        val PHOTO_SORT_TYPE = stringPreferencesKey("photo_sort_type")
        val COLLECTION_PHOTO_SORT_TYPES_JSON = stringPreferencesKey("collection_photo_sort_types_json")
        val LAST_SEEN_YEARLY_RECAP_YEAR = intPreferencesKey("last_seen_yearly_recap_year")
        val LAST_SEEN_MONTHLY_RECAP_PERIOD = stringPreferencesKey("last_seen_monthly_recap_period")
        val SNIPPET_FIRST_SEEN_JSON = stringPreferencesKey("snippet_first_seen_json")
        val MEMORY_POSTED_IDS = stringSetPreferencesKey("posted_ids")
    }

    /**
     * Flow emitting [UserPreferences] whenever stored preferences change.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }

    val themePreferenceFlow: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.THEME_PREFERENCE] ?: "SYSTEM" }

    val useDynamicColorsFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true }

    val autoBackupScheduleFlow: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.AUTO_BACKUP_SCHEDULE] ?: "Disabled" }

    val memoryPostedNotificationIdsFlow: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.MEMORY_POSTED_IDS] ?: emptySet() }

    suspend fun getMemoryPostedIds(): Set<String> {
        return dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .first()[PreferencesKeys.MEMORY_POSTED_IDS] ?: emptySet()
    }

    suspend fun addMemoryPostedId(photoId: String): Set<String> {
        var updated = emptySet<String>()
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.MEMORY_POSTED_IDS] ?: emptySet()
            updated = current + photoId
            preferences[PreferencesKeys.MEMORY_POSTED_IDS] = updated
        }
        return updated
    }

    suspend fun removeMemoryPostedId(photoId: String): Set<String> {
        var updated = emptySet<String>()
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.MEMORY_POSTED_IDS] ?: emptySet()
            updated = current - photoId
            preferences[PreferencesKeys.MEMORY_POSTED_IDS] = updated
        }
        return updated
    }

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val expandLabel = preferences[PreferencesKeys.EXPAND_LABEL_ICON]
            ?: preferences[PreferencesKeys.EXPAND_PHOTO_NAME_ICON] ?: false
        val expandCalendar = preferences[PreferencesKeys.EXPAND_CALENDAR_ICON]
            ?: preferences[PreferencesKeys.EXPAND_DAY_AND_DATE_ICON] ?: false

        return UserPreferences(
            themePreference = preferences[PreferencesKeys.THEME_PREFERENCE] ?: "SYSTEM",
            useDynamicColors = preferences[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true,
            expandLabelIcon = expandLabel,
            expandCalendarIcon = expandCalendar,
            expandLocationIcon = preferences[PreferencesKeys.EXPAND_LOCATION_ICON] ?: false,
            autoBackupSchedule = preferences[PreferencesKeys.AUTO_BACKUP_SCHEDULE] ?: "Disabled",
            notificationReminderEnabled = preferences[PreferencesKeys.NOTIFICATION_REMINDER_ENABLED] ?: false,
            notificationReminderHour = preferences[PreferencesKeys.NOTIFICATION_REMINDER_HOUR] ?: 9,
            notificationReminderMinute = preferences[PreferencesKeys.NOTIFICATION_REMINDER_MINUTE] ?: 0,
            snippetSortType = preferences[PreferencesKeys.SNIPPET_SORT_TYPE] ?: "New",
            isSortAscending = preferences[PreferencesKeys.IS_SORT_ASCENDING] ?: true,
            selectedFilterSnippets = preferences[PreferencesKeys.SELECTED_FILTER_SNIPPETS] ?: emptySet(),
            displayMode = preferences[PreferencesKeys.DISPLAY_MODE] ?: "Day",
            collectionDisplayModesJson = preferences[PreferencesKeys.COLLECTION_DISPLAY_MODES_JSON] ?: "{}",
            photoSortType = preferences[PreferencesKeys.PHOTO_SORT_TYPE] ?: "DateNewest",
            collectionPhotoSortTypesJson = preferences[PreferencesKeys.COLLECTION_PHOTO_SORT_TYPES_JSON] ?: "{}",
            lastSeenYearlyRecapYear = preferences[PreferencesKeys.LAST_SEEN_YEARLY_RECAP_YEAR] ?: -1,
            lastSeenMonthlyRecapPeriod = preferences[PreferencesKeys.LAST_SEEN_MONTHLY_RECAP_PERIOD] ?: "",
            snippetFirstSeenJson = preferences[PreferencesKeys.SNIPPET_FIRST_SEEN_JSON] ?: "{}",
            memoryPostedNotificationIds = preferences[PreferencesKeys.MEMORY_POSTED_IDS] ?: emptySet()
        )
    }

    suspend fun updateThemePreference(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] = theme
        }
    }

    suspend fun updateUseDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = useDynamicColors
        }
    }

    suspend fun updateExpandLabelIcon(expand: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPAND_LABEL_ICON] = expand
            preferences[PreferencesKeys.EXPAND_PHOTO_NAME_ICON] = expand
        }
    }

    suspend fun updateExpandCalendarIcon(expand: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPAND_CALENDAR_ICON] = expand
            preferences[PreferencesKeys.EXPAND_DAY_AND_DATE_ICON] = expand
        }
    }

    suspend fun updateExpandLocationIcon(expand: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPAND_LOCATION_ICON] = expand
        }
    }

    suspend fun updateAutoBackupSchedule(schedule: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP_SCHEDULE] = schedule
        }
    }

    suspend fun updateNotificationReminder(enabled: Boolean, hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.NOTIFICATION_REMINDER_HOUR] = hour
            preferences[PreferencesKeys.NOTIFICATION_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateFilterState(
        snippetSortType: String,
        isSortAscending: Boolean,
        selectedFilterSnippets: Set<String>,
        displayMode: String,
        collectionDisplayModesJson: String,
        photoSortType: String,
        collectionPhotoSortTypesJson: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNIPPET_SORT_TYPE] = snippetSortType
            preferences[PreferencesKeys.IS_SORT_ASCENDING] = isSortAscending
            preferences[PreferencesKeys.SELECTED_FILTER_SNIPPETS] = selectedFilterSnippets
            preferences[PreferencesKeys.DISPLAY_MODE] = displayMode
            preferences[PreferencesKeys.COLLECTION_DISPLAY_MODES_JSON] = collectionDisplayModesJson
            preferences[PreferencesKeys.PHOTO_SORT_TYPE] = photoSortType
            preferences[PreferencesKeys.COLLECTION_PHOTO_SORT_TYPES_JSON] = collectionPhotoSortTypesJson
        }
    }

    suspend fun updateLastSeenYearlyRecapYear(year: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEEN_YEARLY_RECAP_YEAR] = year
        }
    }

    suspend fun updateLastSeenMonthlyRecapPeriod(period: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEEN_MONTHLY_RECAP_PERIOD] = period
        }
    }

    suspend fun updateSnippetFirstSeenJson(json: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNIPPET_FIRST_SEEN_JSON] = json
        }
    }

    suspend fun restorePreferences(settings: Map<String, Any>) {
        dataStore.edit { preferences ->
            (settings["theme_preference"] as? String)?.let { preferences[PreferencesKeys.THEME_PREFERENCE] = it }
            (settings["use_dynamic_colors"] as? Boolean)?.let { preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = it }
            ((settings["expand_label_icon"] ?: settings["expand_photo_name_icon"]) as? Boolean)?.let {
                preferences[PreferencesKeys.EXPAND_LABEL_ICON] = it
                preferences[PreferencesKeys.EXPAND_PHOTO_NAME_ICON] = it
            }
            ((settings["expand_calendar_icon"] ?: settings["expand_day_and_date_icon"]) as? Boolean)?.let {
                preferences[PreferencesKeys.EXPAND_CALENDAR_ICON] = it
                preferences[PreferencesKeys.EXPAND_DAY_AND_DATE_ICON] = it
            }
            (settings["expand_location_icon"] as? Boolean)?.let { preferences[PreferencesKeys.EXPAND_LOCATION_ICON] = it }
            (settings["auto_backup_schedule"] as? String)?.let { preferences[PreferencesKeys.AUTO_BACKUP_SCHEDULE] = it }
            (settings["notification_reminder_enabled"] as? Boolean)?.let { preferences[PreferencesKeys.NOTIFICATION_REMINDER_ENABLED] = it }
            (settings["notification_reminder_hour"] as? Number)?.let { preferences[PreferencesKeys.NOTIFICATION_REMINDER_HOUR] = it.toInt() }
            (settings["notification_reminder_minute"] as? Number)?.let { preferences[PreferencesKeys.NOTIFICATION_REMINDER_MINUTE] = it.toInt() }
            (settings["snippet_sort_type"] as? String)?.let { preferences[PreferencesKeys.SNIPPET_SORT_TYPE] = it }
            (settings["is_sort_ascending"] as? Boolean)?.let { preferences[PreferencesKeys.IS_SORT_ASCENDING] = it }
            (settings["selected_filter_snippets"] as? Set<*>)
                ?.filterIsInstance<String>()
                ?.toSet()
                ?.let { preferences[PreferencesKeys.SELECTED_FILTER_SNIPPETS] = it }
            (settings["display_mode"] as? String)?.let { preferences[PreferencesKeys.DISPLAY_MODE] = it }
            (settings["collection_display_modes_json"] as? String)?.let { preferences[PreferencesKeys.COLLECTION_DISPLAY_MODES_JSON] = it }
            (settings["photo_sort_type"] as? String)?.let { preferences[PreferencesKeys.PHOTO_SORT_TYPE] = it }
            (settings["collection_photo_sort_types_json"] as? String)?.let { preferences[PreferencesKeys.COLLECTION_PHOTO_SORT_TYPES_JSON] = it }
            (settings["last_seen_yearly_recap_year"] as? Number)?.let { preferences[PreferencesKeys.LAST_SEEN_YEARLY_RECAP_YEAR] = it.toInt() }
            (settings["last_seen_monthly_recap_period"] as? String)?.let { preferences[PreferencesKeys.LAST_SEEN_MONTHLY_RECAP_PERIOD] = it }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
