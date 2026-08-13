package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.scatter.config.Scatter3DChartConfig
import com.himanshoe.charty3d.scatter.data.Scatter3DPoint
import kotlin.math.abs
import kotlin.math.sin

private const val SCENE_EXTENT = 100f
private const val EDGE_PADDING = 20f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f
private const val DEGREES_TO_RADIANS = 0.017453292f

/** The measured range of a scatter along one axis, and how to map a value onto the scene. */
internal data class AxisRange(
    val min: Float,
    val max: Float,
) {
    /**
     * Maps [value] onto `0..SCENE_EXTENT`.
     *
     * A range of zero — every observation sharing one value on this axis, which is a legitimate
     * dataset — would divide by zero, so it collapses to the middle of the axis instead.
     */
    fun normalize(value: Float): Float {
        val span = max - min
        return if (span <= 0f) {
            SCENE_EXTENT / HALF
        } else {
            (value - min) / span * SCENE_EXTENT
        }
    }
}

/** The three measured ranges of a cloud, one per axis. */
internal data class Scatter3DRanges(
    val x: AxisRange,
    val y: AxisRange,
    val z: AxisRange,
)

/** Measures the cloud on each axis independently, so no axis squashes the others. */
internal fun scatter3DRanges(dataList: List<Scatter3DPoint>): Scatter3DRanges {
    if (dataList.isEmpty()) {
        return Scatter3DRanges(
            x = AxisRange(min = 0f, max = 1f),
            y = AxisRange(min = 0f, max = 1f),
            z = AxisRange(min = 0f, max = 1f),
        )
    }
    return Scatter3DRanges(
        x = AxisRange(min = dataList.minOf { it.x }, max = dataList.maxOf { it.x }),
        y = AxisRange(min = dataList.minOf { it.y }, max = dataList.maxOf { it.y }),
        z = AxisRange(min = dataList.minOf { it.z }, max = dataList.maxOf { it.z }),
    )
}

/** A point's position in scene space, with y negated so larger values rise up the canvas. */
internal fun scatter3DScenePoint(
    point: Scatter3DPoint,
    ranges: Scatter3DRanges,
): Point3D =
    Point3D(
        x = ranges.x.normalize(point.x),
        y = -ranges.y.normalize(point.y),
        z = ranges.z.normalize(point.z),
    )

/** The eight corners of the box bounding the whole cloud. */
internal fun scatter3DBoxCorners(): List<Point3D> =
    listOf(0f, SCENE_EXTENT).flatMap { x ->
        listOf(0f, -SCENE_EXTENT).flatMap { y ->
            listOf(0f, SCENE_EXTENT).map { z -> Point3D(x = x, y = y, z = z) }
        }
    }

/** The twelve edges of the bounding box, as pairs of its corners. */
internal fun scatter3DBoxEdges(): List<Pair<Point3D, Point3D>> {
    val corners = scatter3DBoxCorners()
    val edges = mutableListOf<Pair<Point3D, Point3D>>()
    corners.forEachIndexed { i, a ->
        corners.drop(i + 1).forEach { b ->
            val shared =
                listOf(a.x == b.x, a.y == b.y, a.z == b.z).count { same -> same }
            if (shared == 2) {
                edges.add(a to b)
            }
        }
    }
    return edges
}

/**
 * One point ready to draw: where it lands, how big it is at that depth, and how far away it is.
 *
 * @property position Canvas position of the point's centre.
 * @property radius Drawn radius, already scaled for depth.
 * @property depth Distance from the viewer, used to draw far points before near ones.
 * @property floor Canvas position directly below the point on the box's floor, for its drop line.
 * @property data The observation this point represents.
 */
internal data class ProjectedPoint(
    val position: Offset,
    val radius: Float,
    val depth: Float,
    val floor: Offset,
    val data: Scatter3DPoint,
)

/**
 * Projects every observation, sizing each by its depth so the cloud reads as having a front and a
 * back.
 *
 * Size is interpolated between [Scatter3DChartConfig.nearScale] and
 * [Scatter3DChartConfig.farScale] across the depth actually present in this cloud, rather than an
 * absolute distance, so a shallow cloud separates as clearly as a deep one.
 */
internal fun scatter3DPoints(
    dataList: List<Scatter3DPoint>,
    config: Scatter3DChartConfig,
    ranges: Scatter3DRanges,
    progress: Float,
    fit: SceneFit,
): List<ProjectedPoint> {
    if (dataList.isEmpty()) {
        return emptyList()
    }
    val scene = dataList.map { point -> scatter3DScenePoint(point = point, ranges = ranges) }
    val depths = scene.map { point -> config.projection.toViewSpace(point).z }
    val nearest = depths.min()
    val furthest = depths.max()
    val depthSpan = (furthest - nearest).coerceAtLeast(MIN_SPAN)

    return dataList.mapIndexed { index, data ->
        val raised = scene[index].copy(y = scene[index].y * progress)
        val depth = depths[index]
        val nearness = 1f - (depth - nearest) / depthSpan
        val scale = config.farScale + (config.nearScale - config.farScale) * nearness
        ProjectedPoint(
            position = fit.apply(config.projection.project(point = raised, origin = Offset.Zero)),
            radius = config.pointRadius * scale,
            depth = depth,
            floor =
                fit.apply(
                    config.projection.project(
                        point = raised.copy(y = 0f),
                        origin = Offset.Zero,
                    ),
                ),
            data = data,
        )
    }
}

/** Measures the projected bounding box and returns the transform that centres it inside [size]. */
internal fun scatter3DFit(
    size: Size,
    config: Scatter3DChartConfig,
): SceneFit {
    val projected =
        scatter3DBoxCorners().map { point -> config.projection.project(point = point, origin = Offset.Zero) }
    val minX = projected.minOf { it.x }
    val maxX = projected.maxOf { it.x }
    val minY = projected.minOf { it.y }
    val maxY = projected.maxOf { it.y }
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

/**
 * Finds the observation drawn at [position], searching near points first so a tap selects the one on
 * top rather than one buried behind it.
 */
internal fun List<ProjectedPoint>.hitTestPoints(position: Offset): Scatter3DPoint? =
    sortedBy { point -> point.depth }
        .firstOrNull { point ->
            val dx = position.x - point.position.x
            val dy = position.y - point.position.y
            dx * dx + dy * dy <= point.radius * point.radius
        }?.data

/**
 * How much a circle lying on the box's floor is squashed vertically once projected.
 *
 * A horizontal plane seen at pitch θ compresses by `sin θ`, so a point's round shadow becomes an
 * ellipse of that aspect. Deriving it rather than picking a constant is what keeps the shadow
 * agreeing with the box it falls inside as the angle changes.
 */
internal fun scatter3DFloorSquash(config: Scatter3DChartConfig): Float =
    abs(sin(config.projection.pitch * DEGREES_TO_RADIANS))
