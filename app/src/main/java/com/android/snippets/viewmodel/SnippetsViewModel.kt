package com.android.snippets.viewmodel
import com.android.snippets.model.ThemePreference

import android.app.Application
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.android.snippets.service.AddPhotoTileService
import com.ln.android.snippets.R
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.snippets.logic.SnippetStyleHelper
import com.android.snippets.ui.util.PhotoCardExporter
import com.android.snippets.model.Photo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
import coil3.request.crossfade
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.data.repository.CollectionRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

typealias SnippetSortType = com.android.snippets.model.SnippetSortType
typealias PhotoSortType = com.android.snippets.model.PhotoSortType
typealias SnippetStyle = com.android.snippets.model.SnippetStyle

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
        @Volatile
        var cachedPhotos: List<Photo> = emptyList()
        @Volatile
        var cachedSnippetFirstSeenTimes: Map<String, Long> = emptyMap()
        @Volatile
        var cachedLocationNameMap: Map<String, String> = emptyMap()
        @Volatile
        var cachedUniqueLocations: List<String> = emptyList()
        @Volatile
        var cachedAllUniqueSnippets: List<String> = emptyList()

        fun rebuildSnippetFirstSeenTimes(source: List<Photo>): Map<String, Long> =
            com.android.snippets.logic.SnippetManager.rebuildSnippetFirstSeenTimes(source)

        private const val MIN_COLD_LAUNCH_LOADING_MS = 2000L
        private const val PRELOAD_PHOTO_COUNT = 40
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _uiState = MutableStateFlow(
        if (cachedPhotos.isNotEmpty()) {
            val initialSnippets = if (cachedAllUniqueSnippets.isNotEmpty()) {
                cachedAllUniqueSnippets
            } else {
                com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                    cachedPhotos,
                    SnippetSortType.New,
                    cachedSnippetFirstSeenTimes
                )
            }
            val locMap = if (cachedLocationNameMap.isNotEmpty()) {
                cachedLocationNameMap
            } else {
                com.android.snippets.ui.util.LocationUtils.buildLocationNameMap(cachedPhotos)
            }
            val uniqueLocs = if (cachedUniqueLocations.isNotEmpty()) {
                cachedUniqueLocations
            } else {
                com.android.snippets.ui.util.LocationUtils.buildUniqueLocations(cachedPhotos)
            }
            SnippetsUiState(
                photos = cachedPhotos,
                snippetFirstSeenTimes = cachedSnippetFirstSeenTimes,
                locationNameMap = locMap,
                allUniqueLocations = uniqueLocs,
                allUniqueSnippets = initialSnippets,
                isInitialLoading = false
            )
        } else {
            SnippetsUiState()
        }
    )
    val uiState: StateFlow<SnippetsUiState> = _uiState.asStateFlow()

    fun updateUiState(transform: (SnippetsUiState) -> SnippetsUiState) {
        _uiState.update(transform)
    }

    var photos: List<Photo>
        get() = _uiState.value.photos
        private set(value) {
            val locMap = com.android.snippets.ui.util.LocationUtils.buildLocationNameMap(value)
            val uniqueLocs = com.android.snippets.ui.util.LocationUtils.buildUniqueLocations(value)
            val allSnippets = com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                value,
                _uiState.value.snippetSortType,
                _uiState.value.snippetFirstSeenTimes
            )
            cachedPhotos = value
            cachedLocationNameMap = locMap
            cachedUniqueLocations = uniqueLocs
            cachedAllUniqueSnippets = allSnippets
            _uiState.update {
                it.copy(
                    photos = value,
                    locationNameMap = locMap,
                    allUniqueLocations = uniqueLocs,
                    allUniqueSnippets = allSnippets
                )
            }
        }

    var isInitialLoading: Boolean
        get() = _uiState.value.isInitialLoading
        private set(value) { _uiState.update { it.copy(isInitialLoading = value) } }

    var userBoxes: List<String>
        get() = _uiState.value.userBoxes
        private set(value) { _uiState.update { it.copy(userBoxes = value) } }

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

    var showCreateDialog: Boolean
        get() = _uiState.value.showCreateDialog
        set(value) { _uiState.update { it.copy(showCreateDialog = value) } }

    var editingBoxEmoji: String?
        get() = _uiState.value.editingBoxEmoji
        set(value) { _uiState.update { it.copy(editingBoxEmoji = value) } }

    var editingEmojiPreview: String?
        get() = _uiState.value.editingEmojiPreview
        set(value) { _uiState.update { it.copy(editingEmojiPreview = value) } }

    var boxIcons: Map<String, String>
        get() = _uiState.value.boxIcons
        set(value) { _uiState.update { it.copy(boxIcons = value) } }

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
        com.android.snippets.logic.SnippetManager.validateAndExecuteAddSnippet(
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

    fun createNewBox(): String {
        val newName = com.android.snippets.logic.BoxManager.generateNextBoxName(userBoxes)
        userBoxes = userBoxes + newName
        boxIcons = boxIcons + (newName to "")
        saveCollections()
        libraryCurrentTab = newName
        navigateLibrary()
        return newName
    }

    fun startEditingBoxEmoji(box: String) {
        editingBoxEmoji = box
        editingEmojiPreview = if (box == "Box 1" || box == "Favorites") {
            val custom = boxIcons["Box 1"] ?: boxIcons["Favorites"]
            if (custom != null && custom.none { it in 'a'..'z' || it in 'A'..'Z' }) custom else "❤️"
        } else {
            val custom = boxIcons[box]
            if (custom != null && custom.none { it in 'a'..'z' || it in 'A'..'Z' }) custom else ""
        }
        _uiState.update { it.copy(emojiKeyboardRequestCount = it.emojiKeyboardRequestCount + 1) }
    }

    fun updateEditingEmojiPreview(box: String, emoji: String) {
        if (editingBoxEmoji == box) {
            val clean = emoji.trim()
            if (clean.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                editingEmojiPreview = clean
                boxIcons = boxIcons + (box to clean)
            }
        }
    }

    fun finalizeBoxEmoji(box: String, emoji: String) {
        val result = com.android.snippets.logic.BoxManager.finalizeBoxEmoji(
            userBoxes = userBoxes,
            boxIcons = boxIcons,
            photos = photos,
            box = box,
            emoji = emoji
        ) ?: run {
            if (box.isBlank() && userBoxes.contains(box)) {
                deleteBox(box)
            }
            editingBoxEmoji = null
            editingEmojiPreview = null
            return
        }

        userBoxes = result.updatedBoxes
        boxIcons = result.updatedIcons
        val finalName = result.finalName
        val oldIndex = result.oldIndex

        if (finalName != "Box 1" && finalName != "Favorites") {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                storageMutex.withLock {
                    try {
                        val existing = collectionRepository.getCollectionByName(box)
                        if (existing != null) {
                            collectionRepository.deleteCollection(existing)
                        }
                        collectionRepository.insertCollection(
                            com.android.snippets.data.CollectionEntity(
                                name = finalName,
                                icon = result.updatedIcons[finalName] ?: emoji.trim(),
                                orderIndex = if (oldIndex != -1) oldIndex else userBoxes.size - 1
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        if (result.photosChanged) {
            photos = result.updatedPhotos
            savePhotos()
        }

        if (libraryCurrentTab == box) {
            libraryCurrentTab = finalName
        }

        saveCollections()
        editingBoxEmoji = null
        editingEmojiPreview = null
    }

    fun cancelEditingBoxEmoji() {
        val col = editingBoxEmoji
        editingBoxEmoji = null
        editingEmojiPreview = null
        if (col != null && col.isBlank() && userBoxes.contains(col)) {
            deleteBox(col)
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

    var pendingBoxAssignment: String?
        get() = _uiState.value.pendingBoxAssignment
        private set(value) { _uiState.update { it.copy(pendingBoxAssignment = value) } }

    var pendingBoxRemoval: String?
        get() = _uiState.value.pendingBoxRemoval
        private set(value) { _uiState.update { it.copy(pendingBoxRemoval = value) } }

    fun startBoxAssignment(box: String) {
        _uiState.update {
            it.copy(
                pendingBoxAssignment = box,
                selectedPhotoIds = emptySet(),
                libraryCurrentTab = "Box"
            )
        }
    }

    fun startBoxRemoval(box: String) {
        _uiState.update {
            it.copy(pendingBoxRemoval = box, selectedPhotoIds = emptySet())
        }
    }

    fun confirmBoxAssignment() {
        val box = pendingBoxAssignment ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = com.android.snippets.logic.BoxManager.assignPhotosToBox(photos, selectedPhotoIds, box)
            savePhotos()
            
            val boxDisplayName = com.android.snippets.logic.BoxManager.getBoxDisplayName(box, userBoxes)
            val message = if (box == "Box 1" || box == "Favorites") "Added to Box 1" else if (box == "Box" || box == "Library") "Added to Box" else "Added to $boxDisplayName"
            showSnackbar(
                message = message,
                actionLabel = "View",
                onAction = {
                    libraryCurrentTab = box
                    navigateLibrary()
                }
            )
        }
        _uiState.update {
            it.copy(
                pendingBoxAssignment = null,
                selectedPhotoIds = emptySet()
            )
        }
        navigateLibrary()
    }

    fun confirmBoxRemoval() {
        val box = pendingBoxRemoval ?: return
        if (selectedPhotoIds.isNotEmpty()) {
            photos = com.android.snippets.logic.BoxManager.removePhotosFromBox(photos, selectedPhotoIds, box)
            savePhotos()
            
            val boxDisplayName = com.android.snippets.logic.BoxManager.getBoxDisplayName(box, userBoxes)
            val message = if (box == "Box 1" || box == "Favorites") "Removed from Box 1" else if (box == "Box" || box == "Library") "Removed from Box" else "Removed from $boxDisplayName"
            showSnackbar(message = message)
        }
        _uiState.update {
            it.copy(
                pendingBoxRemoval = null,
                selectedPhotoIds = emptySet(),
                libraryCurrentTab = box
            )
        }
        navigateLibrary()
    }

    var showBulkAddToBoxDialog: Boolean
        get() = _uiState.value.showBulkAddToBoxDialog
        set(value) { _uiState.update { it.copy(showBulkAddToBoxDialog = value) } }

    var showBulkDeleteModal: Boolean
        get() = _uiState.value.showBulkDeleteModal
        set(value) { _uiState.update { it.copy(showBulkDeleteModal = value) } }

    var showRequestTileDialog: Boolean
        get() = _uiState.value.showRequestTileDialog
        set(value) { _uiState.update { it.copy(showRequestTileDialog = value) } }

    var snippetSortType: SnippetSortType
        get() = _uiState.value.snippetSortType
        set(value) {
            _uiState.update { current ->
                val allSnippets = com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                    current.photos,
                    value,
                    current.snippetFirstSeenTimes
                )
                current.copy(snippetSortType = value, allUniqueSnippets = allSnippets)
            }
            saveFilterState()
        }

    var isSortAscending: Boolean
        get() = _uiState.value.isSortAscending
        set(value) {
            _uiState.update { it.copy(isSortAscending = value) }
            saveFilterState()
        }

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
        if (tabName == "Box" || tabName == "Library") return _uiState.value.photoSortType
        return _uiState.value.boxPhotoSortTypes[tabName] ?: PhotoSortType.DateNewest
    }

    fun setPhotoSortTypeFor(tabName: String, sortType: PhotoSortType) {
        if (tabName == "Box" || tabName == "Library") {
            _uiState.update { it.copy(photoSortType = sortType, scrollToTopTrigger = System.currentTimeMillis()) }
        } else {
            _uiState.update {
                it.copy(
                    boxPhotoSortTypes = it.boxPhotoSortTypes + (tabName to sortType),
                    scrollToTopTrigger = System.currentTimeMillis()
                )
            }
        }
        saveFilterState()
    }

    fun sortPhotos(baseList: List<Photo>, sort: PhotoSortType): List<Photo> =
        com.android.snippets.logic.PhotoSearchHelper.sortPhotos(baseList, sort)

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
                    val resolvedSnippetSort = try { SnippetSortType.valueOf(userPrefs.snippetSortType) } catch (e: Exception) { SnippetSortType.New }
                    val allSnippets = if (resolvedSnippetSort != current.snippetSortType && current.photos.isNotEmpty()) {
                        com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                            current.photos,
                            resolvedSnippetSort,
                            current.snippetFirstSeenTimes
                        )
                    } else {
                        current.allUniqueSnippets
                    }
                    current.copy(
                        hasPromptedNotificationPermission = userPrefs.hasPromptedNotificationPermission,
                        hasRequestedTileService = userPrefs.hasRequestedTileService,
                        lastSeenYearlyRecapYear = userPrefs.lastSeenYearlyRecapYear,
                        lastSeenMonthlyRecapPeriod = userPrefs.lastSeenMonthlyRecapPeriod,
                        snippetSortType = resolvedSnippetSort,
                        isSortAscending = userPrefs.isSortAscending,
                        selectedFilterSnippets = if (current.filteringCategory != null) current.selectedFilterSnippets else userPrefs.selectedFilterSnippets.toList(),
                        photoSortType = try { PhotoSortType.valueOf(userPrefs.photoSortType) } catch (e: Exception) { PhotoSortType.DateNewest },
                        boxPhotoSortTypes = sortMap,
                        allUniqueSnippets = allSnippets
                    )
                }
            }
        }
        observeRepositoryFlows()
        if (cachedPhotos.isEmpty()) {
            loadAllData()
        }
    }

    private fun observeRepositoryFlows() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            collectionRepository.allCollectionsFlow.collect { entities ->
                val sorted = entities.sortedBy { it.orderIndex }
                val mappedNames = mutableListOf<String>()
                val mappedIcons = mutableMapOf<String, String>()
                var hasLegacyBox1 = false

                sorted.forEachIndexed { idx, entity ->
                    val expectedName = "Box ${idx + 2}"
                    val match = Regex("^Box (\\d+)$", RegexOption.IGNORE_CASE).find(entity.name)
                    val boxNum = match?.groupValues?.get(1)?.toIntOrNull()

                    if (entity.name == "Favorites") {
                        entity.icon?.takeIf { it.isNotBlank() && it.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }?.let { mappedIcons["Favorites"] = it }
                    } else if (boxNum != null && boxNum >= 2) {
                        mappedNames.add(entity.name)
                        entity.icon?.takeIf { it.isNotBlank() && it.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }?.let { mappedIcons[entity.name] = it }
                    } else {
                        hasLegacyBox1 = true
                        mappedNames.add(expectedName)
                        val icon = entity.icon?.takeIf { it.isNotBlank() && it.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }
                            ?: if (entity.name.none { it in 'a'..'z' || it in 'A'..'Z' }) entity.name else ""
                        if (icon.isNotBlank()) {
                            mappedIcons[expectedName] = icon
                        }
                    }
                }

                if (hasLegacyBox1) {
                    try {
                        sorted.filter { it.name != "Favorites" }.forEach { collectionRepository.deleteCollection(it) }
                        mappedNames.forEachIndexed { idx, name ->
                            collectionRepository.insertCollection(
                                com.android.snippets.data.CollectionEntity(
                                    name = name,
                                    icon = mappedIcons[name] ?: "",
                                    orderIndex = idx
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (userBoxes != mappedNames) {
                        userBoxes = mappedNames
                    }
                    if (boxIcons != mappedIcons) {
                        boxIcons = mappedIcons
                    }
                }
            }
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            photoRepository.allPhotosFlow.collect { roomPhotos ->
                val currentBoxes = _uiState.value.userBoxes
                val currentIcons = _uiState.value.boxIcons
                val targetFirstBox = currentBoxes.firstOrNull() ?: "Box 2"
                val iconToBoxMap = currentIcons.filter { (_, v) -> v.isNotBlank() && v.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }
                    .entries.associate { (k, v) -> v to k }

                var needsPhotoUpdate = false
                val healedPhotos = roomPhotos.map { rp ->
                    val cols = (rp.collections ?: emptyList()).toMutableList()
                    var changed = false
                    var isFav = rp.isFavorite
                    if (cols.contains("Favorites")) {
                        cols.remove("Favorites")
                        isFav = true
                        changed = true
                    }
                    if (cols.contains("Box 1")) {
                        cols.remove("Box 1")
                        if (!isFav) {
                            if (!cols.contains(targetFirstBox)) cols.add(targetFirstBox)
                        }
                        changed = true
                    }
                    val emojis = cols.filter { it.isNotBlank() && it.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }
                    if (emojis.isNotEmpty()) {
                        emojis.forEach { em ->
                            cols.remove(em)
                            val b = iconToBoxMap[em] ?: targetFirstBox
                            if (!cols.contains(b)) cols.add(b)
                        }
                        changed = true
                    }
                    if (changed) {
                        needsPhotoUpdate = true
                        rp.copy(isFavorite = isFav, collections = cols.distinct())
                    } else rp
                }

                if (needsPhotoUpdate) {
                    try {
                        photoRepository.insertPhotos(healedPhotos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val activePhotos = if (needsPhotoUpdate) healedPhotos else roomPhotos
                val current = photos
                if (current.isNotEmpty() && current.size == activePhotos.size) {
                    val currentMap = current.associateBy { it.id }
                    val isIdentical = activePhotos.all { rp ->
                        val p = currentMap[rp.id]
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

                val processedPhotos = activePhotos.map { p ->
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

                val rebuiltFirstSeen = rebuildSnippetFirstSeenTimes(processedPhotos)
                val locMap = com.android.snippets.ui.util.LocationUtils.buildLocationNameMap(processedPhotos)
                val uniqueLocs = com.android.snippets.ui.util.LocationUtils.buildUniqueLocations(processedPhotos)
                val allSnippets = com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                    processedPhotos,
                    _uiState.value.snippetSortType,
                    rebuiltFirstSeen
                )

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    cachedPhotos = processedPhotos
                    cachedSnippetFirstSeenTimes = rebuiltFirstSeen
                    cachedLocationNameMap = locMap
                    cachedUniqueLocations = uniqueLocs
                    cachedAllUniqueSnippets = allSnippets
                    if (photos != processedPhotos) {
                        _uiState.update {
                            it.copy(
                                photos = processedPhotos,
                                snippetFirstSeenTimes = rebuiltFirstSeen,
                                locationNameMap = locMap,
                                allUniqueLocations = uniqueLocs,
                                allUniqueSnippets = allSnippets
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val startTime = System.currentTimeMillis()

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

            // 1. Fully heal file URIs, content URIs, and backfill dimensions BEFORE surfacing to UI
            val (fileHealedPhotos, fileHealingNeeded) = com.android.snippets.logic.PhotoStorageManager.healFileUris(app, rawRoomPhotos)
            val (contentHealedPhotos, contentHealingNeeded) = com.android.snippets.logic.PhotoStorageManager.healContentUris(app, fileHealedPhotos)
            val (dimensionHealedPhotos, dimensionsHealingNeeded) = com.android.snippets.logic.PhotoStorageManager.backfillMissingDimensions(app, contentHealedPhotos)

            var workingPhotos = dimensionHealedPhotos
            var dbNeedsUpdate = fileHealingNeeded || contentHealingNeeded || dimensionsHealingNeeded

            // 2. Resolve place names for any photos with location links
            val needsLocationResolution = workingPhotos.filter { !it.locationLink.isNullOrBlank() && it.locationName.isNullOrBlank() }
            if (needsLocationResolution.isNotEmpty()) {
                val updatedPhotos = workingPhotos.toMutableList()
                var locationsChanged = false
                needsLocationResolution.forEach { p ->
                    val link = p.locationLink ?: return@forEach
                    val name = com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(link, app)
                        ?: try {
                            withTimeoutOrNull(1500L) {
                                com.android.snippets.ui.util.LocationUtils.resolveUrlLocation(link, app)
                            }
                        } catch (_: Exception) {
                            null
                        }
                    if (!name.isNullOrBlank()) {
                        val clean = com.android.snippets.ui.util.LocationUtils.cleanLocationName(name)
                        val idx = updatedPhotos.indexOfFirst { it.id == p.id }
                        if (idx != -1 && updatedPhotos[idx].locationName.isNullOrBlank()) {
                            updatedPhotos[idx] = updatedPhotos[idx].copy(locationName = clean)
                            locationsChanged = true
                        }
                    }
                }
                if (locationsChanged) {
                    workingPhotos = updatedPhotos
                    dbNeedsUpdate = true
                }
            }

            // Persist all optimizations to Room once in a single transaction if changes occurred
            if (dbNeedsUpdate) {
                try {
                    photoRepository.insertPhotos(workingPhotos)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. Load collections
            val collectionEntities = try {
                collectionRepository.getAllCollections()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            var needsMigration = false
            val migratedNames = mutableListOf<String>()
            val migratedIcons = collectionEntities.mapNotNull { it.icon?.let { icon -> it.name to icon } }.toMap().toMutableMap()
            val oldToNewNameMap = mutableMapOf<String, String>()

            collectionEntities.sortedBy { it.orderIndex }.forEachIndexed { idx, entity ->
                val oldName = entity.name
                val match = Regex("^Box (\\d+)$", RegexOption.IGNORE_CASE).find(oldName)
                val boxNum = match?.groupValues?.get(1)?.toIntOrNull()
                val expectedName = "Box ${idx + 2}"
                val newName = if (boxNum != null && boxNum >= 2) {
                    oldName
                } else {
                    needsMigration = true
                    expectedName
                }
                migratedNames.add(newName)
                if (oldName != newName) {
                    oldToNewNameMap[oldName] = newName
                    val existingIcon = entity.icon?.takeIf { it.isNotBlank() } ?: oldName
                    migratedIcons.remove(oldName)
                    migratedIcons[newName] = existingIcon
                }
            }

            val roomCollections = (if (needsMigration) migratedNames else collectionEntities.map { it.name })
                .filter { it != "Box" && it != "Library" && it != "Box 1" && it != "Favorites" }
            val loadedIcons = (if (needsMigration) migratedIcons else collectionEntities.mapNotNull { it.icon?.let { icon -> it.name to icon } }.toMap()).toMutableMap()
            loadedIcons.remove("Box 1")
            loadedIcons.remove("Favorites")
            loadedIcons.keys.toList().forEach { k ->
                val v = loadedIcons[k] ?: ""
                if (v.any { it in 'a'..'z' || it in 'A'..'Z' }) {
                    loadedIcons[k] = ""
                }
            }

            val targetFirstBox = roomCollections.firstOrNull() ?: "Box 2"
            val iconToBoxMap = loadedIcons.filter { (_, v) -> v.isNotBlank() && v.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }
                .entries.associate { (k, v) -> v to k }

            var photosRestored = false
            workingPhotos = workingPhotos.map { photo ->
                val cols = photo.collections.toMutableList()
                var pChanged = false
                var isFav = photo.isFavorite

                if (cols.contains("Favorites")) {
                    cols.remove("Favorites")
                    isFav = true
                    pChanged = true
                }
                if (cols.contains("Box 1")) {
                    cols.remove("Box 1")
                    if (!isFav) {
                        if (!cols.contains(targetFirstBox)) cols.add(targetFirstBox)
                    }
                    pChanged = true
                }
                oldToNewNameMap.forEach { (oldName, newName) ->
                    if (cols.contains(oldName)) {
                        cols.remove(oldName)
                        if (!cols.contains(newName)) cols.add(newName)
                        pChanged = true
                    }
                }
                val emojis = cols.filter { it.isNotBlank() && it.none { c -> c in 'a'..'z' || c in 'A'..'Z' } }
                if (emojis.isNotEmpty()) {
                    emojis.forEach { em ->
                        cols.remove(em)
                        val b = iconToBoxMap[em] ?: targetFirstBox
                        if (!cols.contains(b)) cols.add(b)
                    }
                    pChanged = true
                }

                if (pChanged) {
                    photosRestored = true
                    photo.copy(isFavorite = isFav, collections = cols.distinct())
                } else photo
            }

            if (photosRestored) {
                try {
                    photoRepository.insertPhotos(workingPhotos)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (needsMigration) {
                try {
                    collectionEntities.forEach { collectionRepository.deleteCollection(it) }
                    roomCollections.forEachIndexed { idx, name ->
                        collectionRepository.insertCollection(
                            com.android.snippets.data.CollectionEntity(
                                name = name,
                                icon = loadedIcons[name] ?: "",
                                orderIndex = idx
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 4. Sort and process photos
            val processedPhotos = workingPhotos.map { p ->
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
            val locMap = com.android.snippets.ui.util.LocationUtils.buildLocationNameMap(processedPhotos)
            val uniqueLocs = com.android.snippets.ui.util.LocationUtils.buildUniqueLocations(processedPhotos)
            val allSnippets = com.android.snippets.logic.SnippetManager.extractAllUniqueSnippets(
                processedPhotos,
                _uiState.value.snippetSortType,
                mergedFirstSeen
            )

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

            cachedPhotos = processedPhotos
            cachedSnippetFirstSeenTimes = mergedFirstSeen
            cachedLocationNameMap = locMap
            cachedUniqueLocations = uniqueLocs
            cachedAllUniqueSnippets = allSnippets

            // 5. Update UI state with fully healed and stabilized data
            _uiState.update {
                it.copy(
                    photos = processedPhotos,
                    userBoxes = roomCollections,
                    boxIcons = loadedIcons,
                    snippetFirstSeenTimes = mergedFirstSeen,
                    locationNameMap = locMap,
                    allUniqueLocations = uniqueLocs,
                    allUniqueSnippets = allSnippets
                )
            }

            // 6. Preload and warm up images in Coil's memory and disk cache concurrently
            preloadPhotoImages(processedPhotos.take(PRELOAD_PHOTO_COUNT))

            // 7. Reconcile memories and notifications
            reconcileSurfacedMemories()
            reconcileMemoryNotifications()
            checkAndTriggerRecapOnLaunch()

            // 8. Ensure cold launch loading indicator lasts long enough to guarantee complete stabilization
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < MIN_COLD_LAUNCH_LOADING_MS) {
                delay(MIN_COLD_LAUNCH_LOADING_MS - elapsed)
            }

            // 9. Dismiss loading indicator
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                isInitialLoading = false
            }
        }
    }

    private suspend fun preloadPhotoImages(photosToPreload: List<Photo>) = coroutineScope {
        if (photosToPreload.isEmpty()) return@coroutineScope

        val app = getApplication<Application>()
        val loader = app.imageLoader
        val dispatcher = kotlinx.coroutines.Dispatchers.IO.limitedParallelism(4)

        photosToPreload.map { photo ->
            async(dispatcher) {
                try {
                    val request = ImageRequest.Builder(app)
                        .data(photo.uriString)
                        .crossfade(false)
                        .memoryCacheKey(photo.uriString)
                        .placeholderMemoryCacheKey(photo.uriString)
                        .build()
                    loader.execute(request)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
        }.awaitAll()
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
                collectionPhotoSortTypesJson = json.encodeToString(_uiState.value.boxPhotoSortTypes)
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
                    val validBoxes = userBoxes.filter { it != "Box" && it != "Library" && it != "Box 1" && it != "Favorites" }
                    val userColSet = validBoxes.toSet()
                    val deletedCols = currentCols.filter { it.name !in userColSet }
                    if (deletedCols.isNotEmpty()) {
                        deletedCols.forEach { collectionRepository.deleteCollection(it) }
                    }
                    collectionRepository.insertCollections(
                        validBoxes.mapIndexed { idx, name ->
                            val existing = currentCols.find { it.name == name }
                            com.android.snippets.data.CollectionEntity(
                                name = name,
                                icon = boxIcons[name] ?: existing?.icon,
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
                val wasEmpty = currentPhotosList.isEmpty()
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
                        openDetail(singleDetailToOpen, tab = collection ?: "Box")
                    } else if (newPhotosList.isNotEmpty() && uris.size > 1) {
                        showSnackbar("${newPhotosList.size} photos added")
                    }
                }

                preloadPhotoImages(newPhotosList.take(8))
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isAddingPhotos = false
                    if (wasEmpty && newPhotosList.isNotEmpty() && !_uiState.value.hasRequestedTileService) {
                        showRequestTileDialog = true
                    }
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

    fun addPhotosToLibrary(uris: List<Uri>, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = uris, locationLink = locationLink, locationName = locationName)
    }

    fun addPhotosToBox(uris: List<Uri>, box: String, locationLink: String? = null, locationName: String? = null) {
        addPhotos(uris = uris, collection = box, locationLink = locationLink, locationName = locationName)
    }

    fun createBox(box: String, openAfterCreate: Boolean = true) {
        val emoji = box.trim()
        if (emoji.isNotBlank() && userBoxes.none { it.equals(emoji, ignoreCase = true) }) {
            userBoxes = userBoxes + emoji
            saveCollections()
            if (openAfterCreate) {
                libraryCurrentTab = emoji
                navigateLibrary()
            }
        }
    }

    fun deleteBox(box: String) {
        if (box == "Box" || box == "Library" || box == "Box 1" || box == "Favorites" || !userBoxes.contains(box)) return

        val result = com.android.snippets.logic.BoxManager.deleteBox(
            userBoxes = userBoxes,
            boxIcons = boxIcons,
            photos = photos,
            selectedFilterSnippets = selectedFilterSnippets,
            box = box
        )

        userBoxes = result.updatedBoxes
        boxIcons = result.updatedIcons
        _uiState.update { it.copy(boxPhotoSortTypes = it.boxPhotoSortTypes - box) }
        
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

        if (pendingBoxAssignment == box) {
            pendingBoxAssignment = null
        }
        if (editingBoxEmoji == box) {
            cancelEditingBoxEmoji()
        }
        if (libraryCurrentTab == box) {
            libraryCurrentTab = "Box"
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

    fun dismissRequestTileDialog() {
        showRequestTileDialog = false
        markTileServiceRequested()
    }

    fun confirmRequestTileDialog(context: Context) {
        showRequestTileDialog = false
        markTileServiceRequested()
        requestTileService(context)
    }

    fun markTileServiceRequested() {
        viewModelScope.launch {
            userPreferencesRepository.updateHasRequestedTileService(true)
        }
    }

    fun requestTileService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager = context.getSystemService(StatusBarManager::class.java)
            val componentName = ComponentName(context, AddPhotoTileService::class.java)
            val icon = Icon.createWithResource(context, R.drawable.ic_qs_add_photo)
            val label = context.getString(R.string.tile_add_photos)
            val executor = ContextCompat.getMainExecutor(context)
            try {
                statusBarManager?.requestAddTileService(
                    componentName,
                    label,
                    icon,
                    executor
                ) { result ->
                    when (result) {
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> {
                            showSnackbar(context.getString(R.string.tile_added_success))
                        }
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                            showSnackbar(context.getString(R.string.tile_already_added))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showSnackbar(context.getString(R.string.tile_add_manual_instruction))
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
        val returnCollection = pendingBoxAssignment
        selectedPhotoIds = emptySet()
        pendingBoxAssignment = null
        pendingBoxRemoval = null
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

    fun toggleSelectedInBox(box: String) {
        isBusy = true
        val now = System.currentTimeMillis()
        photos = photos.map { photo ->
            if (selectedPhotoIds.contains(photo.id)) {
                val updatedCollections = if (photo.collections.contains(box)) {
                    photo.collections - box
                } else {
                    photo.collections + box
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
            showSnackbar(message = if (count == 1) "Added to Box 1" else "$count added to Box 1")
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

    fun containsEmoji(s: String): Boolean = SnippetStyleHelper.containsEmoji(s)

    fun containsEmoticon(s: String): Boolean = SnippetStyleHelper.containsEmoticon(s)

    val allUniqueSnippets: List<String>
        get() = _uiState.value.allUniqueSnippets

    val locationNameMap: Map<String, String>
        get() = _uiState.value.locationNameMap

    val allUniqueLocations: List<String>
        get() = _uiState.value.allUniqueLocations

    fun getPureSnippets(photo: Photo): List<String> {
        return photo.snippets.filterNot { snippet -> 
            userBoxes.any { it.equals(snippet, ignoreCase = true) } 
        }
    }

    fun getBoxIcon(box: String): Any =
        com.android.snippets.logic.BoxManager.getBoxIcon(
            box = box,
            editingBoxEmoji = editingBoxEmoji,
            editingEmojiPreview = editingEmojiPreview,
            boxIcons = boxIcons
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
