package com.himanshoe.charty.line.data

import androidx.compose.runtime.Immutable

/**
 * Represents a point in a multiline chart that was clicked
 *
 * @property lineGroup The line group containing this point
 * @property seriesIndex The index of the line series (0 = first line, 1 = second line, etc.)
 * @property dataIndex The index of the data point within the line
 * @property value The value at this point
 */
@Immutable
data class MultilinePoint(
    val lineGroup: LineGroup,
    val seriesIndex: Int,
    val dataIndex: Int,
    val value: Float,
)
