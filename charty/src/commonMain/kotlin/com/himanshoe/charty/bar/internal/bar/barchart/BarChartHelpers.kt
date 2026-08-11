package com.himanshoe.charty.bar.internal.bar.barchart

import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.util.baselineValueRange
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue

/**
 * Computes the `(min, max)` value axis range for a bar chart, honoring [negativeValuesDrawMode].
 *
 * - [NegativeValuesDrawMode.BELOW_AXIS] clamps the minimum to `0` unless the data actually goes
 *   negative, so bars grow from a zero baseline and negatives drop below it.
 * - [NegativeValuesDrawMode.FROM_MIN_VALUE] keeps the real (nice-rounded) minimum, so the axis
 *   starts at the lowest data value instead of zero.
 */
internal fun barValueRange(
    values: List<Float>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    when (negativeValuesDrawMode) {
        NegativeValuesDrawMode.FROM_MIN_VALUE -> calculateMinValue(values) to calculateMaxValue(values)
        NegativeValuesDrawMode.BELOW_AXIS -> baselineValueRange(values)
    }

/**
 * Computes `(barTop, barHeight)` for a vertical bar.
 *
 * The "grow downward from the baseline" treatment is applied **only** in
 * [BELOW_AXIS][NegativeValuesDrawMode.BELOW_AXIS] mode, where the baseline is the zero line. In
 * [FROM_MIN_VALUE][NegativeValuesDrawMode.FROM_MIN_VALUE] mode the baseline is the bottom edge, so
 * every bar (including negative-valued ones, which still sit above the axis minimum) grows upward.
 */
internal fun calculateVerticalBarDimensions(
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    baselineY: Float,
    barValueY: Float,
    animationProgress: Float,
): Pair<Float, Float> =
    if (isNegative && isBelowAxisMode) {
        baselineY to (barValueY - baselineY) * animationProgress
    } else {
        val animatedBarHeight = (baselineY - barValueY) * animationProgress
        (baselineY - animatedBarHeight) to animatedBarHeight
    }

/**
 * Calculate the baseline Y position for bars, considering negative values and axis position
 */
internal fun calculateBarBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: ChartContext,
): Float =
    if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }

/**
 * Helper function to create the axis configuration for the Y axis
 */
internal fun createBarAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = 6,
        drawAxisAtZero = isBelowAxisMode,
    )
