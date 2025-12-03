package com.himanshoe.charty.line.data

import com.himanshoe.charty.common.data.ChartDataPoint

/**
 * Data class representing a point in a line chart
 *
 * @param label The label for this point (displayed on X-axis)
 * @param value The Y-value of the point
 */
data class LineData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint
