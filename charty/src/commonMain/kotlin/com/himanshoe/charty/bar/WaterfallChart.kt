package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallBarParams
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallRange
import com.himanshoe.charty.bar.internal.bar.waterfall.createWaterfallClickModifier
import com.himanshoe.charty.bar.internal.bar.waterfall.drawWaterfallBar
import com.himanshoe.charty.bar.internal.bar.waterfall.rememberCumulativeValues
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
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
 * @param config Configuration for waterfall chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar is clicked
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun WaterfallChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    config: WaterfallChartConfig = WaterfallChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Waterfall chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val cumulativeValues = rememberCumulativeValues(dataList)
    val (minValue, maxValue) =
        remember(cumulativeValues) {
            calculateWaterfallRange(cumulativeValues)
        }

    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createWaterfallClickModifier(
            items = dataList,
            config = config,
            barBounds = tooltipManager.bounds,
            onBarClick = onBarClick,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
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
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Waterfall chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            dataList.fastForEachIndexed { index, bar ->
                val barParams =
                    calculateWaterfallBarParams(
                        index = index,
                        bar = bar,
                        items = dataList,
                        cumulativeValues = cumulativeValues,
                        config = config,
                        chartContext = chartContext,
                        animationProgress = animationProgress.value,
                    )

                if (onBarClick != null || interactionConfig.dragTooltipActive) {
                    tooltipManager.bounds.add(barParams.bounds to bar)
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
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
