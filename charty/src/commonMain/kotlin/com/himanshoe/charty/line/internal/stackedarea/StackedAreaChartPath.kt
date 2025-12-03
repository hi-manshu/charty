package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * Create an area path with smooth curves
 */
internal fun createSmoothAreaPath(
    cumulativePositions: List<Offset>,
    startX: Float,
    baselineY: Float,
): Path {
    return Path().apply {
        if (cumulativePositions.isEmpty()) return@apply

        val firstPoint = cumulativePositions[0]
        val control1X = startX + (firstPoint.x - startX) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
        val control2X = startX + StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
            (firstPoint.x - startX) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
        val control2Y = firstPoint.y

        moveTo(startX, baselineY)
        cubicTo(control1X, baselineY, control2X, control2Y, firstPoint.x, firstPoint.y)

        for (i in 0 until cumulativePositions.size - 1) {
            val current = cumulativePositions[i]
            val next = cumulativePositions[i + 1]
            val controlPoint1X = current.x + (next.x - current.x) /
                StackedAreaChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
            val controlPoint1Y = current.y
            val controlPoint2X = current.x + StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
                (next.x - current.x) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
            val controlPoint2Y = next.y

            cubicTo(
                controlPoint1X,
                controlPoint1Y,
                controlPoint2X,
                controlPoint2Y,
                next.x,
                next.y,
            )
        }

        lineTo(cumulativePositions.last().x, baselineY)
        lineTo(startX, baselineY)
        close()
    }
}

/**
 * Create an area path with straight lines
 */
internal fun createStraightAreaPath(
    cumulativePositions: List<Offset>,
    startX: Float,
    baselineY: Float,
): Path {
    return Path().apply {
        if (cumulativePositions.isEmpty()) return@apply

        val firstPoint = cumulativePositions[0]
        moveTo(startX, baselineY)
        lineTo(firstPoint.x, firstPoint.y)

        for (i in 1 until cumulativePositions.size) {
            lineTo(cumulativePositions[i].x, cumulativePositions[i].y)
        }

        lineTo(cumulativePositions.last().x, baselineY)
        lineTo(startX, baselineY)
        close()
    }
}

/**
 * Create a line path with smooth curves for the top border
 */
internal fun createSmoothLinePath(
    cumulativePositions: List<Offset>,
    startX: Float,
    baselineY: Float,
): Path {
    return Path().apply {
        if (cumulativePositions.isEmpty()) return@apply

        val firstPoint = cumulativePositions[0]
        val control1X = startX + (firstPoint.x - startX) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
        val control2X = startX + StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
            (firstPoint.x - startX) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
        val control2Y = firstPoint.y

        moveTo(startX, baselineY)
        cubicTo(control1X, baselineY, control2X, control2Y, firstPoint.x, firstPoint.y)

        for (i in 0 until cumulativePositions.size - 1) {
            val current = cumulativePositions[i]
            val next = cumulativePositions[i + 1]
            val controlPoint1X = current.x + (next.x - current.x) /
                StackedAreaChartConstants.BEZIER_CONTROL_POINT_1_DIVISOR
            val controlPoint1Y = current.y
            val controlPoint2X = current.x + StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER *
                (next.x - current.x) / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_DIVISOR
            val controlPoint2Y = next.y

            cubicTo(
                controlPoint1X,
                controlPoint1Y,
                controlPoint2X,
                controlPoint2Y,
                next.x,
                next.y,
            )
        }
    }
}

/**
 * Create a line path with straight lines for the top border
 */
internal fun createStraightLinePath(
    cumulativePositions: List<Offset>,
    startX: Float,
    baselineY: Float,
): Path {
    return Path().apply {
        if (cumulativePositions.isEmpty()) return@apply

        val firstPoint = cumulativePositions[0]
        moveTo(startX, baselineY)
        lineTo(firstPoint.x, firstPoint.y)

        for (i in 1 until cumulativePositions.size) {
            lineTo(cumulativePositions[i].x, cumulativePositions[i].y)
        }
    }
}

