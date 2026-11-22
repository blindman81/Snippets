package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterListOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.shapes.CookieShape
import com.ln.android.snippets.R

@Composable
fun PremiumEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            if (onClick != null) {
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onClick()
                    },
                    modifier = Modifier.size(170.dp),
                    shape = CookieShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.size(170.dp),
                    shape = CookieShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
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
