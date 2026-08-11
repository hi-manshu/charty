package com.himanshoe.charty.point

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generatePointChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairOverlay
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
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
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData

private const val TAP_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_TAP_RADIUS_MULTIPLIER
private const val POINT_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER
private const val HIGHLIGHT_CIRCLE_OUTER_OFFSET = ChartConstants.DEFAULT_HIGHLIGHT_OUTER_OFFSET
private const val HIGHLIGHT_CIRCLE_INNER_OFFSET = ChartConstants.DEFAULT_HIGHLIGHT_INNER_OFFSET
private const val GUIDELINE_WIDTH = ChartConstants.DEFAULT_GUIDELINE_WIDTH
private const val GUIDELINE_ALPHA = ChartConstants.DEFAULT_GUIDELINE_ALPHA
private const val AXIS_STEPS = ChartConstants.DEFAULT_AXIS_STEPS
private const val MIN_ANIMATION_PROGRESS = ChartConstants.MIN_ANIMATION_PROGRESS
private const val MAX_ANIMATION_PROGRESS = ChartConstants.MAX_ANIMATION_PROGRESS

/**
 * Creates a modifier with click handling for point chart.
 */
private fun createChartModifier(
    modifier: Modifier,
    dataList: List<PointData>,
    pointConfig: PointChartConfig,
    pointBounds: List<Pair<Offset, PointData>>,
    onPointClick: ((PointData) -> Unit)?,
    onTooltipUpdate: (TooltipState?, PointData?) -> Unit,
): Modifier =
    if (onPointClick != null) {
        modifier.pointChartClickHandler(
            dataList = dataList,
            pointBounds = pointBounds,
            tapRadius = pointConfig.pointRadius * TAP_RADIUS_MULTIPLIER,
            onPointClick = onPointClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = { pointData, position ->
                createPointTooltipState(
                    content = pointConfig.tooltipFormatter(pointData),
                    position = position,
                    pointRadius = pointConfig.pointRadius,
                    tooltipPosition = pointConfig.tooltipPosition,
                    pointRadiusMultiplier = POINT_RADIUS_MULTIPLIER,
                )
            },
        )
    } else {
        modifier
    }

