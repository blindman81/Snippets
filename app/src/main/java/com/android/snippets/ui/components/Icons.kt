package com.android.snippets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.group
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentSize

@Composable
fun HeartBrokenBoltIcon(): ImageVector {
    return ImageVector.Builder(name = "HeartBrokenBolt", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.primary)) {
        moveTo(481f, 877f)
        quadToRelative(-134f, -135f, -213.5f, -218f)
        reflectiveQuadToRelative(-121f, -138f)
        quadToRelative(-41.5f, -55f, -54f, -94f)
        reflectiveQuadTo(80f, 340f)
        quadToRelative(0f, -92f, 64f, -156f)
        reflectiveQuadToRelative(156f, -64f)
        quadToRelative(45f, 0f, 87f, 16.5f)
        reflectiveQuadToRelative(75f, 47.5f)
        lineToRelative(-62f, 216f)
        horizontalLineToRelative(120f)
        lineToRelative(-34f, 335f)
        lineToRelative(114f, -375f)
        horizontalLineTo(480f)
        lineToRelative(71f, -212f)
        quadToRelative(25f, -14f, 52.5f, -21f)
        reflectiveQuadToRelative(56.5f, -7f)
        quadToRelative(92f, 0f, 156f, 64f)
        reflectiveQuadToRelative(64f, 156f)
        quadToRelative(0f, 48f, -13f, 88f)
        reflectiveQuadToRelative(-55f, 95.5f)
        quadToRelative(-42f, 55.5f, -121f, 138f)
        reflectiveQuadTo(481f, 877f)
        close()
        moveToRelative(-71f, -186f)
        lineToRelative(21f, -211f)
        horizontalLineTo(294f)
        lineToRelative(75f, -263f)
        quadToRelative(-16f, -8f, -33.5f, -12.5f)
        reflectiveQuadTo(300f, 200f)
        quadToRelative(-58f, 0f, -99f, 41f)
        reflectiveQuadToRelative(-41f, 99f)
        quadToRelative(0f, 31f, 11.5f, 62f)
        reflectiveQuadToRelative(40f, 70.5f)
        quadToRelative(28.5f, 39.5f, 77f, 92f)
        reflectiveQuadTo(410f, 691f)
        close()
        moveToRelative(188f, -48f)
        quadToRelative(111f, -113f, 156.5f, -180f)
        reflectiveQuadTo(800f, 340f)
        quadToRelative(0f, -58f, -41f, -99f)
        reflectiveQuadToRelative(-99f, -41f)
        quadToRelative(-11f, 0f, -22f, 1.5f)
        reflectiveQuadToRelative(-22f, 5.5f)
        lineToRelative(-24f, 73f)
        horizontalLineToRelative(116f)
        lineToRelative(-110f, 354f)
        close()
        moveToRelative(110f, -363f)
        close()
        close()
    }.build()
}

@Composable
fun SearchNoResultsIcon(): ImageVector {
    return ImageVector.Builder(name = "SearchNoResults", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.primary)) {
        moveTo(138.5f, 821.5f)
        quadToRelative(-58.5f, -58.5f, -58.5f, -141.5f)
        reflectiveQuadTo(138.5f, 538.5f)
        quadToRelative(58.5f, -58.5f, 141.5f, -58.5f)
        reflectiveQuadToRelative(141.5f, 58.5f)
        quadToRelative(58.5f, 58.5f, 58.5f, 141.5f)
        reflectiveQuadToRelative(-58.5f, 141.5f)
        quadToRelative(-58.5f, 58.5f, -141.5f, 58.5f)
        reflectiveQuadToRelative(-141.5f, -58.5f)
        close()
        moveTo(824f, 840f)
        lineTo(568f, 584f)
        quadToRelative(-12f, -13f, -25.5f, -26.5f)
        reflectiveQuadTo(516f, 532f)
        quadToRelative(38f, -24f, 61f, -64f)
        reflectiveQuadToRelative(23f, -88f)
        quadToRelative(0f, -75f, -52.5f, -127.5f)
        reflectiveQuadTo(420f, 200f)
        quadToRelative(-75f, 0f, -127.5f, 52.5f)
        reflectiveQuadTo(240f, 380f)
        quadToRelative(0f, 6f, 0.5f, 11.5f)
        reflectiveQuadToRelative(1.5f, 11.5f)
        quadToRelative(-18f, 2f, -39.5f, 8f)
        reflectiveQuadTo(164f, 425f)
        quadToRelative(-2f, -11f, -3f, -22f)
        reflectiveQuadToRelative(-1f, -23f)
        quadToRelative(0f, -109f, 75.5f, -184.5f)
        reflectiveQuadTo(420f, 120f)
        quadToRelative(109f, 0f, 184.5f, 75.5f)
        reflectiveQuadTo(680f, 380f)
        quadToRelative(0f, 43f, -13.5f, 81.5f)
        reflectiveQuadTo(629f, 532f)
        lineToRelative(251f, 252f)
        lineToRelative(-56f, 56f)
        close()
        moveToRelative(-615f, -61f)
        lineToRelative(71f, -71f)
        lineToRelative(70f, 71f)
        lineToRelative(29f, -28f)
        lineToRelative(-71f, -71f)
        lineToRelative(71f, -71f)
        lineToRelative(-28f, -28f)
        lineToRelative(-71f, 71f)
        lineToRelative(-71f, -71f)
        lineToRelative(-28f, 28f)
        lineToRelative(71f, 71f)
        lineToRelative(-71f, 71f)
        lineToRelative(28f, 28f)
        close()
    }.build()
}

