package com.android.snippets.ui.components

import android.view.View
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import com.android.snippets.ui.EmptyFilterState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.snippets.logic.PhotoSearchHelper
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetStyle
import com.android.snippets.ui.theme.getSnippetTextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSnippetContent(   
    viewModel: SnippetsViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showDisplay: Boolean = false,
    showSort: Boolean = true,
    scrollState: androidx.compose.foundation.ScrollState? = null,
    selectedTabIndex: Int = 0,
    uiState: SnippetsUiState = viewModel.uiState.collectAsStateWithLifecycle().value
) {
    val view = LocalView.current
    val internalScrollState = rememberScrollState()
    val effectiveScrollState = scrollState ?: internalScrollState

    val photosInTab = remember(uiState.photos, uiState.filteringCategory) {
        val cat = uiState.filteringCategory ?: "Library"
        PhotoSearchHelper.filterPhotosByTab(uiState.photos, cat)
    }

    val allSnippets = remember(photosInTab) {
        val set = LinkedHashSet<String>()
        for (photo in photosInTab) {
            set.addAll(photo.snippets)
        }
        set.toList()
    }

    val snippetPhotoCounts = remember(photosInTab, allSnippets) {
        val counts = HashMap<String, Int>(allSnippets.size)
        for (s in allSnippets) {
            counts[s] = 0
        }
        val canonicalMap = allSnippets.associateBy { it.lowercase() }
        for (photo in photosInTab) {
            val seenInPhoto = HashSet<String>()
            for (s in photo.snippets) {
                val canon = canonicalMap[s.lowercase()] ?: s
                if (seenInPhoto.add(canon)) {
                    counts[canon] = (counts[canon] ?: 0) + 1
                }
            }
        }
        counts
    }

    val snippetCreationDates = remember(uiState.photos, uiState.snippetFirstSeenTimes, allSnippets) {
        val map = HashMap<String, Long>(allSnippets.size)
        for (s in allSnippets) {
            val time = uiState.snippetFirstSeenTimes[s]
            if (time != null && time > 0L) {
                map[s] = time
            }
        }
        val missing = allSnippets.filter { !map.containsKey(it) }
        if (missing.isNotEmpty()) {
            val missingSet = missing.toSet()
            for (photo in uiState.photos) {
                val photoTime = if (photo.snippetsAddedTime > 0L) photo.snippetsAddedTime else photo.date
                for (s in photo.snippets) {
                    if (missingSet.contains(s)) {
                        val current = map[s]
                        if (current == null || photoTime < current) {
                            map[s] = photoTime
                        }
                    }
                }
            }
        }
        map
    }

    val monthFormat = remember { java.text.SimpleDateFormat("MMM, yyyy", java.util.Locale.getDefault()) }

    val azSnippets = remember(allSnippets) {
        allSnippets.filter { snippet ->
            val firstLetter = snippet.firstOrNull { it.isLetter() }?.uppercaseChar()
            firstLetter != null || (!viewModel.containsEmoji(snippet) && snippet.any { it.isDigit() })
        }
    }

    val filteredSnippets = remember(allSnippets, uiState.snippetSortType, azSnippets) {
        when (uiState.snippetSortType) {
            SnippetSortType.AZ -> azSnippets
            SnippetSortType.Emoji -> allSnippets.filter { viewModel.containsEmoji(it) }
            SnippetSortType.Emoticons -> allSnippets.filter { viewModel.containsEmoticon(it) }
            else -> allSnippets
        }
    }

    val isOld = uiState.snippetSortType == SnippetSortType.Old

    val grouped = remember(filteredSnippets, snippetCreationDates, monthFormat) {
        filteredSnippets.groupBy { snippet ->
            val time = snippetCreationDates[snippet] ?: 0L
            monthFormat.format(java.util.Date(time))
        }
    }

    val groupTimestamps = remember(grouped, snippetCreationDates) {
        grouped.mapValues { (_, snips) ->
            snips.minOfOrNull { snippetCreationDates[it] ?: 0L } ?: 0L
        }
    }

    val sortedKeys = remember(grouped, isOld, groupTimestamps) {
        if (isOld) {
            grouped.keys.sortedBy { groupTimestamps[it] ?: 0L }
        } else {
            grouped.keys.sortedByDescending { groupTimestamps[it] ?: 0L }
        }
    }

    val sortedGroupedSnippets = remember(grouped, isOld, snippetCreationDates) {
        grouped.mapValues { (_, list) ->
            if (isOld) {
                list.sortedBy { snippetCreationDates[it] ?: 0L }
            } else {
                list.sortedByDescending { snippetCreationDates[it] ?: 0L }
            }
        }
    }

    val isContentLoading = uiState.isInitialLoading && uiState.photos.isEmpty()
    val isListEmpty = filteredSnippets.isEmpty()

    if (isContentLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    } else if (isListEmpty) {
        EmptyFilterState(modifier = modifier)
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(effectiveScrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            if (uiState.snippetSortType == SnippetSortType.AZ) {
                val snippetComparator = remember {
                    Comparator<String> { a, b ->
                        val cleanA = a.dropWhile { !it.isLetter() }
                        val cleanB = b.dropWhile { !it.isLetter() }
                        val res = cleanA.compareTo(cleanB, ignoreCase = true)
                        if (res != 0) res else a.compareTo(b, ignoreCase = true)
                    }
                }
                val groupedByChar = remember(azSnippets, snippetComparator) {
                    azSnippets.groupBy { snippet ->
                        snippet.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
                    }.mapValues { (_, list) -> list.sortedWith(snippetComparator) }
                }
                val sortedChars = remember(groupedByChar) {
                    groupedByChar.keys.sortedWith { a, b ->
                        if (a == '#') 1 else if (b == '#') -1 else a.compareTo(b)
                    }
                }

                sortedChars.forEachIndexed { index, char ->
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = if (index == 0) 4.dp else 16.dp, bottom = 8.dp)
                    )
                    FlexBox(
                        modifier = Modifier.fillMaxWidth(),
                        config = {
                            wrap(FlexWrap.Wrap)
                            gap(12.dp)
                            maxItemsInEachLine(4)
                        }
                    ) {
                        val snippetsInChar = groupedByChar[char]
                        snippetsInChar?.forEach { snippet ->
                            val isSelected = uiState.selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
                            val count = snippetPhotoCounts[snippet] ?: 0
                            SnippetFilterChip(viewModel, snippet, isSelected, count, view)
                        }
                    }
                }
            } else {
                sortedKeys.forEachIndexed { index, key ->
                    Text(
                        text = key,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = if (index == 0) 4.dp else 16.dp, bottom = 8.dp)
                    )
                    FlexBox(
                        modifier = Modifier.fillMaxWidth(),
                        config = {
                            wrap(FlexWrap.Wrap)
                            gap(12.dp)
                            maxItemsInEachLine(4)
                        }
                    ) {
                        val snippetsInGroup = sortedGroupedSnippets[key]
                        snippetsInGroup?.forEach { snippet ->
                            val isSelected = uiState.selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
                            val count = snippetPhotoCounts[snippet] ?: 0
                            SnippetFilterChip(viewModel, snippet, isSelected, count, view)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SnippetFilterChip(
    viewModel: SnippetsViewModel,
    snippet: String,
    isSelected: Boolean,
    count: Int,
    view: android.view.View
) {
    FilterChip(
        selected = isSelected,
        onClick = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            viewModel.toggleFilterSnippet(snippet)
        },
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = snippet,
                    style = getSnippetTextStyle(
                        SnippetStyle.Default,
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                )
                if (isSelected && count > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        leadingIcon = null,
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
