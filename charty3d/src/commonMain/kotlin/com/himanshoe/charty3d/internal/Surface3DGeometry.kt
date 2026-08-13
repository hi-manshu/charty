package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.surface.config.Surface3DChartConfig
import com.himanshoe.charty3d.surface.data.SurfaceData
import kotlin.math.abs

private const val SCENE_EXTENT = 100f
private const val SCENE_HEIGHT = 55f
private const val EDGE_PADDING = 20f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f
private const val MIN_SLOPE_LIGHT = 0.72f
private const val SLOPE_LIGHT_RANGE = 0.28f

/** One quad of the mesh, carrying the height at its centre so it can be coloured and sorted. */
internal data class SurfaceQuad(
    val face: ProjectedFace<Float>,
    val meanHeight: Float,
    val flatness: Float,
)

/** The lowest and highest sample in the grid, which the colour ramp is stretched across. */
internal fun surfaceHeightRange(data: SurfaceData): ClosedFloatingPointRange<Float> {
    val min = data.heights.min()
    val max = data.heights.max()
    return if (min == max) {
        min..(min + 1f)
    } else {
        min..max
    }
}

/** Where a grid sample sits in scene space, with height scaled into the scene's own extent. */
private fun samplePoint(
    row: Int,
    column: Int,
    data: SurfaceData,
    range: ClosedFloatingPointRange<Float>,
    progress: Float,
): Point3D {
    val span = (range.endInclusive - range.start).coerceAtLeast(MIN_SPAN)
    val normalized = (data.heightAt(row = row, column = column) - range.start) / span
    return Point3D(
        x = column.toFloat() / (data.columnCount - 1) * SCENE_EXTENT,
        y = -normalized * SCENE_HEIGHT * progress,
        z = row.toFloat() / (data.rowCount - 1) * SCENE_EXTENT,
    )
}

/**
 * Builds the mesh as one quad per grid cell, each carrying its own mean height.
 *
 * Unlike the solids in this module the mesh is not closed, so nothing is culled: a surface can be
 * seen from underneath, and hiding the faces that tilt away would punch holes in it. Ordering alone
 * decides what covers what, which is why every quad carries its depth.
 */
internal fun surface3DQuads(
    data: SurfaceData,
    config: Surface3DChartConfig,
    progress: Float,
    fit: SceneFit,
): List<SurfaceQuad> {
    val range = surfaceHeightRange(data)
    val quads = mutableListOf<SurfaceQuad>()

    for (row in 0 until data.rowCount - 1) {
        for (column in 0 until data.columnCount - 1) {
            val corners =
                listOf(
                    samplePoint(row = row, column = column, data = data, range = range, progress = progress),
                    samplePoint(row = row, column = column + 1, data = data, range = range, progress = progress),
                    samplePoint(row = row + 1, column = column + 1, data = data, range = range, progress = progress),
                    samplePoint(row = row + 1, column = column, data = data, range = range, progress = progress),
                )
            val view = corners.map { point -> config.projection.toViewSpace(point) }
            val heights =
                listOf(
                    data.heightAt(row = row, column = column),
                    data.heightAt(row = row, column = column + 1),
                    data.heightAt(row = row + 1, column = column + 1),
                    data.heightAt(row = row + 1, column = column),
                )
            quads.add(
                SurfaceQuad(
                    face =
                        ProjectedFace(
                            points =
                                corners.map { point ->
                                    fit.apply(config.projection.project(point = point, origin = Offset.Zero))
                                },
                            side = com.himanshoe.charty3d.projection.FaceSide.TOP,
                            depth = view.sumOf { point -> point.z.toDouble() }.toFloat() / view.size,
                            payload = heights.average().toFloat(),
                        ),
                    meanHeight = heights.average().toFloat(),
                    flatness = quadFlatness(corners),
                ),
            )
        }
    }
    return quads
}

/**
 * How level a quad is, from `0f` for a vertical wall to `1f` for perfectly flat.
 *
 * A surface shaded by height alone has no relief — two hills of equal height look identical however
 * differently they are sloped. Measuring the tilt from the corner heights, rather than lighting the
 * projected polygon, keeps the shading tied to the data instead of the viewing angle.
 */
private fun quadFlatness(corners: List<Point3D>): Float {
    val rise = corners.maxOf { it.y } - corners.minOf { it.y }
    val run = SCENE_EXTENT / HALF
    return (1f - (abs(rise) / run).coerceIn(0f, 1f))
}

/** The colour a quad is filled with: the height ramp, optionally darkened by how steeply it tilts. */
internal fun surfaceQuadColor(
    quad: SurfaceQuad,
    range: ClosedFloatingPointRange<Float>,
    config: Surface3DChartConfig,
    low: Color,
    high: Color,
): Color {
    val span = (range.endInclusive - range.start).coerceAtLeast(MIN_SPAN)
    val base = lerp(low, high, ((quad.meanHeight - range.start) / span).coerceIn(0f, 1f))
    if (config.shading == com.himanshoe.charty3d.surface.config.SurfaceShading.HEIGHT) {
        return base
    }
    val light = MIN_SLOPE_LIGHT + SLOPE_LIGHT_RANGE * quad.flatness
    return Color(
        red = (base.red * light).coerceIn(0f, 1f),
        green = (base.green * light).coerceIn(0f, 1f),
        blue = (base.blue * light).coerceIn(0f, 1f),
        alpha = base.alpha,
    )
}

/** Measures the projected mesh and returns the transform that centres it inside [size]. */
internal fun surface3DFit(
    size: Size,
    config: Surface3DChartConfig,
): SceneFit {
    val corners =
        listOf(0f, SCENE_EXTENT).flatMap { x ->
            listOf(0f, -SCENE_HEIGHT).flatMap { y ->
                listOf(0f, SCENE_EXTENT).map { z -> Point3D(x = x, y = y, z = z) }
            }
        }
    val projected = corners.map { point -> config.projection.project(point = point, origin = Offset.Zero) }
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

/** Finds the grid cell drawn at [position], searching near quads first. */
internal fun List<SurfaceQuad>.hitTestQuads(position: Offset): SurfaceQuad? =
    sortedBy { quad -> quad.face.depth }
        .firstOrNull { quad -> quad.face.contains(position) }
