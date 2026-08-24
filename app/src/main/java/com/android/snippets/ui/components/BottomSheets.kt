package com.android.snippets.ui.components

import kotlinx.coroutines.launch

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
import com.android.snippets.ui.SelectIcon
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
import com.android.snippets.ui.SelectIcon
import com.android.snippets.model.Photo
import com.android.snippets.ui.PhotoMasonryItem
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import com.android.snippets.ui.getSnippetTextStyle
import com.android.snippets.viewmodel.SnippetStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.util.Motion
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel,
    view: android.view.View
) {
    if (show) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrimColor = BottomSheetDefaults.ScrimColor
        ) {
            Motion.ExpressiveSheetContent {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = com.android.snippets.ui.shapes.CookieShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }
                            Text(
                                text = "Menu",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Row 1: Stats, Select, Templates
                        ButtonGroup(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            overflowIndicator = {}
                        ) {
                            menuItem(
                                icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "Stats",
                                weight = 0.9f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateStats()
                                }
                            )

                            menuItem(
                                icon = { Icon(SelectIcon(), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "Select",
                                weight = 0.9f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.forceSelectionMode = true
                                }
                            )

                            menuItem(
                                icon = { Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "Templates",
                                weight = 1.2f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateTemplates()
                                }
                            )
                        }

                        // Row 2: New Collection, Settings, About
                        ButtonGroup(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            overflowIndicator = {}
                        ) {
                            menuItem(
                                icon = { Icon(painterResource(id = com.ln.android.snippets.R.drawable.ic_create_new_folder), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "New Collection",
                                weight = 1.45f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateCollections(focusCreate = true)
                                }
                            )

                            menuItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "Settings",
                                weight = 0.95f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateSettings()
                                }
                            )

                            menuItem(
                                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = "About",
                                weight = 0.65f,
                                onClick = {
                                    onDismissRequest()
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.navigateAbout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ButtonGroupScope.menuItem(
    icon: @Composable () -> Unit,
    label: String,
    weight: Float = 1f,
    onClick: () -> Unit
) {
    customItem(
        buttonGroupContent = {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .weight(weight)
                    .height(48.dp),
                shapes = ButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = RoundedCornerShape(12.dp)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    icon()
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlex
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        menuContent = {
            DropdownMenuItem(
                text = { Text(label) },
                leadingIcon = icon,
                onClick = onClick
            )
        }
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionOptionsBottomSheet(
    collectionName: String,
    show: Boolean,
    onDismissRequest: () -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhotos: () -> Unit
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrimColor = BottomSheetDefaults.ScrimColor
        ) {
            Motion.ExpressiveSheetContent {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp)
                            .padding(bottom = 8.dp),
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
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrimColor = BottomSheetDefaults.ScrimColor
        ) {
            Motion.ExpressiveSheetContent {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = com.android.snippets.ui.shapes.CookieShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(24.dp))
                            }
                        }
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (curated.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = com.android.snippets.ui.shapes.CookieShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    border = androidx.compose.foundation.BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                ) {}
                                Icon(
                                    imageVector = com.android.snippets.ui.NoMemoriesIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val carouselState = rememberCarouselState(initialItem = 0) { curated.size }
                            val carouselItemShape = remember { RoundedCornerShape(24.dp) }
                            
                            val activeIndex = remember { derivedStateOf { carouselState.currentItem } }
                            val currentMemory = remember(curated) {
                                derivedStateOf { curated.getOrNull(activeIndex.value) }
                            }
                            
                            val dateText = remember {
                                derivedStateOf {
                                    currentMemory.value?.let { photo ->
                                        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                                        monthFormat.format(java.util.Date(photo.date))
                                    } ?: ""
                                }
                            }
                            
                            Text(
                                text = dateText.value,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                            )
                            
                            HorizontalUncontainedCarousel(
                                state = carouselState,
                                itemWidth = 186.dp,
                                itemSpacing = 8.dp,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) { itemIndex ->
                                val photo = curated[itemIndex]
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .maskClip(carouselItemShape)
                                ) {
                                    PhotoMasonryItem(
                                        photo = photo,
                                        isSelected = false,
                                        selectionMode = false,
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                            val unsortedIndex = viewModel.curatedMemories.indexOfFirst { it.id == photo.id }
                                            if (unsortedIndex != -1) {
                                                viewModel.openMemory(unsortedIndex)
                                            }
                                            onDismissRequest()
                                        },
                                        showFavoriteIcon = false,
                                        fillCard = true,
                                        grayOutIfViewed = true,
                                        shape = carouselItemShape,
                                        modifier = Modifier.fillMaxSize()
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
