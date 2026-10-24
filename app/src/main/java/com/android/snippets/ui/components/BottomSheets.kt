package com.android.snippets.ui.components

import kotlinx.coroutines.launch
import dev.chrisbanes.haze.HazeDefaults

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.Screen
import com.android.snippets.ui.MarqueeSelectIcon
import com.android.snippets.ui.CollectionIcon
import com.android.snippets.ui.MoreSectionIcon
import com.android.snippets.ui.ShapedSectionHeader
import com.android.snippets.ui.SettingsCardItem
import com.android.snippets.ui.CardPosition
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import com.android.snippets.model.Photo
import com.android.snippets.ui.PhotoMasonryItem
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.geometry.Rect
import com.android.snippets.ui.getSnippetTextStyle
import com.android.snippets.viewmodel.SnippetStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.util.Motion
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.snippets.ui.shapes.Cookie9Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.android.snippets.ui.theme.GoogleSansFlexRound

private val GrayscaleColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel,
    view: android.view.View
) {
    if (show) {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
        BlurredModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Motion.ExpressiveSheetContent {
                data class MenuItemInfo(
                    val label: String,
                    val icon: Any,
                    val onClick: () -> Unit
                )

                val marqueeIcon = MarqueeSelectIcon()
                val menuItems = remember(marqueeIcon) {
                    listOf(
                        MenuItemInfo(
                            label = "Create",
                            icon = com.ln.android.snippets.R.drawable.ic_create_new_folder,
                            onClick = {
                                onDismissRequest()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.navigateEmojiPicker(null)
                            }
                        ),
                        MenuItemInfo(
                            label = "Select",
                            icon = marqueeIcon,
                            onClick = {
                                onDismissRequest()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.forceSelectionMode = true
                            }
                        ),
                        MenuItemInfo(
                            label = "Stats",
                            icon = Icons.Rounded.BarChart,
                            onClick = {
                                onDismissRequest()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.navigateStats()
                            }
                        ),
                        MenuItemInfo(
                            label = "Settings",
                            icon = Icons.Rounded.Settings,
                            onClick = {
                                onDismissRequest()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.navigateSettings()
                            }
                        ),
                        MenuItemInfo(
                            label = "About",
                            icon = Icons.Rounded.Info,
                            onClick = {
                                onDismissRequest()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.navigateAbout()
                            }
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 22.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left column: Static Menu icon
                    Surface(
                        shape = com.android.snippets.ui.shapes.CookieShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Right column: Menu buttons (2 per row)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { clip = false },
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        menuItems.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { clip = false },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                rowItems.forEach { item ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .graphicsLayer { clip = false }
                                    ) {
                                        Box(
                                            modifier = Modifier.zIndex(10f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (val icon = item.icon) {
                                                is androidx.compose.ui.graphics.vector.ImageVector -> AnimatedCookieButton(
                                                    onClick = item.onClick,
                                                    icon = icon,
                                                    contentDescription = item.label,
                                                    size = 84.dp,
                                                    iconSize = 38.dp,
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                is Int -> AnimatedCookieButton(
                                                    onClick = item.onClick,
                                                    icon = icon,
                                                    contentDescription = item.label,
                                                    size = 84.dp,
                                                    iconSize = 38.dp,
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { item.onClick() }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = GoogleSansFlexRound
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionOptionsBottomSheet(
    collection: String,
    show: Boolean,
    onDismissRequest: () -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhotos: () -> Unit
) {
    if (show) {
        BlurredModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Motion.ExpressiveSheetContent {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShapedSectionHeader(icon = MoreSectionIcon())
                        Text(
                            text = "Action",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onAddPhotos()
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = RoundedCornerShape(12.dp)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onRemovePhotos()
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = RoundedCornerShape(12.dp)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel,
    view: android.view.View
) {
    if (show) {
        val curated = remember(viewModel.curatedMemories) {
            viewModel.curatedMemories.sortedByDescending { it.date }.take(10)
        }
        BlurredModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Motion.ExpressiveSheetContent {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (curated.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 22.dp, bottom = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = com.android.snippets.ui.shapes.CookieShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }

                            Surface(
                                shape = com.android.snippets.ui.shapes.Cookie9Shape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(84.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.HistoryToggleOff,
                                        contentDescription = "No memories",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        val monthFormat = remember { java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()) }
                        val yearFormat = remember { java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()) }
                        val currentPhoto = remember(curated) { curated.firstOrNull() }
                        val displayedMonth = remember(currentPhoto) {
                            currentPhoto?.let { monthFormat.format(java.util.Date(it.date)) } ?: ""
                        }
                        val displayedYear = remember(currentPhoto) {
                            currentPhoto?.let { yearFormat.format(java.util.Date(it.date)) } ?: ""
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 22.dp, bottom = 24.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Icon on top, month & year below icon
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top,
                                modifier = Modifier.width(48.dp)
                            ) {
                                Surface(
                                    shape = com.android.snippets.ui.shapes.CookieShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = displayedMonth,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GoogleSansFlexRound
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = displayedYear,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GoogleSansFlexRound
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // 3 memories per row
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer { clip = false },
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                curated.chunked(3).forEach { rowPhotos ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { clip = false },
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        rowPhotos.forEach { photo ->
                                            val itemIndex = curated.indexOf(photo)
                                            Surface(
                                                shape = com.android.snippets.ui.shapes.Cookie9Shape,
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier
                                                    .size(84.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                            viewModel.openMemory(curated, itemIndex)
                                                        }
                                                    )
                                            ) {
                                                val context = LocalContext.current
                                                val imageRequest = remember(photo.uriString) {
                                                    ImageRequest.Builder(context)
                                                        .data(photo.uriString)
                                                        .crossfade(true)
                                                        .memoryCacheKey(photo.uriString)
                                                        .build()
                                                }
                                                AsyncImage(
                                                    model = imageRequest,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    colorFilter = if (photo.isViewed) GrayscaleColorFilter else null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .then(if (photo.isViewed) Modifier.alpha(0.5f) else Modifier)
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
