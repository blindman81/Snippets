package com.android.snippets.ui.components
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import kotlinx.coroutines.launch


import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.automirrored.filled.Label
import com.android.snippets.viewmodel.SnippetsViewModel

import com.android.snippets.ui.theme.titleEmphasized

import com.android.snippets.ui.CardPosition
import com.android.snippets.ui.SettingsCardItem
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Calendar
import com.android.snippets.model.Photo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionToolbar(
    viewModel: SnippetsViewModel,
    onDeleteClick: () -> Unit,
    isSpinning: Boolean = true,
    isScrolled: Boolean = false
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedCookieButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.clearSelection()
                    },
                    icon = Icons.Default.Close,
                    contentDescription = "Clear Selection",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    size = 44.dp,
                    isSpinning = isSpinning
                )

                Text(
                    text = viewModel.selectedPhotoIds.size.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (viewModel.pendingCollectionAssignment != null || viewModel.pendingCollectionRemoval != null) {
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            if (viewModel.pendingCollectionAssignment != null) {
                                viewModel.confirmCollectionAssignment()
                            } else {
                                viewModel.confirmCollectionRemoval()
                            }
                        },
                        icon = Icons.Default.Check,
                        contentDescription = "Confirm",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 44.dp,
                        isSpinning = isSpinning
                    )
                } else {
                    AnimatedCookieButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                            onDeleteClick()
                        },
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 44.dp,
                        isSpinning = isSpinning
                    )
                }
            }
        }
    }
}




