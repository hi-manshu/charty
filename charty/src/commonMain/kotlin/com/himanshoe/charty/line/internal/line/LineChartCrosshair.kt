package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private val CROSSHAIR_DASH_INTERVALS = floatArrayOf(8f, 4f)
private val CROSSHAIR_DASH_EFFECT = PathEffect.dashPathEffect(CROSSHAIR_DASH_INTERVALS)
private const val DOT_OUTER_PADDING = 2f

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
    val dashEffect = CROSSHAIR_DASH_EFFECT

    drawLine(
        brush = Brush.verticalGradient(config.verticalLineColor.value),
        start = Offset(state.x, chartContext.top),
        end = Offset(state.x, chartContext.bottom),
        strokeWidth = config.lineWidth,
        pathEffect = dashEffect,
    )

    if (config.showHorizontalLine) {
        drawLine(
            brush = Brush.horizontalGradient(config.horizontalLineColor.value),
            start = Offset(chartContext.left, state.y),
            end = Offset(chartContext.right, state.y),
            strokeWidth = config.lineWidth,
            pathEffect = dashEffect,
        )
    }

    drawCircle(
        color = Color.White,
        radius = config.dotRadius + DOT_OUTER_PADDING,
        center = Offset(state.x, state.y),
    )
    drawCircle(
        brush = Brush.linearGradient(chartColor.value),
        radius = config.dotRadius,
        center = Offset(state.x, state.y),
    )

    if (config.showLabel) {
        val tooltipState =
            TooltipState(
                content = state.label,
                x = state.x - config.dotRadius,
                y = state.y,
                barWidth = config.dotRadius * 2f,
                position = TooltipPosition.ABOVE,
            )
        drawTooltip(
            tooltipState = tooltipState,
            config = config.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
