package com.himanshoe.charty.line.data

import androidx.compose.runtime.Immutable

/**
 * Represents an area segment in a stacked area chart that was clicked
 *
 * @property lineGroup The line group containing this segment
 * @property seriesIndex The index of the area series (0 = bottom area, 1 = next area, etc.)
 * @property dataIndex The index of the data point within the area
 * @property value The value of this segment (not cumulative)
 * @property cumulativeValue The cumulative value at this point
 */
@Immutable
data class StackedAreaPoint(
    val lineGroup: LineGroup,
    val seriesIndex: Int,
    val dataIndex: Int,
    val value: Float,
    val cumulativeValue: Float,
)
