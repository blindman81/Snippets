package com.android.snippets.ui.shapes

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.compose.ui.graphics.Matrix

enum class AppShape(val displayName: String) {
    COOKIE_12_SIDED("12-sided cookie"),
    VERY_SUNNY("Very sunny"),
    GEM("Gem"),
    SQUARE("Square"),
    PILL("Pill")
}

val CookiePolygon = RoundedPolygon.star(
    numVerticesPerRadius = 12,
    radius = 0.5f,
    innerRadius = 0.5f * 0.88f,
    rounding = CornerRounding(0.5f * 0.12f),
    centerX = 0.5f,
    centerY = 0.5f
)

class RoundedPolygonShape(
    val polygon: RoundedPolygon
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = polygon.toPath().asComposePath()
        val bounds = path.getBounds()
        val matrix = Matrix()
        matrix.translate(-bounds.left, -bounds.top)
        val scaleX = if (bounds.width > 0f) size.width / bounds.width else 1f
        val scaleY = if (bounds.height > 0f) size.height / bounds.height else 1f
        matrix.scale(scaleX, scaleY)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.toComposeShape(): Shape {
    return when (this) {
        AppShape.COOKIE_12_SIDED -> RoundedPolygonShape(CookiePolygon)
        AppShape.VERY_SUNNY -> RoundedPolygonShape(MaterialShapes.VerySunny)
        AppShape.GEM -> RoundedPolygonShape(MaterialShapes.Gem)
        AppShape.SQUARE -> RoundedPolygonShape(MaterialShapes.Square)
        AppShape.PILL -> RoundedPolygonShape(MaterialShapes.Pill)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.getNormalizedPolygon(): RoundedPolygon {
    return when (this) {
        AppShape.COOKIE_12_SIDED -> CookiePolygon
        AppShape.VERY_SUNNY -> MaterialShapes.VerySunny
        AppShape.GEM -> MaterialShapes.Gem
        AppShape.SQUARE -> MaterialShapes.Square
        AppShape.PILL -> MaterialShapes.Pill
    }
}

val LocalAppShape = staticCompositionLocalOf<Shape> { RoundedPolygonShape(CookiePolygon) }
val LocalAppShapeType = staticCompositionLocalOf<AppShape> { AppShape.COOKIE_12_SIDED }
