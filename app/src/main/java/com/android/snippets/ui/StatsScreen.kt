package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.MainTopBar
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.SnippetStyle
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R
import java.util.Calendar
import java.util.Locale

/**
 * Snippet stats and fun insights screen composable redesigned with cookie9sided.
 */
@Composable
fun StatsScreen(viewModel: SnippetsViewModel) {
    AppPredictiveBackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scrollState = rememberScrollState()

    val photos = viewModel.photos

    // Summary Insights Calculations
    val totalPhotos = photos.size
    val allSnippets = remember(photos) { photos.flatMap { it.snippets } }
    val totalSnippetsCount = allSnippets.size

    // 1. Most used snippet
    val mostUsedSnippetEntry = remember(allSnippets) {
        allSnippets.groupingBy { it }.eachCount().maxByOrNull { it.value }
    }

    val context = LocalContext.current
    // 2. Most photographed location
    val mostPhotographedLocationEntry = remember(photos, context) {
        photos.mapNotNull { photo ->
            val loc = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)?.trim()
            if (!loc.isNullOrEmpty()) loc else null
        }.groupingBy { it }.eachCount().maxByOrNull { it.value }
    }

    // 3. Longest streak
    val longestStreakDays = remember(photos) {
        if (photos.isEmpty()) 0
        else {
            val daysSet = photos.map { photo ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = photo.date
                val year = cal.get(Calendar.YEAR)
                val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                year * 366 + dayOfYear
            }.distinct().sorted()

            var maxStreak = 0
            var currentStreak = 0
            var prevDay: Int? = null

            for (day in daysSet) {
                if (prevDay == null) {
                    currentStreak = 1
                } else if (day == prevDay + 1) {
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

    // 4. Photos this month
    val photosThisMonthCount = remember(photos) {
        val nowCal = Calendar.getInstance()
        val currentMonth = nowCal.get(Calendar.MONTH)
        val currentYear = nowCal.get(Calendar.YEAR)

        photos.count { photo ->
            val photoCal = Calendar.getInstance()
            photoCal.timeInMillis = photo.date
            photoCal.get(Calendar.MONTH) == currentMonth && photoCal.get(Calendar.YEAR) == currentYear
        }
    }

    // 5. Favorite snippet
    val favoriteSnippetEntry = remember(photos) {
        photos.filter { it.isFavorite }
            .flatMap { it.snippets }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
    }

    // 6. Average snippets per photo
    val averageSnippets = remember(totalPhotos, totalSnippetsCount) {
        if (totalPhotos == 0) 0f else totalSnippetsCount.toFloat() / totalPhotos
    }

    // 6b. Most snippets on a photo
    val maxSnippetsOnPhoto = remember(photos) {
        if (photos.isEmpty()) 0 else photos.maxOf { it.snippets.size }
    }

    // 6c. Least snippets on a photo
    val minSnippetsOnPhoto = remember(photos) {
        if (photos.isEmpty()) 0 else {
            val nonZero = photos.map { it.snippets.size }.filter { it > 0 }
            if (nonZero.isNotEmpty()) nonZero.minOrNull() ?: 0 else 0
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                title = "Stats",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
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
            // --- OVERVIEW SECTION (3 small cookies in a single row) ---
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
                            text = totalSnippetsCount.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
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
                            text = "$longestStreakDays d",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }
                )
            }

            // --- INSIGHTS SECTION (2 cookies per row) ---
            SectionHeader(title = "Insights")

            // Row 1: Top Snippet & Favorite Snippet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = mostUsedSnippetEntry?.key ?: "None",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            if (mostUsedSnippetEntry != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mostUsedSnippetEntry.value.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top Snippet",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = favoriteSnippetEntry?.key ?: "None",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            if (favoriteSnippetEntry != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = favoriteSnippetEntry.value.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Favorite",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )
            }



            // Row 3: Top Location & Photos This Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = mostPhotographedLocationEntry?.key ?: "None",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            if (mostPhotographedLocationEntry != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mostPhotographedLocationEntry.value.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top Location",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Text(
                            text = photosThisMonthCount.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "This Month",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )
            }

            // Row 4: Average Snippets & Longest Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Text(
                            text = String.format(Locale.US, "%.1f", averageSnippets),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TextSnippet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Avg / Photo",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Text(
                            text = "$longestStreakDays d",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Max Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )
            }

            // Row 5: Most Snippets on a Photo & Least Snippets on a Photo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Text(
                            text = maxSnippetsOnPhoto.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Max Snippets",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )

                CookieStatItem(
                    modifier = Modifier.weight(1f),
                    cookieContent = {
                        Text(
                            text = minSnippetsOnPhoto.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    },
                    pillContent = {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Min Snippets",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )
            }

            // --- RATINGS SECTION (2 cookies per row) ---
            SectionHeader(title = "Ratings")

            val ratingCounts = remember(photos) {
                val counts = IntArray(6)
                photos.forEach { photo ->
                    val r = photo.rating.coerceIn(0, 5)
                    counts[r]++
                }
                counts
            }

            val ratingPairs = listOf(
                5 to 4,
                3 to 2,
                1 to 0
            )

            ratingPairs.forEach { (r1, r2) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val count1 = ratingCounts[r1]
                    CookieStatItem(
                        modifier = Modifier.weight(1f),
                        cookieContent = {
                            Text(
                                text = count1.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (r1 > 0 && count1 > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        },
                        pillContent = {
                            StarRatingBadge(rating = r1)
                        }
                    )

                    val count2 = ratingCounts[r2]
                    CookieStatItem(
                        modifier = Modifier.weight(1f),
                        cookieContent = {
                            Text(
                                text = count2.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (r2 > 0 && count2 > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        },
                        pillContent = {
                            StarRatingBadge(rating = r2)
                        }
                    )
                }
            }

            // --- LOCATION SECTION (2 cookies per row) ---
            SectionHeader(title = "Location")

            val topLocations = remember(photos, context) {
                photos.mapNotNull { photo ->
                    val loc = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)?.trim()
                    if (!loc.isNullOrEmpty()) loc else null
                }.groupingBy { it }.eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(6)
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
                topLocations.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val (name1, count1) = row[0]
                        CookieStatItem(
                            modifier = Modifier.weight(1f),
                            cookieContent = {
                                Text(
                                    text = count1.toString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            },
                            pillContent = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = name1,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        if (row.size > 1) {
                            val (name2, count2) = row[1]
                            CookieStatItem(
                                modifier = Modifier.weight(1f),
                                cookieContent = {
                                    Text(
                                        text = count2.toString(),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                pillContent = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = name2,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        } else {
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

@Composable
private fun CookieStatItem(
    modifier: Modifier = Modifier,
    cookieSize: Dp = 104.dp,
    cookieColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    pillColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    cookieContent: @Composable BoxScope.() -> Unit,
    pillContent: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(cookieSize),
            shape = CookieShape,
            color = cookieColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                cookieContent()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = CircleShape,
            color = pillColor,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                pillContent()
            }
        }
    }
}
