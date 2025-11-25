package com.himanshoe.charty.common.axis

import kotlin.math.ceil

/**
 * Basic configuration for chart axes.
 * Holds the labels that appear along the X and Y axis.
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
     * Companion object providing factory methods for creating ChartAxisConfig instances
     */
    companion object {
        /**
         * Helper factory that builds evenly spaced Y labels for numeric data.
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
