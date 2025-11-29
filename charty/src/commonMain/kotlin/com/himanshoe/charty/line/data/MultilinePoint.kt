package com.himanshoe.charty.line.data

/**
 * Represents a point in a multiline chart that was clicked
 *
 * @param lineGroup The line group containing this point
 * @param seriesIndex The index of the line series (0 = first line, 1 = second line, etc.)
 * @param dataIndex The index of the data point within the line
 * @param value The value at this point
 */
data class MultilinePoint(
    val lineGroup: LineGroup,
    val seriesIndex: Int,
    val dataIndex: Int,
    val value: Float,
)