@Composable
fun CookieIcon12(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
): ImageVector {
    return ImageVector.Builder(
        name = "Cookie12",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(
        fill = SolidColor(tint),
        pathFillType = PathFillType.EvenOdd
    ) {
        moveTo(480f, 480f)
        moveTo(480f, 880f)
        quadTo(397f, 880f, 324f, 848.5f)
        reflectiveQuadTo(197f, 763f)
        quadTo(143f, 709f, 111.5f, 636f)
        reflectiveQuadTo(80f, 480f)
        quadTo(80f, 397f, 111.5f, 324f)
        reflectiveQuadTo(197f, 197f)
        quadTo(251f, 143f, 324f, 111.5f)
        reflectiveQuadTo(480f, 80f)
        quadTo(523f, 80f, 563f, 88.5f)
        reflectiveQuadTo(640f, 104.5f)
        verticalLineToRelative(90f)
        quadTo(605f, 174.5f, 564.5f, 163f)
        reflectiveQuadTo(480f, 160f)
        quadTo(347f, 160f, 253.5f, 253.5f)
        reflectiveQuadTo(160f, 480f)
        quadTo(160f, 613f, 253.5f, 706.5f)
        reflectiveQuadTo(480f, 800f)
        quadTo(613f, 800f, 706.5f, 706.5f)
        reflectiveQuadTo(800f, 480f)
        quadTo(800f, 448f, 793.5f, 418f)
        reflectiveQuadTo(776f, 360f)
        horizontalLineToRelative(86f)
        quadTo(871f, 389f, 875.5f, 418.5f)
        reflectiveQuadTo(880f, 480f)
        quadTo(880f, 563f, 848.5f, 636f)
        reflectiveQuadTo(763f, 763f)
        quadTo(709f, 817f, 636f, 848.5f)
        reflectiveQuadTo(480f, 880f)
        close()

        moveTo(800f, 280f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(-80f)
        close()

        moveTo(340f, 440f)
        quadTo(365f, 440f, 382.5f, 422.5f)
        reflectiveQuadTo(400f, 380f)
        quadTo(400f, 355f, 382.5f, 337.5f)
        reflectiveQuadTo(340f, 320f)
        quadTo(315f, 320f, 297.5f, 337.5f)
        reflectiveQuadTo(280f, 380f)
        quadTo(280f, 405f, 297.5f, 422.5f)
        reflectiveQuadTo(340f, 440f)
        close()

        moveTo(620f, 440f)
        quadTo(645f, 440f, 662.5f, 422.5f)
        reflectiveQuadTo(680f, 380f)
        quadTo(680f, 355f, 662.5f, 337.5f)
        reflectiveQuadTo(620f, 320f)
        quadTo(595f, 320f, 577.5f, 337.5f)
        reflectiveQuadTo(560f, 380f)
        quadTo(560f, 405f, 577.5f, 422.5f)
        reflectiveQuadTo(620f, 440f)
        close()

        moveTo(603.5f, 661.5f)
        quadTo(659f, 623f, 684f, 560f)
        horizontalLineTo(276f)
        quadTo(301f, 723f, 356.5f, 761.5f)
        reflectiveQuadTo(480f, 700f)
        quadTo(548f, 700f, 603.5f, 661.5f)
        close()
    }.build()
}

@Composable
fun EmptyLibraryIcon(): ImageVector {
    return ImageVector.Builder(name = "EmptyLibrary", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.primary)) {
        moveTo(520f, 560f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-120f)
        horizontalLineToRelative(120f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(-120f)
        verticalLineToRelative(-120f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(120f)
        horizontalLineToRelative(-120f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(120f)
        verticalLineToRelative(120f)
        close()
        moveTo(320f, 720f)
        quadToRelative(-33f, 0f, -56.5f, -23.5f)
        reflectiveQuadTo(240f, 640f)
        verticalLineToRelative(-480f)
        quadToRelative(0f, -33f, 23.5f, -56.5f)
        reflectiveQuadTo(320f, 80f)
        horizontalLineToRelative(480f)
        quadToRelative(33f, 0f, 56.5f, 23.5f)
        reflectiveQuadTo(880f, 160f)
        verticalLineToRelative(480f)
        quadToRelative(0f, 33f, -23.5f, 56.5f)
        reflectiveQuadTo(800f, 720f)
        horizontalLineTo(320f)
        close()
        moveToRelative(0f, -80f)
        horizontalLineToRelative(480f)
        verticalLineToRelative(-480f)
        horizontalLineTo(320f)
        verticalLineToRelative(480f)
        close()
        moveTo(160f, 880f)
        quadToRelative(-33f, 0f, -56.5f, -23.5f)
        reflectiveQuadTo(80f, 800f)
        verticalLineToRelative(-560f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(560f)
        horizontalLineToRelative(560f)
        verticalLineToRelative(80f)
        horizontalLineTo(160f)
        close()
        moveToRelative(160f, -720f)
        verticalLineToRelative(480f)
        verticalLineToRelative(-480f)
        close()
    }.build()
}

@Composable
fun EmptyCollectionIcon(): ImageVector {
    return ImageVector.Builder(name = "EmptyCollection", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f).path(fill = SolidColor(MaterialTheme.colorScheme.primary)) {
        moveTo(160f, 800f)
        quadToRelative(-33f, 0f, -56.5f, -23.5f)
        reflectiveQuadTo(80f, 720f)
        verticalLineToRelative(-480f)
        quadToRelative(0f, -33f, 23.5f, -56.5f)
        reflectiveQuadTo(160f, 160f)
        horizontalLineToRelative(240f)
        lineToRelative(80f, 80f)
        horizontalLineToRelative(320f)
        quadToRelative(33f, 0f, 56.5f, 23.5f)
        reflectiveQuadTo(880f, 320f)
        horizontalLineTo(447f)
        lineToRelative(-80f, -80f)
        horizontalLineTo(160f)
        verticalLineToRelative(480f)
        lineToRelative(96f, -320f)
        horizontalLineToRelative(684f)
        lineTo(837f, 743f)
        quadToRelative(-8f, 26f, -29.5f, 41.5f)
        reflectiveQuadTo(760f, 800f)
        horizontalLineTo(160f)
        close()
        moveToRelative(84f, -80f)
        horizontalLineToRelative(516f)
        lineToRelative(72f, -240f)
        horizontalLineTo(316f)
        lineToRelative(-72f, 240f)
        close()
        moveToRelative(0f, 0f)
        lineToRelative(72f, -240f)
        lineToRelative(-72f, 240f)
        close()
        moveToRelative(-84f, -400f)
        verticalLineToRelative(-80f)
        verticalLineToRelative(80f)
        close()
    }.build()
}

@Composable
fun CollectionIcon(
    icon: Any,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    if (icon is String) {
        BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
            val fontSize = (minOf(maxWidth, maxHeight) * 1.0f).value.sp
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.wrapContentSize(unbounded = true)
            )
        }
    } else if (icon is ImageVector) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = modifier,
            tint = tint
        )
    } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = modifier,
            tint = tint
        )
    }
}

