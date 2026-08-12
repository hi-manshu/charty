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
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.horizontalBarMarkerPositions
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.NormalizedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.createNormalizedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.createNormalizedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.drawNormalizedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.drawNormalizedHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.normalizedhorizontal.rememberNormalizedHorizontalColors
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarGroups
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable that displays a **normalized (100%) horizontal bar chart**.
 *
 * Each [BarGroup] is rendered as a single horizontal bar that always extends to 100% of the
 * available width. Segments are sized proportionally to each value's share of the group total.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group becomes one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors Colour palette for segments.
 * @param config Appearance and behaviour settings — bar thickness, corner radius, animation,
 *   and tooltip configuration.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onSegmentClick Optional callback invoked when a segment is tapped.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun NormalizedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    config: NormalizedHorizontalBarChartConfig = NormalizedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((NormalizedHorizontalBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<NormalizedHorizontalBarSegment> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val visible =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = config.visibleWindow,
            animation = config.animation,
        )
    val dataList = visible.data

    val displayList =
        rememberAnimatedBarGroups(
            dataList = dataList,
            animation = config.animation,
            enabled = config.animateValueChanges,
        )
    val colorList = rememberNormalizedHorizontalColors(dataList = displayList, colors = colors)
    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, NormalizedHorizontalBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createNormalizedHorizontalBarChartModifier(
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

    Box(modifier = chartModifier) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription =
                        interactionConfig.accessibilityDescription
                            ?: "Normalized horizontal bar chart, ${fullDataList.size} data points.",
                    dataPointDescriptions =
                        buildDataPointDescriptions(
                            labels =
                                dataList.fastMap {
                                    it.label
                                },
                            values = dataList.fastMap { it.values.sum() },
                        ),
                ),
            streamingLayout = visible.streaming,
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createNormalizedHorizontalAxisConfig(),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawNormalizedHorizontalBars(
                NormalizedHorizontalBarDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    config = config,
                    colorList = colorList,
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
                        values = List(displayList.size) { 1f },
                        minValue = 0f,
                        maxValue = 1f,
                    ),
                valueLabelFor = { index -> formatMarkerValue(displayList[index].values.sum()) },
                textMeasurer = textMeasurer,
            )

            if (tooltip.isCanvas()) {
                drawNormalizedHorizontalTooltipIfNeeded(
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
