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
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairHost
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.drawVerticalBarMarkers
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.seriesCrosshairHandler
import com.himanshoe.charty.bar.internal.bar.seriesStreamingPan
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateCumulativeValues
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallBarParams
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallBarTopValues
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallRange
import com.himanshoe.charty.bar.internal.bar.waterfall.createWaterfallClickModifier
import com.himanshoe.charty.bar.internal.bar.waterfall.drawWaterfallBar
import com.himanshoe.charty.bar.internal.bar.waterfall.drawWaterfallCrosshair
import com.himanshoe.charty.bar.internal.bar.waterfall.rememberCumulativeValues
import com.himanshoe.charty.bar.internal.bar.waterfall.rememberWaterfallCrosshair
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * Waterfall Chart - visualizes cumulative effect of sequential gains/losses.
 *
 * Example:
 * ```kotlin
 * WaterfallChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Start", value = 100f),
 *             BarData(label = "Sales", value = 40f),
 *             BarData(label = "Costs", value = -30f),
 *             BarData(label = "End", value = 110f),
 *         )
 *     },
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param config Configuration for waterfall chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar is clicked
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   vertical guide line that snaps by x to the nearest step and rests on the top edge of its floating
 *   bar — the same anchor the chart's persistent markers use, which is the higher of the running
 *   totals either side of the step. Its label reads that step's own delta through
 *   [WaterfallChartConfig.tooltipFormatter], matching both the tap tooltip and the [BarData] handed to
 *   [onBarClick]; use a marker to label a running total instead. It is a drag gesture that leaves taps
 *   alone, so tapping a bar still raises its tooltip; streaming scrollback
 *   ([ChartInteractionConfig.streamingState]) does not survive it, because the crosshair owns the drag.
 */
@Composable
fun WaterfallChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    config: WaterfallChartConfig = WaterfallChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
    crosshair: ChartCrosshair<BarData>? = null,
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
            animation = config.animation,
            visibleWindow = config.visibleWindow,
            displayData = {
                rememberAnimatedBarValues(
                    dataList = it,
                    animation = config.animation,
                    enabled = config.animateValueChanges,
                )
            },
        ) { _, display ->
            remember(display) { calculateWaterfallRange(calculateCumulativeValues(display)) }
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val cumulativeValues = rememberCumulativeValues(displayList)
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue

    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, BarData>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()
    val crosshairScope =
        rememberWaterfallCrosshair(config = config, crosshair = crosshair, interactionConfig = interactionConfig)

    val clickModifier =
        createWaterfallClickModifier(
            items = dataList,
            config = config,
            barBounds = tooltipManager.bounds,
            onBarClick = onBarClick,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        ).seriesCrosshairHandler(crosshair = crosshairScope, dataList = dataList)

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.seriesStreamingPan(streaming = chartState.streaming, crosshair = crosshairScope)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.value },
                    fallbackDescription = "Waterfall chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 6,
                    drawAxisAtZero = true,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            displayList.fastForEachIndexed { index, bar ->
                val barParams =
                    calculateWaterfallBarParams(
                        index = index,
                        bar = bar,
                        items = displayList,
                        cumulativeValues = cumulativeValues,
                        config = config,
                        chartContext = chartContext,
                        animationProgress = animationProgress.value,
                    )

                if (onBarClick != null || interactionConfig.dragTooltipActive) {
                    tooltipManager.bounds.add(barParams.bounds to dataList[index])
                }

                drawWaterfallBar(
                    brush = barParams.brush,
                    x = barParams.x,
                    y = barParams.y,
                    width = barParams.width,
                    height = barParams.height,
                    cornerRadius = config.cornerRadius.value,
                )
            }

            if (config.markers.isNotEmpty()) {
                drawVerticalBarMarkers(
                    chartContext = chartContext,
                    markers = config.markers,
                    values = calculateWaterfallBarTopValues(cumulativeValues),
                    textMeasurer = textMeasurer,
                )
            }

            if (tooltip.isCanvas()) {
                tooltipManager.tooltipState?.let { state ->
                    drawTooltip(
                        tooltipState = state,
                        config = config.tooltipConfig,
                        chartWidth = chartContext.right,
                        chartTop = chartContext.top,
                        textMeasurer = textMeasurer,
                        chartBottom = chartContext.bottom,
                    )
                }
            }

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )

            drawWaterfallCrosshair(
                crosshair = crosshairScope,
                dataList = dataList,
                cumulativeValues = cumulativeValues,
                chartContext = chartContext,
                config = config,
                textMeasurer = textMeasurer,
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
        SeriesCrosshairHost(crosshair = crosshairScope)
    }
}