@Composable
fun DiscordIcon(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "Discord",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 127.14f,
            viewportHeight = 127.14f
        ).apply {
            group(translationY = 15.39f) {
                path(fill = SolidColor(Color(0xFF5865F2))) {
                    moveTo(107.7f, 8.07f)
                    arcToRelative(105.15f, 105.15f, 0f, false, false, -26.23f, -8.07f)
                    arcToRelative(72.06f, 72.06f, 0f, false, false, -3.36f, 6.83f)
                    arcToRelative(97.68f, 97.68f, 0f, false, false, -29.11f, 0f)
                    arcToRelative(72.06f, 72.06f, 0f, false, false, -3.36f, -6.83f)
                    arcToRelative(105.89f, 105.89f, 0f, false, false, -26.25f, 8.09f)
                    curveTo(2.71f, 32.65f, -1.82f, 56.6f, 0.4f, 80.21f)
                    arcToRelative(105.73f, 105.73f, 0f, false, false, 32.17f, 16.15f)
                    arcToRelative(77.7f, 77.7f, 0f, false, false, 6.89f, -11.11f)
                    arcToRelative(64.62f, 64.62f, 0f, false, true, -10.85f, -5.18f)
                    curveToRelative(0.91f, -0.66f, 1.8f, -1.34f, 2.66f, -2f)
                    arcToRelative(75.57f, 75.57f, 0f, false, false, 64.32f, 0f)
                    curveToRelative(0.87f, 0.71f, 1.76f, 1.39f, 2.66f, 2f)
                    arcToRelative(64.59f, 64.59f, 0f, false, true, -10.85f, 5.18f)
                    arcToRelative(77.59f, 77.59f, 0f, false, false, 6.89f, 11.11f)
                    arcToRelative(105.54f, 105.54f, 0f, false, false, 32.19f, -16.15f)
                    curveToRelative(2.72f, -27.34f, -4.57f, -51.13f, -19.34f, -72.14f)
                    close()
                    // Eyes
                    moveTo(42.45f, 65.69f)
                    curveTo(36.18f, 65.69f, 31f, 60f, 31f, 53f)
                    reflectiveCurveToRelative(5f, -12.74f, 11.43f, -12.74f)
                    reflectiveCurveTo(54f, 46f, 53.89f, 53f)
                    reflectiveCurveTo(48.84f, 65.69f, 42.45f, 65.69f)
                    close()
                    // Fixed missing closing bracket for eyes path
                    moveTo(84.69f, 65.69f)
                    curveToRelative(-6.28f, 0f, -11.44f, -5.69f, -11.44f, -12.69f)
                    reflectiveCurveToRelative(5f, -12.74f, 11.44f, -12.74f)
                    reflectiveCurveTo(96.23f, 46f, 96.12f, 53f)
                    reflectiveCurveTo(91.08f, 65.69f, 84.69f, 65.69f)
                    close()
                }
            }
        }.build()
    }
}

