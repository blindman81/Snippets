package com.android.snippets.ui.components

import android.view.View
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetStyle
import com.android.snippets.ui.theme.getSnippetTextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSnippetContent(   
    viewModel: SnippetsViewModel,
    onDismiss: () -> Unit,
    showDisplay: Boolean = false,
    showSort: Boolean = true,
    scrollState: androidx.compose.foundation.ScrollState? = null,
    selectedTabIndex: Int = 0
) {
    val view = LocalView.current
    val internalScrollState = rememberScrollState()
    val effectiveScrollState = scrollState ?: internalScrollState
    val allSnippets = remember(viewModel.photos, viewModel.filteringCategory) {
        val cat = viewModel.filteringCategory ?: "Library"
        viewModel.photos.filter { photo ->
            when (cat) {
                "Library" -> true
                "Favorites" -> photo.isFavorite
                else -> photo.collections.contains(cat)
            }
        }.flatMap { it.snippets }.distinct()
    }

    val snippetCreationDates = remember(viewModel.photos, viewModel.snippetFirstSeenTimes) {
        val map = mutableMapOf<String, Long>()
        viewModel.photos.flatMap { it.snippets }.distinct().forEach { snippet ->
            val time = viewModel.getSnippetFirstSeenTime(snippet)
            if (time != null && time > 0L) {
                map[snippet] = time
            } else {
                val earliestPhotoTime = viewModel.photos.filter { it.snippets.contains(snippet) }.minOfOrNull {
                    if (it.snippetsAddedTime > 0L) it.snippetsAddedTime else it.date
                }
                if (earliestPhotoTime != null) {
                    map[snippet] = earliestPhotoTime
                }
            }
        }
        map
    }

    val monthFormat = remember { java.text.SimpleDateFormat("MMM, yyyy", java.util.Locale.getDefault()) }

    val filteredSnippets = remember(allSnippets, viewModel.snippetSortType) {
        when (viewModel.snippetSortType) {
            SnippetSortType.Emoji -> allSnippets.filter { viewModel.containsEmoji(it) }
            SnippetSortType.Emoticons -> allSnippets.filter { viewModel.containsEmoticon(it) }
            else -> allSnippets
        }
    }

    val isOld = viewModel.snippetSortType == SnippetSortType.Old

    val grouped = remember(filteredSnippets, snippetCreationDates, monthFormat) {
        filteredSnippets.groupBy { snippet ->
            val time = snippetCreationDates[snippet] ?: 0L
            monthFormat.format(java.util.Date(time))
        }
    }

    val sortedKeys = remember(grouped, isOld, monthFormat) {
        if (isOld) {
            grouped.keys.sortedBy { monthFormat.parse(it)?.time ?: 0L }
        } else {
            grouped.keys.sortedByDescending { monthFormat.parse(it)?.time ?: 0L }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(effectiveScrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        if (viewModel.snippetSortType == SnippetSortType.AZ) {
            val groupedByChar = remember(allSnippets) {
                allSnippets.groupBy { it.firstOrNull()?.uppercaseChar() ?: '#' }
            }
            val sortedChars = remember(groupedByChar) {
                groupedByChar.keys.sorted()
            }

            sortedChars.forEachIndexed { index, char ->
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = if (index == 0) 4.dp else 16.dp, bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val snippetsInChar = groupedByChar[char]?.sortedWith(String.CASE_INSENSITIVE_ORDER)
                    snippetsInChar?.forEach { snippet ->
                        SnippetFilterChip(viewModel, snippet, view)
                    }
                }
            }
        } else {
            sortedKeys.forEachIndexed { index, key ->
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = if (index == 0) 4.dp else 16.dp, bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val snippetsInGroup = if (isOld) {
                        grouped[key]?.sortedBy { snippetCreationDates[it] ?: 0L }
                    } else {
                        grouped[key]?.sortedByDescending { snippetCreationDates[it] ?: 0L }
                    }
                    snippetsInGroup?.forEach { snippet ->
                        SnippetFilterChip(viewModel, snippet, view)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SnippetFilterChip(viewModel: SnippetsViewModel, snippet: String, view: android.view.View) {
    val isSelected = viewModel.selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
    FilterChip(
        selected = isSelected,
        onClick = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            viewModel.toggleFilterSnippet(snippet)
        },
        label = {
            Text(
                text = snippet,
                style = getSnippetTextStyle(
                    SnippetStyle.Default,
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = if (isSelected) com.android.snippets.ui.theme.GoogleSansFlexWide else com.android.snippets.ui.theme.GoogleSansFlexRound,
                        fontWeight = FontWeight.Bold
                    )
                )
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = null
    )
}
