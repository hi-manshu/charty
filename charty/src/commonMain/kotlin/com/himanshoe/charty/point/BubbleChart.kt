package com.himanshoe.charty.point

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateBubbleChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberCrosshairManager
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData

/**
 * A composable function that displays a bubble chart.
 *
 * @param data A lambda function that returns a list of [BubbleData] to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bubbles.
 * @param config The configuration for the bubbles' appearance.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param minBubbleRadius The minimum radius for a bubble in pixels.
 * @param onBubbleClick A lambda function invoked when a bubble is clicked.
 * @param crosshairConfig When non-null, enables a draggable crosshair snapping to the nearest bubble.
 *   Enabling this replaces tap-to-click interaction.
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
@OptIn(ExperimentalTextApi::class)
@Composable
fun BubbleChart(
    data: () -> List<BubbleData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    config: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    minBubbleRadius: Float = 10f,
    onBubbleClick: ((BubbleData) -> Unit)? = null,
    crosshairConfig: ChartCrosshairConfig? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Bubble chart data cannot be empty" }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(config.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val bubbleBounds = remember { mutableListOf<BubbleBounds>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BubbleData>>() }
    val crosshairManager: CrosshairManager? = if (crosshairConfig != null) rememberCrosshairManager() else null
    val sizeInfo = remember(dataList) { calculateBubbleSizeInfo(dataList) }
    val textMeasurer = rememberTextMeasurer()

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(config.animation)

    val chartDescription = rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
        generateBubbleChartDescription(it)
    }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val gestureBase = when {
        crosshairManager != null -> Modifier.chartCrosshairHandler(
            dataList = dataList,
            pointBounds = crosshairBounds,
            onCrosshairUpdate = crosshairManager::update,
            labelFormatter = { bubble -> "${bubble.label}: ${bubble.yValue}" },
            dismissOnRelease = crosshairConfig?.dismissOnRelease ?: true,
        )
        else -> createBubbleClickModifier(dataList, bubbleBounds, onBubbleClick)
    }
    val chartModifier = buildInteractionModifier(
        base = modifier.then(gestureBase),
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.fastMap { it.label },
        yAxisConfig = AxisConfig(
            minValue = sizeInfo.minValue,
            maxValue = sizeInfo.maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
        contentDescription = chartDescription,
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

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
            onBubbleClick = if (crosshairManager == null) onBubbleClick else null,
            bubbleBounds = bubbleBounds,
        )

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)

        crosshairManager?.state?.let { state ->
            crosshairConfig?.let { cfg ->
                drawLineChartCrosshair(state, cfg, chartContext, textMeasurer, color)
            }
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

        val bubbleRadius = calculateBubbleRadius(
            bubble.size,
            sizeInfo.minSize,
            sizeInfo.sizeRange,
            minBubbleRadius,
            config.pointRadius,
        )

        val bubbleColor = when (color) {
            is ChartyColor.Solid -> color.color
            is ChartyColor.Gradient -> color.colors[index % color.colors.size]
        }

        if (bubbleAnimationProgress > 0f) {
            val center = Offset(bubbleX, bubbleY)
            val animatedRadius = bubbleRadius * bubbleAnimationProgress

            if (onBubbleClick != null) {
                bubbleBounds.add(BubbleBounds(center, animatedRadius, bubble))
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
