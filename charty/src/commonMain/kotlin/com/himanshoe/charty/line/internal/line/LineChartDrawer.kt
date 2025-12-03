package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.line.config.LineChartConfig

/**
 * Draw a smooth curve line through points using cubic bezier curves
 */
internal fun DrawScope.drawSmoothLine(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
) {
    val path = Path()

    if (pointPositions.isNotEmpty()) {
        path.moveTo(pointPositions[0].x, pointPositions[0].y)

        for (i in 0 until pointPositions.size - 1) {
            val current = pointPositions[i]
            val next = pointPositions[i + 1]

            val controlPoint1X = current.x + (next.x - current.x) / LineChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
            val controlPoint1Y = current.y
            val controlPoint2X = current.x + LineChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
                (next.x - current.x) / LineChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
            val controlPoint2Y = next.y

            path.cubicTo(
                x1 = controlPoint1X,
                y1 = controlPoint1Y,
                x2 = controlPoint2X,
                y2 = controlPoint2Y,
                x3 = next.x,
                y3 = next.y,
            )
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(color.value),
            style = Stroke(
                width = lineConfig.lineWidth,
                cap = lineConfig.strokeCap,
            ),
            alpha = animationProgress,
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
) {
    val segmentsToDraw = ((pointPositions.size - 1) * animationProgress).toInt()
    val segmentProgress = ((pointPositions.size - 1) * animationProgress) - segmentsToDraw

    for (i in 0 until segmentsToDraw) {
        drawLine(
            brush = Brush.linearGradient(color.value),
            start = pointPositions[i],
            end = pointPositions[i + 1],
            strokeWidth = lineConfig.lineWidth,
            cap = lineConfig.strokeCap,
        )
    }

    if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
        val start = pointPositions[segmentsToDraw]
        val end = pointPositions[segmentsToDraw + 1]
        val partialEnd = Offset(
            x = start.x + (end.x - start.x) * segmentProgress,
            y = start.y + (end.y - start.y) * segmentProgress,
        )
        drawLine(
            brush = Brush.linearGradient(color.value),
            start = start,
            end = partialEnd,
            strokeWidth = lineConfig.lineWidth,
            cap = lineConfig.strokeCap,
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
        val pointProgress = index.toFloat() / (pointPositions.size - 1)
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

