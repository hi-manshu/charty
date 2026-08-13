package com.himanshoe.charty3d.scatter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.himanshoe.charty3d.internal.ProjectedPoint
import com.himanshoe.charty3d.internal.SceneFit
import com.himanshoe.charty3d.internal.hitTestPoints
import com.himanshoe.charty3d.internal.scatter3DBoxEdges
import com.himanshoe.charty3d.internal.scatter3DFit
import com.himanshoe.charty3d.internal.scatter3DFloorSquash
import com.himanshoe.charty3d.internal.scatter3DPoints
import com.himanshoe.charty3d.internal.scatter3DRanges
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.shadeForSide
import com.himanshoe.charty3d.scatter.config.Scatter3DChartConfig
import com.himanshoe.charty3d.scatter.data.Scatter3DPoint

private const val LABEL_GAP = 6f
private const val BOX_ALPHA = 0.35f
private const val HIGHLIGHT_OFFSET = 0.35f
private const val HIGHLIGHT_SPREAD = 1.15f
private const val SHADOW_SPREAD_AT_HEIGHT = 0.8f
private const val SHADOW_FADE_AT_HEIGHT = 0.65f

/**
 * A scatter plot of three variables, projected onto the canvas from a configurable viewing angle.
 *
 * ## Why this one is different
 * Everywhere else in `charty-3d` depth is decoration and a flat chart reads better. Here it is not:
 * `z` is a measured variable, so the third dimension is carrying data rather than costing accuracy
 * for the look of it. This is the projected chart with an analytical case, and the one to reach for
 * when you genuinely have three variables per observation.
 *
 * It still has the honest limits of a projection. Two observations can land on the same spot from
 * one angle and separate from another, so the viewing angle is part of what the reader sees rather
 * than a neutral choice. The wireframe box and the drop lines are both on by default because without
 * them a projected cloud is close to unreadable — they are what let the eye place a point in space
 * rather than on the page.
 *
 * Taps resolve **nearest point first**, so a tap selects the observation drawn on top.
 *
 * ```kotlin
 * Scatter3DChart(
 *     data = { observations },
 *     modifier = Modifier.fillMaxWidth().height(320.dp),
 *     onPointClick = { point -> println("${point.label}: ${point.x}, ${point.y}, ${point.z}") },
 * )
 * ```
 *
 * @param data Supplies the observations to plot; each axis is scaled to its own measured range, so
 *   no variable squashes the others.
 * @param modifier Modifier applied to the chart's canvas.
 * @param emptyContent Shown in place of the chart when [data] is empty; `null` uses the built-in
 *   placeholder.
 * @param color Fill for points that carry no colour of their own. Only a gradient's first stop is
 *   used, since a point is drawn as flat colour.
 * @param scatterConfig How the cloud is projected, sized, framed, and animated.
 * @param onPointClick Called with the observation under a tap, resolved nearest first; `null`
 *   disables tapping.
 */
@ChartyExperimental
@Composable
fun Scatter3DChart(
    data: () -> List<Scatter3DPoint>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    scatterConfig: Scatter3DChartConfig = Scatter3DChartConfig(),
    onPointClick: ((Scatter3DPoint) -> Unit)? = null,
) {
    val dataList by remember(data) { derivedStateOf { data() } }
    if (dataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val progress = rememberChartAnimation(animation = scatterConfig.animation)
    val ranges = remember(dataList) { scatter3DRanges(dataList) }
    val drawnPoints = remember { mutableListOf<ProjectedPoint>() }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .then(
                    if (onPointClick == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(dataList, scatterConfig) {
                            detectTapGestures { position -> drawnPoints.hitTestPoints(position)?.let(onPointClick) }
                        }
                    },
                ),
    ) {
        val fit = scatter3DFit(size = size, config = scatterConfig)
        val points =
            scatter3DPoints(
                dataList = dataList,
                config = scatterConfig,
                ranges = ranges,
                progress = progress.value,
                fit = fit,
            )
        drawnPoints.clear()
        drawnPoints.addAll(points)

        scatterConfig.plotBackground?.let { background ->
            drawRect(brush = Brush.verticalGradient(background.value), size = size)
        }
        if (scatterConfig.showBox) {
            drawScatter3DBox(config = scatterConfig, fit = fit)
        }
        val farToNear = points.sortedByDescending { point -> point.depth }
        if (scatterConfig.showFloorShadows) {
            val squash = scatter3DFloorSquash(scatterConfig)
            farToNear.fastForEach { point ->
                drawScatter3DShadow(point = point, config = scatterConfig, squash = squash)
            }
        }
        farToNear.fastForEach { point ->
            val fill = (point.data.color ?: color).value.first()
            if (scatterConfig.showDropLines) {
                drawLine(
                    color = fill.copy(alpha = scatterConfig.dropLineAlpha),
                    start = point.position,
                    end = point.floor,
                    strokeWidth = scatterConfig.boxStrokeWidth,
                )
            }
            drawScatter3DPoint(point = point, fill = fill, shaded = scatterConfig.sphereShading)
            if (scatterConfig.showLabels && point.data.label.isNotEmpty()) {
                drawScatter3DLabel(point = point, config = scatterConfig, textMeasurer = textMeasurer)
            }
        }
    }
}

