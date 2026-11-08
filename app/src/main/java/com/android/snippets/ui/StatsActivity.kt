package com.android.snippets.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.viewmodel.StatsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsActivity : ComponentActivity() {
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme = when (viewModel.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }

            SnippetsTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    StatsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
