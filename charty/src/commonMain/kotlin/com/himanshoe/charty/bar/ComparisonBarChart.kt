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
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairHost
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.comparison.ComparisonBarDrawParams
import com.himanshoe.charty.bar.internal.bar.comparison.calculateComparisonBaselineY
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonAxisConfig
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonChartModifier
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonBars
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonCrosshair
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.rememberComparisonChartValues
import com.himanshoe.charty.bar.internal.bar.comparison.rememberComparisonCrosshair
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarGroups
import com.himanshoe.charty.bar.internal.bar.seriesCrosshairHandler
import com.himanshoe.charty.bar.internal.bar.seriesStreamingPan
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a comparison bar chart.
 *
 * Example:
 * ```kotlin
 * ComparisonBarChart(
 *     data = {
 *         listOf(
 *             BarGroup(label = "Q1", values = listOf(120f, 90f)),
 *             BarGroup(label = "Q2", values = listOf(150f, 110f)),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [BarGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param comparisonConfig The configuration for the comparison bar chart.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a bar segment is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   vertical guide line that snaps by x per **group** rather than per bar, because one guide line
 *   cannot sit on several bars at once and the x axis is labelled by group. It rests on the top of the
 *   group's tallest bar — the same anchor the chart's persistent markers use — and reports that bar as
 *   a [ComparisonBarSegment], so its label reads the tallest bar's value through
 *   [ComparisonBarChartConfig.tooltipFormatter]. It is a drag gesture that leaves taps alone, so
 *   tapping a bar still raises its own tooltip; streaming scrollback
 *   ([ChartInteractionConfig.streamingState]) does not survive it, because the crosshair owns the drag.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun ComparisonBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    comparisonConfig: ComparisonBarChartConfig = ComparisonBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((ComparisonBarSegment) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<ComparisonBarSegment> = ChartTooltip.canvas(),
    crosshair: ChartCrosshair<ComparisonBarSegment>? = null,
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    require(fullDataList.fastAll { it.values.isNotEmpty() }) { "Each comparison group must have at least one value" }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = comparisonConfig.animation,
            visibleWindow = comparisonConfig.visibleWindow,
            displayData = {
                rememberAnimatedBarGroups(
                    dataList = it,
                    animation = comparisonConfig.animation,
                    enabled = comparisonConfig.animateValueChanges,
                )
            },
        ) { windowed, _ -> rememberComparisonChartValues(windowed) }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, ComparisonBarSegment>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()
    val crosshairScope =
        rememberComparisonCrosshair(
            config = comparisonConfig,
            crosshair = crosshair,
            interactionConfig = interactionConfig,
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
        ).seriesCrosshairHandler(crosshair = crosshairScope, dataList = dataList)

    val chartModifier =
        buildInteractionModifier(
            base = clickModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.seriesStreamingPan(streaming = chartState.streaming, crosshair = crosshairScope)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.values.sum() },
                    fallbackDescription = "Comparison bar chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                createComparisonAxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    isBelowAxisMode = isBelowAxisMode,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val baselineY =
                calculateComparisonBaselineY(
                    minValue = minValue,
                    isBelowAxisMode = isBelowAxisMode,
                    chartContext = chartContext,
                )

            drawComparisonBars(
                ComparisonBarDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    comparisonConfig = comparisonConfig,
                    baselineY = baselineY,
                    animationProgress = animationProgress.value,
                    barBounds = tooltipManager.bounds,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                    textMeasurer = textMeasurer,
                ),
            )

            drawComparisonReferenceLineIfNeeded(
                comparisonConfig = comparisonConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawComparisonTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    comparisonConfig = comparisonConfig,
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

            drawComparisonCrosshair(
                crosshair = crosshairScope,
                dataList = dataList,
                displayList = displayList,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
        SeriesCrosshairHost(crosshair = crosshairScope)
    }
}
