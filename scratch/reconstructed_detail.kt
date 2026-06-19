$brainDir = "C:\Users\mendu\.gemini\antigravity\brain"
$files = Get-ChildItem -Path $brainDir -Filter transcript.jsonl -Recurse

Write-Host "Scanning $($files.Count) transcript files for write_to_file/replace_file_content..."

foreach ($file in $files) {
    $lines = Get-Content $file.FullName
    foreach ($line in $lines) {
        if ($line -like "*DetailComponents.kt*" -and ($line -like "*write_to_file*" -or $line -like "*write_file*")) {
            try {
                $data = $line | ConvertFrom-Json
                # Check tool_calls
                if ($data.tool_calls) {
                    foreach ($call in $data.tool_calls) {
                        if ($call.name -eq "write_to_file" -and $call.args.TargetFile -like "*DetailComponents.kt*") {
                            $code = $call.args.CodeContent
                            if ($code -and $code.Length -gt 20000) {
                                $code | Set-Content "C:\Users\mendu\Snippets\scratch\recovered_write.txt" -Encoding UTF8
                                Write-Host "Successfully recovered full file from write_to_file in $($file.FullName)"
                                break
                            }
                        }
                    }
                }
            } catch {
                # Ignore json parse errors
            }
        }
    }
}

                extracted.append(l)

    with open("C:/Users/mendu/Snippets/extracted.txt", "w", encoding='utf-8') as f:
        f.write("\n".join(extracted))

    with open(out_path, 'r', encoding='utf-8') as f:
        current_content = f.read()

    # The file is mangled around line 297.
    # We want to replace everything from `        else -> base\n    }\n}` onwards
    # Actually, the file currently ends abruptly or is missing 650 lines.
    # Let's just find the `    }\n}` and stitch it together.
    import re
    match = re.search(r'(?s)(.*?else -> base\s+?}\s+?})', current_content)
    if match:
        top_part = match.group(1)
        
        # The bottom part (unchanged lines) that the diff showed as remaining:
        #                                 ),
        #                                 label = "offset"
        #                             )
        # We need to find this in current_content.
        bot_idx = current_content.find('                                ),\n                                label = "offset"\n                            )')

// --- SHARED ARTISTIC COMPONENTS ---

@Composable
fun DetailTopBar(
// --- SHARED ARTISTIC COMPONENTS ---

@Composable
fun DetailTopBar(
    photo: com.android.snippets.model.Photo,
    viewModel: SnippetsViewModel,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    val collectionName = photo.collections.firstOrNull()
    val isFavorite = photo.isFavorite

    val titleText = when {
        collectionName != null -> collectionName
        isFavorite -> "Favorites"
        else -> "Library"
    }

    val textLength = titleText.length
    val targetTextMaxWidth = if (isScrolled) {
        (textLength * 8).dp.coerceIn(60.dp, 120.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 24.dp, top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedCookieButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tooltip = "Back",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    isSpinning = isSpinning,
                    hapticOnHold = true,
                    shape = CookieShape,
                    size = 56.dp
                )
            }
        }
    }
}

