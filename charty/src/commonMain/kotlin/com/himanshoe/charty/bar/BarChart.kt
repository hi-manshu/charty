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
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.config.Animation

/**
 * Bar Chart - Display data as vertical bars
 *
 * A bar chart presents categorical data with rectangular bars. The height of each bar
 * is proportional to the value it represents. Ideal for comparing discrete categories.
 *
 * Usage:
 * ```kotlin
 * BarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         roundedTopCorners = true,
 *         topCornerRadius = CornerRadius.Large,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for vertical gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val (minValue, maxValue) = remember(dataList, barConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }

    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

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

        // Draw bars
        dataList.fastForEachIndexed { index, bar ->
            val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)
            val barValueY = chartContext.convertValueToYPosition(bar.value)
            val isNegative = bar.value < 0f

            val barTop: Float
            val barHeight: Float

            if (isNegative) {
                barTop = baselineY
                val fullBarHeight = barValueY - baselineY
                barHeight = fullBarHeight * animationProgress.value
            } else {
                val fullBarHeight = baselineY - barValueY
                val animatedBarHeight = fullBarHeight * animationProgress.value
                barTop = baselineY - animatedBarHeight
                barHeight = animatedBarHeight
            }

            // Use per-bar color if available, otherwise fall back to chart color
            val barColor = bar.color ?: color
            val brush = with(chartContext) { barColor.toVerticalGradientBrush() }

            drawRoundedBar(
                brush = brush,
                x = barX,
                y = barTop,
                width = barWidth,
                height = barHeight,
                isNegative = isNegative,
                isBelowAxisMode = isBelowAxisMode,
                cornerRadius = barConfig.cornerRadius.value
            )
        }

        // Draw reference / target line on top of bars if configured
        barConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer
            )
        }
    }
}

/**
 * Helper function to draw a bar with rounded corners based on bar position
 */
private fun DrawScope.drawRoundedBar(
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
    drawPath(path, brush)
}
