package com.himanshoe.charty.combo.data

/**
 * Data class representing a combined bar and line chart entry
 *
 * @param label The label for this data point (displayed on X-axis)
 * @param barValue The value for the bar visualization
 * @param lineValue The value for the line visualization
 */
data class ComboChartData(
    val label: String,
    val barValue: Float,
    val lineValue: Float
)

