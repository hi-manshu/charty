package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.AxisConfig
import kotlin.math.pow
import kotlin.math.sqrt

private const val DEFAULT_AXIS_STEPS = 6

/**
 * Creates axis configuration for lollipop chart.
 */
internal fun createAxisConfig(minValue: Float, maxValue: Float): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = true,
    )
}

/**
 * Calculates Euclidean distance between two points.
 */
internal fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx.pow(2) + dy.pow(2))
}

/**
 * Creates brush for the lollipop stem based on ChartyColor.
 */
internal fun createStemBrush(
    chartyColor: ChartyColor,
    baselineY: Float,
    barValueY: Float,
): Brush {
    return when (chartyColor) {
        is ChartyColor.Solid ->
            Brush.verticalGradient(
                colors = listOf(chartyColor.color, chartyColor.color),
                startY = baselineY,
                endY = barValueY,
            )

        is ChartyColor.Gradient ->
            Brush.verticalGradient(
                colors = chartyColor.colors,
                startY = baselineY,
                endY = barValueY,
            )
    }
}

/**
 * Extracts circle color from ChartyColor based on index.
 */
internal fun getCircleColor(circleChartyColor: ChartyColor, index: Int): androidx.compose.ui.graphics.Color {
    return when (circleChartyColor) {
        is ChartyColor.Solid -> circleChartyColor.color
        is ChartyColor.Gradient -> circleChartyColor.colors[index % circleChartyColor.colors.size]
    }
}

