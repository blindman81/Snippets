package com.android.snippets.ui

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.*
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.ln.android.snippets.R
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private data class AboutItemData(
    val label: String,
    val icon: Any,
    val subtext: String? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)

private data class LicenseData(
    val name: String,
    val license: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val hazeState = LocalHazeState.current ?: rememberHazeState()
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val onContactMailClick: () -> Unit = {
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
    }

    val aboutItems = remember(context) {
        listOf(
            AboutItemData(
                label = "Lakshmi Narayana",
                icon = Icons.Outlined.Person,
                subtext = "UI and UX design ideas",
                trailingContent = {
                    Cookiebutton(
                        onClick = onContactMailClick,
                        icon = Icons.Rounded.ContactMail,
                        contentDescription = "Contact Mail",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 44.dp
                    )
                },
                onClick = onContactMailClick
            ),
            AboutItemData(
                label = "Code by Agentic tools",
                icon = Icons.Outlined.Code,
                subtext = "Antigravity with Codex and Claude sparsely used",
                onClick = {}
            ),
            AboutItemData(
                label = "In development since April 1, 2026",
                icon = Icons.Outlined.CalendarMonth,
                onClick = {}
            )
        )
    }

    val licenseItems = remember {
        listOf(
            LicenseData("Android Jetpack & Jetpack Compose", "Apache License 2.0"),
            LicenseData("Material Components & Material 3", "Apache License 2.0"),
            LicenseData("Google Material Symbols", "Apache License 2.0"),
            LicenseData("Kotlin & Kotlinx Coroutines", "Apache License 2.0"),
            LicenseData("Kotlinx Serialization", "Apache License 2.0"),
            LicenseData("Dagger Hilt", "Apache License 2.0"),
            LicenseData("Coil", "Apache License 2.0"),
            LicenseData("OkHttp", "Apache License 2.0"),
            LicenseData("Okio", "Apache License 2.0"),
            LicenseData("Haze", "Apache License 2.0"),
            LicenseData("AndroidX Graphics Shapes", "Apache License 2.0"),
            LicenseData("AndroidX Room", "Apache License 2.0"),
            LicenseData("AndroidX DataStore", "Apache License 2.0"),
            LicenseData("AndroidX WorkManager", "Apache License 2.0"),
            LicenseData("AndroidX Paging", "Apache License 2.0"),
            LicenseData("AndroidX Navigation Compose", "Apache License 2.0"),
            LicenseData("AndroidX ExifInterface", "Apache License 2.0"),
            LicenseData("AndroidX Core SplashScreen", "Apache License 2.0"),
            LicenseData("AndroidX Profile Installer", "Apache License 2.0"),
            LicenseData("Google Sans Flex", "SIL Open Font License 1.1")
        )
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
                    .hazeSource(state = hazeState),
                topBar = {
                    AppBar(
                        title = "About",
                        onNavigationClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            backDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() ?: (view.context as? androidx.activity.ComponentActivity)?.finish()
                        },
                        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                        isSpinning = true,
                        isScrolled = isScrolled,
                        leftAlignTitle = true,
                        showDividerOnScroll = true,
                        scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    aboutItems.forEachIndexed { index, item ->
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = aboutItems.size,
                            label = item.label,
                            subtext = item.subtext,
                            icon = item.icon,
                            trailingContent = item.trailingContent,
                            onClick = item.onClick
                        )
                    }

                    Text(
                        text = "Licenses",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = GoogleSansFlexRound
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 4.dp)
                    )

                    licenseItems.forEachIndexed { index, item ->
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = licenseItems.size,
                            label = item.name,
                            subtext = item.license,
                            icon = null,
                            onClick = {
                                val intent = Intent(context, LicenseAgreementActivity::class.java).apply {
                                    putExtra(LicenseAgreementActivity.EXTRA_LIBRARY_NAME, item.name)
                                    putExtra(LicenseAgreementActivity.EXTRA_LICENSE_NAME, item.license)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the full license agreement text for a library selected from the About screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseAgreementScreen(
    libraryName: String,
    licenseName: String
) {
    val context = LocalContext.current
    val view = LocalView.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val hazeState = LocalHazeState.current ?: rememberHazeState()
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val agreementText = remember(licenseName, context) {
        val rawResId = when {
            licenseName.contains("SIL", ignoreCase = true) || licenseName.contains("OFL", ignoreCase = true) ->
                R.raw.license_sil_ofl_1_1
            else -> R.raw.license_apache_2_0
        }
        context.resources.openRawResource(rawResId).bufferedReader().use { it.readText() }
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
                    .hazeSource(state = hazeState),
                topBar = {
                    AppBar(
                        title = libraryName.ifBlank { licenseName },
                        onNavigationClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            backDispatcher?.onBackPressed()
                                ?: (view.context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                                ?: (view.context as? androidx.activity.ComponentActivity)?.finish()
                        },
                        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                        isSpinning = true,
                        isScrolled = isScrolled,
                        leftAlignTitle = true,
                        showDividerOnScroll = true,
                        scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (licenseName.isNotBlank()) {
                        Text(
                            text = licenseName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GoogleSansFlexRound
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer {
                            Text(
                                text = agreementText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = GoogleSansFlexRound,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
