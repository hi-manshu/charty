package com.himanshoe.charty.bar.ext

import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import kotlin.jvm.JvmName
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Calculate appropriate max value with nice rounding
 * Rounds up to the nearest multiple of stepSize
 *
 * @param values List of values to find max from
 * @param stepSize Step size for rounding (default 10)
 * @return Rounded max value
 */
internal fun calculateMaxValue(values: List<Float>, stepSize: Int = 10): Float {
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
internal fun calculateMinValue(values: List<Float>, stepSize: Int = 10): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Extension function to get all values from BarData list
 */
@JvmName("getBarDataValues")
fun List<BarData>.getValues(): List<Float> = map { it.value }

/**
 * Extension function to get all labels from BarData list
 */
@JvmName("getBarDataLabels")
fun List<BarData>.getLabels(): List<String> = map { it.label }

/**
 * Extension function to get all values from BarGroup list (flattened)
 */
@JvmName("getBarGroupAllValues")
fun List<BarGroup>.getAllValues(): List<Float> = flatMap { it.values }

/**
 * Extension function to get all labels from BarGroup list
 */
@JvmName("getBarGroupLabels")
fun List<BarGroup>.getLabels(): List<String> = map { it.label }

