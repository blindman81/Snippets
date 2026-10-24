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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryToolbar(
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
    onMenuClick: () -> Unit,
    onManageClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFilterClick: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    BlurredSurface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .safeDrawingPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .widthIn(max = 500.dp)
            .fillMaxWidth()
            .height(84.dp)
            .graphicsLayer {
                translationY = cookieToolbarOffset.toPx()
                scaleX = cookieToolbarScale
                scaleY = cookieToolbarScale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            }
    ) {
        val motionScheme = MaterialTheme.motionScheme
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
                // Search Mode
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close / back
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            onSearchOpenChange(false)
                            viewModel.searchQuery = ""
                        },
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Close Search",
                        tooltip = "Close",
                        isSpinning = true,
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        onKeyboardAction = {
                            onSearchOpenChange(false)
                            focusManager.clearFocus()
                        },
                        decorator = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (viewModel.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search...",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            isSpinning = true,
                            size = 48.dp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                // Normal Mode
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
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
                        contentDescription = "Menu",
                        isSpinning = !isAnyPopupActive,
                        size = 48.dp,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Search
                    val isSearchActive = viewModel.searchQuery.isNotEmpty()
                    val searchIcon = if (isSearchActive && flatPhotosRaw.any { photo ->
                        when (currentTab) {
                            "Library" -> true
                            "Favorites" -> photo.isFavorite
                            else -> photo.collections.contains(currentTab)
                        }
                    }) SearchSuccessIcon() else Icons.Rounded.Search
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onSearchOpenChange(true)
                        },
                        icon = searchIcon,
                        contentDescription = "Search",
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
                        icon = ImageVector.vectorResource(id = R.drawable.ic_folder_managed),
                        contentDescription = "Manage",
                        isSpinning = !isAnyPopupActive,
                        size = 48.dp,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // History
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onHistoryClick()
                        },
                        icon = Icons.Rounded.History,
                        contentDescription = "History",
                        isSpinning = !isAnyPopupActive,
                        size = 48.dp,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        badge = {
                            // Unviewed-memories indicator dot
                            if (viewModel.hasUnviewedMemories) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    )

                    // Filters
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onFilterClick()
                        },
                        icon = Icons.Rounded.FilterList,
                        contentDescription = "Filters",
                        isSpinning = !isAnyPopupActive,
                        size = 48.dp,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
