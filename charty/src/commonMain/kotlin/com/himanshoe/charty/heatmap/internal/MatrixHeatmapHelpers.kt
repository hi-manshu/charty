package com.himanshoe.charty.heatmap.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.heatmap.data.HeatmapCell
import kotlin.math.roundToInt

internal const val HEATMAP_MIN_RAMP_ALPHA = 0.15f

private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
private const val DARK_TEXT_COLOR = 0xFF1F2328
private const val VALUE_DECIMAL_FACTOR = 10f

/**
 * The fully resolved matrix layout for [com.himanshoe.charty.heatmap.MatrixHeatmapChart].
 *
 * @property rowLabels Distinct row labels in first-appearance order; one grid row each.
 * @property columnLabels Distinct column labels in first-appearance order; one grid column each.
 * @property cellMap O(1) lookup from (rowIndex, columnIndex) to the cell occupying that position.
 *   When the data contains duplicate (row, column) pairs the last occurrence wins.
 * @property minValue The smallest cell value in the dataset; `0f` when the dataset is empty.
 * @property maxValue The largest cell value in the dataset; `0f` when the dataset is empty.
 */
internal data class MatrixGrid(
    val rowLabels: List<String>,
    val columnLabels: List<String>,
    val cellMap: Map<Pair<Int, Int>, HeatmapCell>,
    val minValue: Float,
    val maxValue: Float,
)

/**
 * Builds the [MatrixGrid] for a list of [HeatmapCell] entries.
 *
 * Rows and columns are ordered by the first appearance of their labels in [cells], so callers
 * control axis order through data order. Positions without a cell stay absent from
 * [MatrixGrid.cellMap] and render as empty cells.
 */
internal fun computeMatrixGrid(cells: List<HeatmapCell>): MatrixGrid {
    if (cells.isEmpty()) {
        return MatrixGrid(
            rowLabels = emptyList(),
            columnLabels = emptyList(),
            cellMap = emptyMap(),
            minValue = 0f,
            maxValue = 0f,
        )
    }
    val rowLabels = cells.map { it.rowLabel }.distinct()
    val columnLabels = cells.map { it.columnLabel }.distinct()
    val rowIndexOf = rowLabels.withIndex().associate { (index, label) -> label to index }
    val columnIndexOf = columnLabels.withIndex().associate { (index, label) -> label to index }
    val cellMap =
        cells.associateBy { cell ->
            rowIndexOf.getValue(cell.rowLabel) to columnIndexOf.getValue(cell.columnLabel)
        }
    return MatrixGrid(
        rowLabels = rowLabels,
        columnLabels = columnLabels,
        cellMap = cellMap,
        minValue = cells.minOf { it.value },
        maxValue = cells.maxOf { it.value },
    )
}

/**
 * Flattens the grid into a row-major list with one entry per grid position — the cell occupying it,
 * or `null` where there is no data. Indexing a list keeps the draw loop from allocating a
 * `Pair` key for every position on every frame.
 */
internal fun MatrixGrid.flattenCells(): List<HeatmapCell?> {
    val columns = columnLabels.size
    return List(rowLabels.size * columns) { index ->
        cellMap[index / columns to index % columns]
    }
}

/**
 * Normalises [value] into the `[0, 1]` range spanned by [minValue] and [maxValue].
 *
 * When the dataset has no spread (`minValue == maxValue`, or an inverted range) every cell maps
 * to `1f` so a uniform dataset renders at full intensity rather than disappearing.
 */
internal fun normalizeHeatmapValue(
    value: Float,
    minValue: Float,
    maxValue: Float,
): Float {
    if (maxValue <= minValue) {
        return 1f
    }
    return ((value - minValue) / (maxValue - minValue)).coerceIn(minimumValue = 0f, maximumValue = 1f)
}

/**
 * Linearly interpolates each RGBA channel between [start] and [stop] at [fraction].
 *
 * Channel-wise interpolation keeps the math pure and platform-independent, unlike
 * `androidx.compose.ui.graphics.lerp` which round-trips through a perceptual color space.
 */
internal fun lerpColorLinear(
    start: Color,
    stop: Color,
    fraction: Float,
): Color {
    val f = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f,
    )
}

/**
 * Maps a normalised [fraction] in `[0, 1]` onto [colorScale].
 *
 * [ChartyColor.Gradient] stops define the low→high scale: the fraction is interpolated piecewise
 * linearly across adjacent stops, so 2-stop, 3-stop, and longer scales all work. A
 * [ChartyColor.Solid] (or a single-stop gradient) becomes a single-hue alpha ramp from
 * [HEATMAP_MIN_RAMP_ALPHA] at the low end to the color's own alpha at the high end.
 */
