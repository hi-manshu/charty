package com.himanshoe.charty.point.data

import com.himanshoe.charty.common.data.ChartDataPoint

/**
 * Data class representing a single point in a point/scatter chart
 *
 * @param label The label for this point (displayed on X-axis)
 * @param value The Y-value of the point
 */
data class PointData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint
