package com.himanshoe.charty.bar.ext

import com.himanshoe.charty.bar.data.BarGroup
import kotlin.jvm.JvmName

/**
 * Extension functions for BarData and BarGroup lists.
 * Note: For BarData, use the common ChartDataPoint interface extensions:
 *   - getValues()
 *   - getLabels()
 *   - calculateMinValue()
 *   - calculateMaxValue()
 *
 * This file contains only BarGroup-specific extensions.
 */

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