@Composable
fun AndroidIcon(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "AndroidIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            group(translationY = 960f) {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(40f, -240f)
                    quadToRelative(9f, -107f, 65.5f, -197f)
                    reflectiveQuadTo(256f, -580f)
                    lineToRelative(-74f, -128f)
                    quadToRelative(-6f, -9f, -3f, -19f)
                    reflectiveQuadToRelative(13f, -15f)
                    quadToRelative(8f, -5f, 18f, -2f)
                    reflectiveQuadToRelative(16f, 12f)
                    lineToRelative(74f, 128f)
                    quadToRelative(86f, -36f, 180f, -36f)
                    reflectiveQuadToRelative(180f, 36f)
                    lineToRelative(74f, -128f)
                    quadToRelative(6f, -9f, 16f, -12f)
                    reflectiveQuadToRelative(18f, 2f)
                    quadToRelative(10f, 5f, 13f, 15f)
                    reflectiveQuadToRelative(-3f, 19f)
                    lineToRelative(-74f, 128f)
                    quadToRelative(94f, 53f, 150.5f, 143f)
                    reflectiveQuadTo(920f, -240f)
                    horizontalLineTo(40f)
                    close()
                    // Eyes
                    moveTo(315.5f, -364.5f)
                    quadTo(330f, -379f, 330f, -400f)
                    reflectiveQuadToRelative(-14.5f, -35.5f)
                    quadTo(301f, -450f, 280f, -450f)
                    reflectiveQuadToRelative(-35.5f, 14.5f)
                    quadTo(230f, -421f, 230f, -400f)
                    reflectiveQuadToRelative(14.5f, 35.5f)
                    quadTo(259f, -350f, 280f, -350f)
                    reflectiveQuadToRelative(35.5f, -14.5f)
                    close()
                    moveTo(715.5f, -364.5f)
                    quadTo(730f, -379f, 730f, -400f)
                    reflectiveQuadToRelative(-14.5f, -35.5f)
                    quadTo(701f, -450f, 680f, -450f)
                    reflectiveQuadToRelative(-35.5f, 14.5f)
                    quadTo(630f, -421f, 630f, -400f)
                    reflectiveQuadToRelative(14.5f, 35.5f)
                    quadTo(659f, -350f, 680f, -350f)
                    reflectiveQuadToRelative(35.5f, -14.5f)
                    close()
                }
            }
        }.build()
    }
}

