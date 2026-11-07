package com.android.snippets.logic

import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.PhotoSortType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoSearchHelper {

    private val groupDateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val weekDateFormat = SimpleDateFormat("'Week' w, yyyy", Locale.getDefault())
    private val monthDateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearDateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    fun filterPhotos(
        photos: List<Photo>,
        parsed: ParsedSearchQuery,
        selectedFilterSnippets: List<String>
    ): List<Photo> {
        val textTokens = parsed.textTokens
        val directives = parsed.directives
        val hasSearchCriteria = textTokens.isNotEmpty() || directives.isNotEmpty()

        if (!hasSearchCriteria && selectedFilterSnippets.isEmpty()) {
            return photos
        }

        return photos.filter { photo ->
            var dateObj: Date? = null
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

                    val isPrecededByNot = s.startsWith("not ", ignoreCase = true) && !isNegationQuery
                    !isPrecededByNot
                } ||
                photo.collections.any { it.contains(part, ignoreCase = true) } ||
                (photo.name?.contains(part, ignoreCase = true) == true) ||
                run {
                    if (dateObj == null) {
                        dateObj = Date(photo.date)
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
