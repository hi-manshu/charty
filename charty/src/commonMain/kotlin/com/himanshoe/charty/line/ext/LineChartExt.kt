package com.himanshoe.charty.line.ext

import com.himanshoe.charty.line.data.LineGroup
import kotlin.jvm.JvmName

/**
 * Extension functions for LineData and LineGroup lists.
 * Note: For LineData, use the common ChartDataPoint interface extensions:
 *   - getValues()
 *   - getLabels()
 *   - calculateMinValue()
 *   - calculateMaxValue()
 *
 * This file contains only LineGroup-specific extensions.
 */

/**
 * Extension function to get all values from all groups in LineGroup list
 */
@JvmName("getLineGroupAllValues")
fun List<LineGroup>.getAllValues(): List<Float> = flatMap { it.values }

/**
 * Extension function to get all labels from LineGroup list
 */
@JvmName("getLineGroupLabels")
fun List<LineGroup>.getLabels(): List<String> = map { it.label }