@Composable
fun LibraryIcon(): ImageVector {
    val primaryColor = MaterialTheme.colorScheme.primary
    return remember {
        ImageVector.Builder(
            name = "LibraryIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            group(translationY = 960f) {
                path(fill = SolidColor(primaryColor)) {
                    moveTo(360f, -400f)
                    horizontalLineToRelative(400f)
                    lineTo(622f, -580f)
                    lineToRelative(-92f, 120f)
                    lineToRelative(-62f, -80f)
                    lineToRelative(-108f, 140f)
                    close()
                    
                    moveToRelative(-40f, 160f)
                    quadToRelative(-33f, 0f, -56.5f, -23.5f)
                    reflectiveQuadTo(240f, -320f)
                    verticalLineToRelative(-480f)
                    quadToRelative(0f, -33f, 23.5f, -56.5f)
                    reflectiveQuadTo(320f, -880f)
                    horizontalLineToRelative(480f)
                    quadToRelative(33f, 0f, 56.5f, 23.5f)
                    reflectiveQuadTo(880f, -800f)
                    verticalLineToRelative(480f)
                    quadToRelative(0f, 33f, -23.5f, 56.5f)
                    reflectiveQuadTo(800f, -240f)
                    horizontalLineTo(320f)
                    close()
                    
                    moveToRelative(0f, -80f)
                    horizontalLineToRelative(480f)
                    verticalLineToRelative(-480f)
                    horizontalLineTo(320f)
                    verticalLineToRelative(480f)
                    close()
                    
                    moveTo(160f, -80f)
                    quadToRelative(-33f, 0f, -56.5f, -23.5f)
                    reflectiveQuadTo(80f, -160f)
                    verticalLineToRelative(-560f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(560f)
                    horizontalLineToRelative(560f)
                    verticalLineToRelative(80f)
                    horizontalLineTo(160f)
                    close()
                    
                    moveToRelative(160f, -720f)
                    verticalLineToRelative(480f)
                    verticalLineToRelative(-480f)
                    close()
                }
            }
        }.build()
    }
}

@Composable
fun SearchSuccessIcon(): ImageVector {
    val primaryColor = MaterialTheme.colorScheme.primary
    return remember {
        ImageVector.Builder(
            name = "SearchSuccessIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            group(translationY = 960f) {
                path(fill = SolidColor(primaryColor)) {
                    moveTo(358f, -488f)
                    lineToRelative(-97f, -96f)
                    lineToRelative(42f, -42f)
                    lineToRelative(54f, 54f)
                    lineToRelative(100f, -100f)
                    lineToRelative(42f, 42f)
                    lineToRelative(-141f, 142f)
                    close()
                    
                    moveToRelative(426f, 368f)
                    lineToRelative(-252f, -252f)
                    quadToRelative(-30f, 24f, -69f, 38f)
                    reflectiveQuadToRelative(-83f, 14f)
                    quadToRelative(-109f, 0f, -184.5f, -75.5f)
                    reflectiveQuadTo(120f, -580f)
                    quadToRelative(0f, -109f, 75.5f, -184.5f)
                    reflectiveQuadTo(380f, -840f)
                    quadToRelative(109f, 0f, 184.5f, 75.5f)
                    reflectiveQuadTo(640f, -580f)
                    quadToRelative(0f, 44f, -14f, 83f)
                    reflectiveQuadToRelative(-38f, 69f)
                    lineToRelative(252f, 252f)
                    lineToRelative(-56f, 56f)
                    close()
                    
                    moveTo(380f, -400f)
                    quadToRelative(75f, 0f, 127.5f, -52.5f)
                    reflectiveQuadTo(560f, -580f)
                    quadToRelative(0f, -75f, -52.5f, -127.5f)
                    reflectiveQuadTo(380f, -760f)
                    quadToRelative(-75f, 0f, -127.5f, 52.5f)
                    reflectiveQuadTo(200f, -580f)
                    quadToRelative(0f, 75f, 52.5f, 127.5f)
                    reflectiveQuadTo(380f, -400f)
                    close()
                }
            }
        }.build()
    }
}

