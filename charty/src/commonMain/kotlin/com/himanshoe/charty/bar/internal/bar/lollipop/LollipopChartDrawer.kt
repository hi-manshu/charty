package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private const val CIRCLE_HIGHLIGHT_OUTER_PADDING = 3f
private const val CIRCLE_HIGHLIGHT_INNER_PADDING = 2f
private const val HIGHLIGHT_LINE_WIDTH = 1.5f
private const val HIGHLIGHT_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_CIRCLE_ALPHA = 0.3f

/**
 * Draws all lollipops on the chart.
 */
internal fun DrawScope.drawLollipops(
    dataList: List<BarData>,
    chartContext: ChartContext,
    config: LollipopBarChartConfig,
    animationProgress: Float,
    colors: ChartyColor,
    onBarClick: ((BarData) -> Unit)?,
    lollipopBounds: MutableList<Pair<Offset, BarData>>,
) {
    val baselineY = chartContext.bottom

    dataList.fastForEachIndexed { index, bar ->
        val barLeft = chartContext.calculateBarLeftPosition(index, dataList.size, config.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(dataList.size, config.barWidthFraction)
        val centerX = barLeft + barWidth / 2f

        val barValueY = chartContext.convertValueToYPosition(bar.value)
        val animatedTopY = baselineY - (baselineY - barValueY) * animationProgress

        if (onBarClick != null) {
            lollipopBounds.add(Offset(centerX, animatedTopY) to bar)
        }

        val chartyColor = bar.color ?: colors
        val circleChartyColor = config.circleColor ?: chartyColor

        val stemBrush = createStemBrush(chartyColor, baselineY, barValueY)
        val circleColor = getCircleColor(circleChartyColor, index)

        // Draw stem
        drawLine(
            brush = stemBrush,
            start = Offset(centerX, baselineY),
            end = Offset(centerX, animatedTopY),
            strokeWidth = config.stemThickness,
        )

        // Draw circle
        drawLollipopCircle(
            color = circleColor,
            center = Offset(centerX, animatedTopY),
            radius = config.circleRadius,
            strokeWidth = config.circleStrokeWidth,
        )
    }
}

/**
 * Draws a single lollipop circle (filled or stroked).
 */
private fun DrawScope.drawLollipopCircle(
    color: Color,
    center: Offset,
    radius: Float,
    strokeWidth: Float,
) {
    if (strokeWidth > 0f) {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth),
        )
    } else {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
        )
    }
}

/**
 * Draws tooltip highlight (vertical line and circles) if tooltip is active.
 */
internal fun DrawScope.drawTooltipHighlightIfNeeded(
    tooltipState: TooltipState?,
    config: LollipopBarChartConfig,
    chartContext: ChartContext,
) {
    tooltipState?.let { state ->
        val clickedPosition = Offset(
            state.x + config.circleRadius,
            state.y,
        )

        // Draw vertical highlight line
        drawLine(
            color = Color.Black.copy(alpha = HIGHLIGHT_LINE_ALPHA),
            start = Offset(clickedPosition.x, chartContext.top),
            end = Offset(clickedPosition.x, chartContext.bottom),
            strokeWidth = HIGHLIGHT_LINE_WIDTH,
        )

        // Draw highlight circles
        drawCircle(
            color = Color.White,
            radius = config.circleRadius + CIRCLE_HIGHLIGHT_OUTER_PADDING,
            center = clickedPosition,
        )
        drawCircle(
            color = Color.Black.copy(alpha = HIGHLIGHT_CIRCLE_ALPHA),
            radius = config.circleRadius + CIRCLE_HIGHLIGHT_INNER_PADDING,
            center = clickedPosition,
        )
    }
}

/**
 * Draws tooltip if active.
 */
@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    config: LollipopBarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = config.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