@Composable
fun DetailBottomBar(
    hasSnippets: Boolean,
    onAdd: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
        shape = RectangleShape,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .then(
                    if (animatedVisibilityScope != null) {
                        with(animatedVisibilityScope) {
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tooltip = "Back",
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 56.dp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Scroll-in photo thumbnail (48dp rectangle with white border)
                AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                        initialOffsetX = { -it }
                    ),
                    exit = fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                        animationSpec = tween(180),
                    onAdd()
                },
                dropdownContent = { closeMenu ->
                    val menuGroupShape = RoundedCornerShape(12.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Group 1: Download, Share, Favorite
                        Surface(
                            shape = menuGroupShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .let { mod ->
                            if (hazeState != null) {
                                mod.hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        blurRadius = 24.dp,
                                        noiseFactor = 0.05f,
                                        tint = HazeTint(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                                    )
                                )
                            } else {
                                mod
                            }
                        }
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.currentScreen = com.android.snippets.viewmodel.Screen.Collections
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = pillPaddingHorizontal, vertical = pillPaddingVertical)
                    ) {
                        when {
                            collectionName != null -> {
                                CollectionIcon(
                                    icon = viewModel.getCollectionIcon(collectionName),
                                    modifier = Modifier.size(pillIconSize),
                                    tint = MaterialTheme.colorScheme.primary
                    ) {
                        when {
                            collectionName != null -> {
                                CollectionIcon(
                                    icon = viewModel.getCollectionIcon(collectionName),
// MISSING LINE 186
// MISSING LINE 187
// MISSING LINE 188
// MISSING LINE 189
// MISSING LINE 190
// MISSING LINE 191
// MISSING LINE 192
// MISSING LINE 193
// MISSING LINE 194
// MISSING LINE 195
// MISSING LINE 196
// MISSING LINE 197
// MISSING LINE 198
// MISSING LINE 199
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(pillSpacerSize))
                                Text(
                                    text = "Favorites",
                                    style = com.android.snippets.ui.theme.titleEmphasized.copy(
                                        fontSize = pillTextSize.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = textMaxWidth)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = LibraryIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(pillIconSize),
                    isSpinning = isSpinning,
                    size = 48.dp
                )
            }
        }
                AnimatedCookieButton(
                    onClick = onDelete,
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tooltip = "Delete",
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    isSpinning = isSpinning,
                    size = 56.dp
                )
            }
        }
    }
}


            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = com.android.snippets.ui.shapes.AdaptivePuffyShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 4.dp,
            tonalElevation = 4.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

            AnimatedCookieButton(
                onClick = onDelete,
                icon = Icons.Default.Delete,
                contentDescription = "Delete",
                tooltip = "Delete",
                isSpinning = isSpinning
            )
        }
    }
}

@Composable
fun DetailBottomBar(
    hasSnippets: Boolean,
            expansionSpinTrigger = true
            kotlinx.coroutines.delay(1000) // Keep true long enough for animation
            expansionSpinTrigger = false
        }
    }

