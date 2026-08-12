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
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.rememberStackedMaxTotal
import com.himanshoe.charty.bar.internal.bar.stacked.StackedBarDrawParams
import com.himanshoe.charty.bar.internal.bar.stacked.createStackedBarChartModifier
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedBars
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.stacked.drawStackedTooltipIfNeeded
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBarGroupChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * Stacked Bar Chart - Display data as stacked vertical bars showing composition
 *
 * A stacked bar chart shows multiple values stacked on top of each other,
 * displaying both individual values and the total. Useful for showing part-to-whole
 * relationships and composition over categories.
 *
 * Usage:
 * ```kotlin
 * StackedBarChart(
 *     data = {
 *         listOf(
 *             BarGroup(label = "Q1", values = listOf(20f, 30f, 15f)),
 *             BarGroup(label = "Q2", values = listOf(25f, 35f, 20f)),
 *             BarGroup(label = "Q3", values = listOf(30f, 25f, 25f))
 *         )
 *     },
 *     colors = ChartyColors.DefaultGradient,
 *     stackedConfig = StackedBarChartConfig(
 *         barWidthFraction = 0.7f,
 *         topCornerRadius = CornerRadius.Medium
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups (each group represents one stacked bar)
 * @param modifier Modifier for the chart
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors Color configuration - Gradient assigns different color to each stack segment
 * @param stackedConfig Configuration for stacked bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Called when a stacked segment is clicked, providing the [StackedBarSegment].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun StackedBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    stackedConfig: StackedBarChartConfig = StackedBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSegmentClick: ((StackedBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<StackedBarSegment> = ChartTooltip.canvas(),
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }
    require(fullDataList.fastAll { group -> group.values.fastAll { it >= 0f } }) {
        "Stacked bar chart does not support negative values"
    }

    val dataList =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = stackedConfig.visibleWindow,
        )

    val (maxTotal, colorList) = rememberStackedMaxTotal(dataList = dataList, colors = colors)
    val animationProgress = rememberChartAnimation(stackedConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, StackedBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateBarGroupChartDescription(data = it, chartTypeName = "stacked bar")
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createStackedBarChartModifier(
            dataList = dataList,
            stackedConfig = stackedConfig,
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
            dataPointDescriptions =
                buildDataPointDescriptions(
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.values.sum() },
                ),
            yAxisConfig =
                AxisConfig(
                    minValue = 0f,
                    maxValue = maxTotal,
                    steps = 6,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawStackedBars(
                StackedBarDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    stackedConfig = stackedConfig,
                    colorList = colorList,
                    animationProgress = animationProgress.value,
                    onSegmentClick = onSegmentClick,
                    segmentBounds = tooltipManager.bounds,
                    textMeasurer = textMeasurer,
                    recordBounds = onSegmentClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawStackedReferenceLineIfNeeded(
                stackedConfig = stackedConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawStackedTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    stackedConfig = stackedConfig,
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
