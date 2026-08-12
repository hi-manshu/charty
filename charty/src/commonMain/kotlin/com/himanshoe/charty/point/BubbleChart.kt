package com.himanshoe.charty.point

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBubbleChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
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
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(config.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }
    val crosshairConfig = crosshair?.config ?: config.crosshairConfig
    val activeCrosshair = crosshair ?: config.crosshairConfig?.let { ChartCrosshair<BubbleData>(config = it) }

    val dataList =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = config.visibleWindow,
        )

    val bubbleBounds = remember { mutableListOf<BubbleBounds>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BubbleData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BubbleData>(crosshairConfig != null)
    val sizeInfo = remember(dataList) { calculateBubbleSizeInfo(dataList) }
    val textMeasurer = rememberTextMeasurer()

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(config.animation)

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateBubbleChartDescription(it)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

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

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            dataPointDescriptions =
                buildDataPointDescriptions(
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.yValue },
                ),
            yAxisConfig =
                AxisConfig(
                    minValue = sizeInfo.minValue,
                    maxValue = sizeInfo.maxValue,
                    steps = 6,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            bubbleBounds.clear()
            crosshairBounds.clear()
            if (crosshairManager != null) {
                dataList.fastForEachIndexed { index, bubble ->
                    val x = chartContext.calculateCenteredXPosition(index, dataList.size)
                    crosshairBounds.add(Offset(x, chartContext.convertValueToYPosition(bubble.yValue)) to bubble)
                }
            }

            drawAllBubbles(
                dataList = dataList,
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
