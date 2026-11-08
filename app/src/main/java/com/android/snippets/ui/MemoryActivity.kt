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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.android.snippets.logic.MemoryWorker
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.viewmodel.MemoryViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemoryActivity : ComponentActivity() {
    private val viewModel: MemoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val photoId = intent.getStringExtra("photo_id")
        val memoryIndex = intent.getIntExtra("memory_index", 0)

        setContent {
            val isDarkTheme = when (viewModel.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }

            LaunchedEffect(Unit) {
                MemoryWorker.isInMemoryScreen = true
                MemoryWorker.cancelSummaryNotification(this@MemoryActivity)
            }

            LaunchedEffect(viewModel.curatedMemories, viewModel.photos, photoId) {
                if (photoId != null) {
                    viewModel.openMemoryForPhoto(photoId)
                } else if (viewModel.curatedMemories.isNotEmpty()) {
                    viewModel.openMemory(memoryIndex)
                } else if (viewModel.photos.isNotEmpty()) {
                    viewModel.openMemory(viewModel.photos, 0)
                }
            }

            SnippetsTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoryScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MemoryWorker.isInMemoryScreen = false
    }
}
