package com.himanshoe.charty.bar

/**
 * Data class representing a single bar in a bar chart
 *
 * @param label The label displayed on X-axis
 * @param value The value of the bar (determines height)
 */
data class BarData(
    val label: String,
    val value: Float
)

