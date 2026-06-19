$path = "C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\DetailComponents.kt"
$content = [System.IO.File]::ReadAllText($path)

# Normalize newlines for matching
$content = $content -replace "`r`n", "`n"

$c1_old = "fun CloudSnippetItem(
    text: String, 
    index: Int, 
    totalCount: Int, 
    photoColors: List<Int> = emptyList(),
    forcedColor: Int? = null,
    forcedStyle: com.android.snippets.viewmodel.SnippetStyle? = null
) {"
$c1_new = "fun CloudSnippetItem(
    text: String, 
    index: Int, 
    totalCount: Int, 
    photoColors: List<Int> = emptyList(),
    forcedColor: Int? = null,
    forcedStyle: com.android.snippets.viewmodel.SnippetStyle? = null,
    forcedShape: com.android.snippets.viewmodel.SnippetShape? = null
) {"

$c2_old = "        Surface(
            color = containerColor,
            shape = CircleShape,
            border = if (borderColor != null) BorderStroke(1.5.dp, borderColor) else null
        ) {"
$c2_new = "        Surface(
            color = containerColor,
            shape = com.android.snippets.ui.components.snippetShapeFor(forcedShape ?: com.android.snippets.viewmodel.SnippetShape.Default),
            border = if (borderColor != null) BorderStroke(1.5.dp, borderColor) else null
        ) {"

$c3_old = "fun AddSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onAdd: (String, Int, com.android.snippets.viewmodel.SnippetStyle) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf(`"`") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
    var selectedStyle by remember { mutableStateOf(com.android.snippets.viewmodel.SnippetStyle.Default) }
    var currentStep by remember { androidx.compose.runtime.mutableIntStateOf(0) }"

$c3_new = "fun AddSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onAdd: (String, Int, com.android.snippets.viewmodel.SnippetStyle, com.android.snippets.viewmodel.SnippetShape) -> Unit,
    onClose: () -> Unit,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf(`"`") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
    var selectedStyle by remember { mutableStateOf(com.android.snippets.viewmodel.SnippetStyle.Default) }
    var selectedShape by remember { mutableStateOf(com.android.snippets.viewmodel.SnippetShape.Default) }
    var currentStep by remember { androidx.compose.runtime.mutableIntStateOf(0) }"

$c4_old = "                    text = when(currentStep) {
                        0 -> `"Add a Snippet`"
                        1 -> `"Choose a color`"
                        else -> `"Choose a style`"
                    },"
$c4_new = "                    text = when(currentStep) {
                        0 -> `"Add a Snippet`"
                        1 -> `"Choose a color`"
                        2 -> `"Choose a style`"
                        else -> `"Choose a shape`"
                    },"

$c5_old = "                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))"
$c5_new = "                                }
                                3 -> { // Shape Step
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.height(280.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(8.dp),
                                        userScrollEnabled = true
                                    ) {
                                        items(com.android.snippets.viewmodel.SnippetShape.values()) { shape ->
                                            val isSelected = selectedShape == shape
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                    selectedShape = shape
                                                }
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(60.dp),
                                                    shape = com.android.snippets.ui.components.snippetShapeFor(shape),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                    border = if (isSelected) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = `"Aa`",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = com.android.snippets.ui.components.snippetShapeLabel(shape),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(top = 8.dp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))"

$c6_old = "                // Footer Action
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    enabled = currentStep > 0 || text.isNotBlank(),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (currentStep < 2) {
                            currentStep++
                        } else {
                            val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                            onAdd(text.trim(), finalColor, selectedStyle)
                            onClose()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep < 2) `"Next`" else `"Done`",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (currentStep < 2) {"
$c6_new = "                // Footer Action
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    enabled = currentStep > 0 || text.isNotBlank(),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                            onAdd(text.trim(), finalColor, selectedStyle, selectedShape)
                            onClose()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep < 3) `"Next`" else `"Done`",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (currentStep < 3) {"

$content = $content.Replace($c1_old, $c1_new)
$content = $content.Replace($c2_old, $c2_new)
$content = $content.Replace($c3_old, $c3_new)
$content = $content.Replace($c4_old, $c4_new)
$content = $content.Replace($c5_old, $c5_new)
$content = $content.Replace($c6_old, $c6_new)

[System.IO.File]::WriteAllText($path, $content)
Write-Host "Success"
