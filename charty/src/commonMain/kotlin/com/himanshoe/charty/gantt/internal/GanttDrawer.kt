package com.himanshoe.charty.gantt.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSegment
import com.himanshoe.charty.gantt.data.GanttSelection

private const val LABEL_PADDING = 6f
private const val HALF = 2f
private const val FULL_ALPHA = 1f

/**
 * Draws every row's segments along the value axis, dimming the unfinished tail of any segment that
 * reports progress, and writing a segment's label inside it when there is room.
 *
 * The default brush is built once per frame and reused by every segment; only a segment that
 * overrides its colour builds one of its own.
 *
 * @param params The frame's drawing inputs.
 */
internal fun DrawScope.drawGanttRows(params: GanttDrawParams) {
    val rowCount = params.rows.size
    if (rowCount == 0) {
        return
    }
    val defaultBrush =
        Brush.horizontalGradient(
            colors = params.color.value,
            startX = params.chartContext.left,
            endX = params.chartContext.right,
        )
    params.rows.fastForEachIndexed { rowIndex, row ->
        row.segments.fastForEachIndexed { segmentIndex, segment ->
            drawGanttSegment(
                params = params,
                row = row,
                rowIndex = rowIndex,
                segment = segment,
                brush =
                    segment.color?.let { color ->
                        Brush.horizontalGradient(
                            colors = color.value,
                            startX = params.chartContext.left,
                            endX = params.chartContext.right,
                        )
                    } ?: defaultBrush,
                label =
                    params.labels
                        ?.rows
                        ?.getOrNull(rowIndex)
                        ?.getOrNull(segmentIndex),
            )
        }
    }
}

private fun DrawScope.drawGanttSegment(
    params: GanttDrawParams,
    row: GanttRow,
    rowIndex: Int,
    segment: GanttSegment,
    brush: Brush,
    label: TextLayoutResult?,
) {
    val rect =
        ganttSegmentRect(
            chartContext = params.chartContext,
            index = rowIndex,
            rowCount = params.rows.size,
            segment = segment,
            minValue = params.minValue,
            maxValue = params.maxValue,
            barHeightFraction = params.config.barHeightFraction,
            animationProgress = params.animationProgress,
        )
    if (rect.width <= 0f) {
        return
    }
    val radius = CornerRadius(x = params.config.cornerRadius.value, y = params.config.cornerRadius.value)
    val baseAlpha =
        if (segment.progress == null) {
            FULL_ALPHA
        } else {
            params.config.incompleteAlpha
        }
    drawRoundRect(
        brush = brush,
        topLeft = Offset(x = rect.left, y = rect.top),
        size = Size(width = rect.width, height = rect.height),
        cornerRadius = radius,
        alpha = baseAlpha,
    )
    ganttProgressRect(rect = rect, progress = segment.progress)?.let { completed ->
        if (completed.width > 0f) {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x = completed.left, y = completed.top),
                size = Size(width = completed.width, height = completed.height),
                cornerRadius = radius,
            )
        }
    }
    if (params.recordBounds) {
        params.onSegmentBoundCalculated(rect to GanttSelection(row = row, segment = segment))
    }
    label?.let { measured -> drawGanttLabel(label = measured, rect = rect) }
}

private fun DrawScope.drawGanttLabel(
    label: TextLayoutResult,
    rect: Rect,
) {
    if (label.size.width + LABEL_PADDING * HALF > rect.width) {
        return
    }
    drawText(
        textLayoutResult = label,
        topLeft =
            Offset(
                x = rect.left + LABEL_PADDING,
                y = rect.top + (rect.height - label.size.height) / HALF,
            ),
    )
}
