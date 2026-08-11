package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.ScrollEdgeFadeConfig

/**
 * Draws the leading/trailing edge scrims for a scrollable chart. [fadeLeft]/[fadeRight] should be
 * `true` only when data is scrolled past that edge (e.g. from `!viewPortState.isAtStart` /
 * `!viewPortState.isAtEnd`), so no scrim shows when the full dataset is in view.
 *
 * Call after the chart's data so the scrims sit on top.
 *
 * @param chartContext The chart's drawing context, providing the plot bounds.
 * @param config The scrim styling.
 * @param fadeLeft Whether to draw the left scrim.
 * @param fadeRight Whether to draw the right scrim.
 */
fun DrawScope.drawScrollEdgeFades(
    chartContext: ChartContext,
    config: ScrollEdgeFadeConfig,
    fadeLeft: Boolean,
    fadeRight: Boolean,
) {
    if (config.width <= 0f) {
        return
    }
    val edgeColor = config.color.copy(alpha = config.alpha)
    val transparent = config.color.copy(alpha = 0f)
    val width = config.width.coerceAtMost(chartContext.width)
    if (fadeLeft) {
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colors = listOf(edgeColor, transparent),
                    startX = chartContext.left,
                    endX = chartContext.left + width,
                ),
            topLeft = Offset(x = chartContext.left, y = chartContext.top),
            size = Size(width = width, height = chartContext.height),
        )
    }
    if (fadeRight) {
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colors = listOf(transparent, edgeColor),
                    startX = chartContext.right - width,
                    endX = chartContext.right,
                ),
            topLeft = Offset(x = chartContext.right - width, y = chartContext.top),
            size = Size(width = width, height = chartContext.height),
        )
    }
}
