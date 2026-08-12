package com.himanshoe.charty.heatmap.data

import androidx.compose.runtime.Immutable

/**
 * A single cell of a [com.himanshoe.charty.heatmap.MatrixHeatmapChart].
 *
 * Each cell addresses one (row, column) intersection of the matrix by label. Rows and columns are
 * ordered by the first appearance of their labels in the data list, so the input order of cells
 * defines the on-screen order of the axes.
 *
 * @property rowLabel The label of the row this cell belongs to; drawn on the left of the grid.
 * @property columnLabel The label of the column this cell belongs to; drawn below the grid.
 * @property value The magnitude of this cell. Values are normalised across the whole dataset and
 *   mapped onto the configured color scale, so only their relative size matters.
 */
@Immutable
data class HeatmapCell(
    val rowLabel: String,
    val columnLabel: String,
    val value: Float,
)
