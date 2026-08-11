package com.himanshoe.charty.point.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.data.ChartDataPoint

/**
 * Data class representing a single point in a point/scatter chart
 *
 * @property label The label for this point (displayed on X-axis)
 * @property value The Y-value of the point
 */
@Immutable
data class PointData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint
