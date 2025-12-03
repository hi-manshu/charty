package com.himanshoe.charty.common.data

/**
 * Common interface for basic chart data with label and value.
 * This interface allows for generic operations on chart data across different chart types.
 */
interface ChartDataPoint {
    /**
     * The label for this data point (typically displayed on X-axis)
     */
    val label: String

    /**
     * The Y-value of the data point
     */
    val value: Float
}

/**
 * Extension functions for lists of chart data points
 */

/**
 * Extract all values from a list of ChartDataPoint
 */
fun <T : ChartDataPoint> List<T>.getValues(): List<Float> = map { it.value }

/**
 * Extract all labels from a list of ChartDataPoint
 */
fun <T : ChartDataPoint> List<T>.getLabels(): List<String> = map { it.label }

/**
 * Calculate minimum value with nice rounding
 */
fun <T : ChartDataPoint> List<T>.calculateMinValue(stepSize: Int = 10): Float {
    return com.himanshoe.charty.common.util.calculateMinValue(getValues(), stepSize)
}

/**
 * Calculate maximum value with nice rounding
 */
fun <T : ChartDataPoint> List<T>.calculateMaxValue(stepSize: Int = 10): Float {
    return com.himanshoe.charty.common.util.calculateMaxValue(getValues(), stepSize)
}

/**
 * Calculate both min and max values with nice rounding
 */
fun <T : ChartDataPoint> List<T>.calculateMinMaxValue(stepSize: Int = 10): Pair<Float, Float> {
    return com.himanshoe.charty.common.util.calculateMinMaxValue(getValues(), stepSize)
}

