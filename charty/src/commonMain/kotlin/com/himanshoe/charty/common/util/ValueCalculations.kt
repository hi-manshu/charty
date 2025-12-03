package com.himanshoe.charty.common.util

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Common utilities for calculating min/max values with nice rounding
 * for chart axis scaling.
 */

/**
 * Calculates an appropriate maximum value with "nice" rounding, suitable for a chart axis.
 *
 * This function rounds the maximum value in the list up to the nearest multiple of [stepSize].
 *
 * @param values The list of values from which to find the maximum.
 * @param stepSize The step size for rounding.
 * @return The rounded maximum value.
 */
fun calculateMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculates an appropriate minimum value with "nice" rounding, suitable for a chart axis.
 *
 * This function rounds the minimum value in the list down to the nearest multiple of [stepSize].
 *
 * @param values The list of values from which to find the minimum.
 * @param stepSize The step size for rounding.
 * @return The rounded minimum value.
 */
fun calculateMinValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculates the minimum and maximum values with percentage-based padding.
 *
 * This is useful for charts like candlestick charts where padding is preferred over step-based rounding.
 *
 * @param values The list of values from which to calculate the range.
 * @param paddingMultiplier The padding as a fraction (e.g., 0.05 for 5% padding).
 * @return A [Pair] containing the minimum and maximum values with padding applied.
 */
fun calculateMinMaxWithPadding(
    values: List<Float>,
    paddingMultiplier: Float = 0.05f,
): Pair<Float, Float> {
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    return (min * (1f - paddingMultiplier)) to (max * (1f + paddingMultiplier))
}

/**
 * Calculates the minimum and maximum values from a list with step-based rounding.
 *
 * @param values The list of values.
 * @param stepSize The step size for rounding.
 * @return A [Pair] containing the minimum and maximum values with "nice" rounding.
 */
fun calculateMinMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Pair<Float, Float> {
    return calculateMinValue(values, stepSize) to calculateMaxValue(values, stepSize)
}

