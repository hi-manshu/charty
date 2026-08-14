package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarSegment
import com.himanshoe.charty.bar.internal.bar.BarCorners
import com.himanshoe.charty.bar.internal.bar.drawBarShape
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.PERCENT_SCALE
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Renders 100%-normalised stacked horizontal bars for all groups in [params].
 *
 * ## Layout
 * Every bar always extends the full available width regardless of the group's actual
 * total. Segment widths are proportional to each value's share of the group total
 * (`value / groupTotal`), so all bars show part-to-whole relationships at a glance.
 *
 * ## Animation
 * All segments grow left-to-right together, scaled uniformly by [animationProgress].
 * This gives the bar a smooth, cohesive entrance from the left edge.
 *
 * ## Corner radius
 * Only the trailing (rightmost) segment receives the [rightCornerRadius] from the config,
 * keeping the left edge square (flush against the axis).
 *
 * ## Colour resolution (per segment, in priority order)
 * 1. `BarGroup.colors[segmentIndex]` — per-segment override.
 * 2. `colorList[segmentIndex % colorList.size]` — chart-level palette cycle.
 *
 * ## Negative values
 * Segments with a value ≤ 0 are silently skipped; normalization is only meaningful for
 * positive values. Validate your data upstream if this matters for your use case.
 */
internal fun DrawScope.drawNormalizedHorizontalBars(params: NormalizedHorizontalBarDrawParams) {
    val totalGroups = params.dataList.size
    val rowHeight = params.chartContext.calculateSlotHeight(totalGroups)
    val barThickness = rowHeight * params.config.barWidthFraction
    val availableWidth = params.chartContext.width

    params.dataList.fastForEachIndexed { groupIndex, barGroup ->
        val groupTotal = barGroup.values.fastFilter { it > 0f }.sum()
        if (groupTotal == 0f) {
            return@fastForEachIndexed
        }

        val centeredBarY =
            params.chartContext.calculateSlotTopPosition(groupIndex, totalGroups) +
                (rowHeight - barThickness) / 2f

        val effectiveWidth = availableWidth * params.animationProgress
        var cumulativeFraction = 0f
        val lastActiveIndex = barGroup.values.indexOfLast { it > 0f }

        barGroup.values.fastForEachIndexed { segmentIndex, value ->
            if (value <= 0f) {
                return@fastForEachIndexed
            }

            val fraction = value / groupTotal
            val segmentPercentage = fraction * PERCENT_SCALE
            val startX = params.chartContext.left + (cumulativeFraction * effectiveWidth)
            val segmentWidth = fraction * effectiveWidth

            cumulativeFraction += fraction

            if (segmentWidth <= 0f) {
                return@fastForEachIndexed
            }

            if (params.recordBounds) {
                params.segmentBounds.add(
                    Rect(
                        left = startX,
                        top = centeredBarY,
                        right = startX + segmentWidth,
                        bottom = centeredBarY + barThickness,
                    ) to
                        NormalizedHorizontalBarSegment(
                            barGroup = barGroup,
                            segmentIndex = segmentIndex,
                            segmentValue = value,
                            segmentPercentage = segmentPercentage,
                        ),
                )
            }

            val segmentColor =
                if (barGroup.colors != null && segmentIndex < barGroup.colors.size) {
                    barGroup.colors[segmentIndex]
                } else {
                    ChartyColor.Solid(params.colorList[segmentIndex % params.colorList.size])
                }

            val isLastSegment = segmentIndex == lastActiveIndex
            val cornerRadius =
                if (isLastSegment) {
                    params.config.rightCornerRadius.value
                } else {
                    0f
                }

            val brush =
                Brush.horizontalGradient(
                    colors = segmentColor.value,
                    startX = startX,
                    endX = startX + segmentWidth,
                )

            drawNormalizedHorizontalSegment(
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
private fun DrawScope.drawNormalizedHorizontalSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    isLastSegment: Boolean,
) {
    drawBarShape(
        brush = brush,
        x = x,
        y = y,
        width = width,
        height = height,
        cornerRadius = cornerRadius,
        // Only the segment at the end of the stack is rounded; the ones before it butt against a
        // neighbour, and a rounded join would show the background through the gap.
        corners =
            if (isLastSegment && cornerRadius > 0f) {
                BarCorners.TRAILING
            } else {
                BarCorners.NONE
            },
    )
}

internal fun DrawScope.drawNormalizedHorizontalTooltipIfNeeded(
    tooltipState: TooltipState?,
    config: NormalizedHorizontalBarChartConfig,
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
