package com.android.snippets.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.ui.theme.configurePredictiveBackTransition
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val snippetsViewModel: SnippetsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configurePredictiveBackTransition()

        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Library"
        snippetsViewModel.filteringCategory = category
        intent.getStringArrayListExtra(EXTRA_SELECTED_FILTERS)?.let { initialFilters ->
            snippetsViewModel.selectedFilterSnippets = initialFilters
        }

        lifecycleScope.launch {
            snapshotFlow { snippetsViewModel.selectedFilterSnippets }.collect { filters ->
                val data = Intent().apply {
                    putStringArrayListExtra(
                        EXTRA_SELECTED_FILTERS,
                        ArrayList(filters)
                    )
                }
                setResult(Activity.RESULT_OK, data)
            }
        }

        setContent {
            val isDarkTheme = when (settingsViewModel.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            SnippetsTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    SnippetsScreen(
                        viewModel = snippetsViewModel,
                        category = category,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }

    override fun finish() {
        val data = Intent().apply {
            putStringArrayListExtra(
                EXTRA_SELECTED_FILTERS,
                ArrayList(snippetsViewModel.selectedFilterSnippets)
            )
        }
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_SELECTED_FILTERS = "extra_selected_filters"
    }
}
