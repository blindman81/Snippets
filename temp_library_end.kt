                
                if (longPressedCollection != null) {
                    ModalBottomSheet(
                        onDismissRequest = { longPressedCollection = null },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                        ) {
                            val isSystemCollection = longPressedCollection == "Library" || longPressedCollection == "Favorites" || longPressedCollection == "Liked"
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "VIEW",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 8.dp)
                            )

                            androidx.compose.material3.ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                overflowIndicator = {}
                            ) {
                                val dateOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.DateNewest, com.android.snippets.viewmodel.PhotoSortType.DateOldest)
// Close / back
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                        isSearchOpen = false
                                        viewModel.searchQuery = ""
                                    },
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close Search",
                                    tooltip = "Close",
                                    isSpinning = true,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Text field
                                BasicTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.searchQuery = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        viewModel.addRecentSearch(viewModel.searchQuery)
                                        isSearchOpen = false
                                        focusManager.clearFocus()
                                    }),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (viewModel.searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                // Clear button
                                if (viewModel.searchQuery.isNotEmpty()) {
                                    AnimatedCookieButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            viewModel.searchQuery = ""
                                        },
                                        icon = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tooltip = "Clear",
                                        isSpinning = true,
                                        size = 48.dp,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        // ── NORMAL MODE ───────────────────────────────────────────
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shadowElevation = 8.dp,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .height(64.dp)
                                .clip(CircleShape)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Menu
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        showMenuPopup = !showMenuPopup
                                        showCollectionsPopup = false
                                    },
                                    icon = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tooltip = "Menu",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (showMenuPopup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (showMenuPopup) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Search
                                val isSearchActive = viewModel.searchQuery.isNotEmpty()
                                val searchIcon = if (isSearchActive && grouped.isNotEmpty()) SearchSuccessIcon() else Icons.Default.Search
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        isSearchOpen = true
                                    },
                                    icon = searchIcon,
                                    contentDescription = "Search",
                                    tooltip = "Search",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Browse
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.navigateBrowse()
                                    },
                                    icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = com.android.snippets.R.drawable.ic_browse),
                                    contentDescription = "Browse",
                                    tooltip = "Browse Community",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Filter
                                val isFilterActive = viewModel.selectedFilterSnippets.isNotEmpty() || viewModel.showFilterSheet
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.filteringCategory = currentTab
                                        viewModel.navigateFilter()
                                    },
                                    icon = Icons.Default.FilterList,
                                    contentDescription = "Filters",
                                    tooltip = "Filters",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isFilterActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        MenuBottomSheet(
                            show = showMenuPopup,
                            onDismissRequest = { showMenuPopup = false },
                            viewModel = viewModel,
                            view = view
                        )

                        Spacer(Modifier.height(4.dp))
                    }
                }
                }
                
                if (longPressedCollection != null) {
                    ModalBottomSheet(
                        onDismissRequest = { longPressedCollection = null },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                        ) {
                            val isSystemCollection = longPressedCollection == "Library" || longPressedCollection == "Favorites" || longPressedCollection == "Liked"
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "VIEW",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 8.dp)
                            )

                            androidx.compose.material3.ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                overflowIndicator = {}
                            ) {
                                val dateOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.DateNewest, com.android.snippets.viewmodel.PhotoSortType.DateOldest)
                                val dateLabels = listOf("Newest first", "Oldest first")
                                val dateIcons = listOf(Icons.Default.ArrowDownward, Icons.Default.ArrowUpward)

                                var i = 0
                                for (option in dateOptions) {
                                    val isSelected = viewModel.getPhotoSortTypeFor(longPressedCollection ?: "Library") == option
                                    val currentIndex = i
                            }

                            androidx.compose.material3.ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                overflowIndicator = {}
                            ) {
                                val snippetOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.MostSnippets, com.android.snippets.viewmodel.PhotoSortType.LeastSnippets)
                                val snippetLabels = listOf("Most snippets", "Least snippets")
                                val snippetIcons = listOf(androidx.compose.material.icons.Icons.Default.TextSnippet, androidx.compose.material.icons.Icons.Default.TextSnippet)

                                var j = 0
                                for (option in snippetOptions) {
                                    val isSelected = viewModel.getPhotoSortTypeFor(longPressedCollection ?: "Library") == option
                                    val currentIndex = j
                                    customItem(buttonGroupContent = {
                                        
                                        FilledTonalButton(
                                            onClick = {
                                                if (!isSelected) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    viewModel.setPhotoSortTypeFor(longPressedCollection ?: "Library", option)
                                                    listStates.values.forEach { state ->
                                                        scope.launch { state.scrollToItem(0) }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = currentShape,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(snippetIcons[currentIndex], null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(snippetLabels[currentIndex])
                                        }
                                    }, menuContent = {})
                                    j++
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ACTIONS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 8.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
