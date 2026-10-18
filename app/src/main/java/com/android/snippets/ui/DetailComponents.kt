package com.android.snippets.ui
import com.ln.android.snippets.R
import androidx.compose.ui.res.painterResource
import com.android.snippets.ui.components.*
import com.android.snippets.ui.theme.*
import dev.chrisbanes.haze.HazeDefaults
import com.android.snippets.ui.util.Motion
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.android.snippets.model.Photo
import com.android.snippets.ui.util.LocationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.snippets.ui.util.rotateWithBounds
import com.android.snippets.viewmodel.SnippetsViewModel
import kotlin.random.Random
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.Cookie9Shape
import androidx.compose.ui.graphics.luminance

// --- SHARED ARTISTIC COMPONENTS ---

@Composable
fun DetailTopBar(
    photo: com.android.snippets.model.Photo,
    viewModel: SnippetsViewModel,
    onBack: () -> Unit,
    isSpinning: Boolean = true,
    isScrolled: Boolean = false,
    onPhotoThumbnailClick: () -> Unit = {},
    hasSnippets: Boolean,
    showAddButton: Boolean = true,
    onAdd: () -> Unit,
    onDownload: () -> Unit,
    onSnippets: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRate: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    hasLocationLink: Boolean = false,
    onAddLinkClick: () -> Unit = {},
    onNamePhotoClick: () -> Unit = {},
    isInfoExpanded: Boolean = false,
    onToggleInfoExpand: () -> Unit = {},
    onOpenLink: (String) -> Unit = {},
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null
) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RectangleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .then(
                    if (animatedVisibilityScope != null) {
                        with(animatedVisibilityScope) {
                            val motionScheme = MaterialTheme.motionScheme
                            Modifier.animateEnterExit(
                                enter = fadeIn(animationSpec = motionScheme.fastEffectsSpec()) + slideInVertically(animationSpec = motionScheme.fastSpatialSpec()) { -it / 2 },
                                exit = fadeOut(animationSpec = motionScheme.fastEffectsSpec()) + slideOutVertically(animationSpec = motionScheme.fastSpatialSpec()) { -it / 2 }
                            )
                        }
                    } else Modifier
                )
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedCookieButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    isSpinning = isSpinning,
                    hapticOnHold = true,
                    shape = CookieShape,
                    size = 48.dp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            DetailPhotoInfoSection(
                photo = photo,
                isExpanded = isInfoExpanded,
                onToggleExpand = onToggleInfoExpand,
                onOpenLink = onOpenLink,
                onRateClick = onRate,
                onEditLinkClick = onAddLinkClick,
                onNameClick = onNamePhotoClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (showAddButton) {
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    SplitButton(
                        primaryIcon = Icons.Default.Add,
                        contentDescription = "Add snippets",
                        onPrimaryClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onAdd()
                        },
                dropdownContent = { closeMenu ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Group 1: Download, Share, Favorite
                        DropdownMenuItem(
                            text = { Text("Download") },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) },
                            enabled = hasSnippets,
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onDownload(); closeMenu() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            enabled = hasSnippets,
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onShare(); closeMenu() }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "Unfavorite" else "Favorite") },
                            leadingIcon = { Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) },
                            enabled = hasSnippets,
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onToggleFavorite(); closeMenu() }
                        )

                        HorizontalDivider()

                        // Name photo
                        DropdownMenuItem(
                            text = { Text(if (!photo.name.isNullOrBlank()) "Rename photo" else "Name photo") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) },
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onNamePhotoClick(); closeMenu() }
                        )

                        // Group 2: Location link
                        DropdownMenuItem(
                            text = { Text(if (hasLocationLink) "Edit location link" else "Link to a place") },
                            leadingIcon = { Icon(Icons.Default.Place, null) },
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onAddLinkClick(); closeMenu() }
                        )

                        DropdownMenuItem(
                            text = { Text("Rate") },
                            leadingIcon = { Icon(painterResource(id = R.drawable.ic_star_rating), null) },
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onRate(); closeMenu() }
                        )

                        if (hasSnippets) {
                            DropdownMenuItem(
                                text = { Text("Snippets") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.TextSnippet, null) },
                                onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onSnippets(); closeMenu() }
                            )
                        }

                        HorizontalDivider()

                        // Group 3: Delete
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onDelete(); closeMenu() }
                        )
                    }
                }
            )
        }
    }
}
    }
}

