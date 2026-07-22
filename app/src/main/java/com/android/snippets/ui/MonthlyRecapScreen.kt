package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.MainTopBar
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.AppPredictiveBackHandler
import com.android.snippets.ui.CardPosition
import com.android.snippets.viewmodel.SnippetsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRecapScreen(
    viewModel: SnippetsViewModel,
    onBack: () -> Unit = { viewModel.navigateBack() }
) {
    AppPredictiveBackHandler {
        onBack()
    }

    val photos = viewModel.recapPhotos
    val title = viewModel.recapTitle

    Scaffold(
        topBar = {
            MainTopBar(
                viewModel = viewModel,
                title = title,
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                leftAlignTitle = true,
                showSearchIcon = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (photos.isEmpty()) {
                EmptyRecapCard(
                    title = title,
                    onBack = onBack
                )
            } else {
                RecapCardList(
                    photos = photos,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun EmptyRecapCard(
    title: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .height(300.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.ln.android.snippets.R.drawable.ic_star_rating),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "End of $title",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You've reviewed all standout memories in this recap!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back to Library")
            }
        }
    }
}

@Composable
private fun RecapCardList(
    photos: List<Photo>,
    viewModel: SnippetsViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->
            RecapHeroCard(
                photo = photo,
                snippets = photo.snippets,
                totalCount = photos.size,
                currentIndex = index + 1,
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .height(300.dp),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun RecapHeroCard(
    photo: Photo,
    snippets: List<String>,
    totalCount: Int,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    viewModel: SnippetsViewModel
) {
    val cardShape = RoundedCornerShape(24.dp)
    val formattedDate = remember(photo.date) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(photo.date))
    }

    val blockColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Surface(
        modifier = modifier
            .shadow(6.dp, cardShape)
            .clip(cardShape)
            .clickable {
                viewModel.openDetail(photo.id)
            },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Photo Box takes 60% of card space, centered
            Box(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight()
                    .background(
                        color = blockColor,
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 2.dp, bottomEnd = 2.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    AsyncImage(
                        model = photo.uriString,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Date Tag overlay on photo (bottom right)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Vertical gap running all the way down
            Spacer(modifier = Modifier.width(4.dp))

            // Right side column takes 40% of space
            Column(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight()
            ) {
                // Top section (icons)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.22f)
                        .background(
                            color = blockColor,
                            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 2.dp, bottomStart = 2.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        val iconColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else Color.Black
                        val iconContainerColor = MaterialTheme.colorScheme.secondaryContainer

                        if (photo.rating > 0) {
                            Surface(
                                color = iconContainerColor,
                                shape = LocalAppShape.current,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.ln.android.snippets.R.drawable.ic_star_rating),
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.fillMaxSize().padding(6.dp)
                                    )
                                    Text(
                                        text = photo.rating.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                        color = iconContainerColor,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }

                        if (photo.isFavorite) {
                            Surface(
                                color = iconContainerColor,
                                shape = LocalAppShape.current,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorite",
                                        tint = iconColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Horizontal Gap
                Spacer(modifier = Modifier.height(2.dp))

                // Bottom section: Snippets list (fits up to 6 snippets)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.78f)
                        .background(
                            color = blockColor,
                            shape = RoundedCornerShape(bottomEnd = 24.dp, topStart = 2.dp, bottomStart = 2.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (snippets.isEmpty()) {
                        Text(
                            text = "No snippets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            snippets.take(6).forEachIndexed { index, snippet ->
                                val snippetPosition = when {
                                    snippets.size == 1 -> CardPosition.Single
                                    index == 0 -> CardPosition.First
                                    index == snippets.size - 1 -> CardPosition.Last
                                    else -> CardPosition.Middle
                                }
                                val snippetShape = when (snippetPosition) {
                                    CardPosition.Single -> RoundedCornerShape(12.dp)
                                    CardPosition.First -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                    CardPosition.Middle -> RoundedCornerShape(2.dp)
                                    CardPosition.Last -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                                }

                                val forcedColor = viewModel.getSnippetColor(snippet)
                                val baseColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                                val snippetColor = remember(baseColor, isDark) {
                                    val lum = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
                                    if (isDark && lum < 0.3f) baseColor.copy(red = (baseColor.red + 0.4f).coerceAtMost(1f), green = (baseColor.green + 0.4f).coerceAtMost(1f), blue = (baseColor.blue + 0.4f).coerceAtMost(1f))
                                    else if (!isDark && lum > 0.7f) baseColor.copy(red = (baseColor.red - 0.4f).coerceAtLeast(0f), green = (baseColor.green - 0.4f).coerceAtLeast(0f), blue = (baseColor.blue - 0.4f).coerceAtLeast(0f))
                                    else baseColor
                                }

                                Surface(
                                    shape = snippetShape,
                                    color = snippetColor.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, snippetColor.copy(alpha = 0.30f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = snippet,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            ),
                                            color = snippetColor,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
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
