package com.himanshoe.charty.line.data

/**
 * Data class representing a point in a line chart
 *
 * @param label The label for this point (displayed on X-axis)
 * @param value The Y-value of the point
 */
data class LineData(
    val label: String,
    val value: Float
)
