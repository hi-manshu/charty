package com.himanshoe.charty.line.ext

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_FILL_ALPHA = 0.3f

/**
 * Create brush for area fill with gradient effect
 */
internal fun createAreaBrush(
    color: ChartyColor,
    fillAlpha: Float,
    chartTop: Float,
    chartBottom: Float,
): Brush =
    when (color) {
        is ChartyColor.Solid ->
            Brush.verticalGradient(
                colors =
                    listOf(
                        color.color.copy(alpha = fillAlpha),
                        color.color.copy(alpha = fillAlpha * DEFAULT_FILL_ALPHA),
                    ),
                startY = chartTop,
                endY = chartBottom,
            )

        is ChartyColor.Gradient ->
            Brush.verticalGradient(
                colors = color.colors.fastMap { it.copy(alpha = it.alpha * fillAlpha) },
                startY = chartTop,
                endY = chartBottom,
            )
    }

/**
 * Create brush for line
 */
internal fun createLineBrush(color: ChartyColor): Brush =
    when (color) {
        is ChartyColor.Solid -> Brush.linearGradient(listOf(color.color, color.color))
        is ChartyColor.Gradient -> Brush.linearGradient(color.colors)
    }
