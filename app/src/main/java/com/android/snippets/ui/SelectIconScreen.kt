package com.android.snippets.ui
import com.android.snippets.ui.components.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.animation.core.*
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.AppShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.viewmodel.SnippetsViewModel
import android.view.HapticFeedbackConstants

@Composable
fun SelectIconScreen(viewModel: SnippetsViewModel) {
    androidx.activity.compose.BackHandler {
        viewModel.currentScreen = viewModel.previousScreen

    }
    val view = LocalView.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val initialEmoji = remember(viewModel.editingCollectionIcon) {
        val icon = viewModel.editingCollectionIcon?.let { viewModel.collectionIcons[it] } ?: ""
        // System icons are names, emojis are characters. Named system icons shouldn't populate the emoji field.
        val systemIcons = listOf("Landscape", "Nature", "Music", "Camping", "Sparkle", "Moon", "Sun", "Cloud", "Rain", "Bird", "Person", "Group", "LocalDrink", "Cookie", "Pets", "Travel", "Waves", "Apartment", "Stadium", "Sailing", "Emoji Nature", "Favorite")
        if (icon in systemIcons) "" else icon
    }
    var selectedEmoji by remember(initialEmoji) { mutableStateOf(initialEmoji) }
    
    var isHolding by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }
    val animTranslationX = remember { Animatable(0f) }
    val animTranslationY = remember { Animatable(0f) }

    val shapeType = LocalAppShapeType.current
    val isSpinningShape = when (shapeType) {
        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> true
        else -> false
    }

    LaunchedEffect(isHolding) {
        if (isSpinningShape) {
            val duration = if (isHolding) 600 else 30000
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
            }
        } else {
            val duration = if (isHolding) 400 else 4000
            when (shapeType) {
                AppShape.CLOVER_4_LEAF -> {
                    while (true) {
                        animTranslationY.animateTo(8f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                        animTranslationY.animateTo(-8f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                    }
                }
                AppShape.CLOVER_8_LEAF -> {
                    while (true) {
                        animTranslationX.animateTo(8f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                        animTranslationX.animateTo(-8f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                    }
                }
                else -> {
                    while (true) {
                        animScaleX.animateTo(1.05f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                        animScaleX.animateTo(0.95f, animationSpec = tween(duration / 2, easing = FastOutSlowInEasing))
                    }
                }
            }
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            while (true) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    val icons = listOf(
        "Landscape" to Icons.Default.Landscape,
        "Nature" to NatureIcon(),
        "Music" to MusicIcon(),
        "Camping" to CampingIcon(),
        "Sparkle" to SparkleIcon(),
        "Moon" to MoonIcon(),
        "Sun" to SunIcon(),
        "Cloud" to CloudIcon(),
        "Rain" to RainIcon(),
        "Bird" to BirdIcon(),
        "Person" to Icons.Default.Person,
        "Group" to Icons.Default.Groups,
        "LocalDrink" to Icons.Default.LocalDrink,
        "Cookie" to Icons.Default.Cookie,
        "Pets" to Icons.Default.Pets,
        "Travel" to Icons.Default.Flight,
        "Waves" to Icons.Default.Waves,
        "Apartment" to Icons.Default.Apartment,
        "Stadium" to Icons.Default.Stadium,
        "Sailing" to Icons.Default.Sailing,
        "Emoji Nature" to EmojiNatureIcon(),
        "Favorite" to Icons.Default.Favorite
    )

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
                        imageVector = Icons.Default.AddReaction,
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedCookieButton(
                            onClick = { 
                                viewModel.currentScreen = viewModel.previousScreen

                            },
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tooltip = "Back",
                            size = 56.dp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        AnimatedCookieButton(
                            onClick = {
                                if (selectedEmoji.isNotBlank()) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    viewModel.editingCollectionIcon?.let { collectionName ->
                                        viewModel.updateCollectionIcon(collectionName, selectedEmoji)
                                    }
                                    viewModel.currentScreen = viewModel.previousScreen

                                }
                            },
                            icon = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tooltip = "Done",
                            size = 56.dp,
                            containerColor = if (selectedEmoji.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedEmoji.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            // Icon Grid (Scrollable)
            // Centered Spinning Cookie
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(120.dp))
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHolding = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isHolding = false
                                    }
                                },
                                onTap = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                                    selectedEmoji = "" 
                                    focusRequester.requestFocus() 
                                    keyboardController?.show()
                                }
                            )
                        }
                ) {
                    // Spinning 12-Sided Cookie Background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { 
                                rotationZ = if (isSpinningShape) rotation.value else 0f
                                scaleX = if (isSpinningShape) 1f else animScaleX.value
                                scaleY = if (isSpinningShape) 1f else animScaleX.value
                                translationX = if (shapeType == AppShape.CLOVER_8_LEAF) animTranslationX.value else 0f
                                translationY = if (shapeType == AppShape.CLOVER_4_LEAF) animTranslationY.value else 0f
                            }
                            .clip(LocalAppShape.current)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )

                    // Icon inside the cookie
                    if (selectedEmoji.isEmpty()) {
                        Icon(
                            imageVector = Icons.Default.AddReaction,
                            contentDescription = "Tap to select emoji",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    } else {
                        // Static Emoji on top
                        Text(
                            text = selectedEmoji,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                            modifier = Modifier.wrapContentSize(unbounded = true)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hidden TextField to trigger keyboard
                BasicTextField(
                    value = selectedEmoji,
                    onValueChange = { input ->
                        val emojiOnly = input.filter { char ->
                            val type = Character.getType(char).toInt()
                            type == Character.SURROGATE.toInt() || 
                            type == Character.OTHER_SYMBOL.toInt() ||
                            char.code > 0x2000 // Catch many more complex emojis
                        }
                        if (emojiOnly.length <= 8) { // Increased for multi-part emojis
                            selectedEmoji = emojiOnly
                        }
                    },
                    modifier = Modifier.size(1.dp).focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (selectedEmoji.isNotBlank()) {
                                viewModel.editingCollectionIcon?.let { collectionName ->
                                    viewModel.updateCollectionIcon(collectionName, selectedEmoji)
                                }
                                viewModel.currentScreen = viewModel.previousScreen

                            }
                        }
                    )
                )
                
                Spacer(modifier = Modifier.height(120.dp))
            }


        }
    }
}

