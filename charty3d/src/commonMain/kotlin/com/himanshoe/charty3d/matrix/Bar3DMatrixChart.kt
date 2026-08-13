package com.himanshoe.charty3d.matrix

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty3d.internal.MatrixGrid
import com.himanshoe.charty3d.internal.SceneFit
import com.himanshoe.charty3d.internal.bar3DMatrixFaces
import com.himanshoe.charty3d.internal.bar3DMatrixFit
import com.himanshoe.charty3d.internal.matrixCellColor
import com.himanshoe.charty3d.internal.matrixColumnLabelAnchor
import com.himanshoe.charty3d.internal.matrixGrid
import com.himanshoe.charty3d.internal.matrixRowLabelAnchor
import com.himanshoe.charty3d.internal.matrixValueRange
import com.himanshoe.charty3d.matrix.config.Bar3DMatrixConfig
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.projection.shadeForSide
import com.himanshoe.charty3d.projection.sortedFarToNear

private const val LABEL_GAP = 10f
private const val HALF = 2f

/**
 * A grid of bars standing on a floor, where the row and the column are both variables and the bar's
 * height is a third.
 *
 * This is a matrix heatmap that can also be read by height. Unlike the extruded charts in this
 * module, its depth is not decoration: the floor's two axes each carry a variable, so flattening it
 * would cost you one of them. Height and colour encode the same value deliberately, reinforcing each
 * other rather than competing — a tall bar is also a hot one.
 *
 * Occlusion is the honest cost. A tall bar at the front hides shorter ones behind it, and no viewing
 * angle removes that for every dataset; rotating the projection is how a reader looks behind it. When
 * every cell must be readable at once, [com.himanshoe.charty.heatmap.MatrixHeatmapChart] shows the
 * same data flat and complete.
 *
 * Taps resolve **nearest face first**, so a tap selects the bar you can see.
 *
 * ```kotlin
 * Bar3DMatrixChart(
 *     data = { cells },
 *     modifier = Modifier.fillMaxWidth().height(340.dp),
 *     onCellClick = { cell -> println("${cell.rowLabel} × ${cell.columnLabel}: ${cell.value}") },
 * )
 * ```
 *
 * @param data Supplies the cells; rows and columns are laid out in the order they first appear, so
 *   a meaningful ordering you pass in is preserved rather than sorted away.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is empty; `null` uses the built-in
 *   placeholder.
 * @param matrixConfig How the grid is projected, sized, coloured, and animated.
 * @param onCellClick Called with the cell under a tap, resolved nearest face first; `null` disables
 *   tapping.
 */
@ChartyExperimental
@Composable
fun Bar3DMatrixChart(
    data: () -> List<HeatmapCell>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    matrixConfig: Bar3DMatrixConfig = Bar3DMatrixConfig(),
    onCellClick: ((HeatmapCell) -> Unit)? = null,
) {
    val dataList by remember(data) { derivedStateOf { data() } }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val progress = rememberChartAnimation(animation = matrixConfig.animation)
    val grid = remember(dataList) { matrixGrid(dataList) }
    val range = remember(dataList) { matrixValueRange(dataList) }
    val low = matrixConfig.lowColor ?: ChartyThemeDefaults.seriesColor(index = 0)
    val high = matrixConfig.highColor ?: ChartyThemeDefaults.seriesColor(index = 1)
    val drawnFaces = remember { mutableListOf<ProjectedFace<HeatmapCell>>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onCellClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(dataList, matrixConfig) {
                            detectTapGestures { position -> drawnFaces.hitTest(position)?.let(onCellClick) }
                        }
                    },
                ),
    ) {
        val fit = bar3DMatrixFit(size = size, config = matrixConfig)
        val faces =
            bar3DMatrixFaces(
                dataList = dataList,
                grid = grid,
                range = range,
                config = matrixConfig,
                progress = progress.value,
                fit = fit,
            )
        drawnFaces.clear()
        drawnFaces.addAll(faces)

        matrixConfig.plotBackground?.let { background ->
            drawRect(brush = Brush.verticalGradient(background.value), size = size)
        }
        faces.sortedFarToNear().fastForEach { face ->
            val base =
                matrixCellColor(
                    cell = face.payload,
                    range = range,
                    low = low.value.first(),
                    high = high.value.first(),
                )
            drawPath(path = face.toPath(), color = shadeForSide(color = base, side = face.side))
        }
        if (matrixConfig.showAxisLabels) {
            drawMatrixAxisLabels(
                grid = grid,
                config = matrixConfig,
                fit = fit,
                textMeasurer = textMeasurer,
            )
        }
    }
}

/** Prints the column names along the floor's near edge and the row names along its left edge. */
private fun DrawScope.drawMatrixAxisLabels(
    grid: MatrixGrid,
    config: Bar3DMatrixConfig,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    grid.columns.forEachIndexed { index, name ->
        val anchor = matrixColumnLabelAnchor(columnIndex = index, grid = grid, config = config, fit = fit)
        val layout = textMeasurer.measure(text = name, style = config.labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y + LABEL_GAP),
        )
    }
    grid.rows.forEachIndexed { index, name ->
        val anchor = matrixRowLabelAnchor(rowIndex = index, grid = grid, config = config, fit = fit)
        val layout = textMeasurer.measure(text = name, style = config.labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = anchor.x - layout.size.width - LABEL_GAP, y = anchor.y - layout.size.height / HALF),
        )
    }
}
