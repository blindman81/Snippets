package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ln.android.snippets.R
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.util.Motion

/**
 * Official Material 3 Specs Floating Action Button (FAB) for the Food Randomizer feature.
 *
 * M3 Specs Followed:
 * - Container shape: LocalAppShape.current
 * - Container size: 56dp (Standard FAB)
 * - Icon size: 24dp
 * - Container color: secondaryContainer with onSecondaryContainer content color
 * - Elevation: FloatingActionButtonDefaults.elevation (6.dp default, 12.dp pressed)
 * - Interactive Haptics & M3 Tooltip integration
 */
@Composable
fun FoodRandomizerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExtended: Boolean = false
) {
    AnimatedCookieButton(
        onClick = onClick,
        icon = R.drawable.ic_food_randomizer,
        contentDescription = "What to Eat? (Randomizer)",
        tooltip = "What to Eat? (Randomizer)",
        size = 56.dp,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

/**
 * Modal Bottom Sheet presenting the Food Randomizer dish recommendation with smooth slot machine shuffle animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodRandomizerBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    eatlistPhotos: List<Photo>,
    onSelectPhoto: (Photo) -> Unit
) {
    if (!show) return

    val view = LocalView.current
    var currentPhoto by remember(eatlistPhotos) {
        mutableStateOf(eatlistPhotos.shuffled().firstOrNull())
    }
    var shuffleTrigger by remember { mutableStateOf(0) }

    fun pickNextRandom() {
        if (eatlistPhotos.isEmpty()) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val available = if (eatlistPhotos.size > 1) eatlistPhotos.filter { it.id != currentPhoto?.id } else eatlistPhotos
        currentPhoto = available.shuffled().firstOrNull() ?: eatlistPhotos.firstOrNull()
        shuffleTrigger++
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Motion.ExpressiveSheetContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val appShape = LocalAppShape.current
                Surface(
                    shape = appShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            pickNextRandom()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_food_randomizer),
                            contentDescription = "Shuffle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Not sure what to eat?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Here's a random food recommendation from your eatlist.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MealDropdownChip()
            }

            if (eatlistPhotos.isEmpty() || currentPhoto == null) {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_eatlist),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Your Eatlist is empty!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add some food photos to your Eatlist to start using the randomizer.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Dish Display Card with shuffle animation
                AnimatedContent(
                    targetState = Pair(currentPhoto, shuffleTrigger),
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = 400f)))
                            .togetherWith(fadeOut() + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 400f)))
                    },
                    label = "dish_shuffle"
                ) { (photo, _) ->
                    if (photo != null) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = photo.uriString,
                                    contentDescription = photo.snippets.firstOrNull() ?: "Food photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.75f)
                                                ),
                                                startY = 200f
                                            )
                                        )
                                )

                                // Top badge (Rating)
                                if (photo.rating > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = Color.Black.copy(alpha = 0.65f),
                                            contentColor = Color.White
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB800),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = photo.rating.toString(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Bottom dish snippet text
                                val primarySnippet = photo.snippets.firstOrNull()
                                if (!primarySnippet.isNullOrEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = primarySnippet,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons with M3 Press Shape Morphing Animation (Circular to Rounded Rectangle)
                val shuffleInteraction = remember { MutableInteractionSource() }
                val isShufflePressed by shuffleInteraction.collectIsPressedAsState()
                val shuffleCornerRadius by animateDpAsState(
                    targetValue = if (isShufflePressed) 12.dp else 50.dp,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "shuffle_corner_morph"
                )

                val eatInteraction = remember { MutableInteractionSource() }
                val isEatPressed by eatInteraction.collectIsPressedAsState()
                val eatCornerRadius by animateDpAsState(
                    targetValue = if (isEatPressed) 12.dp else 50.dp,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "eat_corner_morph"
                )

                val buttonGradient = rememberAnimatedGradientBrush(
                    colors = AnimatedGradientDefaults.themeGradient()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shuffleShape = RoundedCornerShape(shuffleCornerRadius)
                    OutlinedButton(
                        onClick = { pickNextRandom() },
                        shape = shuffleShape,
                        interactionSource = shuffleInteraction,
                        contentPadding = ButtonDefaults.ContentPadding,
                        border = null,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(ButtonDefaults.MinHeight)
                            .clip(shuffleShape)
                            .animatedGradientBorder(
                                borderWidth = 1.5.dp,
                                colors = AnimatedGradientDefaults.themeGradient(),
                                shape = shuffleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_food_randomizer),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Shuffle",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val eatShape = RoundedCornerShape(eatCornerRadius)
                    Button(
                        onClick = {
                            currentPhoto?.let {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onDismissRequest()
                                onSelectPhoto(it)
                            }
                        },
                        shape = eatShape,
                        interactionSource = eatInteraction,
                        contentPadding = ButtonDefaults.ContentPadding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(ButtonDefaults.MinHeight)
                            .clip(eatShape)
                            .background(buttonGradient)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Let's Eat!",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
}
