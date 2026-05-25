package com.himanshoe.charty.common.brush

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private val DEFAULT_FILL_COLOR = Color(0xFF2196F3).copy(alpha = 0.15f)
private val DEFAULT_BORDER_COLOR = Color(0xFF2196F3).copy(alpha = 0.6f)
private const val EDGE_LINE_WIDTH = 2f

/**
 * Draws the active brush selection as a semi-transparent rectangle with left and right edge markers.
 *
 * Call this at the end of the chart's draw lambda so the overlay appears on top of all chart
 * content. Does nothing when no selection is active.
 *
 * @param state The current brush selection state.
 * @param chartLeft Pixel x-coordinate of the chart's left drawing boundary, used to clamp the selection.
 * @param chartTop Pixel y-coordinate of the chart's top drawing boundary.
 * @param chartRight Pixel x-coordinate of the chart's right drawing boundary.
 * @param chartBottom Pixel y-coordinate of the chart's bottom drawing boundary.
 * @param fillColor Background fill of the selection rectangle.
 * @param borderColor Color of the left and right edge lines.
 */
fun DrawScope.drawBrushSelection(
    state: BrushSelectionState,
    chartLeft: Float,
    chartTop: Float,
    chartRight: Float,
    chartBottom: Float,
    fillColor: Color = DEFAULT_FILL_COLOR,
    borderColor: Color = DEFAULT_BORDER_COLOR,
) {
    val (startPx, endPx) = state.pixelRange ?: return
    val left = startPx.coerceIn(chartLeft, chartRight)
    val right = endPx.coerceIn(chartLeft, chartRight)
    if (right <= left) return

    val top = chartTop
    val height = chartBottom - chartTop

    drawRect(
        color = fillColor,
        topLeft = Offset(left, top),
        size = Size(right - left, height),
    )
    drawLine(borderColor, Offset(left, top), Offset(left, top + height), EDGE_LINE_WIDTH)
    drawLine(borderColor, Offset(right, top), Offset(right, top + height), EDGE_LINE_WIDTH)
}