internal fun interpolateHeatmapColor(
    colorScale: ChartyColor,
    fraction: Float,
): Color {
    val f = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f)
    val stops =
        when (colorScale) {
            is ChartyColor.Solid -> listOf(colorScale.color)
            is ChartyColor.Gradient -> colorScale.colors
        }
    if (stops.size == 1) {
        val base = stops.first()
        return base.copy(alpha = base.alpha * (HEATMAP_MIN_RAMP_ALPHA + f * (1f - HEATMAP_MIN_RAMP_ALPHA)))
    }
    val scaled = f * (stops.size - 1)
    val index = scaled.toInt().coerceAtMost(stops.size - 2)
    return lerpColorLinear(start = stops[index], stop = stops[index + 1], fraction = scaled - index)
}

/**
 * Per-cell entry-animation progress producing a diagonal top-left→bottom-right sweep.
 *
 * Cells on earlier diagonals (`rowIndex + columnIndex`) reveal first; every cell reaches full
 * progress exactly when [progress] reaches `1f`.
 */
internal fun matrixCellProgress(
    progress: Float,
    rowIndex: Int,
    columnIndex: Int,
    rowCount: Int,
    columnCount: Int,
): Float {
    val totalDiagonals = rowCount + columnCount - 1
    if (totalDiagonals <= 1) {
        return progress.coerceIn(minimumValue = 0f, maximumValue = 1f)
    }
    val diagonal = (rowIndex + columnIndex).toFloat()
    return (progress * totalDiagonals - diagonal).coerceIn(minimumValue = 0f, maximumValue = 1f)
}

/**
 * Picks a readable in-cell text color for the given cell [background]: dark text on light cells,
 * white text on dark cells.
 */
internal fun heatmapContrastTextColor(background: Color): Color =
    if (background.luminance() > CONTRAST_LUMINANCE_THRESHOLD) {
        Color(DARK_TEXT_COLOR)
    } else {
        Color.White
    }

/**
 * Default in-cell value formatting: integers render without a decimal part, everything else is
 * rounded to one decimal place.
 */
internal fun formatHeatmapValue(value: Float): String {
    val rounded = (value * VALUE_DECIMAL_FACTOR).roundToInt() / VALUE_DECIMAL_FACTOR
    val whole = rounded.toInt()
    return if (rounded == whole.toFloat()) {
        whole.toString()
    } else {
        rounded.toString()
    }
}

/**
 * Default tooltip text for a tapped cell: its row, its column, and its formatted value.
 */
internal fun formatHeatmapTooltip(cell: HeatmapCell): String =
    "${cell.rowLabel} · ${cell.columnLabel}: ${formatHeatmapValue(cell.value)}"

/**
 * Resolves the data cell whose recorded bounds contain [position], or `null` when the tap landed on
 * an empty grid position, on a label, or outside the grid. The first match wins; cells never
 * overlap, so at most one can contain the point.
 */
internal fun resolveHeatmapCellAt(
    cellBounds: List<Pair<Rect, HeatmapCell>>,
    position: Offset,
): Pair<Rect, HeatmapCell>? = cellBounds.firstOrNull { (rect, _) -> rect.contains(position) }

/**
 * Builds the tooltip anchor for a tapped cell, anchored to the cell's top edge.
 *
 * [TooltipState.x] is the anchor's **leading** edge, not its centre: the drawer centres the bubble
 * itself by adding half of [TooltipState.barWidth]. Passing an already-centred x here would shift
 * the bubble half a cell to the right.
 */
internal fun heatmapTooltipState(
    bounds: Rect,
    content: String,
): TooltipState =
    TooltipState(
        content = content,
        x = bounds.left,
        y = bounds.top,
        barWidth = bounds.width,
    )

/**
 * Builds the screen-reader summary for a matrix heatmap from its resolved [grid].
 */
internal fun buildMatrixHeatmapDescription(grid: MatrixGrid): String {
    if (grid.rowLabels.isEmpty()) {
        return "Empty matrix heatmap"
    }
    val rows = grid.rowLabels.size
    val columns = grid.columnLabels.size
    val filled = grid.cellMap.size
    val range = "${formatHeatmapValue(grid.minValue)} to ${formatHeatmapValue(grid.maxValue)}"
    return "Matrix heatmap with $rows rows and $columns columns. " +
        "$filled of ${rows * columns} cells have data. Values range from $range."
}
