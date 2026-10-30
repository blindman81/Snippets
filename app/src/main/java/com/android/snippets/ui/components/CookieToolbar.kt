package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import com.android.snippets.ui.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R

@Composable
fun SelectionToolbarContent(
    viewModel: SnippetsViewModel,
    onDeleteClick: () -> Unit,
    isSpinning: Boolean = true,
    isScrolled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedCookieButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                viewModel.clearSelection()
            },
            icon = Icons.Rounded.Close,
            contentDescription = "Clear Selection",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            size = 44.dp,
            isSpinning = isSpinning
        )

        val textToShow = when {
            viewModel.pendingCollectionAssignment != null -> "Add photos"
            viewModel.pendingCollectionRemoval != null -> "Remove photos"
            else -> ""
        }
        if (textToShow.isNotEmpty()) {
            Text(
                text = textToShow,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            AnimatedCookieButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.shareSelectedPhotos(context)
                },
                icon = R.drawable.ic_share,
                contentDescription = "Share Selection",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                size = 44.dp,
                isSpinning = isSpinning
            )
        }

        if (viewModel.pendingCollectionAssignment != null || viewModel.pendingCollectionRemoval != null) {
            AnimatedCookieButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    if (viewModel.pendingCollectionAssignment != null) {
                        viewModel.confirmCollectionAssignment()
                    } else if (viewModel.pendingCollectionRemoval != null) {
                        viewModel.confirmCollectionRemoval()
                    }
                },
                icon = Icons.Rounded.Check,
                contentDescription = "Confirm",
                isSelected = true,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                size = 44.dp,
                isSpinning = isSpinning
            )
        } else {
            AnimatedCookieButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    onDeleteClick()
                },
                icon = R.drawable.ic_delete,
                contentDescription = "Delete",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                size = 44.dp,
                isSpinning = isSpinning
            )
        }
    }
}

