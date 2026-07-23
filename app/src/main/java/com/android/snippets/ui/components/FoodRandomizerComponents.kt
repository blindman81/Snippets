package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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

/**
 * Official Material 3 Specs Floating Action Button (FAB) for the Food Randomizer feature.
 *
 * M3 Specs Followed:
 * - Container shape: 16dp rounded corners (FloatingActionButtonDefaults.shape / ShapeDefaults.Large)
 * - Container size: 56dp (Standard FAB)
 * - Icon size: 24dp
 * - Container color: primaryContainer with onPrimaryContainer content color
 * - Elevation: FloatingActionButtonDefaults.elevation (6.dp default, 12.dp pressed)
 * - Interactive Haptics & M3 Tooltip integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodRandomizerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExtended: Boolean = false
) {
    val view = LocalView.current
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("What to Eat? (Randomizer)")
            }
        },
        state = tooltipState
    ) {
        if (isExtended) {
            ExtendedFloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_food_randomizer),
                        contentDescription = "Food Randomizer",
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        text = "What to Eat?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = FloatingActionButtonDefaults.shape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = modifier
            )
        } else {
            FloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = FloatingActionButtonDefaults.shape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_food_randomizer),
                    contentDescription = "What to Eat? (Randomizer)",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_food_randomizer),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "What to Eat?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Random dish recommendation from your Eatlist",
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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

                                // Top badges (Rating & Eatlist badge)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (photo.rating > 0) {
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
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(100),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_eatlist),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Eatlist",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Bottom dish title / snippet text
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(20.dp)
                                ) {
                                    val primarySnippet = photo.snippets.firstOrNull()
                                    if (!primarySnippet.isNullOrEmpty()) {
                                        Text(
                                            text = primarySnippet,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
                                        Text(
                                            text = "Tasty Discovery",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { pickNextRandom() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_food_randomizer),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shuffle", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            currentPhoto?.let {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onDismissRequest()
                                onSelectPhoto(it)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Let's Eat!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
