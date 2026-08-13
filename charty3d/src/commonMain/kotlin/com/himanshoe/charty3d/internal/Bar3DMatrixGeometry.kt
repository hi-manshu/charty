package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty3d.matrix.config.Bar3DMatrixConfig
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace

private const val SCENE_FLOOR = 100f
private const val SCENE_HEIGHT = 65f
private const val EDGE_PADDING = 24f
private const val LABEL_ROOM = 30f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f

/** The grid a matrix chart lays out on: its distinct rows and columns, in first-seen order. */
internal data class MatrixGrid(
    val rows: List<String>,
    val columns: List<String>,
) {
    /** Whether the grid has any cells at all to place. */
    val isEmpty: Boolean get() = rows.isEmpty() || columns.isEmpty()
}

/**
 * The rows and columns present in [dataList], in the order they first appear.
 *
 * First-seen order rather than sorted, because the caller's order is usually meaningful — months,
 * stages, severity bands — and sorting would silently discard it.
 */
internal fun matrixGrid(dataList: List<HeatmapCell>): MatrixGrid =
    MatrixGrid(
        rows = dataList.map { cell -> cell.rowLabel }.distinct(),
        columns = dataList.map { cell -> cell.columnLabel }.distinct(),
    )

/** The lowest and highest value in the grid, which height and colour are both scaled against. */
internal fun matrixValueRange(dataList: List<HeatmapCell>): ClosedFloatingPointRange<Float> {
    if (dataList.isEmpty()) {
        return 0f..1f
    }
    val min = dataList.minOf { cell -> cell.value }
    val max = dataList.maxOf { cell -> cell.value }
    return if (min == max) {
        min..(min + 1f)
    } else {
        min..max
    }
}

/** Where one cell's bar stands on the floor, and how wide its footprint is. */
private fun cellFootprint(
    rowIndex: Int,
    columnIndex: Int,
    grid: MatrixGrid,
    config: Bar3DMatrixConfig,
): Triple<Float, Float, Float> {
    val cellWidth = SCENE_FLOOR / grid.columns.size
    val cellDepth = SCENE_FLOOR / grid.rows.size
    val footprintX = cellWidth * config.barFootprintFraction
    val footprintZ = cellDepth * config.barFootprintFraction
    val left = columnIndex * cellWidth + (cellWidth - footprintX) / HALF
    val near = rowIndex * cellDepth + (cellDepth - footprintZ) / HALF
    return Triple(left, near, footprintX)
}

/** The z position of a row's near edge, and the depth of one cell. */
private fun cellDepthAt(
    rowIndex: Int,
    grid: MatrixGrid,
    config: Bar3DMatrixConfig,
): Pair<Float, Float> {
    val cellDepth = SCENE_FLOOR / grid.rows.size
    val footprintZ = cellDepth * config.barFootprintFraction
    return (rowIndex * cellDepth + (cellDepth - footprintZ) / HALF) to footprintZ
}

/**
 * Builds every visible face of every bar in the grid.
 *
 * Both floor axes carry data here — one variable per row, one per column — so the depth is not
 * decoration the way an extruded bar chart's is. That is what makes this worth projecting.
 *
 * Height is measured from zero rather than from the smallest cell, so a bar's length stays
 * proportional to its value. Scaling across the observed range instead would draw the smallest cell
 * as nothing at all, which reads as missing data rather than as a low value. Colour still uses the
 * observed range, where stretching it costs nothing and buys contrast.
 */
