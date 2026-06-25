package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.saveable.rememberSaveable
import com.android.snippets.ui.components.*
import com.android.snippets.ui.theme.titleEmphasized
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun FilterScreen(viewModel: SnippetsViewModel) {
    val view = LocalView.current
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = selectedTabIndex, pageCount = { 2 })
    
    // Sync selectedTabIndex with pager state changes
    var isFirstFilterTabLoad by remember { mutableStateOf(true) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (selectedTabIndex != page) {
                selectedTabIndex = page
                if (!isFirstFilterTabLoad) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            isFirstFilterTabLoad = false
        }
    }
    
    val snippetsIcon = remember {
        ImageVector.Builder(
            name = "SnippetsTab",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            addGroup(
                name = "snippets_group",
                translationY = 960f
            ).apply {
                addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString("M200-200h560v-367L567-760H200v560Zm0 80q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h400l240 240v400q0 33-23.5 56.5T760-120H200Zm80-160h400v-80H280v80Zm0-160h400v-80H280v80Zm0-160h280v-80H280v80Zm-80 400v-560 560Z").toNodes(),
                    fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)
                )
            }
        }.build()
    }

    val sortIcon = remember {
        ImageVector.Builder(
            name = "SortTab",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            addGroup(
                name = "sort_group",
                translationY = 960f
            ).apply {
                addPath(
                    pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString("M120-240v-80h240v80H120Zm0-200v-80h480v80H120Zm0-200v-80h720v80H120Z").toNodes(),
                    fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)
                )
            }
        }.build()
    }



    BackHandler {
        viewModel.closeFilter()
    }
    val snippetsScrollState = rememberScrollState()
    val sortScrollState = rememberScrollState()
    val activeScrollState = when (selectedTabIndex) {
        0 -> snippetsScrollState
        else -> sortScrollState
    }
    val isScrolled by remember { derivedStateOf<Boolean> { activeScrollState.value > 0 } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        @OptIn(ExperimentalMaterial3Api::class)
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTabIndex = selectedTabIndex,
                        matchContentSize = true
                    ),
                    width = Dp.Unspecified,
                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                )
            }
        ) {
            val tabs = listOf("Snippets" to snippetsIcon, "Sort" to sortIcon)
            tabs.forEachIndexed { index, (label, icon) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        val isSelected = selectedTabIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(if (isSelected) 2.dp else 4.dp)
                        ) {
                            Icon(icon, null, modifier = Modifier.size(24.dp))
                             Text(
                                 text = label,
                                 style = MaterialTheme.typography.titleMedium.copy(
                                     fontFamily = com.android.snippets.ui.theme.GoogleSans
                                 ),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 2.dp, end = 2.dp, top = 0.dp, bottom = 0.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 500.dp)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                FilterSnippetContent(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeFilter() },
                    showDisplay = true,
                    showSort = false,
                    scrollState = when (page) {
                        0 -> snippetsScrollState
                        else -> sortScrollState
                    },
                    selectedTabIndex = page
                )
            }
        }
    }
}
