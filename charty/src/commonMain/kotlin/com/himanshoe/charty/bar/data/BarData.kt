package com.himanshoe.charty.bar.data

import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.data.ChartDataPoint

/**
 * Data class representing a single bar in a bar chart
 *
 * @param label The label displayed on X-axis
 * @param value The value of the bar (determines height)
 * @param color Optional color for this specific bar. If null, uses the chart's default color scheme
 */
data class BarData(
    override val label: String,
    override val value: Float,
    val color: ChartyColor? = null,
) : ChartDataPoint
