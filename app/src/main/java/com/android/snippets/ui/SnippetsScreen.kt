package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.snippets.ui.components.AppBar
import com.android.snippets.ui.components.CookieButtonGroup
import com.android.snippets.ui.components.FilterSnippetContent
import com.android.snippets.ui.components.cookieButtonItem
import com.android.snippets.viewmodel.SnippetSortType
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun SnippetsScreen(
    viewModel: SnippetsViewModel,
    category: String = "Library",
    onNavigateBack: () -> Unit
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val currentSortType = uiState.snippetSortType
    val sortIcon = if (currentSortType == SnippetSortType.Old) {
        Icons.Rounded.ArrowDownward
    } else {
        Icons.Rounded.ArrowUpward
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppBar(
                title = "Snippets",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    onNavigateBack()
                },
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isSpinning = true,
                isScrolled = isScrolled,
                leftAlignTitle = true,
                showDividerOnScroll = true,
                scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                actions = {
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
            )
        }
    ) { innerPadding ->
        FilterSnippetContent(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = onNavigateBack,
            showDisplay = true,
            showSort = false,
            scrollState = scrollState,
            selectedTabIndex = 0,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        )
    }
}
