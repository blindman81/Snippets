package com.android.snippets.ui
import com.android.snippets.ui.components.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path



import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.android.snippets.viewmodel.SnippetsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: SnippetsViewModel) {
    androidx.activity.compose.BackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
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

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            MainTopBar(
                title = "About",
                onNavigationClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary() 
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = !(showMenu || showLicenses),
                isScrolled = isScrolled,
                leftAlignTitle = true,
                actions = {
                    Box {
                        AnimatedCookieButton(
                            onClick = { showMenu = true },
                            icon = Icons.Default.MoreVert,
                            contentDescription = "More",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            isSpinning = !(showMenu || showLicenses)
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(x = (-16).dp, y = 8.dp),
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "DEVELOPER",
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
                    text = buildAnnotatedString {
                        append("Created with ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)) {
                            append("Antigravity")
                        }
                        append(", ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)) {
                            append("Codex")
                        }
                        append(", and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)) {
                            append("Stitch")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 240.dp)
                )

                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                val xLogo = remember {
                    androidx.compose.ui.graphics.vector.ImageVector.Builder(
                        name = "XLogo",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f
                    ).path(fill = SolidColor(Color.Black)) {
                        moveTo(18.244f, 2.25f)
                        horizontalLineToRelative(3.308f)
                        lineToRelative(-7.227f, 8.26f)
                        lineToRelative(8.502f, 11.24f)
                        horizontalLineToRelative(-6.657f)
                        lineToRelative(-5.214f, -6.817f)
                        lineToRelative(-5.966f, 6.817f)
                        horizontalLineTo(1.68f)
                        lineToRelative(7.73f, -8.835f)
                        lineTo(1.254f, 2.25f)
                        horizontalLineTo(8.08f)
                        lineToRelative(4.713f, 6.231f)
                        close()
                        moveTo(17.083f, 19.77f)
                        horizontalLineToRelative(1.833f)
                        lineTo(7.084f, 4.126f)
                        horizontalLineTo(5.117f)
                        close()
                    }.build()


                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedCookieButton(
                        onClick = { 
                            try {
                                uriHandler.openUri("https://discord.gg/t272UTb4q")
                            } catch (e: Exception) {
                                // Handle potential error
                            }
                        },
                        icon = DiscordIcon(),
                        contentDescription = "Discord",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 56.dp
                    )

                    AnimatedCookieButton(
                        onClick = { 
                            try {
                                uriHandler.openUri("https://x.com/NL_1818")
                            } catch (e: Exception) {
                                // Handle potential error
                            }
                        },
                        icon = xLogo,
                        contentDescription = "X",
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
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(
                "Open source licenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
                    name = "Android Jetpack (AndroidX)",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Material Components for Android",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2018 The Android Open Source Project"
                )
                LicenseItem(
                    name = "Kotlin Standard Library",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2010-2024 JetBrains s.r.o."
                )
                LicenseItem(
                    name = "Coil",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2024 Coil Contributors"
                )
                LicenseItem(
                    name = "Gson",
                    license = "Apache License 2.0",
                    copyright = "Copyright (c) 2008 Google Inc."
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
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
