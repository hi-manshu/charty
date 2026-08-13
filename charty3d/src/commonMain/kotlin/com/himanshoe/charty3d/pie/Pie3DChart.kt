package com.himanshoe.charty3d.pie

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
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.internal.SceneFit
import com.himanshoe.charty3d.internal.orderedForDrawing
import com.himanshoe.charty3d.internal.pie3DFaces
import com.himanshoe.charty3d.internal.pie3DFit
import com.himanshoe.charty3d.internal.pie3DLabelAnchor
import com.himanshoe.charty3d.internal.pie3DSweeps
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.pie.config.Pie3DLabelContent
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.projection.shadeForSide
import kotlin.math.roundToInt

private const val PERCENT = 100f
private const val HALF = 2f

/**
 * A pie or ring chart drawn as a solid disc, projected onto the canvas from a configurable viewing
 * angle.
 *
 * Each slice is built as a wedge with a top surface, an outer wall, and a radial cut on either side,
 * and the whole scene is painted from far to near. That ordering is what lets a slice at the front
 * hide the wall of one behind it without either slice knowing the other exists.
 *
 * ## Read this before choosing a 3D pie
 * A flat pie already asks the eye to compare angles, which people do poorly. Tilting it makes that
 * worse in a specific and well-documented way: the slices nearest the viewer show their walls and so
 * present more ink than slices of the same value at the back. The chart cannot correct for it — it is
 * what the projection does. Use this when the figure is the point and the exact shares are read from
 * labels or a legend; use [com.himanshoe.charty.pie.PieChart], or better a bar chart, when the
 * comparison is the point.
 *
 * Taps resolve **nearest face first**, so a tap always selects the slice you can see rather than one
 * hidden behind it.
 *
 * ```kotlin
 * Pie3DChart(
 *     data = { listOf(PieData(label = "A", value = 40f), PieData(label = "B", value = 60f)) },
 *     modifier = Modifier.fillMaxWidth().height(300.dp),
 *     pieConfig = Pie3DChartConfig(holeFraction = 0.45f, labelContent = Pie3DLabelContent.PERCENTAGE),
 *     onSliceClick = { slice -> println(slice.label) },
 * )
 * ```
 *
 * @param data Supplies the slices, drawn clockwise from twelve o'clock in the order given.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is empty; `null` uses the built-in
 *   placeholder.
 * @param colors Palette cycled across slices that carry no colour of their own. Only a gradient's
 *   first stop is used, because each face is shaded by its orientation rather than filled with a ramp.
 * @param pieConfig How the disc is projected, sized, animated, and labelled.
 * @param onSliceClick Called with the slice under a tap, resolved nearest face first; `null` disables
 *   tapping.
 */
@ChartyExperimental
@Composable
fun Pie3DChart(
    data: () -> List<PieData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: List<ChartyColor> = emptyList(),
    pieConfig: Pie3DChartConfig = Pie3DChartConfig(),
    onSliceClick: ((PieData) -> Unit)? = null,
) {
    val dataList by remember(data) { derivedStateOf { data() } }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val progress = rememberChartAnimation(animation = pieConfig.animation)
    val themePalette = List(dataList.size) { index -> ChartyThemeDefaults.seriesColor(index = index) }
    val palette = remember(colors, themePalette) { colors.ifEmpty { themePalette } }
    val drawnFaces = remember { mutableListOf<ProjectedFace<PieData>>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onSliceClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(dataList, pieConfig) {
                            detectTapGestures { position -> drawnFaces.hitTest(position)?.let(onSliceClick) }
                        }
                    },
                ),
    ) {
        val fit = pie3DFit(size = size, config = pieConfig)
        val faces = pie3DFaces(dataList = dataList, config = pieConfig, progress = progress.value, fit = fit)
        drawnFaces.clear()
        drawnFaces.addAll(faces)

        pieConfig.plotBackground?.let { background ->
            drawRect(brush = Brush.verticalGradient(background.value), size = size)
        }
        faces.orderedForDrawing().fastForEach { face ->
            val index = dataList.indexOf(face.payload).coerceAtLeast(0)
            val base = face.payload.color ?: palette[index % palette.size]
            drawPath(path = face.toPath(), color = shadeForSide(color = base.value.first(), side = face.side))
        }
        if (pieConfig.labelContent != Pie3DLabelContent.NONE) {
            drawPie3DLabels(
                dataList = dataList,
                config = pieConfig,
                progress = progress.value,
                fit = fit,
                textMeasurer = textMeasurer,
            )
        }
    }
}

/** Prints each slice's label at the middle of its top surface, skipping slices too thin to hold one. */
private fun DrawScope.drawPie3DLabels(
    dataList: List<PieData>,
    config: Pie3DChartConfig,
    progress: Float,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    val total = dataList.sumOf { slice -> slice.value.toDouble() }.toFloat()
    if (total <= 0f) {
        return
    }
    pie3DSweeps(dataList = dataList, progress = progress).forEach { sweep ->
        val share = sweep.data.value / total * PERCENT
        if (share < config.minimumSharePercentageForLabel || sweep.sweepDegrees <= 0f) {
            return@forEach
        }
        val text =
            when (config.labelContent) {
                Pie3DLabelContent.NONE -> return@forEach
                Pie3DLabelContent.NAME -> sweep.data.label
                Pie3DLabelContent.PERCENTAGE -> "${share.roundToInt()}%"
                Pie3DLabelContent.NAME_AND_PERCENTAGE -> "${sweep.data.label} ${share.roundToInt()}%"
            }
        val anchor = pie3DLabelAnchor(sweep = sweep, config = config, fit = fit)
        val layout = textMeasurer.measure(text = text, style = config.labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft =
                Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y - layout.size.height / HALF),
        )
    }
}
