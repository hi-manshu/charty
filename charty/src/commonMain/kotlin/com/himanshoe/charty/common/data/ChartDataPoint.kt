package com.himanshoe.charty.common.data

/**
 * A common interface for a basic chart data point, which includes a label and a value.
 *
 * This interface allows for generic operations on chart data across different chart types,
 * promoting a consistent data structure.
 *
 * @property label The label for this data point, typically displayed on the x-axis.
 * @property value The y-value of the data point.
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
 * Extracts all values from a list of [ChartDataPoint]s.
 *
 * @return A list of floats representing the values of the data points.
 */
fun <T : ChartDataPoint> List<T>.getValues(): List<Float> = map { it.value }

/**
 * Extracts all labels from a list of [ChartDataPoint]s.
 *
 * @return A list of strings representing the labels of the data points.
 */
fun <T : ChartDataPoint> List<T>.getLabels(): List<String> = map { it.label }

/**
 * Calculates the minimum value in a list of [ChartDataPoint]s, with nice rounding.
 *
 * @param stepSize The step size used for rounding the minimum value.
 * @return The calculated minimum value.
 */
fun <T : ChartDataPoint> List<T>.calculateMinValue(stepSize: Int = 10): Float {
    return com.himanshoe.charty.common.util.calculateMinValue(getValues(), stepSize)
}

/**
 * Calculates the maximum value in a list of [ChartDataPoint]s, with nice rounding.
 *
 * @param stepSize The step size used for rounding the maximum value.
 * @return The calculated maximum value.
 */
fun <T : ChartDataPoint> List<T>.calculateMaxValue(stepSize: Int = 10): Float {
    return com.himanshoe.charty.common.util.calculateMaxValue(getValues(), stepSize)
}

/**
 * Calculates both the minimum and maximum values in a list of [ChartDataPoint]s, with nice rounding.
 *
 * @param stepSize The step size used for rounding the values.
 * @return A [Pair] containing the calculated minimum and maximum values.
 */
fun <T : ChartDataPoint> List<T>.calculateMinMaxValue(stepSize: Int = 10): Pair<Float, Float> {
    return com.himanshoe.charty.common.util.calculateMinMaxValue(getValues(), stepSize)
}

