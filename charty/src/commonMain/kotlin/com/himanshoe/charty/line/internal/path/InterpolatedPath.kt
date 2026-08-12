package com.himanshoe.charty.line.internal.path

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.line.config.LineInterpolation

private const val CONTROL_POINT_DIVISOR = 3f
private const val CONTROL_POINT_MULTIPLIER = 2f

/**
 * One cubic-bezier hop of a [LineInterpolation.SMOOTH] outline, from the previous vertex to [end].
 *
 * @property control1 First bezier control point, level with the previous vertex.
 * @property control2 Second bezier control point, level with [end].
 * @property end Vertex the hop lands on.
 */
internal data class CubicSegment(
    val control1: Offset,
    val control2: Offset,
    val end: Offset,
)

/**
 * The corner a [LineInterpolation.STEP] outline turns at between [start] and [end]: the value of
 * [start] holds until the x of [end], then the outline jumps vertically.
 */
internal fun stepCorner(
    start: Offset,
    end: Offset,
): Offset = Offset(x = end.x, y = start.y)

/**
 * Expands [points] into the polyline a [LineInterpolation.STEP] outline traces: every consecutive
 * pair contributes its [stepCorner] followed by the next point, so `n` points yield `2n - 1`
 * vertices. Returns an empty list for empty input and the single point unchanged for one point.
 */
internal fun stepVertices(points: List<Offset>): List<Offset> {
    if (points.isEmpty()) {
        return emptyList()
    }
    val vertices = ArrayList<Offset>(points.size * 2 - 1)
    vertices.add(points.first())
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        vertices.add(stepCorner(start = previous, end = current))
        vertices.add(current)
    }
    return vertices
}

/**
 * Builds the cubic hops a [LineInterpolation.SMOOTH] outline traces through [points]. Fewer than
 * two points describe no hop at all, so the result is empty and the outline degenerates to the
 * [LineInterpolation.LINEAR] one.
 */
internal fun smoothSegments(points: List<Offset>): List<CubicSegment> {
    if (points.size < 2) {
        return emptyList()
    }
    return (0 until points.size - 1).map { index ->
        val current = points[index]
        val next = points[index + 1]
        val deltaX = next.x - current.x
        CubicSegment(
            control1 = Offset(x = current.x + deltaX / CONTROL_POINT_DIVISOR, y = current.y),
            control2 = Offset(x = current.x + CONTROL_POINT_MULTIPLIER * deltaX / CONTROL_POINT_DIVISOR, y = next.y),
            end = next,
        )
    }
}

/**
 * Appends every vertex of [vertices] after the first to this path using [interpolation]. The path
 * is expected to already sit on `vertices.first()`.
 */
private fun Path.appendInterpolation(
    vertices: List<Offset>,
    interpolation: LineInterpolation,
) {
    when (interpolation) {
        LineInterpolation.SMOOTH ->
            smoothSegments(vertices).fastForEach { segment ->
                cubicTo(
                    x1 = segment.control1.x,
                    y1 = segment.control1.y,
                    x2 = segment.control2.x,
                    y2 = segment.control2.y,
                    x3 = segment.end.x,
                    y3 = segment.end.y,
                )
            }

        LineInterpolation.STEP ->
            stepVertices(vertices).drop(1).fastForEach { vertex ->
                lineTo(x = vertex.x, y = vertex.y)
            }

        LineInterpolation.LINEAR ->
            for (index in 1 until vertices.size) {
                lineTo(x = vertices[index].x, y = vertices[index].y)
            }
    }
}

/**
 * The stroke outline through [points] drawn with [interpolation]. When [anchor] is non-null the
 * outline starts there and reaches the first point through the same interpolation, which is how the
 * multi-series charts pin their lines to the chart's bottom-left corner.
 */
internal fun interpolatedLinePath(
    points: List<Offset>,
    interpolation: LineInterpolation,
    anchor: Offset? = null,
): Path {
    if (points.isEmpty()) {
        return Path()
    }
    val vertices = if (anchor != null) listOf(anchor) + points else points
    return Path().apply {
        moveTo(x = vertices.first().x, y = vertices.first().y)
        appendInterpolation(vertices = vertices, interpolation = interpolation)
    }
}

/**
 * The filled region between [baselineY] and the very outline [interpolatedLinePath] strokes for the
 * same [points], [interpolation] and [anchor], so a fill never disagrees with the line above it.
 * Without an [anchor] the region starts on the baseline directly below the first point.
 */
internal fun interpolatedAreaPath(
    points: List<Offset>,
    baselineY: Float,
    interpolation: LineInterpolation,
    anchor: Offset? = null,
): Path {
    if (points.isEmpty()) {
        return Path()
    }
    val start = anchor ?: Offset(x = points.first().x, y = baselineY)
    return Path().apply {
        moveTo(x = start.x, y = start.y)
        appendInterpolation(vertices = listOf(start) + points, interpolation = interpolation)
        lineTo(x = points.last().x, y = baselineY)
        close()
    }
}
