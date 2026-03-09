package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * Builds the [AxisConfig] for the value (horizontal) axis.
 *
 * [drawAxisAtZero] is set to `true` when the data spans both negative and positive values
 * in [NegativeValuesDrawMode.BELOW_AXIS] mode so that the zero line is rendered.
 */
internal fun createGroupedHorizontalAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean,
): AxisConfig = AxisConfig(
    minValue = minValue,
    maxValue = maxValue,
    steps = GROUPED_HORIZONTAL_DEFAULT_AXIS_STEPS,
    drawAxisAtZero = drawAxisAtZero,
)

/**
 * Returns the left-side pixel offset reserved for value-axis tick labels.
 */
internal fun calculateGroupedHorizontalAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float =
    if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * GROUPED_HORIZONTAL_AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }

/**
 * Computes the x-coordinate of the value baseline.
 *
 * - When [drawAxisAtZero] is `true` the baseline sits at the pixel position that
 *   corresponds to value 0, allowing negative bars to grow to the left.
 * - Otherwise the baseline is at the left edge of the drawable area.
 */
internal fun calculateGroupedHorizontalBaselineX(
    drawAxisAtZero: Boolean,
    minValue: Float,
    maxValue: Float,
    chartContext: ChartContext,
    axisOffset: Float,
): Float {
    return if (drawAxisAtZero) {
        val range = maxValue - minValue
        val zeroNormalized = (0f - minValue) / range
        chartContext.left + (zeroNormalized * chartContext.width)
    } else {
        chartContext.left + axisOffset
    }
}
