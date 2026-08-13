package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val SCENE_RADIUS = 50f
private const val FULL_TURN = 360f
private const val DEGREES_TO_RADIANS = (PI / 180f).toFloat()
private const val EDGE_PADDING = 16f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f
private const val START_ANGLE = -90f
private const val MIN_VISIBLE_PITCH = 6f

/** One slice's angular extent, in degrees, measured clockwise from twelve o'clock. */
internal data class SliceSweep(
    val data: PieData,
    val startDegrees: Float,
    val sweepDegrees: Float,
) {
    /** The angle halfway through the slice, where its label and explode direction point. */
    val midDegrees: Float get() = startDegrees + sweepDegrees / HALF
}

/**
 * Divides the disc among [dataList] in the order given, scaled by [progress] so the pie sweeps open.
 *
 * A total of zero has no shares to divide, so every slice gets nothing rather than a `NaN` sweep.
 */
internal fun pie3DSweeps(
    dataList: List<PieData>,
    progress: Float,
): List<SliceSweep> {
    val total = dataList.sumOf { slice -> slice.value.toDouble() }.toFloat()
    if (total <= 0f) {
        return dataList.map { slice -> SliceSweep(data = slice, startDegrees = START_ANGLE, sweepDegrees = 0f) }
    }
    var cursor = START_ANGLE
    return dataList.map { slice ->
        val sweep = (slice.value / total) * FULL_TURN * progress
        SliceSweep(data = slice, startDegrees = cursor, sweepDegrees = sweep).also { cursor += sweep }
    }
}

/** A point on the disc's rim at [degrees], at [radius], on the plane [y]. */
private fun rimPoint(
    degrees: Float,
    radius: Float,
    y: Float,
    offset: Offset,
): Point3D {
    val radians = degrees * DEGREES_TO_RADIANS
    return Point3D(x = offset.x + cos(radians) * radius, y = y, z = offset.y + sin(radians) * radius)
}

/**
 * How far a slice is pushed out from the centre, and in which direction.
 *
 * Exploding moves each slice along its own mid-angle, so the gap opens evenly rather than shearing
 * the disc to one side.
 */
private fun explodeOffset(
    sweep: SliceSweep,
    config: Pie3DChartConfig,
): Offset {
    if (config.explodeFraction == 0f) {
        return Offset.Zero
    }
    val radians = sweep.midDegrees * DEGREES_TO_RADIANS
    val distance = SCENE_RADIUS * config.explodeFraction
    return Offset(x = cos(radians) * distance, y = sin(radians) * distance)
}

/**
 * The angles at which a slice is sampled, always including both ends so neighbouring slices meet
 * exactly rather than leaving a hairline of background between them.
 */
private fun sliceAngles(
    sweep: SliceSweep,
    config: Pie3DChartConfig,
): List<Float> {
    val steps = max(1, (config.arcSegments * (sweep.sweepDegrees / FULL_TURN)).toInt())
    return (0..steps).map { step -> sweep.startDegrees + sweep.sweepDegrees * step / steps }
}

/**
 * The pitch actually used, raised off zero so the disc is never seen exactly edge-on.
 *
 * At a pitch of zero every face of a flat disc is edge-on, the culling rule correctly discards all
 * of them, and the chart renders nothing at all. Rather than let a caller reach that cliff, the
 * shallowest view is held just above it.
 */
internal fun pie3DProjection(config: Pie3DChartConfig) =
    if (config.projection.pitch in -MIN_VISIBLE_PITCH..MIN_VISIBLE_PITCH) {
        config.projection.copy(pitch = MIN_VISIBLE_PITCH)
    } else {
        config.projection
    }

/**
 * Builds every visible face of every slice: the top surface, the outer wall, the inner wall of a
 * ring, and the two radial cuts either side.
 *
 * The top surface is one polygon spanning the whole slice rather than a fan of quads. Adjacent
 * anti-aliased quads do not quite meet, and the seams read as faint radial lines across a slice that
 * should be flat colour.
 *
 * The walls are what make a pie read as a solid, and they are also where the painter's algorithm
 * earns its keep — the wall of a slice at the back must be drawn before the top of a slice in front
 * of it, which sorting by depth handles without either slice knowing about the other.
 */