internal fun bar3DMatrixFaces(
    dataList: List<HeatmapCell>,
    grid: MatrixGrid,
    range: ClosedFloatingPointRange<Float>,
    config: Bar3DMatrixConfig,
    progress: Float,
    fit: SceneFit,
): List<ProjectedFace<HeatmapCell>> {
    if (grid.isEmpty) {
        return emptyList()
    }
    val faces = mutableListOf<ProjectedFace<HeatmapCell>>()

    dataList.forEach { cell ->
        val rowIndex = grid.rows.indexOf(cell.rowLabel)
        val columnIndex = grid.columns.indexOf(cell.columnLabel)
        if (rowIndex < 0 || columnIndex < 0) {
            return@forEach
        }
        val (left, _, footprintX) = cellFootprint(rowIndex, columnIndex, grid, config)
        val (near, footprintZ) = cellDepthAt(rowIndex, grid, config)
        val height = (cell.value / range.endInclusive.coerceAtLeast(MIN_SPAN)) * SCENE_HEIGHT * progress
        faces +=
            boxFaces(
                projection = config.projection,
                origin = Offset.Zero,
                nearLeft = Point3D(x = left, y = 0f, z = near),
                width = footprintX,
                height = -height,
                depth = footprintZ,
                payload = cell,
            ).map { face -> face.copy(points = face.points.map(fit::apply)) }
    }
    return faces
}

/**
 * The colour a cell's bar is filled with: interpolated between the low and high colours by the
 * cell's value, so height and colour say the same thing.
 */
internal fun matrixCellColor(
    cell: HeatmapCell,
    range: ClosedFloatingPointRange<Float>,
    low: Color,
    high: Color,
): Color {
    val span = (range.endInclusive - range.start).coerceAtLeast(MIN_SPAN)
    return lerp(low, high, ((cell.value - range.start) / span).coerceIn(0f, 1f))
}

/** Measures the projected grid and returns the transform that centres it inside [size]. */
internal fun bar3DMatrixFit(
    size: Size,
    config: Bar3DMatrixConfig,
): SceneFit {
    val corners =
        listOf(0f, SCENE_FLOOR).flatMap { x ->
            listOf(0f, -SCENE_HEIGHT).flatMap { y ->
                listOf(0f, SCENE_FLOOR).map { z -> Point3D(x = x, y = y, z = z) }
            }
        }
    val projected = corners.map { point -> config.projection.project(point = point, origin = Offset.Zero) }
    val minX = projected.minOf { it.x }
    val maxX = projected.maxOf { it.x }
    val minY = projected.minOf { it.y }
    val maxY = projected.maxOf { it.y }
    val spanX = (maxX - minX).coerceAtLeast(MIN_SPAN)
    val spanY = (maxY - minY).coerceAtLeast(MIN_SPAN)

    val room = if (config.showAxisLabels) LABEL_ROOM else 0f
    val availableWidth = (size.width - EDGE_PADDING * HALF - room).coerceAtLeast(MIN_SPAN)
    val availableHeight = (size.height - EDGE_PADDING * HALF - room).coerceAtLeast(MIN_SPAN)
    val scale = minOf(availableWidth / spanX, availableHeight / spanY)
    return SceneFit(
        scale = scale,
        offset =
            Offset(
                x = (size.width - spanX * scale) / HALF - minX * scale,
                y = (size.height - spanY * scale) / HALF - minY * scale,
            ),
    )
}

/** The canvas position a column's name sits at, on the floor's near edge. */
internal fun matrixColumnLabelAnchor(
    columnIndex: Int,
    grid: MatrixGrid,
    config: Bar3DMatrixConfig,
    fit: SceneFit,
): Offset {
    val cellWidth = SCENE_FLOOR / grid.columns.size
    return fit.apply(
        config.projection.project(
            point = Point3D(x = columnIndex * cellWidth + cellWidth / HALF, y = 0f, z = 0f),
            origin = Offset.Zero,
        ),
    )
}

/** The canvas position a row's name sits at, on the floor's left edge. */
internal fun matrixRowLabelAnchor(
    rowIndex: Int,
    grid: MatrixGrid,
    config: Bar3DMatrixConfig,
    fit: SceneFit,
): Offset {
    val cellDepth = SCENE_FLOOR / grid.rows.size
    return fit.apply(
        config.projection.project(
            point = Point3D(x = 0f, y = 0f, z = rowIndex * cellDepth + cellDepth / HALF),
            origin = Offset.Zero,
        ),
    )
}
