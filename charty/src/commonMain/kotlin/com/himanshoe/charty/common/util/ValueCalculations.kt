package com.himanshoe.charty.common.util

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Common utilities for calculating min/max values with nice rounding
 * for chart axis scaling.
 */

private const val NICE_STEP_UPPER_1 = 1.5f
private const val NICE_STEP_UPPER_2 = 3.5f
private const val NICE_STEP_UPPER_5 = 7.5f
private const val NICE_FACTOR_2 = 2f
private const val NICE_FACTOR_5 = 5f
private const val NICE_FACTOR_10 = 10f
private const val NICE_RANGE_ROUND_HALF = 0.5f

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
): Pair<Float, Float> = calculateMinValue(values, stepSize) to calculateMaxValue(values, stepSize)

/**
 * Computes the `(min, max)` value range for a chart whose bars grow from a zero baseline.
 *
 * Applies the shared rule used by the bar-family charts: the maximum is "nice"-rounded via
 * [calculateMaxValue], and the minimum is clamped to `0f` unless the data actually goes negative
 * (in which case the "nice"-rounded [calculateMinValue] is used). Centralizing this keeps the
 * baseline behaviour identical across `BarChart`, `HorizontalBarChart`, `BubbleBarChart`,
 * `ComparisonBarChart`, and any future bar variant.
 *
 * @param values The flattened data values.
 * @return A [Pair] of `(min, max)` for the value axis.
 */
internal fun baselineValueRange(values: List<Float>): Pair<Float, Float> {
    val calculatedMin = calculateMinValue(values)
    val calculatedMax = calculateMaxValue(values)
    val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
    return finalMin to calculatedMax
}

/**
 * Rounds a rough step value up to the nearest "nice" increment (1, 2, or 5 × 10ᵏ).
 * Produces human-readable axis tick intervals.
 */
internal fun niceAxisStep(roughStep: Float): Float {
    if (roughStep <= 0f) return 1f
    val exponent = floor(log10(roughStep)).toInt()
    val magnitude = NICE_FACTOR_10.pow(exponent)
    val normalized = roughStep / magnitude
    val niceFraction =
        when {
            normalized <= NICE_STEP_UPPER_1 -> 1f
            normalized <= NICE_STEP_UPPER_2 -> NICE_FACTOR_2
            normalized <= NICE_STEP_UPPER_5 -> NICE_FACTOR_5
            else -> NICE_FACTOR_10
        }
    return niceFraction * magnitude
}

/**
 * Computes a "nice" axis range guaranteed to include zero as a tick whenever the range
 * spans both negative and positive values.
 *
 * Uses a D3-style algorithm: the rough step is rounded to the nearest 1/2/5 × 10ᵏ, then the
 * domain is expanded to the nearest multiples of that step. Because [niceMin] and [niceMax]
 * are both multiples of the step, zero (also a multiple of any positive step) is always a tick.
 *
 * @param rawMin Minimum data value (already clamped/adjusted where needed).
 * @param rawMax Maximum data value.
 * @param targetSteps Approximate desired number of intervals between ticks.
 * @return Triple(niceMin, niceMax, actualSteps).
 */
internal fun calculateNiceAxisRange(
    rawMin: Float,
    rawMax: Float,
    targetSteps: Int,
): Triple<Float, Float, Int> {
    require(targetSteps > 0)
    if (rawMin == rawMax) {
        val half = if (rawMin == 0f) 1f else abs(rawMin)
        return Triple(rawMin - half, rawMax + half, 2)
    }
    val range = rawMax - rawMin
    val step = niceAxisStep(range / targetSteps)
    val niceMin = floor(rawMin / step) * step
    val niceMax = ceil(rawMax / step) * step
    val actualSteps = ((niceMax - niceMin) / step + NICE_RANGE_ROUND_HALF).toInt().coerceAtLeast(1)
    return Triple(niceMin, niceMax, actualSteps)
}
