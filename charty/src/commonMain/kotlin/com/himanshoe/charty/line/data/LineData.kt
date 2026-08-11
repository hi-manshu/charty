package com.himanshoe.charty.line.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.data.ChartDataPoint

/**
 * Data class representing a point in a line chart
 *
 * @property label The label for this point (displayed on X-axis)
 * @property value The Y-value of the point
 */
@Immutable
data class LineData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint
