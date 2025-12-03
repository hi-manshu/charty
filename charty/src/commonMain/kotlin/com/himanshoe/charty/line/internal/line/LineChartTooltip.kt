package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Draw the tooltip with highlight effects
 */
internal fun DrawScope.drawLineChartTooltip(
    tooltipState: TooltipState,
    pointBounds: List<Pair<Offset, LineData>>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    val clickedPosition = pointBounds.find { (_, data) ->
        lineConfig.tooltipFormatter(data) == tooltipState.content
    }?.first

    clickedPosition?.let { position ->
        drawLine(
            color = Color.Black.copy(alpha = LineChartConstants.TOOLTIP_LINE_ALPHA),
            start = Offset(position.x, chartContext.top),
            end = Offset(position.x, chartContext.bottom),
            strokeWidth = LineChartConstants.TOOLTIP_LINE_WIDTH,
        )

        drawCircle(
            color = Color.White,
            radius = lineConfig.pointRadius + LineChartConstants.POINT_HIGHLIGHT_RADIUS_ADDITION,
            center = position,
        )
        drawCircle(
            brush = Brush.linearGradient(color.value),
            radius = lineConfig.pointRadius + LineChartConstants.POINT_HIGHLIGHT_INNER_RADIUS_ADDITION,
            center = position,
        )
    }

    drawTooltip(
        tooltipState = tooltipState,
        config = lineConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartWidth = chartContext.right,
        chartTop = chartContext.top,
        chartBottom = chartContext.bottom,
    )
}

