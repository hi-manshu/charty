package com.himanshoe.charty.line.data

/**
 * Represents an area segment in a stacked area chart that was clicked
 *
 * @param lineGroup The line group containing this segment
 * @param seriesIndex The index of the area series (0 = bottom area, 1 = next area, etc.)
 * @param dataIndex The index of the data point within the area
 * @param value The value of this segment (not cumulative)
 * @param cumulativeValue The cumulative value at this point
 */
data class StackedAreaPoint(
    val lineGroup: LineGroup,
    val seriesIndex: Int,
    val dataIndex: Int,
    val value: Float,
    val cumulativeValue: Float,
)
