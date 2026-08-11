package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AutoScrollToLatestEffect
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateLineChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshairOverlay
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.TooltipManager
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.calculatePointPositions
import com.himanshoe.charty.line.internal.line.drawAnimatedPoints
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.line.drawLineChartTooltip
import com.himanshoe.charty.line.internal.line.drawSmoothLine
import com.himanshoe.charty.line.internal.line.drawStraightLineSegments
import com.himanshoe.charty.line.internal.line.lineChartInteractionHandler

/**
 * A composable function that displays an interactive line chart.
 *
 * @param data A lambda returning the list of [LineData] points to display.
 * @param modifier Modifier applied to the chart composable.
 * @param color The line and point colour defined by [ChartyColor].
 * @param lineConfig Appearance and behaviour configuration for the line.
 * @param scaffoldConfig Axes and label configuration for the chart scaffold.
 * @param onPointClick Invoked when the user taps a data point.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [LineData].
 * @param crosshairContent An optional composable slot for rendering a custom crosshair label. When
 *   provided (and a crosshair is configured via [lineConfig]), it replaces the default canvas
 *   crosshair label and is invoked with the [LineData] under the dragging finger.
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
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (LineData) -> Unit)? = null,
    crosshairContent: (@Composable (LineData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Line chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberLineValueRange(dataList, lineConfig.negativeValuesDrawMode)
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(lineConfig.animation)
    val displayList = rememberAnimatedLineData(dataList, lineConfig.animation, lineConfig.animateValueChanges)

    val tooltipManager = rememberTooltipManager<Offset, LineData>()
    val textMeasurer = rememberTextMeasurer()

    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineData>(lineConfig.crosshairConfig != null)

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateLineChartDescription(it, minValue, maxValue)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )
    AutoScrollToLatestEffect(interactionConfig.viewPortState, fullDataList.size, interactionConfig.autoScrollToLatest)

    val interactionModifier =
        Modifier.lineChartInteractionHandler(
            dataList = dataList,
            lineConfig = lineConfig,
            pointBounds = tooltipManager.bounds,
            onPointClick = onPointClick,
            onTooltipStateChange = tooltipManager::updateTooltip,
            crosshairManager = crosshairManager,
            brushSelectionState = interactionConfig.brushSelectionState,
            onRangeSelect = interactionConfig.onRangeSelect,
        )

    val zoomModifier = interactionConfig.viewPortState?.let { Modifier.chartZoomAndPan(it) } ?: Modifier

    Box(modifier = modifier.then(interactionModifier).then(zoomModifier)) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig = lineAxisConfig(minValue = minValue, maxValue = maxValue, drawAxisAtZero = isBelowAxisMode),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

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

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)

            drawLineCrosshairAndTooltip(
                crosshairState = animatedCrosshairState?.resolve(),
                tooltipManager = tooltipManager,
                lineConfig = lineConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                color = color,
                drawBubble = tooltipContent == null,
                drawCrosshairLabel = crosshairContent == null,
            )
        }

        LineChartOverlays(
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
private fun BoxScope.LineChartOverlays(
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

    if (lineConfig.crosshairConfig == null) {
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
    if (lineConfig.smoothCurve) {
        drawSmoothLine(
            pointPositions = pointPositions,
            color = color,
            lineConfig = lineConfig,
            animationProgress = animationProgress,
        )
    } else {
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
