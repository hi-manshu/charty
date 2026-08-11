package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateLineChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshairOverlay
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.TooltipManager
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.createAreaBrush
import com.himanshoe.charty.line.ext.createAreaPath
import com.himanshoe.charty.line.ext.createLineBrush
import com.himanshoe.charty.line.ext.createLinePath
import com.himanshoe.charty.line.internal.area.createAreaChartModifier
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair

private const val DEFAULT_AXIS_STEPS = 6
private const val HIGHLIGHT_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_LINE_WIDTH = 1.5f
private const val HIGHLIGHT_CIRCLE_OUTER_PADDING = 3f
private const val HIGHLIGHT_CIRCLE_INNER_PADDING = 2f

/**
 * Parameters for drawing area chart
 */
private data class AreaChartDrawParams(
    val dataList: List<LineData>,
    val pointPositions: List<Offset>,
    val baselineY: Float,
    val config: LineChartConfig,
    val color: ChartyColor,
    val fillAlpha: Float,
    val animationProgress: Float,
    val chartContext: com.himanshoe.charty.common.ChartContext,
    val textMeasurer: androidx.compose.ui.text.TextMeasurer,
    val onBarBoundCalculated: (Pair<Offset, LineData>) -> Unit,
)

/**
 * A composable function that displays an area chart.
 *
 * Example:
 * ```kotlin
 * AreaChart(
 *     data = {
 *         listOf(
 *             LineData(label = "Jan", value = 20f),
 *             LineData(label = "Feb", value = 45f),
 *             LineData(label = "Mar", value = 30f),
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineData] points to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the filled area.
 * @param lineConfig The configuration for the line and its points.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onPointClick A lambda function invoked when a point on the line is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [LineData].
 */
@Composable
fun AreaChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                ChartyColors.Blue,
                ChartyColors.BlueAlpha30,
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (LineData) -> Unit)? = null,
    crosshairContent: (@Composable (LineData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Area chart data cannot be empty" }
    val fillAlpha = lineConfig.fillAlpha

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberAreaValueRange(dataList, lineConfig.negativeValuesDrawMode)
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(lineConfig.animation)
    val tooltipManager = rememberTooltipManager<Offset, LineData>()
    val textMeasurer = rememberTextMeasurer()

    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineData>(lineConfig.crosshairConfig != null)

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateLineChartDescription(data = it, minValue = minValue, maxValue = maxValue)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val chartModifier =
        modifier.then(
            buildAreaModifier(
                crosshairManager = crosshairManager,
                dataList = dataList,
                tooltipManager = tooltipManager,
                lineConfig = lineConfig,
                onPointClick = onPointClick,
                interactionConfig = interactionConfig,
            ),
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            tooltipManager.clearBounds()
            val pointPositions = calculatePointPositions(dataList, chartContext) { tooltipManager.bounds.add(it) }
            val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)
            drawAreaChart(
                params =
                    AreaChartDrawParams(
                        dataList = dataList,
                        pointPositions = pointPositions,
                        baselineY = baselineY,
                        config = lineConfig,
                        color = color,
                        fillAlpha = fillAlpha,
                        animationProgress = animationProgress.value,
                        chartContext = chartContext,
                        textMeasurer = textMeasurer,
                        onBarBoundCalculated = { if (onPointClick != null) tooltipManager.bounds.add(it) },
                    ),
            )
            if (crosshairManager == null) {
                drawTooltipHighlightIfNeeded(
                    tooltipManager.tooltipState,
                    lineConfig,
                    tooltipManager.bounds,
                    chartContext,
                    color,
                )
                if (tooltipContent == null) {
                    drawTooltipIfNeeded(tooltipManager.tooltipState, lineConfig, textMeasurer, chartContext)
                }
            }
            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )
            animatedCrosshairState?.resolve()?.let { crosshairState ->
                lineConfig.crosshairConfig?.let { crosshairConfig ->
                    drawLineChartCrosshair(
                        crosshairState,
                        crosshairConfig,
                        chartContext,
                        textMeasurer,
                        color,
                        drawLabel = crosshairContent == null,
                    )
                }
            }
        }

        AreaChartOverlays(
            tooltipManager = tooltipManager,
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            lineConfig = lineConfig,
            tooltipContent = tooltipContent,
            crosshairContent = crosshairContent,
        )
    }
}

@Composable
private fun BoxScope.AreaChartOverlays(
    tooltipManager: TooltipManager<Offset, LineData>,
    crosshairManager: CrosshairManager<LineData>?,
    animatedCrosshairState: CrosshairState?,
    lineConfig: LineChartConfig,
    tooltipContent: (@Composable (LineData) -> Unit)?,
    crosshairContent: (@Composable (LineData) -> Unit)?,
) {
    if (tooltipContent != null) {
        ChartTooltipOverlay(
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            config = lineConfig.tooltipConfig,
            modifier = Modifier.matchParentSize(),
            content = tooltipContent,
        )
    }

    if (crosshairContent != null && crosshairManager != null) {
        ChartCrosshairOverlay(
            item = crosshairManager.selectedItem,
            state = animatedCrosshairState,
            config = lineConfig.crosshairConfig?.tooltipConfig ?: lineConfig.tooltipConfig,
            modifier = Modifier.matchParentSize(),
            content = crosshairContent,
        )
    }
}

