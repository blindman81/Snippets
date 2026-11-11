package com.android.snippets.ui

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.logic.SnippetManager
import com.android.snippets.ui.components.MainAppBar
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.StatsViewModel
import com.ln.android.snippets.R
import java.util.Calendar

/**
 * Snippet stats and fun insights screen composable redesigned with cookie9sided.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val view = LocalView.current
    val backDispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scrollState = rememberScrollState()

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            MainAppBar(
                title = "Stats",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    backDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.finish()
                },
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isSpinning = true,
                isScrolled = scrollState.value > 0,
                leftAlignTitle = true,
                showDividerOnScroll = false
            )
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- INSIGHTS SECTION (3 cookies per row) ---
            SectionHeader(title = "Insights")

            // Row 1: Photos, Snippets, Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = totalPhotos.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Rounded.Photo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = uniqueSnippetsCount.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TextSnippet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Snippets",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = "$currentStreakDays d",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Rounded.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }
                )
            }

            // Row 2: New Photos, New Snippets, Max Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = photosThisMonthCount.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "New Photos",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = newSnippetsThisMonthCount.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "New Snippets",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieSize = 82.dp,
                    cookieContent = {
                        Text(
                            text = "$longestStreakDays d",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Rounded.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Max Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            // --- RATINGS SECTION (3 cookies per row) ---
            SectionHeader(title = "Ratings")

            val ratingCounts = remember(photos) {
                val counts = IntArray(6)
                photos.forEach { photo ->
                    val r = photo.rating.coerceIn(0, 5)
                    counts[r]++
                }
                counts
            }

            val ratingTriplets = listOf(
                listOf(5, 4, 3),
                listOf(2, 1, 0)
            )

            ratingTriplets.forEach { triplet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    triplet.forEach { r ->
                        val count = ratingCounts[r]
                        val ratingQuery = if (r == 1) "1 star" else "$r stars"
                        CookieStatItem(
                            modifier = Modifier.weight(1f),
                            cookieSize = 82.dp,
                            onClick = { onOpenSearch(ratingQuery) },
                            cookieContent = {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (r > 0 && count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            },
                            pillContent = {
                                StarRatingBadge(rating = r)
                            }
                        )
                    }
                }
            }

            // --- LOCATION SECTION (3 cookies per row) ---
            SectionHeader(title = "Location")

            val topLocations = remember(photos, context) {
                photos.mapNotNull { photo ->
                    val loc = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)?.trim()
                    if (!loc.isNullOrEmpty()) loc else null
                }.groupingBy { it }.eachCount()
                    .toList()
                    .filter { it.second >= 3 }
                    .sortedByDescending { it.second }
            }

            if (topLocations.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No location data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                topLocations.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (name, count) ->
                            CookieStatItem(
                                modifier = Modifier.weight(1f),
                                cookieSize = 82.dp,
                                onClick = { onOpenSearch(name) },
                                cookieContent = {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                pillContent = {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 14.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun StarRatingBadge(
    rating: Int,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        val isRated = rating > 0
        Icon(
            painter = painterResource(id = R.drawable.ic_star_rating),
            contentDescription = null,
            tint = if (isRated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = if (isRated) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(top = 0.5.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookieStatItem(
    modifier: Modifier = Modifier,
    cookieSize: Dp = 82.dp,
    cookieColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    pillColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    rippleColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    cookieContent: @Composable BoxScope.() -> Unit,
    pillContent: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = rippleColor)
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(cookieSize),
                shape = CookieShape,
                color = cookieColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onClick ?: {},
                enabled = onClick != null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    cookieContent()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = CircleShape,
                color = pillColor,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onClick ?: {},
                enabled = onClick != null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    pillContent()
                }
            }
        }
    }
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
            val daysInPrevYear = prevCal.getActualMaximum(Calendar.DAY_OF_YEAR) // 366 for leap year, 365 otherwise
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
            val daysInPrevYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR) // 366 or 365
            Day(prevYear, daysInPrevYear)
        }
    }
}
