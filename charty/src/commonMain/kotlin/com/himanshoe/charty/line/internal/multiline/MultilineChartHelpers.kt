package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

/**
 * Draw a smooth curve line through points using cubic bezier curves
 */
internal fun Path.drawSmoothMultiline(
    pointPositions: List<Offset>,
    startX: Float,
    startY: Float,
) {
    if (pointPositions.isEmpty()) return

    val firstPoint = pointPositions[0]
    val control1X = startX + (firstPoint.x - startX) / MultilineChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
    val control2X = startX + MultilineChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
        (firstPoint.x - startX) / MultilineChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
    val control2Y = firstPoint.y

    moveTo(startX, startY)
    cubicTo(control1X, startY, control2X, control2Y, firstPoint.x, firstPoint.y)

    for (i in 0 until pointPositions.size - 1) {
        val current = pointPositions[i]
        val next = pointPositions[i + 1]

        val controlPoint1X = current.x + (next.x - current.x) / MultilineChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
        val controlPoint1Y = current.y
        val controlPoint2X = current.x + MultilineChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
            (next.x - current.x) / MultilineChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
        val controlPoint2Y = next.y

        cubicTo(
            x1 = controlPoint1X,
            y1 = controlPoint1Y,
            x2 = controlPoint2X,
            y2 = controlPoint2Y,
            x3 = next.x,
            y3 = next.y,
        )
    }
}

/**
 * Draw straight line segments through points
 */
internal fun Path.drawStraightMultiline(
    pointPositions: List<Offset>,
    startX: Float,
    startY: Float,
) {
    if (pointPositions.isEmpty()) return

    val firstPoint = pointPositions[0]
    moveTo(startX, startY)
    lineTo(firstPoint.x, firstPoint.y)

    for (i in 1 until pointPositions.size) {
        lineTo(pointPositions[i].x, pointPositions[i].y)
    }
}

/**
 * Calculate point positions for a series
 */
internal fun ChartContext.calculateSeriesPointPositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
): List<Offset> {
    return dataList.fastMapIndexed { index, group ->
        val value = group.values.getOrNull(seriesIndex) ?: 0f
        Offset(
            x = calculateCenteredXPosition(index, dataList.size),
            y = convertValueToYPosition(value),
        )
    }
}

