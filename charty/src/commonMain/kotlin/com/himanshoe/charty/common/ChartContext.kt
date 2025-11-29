package com.himanshoe.charty.common

import androidx.compose.ui.graphics.Brush
import com.himanshoe.charty.color.ChartyColor

private const val CENTER_OFFSET = 0.5f
private const val DEFAULT_BAR_WIDTH_FRACTION = 0.6f
private const val CENTER_DIVISOR = 2f
private const val ZERO_RANGE = 0f

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
    val maxValue: Float,
) {
    /**
     * Converts a data value to its corresponding Y-axis pixel coordinate
     *
     * @param value The data value to convert
     * @return The Y coordinate in pixels (top to bottom)
     */
    fun convertValueToYPosition(value: Float): Float {
        val range = maxValue - minValue
        if (range == ZERO_RANGE) return bottom
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
    fun calculateBarLeftPosition(
        index: Int,
        totalBars: Int,
        barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    ): Float {
        val sectionWidth = width / totalBars
        val barWidth = sectionWidth * barWidthFraction
        return left + (sectionWidth * index) + (sectionWidth - barWidth) / CENTER_DIVISOR
    }

    /**
     * Calculates the width for bars in the chart
     *
     * @param totalBars Total number of bars
     * @param widthFraction Fraction of available space each bar should occupy (0.0 to 1.0)
     * @return The width in pixels
     */
    fun calculateBarWidth(
        totalBars: Int,
        widthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    ): Float = (width / totalBars) * widthFraction

    /**
     * Calculates the centered X position for an item at the given index
     * Useful for points, labels, and other centered elements
     *
     * @param index The index of the item (0-based)
     * @param totalItems Total number of items
     * @return The centered X coordinate
     */
    fun calculateCenteredXPosition(
        index: Int,
        totalItems: Int,
    ): Float = left + (width * (index + CENTER_OFFSET) / totalItems)

    /**
     * Converts ChartyColor to a vertical gradient Brush
     *
     * @return Brush for drawing with gradient support
     */
    fun ChartyColor.toVerticalGradientBrush(): Brush =
        Brush.verticalGradient(
            colors = value,
            startY = top,
            endY = bottom,
        )
}
