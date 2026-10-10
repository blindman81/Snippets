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
import android.os.Build
import android.view.HapticFeedbackConstants


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButton(
    primaryIcon: ImageVector,
    primaryText: String? = null,
    contentDescription: String? = primaryText,
    onPrimaryClick: () -> Unit,
    dropdownContent: @Composable ColumnScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

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
                    shapes = leadingShapes
                ) {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = contentDescription ?: primaryText,
                        modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize)
                    )
                    if (!primaryText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = primaryText)
                    }
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
                        shapes = trailingShapes
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
                        containerColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 0.dp else 6.dp
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            BlurredSurface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shadowElevation = 6.dp,
                                blurStartRadius = 4.dp,
                                blurEndRadius = 24.dp
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    dropdownContent { expanded = false }
                                }
                            }
                        } else {
                            dropdownContent { expanded = false }
                        }
                    }
                }
            }
        )
    }
}
