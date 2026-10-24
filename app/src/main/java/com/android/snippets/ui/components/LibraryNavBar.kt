package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import com.android.snippets.ui.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.viewmodel.SnippetsViewModel
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

@Composable
fun LibraryStatusBarBlur(
    statusBarTop: Dp,
    alpha: Float,
    hazeState: HazeState,
    style: HazeStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarTop)
            .graphicsLayer { this.alpha = alpha }
            .then(
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = style
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f
                        )
                    }
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                }
            )
    )
}

@Composable
fun LibraryNavBar(
    allTabs: List<String>,
    activeTabTarget: String,
    currentTab: String,
    pageTabs: List<String>,
    pagerState: PagerState,
    viewModel: SnippetsViewModel,
    tabRowScrollState: ScrollState,
    tabPositions: MutableMap<String, Float>,
    tabWidths: MutableMap<String, Float>,
    statusBarTop: Dp,
    cookieNavBarOffset: Dp,
    cookieNavBarAlpha: Float,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarTop + 8.dp)
            .graphicsLayer {
                translationY = cookieNavBarOffset.toPx()
                alpha = cookieNavBarAlpha
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .horizontalScroll(tabRowScrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cookieNavBarItems = remember(allTabs, activeTabTarget) {
                allTabs.mapIndexed { _, tabName ->
                    val iconOrEmoji = when (tabName) {
                        "Library" -> CollectionsBookmarkIcon(filled = tabName == activeTabTarget)
                        "Favorites" -> Icons.Rounded.Favorite
                        else -> viewModel.getCollectionIcon(tabName)
                    }
                    val isSystem = tabName == "Library" || tabName == "Favorites"
                    val positionModifier = Modifier.onGloballyPositioned { coords ->
                        tabPositions[tabName] = coords.boundsInParent().left
                        tabWidths[tabName] = coords.size.width.toFloat()
                    }

                    cookieButtonItem(
                        contentDescription = tabName,
                        tooltip = if (isSystem) tabName else null,
                        modifier = positionModifier,
                        icon = { iconModifier, tint ->
                            when (iconOrEmoji) {
                                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                                    iconOrEmoji,
                                    contentDescription = tabName,
                                    modifier = iconModifier.size(20.dp),
                                    tint = tint
                                )
                                is Int -> Icon(
                                    painterResource(id = iconOrEmoji),
                                    contentDescription = tabName,
                                    modifier = iconModifier.size(20.dp),
                                    tint = tint
                                )
                                is String -> Text(
                                    text = iconOrEmoji,
                                    fontSize = 16.sp,
                                    modifier = iconModifier
                                )
                                else -> Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = tabName,
                                    modifier = iconModifier.size(20.dp),
                                    tint = tint
                                )
                            }
                        }
                    )
                }
            }

            val selectedTabIndex = allTabs.indexOf(activeTabTarget).coerceAtLeast(0)

            CookieButtonGroup(
                selectedIndex = selectedTabIndex,
                onSelect = selectTab@{ index ->
                    val tabName = allTabs.getOrNull(index) ?: return@selectTab
                    val pageIndex = pageTabs.indexOf(tabName)
                    if (pageIndex != -1) {
                        if (tabName == currentTab) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        } else {
                            scope.launch {
                                val current = pagerState.currentPage
                                if (kotlin.math.abs(pageIndex - current) > 1) {
                                    pagerState.scrollToPage(if (pageIndex > current) pageIndex - 1 else pageIndex + 1)
                                }
                                pagerState.animateScrollToPage(pageIndex)
                            }
                        }
                    }
                },
                items = cookieNavBarItems,
                cookieSize = 48.dp,
                overlap = 0.dp,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                enableWiggle = true
            )
        }
    }
}
