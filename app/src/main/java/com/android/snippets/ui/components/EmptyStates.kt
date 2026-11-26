package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterListOff
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.shapes.CookieShape
import com.ln.android.snippets.R

@Composable
fun PremiumEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    cookieSize: Dp = 170.dp,
    iconSize: Dp = 72.dp,
    fillContainer: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val borderStroke = BorderStroke(2.dp, containerColor.copy(alpha = 0.5f))
    val iconContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    val containerModifier = if (fillContainer) {
        modifier
            .fillMaxSize()
            .padding(32.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    }

    Column(
        modifier = containerModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(cookieSize),
            contentAlignment = Alignment.Center
        ) {
            if (onClick != null) {
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onClick()
                    },
                    modifier = Modifier.size(cookieSize),
                    shape = CookieShape,
                    color = containerColor,
                    border = borderStroke,
                    content = iconContent
                )
            } else {
                Surface(
                    modifier = Modifier.size(cookieSize),
                    shape = CookieShape,
                    color = containerColor,
                    border = borderStroke,
                    content = iconContent
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    modifier: Modifier = Modifier
) {
    PremiumEmptyState(
        modifier = modifier,
        icon = ImageVector.vectorResource(R.drawable.ic_box_add)
    )
}

@Composable
fun EmptyBox1State(
    modifier: Modifier = Modifier
) {
    PremiumEmptyState(
        modifier = modifier,
        icon = ImageVector.vectorResource(R.drawable.ic_box_add)
    )
}

@Composable
fun EmptyBoxState(
    modifier: Modifier = Modifier,
    icon: ImageVector = ImageVector.vectorResource(R.drawable.ic_box_add),
    onClick: (() -> Unit)? = null
) {
    PremiumEmptyState(
        modifier = modifier,
        icon = icon,
        onClick = onClick
    )
}

@Composable
fun EmptyFilterState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.FilterListOff,
    onClick: (() -> Unit)? = null
) {
    PremiumEmptyState(
        modifier = modifier,
        icon = icon,
        onClick = onClick
    )
}

@Composable
fun EmptyLocationState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.LocationOff,
    onClick: (() -> Unit)? = null
) {
    PremiumEmptyState(
        modifier = modifier,
        icon = icon,
        fillContainer = false,
        onClick = onClick
    )
}
