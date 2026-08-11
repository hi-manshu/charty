package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.data.LineGroup
import kotlin.math.abs

private val MULTILINE_CROSSHAIR_DASH = floatArrayOf(8f, 4f)
private val MULTILINE_CROSSHAIR_DASH_EFFECT = PathEffect.dashPathEffect(MULTILINE_CROSSHAIR_DASH)
private const val MULTILINE_DOT_OUTER_PADDING = 2f

/**
 * Draws the crosshair overlay for a multiline chart.
 *
 * Renders a dashed vertical line at the snapped x-position, a highlight dot at each
 * series' y-coordinate, an optional horizontal line at the first series' y, and a
 * combined label bubble showing all series values.
 *
 * @param state Current crosshair position and combined label.
 * @param config Visual configuration for the crosshair.
 * @param chartContext The chart's coordinate context.
 * @param dataList The visible dataset — used to find the snapped index and per-series values.
 * @param colorList Per-series colours cycling as `colorList[seriesIndex % colorList.size]`.
 * @param textMeasurer Required for measuring and drawing the label text.
 * @param drawLabel When `false`, the value label bubble is suppressed (lines and dots are still
 *   drawn), letting a caller-supplied `crosshairContent` composable render the label instead.
 */
@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawMultilineChartCrosshair(
    state: CrosshairState,
    config: ChartCrosshairConfig,
    chartContext: ChartContext,
    dataList: List<LineGroup>,
    colorList: List<Color>,
    textMeasurer: TextMeasurer,
    drawLabel: Boolean = true,
) {
    if (dataList.isEmpty() || colorList.isEmpty()) return
    val dashEffect = MULTILINE_CROSSHAIR_DASH_EFFECT

    val snappedIndex =
        dataList.indices.minByOrNull { idx ->
            abs(chartContext.calculateCenteredXPosition(idx, dataList.size) - state.x)
        } ?: return

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

    dataList[snappedIndex].values.fastForEachIndexed { seriesIndex, value ->
        val seriesY = chartContext.convertValueToYPosition(value)
        val seriesColor = colorList[seriesIndex % colorList.size]
        drawCircle(
            color = Color.White,
            radius = config.dotRadius + MULTILINE_DOT_OUTER_PADDING,
            center = Offset(state.x, seriesY),
        )
        drawCircle(
            color = seriesColor,
            radius = config.dotRadius,
            center = Offset(state.x, seriesY),
        )
    }

    if (config.showLabel && drawLabel) {
        drawTooltip(
            tooltipState =
                TooltipState(
                    content = state.label,
                    x = state.x - config.dotRadius,
                    y = state.y,
                    barWidth = config.dotRadius * 2f,
                    position = TooltipPosition.ABOVE,
                ),
            config = config.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
