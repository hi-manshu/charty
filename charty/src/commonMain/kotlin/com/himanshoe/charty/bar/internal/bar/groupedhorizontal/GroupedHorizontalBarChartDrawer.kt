package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarEntry
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Renders all grouped horizontal bars for the given [params].
 *
 * ## Layout
 * The chart height is divided into equal horizontal bands — one per [BarGroup].
 * Within each band, the bars are stacked vertically (top to bottom) and centred
 * using [barWidthFraction]. A configurable [barSpacing] gap separates adjacent bars.
 *
 * ## Negative value support
 * - **[NegativeValuesDrawMode.BELOW_AXIS]**: positive bars extend to the right of the
 *   zero baseline; negative bars extend to the left. A zero axis line is shown.
 * - **[NegativeValuesDrawMode.FROM_MIN_VALUE]**: all bars extend rightward from the
 *   minimum value so the shortest bar is zero-width.
 *
 * ## Animation
 * Each bar grows from the baseline outward, scaled uniformly by [animationProgress].
 *
 * ## Colour
 * Resolved in priority order:
 * 1. `BarGroup.colors[barIndex]` — per-bar override.
 * 2. `colorList[barIndex % colorList.size]` — chart-level palette cycle.
 */
internal fun DrawScope.drawGroupedHorizontalBars(params: GroupedHorizontalBarDrawParams) {
    val range = params.maxValue - params.minValue
    if (range == 0f) {
        return
    }

    val isBelowAxisMode = params.config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val totalGroups = params.dataList.size
    val rowHeight = params.chartContext.calculateSlotHeight(totalGroups)

    params.dataList.fastForEachIndexed { groupIndex, barGroup ->
        val numBars = barGroup.values.size
        if (numBars == 0) {
            return@fastForEachIndexed
        }

        val usableRowHeight = rowHeight * params.config.barWidthFraction
        val totalSpacing = params.config.barSpacing * (numBars - 1).coerceAtLeast(0)
        val singleBarHeight = ((usableRowHeight - totalSpacing) / numBars).coerceAtLeast(1f)
        val rowTop = params.chartContext.calculateSlotTopPosition(groupIndex, totalGroups)
        val usableStart = rowTop + (rowHeight - usableRowHeight) / 2f

        barGroup.values.fastForEachIndexed { barIndex, value ->
            val barTop = usableStart + barIndex * (singleBarHeight + params.config.barSpacing)

            val valueNormalized = (value - params.minValue) / range
            val barValueX = params.chartContext.left + (valueNormalized * params.chartContext.width)

            val isNegative = value < 0f

            val (barLeft, barWidth) =
                if (isNegative && isBelowAxisMode) {
                    val fullWidth = params.baselineX - barValueX
                    barValueX to (fullWidth * params.animationProgress)
                } else {
                    val fullWidth = barValueX - params.baselineX
                    params.baselineX to (fullWidth * params.animationProgress)
                }

            if (barWidth <= 0f) {
                return@fastForEachIndexed
            }

            if (params.recordBounds) {
                params.barBounds.add(
                    Rect(
                        left = barLeft,
                        top = barTop,
                        right = barLeft + barWidth,
                        bottom = barTop + singleBarHeight,
                    ) to
                        GroupedHorizontalBarEntry(
                            barGroup = barGroup,
                            barIndex = barIndex,
                            barValue = value,
                        ),
                )
            }

            val barColor =
                if (barGroup.colors != null && barIndex < barGroup.colors.size) {
                    barGroup.colors[barIndex]
                } else {
                    ChartyColor.Solid(params.colorList[barIndex % params.colorList.size])
                }

            val brush =
                Brush.horizontalGradient(
                    colors = barColor.value,
                    startX = params.chartContext.left,
                    endX = params.chartContext.right,
                )

            drawGroupedHorizontalBar(
                brush = brush,
                x = barLeft,
                y = barTop,
                width = barWidth,
                height = singleBarHeight,
                cornerRadius = params.config.cornerRadius.value,
                isNegative = isNegative,
                isBelowAxisMode = isBelowAxisMode,
            )
        }
    }
}

/**
 * Draws a single horizontal bar with appropriate rounded corners.
 *
 * Positive bars round the **right** (trailing) corners; negative bars (in
 * [NegativeValuesDrawMode.BELOW_AXIS]) round the **left** (leading) corners so that
 * the open end always faces the baseline.
 */
private fun DrawScope.drawGroupedHorizontalBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
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
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
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
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            }
        }
    drawPath(path, brush)
}

internal fun DrawScope.drawGroupedHorizontalReferenceLineIfNeeded(
    config: GroupedHorizontalBarChartConfig,
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

internal fun DrawScope.drawGroupedHorizontalTooltipIfNeeded(
    tooltipState: TooltipState?,
    config: GroupedHorizontalBarChartConfig,
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
