package com.android.snippets.ui

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.components.*
import com.android.snippets.ui.util.fadingEdge
import com.ln.android.snippets.R
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scope = rememberCoroutineScope()
    val hazeState = LocalHazeState.current ?: rememberHazeState()
    var showLicenses by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf<Boolean> { scrollState.value > 0 } }
    
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    PredictiveBackHandler(enabled = showLicenses) { progress ->
        try {
            progress.collect { }
            showLicenses = false
        } catch (e: CancellationException) {
            // cancelled
        }
    }

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .nestedScroll(nestedScrollConnection),
                topBar = {
                    MainTopBar(
                        title = "About",
                        onNavigationClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            backDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.finish()
                        },
                        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                        isSpinning = !showLicenses,
                        isScrolled = isScrolled,
                        leftAlignTitle = true,
                        actions = {
                            Cookiebutton(
                                onClick = { showLicenses = true },
                                icon = R.drawable.ic_license,
                                contentDescription = "Open source licenses",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                isSpinning = !showLicenses
                            )
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "DIRECTOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Lakshmi Narayana",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "UI and UX design ideas by me, code by agentic tools.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 280.dp)
                        )

                        Cookiebutton(
                            onClick = {
                                val email = "lnsnippets2026@gmail.com"
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$email")
                                        }
                                        context.startActivity(fallbackIntent)
                                    } catch (e2: Exception) {
                                        // Fallback if no email client is available
                                    }
                                }
                            },
                            icon = Icons.Rounded.ContactMail,
                            contentDescription = "Contact Mail",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            size = 56.dp
                        )
                    }
                }
            }

            if (showLicenses) {
                LicenseDialog(onDismiss = { showLicenses = false })
            }
        }
    }
}

@Composable
fun LicenseDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val bottomFade by animateDpAsState(
        targetValue = if (scrollState.canScrollForward) 32.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "licenseBottomFade"
    )
    val topFade by animateDpAsState(
        targetValue = if (scrollState.canScrollBackward) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "licenseTopFade"
    )

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Open source licenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .fadingEdge(top = topFade, bottom = bottomFade)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LicenseItem(
                    name = "Android Jetpack & Jetpack Compose",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018-2026 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Material Components & Material 3",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018-2026 Google LLC & The Android Open Source Project"
                )
                LicenseItem(
                    name = "Google Material Symbols",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2020-2026 Google LLC"
                )
                LicenseItem(
                    name = "Kotlin & Kotlinx Coroutines",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors"
                )
                LicenseItem(
                    name = "Kotlinx Serialization",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2017-2026 JetBrains s.r.o."
                )
                LicenseItem(
                    name = "Dagger Hilt",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2012-2026 The Dagger Authors"
                )
                LicenseItem(
                    name = "Coil",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2020-2026 Coil Contributors"
                )
                LicenseItem(
                    name = "OkHttp",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2013-2026 Square, Inc."
                )
                LicenseItem(
                    name = "Haze",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2023-2026 Chris Banes"
                )
                LicenseItem(
                    name = "AndroidX Graphics Shapes",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2023-2026 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Google Sans Flex",
                    license = "SIL Open Font License 1.1",
                    copyright = "Copyright (c) Google LLC"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}

@Composable
private fun LicenseItem(name: String, license: String, copyright: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = license,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = copyright,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
