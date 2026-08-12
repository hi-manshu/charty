package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AutoScrollToLatestEffect
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateLineChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawSelectionColumnIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.TooltipManager
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.calculatePointPositions
import com.himanshoe.charty.line.internal.line.drawAnimatedPoints
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.line.drawLineChartTooltip
import com.himanshoe.charty.line.internal.line.drawSmoothLine
import com.himanshoe.charty.line.internal.line.drawStepLineSegments
import com.himanshoe.charty.line.internal.line.drawStraightLineSegments
import com.himanshoe.charty.line.internal.line.lineChartInteractionHandler

/**
 * A composable function that displays an interactive line chart.
 *
 * @param data A lambda returning the list of [LineData] points to display.
 * @param modifier Modifier applied to the chart composable.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The line and point colour defined by [ChartyColor].
 * @param lineConfig Appearance and behaviour configuration for the line.
 * @param scaffoldConfig Axes and label configuration for the chart scaffold.
 * @param onPointClick Invoked when the user taps a data point.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it. It
 *   is a drag gesture that leaves taps alone, so tap-to-tooltip and the chart's click callback
 *   keep working alongside it; streaming scrollback ([ChartInteractionConfig.streamingState])
 *   does not, because the crosshair owns the drag.
 * @param tooltip How the tap tooltip is shown: [ChartTooltip.canvas] (the built-in bubble, styled via
 *   [LineChartConfig.tooltipConfig]), [ChartTooltip.compose] (your Composable, e.g. `PillTooltip`), or
 *   [ChartTooltip.none].
 *
 * Example:
 * ```kotlin
 * LineChart(
 *     data = { priceData },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     lineConfig = LineChartConfig(smoothCurve = true),
 * )
 * ```
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<LineData>? = null,
    tooltip: ChartTooltip<LineData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val effectiveLineConfig = crosshair?.let { lineConfig.copy(crosshairConfig = it.config) } ?: lineConfig
    val activeCrosshair = crosshair ?: lineConfig.crosshairConfig?.let { ChartCrosshair<LineData>(config = it) }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = lineConfig.animation,
            visibleWindow = lineConfig.visibleWindow,
            downsampleThreshold = lineConfig.downsampleThreshold,
            downsampleValue = { it.value },
            displayData = {
                rememberAnimatedLineData(
                    dataList = it,
                    animation = lineConfig.animation,
                    enabled = lineConfig.animateValueChanges,
                )
            },
            describe = { series, min, max ->
                generateLineChartDescription(data = series, minValue = min, maxValue = max)
            },
        ) { windowed, _ ->
            rememberLineValueRange(
                dataList = windowed,
                negativeValuesDrawMode = lineConfig.negativeValuesDrawMode,
            )
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val animationProgress = chartState.animationProgress
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val tooltipManager = rememberTooltipManager<Offset, LineData>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()

    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineData>(
            enabled = effectiveLineConfig.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
            streamingState = interactionConfig.streamingState,
        )

    AutoScrollToLatestEffect(
        viewPortState = interactionConfig.viewPortState,
        fullDataSize = fullDataList.size,
        enabled = interactionConfig.autoScrollToLatest,
    )

    val interactionModifier =
        Modifier.lineChartInteractionHandler(
            dataList = dataList,
            lineConfig = effectiveLineConfig,
            pointBounds = tooltipManager.bounds,
            onPointClick = onPointClick,
            onTooltipStateChange = tooltipManager::updateTooltip,
            crosshairManager = crosshairManager,
            brushSelectionState = interactionConfig.brushSelectionState,
            onRangeSelect = interactionConfig.onRangeSelect,
        )

    val zoomModifier = interactionConfig.viewPortState?.let { Modifier.chartZoomAndPan(it) } ?: Modifier
    val pan = interactionConfig.streamingPan(streaming = chartState.streaming, orientation = ChartOrientation.VERTICAL)

    Box(modifier = modifier.then(interactionModifier).then(zoomModifier).then(pan)) {
        ChartScaffold(
            accessibility = lineChartAccessibility(chartDescription = chartState.description, dataList = dataList),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig = lineAxisConfig(minValue = minValue, maxValue = maxValue, drawAxisAtZero = isBelowAxisMode),
            config = scaffoldConfig,
            streaming = interactionConfig.streamingRender(chartState.streaming),
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawReferenceBandIfNeeded(
                referenceBandConfig = lineConfig.referenceBand,
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                textMeasurer = textMeasurer,
            )

            val pointPositions = chartContext.calculatePointPositions(displayList)

            if (onPointClick != null || crosshairManager != null) {
                pointPositions.fastForEachIndexed { index, position ->
                    tooltipManager.bounds.add(position to dataList[index])
                }
            }

            drawSelectionColumnIfNeeded(
                enabled = lineConfig.highlightSelectedColumn,
                selectedX =
                    tooltipManager.bounds
                        .fastFirstOrNull { it.second == tooltipManager.selectedItem }
                        ?.first
                        ?.x,
                columnWidth = lineConfig.selectionColumnWidth ?: (chartContext.width / dataList.size),
                color = lineConfig.selectionColumnColor,
                chartContext = chartContext,
            )

            drawLineContent(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress.value,
                chartContext = chartContext,
                dataList = dataList,
                textMeasurer = textMeasurer,
            )

            drawReferenceLineIfNeeded(
                referenceLineConfig = lineConfig.referenceLine,
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                textMeasurer = textMeasurer,
            )

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )

            drawLineCrosshairAndTooltip(
                crosshairState = animatedCrosshairState?.resolve(),
                tooltipManager = tooltipManager,
                lineConfig = effectiveLineConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                color = color,
                drawBubble = tooltip.isCanvas(),
                drawCrosshairLabel = false,
            )
        }

        LineChartOverlays(
            tooltipManager = tooltipManager,
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            tooltip = tooltip,
            crosshair = activeCrosshair,
        )
    }
}

private fun lineChartAccessibility(
    chartDescription: String?,
    dataList: List<LineData>,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = chartDescription,
        dataPointDescriptions = buildDataPointDescriptions(dataList.getLabels(), dataList.getValues()),
    )

@Composable
private fun BoxScope.LineChartOverlays(
    tooltipManager: TooltipManager<Offset, LineData>,
    crosshairManager: CrosshairManager<LineData>?,
    animatedCrosshairState: CrosshairState?,
    tooltip: ChartTooltip<LineData>,
    crosshair: ChartCrosshair<LineData>?,
) {
    ChartTooltipHost(
        tooltip = tooltip,
        item = tooltipManager.selectedItem,
        anchor = tooltipManager.tooltipState,
        modifier = Modifier.matchParentSize(),
    )

    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairManager?.selectedItem,
            state = animatedCrosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}

private fun DrawScope.drawLineCrosshairAndTooltip(
    crosshairState: CrosshairState?,
    tooltipManager: TooltipManager<Offset, LineData>,
    lineConfig: LineChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    color: ChartyColor,
    drawBubble: Boolean,
    drawCrosshairLabel: Boolean,
) {
    crosshairState?.let { resolvedState ->
        lineConfig.crosshairConfig?.let { crosshairConfig ->
            drawLineChartCrosshair(
                state = resolvedState,
                config = crosshairConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                chartColor = color,
                drawLabel = drawCrosshairLabel,
            )
        }
    }

    tooltipManager.tooltipState?.let { state ->
        drawLineChartTooltip(
            tooltipState = state,
            pointBounds = tooltipManager.bounds,
            color = color,
            lineConfig = lineConfig,
            chartContext = chartContext,
            textMeasurer = textMeasurer,
            drawBubble = drawBubble,
        )
    }
}

private fun DrawScope.drawLineContent(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineConfig: LineChartConfig,
    animationProgress: Float,
    chartContext: ChartContext,
    dataList: List<LineData>,
    textMeasurer: TextMeasurer,
) {
    when (resolveLineInterpolation(lineConfig)) {
        LineInterpolation.SMOOTH ->
            drawSmoothLine(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress,
            )

        LineInterpolation.STEP ->
            drawStepLineSegments(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress,
            )

        LineInterpolation.LINEAR ->
            drawStraightLineSegments(
                pointPositions = pointPositions,
                color = color,
                lineConfig = lineConfig,
                animationProgress = animationProgress,
            )
    }

    if (lineConfig.showPoints) {
        drawAnimatedPoints(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    }

    drawPersistentMarkers(
        chartContext = chartContext,
        markers = lineConfig.markers,
        pointPositions = pointPositions,
        valueLabelFor = { index -> formatMarkerValue(dataList[index].value) },
        textMeasurer = textMeasurer,
    )
}

private const val LINE_AXIS_STEPS = 6

/** Builds the line chart's value axis configuration. */
private fun lineAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = LINE_AXIS_STEPS,
        drawAxisAtZero = drawAxisAtZero,
    )

