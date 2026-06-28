package com.android.snippets.ui

/**
 * Snippet stats and fun insights screen composable.
 */

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
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
import com.android.snippets.ui.components.MainTopBar
import com.android.snippets.viewmodel.SnippetsViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: SnippetsViewModel) {
    BackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {}
    }

    val photos = viewModel.photos

    // Summary Insights Calculations
    val totalPhotos = photos.size
    val allSnippets = remember(photos) { photos.flatMap { it.snippets } }
    val totalSnippetsCount = allSnippets.size

    // 1. Most used snippet
    val mostUsedSnippetEntry = remember(allSnippets) {
        allSnippets.groupingBy { it }.eachCount().maxByOrNull { it.value }
    }

    // 2. Most photographed location
    val mostPhotographedLocationEntry = remember(photos) {
        photos.mapNotNull { photo ->
            val loc = photo.locationName?.trim()
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

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            MainTopBar(
                title = "Stats",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = true,
                isScrolled = isScrolled,
                leftAlignTitle = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary cards
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
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
                    subtitle = if (mostUsedSnippetEntry != null) "${mostUsedSnippetEntry.value} photos" else "0 photos",
                    position = CardPosition.First
                )

                // 2. Most photographed location card
                InsightCard(
                    icon = Icons.Default.LocationOn,
                    title = "Most photographed location",
                    value = mostPhotographedLocationEntry?.key ?: "No location data",
                    subtitle = if (mostPhotographedLocationEntry != null) "${mostPhotographedLocationEntry.value} photos" else "0 photos",
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

                // 5. Favorite snippet card
                InsightCard(
                    icon = Icons.Default.Favorite,
                    title = "Favorite snippet",
                    value = favoriteSnippetEntry?.key ?: "None yet",
                    subtitle = if (favoriteSnippetEntry != null) "${favoriteSnippetEntry.value} favorite photos" else "No favorite snippets tagged",
                    position = CardPosition.Last
                )
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

@Composable
private fun InsightCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    position: CardPosition
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
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    )
}
