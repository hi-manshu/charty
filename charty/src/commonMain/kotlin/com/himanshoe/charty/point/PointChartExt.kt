package com.himanshoe.charty.point

import com.himanshoe.charty.point.data.PointData
import kotlin.jvm.JvmName
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Calculate appropriate max value with nice rounding
 */
internal fun calculateMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate appropriate min value with nice rounding
 */
internal fun calculateMinValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Extension function to get all values from PointData list
 */
@JvmName("getPointDataValues")
fun List<PointData>.getValues(): List<Float> = map { it.value }

/**
 * Extension function to get all labels from PointData list
 */
@JvmName("getPointDataLabels")
fun List<PointData>.getLabels(): List<String> = map { it.label }
