package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.toBrush
import com.himanshoe.charty.common.ChartContext

/**
 * Draws a translucent vertical band spanning the plot height, centred on [centerX] and [columnWidth]
 * wide, to highlight the column behind a selected point. Clamped to the chart's horizontal bounds.
 *
 * @param chartContext The chart's drawing context (supplies the plot bounds).
 * @param centerX The x position (in pixels) of the selected point.
 * @param columnWidth The band width in pixels; typically the per-point column width.
 * @param color The band fill (usually semi-transparent), a [ChartyColor] so solid or gradient works.
 */
fun DrawScope.drawSelectionColumn(
    chartContext: ChartContext,
    centerX: Float,
    columnWidth: Float,
    color: ChartyColor,
) {
    val half = columnWidth / 2f
    val left = (centerX - half).coerceAtLeast(chartContext.left)
    val right = (centerX + half).coerceAtMost(chartContext.right)
    if (right <= left) {
        return
    }
    drawRect(
        brush = color.toBrush(),
        topLeft = Offset(x = left, y = chartContext.top),
        size = Size(width = right - left, height = chartContext.height),
    )
}

/**
 * Draws a [drawSelectionColumn] only when [enabled] and a [selectedX] is present, so callers can wire
 * it in one line. No-op otherwise.
 */
fun DrawScope.drawSelectionColumnIfNeeded(
    enabled: Boolean,
    selectedX: Float?,
    columnWidth: Float,
    color: ChartyColor,
    chartContext: ChartContext,
) {
    if (!enabled || selectedX == null) {
        return
    }
    drawSelectionColumn(chartContext = chartContext, centerX = selectedX, columnWidth = columnWidth, color = color)
}
