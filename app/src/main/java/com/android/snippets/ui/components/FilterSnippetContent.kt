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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextSnippet

import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetStyle
import com.android.snippets.ui.SettingsCardItem
import com.android.snippets.ui.CardPosition
import com.android.snippets.ui.CollectionIcon
import com.android.snippets.ui.getSnippetTextStyle

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
            if (time != null) {
                map[snippet] = time
            }
        }
        map
    }

    val monthFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()) }
    val yearFormat = remember { java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(effectiveScrollState)
            .padding(16.dp)
    ) {
        when (selectedTabIndex) {
            0 -> {
                // Tab 1: Snippets
                if (viewModel.snippetSortType == SnippetSortType.AZ) {
                    val grouped = allSnippets.groupBy { it.first().uppercaseChar() }
                    val sortedKeys = if (viewModel.isSortAscending) {
                        grouped.keys.sortedBy { it }
                    } else {
                        grouped.keys.sortedByDescending { it }
                    }

                    sortedKeys.forEach { char ->
                        Text(
                            text = char.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped[char]?.forEach { snippet ->
                                SnippetFilterChip(viewModel, snippet, view)
                            }
                        }
                    }
                } else if (viewModel.snippetSortType == SnippetSortType.Month) {
                    val grouped = allSnippets.groupBy { snippet ->
                        val time = snippetCreationDates[snippet] ?: 0L
                        monthFormat.format(java.util.Date(time))
                    }
                    val sortedKeys = if (viewModel.isSortAscending) {
                        grouped.keys.sortedBy { monthFormat.parse(it)?.time ?: 0L }
                    } else {
                        grouped.keys.sortedByDescending { monthFormat.parse(it)?.time ?: 0L }
                    }

                    sortedKeys.forEach { key ->
                        Text(
                            text = key.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped[key]?.forEach { snippet ->
                                SnippetFilterChip(viewModel, snippet, view)
                            }
                        }
                    }
                } else if (viewModel.snippetSortType == SnippetSortType.Color) {
                    val grouped = allSnippets.groupBy { snippet -> viewModel.getSnippetColor(snippet) ?: Int.MIN_VALUE }
                    val sortedKeys = if (viewModel.isSortAscending) {
                        grouped.keys.sorted()
                    } else {
                        grouped.keys.sortedDescending()
                    }

                    sortedKeys.forEach { colorInt ->
                        val swatchColor = if (colorInt == Int.MIN_VALUE) MaterialTheme.colorScheme.outlineVariant else Color(colorInt)
                        val label = if (colorInt == Int.MIN_VALUE) {
                            "UNCOLORED"
                        } else {
                            String.format("#%06X", 0xFFFFFF and colorInt)
                        }

                        Row(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(16.dp),
                                shape = CircleShape,
                                color = swatchColor
                            ) {}
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped[colorInt]?.forEach { snippet ->
                                SnippetFilterChip(viewModel, snippet, view)
                            }
                        }
                    }
                } else if (viewModel.snippetSortType == SnippetSortType.Style) {
                    val grouped = allSnippets.groupBy { snippet -> viewModel.getSnippetStyle(snippet) }
                    val sortedKeys = if (viewModel.isSortAscending) {
                        grouped.keys.sortedBy { it.name }
                    } else {
                        grouped.keys.sortedByDescending { it.name }
                    }

                    sortedKeys.forEach { style ->
                        Row(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "S",
                                style = getSnippetTextStyle(style, MaterialTheme.typography.titleSmall),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = styleHeaderLabel(style),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped[style]?.forEach { snippet ->
                                SnippetFilterChip(viewModel, snippet, view)
                            }
                        }
                    }
                } else {
                    // Default / Other sorts
                    val displaySnippets = viewModel.allUniqueSnippets.filter { it in allSnippets }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        displaySnippets.forEach { snippet ->
                            SnippetFilterChip(viewModel, snippet, view)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }

            1 -> {
                // Tab 2: Sort
                val snippetTypes = listOf(
                    SnippetSortType.New, SnippetSortType.Old, SnippetSortType.AZ,
                    SnippetSortType.Month, SnippetSortType.Year, SnippetSortType.Favorites,
                    SnippetSortType.Emoji, SnippetSortType.Emoticons, SnippetSortType.Color,
                    SnippetSortType.Style
                )

                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    snippetTypes.forEachIndexed { index, type ->
                        val position = when (index) {
                            0 -> if (snippetTypes.size == 1) CardPosition.Single else CardPosition.First
                            snippetTypes.size - 1 -> CardPosition.Last
                            else -> CardPosition.Middle
                        }
                        SortOptionItem(viewModel, type, view, position)
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }


        }
    }
}

@Composable
private fun SnippetFilterChip(viewModel: SnippetsViewModel, snippet: String, view: android.view.View) {
    val isSelected = viewModel.selectedFilterSnippets.any { it.equals(snippet, ignoreCase = true) }
    val snippetStyle = viewModel.getSnippetStyle(snippet)
    FilterChip(
        selected = isSelected,
        onClick = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            viewModel.toggleFilterSnippet(snippet)
        },
        label = {
            Text(
                snippet,
                style = getSnippetTextStyle(snippetStyle, MaterialTheme.typography.labelLarge),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
        } else null,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent
        )
    )
}

