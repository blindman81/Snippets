package com.android.snippets.logic

import com.android.snippets.model.Photo
import com.android.snippets.ui.util.LocationUtils
import com.android.snippets.viewmodel.PhotoSortType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoSearchHelper {

    private val groupDateFormat = ThreadLocal.withInitial { SimpleDateFormat("EEE, d MMM", Locale.getDefault()) }
    private val weekDateFormat = ThreadLocal.withInitial { SimpleDateFormat("'Week' w, yyyy", Locale.getDefault()) }
    private val monthDateFormat = ThreadLocal.withInitial { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    private val yearDateFormat = ThreadLocal.withInitial { SimpleDateFormat("yyyy", Locale.getDefault()) }
    private val dayOfWeekFormat = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale.getDefault()) }

    fun matchesTab(photo: Photo, tab: String): Boolean = when (tab) {
        "Library" -> true
        "Favorites" -> photo.isFavorite
        else -> photo.collections.contains(tab)
    }

    fun filterPhotosByTab(photos: List<Photo>, tab: String): List<Photo> {
        if (tab == "Library") return photos
        return photos.filter { matchesTab(it, tab) }
    }

    fun filterPhotos(
        photos: List<Photo>,
        parsed: ParsedSearchQuery,
        selectedFilterSnippets: List<String>,
        locationNameMap: Map<String, String> = emptyMap(),
        tab: String? = null
    ): List<Photo> {
        val baseList = if (tab != null && tab != "Library") {
            photos.filter { matchesTab(it, tab) }
        } else {
            photos
        }

        val textTokens = parsed.textTokens
        val directives = parsed.directives
        val hasSearchCriteria = textTokens.isNotEmpty() || directives.isNotEmpty()

        if (!hasSearchCriteria && selectedFilterSnippets.isEmpty()) {
            return baseList
        }

        return baseList.filter { photo ->
            var dateObj: Date? = null
            var dayName: String? = null
            var monthName: String? = null
            var yearName: String? = null
            var weekName: String? = null
            var fullDate: String? = null

            // 1. Text criteria: All textTokens must match (AND logic)
            val matchesText = textTokens.isEmpty() || textTokens.all { part ->
                val isLocationQuery = part.startsWith("location:", ignoreCase = true) ||
                        part.startsWith("loc:", ignoreCase = true) ||
                        part.startsWith("place:", ignoreCase = true)
                val searchTerm = if (isLocationQuery) {
                    part.substringAfter(':').trim()
                } else {
                    part
                }

                val locCandidates = listOfNotNull(
                    photo.locationName?.trim()?.takeIf { it.isNotBlank() },
                    photo.locationLink?.let { locationNameMap[it]?.trim()?.takeIf { name -> name.isNotBlank() } },
                    photo.locationLink?.let { LocationUtils.extractPlaceNameFromLink(it)?.trim()?.takeIf { name -> name.isNotBlank() } },
                    photo.locationLink?.trim()?.takeIf { !it.startsWith("http://", ignoreCase = true) && !it.startsWith("https://", ignoreCase = true) && it.isNotBlank() }
                )

                val matchesLocation = if (isLocationQuery && searchTerm.isEmpty()) {
                    locCandidates.isNotEmpty()
                } else {
                    locCandidates.any { loc ->
                        loc.contains(searchTerm, ignoreCase = true) ||
                        LocationUtils.cleanLocationName(loc).contains(searchTerm, ignoreCase = true)
                    }
                }

                if (isLocationQuery) {
                    matchesLocation
                } else {
                    val isNegationQuery = part.startsWith("not ", ignoreCase = true) || part.equals("not", ignoreCase = true)

                    photo.snippets.any { s ->
                        val index = s.indexOf(part, ignoreCase = true)
                        if (index == -1) return@any false

                        val isPrecededByNot = s.startsWith("not ", ignoreCase = true) && !isNegationQuery
                        !isPrecededByNot
                    } ||
                    photo.collections.any { it.contains(part, ignoreCase = true) } ||
                    (photo.name?.contains(part, ignoreCase = true) == true) ||
                    matchesLocation ||
                    run {
                        if (dateObj == null) {
                            dateObj = Date(photo.date)
                            dayName = dayOfWeekFormat.get()?.format(dateObj) ?: ""
                            monthName = monthDateFormat.get()?.format(dateObj) ?: ""
                            yearName = yearDateFormat.get()?.format(dateObj) ?: ""
                            weekName = weekDateFormat.get()?.format(dateObj) ?: ""
                            fullDate = groupDateFormat.get()?.format(dateObj) ?: ""
                        }
                        dayName!!.contains(part, ignoreCase = true) ||
                        monthName!!.contains(part, ignoreCase = true) ||
                        yearName!!.contains(part, ignoreCase = true) ||
                        weekName!!.contains(part, ignoreCase = true) ||
                        fullDate!!.contains(part, ignoreCase = true)
                    }
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
                matchesSearch || matchesChips
            } else if (hasSearchCriteria) {
                matchesSearch
            } else {
                matchesChips
            }
        }
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
}
