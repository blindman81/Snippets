package com.android.snippets.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

private var _thumbUpIcon: ImageVector? = null

val ThumbUpIcon: ImageVector
    get() {
        if (_thumbUpIcon != null) {
            return _thumbUpIcon!!
        }
        _thumbUpIcon = ImageVector.Builder(
            name = "ThumbUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            group(translationY = 960f) {
                path(
                    fill = SolidColor(Color(0xFF1F1F1F)),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                    strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(720f, -120f)
                    lineTo(280f, -120f)
                    lineToRelative(0f, -520f)
                    lineToRelative(280f, -280f)
                    lineToRelative(50f, 50f)
                    quadToRelative(7f, 7f, 11.5f, 19f)
                    reflectiveQuadToRelative(4.5f, 23f)
                    lineToRelative(0f, 14f)
                    lineToRelative(-44f, 174f)
                    lineToRelative(258f, 0f)
                    quadToRelative(32f, 0f, 56f, 24f)
                    reflectiveQuadToRelative(24f, 56f)
                    lineToRelative(0f, 80f)
                    quadToRelative(0f, 7f, -2f, 15f)
                    reflectiveQuadToRelative(-4f, 15f)
                    lineTo(794f, -168f)
                    quadToRelative(-9f, 20f, -30f, 34f)
                    reflectiveQuadToRelative(-44f, 14f)
                    close()
                    moveTo(360f, -200f)
                    lineToRelative(360f, 0f)
                    lineToRelative(120f, -280f)
                    lineToRelative(0f, -80f)
                    lineTo(480f, -560f)
                    lineToRelative(54f, -220f)
                    lineToRelative(-174f, 174f)
                    lineToRelative(0f, 406f)
                    close()
                    moveTo(360f, -606f)
                    lineToRelative(0f, 406f)
                    lineToRelative(0f, -406f)
                    close()
                    moveTo(280f, -640f)
                    lineToRelative(0f, 80f)
                    lineTo(160f, -560f)
                    lineToRelative(0f, 360f)
                    lineToRelative(120f, 0f)
                    lineToRelative(0f, 80f)
                    lineTo(80f, -120f)
                    lineToRelative(0f, -520f)
                    lineToRelative(200f, 0f)
                    close()
                }
            }
        }.build()
        return _thumbUpIcon!!
    }
