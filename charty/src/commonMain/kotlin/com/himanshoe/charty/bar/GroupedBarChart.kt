package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig

/**
 * Grouped Bar Chart - Display multiple bars per category
 *
 * A grouped bar chart displays multiple data series side-by-side for each category.
 * Perfect for comparing sub-categories or multiple metrics within each main category.
 *
 * Usage:
 * ```kotlin
 * GroupedBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(45f, 52f)),
 *             BarGroup("Q2", listOf(58f, 63f)),
 *             BarGroup("Q3", listOf(72f, 68f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFFE91E63), Color(0xFF2196F3))
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups, each containing multiple values
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient recommended for distinguishing bars in each group
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun GroupedBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Gradient(
        listOf(
            Color(0xFFE91E63),
            Color(0xFF2196F3)
        )
    ),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = data()
    require(dataList.isNotEmpty()) { "Grouped bar chart data cannot be empty" }

    val maxValue = calculateMaxValue(dataList.getAllValues())
    val colorList = colors.value

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = 0f,
            maxValue = maxValue,
            steps = 6
        ),
        config = scaffoldConfig
    ) { chartContext ->
        // Use fastForEachIndexed for better performance
        dataList.fastForEachIndexed { groupIndex, group ->
            // Calculate width allocated for this group
            val groupWidth = chartContext.width / dataList.size

            // Calculate individual bar width within the group
            val barWidth = groupWidth / group.values.size * 0.8f

            // Use fastForEachIndexed for inner loop as well
            group.values.fastForEachIndexed { barIndex, value ->
                // Calculate X position for this bar within its group
                val barX = chartContext.left +
                        groupWidth * groupIndex +
                        barWidth * barIndex +
                        groupWidth * 0.1f

                // Convert value to Y coordinate
                val barTop = chartContext.convertValueToYPosition(value)

                // Calculate bar height
                val barHeight = chartContext.bottom - barTop

                // Get color for this bar from the color list
                val barColor = colorList[barIndex % colorList.size]

                // Draw the bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(barX, barTop),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}

