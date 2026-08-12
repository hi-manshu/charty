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
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.horizontalBarMarkerPositions
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarGroups
import com.himanshoe.charty.bar.internal.bar.rememberStackedMaxTotal
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.StackedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalTooltipIfNeeded
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
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
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable that displays a **stacked horizontal bar chart**.
 *
 * Each [BarGroup] is rendered as a single horizontal bar split into colour-coded
 * segments — one segment per value in the group. Bars grow left-to-right with a
 * smooth entrance animation; all segments scale uniformly so the bar reads as a
 * cohesive unit rather than independent pieces.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group becomes one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors Colour palette used to differentiate segments.
 * @param config Appearance and behaviour configuration — bar thickness, corner radius,
 *   animation, reference line, and tooltip settings.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onSegmentClick Optional callback invoked when a segment is tapped.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun StackedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    config: StackedHorizontalBarChartConfig = StackedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((StackedHorizontalBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<StackedHorizontalBarSegment> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }
    require(fullDataList.fastAll { group -> group.values.fastAll { it >= 0f } }) {
        "Stacked horizontal bar chart does not support negative values"
    }

    val colorList = remember(colors) { colors.value }
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
        ) { _, display -> 0f to rememberStackedMaxTotal(dataList = display, colors = colors).first }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val maxTotal = chartState.maxValue
    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, StackedHorizontalBarSegment>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()

    val clickModifier =
        createStackedHorizontalBarChartModifier(
            dataList = dataList,
            config = config,
            onSegmentClick = onSegmentClick,
            segmentBounds = tooltipManager.bounds,
            onTooltipStateChange = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan =
        interactionConfig.streamingPan(
            streaming = chartState.streaming,
            orientation = ChartOrientation.HORIZONTAL,
        )

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription =
                        interactionConfig.accessibilityDescription
                            ?: "Stacked horizontal bar chart, ${fullDataList.size} data points.",
                    dataPointDescriptions =
                        buildDataPointDescriptions(
                            labels =
                                dataList.fastMap {
                                    it.label
                                },
                            values = dataList.fastMap { it.values.sum() },
                        ),
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createStackedHorizontalAxisConfig(maxTotal),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawStackedHorizontalBars(
                StackedHorizontalBarDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    config = config,
                    colorList = colorList,
                    maxTotal = maxTotal,
                    animationProgress = animationProgress.value,
                    onSegmentClick = onSegmentClick,
                    segmentBounds = tooltipManager.bounds,
                    recordBounds = onSegmentClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawPersistentMarkers(
                chartContext = chartContext,
                markers = config.markers,
                pointPositions =
                    horizontalBarMarkerPositions(
                        chartContext = chartContext,
                        values = displayList.fastMap { group -> group.values.sum() },
                        minValue = 0f,
                        maxValue = maxTotal,
                    ),
                valueLabelFor = { index -> formatMarkerValue(displayList[index].values.sum()) },
                textMeasurer = textMeasurer,
            )

            drawStackedHorizontalReferenceLineIfNeeded(
                config = config,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawStackedHorizontalTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    config = config,
                    textMeasurer = textMeasurer,
                    chartContext = chartContext,
                )
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
