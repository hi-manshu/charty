package com.himanshoe.charty.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.heatmap.internal.MatrixDrawSpec
import com.himanshoe.charty.heatmap.internal.MatrixGrid
import com.himanshoe.charty.heatmap.internal.buildMatrixHeatmapDescription
import com.himanshoe.charty.heatmap.internal.computeMatrixGrid
import com.himanshoe.charty.heatmap.internal.drawMatrixHeatmap

/**
 * A composable function that displays a general-purpose matrix heatmap: a grid of rounded cells
 * where each cell's color intensity encodes its value.
 *
 * Rows and columns are derived from the data itself — distinct [HeatmapCell.rowLabel] and
 * [HeatmapCell.columnLabel] values in first-appearance order — so the input order of cells defines
 * the axis order. Values are normalised across the whole dataset and mapped onto
 * [MatrixHeatmapConfig.colorScale]; grid positions without data use
 * [MatrixHeatmapConfig.emptyCellColor]. Row labels are drawn on the left, column labels below the
 * grid, and cells fade and scale in along a diagonal entry animation. The chart lays itself out on
 * its own canvas and fills the size given by [modifier].
 *
 * @param data A lambda that returns the list of [HeatmapCell] entries to display. When the list is
 *   empty, a [ChartEmptyState] placeholder is shown instead. Duplicate (row, column) pairs keep the
 *   last occurrence.
 * @param modifier Modifier applied to the chart canvas; it determines the area the grid fills.
 * @param config Chart appearance and behaviour; see [MatrixHeatmapConfig].
 * @param onCellClick Optional callback invoked when the user taps a grid position that has data.
 *
 * Example usage:
 * ```kotlin
 * MatrixHeatmapChart(
 *     data = {
 *         listOf(
 *             HeatmapCell(rowLabel = "Mon", columnLabel = "9h", value = 12f),
 *             HeatmapCell(rowLabel = "Mon", columnLabel = "10h", value = 30f),
 *             HeatmapCell(rowLabel = "Tue", columnLabel = "9h", value = 22f),
 *         )
 *     },
 *     config = MatrixHeatmapConfig(showValues = true),
 *     onCellClick = { cell -> println("${cell.rowLabel} × ${cell.columnLabel}: ${cell.value}") },
 * )
 * ```
 */
@Composable
fun MatrixHeatmapChart(
    data: () -> List<HeatmapCell>,
    modifier: Modifier = Modifier,
    config: MatrixHeatmapConfig = MatrixHeatmapConfig(),
    onCellClick: ((HeatmapCell) -> Unit)? = null,
) {
    val cells by remember(data) { derivedStateOf { data() } }
    val grid = remember(cells) { computeMatrixGrid(cells) }
    if (grid.rowLabels.isEmpty()) {
        ChartEmptyState(modifier = modifier)
    } else {
        MatrixHeatmapContent(grid = grid, onCellClick = onCellClick, modifier = modifier, config = config)
    }
}

@Composable
private fun MatrixHeatmapContent(
    grid: MatrixGrid,
    onCellClick: ((HeatmapCell) -> Unit)?,
    modifier: Modifier = Modifier,
    config: MatrixHeatmapConfig = MatrixHeatmapConfig(),
) {
    val textMeasurer = rememberTextMeasurer()
    val animationProgress = rememberChartAnimation(config.animation)
    val currentOnCellClick by rememberUpdatedState(onCellClick)
    val cellBounds = remember { mutableListOf<Pair<Rect, HeatmapCell>>() }

    val measuredRowLabels =
        remember(grid.rowLabels, config.labelTextStyle) {
            grid.rowLabels.map { label -> textMeasurer.measure(text = label, style = config.labelTextStyle) }
        }
    val measuredColumnLabels =
        remember(grid.columnLabels, config.labelTextStyle) {
            grid.columnLabels.map { label -> textMeasurer.measure(text = label, style = config.labelTextStyle) }
        }
    val measuredValues =
        rememberMeasuredValues(grid = grid, config = config, textMeasurer = textMeasurer)
    val chartDescription = remember(grid) { buildMatrixHeatmapDescription(grid) }

    Canvas(
        modifier =
            modifier
                .semantics { contentDescription = chartDescription }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hit = cellBounds.firstOrNull { (rect, _) -> rect.contains(offset) }
                        if (hit != null) {
                            currentOnCellClick?.invoke(hit.second)
                        }
                    }
                },
    ) {
        drawMatrixHeatmap(
            MatrixDrawSpec(
                grid = grid,
                config = config,
                measuredRowLabels = measuredRowLabels,
                measuredColumnLabels = measuredColumnLabels,
                measuredValues = measuredValues,
                animationProgress = animationProgress.value,
                cellBounds = cellBounds,
            ),
        )
    }
}

@Composable
private fun rememberMeasuredValues(
    grid: MatrixGrid,
    config: MatrixHeatmapConfig,
    textMeasurer: TextMeasurer,
): Map<Pair<Int, Int>, TextLayoutResult> {
    return remember(grid, config.showValues, config.valueFormatter, config.labelTextStyle) {
        if (!config.showValues) {
            return@remember emptyMap()
        }
        grid.cellMap.mapValues { (_, cell) ->
            textMeasurer.measure(text = config.valueFormatter(cell.value), style = config.labelTextStyle)
        }
    }
}
