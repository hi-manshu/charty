package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.internal.path.interpolatedLinePath
import com.himanshoe.charty.line.internal.path.stepCorner

/**
 * Draw a smooth curve line through points using cubic bezier curves
 */
internal fun DrawScope.drawSmoothLine(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    if (pointPositions.isEmpty()) {
        return
    }

    val path = interpolatedLinePath(points = pointPositions, interpolation = LineInterpolation.SMOOTH)

    val startX = pointPositions.first().x
    val endX = pointPositions.last().x
    val clipRight = startX + (endX - startX) * animationProgress

    clipRect(right = clipRight) {
        drawPath(
            path = path,
            brush = Brush.linearGradient(color.value),
            style =
                Stroke(
                    width = lineConfig.lineWidth,
                    cap = lineConfig.strokeCap,
                ),
        )
    }
}

/**
 * Draw straight line segments with animation
 */
internal fun DrawScope.drawStraightLineSegments(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) = drawAnimatedLineSegments(
    pointPositions = pointPositions,
    color = color,
    lineWidth = lineConfig.lineWidth,
    strokeCap = lineConfig.strokeCap,
    animationProgress = animationProgress,
)

/**
 * Strokes [pointPositions] segment by segment, revealing the line as [animationProgress] runs.
 *
 * The final segment is drawn partially, so the line grows out of the last vertex rather than snapping
 * whole into place a segment at a time. Takes the stroke width and cap directly rather than a config,
 * because the combo chart draws the same line from a different one — it used to carry a verbatim copy
 * of this function, which meant any change to how a line animates had to be made twice.
 */
internal fun DrawScope.drawAnimatedLineSegments(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineWidth: Float,
    strokeCap: StrokeCap,
    animationProgress: Float,
) {
    val segmentsToDraw = ((pointPositions.size - 1) * animationProgress).toInt()
    val segmentProgress = ((pointPositions.size - 1) * animationProgress) - segmentsToDraw
    val brush = Brush.linearGradient(color.value)

    for (i in 0 until segmentsToDraw) {
        drawLine(
            brush = brush,
            start = pointPositions[i],
            end = pointPositions[i + 1],
            strokeWidth = lineWidth,
            cap = strokeCap,
        )
    }

    if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
        val start = pointPositions[segmentsToDraw]
        val end = pointPositions[segmentsToDraw + 1]
        val partialEnd =
            Offset(
                x = start.x + (end.x - start.x) * segmentProgress,
                y = start.y + (end.y - start.y) * segmentProgress,
            )
        drawLine(
            brush = brush,
            start = start,
            end = partialEnd,
            strokeWidth = lineWidth,
            cap = strokeCap,
        )
    }
}

/**
 * Draw points on the line with animation
 */
internal fun DrawScope.drawAnimatedPoints(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1).coerceAtLeast(1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = Brush.linearGradient(color.value),
                radius = lineConfig.pointRadius,
                center = position,
                alpha = lineConfig.pointAlpha,
            )
        }
    }
}

/**
 * Draws the line as horizontal-then-vertical steps: from each point the value holds along x until
 * the next point's x, then jumps to the next value. Animates the same way as
 * [drawStraightLineSegments] — full steps up to the progress point, then a partial vertical jump.
 */
internal fun DrawScope.drawStepLineSegments(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    if (pointPositions.isEmpty()) {
        return
    }
    val brush = Brush.linearGradient(color.value)
    val segmentsToDraw = ((pointPositions.size - 1) * animationProgress).toInt()
    val segmentProgress = ((pointPositions.size - 1) * animationProgress) - segmentsToDraw

    for (i in 0 until segmentsToDraw) {
        drawStep(
            brush = brush,
            start = pointPositions[i],
            end = pointPositions[i + 1],
            lineConfig = lineConfig,
            verticalFraction = 1f,
        )
    }
    if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0f) {
        drawStep(
            brush = brush,
            start = pointPositions[segmentsToDraw],
            end = pointPositions[segmentsToDraw + 1],
            lineConfig = lineConfig,
            verticalFraction = segmentProgress,
        )
    }
}

private fun DrawScope.drawStep(
    brush: Brush,
    start: Offset,
    end: Offset,
    lineConfig: LineChartConfig,
    verticalFraction: Float,
) {
    val corner = stepCorner(start = start, end = end)
    drawLine(brush = brush, start = start, end = corner, strokeWidth = lineConfig.lineWidth, cap = lineConfig.strokeCap)
    val verticalEnd = Offset(x = end.x, y = start.y + (end.y - start.y) * verticalFraction)
    drawLine(
        brush = brush,
        start = corner,
        end = verticalEnd,
        strokeWidth = lineConfig.lineWidth,
        cap = lineConfig.strokeCap,
    )
}
