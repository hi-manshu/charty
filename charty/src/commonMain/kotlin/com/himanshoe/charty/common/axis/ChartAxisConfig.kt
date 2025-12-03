package com.himanshoe.charty.common.axis

import kotlin.math.ceil

/**
 * A data class that holds the basic configuration for chart axes, including the labels for the x and y axes.
 *
 * @property xAxisLabels A list of strings representing the labels for the x-axis.
 * @property yAxisLabels A list of strings representing the labels for the y-axis. It must contain at least two entries.
 * @property ySteps The number of steps or intervals on the y-axis, derived from the size of [yAxisLabels].
 */
data class ChartAxisConfig(
    val xAxisLabels: List<String>,
    val yAxisLabels: List<String>,
) {
    init {
        require(yAxisLabels.size >= 2) { "yAxisLabels must contain at least two entries" }
    }

    val ySteps: Int get() = yAxisLabels.size - 1

    /**
     * A companion object that provides factory methods for creating [ChartAxisConfig] instances.
     */
    companion object {
        /**
         * A helper factory function that creates evenly spaced y-axis labels for numeric data.
         *
         * @param xAxisLabels A list of strings for the x-axis labels.
         * @param maxValue The maximum value in the dataset, used to determine the range of the y-axis.
         * @param stepSize The desired interval between y-axis labels.
         * @return A [ChartAxisConfig] instance with automatically generated y-axis labels.
         */
        fun fromNumericRange(
            xAxisLabels: List<String>,
            maxValue: Float,
            stepSize: Int = 10,
        ): ChartAxisConfig {
            val safeStep = stepSize.coerceAtLeast(1)
            val top = ceil(maxValue / safeStep).toInt() * safeStep
            val steps = (top / safeStep)
            val yLabels = (0..steps).map { value -> (value * safeStep).toString() }
            return ChartAxisConfig(xAxisLabels, yLabels)
        }
    }
}
