package com.himanshoe.charty.common.brush

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.toBrush

private const val SELECTION_ARGB = 0xFF2196F3
private const val FILL_ALPHA = 0.15f
private const val BORDER_ALPHA = 0.6f
private const val EDGE_LINE_WIDTH = 2f

private val DEFAULT_FILL = ChartyColor.Solid(Color(SELECTION_ARGB).copy(alpha = FILL_ALPHA))
private val DEFAULT_BORDER = ChartyColor.Solid(Color(SELECTION_ARGB).copy(alpha = BORDER_ALPHA))

/**
 * How the active brush selection is painted: the translucent rectangle over the selected range and
 * the two edge markers that bound it. Build one from the ambient theme with
 * [com.himanshoe.charty.common.theme.ChartyThemeDefaults.brushSelectionStyle].
 *
 * @property fill Background fill of the selection rectangle, solid or gradient.
 * @property border Color of the left and right edge lines, solid or gradient.
 * @property edgeWidth Stroke width of the edge lines, in pixels.
 */
@Immutable
data class BrushSelectionStyle(
    val fill: ChartyColor = DEFAULT_FILL,
    val border: ChartyColor = DEFAULT_BORDER,
    val edgeWidth: Float = EDGE_LINE_WIDTH,
) {
    init {
        require(edgeWidth >= 0f) { "Brush selection edgeWidth must be non-negative" }
    }
}

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
 * @param style How the rectangle and its edge markers are painted.
 */
fun DrawScope.drawBrushSelection(
    state: BrushSelectionState,
    chartLeft: Float,
    chartTop: Float,
    chartRight: Float,
    chartBottom: Float,
    style: BrushSelectionStyle = BrushSelectionStyle(),
) {
    val (startPx, endPx) = state.pixelRange ?: return
    val left = startPx.coerceIn(chartLeft, chartRight)
    val right = endPx.coerceIn(chartLeft, chartRight)
    if (right <= left) {
        return
    }

    val top = chartTop
    val height = chartBottom - chartTop
    val borderBrush = style.border.toBrush()

    drawRect(
        brush = style.fill.toBrush(),
        topLeft = Offset(left, top),
        size = Size(right - left, height),
    )
    drawLine(
        brush = borderBrush,
        start = Offset(left, top),
        end = Offset(left, top + height),
        strokeWidth = style.edgeWidth,
    )
    drawLine(
        brush = borderBrush,
        start = Offset(right, top),
        end = Offset(right, top + height),
        strokeWidth = style.edgeWidth,
    )
}
