package com.android.snippets.logic

import com.android.snippets.viewmodel.PhotoSortType
import java.util.Locale

sealed class SearchDirective {
    object Old : SearchDirective()
    object New : SearchDirective()
    object MostSnippets : SearchDirective()
    object LeastSnippets : SearchDirective()
    object MostStars : SearchDirective()
    object LeastStars : SearchDirective()
    object Snippets : SearchDirective()
    object Stars : SearchDirective()
    data class StarRating(val rating: Int) : SearchDirective()

    val sortType: PhotoSortType
        get() = when (this) {
            is Old -> PhotoSortType.DateOldest
            is New -> PhotoSortType.DateNewest
            is MostSnippets, is Snippets -> PhotoSortType.MostSnippets
            is LeastSnippets -> PhotoSortType.LeastSnippets
            is MostStars, is Stars, is StarRating -> PhotoSortType.MostStarred
            is LeastStars -> PhotoSortType.LeastStarred
        }
}

data class ParsedSearchQuery(
    val textTokens: List<String>,
    val directives: List<SearchDirective>
)

object SearchQueryParser {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val STAR_COUNT_REGEX = Regex("^([1-5])\\s*stars?$")

    fun matchSearchDirective(rawToken: String): SearchDirective? {
        val clean = rawToken.trim().lowercase(Locale.getDefault())
            .replace(WHITESPACE_REGEX, " ")
            .removePrefix("search ")
            .removePrefix("searching ")
            .removePrefix("sort ")
            .removePrefix("sort by ")
            .removePrefix("by ")

        when (clean) {
            "old", "oldest", "old / oldest", "old or oldest", "old first", "oldest first",
            "date old", "date oldest", "photos old", "photos oldest", "photos by old",
            "photos by oldest", "old photos", "oldest photos" -> return SearchDirective.Old

            "new", "newest", "new / newest", "new or newest", "new first", "newest first",
            "date new", "date newest", "photos new", "photos newest", "photos by new",
            "photos by newest", "new photos", "newest photos" -> return SearchDirective.New

            "most snippets", "snippets most", "most snippet", "snippet most",
            "snippets by most", "snippets by highest", "highest snippets",
            "snippets by most or least", "most or least snippets" -> return SearchDirective.MostSnippets

            "least snippets", "snippets least", "least snippet", "snippet least",
            "snippets by least", "snippets by lowest", "lowest snippets" -> return SearchDirective.LeastSnippets

            "most stars", "stars most", "most star", "star most", "most starred", "starred most",
            "stars by most", "starred by most", "highest stars", "most rating", "highest rating",
            "stars by most or least", "most or least stars", "most or least starred" -> return SearchDirective.MostStars

            "least stars", "stars least", "least star", "star least", "least starred", "starred least",
            "stars by least", "starred by least", "lowest stars", "least rating", "lowest rating" -> return SearchDirective.LeastStars

            "snippets", "snippet" -> return SearchDirective.Snippets
            "stars", "star", "starred" -> return SearchDirective.Stars
            "most", "most or least" -> return SearchDirective.MostSnippets
            "least" -> return SearchDirective.LeastSnippets
        }

        val starCountMatch = STAR_COUNT_REGEX.matchEntire(clean)
        if (starCountMatch != null) {
            val count = starCountMatch.groupValues[1].toIntOrNull()
            if (count != null) return SearchDirective.StarRating(count)
        }

        return null
    }

    private val DIRECTIVE_PHRASES = listOf(
        "snippets by most or least", "most or least snippets",
        "stars by most or least", "most or least stars", "most or least starred",
        "photos by oldest", "photos by old", "oldest photos", "old photos",
        "photos by newest", "photos by new", "newest photos", "new photos",
        "most snippets", "least snippets", "snippets most", "snippets least",
        "snippets by most", "snippets by least", "most snippet", "least snippet",
        "most stars", "least stars", "stars most", "stars least",
        "most starred", "least starred", "stars by most", "stars by least",
        "starred by most", "starred by least",
        "oldest first", "old first", "newest first", "new first",
        "old / oldest", "old or oldest", "most or least",
        "oldest", "old", "newest", "new",
        "snippets", "snippet", "stars", "star", "starred",
        "most", "least"
    )

    fun parseSearchQuery(query: String): ParsedSearchQuery {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return ParsedSearchQuery(emptyList(), emptyList())

        val textTokens = mutableListOf<String>()
        val directives = mutableListOf<SearchDirective>()

        if (trimmed.contains(",")) {
            val parts = trimmed.split(",").map { it.trim() }.filter { it.isNotBlank() }
            for (part in parts) {
                val directive = matchSearchDirective(part)
                if (directive != null) {
                    directives.add(directive)
                } else {
                    textTokens.add(part)
                }
            }
        } else {
            val singleDirective = matchSearchDirective(trimmed)
            if (singleDirective != null) {
                directives.add(singleDirective)
            } else {
                val lower = trimmed.lowercase(Locale.getDefault())
                var matched = false
                for (phrase in DIRECTIVE_PHRASES) {
                    if (lower.endsWith(" $phrase")) {
                        val prefix = trimmed.substring(0, trimmed.length - phrase.length).trim()
                        if (prefix.isNotBlank()) {
                            val directive = matchSearchDirective(phrase)
                            if (directive != null) {
                                textTokens.add(prefix)
                                directives.add(directive)
                                matched = true
                                break
                            }
                        }
                    }
                }
                if (!matched) {
                    textTokens.add(trimmed)
                }
            }
        }

        return ParsedSearchQuery(textTokens, directives)
    }
}
