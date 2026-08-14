package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.BarCorners
import com.himanshoe.charty.bar.internal.bar.drawBarShape
import com.himanshoe.charty.bar.internal.bar.drawVerticalBarMarkers
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun DrawScope.drawComparisonBars(params: ComparisonBarDrawParams) {
    params.dataList.fastForEachIndexed { groupIndex, group ->
        val groupWidth = params.chartContext.width / params.dataList.size
        val barWidth = groupWidth / group.values.size * COMPARISON_BAR_WIDTH_FRACTION

        group.values.fastForEachIndexed { barIndex, value ->
            val barX =
                calculateComparisonBarX(
                    chartContext = params.chartContext,
                    groupWidth = groupWidth,
                    groupIndex = groupIndex,
                    barWidth = barWidth,
                    barIndex = barIndex,
                )

            val barValueY = params.chartContext.convertValueToYPosition(value)
            val isNegative = value < 0f

            val (rawBarTop, rawBarHeight) =
                calculateComparisonBarDimensions(
                    value = value,
                    baselineY = params.baselineY,
                    barValueY = barValueY,
                )
            val barHeight = rawBarHeight * params.animationProgress
            val barTop =
                if (isNegative) {
                    rawBarTop
                } else {
                    params.baselineY - barHeight
                }

            if (params.recordBounds) {
                params.barBounds.add(
                    Rect(
                        left = barX,
                        top = barTop,
                        right = barX + barWidth,
                        bottom = barTop + barHeight,
                    ) to
                        ComparisonBarSegment(
                            barGroup = group,
                            barIndex = barIndex,
                            barValue = value,
                        ),
                )
            }

            val barChartyColor = getComparisonBarColor(group = group, barIndex = barIndex)
            val barBrush =
                createComparisonBarBrush(barChartyColor = barChartyColor, barTop = barTop, barHeight = barHeight)

            drawComparisonRoundedBar(
                brush = barBrush,
                x = barX,
                y = barTop,
                width = barWidth,
                height = barHeight,
                isNegative = isNegative,
                isBelowAxisMode = params.comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS,
                cornerRadius = params.comparisonConfig.cornerRadius.value,
            )
        }
    }
    if (params.comparisonConfig.markers.isNotEmpty()) {
        drawVerticalBarMarkers(
            chartContext = params.chartContext,
            markers = params.comparisonConfig.markers,
            values = params.dataList.fastMap { group -> group.values.maxOrNull() ?: 0f },
            textMeasurer = params.textMeasurer,
        )
    }
}

private fun getComparisonBarColor(
    group: BarGroup,
    barIndex: Int,
): ChartyColor {
    require(group.colors != null) {
        "ComparisonBarChart requires each BarGroup to specify colors. Please set the 'colors' property in BarGroup."
    }
    require(barIndex < group.colors.size) {
        "BarGroup '${group.label}' has ${group.values.size} values but only " +
            "${group.colors.size} colors. Please provide a color for each value."
    }
    return group.colors[barIndex]
}

private fun createComparisonBarBrush(
    barChartyColor: ChartyColor,
    barTop: Float,
    barHeight: Float,
): Brush =
    Brush.verticalGradient(
        colors = barChartyColor.value,
        startY = barTop,
        endY = barTop + barHeight,
    )

/**
 * Helper function to draw a comparison bar with rounded corners and gradient support
 */
private fun DrawScope.drawComparisonRoundedBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    drawBarShape(
        brush = brush,
        x = x,
        y = y,
        width = width,
        height = height,
        cornerRadius = cornerRadius,
        corners =
            if (isNegative && isBelowAxisMode) {
                BarCorners.BOTTOM
            } else {
                BarCorners.TOP
            },
    )
}

internal fun DrawScope.drawComparisonReferenceLineIfNeeded(
    comparisonConfig: ComparisonBarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    drawReferenceLineIfNeeded(
        referenceLineConfig = comparisonConfig.referenceLine,
        chartContext = chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = textMeasurer,
    )
}

internal fun DrawScope.drawComparisonTooltipIfNeeded(
    tooltipState: TooltipState?,
    comparisonConfig: ComparisonBarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    drawTooltipIfNeeded(
        tooltipState = tooltipState,
        tooltipConfig = comparisonConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartContext = chartContext,
    )
}
