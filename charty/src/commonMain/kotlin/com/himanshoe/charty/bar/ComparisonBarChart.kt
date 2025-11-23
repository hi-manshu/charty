package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig

/**
 * Comparison Bar Chart - Display multiple bars per category for comparison
 *
 * A comparison bar chart displays multiple data series side-by-side for each category.
 * Perfect for comparing sub-categories or multiple metrics within each main category.
 * Formerly known as Grouped Bar Chart.
 *
 * Usage:
 * ```kotlin
 * ComparisonBarChart(
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
 * @param comparisonConfig Configuration for comparison chart behavior (e.g., negative values draw mode)
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun ComparisonBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Gradient(
        listOf(
            Color(0xFFE91E63),
            Color(0xFF2196F3)
        )
    ),
    comparisonConfig: ComparisonBarChartConfig = ComparisonBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = data()
    require(dataList.isNotEmpty()) { "Comparison bar chart data cannot be empty" }

    val allValues = dataList.getAllValues()
    val minValue = calculateMinValue(allValues)
    val maxValue = calculateMaxValue(allValues)
    val colorList = colors.value

    // Determine if we're in BELOW_AXIS mode (axis centered at zero when mixed values)
    val isBelowAxisMode = (comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS)

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            // When using FROM_MIN_VALUE mode, always draw axis at bottom (not centered at zero)
            drawAxisAtZero = isBelowAxisMode
        ),
        config = scaffoldConfig
    ) { chartContext ->
        // Determine baseline position based on configuration:
        // - BELOW_AXIS: baseline at zero line when there are negative values, otherwise at chart bottom
        // - FROM_MIN_VALUE: baseline always at chart bottom (starts from minimum value)
        val baselineY = if (minValue < 0f && isBelowAxisMode) {
            chartContext.convertValueToYPosition(0f)
        } else {
            chartContext.bottom
        }

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
                val barValueY = chartContext.convertValueToYPosition(value)

                // Determine if bar is positive or negative
                val isNegative = value < 0f

                // Calculate bar position and height based on whether it's positive or negative
                val barTop: Float
                val barHeight: Float

                if (isNegative) {
                    // For negative values: bar starts at baseline (zero line) and extends downward
                    barTop = baselineY
                    barHeight = barValueY - baselineY
                } else {
                    // For positive values: bar starts at value and extends down to baseline
                    barHeight = baselineY - barValueY
                    barTop = baselineY - barHeight
                }

                // Get color for this bar from the color list
                val barColor = colorList[barIndex % colorList.size]

                // Always draw bars with rounded corners (automatic)
                drawRoundedBar(
                    color = barColor,
                    x = barX,
                    y = barTop,
                    width = barWidth,
                    height = barHeight,
                    isNegative = isNegative,
                    isBelowAxisMode = isBelowAxisMode,
                    cornerRadius = comparisonConfig.cornerRadius.value
                )
            }
        }
    }
}

/**
 * Helper function to draw a comparison bar with rounded corners
 * Bars above the axis line get top corners rounded
 * Bars below the axis line get bottom corners rounded
 * Corner radius is configurable via ComparisonBarChartConfig
 */
private fun DrawScope.drawRoundedBar(
    color: Color,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float
) {
    val path = Path().apply {
        // Determine which corners to round based on bar position relative to axis
        // If bar extends below axis (negative), round bottom corners
        // If bar extends above axis (positive or when axis is at bottom), round top corners
        if (isNegative && isBelowAxisMode) {
            // Negative bar extending below the axis line: round bottom corners
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        } else {
            // Positive bar or bar extending above axis: round top corners
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeftCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius.Zero
                )
            )
        }
    }
    drawPath(path, color)
}

/**
 * @deprecated Use ComparisonBarChart instead. GroupedBarChart has been renamed to ComparisonBarChart.
 */
@Deprecated(
    message = "GroupedBarChart has been renamed to ComparisonBarChart",
    replaceWith = ReplaceWith("ComparisonBarChart(data, modifier, colors, ComparisonBarChartConfig(), scaffoldConfig)"),
    level = DeprecationLevel.WARNING
)
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
    ComparisonBarChart(
        data = data,
        modifier = modifier,
        colors = colors,
        comparisonConfig = ComparisonBarChartConfig(),
        scaffoldConfig = scaffoldConfig
    )
}
