package com.android.snippets.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun FilterFloatingSheet(
    viewModel: SnippetsViewModel,
    modifier: Modifier = Modifier
) {
    val sortIcon = if (viewModel.snippetSortType == SnippetSortType.Old) {
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
                    selectedIndex = when (viewModel.snippetSortType) {
                        SnippetSortType.New, SnippetSortType.Old -> 0
                        SnippetSortType.Emoji -> 1
                        SnippetSortType.AZ -> 2
                        SnippetSortType.Emoticons -> 3
                    },
                    cookieSize = 44.dp,
                    items = listOf(
                        cookieButtonItem(
                            icon = sortIcon,
                            contentDescription = if (viewModel.snippetSortType == SnippetSortType.Old) "Oldest snippets" else "Newest snippets",
                            tooltip = if (viewModel.snippetSortType == SnippetSortType.Old) "Oldest snippets" else "Newest snippets",
                            isSelected = viewModel.snippetSortType == SnippetSortType.New || viewModel.snippetSortType == SnippetSortType.Old,
                            onClick = {
                                if (viewModel.snippetSortType == SnippetSortType.New) {
                                    viewModel.snippetSortType = SnippetSortType.Old
                                } else {
                                    viewModel.snippetSortType = SnippetSortType.New
                                }
                            }
                        ),
                        cookieButtonItem(
                            icon = Icons.Rounded.Mood,
                            contentDescription = "Emojis",
                            tooltip = "Emojis",
                            isSelected = viewModel.snippetSortType == SnippetSortType.Emoji,
                            onClick = {
                                viewModel.snippetSortType = SnippetSortType.Emoji
                            }
                        ),
                        cookieButtonItem(
                            icon = Icons.Rounded.SortByAlpha,
                            contentDescription = "Sort A-Z",
                            tooltip = "Sort A-Z",
                            isSelected = viewModel.snippetSortType == SnippetSortType.AZ,
                            onClick = {
                                viewModel.snippetSortType = SnippetSortType.AZ
                            }
                        ),
                        cookieButtonItem(
                            icon = Icons.Rounded.EmojiEmotions,
                            contentDescription = "Emoticons",
                            tooltip = "Emoticons",
                            isSelected = viewModel.snippetSortType == SnippetSortType.Emoticons,
                            onClick = {
                                viewModel.snippetSortType = SnippetSortType.Emoticons
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
                    onDismiss = { viewModel.closeFilter() },
                    showDisplay = true,
                    showSort = false,
                    selectedTabIndex = 0
                )
            }
        }
    }
}
