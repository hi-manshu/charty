@file:Suppress("LongMethod", "LongParameterList", "FunctionNaming", "CyclomaticComplexMethod", "WildcardImport", "MagicNumber", "MaxLineLength", "ReturnCount", "UnusedImports")

package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.config.Animation

/**
 * Horizontal Bar Chart - Display data as horizontal bars
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars.
 * The length of each bar is proportional to the value it represents.
 * Ideal for comparing categories with long labels or when you have many categories.
 *
 * Usage:
 * ```kotlin
 * HorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarData("Category A", 100f),
 *             BarData("Category B", 150f),
 *             BarData("Category C", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Large
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for horizontal gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Horizontal bar chart data cannot be empty" }

    val (minValue, maxValue) = remember(dataList, barConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }

    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = remember {
        Animatable(if (barConfig.animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(barConfig.animation) {
        if (barConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = barConfig.animation.duration)
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = drawAxisAtZero
        ),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL
    ) { chartContext ->
        val axisOffset = if (scaffoldConfig.showAxis) scaffoldConfig.axisThickness * 20f else 0f

        val baselineX = if (drawAxisAtZero) {
            val range = maxValue - minValue
            val zeroNormalized = (0f - minValue) / range
            chartContext.left + (zeroNormalized * chartContext.width)
        } else {
            chartContext.left + axisOffset
        }

        dataList.fastForEachIndexed { index, bar ->
            val barHeight = chartContext.height / dataList.size
            val barY = chartContext.top + (barHeight * index)
            val barThickness = barHeight * barConfig.barWidthFraction
            val centeredBarY = barY + (barHeight - barThickness) / 2

            val range = maxValue - minValue
            val valueNormalized = (bar.value - minValue) / range
            val barValueX = chartContext.left + axisOffset + (valueNormalized * (chartContext.width - axisOffset))
            val isNegative = bar.value < 0f

            val barLeft: Float
            val barWidth: Float

            if (isNegative && isBelowAxisMode) {
                // For negative values in BELOW_AXIS mode: bar extends from value to baseline (zero line)
                val fullBarWidth = baselineX - barValueX
                barWidth = fullBarWidth * animationProgress.value
                barLeft = barValueX
            } else {
                val fullBarWidth = barValueX - baselineX
                barWidth = fullBarWidth * animationProgress.value
                barLeft = baselineX
            }

            val barColor = bar.color ?: color
            val brush = when (barColor) {
                is ChartyColor.Solid -> androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(barColor.color, barColor.color),
                    startX = chartContext.left,
                    endX = chartContext.right
                )
                is ChartyColor.Gradient -> androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = barColor.colors,
                    startX = chartContext.left,
                    endX = chartContext.right
                )
            }

            drawRoundedHorizontalBar(
                brush = brush,
                x = barLeft,
                y = centeredBarY,
                width = barWidth,
                height = barThickness,
                isNegative = isNegative,
                isBelowAxisMode = isBelowAxisMode,
                cornerRadius = barConfig.cornerRadius.value
            )
        }

        // Draw reference / target line if configured
        barConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.HORIZONTAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer
            )
        }
    }
}

/**
 * Helper function to draw a horizontal bar with rounded corners
 */
private fun DrawScope.drawRoundedHorizontalBar(
    brush: androidx.compose.ui.graphics.Brush,
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
            // Negative bar extending left: round left corners
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomRightCornerRadius = CornerRadius.Zero
                )
            )
        } else {
            // Positive bar extending right: round right corners
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeftCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
    }

    drawPath(path, brush)
}
