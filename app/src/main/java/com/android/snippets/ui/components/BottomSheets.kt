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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 116.dp)
                        .padding(top = 8.dp, bottom = 44.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        val buttonSize = remember(maxWidth) {
                            if (maxWidth < 312.dp) {
                                ((maxWidth - 20.dp) / 6).coerceIn(40.dp, 48.dp)
                            } else {
                                48.dp
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Static icon
                            Surface(
                                shape = com.android.snippets.ui.shapes.CookieShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(buttonSize)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(buttonSize * 0.5f))
                                }
                            }

                            // 1. New Collection
                            AnimatedCookieButton(
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateEmojiPicker(null)
                                },
                                icon = com.ln.android.snippets.R.drawable.ic_create_new_folder,
                                contentDescription = "New Collection",
                                tooltip = "New Collection",
                                size = buttonSize,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // 2. Select
                            AnimatedCookieButton(
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.forceSelectionMode = true
                                },
                                icon = MarqueeSelectIcon(),
                                contentDescription = "Select",
                                tooltip = "Select",
                                size = buttonSize,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // 3. Stats
                            AnimatedCookieButton(
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateStats()
                                },
                                icon = Icons.Default.BarChart,
                                contentDescription = "Stats",
                                tooltip = "Stats",
                                size = buttonSize,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // 4. Settings
                            AnimatedCookieButton(
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateSettings()
                                },
                                icon = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tooltip = "Settings",
                                size = buttonSize,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // 5. About
                            AnimatedCookieButton(
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateAbout()
                                },
                                icon = Icons.Default.Info,
                                contentDescription = "About",
                                tooltip = "About",
                                size = buttonSize,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
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
                            Icon(Icons.Default.Remove, contentDescription = null)
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
            viewModel.curatedMemories.sortedByDescending { it.date }
        }
        BlurredModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Motion.ExpressiveSheetContent {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 116.dp)
                        .padding(top = 8.dp, bottom = 36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (curated.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
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
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }

                            Surface(
                                shape = com.android.snippets.ui.shapes.Cookie9Shape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(84.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HistoryToggleOff,
                                        contentDescription = "No memories",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        val lazyListState = rememberLazyListState()
                        val monthFormat = remember { java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()) }
                        val yearFormat = remember { java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()) }
                        val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
                        val currentPhoto = remember(curated, firstVisibleIndex) {
                            curated.getOrNull(firstVisibleIndex) ?: curated.firstOrNull()
                        }
                        val displayedMonth = remember(currentPhoto) {
                            currentPhoto?.let { monthFormat.format(java.util.Date(it.date)) } ?: ""
                        }
                        val displayedYear = remember(currentPhoto) {
                            currentPhoto?.let { yearFormat.format(java.util.Date(it.date)) } ?: ""
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Icon on top, month & year below icon
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.width(56.dp)
                            ) {
                                Surface(
                                    shape = com.android.snippets.ui.shapes.CookieShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = displayedMonth,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = com.android.snippets.ui.theme.GoogleSansFlex
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = displayedYear,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = com.android.snippets.ui.theme.GoogleSansFlex
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Static 84dp cookie9sided memory buttons in standard scrollable LazyRow
                            LazyRow(
                                state = lazyListState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(end = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(curated, key = { _, photo -> photo.id }) { itemIndex, photo ->
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
