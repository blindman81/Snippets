package com.android.snippets.ui.components

import com.android.snippets.ui.shapes.LocalAppShape

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*

import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

@Composable
fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val targetContainerColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = remember { Animatable(targetContainerColor) }
    
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(colorScheme) {
        containerColor.snapTo(targetContainerColor)
    }

    LaunchedEffect(checked) {
        containerColor.animateTo(targetContainerColor, tween(300))
    }

    val rotation by animateFloatAsState(
        targetValue = if (checked) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "switch_rotation"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(LocalAppShape.current)
            .background(if (enabled) containerColor.value else containerColor.value.copy(alpha = 0.38f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = checked,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "switch_icon_transition"
        ) { isChecked ->
            Icon(
                imageVector = if (isChecked) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (enabled) {
                    if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    (if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
fun CookieCheckmark(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(300),
        label = "cookie_check_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
        animationSpec = tween(300),
        label = "cookie_check_icon"
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(LocalAppShape.current)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )
    }
}
