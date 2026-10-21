package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.ui.DownloadMaterialSymbol
import com.android.snippets.ui.ShareOutlinedMaterialSymbol
import com.android.snippets.ui.KidStarOutlinedMaterialSymbol
import com.android.snippets.ui.DeleteMaterialSymbol
import com.ln.android.snippets.R

@Composable
fun PhotoSplitButton(
    photo: Photo,
    onAdd: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNamePhotoClick: () -> Unit,
    onAddLinkClick: () -> Unit,
    onRate: () -> Unit,
    onSnippets: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp
) {
    val view = LocalView.current
    val hasSnippets = photo.snippets.isNotEmpty()
    val isFavorite = photo.isFavorite
    val hasName = !photo.name.isNullOrBlank()
    val hasLocation = !photo.locationLink.isNullOrBlank()
    val hasRating = photo.rating > 0

    PhotoCookieSplitButton(
        primaryIcon = Icons.Default.Add,
        contentDescription = "Add snippets",
        onPrimaryClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onAdd()
        },
        menuColumns = 4,
        buttonSize = buttonSize,
        modifier = modifier,
        items = listOf(
            cookieButtonItem(
                icon = { mod, tint ->
                    Icon(
                        imageVector = DownloadMaterialSymbol,
                        contentDescription = "Download",
                        modifier = mod,
                        tint = tint
                    )
                },
                tooltip = "Download",
                enabled = hasSnippets,
                onClick = onDownload
            ),
            cookieButtonItem(
                icon = { mod, tint ->
                    Icon(
                        imageVector = ShareOutlinedMaterialSymbol,
                        contentDescription = "Share",
                        modifier = mod,
                        tint = tint
                    )
                },
                tooltip = "Share",
                enabled = hasSnippets,
                onClick = onShare
            ),
            cookieButtonItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                tooltip = if (isFavorite) "Unfavorite" else "Favorite",
                isSelected = isFavorite,
                onClick = onToggleFavorite
            ),
            cookieButtonItem(
                icon = if (hasName) Icons.AutoMirrored.Filled.Label else Icons.AutoMirrored.Outlined.Label,
                contentDescription = if (hasName) "Rename photo" else "Name photo",
                tooltip = if (hasName) "Rename photo" else "Name photo",
                isSelected = hasName,
                onClick = onNamePhotoClick
            ),
            cookieButtonItem(
                icon = if (hasLocation) Icons.Filled.Place else Icons.Outlined.Place,
                contentDescription = if (hasLocation) "Edit location link" else "Link to a place",
                tooltip = if (hasLocation) "Edit location link" else "Link to a place",
                isSelected = hasLocation,
                onClick = onAddLinkClick
            ),
            cookieButtonItem(
                icon = { mod, tint ->
                    if (hasRating) {
                        Box(
                            modifier = mod.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_rating),
                                contentDescription = "Rating ${photo.rating}",
                                tint = tint,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = photo.rating.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 0.5.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = KidStarOutlinedMaterialSymbol,
                            contentDescription = "Rate",
                            modifier = mod.size(24.dp),
                            tint = tint
                        )
                    }
                },
                tooltip = "Rate",
                isSelected = hasRating,
                onClick = onRate
            ),
            cookieButtonItem(
                icon = if (hasSnippets) Icons.AutoMirrored.Filled.TextSnippet else Icons.AutoMirrored.Outlined.TextSnippet,
                contentDescription = "Snippets",
                tooltip = "Snippets",
                enabled = hasSnippets,
                isSelected = hasSnippets,
                onClick = onSnippets
            ),
            cookieButtonItem(
                icon = { mod, tint ->
                    Icon(
                        imageVector = DeleteMaterialSymbol,
                        contentDescription = "Delete",
                        modifier = mod,
                        tint = tint
                    )
                },
                tooltip = "Delete",
                onClick = onDelete
            )
        )
    )
}