@Composable
fun getSnippetTextStyle(
    style: com.android.snippets.viewmodel.SnippetStyle,
    base: androidx.compose.ui.text.TextStyle,
                        }
                    }
                }
            }

            Surface(
                shape = CookieShape,
                color = if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                border = BorderStroke(
                    width = 1.dp,
        )
        com.android.snippets.viewmodel.SnippetStyle.Condensed -> base.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily(typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD))
        )
        com.android.snippets.viewmodel.SnippetStyle.Bold -> base.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily(typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL))
            expansionSpinTrigger = false
        }
    }

    Surface(
                        } else {
                            mod
                        }
                    }
            ) {
    val personality = stableRandom.nextInt(0, 5)
    val colorStrategy = (index + stableRandom.nextInt(0, 10)) % 3
    val rotation = stableRandom.nextInt(-5, 5).toFloat()

    // Contrast-aware Snippet Color
    val isDark = !MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue > 1.5f }
    val baseSnippetColor = if (forcedColor != null) {
        Color(forcedColor)
    } else {
        when (colorStrategy) {
            0 -> { // 1. Photo Derived
                if (photoColors.isNotEmpty()) Color(photoColors[stableRandom.nextInt(photoColors.size)])
                else MaterialTheme.colorScheme.primary
            }
            1 -> { // 2. System Themed
                val themeColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val shape = com.android.snippets.ui.shapes.AnimatedPuffyShape(
        phase = phaseAnim.value,
        bounds = bounds,
        obstacles = registry.obstacles.toList()
    )
    Surface(
        shape = shape,
        color = barColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
            .padding(horizontal = 8.dp)
            .width(barWidth)
            .height(80.dp)
    ) {
        AnimatedContent(
            targetState = hasSnippets,
            transitionSpec = {
                Motion.bottomBarSwap(this)
            },
            contentAlignment = Alignment.Center,
            label = "bottom_bar_swap"
        ) { snippetsExist ->
            if (snippetsExist) {

    val containerColor = when (personality) {
        1 -> snippetColor.copy(alpha = 0.15f)
        2 -> Color.Transparent
        3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> snippetColor.copy(alpha = 0.08f)
    }
    
    val borderColor = if (personality == 2) snippetColor.copy(alpha = 0.4f) else null
        3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> snippetColor.copy(alpha = 0.08f)
    }
    
    val borderColor = if (personality == 2) snippetColor.copy(alpha = 0.4f) else null

    Box(modifier = Modifier.rotateWithBounds(rotation)) {
        Surface(
            color = containerColor,
            shape = CircleShape,
        )
        else -> base
    }
}

@Composable
fun CloudSnippetItem(
    text: String, 
    index: Int, 
    totalCount: Int, 
    photoColors: List<Int> = emptyList(),
    forcedColor: Int? = null,
    forcedStyle: com.android.snippets.viewmodel.SnippetStyle? = null
) {
    val stableRandom = remember(text) { Random(text.hashCode()) }
    val personality = stableRandom.nextInt(0, 5)
    val colorStrategy = (index + stableRandom.nextInt(0, 10)) % 3
    val rotation = stableRandom.nextInt(-5, 5).toFloat()

    // Contrast-aware Snippet Color
    val isDark = !MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue > 1.5f }
    val baseSnippetColor = if (forcedColor != null) {
        Color(forcedColor)
    } else {
        when (colorStrategy) {
            0 -> { // 1. Photo Derived
                if (photoColors.isNotEmpty()) Color(photoColors[stableRandom.nextInt(photoColors.size)])
                else MaterialTheme.colorScheme.primary
            }
            1 -> { // 2. System Themed
                val themeColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                themeColors[stableRandom.nextInt(themeColors.size)]
            }
            else -> { // 3. Vivid Pop
                val vividColors = if (isDark) listOf(
                    Color(0xFFFF8A65), // Light Deep Orange
                    Color(0xFFF06292), // Light Pink
                    Color(0xFFBA68C8), // Light Purple
                    Color(0xFF4DD0E1), // Light Cyan
                    Color(0xFF81C784), // Light Green
                    Color(0xFFFFD54F)  // Light Amber
                ) else listOf(
                    Color(0xFFD84315), // Dark Deep Orange
                    Color(0xFFC2185B), // Dark Pink
                    Color(0xFF7B1FA2), // Dark Purple
                    Color(0xFF0097A7), // Dark Cyan
                    Color(0xFF388E3C), // Dark Green
                    Color(0xFFFFA000)  // Dark Amber
                )
                vividColors[stableRandom.nextInt(vividColors.size)]
            }
        }
    }

    // Ensure it's not too close to black/white clashing with the theme
    val snippetColor = remember(baseSnippetColor, isDark) {
// MISSING LINE 441
// MISSING LINE 442
// MISSING LINE 443
// MISSING LINE 444
// MISSING LINE 445
// MISSING LINE 446
// MISSING LINE 447
// MISSING LINE 448
// MISSING LINE 449
// MISSING LINE 450
// MISSING LINE 451
// MISSING LINE 452
// MISSING LINE 453
// MISSING LINE 454
// MISSING LINE 455
// MISSING LINE 456
// MISSING LINE 457
// MISSING LINE 458
// MISSING LINE 459
    val view = LocalView.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    onClose()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        var animateScale by remember { mutableStateOf(0.92f) }
        LaunchedEffect(Unit) {
            animateScale = 1.0f
        }
        val scaleFactor by animateFloatAsState(
            targetValue = animateScale,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            label = "card_scale"
        )

        Surface(
            shape = RoundedCornerShape(48.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .widthIn(max = 400.dp)
                .graphicsLayer {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Stop click propagation to the tappable background overlay */ },
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Snippets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    AnimatedCookieButton(
                         onClick = onClose,
                         icon = Icons.Default.Close,
                         contentDescription = "Close",
                         tooltip = "Close",
                         size = 40.dp,
                         containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                         contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
// MISSING LINE 521
// MISSING LINE 522
// MISSING LINE 523
// MISSING LINE 524
// MISSING LINE 525
// MISSING LINE 526
// MISSING LINE 527
// MISSING LINE 528
// MISSING LINE 529
// MISSING LINE 530
// MISSING LINE 531
// MISSING LINE 532
// MISSING LINE 533
// MISSING LINE 534
// MISSING LINE 535
// MISSING LINE 536
// MISSING LINE 537
// MISSING LINE 538
// MISSING LINE 539
// MISSING LINE 540
// MISSING LINE 541
// MISSING LINE 542
// MISSING LINE 543
// MISSING LINE 544
// MISSING LINE 545
// MISSING LINE 546
// MISSING LINE 547
// MISSING LINE 548
// MISSING LINE 549
// MISSING LINE 550
// MISSING LINE 551
// MISSING LINE 552
// MISSING LINE 553
// MISSING LINE 554
// MISSING LINE 555
// MISSING LINE 556
// MISSING LINE 557
// MISSING LINE 558
// MISSING LINE 559
// MISSING LINE 560
// MISSING LINE 561
// MISSING LINE 562
// MISSING LINE 563
// MISSING LINE 564
// MISSING LINE 565
// MISSING LINE 566
// MISSING LINE 567
// MISSING LINE 568
// MISSING LINE 569
// MISSING LINE 570
// MISSING LINE 571
// MISSING LINE 572
// MISSING LINE 573
// MISSING LINE 574
// MISSING LINE 575
// MISSING LINE 576
// MISSING LINE 577
// MISSING LINE 578
// MISSING LINE 579
// MISSING LINE 580
// MISSING LINE 581
// MISSING LINE 582
// MISSING LINE 583
// MISSING LINE 584
// MISSING LINE 585
// MISSING LINE 586
// MISSING LINE 587
// MISSING LINE 588
// MISSING LINE 589
// MISSING LINE 590
// MISSING LINE 591
// MISSING LINE 592
// MISSING LINE 593
// MISSING LINE 594
) {
    val view = LocalView.current
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
    var selectedStyle by remember { mutableStateOf(com.android.snippets.viewmodel.SnippetStyle.Default) }
    var currentStep by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    
    val snippetColorsPalette = remember {
        listOf(
            0xFFEF5350.toInt(), // Red
            0xFFEC407A.toInt(), // Pink
            0xFFAB47BC.toInt(), // Purple
            0xFF42A5F5.toInt(), // Blue
            0xFF26A69A.toInt(), // Teal
            0xFF66BB6A.toInt(), // Green
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddSnippetsModal(
    photo: com.android.snippets.model.Photo,
    onAdd: (String, Int, com.android.snippets.viewmodel.SnippetStyle) -> Unit,
    onClose: () -> Unit,
            0xFFFFA726.toInt(), // Orange
            0xFF8D6E63.toInt(), // Brown
            0xFF78909C.toInt(), // Slate
            0xFFD4E157.toInt()  // Lime
        )
    }

    val localSnippetsCount = photo.snippets.size

    ModalBottomSheet(
    val snippetColorsPalette = remember {
        listOf(
            0xFFEF5350.toInt(), // Red
            0xFFEC407A.toInt(), // Pink
            0xFFAB47BC.toInt(), // Purple
            0xFF42A5F5.toInt(), // Blue
            0xFF26A69A.toInt(), // Teal
            0xFF66BB6A.toInt(), // Green
            0xFFFFEE58.toInt(), // Yellow
            0xFFFFA726.toInt(), // Orange
            0xFF8D6E63.toInt(), // Brown
            0xFF78909C.toInt(), // Slate
            0xFFD4E157.toInt()  // Lime
        )
    }
        )
    }

    val localSnippetsCount = photo.snippets.size

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = BottomSheetDefaults.ScrimColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
                    )
                }
                2 -> { // Outline Border Pill
                    Text(
                        text = text, 
                        modifier = Modifier.padding(horizontal = (20 * scalingFactor).dp, vertical = (10 * scalingFactor).dp), 
                        style = getSnippetTextStyle(forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default, MaterialTheme.typography.headlineSmall, isCloud = true).copy(fontSize = (MaterialTheme.typography.headlineSmall.fontSize.value * scalingFactor).sp), 
                        color = snippetColor

            ButtonGroup(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                overflowIndicator = {}
            ) {
                options.forEachIndexed { index, label ->
                    toggleableItem(
                        checked = index == selectedIndex,
                        onCheckedChange = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            selectedIndex = index
                        },
                        label = label
                    )
                }
            }

            }
        }
    }
}

@Composable
fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    AnimatedCookieButton(
        onClick = onClick,
        icon = icon,
        contentColor = tint
    )
}

@Composable
fun HeaderActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    val view = LocalView.current
    Surface(
        modifier = Modifier.size(44.dp), 
        shape = CircleShape, 
        color = Color.Transparent, 
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick()
        }
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp)) }
    }
}

