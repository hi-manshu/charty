package com.himanshoe.charty.common

import androidx.compose.ui.graphics.Brush
import com.himanshoe.charty.color.ChartyColor

private const val CENTER_OFFSET = 0.5f
private const val DEFAULT_BAR_WIDTH_FRACTION = 0.6f
private const val CENTER_DIVISOR = 2f
private const val ZERO_RANGE = 0f

/**
 * Provides a context for drawing within a chart, encapsulating the dimensions and value range.
 * This class offers utility functions to convert data values into pixel coordinates and calculate positions for chart elements.
 *
 * @property left The starting x-coordinate of the drawing area.
 * @property top The starting y-coordinate of the drawing area.
 * @property right The ending x-coordinate of the drawing area.
 * @property bottom The ending y-coordinate of the drawing area.
 * @property width The total width of the drawing area.
 * @property height The total height of the drawing area.
 * @property minValue The minimum value in the dataset, corresponding to the bottom of the chart.
 * @property maxValue The maximum value in the dataset, corresponding to the top of the chart.
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
     * Converts a data value to its corresponding y-coordinate on the canvas.
     *
     * @param value The data value to be converted.
     * @return The y-coordinate on the canvas.
     */
    fun convertValueToYPosition(value: Float): Float {
        val range = maxValue - minValue
        if (range == ZERO_RANGE) return bottom
        val normalized = (value - minValue) / range
        return bottom - (normalized * height)
    }

    /**
     * Calculates the x-coordinate for the left edge of a bar at a given index.
     *
     * @param index The index of the bar.
     * @param totalBars The total number of bars in the chart.
     * @param barWidthFraction The fraction of the available space that the bar should occupy.
     * @return The x-coordinate for the left edge of the bar.
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
     * Calculates the width of a bar in the chart.
     *
     * @param totalBars The total number of bars in the chart.
     * @param widthFraction The fraction of the available space that each bar should occupy.
     * @return The width of the bar in pixels.
     */
    fun calculateBarWidth(
        totalBars: Int,
        widthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    ): Float = (width / totalBars) * widthFraction

    /**
     * Calculates the centered x-coordinate for an item at a given index.
     * This is useful for positioning points, labels, or other elements that need to be centered.
     *
     * @param index The index of the item.
     * @param totalItems The total number of items.
     * @return The centered x-coordinate for the item.
     */
    fun calculateCenteredXPosition(
        index: Int,
        totalItems: Int,
    ): Float = left + (width * (index + CENTER_OFFSET) / totalItems)

    /**
     * Converts a [ChartyColor] into a vertical gradient [Brush].
     *
     * @return A [Brush] that can be used for drawing gradients.
     */
    fun ChartyColor.toVerticalGradientBrush(): Brush =
        Brush.verticalGradient(
            colors = value,
            startY = top,
            endY = bottom,
        )
}