@Composable
fun NatureIcon(): ImageVector {
    return ImageVector.Builder(
        name = "Nature",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(
        fill = SolidColor(MaterialTheme.colorScheme.onSurface),
        pathFillType = PathFillType.EvenOdd
    ) {
        // Outer Boundary and Trunk
        moveTo(200f, 880f)
        lineTo(200f, 800f)
        lineTo(440f, 800f)
        lineTo(440f, 640f)
        lineTo(360f, 640f)
        quadTo(277f, 640f, 218.5f, 581.5f)
        quadTo(160f, 523f, 160f, 440f)
        quadTo(160f, 380f, 193f, 329.5f)
        quadTo(226f, 279f, 282f, 256f)
        quadTo(291f, 181f, 347.5f, 130.5f)
        quadTo(404f, 80f, 480f, 80f)
        quadTo(556f, 80f, 612.5f, 130.5f)
        quadTo(669f, 181f, 678f, 256f)
        quadTo(734f, 279f, 767f, 329.5f)
        quadTo(800f, 380f, 800f, 440f)
        quadTo(800f, 523f, 741.5f, 581.5f)
        quadTo(683f, 640f, 600f, 640f)
        lineTo(520f, 640f)
        lineTo(520f, 800f)
        lineTo(760f, 800f)
        lineTo(760f, 880f)
        lineTo(200f, 880f)
        close()

        // Inner Cutout (Creating the Outline effect)
        moveTo(360f, 560f)
        lineTo(600f, 560f)
        quadTo(650f, 560f, 685f, 525f)
        quadTo(720f, 490f, 720f, 440f)
        quadTo(720f, 404f, 699.5f, 374f)
        quadTo(679f, 344f, 646f, 330f)
        lineTo(604f, 312f)
        lineTo(598f, 266f)
        quadTo(592f, 221f, 558.5f, 190.5f)
        quadTo(525f, 160f, 480f, 160f)
        quadTo(435f, 160f, 401.5f, 190.5f)
        quadTo(368f, 221f, 362f, 266f)
        lineTo(356f, 312f)
        lineTo(314f, 330f)
        quadTo(281f, 344f, 260.5f, 374f)
        quadTo(240f, 404f, 240f, 440f)
        quadTo(240f, 490f, 275f, 525f)
        quadTo(310f, 560f, 360f, 560f)
        close()
    }.build()
}

@Composable
fun EmojiNatureIcon(): ImageVector {
    return ImageVector.Builder(
        name = "EmojiNature",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(
        fill = SolidColor(MaterialTheme.colorScheme.onSurface),
        pathFillType = PathFillType.EvenOdd
    ) {
        // Upper Detail (Bloom)
        moveTo(720f, 360f)
        lineToRelative(-32f, 28f)
        quadToRelative(-14f, 13f, -33f, 13f)
        reflectiveQuadToRelative(-33f, -11f)
        quadToRelative(-14f, -11f, -19f, -28f)
        reflectiveQuadToRelative(1f, -36f)
        lineToRelative(16f, -50f)
        lineToRelative(-34f, -20f)
        quadToRelative(-16f, -9f, -22.5f, -26f)
        reflectiveQuadToRelative(-1.5f, -34f)
        quadToRelative(5f, -17f, 20f, -26.5f)
        reflectiveQuadToRelative(34f, -9.5f)
        horizontalLineToRelative(40f)
        lineToRelative(12f, -38f)
        quadToRelative(6f, -19f, 20.5f, -30.5f)
        reflectiveQuadTo(720f, 80f)
        quadToRelative(17f, 0f, 31.5f, 11.5f)
        reflectiveQuadTo(772f, 122f)
        lineToRelative(12f, 38f)
        horizontalLineToRelative(40f)
        quadToRelative(19f, 0f, 33.5f, 9.5f)
        reflectiveQuadTo(878f, 196f)
        quadToRelative(7f, 18f, 0f, 35f)
        reflectiveQuadToRelative(-22f, 25f)
        lineToRelative(-36f, 20f)
        lineToRelative(16f, 50f)
        quadToRelative(6f, 19f, 1f, 36.5f)
        reflectiveQuadTo(818f, 390f)
        quadToRelative(-15f, 11f, -33.5f, 11f)
        reflectiveQuadTo(752f, 388f)
        lineToRelative(-32f, -28f)
        close()

        // Center of Bloom
        moveToRelative(28.5f, -91.5f)
        quadToRelative(11.5f, -11.5f, 11.5f, -28.5f)
        reflectiveQuadToRelative(-11.5f, -28.5f)
        quadToRelative(-11.5f, -11.5f, -28.5f, -11.5f)
        reflectiveQuadToRelative(-28.5f, 11.5f)
        quadToRelative(-11.5f, 11.5f, -11.5f, 28.5f)
        reflectiveQuadToRelative(11.5f, 28.5f)
        quadToRelative(11.5f, 11.5f, 28.5f, 11.5f)
        reflectiveQuadToRelative(28.5f, -11.5f)
        close()

        // Main Leaf Structure
        moveTo(552f, 716f)
        quadToRelative(23f, 60f, -15f, 112f)
        reflectiveQuadTo(430f, 880f)
        quadToRelative(-33f, 0f, -62.5f, -17f)
        reflectiveQuadTo(324f, 818f)
        quadToRelative(-83f, 12f, -137.5f, -42.5f)
        reflectiveQuadTo(142f, 636f)
        quadToRelative(-30f, -17f, -46f, -46.5f)
        reflectiveQuadTo(80f, 522f)
        quadToRelative(0f, -61f, 55.5f, -98.5f)
        reflectiveQuadTo(244f, 408f)
        lineToRelative(62f, 26f)
        quadToRelative(20f, -31f, 53f, -50.5f)
        reflectiveQuadToRelative(71f, -21.5f)
        verticalLineToRelative(-82f)
        horizontalLineToRelative(60f)
        verticalLineToRelative(90f)
        quadToRelative(37f, 11f, 61f, 34.5f)
        reflectiveQuadToRelative(41f, 65.5f)
        horizontalLineToRelative(88f)
        verticalLineToRelative(60f)
        horizontalLineToRelative(-82f)
        quadToRelative(-2f, 38f, -20.5f, 71f)
        reflectiveQuadTo(528f, 654f)
        lineToRelative(24f, 62f)
        close()

        // Detail 1
        moveToRelative(-348f, 24f)
        quadToRelative(0f, -27f, 4.5f, -52.5f)
        reflectiveQuadTo(322f, 638f)
        quadToRelative(-23f, 11f, -49.5f, 15.5f)
        reflectiveQuadTo(220f, 656f)
        quadToRelative(0f, 39f, 22.5f, 61.5f)
        reflectiveQuadTo(304f, 740f)
        close()

        // Detail 2
        moveToRelative(-74f, -164f)
        quadToRelative(32f, 0f, 56.5f, -8f)
        reflectiveQuadToRelative(63.5f, -32f)
        lineToRelative(-120f, -50f)
        quadToRelative(-29f, -12f, -49.5f, 0.5f)
        reflectiveQuadTo(160f, 526f)
        quadToRelative(0f, 26f, 17f, 38f)
        reflectiveQuadToRelative(53f, 12f)
        close()

        // Detail 3
        moveToRelative(200f, 224f)
        quadToRelative(25f, 0f, 40.5f, -17.5f)
        reflectiveQuadTo(478f, 746f)
        lineToRelative(-54f, -136f)
        quadToRelative(-19f, 32f, -29.5f, 64f)
        reflectiveQuadToRelative(-10.5f, 66f)
        quadToRelative(0f, 33f, 11.5f, 50.5f)
        reflectiveQuadTo(430f, 800f)
        close()

        // Detail 4
        moveToRelative(66f, -222f)
        quadToRelative(10f, -10f, 16f, -26.5f)
        reflectiveQuadToRelative(6f, -34.5f)
        quadToRelative(0f, -32f, -21f, -54f)
        reflectiveQuadToRelative(-52f, -22f)
        quadToRelative(-18f, 0f, -34f, 6f)
        reflectiveQuadToRelative(-27f, 17f)
        lineToRelative(78f, 36f)
        lineToRelative(34f, 78f)
        close()
    }.build()
}

@Composable
fun MusicIcon(): ImageVector {
    return ImageVector.Builder(name = "Music", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(780f, 840f)
        quadToRelative(-25f, 0f, -42.5f, -17.5f)
        reflectiveQuadTo(720f, 780f)
        quadToRelative(0f, -25f, 17.5f, -42.5f)
        reflectiveQuadTo(780f, 720f)
        quadToRelative(25f, 0f, 42.5f, 17.5f)
        reflectiveQuadTo(840f, 780f)
        quadToRelative(0f, 25f, -17.5f, 42.5f)
        reflectiveQuadTo(780f, 840f)
        close()
        moveTo(400f, 880f)
        quadToRelative(-100f, 0f, -170f, -23.5f)
        reflectiveQuadTo(160f, 800f)
        quadToRelative(0f, -23f, 33f, -41f)
        reflectiveQuadToRelative(87f, -29f)
        verticalLineToRelative(70f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-720f)
        lineToRelative(320f, 156f)
        lineToRelative(-240f, 124f)
        verticalLineToRelative(362f)
        quadToRelative(86f, 5f, 143f, 26.5f)
        reflectiveQuadToRelative(57f, 51.5f)
        quadToRelative(0f, 33f, -70f, 56.5f)
        reflectiveQuadTo(400f, 880f)
        close()
    }.build()
}

@Composable
fun CampingIcon(): ImageVector {
    return ImageVector.Builder(name = "Camping", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(348f, 517f)
        lineToRelative(55f, 37f)
        lineToRelative(77f, -39f)
        lineToRelative(77f, 39f)
        lineToRelative(53f, -35f)
        lineToRelative(-40f, -79f)
        horizontalLineTo(386f)
        lineToRelative(-38f, 77f)
        close()
        moveTo(209f, 800f)
        horizontalLineToRelative(541f)
        lineToRelative(-104f, -209f)
        lineToRelative(-83f, 55f)
        lineToRelative(-83f, -41f)
        lineToRelative(-83f, 41f)
        lineToRelative(-85f, -56f)
        lineToRelative(-103f, 210f)
        close()
        moveTo(80f, 880f)
        lineToRelative(234f, -475f)
        quadToRelative(10f, -20f, 29.5f, -32.5f)
        reflectiveQuadTo(386f, 360f)
        horizontalLineToRelative(54f)
        verticalLineToRelative(-280f)
        horizontalLineToRelative(280f)
        lineToRelative(-40f, 80f)
        lineToRelative(40f, 80f)
        horizontalLineTo(520f)
        verticalLineToRelative(120f)
        horizontalLineToRelative(50f)
        quadToRelative(23f, 0f, 42f, 12f)
        reflectiveQuadToRelative(30f, 32f)
        lineToRelative(238f, 476f)
        horizontalLineTo(80f)
        close()
    }.build()
}

@Composable
fun SparkleIcon(): ImageVector {
    return ImageVector.Builder(name = "Sparkle", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(852f, 748f)
        lineToRelative(-120f, -120f)
        lineToRelative(56f, -56f)
        lineToRelative(120f, 120f)
        lineToRelative(-56f, 56f)
        close()
        moveTo(708f, 268f)
        lineToRelative(-56f, -56f)
        lineToRelative(120f, -120f)
        lineToRelative(56f, 56f)
        lineToRelative(-120f, 120f)
        close()
        moveTo(252f, 268f)
        lineToRelative(-120f, -120f)
        lineToRelative(56f, -56f)
        lineToRelative(120f, 120f)
        lineToRelative(-56f, 56f)
        close()
        moveTo(108f, 748f)
        lineToRelative(-56f, -56f)
        lineToRelative(120f, -120f)
        lineToRelative(56f, 56f)
        lineToRelative(-120f, 120f)
        close()
        moveTo(354f, 673f)
        lineToRelative(126f, -76f)
        lineToRelative(126f, 77f)
        lineToRelative(-33f, -144f)
        lineToRelative(111f, -96f)
        lineToRelative(-146f, -13f)
        lineToRelative(-58f, -136f)
        lineToRelative(-58f, 135f)
        lineToRelative(-146f, 13f)
        lineToRelative(111f, 97f)
        lineToRelative(-33f, 143f)
        close()
        moveTo(233f, 840f)
        lineToRelative(65f, -281f)
        lineToRelative(-218f, -189f)
        lineToRelative(288f, -25f)
        lineToRelative(112f, -265f)
        lineToRelative(112f, 265f)
        lineToRelative(288f, 25f)
        lineToRelative(-218f, 189f)
        lineToRelative(65f, 281f)
        lineToRelative(-247f, -149f)
        lineToRelative(-247f, 149f)
        close()
        moveTo(480f, 479f)
        close()
    }.build()
}

@Composable
fun MoonIcon(): ImageVector {
    return ImageVector.Builder(name = "Moon", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(484f, 880f)
        quadToRelative(-84f, 0f, -157.5f, -32f)
        reflectiveQuadToRelative(-128f, -86.5f)
        reflectiveQuadTo(112f, 633.5f)
        reflectiveQuadTo(80f, 476f)
        quadToRelative(0f, -146f, 93f, -257.5f)
        reflectiveQuadTo(410f, 80f)
        quadToRelative(-18f, 99f, 11f, 193.5f)
        reflectiveQuadTo(521f, 439f)
        quadToRelative(71f, 71f, 165.5f, 100f)
        reflectiveQuadTo(880f, 550f)
        quadToRelative(-26f, 144f, -138f, 237f)
        reflectiveQuadTo(484f, 880f)
        close()
        moveTo(484f, 800f)
        quadToRelative(88f, 0f, 163f, -44f)
        reflectiveQuadToRelative(118f, -121f)
        quadToRelative(-86f, -8f, -163f, -43.5f)
        reflectiveQuadTo(464f, 495f)
        quadToRelative(-61f, -61f, -97f, -138f)
        reflectiveQuadToRelative(-43f, -163f)
        quadToRelative(-77f, 43f, -120.5f, 118.5f)
        reflectiveQuadTo(160f, 476f)
        quadToRelative(0f, 135f, 94.5f, 229.5f)
        reflectiveQuadTo(484f, 800f)
        close()
        moveTo(464f, 495f)
        close()
    }.build()
}

@Composable
fun SunIcon(): ImageVector {
    return ImageVector.Builder(name = "Sun", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(440f, 200f)
        verticalLineToRelative(-160f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(-80f)
        close()
        moveTo(706f, 310f)
        lineToRelative(-55f, -55f)
        lineToRelative(112f, -115f)
        lineToRelative(56f, 57f)
        lineToRelative(-113f, 113f)
        close()
        moveTo(760f, 520f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(80f)
        horizontalLineTo(760f)
        close()
        moveTo(440f, 920f)
        verticalLineToRelative(-160f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(-80f)
        close()
        moveTo(254f, 308f)
        lineToRelative(-114f, -111f)
        lineToRelative(57f, -56f)
        lineToRelative(113f, 113f)
        lineToRelative(-56f, 54f)
        close()
        moveTo(762f, 820f)
        lineToRelative(-111f, -115f)
        lineToRelative(54f, -54f)
        lineToRelative(114f, 110f)
        lineToRelative(-57f, 59f)
        close()
        moveTo(40f, 520f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(80f)
        horizontalLineTo(40f)
        close()
        moveTo(197f, 820f)
        lineToRelative(-56f, -57f)
        lineToRelative(112f, -112f)
        lineToRelative(29f, 27f)
        lineToRelative(29f, 28f)
        lineToRelative(-114f, 114f)
        close()
        moveTo(310f, 650f)
        quadToRelative(-70f, -70f, -70f, -170f)
        reflectiveQuadToRelative(70f, -170f)
        quadToRelative(70f, -70f, 170f, -70f)
        reflectiveQuadToRelative(170f, 70f)
        quadToRelative(70f, 70f, 70f, 170f)
        reflectiveQuadToRelative(-70f, 170f)
        quadToRelative(-70f, 70f, -170f, 70f)
        reflectiveQuadToRelative(-170f, -70f)
        close()
        moveToRelative(283f, -57f)
        quadToRelative(47f, -47f, 47f, -113f)
        reflectiveQuadToRelative(-47f, -113f)
        quadToRelative(-47f, -47f, -113f, -47f)
        reflectiveQuadToRelative(-113f, 47f)
        quadToRelative(-47f, 47f, -47f, 113f)
        reflectiveQuadToRelative(47f, 113f)
        quadToRelative(47f, 47f, 113f, 47f)
        reflectiveQuadToRelative(113f, -47f)
        close()
        moveTo(480f, 480f)
        close()
    }.build()
}

@Composable
fun CloudIcon(): ImageVector {
    return ImageVector.Builder(name = "Cloud", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(558f, 876f)
        quadToRelative(-15f, 8f, -30.5f, 2.5f)
        reflectiveQuadTo(504f, 858f)
        lineToRelative(-60f, -120f)
        quadToRelative(-8f, -15f, -2.5f, -30.5f)
        reflectiveQuadTo(462f, 684f)
        quadToRelative(15f, -8f, 30.5f, -2.5f)
        reflectiveQuadTo(516f, 702f)
        lineToRelative(60f, 120f)
        quadToRelative(8f, 15f, 2.5f, 30.5f)
        reflectiveQuadTo(558f, 876f)
        close()
        moveTo(798f, 876f)
        quadToRelative(-15f, 8f, -30.5f, 2.5f)
        reflectiveQuadTo(744f, 858f)
        lineToRelative(-60f, -120f)
        quadToRelative(-8f, -15f, -2.5f, -30.5f)
        reflectiveQuadTo(702f, 684f)
        quadToRelative(15f, -8f, 30.5f, -2.5f)
        reflectiveQuadTo(756f, 702f)
        lineToRelative(60f, 120f)
        quadToRelative(8f, 15f, 2.5f, 30.5f)
        reflectiveQuadTo(798f, 876f)
        close()
        moveTo(318f, 876f)
        quadToRelative(-15f, 8f, -30.5f, 2.5f)
        reflectiveQuadTo(264f, 858f)
        lineToRelative(-60f, -120f)
        quadToRelative(-8f, -15f, -2.5f, -30.5f)
        reflectiveQuadTo(222f, 684f)
        quadToRelative(15f, -8f, 30.5f, -2.5f)
        reflectiveQuadTo(276f, 702f)
        lineToRelative(60f, 120f)
        quadToRelative(8f, 15f, 2.5f, 30.5f)
        reflectiveQuadTo(318f, 876f)
        close()
        moveTo(300f, 640f)
        quadToRelative(-91f, 0f, -155.5f, -64.5f)
        reflectiveQuadTo(80f, 420f)
        quadToRelative(0f, -83f, 55f, -145f)
        reflectiveQuadToRelative(136f, -73f)
        quadToRelative(32f, -57f, 87.5f, -89.5f)
        reflectiveQuadTo(480f, 80f)
        quadToRelative(90f, 0f, 156.5f, 57.5f)
        reflectiveQuadTo(717f, 281f)
        quadToRelative(69f, 6f, 116f, 57f)
        reflectiveQuadToRelative(47f, 122f)
        quadToRelative(0f, 75f, -52.5f, 127.5f)
        reflectiveQuadTo(700f, 640f)
        horizontalLineTo(300f)
        close()
        moveToRelative(0f, -80f)
        horizontalLineToRelative(400f)
        quadToRelative(42f, 0f, 71f, -29f)
        reflectiveQuadToRelative(29f, -71f)
        quadToRelative(0f, -42f, -29f, -71f)
        reflectiveQuadToRelative(-71f, -29f)
        horizontalLineToRelative(-60f)
        verticalLineToRelative(-40f)
        quadToRelative(0f, -66f, -47f, -113f)
        reflectiveQuadToRelative(-113f, -47f)
        quadToRelative(-48f, 0f, -87.5f, 26f)
        reflectiveQuadTo(333f, 256f)
        lineToRelative(-10f, 24f)
        horizontalLineToRelative(-25f)
        quadToRelative(-57f, 2f, -97.5f, 42.5f)
        reflectiveQuadTo(160f, 420f)
        quadToRelative(0f, 58f, 41f, 99f)
        reflectiveQuadToRelative(99f, 41f)
        close()
        moveToRelative(180f, -200f)
        close()
    }.build()
}

@Composable
fun RainIcon(): ImageVector {
    return ImageVector.Builder(name = "Rain", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(720f, 760f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(680f, 720f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(720f, 680f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(760f, 720f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(720f, 760f)
        close()
        moveTo(280f, 880f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(240f, 840f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(280f, 800f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(320f, 840f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(280f, 880f)
        close()
        moveTo(240f, 760f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(200f, 720f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(240f, 680f)
        horizontalLineToRelative(360f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(640f, 720f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(600f, 760f)
        horizontalLineTo(240f)
        close()
        moveTo(400f, 880f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(360f, 840f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(400f, 800f)
        horizontalLineToRelative(280f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(720f, 840f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(680f, 880f)
        horizontalLineTo(400f)
        close()
        moveTo(300f, 640f)
        quadToRelative(-91f, 0f, -155.5f, -64.5f)
        reflectiveQuadTo(80f, 420f)
        quadToRelative(0f, -83f, 55f, -145f)
        reflectiveQuadToRelative(136f, -73f)
        quadToRelative(32f, -57f, 87.5f, -89.5f)
        reflectiveQuadTo(480f, 80f)
        quadToRelative(90f, 0f, 156.5f, 57.5f)
        reflectiveQuadTo(717f, 281f)
        quadToRelative(69f, 6f, 116f, 57f)
        reflectiveQuadToRelative(47f, 122f)
        quadToRelative(0f, 75f, -52.5f, 127.5f)
        reflectiveQuadTo(700f, 640f)
        horizontalLineTo(300f)
        close()
        moveToRelative(0f, -80f)
        horizontalLineToRelative(400f)
        quadToRelative(42f, 0f, 71f, -29f)
        reflectiveQuadToRelative(29f, -71f)
        quadToRelative(0f, -42f, -29f, -71f)
        reflectiveQuadToRelative(-71f, -29f)
        horizontalLineToRelative(-60f)
        verticalLineToRelative(-40f)
        quadToRelative(0f, -66f, -47f, -113f)
        reflectiveQuadToRelative(-113f, -47f)
        quadToRelative(-48f, 0f, -87.5f, 26f)
        reflectiveQuadTo(333f, 256f)
        lineToRelative(-10f, 24f)
        horizontalLineToRelative(-25f)
        quadToRelative(-57f, 2f, -97.5f, 42.5f)
        reflectiveQuadTo(160f, 420f)
        quadToRelative(0f, 58f, 41f, 99f)
        reflectiveQuadToRelative(99f, 41f)
        close()
        moveToRelative(180f, -200f)
        close()
    }.build()
}

@Composable
fun BirdIcon(): ImageVector {
    return ImageVector.Builder(name = "Bird", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(334f, 880f)
        lineToRelative(-74f, -30f)
        lineToRelative(58f, -141f)
        quadToRelative(-106f, -28f, -172f, -114f)
        reflectiveQuadTo(80f, 400f)
        verticalLineToRelative(-160f)
        quadToRelative(0f, -66f, 47f, -113f)
        reflectiveQuadToRelative(113f, -47f)
        quadToRelative(22f, 0f, 42f, 7.5f)
        reflectiveQuadToRelative(40f, 15.5f)
        lineToRelative(238f, 97f)
        lineToRelative(-160f, 60f)
        verticalLineToRelative(60f)
        lineToRelative(440f, 280f)
        lineToRelative(40f, 200f)
        horizontalLineToRelative(-80f)
        lineToRelative(-40f, -80f)
        horizontalLineTo(560f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(-160f)
        horizontalLineToRelative(-80f)
        lineToRelative(-66f, 160f)
        close()
        moveToRelative(66f, -240f)
        horizontalLineToRelative(353f)
        lineToRelative(-63f, -40f)
        horizontalLineTo(400f)
        quadToRelative(-66f, 0f, -113f, -47f)
        reflectiveQuadToRelative(-47f, -113f)
        horizontalLineToRelative(80f)
        quadToRelative(0f, 33f, 23.5f, 56.5f)
        reflectiveQuadTo(400f, 520f)
        horizontalLineToRelative(165f)
        lineToRelative(-245f, -156f)
        verticalLineToRelative(-124f)
        quadToRelative(0f, -33f, -23.5f, -56.5f)
        reflectiveQuadTo(240f, 160f)
        quadToRelative(-33f, 0f, -56.5f, 23.5f)
        reflectiveQuadTo(160f, 240f)
        verticalLineToRelative(160f)
        quadToRelative(0f, 100f, 70f, 170f)
        reflectiveQuadToRelative(170f, 70f)
        close()
        moveToRelative(-188.5f, -371.5f)
        quadToRelative(-11.5f, -11.5f, -11.5f, -28.5f)
        reflectiveQuadToRelative(11.5f, -28.5f)
        quadToRelative(11.5f, -11.5f, 28.5f, -11.5f)
        reflectiveQuadToRelative(28.5f, 11.5f)
        quadToRelative(11.5f, 11.5f, 11.5f, 28.5f)
        reflectiveQuadToRelative(-11.5f, 28.5f)
        quadToRelative(-11.5f, 11.5f, -28.5f, 11.5f)
        reflectiveQuadToRelative(-28.5f, -11.5f)
        close()
        moveTo(400f, 600f)
        close()
    }.build()
}

@Composable
fun GridIcon(): ImageVector {
    return ImageVector.Builder(name = "Grid", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.onSurface)) {
        moveTo(240f, 610f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(290f, 560f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(240f, 610f)
        close()
        moveTo(240f, 290f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(290f, 240f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(240f, 290f)
        close()
        moveTo(360f, 770f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(410f, 720f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(360f, 770f)
        close()
        moveTo(360f, 450f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(410f, 400f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(360f, 450f)
        close()
        moveTo(480f, 610f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(530f, 560f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(480f, 610f)
        close()
        moveTo(480f, 290f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(530f, 240f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(480f, 290f)
        close()
        moveTo(600f, 770f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(650f, 720f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(600f, 770f)
        close()
        moveTo(600f, 450f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(650f, 400f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(600f, 450f)
        close()
        moveTo(720f, 610f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(770f, 560f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(720f, 610f)
        close()
        moveTo(720f, 290f)
        quadToRelative(-20f, 0f, -35f, -15f)
        reflectiveQuadToRelative(-15f, -35f)
        quadToRelative(0f, -21f, 15f, -35.5f)
        reflectiveQuadToRelative(35f, -14.5f)
        quadToRelative(21f, 0f, 35.5f, 14.5f)
        reflectiveQuadTo(770f, 240f)
        quadToRelative(0f, 20f, -14.5f, 35f)
        reflectiveQuadTo(720f, 290f)
        close()
    }.build()
}

@Composable
fun IconSelectionItem(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        AnimatedCookieButton(
            onClick = onClick,
            icon = icon,
            modifier = Modifier.fillMaxSize(),
            shape = LocalAppShape.current,
            size = 120.dp
        )
    }
}
