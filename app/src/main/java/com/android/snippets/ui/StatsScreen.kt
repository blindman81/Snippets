package com.android.snippets.ui
import com.ln.android.snippets.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip

/**
 * Snippet stats and fun insights screen composable.
 */

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.TextSnippet
import com.android.snippets.ui.components.MainTopBar
import com.android.snippets.ui.components.LargeMainTopBar
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: SnippetsViewModel) {
    AppPredictiveBackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val photos = viewModel.photos

    // Summary Insights Calculations
    val totalPhotos = photos.size
    val allSnippets = remember(photos) { photos.flatMap { it.snippets } }
    val totalSnippetsCount = allSnippets.size

    // 1. Most used snippet
    val mostUsedSnippetEntry = remember(allSnippets) {
        allSnippets.groupingBy { it }.eachCount().maxByOrNull { it.value }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
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

    // 7. Most used style
    val mostUsedStyleEntry = remember(allSnippets) {
        if (allSnippets.isEmpty()) null
        else {
            allSnippets.map { viewModel.getSnippetStyle(it) }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
        }
    }

    // 8. Most used color
    val mostUsedColorEntry = remember(allSnippets) {
        if (allSnippets.isEmpty()) null
        else {
            allSnippets.map { viewModel.getSnippetColor(it) ?: Int.MIN_VALUE }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
        }
    }

    val badgeColor = remember(mostUsedColorEntry) {
        val colorInt = mostUsedColorEntry?.key
        if (colorInt != null && colorInt != Int.MIN_VALUE) {
            Color(colorInt)
        } else {
            null
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeMainTopBar(
                title = "Stats",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = true,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    bottom = innerPadding.calculateBottomPadding() + 12.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 12.dp))
            // Header summary cards
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatSummaryItem(
                        value = totalPhotos.toString(),
                        label = "Total Photos"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(LocalContentColor.current.copy(alpha = 0.2f))
                    )
                    StatSummaryItem(
                        value = totalSnippetsCount.toString(),
                        label = "Snippets Added"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(LocalContentColor.current.copy(alpha = 0.2f))
                    )
                    StatSummaryItem(
                        value = "$longestStreakDays d",
                        label = "Day Streak"
                    )
                }
            }

            Text(
                text = "INSIGHTS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Most used snippet card
                InsightCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "Most used snippet",
                    value = mostUsedSnippetEntry?.key ?: "None yet",
                    subtitle = if (mostUsedSnippetEntry != null) "${mostUsedSnippetEntry.value} ${if (mostUsedSnippetEntry.value == 1) "photo" else "photos"}" else "0 photos",
                    position = CardPosition.First
                )

                // 1b. Most used style card
                InsightCard(
                    icon = Icons.Default.Style,
                    title = "Most used style",
                    value = mostUsedStyleEntry?.let { formatStyleName(it.key) } ?: "None yet",
                    subtitle = if (mostUsedStyleEntry != null) "${mostUsedStyleEntry.value} ${if (mostUsedStyleEntry.value == 1) "use" else "uses"}" else "0 uses",
                    position = CardPosition.Middle
                )

                // 1c. Most used color card
                InsightCard(
                    icon = Icons.Default.Palette,
                    title = "Most used color",
                    value = mostUsedColorEntry?.let { formatColorName(it.key) } ?: "None yet",
                    subtitle = if (mostUsedColorEntry != null) "${mostUsedColorEntry.value} ${if (mostUsedColorEntry.value == 1) "use" else "uses"}" else "0 uses",
                    position = CardPosition.Middle,
                    badgeColor = badgeColor
                )

                // 2. Most photographed location card
                InsightCard(
                    icon = Icons.Default.LocationOn,
                    title = "Most photographed location",
                    value = mostPhotographedLocationEntry?.key ?: "No location data",
                    subtitle = if (mostPhotographedLocationEntry != null) "${mostPhotographedLocationEntry.value} ${if (mostPhotographedLocationEntry.value == 1) "photo" else "photos"}" else "0 photos",
                    position = CardPosition.Middle
                )

                // 3. Longest streak card
                InsightCard(
                    icon = Icons.Default.Whatshot,
                    title = "Longest streak",
                    value = "$longestStreakDays ${if (longestStreakDays == 1) "day" else "days"}",
                    subtitle = "Consecutive days taking photos",
                    position = CardPosition.Middle
                )

                // 4. Photos this month card
                InsightCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Photos this month",
                    value = "$photosThisMonthCount ${if (photosThisMonthCount == 1) "photo" else "photos"}",
                    subtitle = "Added in the current calendar month",
                    position = CardPosition.Middle
                )

                // 6. Average snippets card
                InsightCard(
                    icon = Icons.Default.TextSnippet,
                    title = "Average snippets",
                    value = String.format(Locale.US, "%.1f", averageSnippets),
                    subtitle = "Average number of snippets per photo",
                    position = CardPosition.Middle
                )

                // 6b. Most snippets on a photo card
                InsightCard(
                    icon = Icons.Default.VerticalAlignTop,
                    title = "Most snippets on a photo",
                    value = "$maxSnippetsOnPhoto ${if (maxSnippetsOnPhoto == 1) "snippet" else "snippets"}",
                    subtitle = "Highest snippet count on a single photo",
                    position = CardPosition.Middle
                )

                // 6c. Least snippets on a photo card
                InsightCard(
                    icon = Icons.Default.VerticalAlignBottom,
                    title = "Least snippets on a photo",
                    value = "$minSnippetsOnPhoto ${if (minSnippetsOnPhoto == 1) "snippet" else "snippets"}",
                    subtitle = "Lowest snippet count on a single photo",
                    position = CardPosition.Middle
                )

                // 5. Favorite snippet card
                InsightCard(
                    icon = Icons.Default.Favorite,
                    title = "Favorite snippet",
                    value = favoriteSnippetEntry?.key ?: "None yet",
                    subtitle = if (favoriteSnippetEntry != null) "${favoriteSnippetEntry.value} favorite photos" else "No favorite snippets tagged",
                    position = CardPosition.Last
                )
            }

            Text(
                text = "RATINGS DISTRIBUTION",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )

            val ratingCounts = remember(photos) {
                val counts = IntArray(6)
                photos.forEach { photo ->
                    val r = photo.rating.coerceIn(0, 5)
                    counts[r]++
                }
                counts
            }
            val maxCount = remember(ratingCounts) {
                ratingCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (r in 5 downTo 0) {
                    val count = ratingCounts[r]
                    val progress = count.toFloat() / maxCount
                    val position = when (r) {
                        5 -> CardPosition.First
                        0 -> CardPosition.Last
                        else -> CardPosition.Middle
                    }

                    DynamicCardContainer(
                        position = position
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(60.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star_rating),
                                    contentDescription = null,
                                    tint = if (r > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = r.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (r > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(30.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Text(
                text = "LOCATION DISTRIBUTION",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )

            val (topLocations, locationMaxCount) = remember(photos, context) {
                val counts = photos.mapNotNull { photo ->
                    val loc = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)?.trim()
                    if (!loc.isNullOrEmpty()) loc else null
                }.groupingBy { it }.eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                val top = counts.take(5)
                val max = top.firstOrNull()?.second?.coerceAtLeast(1) ?: 1
                Pair(top, max)
            }

            if (topLocations.isEmpty()) {
                DynamicCardContainer(
                    position = CardPosition.Single
                ) {
                    Text(
                        text = "No location data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topLocations.forEachIndexed { index, (name, count) ->
                        val progress = count.toFloat() / locationMaxCount
                        val position = when {
                            topLocations.size == 1 -> CardPosition.Single
                            index == 0 -> CardPosition.First
                            index == topLocations.size - 1 -> CardPosition.Last
                            else -> CardPosition.Middle
                        }

                        DynamicCardContainer(
                            position = position
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(30.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val contentColor = LocalContentColor.current
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}

private fun formatStyleName(style: SnippetStyle): String = when (style) {
    SnippetStyle.Default -> "Default"
    SnippetStyle.Thin -> "Thin"
    SnippetStyle.Cursive -> "Cursive"
    SnippetStyle.Mono -> "Mono"
    SnippetStyle.Serif -> "Serif"
    SnippetStyle.Spaced -> "Spaced"
    SnippetStyle.Bold -> "Bold"
    SnippetStyle.FlexHeavy -> "Flex Heavy"
    SnippetStyle.FlexWide -> "Flex Wide"
    SnippetStyle.FlexSlant -> "Flex Slanted"
    SnippetStyle.FlexGrade -> "Flex Grade"
}

private fun formatColorName(colorInt: Int?): String {
    if (colorInt == null || colorInt == Int.MIN_VALUE) return "Default"
    return when (colorInt.toLong() and 0xFFFFFFFFL) {
        0xFFEF5350L -> "Red"
        0xFFEC407AL -> "Pink"
        0xFFAB47BCL -> "Purple"
        0xFF42A5F5L -> "Blue"
        0xFF26A69AL -> "Teal"
        0xFF66BB6AL -> "Green"
        0xFFFFEE58L -> "Yellow"
        0xFFFFA726L -> "Orange"
        0xFF8D6E63L -> "Brown"
        0xFF78909CL -> "Slate"
        0xFFD4E157L -> "Lime"
        else -> String.format("#%06X", 0xFFFFFF and colorInt)
    }
}

@Composable
private fun InsightCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    position: CardPosition,
    badgeColor: Color? = null
) {
    SettingsCardItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        position = position,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        trailingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (badgeColor != null) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = badgeColor,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        ) {}
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    )
}
