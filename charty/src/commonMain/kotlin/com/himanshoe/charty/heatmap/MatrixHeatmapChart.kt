package com.himanshoe.charty.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.heatmap.internal.MatrixDrawSpec
import com.himanshoe.charty.heatmap.internal.MatrixGrid
import com.himanshoe.charty.heatmap.internal.buildMatrixHeatmapDescription
import com.himanshoe.charty.heatmap.internal.computeMatrixGrid
import com.himanshoe.charty.heatmap.internal.drawMatrixHeatmap
import com.himanshoe.charty.heatmap.internal.flattenCells
import com.himanshoe.charty.heatmap.internal.heatmapTooltipState
import com.himanshoe.charty.heatmap.internal.resolveHeatmapCellAt

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
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param config Chart appearance and behaviour; see [MatrixHeatmapConfig].
 * @param onCellClick Optional callback invoked when the user taps a grid position that has data.
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an
 *   empty string to suppress it.
 * @param tooltip How a tapped cell is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay, or [ChartTooltip.none] to disable it. Its text comes from
 *   [MatrixHeatmapConfig.tooltipFormatter] and it is shown alongside [onCellClick], not instead of it.
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
    emptyContent: (@Composable () -> Unit)? = null,
    config: MatrixHeatmapConfig = MatrixHeatmapConfig(),
    onCellClick: ((HeatmapCell) -> Unit)? = null,
    accessibilityDescription: String? = null,
    tooltip: ChartTooltip<HeatmapCell> = ChartTooltip.canvas(),
) {
    val cells by remember(data) { derivedStateOf { data() } }
    val grid = remember(cells) { computeMatrixGrid(cells) }
    if (grid.rowLabels.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
    } else {
        MatrixHeatmapContent(
            grid = grid,
            onCellClick = onCellClick,
            accessibilityDescription = accessibilityDescription,
            modifier = modifier,
            config = config,
            tooltip = tooltip,
        )
    }
}

@Composable
private fun MatrixHeatmapContent(
    grid: MatrixGrid,
    onCellClick: ((HeatmapCell) -> Unit)?,
    accessibilityDescription: String?,
    modifier: Modifier = Modifier,
    config: MatrixHeatmapConfig = MatrixHeatmapConfig(),
    tooltip: ChartTooltip<HeatmapCell> = ChartTooltip.canvas(),
) {
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = config.tooltipConfig.orThemeDefault()
    val animationProgress = rememberChartAnimation(config.animation)
    val currentOnCellClick by rememberUpdatedState(onCellClick)
    val tooltipManager = rememberTooltipManager<Rect, HeatmapCell>(dataKey = grid)

    val measuredRowLabels =
        remember(grid.rowLabels, config.labelTextStyle) {
            grid.rowLabels.map { label -> textMeasurer.measure(text = label, style = config.labelTextStyle) }
        }
    val measuredColumnLabels =
        remember(grid.columnLabels, config.labelTextStyle) {
            grid.columnLabels.map { label -> textMeasurer.measure(text = label, style = config.labelTextStyle) }
        }
    val flatCells = remember(grid) { grid.flattenCells() }
    val measuredValues =
        rememberMeasuredValues(flatCells = flatCells, config = config, textMeasurer = textMeasurer)
    val chartDescription =
        remember(grid, accessibilityDescription) {
            when (accessibilityDescription) {
                "" -> null
                null -> buildMatrixHeatmapDescription(grid)
                else -> accessibilityDescription
            }
        }
    val semanticsModifier =
        if (chartDescription != null) {
            Modifier.semantics { contentDescription = chartDescription }
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .then(semanticsModifier)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hit = resolveHeatmapCellAt(cellBounds = tooltipManager.bounds, position = offset)
                        if (hit == null) {
                            tooltipManager.dismiss()
                        } else {
                            currentOnCellClick?.invoke(hit.second)
                            tooltipManager.updateTooltip(
                                state =
                                    heatmapTooltipState(
                                        bounds = hit.first,
                                        content = config.tooltipFormatter(hit.second),
                                    ),
                                item = hit.second,
                            )
                        }
                    }
                },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMatrixHeatmap(
                MatrixDrawSpec(
                    grid = grid,
                    config = config,
                    measuredRowLabels = measuredRowLabels,
                    measuredColumnLabels = measuredColumnLabels,
                    flatCells = flatCells,
                    measuredValues = measuredValues,
                    animationProgress = animationProgress.value,
                    cellBounds = tooltipManager.bounds,
                ),
            )
            if (tooltip.isCanvas()) {
                tooltipManager.tooltipState?.let { state ->
                    drawTooltip(
                        tooltipState = state,
                        config = resolvedTooltipConfig,
                        textMeasurer = textMeasurer,
                        chartWidth = size.width,
                        chartTop = 0f,
                        chartBottom = size.height,
                    )
                }
            }
        }
        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun rememberMeasuredValues(
    flatCells: List<HeatmapCell?>,
    config: MatrixHeatmapConfig,
    textMeasurer: TextMeasurer,
): List<TextLayoutResult?> {
    return remember(flatCells, config.showValues, config.valueFormatter, config.labelTextStyle) {
        if (!config.showValues) {
            return@remember emptyList()
        }
        flatCells.map { cell ->
            cell?.let {
                textMeasurer.measure(text = config.valueFormatter(it.value), style = config.labelTextStyle)
            }
        }
    }
}
