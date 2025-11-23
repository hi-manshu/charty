package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getAllValues
import com.himanshoe.charty.bar.ext.getLabels
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
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Comparison bar chart data cannot be empty" }

    val (minValue, maxValue, colorList) = remember(dataList, colors) {
        val allValues = dataList.getAllValues()
        Triple(
            calculateMinValue(allValues),
            calculateMaxValue(allValues),
            colors.value
        )
    }

    val isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

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
        val baselineY = if (minValue < 0f && isBelowAxisMode) {
            chartContext.convertValueToYPosition(0f)
        } else {
            chartContext.bottom
        }

        dataList.fastForEachIndexed { groupIndex, group ->
            val groupWidth = chartContext.width / dataList.size
            val barWidth = groupWidth / group.values.size * 0.8f

            group.values.fastForEachIndexed { barIndex, value ->
                val barX = chartContext.left +
                        groupWidth * groupIndex +
                        barWidth * barIndex +
                        groupWidth * 0.1f

                val barValueY = chartContext.convertValueToYPosition(value)
                val isNegative = value < 0f

                val barTop: Float
                val barHeight: Float

                if (isNegative) {
                    barTop = baselineY
                    barHeight = barValueY - baselineY
                } else {
                    barHeight = baselineY - barValueY
                    barTop = baselineY - barHeight
                }

                val barColor = colorList[barIndex % colorList.size]

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
        if (isNegative && isBelowAxisMode) {
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
