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
private const val WALL_SEGMENT_OVERLAP = 0.25f

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
 * How far a wall segment running from [from] to [to] actually reaches.
 *
 * Every segment but the slice's last overruns its successor by a fraction of a step. Two
 * anti-aliased quads meeting on an exact shared edge leave a hairline of background between them, and
 * on a wall that reads as a row of pale stripes down the side of the disc. The final segment is left
 * alone so the slice still ends exactly where its neighbour begins.
 */
private fun wallEnd(
    from: Float,
    to: Float,
    last: Float,
): Float =
    if (to == last) {
        to
    } else {
        to + (to - from) * WALL_SEGMENT_OVERLAP
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
 * of it, which sorting by depth handles without either slice knowing about the other. They stay a
 * strip of quads rather than becoming one polygon, because one polygon would carry a single depth for
 * a band that can wrap most of the way round the disc, and that depth would sort wrongly against the
 * neighbours. Each quad instead overruns its successor slightly: neighbours within a slice share a
 * colour, so the overlap is invisible, whereas the hairline it closes is not.
 *
 * The radial cuts either side of a slice are built **only when the disc is exploded**. On a whole
 * disc the slices tile the full circle, so every cut is an interior surface that nothing can see —
 * and building one anyway lets it be painted over the neighbour it hides behind, which shows up as a
 * wedge of the wrong colour lying across that neighbour's top. Back-face culling is the usual answer
 * and is the wrong one here: an arc's winding flips as it travels round the circle, so the same test
 * that keeps a box honest discards half a rim.
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

        val last = angles.last()
        angles.zipWithNext().forEach { (from, to) ->
            val reach = wallEnd(from = from, to = to, last = last)
            add(
                corners =
                    listOf(
                        rimPoint(degrees = from, radius = SCENE_RADIUS, y = -thickness, offset = shift),
                        rimPoint(degrees = reach, radius = SCENE_RADIUS, y = -thickness, offset = shift),
                        rimPoint(degrees = reach, radius = SCENE_RADIUS, y = 0f, offset = shift),
                        rimPoint(degrees = from, radius = SCENE_RADIUS, y = 0f, offset = shift),
                    ),
                side = FaceSide.FRONT,
                slice = sweep.data,
            )
            if (inner > 0f) {
                add(
                    corners =
                        listOf(
                            rimPoint(degrees = reach, radius = inner, y = -thickness, offset = shift),
                            rimPoint(degrees = from, radius = inner, y = -thickness, offset = shift),
                            rimPoint(degrees = from, radius = inner, y = 0f, offset = shift),
                            rimPoint(degrees = reach, radius = inner, y = 0f, offset = shift),
                        ),
                    side = FaceSide.SIDE,
                    slice = sweep.data,
                )
            }
        }

        val cuts =
            if (config.explodeFraction > 0f) {
                listOf(sweep.startDegrees, sweep.startDegrees + sweep.sweepDegrees)
            } else {
                emptyList()
            }
        cuts.forEach { cut ->
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

/**
 * Orders a disc's faces for drawing: whole slices from far to near, and within each slice its walls
 * and cuts before its top.
 *
 * Sorting every face by its own mean depth is not enough here. A slice's radial cut runs from its
 * inner edge to its outer one through the full thickness, so its mean depth can come out nearer than
 * the top it belongs to — and the cut then paints over the surface it is supposed to bound, which
 * reads as a slice missing its top.
 *
 * Grouping by slice first removes the question. A disc seen from above always shows a slice's top in
 * front of that same slice's sides, whatever their mean depths work out to, while between slices the
 * mean depth is a sound comparison because they do not interpenetrate.
 */
internal fun List<ProjectedFace<PieData>>.orderedForDrawing(): List<ProjectedFace<PieData>> =
    groupBy { face -> face.payload }
        .entries
        .sortedByDescending { (_, slice) -> slice.sumOf { face -> face.depth.toDouble() } / slice.size }
        .flatMap { (_, slice) ->
            slice.sortedBy { face ->
                if (face.side == FaceSide.TOP) {
                    1
                } else {
                    0
                }
            }
        }
