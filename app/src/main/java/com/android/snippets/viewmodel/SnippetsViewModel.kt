package com.android.snippets.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.util.MediaSaver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.logic.MemoryWorker
import android.app.backup.BackupManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.widget.Toast
import android.graphics.BitmapFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen { Library, Detail, Memory, Settings, About, SelectIcon, Filter, PhotosCarousel, Stats }
enum class SnippetSortType { New, Old, AZ, Month, Year, Emoji, Emoticons, Favorites, Color, Style }
enum class PhotoSortType { DateNewest, DateOldest, MostSnippets, LeastSnippets }
enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class DisplayMode { Day, Week, Month }
enum class SnippetStyle { Default, Thin, Cursive, Mono, Serif, Spaced, Bold, FlexHeavy, FlexWide, FlexSlant, FlexGrade }


class SnippetsViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val MEMORY_REMINDER_PREFIX = "memory_reminder_"
        const val NEW_MEMORY_NOTIFICATION_DELAY_HOURS = 12L
        const val VIEWED_MEMORY_VISIBLE_HOURS = 24L
        const val RECENTLY_VIEWED_MEMORY_HOURS = 24L
        const val VIEWED_MEMORY_RESET_DAYS = 3L
        const val SURFACED_MEMORY_SPACING_HOURS = 3L
        const val TEST_NOTIFICATION_DELAY_SECONDS = 5L
        const val TEST_NOTIFICATION_PHOTO_ID = "test_notification"

        val NEW_MEMORY_WAIT_MS: Long = TimeUnit.HOURS.toMillis(NEW_MEMORY_NOTIFICATION_DELAY_HOURS)
        val VIEWED_MEMORY_VISIBLE_MS: Long = TimeUnit.HOURS.toMillis(VIEWED_MEMORY_VISIBLE_HOURS)
        val RECENTLY_VIEWED_MEMORY_MS: Long = TimeUnit.HOURS.toMillis(RECENTLY_VIEWED_MEMORY_HOURS)
        val VIEWED_MEMORY_RESET_MS: Long = TimeUnit.DAYS.toMillis(VIEWED_MEMORY_RESET_DAYS)
        val SURFACED_MEMORY_SPACING_MS: Long = TimeUnit.HOURS.toMillis(SURFACED_MEMORY_SPACING_HOURS)
    }

    private val prefs = application.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set

    var isInitialLoading by mutableStateOf(true)
        private set




    var userCollections by mutableStateOf<List<String>>(emptyList())
        private set

    var currentScreen by mutableStateOf(Screen.Library)
    var previousScreen by mutableStateOf(Screen.Library)
        private set

    
    var libraryCurrentTab by mutableStateOf("Library")
    val libraryListStates = androidx.compose.runtime.mutableStateMapOf<String, androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState>()
    
    var activePhotoId by mutableStateOf<String?>(null)
    var detailReturnScreen by mutableStateOf(Screen.Library)
    var filterReturnScreen by mutableStateOf(Screen.Library)
    var rotatingSearchHint by mutableStateOf("Library")
        private set
    var searchQuery by mutableStateOf("")
    var isSearchViewOpen by mutableStateOf(false)
    var recentSearches by mutableStateOf<List<String>>(emptyList())
        private set
    var showClearHistoryDialog by mutableStateOf(false)
    
    var isSearching by mutableStateOf(false)
        private set
    var isFiltering by mutableStateOf(false)
        private set
    var isBusy by mutableStateOf(false)
    var isAddingPhotos by mutableStateOf(false)
    var isCuratingMemories by mutableStateOf(false)

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        recentSearches = (listOf(trimmed) + recentSearches.filter { !it.equals(trimmed, ignoreCase = true) }).take(6)
        saveRecentSearches()
    }

    fun removeRecentSearch(query: String) {
        recentSearches = recentSearches.filter { !it.equals(query, ignoreCase = true) }
        saveRecentSearches()
    }

    fun clearRecentSearches() {
        recentSearches = emptyList()
        saveRecentSearches()
    }

    private fun saveRecentSearches() {
        val json = gson.toJson(recentSearches)
        prefs.edit().putString("recent_searches_json", json).apply()
    }

    private fun loadRecentSearches() {
        val json = prefs.getString("recent_searches_json", null)
        if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            recentSearches = gson.fromJson(json, type) ?: emptyList()
        }
    }
    var selectedFilterSnippets by mutableStateOf<List<String>>(emptyList())

    fun toggleFilterSnippet(snippet: String) {
        if (selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
        } else {
            if (selectedFilterSnippets.size >= 6) {
                showSnackbar("Upto 6 Chips!")
                return
            }
            selectedFilterSnippets = selectedFilterSnippets + snippet
        }
    }

    var activeCollection by mutableStateOf<String?>(null)
    var currentMemoryIndex by mutableStateOf(0)
    var activeMemoriesSnapshot by mutableStateOf<List<Photo>>(emptyList())
    var showCreateDialog by mutableStateOf(false)
    var editingCollectionIcon by mutableStateOf<String?>(null)
    var collectionIcons by mutableStateOf<Map<String, String>>(emptyMap())
    var focusCreateCollection by mutableStateOf(false)
    var memoriesPage by androidx.compose.runtime.mutableIntStateOf(0)
    var pinnedCollections by mutableStateOf<Set<String>>(emptySet())
        private set
    
    var snippetColors by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    
    var snippetStyles by mutableStateOf<Map<String, SnippetStyle>>(emptyMap())
        private set



    var snippetFirstSeenTimes by mutableStateOf<Map<String, Long>>(emptyMap())
        private set
    
    // Filter Context
    var filteringCategory by mutableStateOf<String?>(null)

    // Snackbar State
    var snackbarMessage by mutableStateOf<String?>(null)
    var snackbarActionLabel by mutableStateOf<String?>(null)
    var onSnackbarAction by mutableStateOf<(() -> Unit)?>(null)

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        snackbarMessage = message
        snackbarActionLabel = actionLabel
        onSnackbarAction = onAction
    }

    // Original collections order
    val sortedCollections: List<String>
        get() {
            return userCollections
        }
    
    // Settings State
    var themePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set
    var useDynamicColors by mutableStateOf(true)
        private set
    var showTimeInMemories by mutableStateOf(false)
        private set
    var selectedShape by mutableStateOf(AppShape.COOKIE_12_SIDED)
        private set
    var makePhotosFollowShape by mutableStateOf(false)
        private set
    var showFilterSheet by mutableStateOf(false)


    var showCarouselsIn by mutableStateOf<Set<String>>(
        prefs.getStringSet("show_carousels_in", null) ?: emptySet()
    )
        private set

    var searchHintsByTap by mutableStateOf(false)
        private set
    
    val separateCollectionSort = true
    
    private val collectionIconsFile = File(application.filesDir, "collection_icons_v2.json")
    
    fun navigateSelectIcon(collectionName: String) {
        editingCollectionIcon = collectionName
        previousScreen = currentScreen
        currentScreen = Screen.SelectIcon
    }



    fun navigatePhotosCarousel() {
        previousScreen = currentScreen
        currentScreen = Screen.PhotosCarousel
    }



    
    fun updateCollectionIcon(collectionName: String, iconOrEmoji: String) {
        collectionIcons = collectionIcons + (collectionName to iconOrEmoji)
        saveCollectionIcons()
    }
    
    private fun saveCollectionIcons() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val json = gson.toJson(collectionIcons)
                collectionIconsFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    var requestNotificationPermission by mutableStateOf(false)
    
    // Multi-Selection State
    var selectedPhotoIds by mutableStateOf<Set<String>>(emptySet())
        private set


    var forceSelectionMode by mutableStateOf(false)



    val isSelectionMode: Boolean
        get() = forceSelectionMode || selectedPhotoIds.isNotEmpty() || pendingCollectionAssignment != null || pendingCollectionRemoval != null

    var pendingCollectionAssignment by mutableStateOf<String?>(null)
        private set
    var pendingCollectionRemoval by mutableStateOf<String?>(null)
        private set

    var showCountsIn by mutableStateOf<Set<String>>(
        prefs.getStringSet("show_counts_in", null) ?: setOf("Favorites")
    )
    
    fun toggleCountVisibility(name: String) {
        showCountsIn = if (showCountsIn.contains(name)) {
            showCountsIn - name
        } else {
            showCountsIn + name
        }
        prefs.edit().putStringSet("show_counts_in", showCountsIn).apply()
    }



    fun toggleCarouselVisibility(name: String) {
        showCarouselsIn = if (showCarouselsIn.contains(name)) {
            showCarouselsIn - name
        } else {
            showCarouselsIn + name
        }
        prefs.edit().putStringSet("show_carousels_in", showCarouselsIn).apply()
    }

    private val categoryCounts by derivedStateOf {
        val counts = HashMap<String, Int>(userCollections.size + 2)
        counts["Library"] = photos.size
        var favoritesCount = 0
        
        val pList = photos
        for (i in pList.indices) {
            val photo = pList[i]
            if (photo.isFavorite) favoritesCount++
            val cols = photo.collections
            for (j in cols.indices) {
                val col = cols[j]
                counts[col] = (counts[col] ?: 0) + 1
            }
        }
        
        counts["Favorites"] = favoritesCount
        val uCols = userCollections
        for (i in uCols.indices) {
            val col = uCols[i]
            if (!counts.containsKey(col)) counts[col] = 0
        }
        counts
    }

    fun getCountFor(name: String): Int {
        return categoryCounts[name] ?: 0
    }

    fun startCollectionAssignment(collectionName: String) {
        pendingCollectionAssignment = collectionName
        selectedPhotoIds = emptySet()
        currentScreen = Screen.Library
    }

    fun startCollectionRemoval(collectionName: String) {
        pendingCollectionRemoval = collectionName
        selectedPhotoIds = emptySet()
        currentScreen = Screen.Library
    }


    fun confirmCollectionAssignment() {
        val collectionName = pendingCollectionAssignment ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            val now = System.currentTimeMillis()
            photos = photos.map { photo ->
                if (selectedPhotoIds.contains(photo.id)) {
                    if (collectionName == "Favorites") {
                        photo.copy(isFavorite = true)
                    } else {
                        if (!photo.collections.contains(collectionName)) {
                            photo.copy(
                                collections = photo.collections + collectionName,
                                snippetsAddedTime = if (photo.snippetsAddedTime == 0L) now else photo.snippetsAddedTime
                            )
                        } else photo
                    }
                } else photo
            }
            savePhotos()
            
            if (collectionName != "Favorites") {
                showSnackbar(
                    message = "Added to $collectionName",
                    actionLabel = "View",
                    onAction = {
                        libraryCurrentTab = collectionName
                        navigateLibrary()
                    }
                )
            }
        }
        pendingCollectionAssignment = null
        selectedPhotoIds = emptySet()
        libraryCurrentTab = collectionName
        navigateLibrary()
    }

    fun confirmCollectionRemoval() {
        val collectionName = pendingCollectionRemoval ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = photos.map { photo ->
                if (selectedPhotoIds.contains(photo.id)) {
                    if (collectionName == "Favorites") {
                        photo.copy(isFavorite = false)
                    } else {
                        photo.copy(collections = photo.collections - collectionName)
                    }
                } else photo
            }
            savePhotos()
            
            if (collectionName != "Favorites") {
                showSnackbar(message = "Removed from $collectionName")
            }
        }
        pendingCollectionRemoval = null
        selectedPhotoIds = emptySet()
        libraryCurrentTab = collectionName
        navigateLibrary()
    }


    var showBulkAddToCollectionDialog by mutableStateOf(false)
    var showBulkDeleteModal by mutableStateOf(false)

    private var _snippetSortType = mutableStateOf(SnippetSortType.New)
    var snippetSortType: SnippetSortType
        get() = _snippetSortType.value
        set(value) {
            _snippetSortType.value = value
            saveFilterState()
        }

    private var _isSortAscending = mutableStateOf(true)
    var isSortAscending: Boolean
        get() = _isSortAscending.value
        set(value) {
            _isSortAscending.value = value
            saveFilterState()
        }

    private var _displayMode = mutableStateOf(DisplayMode.Day)
    private var _collectionDisplayModes = mutableStateOf<Map<String, DisplayMode>>(emptyMap())
    
    var displayMode: DisplayMode
        get() = _displayMode.value
        set(value) {
            _displayMode.value = value
            saveFilterState()
        }
    
    var searchDisplayMode by mutableStateOf(DisplayMode.Day)
    
    private var _photoSortType = mutableStateOf(PhotoSortType.DateNewest)
    private var _collectionPhotoSortTypes = mutableStateOf<Map<String, PhotoSortType>>(emptyMap())
    
    var photoSortType: PhotoSortType
        get() = _photoSortType.value
        set(value) {
            _photoSortType.value = value
            saveFilterState()
        }
        
    fun getPhotoSortTypeFor(tabName: String): PhotoSortType {
        if (tabName == "Library") return _photoSortType.value
        return _collectionPhotoSortTypes.value[tabName] ?: PhotoSortType.DateNewest
    }

    fun setPhotoSortTypeFor(tabName: String, sortType: PhotoSortType) {
        if (tabName == "Library") {
            _photoSortType.value = sortType
        } else {
            _collectionPhotoSortTypes.value = _collectionPhotoSortTypes.value + (tabName to sortType)
        }
        saveFilterState()
    }
    
    fun sortPhotos(baseList: List<Photo>, sort: PhotoSortType): List<Photo> {
        return when (sort) {
            PhotoSortType.DateNewest -> baseList.sortedByDescending { it.date }
            PhotoSortType.DateOldest -> baseList.sortedBy { it.date }
            PhotoSortType.MostSnippets -> baseList.sortedWith(compareByDescending<Photo> { it.snippets.size }.thenByDescending { it.date })
            PhotoSortType.LeastSnippets -> baseList.sortedWith(compareBy<Photo> { it.snippets.size }.thenByDescending { it.date })
        }
    }
    
    // State to track if the next added photo should be a favorite
    var pendingFavoriteIntent by mutableStateOf(false)
    var pendingAddPhotoIntentToken by mutableStateOf(0L)

    private val storageMutex = Mutex()

    private val photosFile = File(application.filesDir, "photos_v2.json")
    private val collectionsFile = File(application.filesDir, "collections_v2.json")
    private val snippetColorsFile = File(application.filesDir, "snippet_colors_v2.json")
    private val snippetStylesFile = File(application.filesDir, "snippet_styles_v2.json")
    private val snippetFirstSeenFile = File(application.filesDir, "snippet_first_seen_v1.json")

    init {
        loadFilterState()
        loadSettingsState()
        loadPhotos()
        loadSnippetColors()
        loadSnippetStyles()
        loadSnippetFirstSeenTimes()
        pinnedCollections = prefs.getStringSet("pinned_collections", emptySet()) ?: emptySet()
        loadRecentSearches()
        startHintRotation()

    }

    private fun loadPhotos() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Migration check: If new files don't exist, try loading from SharedPreferences
            if (!photosFile.exists() && prefs.contains("photos_json")) {
                migrateFromPrefs()
            }

            val loadedPhotos = try {
                if (photosFile.exists()) {
                    val json = photosFile.readText()
                    val jsonArray = gson.fromJson(json, com.google.gson.JsonArray::class.java)
                    val result = mutableListOf<Photo>()
                    jsonArray.forEach { element ->
                        try {
                            val photo = gson.fromJson(element, Photo::class.java)
                            result.add(photo)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Skip corrupted individual photo
                        }
                    }
                    result
                } else emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val loadedCollections = try {
                if (collectionsFile.exists()) {
                    val collJson = collectionsFile.readText()
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson<List<String>>(collJson, type) ?: emptyList()
                } else emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }


            val loadedIcons = try {
                if (collectionIconsFile.exists()) {
                    val iconJson = collectionIconsFile.readText()
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    gson.fromJson<Map<String, String>>(iconJson, type) ?: emptyMap()
                } else emptyMap()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }

            val processedPhotos = loadedPhotos.map { p ->
                val (widthPx, heightPx) = if (p.widthPx > 0 && p.heightPx > 0) {
                    p.widthPx to p.heightPx
                } else {
                    extractImageDimensions(Uri.parse(p.uriString))
                }
                Photo(
                    id = p.id,
                    uriString = p.uriString,
                    date = p.date,
                    snippets = p.snippets ?: emptyList(),
                    isViewed = p.isViewed,
                    lastViewedTime = p.lastViewedTime,
                    snippetsAddedTime = p.snippetsAddedTime,
                    surfacedTime = p.surfacedTime,
                    isFavorite = p.isFavorite,
                    isLibraryUpload = p.isLibraryUpload,
                    collections = p.collections ?: emptyList(),
                    widthPx = widthPx,
                    heightPx = heightPx,
                    isPublic = p.isPublic,
                    locationLink = p.locationLink
                )
            }

            // Run expensive operations on IO thread to avoid ANR on Main thread
            val currentPhotos = photos
            val currentSnippetTimes = snippetFirstSeenTimes
            val merged = (currentPhotos + processedPhotos).distinctBy { it.id }.sortedByDescending { it.date }
            val rebuiltSnippetTimes = rebuildSnippetFirstSeenTimes(merged)
            val updatedSnippetTimes = if (currentSnippetTimes.isEmpty()) {
                rebuiltSnippetTimes
            } else {
                rebuiltSnippetTimes.mapValues { (snippet, rebuiltTime) ->
                    currentSnippetTimes[snippet] ?: rebuiltTime
                }
            }


            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                // MERGE: Update the states with the precomputed values
                photos = merged
                snippetFirstSeenTimes = updatedSnippetTimes
                userCollections = loadedCollections
                collectionIcons = loadedIcons
                isInitialLoading = false
                saveSnippetFirstSeenTimes()
                reconcileSurfacedMemories()
                reconcileMemoryNotifications()
            }
        }
    }

    private fun migrateFromPrefs() {
        try {
            val json = prefs.getString("photos_json", null)
            if (json != null) photosFile.writeText(json)
            val collJson = prefs.getString("collections_json", null)
            if (collJson != null) collectionsFile.writeText(collJson)
            
            // Clear old prefs after successful migration
            prefs.edit().remove("photos_json").remove("collections_json").apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveFilterState() {
        prefs.edit()
            .putString("snippet_sort_type", snippetSortType.name)
            .putBoolean("is_sort_ascending", isSortAscending)
            .putStringSet("selected_filter_snippets", selectedFilterSnippets.toSet())
            .putString("display_mode", _displayMode.value.name)
            .putString("collection_display_modes_json", gson.toJson(_collectionDisplayModes.value))
            .putString("photo_sort_type", _photoSortType.value.name)
            .putString("collection_photo_sort_types_json", gson.toJson(_collectionPhotoSortTypes.value))
            .apply()
    }

    private fun loadFilterState() {
        val sortName = prefs.getString("snippet_sort_type", SnippetSortType.New.name)
        _snippetSortType.value = try { SnippetSortType.valueOf(sortName!!) } catch(e: Exception) { SnippetSortType.New }
        _isSortAscending.value = prefs.getBoolean("is_sort_ascending", true)
        selectedFilterSnippets = prefs.getStringSet("selected_filter_snippets", emptySet())?.toList() ?: emptyList()
        
        val displayModeName = prefs.getString("display_mode", DisplayMode.Day.name)
        _displayMode.value = try { DisplayMode.valueOf(displayModeName!!) } catch(e: Exception) { DisplayMode.Day }
        
        val collDisplayModesJson = prefs.getString("collection_display_modes_json", null)
        if (collDisplayModesJson != null) {
            val type = object : TypeToken<Map<String, DisplayMode>>() {}.type
            _collectionDisplayModes.value = try { gson.fromJson(collDisplayModesJson, type) } catch(e: Exception) { emptyMap() }
        }
        
        val sortTypeName = prefs.getString("photo_sort_type", PhotoSortType.DateNewest.name)
        _photoSortType.value = try { PhotoSortType.valueOf(sortTypeName!!) } catch(e: Exception) { PhotoSortType.DateNewest }
        
        val collSortTypesJson = prefs.getString("collection_photo_sort_types_json", null)
        if (collSortTypesJson != null) {
            val type = object : TypeToken<Map<String, PhotoSortType>>() {}.type
            _collectionPhotoSortTypes.value = try { gson.fromJson(collSortTypesJson, type) } catch(e: Exception) { emptyMap() }
        }
    }

    private fun loadSettingsState() {
        val savedThemePreference = prefs.getString("theme_preference", ThemePreference.SYSTEM.name)
        themePreference = try { ThemePreference.valueOf(savedThemePreference!!) } catch (e: Exception) { ThemePreference.SYSTEM }
        useDynamicColors = prefs.getBoolean("use_dynamic_colors", true)
        showTimeInMemories = prefs.getBoolean("show_time_in_memories", false)
        val savedShape = prefs.getString("selected_shape", AppShape.COOKIE_12_SIDED.name)
        selectedShape = try { AppShape.valueOf(savedShape!!) } catch (e: Exception) { AppShape.COOKIE_12_SIDED }
        makePhotosFollowShape = prefs.getBoolean("make_photos_follow_shape", false)

        showCarouselsIn = prefs.getStringSet("show_carousels_in", null) ?: emptySet()
        searchHintsByTap = prefs.getBoolean("search_hints_by_tap", false)
    }

    private fun savePhotos(): kotlinx.coroutines.Job {
        val snapshot = photos // Capture on main thread immediately — avoids race if photos mutates before IO runs
        return viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageMutex.withLock {
                try {
                    val json = gson.toJson(snapshot)
                    photosFile.writeText(json)
                    BackupManager(getApplication()).dataChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun rebuildSnippetFirstSeenTimes(source: List<Photo>): Map<String, Long> {
        val rebuilt = mutableMapOf<String, Long>()
        source.forEach { photo ->
            val time = if (photo.snippetsAddedTime > 0L) photo.snippetsAddedTime else photo.date
            photo.snippets.forEach { snippet ->
                rebuilt[snippet] = minOf(rebuilt[snippet] ?: Long.MAX_VALUE, time)
            }
        }
        return rebuilt
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        if (time1 == 0L || time2 == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun reconcileSurfacedMemories(now: Long = System.currentTimeMillis()) {
        var changed = false
        val scheduledNotifications = mutableListOf<Pair<String, Long>>()
        val updatedPhotos = photos.map { photo ->
            if (shouldResurfaceViewedMemory(photo, now)) {
                changed = true
                photo.copy(isViewed = false, lastViewedTime = 0L, surfacedTime = 0L)
            } else photo
        }.toMutableList()

        val assignedTimes = updatedPhotos.map { it.surfacedTime }.filter { it != 0L }

        val queuedCandidates = updatedPhotos.filter { photo ->
            photo.isLibraryUpload &&
            photo.snippets.isNotEmpty() &&
            photo.snippetsAddedTime != 0L &&
            (!photo.isViewed || photo.snippetsAddedTime > photo.lastViewedTime) &&
            photo.surfacedTime == 0L
        }.sortedByDescending { it.snippetsAddedTime }

        if (queuedCandidates.isNotEmpty()) {
            val tempAssignedTimes = assignedTimes.toMutableList()
            for (candidate in queuedCandidates) {
                val targetTime = findNextAvailableSurfacedTime(candidate, now, tempAssignedTimes)
                tempAssignedTimes.add(targetTime)

                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = targetTime)
                    scheduledNotifications.add(candidate.id to targetTime)
                    changed = true
                }
            }
        }

        if (changed) {
            photos = updatedPhotos
            savePhotos()
            scheduledNotifications.forEach { (id, targetTime) ->
                val delayMs = maxOf(0L, targetTime - now)
                val photo = photos.find { it.id == id }
                val notificationType = if (photo?.isViewed == true) {
                    MemoryWorker.TYPE_UPDATED
                } else if (photo != null && photo.snippetsAddedTime > photo.lastViewedTime && photo.lastViewedTime != 0L) {
                    MemoryWorker.TYPE_RESURFACED
                } else {
                    MemoryWorker.TYPE_NEW
                }
                scheduleMemoryNotification(
                    photoId = id,
                    delay = delayMs,
                    delayUnit = TimeUnit.MILLISECONDS,
                    notificationType = notificationType,
                    policy = androidx.work.ExistingWorkPolicy.REPLACE,
                    resetPostedState = true
                )
            }
        }
    }

    private fun findNextAvailableSurfacedTime(photo: Photo, now: Long, assignedTimes: List<Long>): Long {
        val earliestTime = photo.snippetsAddedTime + NEW_MEMORY_WAIT_MS
        var candidateTime = maxOf(now, earliestTime)

        candidateTime = adjustForQuietHours(candidateTime)

        val activeAssignedTimes = assignedTimes.toMutableList()

        var attempts = 0
        while (attempts < 1000) {
            val conflict = activeAssignedTimes.find { assigned ->
                val diff = if (assigned > candidateTime) assigned - candidateTime else candidateTime - assigned
                diff < SURFACED_MEMORY_SPACING_MS
            }
            if (conflict != null) {
                candidateTime = conflict + SURFACED_MEMORY_SPACING_MS
                candidateTime = adjustForQuietHours(candidateTime)
                attempts++
                continue
            }

            val dayStart = getStartOfDay(candidateTime)
            val countOnDay = activeAssignedTimes.count { getStartOfDay(it) == dayStart }
            if (countOnDay >= 5) {
                val calendar = Calendar.getInstance().apply { timeInMillis = candidateTime }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                candidateTime = calendar.timeInMillis
                attempts++
                continue
            }

            break
        }
        return candidateTime
    }

    private fun adjustForQuietHours(timeMs: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMs }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour >= 22) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        } else if (hour < 8) {
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        return timeMs
    }

    private fun getStartOfDay(timeMs: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMs }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun checkAndResetMemories() {
        reconcileSurfacedMemories()
    }

    private fun shouldResurfaceViewedMemory(photo: Photo, now: Long): Boolean {
        return photo.isViewed &&
            photo.isLibraryUpload &&
            photo.snippets.isNotEmpty() &&
            photo.snippetsAddedTime != 0L &&
            now - photo.lastViewedTime >= VIEWED_MEMORY_RESET_MS
    }
    private fun saveCollections() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageMutex.withLock {
                try {
                    val json = gson.toJson(userCollections)
                    collectionsFile.writeText(json)
                    BackupManager(getApplication()).dataChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Settings Setters
    fun updateThemePreference(pref: ThemePreference) {
        themePreference = pref
        prefs.edit().putString("theme_preference", pref.name).apply()
    }

    fun updateDynamicColors(use: Boolean) {
        useDynamicColors = use
        prefs.edit().putBoolean("use_dynamic_colors", use).apply()
    }

    fun updateSelectedShape(shape: AppShape) {
        selectedShape = shape
        prefs.edit().putString("selected_shape", shape.name).apply()
    }

    fun updateMakePhotosFollowShape(follow: Boolean) {
        makePhotosFollowShape = follow
        prefs.edit().putBoolean("make_photos_follow_shape", follow).apply()
    }



    fun addPhoto(uri: Uri, isFavorite: Boolean = false) {
        isAddingPhotos = true
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val captureDate = extractCaptureDate(uri)
                val duplicate = photos.find { it.date == captureDate && it.uriString.contains(uri.lastPathSegment ?: "___") }
                
                if (duplicate != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (isFavorite && !duplicate.isFavorite) {
                            toggleFavorite(duplicate.id)
                        }
                        isAddingPhotos = false
                    }
                    return@launch
                }

                val internalUri = saveToInternalStorage(uri) ?: uri.toString()
                val (widthPx, heightPx) = extractImageDimensions(Uri.parse(internalUri))
                val newPhoto = Photo(
                    uriString = internalUri,
                    date = captureDate,
                    snippets = emptyList(),
                    isFavorite = isFavorite,
                    isLibraryUpload = true,
                    widthPx = widthPx,
                    heightPx = heightPx
                )
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    photos = (listOf(newPhoto) + photos).sortedByDescending { it.date }
                    savePhotos()
                    isAddingPhotos = false
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isAddingPhotos = false
                }
            }
        }
    }

    fun addPhotoToCollection(uri: Uri, collectionName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val captureDate = extractCaptureDate(uri)
            
            val duplicate = photos.find { it.date == captureDate && it.uriString.contains(uri.lastPathSegment ?: "___") }
            if (duplicate != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (!duplicate.collections.contains(collectionName)) {
                        photos = photos.map {
                            if (it.id == duplicate.id) it.copy(collections = it.collections + collectionName) else it
                        }
                        savePhotos()
                    }
                }
                return@launch
            }

            val internalUri = saveToInternalStorage(uri) ?: uri.toString()
            val (widthPx, heightPx) = extractImageDimensions(Uri.parse(internalUri))
            val newPhoto = Photo(
                uriString = internalUri,
                date = captureDate,
                snippets = emptyList(),
                collections = listOf(collectionName),
                isLibraryUpload = true,
                widthPx = widthPx,
                heightPx = heightPx
            )
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                photos = (listOf(newPhoto) + photos).sortedByDescending { it.date }
                savePhotos()
            }
        }
    }

    private fun extractImageDimensions(uri: Uri): Pair<Int, Int> {
        return try {
            val resolver = getApplication<Application>().contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options) }
            val width = options.outWidth.takeIf { it > 0 } ?: 0
            val height = options.outHeight.takeIf { it > 0 } ?: 0
            width to height
        } catch (_: Exception) {
            0 to 0
        }
    }



    fun createCollection(name: String, openAfterCreate: Boolean = true) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && userCollections.none { it.equals(trimmed, ignoreCase = true) }) {
            userCollections = userCollections + trimmed

            
            saveCollections()
            if (openAfterCreate) {
                libraryCurrentTab = trimmed
                navigateLibrary()
            }
        }
    }


    fun deleteCollection(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        userCollections = userCollections.filter { it != trimmedName }
        pinnedCollections = pinnedCollections - trimmedName
        
        // Cleanup per-collection settings
        _collectionDisplayModes.value = _collectionDisplayModes.value - trimmedName
        _collectionPhotoSortTypes.value = _collectionPhotoSortTypes.value - trimmedName
        
        saveCollections()
        saveFilterState()
        prefs.edit().putStringSet("pinned_collections", pinnedCollections).apply()
        collectionIcons = collectionIcons - trimmedName
        saveCollectionIcons()
        showCountsIn = showCountsIn - trimmedName
        prefs.edit().putStringSet("show_counts_in", showCountsIn).apply()
        showCarouselsIn = showCarouselsIn - trimmedName
        prefs.edit().putStringSet("show_carousels_in", showCarouselsIn).apply()
        saveCollections()

        var photosChanged = false
        photos = photos.map { photo ->
            if (photo.collections.contains(trimmedName)) {
                photosChanged = true
                photo.copy(collections = photo.collections - trimmedName)
            } else photo
        }
        if (photosChanged) savePhotos()

        if (activeCollection == trimmedName) {
            navigateLibrary()
        }
        if (pendingCollectionAssignment == trimmedName) {
            pendingCollectionAssignment = null
        }
    }

    fun togglePinCollection(name: String) {
        val isCurrentlyPinned = pinnedCollections.contains(name)
        pinnedCollections = if (isCurrentlyPinned) {
            pinnedCollections - name
        } else {
            if (pinnedCollections.size >= 3) {
                showSnackbar("Upto 3 pins!")
                return
            }
            pinnedCollections + name
        }
        prefs.edit().putStringSet("pinned_collections", pinnedCollections).apply()
    }

    fun renameCollection(oldName: String, newName: String) {
        val trimmedOldName = oldName.trim()
        val trimmedNewName = newName.trim()

        if (trimmedOldName.isBlank() || trimmedNewName.isBlank() || trimmedOldName == trimmedNewName) return
        if (userCollections.any { it.equals(trimmedNewName, ignoreCase = true) && it != trimmedOldName }) return

        userCollections = userCollections.map { collection ->
            if (collection == trimmedOldName) trimmedNewName else collection
        }
        saveCollections()

        photos = photos.map { photo ->
            if (photo.snippets.any { it == trimmedOldName } || photo.collections.any { it == trimmedOldName }) {
                photo.copy(
                    snippets = photo.snippets.map { snippet ->
                        if (snippet == trimmedOldName) trimmedNewName else snippet
                    },
                    collections = photo.collections.map { collection ->
                        if (collection == trimmedOldName) trimmedNewName else collection
                    }.distinct()
                )
            } else {
                photo
            }
        }
        savePhotos()

        if (collectionIcons.containsKey(trimmedOldName)) {
            val icon = collectionIcons[trimmedOldName]
            collectionIcons = collectionIcons - trimmedOldName
            if (icon != null) collectionIcons = collectionIcons + (trimmedNewName to icon)
            saveCollectionIcons()
        }
        if (pinnedCollections.contains(trimmedOldName)) {
            pinnedCollections = pinnedCollections - trimmedOldName + trimmedNewName
            prefs.edit().putStringSet("pinned_collections", pinnedCollections).apply()
        }
        if (showCountsIn.contains(trimmedOldName)) {
            showCountsIn = showCountsIn - trimmedOldName + trimmedNewName
            prefs.edit().putStringSet("show_counts_in", showCountsIn).apply()
        }
        if (showCarouselsIn.contains(trimmedOldName)) {
            showCarouselsIn = showCarouselsIn - trimmedOldName + trimmedNewName
            prefs.edit().putStringSet("show_carousels_in", showCarouselsIn).apply()
        }

        if (activeCollection == trimmedOldName) {
            activeCollection = trimmedNewName
        }
        if (pendingCollectionAssignment == trimmedOldName) {
            pendingCollectionAssignment = trimmedNewName
        }
        if (selectedFilterSnippets.contains(trimmedOldName)) {
            selectedFilterSnippets = selectedFilterSnippets.map { if (it == trimmedOldName) trimmedNewName else it }
            saveFilterState()
        }
    }

    fun moveCollectionLeft(name: String) {
        val trimmed = name.trim()
        val index = userCollections.indexOf(trimmed)
        if (index > 0) {
            val newList = userCollections.toMutableList()
            val temp = newList[index - 1]
            newList[index - 1] = newList[index]
            newList[index] = temp
            userCollections = newList
            saveCollections()
        }
    }

    fun moveCollectionRight(name: String) {
        val trimmed = name.trim()
        val index = userCollections.indexOf(trimmed)
        if (index != -1 && index < userCollections.size - 1) {
            val newList = userCollections.toMutableList()
            val temp = newList[index + 1]
            newList[index + 1] = newList[index]
            newList[index] = temp
            userCollections = newList
            saveCollections()
        }
    }

    private fun memoryReminderName(photoId: String) = "$MEMORY_REMINDER_PREFIX$photoId"

    private fun scheduleNewMemoryNotification(photoId: String) {
        scheduleMemoryNotification(
            photoId = photoId,
            delay = NEW_MEMORY_NOTIFICATION_DELAY_HOURS,
            delayUnit = TimeUnit.HOURS,
            notificationType = MemoryWorker.TYPE_NEW
        )
    }

    private fun scheduleUpdatedMemoryNotification(photoId: String) {
        scheduleMemoryNotification(
            photoId = photoId,
            delay = NEW_MEMORY_NOTIFICATION_DELAY_HOURS,
            delayUnit = TimeUnit.HOURS,
            notificationType = MemoryWorker.TYPE_UPDATED
        )
    }

    private fun scheduleMemoryNotification(
        photoId: String,
        delay: Long,
        delayUnit: TimeUnit = TimeUnit.HOURS,
        notificationType: String = MemoryWorker.TYPE_NEW,
        policy: androidx.work.ExistingWorkPolicy = androidx.work.ExistingWorkPolicy.REPLACE,
        resetPostedState: Boolean = true
    ) {
        if (photoId.isBlank()) return

        if (resetPostedState) {
            MemoryWorker.clearPostedNotificationState(getApplication(), photoId)
        }
        val reminderName = memoryReminderName(photoId)
        val workRequestBuilder = OneTimeWorkRequestBuilder<MemoryWorker>()
            .setInitialDelay(delay, delayUnit)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.LINEAR,
                30,
                TimeUnit.MINUTES
            )
            .setInputData(
                androidx.work.workDataOf(
                    MemoryWorker.INPUT_PHOTO_ID to photoId,
                    MemoryWorker.INPUT_NOTIFICATION_TYPE to notificationType
                )
            )
            .addTag(reminderName)

        // For resurfaced memories (zero delay), use expedited work to deliver immediately
        if (delay == 0L) {
            workRequestBuilder.setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        val workRequest = workRequestBuilder.build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            reminderName,
            policy,
            workRequest
        )
    }

    private fun reconcileMemoryNotifications() {
        val now = System.currentTimeMillis()
        photos
            .filterNot { MemoryWorker.wasNotificationPosted(getApplication(), it.id) }
            .forEach { photo ->
                if (photo.surfacedTime != 0L) {
                    val delayMs = maxOf(0L, photo.surfacedTime - now)
                    val notificationType = if (photo.isViewed) {
                        MemoryWorker.TYPE_UPDATED
                    } else if (photo.snippetsAddedTime > photo.lastViewedTime && photo.lastViewedTime != 0L) {
                        MemoryWorker.TYPE_RESURFACED
                    } else {
                        MemoryWorker.TYPE_NEW
                    }
                    scheduleMemoryNotification(
                        photoId = photo.id,
                        delay = delayMs,
                        delayUnit = TimeUnit.MILLISECONDS,
                        notificationType = notificationType,
                        policy = androidx.work.ExistingWorkPolicy.KEEP,
                        resetPostedState = false
                    )
                }
            }
    }

    private fun cancelMemoryNotification(photoId: String) {
        if (photoId.isBlank()) return
        val reminderName = memoryReminderName(photoId)
        WorkManager.getInstance(getApplication()).cancelUniqueWork(reminderName)
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(reminderName)
        MemoryWorker.cancelPostedNotification(getApplication(), photoId)
    }

    fun testNotification() {
        val photosWithSnippets = photos.filter { it.snippets.isNotEmpty() }
        val testId = photosWithSnippets.firstOrNull()?.id ?: TEST_NOTIFICATION_PHOTO_ID
        
        val workRequest = OneTimeWorkRequestBuilder<MemoryWorker>()
            .setInitialDelay(TEST_NOTIFICATION_DELAY_SECONDS, TimeUnit.SECONDS)
            .setInputData(
                androidx.work.workDataOf(
                    MemoryWorker.INPUT_PHOTO_ID to testId,
                    MemoryWorker.INPUT_NOTIFICATION_TYPE to MemoryWorker.TYPE_NEW
                )
            )
            .build()
        requestNotificationPermission = true
        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    private fun extractCaptureDate(uri: Uri): Long {
        val context = getApplication<Application>()
        var fallbackDate = 0L
        
        // Try to get the file's last modified date as a better fallback than current time
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateModifiedIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_MODIFIED)
                    if (dateModifiedIndex != -1) {
                        val dateSeconds = cursor.getLong(dateModifiedIndex)
                        if (dateSeconds > 0) fallbackDate = dateSeconds * 1000L
                    }
                    if (fallbackDate == 0L) {
                        val docLastModIndex = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        if (docLastModIndex != -1) {
                            val dateMs = cursor.getLong(docLastModIndex)
                            if (dateMs > 0) fallbackDate = dateMs
                        }
                    }
                }
            }
        } catch (e: Exception) {}
        
        if (fallbackDate == 0L) fallbackDate = System.currentTimeMillis()

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                
                if (dateTime != null) {
                    val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                    format.parse(dateTime)?.time ?: fallbackDate
                } else {
                    fallbackDate
                }
            } ?: fallbackDate
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackDate
        }
    }

    private fun saveToInternalStorage(uri: Uri): String? {
        val context = getApplication<Application>()
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) photosDir.mkdirs()
        
        val fileName = "photo_${System.currentTimeMillis()}.jpg" // Still using JPG extension for clarity, but raw copy preserves quality
        val localFile = File(photosDir, fileName)
        
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output) // Raw byte stream copy ensures original quality
                }
            }
            localFile.absolutePath.let { if (it.startsWith("/")) "file://$it" else it }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateSnippets(photoId: String, text: String, color: Int, style: SnippetStyle) {
        val targetPhoto = photos.find { it.id == photoId }
        if (targetPhoto != null && targetPhoto.snippets.size >= 6) {
            android.widget.Toast.makeText(getApplication(), "Maximum 6 snippets per photo", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return

        // Update color and style first
        updateSnippetColor(trimmedText, color)
        updateSnippetStyle(trimmedText, style)

        val newSnippets = listOf(trimmedText)
        val now = System.currentTimeMillis()
        var firstTimeSnippetsAdded = false
        var resurfacedMemory = false
        val wasAppEmptyBefore = photos.all { it.snippets.isEmpty() }

        if (!snippetFirstSeenTimes.containsKey(trimmedText)) {
            snippetFirstSeenTimes = snippetFirstSeenTimes + (trimmedText to now)
            saveSnippetFirstSeenTimes()
        }
        
        // Update photo's snippets list
        photos = photos.map {
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

        savePhotos()
        reconcileSurfacedMemories()

        if (firstTimeSnippetsAdded && wasAppEmptyBefore) {
            showSnackbar("You just created your first snippet! A new memory will be created soon.")
        }
    }

    fun removeSnippet(photoId: String, snippet: String) {
        var emptiedMemory = false
        photos = photos.map {
            if (it.id == photoId) {
                val updatedSnippets = it.snippets.filter { s -> s != snippet }
                emptiedMemory = it.snippets.isNotEmpty() && updatedSnippets.isEmpty()
                it.copy(
                    snippets = updatedSnippets,
                    snippetsAddedTime = if (updatedSnippets.isEmpty()) 0L else it.snippetsAddedTime
                )
            } else it
        }
        savePhotos()
        

        
        if (emptiedMemory) cancelMemoryNotification(photoId)

        if (photos.none { photo -> photo.snippets.contains(snippet) }) {
            snippetFirstSeenTimes = snippetFirstSeenTimes - snippet
            saveSnippetFirstSeenTimes()
        }
    }

    fun deleteSnippetGlobally(snippet: String) {
        val emptiedMemoryIds = mutableListOf<String>()
        photos = photos.map {
            val updatedSnippets = it.snippets.filter { s -> s != snippet }
            if (it.snippets.isNotEmpty() && updatedSnippets.isEmpty()) {
                emptiedMemoryIds.add(it.id)
            }
            it.copy(
                snippets = updatedSnippets,
                snippetsAddedTime = if (updatedSnippets.isEmpty()) 0L else it.snippetsAddedTime
            )
        }
        savePhotos()
        emptiedMemoryIds.forEach { cancelMemoryNotification(it) }
        

        snippetFirstSeenTimes = snippetFirstSeenTimes - snippet
        saveSnippetFirstSeenTimes()
    }

    fun toggleFavorite(photoId: String) {
        photos = photos.map {
            if (it.id == photoId) it.copy(isFavorite = !it.isFavorite) else it
        }
        savePhotos()
    }

    fun togglePublicStatus(photoId: String) {
        photos = photos.map {
            if (it.id == photoId) it.copy(isPublic = !it.isPublic) else it
        }
        savePhotos()
    }

    fun updateLocationLink(photoId: String, link: String?) {
        photos = photos.map {
            if (it.id == photoId) it.copy(locationLink = link) else it
        }
        savePhotos()
    }

    fun deletePhoto(photoId: String, unpublish: Boolean = true) {
        photos = photos.filter { it.id != photoId }
        savePhotos()
        cancelMemoryNotification(photoId)
        closeDetail()
    }

    // --- Bulk Actions ---
    
    fun toggleSelection(photoId: String) {
        selectedPhotoIds = if (selectedPhotoIds.contains(photoId)) {
            selectedPhotoIds - photoId
        } else {
            selectedPhotoIds + photoId
        }
    }

    fun clearSelection() {
        selectedPhotoIds = emptySet()
        pendingCollectionAssignment = null
        pendingCollectionRemoval = null
        forceSelectionMode = false
    }

    fun deleteSelectedPhotos(unpublish: Boolean = true) {
        isBusy = true
        photos = photos.filter { !selectedPhotoIds.contains(it.id) }
        savePhotos()
        selectedPhotoIds.forEach { cancelMemoryNotification(it) }
        clearSelection()
        isBusy = false
    }

    fun toggleSelectedInCollection(collectionName: String) {
        isBusy = true
        val now = System.currentTimeMillis()
        photos = photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                val updatedCollections = if (photo.collections.contains(collectionName)) {
                    photo.collections - collectionName
                } else {
                    photo.collections + collectionName
                }
                photo.copy(
                    collections = updatedCollections,
                    snippetsAddedTime = if (photo.snippetsAddedTime == 0L) now else photo.snippetsAddedTime
                )
            } else photo
        }
        savePhotos()
        clearSelection()
        isBusy = false
    }
    
    val isAllSelectedFavorited: Boolean by derivedStateOf {
        val selected = photos.filter { selectedPhotoIds.contains(it.id) }
        selected.isNotEmpty() && selected.all { it.isFavorite }
    }

    fun bulkFavoriteSelection() {
        isBusy = true
        val targetState = !isAllSelectedFavorited
        val count = selectedPhotoIds.size
        photos = photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                photo.copy(isFavorite = targetState)
            } else photo
        }
        savePhotos()
        
        if (targetState) {
            showSnackbar(message = "$count to Favorites")
        }
        clearSelection()
        isBusy = false
    }

    fun updateShowTimeInMemories(show: Boolean) {
        showTimeInMemories = show
        prefs.edit().putBoolean("show_time_in_memories", show).apply()
    }

    fun downloadPhotoCard(context: Context, photo: Photo, isDark: Boolean, bgColor: Int) {
        viewModelScope.launch {
            val pureSnippets = getPureSnippets(photo)
            val success = MediaSaver.saveSnippetToGallery(context, photo, pureSnippets, isDark, bgColor, snippetColors, snippetStyles, appShape = selectedShape, showTime = showTimeInMemories)
            if (success) {
                android.widget.Toast.makeText(context, "Downloaded to Gallery", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sharePhotoCard(context: Context, photo: Photo, isDark: Boolean, bgColor: Int) {
        viewModelScope.launch {
            val pureSnippets = getPureSnippets(photo)
            val uri = MediaSaver.getShareableUri(context, photo, pureSnippets, isDark, bgColor, snippetColors, snippetStyles, appShape = selectedShape, showTime = showTimeInMemories)
            if (uri != null) {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("", uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Share Snippets"))
            }
        }
    }

    fun openDetail(id: String, overrideReturnScreen: Screen? = null) {
        if (currentScreen != Screen.Detail) {
            detailReturnScreen = overrideReturnScreen ?: currentScreen
        }
        activePhotoId = id
        previousScreen = currentScreen
        currentScreen = Screen.Detail
        // Defer markAsViewed so the photos-list rebuild doesn't trigger a
        // filteredPhotos recomputation (and DetailScreen recomposition)
        // during the first frame / shared-element transition.
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            markAsViewed(id)
        }
    }

    fun closeDetail() {
        previousScreen = currentScreen
        currentScreen = detailReturnScreen
    }

    fun navigateBack() {
        when (currentScreen) {
            Screen.Detail -> closeDetail()
            Screen.Memory,
            Screen.Settings,
            Screen.About,
            Screen.Stats -> navigateLibrary()
            Screen.SelectIcon,
            Screen.PhotosCarousel -> {
                currentScreen = previousScreen
            }
            Screen.Filter,
            Screen.Library -> Unit
        }
    }

    fun navigateLibrary() {
        searchQuery = ""
        selectedFilterSnippets = emptyList()
        previousScreen = currentScreen
        currentScreen = Screen.Library
        activePhotoId = null
        clearSelection()
    }

    fun navigateFilter() {
        showFilterSheet = true
    }

    fun closeFilter() {
        showFilterSheet = false
    }

    fun navigateSettings() {
        searchQuery = ""
        previousScreen = currentScreen
        currentScreen = Screen.Settings
        activePhotoId = null
    }

    fun navigateAbout() {
        searchQuery = ""
        previousScreen = currentScreen
        currentScreen = Screen.About
        activePhotoId = null
    }

    fun navigateStats() {
        searchQuery = ""
        previousScreen = currentScreen
        currentScreen = Screen.Stats
        activePhotoId = null
    }

    fun navigateCollections(focusCreate: Boolean = false) {
        showCreateDialog = true
    }


    fun openMemory(index: Int) {
        if (curatedMemories.isEmpty()) return
        activeMemoriesSnapshot = curatedMemories
        currentMemoryIndex = index.coerceIn(0, activeMemoriesSnapshot.size - 1)
        previousScreen = currentScreen
        currentScreen = Screen.Memory
        activeMemoriesSnapshot.getOrNull(currentMemoryIndex)?.let { markAsViewed(it.id) }
    }

    fun onMemoryViewed(index: Int) {
        if (activeMemoriesSnapshot.isEmpty()) return
        currentMemoryIndex = index.coerceIn(0, activeMemoriesSnapshot.size - 1)
        activeMemoriesSnapshot.getOrNull(currentMemoryIndex)?.let { markAsViewed(it.id) }
    }

    private fun markAsViewed(id: String) {
        val now = System.currentTimeMillis()
        val photo = photos.find { it.id == id } ?: return

        // Skip entirely if already up-to-date — avoids any list rebuild
        if (photo.isViewed && photo.snippetsAddedTime <= photo.lastViewedTime) {
            return
        }

        // Rebuild list on Default dispatcher so the main thread is never stalled
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val updated = photos.map {
                if (it.id == id) it.copy(isViewed = true, lastViewedTime = now) else it
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                photos = updated
            }
            savePhotos()
        }
        scheduleMemoryNotification(
            photoId = id,
            delay = VIEWED_MEMORY_RESET_MS,
            delayUnit = TimeUnit.MILLISECONDS,
            notificationType = MemoryWorker.TYPE_RESURFACED,
            policy = androidx.work.ExistingWorkPolicy.REPLACE,
            resetPostedState = true
        )
    }

    val activePhoto: Photo? get() = photos.find { it.id == activePhotoId }

    val curatedMemories: List<Photo> by derivedStateOf {
        val now = System.currentTimeMillis()

        photos.filter { photo ->
            photo.isLibraryUpload &&
            photo.snippets.isNotEmpty() && 
            photo.snippetsAddedTime != 0L &&
            (now - photo.snippetsAddedTime >= NEW_MEMORY_WAIT_MS) &&
            (
                (photo.surfacedTime != 0L && photo.surfacedTime <= now && (!photo.isViewed || photo.snippetsAddedTime > photo.lastViewedTime)) ||
                (photo.isViewed && now - photo.lastViewedTime < VIEWED_MEMORY_VISIBLE_MS)
            )
        }.sortedWith(
            compareByDescending<Photo> { 
                !it.isViewed 
            }.thenByDescending { 
                it.isViewed && (now - it.lastViewedTime < RECENTLY_VIEWED_MEMORY_MS)
            }.thenByDescending { 
                if (it.isViewed) it.lastViewedTime else it.surfacedTime 
            }
        )
    }

    val hasUnviewedMemories: Boolean by derivedStateOf {
        curatedMemories.any { !it.isViewed }
    }
    
    val filteredPhotos: List<Photo> by derivedStateOf {
        val query = searchQuery.trim()
        val queryParts = if (query.contains(",")) {
            query.split(",").map { it.trim() }.filter { it.isNotBlank() }
        } else if (query.isNotBlank()) {
            listOf(query)
        } else {
            emptyList()
        }

        val baseList = if (queryParts.isEmpty() && selectedFilterSnippets.isEmpty()) photos
        else photos.filter { photo ->
            var dateObj: java.util.Date? = null
            var dayName: String? = null
            var monthName: String? = null
            var yearName: String? = null
            var weekName: String? = null
            var fullDate: String? = null
            
            // All query parts must match (AND logic)
            val matchesQuery = queryParts.isNotEmpty() && queryParts.all { part ->
                val isNegationQuery = part.startsWith("not ", ignoreCase = true) || part.equals("not", ignoreCase = true)
                
                photo.snippets.any { s ->
                    val index = s.indexOf(part, ignoreCase = true)
                    if (index == -1) return@any false
                    
                    // SMART: If we matched "sweet" inside "not sweet", and we didn't search for "not", skip it
                    val isPrecededByNot = s.startsWith("not ", ignoreCase = true) && !isNegationQuery
                    !isPrecededByNot
                } ||
                photo.collections.any { it.contains(part, ignoreCase = true) } ||
                run {
                    if (dateObj == null) {
                        dateObj = java.util.Date(photo.date)
                        dayName = dayOfWeekFormat.format(dateObj)
                        monthName = monthDateFormat.format(dateObj)
                        yearName = yearDateFormat.format(dateObj)
                        weekName = weekDateFormat.format(dateObj)
                        fullDate = groupDateFormat.format(dateObj)
                    }
                    dayName!!.contains(part, ignoreCase = true) ||
                    monthName!!.contains(part, ignoreCase = true) ||
                    yearName!!.contains(part, ignoreCase = true) ||
                    weekName!!.contains(part, ignoreCase = true) ||
                    fullDate!!.contains(part, ignoreCase = true)
                }
            }

            val matchesChips = selectedFilterSnippets.isNotEmpty() && selectedFilterSnippets.any { chip ->
                photo.snippets.any { it.equals(chip, ignoreCase = true) }
            }

            if (queryParts.isNotEmpty() && selectedFilterSnippets.isNotEmpty()) {
                matchesQuery || matchesChips // Union of search and filters
            } else if (queryParts.isNotEmpty()) {
                matchesQuery
            } else {
                matchesChips
            }
        }
        
        // Smart grouping: Auto-switch searchDisplayMode based on the FIRST matching date-part
        if (queryParts.isNotEmpty()) {
            val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            
            queryParts.forEach { part ->
                val isMonth = months.any { it.startsWith(part, ignoreCase = true) }
                val isWeek = part.startsWith("Week", ignoreCase = true)
                
                if (isMonth || isWeek) {
                    searchDisplayMode = when {
                        isMonth -> DisplayMode.Month
                        isWeek -> DisplayMode.Week
                        else -> searchDisplayMode
                    }
                    return@forEach
                }
            }
        }
        
        when (photoSortType) {
            PhotoSortType.DateNewest -> baseList.sortedByDescending { it.date }
            PhotoSortType.DateOldest -> baseList.sortedBy { it.date }
            PhotoSortType.MostSnippets -> baseList.sortedWith(compareByDescending<Photo> { it.snippets.size }.thenByDescending { it.date })
            PhotoSortType.LeastSnippets -> baseList.sortedWith(compareBy<Photo> { it.snippets.size }.thenByDescending { it.date })
        }
    }

    private val groupDateFormat = java.text.SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val weekDateFormat = java.text.SimpleDateFormat("'Week' w, yyyy", Locale.getDefault())
    private val monthDateFormat = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearDateFormat = java.text.SimpleDateFormat("yyyy", Locale.getDefault())
    private val dayOfWeekFormat = java.text.SimpleDateFormat("EEEE", Locale.getDefault())
    private val groupCalendar = Calendar.getInstance()

    private fun groupPhotosByDate(
        source: List<Photo>, 
        mode: DisplayMode = displayMode,
        sort: PhotoSortType = photoSortType
    ): Map<String, List<Photo>> {
        if (sort == PhotoSortType.MostSnippets || sort == PhotoSortType.LeastSnippets) {
            val label = if (sort == PhotoSortType.MostSnippets) "Most Snippets" else "Least Snippets"
            return mapOf(label to source)
        }
        val now = System.currentTimeMillis()
        groupCalendar.timeInMillis = now
        val todayDay = groupCalendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = groupCalendar.get(Calendar.YEAR)

        return source.groupBy { photo ->
            groupCalendar.timeInMillis = photo.date
            val photoDay = groupCalendar.get(Calendar.DAY_OF_YEAR)
            val photoYear = groupCalendar.get(Calendar.YEAR)

            when (mode) {
                DisplayMode.Day -> {
                    when {
                        photoYear == todayYear && photoDay == todayDay -> "Today"
                        photoYear == todayYear && photoDay == todayDay - 1 -> "Yesterday"
                        else -> groupDateFormat.format(java.util.Date(photo.date))
                    }
                }
                DisplayMode.Week -> {
                    // Prettier week range: "Oct 14 - Oct 20"
                    val startCalendar = Calendar.getInstance().apply {
                        timeInMillis = photo.date
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Consistent week start
                    }
                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = startCalendar.timeInMillis
                        add(Calendar.DAY_OF_WEEK, 6)
                    }
                    val rangeFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                    "${rangeFormat.format(startCalendar.time)} - ${rangeFormat.format(endCalendar.time)}"
                }
                DisplayMode.Month -> monthDateFormat.format(java.util.Date(photo.date))
            }
        }
    }

    val groupedPhotos: Map<String, List<Photo>> by derivedStateOf {
        groupPhotosByDate(photos)
    }

    private fun containsEmoji(s: String): Boolean {
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            if (cp in 0x1F300..0x1F9FF || cp in 0x1F600..0x1F64F || cp in 0x1F680..0x1F6FF || cp in 0x1F1E6..0x1F1FF) return true
            i += Character.charCount(cp)
        }
        return false
    }

    private fun containsEmoticon(s: String): Boolean {
        val emoticons = listOf(
            ":)", ":(", ":D", ":P", ":O", ":-)", ":-(", ":-D", ":-P", ";)", ";D", 
            "^_^", ">_<", "o_o", "O_O", "-_-", "T_T", "8-)", "8)", ":3", "xD", "XD",
            ":' )", ":' (", ":v", ":/", ":\\", ">:)", ">:(", "O:)", "u_u"
        )
        return emoticons.any { s.contains(it) }
    }

    val favoritesUniqueSnippets: List<String> by derivedStateOf {
        photos.filter { it.isFavorite }.flatMap { it.snippets }.distinct()
    }

    fun getCollectionUniqueSnippets(collectionName: String): List<String> {
        return photos.filter { it.collections.contains(collectionName) }.flatMap { it.snippets }.distinct()
    }

    val allUniqueSnippets: List<String> by derivedStateOf {
        val baseSnippets = photos.flatMap { it.snippets }.distinct()

        val filtered = when (snippetSortType) {
            SnippetSortType.Favorites -> {
                val favSnippets = photos.filter { it.isFavorite }.flatMap { it.snippets }.toSet()
                baseSnippets.filter { it in favSnippets }
            }
            SnippetSortType.Emoji -> baseSnippets.filter { containsEmoji(it) }
            SnippetSortType.Emoticons -> baseSnippets.filter { containsEmoticon(it) }
            SnippetSortType.Color -> baseSnippets.filter { snippetColors.containsKey(it) }
            else -> baseSnippets
        }

        when (snippetSortType) {
            SnippetSortType.New, SnippetSortType.Month, SnippetSortType.Year -> filtered.sortedByDescending { snippetFirstSeenTimes[it] ?: 0L }
            SnippetSortType.Old -> filtered.sortedBy { snippetFirstSeenTimes[it] ?: 0L }
            SnippetSortType.AZ -> if (isSortAscending) filtered.sortedWith(String.CASE_INSENSITIVE_ORDER) 
                                 else filtered.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
            SnippetSortType.Color -> if (isSortAscending) filtered.sortedBy { snippetColors[it] ?: 0 } 
                                    else filtered.sortedByDescending { snippetColors[it] ?: 0 }
            SnippetSortType.Style -> if (isSortAscending) filtered.sortedBy { snippetStyles[it]?.name ?: "" }
                                    else filtered.sortedByDescending { snippetStyles[it]?.name ?: "" }
            SnippetSortType.Emoji, SnippetSortType.Emoticons, SnippetSortType.Favorites -> filtered.sorted()
        }
    }
    val searchSuggestions: List<Pair<String, Boolean>> by derivedStateOf {
        val query = searchQuery.trim()
        if (query.isEmpty()) return@derivedStateOf emptyList()
        
        // For multi-part search, suggest for the last part
        val parts = searchQuery.split(",")
        val lastPart = parts.last().trim()
        val prefix = if (parts.size > 1) {
            searchQuery.substring(0, searchQuery.lastIndexOf(",") + 1) + " "
        } else ""

        if (lastPart.length < 2) return@derivedStateOf emptyList()
        
        val nonEmptyCollections = userCollections.filter { coll -> 
            photos.any { it.collections.contains(coll) } 
        }

        val matchingCollections = nonEmptyCollections
            .filter { it.contains(lastPart, ignoreCase = true) }
            .map { (prefix + it) to true }
            
        val matchingSnippets = allUniqueSnippets
            .filter { it.contains(lastPart, ignoreCase = true) }
            .filter { snippet -> !userCollections.any { it.equals(snippet, ignoreCase = true) } }
            .map { (prefix + it) to false }
            
        (matchingCollections + matchingSnippets).take(10)
    }

    val filteredGroupedPhotos: Map<String, List<Photo>> by derivedStateOf {
        groupPhotosByDate(filteredPhotos, _displayMode.value, _photoSortType.value)
    }

    val filteredCollectionPhotos: Map<String, List<Photo>> by derivedStateOf {
        val currentCollection = activeCollection
        val source = if (currentCollection != null) {
            filteredPhotos.filter { it.collections.contains(currentCollection) }
        } else filteredPhotos
        
        val mode = if (separateCollectionSort && currentCollection != null) {
            _collectionDisplayModes.value[currentCollection] ?: DisplayMode.Day
        } else _displayMode.value
        
        val sort = if (separateCollectionSort && currentCollection != null) {
            _collectionPhotoSortTypes.value[currentCollection] ?: PhotoSortType.DateNewest
        } else _photoSortType.value
        
        groupPhotosByDate(source, mode, sort)
    }

    val filteredFavoritesPhotos: Map<String, List<Photo>> by derivedStateOf {
        val source = filteredPhotos.filter { it.isFavorite }
        // Favorites uses Library defaults (as per "leave favorites out" preference)
        groupPhotosByDate(source, _displayMode.value, _photoSortType.value)
    }

    fun getPureSnippets(photo: Photo): List<String> {
        return photo.snippets.filterNot { snippet -> 
            userCollections.any { it.equals(snippet, ignoreCase = true) } 
        }
    }

    fun getCollectionIcon(collectionName: String): Any {
        if (collectionName == "Favorites") return Icons.Default.Favorite
        val iconName = collectionIcons[collectionName] ?: return Icons.Default.Folder
        
        // If it's a single emoji or short string, return it as is
        if (iconName.length <= 4) return iconName
        
        return when (iconName) {
            "Landscape" -> Icons.Default.Landscape
            "Nature" -> Icons.Default.Nature
            "Person" -> Icons.Default.Person
            "Group" -> Icons.Default.Groups
            "LocalDrink" -> Icons.Default.LocalDrink
            "Cookie" -> Icons.Default.Cookie
            "Pets" -> Icons.Default.Pets
            "Travel" -> Icons.Default.Flight
            "Waves" -> Icons.Default.Waves
            "Apartment" -> Icons.Default.Apartment
            else -> Icons.Default.Folder
        }
    }

    fun updateSearchHintsByTap(enabled: Boolean) {
        searchHintsByTap = enabled
        prefs.edit().putBoolean("search_hints_by_tap", enabled).apply()
    }

    private fun startHintRotation() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            while (true) {
                val hints = mutableListOf<String>()
                
                // 1. Base hint based on screen
                val base = "Library"
                hints.add(base)

                // 2. Time-based hints (Dynamic)
                val now = java.util.Date()
                hints.add(yearDateFormat.format(now)) // e.g. 2024
                hints.add(java.text.SimpleDateFormat("MMMM", Locale.getDefault()).format(now)) // e.g. April
                hints.add(dayOfWeekFormat.format(now)) // e.g. Monday
                hints.add("Week ${java.util.Calendar.getInstance().get(java.util.Calendar.WEEK_OF_YEAR)}") // e.g. Week 18

                // 3. Collections (Non-empty)
                userCollections.filter { coll -> photos.any { it.collections.contains(coll) } }
                    .shuffled().take(3).forEach { 
                        hints.add(it) 
                    }
                
                // 4. Snippets
                allUniqueSnippets.shuffled().take(3).forEach { 
                    hints.add(it) 
                }

                if (hints.isEmpty()) {
                    rotatingSearchHint = "Library"
                    kotlinx.coroutines.delay(4000)
                    continue
                }

                for (hint in hints) {
                    rotatingSearchHint = hint
                    kotlinx.coroutines.delay(3000)
                }
            }
        }
    }

    fun getSnippetColor(name: String): Int? {
        return snippetColors[name.trim()]
    }

    fun getSnippetFirstSeenTime(name: String): Long? {
        return snippetFirstSeenTimes[name.trim()]
    }

    fun updateSnippetColor(name: String, color: Int?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        
        snippetColors = if (color == null) {
            snippetColors - trimmed
        } else {
            snippetColors + (trimmed to color)
        }
        saveSnippetColors()
    }

    fun getSnippetStyle(name: String): SnippetStyle {
        return snippetStyles[name.trim()] ?: SnippetStyle.Default
    }

    fun updateSnippetStyle(name: String, style: SnippetStyle) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        
        snippetStyles = snippetStyles + (trimmed to style)
        saveSnippetStyles()
    }



    private fun saveSnippetStyles() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val json = gson.toJson(snippetStyles)
                snippetStylesFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSnippetStyles() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (snippetStylesFile.exists()) {
                try {
                    val json = snippetStylesFile.readText()
                    val type = object : TypeToken<Map<String, SnippetStyle>>() {}.type
                    val loaded: Map<String, SnippetStyle> = gson.fromJson(json, type) ?: emptyMap()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        snippetStyles = loaded
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveSnippetColors() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val json = gson.toJson(snippetColors)
                snippetColorsFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSnippetColors() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (snippetColorsFile.exists()) {
                try {
                    val json = snippetColorsFile.readText()
                    val type = object : TypeToken<Map<String, Int>>() {}.type
                    val loaded: Map<String, Int> = gson.fromJson(json, type) ?: emptyMap()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        snippetColors = loaded
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveSnippetFirstSeenTimes() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val json = gson.toJson(snippetFirstSeenTimes)
                snippetFirstSeenFile.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSnippetFirstSeenTimes() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val loaded = try {
                if (snippetFirstSeenFile.exists()) {
                    val json = snippetFirstSeenFile.readText()
                    val type = object : TypeToken<Map<String, Long>>() {}.type
                    gson.fromJson<Map<String, Long>>(json, type) ?: emptyMap()
                } else emptyMap()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }

            val rebuilt = rebuildSnippetFirstSeenTimes(photos)
            val merged = rebuilt.mapValues { (snippet, rebuiltTime) ->
                loaded[snippet] ?: rebuiltTime
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                snippetFirstSeenTimes = merged
            }

            if (merged != loaded) {
                saveSnippetFirstSeenTimes()
            }
        }
    }

    fun updateSnippets(photoId: String, name: String, color: Int? = null, style: SnippetStyle? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val now = System.currentTimeMillis()
        
        if (color != null) {
            updateSnippetColor(trimmed, color)
        }
        if (style != null) {
            updateSnippetStyle(trimmed, style)
        }

        if (!snippetFirstSeenTimes.containsKey(trimmed)) {
            snippetFirstSeenTimes = snippetFirstSeenTimes + (trimmed to now)
            saveSnippetFirstSeenTimes()
        }
        
        photos = photos.map { photo ->
            if (photo.id == photoId) {
                if (photo.snippets.size < 6 && !photo.snippets.contains(trimmed)) {
                    photo.copy(snippets = photo.snippets + trimmed, snippetsAddedTime = now, surfacedTime = 0L)
                } else photo
            } else photo
        }
        savePhotos()
        reconcileSurfacedMemories()
    }
}
