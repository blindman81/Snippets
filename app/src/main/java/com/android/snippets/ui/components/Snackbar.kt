package com.android.snippets.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.android.snippets.viewmodel.SnippetsViewModel

/**
 * Standard Jetpack Compose Material 3 [Snackbar] with swipe-to-dismiss behavior using [SwipeToDismissBox].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    key(snackbarData) {
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                snackbarData.dismiss()
            }
        }
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            content = {
                Snackbar(
                    snackbarData = snackbarData,
                    modifier = modifier
                )
            }
        )
    }
}

/**
 * App-level SnackbarHost configured with [SnippetsSnackbar].
 */
@Composable
fun SnippetsSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            SnippetsSnackbar(snackbarData = snackbarData)
        }
    )
}

/**
 * Handles observing [SnippetsViewModel.snackbarToken] to show and dispatch snackbars.
 */
@Composable
fun SnippetsSnackbarEffect(
    viewModel: SnippetsViewModel,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(viewModel.snackbarToken) {
        if (viewModel.snackbarToken != 0L) {
            val message = viewModel.snackbarMessage ?: return@LaunchedEffect
            val actionLabel = viewModel.snackbarActionLabel
            val onAction = viewModel.onSnackbarAction
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
            viewModel.snackbarMessage = null
            viewModel.snackbarActionLabel = null
            viewModel.onSnackbarAction = null
        }
    }
}
