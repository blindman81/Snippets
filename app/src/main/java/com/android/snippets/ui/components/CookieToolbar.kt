package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.snippets.logic.PhotoSearchHelper
import com.android.snippets.model.Photo
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun ToolbarCookieItem(
    label: String,
    icon: Any, // ImageVector or Int (@DrawableRes)
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isSpinning: Boolean = true
) {
    when (icon) {
        is ImageVector -> Cookiebutton(
            onClick = onClick,
            icon = icon,
            contentDescription = label,
            tooltip = label,
            isSelected = isSelected,
            isOnBlurredSurface = true,
            isSpinning = isSpinning,
            size = 48.dp,
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = modifier
        )
        is Int -> Cookiebutton(
            onClick = onClick,
            icon = icon,
            contentDescription = label,
            tooltip = label,
            isSelected = isSelected,
            isOnBlurredSurface = true,
            isSpinning = isSpinning,
            size = 48.dp,
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = modifier
        )
    }
}

@Composable
fun SelectionToolbarContent(
    viewModel: SnippetsViewModel,
    uiState: SnippetsUiState = viewModel.uiState.collectAsStateWithLifecycle().value,
    onDeleteClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    flatPhotosRaw: List<Photo> = emptyList(),
    currentTab: String = "",
    isSpinning: Boolean = true,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current

    val isAddOrRemovePhotos = uiState.pendingBoxAssignment != null || uiState.pendingBoxRemoval != null

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Clear Selection
        ToolbarCookieItem(
            label = "Clear",
            icon = Icons.Rounded.Close,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                viewModel.clearSelection()
            },
            isSpinning = isSpinning
        )

        // Search Selection
        val isSearchActive = uiState.searchQuery.isNotEmpty()
        val hasMatchingPhotos = remember(isSearchActive, flatPhotosRaw, currentTab) {
            isSearchActive && flatPhotosRaw.any { photo ->
                PhotoSearchHelper.matchesTab(photo, currentTab)
            }
        }
        val searchIcon = if (hasMatchingPhotos) ImageVector.vectorResource(R.drawable.ic_search_success) else Icons.Rounded.Search
        ToolbarCookieItem(
            label = "Search",
            icon = searchIcon,
            isSelected = isSearchActive,
            onClick = onSearchClick,
            isSpinning = isSpinning
        )

        if (isAddOrRemovePhotos) {
            ToolbarCookieItem(
                label = "Confirm",
                icon = Icons.Rounded.Check,
                isSelected = true,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    if (uiState.pendingBoxAssignment != null) {
                        viewModel.confirmBoxAssignment()
                    } else if (uiState.pendingBoxRemoval != null) {
                        viewModel.confirmBoxRemoval()
                    }
                },
                isSpinning = isSpinning
            )
        } else {
            ToolbarCookieItem(
                label = "Share",
                icon = Icons.Rounded.Share,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.shareSelectedPhotos(context)
                },
                isSpinning = isSpinning
            )

            ToolbarCookieItem(
                label = "Delete",
                icon = Icons.Rounded.Delete,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    onDeleteClick()
                },
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
            .padding(horizontal = 20.dp),
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
            delay(100L)
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
            isOnBlurredSurface = true,
            size = 48.dp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        // Text field with placeholder
        BasicTextField(
            state = searchTextFieldState,
            modifier = Modifier
                .weight(1f)
                .focusRequester(searchFocusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = GoogleSansFlexRound,
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
                    if (searchTextFieldState.text.isEmpty()) {
                        Text(
                            text = "Search",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GoogleSansFlexRound,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = GoogleSansFlexRound,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            )
                        )
                    }
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
                isOnBlurredSurface = true,
                size = 48.dp,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

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
    isFilterOpen: Boolean = false,
    onManageClick: () -> Unit,
    onFilterClick: () -> Unit,
    onMoreClick: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(
        LocalTiltActive provides true,
        LocalTiltModalFocus provides true,
        LocalIsOnBlurredSurface provides true
    ) {
        Column(
            modifier = modifier
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .widthIn(max = 320.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlurredSurface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { /* consume double tap on toolbar surface */ },
                            onLongPress = { /* consume long press on toolbar surface */ },
                            onTap = { /* consume tap on toolbar surface */ }
                        )
                    }
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
                            isSpinning = !isAnyPopupActive
                        )
                    } else if (searchMode == 1) {
                        ExpandedSearchContent(
                            viewModel = viewModel,
                            searchFocusRequester = searchFocusRequester,
                            onCloseOrBack = {
                                onSearchOpenChange(false)
                                focusManager.clearFocus()
                                scope.launch {
                                    delay(200L)
                                    viewModel.searchQuery = ""
                                }
                            },
                            isSpinning = true
                        )
                    } else {
                        // Normal Mode: Manage, Search, Filter and More with text labels
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Edit box
                            ToolbarCookieItem(
                                label = "Edit box",
                                icon = R.drawable.ic_box_edit,
                                isSpinning = !isAnyPopupActive,
                                onClick = onManageClick
                            )

                            // 2. Search
                            val isSearchActive = uiState.searchQuery.isNotEmpty()
                            val hasMatchingPhotos = remember(isSearchActive, flatPhotosRaw, currentTab) {
                                isSearchActive && flatPhotosRaw.any { photo ->
                                    PhotoSearchHelper.matchesTab(photo, currentTab)
                                }
                            }
                            val searchIcon = if (hasMatchingPhotos) ImageVector.vectorResource(R.drawable.ic_search_success) else Icons.Rounded.Search
                            ToolbarCookieItem(
                                label = "Search",
                                icon = searchIcon,
                                isSelected = isSearchActive,
                                isSpinning = !isAnyPopupActive,
                                onClick = { onSearchOpenChange(true) }
                            )

                            // 3. Snippets
                            val hasFilterSnippets = uiState.selectedFilterSnippets.isNotEmpty()
                            ToolbarCookieItem(
                                label = "Snippets",
                                icon = Icons.AutoMirrored.Filled.TextSnippet,
                                isSelected = isFilterOpen || hasFilterSnippets,
                                isSpinning = !isAnyPopupActive,
                                onClick = onFilterClick
                            )

                            // 4. More
                            ToolbarCookieItem(
                                label = "More",
                                icon = R.drawable.ic_more_horiz_filled,
                                isSpinning = !isAnyPopupActive,
                                onClick = onMoreClick
                            )
                        }
                    }
                }
            }
        }
    }
}
