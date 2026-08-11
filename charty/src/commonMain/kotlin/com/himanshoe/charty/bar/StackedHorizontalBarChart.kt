package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.rememberStackedMaxTotal
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.StackedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.createStackedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.stackedhorizontal.drawStackedHorizontalTooltipIfNeeded
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
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
    colors: ChartyColor = ChartyColors.DefaultGradient,
    config: StackedHorizontalBarChartConfig = StackedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((StackedHorizontalBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<StackedHorizontalBarSegment> = ChartTooltip.canvas(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Stacked horizontal bar chart data cannot be empty" }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }
    require(fullDataList.fastAll { group -> group.values.fastAll { it >= 0f } }) {
        "Stacked horizontal bar chart does not support negative values"
    }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (maxTotal, colorList) = rememberStackedMaxTotal(dataList = dataList, colors = colors)
    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, StackedHorizontalBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

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

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createStackedHorizontalAxisConfig(maxTotal),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Stacked horizontal bar chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawStackedHorizontalBars(
                StackedHorizontalBarDrawParams(
                    dataList = dataList,
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
