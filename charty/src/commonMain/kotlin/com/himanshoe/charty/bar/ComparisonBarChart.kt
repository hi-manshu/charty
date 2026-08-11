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
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.internal.bar.comparison.ComparisonBarDrawParams
import com.himanshoe.charty.bar.internal.bar.comparison.calculateComparisonBaselineY
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonAxisConfig
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonChartModifier
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonBars
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.rememberComparisonChartValues
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a comparison bar chart.
 *
 * @param data A lambda function that returns a list of [BarGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param comparisonConfig The configuration for the comparison bar chart.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a bar segment is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped
 *   [ComparisonBarSegment]. When `null`, the built-in tooltip configured via
 *   [comparisonConfig] is drawn instead.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ComparisonBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    comparisonConfig: ComparisonBarChartConfig = ComparisonBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((ComparisonBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (ComparisonBarSegment) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Comparison bar chart data cannot be empty" }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each comparison group must have at least one value" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberComparisonChartValues(dataList)
    val isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(comparisonConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, ComparisonBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createComparisonChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            comparisonConfig = comparisonConfig,
            barBounds = tooltipManager.bounds,
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
            xLabels = dataList.getLabels(),
            yAxisConfig = createComparisonAxisConfig(minValue, maxValue, isBelowAxisMode),
            config = scaffoldConfig,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Comparison bar chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

            tooltipManager.clearBounds()
            val baselineY = calculateComparisonBaselineY(minValue, isBelowAxisMode, chartContext)

            drawComparisonBars(
                ComparisonBarDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    comparisonConfig = comparisonConfig,
                    baselineY = baselineY,
                    animationProgress = animationProgress.value,
                    onBarClick = onBarClick,
                    barBounds = tooltipManager.bounds,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawComparisonReferenceLineIfNeeded(comparisonConfig, chartContext, textMeasurer)
            if (tooltipContent == null) {
                drawComparisonTooltipIfNeeded(tooltipManager.tooltipState, comparisonConfig, textMeasurer, chartContext)
            }

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
        }

        if (tooltipContent != null) {
            ChartTooltipOverlay(
                item = tooltipManager.selectedItem,
                anchor = tooltipManager.tooltipState,
                config = comparisonConfig.tooltipConfig,
                modifier = Modifier.matchParentSize(),
                content = tooltipContent,
            )
        }
    }
}