private fun buildAreaModifier(
    crosshairManager: CrosshairManager<LineData>?,
    dataList: List<LineData>,
    tooltipManager: TooltipManager<Offset, LineData>,
    lineConfig: LineChartConfig,
    onPointClick: ((LineData) -> Unit)?,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    var mod: Modifier =
        if (crosshairManager != null) {
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = tooltipManager.bounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = lineConfig.tooltipFormatter,
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        } else if (onPointClick != null) {
            createAreaChartModifier(
                modifier = Modifier,
                onPointClick = onPointClick,
                dataList = dataList,
                lineConfig = lineConfig,
                pointBounds = tooltipManager.bounds,
                onTooltipUpdate = tooltipManager::updateTooltip,
            )
        } else {
            Modifier
        }
    if (interactionConfig.brushSelectionState != null) {
        mod =
            mod.chartBrushSelectionHandler(
                dataList = dataList,
                brushState = interactionConfig.brushSelectionState,
                onRangeSelect = interactionConfig.onRangeSelect,
            )
    }
    if (interactionConfig.viewPortState != null) {
        mod = mod.chartZoomAndPan(interactionConfig.viewPortState)
    }
    return mod
}

@Composable
private fun rememberAreaValueRange(
    dataList: List<LineData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val minValue =
            com.himanshoe.charty.common.util
                .calculateMinValue(values)
        val maxValue =
            com.himanshoe.charty.common.util
                .calculateMaxValue(values)
        minValue to maxValue
    }

private fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )

private fun calculatePointPositions(
    dataList: List<LineData>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    onPointCalculated: (Pair<Offset, LineData>) -> Unit,
): List<Offset> =
    dataList.fastMapIndexed { index, point ->
        val position =
            Offset(
                x = chartContext.calculateCenteredXPosition(index, dataList.size),
                y = chartContext.convertValueToYPosition(point.value),
            )
        onPointCalculated(position to point)
        position
    }

private fun calculateBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: com.himanshoe.charty.common.ChartContext,
): Float =
    if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaChart(params: AreaChartDrawParams) {
    if (params.pointPositions.isEmpty()) {
        return
    }

    drawReferenceBandIfNeeded(
        referenceBandConfig = params.config.referenceBand,
        chartContext = params.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = params.textMeasurer,
    )

    val startX = params.pointPositions.first().x
    val endX = params.pointPositions.last().x
    val clipRight = startX + (endX - startX) * params.animationProgress

    val areaPath =
        createAreaPath(
            pointPositions = params.pointPositions,
            baselineY = params.baselineY,
            smoothCurve = params.config.smoothCurve,
        )
    val areaBrush =
        createAreaBrush(
            color = params.color,
            fillAlpha = params.fillAlpha,
            chartTop = params.chartContext.top,
            chartBottom = params.chartContext.bottom,
        )
    val linePath = createLinePath(pointPositions = params.pointPositions, smoothCurve = params.config.smoothCurve)
    val lineBrush = createLineBrush(params.color)

    clipRect(right = clipRight) {
        drawPath(path = areaPath, brush = areaBrush, style = Fill)

        drawPath(
            path = linePath,
            brush = lineBrush,
            style = Stroke(width = params.config.lineWidth, cap = params.config.strokeCap),
        )
    }

    if (params.config.showPoints) {
        drawAreaPoints(
            pointPositions = params.pointPositions,
            lineBrush = lineBrush,
            config = params.config,
            animationProgress = params.animationProgress,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaPoints(
    pointPositions: List<Offset>,
    lineBrush: Brush,
    config: LineChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1).coerceAtLeast(1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = lineBrush,
                radius = config.pointRadius,
                center = position,
                alpha = config.pointAlpha,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipHighlightIfNeeded(
    tooltipState: TooltipState?,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    color: ChartyColor,
) {
    tooltipState?.let { state ->
        val clickedPosition =
            pointBounds
                .fastFirstOrNull { (_, data) ->
                    lineConfig.tooltipFormatter(data) == state.content
                }?.first

        clickedPosition?.let { position ->
            drawLine(
                color = Color.Black.copy(alpha = HIGHLIGHT_LINE_ALPHA),
                start = Offset(position.x, chartContext.top),
                end = Offset(position.x, chartContext.bottom),
                strokeWidth = HIGHLIGHT_LINE_WIDTH,
            )
            drawCircle(
                color = Color.White,
                radius = lineConfig.pointRadius + HIGHLIGHT_CIRCLE_OUTER_PADDING,
                center = position,
            )
            drawCircle(
                brush = Brush.linearGradient(color.value),
                radius = lineConfig.pointRadius + HIGHLIGHT_CIRCLE_INNER_PADDING,
                center = position,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    lineConfig: LineChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    chartContext: com.himanshoe.charty.common.ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = lineConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
