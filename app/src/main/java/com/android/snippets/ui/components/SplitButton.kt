package com.android.snippets.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import android.view.HapticFeedbackConstants

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButton(
    primaryIcon: ImageVector,
    primaryText: String,
    onPrimaryClick: () -> Unit,
    dropdownContent: @Composable ColumnScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    useAnimatedGradient: Boolean = true,
    gradientColors: List<Color>? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    val gradientBrush = if (useAnimatedGradient) {
        rememberAnimatedGradientBrush(
            colors = gradientColors ?: AnimatedGradientDefaults.themeGradient()
        )
    } else null

    val leadingShapes = SplitButtonDefaults.leadingButtonShapesFor(48.dp)
    val trailingShapes = SplitButtonDefaults.trailingButtonShapesFor(48.dp)

    Box(modifier = modifier) {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onPrimaryClick()
                    },
                    shapes = leadingShapes,
                    colors = if (gradientBrush != null) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    modifier = if (gradientBrush != null) {
                        Modifier
                            .clip(leadingShapes.shape)
                            .background(gradientBrush)
                    } else {
                        Modifier
                    }
                ) {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = primaryText,
                        modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = primaryText)
                }
            },
            trailingButton = {
                Box {
                    SplitButtonDefaults.TrailingButton(
                        checked = expanded,
                        onCheckedChange = { isChecked ->
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            expanded = isChecked
                        },
                        shapes = trailingShapes,
                        colors = if (gradientBrush != null) {
                            ButtonDefaults.buttonColors(
                                containerColor = if (expanded) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (expanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        modifier = if (gradientBrush != null && !expanded) {
                            Modifier
                                .clip(trailingShapes.shape)
                                .background(gradientBrush)
                        } else {
                            Modifier
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "More options",
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(12.dp),
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 4.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 6.dp
                    ) {
                        dropdownContent { expanded = false }
                    }
                }
            }
        )
    }
}
