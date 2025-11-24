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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation

/**
 * Span Chart - Display ranges/spans horizontally across categories
 *
 * A span chart (also known as a range chart or Gantt-style chart) displays horizontal bars
 * showing ranges or time periods for different categories. Each span has a start and end value,
 * making it ideal for visualizing schedules, timelines, or value ranges.
 *
 * Usage:
 * ```kotlin
 * SpanChart(
 *     data = {
 *         listOf(
 *             SpanData("Category 1", startValue = 5f, endValue = 15f),
 *             SpanData("Category 2", startValue = 10f, endValue = 25f),
 *             SpanData("Category 3", startValue = 3f, endValue = 18f)
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     ),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Medium
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of span data to display
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient recommended for distinguishing multiple spans
 * @param barConfig Configuration for span bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun SpanChart(
    data: () -> List<SpanData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Gradient(
        listOf(
            Color(0xFF2196F3),
            Color(0xFF4CAF50),
            Color(0xFFFF9800)
        )
    ),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Span chart data cannot be empty" }

    val (minValue, maxValue, colorList) = remember(dataList, colors) {
        val allValues = dataList.flatMap { listOf(it.startValue, it.endValue) }
        Triple(
            allValues.minOrNull() ?: 0f,
            allValues.maxOrNull() ?: 100f,
            colors.value
        )
    }

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
        xLabels = dataList.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = false
        ),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL
    ) { chartContext ->
        // Add offset so bars don't overlap with Y-axis line
        val axisOffset = if (scaffoldConfig.showAxis) scaffoldConfig.axisThickness * 20f else 0f

        val range = maxValue - minValue

        dataList.fastForEachIndexed { index, span ->
            // Use per-span color if available, otherwise fall back to chart colors
            val spanChartyColor = span.color ?: colors
            val spanColor = when (spanChartyColor) {
                is ChartyColor.Solid -> spanChartyColor.color
                is ChartyColor.Gradient -> spanChartyColor.colors[index % spanChartyColor.colors.size]
            }

            val barHeight = chartContext.height / dataList.size
            val barY = chartContext.top + (barHeight * index)
            val barThickness = barHeight * barConfig.barWidthFraction
            val centeredBarY = barY + (barHeight - barThickness) / 2

            val startNormalized = (span.startValue - minValue) / range
            val endNormalized = (span.endValue - minValue) / range

            val startX = chartContext.left + axisOffset + (startNormalized * (chartContext.width - axisOffset))
            val endX = chartContext.left + axisOffset + (endNormalized * (chartContext.width - axisOffset))

            val fullSpanWidth = endX - startX
            val animatedSpanWidth = fullSpanWidth * animationProgress.value

            val brush = Brush.horizontalGradient(
                colors = listOf(spanColor, spanColor),
                startX = startX,
                endX = endX
            )

            drawRoundedSpan(
                brush = brush,
                x = startX,
                y = centeredBarY,
                width = animatedSpanWidth,
                height = barThickness,
                cornerRadius = barConfig.cornerRadius.value
            )
        }
    }
}

/**
 * Helper function to draw a span (horizontal bar) with fully rounded corners
 */
private fun DrawScope.drawRoundedSpan(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float
) {
    val path = Path().apply {
        // Span with all corners rounded
        addRoundRect(
            RoundRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height,
                topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        )
    }
    drawPath(path, brush)
}

