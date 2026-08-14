package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.ui.graphics.Brush
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.AxisConfig

private const val DEFAULT_AXIS_STEPS = 6

/**
 * Creates axis configuration for lollipop chart.
 */
internal fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = true,
    )

/**
 * Creates brush for the lollipop stem based on ChartyColor.
 */
internal fun createStemBrush(
    chartyColor: ChartyColor,
    baselineY: Float,
    barValueY: Float,
): Brush =
    when (chartyColor) {
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

/**
 * Extracts circle color from ChartyColor based on index.
 */
internal fun getCircleColor(
    circleChartyColor: ChartyColor,
    index: Int,
): androidx.compose.ui.graphics.Color =
    when (circleChartyColor) {
        is ChartyColor.Solid -> circleChartyColor.color
        is ChartyColor.Gradient -> circleChartyColor.colors[index % circleChartyColor.colors.size]
    }