@Composable
fun FloatingTitlePill(
    title: String,
    icon: Any? = null,
    containerColor: Color = Color.Unspecified,
    isScrolled: Boolean = false,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    scrolledTitle: String? = null,
    hasPrimaryOutline: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    var isHolding by remember { mutableStateOf(false) }
    val animatedScaleState = animateFloatAsState(
        targetValue = if (isHolding) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 1200f),
        label = "pill_scale"
    )

    // Parse day and date statistics vs. photo count
    val parts = subtitle?.split(" · ") ?: emptyList()
    var dateText = ""
    var countText = ""
    if (parts.size >= 2) {
        if (parts[0].contains("photo")) {
            countText = parts[0]
            dateText = parts[1]
        } else {
            dateText = parts[0]
            countText = parts[1]
        }
    } else if (parts.size == 1) {
        dateText = parts[0]
    }

    // Format dateText to show only month and year (e.g., "May 2025")
    val monthYearText = if (dateText.isNotEmpty()) {
        try {
            // Attempt common date formats; fallback to original if parsing fails
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val parsed = parser.parse(dateText)
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(parsed)
        } catch (e: Exception) {
            try {
                val parser = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val parsed = parser.parse(dateText)
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(parsed)
            } catch (e2: Exception) {
                // If parsing fails, fall back to original text (could already be month-year)
                dateText
            }
        }
    } else ""

    val displayText = when {
        isScrolled && !scrolledTitle.isNullOrBlank() -> scrolledTitle
        isScrolled && title == "Library" && monthYearText.isNotEmpty() -> monthYearText
        else -> title
    }
    val textLength = displayText.length
    val targetTextMaxWidth = (textLength * 14).dp.coerceIn(140.dp, 380.dp)

    val textMaxWidth by animateDpAsState(
        targetValue = targetTextMaxWidth,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "textMaxWidth"
    )

    val verticalPadding by animateDpAsState(
        targetValue = if (subtitle != null) 16.dp else 20.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "vertical_padding"
    )

    val horizontalPadding = 36.dp

    val resolvedContainerColor = if (containerColor == Color.Unspecified) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f) else containerColor

    LaunchedEffect(isHolding) {
        if (isHolding) {
            launch {
                while (true) {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                    kotlinx.coroutines.delay(125)
                }
            }
        }
    }

    Surface(
        shape = CircleShape,
        color = resolvedContainerColor,
        shadowElevation = if (isScrolled) 6.dp else 2.dp,
        tonalElevation = if (isScrolled) 6.dp else 2.dp,
        border = if (hasPrimaryOutline) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) else null,
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScaleState.value
                scaleY = animatedScaleState.value
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(CircleShape)
            .let { mod ->
                if (onClick != null || onLongClick != null) {
                    mod.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isHolding = false
                                }
                            },
                            onTap = {
                                if (onClick != null) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onClick()
                                }
                            },
                            onLongPress = {
                                if (onLongClick != null) {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    onLongClick()
                                }
                            }
                        )
                    }
                } else {
                    mod
                }
            }
    ) {
        AnimatedContent(
            targetState = isScrolled,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)))
                    .togetherWith(fadeOut(animationSpec = tween(150)))
            },
            label = "pill_layout_transition"
        ) { scrolled ->
            if (scrolled) {
                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        if (icon is ImageVector) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else if (icon is String) {
                            Text(
                                text = icon,
                                style = titleEmphasized.copy(fontSize = 14.sp),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = displayText,
                        style = titleEmphasized.copy(
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = textMaxWidth)
                    )

                    val digitOnlyCount = countText.filter { it.isDigit() }
                    if (digitOnlyCount.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = digitOnlyCount,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        if (icon is ImageVector) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(if (subtitle != null) 32.dp else 28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        } else if (icon is String) {
                            Text(
                                text = icon,
                                style = titleEmphasized,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = title,
                            style = titleEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    viewModel: SnippetsViewModel? = null,
    title: String? = null,
    placeholder: String? = null,
    onNavigationClick: () -> Unit,
    navigationIcon: ImageVector = Icons.Default.Menu,
    isSpinning: Boolean = true,
    isScrolled: Boolean = false,
    showNavigationIcon: Boolean = true,
    showSearchIcon: Boolean = true,
    leftAlignTitle: Boolean = false,
    titleIcon: ImageVector? = null,
    forceTransparentBackground: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val view = LocalView.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    val targetHeaderColor = if (forceTransparentBackground) {
        Color.Transparent
    } else if (isScrolled) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val headerColor = remember { Animatable(targetHeaderColor) }
    
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(colorScheme) {
        headerColor.snapTo(targetHeaderColor)
    }
    
    LaunchedEffect(isScrolled) {
        headerColor.animateTo(targetHeaderColor, tween(250))
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = if (forceTransparentBackground) Color.Transparent else headerColor.value,
        shape = RectangleShape,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Navigation Button
                if (showNavigationIcon) {
                    AnimatedCookieButton(
                        onClick = {
                            focusManager.clearFocus()
                            onNavigationClick()
                        },
                        icon = navigationIcon,
                        contentDescription = "Navigation",
                        tooltip = if (navigationIcon == Icons.Default.Menu) "Menu" else "Back",
                        isSpinning = isSpinning
                    )
                    if (leftAlignTitle) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                } else if (leftAlignTitle) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Centered Title Text
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (leftAlignTitle) Alignment.CenterStart else Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = if (titleIcon != null) {
                            titleIcon
                        } else if (viewModel != null && title != null) {
                            if (viewModel.userCollections.any { it.equals(title, ignoreCase = true) }) {
                                viewModel.getCollectionIcon(title)
                            } else {
                                null
                            }
                        } else {
                            null
                        }

                        if (icon != null) {
                            if (icon is String) {
                                Text(
                                    text = icon,
                                    style = titleEmphasized,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else if (icon is ImageVector) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        Text(
                            text = title ?: "",
                            style = titleEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Right actions (Search icon button followed by select/other actions)
                Row(
                    modifier = Modifier.widthIn(min = 56.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showSearchIcon && viewModel != null) {
                        AnimatedCookieButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.isSearchViewOpen = true
                            },
                            icon = Icons.Default.Search,
                            contentDescription = "Search",
                            tooltip = "Search",
                            isSpinning = isSpinning
                        )
                    }
                    
                    if (actions != @Composable {}) {
                        Spacer(modifier = Modifier.width(8.dp))
                        actions()
                    }
                }
            }
            
            // Show progress if searching or filtering
            if (viewModel?.isSearching == true || viewModel?.isFiltering == true) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    title: String = "Library",
    navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = {}) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
    },
    actions: @Composable RowScope.() -> Unit = {
        val view = LocalView.current
        IconButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
            }
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    },
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchViewOverlay(
    viewModel: SnippetsViewModel,
    placeholder: String?
) {
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = { viewModel.isSearchViewOpen = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedCookieButton(
                        onClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            viewModel.isSearchViewOpen = false 
                        },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tooltip = "Back",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 56.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp) // Match Library height
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (viewModel.searchQuery.isEmpty()) {
                                            Text(
                                                text = placeholder ?: "Search",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    viewModel.addRecentSearch(viewModel.searchQuery)
                                    viewModel.isSearchViewOpen = false
                                    focusManager.clearFocus()
                                })
                            )

                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { 
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.searchQuery = "" 
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AnimatedCookieButton(
                        onClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.showClearHistoryDialog = true 
                        },
                        icon = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tooltip = "Clear History",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 56.dp
                    )
                }

                // Recent Searches or Suggestions
                if (viewModel.searchQuery.isEmpty()) {
                    Text(
                        text = "RECENT SEARCHES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        viewModel.recentSearches.forEachIndexed { index, search ->
                            val position = when {
                                viewModel.recentSearches.size == 1 -> CardPosition.Single
                                index == 0 -> CardPosition.First
                                index == viewModel.recentSearches.size - 1 -> CardPosition.Last
                                else -> CardPosition.Middle
                            }
                            
                            SettingsCardItem(
                                icon = Icons.Default.History,
                                title = search,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.searchQuery = search
                                    viewModel.addRecentSearch(search)
                                    viewModel.isSearchViewOpen = false
                                },
                                position = position,
                                trailingContent = {
                                    IconButton(
                                        onClick = { 
                                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            viewModel.removeRecentSearch(search) 
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close, 
                                            contentDescription = "Remove", 
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Suggestions logic could go here if needed, but for now we'll just show the filtered results in the background
                    // Actually, the user asked for suggestions when typing.
                    if (viewModel.searchSuggestions.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(viewModel.searchSuggestions) { index, (name, isCollection) ->
                                val position = when {
                                    viewModel.searchSuggestions.size == 1 -> CardPosition.Single
                                    index == 0 -> CardPosition.First
                                    index == viewModel.searchSuggestions.size - 1 -> CardPosition.Last
                                    else -> CardPosition.Middle
                                }
                                
                                SettingsCardItem(
                                    icon = if (isCollection) Icons.Default.Folder else Icons.AutoMirrored.Filled.Label,
                                    title = name,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.searchQuery = name
                                        viewModel.addRecentSearch(name)
                                        viewModel.isSearchViewOpen = false
                                    },
                                    position = position
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearHistoryDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { 
                Text(
                    "Clear search history?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                ) 
            },
            text = { 
                Text(
                    "This will remove all your recent searches.",
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearRecentSearches()
                        viewModel.showClearHistoryDialog = false
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = CircleShape,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Clear All", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.showClearHistoryDialog = false 
                }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

fun getStatsSubtext(photos: List<Photo>): String? {
    if (photos.isEmpty()) return null
    val count = photos.size
    val photoNoun = if (count == 1) "photo" else "photos"

    val minDate = photos.minOf { it.date }
    val maxDate = photos.maxOf { it.date }

    val sdfDay = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val sdfDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val minCal = Calendar.getInstance().apply { timeInMillis = minDate }
    val maxCal = Calendar.getInstance().apply { timeInMillis = maxDate }

    val isSameDay = minCal.get(Calendar.YEAR) == maxCal.get(Calendar.YEAR) &&
            minCal.get(Calendar.DAY_OF_YEAR) == maxCal.get(Calendar.DAY_OF_YEAR)

    return if (isSameDay) {
        "${sdfDay.format(Date(minDate))} · $count $photoNoun"
    } else {
        "$count $photoNoun · ${sdfDate.format(Date(minDate))} - ${sdfDate.format(Date(maxDate))}"
    }
}

