package com.himanshoe.charty.radar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.radar.config.RadarGridStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val FULL_CIRCLE_DEGREES = 360f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * Draw a circular grid line at the specified radius
 */
internal fun DrawScope.drawCircularGridLine(
    center: Offset,
    radius: Float,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
) {
    drawCircle(
        brush = Brush.linearGradient(gridLineColor.value),
        radius = radius,
        center = center,
        style = Stroke(width = gridLineWidth),
    )
}

/**
 * Draw a polygonal grid line at the specified radius
 */
internal fun DrawScope.drawPolygonalGridLine(
    center: Offset,
    radius: Float,
    numberOfAxes: Int,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
    startAngle: Float,
) {
    val path = createPolygonPath(center, radius, numberOfAxes, startAngle)
    drawPath(
        path = path,
        brush = Brush.linearGradient(gridLineColor.value),
        style = Stroke(width = gridLineWidth),
    )
}

/**
 * Create a polygon path for the grid
 */
private fun createPolygonPath(
    center: Offset,
    radius: Float,
    numberOfAxes: Int,
    startAngle: Float,
): Path {
    val path = Path()
    for (i in 0 until numberOfAxes) {
        val angle = (startAngle + (FULL_CIRCLE_DEGREES * i / numberOfAxes)) * DEGREES_TO_RADIANS
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

/**
 * Draw a single grid level based on the grid style
 */
internal fun DrawScope.drawGridLevel(
    center: Offset,
    radius: Float,
    numberOfAxes: Int,
    gridStyle: RadarGridStyle,
    gridLineWidth: Float,
    gridLineColor: ChartyColor,
    startAngle: Float,
) {
    when (gridStyle) {
        RadarGridStyle.CIRCULAR -> {
            drawCircularGridLine(center, radius, gridLineWidth, gridLineColor)
        }

        RadarGridStyle.POLYGON -> {
            drawPolygonalGridLine(center, radius, numberOfAxes, gridLineWidth, gridLineColor, startAngle)
        }
    }
}