/**
 * Resolves the effective [LineInterpolation]: [LineChartConfig.interpolation] wins unless it is
 * [LineInterpolation.LINEAR], in which case the legacy [LineChartConfig.smoothCurve] flag decides
 * between [LineInterpolation.SMOOTH] and [LineInterpolation.LINEAR].
 */
internal fun resolveLineInterpolation(config: LineChartConfig): LineInterpolation =
    when {
        config.interpolation != LineInterpolation.LINEAR -> config.interpolation
        config.smoothCurve -> LineInterpolation.SMOOTH
        else -> LineInterpolation.LINEAR
    }

/**
 * Remembers the min/max value range for [dataList], recomputed only when the data or
 * [negativeValuesDrawMode] changes.
 */
@Composable
private fun rememberLineValueRange(
    dataList: List<LineData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }

/**
 * Returns [dataList] with each point's value tweened toward its target whenever the data changes, so
 * the line glides to its new shape. When [enabled] is `false` or [animation] is disabled the list is
 * returned unchanged. See [rememberAnimatedValues].
 */
@Composable
private fun rememberAnimatedLineData(
    dataList: List<LineData>,
    animation: Animation,
    enabled: Boolean,
): List<LineData> {
    val animatedValues =
        rememberAnimatedValues(
            targetValues = dataList.fastMap { it.value },
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        dataList.fastMapIndexed { index, point -> point.copy(value = animatedValues[index]) }
    }
}
