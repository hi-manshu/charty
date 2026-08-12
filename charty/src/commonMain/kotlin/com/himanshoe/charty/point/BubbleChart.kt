package com.himanshoe.charty.point

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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBubbleChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData

/**
 * A composable function that displays a bubble chart.
 *
 * @param data A lambda function that returns a list of [BubbleData] to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The color or color scheme for the bubbles.
 * @param config The configuration for the bubbles' appearance.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param minBubbleRadius The minimum radius for a bubble in pixels.
 * @param onBubbleClick A lambda function invoked when a bubble is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * Example usage:
 * ```kotlin
 * BubbleChart(
 *     data = {
 *         listOf(
 *             BubbleData("Product A", yValue = 50f, size = 100f),
 *             BubbleData("Product B", yValue = 75f, size = 200f),
 *         )
 *     },
 *     color = ChartyColor.Gradient(listOf(Color(0xFF2196F3), Color(0xFF4CAF50))),
 * )
 * ```
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun BubbleChart(
    data: () -> List<BubbleData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    config: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    minBubbleRadius: Float = 10f,
    onBubbleClick: ((BubbleData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<BubbleData>? = null,
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(config.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }
    val crosshairConfig = crosshair?.config ?: config.crosshairConfig
    val activeCrosshair = crosshair ?: config.crosshairConfig?.let { ChartCrosshair<BubbleData>(config = it) }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = config.animation,
            visibleWindow = config.visibleWindow,
            displayData = {
                rememberAnimatedBubbleData(
                    dataList = it,
                    animation = config.animation,
                    enabled = config.animateValueChanges,
                )
            },
            describe = { series, _, _ -> generateBubbleChartDescription(series) },
        ) { windowed, _ ->
            remember(windowed) {
                calculateBubbleSizeInfo(windowed).let { it.minValue to it.maxValue }
            }
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue

    val bubbleBounds = remember { mutableListOf<BubbleBounds>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BubbleData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BubbleData>(
            enabled = crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
        )
    val sizeInfo = remember(dataList) { calculateBubbleSizeInfo(dataList) }
    val textMeasurer = rememberTextMeasurer()

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = chartState.animationProgress

    val gestureBase =
        when {
            crosshairManager != null ->
                Modifier.chartCrosshairHandler(
                    dataList = dataList,
                    pointBounds = crosshairBounds,
                    onCrosshairUpdate = crosshairManager::update,
                    labelFormatter = { bubble -> "${bubble.label}: ${bubble.yValue}" },
                    dismissOnRelease = crosshairConfig?.dismissOnRelease ?: true,
                )
            else ->
                createBubbleClickModifier(
                    dataList = dataList,
                    bubbleBounds = bubbleBounds,
                    onBubbleClick = onBubbleClick,
                )
        }
    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(gestureBase),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.streamingPan(streaming = chartState.streaming, orientation = ChartOrientation.VERTICAL)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility = bubbleChartAccessibility(chartDescription = chartState.description, dataList = dataList),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 6,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            bubbleBounds.clear()
            populateBubbleCrosshairBounds(
                chartContext = chartContext,
                dataList = dataList,
                enabled = crosshairManager != null,
                crosshairBounds = crosshairBounds,
            )

            drawAllBubbles(
                dataList = displayList,
                chartContext = chartContext,
                sizeInfo = sizeInfo,
                minBubbleRadius = minBubbleRadius,
                config = config,
                color = color,
                animationProgress = animationProgress.value,
                onBubbleClick =
                    onBubbleClick.takeIf { crosshairManager == null },
                bubbleBounds = bubbleBounds,
            )

            drawPersistentMarkers(
                chartContext = chartContext,
                markers = config.markers,
                pointPositions = bubbleMarkerPositions(chartContext = chartContext, dataList = displayList),
                valueLabelFor = { index -> formatMarkerValue(displayList[index].yValue) },
                textMeasurer = textMeasurer,
            )

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )

            drawBubbleCrosshair(
                crosshairState = animatedCrosshairState?.resolve(),
                crosshairConfig = crosshairConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                color = color,
                drawLabel = false,
            )
        }

        BubbleChartOverlay(
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            crosshair = activeCrosshair,
        )
    }
}

/** Builds the bubble chart's accessibility payload: a chart summary plus one entry per drawn bubble. */
private fun bubbleChartAccessibility(
    chartDescription: String?,
    dataList: List<BubbleData>,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = chartDescription,
        dataPointDescriptions =
            buildDataPointDescriptions(
                labels = dataList.fastMap { it.label },
                values = dataList.fastMap { it.yValue },
            ),
    )

