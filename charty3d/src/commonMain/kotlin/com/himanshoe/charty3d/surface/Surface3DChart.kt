package com.himanshoe.charty3d.surface

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty3d.internal.SurfaceQuad
import com.himanshoe.charty3d.internal.hitTestQuads
import com.himanshoe.charty3d.internal.surface3DFit
import com.himanshoe.charty3d.internal.surface3DQuads
import com.himanshoe.charty3d.internal.surfaceHeightRange
import com.himanshoe.charty3d.internal.surfaceQuadColor
import com.himanshoe.charty3d.surface.config.Surface3DChartConfig
import com.himanshoe.charty3d.surface.data.SurfaceData

/**
 * A surface: a height sampled over a grid, drawn as a shaded mesh.
 *
 * ## The one figure that cannot be flattened
 * A contour plot approximates a surface and a heatmap discards its shape entirely. If your data is
 * `z = f(x, y)` sampled on a grid — a response surface, a terrain, a loss landscape, an option
 * surface — this is what it wants to be, and the projection is doing real work rather than
 * decorating a chart that would read better flat.
 *
 * Height sets the colour and the tilt sets the shading, so relief reads even where two regions sit
 * at the same height. The mesh is not a closed solid, so nothing is culled — a surface can legibly
 * be seen from beneath, and hiding the faces that tilt away would tear holes in it. Draw order alone
 * decides what covers what.
 *
 * Its honest limit is the same as any projection's: a fold can hide what is behind it, and the only
 * way to look behind a fold is to turn the surface.
 *
 * ```kotlin
 * Surface3DChart(
 *     data = {
 *         SurfaceData.of(rowCount = 24, columnCount = 24) { row, column ->
 *             sin(row / 3f) * cos(column / 3f)
 *         }
 *     },
 *     modifier = Modifier.fillMaxWidth().height(340.dp),
 * )
 * ```
 *
 * @param data Supplies the grid of heights. The grid must be complete; [SurfaceData] enforces that.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is `null`; `null` uses the built-in
 *   placeholder.
 * @param surfaceConfig How the mesh is projected, coloured, shaded, and animated.
 * @param onCellClick Called with the mean height of the grid cell under a tap, resolved nearest
 *   first; `null` disables tapping.
 */
@ChartyExperimental
@Composable
fun Surface3DChart(
    data: () -> SurfaceData?,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    surfaceConfig: Surface3DChartConfig = Surface3DChartConfig(),
    onCellClick: ((Float) -> Unit)? = null,
) {
    val surface by remember(data) { derivedStateOf { data() } }
    val grid = surface
    if (grid == null) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val progress = rememberChartAnimation(animation = surfaceConfig.animation)
    val range = remember(grid) { surfaceHeightRange(grid) }
    val low = surfaceConfig.lowColor ?: ChartyThemeDefaults.seriesColor(index = 0)
    val high = surfaceConfig.highColor ?: ChartyThemeDefaults.seriesColor(index = 1)
    val drawnQuads = remember { mutableListOf<SurfaceQuad>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onCellClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(grid, surfaceConfig) {
                            detectTapGestures { position ->
                                drawnQuads.hitTestQuads(position)?.let { quad -> onCellClick(quad.meanHeight) }
                            }
                        }
                    },
                ),
    ) {
        val fit = surface3DFit(size = size, config = surfaceConfig)
        val quads = surface3DQuads(data = grid, config = surfaceConfig, progress = progress.value, fit = fit)
        drawnQuads.clear()
        drawnQuads.addAll(quads)

        surfaceConfig.plotBackground?.let { background ->
            drawRect(brush = Brush.verticalGradient(background.value), size = size)
        }
        quads.sortedByDescending { quad -> quad.face.depth }.fastForEach { quad ->
            val path = quad.face.toPath()
            drawPath(
                path = path,
                color =
                    surfaceQuadColor(
                        quad = quad,
                        range = range,
                        config = surfaceConfig,
                        low = low.value.first(),
                        high = high.value.first(),
                    ),
            )
            if (surfaceConfig.showWireframe) {
                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = surfaceConfig.wireframeAlpha),
                    style = Stroke(width = 1f),
                )
            }
        }
    }
}
