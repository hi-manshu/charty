package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastFirstOrNull
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.draw.drawHighlightedPoint
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Draw the tooltip with highlight effects
 * @param tooltipConfig Styling for the tooltip bubble, already resolved against the theme.
 */
internal fun DrawScope.drawLineChartTooltip(
    tooltipState: TooltipState,
    pointBounds: List<Pair<Offset, LineData>>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    drawBubble: Boolean = true,
    tooltipConfig: TooltipConfig,
) {
    val clickedPosition =
        pointBounds
            .fastFirstOrNull { (_, data) ->
                lineConfig.tooltipFormatter(data) == tooltipState.content
            }?.first

    clickedPosition?.let { position ->
        drawLine(
            color = Color.Black.copy(alpha = LineChartConstants.TOOLTIP_LINE_ALPHA),
            start = Offset(position.x, chartContext.top),
            end = Offset(position.x, chartContext.bottom),
            strokeWidth = LineChartConstants.TOOLTIP_LINE_WIDTH,
        )

        drawHighlightedPoint(
            center = position,
            pointRadius = lineConfig.pointRadius,
            colorBrush = Brush.linearGradient(color.value),
            outerRadiusAddition = LineChartConstants.POINT_HIGHLIGHT_RADIUS_ADDITION,
            innerRadiusAddition = LineChartConstants.POINT_HIGHLIGHT_INNER_RADIUS_ADDITION,
        )
    }

    if (drawBubble) {
        drawTooltip(
            tooltipState = tooltipState,
            config = tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
