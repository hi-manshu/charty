package com.himanshoe.charty3d.ribbon

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
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty3d.internal.SceneFit
import com.himanshoe.charty3d.internal.ribbon3DFaces
import com.himanshoe.charty3d.internal.ribbon3DFit
import com.himanshoe.charty3d.internal.ribbonLabelAnchor
import com.himanshoe.charty3d.internal.ribbonValueRange
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.projection.shadeForSide
import com.himanshoe.charty3d.projection.sortedFarToNear
import com.himanshoe.charty3d.ribbon.config.Ribbon3DChartConfig
import com.himanshoe.charty3d.ribbon.data.RibbonSegment

private const val LABEL_GAP = 8f
private const val HALF = 2f

/**
 * Several series drawn as ribbons, each on its own plane in depth.
 *
 * ## What the depth buys
 * Six lines crossing each other on one plane is a thicket; the same six pulled apart into ranked
 * bands can be read one at a time and still compared. Depth here encodes *which series* — not a
 * measured variable like [com.himanshoe.charty3d.scatter.Scatter3DChart]'s z, and not decoration like an
 * extruded bar's. It is the middle case, and it is a real answer to overplotting.
 *
 * The cost is that a series in front can hide one behind it, and that the ranking you pass in
 * decides who hides whom. Order the series deliberately — the one you most want read at the front.
 *
 * Taps resolve **nearest face first**, and report which series and which point was hit.
 *
 * ```kotlin
 * Ribbon3DChart(
 *     data = { listOf(LineGroup(label = "EU", values = eu), LineGroup(label = "US", values = us)) },
 *     modifier = Modifier.fillMaxWidth().height(320.dp),
 *     onSegmentClick = { segment -> println("${segment.seriesName} at ${segment.pointIndex}") },
 * )
 * ```
 *
 * @param data Supplies the series, drawn front to back in the order given.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is empty; `null` uses the built-in
 *   placeholder.
 * @param colors Palette cycled across the series. Empty takes the theme's.
 * @param ribbonConfig How the ribbons are projected, sized, filled, and animated.
 * @param onSegmentClick Called with the segment under a tap, resolved nearest face first; `null`
 *   disables tapping.
 */
@ChartyExperimental
@Composable
fun Ribbon3DChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: List<ChartyColor> = emptyList(),
    ribbonConfig: Ribbon3DChartConfig = Ribbon3DChartConfig(),
    onSegmentClick: ((RibbonSegment) -> Unit)? = null,
) {
    val dataList by remember(data) { derivedStateOf { data() } }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val progress = rememberChartAnimation(animation = ribbonConfig.animation)
    val range = remember(dataList) { ribbonValueRange(dataList) }
    val themePalette = List(dataList.size) { index -> ChartyThemeDefaults.seriesColor(index = index) }
    val palette = remember(colors, themePalette) { colors.ifEmpty { themePalette } }
    val drawnFaces = remember { mutableListOf<ProjectedFace<RibbonSegment>>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onSegmentClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(dataList, ribbonConfig) {
                            detectTapGestures { position -> drawnFaces.hitTest(position)?.let(onSegmentClick) }
                        }
                    },
                ),
    ) {
        val fit = ribbon3DFit(size = size, config = ribbonConfig)
        val faces =
            ribbon3DFaces(
                dataList = dataList,
                config = ribbonConfig,
                range = range,
                progress = progress.value,
                fit = fit,
            )
        drawnFaces.clear()
        drawnFaces.addAll(faces)

        ribbonConfig.plotBackground?.let { background ->
            drawRect(brush = Brush.verticalGradient(background.value), size = size)
        }
        faces.sortedFarToNear().fastForEach { face ->
            val base = palette[face.payload.seriesIndex % palette.size].value.first()
            drawPath(path = face.toPath(), color = shadeForSide(color = base, side = face.side))
        }
        if (ribbonConfig.showSeriesLabels) {
            drawRibbonLabels(
                dataList = dataList,
                config = ribbonConfig,
                fit = fit,
                textMeasurer = textMeasurer,
            )
        }
    }
}

/** Prints each series' name off the near end of its ribbon. */
private fun DrawScope.drawRibbonLabels(
    dataList: List<LineGroup>,
    config: Ribbon3DChartConfig,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    dataList.forEachIndexed { index, group ->
        val anchor =
            ribbonLabelAnchor(
                seriesIndex = index,
                seriesCount = dataList.size,
                config = config,
                fit = fit,
            )
        val layout = textMeasurer.measure(text = group.label, style = config.labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = anchor.x - layout.size.width - LABEL_GAP, y = anchor.y - layout.size.height / HALF),
        )
    }
}
