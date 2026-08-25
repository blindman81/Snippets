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

import androidx.graphics.shapes.Morph

enum class AppShape(val displayName: String) {
    COOKIE_12_SIDED("12-sided cookie"),
    COOKIE_4_SIDED("4-sided cookie"),
    VERY_SUNNY("Very sunny"),
    PILL("Pill"),
    PENTAGON("Pentagon"),
    OVAL("Oval")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookiePolygon: RoundedPolygon = MaterialShapes.Cookie12Sided

val CookieShape = RoundedPolygonShape(CookiePolygon)

class RoundedPolygonShape(
    val polygon: RoundedPolygon
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = polygon.toPath().asComposePath()
        val matrix = Matrix()
        val scale = minOf(size.width, size.height) / 2f
        matrix.translate(size.width / 2f, size.height / 2f)
        matrix.scale(scale, scale)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphPolygons: List<RoundedPolygon> by lazy {
    listOf(
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Pill,
        MaterialShapes.Pentagon,
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.VerySunny,
        MaterialShapes.Oval,
        MaterialShapes.Cookie12Sided
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphs: List<Morph> by lazy {
    val polygons = CookieHoldMorphPolygons
    (0 until polygons.size - 1).map { Morph(polygons[it], polygons[it + 1]) }
}

class MorphSequenceShape(
    val morphs: List<Morph>,
    val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (morphs.isEmpty()) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
        }
        val total = morphs.size
        val normalized = ((progress % total) + total) % total
        val index = normalized.toInt().coerceIn(0, total - 1)
        val fraction = (normalized - index).coerceIn(0f, 1f)

        val path = morphs[index].toPath(fraction).asComposePath()
        val matrix = Matrix()
        val scale = minOf(size.width, size.height) / 2f
        matrix.translate(size.width / 2f, size.height / 2f)
        matrix.scale(scale, scale)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.toComposeShape(): Shape {
    return when (this) {
        AppShape.COOKIE_12_SIDED -> RoundedPolygonShape(CookiePolygon)
        AppShape.COOKIE_4_SIDED -> RoundedPolygonShape(MaterialShapes.Cookie4Sided)
        AppShape.VERY_SUNNY -> RoundedPolygonShape(MaterialShapes.VerySunny)
        AppShape.PILL -> RoundedPolygonShape(MaterialShapes.Pill)
        AppShape.PENTAGON -> RoundedPolygonShape(MaterialShapes.Pentagon)
        AppShape.OVAL -> RoundedPolygonShape(MaterialShapes.Oval)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.getNormalizedPolygon(): RoundedPolygon {
    return when (this) {
        AppShape.COOKIE_12_SIDED -> CookiePolygon
        AppShape.COOKIE_4_SIDED -> MaterialShapes.Cookie4Sided
        AppShape.VERY_SUNNY -> MaterialShapes.VerySunny
        AppShape.PILL -> MaterialShapes.Pill
        AppShape.PENTAGON -> MaterialShapes.Pentagon
        AppShape.OVAL -> MaterialShapes.Oval
    }
}

val LocalAppShape = staticCompositionLocalOf<Shape> { RoundedPolygonShape(CookiePolygon) }
val LocalAppShapeType = staticCompositionLocalOf<AppShape> { AppShape.COOKIE_12_SIDED }

class PolygonDrawable(
    private val polygon: RoundedPolygon,
    private val fillColor: Int
) : android.graphics.drawable.Drawable() {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = android.graphics.Paint.Style.FILL
    }
    private val path = android.graphics.Path()

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        super.onBoundsChange(bounds)
        val polyPath = polygon.toPath()
        val matrix = android.graphics.Matrix()
        val scale = minOf(bounds.width(), bounds.height()).toFloat() / 2f
        matrix.setScale(scale, scale)
        matrix.postTranslate(bounds.exactCenterX(), bounds.exactCenterY())

        path.reset()
        polyPath.transform(matrix, path)
    }

    override fun draw(canvas: android.graphics.Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("android.graphics.PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

