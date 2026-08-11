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
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.mosaic.createMosaicAxisConfig
import com.himanshoe.charty.bar.internal.bar.mosaic.createMosaicChartModifier
import com.himanshoe.charty.bar.internal.bar.mosaic.drawMosaicBars
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
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.drawTooltip
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
 *             BarGroup("2023", listOf(30f, 50f, 20f)),
 *             BarGroup("2024", listOf(45f, 35f, 20f)),
 *         )
 *     },
 *     config = MosaicBarChartConfig(),
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups to display
 * @param modifier Modifier for the chart
 * @param config Configuration for mosaic chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Optional callback when a segment is clicked
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [MosaicBarSegment].
 */
@Composable
fun MosaicBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    config: MosaicBarChartConfig = MosaicBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((MosaicBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (MosaicBarSegment) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Mosaic bar chart data cannot be empty" }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, MosaicBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

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

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createMosaicAxisConfig(),
            config = scaffoldConfig,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Mosaic bar chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

            tooltipManager.clearBounds()

            drawMosaicBars(
                groups = dataList,
                chartContext = chartContext,
                config = config,
                animationProgress = animationProgress.value,
                onSegmentClick = onSegmentClick,
                onSegmentBoundCalculated = { tooltipManager.bounds.add(it) },
                recordBounds = onSegmentClick != null || interactionConfig.dragTooltipActive,
            )

            if (tooltipContent == null) {
                tooltipManager.tooltipState?.let { state ->
                    drawTooltip(
                        tooltipState = state,
                        config = config.tooltipConfig,
                        textMeasurer = textMeasurer,
                        chartWidth = chartContext.right,
                        chartTop = chartContext.top,
                        chartBottom = chartContext.bottom,
                    )
                }
            }

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
        }

        if (tooltipContent != null) {
            ChartTooltipOverlay(
                item = tooltipManager.selectedItem,
                anchor = tooltipManager.tooltipState,
                config = config.tooltipConfig,
                modifier = Modifier.matchParentSize(),
                content = tooltipContent,
            )
        }
    }
}
