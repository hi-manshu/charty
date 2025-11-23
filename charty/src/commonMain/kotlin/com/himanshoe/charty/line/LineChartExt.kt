package com.himanshoe.charty.line

import kotlin.jvm.JvmName
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Calculate appropriate max value with nice rounding
 */
internal fun calculateMaxValue(values: List<Float>, stepSize: Int = 10): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate appropriate min value with nice rounding
 */
internal fun calculateMinValue(values: List<Float>, stepSize: Int = 10): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Extension function to get all values from LineData list
 */
@JvmName("getLineDataValues")
fun List<LineData>.getValues(): List<Float> = map { it.value }

/**
 * Extension function to get all labels from LineData list
 */
@JvmName("getLineDataLabels")
fun List<LineData>.getLabels(): List<String> = map { it.label }

