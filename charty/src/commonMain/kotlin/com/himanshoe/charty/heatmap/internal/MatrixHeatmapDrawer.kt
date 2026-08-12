package com.himanshoe.charty.heatmap.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell

private const val ROW_LABEL_GAP = 8f
private const val COLUMN_LABEL_GAP = 6f
private const val MIN_CELL_SCALE = 0.6f
private const val VALUE_TEXT_MIN_PROGRESS = 0.4f
private const val HALF = 2f

/**
 * Everything the draw phase needs to render a matrix heatmap frame, pre-computed in composition so
 * drawing stays allocation-light.
 *
 * @property grid The resolved matrix layout.
 * @property config The chart configuration.
 * @property measuredRowLabels Pre-measured row labels, index-aligned with [MatrixGrid.rowLabels].
 * @property measuredColumnLabels Pre-measured column labels, index-aligned with
 *   [MatrixGrid.columnLabels].
 * @property measuredValues Pre-measured in-cell value texts keyed by (rowIndex, columnIndex);
 *   empty when values are hidden.
 * @property animationProgress Overall entry-animation progress in `[0, 1]`.
 * @property cellBounds Cleared and refilled each frame with the bounds of visible data cells for
 *   tap hit-testing.
 */
internal data class MatrixDrawSpec(
    val grid: MatrixGrid,
    val config: MatrixHeatmapConfig,
    val measuredRowLabels: List<TextLayoutResult>,
    val measuredColumnLabels: List<TextLayoutResult>,
    val measuredValues: Map<Pair<Int, Int>, TextLayoutResult>,
    val animationProgress: Float,
    val cellBounds: MutableList<Pair<Rect, HeatmapCell>>,
)

/**
 * The per-frame cell geometry derived from the canvas size and [MatrixDrawSpec].
 *
 * @property leftPadding Horizontal space (px) reserved for row labels.
 * @property cellWidth Width (px) of each cell.
 * @property cellHeight Height (px) of each cell.
 * @property spacing Gap (px) between adjacent cells.
 */
internal data class MatrixCellGeometry(
    val leftPadding: Float,
    val cellWidth: Float,
    val cellHeight: Float,
    val spacing: Float,
)

/**
 * Draws the complete matrix heatmap: row labels on the left, column labels below the grid, and the
 * cell grid itself with a diagonal fade-and-scale entry animation.
 */
internal fun DrawScope.drawMatrixHeatmap(spec: MatrixDrawSpec) {
    spec.cellBounds.clear()
    val rows = spec.grid.rowLabels.size
    val columns = spec.grid.columnLabels.size
    if (rows == 0 || columns == 0) {
        return
    }
    val spacing = spec.config.cellSpacing.toPx()
    val leftPadding = spec.measuredRowLabels.maxOf { it.size.width } + ROW_LABEL_GAP
    val bottomPadding = spec.measuredColumnLabels.maxOf { it.size.height } + COLUMN_LABEL_GAP
    val geometry =
        MatrixCellGeometry(
            leftPadding = leftPadding,
            cellWidth = ((size.width - leftPadding - (columns - 1) * spacing) / columns).coerceAtLeast(0f),
            cellHeight = ((size.height - bottomPadding - (rows - 1) * spacing) / rows).coerceAtLeast(0f),
            spacing = spacing,
        )
    drawMatrixRowLabels(spec = spec, geometry = geometry)
    drawMatrixColumnLabels(spec = spec, geometry = geometry, rows = rows)
    for (rowIndex in 0 until rows) {
        for (columnIndex in 0 until columns) {
            drawMatrixCell(spec = spec, geometry = geometry, rowIndex = rowIndex, columnIndex = columnIndex)
        }
    }
}

/**
 * Draws each row label vertically centred against its row, right-aligned to the grid's left edge.
 */
internal fun DrawScope.drawMatrixRowLabels(
    spec: MatrixDrawSpec,
    geometry: MatrixCellGeometry,
) {
    for ((rowIndex, measured) in spec.measuredRowLabels.withIndex()) {
        val x = geometry.leftPadding - measured.size.width - ROW_LABEL_GAP
        val y =
            rowIndex * (geometry.cellHeight + geometry.spacing) +
                (geometry.cellHeight - measured.size.height) / HALF
        drawText(textLayoutResult = measured, topLeft = Offset(x.coerceAtLeast(0f), y))
    }
}