@Composable
fun SwipePrompt(modifier: Modifier = Modifier, alphaValue: Float = 0.6f) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceOffset by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = 12.dp,
        typeConverter = androidx.compose.ui.unit.Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.graphicsLayer { alpha = alphaValue.coerceIn(0f, 1f) }
    ) {
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(36.dp).offset(y = bounceOffset)
        )
        Text(
            text = "SWIPE TO SEE SNIPPETS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun getSnippetTextStyle(
    style: com.android.snippets.viewmodel.SnippetStyle = com.android.snippets.viewmodel.SnippetStyle.Default,
    base: androidx.compose.ui.text.TextStyle,
    isCloud: Boolean = false
): androidx.compose.ui.text.TextStyle {
    return base.copy(
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
    )
}

@Composable
fun CloudSnippetItem(
    text: String, 
    index: Int, 
    totalCount: Int, 
    photoColors: List<Int> = emptyList(),
    forcedColor: Int? = null,
    forcedStyle: com.android.snippets.viewmodel.SnippetStyle? = null,
    isSegmented: Boolean = false
) {
    val stableRandom = remember(text) { Random(text.hashCode()) }
    val personality = stableRandom.nextInt(0, 5)
    val colorStrategy = (index + stableRandom.nextInt(0, 10)) % 3
    val rotation = if (isSegmented) 0f else stableRandom.nextInt(-5, 5).toFloat()

    // Contrast-aware Primary Wallpaper Tinted Snippet Color
    val snippetColor = rememberSnippetBackgroundColor()
    val snippetTextColor = rememberSnippetTextColor()

    // Streamlined style logic: All snippets are fill colored
    val isFilled = true

    val containerColor = snippetColor

    val itemShape = remember(index, totalCount, isSegmented) {
        if (isSegmented) {
            if (totalCount <= 1) {
                RoundedCornerShape(16.dp)
            } else if (index == 0) {
                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 2.dp, bottomEnd = 2.dp)
            } else if (index == totalCount - 1) {
                RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 16.dp, bottomEnd = 16.dp)
            } else {
                RoundedCornerShape(2.dp)
            }
        } else {
            CircleShape
        }
    }

    val itemContent = @Composable {
        Surface(
            modifier = if (isSegmented) Modifier.height(44.dp) else Modifier,
            color = containerColor,
            shape = itemShape,
            border = null,
            shadowElevation = 0.dp
        ) {
            val scalingFactor = com.android.snippets.ui.util.DistributionMath.getGridScalingFactor(totalCount)
    
            if (isSegmented) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text, 
                        modifier = Modifier.padding(horizontal = 16.dp), 
                        style = getSnippetTextStyle(
                            forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, 
                            MaterialTheme.typography.titleMedium, 
                            isCloud = true
                        ),
                        color = snippetTextColor
                    )
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    // Streamlined into 3 visual levels (Large, Medium, Small)
                    when (personality) {
                        0, 1 -> { // Large Chip
                            Text(
                                text = text, 
                                modifier = Modifier.padding(horizontal = (24 * scalingFactor).dp, vertical = (12 * scalingFactor).dp), 
                                style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.headlineMedium, isCloud = true).copy(fontSize = (MaterialTheme.typography.headlineMedium.fontSize.value * scalingFactor).sp),
                                color = snippetTextColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        2, 3 -> { // Medium Chip
                            Text(
                                text = text, 
                                modifier = Modifier.padding(horizontal = (18 * scalingFactor).dp, vertical = (9 * scalingFactor).dp), 
                                style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.titleLarge, isCloud = true).copy(fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * scalingFactor).sp),
                                color = snippetTextColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        else -> { // Small Chip
                            Text(
                                text = text, 
                                modifier = Modifier.padding(horizontal = (12 * scalingFactor).dp, vertical = (6 * scalingFactor).dp), 
                                style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.labelLarge, isCloud = true).copy(fontSize = (MaterialTheme.typography.labelLarge.fontSize.value * scalingFactor).sp),
                                color = snippetTextColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (isSegmented) {
        itemContent()
    } else {
        Box(modifier = Modifier.rotateWithBounds(rotation)) {
            itemContent()
        }
    }
}

@Composable
fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    AnimatedCookieButton(
        onClick = onClick,
        icon = icon,
        contentColor = tint
    )
}

@Composable
fun HeaderActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.size(44.dp), 
        shape = CircleShape, 
        color = Color.Transparent, 
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick()
        }
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp)) }
    }
}

