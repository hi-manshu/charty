package com.himanshoe.charty3d.surface.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.annotation.ChartyExperimental

/**
 * One sample of a surface: a height measured at a position on the grid.
 *
 * @property row Index down the grid, matching [SurfaceData.rowCount].
 * @property column Index across the grid, matching [SurfaceData.columnCount].
 * @property value The height measured there.
 */
@ChartyExperimental
@Immutable
data class SurfacePoint(
    val row: Int,
    val column: Int,
    val value: Float,
)

/**
 * A rectangular grid of heights — a sampled function of two variables.
 *
 * A surface is the one figure that genuinely cannot be flattened: a contour plot approximates it and
 * a heatmap discards the shape entirely. If your data is `z = f(x, y)` sampled on a grid, this is
 * what it wants to be.
 *
 * The grid must be complete: every row and column combination needs a height, because a mesh with a
 * hole in it has no defined surface across the gap. The constructor checks that rather than letting
 * the chart draw a torn sheet.
 *
 * @property rowCount How many samples down the grid.
 * @property columnCount How many samples across.
 * @property heights The measured heights, in row-major order.
 */
@ChartyExperimental
@Immutable
data class SurfaceData(
    val rowCount: Int,
    val columnCount: Int,
    val heights: List<Float>,
) {
    init {
        require(rowCount >= 2) { "a surface needs at least two rows to have any extent, got: $rowCount" }
        require(columnCount >= 2) { "a surface needs at least two columns to have any extent, got: $columnCount" }
        require(heights.size == rowCount * columnCount) {
            "a surface grid must be complete: expected ${rowCount * columnCount} heights for a " +
                "$rowCount×$columnCount grid, got ${heights.size}"
        }
    }

    /** The height at [row], [column]. */
    fun heightAt(
        row: Int,
        column: Int,
    ): Float = heights[row * columnCount + column]

    /** Ways of obtaining a grid other than listing every height by hand. */
    companion object {
        /**
         * Samples [function] over a [rowCount] × [columnCount] grid, which is usually how a surface
         * is actually obtained.
         */
        fun of(
            rowCount: Int,
            columnCount: Int,
            function: (row: Int, column: Int) -> Float,
        ): SurfaceData =
            SurfaceData(
                rowCount = rowCount,
                columnCount = columnCount,
                heights =
                    (0 until rowCount).flatMap { row ->
                        (0 until columnCount).map { column -> function(row, column) }
                    },
            )
    }
}
