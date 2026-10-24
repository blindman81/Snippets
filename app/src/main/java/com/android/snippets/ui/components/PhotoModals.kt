package com.android.snippets.ui.components

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.rememberSnippetBackgroundColor
import com.android.snippets.ui.theme.rememberSnippetTextColor
import com.android.snippets.ui.getSnippetTextStyle
import com.android.snippets.ui.util.LocationUtils
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R
import kotlinx.coroutines.launch

@Composable
fun DeleteConfirmationModal(
    count: Int = 1,
    hasPublicPhotos: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    val view = LocalView.current
    var unpublish by remember { mutableStateOf(true) }
    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (count > 1) "Delete $count photos?" else "Delete photo?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (count > 1) "Deleting these photos also deletes the snippets you created for them." else "Deleting this photo also deletes the snippets you created for this photo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasPublicPhotos) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(
                            checked = unpublish,
                            onCheckedChange = { unpublish = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also remove from public community feed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onConfirm(unpublish)
        }
    )
}

@Composable
fun RateFoodDialog(
    initialRating: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val view = LocalView.current
    var rating by remember { mutableStateOf(initialRating) }

    val scale = remember { Animatable(1f) }
    LaunchedEffect(rating) {
        scale.snapTo(1f)
        scale.animateTo(
            targetValue = 0.75f,
            animationSpec = tween(durationMillis = 80, easing = FastOutLinearInEasing)
        )
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onConfirm(rating)
        },
        title = {
            Text(
                text = "Rate",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            if (rating > 0) rating--
                        },
                        enabled = rating > 0,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease rating",
                            tint = if (rating > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CookieShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                if (rating < 5) rating++
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_rating),
                                contentDescription = "Star icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = rating.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            if (rating < 5) rating++
                        },
                        enabled = rating < 5,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase rating",
                            tint = if (rating < 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun LocationLinkModal(
    initialLink: String?,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    locationNames: Map<String, String> = emptyMap()
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var linkValue by remember { mutableStateOf(initialLink ?: "") }
    val resolvedNames = remember { mutableStateMapOf<String, String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(suggestions, locationNames) {
        resolvedNames.putAll(locationNames)

        suggestions.forEach { suggestion ->
            if (resolvedNames[suggestion] != null) return@forEach

            if (suggestion.startsWith("http://", ignoreCase = true) || suggestion.startsWith("https://", ignoreCase = true)) {
                val localName = LocationUtils.extractPlaceNameFromLink(suggestion)
                if (localName != null) {
                    resolvedNames[suggestion] = LocationUtils.cleanLocationName(localName)
                } else {
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val name = LocationUtils.resolveUrlLocation(suggestion)
                        if (name != null) {
                            resolvedNames[suggestion] = LocationUtils.cleanLocationName(name)
                        }
                    }
                }
            } else {
                resolvedNames[suggestion] = LocationUtils.cleanLocationName(suggestion)
            }
        }
    }

    val filteredSuggestions = remember(linkValue, suggestions, resolvedNames.toMap()) {
        val baseList = if (!initialLink.isNullOrBlank()) {
            suggestions.filter { it.trim().trimEnd('/') != initialLink.trim().trimEnd('/') }
        } else {
            suggestions
        }

        if (linkValue.isBlank()) {
            baseList
        } else {
            val query = linkValue.trim()
            baseList.filter { suggestion ->
                val displayName = resolvedNames[suggestion]
                    ?: LocationUtils.extractPlaceNameFromLink(suggestion)
                    ?: LocationUtils.cleanLocationName(suggestion)
                displayName.contains(query, ignoreCase = true) || suggestion.contains(query, ignoreCase = true)
            }
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val trimmedInput = linkValue.trim()
            val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
            val foundUrl = urlRegex.find(trimmedInput)?.value
            val targetLink = foundUrl ?: trimmedInput

            val rawName = resolvedNames[targetLink]
                ?: resolvedNames[trimmedInput]
                ?: LocationUtils.extractPlaceNameFromLink(trimmedInput)
                ?: LocationUtils.extractPlaceNameFromLink(targetLink)
                ?: if (!targetLink.startsWith("http://", ignoreCase = true) && !targetLink.startsWith("https://", ignoreCase = true)) targetLink else null

            val finalName = rawName?.let { LocationUtils.cleanLocationName(it) }?.takeIf { !it.startsWith("http://", ignoreCase = true) && !it.startsWith("https://", ignoreCase = true) }
            onSave(targetLink, finalName)
        },
        title = {
            Text(
                text = "Link to a place",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paste a Google Maps or location link below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = linkValue,
                    onValueChange = { linkValue = it },
                    placeholder = { Text("https://maps.app.goo.gl/...") },
                    singleLine = true,
                    leadingIcon = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            coroutineScope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val textToPaste = clipEntry?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    ?: run {
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        sysClipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    }
                                if (!textToPaste.isNullOrBlank()) {
                                    val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
                                    val extractedUrl = urlRegex.find(textToPaste)?.value
                                    val extractedName = LocationUtils.extractPlaceNameFromLink(textToPaste)
                                    val targetLink = extractedUrl ?: textToPaste.trim()
                                    linkValue = targetLink
                                    if (extractedName != null) {
                                        resolvedNames[targetLink] = LocationUtils.cleanLocationName(extractedName)
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (linkValue.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                linkValue = ""
                            }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (filteredSuggestions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            val displayName = remember(suggestion, resolvedNames[suggestion]) {
                                resolvedNames[suggestion] ?: LocationUtils.extractPlaceNameFromLink(suggestion) ?: LocationUtils.cleanLocationName(suggestion)
                            }
                            SuggestionChip(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    linkValue = suggestion
                                },
                                label = {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun NamePhotoModal(
    initialName: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var nameValue by remember { mutableStateOf(initialName ?: "") }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onSave(nameValue.trim().takeIf { it.isNotBlank() })
        },
        title = {
            Text(
                text = "Name photo",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameValue,
                    onValueChange = {
                        if (it.length <= 25) {
                            nameValue = it
                        }
                    },
                    placeholder = { Text("Photo name") },
                    singleLine = true,
                    leadingIcon = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            coroutineScope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val textToPaste = clipEntry?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    ?: run {
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        sysClipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    }
                                if (!textToPaste.isNullOrBlank()) {
                                    nameValue = textToPaste.trim().replace("\n", " ").replace("\r", "").take(25)
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (nameValue.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    nameValue = ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "${nameValue.length}/25",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nameValue.length >= 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (nameValue.isNotEmpty()) 36.dp else 12.dp)
                )
            }
        }
    )
}

@Composable
fun SnippetsModal(
    photo: Photo,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: SnippetsViewModel
) {
    var localSnippets by remember(photo.snippets) { mutableStateOf(photo.snippets) }
    val view = LocalView.current

    BlurredAlertDialog(
        onDismissRequest = onClose,
        onConfirm = onClose,
        title = {
            Text(
                "Snippets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (localSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No snippets yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localSnippets.forEach { snippet ->
                        val forcedStyle = viewModel.getSnippetStyle(snippet)
                        val snippetColor = rememberSnippetBackgroundColor()
                        val bgColor = snippetColor
                        val textColor = rememberSnippetTextColor()

                        Surface(shape = CircleShape, color = bgColor, border = null) {
                            Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = snippet,
                                    style = getSnippetTextStyle(
                                        forcedStyle,
                                        MaterialTheme.typography.titleMedium,
                                        isCloud = true
                                    ),
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    onRemove(snippet)
                                    localSnippets = localSnippets - snippet
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, null, tint = textColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AddSnippetsModal(
    photo: Photo,
    onAdd: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf("") }

    val filteredSuggestions = remember(text, viewModel.allUniqueSnippets, photo.snippets) {
        val existing = photo.snippets.toSet()
        if (text.isBlank()) {
            viewModel.allUniqueSnippets.filter { it !in existing }
        } else {
            viewModel.allUniqueSnippets.filter {
                it !in existing && it.contains(text, ignoreCase = true) && !it.equals(text, ignoreCase = true)
            }
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onClose,
        onConfirm = {
            if (text.isNotBlank()) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onAdd(text.trim())
                onClose()
            }
        },
        title = {
            Text(
                text = "Add a Snippet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            if (it.length <= 15) {
                                text = it
                            }
                        },
                        placeholder = { Text("Enter snippet") },
                        singleLine = true,
                        trailingIcon = if (text.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        text = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (text.isNotBlank()) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onAdd(text.trim())
                                onClose()
                            }
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "${text.length}/15",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (text.length >= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 12.dp)
                    )
                }

                if (filteredSuggestions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    text = suggestion
                                },
                                label = {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TextSnippet,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    )
}