// --- ACTIVE STATE WRITING HUB ---

@Composable
fun SnippetsModal(
    photo: com.android.snippets.model.Photo,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    var localSnippets by remember(photo.snippets) { mutableStateOf(photo.snippets) }
    val view = LocalView.current

    BlurredAlertDialog(
        onDismissRequest = onClose,
        onConfirm = onClose,
        title = {
            Text(
                "Snippets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (localSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No snippets yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localSnippets.forEach { snippet ->
                        val forcedStyle = viewModel.getSnippetStyle(snippet)
                        val snippetColor = rememberSnippetBackgroundColor()
                        val bgColor = snippetColor
                        val textColor = rememberSnippetTextColor()
                        
                        Surface(shape = CircleShape, color = bgColor, border = null) {
                            Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = snippet, 
                                    style = getSnippetTextStyle(
                                        forcedStyle, 
                                        MaterialTheme.typography.titleMedium, 
                                        isCloud = true
                                    ), 
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { 
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    onRemove(snippet)
                                    localSnippets = localSnippets - snippet 
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = textColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
 
@Composable
private fun SpinningCookieButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    shape: androidx.compose.ui.graphics.Shape = Cookie9Shape,
    containerColor: Color,
    border: BorderStroke? = null,
    keepContentUpright: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                coroutineScope.launch {
                    val scaleJob = launch {
                        scale.animateTo(0.85f, Motion.PressSpring)
                        scale.animateTo(1f, Motion.BouncySpring)
                    }
                    val rotJob = launch {
                        rotation.animateTo(rotation.value + 360f, Motion.BouncySpring)
                    }
                    scaleJob.join()
                    rotJob.join()
                }
                onClick()
            }
    ) {
        Surface(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = scale.value
                    scaleY = scale.value
                },
            shape = shape,
            color = containerColor,
            border = border
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (keepContentUpright) {
                            Modifier.graphicsLayer {
                                rotationZ = -rotation.value
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onAdd: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf("") }
    val localSnippetsCount = photo.snippets.size

    BlurredModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Motion.ExpressiveSheetContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Add a Snippet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { if (it.length <= 15) text = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            label = { Text("New Snippet", style = MaterialTheme.typography.labelMedium) },
                            placeholder = { Text("Type here...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            )
                        )
                        Text(
                            text = "${text.length}/15",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (text.length >= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 16.dp)
                        )
                    }

                    // Suggestions Menu
                    val suggestions = remember(text, viewModel.allUniqueSnippets) {
                        if (text.isEmpty()) {
                            viewModel.allUniqueSnippets.take(9)
                        } else {
                            viewModel.allUniqueSnippets.filter { 
                                it.contains(text, ignoreCase = true) && !it.equals(text, ignoreCase = true)
                            }.take(9)
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = suggestions.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = { 
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        text = suggestion 
                                    },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    shape = SuggestionChipDefaults.shape,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onAdd(text.trim())
                        onClose()
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = RoundedCornerShape(12.dp)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    ),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "Add Snippet ($localSnippetsCount/6)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


enum class CanvasAction { DOWNLOAD, SHARE }

@Composable
fun CanvasBackgroundDialog(
    action: CanvasAction,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    val view = LocalView.current
    var isDarkSelected by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon at the top
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Choose a background",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Light Option
                    CanvasOptionCard(
                        title = "Light",
                        isDark = false,
                        isSelected = !isDarkSelected,
                        onClick = { 
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            isDarkSelected = false 
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Dark Option
                    CanvasOptionCard(
                        title = "Dark",
                        isDark = true,
                        isSelected = isDarkSelected,
                        onClick = { 
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            isDarkSelected = true 
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Button
                val buttonText = if (action == CanvasAction.DOWNLOAD) "Download" else "Share"
                val buttonIcon = if (action == CanvasAction.DOWNLOAD) Icons.Default.FileDownload else Icons.Default.Share

                Button(
                    onClick = { 
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                        onConfirm(isDarkSelected) 
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = buttonIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(buttonText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun CanvasOptionCard(
    title: String,
    isDark: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                CookieCheckmark(
                    checked = isSelected,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isDark) Color(0xFF121212) else Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp, 
                        if (isDark) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Mock photo preview
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun DetailPhotoInfoSection(
    photo: Photo,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenLink: (String) -> Unit,
    onRateClick: () -> Unit,
    onEditLinkClick: () -> Unit,
    onNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasCustomName = !photo.name.isNullOrBlank()

    val dateTimeText = remember(photo.date) {
        val d = Date(photo.date)
        val day = SimpleDateFormat("EEE", Locale.getDefault()).format(d)
        val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(d)
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(d)
        val time = android.text.format.DateFormat.getTimeFormat(context).format(d)
        "$day, $dayNum $month, $time"
    }

    val dateTimeRatingText = remember(dateTimeText, photo.rating) {
        if (photo.rating > 0) "$dateTimeText. ${photo.rating}/5" else dateTimeText
    }

    // If no photo name was given, show day, date and rating in the header
    val headerTitle = if (hasCustomName) photo.name!! else dateTimeRatingText

    val locationName = remember(photo.locationName, photo.locationLink, photo.id) {
        LocationUtils.getLocationFromExif(context, photo)
    }

    data class InfoSubItem(
        val id: String,
        val title: String,
        val subtitle: String? = null,
        val onClick: (() -> Unit)? = null
    )

    val expandedItems = remember(photo.id, photo.date, photo.rating, locationName, photo.locationLink, dateTimeRatingText, hasCustomName) {
        val list = mutableListOf<InfoSubItem>()

        if (hasCustomName) {
            // Day, date and month, time. 2/5
            list.add(
                InfoSubItem(
                    id = "date_rating",
                    title = dateTimeRatingText,
                    subtitle = null,
                    onClick = onRateClick
                )
            )
        }

        // Location of the photo (skip if not entered, no subtext)
        if (!locationName.isNullOrBlank()) {
            val hasLink = !photo.locationLink.isNullOrBlank()
            list.add(
                InfoSubItem(
                    id = "location",
                    title = locationName,
                    subtitle = null,
                    onClick = {
                        if (hasLink) {
                            onOpenLink(photo.locationLink!!)
                        } else {
                            onEditLinkClick()
                        }
                    }
                )
            )
        }

        list
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expandCollapseRotation"
    )

    val headerTopRadius by animateDpAsState(targetValue = 12.dp, label = "headerTopRadius")
    val headerBottomRadius by animateDpAsState(
        targetValue = if (isExpanded && expandedItems.isNotEmpty()) 2.dp else 12.dp,
        label = "headerBottomRadius"
    )
    val headerShape = RoundedCornerShape(
        topStart = headerTopRadius,
        topEnd = headerTopRadius,
        bottomStart = headerBottomRadius,
        bottomEnd = headerBottomRadius
    )

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        // 1) Info material icon symbol: Photo name - expand / collapse button
        Surface(
            shape = headerShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .clickable(onClick = onToggleExpand)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Photo info",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotationAngle)
                    )
                }
            }
        }

        // Sub-items with visible 2dp segmented gaps
        AnimatedVisibility(
            visible = isExpanded && expandedItems.isNotEmpty(),
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                expandedItems.forEachIndexed { index, item ->
                    val isFirst = index == 0
                    val isLast = index == expandedItems.lastIndex
                    val itemShape = when {
                        isFirst && isLast -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                        isFirst -> RoundedCornerShape(2.dp)
                        isLast -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                        else -> RoundedCornerShape(2.dp)
                    }

                    Surface(
                        shape = itemShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(itemShape)
                            .then(
                                if (item.onClick != null) {
                                    Modifier.clickable(onClick = item.onClick)
                                } else Modifier
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}