// --- ACTIVE STATE WRITING HUB ---

@Composable
fun CurrentSnippetsModal(
    photo: com.android.snippets.model.Photo,
// MISSING LINE 721
// MISSING LINE 722
// MISSING LINE 723
// MISSING LINE 724
// MISSING LINE 725
// MISSING LINE 726
// MISSING LINE 727
// MISSING LINE 728
// MISSING LINE 729
// MISSING LINE 730
// MISSING LINE 731
// MISSING LINE 732
// MISSING LINE 733
// MISSING LINE 734
// MISSING LINE 735
// MISSING LINE 736
// MISSING LINE 737
// MISSING LINE 738
// MISSING LINE 739
// MISSING LINE 740
// MISSING LINE 741
// MISSING LINE 742
// MISSING LINE 743
// MISSING LINE 744
// MISSING LINE 745
// MISSING LINE 746
// MISSING LINE 747
// MISSING LINE 748
// MISSING LINE 749
// MISSING LINE 750
// MISSING LINE 751
// MISSING LINE 752
// MISSING LINE 753
// MISSING LINE 754
// MISSING LINE 755
// MISSING LINE 756
// MISSING LINE 757
// MISSING LINE 758
// MISSING LINE 759
// MISSING LINE 760
// MISSING LINE 761
// MISSING LINE 762
// MISSING LINE 763
// MISSING LINE 764
// MISSING LINE 765
// MISSING LINE 766
// MISSING LINE 767
// MISSING LINE 768
// MISSING LINE 769
// MISSING LINE 770
// MISSING LINE 771
// MISSING LINE 772
// MISSING LINE 773
// MISSING LINE 774
// MISSING LINE 775
// MISSING LINE 776
// MISSING LINE 777
// MISSING LINE 778
// MISSING LINE 779
// MISSING LINE 780
// MISSING LINE 781
// MISSING LINE 782
// MISSING LINE 783
// MISSING LINE 784
// MISSING LINE 785
// MISSING LINE 786
// MISSING LINE 787
// MISSING LINE 788
// MISSING LINE 789
// MISSING LINE 790
// MISSING LINE 791
// MISSING LINE 792
// MISSING LINE 793
// MISSING LINE 794
// MISSING LINE 795
// MISSING LINE 796
// MISSING LINE 797
// MISSING LINE 798
// MISSING LINE 799
// MISSING LINE 800
// MISSING LINE 801
// MISSING LINE 802
// MISSING LINE 803
// MISSING LINE 804
// MISSING LINE 805
// MISSING LINE 806
// MISSING LINE 807
// MISSING LINE 808
// MISSING LINE 809
// MISSING LINE 810
// MISSING LINE 811
// MISSING LINE 812
// MISSING LINE 813
// MISSING LINE 814
// MISSING LINE 815
// MISSING LINE 816
// MISSING LINE 817
// MISSING LINE 818
// MISSING LINE 819
// MISSING LINE 820
// MISSING LINE 821
// MISSING LINE 822
// MISSING LINE 823
// MISSING LINE 824

    val localSnippetsCount = photo.snippets.size
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    onClose()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        var animateScale by remember { mutableStateOf(0.92f) }
        LaunchedEffect(Unit) {
            animateScale = 1.0f
        }
        val scaleFactor by animateFloatAsState(
            targetValue = animateScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "card_scale"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
// MISSING LINE 861
// MISSING LINE 862
// MISSING LINE 863
// MISSING LINE 864
// MISSING LINE 865
// MISSING LINE 866
// MISSING LINE 867
// MISSING LINE 868
// MISSING LINE 869
// MISSING LINE 870
// MISSING LINE 871
// MISSING LINE 872
// MISSING LINE 873
// MISSING LINE 874
// MISSING LINE 875
// MISSING LINE 876
// MISSING LINE 877
// MISSING LINE 878
// MISSING LINE 879
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                        }
                                    }
                                }
                            }
                        }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                enabled = text.isNotBlank(),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                    onAdd(text.trim(), finalColor, selectedStyle)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                    onAdd(text.trim(), finalColor, selectedStyle)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add Snippet ($localSnippetsCount/6)",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}


