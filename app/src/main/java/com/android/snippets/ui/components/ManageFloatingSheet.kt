package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ln.android.snippets.R
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.viewmodel.SnippetsViewModel

private data class ActionItemData(
    val id: String,
    val label: String,
    val icon: Any,
    val onClick: () -> Unit
)

@Composable
fun ManageFloatingSheet(
    collection: String?,
    currentTab: String,
    viewModel: SnippetsViewModel,
    isDeletingCollection: Boolean,
    onDismissRequest: () -> Unit,
    onAddPhotos: (String) -> Unit,
    onStartDeleteCollection: (String) -> Unit
) {
    val view = LocalView.current

    FloatingSheet(
        visible = collection != null,
        onDismissRequest = onDismissRequest,
        sheetModifier = Modifier
            .padding(horizontal = 16.dp)
            .widthIn(max = 320.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 14.dp)
    ) {
        if (collection == null) return@FloatingSheet

        CompositionLocalProvider(
            com.android.snippets.ui.util.LocalTiltActive provides (com.android.snippets.ui.util.LocalTiltActive.current && !isDeletingCollection)
        ) {
            val actions = remember(collection) {
                listOfNotNull(
                    ActionItemData(
                        id = "ADD",
                        label = if (collection == "Library") "Add photo" else "Add photos",
                        icon = Icons.Rounded.AddPhotoAlternate,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onAddPhotos(collection)
                            onDismissRequest()
                        }
                    ),
                    if (collection != "Library") ActionItemData(
                        id = "REMOVE",
                        label = "Remove\nphotos",
                        icon = Icons.Rounded.Remove,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.startCollectionRemoval(collection)
                            onDismissRequest()
                        }
                    ) else null,
                    if (collection != "Library" && collection != "Favorites") ActionItemData(
                        id = "EDIT_EMOJI",
                        label = "Edit emoji",
                        icon = Icons.Rounded.AddReaction,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.navigateEmojiPicker(collection)
                            onDismissRequest()
                        }
                    ) else null,
                    if (collection != "Library" && collection != "Favorites") ActionItemData(
                        id = "DELETE",
                        label = "Delete",
                        icon = R.drawable.ic_delete,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onStartDeleteCollection(collection)
                            onDismissRequest()
                        }
                    ) else null
                )
            }

            // Actions placed right from start/top (compact, left-aligned)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { clip = false },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                actions.chunked(2).forEach { rowActions ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = false },
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        verticalAlignment = Alignment.Top
                    ) {
                        rowActions.forEach { action ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .width(84.dp)
                                    .graphicsLayer { clip = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .zIndex(10f)
                                        .graphicsLayer { clip = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (val icon = action.icon) {
                                        is ImageVector -> AnimatedCookieButton(
                                            onClick = action.onClick,
                                            icon = icon,
                                            contentDescription = action.label.replace("\n", " "),
                                            size = 72.dp,
                                            iconSize = 28.dp,
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        is Int -> AnimatedCookieButton(
                                            onClick = action.onClick,
                                            icon = icon,
                                            contentDescription = action.label.replace("\n", " "),
                                            size = 72.dp,
                                            iconSize = 28.dp,
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                val isDoubleLine = action.label.contains("\n")
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .widthIn(min = 60.dp, max = 84.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(
                                                horizontal = 10.dp,
                                                vertical = if (isDoubleLine) 4.dp else 2.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = action.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = GoogleSansFlexRound,
                                                lineHeight = 13.sp
                                            ),
                                            maxLines = 2,
                                            softWrap = true,
                                            textAlign = TextAlign.Center,
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
}