/**
 * Refreshes the crosshair hit-test anchors from the real (untweened) data, so dragging always snaps
 * to true point positions. Clears them and does nothing more when [enabled] is `false`.
 */
private fun populateBubbleCrosshairBounds(
    chartContext: ChartContext,
    dataList: List<BubbleData>,
    enabled: Boolean,
    crosshairBounds: MutableList<Pair<Offset, BubbleData>>,
) {
    crosshairBounds.clear()
    if (!enabled) {
        return
    }
    bubbleMarkerPositions(chartContext = chartContext, dataList = dataList)
        .fastForEachIndexed { index, position -> crosshairBounds.add(position to dataList[index]) }
}

/**
 * Returns [dataList] with each bubble's y value tweened toward its target whenever the data changes,
 * so bubbles glide to their new heights. Bubble sizes are untouched — they encode a separate
 * dimension. When [enabled] is `false` or [animation] is disabled the list is returned unchanged.
 */
@Composable
private fun rememberAnimatedBubbleData(
    dataList: List<BubbleData>,
    animation: Animation,
    enabled: Boolean,
): List<BubbleData> {
    val animatedValues =
        rememberAnimatedValues(
            targetValues = dataList.fastMap { it.yValue },
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        dataList.fastMapIndexed { index, bubble -> bubble.copy(yValue = animatedValues[index]) }
    }
}

/**
 * Pixel centres of every drawn bubble, ordered the same as [dataList] so a marker index of `-1` lands
 * on the rightmost bubble.
 */
private fun bubbleMarkerPositions(
    chartContext: ChartContext,
    dataList: List<BubbleData>,
): List<Offset> =
    List(dataList.size) { index ->
        Offset(
            x = chartContext.calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y = chartContext.convertValueToYPosition(dataList[index].yValue),
        )
    }

@Composable
private fun BoxScope.BubbleChartOverlay(
    crosshairManager: CrosshairManager<BubbleData>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<BubbleData>?,
) {
    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairManager?.selectedItem,
            state = animatedCrosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}

private fun DrawScope.drawBubbleCrosshair(
    crosshairState: CrosshairState?,
    crosshairConfig: ChartCrosshairConfig?,
    chartContext: ChartContext,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    color: ChartyColor,
    drawLabel: Boolean,
) {
    crosshairState?.let { state ->
        crosshairConfig?.let { cfg ->
            drawLineChartCrosshair(state, cfg, chartContext, textMeasurer, color, drawLabel = drawLabel)
        }
    }
}

private fun DrawScope.drawAllBubbles(
    dataList: List<BubbleData>,
    chartContext: ChartContext,
    sizeInfo: BubbleSizeInfo,
    minBubbleRadius: Float,
    config: PointChartConfig,
    color: ChartyColor,
    animationProgress: Float,
    onBubbleClick: ((BubbleData) -> Unit)?,
    bubbleBounds: MutableList<BubbleBounds>,
) {
    dataList.fastForEachIndexed { index, bubble ->
        val bubbleProgress = index.toFloat() / dataList.size
        val bubbleAnimationProgress = ((animationProgress - bubbleProgress) * dataList.size).coerceIn(0f, 1f)

        val bubbleX = chartContext.calculateCenteredXPosition(index, dataList.size)
        val bubbleY = chartContext.convertValueToYPosition(bubble.yValue)

        val bubbleRadius =
            calculateBubbleRadius(
                bubbleSize = bubble.size,
                minSize = sizeInfo.minSize,
                sizeRange = sizeInfo.sizeRange,
                minBubbleRadius = minBubbleRadius,
                maxBubbleRadius = config.pointRadius,
            )

        val bubbleColor =
            when (color) {
                is ChartyColor.Solid -> color.color
                is ChartyColor.Gradient -> color.colors[index % color.colors.size]
            }

        if (bubbleAnimationProgress > 0f) {
            val center = Offset(bubbleX, bubbleY)
            val animatedRadius = bubbleRadius * bubbleAnimationProgress

            if (onBubbleClick != null) {
                bubbleBounds.add(BubbleBounds(center = center, radius = animatedRadius, data = bubble))
            }

            drawCircle(
                color = bubbleColor.copy(alpha = 0.3f),
                radius = animatedRadius,
                center = center,
                alpha = config.pointAlpha * bubbleAnimationProgress,
            )
            drawCircle(
                color = bubbleColor,
                radius = (bubbleRadius * 0.85f) * bubbleAnimationProgress,
                center = center,
                alpha = config.pointAlpha * bubbleAnimationProgress,
            )
        }
    }
}
