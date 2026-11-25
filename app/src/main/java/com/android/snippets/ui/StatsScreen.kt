package com.android.snippets.ui

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.logic.SnippetManager
import com.android.snippets.ui.components.AppBar
import com.android.snippets.ui.components.ExpressiveSegmentedListItem
import com.android.snippets.ui.components.LocalHazeState
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.viewmodel.StatsViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import java.util.Calendar

/**
 * Snippet stats and fun insights screen composable redesigned with segmented lists and cookie leading icons.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val view = LocalView.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }
    val hazeState = LocalHazeState.current ?: rememberHazeState()

    val photos = viewModel.photos

    // Summary Insights Calculations
    val totalPhotos = photos.size
    val uniqueSnippetsCount = remember(photos) {
        photos.flatMap { it.snippets }.distinct().size
    }

    val context = LocalContext.current
    val onOpenSearch: (String) -> Unit = { query ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val intent = Intent(context, SearchActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("search_query", query)
        }
        context.startActivity(intent)
    }

    // 1. Current active streak (resets to 0 if streak is broken)
    val currentStreakDays = remember(photos) {
        if (photos.isEmpty()) 0
        else {
            val photoDaysSet = photos.map { photo ->
                val cal = Calendar.getInstance().apply { timeInMillis = photo.date }
                Day(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
            }.toSet()

            val nowCal = Calendar.getInstance()
            val today = Day(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.DAY_OF_YEAR))
            val yesterday = today.previousDay()

            val startDay = when {
                today in photoDaysSet -> today
                yesterday in photoDaysSet -> yesterday
                else -> null
            }

            if (startDay == null) {
                0
            } else {
                var streak = 0
                var curr: Day? = startDay
                while (curr != null && curr in photoDaysSet) {
                    streak += 1
                    curr = curr.previousDay()
                }
                streak
            }
        }
    }

    // 2. Longest streak (properly handles 365-day and 366-day leap year boundaries)
    val longestStreakDays = remember(photos) {
        if (photos.isEmpty()) 0
        else {
            val daysList = photos.map { photo ->
                val cal = Calendar.getInstance().apply { timeInMillis = photo.date }
                Day(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
            }.distinct().sorted()

            var maxStreak = 0
            var currentStreak = 0
            var prevDay: Day? = null

            for (day in daysList) {
                if (prevDay == null) {
                    currentStreak = 1
                } else if (day.isImmediatelyAfter(prevDay)) {
                    currentStreak += 1
                } else {
                    currentStreak = 1
                }
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
                prevDay = day
            }
            maxStreak
        }
    }

    // 3. Photos this month
    val photosThisMonthCount = remember(photos) {
        val nowCal = Calendar.getInstance()
        val currentMonth = nowCal.get(Calendar.MONTH)
        val currentYear = nowCal.get(Calendar.YEAR)

        photos.count { photo ->
            val photoCal = Calendar.getInstance().apply { timeInMillis = photo.date }
            photoCal.get(Calendar.MONTH) == currentMonth && photoCal.get(Calendar.YEAR) == currentYear
        }
    }

    // 4. New snippets this month
    val newSnippetsThisMonthCount = remember(photos, viewModel.snippetFirstSeenTimes) {
        val nowCal = Calendar.getInstance()
        val currentMonth = nowCal.get(Calendar.MONTH)
        val currentYear = nowCal.get(Calendar.YEAR)
        val firstSeenMap = viewModel.snippetFirstSeenTimes.ifEmpty {
            SnippetManager.rebuildSnippetFirstSeenTimes(photos)
        }

        firstSeenMap.count { (_, time) ->
            if (time <= 0L) false
            else {
                val cal = Calendar.getInstance().apply { timeInMillis = time }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }
        }
    }

    val insightItems = remember(
        totalPhotos,
        uniqueSnippetsCount,
        currentStreakDays,
        photosThisMonthCount,
        newSnippetsThisMonthCount,
        longestStreakDays
    ) {
        listOf(
            "Photos" to totalPhotos.toString(),
            "Snippets" to uniqueSnippetsCount.toString(),
            "Streak" to "$currentStreakDays d",
            "New Photos" to photosThisMonthCount.toString(),
            "New Snippets" to newSnippetsThisMonthCount.toString(),
            "Max Streak" to "$longestStreakDays d"
        )
    }

    val ratingCounts = remember(photos) {
        val counts = IntArray(6)
        photos.forEach { photo ->
            val r = photo.rating.coerceIn(0, 5)
            counts[r]++
        }
        counts
    }

    val ratingList = remember { listOf(5, 4, 3, 2, 1, 0) }

    val topLocations = remember(photos, context) {
        photos.mapNotNull { photo ->
            val loc = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)?.trim()
            if (!loc.isNullOrEmpty()) loc else null
        }.groupingBy { it }.eachCount()
            .toList()
            .filter { it.second >= 3 }
            .sortedByDescending { it.second }
    }

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                topBar = {
                    AppBar(
                        title = "Stats",
                        onNavigationClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            backDispatcher?.onBackPressed() ?: (view.context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() ?: (view.context as? ComponentActivity)?.finish()
                        },
                        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                        isSpinning = true,
                        isScrolled = isScrolled,
                        leftAlignTitle = true,
                        showDividerOnScroll = true,
                        scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // --- INSIGHTS SECTION ---
                    SectionHeader(
                        title = "Insights",
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    insightItems.forEachIndexed { index, (label, value) ->
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = insightItems.size,
                            label = label,
                            icon = value,
                            consumeIconClick = true,
                            onClick = {}
                        )
                    }

                    // --- RATINGS SECTION ---
                    SectionHeader(
                        title = "Ratings",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )

                    ratingList.forEachIndexed { index, r ->
                        val count = ratingCounts[r]
                        val label = if (r == 1) "1 star" else "$r stars"
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = ratingList.size,
                            label = label,
                            icon = count.toString(),
                            consumeIconClick = true,
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search $label",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { onOpenSearch(label) }
                        )
                    }

                    // --- LOCATION SECTION ---
                    SectionHeader(
                        title = "Location",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )

                    if (topLocations.isEmpty()) {
                        EmptyLocationState()
                    } else {
                        topLocations.forEachIndexed { index, (name, count) ->
                            ExpressiveSegmentedListItem(
                                index = index,
                                count = topLocations.size,
                                label = name,
                                icon = count.toString(),
                                consumeIconClick = true,
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search $name",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { onOpenSearch(name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = GoogleSansFlexRound,
            fontWeight = FontWeight.SemiBold
        ),
        color = color,
        modifier = modifier.padding(start = 8.dp)
    )
}

private data class Day(val year: Int, val dayOfYear: Int) : Comparable<Day> {
    override fun compareTo(other: Day): Int {
        val y = year.compareTo(other.year)
        return if (y != 0) y else dayOfYear.compareTo(other.dayOfYear)
    }

    fun isImmediatelyAfter(prev: Day): Boolean {
        if (year == prev.year) {
            return dayOfYear == prev.dayOfYear + 1
        }
        if (year == prev.year + 1 && dayOfYear == 1) {
            val prevCal = Calendar.getInstance().apply { set(Calendar.YEAR, prev.year) }
            val daysInPrevYear = prevCal.getActualMaximum(Calendar.DAY_OF_YEAR)
            return prev.dayOfYear == daysInPrevYear
        }
        return false
    }

    fun previousDay(): Day {
        return if (dayOfYear > 1) {
            Day(year, dayOfYear - 1)
        } else {
            val prevYear = year - 1
            val cal = Calendar.getInstance().apply { set(Calendar.YEAR, prevYear) }
            val daysInPrevYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
            Day(prevYear, daysInPrevYear)
        }
    }
}
