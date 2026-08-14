package com.himanshoe.charty.common.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Draws the crosshair a chart snaps to a single data point: guide lines through it, a highlight
 * dot on it, and its value label above.
 *
 * Used by the line, area, stacked area, point, bubble, wavy and combo charts — every chart whose
 * crosshair resolves to one point rather than to a column of them, which is why this lives beside
 * the crosshair itself rather than in any one of their packages.
 *
 * @param state Current crosshair position and label.
 * @param config Visual configuration for the crosshair.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Required for measuring and drawing the label text.
 * @param chartColor The chart's colour — used to fill the highlight dot.
 */
internal fun DrawScope.drawPointCrosshair(
    state: CrosshairState,
    config: ChartCrosshairConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    chartColor: ChartyColor,
) {
    drawCrosshairGuides(state = state, config = config, chartContext = chartContext)
    drawCrosshairDot(
        center = Offset(x = state.x, y = state.y),
        config = config,
        fill = Brush.linearGradient(chartColor.value),
    )
    if (config.showLabel) {
        drawCrosshairValueLabel(
            state = state,
            config = config,
            chartContext = chartContext,
            textMeasurer = textMeasurer,
        )
    }
}