/**
 * Draws each column label horizontally centred under its column, below the grid. Labels wider
 * than a cell column are skipped to avoid overlapping neighbours.
 */
internal fun DrawScope.drawMatrixColumnLabels(
    spec: MatrixDrawSpec,
    geometry: MatrixCellGeometry,
    rows: Int,
) {
    val gridBottom = rows * geometry.cellHeight + (rows - 1) * geometry.spacing + COLUMN_LABEL_GAP
    for ((columnIndex, measured) in spec.measuredColumnLabels.withIndex()) {
        if (measured.size.width > geometry.cellWidth + geometry.spacing) {
            continue
        }
        val x =
            geometry.leftPadding + columnIndex * (geometry.cellWidth + geometry.spacing) +
                (geometry.cellWidth - measured.size.width) / HALF
        drawText(textLayoutResult = measured, topLeft = Offset(x, gridBottom))
    }
}

/**
 * Draws a single grid position: a rounded cell colored by its normalised value (or the empty-cell
 * color), the optional centred value text, and appends the cell's bounds for hit-testing once it
 * has become visible.
 */
internal fun DrawScope.drawMatrixCell(
    spec: MatrixDrawSpec,
    geometry: MatrixCellGeometry,
    rowIndex: Int,
    columnIndex: Int,
) {
    val cell = spec.grid.cellMap[rowIndex to columnIndex]
    val progress =
        matrixCellProgress(
            progress = spec.animationProgress,
            rowIndex = rowIndex,
            columnIndex = columnIndex,
            rowCount = spec.grid.rowLabels.size,
            columnCount = spec.grid.columnLabels.size,
        )
    if (progress <= 0f) {
        return
    }
    val color =
        if (cell != null) {
            val fraction =
                normalizeHeatmapValue(
                    value = cell.value,
                    minValue = spec.grid.minValue,
                    maxValue = spec.grid.maxValue,
                )
            interpolateHeatmapColor(colorScale = spec.config.colorScale, fraction = fraction)
        } else {
            spec.config.emptyCellColor.value
                .first()
        }
    val left = geometry.leftPadding + columnIndex * (geometry.cellWidth + geometry.spacing)
    val top = rowIndex * (geometry.cellHeight + geometry.spacing)
    val scale = MIN_CELL_SCALE + (1f - MIN_CELL_SCALE) * progress
    val drawnWidth = geometry.cellWidth * scale
    val drawnHeight = geometry.cellHeight * scale
    val insetX = (geometry.cellWidth - drawnWidth) / HALF
    val insetY = (geometry.cellHeight - drawnHeight) / HALF
    drawRoundRect(
        color = color,
        topLeft = Offset(left + insetX, top + insetY),
        size = Size(drawnWidth, drawnHeight),
        cornerRadius = CornerRadius(spec.config.cellCornerRadius),
        alpha = progress,
    )
    if (cell != null) {
        drawMatrixCellValue(
            spec = spec,
            geometry = geometry,
            cellKey = rowIndex to columnIndex,
            cellBackground = color,
            cellTopLeft = Offset(left, top),
            progress = progress,
        )
        spec.cellBounds.add(
            Rect(left, top, left + geometry.cellWidth, top + geometry.cellHeight) to cell,
        )
    }
}

private fun DrawScope.drawMatrixCellValue(
    spec: MatrixDrawSpec,
    geometry: MatrixCellGeometry,
    cellKey: Pair<Int, Int>,
    cellBackground: Color,
    cellTopLeft: Offset,
    progress: Float,
) {
    if (!spec.config.showValues || progress < VALUE_TEXT_MIN_PROGRESS) {
        return
    }
    val measured = spec.measuredValues[cellKey]
    if (measured == null || measured.size.width > geometry.cellWidth || measured.size.height > geometry.cellHeight) {
        return
    }
    drawText(
        textLayoutResult = measured,
        color = heatmapContrastTextColor(cellBackground),
        topLeft =
            Offset(
                cellTopLeft.x + (geometry.cellWidth - measured.size.width) / HALF,
                cellTopLeft.y + (geometry.cellHeight - measured.size.height) / HALF,
            ),
        alpha = progress,
    )
}
