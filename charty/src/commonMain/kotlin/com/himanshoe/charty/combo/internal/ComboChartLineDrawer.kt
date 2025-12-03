package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig

/**
 * Draw smooth curve line through points using cubic bezier curves
 */
internal fun DrawScope.drawSmoothCurveLine(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    val path = Path()

    if (pointPositions.isNotEmpty()) {
        path.moveTo(pointPositions[0].x, pointPositions[0].y)

        for (i in 0 until pointPositions.size - 1) {
            val current = pointPositions[i]
            val next = pointPositions[i + 1]

            val controlPoint1X = current.x + (next.x - current.x) / ComboChartConstants.THREE
            val controlPoint1Y = current.y
            val controlPoint2X = current.x + ComboChartConstants.TWO * (next.x - current.x) / ComboChartConstants.THREE
            val controlPoint2Y = next.y

            path.cubicTo(
                controlPoint1X,
                controlPoint1Y,
                controlPoint2X,
                controlPoint2Y,
                next.x,
                next.y,
            )
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(lineColor.value),
            style = Stroke(
                width = comboConfig.lineWidth,
                cap = comboConfig.strokeCap,
            ),
            alpha = animationProgress,
        )
    }
}

/**
 * Draw straight line segments with animation
 */
internal fun DrawScope.drawStraightLine(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    val segmentsToDraw = ((pointPositions.size - 1) * animationProgress).toInt()
    val segmentProgress = ((pointPositions.size - 1) * animationProgress) - segmentsToDraw

    for (i in 0 until segmentsToDraw) {
        drawLine(
            brush = Brush.linearGradient(lineColor.value),
            start = pointPositions[i],
            end = pointPositions[i + 1],
            strokeWidth = comboConfig.lineWidth,
            cap = comboConfig.strokeCap,
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
            brush = Brush.linearGradient(lineColor.value),
            start = start,
            end = partialEnd,
            strokeWidth = comboConfig.lineWidth,
            cap = comboConfig.strokeCap,
        )
    }
}

/**
 * Draw points on the line with animation
 */
internal fun DrawScope.drawLinePoints(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = Brush.linearGradient(lineColor.value),
                radius = comboConfig.pointRadius,
                center = position,
                alpha = comboConfig.pointAlpha,
            )
        }
    }
}

