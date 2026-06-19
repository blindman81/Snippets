package com.android.snippets.ui.components

import com.android.snippets.ui.SelectIcon
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import com.android.snippets.ui.SelectIcon
import androidx.compose.ui.res.vectorResource

@Composable
fun FabActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestExpandableFab(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    isVisible: Boolean,
    onAddPhotos: () -> Unit,
    onSelectMode: () -> Unit,
    onBrowse: () -> Unit = {}
) {
    val view = LocalView.current
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        val fabOffset by animateDpAsState(
            targetValue = if (isExpanded) (-120).dp else 0.dp,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
            label = "fab_offset"
        )
        val fabSize by animateDpAsState(
            targetValue = if (isExpanded) 56.dp else 80.dp,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
            label = "fab_size"
        )
        val cornerSize by animateDpAsState(
            targetValue = if (isExpanded) 28.dp else 24.dp,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
            label = "fab_corner"
        )

        // Action Pills
        AnimatedVisibility(
            visible = isExpanded && isVisible,
            enter = slideInVertically(spring(dampingRatio = 0.55f, stiffness = 300f)) { it / 2 } + fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f)),
            exit = slideOutVertically(spring(dampingRatio = 0.6f, stiffness = 500f)) { it / 2 } + fadeOut() + scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.offset(y = fabOffset)
            ) {
                FabActionPill(
                    icon = SelectIcon(),
                    label = "Select",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onSelectMode()
                        onExpandChange(false)
                    }
                )
                
                FabActionPill(
                    icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = com.ln.android.snippets.R.drawable.ic_browse),
                    label = "Browse",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onBrowse()
                        onExpandChange(false)
                    }
                )
                
                FabActionPill(
                    icon = Icons.Default.AddAPhoto,
                    label = "Add a photo",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onAddPhotos()
                        onExpandChange(false)
                    }
                )
            }
        }

        // Main FAB
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 45f else 0f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
                label = "fab_rotation"
            )
            val tooltipState = rememberTooltipState()

            @OptIn(ExperimentalMaterial3Api::class)
        TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Add a photo")
                    }
                },
                state = tooltipState
            ) {
                LaunchedEffect(tooltipState.isVisible) {
                    if (tooltipState.isVisible) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
                FloatingActionButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onExpandChange(!isExpanded)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(cornerSize),
                    modifier = Modifier
                        .size(fabSize)
                        .offset(y = fabOffset)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isExpanded) "Close" else "Add",
                        modifier = Modifier
                            .size(if (isExpanded) 24.dp else 32.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}

fun rotationIn(): EnterTransition {
    return fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f)) + 
           scaleIn(initialScale = 0.3f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)) +
           rotateIn()
}

fun rotationOut(): ExitTransition {
    return fadeOut(animationSpec = spring(dampingRatio = 0.9f, stiffness = 800f)) + 
           scaleOut(targetScale = 0.3f, animationSpec = spring(dampingRatio = 0.9f, stiffness = 800f)) +
           rotateOut()
}

fun rotateIn(): EnterTransition = fadeIn() 
fun rotateOut(): ExitTransition = fadeOut()

