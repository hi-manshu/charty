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
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.mosaic.createMosaicAxisConfig
import com.himanshoe.charty.bar.internal.bar.mosaic.createMosaicChartModifier
import com.himanshoe.charty.bar.internal.bar.mosaic.drawMosaicBars
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarGroups
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * Mosaic Bar Chart - 100% stacked bar chart.
 *
 * Each bar represents a category whose segments are normalized to 100% of
 * the bar height, similar to a mosaic / 100% stacked bar chart.
 *
 * Example:
 * ```kotlin
 * MosaicBarChart(
 *     data = {
 *         listOf(
 *             BarGroup(label = "2023", values = listOf(30f, 50f, 20f)),
 *             BarGroup(label = "2024", values = listOf(45f, 35f, 20f)),
 *         )
 *     },
 *     config = MosaicBarChartConfig(),
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups to display
 * @param modifier Modifier for the chart
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param config Configuration for mosaic chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Optional callback when a segment is clicked
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun MosaicBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    config: MosaicBarChartConfig = MosaicBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((MosaicBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<MosaicBarSegment> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = config.animation,
            visibleWindow = config.visibleWindow,
            displayData = {
                rememberAnimatedBarGroups(
                    dataList = it,
                    animation = config.animation,
                    enabled = config.animateValueChanges,
                )
            },
        )
    val dataList = chartState.data
    val displayList = chartState.displayData
    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, MosaicBarSegment>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = config.tooltipConfig.orThemeDefault()

    val clickModifier =
        createMosaicChartModifier(
            modifier = modifier,
            onSegmentClick = onSegmentClick,
            groups = dataList,
            config = config,
            segmentBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = clickModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.streamingPan(streaming = chartState.streaming, orientation = ChartOrientation.VERTICAL)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.values.sum() },
                    fallbackDescription = "Mosaic bar chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createMosaicAxisConfig(),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawMosaicBars(
                groups = displayList,
                chartContext = chartContext,
                config = config,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                onSegmentBoundCalculated = { tooltipManager.bounds.add(it) },
                recordBounds = onSegmentClick != null || interactionConfig.dragTooltipActive,
            )

            if (config.markers.isNotEmpty()) {
                drawPersistentMarkers(
                    chartContext = chartContext,
                    markers = config.markers,
                    pointPositions =
                        verticalBarMarkerPositions(
                            chartContext = chartContext,
                            values = List(displayList.size) { chartContext.maxValue },
                        ),
                    valueLabelFor = { index -> formatMarkerValue(displayList[index].values.sum()) },
                    textMeasurer = textMeasurer,
                )
            }

            if (tooltip.isCanvas()) {
                tooltipManager.tooltipState?.let { state ->
                    drawTooltip(
                        tooltipState = state,
                        config = resolvedTooltipConfig,
                        textMeasurer = textMeasurer,
                        chartWidth = chartContext.right,
                        chartTop = chartContext.top,
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
