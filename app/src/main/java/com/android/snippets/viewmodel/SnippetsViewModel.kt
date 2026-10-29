package com.android.snippets.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.data.backup.SnippetsBackupManager
import com.android.snippets.logic.SearchDirective
import com.android.snippets.logic.ParsedSearchQuery
import com.android.snippets.logic.SearchQueryParser
import com.android.snippets.logic.SnippetStyleHelper
import com.android.snippets.ui.util.PhotoCardExporter
import com.android.snippets.model.Photo
import com.android.snippets.ui.util.MediaSaver
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import com.android.snippets.logic.MemoryManager
import android.app.backup.BackupManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.widget.Toast
import android.graphics.BitmapFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.android.snippets.data.DatabasePreloader
import com.android.snippets.data.toEntity
import com.android.snippets.data.toPhoto
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.data.repository.CollectionRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen { Library, Memory, Settings, About, EmojiPicker, Stats, MonthlyRecap, YearlyRecap }
enum class SnippetSortType { New, Old, Emoji, AZ, Emoticons }
enum class PhotoSortType { DateNewest, DateOldest, MostSnippets, LeastSnippets, MostStarred, LeastStarred }
enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class DisplayMode { Day, Week, Month }
enum class SnippetStyle { Default, Thin, Cursive, Mono, Serif, Spaced, Bold, FlexHeavy, FlexWide, FlexSlant, FlexGrade, Flex }

typealias SearchDirective = com.android.snippets.logic.SearchDirective
typealias ParsedSearchQuery = com.android.snippets.logic.ParsedSearchQuery


@HiltViewModel
class SnippetsViewModel @Inject constructor(
    application: Application,
    val photoRepository: PhotoRepository,
    val collectionRepository: CollectionRepository,
    val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: SnippetsBackupManager
) : AndroidViewModel(application) {

    companion object {
        fun matchSearchDirective(rawToken: String): SearchDirective? =
            SearchQueryParser.matchSearchDirective(rawToken)

        fun parseSearchQuery(query: String): ParsedSearchQuery =
            SearchQueryParser.parseSearchQuery(query)

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

        const val MEMORY_REMINDER_PREFIX = MemoryManager.MEMORY_REMINDER_PREFIX
        const val MEMORIES_PER_CYCLE = MemoryManager.MEMORIES_PER_CYCLE
        const val CYCLE_MORNING_HOUR = MemoryManager.CYCLE_MORNING_HOUR
        const val CYCLE_EVENING_HOUR = MemoryManager.CYCLE_EVENING_HOUR
        const val CYCLE_WINDOW_HOURS = MemoryManager.CYCLE_WINDOW_HOURS
        const val NEW_MEMORY_NOTIFICATION_DELAY_HOURS = MemoryManager.NEW_MEMORY_NOTIFICATION_DELAY_HOURS
        const val VIEWED_MEMORY_VISIBLE_HOURS = MemoryManager.VIEWED_MEMORY_VISIBLE_HOURS
        const val RECENTLY_VIEWED_MEMORY_HOURS = MemoryManager.RECENTLY_VIEWED_MEMORY_HOURS
        const val VIEWED_MEMORY_RESET_DAYS = MemoryManager.VIEWED_MEMORY_RESET_DAYS
        const val SURFACED_MEMORY_SPACING_HOURS = MemoryManager.SURFACED_MEMORY_SPACING_HOURS
        const val TEST_NOTIFICATION_DELAY_SECONDS = MemoryManager.TEST_NOTIFICATION_DELAY_SECONDS
        const val TEST_NOTIFICATION_PHOTO_ID = MemoryManager.TEST_NOTIFICATION_PHOTO_ID

        val NEW_MEMORY_WAIT_MS: Long get() = MemoryManager.NEW_MEMORY_WAIT_MS
        val VIEWED_MEMORY_VISIBLE_MS: Long get() = MemoryManager.VIEWED_MEMORY_VISIBLE_MS
        val RECENTLY_VIEWED_MEMORY_MS: Long get() = MemoryManager.RECENTLY_VIEWED_MEMORY_MS
        val VIEWED_MEMORY_RESET_MS: Long get() = MemoryManager.VIEWED_MEMORY_RESET_MS
        val SURFACED_MEMORY_SPACING_MS: Long get() = MemoryManager.SURFACED_MEMORY_SPACING_MS
        val CYCLE_WINDOW_MS: Long get() = MemoryManager.CYCLE_WINDOW_MS

                val EMOTICONS_LIST: List<String> get() = SnippetStyleHelper.EMOTICONS_LIST

        fun getCurrentCycleStart(now: Long = System.currentTimeMillis()): Long =
            MemoryManager.getCurrentCycleStart(now)

        fun getNextCycleStart(time: Long = System.currentTimeMillis()): Long =
            MemoryManager.getNextCycleStart(time)
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val photosFlow: StateFlow<List<Photo>> = photoRepository.allPhotosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val collectionsFlow: StateFlow<List<com.android.snippets.data.CollectionEntity>> = collectionRepository.allCollectionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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
    val carouselLazyListState = androidx.compose.foundation.lazy.LazyListState()
    
    var activePhotoId by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    
    var isSearching by mutableStateOf(false)
        private set
    var isFiltering by mutableStateOf(false)
        private set
    var isBusy by mutableStateOf(false)
    var isAddingPhotos by mutableStateOf(false)
    var isCuratingMemories by mutableStateOf(false)

    var selectedFilterSnippets by mutableStateOf<List<String>>(emptyList())

    fun scrollToTop() {
        viewModelScope.launch {
            libraryListStates.values.forEach { state ->
                try {
                    state.scrollToItem(0)
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleFilterSnippet(snippet: String) {
        val wasSelected = selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
        if (wasSelected) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
            scrollToTop()
        } else {
            if (selectedFilterSnippets.size >= 6) {
                showSnackbar("Upto 6 Chips!")
                return
            }
            selectedFilterSnippets = selectedFilterSnippets + snippet
        }
        saveFilterState()
    }

    fun clearFilterSnippets() {
        val hadFilters = selectedFilterSnippets.isNotEmpty()
        selectedFilterSnippets = emptyList()
        if (hadFilters) {
            scrollToTop()
        }
        saveFilterState()
    }

    fun getMatchingSnippetsCount(photo: Photo): Int {
        return if (selectedFilterSnippets.isNotEmpty()) {
            photo.snippets.count { s -> selectedFilterSnippets.any { it.equals(s, ignoreCase = true) } }
        } else {
            photo.snippets.size
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

    var snippetFirstSeenTimes by mutableStateOf<Map<String, Long>>(emptyMap())
        private set
    
    // Filter Context
    var filteringCategory by mutableStateOf<String?>(null)

    // Recap State
    var recapTitle by mutableStateOf("This Month")
        private set
    var recapPhotos by mutableStateOf<List<Photo>>(emptyList())
        private set
    var isYearlyRecap by mutableStateOf(false)
        private set
    var recapMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH))
    var recapYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var lastSeenYearlyRecapYear by androidx.compose.runtime.mutableIntStateOf(-1)
        private set
    var lastSeenMonthlyRecapPeriod by mutableStateOf("")
        private set

    // Snackbar State
    var snackbarMessage by mutableStateOf<String?>(null)
    var snackbarActionLabel by mutableStateOf<String?>(null)
    var onSnackbarAction by mutableStateOf<(() -> Unit)?>(null)
    var snackbarToken by mutableLongStateOf(0L)

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        snackbarMessage = message
        snackbarActionLabel = actionLabel
        onSnackbarAction = onAction
        snackbarToken++
    }

    fun requestAddSnippet(
        photo: Photo,
        onShowNameModal: (Photo) -> Unit,
        onShowAddModal: (Photo) -> Unit
    ) {
        val targetPhoto = photos.find { it.id == photo.id } ?: photo
        validateAndExecuteAddSnippet(
            photo = targetPhoto,
            showSnackbar = { message, actionLabel, onAction ->
                showSnackbar(message, actionLabel, onAction)
            },
            onShowNameModal = { p ->
                val latest = photos.find { it.id == p.id } ?: p
                onShowNameModal(latest)
            },
            onShowAddModal = onShowAddModal
        )
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
    var expandLabelIcon by mutableStateOf(false)
        private set
    var expandCalendarIcon by mutableStateOf(false)
        private set
    var expandLocationIcon by mutableStateOf(false)
        private set

    var notificationReminderEnabled by mutableStateOf(false)
        private set
    var notificationReminderHour by mutableStateOf(9)
        private set
    var notificationReminderMinute by mutableStateOf(0)
        private set


    var autoBackupSchedule by mutableStateOf("Disabled")
        private set
    var showFilterSheet by mutableStateOf(false)


    val separateCollectionSort = true
    
    
    fun navigateEmojiPicker(collection: String? = null) {
        editingCollectionIcon = collection
        previousScreen = currentScreen
        currentScreen = Screen.EmojiPicker
    }





    
    fun updateCollectionIcon(collection: String, iconOrEmoji: String) {
        collectionIcons = collectionIcons + (collection to iconOrEmoji)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val existing = collectionRepository.getCollectionByName(collection)
            if (existing != null) {
                collectionRepository.updateCollection(existing.copy(icon = iconOrEmoji))
            } else {
                collectionRepository.insertCollection(com.android.snippets.data.CollectionEntity(name = collection, icon = iconOrEmoji))
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


    fun startCollectionAssignment(collection: String) {
        pendingCollectionAssignment = collection
        selectedPhotoIds = emptySet()
        currentScreen = Screen.Library
    }

    fun startCollectionRemoval(collection: String) {
        pendingCollectionRemoval = collection
        selectedPhotoIds = emptySet()
        currentScreen = Screen.Library
    }


    fun confirmCollectionAssignment() {
        val collection = pendingCollectionAssignment ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            val now = System.currentTimeMillis()
            photos = photos.map { photo ->
                if (selectedPhotoIds.contains(photo.id)) {
                    if (collection == "Favorites") {
                        photo.copy(isFavorite = true)
                    } else {
                        if (!photo.collections.contains(collection)) {
                            photo.copy(
                                collections = photo.collections + collection,
                                snippetsAddedTime = if (photo.snippetsAddedTime == 0L) now else photo.snippetsAddedTime
                            )
                        } else photo
                    }
                } else photo
            }
            savePhotos()
            
            if (collection != "Favorites") {
                showSnackbar(
                    message = "Added to collection",
                    actionLabel = "View",
                    onAction = {
                        libraryCurrentTab = collection
                        navigateLibrary()
                    }
                )
            }
        }
        pendingCollectionAssignment = null
        selectedPhotoIds = emptySet()
        libraryCurrentTab = collection
        navigateLibrary()
    }

    fun confirmCollectionRemoval() {
        val collection = pendingCollectionRemoval ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = photos.map { photo ->
                if (selectedPhotoIds.contains(photo.id)) {
                    if (collection == "Favorites") {
                        photo.copy(isFavorite = false)
                    } else {
                        photo.copy(collections = photo.collections - collection)
                    }
                } else photo
            }
            savePhotos()
            
            if (collection != "Favorites") {
                showSnackbar(message = "Removed from collection")
            }
        }
        pendingCollectionRemoval = null
        selectedPhotoIds = emptySet()
        libraryCurrentTab = collection
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

    val parsedSearchQuery: ParsedSearchQuery by derivedStateOf {
        parseSearchQuery(searchQuery)
    }

    val currentSearchSortType: PhotoSortType? by derivedStateOf {
        parsedSearchQuery.directives.lastOrNull()?.sortType
    }

    private var _photoSortType = mutableStateOf(PhotoSortType.DateNewest)
    private var _collectionPhotoSortTypes = mutableStateOf<Map<String, PhotoSortType>>(emptyMap())
    
    var photoSortType: PhotoSortType
        get() = currentSearchSortType ?: _photoSortType.value
        set(value) {
            _photoSortType.value = value
            libraryListStates["Library"] = androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState()
            saveFilterState()
        }
        
    fun getPhotoSortTypeFor(tabName: String): PhotoSortType {
        currentSearchSortType?.let { return it }
        if (tabName == "Library") return _photoSortType.value
        return _collectionPhotoSortTypes.value[tabName] ?: PhotoSortType.DateNewest
    }

    fun setPhotoSortTypeFor(tabName: String, sortType: PhotoSortType) {
        if (tabName == "Library") {
            _photoSortType.value = sortType
        } else {
            _collectionPhotoSortTypes.value = _collectionPhotoSortTypes.value + (tabName to sortType)
        }
        libraryListStates[tabName] = androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState()
        saveFilterState()
    }
    
    fun sortPhotos(baseList: List<Photo>, sort: PhotoSortType): List<Photo> {
        return when (sort) {
            PhotoSortType.DateNewest -> baseList.sortedByDescending { it.date }
            PhotoSortType.DateOldest -> baseList.sortedBy { it.date }
            PhotoSortType.MostSnippets -> baseList.sortedWith(compareByDescending<Photo> { it.snippets.size }.thenByDescending { it.date })
            PhotoSortType.LeastSnippets -> baseList.sortedWith(compareBy<Photo> { it.snippets.size }.thenByDescending { it.date })
            PhotoSortType.MostStarred -> baseList.sortedWith(compareByDescending<Photo> { it.rating }.thenByDescending { it.date })
            PhotoSortType.LeastStarred -> baseList.sortedWith(compareBy<Photo> { it.rating }.thenByDescending { it.date })
        }
    }
    
    val pagedPhotos: Flow<PagingData<com.android.snippets.data.PhotoEntity>> = Pager(
        config = PagingConfig(pageSize = 30, enablePlaceholders = false)
    ) {
        photoRepository.getPagingPhotos()
    }.flow

    // State to track if the next added photo should be a favorite
    var pendingFavoriteIntent by mutableStateOf(false)
    var pendingAddPhotoIntentToken by mutableStateOf(0L)

    private val storageMutex = Mutex()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { userPrefs ->
                themePreference = try { ThemePreference.valueOf(userPrefs.themePreference) } catch (e: Exception) { ThemePreference.SYSTEM }
                useDynamicColors = userPrefs.useDynamicColors
                expandLabelIcon = userPrefs.expandLabelIcon
                expandCalendarIcon = userPrefs.expandCalendarIcon
                expandLocationIcon = userPrefs.expandLocationIcon
                notificationReminderEnabled = userPrefs.notificationReminderEnabled
                notificationReminderHour = userPrefs.notificationReminderHour
                notificationReminderMinute = userPrefs.notificationReminderMinute
                lastSeenYearlyRecapYear = userPrefs.lastSeenYearlyRecapYear
                lastSeenMonthlyRecapPeriod = userPrefs.lastSeenMonthlyRecapPeriod
                _snippetSortType.value = try { SnippetSortType.valueOf(userPrefs.snippetSortType) } catch (e: Exception) { SnippetSortType.New }
                _isSortAscending.value = userPrefs.isSortAscending
                selectedFilterSnippets = userPrefs.selectedFilterSnippets.toList()
                _displayMode.value = try { DisplayMode.valueOf(userPrefs.displayMode) } catch (e: Exception) { DisplayMode.Day }
                _photoSortType.value = try { PhotoSortType.valueOf(userPrefs.photoSortType) } catch (e: Exception) { PhotoSortType.DateNewest }

                if (userPrefs.collectionDisplayModesJson.isNotBlank() && userPrefs.collectionDisplayModesJson != "{}") {
                    _collectionDisplayModes.value = try { json.decodeFromString(userPrefs.collectionDisplayModesJson) } catch (e: Exception) { emptyMap() }
                }
                if (userPrefs.collectionPhotoSortTypesJson.isNotBlank() && userPrefs.collectionPhotoSortTypesJson != "{}") {
                    _collectionPhotoSortTypes.value = try { json.decodeFromString(userPrefs.collectionPhotoSortTypesJson) } catch (e: Exception) { emptyMap() }
                }

                if (autoBackupSchedule != userPrefs.autoBackupSchedule) {
                    autoBackupSchedule = userPrefs.autoBackupSchedule
                    scheduleAutoBackup()
                }
            }
        }
        observeRepositoryFlows()
        loadAllData()
    }

    private fun observeRepositoryFlows() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            collectionRepository.allCollectionsFlow.collect { entities ->
                val names = entities.sortedBy { it.orderIndex }.map { it.name }
                val icons = entities.mapNotNull { it.icon?.let { icon -> it.name to icon } }.toMap()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (userCollections != names) {
                        userCollections = names
                    }
                    if (collectionIcons != icons) {
                        collectionIcons = icons
                    }
                }
            }
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            photoRepository.allPhotosFlow.collect { roomPhotos ->
                if (storageMutex.isLocked) return@collect
                val processedPhotos = roomPhotos.map { p ->
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
                        snippetsAddedTime = if (p.snippetsAddedTime != 0L) p.snippetsAddedTime else (if ((p.snippets ?: emptyList()).isNotEmpty()) p.date else 0L),
                        surfacedTime = p.surfacedTime,
                        isFavorite = p.isFavorite,
                        isLibraryUpload = p.isLibraryUpload,
                        collections = p.collections ?: emptyList(),
                        widthPx = widthPx,
                        heightPx = heightPx,
                        isPublic = p.isPublic,
                        locationLink = p.locationLink,
                        locationName = p.locationName,
                        rating = p.rating,
                        name = p.name
                    )
                }.sortedByDescending { it.date }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (photos != processedPhotos) {
                        photos = processedPhotos
                        val rebuiltFirstSeen = rebuildSnippetFirstSeenTimes(processedPhotos)
                        snippetFirstSeenTimes = rebuiltFirstSeen
                    }
                }
            }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                DatabasePreloader.preloadIfNecessary(getApplication(), photoRepository, collectionRepository)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val roomPhotos = try {
                photoRepository.getAllPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val collectionEntities = try {
                collectionRepository.getAllCollections()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            val roomCollections = collectionEntities.map { it.name }
            val loadedIcons = collectionEntities.mapNotNull { it.icon?.let { icon -> it.name to icon } }.toMap()

            val processedPhotos = roomPhotos.map { p ->
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
                    snippetsAddedTime = if (p.snippetsAddedTime != 0L) p.snippetsAddedTime else (if ((p.snippets ?: emptyList()).isNotEmpty()) p.date else 0L),
                    surfacedTime = p.surfacedTime,
                    isFavorite = p.isFavorite,
                    isLibraryUpload = p.isLibraryUpload,
                    collections = p.collections ?: emptyList(),
                    widthPx = widthPx,
                    heightPx = heightPx,
                    isPublic = p.isPublic,
                    locationLink = p.locationLink,
                    locationName = p.locationName,
                    rating = p.rating,
                    name = p.name
                )
            }.sortedByDescending { it.date }

            val loadedFirstSeen = try {
                val currentPrefs = userPreferencesRepository.userPreferencesFlow.first()
                if (currentPrefs.snippetFirstSeenJson.isNotBlank() && currentPrefs.snippetFirstSeenJson != "{}") {
                    json.decodeFromString<Map<String, Long>>(currentPrefs.snippetFirstSeenJson)
                } else emptyMap()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }

            val rebuiltFirstSeen = rebuildSnippetFirstSeenTimes(processedPhotos)
            val mergedFirstSeen = rebuiltFirstSeen.mapValues { (snippet, rebuiltTime) ->
                loadedFirstSeen[snippet] ?: rebuiltTime
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                photos = processedPhotos
                userCollections = roomCollections
                collectionIcons = loadedIcons
                snippetFirstSeenTimes = mergedFirstSeen

                // Auto-heal any photos that were previously stored with a temporary content:// URI in a single batch
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val brokenPhotos = photos.filter { it.uriString.startsWith("content://") }
                    if (brokenPhotos.isNotEmpty()) {
                        var changed = false
                        val updatedList = photos.toMutableList()
                        brokenPhotos.forEach { p ->
                            val healedUri = saveToInternalStorage(android.net.Uri.parse(p.uriString))
                            if (healedUri != null) {
                                val (newW, newH) = extractImageDimensions(android.net.Uri.parse(healedUri))
                                val index = updatedList.indexOfFirst { it.id == p.id }
                                if (index != -1) {
                                    updatedList[index] = updatedList[index].copy(uriString = healedUri, widthPx = newW, heightPx = newH)
                                    changed = true
                                }
                            }
                        }
                        if (changed) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                photos = updatedList
                            }
                            savePhotos()
                        }
                    }

                    // Background resolution for photos with location link but missing place name
                    val needsLocationResolution = photos.filter { !it.locationLink.isNullOrBlank() && it.locationName.isNullOrBlank() }
                    if (needsLocationResolution.isNotEmpty()) {
                        var locationChanged = false
                        val currentPhotos = photos.toMutableList()
                        needsLocationResolution.forEach { p ->
                            val link = p.locationLink ?: return@forEach
                            val name = com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(link, getApplication())
                                ?: com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(link, getApplication())
                            if (!name.isNullOrBlank()) {
                                val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(name)
                                val idx = currentPhotos.indexOfFirst { it.id == p.id }
                                if (idx != -1 && currentPhotos[idx].locationName.isNullOrBlank()) {
                                    currentPhotos[idx] = currentPhotos[idx].copy(locationName = clean)
                                    locationChanged = true
                                }
                            }
                        }
                        if (locationChanged) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                photos = currentPhotos
                            }
                            savePhotos()
                        }
                    }
                }
                isInitialLoading = false

                // Clean up any filter snippets that no longer exist in any photo (prevents stuck empty state)
                val allExistingSnippets = processedPhotos.flatMap { it.snippets }.toSet()
                val validFilters = selectedFilterSnippets.filter { filter ->
                    allExistingSnippets.any { it.equals(filter, ignoreCase = true) }
                }
                if (validFilters.size != selectedFilterSnippets.size) {
                    selectedFilterSnippets = validFilters
                    saveFilterState()
                }

                if (mergedFirstSeen != loadedFirstSeen) {
                    saveSnippetFirstSeenTimes()
                }

                reconcileSurfacedMemories()
                reconcileMemoryNotifications()
                checkAndTriggerRecapOnLaunch()
            }
        }
    }

    private fun saveFilterState() {
        viewModelScope.launch {
            userPreferencesRepository.updateFilterState(
                snippetSortType = snippetSortType.name,
                isSortAscending = isSortAscending,
                selectedFilterSnippets = selectedFilterSnippets.toSet(),
                displayMode = _displayMode.value.name,
                collectionDisplayModesJson = json.encodeToString(_collectionDisplayModes.value),
                photoSortType = _photoSortType.value.name,
                collectionPhotoSortTypesJson = json.encodeToString(_collectionPhotoSortTypes.value)
            )
        }
    }

    private fun savePhotos(): kotlinx.coroutines.Job {
        val snapshot = photos // Capture on main thread immediately — avoids race if photos mutates before IO runs
        return viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                storageMutex.withLock {
                    try {
                        val currentDb = photoRepository.getAllPhotos()
                        val currentIds = snapshot.map { it.id }.toSet()
                        val deletedEntities = currentDb.filter { it.id !in currentIds }
                        if (deletedEntities.isNotEmpty()) {
                            photoRepository.deletePhotos(deletedEntities)
                        }
                        photoRepository.insertPhotos(snapshot)
                        BackupManager(getApplication()).dataChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
        val result = MemoryManager.reconcileSurfacedMemories(photos, now)
        if (result != null) {
            photos = result.first
            savePhotos()
            MemoryManager.scheduleNextCycleWork(getApplication())
        }
    }

    private fun saveCollections() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageMutex.withLock {
                try {
                    val currentCols = collectionRepository.getAllCollections()
                    val userColSet = userCollections.toSet()
                    val deletedCols = currentCols.filter { it.name !in userColSet }
                    if (deletedCols.isNotEmpty()) {
                        deletedCols.forEach { collectionRepository.deleteCollection(it) }
                    }
                    collectionRepository.insertCollections(
                        userCollections.mapIndexed { idx, name ->
                            val existing = currentCols.find { it.name == name }
                            com.android.snippets.data.CollectionEntity(
                                name = name,
                                icon = collectionIcons[name] ?: existing?.icon,
                                orderIndex = idx,
                                isPinned = existing?.isPinned ?: false
                            )
                        }
                    )
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
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(pref.name)
        }
    }

    fun updateDynamicColors(use: Boolean) {
        useDynamicColors = use
        viewModelScope.launch {
            userPreferencesRepository.updateUseDynamicColors(use)
        }
    }

    fun updateNotificationReminder(enabled: Boolean, hour: Int, minute: Int) {
        notificationReminderEnabled = enabled
        notificationReminderHour = hour
        notificationReminderMinute = minute
        viewModelScope.launch {
            userPreferencesRepository.updateNotificationReminder(enabled, hour, minute)
        }
    }








    fun addPhoto(uri: Uri, isFavorite: Boolean = false, locationLink: String? = null, locationName: String? = null) {
        isAddingPhotos = true
        val targetLocation = locationLink?.trim()?.takeIf { it.isNotBlank() }
        val immediateName = locationName?.trim()?.takeIf { it.isNotBlank() }
            ?: targetLocation?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it, getApplication()) }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val captureDate = extractCaptureDate(uri)
                val duplicate = photos.find { it.date == captureDate && it.uriString.contains(uri.lastPathSegment ?: "___") }
                
                if (duplicate != null) {
                    val healedUri = if (duplicate.uriString.startsWith("content://")) {
                        saveToInternalStorage(uri)
                    } else null

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (isFavorite && !duplicate.isFavorite) {
                            toggleFavorite(duplicate.id)
                        }

                        var updatedPhoto = duplicate
                        if (healedUri != null) {
                            // Extract dimensions from the newly saved internal file
                            val (newW, newH) = extractImageDimensions(Uri.parse(healedUri))
                            updatedPhoto = updatedPhoto.copy(
                                uriString = healedUri,
                                widthPx = newW,
                                heightPx = newH
                            )
                        }
                        
                        if (!targetLocation.isNullOrBlank()) {
                            updatedPhoto = updatedPhoto.copy(
                                locationLink = targetLocation,
                                locationName = immediateName ?: updatedPhoto.locationName
                            )
                        }
                        
                        if (updatedPhoto != duplicate) {
                            photos = photos.map {
                                if (it.id == duplicate.id) updatedPhoto else it
                            }
                            savePhotos()
                        }

                        if (!targetLocation.isNullOrBlank()) {
                            openDetail(duplicate.id, tab = "Library", overrideReturnScreen = Screen.Library)
                        }

                        isAddingPhotos = false
                    }

                    if (!targetLocation.isNullOrBlank() && immediateName == null && (targetLocation.startsWith("http://", ignoreCase = true) || targetLocation.startsWith("https://", ignoreCase = true))) {
                        val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(targetLocation, getApplication())
                        if (!resolved.isNullOrBlank()) {
                            val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                photos = photos.map {
                                    if (it.id == duplicate.id && it.locationName.isNullOrBlank()) {
                                        it.copy(locationName = clean)
                                    } else it
                                }
                                savePhotos()
                            }
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
                    snippetsAddedTime = 0L,
                    isFavorite = isFavorite,
                    isLibraryUpload = true,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    locationLink = targetLocation,
                    locationName = immediateName,
                    rating = 0
                )
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    photos = (listOf(newPhoto) + photos).sortedByDescending { it.date }
                    savePhotos()

                    if (!targetLocation.isNullOrBlank()) {
                        openDetail(newPhoto.id, tab = "Library", overrideReturnScreen = Screen.Library)
                    }

                    isAddingPhotos = false
                }

                if (!targetLocation.isNullOrBlank() && immediateName == null && (targetLocation.startsWith("http://", ignoreCase = true) || targetLocation.startsWith("https://", ignoreCase = true))) {
                    val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(targetLocation, getApplication())
                    if (!resolved.isNullOrBlank()) {
                        val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            photos = photos.map {
                                if (it.id == newPhoto.id && it.locationName.isNullOrBlank()) {
                                    it.copy(locationName = clean)
                                } else it
                            }
                            savePhotos()
                        }
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isAddingPhotos = false
                }
            }
        }
    }

    fun addPhotoToLibrary(uri: Uri, locationLink: String? = null, locationName: String? = null) {
        addPhoto(uri = uri, locationLink = locationLink, locationName = locationName)
    }

    fun addPhotoToCollection(uri: Uri, collection: String, locationLink: String? = null, locationName: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val captureDate = extractCaptureDate(uri)
            val targetLocation = locationLink?.trim()?.takeIf { it.isNotBlank() }
            val immediateName = locationName?.trim()?.takeIf { it.isNotBlank() }
                ?: targetLocation?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it, getApplication()) }
            
            val duplicate = photos.find { it.date == captureDate && it.uriString.contains(uri.lastPathSegment ?: "___") }
            if (duplicate != null) {
                val healedUri = if (duplicate.uriString.startsWith("content://")) {
                    saveToInternalStorage(uri)
                } else null

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    var updatedPhoto = duplicate
                    if (!duplicate.collections.contains(collection)) {
                        updatedPhoto = updatedPhoto.copy(collections = duplicate.collections + collection)
                    }
                    if (!targetLocation.isNullOrBlank() && updatedPhoto.locationLink.isNullOrBlank()) {
                        updatedPhoto = updatedPhoto.copy(
                            locationLink = targetLocation,
                            locationName = immediateName ?: updatedPhoto.locationName
                        )
                    }
                    if (healedUri != null) {
                        // Extract dimensions from the healed internal file
                        val (newW, newH) = extractImageDimensions(Uri.parse(healedUri))
                        updatedPhoto = updatedPhoto.copy(
                            uriString = healedUri,
                            widthPx = newW,
                            heightPx = newH
                        )
                    }
                    if (updatedPhoto != duplicate) {
                        photos = photos.map {
                            if (it.id == duplicate.id) updatedPhoto else it
                        }
                        savePhotos()
                    }
                }

                if (!targetLocation.isNullOrBlank() && immediateName == null && (targetLocation.startsWith("http://", ignoreCase = true) || targetLocation.startsWith("https://", ignoreCase = true))) {
                    val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(targetLocation, getApplication())
                    if (!resolved.isNullOrBlank()) {
                        val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            photos = photos.map {
                                if (it.id == duplicate.id && it.locationName.isNullOrBlank()) {
                                    it.copy(locationName = clean)
                                } else it
                            }
                            savePhotos()
                        }
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
                collections = listOf(collection),
                isLibraryUpload = true,
                widthPx = widthPx,
                heightPx = heightPx,
                locationLink = targetLocation,
                locationName = immediateName
            )
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                photos = (listOf(newPhoto) + photos).sortedByDescending { it.date }
                savePhotos()
            }

            if (!targetLocation.isNullOrBlank() && immediateName == null && (targetLocation.startsWith("http://", ignoreCase = true) || targetLocation.startsWith("https://", ignoreCase = true))) {
                val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(targetLocation, getApplication())
                if (!resolved.isNullOrBlank()) {
                    val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        photos = photos.map {
                            if (it.id == newPhoto.id && it.locationName.isNullOrBlank()) {
                                it.copy(locationName = clean)
                            } else it
                        }
                        savePhotos()
                    }
                }
            }
        }
    }

    private fun extractImageDimensions(uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val path = uri.path
            if (path != null && !uri.toString().startsWith("content:")) {
                val file = File(path)
                if (file.exists()) {
                    java.io.FileInputStream(file).use { input ->
                        BitmapFactory.decodeStream(input, null, options)
                    }
                } else {
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options) }
                }
            } else {
                val resolver = getApplication<Application>().contentResolver
                resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options) }
            }
            val width = options.outWidth.takeIf { it > 0 } ?: 0
            val height = options.outHeight.takeIf { it > 0 } ?: 0
            width to height
        } catch (_: Exception) {
            0 to 0
        }
    }



    fun createCollection(collection: String, openAfterCreate: Boolean = true) {
        val emoji = collection.trim()
        if (emoji.isNotBlank() && userCollections.none { it.equals(emoji, ignoreCase = true) }) {
            userCollections = userCollections + emoji
            saveCollections()
            if (openAfterCreate) {
                libraryCurrentTab = emoji
                navigateLibrary()
            }
        }
    }

    fun deleteCollection(collection: String) {
        if (collection.isBlank()) return

        userCollections = userCollections.filter { it != collection }
        
        // Cleanup per-collection settings
        _collectionDisplayModes.value = _collectionDisplayModes.value - collection
        _collectionPhotoSortTypes.value = _collectionPhotoSortTypes.value - collection
        
        saveCollections()
        saveFilterState()
        collectionIcons = collectionIcons - collection

        var photosChanged = false
        photos = photos.map { photo ->
            if (photo.collections.contains(collection)) {
                photosChanged = true
                photo.copy(collections = photo.collections - collection)
            } else photo
        }
        if (photosChanged) savePhotos()

        if (activeCollection == collection) {
            navigateLibrary()
        }
        if (pendingCollectionAssignment == collection) {
            pendingCollectionAssignment = null
        }
        if (selectedFilterSnippets.contains(collection)) {
            selectedFilterSnippets = selectedFilterSnippets.filter { it != collection }
            saveFilterState()
        }
    }


    private fun reconcileMemoryNotifications() {
        MemoryManager.reconcileMemoryNotifications(getApplication(), photos)
    }

    private fun cancelMemoryNotification(photoId: String) {
        MemoryManager.cancelMemoryNotification(getApplication(), photoId)
    }

    fun testNotification() {
        requestNotificationPermission = true
        MemoryManager.testNotification(getApplication(), photos)
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

    private fun deletePhotoFile(uriString: String) {
        try {
            if (uriString.startsWith("file://") || uriString.contains("/files/photos/")) {
                val path = if (uriString.startsWith("file://")) {
                    uriString.substring(7)
                } else {
                    uriString
                }
                val file = File(path)
                if (file.exists()) {
                    val deleted = file.delete()
                    android.util.Log.d("SnippetsViewModel", "Deleted photo file: ${file.absolutePath}, success: $deleted")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSnippets(photoId: String, text: String) {
        val targetPhoto = photos.find { it.id == photoId }
        if (targetPhoto != null && targetPhoto.snippets.size >= 6) {
            android.widget.Toast.makeText(getApplication(), "Maximum 6 snippets per photo", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return

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

        if (selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
            saveFilterState()
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

        if (selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
            saveFilterState()
        }
    }

    fun toggleFavorite(photoId: String) {
        photos = photos.map {
            if (it.id == photoId) it.copy(isFavorite = !it.isFavorite) else it
        }
        savePhotos()
    }

    fun setPhotoRating(photoId: String, rating: Int) {
        photos = photos.map {
            if (it.id == photoId) it.copy(rating = rating) else it
        }
        savePhotos()
    }

    fun togglePublicStatus(photoId: String) {
        photos = photos.map {
            if (it.id == photoId) it.copy(isPublic = !it.isPublic) else it
        }
        savePhotos()
    }

    fun updateLocationLink(photoId: String, link: String?, name: String? = null) {
        val trimmedLink = link?.trim()?.takeIf { it.isNotBlank() }
        val immediateName = name?.trim()?.takeIf { it.isNotBlank() }
            ?: trimmedLink?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it, getApplication()) }

        photos = photos.map {
            if (it.id == photoId) it.copy(locationLink = trimmedLink, locationName = immediateName) else it
        }
        savePhotos()

        if (trimmedLink != null && immediateName == null && (trimmedLink.startsWith("http://", ignoreCase = true) || trimmedLink.startsWith("https://", ignoreCase = true))) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(trimmedLink, getApplication())
                if (!resolved.isNullOrBlank()) {
                    val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        photos = photos.map {
                            if (it.id == photoId && it.locationName.isNullOrBlank()) {
                                it.copy(locationName = clean)
                            } else it
                        }
                        savePhotos()
                    }
                }
            }
        }
    }

    fun updatePhotoName(photoId: String, name: String?) {
        val trimmed = name?.trim()?.takeIf { it.isNotBlank() }?.take(25)
        photos = photos.map {
            if (it.id == photoId) it.copy(name = trimmed) else it
        }
        savePhotos()
    }
    fun deletePhoto(photoId: String, unpublish: Boolean = true) {
        val deletedPhoto = photos.find { it.id == photoId }
        deletedPhoto?.let { deletePhotoFile(it.uriString) }
        photos = photos.filter { it.id != photoId }
        savePhotos()
        cancelMemoryNotification(photoId)
        closeDetail()

        deletedPhoto?.snippets?.forEach { snippet ->
            if (photos.none { photo -> photo.snippets.contains(snippet) }) {
                snippetFirstSeenTimes = snippetFirstSeenTimes - snippet
                saveSnippetFirstSeenTimes()
                if (selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }) {
                    selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
                    saveFilterState()
                }
            }
        }
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
        photos.filter { selectedPhotoIds.contains(it.id) }.forEach { photo ->
            deletePhotoFile(photo.uriString)
        }
        photos = photos.filter { !selectedPhotoIds.contains(it.id) }
        savePhotos()
        selectedPhotoIds.forEach { cancelMemoryNotification(it) }
        clearSelection()
        isBusy = false
    }

    fun toggleSelectedInCollection(collection: String) {
        isBusy = true
        val now = System.currentTimeMillis()
        photos = photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                val updatedCollections = if (photo.collections.contains(collection)) {
                    photo.collections - collection
                } else {
                    photo.collections + collection
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

    fun bulkAddSnippet(photoIds: Set<String>, text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank() || photoIds.isEmpty()) return

        val now = System.currentTimeMillis()
        if (!snippetFirstSeenTimes.containsKey(trimmedText)) {
            snippetFirstSeenTimes = snippetFirstSeenTimes + (trimmedText to now)
            saveSnippetFirstSeenTimes()
        }

        var skippedCount = 0
        photos = photos.map { photo ->
            if (photoIds.contains(photo.id)) {
                if (photo.snippets.size >= 6 && !photo.snippets.contains(trimmedText)) {
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
        savePhotos()
        reconcileSurfacedMemories()

        if (skippedCount > 0) {
            showSnackbar("Added '$trimmedText' to selected photos ($skippedCount skipped because they reached the limit of 6 snippets)")
        } else {
            showSnackbar("Added '$trimmedText' to ${photoIds.size} photos")
        }
        clearSelection()
    }

    fun bulkRemoveSnippet(photoIds: Set<String>, snippet: String) {
        if (photoIds.isEmpty() || snippet.isBlank()) return
        val trimmedSnippet = snippet.trim()

        var emptiedMemoryIds = mutableListOf<String>()
        photos = photos.map { photo ->
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
        savePhotos()
        emptiedMemoryIds.forEach { cancelMemoryNotification(it) }

        if (photos.none { photo -> photo.snippets.contains(trimmedSnippet) }) {
            snippetFirstSeenTimes = snippetFirstSeenTimes - trimmedSnippet
            saveSnippetFirstSeenTimes()
        }

        if (selectedFilterSnippets.any { it.equals(trimmedSnippet, ignoreCase = true) }) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(trimmedSnippet, ignoreCase = true) }
            saveFilterState()
        }

        showSnackbar("Removed '$trimmedSnippet' from selected photos")
        clearSelection()
    }

    fun bulkClearSnippets(photoIds: Set<String>) {
        if (photoIds.isEmpty()) return
        
        photos = photos.map { photo ->
            if (photoIds.contains(photo.id)) {
                cancelMemoryNotification(photo.id)
                photo.copy(
                    snippets = emptyList(),
                    snippetsAddedTime = 0L
                )
            } else photo
        }
        savePhotos()
        
        val allExistingSnippets = photos.flatMap { it.snippets }.toSet()
        snippetFirstSeenTimes = snippetFirstSeenTimes.filterKeys { it in allExistingSnippets }
        saveSnippetFirstSeenTimes()
        
        selectedFilterSnippets = selectedFilterSnippets.filter { it in allExistingSnippets }
        saveFilterState()
        
        showSnackbar("Cleared all snippets from selected photos")
        clearSelection()
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

    fun downloadPhotoCard(context: Context, photo: Photo, isDark: Boolean, bgColor: Int) {
        viewModelScope.launch {
            val pureSnippets = getPureSnippets(photo)
            PhotoCardExporter.downloadPhotoCard(context, photo, pureSnippets, isDark, bgColor)
        }
    }

    fun sharePhotoCard(context: Context, photo: Photo, isDark: Boolean, bgColor: Int) {
        viewModelScope.launch {
            val pureSnippets = getPureSnippets(photo)
            PhotoCardExporter.sharePhotoCard(context, photo, pureSnippets, isDark, bgColor)
        }
    }

    fun shareSelectedPhotos(context: Context) {
        if (selectedPhotoIds.isEmpty()) return
        
        viewModelScope.launch {
            val isDarkTheme = when (themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            val photosToShare = selectedPhotoIds.mapNotNull { photoId ->
                photos.find { it.id == photoId }?.let { it to getPureSnippets(it) }
            }
            if (PhotoCardExporter.shareMultipleCards(context, photosToShare, isDarkTheme)) {
                clearSelection()
            }
        }
    }
    fun openDetail(id: String, tab: String? = null, overrideReturnScreen: Screen? = null) {
        activePhotoId = id
        previousScreen = currentScreen
        currentScreen = Screen.Library
        tab?.let { libraryCurrentTab = it }
    }

    fun closeDetail() {
        activePhotoId = null
    }

    fun navigateBack() {
        when (currentScreen) {
            Screen.Memory,
            Screen.Settings,
            Screen.About,
            Screen.Stats -> navigateLibrary()
            Screen.EmojiPicker,
            Screen.MonthlyRecap,
            Screen.YearlyRecap -> {
                currentScreen = previousScreen
            }
            Screen.Library -> {
                if (activePhotoId != null) {
                    activePhotoId = null
                }
            }
        }
    }


    fun getPhotosForPeriod(month: Int?, year: Int): List<Photo> {
        val matched = photos.filter { photo ->
            val c = Calendar.getInstance().apply { timeInMillis = photo.date }
            val matchesYear = c.get(Calendar.YEAR) == year
            val matchesMonth = month == null || c.get(Calendar.MONTH) == month
            
            val matchesPeriod = matchesYear && matchesMonth
            if (matchesPeriod) {
                if (month == null) {
                    photo.isFavorite || photo.rating >= 3
                } else {
                    true
                }
            } else {
                false
            }
        }
        return matched.sortedWith(
            compareByDescending<Photo> { it.isFavorite }
                .thenByDescending { it.rating }
                .thenByDescending { it.snippets.size }
                .thenByDescending { it.date }
        )
    }

    fun triggerMonthlyRecap(month: Int? = null, year: Int? = null) {
        val cal = Calendar.getInstance()
        val targetMonth = month ?: if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
            (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.get(Calendar.MONTH)
        } else {
            cal.get(Calendar.MONTH)
        }
        val targetYear = year ?: if (cal.get(Calendar.DAY_OF_MONTH) == 1 && targetMonth == 11) {
            cal.get(Calendar.YEAR) - 1
        } else {
            cal.get(Calendar.YEAR)
        }

        val monthCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, targetMonth)
            set(Calendar.YEAR, targetYear)
        }
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(monthCal.time)

        isYearlyRecap = false
        recapTitle = "This $monthName"
        recapPhotos = getPhotosForPeriod(month = targetMonth, year = targetYear)
        recapMonth = targetMonth
        recapYear = targetYear
        previousScreen = currentScreen
        currentScreen = Screen.MonthlyRecap
    }

    fun triggerYearlyRecap(year: Int? = null) {
        val cal = Calendar.getInstance()
        val targetYear = year ?: (cal.get(Calendar.YEAR) - 1).coerceAtLeast(2020)

        isYearlyRecap = true
        recapTitle = "This $targetYear"
        recapPhotos = getPhotosForPeriod(month = null, year = targetYear)
        recapYear = targetYear
        previousScreen = currentScreen
        currentScreen = Screen.YearlyRecap
    }

    fun checkAndTriggerRecapOnLaunch() {
        val now = Calendar.getInstance()
        val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val currentYear = now.get(Calendar.YEAR)

        if (dayOfYear == 1) {
            val targetYear = currentYear - 1
            if (lastSeenYearlyRecapYear != targetYear) {
                val yearlyPhotos = getPhotosForPeriod(month = null, year = targetYear)
                if (yearlyPhotos.isNotEmpty()) {
                    lastSeenYearlyRecapYear = targetYear
                    viewModelScope.launch {
                        userPreferencesRepository.updateLastSeenYearlyRecapYear(targetYear)
                    }
                    triggerYearlyRecap(targetYear)
                }
            }
        } else if (dayOfMonth == 1) {
            val prevCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            val targetMonth = prevCal.get(Calendar.MONTH)
            val targetYear = prevCal.get(Calendar.YEAR)
            val recapKey = "${targetYear}_${targetMonth}"
            if (lastSeenMonthlyRecapPeriod != recapKey) {
                val monthlyPhotos = getPhotosForPeriod(month = targetMonth, year = targetYear)
                if (monthlyPhotos.isNotEmpty()) {
                    lastSeenMonthlyRecapPeriod = recapKey
                    viewModelScope.launch {
                        userPreferencesRepository.updateLastSeenMonthlyRecapPeriod(recapKey)
                    }
                    triggerMonthlyRecap(targetMonth, targetYear)
                }
            }
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


    fun openMemory(memories: List<Photo>, index: Int) {
        if (memories.isEmpty()) return
        activeMemoriesSnapshot = memories
        currentMemoryIndex = index.coerceIn(0, activeMemoriesSnapshot.size - 1)
        previousScreen = currentScreen
        currentScreen = Screen.Memory
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

        // Skip entirely if already up-to-date — avoids any list rebuild
        if (photo.isViewed && photo.snippetsAddedTime <= photo.lastViewedTime) {
            return
        }

        // Rebuild list synchronously on the Main thread to prevent race conditions
        val updated = photos.map {
            if (it.id == id) it.copy(isViewed = true, lastViewedTime = now) else it
        }
        photos = updated
        savePhotos()

        // Immediately cancel the posted notification and clear its posted state
        MemoryWorker.cancelPostedNotification(getApplication(), id)
    }

    val activePhoto: Photo? get() = photos.find { it.id == activePhotoId }

    val curatedMemories: List<Photo> by derivedStateOf {
        MemoryManager.getCuratedMemories(photos)
    }

    val historyMemories: List<Photo> by derivedStateOf {
        MemoryManager.getHistoryMemories(photos)
    }

    val hasUnviewedMemories: Boolean by derivedStateOf {
        MemoryManager.hasUnviewedMemories(curatedMemories)
    }
    
    val filteredPhotos: List<Photo> by derivedStateOf {
        val parsed = parsedSearchQuery
        val textTokens = parsed.textTokens
        val directives = parsed.directives
        val hasSearchCriteria = textTokens.isNotEmpty() || directives.isNotEmpty()

        val baseList = if (!hasSearchCriteria && selectedFilterSnippets.isEmpty()) {
            photos
        } else {
            photos.filter { photo ->
                var dateObj: java.util.Date? = null
                var dayName: String? = null
                var monthName: String? = null
                var yearName: String? = null
                var weekName: String? = null
                var fullDate: String? = null

                // 1. Text criteria: All textTokens must match (AND logic)
                val matchesText = textTokens.isEmpty() || textTokens.all { part ->
                    val isNegationQuery = part.startsWith("not ", ignoreCase = true) || part.equals("not", ignoreCase = true)

                    photo.snippets.any { s ->
                        val index = s.indexOf(part, ignoreCase = true)
                        if (index == -1) return@any false

                        // SMART: If we matched "sweet" inside "not sweet", and we didn't search for "not", skip it
                        val isPrecededByNot = s.startsWith("not ", ignoreCase = true) && !isNegationQuery
                        !isPrecededByNot
                    } ||
                    photo.collections.any { it.contains(part, ignoreCase = true) } ||
                    (photo.name?.contains(part, ignoreCase = true) == true) ||
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

                // 2. Directives criteria: All directives must match (AND logic)
                val matchesDirectives = directives.all { directive ->
                    when (directive) {
                        is SearchDirective.Old, is SearchDirective.New -> true
                        is SearchDirective.MostSnippets, is SearchDirective.LeastSnippets, is SearchDirective.Snippets -> photo.snippets.isNotEmpty()
                        is SearchDirective.MostStars, is SearchDirective.LeastStars, is SearchDirective.Stars -> photo.rating > 0
                        is SearchDirective.StarRating -> photo.rating == directive.rating
                    }
                }

                val matchesSearch = hasSearchCriteria && matchesText && matchesDirectives

                val matchesChips = selectedFilterSnippets.isNotEmpty() && selectedFilterSnippets.any { chip ->
                    photo.snippets.any { it.equals(chip, ignoreCase = true) }
                }

                if (hasSearchCriteria && selectedFilterSnippets.isNotEmpty()) {
                    matchesSearch || matchesChips // Union of search and filters
                } else if (hasSearchCriteria) {
                    matchesSearch
                } else {
                    matchesChips
                }
            }
        }

        // Smart grouping: Auto-switch searchDisplayMode based on the FIRST matching date-part
        if (textTokens.isNotEmpty()) {
            val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

            textTokens.forEach { part ->
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

        val effectiveSort = currentSearchSortType ?: photoSortType
        sortPhotos(baseList, effectiveSort)
    }

    private val groupDateFormat = java.text.SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val weekDateFormat = java.text.SimpleDateFormat("'Week' w, yyyy", Locale.getDefault())
    private val monthDateFormat = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearDateFormat = java.text.SimpleDateFormat("yyyy", Locale.getDefault())
    private val dayOfWeekFormat = java.text.SimpleDateFormat("EEEE", Locale.getDefault())
    private val weekRangeFormat = java.text.SimpleDateFormat("MMM d", Locale.getDefault())
    private val groupCalendar = Calendar.getInstance()
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()

    private fun groupPhotosByDate(
        source: List<Photo>, 
        mode: DisplayMode = displayMode,
        sort: PhotoSortType = photoSortType
    ): Map<String, List<Photo>> {
        if (sort == PhotoSortType.MostSnippets || sort == PhotoSortType.LeastSnippets ||
            sort == PhotoSortType.MostStarred || sort == PhotoSortType.LeastStarred) {
            val label = when (sort) {
                PhotoSortType.MostSnippets -> "Most Snippets"
                PhotoSortType.LeastSnippets -> "Least Snippets"
                PhotoSortType.MostStarred -> "Most Starred"
                PhotoSortType.LeastStarred -> "Least Starred"
                else -> ""
            }
            return mapOf(label to source)
        }
        val now = System.currentTimeMillis()
        groupCalendar.timeInMillis = now
        val todayDay = groupCalendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = groupCalendar.get(Calendar.YEAR)
        val weekCache = HashMap<Int, String>()
        val dateObj = java.util.Date()

        return source.groupBy { photo ->
            groupCalendar.timeInMillis = photo.date
            val photoDay = groupCalendar.get(Calendar.DAY_OF_YEAR)
            val photoYear = groupCalendar.get(Calendar.YEAR)

            when (mode) {
                DisplayMode.Day -> {
                    when {
                        photoYear == todayYear && photoDay == todayDay -> "Today"
                        photoYear == todayYear && photoDay == todayDay - 1 -> "Yesterday"
                        else -> {
                            dateObj.time = photo.date
                            groupDateFormat.format(dateObj)
                        }
                    }
                }
                DisplayMode.Week -> {
                    val photoWeek = groupCalendar.get(Calendar.WEEK_OF_YEAR)
                    val cacheKey = photoYear * 100 + photoWeek
                    weekCache.getOrPut(cacheKey) {
                        startCalendar.timeInMillis = photo.date
                        startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        endCalendar.timeInMillis = startCalendar.timeInMillis
                        endCalendar.add(Calendar.DAY_OF_WEEK, 6)
                        "${weekRangeFormat.format(startCalendar.time)} - ${weekRangeFormat.format(endCalendar.time)}"
                    }
                }
                DisplayMode.Month -> {
                    dateObj.time = photo.date
                    monthDateFormat.format(dateObj)
                }
            }
        }
    }

    val groupedPhotos: Map<String, List<Photo>> by derivedStateOf {
        groupPhotosByDate(photos)
    }

        fun containsEmoji(s: String): Boolean = SnippetStyleHelper.containsEmoji(s)

    fun containsEmoticon(s: String): Boolean = SnippetStyleHelper.containsEmoticon(s)

    val favoritesUniqueSnippets: List<String> by derivedStateOf {
        val set = LinkedHashSet<String>()
        val pList = photos
        for (i in pList.indices) {
            val p = pList[i]
            if (p.isFavorite) {
                val snips = p.snippets
                for (j in snips.indices) set.add(snips[j])
            }
        }
        set.toList()
    }


    val allUniqueSnippets: List<String> by derivedStateOf {
        val baseSnippets = LinkedHashSet<String>()
        val pList = photos
        for (i in pList.indices) {
            val snips = pList[i].snippets
            for (j in snips.indices) baseSnippets.add(snips[j])
        }

        val filtered = when (snippetSortType) {
            SnippetSortType.Emoji -> baseSnippets.filter { containsEmoji(it) }
            SnippetSortType.Emoticons -> baseSnippets.filter { containsEmoticon(it) }
            else -> baseSnippets.toList()
        }

        when (snippetSortType) {
            SnippetSortType.New -> filtered.sortedByDescending { snippetFirstSeenTimes[it] ?: 0L }
            SnippetSortType.Old -> filtered.sortedBy { snippetFirstSeenTimes[it] ?: 0L }
            SnippetSortType.AZ -> filtered.sortedWith(String.CASE_INSENSITIVE_ORDER)
            SnippetSortType.Emoji, SnippetSortType.Emoticons -> filtered.sorted()
        }
    }

    val locationNameMap: Map<String, String> by derivedStateOf {
        val map = mutableMapOf<String, String>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val name = p.locationName?.trim()?.takeIf { it.isNotBlank() }
                ?: link?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it) }
            if (link != null && name != null) {
                val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(name)
                if (clean.isNotBlank()) {
                    map[link] = clean
                }
            }
        }
        map
    }

    val allUniqueLocations: List<String> by derivedStateOf {
        val nameToLink = mutableMapOf<String, String>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val name = p.locationName?.trim()?.takeIf { it.isNotBlank() }
                ?: link?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it) }
            if (link != null && name != null) {
                val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(name)
                if (clean.isNotBlank()) {
                    nameToLink.putIfAbsent(clean.lowercase(), link)
                }
            }
        }

        val counts = mutableMapOf<String, Int>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val rawName = p.locationName?.trim()?.takeIf { it.isNotBlank() }
            val cleanName = rawName?.let { com.android.snippets.ui.util.LocationUtils.cleanLocationName(it) }

            val key = when {
                link != null -> link
                cleanName != null -> nameToLink[cleanName.lowercase()] ?: cleanName
                else -> null
            }

            if (!key.isNullOrBlank()) {
                counts[key] = (counts[key] ?: 0) + 1
            }
        }

        photos.forEach { p ->
            val rawName = p.locationName?.trim()?.takeIf { it.isNotBlank() }
            if (rawName != null) {
                val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(rawName)
                val key = nameToLink[clean.lowercase()] ?: clean
                if (key.isNotBlank() && !counts.containsKey(key)) {
                    counts[key] = 1
                }
            }
        }

        counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.lowercase() }
            )
            .map { it.key }
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
            getPhotoSortTypeFor(currentCollection)
        } else {
            getPhotoSortTypeFor("Library")
        }
        
        groupPhotosByDate(source, mode, sort)
    }

    val filteredFavoritesPhotos: Map<String, List<Photo>> by derivedStateOf {
        val source = filteredPhotos.filter { it.isFavorite }
        // Favorites uses Library defaults (as per "leave favorites out" preference)
        groupPhotosByDate(source, _displayMode.value, getPhotoSortTypeFor("Favorites"))
    }

    fun getPureSnippets(photo: Photo): List<String> {
        return photo.snippets.filterNot { snippet -> 
            userCollections.any { it.equals(snippet, ignoreCase = true) } 
        }
    }

    fun getCollectionIcon(collection: String): Any {
        if (collection == "Favorites") return Icons.Rounded.Favorite
        val iconName = collectionIcons[collection] ?: return Icons.Rounded.Folder
        
        // If it's a single emoji or short string, return it as is
        if (iconName.length <= 4) return iconName
        
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
            else -> Icons.Rounded.Folder
        }
    }

    fun getSnippetFirstSeenTime(name: String): Long? {
        return snippetFirstSeenTimes[name.trim()]
    }

    fun getSnippetStyle(name: String): SnippetStyle {
        return SnippetStyle.Default
    }



    private fun saveSnippetFirstSeenTimes() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonStr = json.encodeToString(snippetFirstSeenTimes)
                userPreferencesRepository.updateSnippetFirstSeenJson(jsonStr)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }





    fun exportBackupToFile(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                backupManager.exportBackup(
                    context = context,
                    uri = uri,
                    photos = photos,
                    userCollections = userCollections,
                    collectionIcons = collectionIcons,
                    snippetFirstSeenTimes = snippetFirstSeenTimes
                )
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Backup file exported successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun importBackupFromFile(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                backupManager.importBackup(context, uri)
                // Force reload all settings and data
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    loadAllData()
                    Toast.makeText(context, "Backup file imported successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun updateAutoBackupSchedule(schedule: String) {
        autoBackupSchedule = schedule
        viewModelScope.launch {
            userPreferencesRepository.updateAutoBackupSchedule(schedule)
        }
        scheduleAutoBackup()
    }

    private fun scheduleAutoBackup() {
        val workManager = androidx.work.WorkManager.getInstance(getApplication())
        if (autoBackupSchedule == "Disabled") {
            workManager.cancelUniqueWork("auto_backup_work")
            android.util.Log.d("SnippetsViewModel", "Cancelled auto backup work.")
        } else {
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.android.snippets.logic.AutoBackupWorker>(
                24, java.util.concurrent.TimeUnit.HOURS
            ).setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresCharging(true)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()
            ).build()
            
            workManager.enqueueUniquePeriodicWork(
                "auto_backup_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            android.util.Log.d("SnippetsViewModel", "Scheduled auto backup work: $autoBackupSchedule")
        }
    }

}