@Composable
fun ExpandedSearchContent(
    viewModel: SnippetsViewModel,
    searchFocusRequester: FocusRequester,
    onCloseOrBack: () -> Unit,
    isSpinning: Boolean = true,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close / back
        AnimatedCookieButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                onCloseOrBack()
            },
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Close Search",
            tooltip = "Close",
            isSpinning = isSpinning,
            size = 48.dp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        val searchTextFieldState = rememberTextFieldState(viewModel.searchQuery)
        LaunchedEffect(searchTextFieldState) {
            snapshotFlow { searchTextFieldState.text.toString() }.collect { newText ->
                if (viewModel.searchQuery != newText) {
                    viewModel.searchQuery = newText
                }
            }
        }
        LaunchedEffect(viewModel.searchQuery) {
            if (searchTextFieldState.text.toString() != viewModel.searchQuery) {
                searchTextFieldState.setTextAndPlaceCursorAtEnd(viewModel.searchQuery)
            }
        }

        // Text field
        BasicTextField(
            state = searchTextFieldState,
            modifier = Modifier
                .weight(1f)
                .focusRequester(searchFocusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = {
                focusManager.clearFocus()
            },
            decorator = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (viewModel.searchQuery.isEmpty()) {
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Clear button
        if (viewModel.searchQuery.isNotEmpty()) {
            AnimatedCookieButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.searchQuery = ""
                },
                icon = Icons.Rounded.Close,
                contentDescription = "Clear",
                tooltip = "Clear",
                isSpinning = isSpinning,
                size = 48.dp,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CookieToolbar(
    viewModel: SnippetsViewModel,
    currentTab: String,
    flatPhotosRaw: List<Photo>,
    isSearchOpen: Boolean,
    onSearchOpenChange: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester,
    isScrolled: Boolean,
    isAnyPopupActive: Boolean,
    cookieToolbarOffset: Dp,
    cookieToolbarScale: Float,
    isMenuOpen: Boolean = false,
    isManageOpen: Boolean = false,
    isFilterOpen: Boolean = false,
    onMenuClick: () -> Unit,
    onManageClick: () -> Unit,
    onFilterClick: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    val isAddOrRemovePhotos = viewModel.isSelectionMode && (viewModel.pendingCollectionAssignment != null || viewModel.pendingCollectionRemoval != null)
    val selectionSearchFocusRequester = remember { FocusRequester() }

    CompositionLocalProvider(
        LocalTiltActive provides true,
        LocalTiltModalFocus provides true
    ) {
        Column(
            modifier = modifier
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = cookieToolbarOffset.toPx()
                    scaleX = cookieToolbarScale
                    scaleY = cookieToolbarScale
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val motionScheme = MaterialTheme.motionScheme

            AnimatedVisibility(
                visible = isAddOrRemovePhotos,
                enter = fadeIn(motionScheme.defaultEffectsSpec()) + slideInVertically(motionScheme.defaultSpatialSpec()) { it / 2 },
                exit = fadeOut(motionScheme.fastEffectsSpec()) + slideOutVertically(motionScheme.fastSpatialSpec()) { it / 2 }
            ) {
                BlurredSurface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) {
                    ExpandedSearchContent(
                        viewModel = viewModel,
                        searchFocusRequester = selectionSearchFocusRequester,
                        onCloseOrBack = {
                            viewModel.searchQuery = ""
                            focusManager.clearFocus()
                        },
                        isSpinning = !isAnyPopupActive
                    )
                }
            }

            BlurredSurface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                AnimatedContent(
                    targetState = when {
                        viewModel.isSelectionMode -> 2
                        isSearchOpen -> 1
                        else -> 0
                    },
                    transitionSpec = {
                        (fadeIn(motionScheme.defaultEffectsSpec()) + scaleIn(initialScale = 0.92f, animationSpec = motionScheme.defaultSpatialSpec()))
                            .togetherWith(fadeOut(motionScheme.fastEffectsSpec()) + scaleOut(targetScale = 0.92f, animationSpec = motionScheme.fastSpatialSpec()))
                    },
                    contentAlignment = Alignment.Center,
                    label = "cookie_toolbar_mode",
                    modifier = Modifier.fillMaxSize()
                ) { searchMode ->
                    if (searchMode == 2) {
                        SelectionToolbarContent(
                            viewModel = viewModel,
                            onDeleteClick = onBulkDeleteClick,
                            isSpinning = !isAnyPopupActive,
                            isScrolled = isScrolled
                        )
                    } else if (searchMode == 1) {
                        ExpandedSearchContent(
                            viewModel = viewModel,
                            searchFocusRequester = searchFocusRequester,
                            onCloseOrBack = {
                                onSearchOpenChange(false)
                                viewModel.searchQuery = ""
                                focusManager.clearFocus()
                            },
                            isSpinning = true
                        )
                    } else {
                        // Normal Mode
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Menu
                            AnimatedCookieButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onMenuClick()
                                },
                                icon = Icons.Rounded.Menu,
                                closeIcon = Icons.Rounded.Close,
                                isOpen = isMenuOpen,
                                contentDescription = if (isMenuOpen) "Close Menu" else "Menu",
                                size = 48.dp
                            )

                            // Search
                            val isSearchActive = viewModel.searchQuery.isNotEmpty()
                            val searchIcon = if (isSearchActive && flatPhotosRaw.any { photo ->
                                when (currentTab) {
                                    "Library" -> true
                                    "Favorites" -> photo.isFavorite
                                    else -> photo.collections.contains(currentTab)
                                }
                            }) ImageVector.vectorResource(R.drawable.ic_search_success) else Icons.Rounded.Search
                            AnimatedCookieButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSearchOpenChange(true)
                                },
                                icon = searchIcon,
                                contentDescription = "Search",
                                isSelected = isSearchActive,
                                isSpinning = !isAnyPopupActive,
                                size = 48.dp,
                                containerColor = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // Manage (Middle)
                            AnimatedCookieButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onManageClick()
                                },
                                icon = R.drawable.ic_folder_managed,
                                closeIcon = Icons.Rounded.Close,
                                isOpen = isManageOpen,
                                contentDescription = if (isManageOpen) "Close Manage" else "Manage",
                                size = 48.dp
                            )

                            // Filters
                            val hasFilterSnippets = viewModel.selectedFilterSnippets.isNotEmpty()
                            val isFilterHighlighted = isFilterOpen || hasFilterSnippets
                            val filterContentDescription = if (isFilterOpen) "Close Filters" else "Filters"
                            AnimatedCookieButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onFilterClick()
                                },
                                icon = Icons.Rounded.FilterList,
                                closeIcon = Icons.Rounded.Close,
                                isOpen = isFilterOpen,
                                isSelected = isFilterHighlighted,
                                contentDescription = filterContentDescription,
                                size = 48.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
