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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.android.snippets.data.repository.CollectionRepository
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.ui.theme.configurePredictiveBackTransition
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MoreActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var collectionRepository: CollectionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configurePredictiveBackTransition()

        val initialBoxes = intent.getStringArrayListExtra(EXTRA_USER_BOXES)
            ?: intent.getStringArrayListExtra(EXTRA_USER_COLLECTIONS)
            ?: emptyList<String>()
        val boxesState = mutableStateOf(initialBoxes)

        lifecycleScope.launch {
            val dbCols = withContext(Dispatchers.IO) {
                try {
                    collectionRepository.getAllCollections().map { it.name }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            if (dbCols.isNotEmpty() || initialBoxes.isEmpty()) {
                boxesState.value = dbCols
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
                    MoreScreen(
                        userBoxes = boxesState.value,
                        onNavigateBack = { finish() },
                        onAction = { action ->
                            val data = Intent().putExtra(EXTRA_ACTION, action)
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_ACTION = "extra_action"
        const val EXTRA_USER_BOXES = "extra_user_boxes"
        const val EXTRA_USER_COLLECTIONS = EXTRA_USER_BOXES
        const val ACTION_CREATE_BOX = "action_create_box"
        const val ACTION_CREATE_COLLECTION = ACTION_CREATE_BOX
    }
}
