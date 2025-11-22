package com.himanshoe.charty.common

/**
 * Configuration for chart axis.
 *
 * @param minValue Minimum value on the axis
 * @param maxValue Maximum value on the axis
 * @param steps Number of steps/divisions on the axis
 * @param label Label for the axis (e.g., "Sales", "Revenue")
 */
data class AxisConfig(
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val steps: Int = 5,
    val label: String = ""
)

/**
 * Configuration for chart scaffold styling.
 *
 * @param showAxis Whether to show axis lines
 * @param showGrid Whether to show grid lines
 * @param showLabels Whether to show axis labels
 * @param axisColor Color of the axis lines
 * @param gridColor Color of the grid lines
 * @param labelColor Color of the labels
 * @param axisThickness Thickness of axis lines
 * @param gridThickness Thickness of grid lines
 */
data class ChartScaffoldConfig(
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val axisColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    val gridColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.LightGray,
    val labelColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f
)

