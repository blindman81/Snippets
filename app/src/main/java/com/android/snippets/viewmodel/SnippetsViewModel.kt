package com.android.snippets.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.logic.SearchDirective
import com.android.snippets.logic.ParsedSearchQuery
import com.android.snippets.logic.SearchQueryParser
import com.android.snippets.logic.SnippetStyleHelper
import com.android.snippets.ui.util.PhotoCardExporter
import com.android.snippets.model.Photo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.launch
import com.android.snippets.logic.MemoryWorker
import com.android.snippets.logic.MemoryManager
import android.app.backup.BackupManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.android.snippets.data.DatabasePreloader
import coil3.imageLoader
import coil3.request.ImageRequest
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.data.repository.CollectionRepository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

typealias SnippetSortType = com.android.snippets.model.SnippetSortType
typealias PhotoSortType = com.android.snippets.model.PhotoSortType
typealias ThemePreference = com.android.snippets.model.ThemePreference
typealias SnippetStyle = com.android.snippets.model.SnippetStyle

typealias SearchDirective = com.android.snippets.logic.SearchDirective
typealias ParsedSearchQuery = com.android.snippets.logic.ParsedSearchQuery


@HiltViewModel
class SnippetsViewModel @Inject constructor(
    application: Application,
    val photoRepository: PhotoRepository,
    val collectionRepository: CollectionRepository,
    val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    sealed interface RecapLaunchEvent {
        data class Monthly(val month: Int, val year: Int) : RecapLaunchEvent
        data class Yearly(val year: Int) : RecapLaunchEvent
    }

    companion object {
        fun rebuildSnippetFirstSeenTimes(source: List<Photo>): Map<String, Long> =
            com.android.snippets.logic.SnippetManager.rebuildSnippetFirstSeenTimes(source)
        fun matchSearchDirective(rawToken: String): SearchDirective? =
            SearchQueryParser.matchSearchDirective(rawToken)

        fun parseSearchQuery(query: String): ParsedSearchQuery =
            SearchQueryParser.parseSearchQuery(query)

        const val SNACKBAR_NAME_PHOTO_FIRST = com.android.snippets.logic.SnippetManager.SNACKBAR_NAME_PHOTO_FIRST
        const val SNACKBAR_ACTION_OPEN = com.android.snippets.logic.SnippetManager.SNACKBAR_ACTION_OPEN

        fun validateAndExecuteAddSnippet(
            photo: Photo,
            showSnackbar: (message: String, actionLabel: String?, onAction: (() -> Unit)?) -> Unit,
            onShowNameModal: (Photo) -> Unit,
            onShowAddModal: (Photo) -> Unit
        ) = com.android.snippets.logic.SnippetManager.validateAndExecuteAddSnippet(photo, showSnackbar, onShowNameModal, onShowAddModal)

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

    private val _uiState = MutableStateFlow(SnippetsUiState())
    val uiState: StateFlow<SnippetsUiState> = _uiState.asStateFlow()

    fun updateUiState(transform: (SnippetsUiState) -> SnippetsUiState) {
        _uiState.update(transform)
    }

    var photos: List<Photo>
        get() = _uiState.value.photos
        private set(value) { _uiState.update { it.copy(photos = value) } }

    var isInitialLoading: Boolean
        get() = _uiState.value.isInitialLoading
        private set(value) { _uiState.update { it.copy(isInitialLoading = value) } }

    var userCollections: List<String>
        get() = _uiState.value.userCollections
        private set(value) { _uiState.update { it.copy(userCollections = value) } }

    var libraryCurrentTab: String
        get() = _uiState.value.libraryCurrentTab
        set(value) { _uiState.update { it.copy(libraryCurrentTab = value) } }

    var activePhotoId: String?
        get() = _uiState.value.activePhotoId
        set(value) { _uiState.update { it.copy(activePhotoId = value) } }

    var searchQuery: String
        get() = _uiState.value.searchQuery
        set(value) { _uiState.update { it.copy(searchQuery = value) } }

    val isSearching: Boolean
        get() = _uiState.value.isSearching

    val isFiltering: Boolean
        get() = _uiState.value.isFiltering

    var isBusy: Boolean
        get() = _uiState.value.isBusy
        set(value) { _uiState.update { it.copy(isBusy = value) } }

    var isAddingPhotos: Boolean
        get() = _uiState.value.isAddingPhotos
        set(value) { _uiState.update { it.copy(isAddingPhotos = value) } }

    var isCuratingMemories: Boolean
        get() = _uiState.value.isCuratingMemories
        set(value) { _uiState.update { it.copy(isCuratingMemories = value) } }

    var selectedFilterSnippets: List<String>
        get() = _uiState.value.selectedFilterSnippets
        set(value) { _uiState.update { it.copy(selectedFilterSnippets = value) } }

    var scrollToTopTrigger: Long
        get() = _uiState.value.scrollToTopTrigger
        private set(value) { _uiState.update { it.copy(scrollToTopTrigger = value) } }

    var pendingRecapLaunch: RecapLaunchEvent?
        get() = _uiState.value.pendingRecapLaunch
        set(value) { _uiState.update { it.copy(pendingRecapLaunch = value) } }

    fun requestScrollToTop() {
        _uiState.update { it.copy(scrollToTopTrigger = System.currentTimeMillis()) }
    }

    fun scrollToTop() {
        requestScrollToTop()
    }

    fun toggleFilterSnippet(snippet: String) {
        val currentFilters = _uiState.value.selectedFilterSnippets
        val wasSelected = currentFilters.any { it.equals(snippet, ignoreCase = true) }
        val updatedFilters = if (wasSelected) {
            currentFilters.filter { !it.equals(snippet, ignoreCase = true) }
        } else {
            if (currentFilters.size >= 6) {
                showSnackbar("Upto 6 Chips!")
                return
            }
            currentFilters + snippet
        }
        _uiState.update { it.copy(selectedFilterSnippets = updatedFilters, scrollToTopTrigger = System.currentTimeMillis()) }
        saveFilterState()
    }

    fun clearFilterSnippets() {
        val hadFilters = _uiState.value.selectedFilterSnippets.isNotEmpty()
        _uiState.update {
            it.copy(
                selectedFilterSnippets = emptyList(),
                scrollToTopTrigger = if (hadFilters) System.currentTimeMillis() else it.scrollToTopTrigger
            )
        }
        saveFilterState()
    }

    fun getMatchingSnippetsCount(photo: Photo): Int =
        com.android.snippets.logic.SnippetManager.getMatchingSnippetsCount(photo, _uiState.value.selectedFilterSnippets)

    var activeCollection: String?
        get() = _uiState.value.activeCollection
        set(value) { _uiState.update { it.copy(activeCollection = value) } }

    var showCreateDialog: Boolean
        get() = _uiState.value.showCreateDialog
        set(value) { _uiState.update { it.copy(showCreateDialog = value) } }

    var editingCollectionEmoji: String?
        get() = _uiState.value.editingCollectionEmoji
        set(value) { _uiState.update { it.copy(editingCollectionEmoji = value) } }

    var editingEmojiPreview: String?
        get() = _uiState.value.editingEmojiPreview
        set(value) { _uiState.update { it.copy(editingEmojiPreview = value) } }

    var collectionIcons: Map<String, String>
        get() = _uiState.value.collectionIcons
        set(value) { _uiState.update { it.copy(collectionIcons = value) } }

    var focusCreateCollection: Boolean
        get() = _uiState.value.focusCreateCollection
        set(value) { _uiState.update { it.copy(focusCreateCollection = value) } }

    var snippetFirstSeenTimes: Map<String, Long>
        get() = _uiState.value.snippetFirstSeenTimes
        private set(value) { _uiState.update { it.copy(snippetFirstSeenTimes = value) } }

    var filteringCategory: String?
        get() = _uiState.value.filteringCategory
        set(value) { _uiState.update { it.copy(filteringCategory = value) } }

    var recapTitle: String
        get() = _uiState.value.recapTitle
        private set(value) { _uiState.update { it.copy(recapTitle = value) } }

    var recapPhotos: List<Photo>
        get() = _uiState.value.recapPhotos
        private set(value) { _uiState.update { it.copy(recapPhotos = value) } }

    var isYearlyRecap: Boolean
        get() = _uiState.value.isYearlyRecap
        private set(value) { _uiState.update { it.copy(isYearlyRecap = value) } }

    var recapMonth: Int
        get() = _uiState.value.recapMonth
        set(value) { _uiState.update { it.copy(recapMonth = value) } }

    var recapYear: Int
        get() = _uiState.value.recapYear
        set(value) { _uiState.update { it.copy(recapYear = value) } }

    var lastSeenYearlyRecapYear: Int
        get() = _uiState.value.lastSeenYearlyRecapYear
        private set(value) { _uiState.update { it.copy(lastSeenYearlyRecapYear = value) } }

    var lastSeenMonthlyRecapPeriod: String
        get() = _uiState.value.lastSeenMonthlyRecapPeriod
        private set(value) { _uiState.update { it.copy(lastSeenMonthlyRecapPeriod = value) } }

    var snackbarMessage: String?
        get() = _uiState.value.snackbarMessage
        set(value) { _uiState.update { it.copy(snackbarMessage = value) } }

    var snackbarActionLabel: String?
        get() = _uiState.value.snackbarActionLabel
        set(value) { _uiState.update { it.copy(snackbarActionLabel = value) } }

    var onSnackbarAction: (() -> Unit)?
        get() = _uiState.value.onSnackbarAction
        set(value) { _uiState.update { it.copy(onSnackbarAction = value) } }

    var snackbarToken: Long
        get() = _uiState.value.snackbarToken
        set(value) { _uiState.update { it.copy(snackbarToken = value) } }

    fun showSnackbar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _uiState.update {
            it.copy(
                snackbarMessage = message,
                snackbarActionLabel = actionLabel,
                onSnackbarAction = onAction,
                snackbarToken = it.snackbarToken + 1
            )
        }
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

    val sortedCollections: List<String>
        get() = _uiState.value.userCollections

    var showFilterSheet: Boolean
        get() = _uiState.value.showFilterSheet
        set(value) { _uiState.update { it.copy(showFilterSheet = value) } }


    val separateCollectionSort = true
    
    
    fun createNewCollection(): String {
        val newName = com.android.snippets.logic.CollectionManager.generateNewPlaceholderName(userCollections)
        userCollections = userCollections + newName
        collectionIcons = collectionIcons + (newName to "")
        saveCollections()
        libraryCurrentTab = newName
        navigateLibrary()
        editingCollectionEmoji = newName
        editingEmojiPreview = ""
        _uiState.update { it.copy(emojiKeyboardRequestCount = it.emojiKeyboardRequestCount + 1) }
        return newName
    }

    fun startEditingCollectionEmoji(collection: String) {
        editingCollectionEmoji = collection
        editingEmojiPreview = collectionIcons[collection] ?: if (collection.isBlank()) "" else collection
        _uiState.update { it.copy(emojiKeyboardRequestCount = it.emojiKeyboardRequestCount + 1) }
    }

    fun updateEditingEmojiPreview(collection: String, emoji: String) {
        if (editingCollectionEmoji == collection) {
            editingEmojiPreview = emoji
            collectionIcons = collectionIcons + (collection to emoji)
        }
    }

    fun finalizeCollectionEmoji(collection: String, emoji: String) {
        val result = com.android.snippets.logic.CollectionManager.finalizeCollectionEmoji(
            userCollections = userCollections,
            collectionIcons = collectionIcons,
            photos = photos,
            collection = collection,
            emoji = emoji
        ) ?: run {
            if (collection.isBlank() && userCollections.contains(collection)) {
                deleteCollection(collection)
            }
            editingCollectionEmoji = null
            editingEmojiPreview = null
            return
        }

        userCollections = result.updatedCollections
        collectionIcons = result.updatedIcons
        val finalName = result.finalName
        val oldIndex = result.oldIndex

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageMutex.withLock {
                try {
                    val existing = collectionRepository.getCollectionByName(collection)
                    if (existing != null) {
                        collectionRepository.deleteCollection(existing)
                    }
                    collectionRepository.insertCollection(
                        com.android.snippets.data.CollectionEntity(
                            name = finalName,
                            icon = result.updatedIcons[finalName] ?: emoji.trim(),
                            orderIndex = if (oldIndex != -1) oldIndex else userCollections.size - 1
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (result.photosChanged) {
            photos = result.updatedPhotos
            savePhotos()
        }

        if (libraryCurrentTab == collection) {
            libraryCurrentTab = finalName
        }

        saveCollections()
        editingCollectionEmoji = null
        editingEmojiPreview = null
    }

    fun cancelEditingCollectionEmoji() {
        val col = editingCollectionEmoji
        editingCollectionEmoji = null
        editingEmojiPreview = null
        if (col != null && col.isBlank() && userCollections.contains(col)) {
            deleteCollection(col)
        }
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
    
    var requestNotificationPermission: Boolean
        get() = _uiState.value.requestNotificationPermission
        set(value) { _uiState.update { it.copy(requestNotificationPermission = value) } }

    var hasPromptedNotificationPermission: Boolean
        get() = _uiState.value.hasPromptedNotificationPermission
        private set(value) { _uiState.update { it.copy(hasPromptedNotificationPermission = value) } }

    // Multi-Selection State
    var selectedPhotoIds: Set<String>
        get() = _uiState.value.selectedPhotoIds
        private set(value) { _uiState.update { it.copy(selectedPhotoIds = value) } }

    var forceSelectionMode: Boolean
        get() = _uiState.value.forceSelectionMode
        set(value) { _uiState.update { it.copy(forceSelectionMode = value) } }

    val isSelectionMode: Boolean
        get() = _uiState.value.isSelectionMode

    var pendingCollectionAssignment: String?
        get() = _uiState.value.pendingCollectionAssignment
        private set(value) { _uiState.update { it.copy(pendingCollectionAssignment = value) } }

    var pendingCollectionRemoval: String?
        get() = _uiState.value.pendingCollectionRemoval
        private set(value) { _uiState.update { it.copy(pendingCollectionRemoval = value) } }

    fun startCollectionAssignment(collection: String) {
        _uiState.update {
            it.copy(
                pendingCollectionAssignment = collection,
                selectedPhotoIds = emptySet(),
                libraryCurrentTab = "Library"
            )
        }
    }

    fun startCollectionRemoval(collection: String) {
        _uiState.update {
            it.copy(pendingCollectionRemoval = collection, selectedPhotoIds = emptySet())
        }
    }

    fun confirmCollectionAssignment() {
        val collection = pendingCollectionAssignment ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = com.android.snippets.logic.CollectionManager.assignPhotosToCollection(photos, selectedPhotoIds, collection)
            savePhotos()
            
            val message = if (collection == "Favorites") "Added to Favorites" else "Added to collection"
            showSnackbar(
                message = message,
                actionLabel = "View",
                onAction = {
                    libraryCurrentTab = collection
                    navigateLibrary()
                }
            )
        }
        _uiState.update {
            it.copy(
                pendingCollectionAssignment = null,
                selectedPhotoIds = emptySet()
            )
        }
        navigateLibrary()
    }

    fun confirmCollectionRemoval() {
        val collection = pendingCollectionRemoval ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = com.android.snippets.logic.CollectionManager.removePhotosFromCollection(photos, selectedPhotoIds, collection)
            savePhotos()
            
            if (collection != "Favorites") {
                showSnackbar(message = "Removed from collection")
            }
        }
        _uiState.update {
            it.copy(
                pendingCollectionRemoval = null,
                selectedPhotoIds = emptySet(),
                libraryCurrentTab = collection
            )
        }
        navigateLibrary()
    }

    var showBulkAddToCollectionDialog: Boolean
        get() = _uiState.value.showBulkAddToCollectionDialog
        set(value) { _uiState.update { it.copy(showBulkAddToCollectionDialog = value) } }

    var showBulkDeleteModal: Boolean
        get() = _uiState.value.showBulkDeleteModal
        set(value) { _uiState.update { it.copy(showBulkDeleteModal = value) } }

    var snippetSortType: SnippetSortType
        get() = _uiState.value.snippetSortType
        set(value) {
            _uiState.update { it.copy(snippetSortType = value) }
            saveFilterState()
        }

    var isSortAscending: Boolean
        get() = _uiState.value.isSortAscending
        set(value) {
            _uiState.update { it.copy(isSortAscending = value) }
            saveFilterState()
        }

    val parsedSearchQuery: ParsedSearchQuery
        get() = _uiState.value.parsedSearchQuery

    val currentSearchSortType: PhotoSortType?
        get() = _uiState.value.currentSearchSortType

    var photoSortType: PhotoSortType
        get() = currentSearchSortType ?: _uiState.value.photoSortType
        set(value) {
            _uiState.update { it.copy(photoSortType = value, scrollToTopTrigger = System.currentTimeMillis()) }
            saveFilterState()
        }

    fun getPhotoSortTypeFor(tabName: String): PhotoSortType {
        currentSearchSortType?.let { return it }
        if (tabName == "Library") return _uiState.value.photoSortType
        return _uiState.value.collectionPhotoSortTypes[tabName] ?: PhotoSortType.DateNewest
    }

    fun setPhotoSortTypeFor(tabName: String, sortType: PhotoSortType) {
        if (tabName == "Library") {
            _uiState.update { it.copy(photoSortType = sortType, scrollToTopTrigger = System.currentTimeMillis()) }
        } else {
            _uiState.update {
                it.copy(
                    collectionPhotoSortTypes = it.collectionPhotoSortTypes + (tabName to sortType),
                    scrollToTopTrigger = System.currentTimeMillis()
                )
            }
        }
        saveFilterState()
    }

    fun sortPhotos(baseList: List<Photo>, sort: PhotoSortType): List<Photo> =
        com.android.snippets.logic.PhotoSearchHelper.sortPhotos(baseList, sort)

    val pagedPhotos: Flow<PagingData<com.android.snippets.data.PhotoEntity>> = Pager(
        config = PagingConfig(pageSize = 30, enablePlaceholders = false)
    ) {
        photoRepository.getPagingPhotos()
    }.flow

    var pendingFavoriteIntent: Boolean
        get() = _uiState.value.pendingFavoriteIntent
        set(value) { _uiState.update { it.copy(pendingFavoriteIntent = value) } }

    var pendingAddPhotoIntentToken: Long
        get() = _uiState.value.pendingAddPhotoIntentToken
        set(value) { _uiState.update { it.copy(pendingAddPhotoIntentToken = value) } }

    private val storageMutex = Mutex()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { userPrefs ->
                val sortMap: Map<String, PhotoSortType> = if (userPrefs.collectionPhotoSortTypesJson.isNotBlank() && userPrefs.collectionPhotoSortTypesJson != "{}") {
                    try { json.decodeFromString(userPrefs.collectionPhotoSortTypesJson) } catch (e: Exception) { emptyMap() }
                } else emptyMap()

                _uiState.update { current ->
                    current.copy(
                        hasPromptedNotificationPermission = userPrefs.hasPromptedNotificationPermission,
                        lastSeenYearlyRecapYear = userPrefs.lastSeenYearlyRecapYear,
                        lastSeenMonthlyRecapPeriod = userPrefs.lastSeenMonthlyRecapPeriod,
                        snippetSortType = try { SnippetSortType.valueOf(userPrefs.snippetSortType) } catch (e: Exception) { SnippetSortType.New },
                        isSortAscending = userPrefs.isSortAscending,
                        selectedFilterSnippets = userPrefs.selectedFilterSnippets.toList(),
                        photoSortType = try { PhotoSortType.valueOf(userPrefs.photoSortType) } catch (e: Exception) { PhotoSortType.DateNewest },
                        collectionPhotoSortTypes = sortMap
                    )
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
                val current = photos
                if (current.isNotEmpty() && current.size == roomPhotos.size) {
                    val isIdentical = roomPhotos.all { rp ->
                        val p = current.find { it.id == rp.id }
                        p != null && p.date == rp.date && p.snippets == (rp.snippets ?: emptyList<String>()) &&
                                p.isFavorite == rp.isFavorite && p.rating == rp.rating &&
                                p.name == rp.name && p.locationName == rp.locationName &&
                                p.collections == (rp.collections ?: emptyList<String>()) &&
                                p.isViewed == rp.isViewed && p.lastViewedTime == rp.lastViewedTime &&
                                p.surfacedTime == rp.surfacedTime &&
                                p.widthPx == rp.widthPx && p.heightPx == rp.heightPx &&
                                p.uriString == rp.uriString
                    }
                    if (isIdentical) return@collect
                }

                val processedPhotos = roomPhotos.map { p ->
                    val (widthPx, heightPx) = if (p.widthPx > 0 && p.heightPx > 0) {
                        p.widthPx to p.heightPx
                    } else {
                        com.android.snippets.logic.PhotoStorageManager.extractImageDimensions(getApplication(), Uri.parse(p.uriString))
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

            val rawRoomPhotos = try {
                photoRepository.getAllPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val app = getApplication<Application>()
            val (roomPhotos, fileHealingNeeded) = com.android.snippets.logic.PhotoStorageManager.healFileUris(app, rawRoomPhotos)
            if (fileHealingNeeded) {
                try {
                    photoRepository.insertPhotos(roomPhotos)
                } catch (_: Exception) {}
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
                if (p.snippetsAddedTime == 0L && p.snippets.isNotEmpty()) {
                    p.copy(snippetsAddedTime = p.date)
                } else p
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

            _uiState.update {
                it.copy(
                    photos = processedPhotos,
                    userCollections = roomCollections,
                    collectionIcons = loadedIcons,
                    snippetFirstSeenTimes = mergedFirstSeen,
                    isInitialLoading = false
                )
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {

                // Pre-warm Coil's memory cache for the first batch of photos so they render instantly
                val photosToPreload = processedPhotos.take(20)
                if (photosToPreload.isNotEmpty()) {
                    val loader = app.imageLoader
                    photosToPreload.forEach { p ->
                        val request = ImageRequest.Builder(app)
                            .data(p.uriString)
                            .memoryCacheKey(p.uriString)
                            .build()
                        loader.enqueue(request)
                    }
                }

                // Auto-heal content URIs, dimensions, and resolve location links asynchronously in background
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val (healedContentPhotos, contentHealed) = com.android.snippets.logic.PhotoStorageManager.healContentUris(app, photos)
                    val (finalPhotos, dimensionsHealed) = com.android.snippets.logic.PhotoStorageManager.backfillMissingDimensions(app, healedContentPhotos)
                    if (contentHealed || dimensionsHealed) {
                        try {
                            photoRepository.insertPhotos(finalPhotos)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                photos = finalPhotos
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Background resolution for photos with location link but missing place name
                    val needsLocationResolution = photos.filter { !it.locationLink.isNullOrBlank() && it.locationName.isNullOrBlank() }
                    if (needsLocationResolution.isNotEmpty()) {
                        var locationChanged = false
                        val currentPhotos = photos.toMutableList()
                        needsLocationResolution.forEach { p ->
                            val link = p.locationLink ?: return@forEach
                            val name = com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(link, app)
                                ?: com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(link, app)
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
                displayMode = "Day",
                collectionDisplayModesJson = "{}",
                photoSortType = _uiState.value.photoSortType.name,
                collectionPhotoSortTypesJson = json.encodeToString(_uiState.value.collectionPhotoSortTypes)
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
                        val mergedSnapshot = snapshot.map { p ->
                            val dbPhoto = currentDb.find { it.id == p.id }
                            if (dbPhoto != null && dbPhoto.isViewed && (!p.isViewed || dbPhoto.lastViewedTime > p.lastViewedTime)) {
                                p.copy(isViewed = true, lastViewedTime = dbPhoto.lastViewedTime)
                            } else {
                                p
                            }
                        }
                        photoRepository.insertPhotos(mergedSnapshot)
                        if (mergedSnapshot != snapshot) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                photos = mergedSnapshot
                            }
                        }
                        BackupManager(getApplication()).dataChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
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










    fun addPhotos(
        uris: List<Uri>,
        isFavorite: Boolean = false,
        locationLink: String? = null,
        locationName: String? = null,
        collection: String? = null
    ) {
        if (uris.isEmpty()) return
        isAddingPhotos = true
        val targetLocation = locationLink?.trim()?.takeIf { it.isNotBlank() }
        val immediateName = locationName?.trim()?.takeIf { it.isNotBlank() }
            ?: targetLocation?.let { com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(it, getApplication()) }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var currentPhotosList = photos
                val newPhotosList = mutableListOf<Photo>()
                var singleDetailToOpen: String? = null

                for (uri in uris) {
                    val captureDate = com.android.snippets.logic.PhotoStorageManager.extractCaptureDate(getApplication(), uri)
                    val duplicate = currentPhotosList.find { it.date == captureDate && it.uriString.contains(uri.lastPathSegment ?: "___") }

                    if (duplicate != null) {
                        val healedUri = if (duplicate.uriString.startsWith("content://")) {
                            com.android.snippets.logic.PhotoStorageManager.saveToInternalStorage(getApplication(), uri)
                        } else null

                        var updatedPhoto = duplicate
                        if (isFavorite && !duplicate.isFavorite) {
                            updatedPhoto = updatedPhoto.copy(isFavorite = true)
                        }
                        if (collection != null && !duplicate.collections.contains(collection)) {
                            updatedPhoto = updatedPhoto.copy(collections = duplicate.collections + collection)
                        }
                        if (healedUri != null) {
                            val (newW, newH) = com.android.snippets.logic.PhotoStorageManager.extractImageDimensions(getApplication(), Uri.parse(healedUri))
                            updatedPhoto = updatedPhoto.copy(
                                uriString = healedUri,
                                widthPx = newW,
                                heightPx = newH
                            )
                        }
                        if (!targetLocation.isNullOrBlank() && updatedPhoto.locationLink.isNullOrBlank()) {
                            updatedPhoto = updatedPhoto.copy(
                                locationLink = targetLocation,
                                locationName = immediateName ?: updatedPhoto.locationName
                            )
                        }
                        if (updatedPhoto != duplicate) {
                            currentPhotosList = currentPhotosList.map {
                                if (it.id == duplicate.id) updatedPhoto else it
                            }
                        }
                        if (uris.size == 1) {
                            singleDetailToOpen = duplicate.id
                        }
                    } else {
                        val internalUri = com.android.snippets.logic.PhotoStorageManager.saveToInternalStorage(getApplication(), uri) ?: uri.toString()
                        val (widthPx, heightPx) = com.android.snippets.logic.PhotoStorageManager.extractImageDimensions(getApplication(), Uri.parse(internalUri))

                        val newPhoto = Photo(
                            uriString = internalUri,
                            date = captureDate,
                            snippets = emptyList(),
                            snippetsAddedTime = 0L,
                            collections = if (collection != null) listOf(collection) else emptyList(),
                            isFavorite = isFavorite,
                            isLibraryUpload = true,
                            widthPx = widthPx,
                            heightPx = heightPx,
                            locationLink = targetLocation,
                            locationName = immediateName,
                            rating = 0
                        )
                        newPhotosList.add(newPhoto)
                        if (uris.size == 1) {
                            singleDetailToOpen = newPhoto.id
                        }
                    }
                }

                val finalPhotos = (newPhotosList + currentPhotosList).sortedByDescending { it.date }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    photos = finalPhotos
                    savePhotos()

                    if (uris.size == 1 && !targetLocation.isNullOrBlank() && singleDetailToOpen != null) {
                        openDetail(singleDetailToOpen, tab = collection ?: "Library")
                    } else if (newPhotosList.isNotEmpty() && uris.size > 1) {
                        showSnackbar("${newPhotosList.size} photos added")
                    }

                    isAddingPhotos = false
                }

                if (uris.size == 1 && !targetLocation.isNullOrBlank() && immediateName == null &&
                    (targetLocation.startsWith("http://", ignoreCase = true) || targetLocation.startsWith("https://", ignoreCase = true))) {
                    val resolved = com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(targetLocation, getApplication())
                    if (!resolved.isNullOrBlank()) {
                        val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(resolved)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            photos = photos.map {
                                if (it.id == singleDetailToOpen && it.locationName.isNullOrBlank()) {
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

    fun addPhoto(
        uri: Uri,
        isFavorite: Boolean = false,
        locationLink: String? = null,
        locationName: String? = null,
        collection: String? = null
    ) {
        addPhotos(
            uris = listOf(uri),
            isFavorite = isFavorite,
            locationLink = locationLink,
            locationName = locationName,
            collection = collection
        )
    }

    fun addPhotoToLibrary(uri: Uri, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = listOf(uri), locationLink = locationLink, locationName = locationName)
    }

    fun addPhotosToLibrary(uris: List<Uri>, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = uris, locationLink = locationLink, locationName = locationName)
    }

    fun addPhotoToCollection(uri: Uri, collection: String, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = listOf(uri), collection = collection, locationLink = locationLink, locationName = locationName)
    }

    fun addPhotosToCollection(uris: List<Uri>, collection: String, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = uris, collection = collection, locationLink = locationLink, locationName = locationName)
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
        if (collection == "Library" || collection == "Favorites" || !userCollections.contains(collection)) return

        val result = com.android.snippets.logic.CollectionManager.deleteCollection(
            userCollections = userCollections,
            collectionIcons = collectionIcons,
            photos = photos,
            selectedFilterSnippets = selectedFilterSnippets,
            collection = collection
        )

        userCollections = result.updatedCollections
        collectionIcons = result.updatedIcons
        _uiState.update { it.copy(collectionPhotoSortTypes = it.collectionPhotoSortTypes - collection) }
        
        saveCollections()

        if (result.photosChanged) {
            photos = result.updatedPhotos
            savePhotos()
        }

        if (result.filtersChanged) {
            selectedFilterSnippets = result.updatedFilters
            scrollToTop()
            saveFilterState()
        }

        if (activeCollection == collection) {
            navigateLibrary()
        }
        if (pendingCollectionAssignment == collection) {
            pendingCollectionAssignment = null
        }
        if (editingCollectionEmoji == collection) {
            cancelEditingCollectionEmoji()
        }
        if (libraryCurrentTab == collection) {
            libraryCurrentTab = "Library"
        }
    }


    private fun reconcileMemoryNotifications() {
        MemoryManager.reconcileMemoryNotifications(getApplication(), photos)
    }

    private fun cancelMemoryNotification(photoId: String) {
        MemoryManager.cancelMemoryNotification(getApplication(), photoId)
    }

    fun onNotificationPermissionGranted() {
        reconcileSurfacedMemories()
        reconcileMemoryNotifications()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val curated = MemoryManager.getCuratedMemories(photos, now).take(MemoryManager.MEMORIES_PER_CYCLE)
            val unviewed = curated.filter { !it.isViewed }
            if (unviewed.isNotEmpty() && MemoryWorker.canPostNotifications(getApplication())) {
                MemoryManager.triggerImmediateCycleWork(getApplication())
            }
        }
    }

    fun markNotificationPermissionPrompted() {
        hasPromptedNotificationPermission = true
        viewModelScope.launch {
            userPreferencesRepository.updateHasPromptedNotificationPermission(true)
        }
    }

    fun testNotification() {
        requestNotificationPermission = true
        MemoryManager.testNotification(getApplication(), photos)
    }



    fun updateSnippets(photoId: String, text: String) {
        val result = com.android.snippets.logic.SnippetManager.updateSnippets(
            photos = photos,
            snippetFirstSeenTimes = snippetFirstSeenTimes,
            photoId = photoId,
            text = text
        ) ?: run {
            val targetPhoto = photos.find { it.id == photoId }
            if (targetPhoto != null && targetPhoto.snippets.size >= com.android.snippets.logic.SnippetManager.MAX_SNIPPETS_PER_PHOTO) {
                android.widget.Toast.makeText(getApplication(), "Maximum 6 snippets per photo", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (result.updatedFirstSeen != snippetFirstSeenTimes) {
            snippetFirstSeenTimes = result.updatedFirstSeen
            saveSnippetFirstSeenTimes()
        }

        photos = result.updatedPhotos
        savePhotos()
        reconcileSurfacedMemories()

        if (result.isFirstTimeAdded && result.wasAppEmptyBefore) {
            showSnackbar("You just created your first snippet! A new memory will be created soon.")
        }
    }

    fun removeSnippet(photoId: String, snippet: String) {
        val result = com.android.snippets.logic.SnippetManager.removeSnippet(
            photos = photos,
            snippetFirstSeenTimes = snippetFirstSeenTimes,
            selectedFilterSnippets = selectedFilterSnippets,
            photoId = photoId,
            snippet = snippet
        )

        photos = result.updatedPhotos
        savePhotos()

        if (result.emptiedMemory) {
            cancelMemoryNotification(photoId)
        }

        if (result.updatedFirstSeen != snippetFirstSeenTimes) {
            snippetFirstSeenTimes = result.updatedFirstSeen
            saveSnippetFirstSeenTimes()
        }

        if (result.filtersChanged) {
            selectedFilterSnippets = result.updatedFilterSnippets
            scrollToTop()
            saveFilterState()
        }
    }

    fun deleteSnippetGlobally(snippet: String) {
        val result = com.android.snippets.logic.SnippetManager.deleteSnippetGlobally(
            photos = photos,
            snippetFirstSeenTimes = snippetFirstSeenTimes,
            snippet = snippet
        )

        photos = result.updatedPhotos
        savePhotos()
        result.emptiedMemoryIds.forEach { cancelMemoryNotification(it) }

        snippetFirstSeenTimes = result.updatedFirstSeen
        saveSnippetFirstSeenTimes()

        if (selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }) {
            selectedFilterSnippets = selectedFilterSnippets.filter { !it.equals(snippet, ignoreCase = true) }
            scrollToTop()
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
        deletedPhoto?.let { com.android.snippets.logic.PhotoStorageManager.deletePhotoFile(it.uriString) }
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
                    scrollToTop()
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
        val returnCollection = pendingCollectionAssignment
        selectedPhotoIds = emptySet()
        pendingCollectionAssignment = null
        pendingCollectionRemoval = null
        forceSelectionMode = false
        searchQuery = ""
        if (returnCollection != null) {
            libraryCurrentTab = returnCollection
        }
    }

    fun deleteSelectedPhotos(unpublish: Boolean = true) {
        isBusy = true
        photos.filter { selectedPhotoIds.contains(it.id) }.forEach { photo ->
            com.android.snippets.logic.PhotoStorageManager.deletePhotoFile(photo.uriString)
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
    
    val isAllSelectedFavorited: Boolean
        get() = _uiState.value.isAllSelectedFavorited

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
        val result = com.android.snippets.logic.SnippetManager.bulkAddSnippet(
            photos = photos,
            snippetFirstSeenTimes = snippetFirstSeenTimes,
            photoIds = photoIds,
            text = text
        ) ?: return

        if (result.updatedFirstSeen != snippetFirstSeenTimes) {
            snippetFirstSeenTimes = result.updatedFirstSeen
            saveSnippetFirstSeenTimes()
        }

        photos = result.updatedPhotos
        savePhotos()
        reconcileSurfacedMemories()

        val trimmedText = text.trim()
        if (result.skippedCount > 0) {
            showSnackbar("Added '$trimmedText' to selected photos (${result.skippedCount} skipped because they reached the limit of 6 snippets)")
        } else {
            showSnackbar("Added '$trimmedText' to ${result.addedCount} photos")
        }
        clearSelection()
    }

    fun bulkRemoveSnippet(photoIds: Set<String>, snippet: String) {
        val result = com.android.snippets.logic.SnippetManager.bulkRemoveSnippet(
            photos = photos,
            snippetFirstSeenTimes = snippetFirstSeenTimes,
            selectedFilterSnippets = selectedFilterSnippets,
            photoIds = photoIds,
            snippet = snippet
        ) ?: return

        photos = result.updatedPhotos
        savePhotos()
        result.emptiedMemoryIds.forEach { cancelMemoryNotification(it) }

        if (result.updatedFirstSeen != snippetFirstSeenTimes) {
            snippetFirstSeenTimes = result.updatedFirstSeen
            saveSnippetFirstSeenTimes()
        }

        if (result.filtersChanged) {
            selectedFilterSnippets = result.updatedFilterSnippets
            scrollToTop()
            saveFilterState()
        }

        showSnackbar("Removed '${snippet.trim()}' from selected photos")
        clearSelection()
    }

    fun bulkClearSnippets(photoIds: Set<String>) {
        if (photoIds.isEmpty()) return

        val result = com.android.snippets.logic.SnippetManager.bulkClearSnippets(photos, photoIds)
        photos = result.updatedPhotos
        savePhotos()
        result.emptiedMemoryIds.forEach { cancelMemoryNotification(it) }

        snippetFirstSeenTimes = snippetFirstSeenTimes.filterKeys { it in result.remainingSnippets }
        saveSnippetFirstSeenTimes()

        val previousFilterSize = selectedFilterSnippets.size
        selectedFilterSnippets = selectedFilterSnippets.filter { it in result.remainingSnippets }
        if (selectedFilterSnippets.size != previousFilterSize) {
            scrollToTop()
        }
        saveFilterState()

        showSnackbar("Cleared all snippets from selected photos")
        clearSelection()
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
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            val isDarkTheme = when (prefs.themePreference.uppercase()) {
                "LIGHT" -> false
                "DARK" -> true
                else -> (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            val photosToShare = selectedPhotoIds.mapNotNull { photoId ->
                photos.find { it.id == photoId }?.let { it to getPureSnippets(it) }
            }
            if (PhotoCardExporter.shareMultipleCards(context, photosToShare, isDarkTheme)) {
                clearSelection()
            }
        }
    }
    fun openDetail(id: String, tab: String? = null) {
        activePhotoId = id
        tab?.let { libraryCurrentTab = it }
    }

    fun closeDetail() {
        activePhotoId = null
    }

    fun navigateBack() {
        if (activePhotoId != null) {
            activePhotoId = null
        }
    }


    fun getPhotosForPeriod(month: Int?, year: Int): List<Photo> =
        com.android.snippets.logic.RecapManager.getRecapPhotos(photos, month, year)

    fun triggerMonthlyRecap(month: Int? = null, year: Int? = null) {
        val prevMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val targetMonth = month ?: prevMonthCal.get(Calendar.MONTH)
        val targetYear = year ?: if (month != null) Calendar.getInstance().get(Calendar.YEAR) else prevMonthCal.get(Calendar.YEAR)

        val monthCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, targetMonth)
            set(Calendar.YEAR, targetYear)
        }
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(monthCal.time)

        isYearlyRecap = false
        recapTitle = "$monthName Recap"
        recapPhotos = getPhotosForPeriod(month = targetMonth, year = targetYear)
        recapMonth = targetMonth
        recapYear = targetYear
    }

    fun triggerYearlyRecap(year: Int? = null) {
        val cal = Calendar.getInstance()
        val targetYear = year ?: (cal.get(Calendar.YEAR) - 1).coerceAtLeast(2020)

        isYearlyRecap = true
        recapTitle = "$targetYear Recap"
        recapPhotos = getPhotosForPeriod(month = null, year = targetYear)
        recapYear = targetYear
    }

    fun checkAndTriggerRecapOnLaunch() {
        if (photos.isEmpty()) return

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)

        // 1. Check Yearly Recap: when entering a new year (e.g. in January) for previous completed year
        val prevYear = currentYear - 1
        if (currentMonth == Calendar.JANUARY && prevYear >= 2020) {
            if (lastSeenYearlyRecapYear != prevYear) {
                val yearlyPhotos = getPhotosForPeriod(month = null, year = prevYear)
                if (yearlyPhotos.isNotEmpty()) {
                    lastSeenYearlyRecapYear = prevYear
                    viewModelScope.launch {
                        userPreferencesRepository.updateLastSeenYearlyRecapYear(prevYear)
                        userPreferencesRepository.updateLastNotifiedYearlyRecapYear(prevYear)
                    }
                    triggerYearlyRecap(prevYear)
                    pendingRecapLaunch = RecapLaunchEvent.Yearly(prevYear)
                    return
                } else {
                    lastSeenYearlyRecapYear = prevYear
                    viewModelScope.launch {
                        userPreferencesRepository.updateLastSeenYearlyRecapYear(prevYear)
                        userPreferencesRepository.updateLastNotifiedYearlyRecapYear(prevYear)
                    }
                }
            }
        }

        // 2. Check Monthly Recap: for completed previous month
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
                    userPreferencesRepository.updateLastNotifiedMonthlyRecapPeriod(recapKey)
                }
                triggerMonthlyRecap(targetMonth, targetYear)
                pendingRecapLaunch = RecapLaunchEvent.Monthly(targetMonth, targetYear)
            } else {
                // If there were no photos at all for that month, mark as seen to prevent repetitive checks
                lastSeenMonthlyRecapPeriod = recapKey
                viewModelScope.launch {
                    userPreferencesRepository.updateLastSeenMonthlyRecapPeriod(recapKey)
                    userPreferencesRepository.updateLastNotifiedMonthlyRecapPeriod(recapKey)
                }
            }
        }
    }

    fun testRecapNotification(isYearly: Boolean = false) {
        requestNotificationPermission = true
        viewModelScope.launch {
            com.android.snippets.logic.RecapManager.testRecapNotification(getApplication(), photos, isYearly)
        }
    }

    fun navigateLibrary() {
        searchQuery = ""
        val hadFilters = selectedFilterSnippets.isNotEmpty()
        selectedFilterSnippets = emptyList()
        if (hadFilters) {
            requestScrollToTop()
        }
        activePhotoId = null
        clearSelection()
    }

    fun navigateFilter() {
        showFilterSheet = true
    }

    fun closeFilter() {
        showFilterSheet = false
    }

    val activePhoto: Photo?
        get() = _uiState.value.activePhoto
    
    val filteredPhotos: List<Photo>
        get() = _uiState.value.filteredPhotos

    fun containsEmoji(s: String): Boolean = SnippetStyleHelper.containsEmoji(s)

    fun containsEmoticon(s: String): Boolean = SnippetStyleHelper.containsEmoticon(s)

    val favoritesUniqueSnippets: List<String>
        get() = _uiState.value.favoritesUniqueSnippets

    val allUniqueSnippets: List<String>
        get() = _uiState.value.allUniqueSnippets

    val locationNameMap: Map<String, String>
        get() = _uiState.value.locationNameMap

    val allUniqueLocations: List<String>
        get() = _uiState.value.allUniqueLocations

    fun getPureSnippets(photo: Photo): List<String> {
        return photo.snippets.filterNot { snippet -> 
            userCollections.any { it.equals(snippet, ignoreCase = true) } 
        }
    }

    fun getCollectionIcon(collection: String): Any =
        com.android.snippets.logic.CollectionManager.getCollectionIcon(
            collection = collection,
            editingCollectionEmoji = editingCollectionEmoji,
            editingEmojiPreview = editingEmojiPreview,
            collectionIcons = collectionIcons
        )

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







}