package com.himanshoe.charty.point

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartOverlays
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBubbleChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedData
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.DEFAULT_VALUE_AXIS_STEPS
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.internal.bubble.BubbleDrawParams
import com.himanshoe.charty.point.internal.bubble.bubbleMarkerPositions
import com.himanshoe.charty.point.internal.bubble.drawBubbleContent
import com.himanshoe.charty.point.internal.rememberThemedPointStyling

private const val MIN_RADIUS_DIVISOR = 2f

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
 * @param minBubbleRadius The radius of the smallest bubble, in pixels. Defaults to half of
 *   [PointChartConfig.pointRadius], which is the radius of the largest bubble, so the default pair
 *   is always valid and rescales with it. Must be positive and smaller than that maximum.
 * @param onBubbleClick A lambda function invoked when a bubble is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it. It
 *   is a drag gesture that leaves taps alone, so tap-to-tooltip and the chart's click callback
 *   keep working alongside it; streaming scrollback ([ChartInteractionConfig.streamingState])
 *   does not, because the crosshair owns the drag.
 * @param tooltip How a tapped bubble is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. Bubbles vary in
 *   radius, so a tap resolves against the drawn circle itself rather than a uniform column — a tap
 *   just outside a small bubble hits nothing and dismisses the tooltip. Its text comes from
 *   [PointChartConfig.tooltipFormatter], which takes a [com.himanshoe.charty.point.data.PointData]:
 *   the bubble is projected onto one with the size it encodes folded into the label, reading
 *   `Product A (size 100): 50` by default. The tooltip is shown alongside [onBubbleClick], never
 *   instead of it, and the same text labels the crosshair.
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
    minBubbleRadius: Float = config.pointRadius / MIN_RADIUS_DIVISOR,
    onBubbleClick: ((BubbleData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<BubbleData>? = null,
    tooltip: ChartTooltip<BubbleData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(config.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }
    val styling = rememberThemedPointStyling(pointConfig = config, crosshair = crosshair)

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

    val tooltipManager = rememberTooltipManager<BubbleBounds, BubbleData>(dataKey = dataList)
    val tapEnabled = onBubbleClick != null || tooltip !is NoneTooltip
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BubbleData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BubbleData>(
            enabled = styling.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
            streamingState = interactionConfig.streamingState,
        )
    val sizeInfo = remember(dataList) { calculateBubbleSizeInfo(dataList) }
    val textMeasurer = rememberTextMeasurer()

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = chartState.animationProgress

    val gestureBase =
        buildBubbleGestureModifier(
            dataList = dataList,
            bubbleBounds = tooltipManager.bounds,
            crosshairBounds = crosshairBounds,
            onBubbleClick = onBubbleClick,
            crosshairManager = crosshairManager,
            dismissOnRelease = styling.crosshairConfig?.dismissOnRelease ?: true,
            config = config,
            onTooltipUpdate = tooltipManager::updateTooltip,
            tapEnabled = tapEnabled,
        )
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
                    steps = DEFAULT_VALUE_AXIS_STEPS,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            populateBubbleCrosshairBounds(
                chartContext = chartContext,
                dataList = dataList,
                enabled = crosshairManager != null,
                crosshairBounds = crosshairBounds,
            )

            drawBubbleContent(
                params =
                    BubbleDrawParams(
                        dataList = displayList,
                        chartContext = chartContext,
                        sizeInfo = sizeInfo,
                        minBubbleRadius = minBubbleRadius,
                        config = config,
                        color = color,
                        animationProgress = animationProgress.value,
                        recordBubbleBounds = tapEnabled,
                        bubbleBounds = tooltipManager.bounds,
                        textMeasurer = textMeasurer,
                    ),
            )

            drawBubbleTooltip(
                tooltipState = tooltipManager.tooltipState,
                tooltipConfig = styling.tooltipConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                drawBubble = tooltip.isCanvas(),
            )

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )

            drawBubbleCrosshair(
                crosshairState = animatedCrosshairState?.resolve(),
                crosshairConfig = styling.crosshairConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                color = color,
            )
        }

        ChartOverlays(
            tooltip = tooltip,
            tooltipItem = tooltipManager.selectedItem,
            tooltipAnchor = tooltipManager.tooltipState,
            crosshair = styling.activeCrosshair,
            crosshairItem = crosshairManager?.selectedItem,
            crosshairState = animatedCrosshairState?.resolve(),
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
): List<BubbleData> =
    rememberAnimatedData(
        dataList = dataList,
        animation = animation,
        enabled = enabled,
        valueOf = { item -> item.yValue },
        withValue = { item, animated -> item.copy(yValue = animated) },
    )

/**
 * Draws the built-in canvas tooltip for the selected bubble. Draws nothing when nothing is selected
 * or when [drawBubble] is `false`, which is the case for a Compose-overlay or disabled tooltip.
 */
private fun DrawScope.drawBubbleTooltip(
    tooltipState: TooltipState?,
    tooltipConfig: TooltipConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    drawBubble: Boolean,
) {
    if (!drawBubble) {
        return
    }
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

private fun DrawScope.drawBubbleCrosshair(
    crosshairState: CrosshairState?,
    crosshairConfig: ChartCrosshairConfig?,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    color: ChartyColor,
) {
    crosshairState?.let { state ->
        crosshairConfig?.let { cfg ->
            drawLineChartCrosshair(state, cfg, chartContext, textMeasurer, color)
        }
    }
}
