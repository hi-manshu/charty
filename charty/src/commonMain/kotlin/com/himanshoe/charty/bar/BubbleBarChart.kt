package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.bubblebar.BubbleBarDrawParams
import com.himanshoe.charty.bar.internal.bar.bubblebar.calculateBaselineY
import com.himanshoe.charty.bar.internal.bar.bubblebar.createAxisConfig
import com.himanshoe.charty.bar.internal.bar.bubblebar.createBubbleChartModifier
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawBubbleBars
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.bubblebar.rememberValueRange
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a bubble bar chart.
 *
 * Example:
 * ```kotlin
 * BubbleBarChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Mon", value = 12f),
 *             BarData(label = "Tue", value = 18f),
 *             BarData(label = "Wed", value = 9f),
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The color or color scheme for the bubbles.
 * @param bubbleConfig The configuration for the bubbles.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a bar is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 */
@Composable
fun BubbleBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    bubbleConfig: BubbleBarChartConfig = BubbleBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val visible =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = bubbleConfig.visibleWindow,
            animation = bubbleConfig.animation,
            streamingState = interactionConfig.streamingState,
        )
    val dataList = visible.data

    val (rawMinValue, rawMaxValue) =
        rememberValueRange(
            dataList = dataList,
            negativeValuesDrawMode = bubbleConfig.negativeValuesDrawMode,
        )
    val (minValue, maxValue) =
        rememberAnimatedRange(
            minValue = rawMinValue,
            maxValue = rawMaxValue,
            animation = bubbleConfig.animation,
            active = visible.streaming != null,
        )
    val isBelowAxisMode = bubbleConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(bubbleConfig.animation)
    val displayList =
        rememberAnimatedBarValues(
            dataList = dataList,
            animation = bubbleConfig.animation,
            enabled = bubbleConfig.animateValueChanges,
        )
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createBubbleChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            bubbleConfig = bubbleConfig,
            barBounds = barBounds,
            onTooltipUpdate = { state, _ -> tooltipState = state },
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = clickModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    ChartScaffold(
        accessibility =
            ChartAccessibility(
                contentDescription =
                    interactionConfig.accessibilityDescription
                        ?: "Bubble bar chart, ${fullDataList.size} data points.",
                dataPointDescriptions =
                    buildDataPointDescriptions(
                        labels =
                            dataList.fastMap {
                                it.label
                            },
                        values = dataList.fastMap { it.value },
                    ),
            ),
        streaming = interactionConfig.streamingRender(visible.streaming),
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
    ) { chartContext ->
        updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

        barBounds.clear()
        val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

        val drawParams =
            BubbleBarDrawParams(
                dataList = displayList,
                chartContext = chartContext,
                bubbleConfig = bubbleConfig,
                baselineY = baselineY,
                animationProgress = animationProgress.value,
                color = color,
                onBarClick = onBarClick,
                barBounds = barBounds,
                textMeasurer = textMeasurer,
                recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
            )

        drawBubbleBars(drawParams)
        drawPersistentMarkers(
            chartContext = chartContext,
            markers = bubbleConfig.markers,
            pointPositions =
                verticalBarMarkerPositions(
                    chartContext = chartContext,
                    values = displayList.fastMap { it.value },
                ),
            valueLabelFor = { index -> formatMarkerValue(displayList[index].value) },
            textMeasurer = textMeasurer,
        )
        drawReferenceLineIfNeeded(drawParams)
        drawTooltipIfNeeded(drawParams, tooltipState)

        drawInteractionOverlays(
            interactionConfig = interactionConfig,
            chartContext = chartContext,
            totalItems = dataList.size,
            textMeasurer = textMeasurer,
        )
    }
}
