package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.android.snippets.ui.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R

@Composable
fun SelectionToolbarContent(
    viewModel: SnippetsViewModel,
    uiState: SnippetsUiState = viewModel.uiState.collectAsStateWithLifecycle().value,
    onDeleteClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    flatPhotosRaw: List<Photo> = emptyList(),
    currentTab: String = "",
    isSpinning: Boolean = true,
    isScrolled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current

    val isAddOrRemovePhotos = uiState.pendingCollectionAssignment != null || uiState.pendingCollectionRemoval != null

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Cookiebutton(
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

        if (isAddOrRemovePhotos) {
            val isSearchActive = uiState.searchQuery.isNotEmpty()
            val hasMatchingPhotos = remember(isSearchActive, flatPhotosRaw, currentTab) {
                isSearchActive && flatPhotosRaw.any { photo ->
                    when (currentTab) {
                        "Library" -> true
                        "Favorites" -> photo.isFavorite
                        else -> photo.collections.contains(currentTab)
                    }
                }
            }
            val searchIcon = if (hasMatchingPhotos) ImageVector.vectorResource(R.drawable.ic_search_success) else Icons.Rounded.Search
            Cookiebutton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onSearchClick()
                },
                icon = searchIcon,
                contentDescription = "Search",
                isSelected = isSearchActive,
                containerColor = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                size = 44.dp,
                isSpinning = isSpinning
            )
        } else {
            Cookiebutton(
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

        if (isAddOrRemovePhotos) {
            Cookiebutton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    if (uiState.pendingCollectionAssignment != null) {
                        viewModel.confirmCollectionAssignment()
                    } else if (uiState.pendingCollectionRemoval != null) {
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
            Cookiebutton(
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
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100L)
            try {
                searchFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }

        // Close / back
        Cookiebutton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                searchTextFieldState.clearText()
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

        // Text field
        BasicTextField(
            state = searchTextFieldState,
            modifier = Modifier
                .weight(1f)
                .focusRequester(searchFocusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
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
                    innerTextField()
                }
            }
        )

        // Clear button
        AnimatedVisibility(
            visible = searchTextFieldState.text.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200))
        ) {
            Cookiebutton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    searchTextFieldState.clearText()
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
    cookieToolbarOffset: () -> Dp = { 0.dp },
    cookieToolbarScale: () -> Float = { 1f },
    isMenuOpen: Boolean = false,
    isManageOpen: Boolean = false,
    isFilterOpen: Boolean = false,
    onMenuClick: () -> Unit,
    onManageClick: () -> Unit,
    onFilterClick: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

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
                    translationY = cookieToolbarOffset().toPx()
                    scaleX = cookieToolbarScale()
                    scaleY = cookieToolbarScale()
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlurredSurface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                AnimatedContent(
                    targetState = when {
                        isSearchOpen -> 1
                        uiState.isSelectionMode -> 2
                        else -> 0
                    },
                    transitionSpec = {
                        (fadeIn(Motion.StandardFade) + scaleIn(initialScale = 0.92f, animationSpec = Motion.DialogScaleEnter))
                            .togetherWith(fadeOut(Motion.FastFade) + scaleOut(targetScale = 0.92f, animationSpec = Motion.DialogScaleExit))
                    },
                    contentAlignment = Alignment.Center,
                    label = "cookie_toolbar_mode",
                    modifier = Modifier.fillMaxSize()
                ) { searchMode ->
                    if (searchMode == 2) {
                        SelectionToolbarContent(
                            viewModel = viewModel,
                            uiState = uiState,
                            onDeleteClick = onBulkDeleteClick,
                            onSearchClick = { onSearchOpenChange(true) },
                            flatPhotosRaw = flatPhotosRaw,
                            currentTab = currentTab,
                            isSpinning = !isAnyPopupActive,
                            isScrolled = isScrolled
                        )
                    } else if (searchMode == 1) {
                        ExpandedSearchContent(
                            viewModel = viewModel,
                            searchFocusRequester = searchFocusRequester,
                            onCloseOrBack = {
                                onSearchOpenChange(false)
                                focusManager.clearFocus()
                                scope.launch {
                                    kotlinx.coroutines.delay(200L)
                                    viewModel.searchQuery = ""
                                }
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
                            Cookiebutton(
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
                            val isSearchActive = uiState.searchQuery.isNotEmpty()
                            val hasMatchingPhotos = remember(isSearchActive, flatPhotosRaw, currentTab) {
                                isSearchActive && flatPhotosRaw.any { photo ->
                                    when (currentTab) {
                                        "Library" -> true
                                        "Favorites" -> photo.isFavorite
                                        else -> photo.collections.contains(currentTab)
                                    }
                                }
                            }
                            val searchIcon = if (hasMatchingPhotos) ImageVector.vectorResource(R.drawable.ic_search_success) else Icons.Rounded.Search
                            Cookiebutton(
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
                            Cookiebutton(
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
                            val hasFilterSnippets = uiState.selectedFilterSnippets.isNotEmpty()
                            val isFilterHighlighted = isFilterOpen || hasFilterSnippets
                            val filterContentDescription = if (isFilterOpen) "Close Filters" else "Filters"
                            Cookiebutton(
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