private fun DrawScope.drawAllPoints(
    dataList: List<PointData>,
    animationProgress: Float,
    chartContext: ChartContext,
    pointConfig: PointChartConfig,
    color: ChartyColor,
    pointBounds: MutableList<Pair<Offset, PointData>>,
    addToBounds: Boolean,
    textMeasurer: TextMeasurer,
) {
    drawReferenceBandIfNeeded(
        referenceBandConfig = pointConfig.referenceBand,
        chartContext = chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = textMeasurer,
    )
    dataList.fastForEachIndexed { index, point ->
        drawPointWithAnimation(
            point = point,
            index = index,
            dataListSize = dataList.size,
            animationProgress = animationProgress,
            chartContext = chartContext,
            pointConfig = pointConfig,
            color = color,
            pointBounds = pointBounds,
            addToBounds = addToBounds,
        )
    }
    if (pointConfig.markers.isNotEmpty()) {
        val markerPositions =
            List(dataList.size) { index ->
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(dataList[index].value),
                )
            }
        drawPersistentMarkers(
            chartContext = chartContext,
            markers = pointConfig.markers,
            pointPositions = markerPositions,
            valueLabelFor = { index -> formatMarkerValue(dataList[index].value) },
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawPointWithAnimation(
    point: PointData,
    index: Int,
    dataListSize: Int,
    animationProgress: Float,
    chartContext: ChartContext,
    pointConfig: PointChartConfig,
    color: ChartyColor,
    pointBounds: MutableList<Pair<Offset, PointData>>,
    addToBounds: Boolean,
) {
    val pointProgress = index.toFloat() / dataListSize
    val progressOffset = (animationProgress - pointProgress) * dataListSize
    val pointAnimationProgress =
        progressOffset.coerceIn(
            MIN_ANIMATION_PROGRESS,
            MAX_ANIMATION_PROGRESS,
        )

    val pointX = chartContext.calculateCenteredXPosition(index, dataListSize)
    val pointY = chartContext.convertValueToYPosition(point.value)
    val position = Offset(pointX, pointY)

    if (addToBounds) {
        pointBounds.add(position to point)
    }

    if (pointAnimationProgress > MIN_ANIMATION_PROGRESS) {
        drawCircle(
            brush = Brush.linearGradient(color.value),
            radius = pointConfig.pointRadius * pointAnimationProgress,
            center = position,
            alpha = pointConfig.pointAlpha * pointAnimationProgress,
        )
    }
}

private fun DrawScope.drawTooltipHighlight(
    tooltipState: TooltipState,
    pointBounds: List<Pair<Offset, PointData>>,
    pointConfig: PointChartConfig,
    color: ChartyColor,
    chartContext: com.himanshoe.charty.common.ChartContext,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    drawBubble: Boolean = true,
) {
    val clickedPosition =
        pointBounds
            .fastFirstOrNull { (_, data) ->
                pointConfig.tooltipFormatter(data) == tooltipState.content
            }?.first

    clickedPosition?.let { position ->
        drawLine(
            color = Color.Black.copy(alpha = GUIDELINE_ALPHA),
            start = Offset(position.x, chartContext.top),
            end = Offset(position.x, chartContext.bottom),
            strokeWidth = GUIDELINE_WIDTH,
        )
        drawCircle(
            color = Color.White,
            radius = pointConfig.pointRadius + HIGHLIGHT_CIRCLE_OUTER_OFFSET,
            center = position,
        )
        drawCircle(
            brush = Brush.linearGradient(color.value),
            radius = pointConfig.pointRadius + HIGHLIGHT_CIRCLE_INNER_OFFSET,
            center = position,
        )
    }

    if (drawBubble) {
        drawTooltip(
            tooltipState = tooltipState,
            config = pointConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * A composable function that displays a point chart, also known as a scatter plot.
 *
 * @param data A lambda function that returns a list of [PointData] to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the points.
 * @param pointConfig The configuration for the points' appearance and behavior.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onPointClick A lambda function invoked when a point is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * Example usage:
 * ```kotlin
 * PointChart(
 *     data = {
 *         listOf(
 *             PointData(label = "Mon", value = 30f),
 *             PointData(label = "Tue", value = 55f),
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 */
@Composable
fun PointChart(
    data: () -> List<PointData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    pointConfig: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((PointData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (PointData) -> Unit)? = null,
    crosshairContent: (@Composable (PointData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Point chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (minValue, maxValue) =
        remember(dataList, pointConfig.negativeValuesDrawMode) {
            val values = dataList.getValues()
            calculateMinValue(values) to calculateMaxValue(values)
        }

    val isBelowAxisMode = pointConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        rememberChartAnimation(
            animation = pointConfig.animation,
            initialValue = null,
            targetValue = MAX_ANIMATION_PROGRESS,
        )

    val tooltipManager = rememberTooltipManager<Offset, PointData>()
    val textMeasurer = rememberTextMeasurer()

    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<PointData>(pointConfig.crosshairConfig != null)

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generatePointChartDescription(data = it, minValue = minValue, maxValue = maxValue)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val chartModifier =
        modifier.then(
            buildPointChartModifier(
                crosshairManager = crosshairManager,
                dataList = dataList,
                tooltipManager = tooltipManager,
                pointConfig = pointConfig,
                onPointClick = onPointClick,
                crosshairConfig = pointConfig.crosshairConfig,
                interactionConfig = interactionConfig,
            ),
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = AXIS_STEPS,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawAllPoints(
                dataList = dataList,
                animationProgress = animationProgress.value,
                chartContext = chartContext,
                pointConfig = pointConfig,
                color = color,
                pointBounds = tooltipManager.bounds,
                addToBounds = onPointClick != null || crosshairManager != null,
                textMeasurer = textMeasurer,
            )

            drawReferenceLineIfNeeded(
                referenceLineConfig = pointConfig.referenceLine,
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                textMeasurer = textMeasurer,
            )

            drawPointTooltipAndCrosshair(
                crosshairState = animatedCrosshairState?.resolve(),
                tooltipManager = tooltipManager,
                pointConfig = pointConfig,
                crosshairConfig = pointConfig.crosshairConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                color = color,
                drawBubble = tooltipContent == null,
                drawCrosshairLabel = crosshairContent == null,
            )

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )
        }

        PointChartOverlays(
            tooltipManager = tooltipManager,
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            pointConfig = pointConfig,
            crosshairConfig = pointConfig.crosshairConfig,
            tooltipContent = tooltipContent,
            crosshairContent = crosshairContent,
        )
    }
}

@Composable
private fun BoxScope.PointChartOverlays(
    tooltipManager: TooltipManager<Offset, PointData>,
    crosshairManager: CrosshairManager<PointData>?,
    animatedCrosshairState: CrosshairState?,
    pointConfig: PointChartConfig,
    crosshairConfig: ChartCrosshairConfig?,
    tooltipContent: (@Composable (PointData) -> Unit)?,
    crosshairContent: (@Composable (PointData) -> Unit)?,
) {
    if (tooltipContent != null) {
        ChartTooltipOverlay(
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            config = pointConfig.tooltipConfig,
            modifier = Modifier.matchParentSize(),
            content = tooltipContent,
        )
    }

    if (crosshairContent != null && crosshairManager != null) {
        ChartCrosshairOverlay(
            item = crosshairManager.selectedItem,
            state = animatedCrosshairState,
            config = crosshairConfig?.tooltipConfig ?: pointConfig.tooltipConfig,
            modifier = Modifier.matchParentSize(),
            content = crosshairContent,
        )
    }
}

private fun DrawScope.drawPointTooltipAndCrosshair(
    crosshairState: CrosshairState?,
    tooltipManager: TooltipManager<Offset, PointData>,
    pointConfig: PointChartConfig,
    crosshairConfig: ChartCrosshairConfig?,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    color: ChartyColor,
    drawBubble: Boolean,
    drawCrosshairLabel: Boolean,
) {
    if (crosshairConfig == null) {
        tooltipManager.tooltipState?.let { state ->
            drawTooltipHighlight(
                tooltipState = state,
                pointBounds = tooltipManager.bounds,
                pointConfig = pointConfig,
                color = color,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                drawBubble = drawBubble,
            )
        }
    }

    crosshairState?.let { resolvedState ->
        crosshairConfig?.let { config ->
            drawLineChartCrosshair(
                state = resolvedState,
                config = config,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                chartColor = color,
                drawLabel = drawCrosshairLabel,
            )
        }
    }
}

private fun buildPointChartModifier(
    crosshairManager: CrosshairManager<PointData>?,
    dataList: List<PointData>,
    tooltipManager: TooltipManager<Offset, PointData>,
    pointConfig: PointChartConfig,
    onPointClick: ((PointData) -> Unit)?,
    crosshairConfig: ChartCrosshairConfig?,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    var mod: Modifier =
        if (crosshairManager != null) {
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = tooltipManager.bounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { pointData -> pointConfig.tooltipFormatter(pointData) },
                dismissOnRelease = crosshairConfig?.dismissOnRelease ?: true,
            )
        } else {
            createChartModifier(
                modifier = Modifier,
                dataList = dataList,
                pointConfig = pointConfig,
                pointBounds = tooltipManager.bounds,
                onPointClick = onPointClick,
                onTooltipUpdate = tooltipManager::updateTooltip,
            )
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
