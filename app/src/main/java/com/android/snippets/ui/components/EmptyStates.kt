package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.res.vectorResource

@Composable
fun PremiumEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
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
            Surface(
                modifier = Modifier.size(170.dp),
                shape = CookieShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {}
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmptyLibraryState() {
    PremiumEmptyState(
        icon = EmptyLibraryIcon()
    )
}

@Composable
fun EmptyFavoritesState() {
    PremiumEmptyState(
        icon = Icons.Default.HeartBroken
    )
}


@Composable
fun EmptyCollectionState() {
    PremiumEmptyState(
        icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = com.ln.android.snippets.R.drawable.ic_folder_open_empty)
    )
}

@Composable
fun EmptySearchState() {
    PremiumEmptyState(
        icon = SearchNoResultsIcon()
    )
}

