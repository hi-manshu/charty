package com.himanshoe.charty.common.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private val CROSSHAIR_DASH_INTERVALS = floatArrayOf(8f, 4f)
private const val DOT_OUTER_PADDING = 2f
private const val LABEL_SPAN_FACTOR = 2f

/**
 * The dashes every crosshair guide line is drawn with.
 *
 * The line, multiline and bar crosshairs each declared this — same intervals, same effect, three
 * names. A crosshair that dashes differently from the one on the chart beside it is a defect, so
 * there is nothing here for a caller to vary.
 */
internal val CROSSHAIR_DASH_EFFECT = PathEffect.dashPathEffect(CROSSHAIR_DASH_INTERVALS)

/**
 * Draws the crosshair's guide lines: the vertical one always, the horizontal one when the crosshair
 * is configured to show it.
 */
internal fun DrawScope.drawCrosshairGuides(
    state: CrosshairState,
    config: ChartCrosshairConfig,
    chartContext: ChartContext,
) {
    drawLine(
        brush = Brush.verticalGradient(config.verticalLineColor.value),
        start = Offset(x = state.x, y = chartContext.top),
        end = Offset(x = state.x, y = chartContext.bottom),
        strokeWidth = config.lineWidth,
        pathEffect = CROSSHAIR_DASH_EFFECT,
    )
    if (config.showHorizontalLine) {
        drawLine(
            brush = Brush.horizontalGradient(config.horizontalLineColor.value),
            start = Offset(x = chartContext.left, y = state.y),
            end = Offset(x = chartContext.right, y = state.y),
            strokeWidth = config.lineWidth,
            pathEffect = CROSSHAIR_DASH_EFFECT,
        )
    }
}

/**
 * Draws the highlight dot the crosshair puts on a data point: the series colour ringed in white, so
 * it stays visible against a line of its own colour.
 */
internal fun DrawScope.drawCrosshairDot(
    center: Offset,
    config: ChartCrosshairConfig,
    fill: Brush,
) {
    drawCircle(
        color = Color.White,
        radius = config.dotRadius + DOT_OUTER_PADDING,
        center = center,
    )
    drawCircle(
        brush = fill,
        radius = config.dotRadius,
        center = center,
    )
}

/**
 * Draws the crosshair's value label above the highlight dot, sized to span it.
 *
 * Callers check [ChartCrosshairConfig.showLabel] before calling; the line family draws its label as a
 * Composable overlay instead, so this is not a decision that can be made here.
 */
internal fun DrawScope.drawCrosshairValueLabel(
    state: CrosshairState,
    config: ChartCrosshairConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    drawTooltip(
        tooltipState =
            TooltipState(
                content = state.label,
                x = state.x - config.dotRadius,
                y = state.y,
                barWidth = config.dotRadius * LABEL_SPAN_FACTOR,
                position = TooltipPosition.ABOVE,
            ),
        config = config.tooltipConfig,
        textMeasurer = textMeasurer,
        chartWidth = chartContext.right,
        chartTop = chartContext.top,
        chartBottom = chartContext.bottom,
    )
}
