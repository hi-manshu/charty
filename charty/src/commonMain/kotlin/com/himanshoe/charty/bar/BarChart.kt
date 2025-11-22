package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.bar.config.BarChartConfig
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

    val maxValue = calculateMaxValue(dataList.getValues())

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
            minValue = 0f,
            maxValue = maxValue,
            steps = 6
        ),
        config = scaffoldConfig
    ) { chartContext ->
        // Use fastForEachIndexed for better performance
        dataList.fastForEachIndexed { index, bar ->
            // Calculate bar X position using ChartContext helper with custom width fraction
            val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)

            // Calculate bar width using ChartContext helper with custom width fraction
            val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)

            // Convert bar value to Y coordinate
            val barTop = chartContext.convertValueToYPosition(bar.value)

            // Calculate bar height from top to bottom with animation
            val fullBarHeight = chartContext.bottom - barTop
            val animatedBarHeight = fullBarHeight * animationProgress.value
            val animatedBarTop = chartContext.bottom - animatedBarHeight

            // Convert ChartyColor to Brush for gradient support
            val brush = with(chartContext) { color.toVerticalGradientBrush() }

            // Draw the bar with optional rounded top corners
            if (barConfig.roundedTopCorners) {
                drawRoundedTopBar(
                    brush = brush,
                    x = barX,
                    y = animatedBarTop,
                    width = barWidth,
                    height = animatedBarHeight,
                    cornerRadius = barConfig.topCornerRadius.value
                )
            } else {
                drawRect(
                    brush = brush,
                    topLeft = Offset(barX, animatedBarTop),
                    size = Size(barWidth, animatedBarHeight)
                )
            }
        }
    }
}

/**
 * Helper function to draw a bar with rounded top corners
 */
private fun DrawScope.drawRoundedTopBar(
    brush: androidx.compose.ui.graphics.Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float
) {
    val path = Path().apply {
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
    drawPath(path, brush)
}
