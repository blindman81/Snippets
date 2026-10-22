package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.components.FilterSnippetContent
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(viewModel: SnippetsViewModel) {
    val view = LocalView.current

    AppPredictiveBackHandler {
        viewModel.closeFilter()
    }

    var expanded by remember { mutableStateOf(false) }

    var isNew by remember(viewModel.snippetSortType) {
        mutableStateOf(viewModel.snippetSortType != SnippetSortType.Old)
    }
    var azAscending by remember(viewModel.snippetSortType, viewModel.isSortAscending) {
        mutableStateOf(if (viewModel.snippetSortType == SnippetSortType.AZ) viewModel.isSortAscending else true)
    }
    var monthAscending by remember(viewModel.snippetSortType, viewModel.isSortAscending) {
        mutableStateOf(if (viewModel.snippetSortType == SnippetSortType.Month) viewModel.isSortAscending else true)
    }
    var yearAscending by remember(viewModel.snippetSortType, viewModel.isSortAscending) {
        mutableStateOf(if (viewModel.snippetSortType == SnippetSortType.Year) viewModel.isSortAscending else true)
    }
    val currentSortLabel = when (viewModel.snippetSortType) {
        SnippetSortType.New -> "new"
        SnippetSortType.Old -> "old"
        SnippetSortType.AZ -> if (viewModel.isSortAscending) "a-z" else "z-a"
        SnippetSortType.Month -> if (viewModel.isSortAscending) "first month" else "last month"
        SnippetSortType.Year -> if (viewModel.isSortAscending) "first year" else "last year"
        SnippetSortType.Favorites -> "favorites"
        SnippetSortType.Emoji -> "emoji"
        SnippetSortType.Emoticons -> "emoticons"
    }

    val currentSortArrow = when (viewModel.snippetSortType) {
        SnippetSortType.Old -> Icons.Rounded.ArrowDownward
        SnippetSortType.New -> Icons.Rounded.ArrowUpward
        SnippetSortType.AZ, SnippetSortType.Month, SnippetSortType.Year -> {
            if (viewModel.isSortAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward
        }
        else -> null
    }

    data class SortMenuItem(
        val text: String,
        val arrowIcon: ImageVector? = null,
        val onToggleArrow: (() -> Unit)? = null,
        val onSelect: () -> Unit,
        val isSelected: Boolean
    )

    val menuItems = listOf(
        // 1. new > old
        SortMenuItem(
            text = if (isNew) "new" else "old",
            arrowIcon = if (isNew) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
            onToggleArrow = {
                val next = !isNew
                isNew = next
                if (viewModel.snippetSortType == SnippetSortType.New || viewModel.snippetSortType == SnippetSortType.Old) {
                    viewModel.snippetSortType = if (next) SnippetSortType.New else SnippetSortType.Old
                }
            },
            onSelect = {
                viewModel.snippetSortType = if (isNew) SnippetSortType.New else SnippetSortType.Old
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.New || viewModel.snippetSortType == SnippetSortType.Old
        ),
        // 2. a-z > z-a
        SortMenuItem(
            text = if (azAscending) "a-z" else "z-a",
            arrowIcon = if (azAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
            onToggleArrow = {
                val next = !azAscending
                azAscending = next
                if (viewModel.snippetSortType == SnippetSortType.AZ) {
                    viewModel.isSortAscending = next
                }
            },
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.AZ
                viewModel.isSortAscending = azAscending
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.AZ
        ),
        // 3. first month > last month
        SortMenuItem(
            text = if (monthAscending) "first month" else "last month",
            arrowIcon = if (monthAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
            onToggleArrow = {
                val next = !monthAscending
                monthAscending = next
                if (viewModel.snippetSortType == SnippetSortType.Month) {
                    viewModel.isSortAscending = next
                }
            },
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.Month
                viewModel.isSortAscending = monthAscending
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.Month
        ),
        // 4. first year > last year
        SortMenuItem(
            text = if (yearAscending) "first year" else "last year",
            arrowIcon = if (yearAscending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
            onToggleArrow = {
                val next = !yearAscending
                yearAscending = next
                if (viewModel.snippetSortType == SnippetSortType.Year) {
                    viewModel.isSortAscending = next
                }
            },
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.Year
                viewModel.isSortAscending = yearAscending
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.Year
        ),
        // 5. favorites
        SortMenuItem(
            text = "favorites",
            arrowIcon = null,
            onToggleArrow = null,
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.Favorites
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.Favorites
        ),
        // 6. emoji
        SortMenuItem(
            text = "emoji",
            arrowIcon = null,
            onToggleArrow = null,
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.Emoji
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.Emoji
        ),
        // 7. emoticons
        SortMenuItem(
            text = "emoticons",
            arrowIcon = null,
            onToggleArrow = null,
            onSelect = {
                viewModel.snippetSortType = SnippetSortType.Emoticons
            },
            isSelected = viewModel.snippetSortType == SnippetSortType.Emoticons
        )
    )

    val configuration = LocalConfiguration.current
    val maxContentHeight = (configuration.screenHeightDp * 0.72f).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CookieShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.FilterList, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }

            Box {
                FilterChip(
                    selected = true,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        expanded = !expanded
                    },
                    label = {
                        Text(
                            text = currentSortLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    },
                    leadingIcon = currentSortArrow?.let { arrow ->
                        {
                            Icon(
                                imageVector = arrow,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    menuItems.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = if (item.isSelected) com.android.snippets.ui.theme.GoogleSansFlexWide else com.android.snippets.ui.theme.GoogleSansFlexRound,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            leadingIcon = if (item.arrowIcon != null) {
                                {
                                    IconButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            item.onToggleArrow?.invoke()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = item.arrowIcon,
                                            contentDescription = "Toggle sort direction",
                                            tint = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else null,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                item.onSelect()
                                expanded = false
                            },
                            contentPadding = PaddingValues(
                                start = if (item.arrowIcon != null) 8.dp else 16.dp,
                                end = 8.dp,
                                top = 2.dp,
                                bottom = 2.dp
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
        ) {
            FilterSnippetContent(
                viewModel = viewModel,
                onDismiss = { viewModel.closeFilter() },
                showDisplay = true,
                showSort = false,
                selectedTabIndex = 0
            )
        }
    }
}
