package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Draw the bars on the chart
 */
internal fun DrawScope.drawBars(
    dataList: List<BarData>,
    chartContext: ChartContext,
    barConfig: BarChartConfig,
    baselineY: Float,
    animationProgress: Float,
    color: ChartyColor,
    onBarClick: ((BarData) -> Unit)?,
    barBounds: MutableList<Pair<Rect, BarData>>,
) {
    dataList.fastForEachIndexed { index, bar ->
        val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)
        val barValueY = chartContext.convertValueToYPosition(bar.value)
        val isNegative = bar.value < 0f

        val (barTop, barHeight) = if (isNegative) {
            baselineY to (barValueY - baselineY) * animationProgress
        } else {
            val animatedBarHeight = (baselineY - barValueY) * animationProgress
            baselineY - animatedBarHeight to animatedBarHeight
        }

        if (onBarClick != null) {
            barBounds.add(
                Rect(
                    left = barX,
                    top = barTop,
                    right = barX + barWidth,
                    bottom = barTop + barHeight,
                ) to bar,
            )
        }

        val barColor = bar.color ?: color
        val brush = with(chartContext) { barColor.toVerticalGradientBrush() }

        drawRoundedBar(
            brush = brush,
            x = barX,
            y = barTop,
            width = barWidth,
            height = barHeight,
            isNegative = isNegative,
            isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS,
            cornerRadius = barConfig.cornerRadius.value,
        )
    }
}

/**
 * Draw the reference line if configured
 */
@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawBarReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    drawReferenceLineIfNeeded(
        referenceLineConfig = barConfig.referenceLine,
        chartContext = chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = textMeasurer,
    )
}

/**
 * Draw the tooltip if the state is not null
 */
@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawBarTooltipIfNeeded(
    tooltipState: TooltipState?,
    barConfig: BarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    drawTooltipIfNeeded(
        tooltipState = tooltipState,
        tooltipConfig = barConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartContext = chartContext,
    )
}

/**
 * Helper function to draw a bar with rounded corners based on bar position
 */
private fun DrawScope.drawRoundedBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            if (isNegative && isBelowAxisMode) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            }
        }
    drawPath(path, brush)
}

