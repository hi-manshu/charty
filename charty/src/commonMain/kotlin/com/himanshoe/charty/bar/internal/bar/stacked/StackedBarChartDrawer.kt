package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun DrawScope.drawStackedBars(params: StackedBarDrawParams) {
    params.dataList.fastForEachIndexed { groupIndex, barGroup ->
        val barX = params.chartContext.calculateBarLeftPosition(
            groupIndex,
            params.dataList.size,
            params.stackedConfig.barWidthFraction
        )
        val barWidth = params.chartContext.calculateBarWidth(
            params.dataList.size,
            params.stackedConfig.barWidthFraction
        )
        var cumulativeValue = 0f

        barGroup.values.fastForEachIndexed { segmentIndex, value ->
            val segmentBottomValue = cumulativeValue
            val segmentTopValue = cumulativeValue + value
            cumulativeValue = segmentTopValue

            val segmentBottomY = params.chartContext.convertValueToYPosition(segmentBottomValue)
            val segmentTopY = params.chartContext.convertValueToYPosition(segmentTopValue)
            val fullSegmentHeight = segmentBottomY - segmentTopY
            val animatedHeight = fullSegmentHeight * params.animationProgress
            val animatedTopY = segmentBottomY - animatedHeight

            if (params.onSegmentClick != null && animatedHeight > 0) {
                params.segmentBounds.add(
                    Rect(
                        left = barX,
                        top = animatedTopY,
                        right = barX + barWidth,
                        bottom = segmentBottomY,
                    ) to StackedBarSegment(
                        barGroup = barGroup,
                        segmentIndex = segmentIndex,
                        segmentValue = value,
                    ),
                )
            }

            val segmentChartyColor =
                if (barGroup.colors != null && segmentIndex < barGroup.colors.size) {
                    barGroup.colors[segmentIndex]
                } else {
                    ChartyColor.Solid(params.colorList[segmentIndex % params.colorList.size])
                }
            val isTopSegment = segmentIndex == barGroup.values.size - 1

            val segmentBrush = Brush.verticalGradient(
                colors = segmentChartyColor.value,
                startY = animatedTopY,
                endY = animatedTopY + animatedHeight,
            )

            drawStackedSegment(
                brush = segmentBrush,
                x = barX,
                y = animatedTopY,
                width = barWidth,
                height = animatedHeight,
                cornerRadius = if (isTopSegment) params.stackedConfig.topCornerRadius.value else 0f,
                isTopSegment = isTopSegment,
            )
        }
    }
}

/**
 * Helper function to draw a segment of a stacked bar with gradient support
 * Only the top segment gets rounded corners
 */
private fun DrawScope.drawStackedSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    isTopSegment: Boolean,
) {
    val path = Path().apply {
        if (isTopSegment && cornerRadius > 0f) {
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
        } else {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius.Zero,
                ),
            )
        }
    }
    drawPath(path, brush)
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawStackedReferenceLineIfNeeded(
    stackedConfig: StackedBarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer
) {
    drawReferenceLineIfNeeded(
        referenceLineConfig = stackedConfig.referenceLine,
        chartContext = chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = textMeasurer,
    )
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawStackedTooltipIfNeeded(
    tooltipState: TooltipState?,
    stackedConfig: StackedBarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext
) {
    drawTooltipIfNeeded(
        tooltipState = tooltipState,
        tooltipConfig = stackedConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartContext = chartContext,
    )
}

