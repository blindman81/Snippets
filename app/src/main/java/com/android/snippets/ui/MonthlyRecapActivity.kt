package com.android.snippets.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonthlyRecapActivity : ComponentActivity() {
    private val viewModel: SnippetsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val targetMonth = if (intent.hasExtra("month")) intent.getIntExtra("month", -1).takeIf { it != -1 } else null
        val targetYear = if (intent.hasExtra("year")) intent.getIntExtra("year", -1).takeIf { it != -1 } else null

        setContent {
            val isDarkTheme = when (settingsViewModel.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }

            var recapInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(viewModel.photos, viewModel.isInitialLoading) {
                if (!viewModel.isInitialLoading) {
                    viewModel.triggerMonthlyRecap(targetMonth, targetYear)
                    recapInitialized = true
                }
            }

            SnippetsTheme(
                darkTheme = isDarkTheme,
                dynamicColor = settingsViewModel.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (viewModel.isInitialLoading || !recapInitialized) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        MonthlyRecapScreen(
                            viewModel = viewModel,
                            onBack = { finish() }
                        )
                    }
                }
            }
        }
    }
}
