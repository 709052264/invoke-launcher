package org.invokevirtual.launcher.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import org.invokevirtual.launcher.data.TileShapePreset
import kotlin.math.min
import kotlin.math.roundToInt

private fun diagonalLeafPolygon(primaryTopLeft: Boolean): RoundedPolygon = RoundedPolygon(
    vertices = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f),
    perVertexRounding = if (primaryTopLeft) {
        listOf(
            CornerRounding(radius = 0.46f, smoothing = 0.6f),
            CornerRounding(radius = 0.12f, smoothing = 0.3f),
            CornerRounding(radius = 0.46f, smoothing = 0.6f),
            CornerRounding(radius = 0.12f, smoothing = 0.3f),
        )
    } else {
        listOf(
            CornerRounding(radius = 0.12f, smoothing = 0.3f),
            CornerRounding(radius = 0.46f, smoothing = 0.6f),
            CornerRounding(radius = 0.12f, smoothing = 0.3f),
            CornerRounding(radius = 0.46f, smoothing = 0.6f),
        )
    },
    centerX = 0.5f,
    centerY = 0.5f,
)

private val LeafPolygon: RoundedPolygon by lazy { diagonalLeafPolygon(primaryTopLeft = true) }
private val LeafAltPolygon: RoundedPolygon by lazy { diagonalLeafPolygon(primaryTopLeft = false) }
private val TeardropPolygon: RoundedPolygon by lazy {
    RoundedPolygon(
        vertices = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f),
        perVertexRounding = listOf(
            CornerRounding(radius = 0.10f, smoothing = 0.25f),
            CornerRounding(radius = 0.48f, smoothing = 0.6f),
            CornerRounding(radius = 0.48f, smoothing = 0.6f),
            CornerRounding(radius = 0.48f, smoothing = 0.6f),
        ),
        centerX = 0.5f,
        centerY = 0.5f,
    )
}
private val HexagonPolygon: RoundedPolygon by lazy {
    RoundedPolygon(
        numVertices = 6,
        radius = 0.5f,
        centerX = 0.5f,
        centerY = 0.5f,
        rounding = CornerRounding(radius = 0.16f, smoothing = 0.4f),
    )
}
private val CrispRectPolygon: RoundedPolygon by lazy {
    RoundedPolygon(
        vertices = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f),
        rounding = CornerRounding(radius = 0.14f, smoothing = 0.25f),
        centerX = 0.5f,
        centerY = 0.5f,
    )
}

fun TileShapePreset.restPolygon(): RoundedPolygon = when (this) {
    TileShapePreset.SQUIRCLE -> MaterialShapes.Square
    TileShapePreset.CIRCLE -> MaterialShapes.Circle
    TileShapePreset.PILL -> MaterialShapes.Pill
    TileShapePreset.LEAF -> LeafPolygon
    TileShapePreset.LEAF_ALT -> LeafAltPolygon
    TileShapePreset.ARCH -> MaterialShapes.Arch
    TileShapePreset.TEARDROP -> TeardropPolygon
    TileShapePreset.GEM -> MaterialShapes.Gem
    TileShapePreset.HEXAGON -> HexagonPolygon
    TileShapePreset.COOKIE -> MaterialShapes.Cookie4Sided
    TileShapePreset.CLOVER -> MaterialShapes.Clover4Leaf
    TileShapePreset.SOFT_BURST -> MaterialShapes.SoftBurst
    TileShapePreset.RECTANGLE -> CrispRectPolygon
}

fun TileShapePreset.pressedPolygon(): RoundedPolygon = when (this) {
    TileShapePreset.SQUIRCLE -> MaterialShapes.Cookie4Sided
    TileShapePreset.CIRCLE -> MaterialShapes.Cookie6Sided
    TileShapePreset.PILL -> MaterialShapes.Cookie4Sided
    TileShapePreset.LEAF -> MaterialShapes.Pill
    TileShapePreset.LEAF_ALT -> MaterialShapes.Pill
    TileShapePreset.ARCH -> MaterialShapes.Pill
    TileShapePreset.TEARDROP -> MaterialShapes.Circle
    TileShapePreset.GEM -> MaterialShapes.Clover4Leaf
    TileShapePreset.HEXAGON -> MaterialShapes.Cookie6Sided
    TileShapePreset.COOKIE -> MaterialShapes.Circle
    TileShapePreset.CLOVER -> MaterialShapes.SoftBurst
    TileShapePreset.SOFT_BURST -> MaterialShapes.Cookie9Sided
    TileShapePreset.RECTANGLE -> MaterialShapes.Square
}

