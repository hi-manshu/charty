package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
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
import com.himanshoe.charty.common.ChartScaffold
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
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
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
 *             BarGroup("Q1", listOf(20f, 30f, 15f)),
 *             BarGroup("Q2", listOf(25f, 35f, 20f)),
 *             BarGroup("Q3", listOf(30f, 25f, 25f))
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
 * @param colors Color configuration - Gradient assigns different color to each stack segment
 * @param stackedConfig Configuration for stacked bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Called when a stacked segment is clicked, providing the [StackedBarSegment].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [StackedBarSegment].
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun StackedBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultGradient,
    stackedConfig: StackedBarChartConfig = StackedBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((StackedBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (StackedBarSegment) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Stacked bar chart data cannot be empty" }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (maxTotal, colorList) = rememberStackedMaxTotal(dataList, colors)
    val animationProgress = rememberChartAnimation(stackedConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, StackedBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateBarGroupChartDescription(it, "stacked bar")
        }

    syncInteractionDataSizes(
        interactionConfig.viewPortState,
        interactionConfig.brushSelectionState,
        fullDataList.size,
        dataList.size,
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
            yAxisConfig =
                AxisConfig(
                    minValue = 0f,
                    maxValue = maxTotal,
                    steps = 6,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

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

            drawStackedReferenceLineIfNeeded(stackedConfig, chartContext, textMeasurer)
            if (tooltipContent == null) {
                drawStackedTooltipIfNeeded(tooltipManager.tooltipState, stackedConfig, textMeasurer, chartContext)
            }

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
        }

        if (tooltipContent != null) {
            ChartTooltipOverlay(
                item = tooltipManager.selectedItem,
                anchor = tooltipManager.tooltipState,
                config = stackedConfig.tooltipConfig,
                modifier = Modifier.matchParentSize(),
                content = tooltipContent,
            )
        }
    }
}
