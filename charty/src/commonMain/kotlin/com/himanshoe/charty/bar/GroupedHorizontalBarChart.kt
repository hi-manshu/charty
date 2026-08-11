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
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarEntry
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.GroupedHorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.calculateGroupedHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.createGroupedHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.createGroupedHorizontalBarChartModifier
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalBars
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.drawGroupedHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.groupedhorizontal.rememberGroupedHorizontalState
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
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
 * A composable that displays a **grouped horizontal bar chart**.
 *
 * Each [BarGroup] becomes one row of horizontally-oriented bars laid out side-by-side
 * vertically within the row.
 *
 * @param data Lambda returning the list of [BarGroup] to display. Each group forms one bar row.
 * @param modifier Modifier applied to the chart container.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors Colour palette for the bars.
 * @param config Appearance and behaviour settings.
 * @param scaffoldConfig Chart scaffold configuration controlling axes, grid lines, and labels.
 * @param onBarClick Optional callback invoked when a bar is tapped.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun GroupedHorizontalBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColors.ModernPalette,
    config: GroupedHorizontalBarChartConfig = GroupedHorizontalBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((GroupedHorizontalBarEntry) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<GroupedHorizontalBarEntry> = ChartTooltip.canvas(),
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val state =
        rememberGroupedHorizontalState(
            dataList = dataList,
            negativeValuesDrawMode = config.negativeValuesDrawMode,
            colors = colors,
        )

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = state.minValue < 0f && state.maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberChartAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Rect, GroupedHorizontalBarEntry>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createGroupedHorizontalBarChartModifier(
            dataList = dataList,
            config = config,
            onBarClick = onBarClick,
            barBounds = tooltipManager.bounds,
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
                createGroupedHorizontalAxisConfig(
                    minValue = state.minValue,
                    maxValue = state.maxValue,
                    steps = state.axisSteps,
                    drawAxisAtZero = drawAxisAtZero,
                ),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Grouped horizontal bar chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val baselineX =
                calculateGroupedHorizontalBaselineX(
                    drawAxisAtZero = drawAxisAtZero,
                    minValue = state.minValue,
                    maxValue = state.maxValue,
                    chartContext = chartContext,
                )

            drawGroupedHorizontalBars(
                GroupedHorizontalBarDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    config = config,
                    colorList = state.colorList,
                    baselineX = baselineX,
                    minValue = state.minValue,
                    maxValue = state.maxValue,
                    animationProgress = animationProgress.value,
                    onBarClick = onBarClick,
                    barBounds = tooltipManager.bounds,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawGroupedHorizontalReferenceLineIfNeeded(
                config = config,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawGroupedHorizontalTooltipIfNeeded(
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
