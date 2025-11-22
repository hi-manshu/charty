package com.himanshoe.charty.point

import kotlin.jvm.JvmName
import kotlin.math.ceil

/**
 * Calculate appropriate max value with nice rounding
 */
internal fun calculateMaxValue(values: List<Float>, stepSize: Int = 10): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
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

