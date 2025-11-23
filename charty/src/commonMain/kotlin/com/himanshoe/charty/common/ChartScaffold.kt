package com.himanshoe.charty.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

/**
 * Context object passed to chart drawing lambdas.
 * Provides all necessary positioning and conversion functions.
 */
data class ChartContext(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val width: Float,
    val height: Float,
    val minValue: Float,
    val maxValue: Float
) {
    /**
     * Converts a data value to its corresponding Y-axis pixel coordinate
     *
     * @param value The data value to convert
     * @return The Y coordinate in pixels (top to bottom)
     */
    fun convertValueToYPosition(value: Float): Float {
        val range = maxValue - minValue
        if (range == 0f) return bottom
        val normalized = (value - minValue) / range
        return bottom - (normalized * height)
    }

    /**
     * Calculates the left X position for a bar at the given index
     * Centers the bar within its allocated space
     *
     * @param index The index of the bar (0-based)
     * @param totalBars Total number of bars in the chart
     * @param barWidthFraction Fraction of available space the bar should occupy (0.0 to 1.0)
     * @return The X coordinate for the bar's left edge
     */
    fun calculateBarLeftPosition(index: Int, totalBars: Int, barWidthFraction: Float = 0.6f): Float {
        val sectionWidth = width / totalBars
        val barWidth = sectionWidth * barWidthFraction
        return left + (sectionWidth * index) + (sectionWidth - barWidth) / 2
    }

    /**
     * Calculates the width for bars in the chart
     *
     * @param totalBars Total number of bars
     * @param widthFraction Fraction of available space each bar should occupy (0.0 to 1.0)
     * @return The width in pixels
     */
    fun calculateBarWidth(totalBars: Int, widthFraction: Float = 0.6f): Float {
        return (width / totalBars) * widthFraction
    }

    /**
     * Calculates the centered X position for an item at the given index
     * Useful for points, labels, and other centered elements
     *
     * @param index The index of the item (0-based)
     * @param totalItems Total number of items
     * @return The centered X coordinate
     */
    fun calculateCenteredXPosition(index: Int, totalItems: Int): Float {
        return left + (width * (index + 0.5f) / totalItems)
    }

    /**
     * Converts ChartyColor to a vertical gradient Brush
     *
     * @return Brush for drawing with gradient support
     */
    fun ChartyColor.toVerticalGradientBrush(): Brush {
        return when (this) {
            is ChartyColor.Solid -> Brush.verticalGradient(
                colors = listOf(color, color),
                startY = top,
                endY = bottom
            )
            is ChartyColor.Gradient -> Brush.verticalGradient(
                colors = colors,
                startY = top,
                endY = bottom
            )
        }
    }
}

/**
 * ChartScaffold - Clean and simple chart scaffold with axis and labels.
 *
 * All positioning is handled internally via ChartContext.
 */
@Composable
fun ChartScaffold(
    modifier: Modifier = Modifier,
    xLabels: List<String> = emptyList(),
    yAxisConfig: AxisConfig = AxisConfig(),
    config: ChartScaffoldConfig = ChartScaffoldConfig(),
    content: DrawScope.(ChartContext) -> Unit
) {
    Box(modifier = modifier) {
        DrawAxisAndLabels(
            xLabels = xLabels,
            yAxisConfig = yAxisConfig,
            config = config
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Calculate padding based on label visibility
            val leftPadding = if (config.showLabels) 60f else 20f
            val rightPadding = 20f
            val topPadding = 20f
            val bottomPadding = if (config.showLabels && xLabels.isNotEmpty()) 50f else 20f

            val chartContext = ChartContext(
                left = leftPadding,
                top = topPadding,
                right = size.width - rightPadding,
                bottom = size.height - bottomPadding,
                width = size.width - leftPadding - rightPadding,
                height = size.height - topPadding - bottomPadding,
                minValue = yAxisConfig.minValue,
                maxValue = yAxisConfig.maxValue
            )

            content(chartContext)
        }
    }
}

/**
 * Draws axis lines, grid lines, and labels.
 * Separated for clarity.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun DrawAxisAndLabels(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = config.labelColor, fontSize = 12.sp)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Calculate chart area
        val leftPadding = if (config.showLabels) 60f else 20f
        val rightPadding = 20f
        val topPadding = 20f
        val bottomPadding = if (config.showLabels && xLabels.isNotEmpty()) 50f else 20f

        val chartLeft = leftPadding
        val chartTop = topPadding
        val chartRight = size.width - rightPadding
        val chartBottom = size.height - bottomPadding
        val chartHeight = chartBottom - chartTop

        // Draw Y-axis line
        if (config.showAxis) {
            drawLine(
                color = config.axisColor,
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = config.axisThickness
            )
        }

        // Draw X-axis line (at zero position if configured and there are negative values)
        if (config.showAxis) {
            val xAxisPosition = if (yAxisConfig.minValue < 0f && yAxisConfig.maxValue > 0f && yAxisConfig.drawAxisAtZero) {
                // Position at zero when we have both positive and negative values and drawAxisAtZero is true
                val range = yAxisConfig.maxValue - yAxisConfig.minValue
                val zeroNormalized = (0f - yAxisConfig.minValue) / range
                chartBottom - (zeroNormalized * chartHeight)
            } else {
                // Otherwise place the X axis at the bottom (min value)
                chartBottom
            }

            drawLine(
                color = config.axisColor,
                start = Offset(chartLeft, xAxisPosition),
                end = Offset(chartRight, xAxisPosition),
                strokeWidth = config.axisThickness
            )
        }

        // Draw Y-axis grid and labels
        val steps = yAxisConfig.steps.coerceAtLeast(2)
        for (i in 0..steps) {
            val value = yAxisConfig.minValue +
                (yAxisConfig.maxValue - yAxisConfig.minValue) * (i.toFloat() / steps)
            val normalized = (value - yAxisConfig.minValue) / (yAxisConfig.maxValue - yAxisConfig.minValue)
            val y = chartBottom - (normalized * chartHeight)

            // Grid line
            if (config.showGrid && i > 0 && i < steps) {
                drawLine(
                    color = config.gridColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = config.gridThickness
                )
            }

            // Y-axis label
            if (config.showLabels) {
                val labelText = if (value % 1 == 0f) value.toInt().toString() else value.toString()
                val textLayout = textMeasurer.measure(AnnotatedString(labelText), labelStyle)

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        chartLeft - textLayout.size.width - 10f,
                        y - textLayout.size.height / 2
                    )
                )
            }
        }

        // Draw X-axis labels (centered under each bar position)
        if (config.showLabels && xLabels.isNotEmpty()) {
            xLabels.forEachIndexed { index, label ->
                // Center label under the bar position
                val centerX = chartLeft + (chartRight - chartLeft) * (index + 0.5f) / xLabels.size
                val textLayout = textMeasurer.measure(AnnotatedString(label), labelStyle)

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        centerX - textLayout.size.width / 2,
                        chartBottom + 10f
                    )
                )
            }
        }
    }
}
