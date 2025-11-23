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
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
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
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = data()
    require(dataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val values = dataList.getValues()
    val minValue = calculateMinValue(values)
    val maxValue = calculateMaxValue(values)

    // Determine if we're in BELOW_AXIS mode (axis centered at zero when mixed values)
    val isBelowAxisMode = (barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS)

    // Animation
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
        dataList.fastForEachIndexed { index, bar ->
            // Calculate bar X position using ChartContext helper with custom width fraction
            val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)

            // Calculate bar width using ChartContext helper with custom width fraction
            val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)

            // Convert bar value to Y coordinate
            val barValueY = chartContext.convertValueToYPosition(bar.value)

            // Determine if bar is positive or negative
            val isNegative = bar.value < 0f

            // Calculate bar position and height based on whether it's positive or negative
            val barTop: Float
            val barHeight: Float

            if (isNegative) {
                // For negative values: bar starts at baseline (zero line) and extends downward
                barTop = baselineY
                val fullBarHeight = barValueY - baselineY
                barHeight = fullBarHeight * animationProgress.value
            } else {
                // For positive values: bar starts at value and extends down to baseline
                val fullBarHeight = baselineY - barValueY
                val animatedBarHeight = fullBarHeight * animationProgress.value
                barTop = baselineY - animatedBarHeight
                barHeight = animatedBarHeight
            }

            // Convert ChartyColor to Brush for gradient support
            val brush = with(chartContext) { color.toVerticalGradientBrush() }

            // Always draw bars with rounded corners (automatic)
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
    }
}

/**
 * Helper function to draw a bar with rounded corners
 * Bars above the axis line get top corners rounded
 * Bars below the axis line get bottom corners rounded
 * Corner radius is configurable via BarChartConfig
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
    drawPath(path, brush)
}
