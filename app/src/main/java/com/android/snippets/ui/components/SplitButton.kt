package com.android.snippets.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants

@Composable
fun SplitButton(
    primaryIcon: ImageVector,
    primaryText: String,
    onPrimaryClick: () -> Unit,
    dropdownContent: @Composable ColumnScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    // Animations for morphing between unselected and selected states
    val gap by animateDpAsState(
        targetValue = if (expanded) 8.dp else 2.dp,
        label = "gapAnimation"
    )
    
    val rightShapeLeftCorner by animateDpAsState(
        targetValue = if (expanded) 28.dp else 4.dp,
        label = "rightShapeCorner"
    )

    val leftShapeRightCorner by animateDpAsState(
        targetValue = if (expanded) 28.dp else 4.dp,
        label = "leftShapeCorner"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    Row(
        modifier = modifier.height(48.dp), // Medium size standard
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        // Primary Action Button (Left)
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxHeight(),
            shape = RoundedCornerShape(
                topStart = CornerSize(28.dp),
                topEnd = CornerSize(4.dp),
                bottomEnd = CornerSize(4.dp),
                bottomStart = CornerSize(28.dp)
            ),
            contentPadding = PaddingValues(start = 24.dp, end = 20.dp)
        ) {
            Icon(
                imageVector = primaryIcon,
                contentDescription = primaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = primaryText)
        }

        // Secondary Menu Button (Right)
        Box {
            Button(
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    expanded = true 
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (expanded) 48.dp else 52.dp),
                shape = RoundedCornerShape(
                    topStart = CornerSize(rightShapeLeftCorner),
                    topEnd = CornerSize(28.dp),
                    bottomEnd = CornerSize(28.dp),
                    bottomStart = CornerSize(rightShapeLeftCorner)
                ),
                contentPadding = PaddingValues(0.dp)
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
                offset = androidx.compose.ui.unit.DpOffset(0.dp, 0.dp),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = null
            ) {
                dropdownContent { expanded = false }
            }
        }
    }
}