/** Draws the wireframe box the cloud sits in, which is what lets the eye place a point in space. */
private fun DrawScope.drawScatter3DBox(
    config: Scatter3DChartConfig,
    fit: SceneFit,
) {
    val stroke = config.boxColor?.value?.first() ?: Color.Gray
    scatter3DBoxEdges().forEach { (from, to) ->
        drawLine(
            color = stroke.copy(alpha = BOX_ALPHA),
            start = fit.apply(config.projection.project(point = from, origin = Offset.Zero)),
            end = fit.apply(config.projection.project(point = to, origin = Offset.Zero)),
            strokeWidth = config.boxStrokeWidth,
        )
    }
}

/** Prints a point's label just above it, clear of the marker itself. */
private fun DrawScope.drawScatter3DLabel(
    point: ProjectedPoint,
    config: Scatter3DChartConfig,
    textMeasurer: TextMeasurer,
) {
    val layout = textMeasurer.measure(text = point.data.label, style = config.labelStyle)
    drawText(
        textLayoutResult = layout,
        topLeft =
            Offset(
                x = point.position.x - layout.size.width / 2f,
                y = point.position.y - point.radius - layout.size.height - LABEL_GAP,
            ),
    )
}

/**
 * Draws one observation, shaded as a lit sphere unless [shaded] is off.
 *
 * The highlight sits up and to the left of centre and the fill falls away to a darker rim, which is
 * the same light the solid charts shade their faces by — a chart mixing two lighting directions
 * reads as two charts.
 */
private fun DrawScope.drawScatter3DPoint(
    point: ProjectedPoint,
    fill: Color,
    shaded: Boolean,
) {
    if (!shaded) {
        drawCircle(color = fill, radius = point.radius, center = point.position)
        return
    }
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        shadeForSide(color = fill, side = FaceSide.TOP),
                        fill,
                        shadeForSide(color = fill, side = FaceSide.SIDE),
                    ),
                center =
                    Offset(
                        x = point.position.x - point.radius * HIGHLIGHT_OFFSET,
                        y = point.position.y - point.radius * HIGHLIGHT_OFFSET,
                    ),
                radius = point.radius * HIGHLIGHT_SPREAD,
            ),
        radius = point.radius,
        center = point.position,
    )
}

/**
 * Draws the shadow an observation casts on the floor of the box: an ellipse at its foot, squashed by
 * the viewing pitch, fading as the point rises away from the floor.
 *
 * The fade is what makes the shadow informative rather than decorative — a point resting on the
 * floor has a tight dark shadow, one high above it a faint diffuse one, which is the cue the eye
 * already knows how to read.
 */
private fun DrawScope.drawScatter3DShadow(
    point: ProjectedPoint,
    config: Scatter3DChartConfig,
    squash: Float,
) {
    val height = (point.floor.y - point.position.y).coerceAtLeast(0f)
    val lift = (height / size.height).coerceIn(0f, 1f)
    val spread = point.radius * (1f + lift * SHADOW_SPREAD_AT_HEIGHT)
    val alpha = config.floorShadowAlpha * (1f - lift * SHADOW_FADE_AT_HEIGHT)
    if (alpha <= 0f) {
        return
    }
    drawOval(
        color = Color.Black.copy(alpha = alpha),
        topLeft = Offset(x = point.floor.x - spread, y = point.floor.y - spread * squash),
        size = Size(width = spread * 2f, height = spread * 2f * squash),
    )
}
