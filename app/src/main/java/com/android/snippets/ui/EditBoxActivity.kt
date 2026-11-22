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
import androidx.compose.ui.Modifier
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.ui.theme.configurePredictiveBackTransition
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditBoxActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configurePredictiveBackTransition()

        val box = intent.getStringExtra(EXTRA_BOX)
            ?: intent.getStringExtra(EXTRA_COLLECTION)
            ?: "Box"
        val userBoxes = intent.getStringArrayListExtra(EXTRA_USER_BOXES)
            ?: intent.getStringArrayListExtra(EXTRA_USER_COLLECTIONS)
            ?: emptyList<String>()

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
                    EditBoxScreen(
                        box = box,
                        userBoxes = userBoxes,
                        onNavigateBack = { finish() },
                        onAction = { action ->
                            val data = Intent().apply {
                                putExtra(EXTRA_ACTION, action)
                                putExtra(EXTRA_BOX, box)
                                putExtra(EXTRA_COLLECTION, box)
                            }
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_BOX = "extra_box"
        const val EXTRA_COLLECTION = EXTRA_BOX
        const val EXTRA_USER_BOXES = "extra_user_boxes"
        const val EXTRA_USER_COLLECTIONS = EXTRA_USER_BOXES
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_ADD_PHOTOS = "action_add_photos"
        const val ACTION_REMOVE_PHOTOS = "action_remove_photos"
        const val ACTION_EDIT_EMOJI = "action_edit_emoji"
        const val ACTION_DELETE_BOX = "action_delete_box"
        const val ACTION_DELETE_COLLECTION = ACTION_DELETE_BOX
    }
}
