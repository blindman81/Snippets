package com.android.snippets.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun FilterFloatingSheet(
    viewModel: SnippetsViewModel,
    modifier: Modifier = Modifier,
    uiState: SnippetsUiState = viewModel.uiState.collectAsState().value
) {
    val currentSortType = uiState.snippetSortType
    val sortIcon = if (currentSortType == SnippetSortType.Old) {
        Icons.Rounded.ArrowDownward
    } else {
        Icons.Rounded.ArrowUpward
    }

    CompositionLocalProvider(
        LocalTiltActive provides true,
        LocalTiltModalFocus provides true
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Button group on the right
                CookieButtonGroup(
                    selectedIndex = when (currentSortType) {
                        SnippetSortType.New, SnippetSortType.Old -> 0
                        SnippetSortType.Emoji -> 1
                        SnippetSortType.AZ -> 2
                        else -> -1
                    },
                    cookieSize = 44.dp,
                    items = listOf(
                        cookieButtonItem(
                            icon = sortIcon,
                            contentDescription = if (currentSortType == SnippetSortType.Old) "Oldest snippets" else "Newest snippets",
                            tooltip = if (currentSortType == SnippetSortType.Old) "Oldest snippets" else "Newest snippets",
                            isSelected = currentSortType == SnippetSortType.New || currentSortType == SnippetSortType.Old,
                            onClick = {
                                if (currentSortType == SnippetSortType.New) {
                                    viewModel.snippetSortType = SnippetSortType.Old
                                } else {
                                    viewModel.snippetSortType = SnippetSortType.New
                                }
                            }
                        ),
                        cookieButtonItem(
                            icon = if (currentSortType == SnippetSortType.Emoji) Icons.Filled.Mood else Icons.Rounded.Mood,
                            contentDescription = "Emojis",
                            tooltip = "Emojis",
                            isSelected = currentSortType == SnippetSortType.Emoji,
                            onClick = {
                                viewModel.snippetSortType = SnippetSortType.Emoji
                            }
                        ),
                        cookieButtonItem(
                            icon = Icons.Rounded.SortByAlpha,
                            contentDescription = "Sort A-Z",
                            tooltip = "Sort A-Z",
                            isSelected = currentSortType == SnippetSortType.AZ,
                            onClick = {
                                viewModel.snippetSortType = SnippetSortType.AZ
                            }
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                FilterSnippetContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    onDismiss = { viewModel.closeFilter() },
                    showDisplay = true,
                    showSort = false,
                    selectedTabIndex = 0
                )
            }
        }
    }
}
