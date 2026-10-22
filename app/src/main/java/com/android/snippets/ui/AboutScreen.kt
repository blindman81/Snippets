package com.android.snippets.ui
import com.android.snippets.ui.components.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path



import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.android.snippets.viewmodel.SnippetsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: SnippetsViewModel) {
    AppPredictiveBackHandler {
        viewModel.navigateLibrary()
    }
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val hazeState = LocalHazeState.current ?: rememberHazeState()
    var showMenu by remember { mutableStateOf(false) }
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

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            topBar = {
                MainTopBar(
                    title = "About",
                    onNavigationClick = { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.navigateLibrary() 
                    },
                    navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                    isSpinning = !(showMenu || showLicenses),
                    isScrolled = isScrolled,
                    leftAlignTitle = true,
                    actions = {
                        Box {
                            AnimatedCookieButton(
                                onClick = { showMenu = true },
                                icon = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                isSpinning = !(showMenu || showLicenses)
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                shape = RoundedCornerShape(12.dp),
                                offset = DpOffset(x = (-16).dp, y = 8.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open source licenses", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showMenu = false
                                        showLicenses = true
                                    }
                                )
                            }
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                if (showLicenses) {
                    LicenseDialog(onDismiss = { showLicenses = false })
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState),
                    contentAlignment = Alignment.TopCenter
                ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Lakshmi Narayana",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "UI and UX design by me, code by agentic tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp)
                )

                AnimatedCookieButton(
                    onClick = {
                        val email = "lnsnippets2026@gmail.com"
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                            setPackage("com.google.android.gm")
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
}
}
}

@Composable
fun LicenseDialog(onDismiss: () -> Unit) {
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
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LicenseItem(
                    name = "Android Jetpack & Jetpack Compose",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018-2025 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Material Components for Android",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018-2025 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Kotlin & Kotlin Coroutines",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors"
                )
                LicenseItem(
                    name = "Coil",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2024 Coil Contributors"
                )
                LicenseItem(
                    name = "Haze",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2023-2025 Chris Banes"
                )
                LicenseItem(
                    name = "Gson",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2008 Google Inc."
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
