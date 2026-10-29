package com.android.snippets.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.theme.titleEmphasized
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun MainTopBar(
    viewModel: SnippetsViewModel? = null,
    title: String? = null,
    onNavigationClick: () -> Unit,
    navigationIcon: ImageVector = Icons.Rounded.Menu,
    isSpinning: Boolean = true,
    isScrolled: Boolean = false,
    showNavigationIcon: Boolean = true,
    leftAlignTitle: Boolean = true,
    titleIcon: ImageVector? = null,
    forceTransparentBackground: Boolean = false,
    showDividerOnScroll: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    
    val targetHeaderColor = if (forceTransparentBackground) {
        Color.Transparent
    } else if (isScrolled) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val headerColor by animateColorAsState(
        targetValue = targetHeaderColor,
        animationSpec = tween(250),
        label = "headerColor"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (forceTransparentBackground) Color.Transparent else headerColor,
        shape = RectangleShape,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Navigation Button
                if (showNavigationIcon) {
                    AnimatedCookieButton(
                        onClick = {
                            focusManager.clearFocus()
                            onNavigationClick()
                        },
                        icon = navigationIcon,
                        contentDescription = "Navigation",
                        isSpinning = isSpinning
                    )
                    if (leftAlignTitle) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                } else if (leftAlignTitle) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Centered Title Text
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (leftAlignTitle) Alignment.CenterStart else Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = if (titleIcon != null) {
                            titleIcon
                        } else if (viewModel != null && title != null) {
                            if (viewModel.userCollections.any { it.equals(title, ignoreCase = true) }) {
                                viewModel.getCollectionIcon(title)
                            } else {
                                null
                            }
                        } else {
                            null
                        }

                        if (icon != null) {
                            if (icon is String) {
                                Text(
                                    text = icon,
                                    style = titleEmphasized,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else if (icon is ImageVector) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        Text(
                            text = title ?: "",
                            style = titleEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Right actions
                Row(
                    modifier = Modifier.widthIn(min = 56.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (actions != @Composable {}) {
                        Spacer(modifier = Modifier.width(8.dp))
                        actions()
                    }
                }
            }
            
            // Show progress if searching or filtering
            if (viewModel?.isSearching == true || viewModel?.isFiltering == true) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            } else if (isScrolled && !forceTransparentBackground && showDividerOnScroll) {
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