enum class CanvasAction { DOWNLOAD, SHARE }

@Composable
// MISSING LINE 936
// MISSING LINE 937
// MISSING LINE 938
// MISSING LINE 939
// MISSING LINE 940
// MISSING LINE 941
// MISSING LINE 942
// MISSING LINE 943
// MISSING LINE 944
// MISSING LINE 945
// MISSING LINE 946
// MISSING LINE 947
// MISSING LINE 948
// MISSING LINE 949
// MISSING LINE 950
// MISSING LINE 951
// MISSING LINE 952
// MISSING LINE 953
// MISSING LINE 954
// MISSING LINE 955
// MISSING LINE 956
// MISSING LINE 957
// MISSING LINE 958
// MISSING LINE 959
// MISSING LINE 960
// MISSING LINE 961
// MISSING LINE 962
// MISSING LINE 963
// MISSING LINE 964
// MISSING LINE 965
// MISSING LINE 966
// MISSING LINE 967
// MISSING LINE 968
// MISSING LINE 969
// MISSING LINE 970
// MISSING LINE 971
// MISSING LINE 972
// MISSING LINE 973
// MISSING LINE 974
// MISSING LINE 975
// MISSING LINE 976
// MISSING LINE 977
// MISSING LINE 978
// MISSING LINE 979
// MISSING LINE 980
// MISSING LINE 981
// MISSING LINE 982
// MISSING LINE 983
// MISSING LINE 984
// MISSING LINE 985
// MISSING LINE 986
// MISSING LINE 987
// MISSING LINE 988
// MISSING LINE 989
// MISSING LINE 990
// MISSING LINE 991
// MISSING LINE 992
// MISSING LINE 993
// MISSING LINE 994
// MISSING LINE 995
// MISSING LINE 996
// MISSING LINE 997
// MISSING LINE 998
// MISSING LINE 999
// MISSING LINE 1000
// MISSING LINE 1001
// MISSING LINE 1002
// MISSING LINE 1003
// MISSING LINE 1004
// MISSING LINE 1005
// MISSING LINE 1006
// MISSING LINE 1007
// MISSING LINE 1008
// MISSING LINE 1009
// MISSING LINE 1010
// MISSING LINE 1011
// MISSING LINE 1012
// MISSING LINE 1013
// MISSING LINE 1014
// MISSING LINE 1015
// MISSING LINE 1016
// MISSING LINE 1017
// MISSING LINE 1018
// MISSING LINE 1019
// MISSING LINE 1020
// MISSING LINE 1021
// MISSING LINE 1022
// MISSING LINE 1023
// MISSING LINE 1024
// MISSING LINE 1025
// MISSING LINE 1026
// MISSING LINE 1027
// MISSING LINE 1028
// MISSING LINE 1029
// MISSING LINE 1030
// MISSING LINE 1031
// MISSING LINE 1032
// MISSING LINE 1033
// MISSING LINE 1034
// MISSING LINE 1035
// MISSING LINE 1036
// MISSING LINE 1037
// MISSING LINE 1038
// MISSING LINE 1039
// MISSING LINE 1040
// MISSING LINE 1041
// MISSING LINE 1042
// MISSING LINE 1043
// MISSING LINE 1044
// MISSING LINE 1045
// MISSING LINE 1046
// MISSING LINE 1047
// MISSING LINE 1048
// MISSING LINE 1049
// MISSING LINE 1050
// MISSING LINE 1051
// MISSING LINE 1052
// MISSING LINE 1053
// MISSING LINE 1054
// MISSING LINE 1055
// MISSING LINE 1056
// MISSING LINE 1057
// MISSING LINE 1058
// MISSING LINE 1059
// MISSING LINE 1060
// MISSING LINE 1061
// MISSING LINE 1062
// MISSING LINE 1063
// MISSING LINE 1064
// MISSING LINE 1065
// MISSING LINE 1066
// MISSING LINE 1067
// MISSING LINE 1068
// MISSING LINE 1069
// MISSING LINE 1070
// MISSING LINE 1071
// MISSING LINE 1072
// MISSING LINE 1073
// MISSING LINE 1074
// MISSING LINE 1075
// MISSING LINE 1076
// MISSING LINE 1077
// MISSING LINE 1078
// MISSING LINE 1079
// MISSING LINE 1080
// MISSING LINE 1081
// MISSING LINE 1082
// MISSING LINE 1083
// MISSING LINE 1084
// MISSING LINE 1085
// MISSING LINE 1086
// MISSING LINE 1087
// MISSING LINE 1088
// MISSING LINE 1089
