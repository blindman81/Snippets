package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddReaction
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.AnimatedCookieButton
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun EmojiPickerScreen(viewModel: SnippetsViewModel) {
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val initialEmoji = remember(viewModel.editingCollectionIcon) {
        viewModel.editingCollectionIcon?.let { viewModel.collectionIcons[it] } ?: ""
    }
    val textFieldState = rememberTextFieldState(initialEmoji)

    LaunchedEffect(initialEmoji) {
        if (textFieldState.text.toString() != initialEmoji) {
            textFieldState.setTextAndPlaceCursorAtEnd(initialEmoji)
        }
    }

    val onConfirm = remember(textFieldState, viewModel, view) {
        {
            val selectedEmoji = textFieldState.text.toString()
            if (selectedEmoji.isNotBlank()) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                val collection = viewModel.editingCollectionIcon
                if (collection != null) {
                    viewModel.updateCollectionIcon(collection, selectedEmoji)
                    viewModel.currentScreen = viewModel.previousScreen
                } else {
                    val emoji = selectedEmoji.trim()
                    viewModel.createCollection(emoji, openAfterCreate = true)
                    viewModel.updateCollectionIcon(emoji, selectedEmoji)
                    viewModel.libraryCurrentTab = emoji
                    viewModel.navigateLibrary()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddReaction,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Pick an Emoji",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 8.dp,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedCookieButton(
                            onClick = { 
                                viewModel.currentScreen = viewModel.previousScreen
                            },
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            size = 56.dp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        val isEmojiSelected = textFieldState.text.isNotBlank()
                        AnimatedCookieButton(
                            onClick = onConfirm,
                            icon = Icons.Rounded.Check,
                            contentDescription = "Confirm",
                            tooltip = "Done",
                            size = 56.dp,
                            enabled = isEmojiSelected,
                            containerColor = if (isEmojiSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isEmojiSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            textFieldState.clearText()
                            focusRequester.requestFocus() 
                            keyboardController?.show()
                        }
                ) {
                    // 9-Sided Cookie Background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CookieShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )

                    // Icon inside the cookie
                    if (textFieldState.text.isEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.AddReaction,
                            contentDescription = "Tap to select emoji",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    } else {
                        // Static Emoji on top
                        Text(
                            text = textFieldState.text.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                            modifier = Modifier.wrapContentSize(unbounded = true)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hidden TextField to trigger keyboard
                BasicTextField(
                    state = textFieldState,
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(focusRequester),
                    inputTransformation = EmojiInputTransformation,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        onConfirm()
                    }
                )
            }
        }
    }
}

private object EmojiInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val emojiOnly = asCharSequence().filter { char ->
            val type = Character.getType(char).toInt()
            type == Character.SURROGATE.toInt() || 
            type == Character.OTHER_SYMBOL.toInt() ||
            char.code > 0x2000 // Catch many more complex emojis
        }
        if (emojiOnly.length > 8) {
            revertAllChanges()
        } else if (emojiOnly != asCharSequence()) {
            replace(0, length, emojiOnly)
        }
    }
}
