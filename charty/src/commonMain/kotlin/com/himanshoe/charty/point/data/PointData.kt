package com.himanshoe.charty.point.data

/**
 * Data class representing a single point in a point/scatter chart
 *
 * @param label The label for this point (displayed on X-axis)
 * @param value The Y-value of the point
 */
data class PointData(
    val label: String,
    val value: Float
)

