package com.himanshoe.charty.common

import androidx.compose.ui.graphics.Brush
import com.himanshoe.charty.color.ChartyColor

private const val CENTER_OFFSET = 0.5f
private const val DEFAULT_BAR_WIDTH_FRACTION = 0.6f
private const val CENTER_DIVISOR = 2f
private const val ZERO_RANGE = 0f
private const val EMPTY_EXTENT = 0f

/**
 * Provides a context for drawing within a chart, encapsulating the dimensions and value range.
 * This class offers utility functions to convert data values into pixel coordinates and calculate positions for chart elements.
 *
 * @property left The starting x-coordinate of the drawing area.
 * @property top The starting y-coordinate of the drawing area.
 * @property right The ending x-coordinate of the drawing area.
 * @property bottom The ending y-coordinate of the drawing area.
 * @property minValue The minimum value in the dataset, corresponding to the bottom of the chart.
 * @property maxValue The maximum value in the dataset, corresponding to the top of the chart.
 * @property streaming When non-null, the chart is in rolling-window streaming mode and category
 *   positions are placed by this sliding [StreamingLayout] instead of by evenly dividing the plot
 *   among all items; `null` (the default) is the ordinary static layout.
 */
data class ChartContext(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val minValue: Float,
    val maxValue: Float,
    val streaming: StreamingLayout? = null,
) {
    /** The total width of the drawing area, derived from [right] – [left]. */
    val width: Float get() = right - left

    /** The total height of the drawing area, derived from [bottom] – [top]. */
    val height: Float get() = bottom - top

    /**
     * Converts a data value to its corresponding y-coordinate on the canvas, using this context's
     * own `[minValue, maxValue]` range. A degenerate range (`minValue == maxValue`, which an
     * all-equal series produces) collapses every value onto [bottom] rather than dividing by zero.
     *
     * @param value The data value to be converted.
     * @return The y-coordinate on the canvas.
     */
    fun convertValueToYPosition(value: Float): Float =
        convertValueToYPosition(value = value, rangeMin = minValue, rangeMax = maxValue)

    /**
     * Converts a data value to its y-coordinate using an explicit `[rangeMin, rangeMax]` range
     * instead of the context's own range. Used to plot a series against a **secondary Y axis** (a
     * different scale) in dual-axis charts.
     *
     * A degenerate range (`rangeMin == rangeMax`) returns [bottom], so a flat series draws on the
     * baseline instead of leaking a NaN or an infinity into the draw call.
     *
     * @param value The data value to be converted.
     * @param rangeMin The minimum of the axis this value belongs to.
     * @param rangeMax The maximum of the axis this value belongs to.
     * @return The y-coordinate on the canvas.
     */
    fun convertValueToYPosition(
        value: Float,
        rangeMin: Float,
        rangeMax: Float,
    ): Float {
        val range = rangeMax - rangeMin
        if (range == ZERO_RANGE) {
            return bottom
        }
        val normalized = (value - rangeMin) / range
        return bottom - (normalized * height)
    }

    /**
     * Width of one category slot along the horizontal axis: the plot divided evenly among
     * [totalItems], or the rolling window's slot width when [streaming] is active. A non-positive
     * [totalItems] describes no slots at all and yields `0f` instead of an infinity.
     */
    private fun slotWidth(totalItems: Int): Float =
        streaming?.let { width * it.slotFraction }
            ?: if (totalItems > 0) {
                width / totalItems
            } else {
                EMPTY_EXTENT
            }

    /**
     * Calculates the x-coordinate for the left edge of a bar at a given index.
     *
     * @param index The index of the bar.
     * @param totalBars The total number of bars in the chart. A non-positive count describes no
     *   slots, so the plot's left edge is returned.
     * @param barWidthFraction The fraction of the available space that the bar should occupy.
     * @return The x-coordinate for the left edge of the bar.
     */
    fun calculateBarLeftPosition(
        index: Int,
        totalBars: Int,
        barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    ): Float {
        val sectionWidth = slotWidth(totalBars)
        val barWidth = sectionWidth * barWidthFraction
        val slotLeft = streaming?.let { left + width * it.startFraction(index) } ?: (left + sectionWidth * index)
        return slotLeft + (sectionWidth - barWidth) / CENTER_DIVISOR
    }

    /**
     * Calculates the width of a bar in the chart.
     *
     * @param totalBars The total number of bars in the chart. A non-positive count yields `0f`.
     * @param widthFraction The fraction of the available space that each bar should occupy.
     * @return The width of the bar in pixels.
     */
    fun calculateBarWidth(
        totalBars: Int,
        widthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    ): Float = slotWidth(totalBars) * widthFraction

    /**
     * Calculates the centered x-coordinate for an item at a given index.
     * This is useful for positioning points, labels, or other elements that need to be centered.
     *
     * @param index The index of the item.
     * @param totalItems The total number of items. A non-positive count describes no slots, so the
     *   plot's left edge is returned.
     * @return The centered x-coordinate for the item.
     */
    fun calculateCenteredXPosition(
        index: Int,
        totalItems: Int,
    ): Float =
        streaming?.let { left + width * it.centerFraction(index) }
            ?: if (totalItems > 0) {
                left + (width * (index + CENTER_OFFSET) / totalItems)
            } else {
                left
            }

    /**
     * Height of one category row for a horizontal chart. Mirrors [calculateBarWidth] on the vertical
     * (category) axis, honouring a rolling [streaming] window when one is active.
     *
     * @param totalItems The total number of category rows. A non-positive count yields `0f`.
     * @return The height of a single row in pixels.
     */
    fun calculateSlotHeight(totalItems: Int): Float =
        streaming?.let { height * it.slotFraction }
            ?: if (totalItems > 0) {
                height / totalItems
            } else {
                EMPTY_EXTENT
            }

    /**
     * Top y-coordinate of the category row at [index] for a horizontal chart. Mirrors
     * [calculateBarLeftPosition]'s slot origin on the vertical axis, honouring a rolling [streaming]
     * window when one is active.
     *
     * @param index The index of the row.
     * @param totalItems The total number of category rows. A non-positive count describes no rows,
     *   so the plot's top edge is returned.
     * @return The y-coordinate of the row's top edge in pixels.
     */
    fun calculateSlotTopPosition(
        index: Int,
        totalItems: Int,
    ): Float =
        streaming?.let { top + height * it.startFraction(index) }
            ?: (top + calculateSlotHeight(totalItems) * index)

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