/**
 * Generates an aspect-aware [RoundedPolygon] normalized into `[0..1, 0..1]` space so corners,
 * chamfers, arches, and scallops stay geometrically proportional at ANY custom widget or tile
 * aspect ratio (`1×2`, `2×1`, `2×2`, `3×2`, `4×1`, `4×2`, `4×3`, or arbitrary custom dimensions),
 * enabling true `androidx.graphics.shapes.Morph` transitions across both shape and size changes.
 */
fun TileShapePreset.toAspectPolygon(aspectRatio: Float): RoundedPolygon {
    val aspect = aspectRatio.coerceIn(0.25f, 4.5f)
    if (aspect in 0.84f..1.18f) {
        return restPolygon()
    }

    val minDim = min(aspect, 1f)
    val rawPoly = when (this) {
        TileShapePreset.SQUIRCLE -> RoundedPolygon(
            vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
            rounding = CornerRounding(radius = minDim * 0.24f, smoothing = 0.72f),
            centerX = aspect * 0.5f,
            centerY = 0.5f,
        )

        TileShapePreset.CIRCLE, TileShapePreset.PILL -> RoundedPolygon(
            vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
            rounding = CornerRounding(radius = minDim * 0.50f, smoothing = 0.05f),
            centerX = aspect * 0.5f,
            centerY = 0.5f,
        )

        TileShapePreset.RECTANGLE -> RoundedPolygon(
            vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
            rounding = CornerRounding(radius = minDim * 0.12f, smoothing = 0.2f),
            centerX = aspect * 0.5f,
            centerY = 0.5f,
        )

        TileShapePreset.LEAF -> {
            val large = minDim * 0.42f
            val small = minDim * 0.12f
            RoundedPolygon(
                vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
                perVertexRounding = listOf(
                    CornerRounding(radius = large, smoothing = 0.6f),
                    CornerRounding(radius = small, smoothing = 0.25f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                    CornerRounding(radius = small, smoothing = 0.25f),
                ),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.LEAF_ALT -> {
            val large = minDim * 0.42f
            val small = minDim * 0.12f
            RoundedPolygon(
                vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
                perVertexRounding = listOf(
                    CornerRounding(radius = small, smoothing = 0.25f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                    CornerRounding(radius = small, smoothing = 0.25f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                ),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.ARCH -> {
            val topR = minDim * 0.46f
            val bottomR = minDim * 0.14f
            RoundedPolygon(
                vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
                perVertexRounding = listOf(
                    CornerRounding(radius = topR, smoothing = 0.6f),
                    CornerRounding(radius = topR, smoothing = 0.6f),
                    CornerRounding(radius = bottomR, smoothing = 0.25f),
                    CornerRounding(radius = bottomR, smoothing = 0.25f),
                ),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.TEARDROP -> {
            val large = minDim * 0.44f
            val small = minDim * 0.12f
            RoundedPolygon(
                vertices = floatArrayOf(0f, 0f, aspect, 0f, aspect, 1f, 0f, 1f),
                perVertexRounding = listOf(
                    CornerRounding(radius = small, smoothing = 0.25f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                    CornerRounding(radius = large, smoothing = 0.6f),
                ),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.GEM -> {
            val cut = minDim * 0.22f
            RoundedPolygon(
                vertices = floatArrayOf(
                    cut, 0f,
                    aspect - cut, 0f,
                    aspect, cut,
                    aspect, 1f - cut,
                    aspect - cut, 1f,
                    cut, 1f,
                    0f, 1f - cut,
                    0f, cut,
                ),
                rounding = CornerRounding(radius = minDim * 0.04f, smoothing = 0.2f),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.HEXAGON -> {
            val inset = minDim * 0.20f
            val vertices = if (aspect >= 1f) {
                floatArrayOf(
                    inset, 0f,
                    aspect - inset, 0f,
                    aspect, 0.5f,
                    aspect - inset, 1f,
                    inset, 1f,
                    0f, 0.5f,
                )
            } else {
                floatArrayOf(
                    aspect * 0.5f, 0f,
                    aspect, inset,
                    aspect, 1f - inset,
                    aspect * 0.5f, 1f,
                    0f, 1f - inset,
                    0f, inset,
                )
            }
            RoundedPolygon(
                vertices = vertices,
                rounding = CornerRounding(radius = minDim * 0.10f, smoothing = 0.4f),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.CLOVER -> {
            val pinch = minDim * 0.09f
            val lobeR = minDim * 0.34f
            val pinchR = minDim * 0.22f
            RoundedPolygon(
                vertices = floatArrayOf(
                    0f, 0f,
                    aspect * 0.5f, pinch,
                    aspect, 0f,
                    aspect - pinch * 0.7f, 0.5f,
                    aspect, 1f,
                    aspect * 0.5f, 1f - pinch,
                    0f, 1f,
                    pinch * 0.7f, 0.5f,
                ),
                perVertexRounding = listOf(
                    CornerRounding(radius = lobeR, smoothing = 0.5f),
                    CornerRounding(radius = pinchR, smoothing = 0.5f),
                    CornerRounding(radius = lobeR, smoothing = 0.5f),
                    CornerRounding(radius = pinchR, smoothing = 0.5f),
                    CornerRounding(radius = lobeR, smoothing = 0.5f),
                    CornerRounding(radius = pinchR, smoothing = 0.5f),
                    CornerRounding(radius = lobeR, smoothing = 0.5f),
                    CornerRounding(radius = pinchR, smoothing = 0.5f),
                ),
                centerX = aspect * 0.5f,
                centerY = 0.5f,
            )
        }

        TileShapePreset.COOKIE -> buildScallopedPerimeterPolygon(
            aspect = aspect,
            minDim = minDim,
            amp = minDim * 0.055f,
            crestsX = if (aspect >= 2.0f) 4 else if (aspect >= 1.18f) 3 else 1,
            crestsY = if (aspect <= 0.55f) 4 else if (aspect <= 0.84f) 3 else 1,
            cornerRadius = minDim * 0.11f,
        )

        TileShapePreset.SOFT_BURST -> buildScallopedPerimeterPolygon(
            aspect = aspect,
            minDim = minDim,
            amp = minDim * 0.042f,
            crestsX = if (aspect >= 2.0f) 5 else if (aspect >= 1.18f) 4 else 1,
            crestsY = if (aspect <= 0.55f) 5 else if (aspect <= 0.84f) 4 else 1,
            cornerRadius = minDim * 0.075f,
        )
    }

    return rawPoly.transformed { x, y -> TransformResult(x / aspect, y) }
}

private fun buildScallopedPerimeterPolygon(
    aspect: Float,
    minDim: Float,
    amp: Float,
    crestsX: Int,
    crestsY: Int,
    cornerRadius: Float,
): RoundedPolygon {
    val inset = minDim * 0.11f
    val xStart = inset
    val xEnd = (aspect - inset).coerceAtLeast(xStart + 0.05f)
    val yStart = inset
    val yEnd = (1f - inset).coerceAtLeast(yStart + 0.05f)
    val coords = mutableListOf<Float>()

    fun addPt(x: Float, y: Float) {
        coords += x
        coords += y
    }

    val stepsX = crestsX * 2
    val stepsY = crestsY * 2

    // Top-left valley anchor -> top waves -> top-right valley anchor
    addPt(amp, amp)
    for (i in 0..stepsX) {
        val t = i.toFloat() / stepsX
        addPt(xStart + (xEnd - xStart) * t, if (i % 2 == 0) amp else 0f)
    }
    addPt(aspect - amp, amp)

    // Right waves -> bottom-right valley anchor
    for (i in 0..stepsY) {
        val t = i.toFloat() / stepsY
        addPt(if (i % 2 == 0) aspect - amp else aspect, yStart + (yEnd - yStart) * t)
    }
    addPt(aspect - amp, 1f - amp)

    // Bottom waves -> bottom-left valley anchor
    for (i in 0..stepsX) {
        val t = i.toFloat() / stepsX
        addPt(xEnd - (xEnd - xStart) * t, if (i % 2 == 0) 1f - amp else 1f)
    }
    addPt(amp, 1f - amp)

    // Left waves
    for (i in 0..stepsY) {
        val t = i.toFloat() / stepsY
        addPt(if (i % 2 == 0) amp else 0f, yEnd - (yEnd - yStart) * t)
    }

    return RoundedPolygon(
        vertices = coords.toFloatArray(),
        rounding = CornerRounding(radius = cornerRadius, smoothing = 0.45f),
        centerX = aspect * 0.5f,
        centerY = 0.5f,
    )
}

fun TileShapePreset.toPressedAspectPolygon(aspectRatio: Float): RoundedPolygon {
    val aspect = aspectRatio.coerceIn(0.25f, 4.5f)
    if (aspect in 0.84f..1.18f) {
        return pressedPolygon()
    }
    val targetPreset = when (this) {
        TileShapePreset.SQUIRCLE -> TileShapePreset.COOKIE
        TileShapePreset.CIRCLE -> TileShapePreset.SQUIRCLE
        TileShapePreset.PILL -> TileShapePreset.SQUIRCLE
        TileShapePreset.LEAF -> TileShapePreset.PILL
        TileShapePreset.LEAF_ALT -> TileShapePreset.PILL
        TileShapePreset.ARCH -> TileShapePreset.PILL
        TileShapePreset.TEARDROP -> TileShapePreset.CIRCLE
        TileShapePreset.GEM -> TileShapePreset.CLOVER
        TileShapePreset.HEXAGON -> TileShapePreset.PILL
        TileShapePreset.COOKIE -> TileShapePreset.CIRCLE
        TileShapePreset.CLOVER -> TileShapePreset.SOFT_BURST
        TileShapePreset.SOFT_BURST -> TileShapePreset.COOKIE
        TileShapePreset.RECTANGLE -> TileShapePreset.SQUIRCLE
    }
    return targetPreset.toAspectPolygon(aspect)
}

/**
 * Aspect-safe container shapes for multi-cell tiles and previews.
 */
fun TileShapePreset.toContainerShape(
    isWide: Boolean = false,
    isHero: Boolean = false,
    isTall: Boolean = false,
): Shape = when (this) {
    TileShapePreset.SQUIRCLE -> RoundedCornerShape(
        when {
            isHero -> 28.dp
            isTall -> 24.dp
            else -> 22.dp
        },
    )

    TileShapePreset.CIRCLE -> when {
        isHero -> CircleShape
        isWide || isTall -> RoundedCornerShape(percent = 50)
        else -> RoundedCornerShape(48.dp)
    }

    TileShapePreset.PILL -> RoundedCornerShape(percent = 50)

    TileShapePreset.LEAF -> RoundedCornerShape(
        topStart = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
    )

    TileShapePreset.LEAF_ALT -> RoundedCornerShape(
        topStart = 10.dp,
        topEnd = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
        bottomStart = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
        bottomEnd = 10.dp,
    )

    TileShapePreset.ARCH -> RoundedCornerShape(
        topStart = when {
            isHero -> 54.dp
            isTall -> 38.dp
            else -> 34.dp
        },
        topEnd = when {
            isHero -> 54.dp
            isTall -> 38.dp
            else -> 34.dp
        },
        bottomStart = 12.dp,
        bottomEnd = 12.dp,
    )

    TileShapePreset.TEARDROP -> RoundedCornerShape(
        topStart = 10.dp,
        topEnd = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
        bottomStart = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
        bottomEnd = when {
            isHero -> 48.dp
            isTall -> 28.dp
            else -> 30.dp
        },
    )

    TileShapePreset.GEM -> CutCornerShape(if (isHero) 28.dp else 18.dp)

    TileShapePreset.HEXAGON -> when {
        isHero && !isWide -> MorphingPolygonShape(Morph(HexagonPolygon, HexagonPolygon), progress = 0f)
        isTall -> hexagonVerticalRibbonShape()
        else -> hexagonRibbonShape()
    }

    TileShapePreset.COOKIE -> if (!isWide && !isTall && isHero) {
        MorphingPolygonShape(Morph(MaterialShapes.Square, MaterialShapes.Cookie4Sided), progress = 0.65f)
    } else {
        scallopedContainerShape(waveCount = if (isWide) 6 else 4, amplitudeRatio = 0.04f)
    }

    TileShapePreset.CLOVER -> if (!isWide && !isTall && isHero) {
        MorphingPolygonShape(Morph(MaterialShapes.Square, MaterialShapes.Clover4Leaf), progress = 0.58f)
    } else {
        doubleLobeCapsuleShape()
    }

    TileShapePreset.SOFT_BURST -> if (!isWide && !isTall && isHero) {
        MorphingPolygonShape(Morph(MaterialShapes.Square, MaterialShapes.SoftBurst), progress = 0.56f)
    } else {
        scallopedContainerShape(waveCount = if (isWide) 8 else 5, amplitudeRatio = 0.035f)
    }

    TileShapePreset.RECTANGLE -> RoundedCornerShape(10.dp)
}

private fun hexagonRibbonShape(): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val insetX = (min(w, h) * 0.14f).coerceAtMost(w * 0.16f)
    moveTo(insetX, 0f)
    lineTo(w - insetX, 0f)
    lineTo(w, h * 0.5f)
    lineTo(w - insetX, h)
    lineTo(insetX, h)
    lineTo(0f, h * 0.5f)
    close()
}

private fun hexagonVerticalRibbonShape(): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val insetY = (min(w, h) * 0.16f).coerceAtMost(h * 0.14f)
    moveTo(w * 0.5f, 0f)
    lineTo(w, insetY)
    lineTo(w, h - insetY)
    lineTo(w * 0.5f, h)
    lineTo(0f, h - insetY)
    lineTo(0f, insetY)
    close()
}

private fun scallopedContainerShape(
    waveCount: Int = 6,
    amplitudeRatio: Float = 0.045f,
): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val minDim = min(w, h)
    val amp = minDim * amplitudeRatio
    val corner = minDim * 0.16f

    moveTo(corner, amp)
    val topSpan = (w - 2f * corner).coerceAtLeast(1f)
    val step = topSpan / waveCount
    for (i in 0 until waveCount) {
        val x0 = corner + i * step
        val xMid = x0 + step * 0.5f
        val x1 = x0 + step
        val cy = if (i % 2 == 0) 0f else amp * 1.4f
        quadraticTo(xMid, cy, x1, amp)
    }

    quadraticTo(w - amp, amp, w - amp, corner)
    lineTo(w - amp, h - corner)
    quadraticTo(w - amp, h - amp, w - corner, h - amp)

    for (i in 0 until waveCount) {
        val x0 = (w - corner) - i * step
        val xMid = x0 - step * 0.5f
        val x1 = x0 - step
        val cy = if (i % 2 == 0) h else h - amp * 1.4f
        quadraticTo(xMid, cy, x1, h - amp)
    }

    quadraticTo(amp, h - amp, amp, h - corner)
    lineTo(amp, corner)
    quadraticTo(amp, amp, corner, amp)
    close()
}

private fun doubleLobeCapsuleShape(): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val minDim = min(w, h)
    val r = (minDim * 0.34f).coerceAtMost(w * 0.3f)
    val pinch = minDim * 0.06f

    moveTo(r, 0f)
    quadraticTo(w * 0.5f, pinch * 2f, w - r, 0f)
    quadraticTo(w, 0f, w, h * 0.5f)
    quadraticTo(w, h, w - r, h)
    quadraticTo(w * 0.5f, h - pinch * 2f, r, h)
    quadraticTo(0f, h, 0f, h * 0.5f)
    quadraticTo(0f, 0f, r, 0f)
    close()
}

object InvokeShapes {
    val BentoCapsuleShape = RoundedCornerShape(32.dp)

    fun segmentedItemShape(
        index: Int,
        count: Int,
        outerRadius: Dp = 20.dp,
        innerRadius: Dp = 4.dp,
    ): RoundedCornerShape = when {
        count <= 1 -> RoundedCornerShape(outerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius,
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius,
        )
        else -> RoundedCornerShape(innerRadius)
    }
}

class MorphingPolygonShape(
    private val morph: Morph,
    private val progressProvider: () -> Float,
    private val rotationDegrees: Float = 0f,
) : Shape {
    constructor(
        morph: Morph,
        progress: Float,
        rotationDegrees: Float = 0f,
    ) : this(morph, { progress }, rotationDegrees)

    private val path = Path()
    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        path.rewind()
        morph.toPath(progress = progressProvider().coerceIn(0f, 1f), path = path)
        matrix.reset()
        matrix.scale(x = size.width, y = size.height)
        if (rotationDegrees != 0f) {
            val pivot = size.center
            matrix.translate(pivot.x, pivot.y)
            matrix.rotateZ(rotationDegrees)
            matrix.translate(-pivot.x, -pivot.y)
        }
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@Composable
fun rememberMorphShape(
    start: RoundedPolygon,
    end: RoundedPolygon,
    progress: Float,
    rotationDegrees: Float = 0f,
): Shape {
    val morph = remember(start, end) { Morph(start, end) }
    return remember(morph, progress, rotationDegrees) {
        MorphingPolygonShape(
            morph = morph,
            progress = progress,
            rotationDegrees = rotationDegrees,
        )
    }
}

/**
 * Animated aspect-aware shape morphing for widgets and multi-cell containers.
 * Smoothly morphs using [Morph] both when [shapePreset] or [aspectRatio] changes (no ugly
 * collapse/expand!) and when [pressed] changes, without recreating [Morph] or [Path] per frame.
 */
@Composable
fun rememberAnimatedContainerMorphShape(
    shapePreset: TileShapePreset,
    aspectRatio: Float,
    pressed: Boolean = false,
    reducedMotion: Boolean = LocalReducedMotion.current,
): Shape {
    val quantizedAspect = ((aspectRatio.coerceIn(0.25f, 4.5f) * 40f).roundToInt() / 40f)
    val targetRestPoly = remember(shapePreset, quantizedAspect) {
        shapePreset.toAspectPolygon(quantizedAspect)
    }
    val targetPressedPoly = remember(shapePreset, quantizedAspect) {
        shapePreset.toPressedAspectPolygon(quantizedAspect)
    }

    var fromPoly by remember { mutableStateOf(targetRestPoly) }
    var toPoly by remember { mutableStateOf(targetRestPoly) }
    val transitionProgress = remember { Animatable(1f) }
    val transitionSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(targetRestPoly, reducedMotion) {
        if (toPoly !== targetRestPoly) {
            if (reducedMotion) {
                fromPoly = targetRestPoly
                toPoly = targetRestPoly
                transitionProgress.snapTo(1f)
            } else {
                fromPoly = toPoly
                toPoly = targetRestPoly
                transitionProgress.snapTo(0f)
                transitionProgress.animateTo(1f, transitionSpec)
            }
        }
    }

    val fastSpatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val pressProgressState = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else fastSpatial,
        label = "containerPressMorph",
    )

    val isTransitioning = transitionProgress.value < 0.999f
    return if (isTransitioning) {
        val transitionMorph = remember(fromPoly, toPoly) { Morph(fromPoly, toPoly) }
        remember(transitionMorph) {
            MorphingPolygonShape(
                morph = transitionMorph,
                progressProvider = { transitionProgress.value },
            )
        }
    } else {
        val pressMorph = remember(targetRestPoly, targetPressedPoly) {
            Morph(targetRestPoly, targetPressedPoly)
        }
        remember(pressMorph) {
            MorphingPolygonShape(
                morph = pressMorph,
                progressProvider = { pressProgressState.value },
            )
        }
    }
}

