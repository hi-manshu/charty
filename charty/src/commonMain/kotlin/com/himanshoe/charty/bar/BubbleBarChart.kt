package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barAccessibility
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
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
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
 * @param tooltip How a tapped column is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. A tap resolves
 *   to the whole column — the bubbles in a column all encode one [BarData] — and its text comes from
 *   [BubbleBarChartConfig.tooltipFormatter], reading `label: value` by default. The tooltip is shown
 *   alongside [onBarClick], never instead of it.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
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
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = bubbleConfig.animation,
            visibleWindow = bubbleConfig.visibleWindow,
            displayData = {
                rememberAnimatedBarValues(
                    dataList = it,
                    animation = bubbleConfig.animation,
                    enabled = bubbleConfig.animateValueChanges,
                )
            },
        ) { windowed, _ ->
            rememberValueRange(
                dataList = windowed,
                negativeValuesDrawMode = bubbleConfig.negativeValuesDrawMode,
            )
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val isBelowAxisMode = bubbleConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, BarData>(dataKey = dataList)
    val tapEnabled = onBarClick != null || tooltip !is NoneTooltip
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = bubbleConfig.tooltipConfig.orThemeDefault()

    val clickModifier =
        createBubbleChartModifier(
            modifier = Modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            bubbleConfig = bubbleConfig,
            barBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
            tapEnabled = tapEnabled,
        )

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.value },
                    fallbackDescription = "Bubble bar chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

            val drawParams =
                BubbleBarDrawParams(
                    tooltipConfig = resolvedTooltipConfig,
                    dataList = displayList,
                    chartContext = chartContext,
                    bubbleConfig = bubbleConfig,
                    baselineY = baselineY,
                    animationProgress = animationProgress.value,
                    color = color,
                    barBounds = tooltipManager.bounds,
                    textMeasurer = textMeasurer,
                    recordBounds = tapEnabled || interactionConfig.dragTooltipActive,
                )

            drawBubbleBars(drawParams)
            if (bubbleConfig.markers.isNotEmpty()) {
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
            }
            drawReferenceLineIfNeeded(drawParams)
            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(drawParams, tooltipManager.tooltipState)
            }

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
