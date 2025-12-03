package com.himanshoe.charty.common.util

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Common utilities for calculating min/max values with nice rounding
 * for chart axis scaling.
 */

/**
 * Calculate appropriate max value with nice rounding
 * Rounds up to the nearest multiple of stepSize
 *
 * @param values List of values to find max from
 * @param stepSize Step size for rounding (default 10)
 * @return Rounded max value
 */
fun calculateMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate appropriate min value with nice rounding
 * Rounds down to the nearest multiple of stepSize
 *
 * @param values List of values to find min from
 * @param stepSize Step size for rounding (default 10)
 * @return Rounded min value
 */
fun calculateMinValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate min and max with percentage-based padding
 * Used for charts like candlestick where padding is preferred over step-based rounding
 *
 * @param values List of values to calculate range from
 * @param paddingMultiplier Padding as a fraction (e.g., 0.05 for 5% padding)
 * @return Pair of (min, max) with padding applied
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
 * Calculate min and max values from a list with step-based rounding
 *
 * @param values List of values
 * @param stepSize Step size for rounding
 * @return Pair of (min, max) with nice rounding
 */
fun calculateMinMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Pair<Float, Float> {
    return calculateMinValue(values, stepSize) to calculateMaxValue(values, stepSize)
}

