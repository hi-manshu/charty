package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Renders stacked horizontal bars for all groups in [params].
 *
 * ## Layout
 * Each [BarGroup] occupies an equal horizontal band. Within that band the bar is
 * vertically centred using [barWidthFraction]. Segments are laid out left-to-right,
 * all scaling uniformly with [animationProgress] so the bar grows as one unit from
 * the left edge — giving a smooth, cohesive entrance animation.
 *
 * ## Corner radius
 * Only the trailing (rightmost) segment receives the [rightCornerRadius] supplied via
 * [StackedHorizontalBarChartConfig], mirroring the top-corner convention of the
 * vertical [StackedBarChart].
 *
 * ## Colour
 * Segment colours are resolved in priority order:
 * 1. Per-segment colour from [BarGroup.colors] (if provided).
 * 2. The colour at `segmentIndex % colorList.size` from the chart's colour palette,
 *    applied as a horizontal gradient scoped to the individual segment.
 */
internal fun DrawScope.drawStackedHorizontalBars(params: StackedHorizontalBarDrawParams) {
    if (params.maxTotal == 0f) return

    val totalGroups = params.dataList.size
    val rowHeight = params.chartContext.height / totalGroups
    val barThickness = rowHeight * params.config.barWidthFraction
    val availableWidth = params.chartContext.width

    params.dataList.fastForEachIndexed { groupIndex, barGroup ->
        val centeredBarY =
            params.chartContext.top +
                (rowHeight * groupIndex) +
                (rowHeight - barThickness) / 2f

        val effectiveWidth = availableWidth * params.animationProgress
        var cumulativeValue = 0f
        val lastSegmentIndex = barGroup.values.size - 1

        barGroup.values.fastForEachIndexed { segmentIndex, value ->
            val startX =
                params.chartContext.left +
                    (cumulativeValue / params.maxTotal) * effectiveWidth
            val segmentWidth = (value / params.maxTotal) * effectiveWidth

            cumulativeValue += value

            if (segmentWidth <= 0f) return@fastForEachIndexed

            if (params.recordBounds) {
                params.segmentBounds.add(
                    Rect(
                        left = startX,
                        top = centeredBarY,
                        right = startX + segmentWidth,
                        bottom = centeredBarY + barThickness,
                    ) to
                        StackedHorizontalBarSegment(
                            barGroup = barGroup,
                            segmentIndex = segmentIndex,
                            segmentValue = value,
                        ),
                )
            }

            val segmentColor =
                if (barGroup.colors != null && segmentIndex < barGroup.colors.size) {
                    barGroup.colors[segmentIndex]
                } else {
                    ChartyColor.Solid(params.colorList[segmentIndex % params.colorList.size])
                }

            val isLastSegment = segmentIndex == lastSegmentIndex
            val cornerRadius = if (isLastSegment) params.config.rightCornerRadius.value else 0f

            val brush =
                Brush.horizontalGradient(
                    colors = segmentColor.value,
                    startX = startX,
                    endX = startX + segmentWidth,
                )

            drawStackedHorizontalSegment(
                brush = brush,
                x = startX,
                y = centeredBarY,
                width = segmentWidth,
                height = barThickness,
                cornerRadius = cornerRadius,
                isLastSegment = isLastSegment,
            )
        }
    }
}

/** Draws a single segment, rounding only the trailing (right) corners on the last segment. */
private fun DrawScope.drawStackedHorizontalSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    isLastSegment: Boolean,
) {
    val path =
        Path().apply {
            if (isLastSegment && cornerRadius > 0f) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
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
internal fun DrawScope.drawStackedHorizontalReferenceLineIfNeeded(
    config: StackedHorizontalBarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    drawReferenceLineIfNeeded(
        referenceLineConfig = config.referenceLine,
        chartContext = chartContext,
        orientation = ChartOrientation.HORIZONTAL,
        textMeasurer = textMeasurer,
    )
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawStackedHorizontalTooltipIfNeeded(
    tooltipState: TooltipState?,
    config: StackedHorizontalBarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    drawTooltipIfNeeded(
        tooltipState = tooltipState,
        tooltipConfig = config.tooltipConfig,
        textMeasurer = textMeasurer,
        chartContext = chartContext,
    )
}
