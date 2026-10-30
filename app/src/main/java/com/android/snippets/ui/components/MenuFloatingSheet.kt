package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun MenuFloatingSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: SnippetsViewModel,
    view: android.view.View
) {
    FloatingSheet(
        visible = show,
        onDismissRequest = onDismissRequest,
        sheetModifier = Modifier
            .padding(horizontal = 16.dp)
            .width(316.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 14.dp)
    ) {
        data class MenuItemInfo(
            val label: String,
            val icon: Any,
            val onClick: () -> Unit
        )

        val marqueeIcon = ImageVector.vectorResource(com.ln.android.snippets.R.drawable.ic_marquee_select)
        val menuItems = remember(marqueeIcon) {
            listOf(
                MenuItemInfo(
                    label = "Create",
                    icon = com.ln.android.snippets.R.drawable.ic_create_new_folder,
                    onClick = {
                        onDismissRequest()
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.navigateEmojiPicker(null)
                    }
                ),
                MenuItemInfo(
                    label = "Select",
                    icon = marqueeIcon,
                    onClick = {
                        onDismissRequest()
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.forceSelectionMode = true
                    }
                ),
                MenuItemInfo(
                    label = "Stats",
                    icon = Icons.Rounded.BarChart,
                    onClick = {
                        onDismissRequest()
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.navigateStats()
                    }
                ),
                MenuItemInfo(
                    label = "Settings",
                    icon = Icons.Rounded.Settings,
                    onClick = {
                        onDismissRequest()
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.navigateSettings()
                    }
                ),
                MenuItemInfo(
                    label = "About",
                    icon = Icons.Rounded.Info,
                    onClick = {
                        onDismissRequest()
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.navigateAbout()
                    }
                )
            )
        }

        // Menu buttons placed right from start/top (compact, left-aligned)
        Column(
            modifier = Modifier.graphicsLayer { clip = false },
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            menuItems.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.graphicsLayer { clip = false },
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    verticalAlignment = Alignment.Top
                ) {
                    rowItems.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .width(84.dp)
                                .graphicsLayer { clip = false }
                        ) {
                            Box(
                                modifier = Modifier.zIndex(10f),
                                contentAlignment = Alignment.Center
                            ) {
                                when (val icon = item.icon) {
                                    is ImageVector -> AnimatedCookieButton(
                                        onClick = item.onClick,
                                        icon = icon,
                                        contentDescription = item.label,
                                        size = 72.dp,
                                        iconSize = 32.dp,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    is Int -> AnimatedCookieButton(
                                        onClick = item.onClick,
                                        icon = icon,
                                        contentDescription = item.label,
                                        size = 72.dp,
                                        iconSize = 32.dp,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = GoogleSansFlexRound
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
