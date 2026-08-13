package com.himanshoe.charty3d.bar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.internal.SceneFit
import com.himanshoe.charty3d.internal.bar3DFaces
import com.himanshoe.charty3d.internal.bar3DFit
import com.himanshoe.charty3d.internal.drawBar3DCategoryLabels
import com.himanshoe.charty3d.internal.drawBar3DFloor
import com.himanshoe.charty3d.internal.drawBar3DLabels
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.projection.shadeForSide
import com.himanshoe.charty3d.projection.sortedFarToNear

/**
 * A bar chart whose bars are drawn as solids, projected onto the canvas from a configurable viewing
 * angle.
 *
 * This is a projected chart, not a rendered one: there is no GPU scene, no depth buffer, and no
 * lighting model. Bars are extruded into boxes, their visible faces are sorted from far to near, and
 * the nearer ones are painted over the further ones. That is enough to read as three-dimensional on
 * every platform Compose runs on, and it costs nothing beyond the `Canvas` a flat chart already uses.
 *
 * ## Read this before choosing a 3D bar chart
 * Depth here is decoration, not a third variable — it carries no data. A tall bar in front can hide
 * a shorter one behind it, and with
 * [perspective][com.himanshoe.charty3d.projection.Projection3D.perspective] above `0f` two bars of
 * equal value draw at different lengths depending on where they sit. When the point is comparing
 * values precisely, a flat [com.himanshoe.charty.bar.BarChart] does it better; when the point is an
 * arresting figure on a dashboard or a slide, this does. Both are legitimate, and the default
 * projection is deliberately shallow with no perspective, so the comparison stays honest unless you
 * ask for more.
 *
 * Taps resolve **nearest face first**, so a tap always selects the bar you can see rather than one
 * occluded behind it.
 *
 * ```kotlin
 * Bar3DChart(
 *     data = { listOf(BarData(label = "Q1", value = 120f), BarData(label = "Q2", value = 180f)) },
 *     modifier = Modifier.fillMaxWidth().height(300.dp),
 *     color = ChartyColor.Solid(Color(0xFF6650A4)),
 *     barConfig = Bar3DChartConfig(projection = Projection3D.Isometric),
 *     onBarClick = { bar -> println(bar.label) },
 * )
 * ```
 *
 * @param data Supplies the bars to draw; read through `derivedStateOf`, so returning a new list
 *   redraws the chart.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is empty; `null` uses the built-in
 *   placeholder.
 * @param color Fill for every bar, unless a [BarData] carries its own. Only a gradient's first stop
 *   is used, because each face is shaded by its orientation rather than filled with a ramp.
 * @param barConfig How the bars are projected, sized, animated, and labelled.
 * @param onBarClick Called with the bar under a tap, resolved nearest face first; `null` disables
 *   tapping.
 */
@Composable
fun Bar3DChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    barConfig: Bar3DChartConfig = Bar3DChartConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList by remember(data) { derivedStateOf { data() } }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val progress = rememberChartAnimation(animation = barConfig.animation)
    val maxValue = remember(dataList) { dataList.maxOf { bar -> bar.value }.coerceAtLeast(1f) }

    val drawnFaces = remember { mutableListOf<ProjectedFace<BarData>>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onBarClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(dataList, barConfig) {
                            detectTapGestures { position ->
                                drawnFaces.hitTest(position)?.let(onBarClick)
                            }
                        }
                    },
                ),
    ) {
        val fit =
            bar3DFit(
                size = size,
                dataList = dataList,
                maxValue = maxValue,
                config = barConfig,
                progress = progress.value,
            )
        val faces =
            bar3DFaces(
                dataList = dataList,
                maxValue = maxValue,
                config = barConfig,
                progress = progress.value,
                fit = fit,
            )
        drawnFaces.clear()
        drawnFaces.addAll(faces)

        drawBar3DScene(
            faces = faces,
            dataList = dataList,
            maxValue = maxValue,
            color = color,
            config = barConfig,
            progress = progress.value,
            fit = fit,
            textMeasurer = textMeasurer,
        )
    }
}

/** Draws the floor, then every bar face from far to near, then the value labels on top. */
private fun DrawScope.drawBar3DScene(
    faces: List<ProjectedFace<BarData>>,
    dataList: List<BarData>,
    maxValue: Float,
    color: ChartyColor,
    config: Bar3DChartConfig,
    progress: Float,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    config.plotBackground?.let { background ->
        drawRect(brush = Brush.verticalGradient(background.value), size = size)
    }
    if (config.showFloor) {
        drawBar3DFloor(config = config, fit = fit)
    }
    faces.sortedFarToNear().fastForEach { face ->
        val base = face.payload.color ?: color
        drawPath(path = face.toPath(), color = shadeForSide(color = base.value.first(), side = face.side))
    }
    if (config.showCategoryLabels) {
        drawBar3DCategoryLabels(dataList = dataList, config = config, fit = fit, textMeasurer = textMeasurer)
    }
    if (config.showValueLabels) {
        drawBar3DLabels(
            dataList = dataList,
            maxValue = maxValue,
            config = config,
            progress = progress,
            fit = fit,
            textMeasurer = textMeasurer,
        )
    }
}
