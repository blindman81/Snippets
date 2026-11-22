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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.vector.ImageVector
import com.ln.android.snippets.R
import com.android.snippets.viewmodel.SnippetsViewModel
import kotlinx.coroutines.launch

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
    cookieNavBarOffset: Dp = 0.dp,
    cookieNavBarAlpha: Float = 1f,
    onScrollCarouselToTop: (() -> Unit)? = null,
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
                clip = false
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(tabRowScrollState)
                .graphicsLayer { clip = false }
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val boxIcon = ImageVector.vectorResource(R.drawable.ic_box)
            val boxFilledIcon = ImageVector.vectorResource(R.drawable.ic_box_filled)
            val cookieNavBarItems = remember(
                allTabs,
                activeTabTarget,
                boxIcon,
                boxFilledIcon,
                viewModel.boxIcons,
                viewModel.editingBoxEmoji,
                viewModel.editingEmojiPreview
            ) {
                allTabs.mapIndexed { _, tabName ->
                    val isSelected = tabName == activeTabTarget
                    val iconOrEmoji = when (tabName) {
                        "Box", "Library" -> if (isSelected) boxFilledIcon else boxIcon
                        "Box 1", "Favorites" -> viewModel.getBoxIcon("Box 1")
                        else -> viewModel.getBoxIcon(tabName)
                    }
                    val isSystem = tabName == "Box" || tabName == "Library" || tabName == "Box 1" || tabName == "Favorites"
                    val displayName = com.android.snippets.logic.BoxManager.getBoxDisplayName(
                        box = tabName,
                        userBoxes = viewModel.userBoxes
                    )
                    val positionModifier = Modifier.onGloballyPositioned { coords ->
                        val left = coords.boundsInParent().left
                        val width = coords.size.width.toFloat()
                        if (tabPositions[tabName] != left) tabPositions[tabName] = left
                        if (tabWidths[tabName] != width) tabWidths[tabName] = width
                    }

                    cookieButtonItem(
                        contentDescription = displayName,
                        tooltip = displayName,
                        modifier = positionModifier,
                        icon = { iconModifier, tint ->
                            when (iconOrEmoji) {
                                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                                    iconOrEmoji,
                                    contentDescription = displayName,
                                    modifier = iconModifier.size(20.dp),
                                    tint = tint
                                )
                                is Int -> {
                                    val resId = if (isSelected && (iconOrEmoji == R.drawable.ic_box || iconOrEmoji == R.drawable.ic_box_filled)) {
                                        R.drawable.ic_box_filled
                                    } else if (!isSelected && iconOrEmoji == R.drawable.ic_box_filled) {
                                        R.drawable.ic_box
                                    } else {
                                        iconOrEmoji
                                    }
                                    Icon(
                                        painterResource(id = resId),
                                        contentDescription = displayName,
                                        modifier = iconModifier.size(20.dp),
                                        tint = tint
                                    )
                                }
                                is String -> if (iconOrEmoji.isNotEmpty() && iconOrEmoji.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                                    Text(
                                        text = iconOrEmoji,
                                        fontSize = 16.sp,
                                        modifier = iconModifier
                                    )
                                } else {
                                    Icon(
                                        if (isSelected) boxFilledIcon else boxIcon,
                                        contentDescription = displayName,
                                        modifier = iconModifier.size(20.dp),
                                        tint = tint
                                    )
                                }
                                else -> Icon(
                                    if (isSelected) boxFilledIcon else boxIcon,
                                    contentDescription = displayName,
                                    modifier = iconModifier.size(20.dp),
                                    tint = tint
                                )
                            }
                        }
                    )
                }
            }

            val selectedTabIndex = allTabs.indexOf(activeTabTarget).coerceAtLeast(0)

            CookieNavBar(
                selectedIndex = selectedTabIndex,
                onSelect = selectTab@{ index ->
                    val tabName = allTabs.getOrNull(index) ?: return@selectTab
                    val pageIndex = pageTabs.indexOf(tabName)
                    if (pageIndex != -1) {
                        if (tabName == currentTab) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onScrollCarouselToTop?.invoke()
                        } else {
                            scope.launch {
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
                isOnBlurredSurface = true,
                enableWiggle = true
            )

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