private fun styleHeaderLabel(style: SnippetStyle): String = when (style) {
    SnippetStyle.Default -> "DEFAULT"
    SnippetStyle.Thin -> "THIN"
    SnippetStyle.Cursive -> "CURSIVE"
    SnippetStyle.Mono -> "MONO"
    SnippetStyle.Serif -> "SERIF"
    SnippetStyle.Spaced -> "SPACED"
    SnippetStyle.Bold -> "BOLD"
    SnippetStyle.FlexHeavy -> "FLEX HEAVY"
    SnippetStyle.FlexWide -> "FLEX WIDE"
    SnippetStyle.FlexSlant -> "FLEX SLANTED"
    SnippetStyle.FlexGrade -> "FLEX GRADE"
}

@Composable
private fun SortOptionItem(viewModel: SnippetsViewModel, type: SnippetSortType, view: android.view.View, position: CardPosition) {
    val isSelected = viewModel.snippetSortType == type
    
    val label = when (type) {
        SnippetSortType.New -> "Newest first"
        SnippetSortType.Old -> "Oldest first"
        SnippetSortType.AZ -> if (isSelected && !viewModel.isSortAscending) "Z-A (alphabetical)" else "A-Z (alphabetical)"
        SnippetSortType.Month -> if (isSelected && !viewModel.isSortAscending) "Last month first" else "First month first"
        SnippetSortType.Year -> if (isSelected && !viewModel.isSortAscending) "Last year first" else "First year first"
        SnippetSortType.Favorites -> "Favorites"
        SnippetSortType.Emoji -> "Sort by emoji"
        SnippetSortType.Emoticons -> "Sort by emoticons"
        SnippetSortType.Color -> "Sort by color"
        SnippetSortType.Style -> "Sort by font style"
        else -> ""
    }

    val icon = when (type) {
        SnippetSortType.Emoji -> Icons.Default.SentimentSatisfiedAlt
        SnippetSortType.Emoticons -> ImageVector.Builder(
            name = "EmoticonSort",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            addGroup(name = "emoticon_group", translationY = 960f).apply {
                addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString("M260-280q-26 0-43-17t-17-43q0-25 17-42.5t43-17.5q25 0 42.5 17.5T320-340q0 26-17.5 43T260-280Zm0-280q-26 0-43-17t-17-43q0-25 17-42.5t43-17.5q25 0 42.5 17.5T320-620q0 26-17.5 43T260-560Zm140 120v-80h160v80H400Zm288 200-66-44q28-43 43-92.5T680-480q0-66-21.5-124T598-709l61-51q48 57 74.5 128.5T760-480q0 67-19 127.5T688-240Z").toNodes(),
                    fill = SolidColor(androidx.compose.ui.graphics.Color.Black)
                )
            }
        }.build()
        SnippetSortType.Favorites -> Icons.Default.Favorite
        SnippetSortType.Color -> Icons.Default.Palette
        SnippetSortType.Style -> Icons.Default.Style
        SnippetSortType.AZ -> ImageVector.Builder(
            name = "AZSort",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            addGroup(name = "az_group", translationY = 960f).apply {
                addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString("m80-280 150-400h86l150 400h-82l-34-96H196l-32 96H80Zm140-164h104l-48-150h-6l-50 150Zm328 164v-76l202-252H556v-72h282v76L638-352h202v72H548ZM360-760l120-120 120 120H360ZM480-80 360-200h240L480-80Z").toNodes(),
                    fill = SolidColor(androidx.compose.ui.graphics.Color.Black)
                )
            }
        }.build()
        SnippetSortType.Month -> ImageVector.Builder(
            name = "MonthSort",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            addGroup(name = "month_group", translationY = 960f).apply {
                addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString("M200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Zm280 240q-17 0-28.5-11.5T440-440q0-17 11.5-28.5T480-480q17 0 28.5 11.5T520-440q0 17-11.5 28.5T480-400Zm-188.5-11.5Q280-423 280-440t11.5-28.5Q303-480 320-480t28.5 11.5Q360-457 360-440t-11.5 28.5Q337-400 320-400t-28.5-11.5ZM640-400q-17 0-28.5-11.5T600-440q0-17 11.5-28.5T640-480q17 0 28.5 11.5T680-440q0 17-11.5 28.5T640-400ZM480-240q-17 0-28.5-11.5T440-280q0-17 11.5-28.5T480-320q17 0 28.5 11.5T520-280q0 17-11.5 28.5T480-240Zm-188.5-11.5Q280-263 280-280t11.5-28.5Q303-320 320-320t28.5 11.5Q360-297 360-280t-11.5 28.5Q337-240 320-240t-28.5-11.5ZM640-240q-17 0-28.5-11.5T600-280q0-17 11.5-28.5T640-320q17 0 28.5 11.5T680-280q0 17-11.5 28.5T640-240Z").toNodes(),
                    fill = SolidColor(androidx.compose.ui.graphics.Color.Black)
                )
            }
        }.build()
        SnippetSortType.Year -> {
            if (isSelected) {
                if (viewModel.isSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
            } else Icons.Default.Style // Placeholder for sort
        }
        SnippetSortType.New -> Icons.Default.TextSnippet
        SnippetSortType.Old -> Icons.Default.TextSnippet
        else -> Icons.Default.Image
    }

    SettingsCardItem(
        icon = icon,
        title = label,
        isSelected = isSelected,
        position = position,
        onClick = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            if (viewModel.snippetSortType == type && (type == SnippetSortType.AZ || type == SnippetSortType.Month || type == SnippetSortType.Year || type == SnippetSortType.Color || type == SnippetSortType.Style)) {
                viewModel.isSortAscending = !viewModel.isSortAscending
            } else {
                viewModel.snippetSortType = type
            }
        }
    )
}


