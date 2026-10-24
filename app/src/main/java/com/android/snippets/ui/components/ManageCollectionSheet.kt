package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import com.android.snippets.ui.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.GoogleSansFlexWide
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.VerticalScrollbar
import com.android.snippets.viewmodel.PhotoSortType
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R
import kotlinx.coroutines.launch

private data class ActionItemData(
    val id: String,
    val label: String,
    val icon: Any,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManageCollectionBottomSheet(
    collection: String,
    currentTab: String,
    viewModel: SnippetsViewModel,
    isDeletingCollection: Boolean,
    onDismissRequest: () -> Unit,
    onAddPhotos: (String) -> Unit,
    onStartDeleteCollection: (String) -> Unit,
    onUpdateSort: (PhotoSortType) -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    BlurredModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            com.android.snippets.ui.util.LocalTiltActive provides (com.android.snippets.ui.util.LocalTiltActive.current && !isDeletingCollection)
        ) {
            Motion.ExpressiveSheetContent {
                val managePagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
                val managePage1ScrollState = rememberScrollState()
                val managePage2ScrollState = rememberScrollState()

                val iconOrEmoji = when (collection) {
                    "Library" -> CollectionsBookmarkIcon(filled = true)
                    "Favorites" -> Icons.Rounded.Favorite
                    else -> viewModel.getCollectionIcon(collection)
                }

                val actions = remember(collection) {
                    listOfNotNull(
                        ActionItemData(
                            id = "ADD",
                            label = "Add photos",
                            icon = Icons.Rounded.AddPhotoAlternate,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onAddPhotos(collection)
                                onDismissRequest()
                            }
                        ),
                        if (collection != "Library") ActionItemData(
                            id = "REMOVE",
                            label = "Remove photos",
                            icon = Icons.Rounded.Remove,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.startCollectionRemoval(collection)
                                onDismissRequest()
                            }
                        ) else null,
                        if (collection != "Library" && collection != "Favorites") ActionItemData(
                            id = "EDIT_EMOJI",
                            label = "Add / edit emoji",
                            icon = Icons.Rounded.AddReaction,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.navigateEmojiPicker(collection)
                                onDismissRequest()
                            }
                        ) else null,
                        if (collection != "Library" && collection != "Favorites") ActionItemData(
                            id = "DELETE",
                            label = "Delete",
                            icon = R.drawable.ic_delete,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onStartDeleteCollection(collection)
                                onDismissRequest()
                            }
                        ) else null
                    )
                }

                val currentSortType = viewModel.getPhotoSortTypeFor(collection.ifEmpty { currentTab })

                // Group 1: New. old
                val newOldItems = remember {
                    listOf(
                        cookieButtonItem(Icons.Rounded.ArrowDownward, contentDescription = "Newest first", tooltip = "Newest first", iconSize = 36.dp),
                        cookieButtonItem(Icons.Rounded.ArrowUpward, contentDescription = "Oldest first", tooltip = "Oldest first", iconSize = 36.dp)
                    )
                }
                val selectedNewOldIndex = when (currentSortType) {
                    PhotoSortType.DateNewest -> 0
                    PhotoSortType.DateOldest -> 1
                    else -> -1
                }

                // Group 2: Most
                val mostItems = remember {
                    listOf(
                        cookieButtonItem(Icons.AutoMirrored.Rounded.TextSnippet, contentDescription = "Most snippets", tooltip = "Most snippets", iconSize = 36.dp),
                        cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Most starred", tooltip = "Most starred", iconSize = 36.dp)
                    )
                }
                val selectedMostIndex = when (currentSortType) {
                    PhotoSortType.MostSnippets -> 0
                    PhotoSortType.MostStarred -> 1
                    else -> -1
                }

                // Group 3: Least
                val leastItems = remember {
                    listOf(
                        cookieButtonItem(Icons.AutoMirrored.Rounded.TextSnippet, contentDescription = "Least snippets", tooltip = "Least snippets", iconSize = 36.dp),
                        cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Least starred", tooltip = "Least starred", iconSize = 36.dp)
                    )
                }
                val selectedLeastIndex = when (currentSortType) {
                    PhotoSortType.LeastSnippets -> 0
                    PhotoSortType.LeastStarred -> 1
                    else -> -1
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Collection icon, pagination dots, and vertical scrollbar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .width(48.dp)
                            .fillMaxHeight()
                    ) {
                        Surface(
                            shape = CookieShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when (iconOrEmoji) {
                                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                                        Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                    is String -> {
                                        Text(text = iconOrEmoji, fontSize = 20.sp)
                                    }
                                    else -> {
                                        Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Spring physics M3 pagination dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(2) { index ->
                                val isSelected = (if (managePagerState.isScrollInProgress) managePagerState.targetPage else managePagerState.currentPage) == index
                                val animatedWidth by animateDpAsState(
                                    targetValue = if (isSelected) 18.dp else 6.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "manage_dot_width_$index"
                                )
                                val primaryAlpha by animateFloatAsState(
                                    targetValue = if (isSelected) 1f else 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "manage_dot_primary_alpha_$index"
                                )

                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(animatedWidth)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (!isSelected) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                scope.launch {
                                                    managePagerState.animateScrollToPage(
                                                        page = index,
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                                            stiffness = Spring.StiffnessLow
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            )
                                    )
                                    if (primaryAlpha > 0.001f) {
                                        val primaryColor = MaterialTheme.colorScheme.primary
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .graphicsLayer { alpha = primaryAlpha }
                                                .border(1.dp, primaryColor, CircleShape)
                                                .clip(CircleShape)
                                                .background(primaryColor)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        VerticalScrollbar(
                            scrollState = if (managePagerState.currentPage == 0) managePage1ScrollState else managePage2ScrollState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }

                    // Horizontal Pager for Page 1 (Actions) and Page 2 (Sort)
                    HorizontalPager(
                        state = managePagerState,
                        beyondViewportPageCount = 2,
                        pageSpacing = 16.dp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        androidx.compose.runtime.CompositionLocalProvider(
                            com.android.snippets.ui.util.LocalTiltActive provides
                                (com.android.snippets.ui.util.LocalTiltActive.current &&
                                    managePagerState.currentPage == page &&
                                    !managePagerState.isScrollInProgress)
                        ) {
                            when (page) {
                                0 -> {
                                    // Page 1: Actions (Animated cookie buttons with pill below, vertical scroll)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(managePage1ScrollState),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        actions.chunked(2).forEach { rowActions ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                rowActions.forEach { action ->
                                                    when (val icon = action.icon) {
                                                        is androidx.compose.ui.graphics.vector.ImageVector -> AnimatedCookieButton(
                                                            onClick = action.onClick,
                                                            icon = icon,
                                                            contentDescription = action.label,
                                                            tooltip = action.label,
                                                            size = 84.dp,
                                                            iconSize = 38.dp,
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                        is Int -> AnimatedCookieButton(
                                                            onClick = action.onClick,
                                                            icon = icon,
                                                            contentDescription = action.label,
                                                            tooltip = action.label,
                                                            size = 84.dp,
                                                            iconSize = 38.dp,
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    // Page 2: Sort button groups (84dp cookie buttons, 3 groups with pill labels, vertical scroll)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(managePage2ScrollState),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        // Group 1: New. old
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CookieButtonGroup(
                                                selectedIndex = selectedNewOldIndex,
                                                onSelect = { index ->
                                                    val option = if (index == 0) PhotoSortType.DateNewest else PhotoSortType.DateOldest
                                                    onUpdateSort(option)
                                                },
                                                items = newOldItems,
                                                columns = 1,
                                                cookieSize = 84.dp,
                                                overlap = 0.dp,
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                enableWiggle = true
                                            )

                                            Row(
                                                modifier = Modifier.width(168.dp),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier.width(84.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.clickable {
                                                            onUpdateSort(PhotoSortType.DateNewest)
                                                        }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "New",
                                                                style = MaterialTheme.typography.labelMedium.copy(
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontFamily = GoogleSansFlexWide
                                                                ),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier.width(84.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.clickable {
                                                            onUpdateSort(PhotoSortType.DateOldest)
                                                        }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "Old",
                                                                style = MaterialTheme.typography.labelMedium.copy(
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontFamily = GoogleSansFlexWide
                                                                ),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Group 2: Most
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CookieButtonGroup(
                                                selectedIndex = selectedMostIndex,
                                                onSelect = { index ->
                                                    val option = if (index == 0) PhotoSortType.MostSnippets else PhotoSortType.MostStarred
                                                    onUpdateSort(option)
                                                },
                                                items = mostItems,
                                                columns = 1,
                                                cookieSize = 84.dp,
                                                overlap = 0.dp,
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                enableWiggle = true
                                            )

                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Most",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontFamily = GoogleSansFlexWide
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        // Group 3: Least
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CookieButtonGroup(
                                                selectedIndex = selectedLeastIndex,
                                                onSelect = { index ->
                                                    val option = if (index == 0) PhotoSortType.LeastSnippets else PhotoSortType.LeastStarred
                                                    onUpdateSort(option)
                                                },
                                                items = leastItems,
                                                columns = 1,
                                                cookieSize = 84.dp,
                                                overlap = 0.dp,
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                enableWiggle = true
                                            )

                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Least",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontFamily = GoogleSansFlexWide
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
