package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.drawCrosshairDot
import com.himanshoe.charty.common.gesture.drawCrosshairGuides
import com.himanshoe.charty.common.gesture.drawCrosshairValueLabel

/**
 * Draws the crosshair overlay for a line or area chart.
 *
 * Renders a dashed vertical line (and optional horizontal line) centred on the snapped
 * data point, a highlight circle at the point, and a value label above it.
 *
 * @param state Current crosshair position and label.
 * @param config Visual configuration for the crosshair.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Required for measuring and drawing the label text.
 * @param chartColor The chart's colour — used to fill the highlight dot.
 */
internal fun DrawScope.drawLineChartCrosshair(
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
