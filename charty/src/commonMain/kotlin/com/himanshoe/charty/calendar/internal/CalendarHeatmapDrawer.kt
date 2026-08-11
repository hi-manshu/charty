package com.himanshoe.charty.calendar.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.CellShape
import com.himanshoe.charty.calendar.data.CalendarData

private const val DAY_LABEL_END_GAP = 4f

/**
 * Draws the month abbreviation labels along the top of the calendar grid.
 *
 * Labels are pre-measured in the composable and passed in to keep this function allocation-free
 * during animation frames.
 *
 * @param monthBoundaries Pairs of (weekIndex, label) from [GridLayout.monthBoundaries].
 * @param measuredLabels Pre-measured [TextLayoutResult] keyed by month label string.
 * @param leftPadding Horizontal offset (px) reserved for day-of-week labels.
 * @param topPadding Vertical offset (px) reserved for month labels.
 * @param cellStridePx Distance (px) between the left edges of adjacent week columns.
 */
internal fun DrawScope.drawMonthLabels(
    monthBoundaries: List<Pair<Int, String>>,
    measuredLabels: Map<String, TextLayoutResult>,
    leftPadding: Float,
    topPadding: Float,
    cellStridePx: Float,
) {
    for ((weekIndex, label) in monthBoundaries) {
        val measured = measuredLabels[label] ?: continue
        val x = leftPadding + weekIndex * cellStridePx
        val y = (topPadding - measured.size.height) / 2f
        drawText(textLayoutResult = measured, topLeft = Offset(x, y.coerceAtLeast(0f)))
    }
}

/**
 * Draws Mon, Wed, and Fri labels on the left side of the calendar grid (GitHub-style).
 *
 * Labels are pre-measured in the composable and passed in to keep this function allocation-free
 * during animation frames.
 *
 * @param dayLabelRows Pre-measured day labels as (dayIndex, [TextLayoutResult]) pairs.
 * @param topPadding Vertical offset (px) reserved for month labels.
 * @param leftPadding Horizontal space (px) reserved for these labels.
 * @param cellStridePx Distance (px) between the top edges of adjacent day rows.
 */
internal fun DrawScope.drawDayLabels(
    dayLabelRows: List<Pair<Int, TextLayoutResult>>,
    topPadding: Float,
    leftPadding: Float,
    cellStridePx: Float,
) {
    for ((dayIndex, measured) in dayLabelRows) {
        val x = leftPadding - measured.size.width - DAY_LABEL_END_GAP
        val y = topPadding + dayIndex * cellStridePx + (cellStridePx - measured.size.height) / 2f
        drawText(textLayoutResult = measured, topLeft = Offset(x.coerceAtLeast(0f), y))
    }
}

/**
 * Draws a single calendar cell using the given [shape].
 */
internal fun DrawScope.drawCell(
    topLeft: Offset,
    cellSizePx: Float,
    color: Color,
    alpha: Float,
    shape: CellShape,
) {
    when (shape) {
        is CellShape.Square ->
            drawRect(
                color = color,
                topLeft = topLeft,
                size = Size(cellSizePx, cellSizePx),
                alpha = alpha,
            )

        is CellShape.RoundedSquare ->
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = Size(cellSizePx, cellSizePx),
                cornerRadius = CornerRadius(shape.cornerRadius),
                alpha = alpha,
            )

        is CellShape.Circle ->
            drawCircle(
                color = color,
                radius = cellSizePx / 2f,
                center = Offset(topLeft.x + cellSizePx / 2f, topLeft.y + cellSizePx / 2f),
                alpha = alpha,
            )

        is CellShape.Diamond -> {
            val cx = topLeft.x + cellSizePx / 2f
            val cy = topLeft.y + cellSizePx / 2f
            val path =
                Path().apply {
                    moveTo(cx, topLeft.y)
                    lineTo(topLeft.x + cellSizePx, cy)
                    lineTo(cx, topLeft.y + cellSizePx)
                    lineTo(topLeft.x, cy)
                    close()
                }
            drawPath(path = path, color = color, alpha = alpha)
        }
    }
}

/**
 * Draws the complete cell grid — both empty background cells and data cells — applying a
 * left-to-right sweep reveal animation driven by [animationProgress].
 *
 * Uses [GridLayout.cellMap] for O(1) per-cell data lookup, avoiding any allocations during
 * the draw phase.
 *
 * Data cells that have become visible are appended to [cellBoundsOutput] for tap hit-testing.
 *
 * @param gridLayout The computed grid layout.
 * @param config The chart configuration.
 * @param maxValue Maximum value in the dataset; used to normalise color intensity.
 * @param leftPadding Horizontal offset (px) for the grid origin.
 * @param topPadding Vertical offset (px) for the grid origin.
 * @param cellSizePx Cell width/height in pixels.
 * @param cellStridePx Cell size plus spacing (distance between top-left corners).
 * @param animationProgress Current animation progress in [0, 1].
 * @param cellBoundsOutput Cleared and refilled with bounding rects for all visible data cells.
 */
internal fun DrawScope.drawCalendarGrid(
    gridLayout: GridLayout,
    config: CalendarHeatmapConfig,
    maxValue: Float,
    leftPadding: Float,
    topPadding: Float,
    cellSizePx: Float,
    cellStridePx: Float,
    animationProgress: Float,
    cellBoundsOutput: MutableList<Pair<Rect, CalendarData>>,
) {
    cellBoundsOutput.clear()

    val totalWeeks = gridLayout.totalWeeks
    if (totalWeeks == 0) return

    val animWeeks = animationProgress * totalWeeks

    for (weekIndex in 0 until totalWeeks) {
        val cellAlpha = (animWeeks - weekIndex).coerceIn(0f, 1f)

        for (dayIndex in 0..6) {
            val cellLeft = leftPadding + weekIndex * cellStridePx
            val cellTop = topPadding + dayIndex * cellStridePx

            val gridCell = gridLayout.cellMap[weekIndex to dayIndex]
            val color =
                if (gridCell != null) {
                    config.resolveColor(gridCell.calendarData.value, maxValue)
                } else {
                    config.emptyColor
                }

            drawCell(Offset(cellLeft, cellTop), cellSizePx, color, cellAlpha, config.cellShape)

            if (gridCell != null && cellAlpha > 0f) {
                cellBoundsOutput.add(
                    Rect(cellLeft, cellTop, cellLeft + cellSizePx, cellTop + cellSizePx)
                        to gridCell.calendarData,
                )
            }
        }
    }
}
