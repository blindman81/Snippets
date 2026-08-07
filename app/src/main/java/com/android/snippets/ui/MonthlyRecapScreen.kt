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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.*
import com.android.snippets.viewmodel.SnippetsViewModel

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
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(photos.isEmpty()) {
        if (photos.isEmpty()) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                viewModel = viewModel,
                title = title,
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                leftAlignTitle = true,
                showSearchIcon = false,
                isScrolled = isScrolled
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
            if (photos.isNotEmpty()) {
                RecapCardList(
                    photos = photos,
                    viewModel = viewModel,
                    listState = listState
                )
            }
        }
    }
}

@Composable
private fun RecapCardList(
    photos: List<Photo>,
    viewModel: SnippetsViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val totalCount = photos.size
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->
            val cardPos = when {
                totalCount == 1 -> CardPosition.Single
                index == 0 -> CardPosition.First
                index == totalCount - 1 -> CardPosition.Last
                else -> CardPosition.Middle
            }

            PhotoCardListItem(
                photo = photo,
                position = CardPosition.Single,
                viewModel = viewModel,
                onClick = {
                    viewModel.openDetail(photo.id)
                }
            )
        }
    }
}

