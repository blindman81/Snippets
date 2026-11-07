package com.android.snippets.viewmodel

import com.android.snippets.logic.ParsedSearchQuery
import com.android.snippets.logic.SearchQueryParser
import com.android.snippets.logic.SnippetManager
import com.android.snippets.model.Photo
import com.android.snippets.model.PhotoSortType
import com.android.snippets.model.SnippetSortType
import com.android.snippets.model.ThemePreference
import com.android.snippets.ui.util.LocationUtils
import java.util.Calendar

/**
 * Immutable UI State for Snippets following Google's Unidirectional Data Flow (UDF) pattern.
 * Encapsulates the entire presentation state of the library and main screens.
 */
data class SnippetsUiState(
    val photos: List<Photo> = emptyList(),
    val isInitialLoading: Boolean = true,
    val userCollections: List<String> = emptyList(),
    val collectionIcons: Map<String, String> = emptyMap(),
    val libraryCurrentTab: String = "Library",
    val activePhotoId: String? = null,
    val searchQuery: String = "",
    val selectedFilterSnippets: List<String> = emptyList(),
    val selectedPhotoIds: Set<String> = emptySet(),
    val forceSelectionMode: Boolean = false,
    val isBusy: Boolean = false,
    val isAddingPhotos: Boolean = false,
    val isCuratingMemories: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showBulkDeleteModal: Boolean = false,
    val showBulkAddToCollectionDialog: Boolean = false,
    val showCreateDialog: Boolean = false,
    val focusCreateCollection: Boolean = false,
    val editingCollectionEmoji: String? = null,
    val editingEmojiPreview: String? = null,
    val activeCollection: String? = null,
    val pendingCollectionAssignment: String? = null,
    val pendingCollectionRemoval: String? = null,
    val photoSortType: PhotoSortType = PhotoSortType.DateNewest,
    val collectionPhotoSortTypes: Map<String, PhotoSortType> = emptyMap(),
    val snippetSortType: SnippetSortType = SnippetSortType.New,
    val isSortAscending: Boolean = true,
    val snackbarMessage: String? = null,
    val snackbarActionLabel: String? = null,
    val onSnackbarAction: (() -> Unit)? = null,
    val snackbarToken: Long = 0L,
    val scrollToTopTrigger: Long = 0L,
    val pendingRecapLaunch: SnippetsViewModel.RecapLaunchEvent? = null,
    val recapTitle: String = "This Month",
    val recapPhotos: List<Photo> = emptyList(),
    val isYearlyRecap: Boolean = false,
    val recapMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val recapYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val lastSeenYearlyRecapYear: Int = -1,
    val lastSeenMonthlyRecapPeriod: String = "",
    val snippetFirstSeenTimes: Map<String, Long> = emptyMap(),
    val filteringCategory: String? = null,
    val hasPromptedNotificationPermission: Boolean = false,
    val requestNotificationPermission: Boolean = false,
    val pendingFavoriteIntent: Boolean = false,
    val pendingAddPhotoIntentToken: Long = 0L
) {
    val isSelectionMode: Boolean
        get() = forceSelectionMode || selectedPhotoIds.isNotEmpty() || pendingCollectionAssignment != null || pendingCollectionRemoval != null

    val isSearching: Boolean
        get() = searchQuery.isNotBlank()

    val isFiltering: Boolean
        get() = selectedFilterSnippets.isNotEmpty()

    val activePhoto: Photo?
        get() = photos.find { it.id == activePhotoId }

    val isAllSelectedFavorited: Boolean
        get() = selectedPhotoIds.isNotEmpty() && selectedPhotoIds.all { id -> photos.find { it.id == id }?.isFavorite == true }

    val parsedSearchQuery: ParsedSearchQuery
        get() = SearchQueryParser.parseSearchQuery(searchQuery)

    val currentSearchSortType: PhotoSortType?
        get() = parsedSearchQuery.directives.lastOrNull()?.sortType

    val effectivePhotoSortType: PhotoSortType
        get() = currentSearchSortType ?: (if (libraryCurrentTab == "Library") photoSortType else collectionPhotoSortTypes[libraryCurrentTab] ?: PhotoSortType.DateNewest)

    val filteredPhotos: List<Photo>
        get() {
            val parsed = parsedSearchQuery
            val hasSearchCriteria = parsed.textTokens.isNotEmpty() || parsed.directives.isNotEmpty()
            val baseList = com.android.snippets.logic.PhotoSearchHelper.filterPhotos(photos, parsed, selectedFilterSnippets)
            val effectiveSort = effectivePhotoSortType
            return if (!hasSearchCriteria && selectedFilterSnippets.isEmpty() && effectiveSort == PhotoSortType.DateNewest) {
                baseList
            } else {
                com.android.snippets.logic.PhotoSearchHelper.sortPhotos(baseList, effectiveSort)
            }
        }

    val favoritesUniqueSnippets: List<String>
        get() = SnippetManager.extractFavoritesUniqueSnippets(photos)

    val allUniqueSnippets: List<String>
        get() = SnippetManager.extractAllUniqueSnippets(photos, snippetSortType, snippetFirstSeenTimes)

    val locationNameMap: Map<String, String>
        get() = LocationUtils.buildLocationNameMap(photos)

    val allUniqueLocations: List<String>
        get() = LocationUtils.buildUniqueLocations(photos)
}
