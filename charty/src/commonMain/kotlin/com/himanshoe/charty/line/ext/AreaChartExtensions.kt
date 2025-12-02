package com.himanshoe.charty.line.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import com.himanshoe.charty.color.ChartyColor
import kotlin.math.pow
import kotlin.math.sqrt

private const val DEFAULT_FILL_ALPHA = 0.3f
private const val CONTROL_POINT_DIVISOR = 3f
private const val CONTROL_POINT_MULTIPLIER = 2f

/**
 * Calculate distance between two points
 */
internal fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx.pow(2) + dy.pow(2))
}

/**
 * Create area path with optional smooth curve
 */
internal fun createAreaPath(pointPositions: List<Offset>, baselineY: Float, smoothCurve: Boolean): Path {
    return Path().apply {
        moveTo(pointPositions[0].x, baselineY)
        lineTo(pointPositions[0].x, pointPositions[0].y)

        if (smoothCurve) {
            drawSmoothCurve(pointPositions)
        } else {
            for (i in 1 until pointPositions.size) {
                lineTo(pointPositions[i].x, pointPositions[i].y)
            }
        }

        lineTo(pointPositions.last().x, baselineY)
        close()
    }
}

/**
 * Create line path with optional smooth curve
 */
internal fun createLinePath(pointPositions: List<Offset>, smoothCurve: Boolean): Path {
    return Path().apply {
        moveTo(pointPositions[0].x, pointPositions[0].y)

        if (smoothCurve) {
            drawSmoothCurve(pointPositions)
        } else {
            for (i in 1 until pointPositions.size) {
                lineTo(pointPositions[i].x, pointPositions[i].y)
            }
        }
    }
}

/**
 * Draw smooth curve through points using cubic bezier curves
 */
private fun Path.drawSmoothCurve(pointPositions: List<Offset>) {
    for (i in 0 until pointPositions.size - 1) {
        val current = pointPositions[i]
        val next = pointPositions[i + 1]
        val controlPoint1X = current.x + (next.x - current.x) / CONTROL_POINT_DIVISOR
        val controlPoint1Y = current.y
        val controlPoint2X = current.x + CONTROL_POINT_MULTIPLIER * (next.x - current.x) / CONTROL_POINT_DIVISOR
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

/**
 * Create brush for area fill with gradient effect
 */
internal fun createAreaBrush(
    color: ChartyColor,
    fillAlpha: Float,
    chartTop: Float,
    chartBottom: Float,
): Brush {
    return when (color) {
        is ChartyColor.Solid ->
            Brush.verticalGradient(
                colors = listOf(
                    color.color.copy(alpha = fillAlpha),
                    color.color.copy(alpha = fillAlpha * DEFAULT_FILL_ALPHA),
                ),
                startY = chartTop,
                endY = chartBottom,
            )
        is ChartyColor.Gradient ->
            Brush.verticalGradient(
                colors = color.colors.map { it.copy(alpha = it.alpha * fillAlpha) },
                startY = chartTop,
                endY = chartBottom,
            )
    }
}

/**
 * Create brush for line
 */
internal fun createLineBrush(color: ChartyColor): Brush {
    return when (color) {
        is ChartyColor.Solid -> Brush.linearGradient(listOf(color.color, color.color))
        is ChartyColor.Gradient -> Brush.linearGradient(color.colors)
    }
}

