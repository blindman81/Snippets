package com.android.snippets.ui.components

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.util.LocationUtils
import kotlinx.coroutines.launch

@Composable
fun LocationLinkModal(
    initialLink: String?,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    locationNames: Map<String, String> = emptyMap(),
    initialName: String? = null
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    val startingName = remember(initialName, initialLink, locationNames) {
        val direct = initialName?.trim()?.takeIf { it.isNotBlank() }
            ?: initialLink?.let { locationNames[it] }
            ?: initialLink?.let { LocationUtils.extractPlaceNameFromLink(it, context) }
        if (!direct.isNullOrBlank()) {
            LocationUtils.cleanLocationName(direct)
        } else if (initialLink != null && !initialLink.startsWith("http://", ignoreCase = true) && !initialLink.startsWith("https://", ignoreCase = true)) {
            LocationUtils.cleanLocationName(initialLink)
        } else {
            ""
        }
    }

    var textValue by remember { mutableStateOf(startingName) }
    var targetLink by remember { mutableStateOf(initialLink?.trim()?.takeIf { it.isNotBlank() }) }
    var isResolving by remember { mutableStateOf(false) }
    val resolvedNames = remember { mutableStateMapOf<String, String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(suggestions, locationNames) {
        resolvedNames.putAll(locationNames)

        suggestions.forEach { suggestion ->
            if (resolvedNames[suggestion] != null) return@forEach

            if (suggestion.startsWith("http://", ignoreCase = true) || suggestion.startsWith("https://", ignoreCase = true)) {
                val localName = LocationUtils.extractPlaceNameFromLink(suggestion, context)
                if (localName != null) {
                    resolvedNames[suggestion] = LocationUtils.cleanLocationName(localName)
                } else {
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val name = LocationUtils.resolveUrlLocation(suggestion, context)
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

    // If modal starts with a URL link but no initial name, resolve it in the background
    LaunchedEffect(initialLink) {
        if (!initialLink.isNullOrBlank() && textValue.isBlank()) {
            val immediate = resolvedNames[initialLink]
                ?: LocationUtils.extractPlaceNameFromLink(initialLink, context)
            if (!immediate.isNullOrBlank()) {
                val clean = LocationUtils.cleanLocationName(immediate)
                textValue = clean
                resolvedNames[initialLink] = clean
            } else if (initialLink.startsWith("http://", ignoreCase = true) || initialLink.startsWith("https://", ignoreCase = true)) {
                isResolving = true
                val resolved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    LocationUtils.resolveUrlLocation(initialLink, context)
                }
                isResolving = false
                if (!resolved.isNullOrBlank()) {
                    val clean = LocationUtils.cleanLocationName(resolved)
                    textValue = clean
                    resolvedNames[initialLink] = clean
                }
            }
        }
    }

    val handleInput: (String) -> Unit = { rawInput ->
        val trimmed = rawInput.trim()
        val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
        val urlMatch = urlRegex.find(rawInput)

        if (urlMatch != null) {
            val foundUrl = urlMatch.value
            targetLink = foundUrl

            val immediateName = resolvedNames[foundUrl]
                ?: resolvedNames[trimmed]
                ?: LocationUtils.extractPlaceNameFromLink(rawInput, context)
                ?: LocationUtils.extractPlaceNameFromLink(foundUrl, context)

            if (!immediateName.isNullOrBlank()) {
                val clean = LocationUtils.cleanLocationName(immediateName)
                textValue = clean
                resolvedNames[foundUrl] = clean
                isResolving = false
            } else {
                textValue = ""
                isResolving = true
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val resolved = LocationUtils.resolveUrlLocation(foundUrl, context)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isResolving = false
                        if (!resolved.isNullOrBlank() && LocationUtils.isValidPlaceName(resolved)) {
                            val clean = LocationUtils.cleanLocationName(resolved)
                            textValue = clean
                            resolvedNames[foundUrl] = clean
                        }
                    }
                }
            }
        } else {
            textValue = rawInput
            if (targetLink?.startsWith("http", ignoreCase = true) == true) {
                targetLink = null
            }
        }
    }

    val filteredSuggestions = remember(textValue, targetLink, suggestions, resolvedNames.toMap()) {
        val baseList = if (!initialLink.isNullOrBlank()) {
            suggestions.filter { it.trim().trimEnd('/') != initialLink.trim().trimEnd('/') }
        } else {
            suggestions
        }

        if (textValue.isBlank()) {
            baseList
        } else {
            val query = textValue.trim()
            baseList.filter { suggestion ->
                val displayName = resolvedNames[suggestion]
                    ?: LocationUtils.extractPlaceNameFromLink(suggestion, context)
                    ?: LocationUtils.cleanLocationName(suggestion)
                displayName.contains(query, ignoreCase = true) || suggestion.contains(query, ignoreCase = true)
            }
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        moveUpOnKeyboardOverlap = true,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val trimmedName = textValue.trim().takeIf { it.isNotBlank() && LocationUtils.isValidPlaceName(it) }
            val finalLink = targetLink?.trim()?.takeIf { it.isNotBlank() } ?: trimmedName
            onSave(finalLink ?: "", trimmedName)
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
                    text = "Enter a place name or paste a Google Maps link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { handleInput(it) },
                    label = { Text("Place or location") },
                    placeholder = {
                        Text(if (isResolving) "Finding place name..." else "Enter place name (or paste Maps link)")
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Rounded.Place, contentDescription = null)
                    },
                    trailingIcon = {
                        if (isResolving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (textValue.isNotEmpty() || !targetLink.isNullOrBlank()) {
                            IconButton(onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                textValue = ""
                                targetLink = null
                            }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        } else {
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
                                        handleInput(textToPaste)
                                    }
                                }
                            }) {
                                Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (!targetLink.isNullOrBlank() && (targetLink?.startsWith("http://", ignoreCase = true) == true || targetLink?.startsWith("https://", ignoreCase = true) == true)) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "Linked: " + targetLink!!.removePrefix("https://").removePrefix("http://"),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    targetLink = null
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove link", modifier = Modifier.size(14.dp))
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }

                if (filteredSuggestions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            val displayName = remember(suggestion, resolvedNames[suggestion]) {
                                resolvedNames[suggestion] ?: LocationUtils.extractPlaceNameFromLink(suggestion, context) ?: LocationUtils.cleanLocationName(suggestion)
                            }
                            SuggestionChip(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    textValue = displayName
                                    targetLink = suggestion
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