internal fun pie3DFaces(
    dataList: List<PieData>,
    config: Pie3DChartConfig,
    progress: Float,
    fit: SceneFit,
): List<ProjectedFace<PieData>> {
    val projection = pie3DProjection(config)
    val thickness = SCENE_RADIUS * config.thicknessFraction
    val inner = SCENE_RADIUS * config.holeFraction
    val faces = mutableListOf<ProjectedFace<PieData>>()

    fun add(
        corners: List<Point3D>,
        side: FaceSide,
        slice: PieData,
    ) {
        val view = corners.map { point -> projection.toViewSpace(point) }
        faces.add(
            ProjectedFace(
                points =
                    corners.map { point ->
                        fit.apply(projection.project(point = point, origin = Offset.Zero))
                    },
                side = side,
                depth = view.sumOf { point -> point.z.toDouble() }.toFloat() / view.size,
                payload = slice,
            ),
        )
    }

    pie3DSweeps(dataList = dataList, progress = progress).forEach { sweep ->
        if (sweep.sweepDegrees <= 0f) {
            return@forEach
        }
        val shift = explodeOffset(sweep = sweep, config = config)
        val angles = sliceAngles(sweep = sweep, config = config)

        add(
            corners =
                angles.map { degrees ->
                    rimPoint(degrees = degrees, radius = inner, y = -thickness, offset = shift)
                } +
                    angles.reversed().map { degrees ->
                        rimPoint(degrees = degrees, radius = SCENE_RADIUS, y = -thickness, offset = shift)
                    },
            side = FaceSide.TOP,
            slice = sweep.data,
        )

        angles.zipWithNext().forEach { (from, to) ->
            add(
                corners =
                    listOf(
                        rimPoint(degrees = from, radius = SCENE_RADIUS, y = -thickness, offset = shift),
                        rimPoint(degrees = to, radius = SCENE_RADIUS, y = -thickness, offset = shift),
                        rimPoint(degrees = to, radius = SCENE_RADIUS, y = 0f, offset = shift),
                        rimPoint(degrees = from, radius = SCENE_RADIUS, y = 0f, offset = shift),
                    ),
                side = FaceSide.FRONT,
                slice = sweep.data,
            )
            if (inner > 0f) {
                add(
                    corners =
                        listOf(
                            rimPoint(degrees = to, radius = inner, y = -thickness, offset = shift),
                            rimPoint(degrees = from, radius = inner, y = -thickness, offset = shift),
                            rimPoint(degrees = from, radius = inner, y = 0f, offset = shift),
                            rimPoint(degrees = to, radius = inner, y = 0f, offset = shift),
                        ),
                    side = FaceSide.SIDE,
                    slice = sweep.data,
                )
            }
        }

        listOf(sweep.startDegrees, sweep.startDegrees + sweep.sweepDegrees).forEach { cut ->
            add(
                corners =
                    listOf(
                        rimPoint(degrees = cut, radius = inner, y = -thickness, offset = shift),
                        rimPoint(degrees = cut, radius = SCENE_RADIUS, y = -thickness, offset = shift),
                        rimPoint(degrees = cut, radius = SCENE_RADIUS, y = 0f, offset = shift),
                        rimPoint(degrees = cut, radius = inner, y = 0f, offset = shift),
                    ),
                side = FaceSide.SIDE,
                slice = sweep.data,
            )
        }
    }
    return faces
}

/** Measures the projected disc and returns the transform that centres it inside [size]. */
internal fun pie3DFit(
    size: Size,
    config: Pie3DChartConfig,
): SceneFit {
    val projection = pie3DProjection(config)
    val thickness = SCENE_RADIUS * config.thicknessFraction
    val reach = SCENE_RADIUS * (1f + config.explodeFraction)
    val corners =
        (0 until config.arcSegments).flatMap { step ->
            val degrees = FULL_TURN * step / config.arcSegments
            listOf(0f, -thickness).map { y -> rimPoint(degrees = degrees, radius = reach, y = y, offset = Offset.Zero) }
        }
    val projected = corners.map { point -> projection.project(point = point, origin = Offset.Zero) }
    if (projected.isEmpty()) {
        return SceneFit(scale = 1f, offset = Offset.Zero)
    }

    val minX = projected.minOf { point -> point.x }
    val maxX = projected.maxOf { point -> point.x }
    val minY = projected.minOf { point -> point.y }
    val maxY = projected.maxOf { point -> point.y }
    val spanX = (maxX - minX).coerceAtLeast(MIN_SPAN)
    val spanY = (maxY - minY).coerceAtLeast(MIN_SPAN)

    val availableWidth = (size.width - EDGE_PADDING * HALF).coerceAtLeast(MIN_SPAN)
    val availableHeight = (size.height - EDGE_PADDING * HALF).coerceAtLeast(MIN_SPAN)
    val scale = minOf(availableWidth / spanX, availableHeight / spanY)
    return SceneFit(
        scale = scale,
        offset =
            Offset(
                x = (size.width - spanX * scale) / HALF - minX * scale,
                y = (size.height - spanY * scale) / HALF - minY * scale,
            ),
    )
}

/** The canvas position a slice's label sits at: the middle of its top surface. */
internal fun pie3DLabelAnchor(
    sweep: SliceSweep,
    config: Pie3DChartConfig,
    fit: SceneFit,
): Offset {
    val projection = pie3DProjection(config)
    val thickness = SCENE_RADIUS * config.thicknessFraction
    val inner = SCENE_RADIUS * config.holeFraction
    val radius = (inner + SCENE_RADIUS) / HALF
    val shift = explodeOffset(sweep = sweep, config = config)
    return fit.apply(
        projection.project(
            point = rimPoint(degrees = sweep.midDegrees, radius = radius, y = -thickness, offset = shift),
            origin = Offset.Zero,
        ),
    )
}
