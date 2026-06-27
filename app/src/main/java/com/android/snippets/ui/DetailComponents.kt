package com.android.snippets.ui
import com.android.snippets.ui.components.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.rotateWithBounds
import com.android.snippets.viewmodel.SnippetsViewModel
import kotlin.random.Random
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

val CookieShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val radius = kotlin.math.min(width, height) / 2f
    val polygon = RoundedPolygon.star(
        numVerticesPerRadius = 12,
        radius = radius,
        innerRadius = radius * 0.88f,
        rounding = CornerRounding(radius * 0.12f),
        centerX = width / 2f,
        centerY = height / 2f
    )
    addPath(polygon.toPath().asComposePath())
}

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
    onAdd: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
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
                .padding(start = 16.dp, end = 24.dp, top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedCookieButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tooltip = "Back",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    isSpinning = isSpinning,
                    hapticOnHold = true,
                    shape = CookieShape,
                    size = 56.dp
                )
            }

            SplitButton(
                primaryIcon = Icons.Default.Add,
                primaryText = "Add snippets",
                onPrimaryClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onAdd()
                },
                dropdownContent = { closeMenu ->
                    val menuGroupShape = RoundedCornerShape(12.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Group 1: Download, Share, Favorite
                        Surface(
                            shape = menuGroupShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.clip(menuGroupShape)
                        ) {
                            Column {
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
                            }
                        }

                        // Group 2: Edit & Delete
                        Surface(
                            shape = menuGroupShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.clip(menuGroupShape)
                        ) {
                            Column {
                                if (hasSnippets) {
                                    DropdownMenuItem(
                                        text = { Text("Edit snippets") },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                                        onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onEdit(); closeMenu() }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onDelete(); closeMenu() }
                                )
                            }
                        }
                    }
                }
            )
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
    style: com.android.snippets.viewmodel.SnippetStyle,
    base: androidx.compose.ui.text.TextStyle,
    isCloud: Boolean = false
): androidx.compose.ui.text.TextStyle {
    return when (style) {
        com.android.snippets.viewmodel.SnippetStyle.Thin -> base.copy(
            fontWeight = FontWeight.Thin,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
        )
        com.android.snippets.viewmodel.SnippetStyle.Cursive -> base.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive
        )
        com.android.snippets.viewmodel.SnippetStyle.Mono -> base.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        com.android.snippets.viewmodel.SnippetStyle.Serif -> base.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
        )
        com.android.snippets.viewmodel.SnippetStyle.Spaced -> base.copy(
            letterSpacing = if (isCloud) 1.5.sp else 4.sp,
            fontWeight = FontWeight.Bold
        )
        com.android.snippets.viewmodel.SnippetStyle.Bold -> base.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily(typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL))
        )
        com.android.snippets.viewmodel.SnippetStyle.FlexHeavy -> base.copy(
            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexHeavy
        )
        com.android.snippets.viewmodel.SnippetStyle.FlexWide -> base.copy(
            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
        )
        com.android.snippets.viewmodel.SnippetStyle.FlexSlant -> base.copy(
            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexSlant
        )
        com.android.snippets.viewmodel.SnippetStyle.FlexGrade -> base.copy(
            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexGrade
        )
        else -> base
    }
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

    // Contrast-aware Snippet Color
    val isDark = !MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue > 1.5f }
    val baseSnippetColor = if (forcedColor != null) {
        Color(forcedColor)
    } else {
        when (colorStrategy) {
            0 -> { // 1. Photo Derived
                if (photoColors.isNotEmpty()) Color(photoColors[stableRandom.nextInt(photoColors.size)])
                else MaterialTheme.colorScheme.primary
            }
            1 -> { // 2. System Themed
                val themeColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                themeColors[stableRandom.nextInt(themeColors.size)]
            }
            else -> { // 3. Vivid Pop
                val vividColors = if (isDark) listOf(
                    Color(0xFFFF8A65), // Light Deep Orange
                    Color(0xFFF06292), // Light Pink
                    Color(0xFFBA68C8), // Light Purple
                    Color(0xFF4DD0E1), // Light Cyan
                    Color(0xFF81C784), // Light Green
                    Color(0xFFFFD54F)  // Light Amber
                ) else listOf(
                    Color(0xFFD84315), // Dark Deep Orange
                    Color(0xFFC2185B), // Dark Pink
                    Color(0xFF7B1FA2), // Dark Purple
                    Color(0xFF0097A7), // Dark Cyan
                    Color(0xFF388E3C), // Dark Green
                    Color(0xFFFFA000)  // Dark Amber
                )
                vividColors[stableRandom.nextInt(vividColors.size)]
            }
        }
    }

    // Ensure it's not too close to black/white clashing with the theme
    val snippetColor = remember(baseSnippetColor, isDark) {
        val lum = baseSnippetColor.let { 0.299f * it.red + 0.587f * it.green + 0.114f * it.blue }
        if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
        else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
        else baseSnippetColor
    }

    // Streamlined style logic: All snippets are fill colored
    val isFilled = true

    val containerColor = if (isSegmented) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else if (isFilled) {
        snippetColor.copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }
    
    val borderColor = if (isSegmented) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    } else if (!isFilled) {
        snippetColor.copy(alpha = 0.40f)
    } else {
        null
    }

    val itemShape = remember(index, totalCount, isSegmented) {
        if (isSegmented) {
            if (totalCount <= 1) {
                RoundedCornerShape(16.dp)
            } else if (index == 0) {
                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp)
            } else if (index == totalCount - 1) {
                RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp)
            } else {
                RectangleShape
            }
        } else {
            CircleShape
        }
    }

    Box(modifier = Modifier.rotateWithBounds(rotation)) {
        Surface(
            color = containerColor,
            shape = itemShape,
            border = if (borderColor != null) BorderStroke(1.dp, borderColor) else null,
            shadowElevation = if (isSegmented) 4.dp else 0.dp
        ) {
            val scalingFactor = com.android.snippets.ui.util.DistributionMath.getGridScalingFactor(totalCount)
    
            if (isSegmented) {
                Text(
                    text = text, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), 
                    style = getSnippetTextStyle(
                        forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, 
                        MaterialTheme.typography.titleMedium, 
                        isCloud = true
                    ), 
                    color = snippetColor
                )
            } else {
                // Streamlined into 3 visual levels (Large, Medium, Small)
                when (personality) {
                    0, 1 -> { // Large Chip
                        Text(
                            text = text, 
                            modifier = Modifier.padding(horizontal = (24 * scalingFactor).dp, vertical = (12 * scalingFactor).dp), 
                            style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.headlineMedium, isCloud = true).copy(fontSize = (MaterialTheme.typography.headlineMedium.fontSize.value * scalingFactor).sp), 
                            color = snippetColor
                        )
                    }
                    2, 3 -> { // Medium Chip
                        Text(
                            text = text, 
                            modifier = Modifier.padding(horizontal = (18 * scalingFactor).dp, vertical = (9 * scalingFactor).dp), 
                            style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.titleLarge, isCloud = true).copy(fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * scalingFactor).sp), 
                            color = snippetColor
                        )
                    }
                    else -> { // Small Chip
                        Text(
                            text = text, 
                            modifier = Modifier.padding(horizontal = (12 * scalingFactor).dp, vertical = (6 * scalingFactor).dp), 
                            style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.labelLarge, isCloud = true).copy(fontSize = (MaterialTheme.typography.labelLarge.fontSize.value * scalingFactor).sp), 
                            color = snippetColor
                        )
                    }
                }
            }
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
fun CurrentSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    var localSnippets by remember(photo.snippets) { mutableStateOf(photo.snippets) }
    val view = LocalView.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    onClose()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        var animateScale by remember { mutableStateOf(0.92f) }
        LaunchedEffect(Unit) {
            animateScale = 1.0f
        }
        val scaleFactor by animateFloatAsState(
            targetValue = animateScale,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            label = "card_scale"
        )

        Surface(
            shape = RoundedCornerShape(48.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .widthIn(max = 400.dp)
                .graphicsLayer {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Stop click propagation to the tappable background overlay */ },
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Snippets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    AnimatedCookieButton(
                         onClick = onClose,
                         icon = Icons.Default.Close,
                         contentDescription = "Close",
                         tooltip = "Close",
                         size = 40.dp,
                         containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                         contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                
                // Content
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    if (localSnippets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            Text("No snippets yet", style = MaterialTheme.typography.bodyMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    } else {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            localSnippets.forEach { snippet ->
                                val forcedColor = viewModel.getSnippetColor(snippet)
                                val isDark = !MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue > 1.5f }
                                
                                val snippetColor = if (forcedColor != null) {
                                    val baseColor = Color(forcedColor)
                                    // Basic contrast awareness
                                    val lum = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
                                    if (isDark && lum < 0.3f) baseColor.copy(red = (baseColor.red + 0.4f).coerceAtMost(1f), green = (baseColor.green + 0.4f).coerceAtMost(1f), blue = (baseColor.blue + 0.4f).coerceAtMost(1f))
                                    else if (!isDark && lum > 0.7f) baseColor.copy(red = (baseColor.red - 0.4f).coerceAtLeast(0f), green = (baseColor.green - 0.4f).coerceAtLeast(0f), blue = (baseColor.blue - 0.4f).coerceAtLeast(0f))
                                    else baseColor
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }

                                val bgColor = snippetColor.copy(alpha = 0.12f)
                                val textColor = snippetColor
                                val borderColor = snippetColor.copy(alpha = 0.3f)
                                
                                Surface(shape = CircleShape, color = bgColor, border = BorderStroke(1.dp, borderColor)) {
                                    Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(snippet, style = getSnippetTextStyle(viewModel.getSnippetStyle(snippet), MaterialTheme.typography.bodyMedium), fontWeight = FontWeight.Normal, color = textColor)
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
                
                // Footer
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("Done", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onAdd: (String, Int, com.android.snippets.viewmodel.SnippetStyle) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
    var selectedStyle by remember { mutableStateOf(com.android.snippets.viewmodel.SnippetStyle.Default) }

    var selectedIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val options = listOf("Text", "Color", "Style")
    
    val snippetColorsPalette = remember {
        listOf(
            0xFFEF5350.toInt(), // Red
            0xFFEC407A.toInt(), // Pink
            0xFFAB47BC.toInt(), // Purple
            0xFF42A5F5.toInt(), // Blue
            0xFF26A69A.toInt(), // Teal
            0xFF66BB6A.toInt(), // Green
            0xFFFFEE58.toInt(), // Yellow
            0xFFFFA726.toInt(), // Orange
            0xFF8D6E63.toInt(), // Brown
            0xFF78909C.toInt(), // Slate
            0xFFD4E157.toInt()  // Lime
        )
    }

    val localSnippetsCount = photo.snippets.size

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = BottomSheetDefaults.ScrimColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add a Snippet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ButtonGroup(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    overflowIndicator = {}
                ) {
                    options.forEachIndexed { index, label ->
                        toggleableItem(
                            weight = 1f,
                            checked = index == selectedIndex,
                            onCheckedChange = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                selectedIndex = index
                            },
                            label = label
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    val motionScheme = MaterialTheme.motionScheme
                    androidx.compose.animation.AnimatedContent(
                        targetState = selectedIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = motionScheme.defaultEffectsSpec()) togetherWith
                            fadeOut(animationSpec = motionScheme.fastEffectsSpec()) using
                            SizeTransform(clip = false)
                        },
                        label = "StepTransition"
                    ) { step ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (step) {
                                0 -> { // Text
                                    OutlinedTextField(
                                        value = text,
                                        onValueChange = { if (it.length <= 10) text = it },
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
                                        supportingText = {
                                            Text(
                                                text = "${text.length}/10",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.End,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (text.length >= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            }
                                        )
                                    )

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
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            suggestions.chunked(3).take(3).forEach { rowItems ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    rowItems.forEach { suggestion ->
                                                        SuggestionChip(
                                                            onClick = { 
                                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                                text = suggestion 
                                                            },
                                                            label = { Text(suggestion, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                                            shape = CircleShape,
                                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                            ),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> { // Color Step
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        modifier = Modifier.fillMaxWidth().height(260.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(8.dp),
                                        userScrollEnabled = true
                                    ) {
                                        item {
                                            val isRandomSelected = selectedColor == -1
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                        selectedColor = if (isRandomSelected) null else -1
                                                    }
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(60.dp).aspectRatio(1f),
                                                    shape = CircleShape,
                                                    color = if (isRandomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                    border = if (isRandomSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Shuffle,
                                                            contentDescription = "Shuffle",
                                                            modifier = Modifier.size(28.dp),
                                                            tint = if (isRandomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        items(snippetColorsPalette) { colorInt ->
                                            val isSelected = selectedColor == colorInt
                                            val color = Color(colorInt)
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                        selectedColor = if (isSelected) null else colorInt
                                                    }
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(if (isSelected) 60.dp else 52.dp).aspectRatio(1f),
                                                    shape = CircleShape,
                                                    color = color,
                                                    border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
                                                ) {}
                                            }
                                        }
                                    }
                                }
                                2 -> { // Style Step
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth().height(290.dp).verticalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        maxItemsInEachRow = 4
                                    ) {
                                        com.android.snippets.viewmodel.SnippetStyle.values().forEach { style ->
                                            val isSelected = selectedStyle == style
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                            selectedStyle = style
                                                        }
                                                ) {
                                                    Surface(
                                                        modifier = Modifier.size(60.dp),
                                                        shape = CircleShape,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                        border = if (isSelected) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "S",
                                                                style = getSnippetTextStyle(style, MaterialTheme.typography.titleLarge),
                                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = style.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(top = 4.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                enabled = text.isNotBlank(),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                    onAdd(text.trim(), finalColor, selectedStyle)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add Snippet ($localSnippetsCount/6)",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
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


