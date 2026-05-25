package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

/**
 * Builds the [AxisConfig] for the value (horizontal) axis.
 *
 * [minValue] and [maxValue] must be "nice" values (multiples of the tick step) produced by
 * [com.himanshoe.charty.common.util.calculateNiceAxisRange]. [steps] is the actual interval
 * count returned by that function, ensuring tick labels land on round numbers and — when the
 * range spans both sides — that zero coincides with a tick mark.
 * [drawAxisAtZero] controls whether the zero-value vertical line is rendered.
 */
internal fun createGroupedHorizontalAxisConfig(
    minValue: Float,
    maxValue: Float,
    steps: Int,
    drawAxisAtZero: Boolean,
): AxisConfig = AxisConfig(
    minValue = minValue,
    maxValue = maxValue,
    steps = steps,
    drawAxisAtZero = drawAxisAtZero,
)

/**
 * Computes the x-coordinate of the value baseline.
 *
 * - When [drawAxisAtZero] is `true` the baseline sits at the pixel position that
 *   corresponds to value 0, allowing negative bars to grow to the left.
 * - Otherwise the baseline is at [ChartContext.left] (the left edge of the drawable area).
 */
internal fun calculateGroupedHorizontalBaselineX(
    drawAxisAtZero: Boolean,
    minValue: Float,
    maxValue: Float,
    chartContext: ChartContext,
): Float {
    return if (drawAxisAtZero) {
        val range = maxValue - minValue
        val zeroNormalized = (0f - minValue) / range
        chartContext.left + (zeroNormalized * chartContext.width)
    } else {
        chartContext.left
    }
}
