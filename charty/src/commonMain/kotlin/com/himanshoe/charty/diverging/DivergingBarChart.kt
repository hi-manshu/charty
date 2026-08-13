package com.himanshoe.charty.diverging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
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
import com.himanshoe.charty.diverging.config.DivergingBarChartConfig
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSelection
import com.himanshoe.charty.diverging.internal.DivergingDrawParams
import com.himanshoe.charty.diverging.internal.DivergingScales
import com.himanshoe.charty.diverging.internal.createDivergingChartModifier
import com.himanshoe.charty.diverging.internal.divergingAccessibility
import com.himanshoe.charty.diverging.internal.divergingAxisConfig
import com.himanshoe.charty.diverging.internal.divergingScales
import com.himanshoe.charty.diverging.internal.divergingValueRange
import com.himanshoe.charty.diverging.internal.drawDivergingBars
import com.himanshoe.charty.diverging.internal.generateDivergingChartDescription
import com.himanshoe.charty.diverging.internal.rememberDivergingValueLabels

/**
 * A diverging bar chart: two opposing series drawn back to back from a shared centre axis, one
 * growing leftwards and one rightwards, with one category per row.
 *
 * It is the shape used for population pyramids, Likert and sentiment breakdowns, and any
 * A-versus-B comparison where the reader's question is "which way does this category lean, and by
 * how much?".
 *
 * **Both values are magnitudes and must be non-negative** — the side a bar is drawn on carries its
 * direction, not the sign of the number (see [DivergingData]). That is the difference from
 * [com.himanshoe.charty.bar.BarChart]'s `negativeValuesDrawMode`, which splits a *single* series
 * across a zero line by the sign of each value.
 *
 * **Layout.** The centre axis sits on the plot's horizontal midpoint, so both halves always get the
 * same room. By default both are scaled against the largest magnitude across *both* series, which
 * keeps the halves comparable — scaling each side to its own maximum would make a small series look
 * as large as a big one. Opt out with
 * [com.himanshoe.charty.diverging.config.DivergingSideScaling.INDEPENDENT], which documents what it
 * costs. Category labels are drawn in the left gutter, alongside their row, rather than in the centre
 * gutter of the classic pyramid: it is where every other horizontal chart in the library puts them,
 * it leaves the centre free for the axis, and it does not shrink as the centre gap does.
 *
 * Example:
 * ```kotlin
 * DivergingBarChart(
 *     data = {
 *         listOf(
 *             DivergingData(label = "0-9", leftValue = 4.1f, rightValue = 3.9f),
 *             DivergingData(label = "10-19", leftValue = 3.6f, rightValue = 3.5f),
 *         )
 *     },
 *     leftSeriesName = "Male",
 *     rightSeriesName = "Female",
 * )
 * ```
 *
 * @param data Lambda returning the category rows to display.
 * @param modifier The modifier applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when `null`
 *   (default) a built-in "No data" state is used.
 * @param leftColor The colour of the left half, unless a row overrides it.
 * @param rightColor The colour of the right half, unless a row overrides it.
 * @param leftSeriesName The name of the left-hand series, used in the screen-reader description.
 * @param rightSeriesName The name of the right-hand series, used in the screen-reader description.
 * @param divergingConfig Appearance and behaviour: bar thickness, corner radius, side scaling, the
 *   centre gap, animation, and value labels.
 * @param scaffoldConfig Styling for the axis, grid, and labels.
 * @param onBarClick Called when a half-bar is tapped, with the row and the side that was tapped.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Suppress("LongParameterList")
@Composable
fun DivergingBarChart(
    data: () -> List<DivergingData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    leftColor: ChartyColor = ChartyThemeDefaults.seriesColor(index = 0),
    rightColor: ChartyColor = ChartyThemeDefaults.seriesColor(index = 1),
    leftSeriesName: String = "left",
    rightSeriesName: String = "right",
    divergingConfig: DivergingBarChartConfig = DivergingBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((DivergingSelection) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<DivergingSelection> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = divergingConfig.animation,
            visibleWindow = divergingConfig.visibleWindow,
            describe = { series, _, _ ->
                generateDivergingChartDescription(
                    dataList = series,
                    leftSeriesName = leftSeriesName,
                    rightSeriesName = rightSeriesName,
                    valueFormatter = divergingConfig.valueFormatter,
                )
            },
        ) { windowed, _ ->
            divergingValueRange(scales = rememberScales(dataList = windowed, config = divergingConfig))
        }
    val dataList = chartState.data
    val scales = rememberScales(dataList = dataList, config = divergingConfig)
    val textMeasurer = rememberTextMeasurer()
    val labels =
        rememberDivergingValueLabels(dataList = dataList, config = divergingConfig, textMeasurer = textMeasurer)
    val tooltipManager = rememberTooltipManager<Rect, DivergingSelection>(dataKey = dataList)

    val chartModifier =
        buildInteractionModifier(
            base =
                createDivergingChartModifier(
                    modifier = modifier,
                    onBarClick = onBarClick,
                    dataList = dataList,
                    config = divergingConfig,
                    barBounds = tooltipManager.bounds,
                    onTooltipUpdate = tooltipManager::updateTooltip,
                    enableScrub = interactionConfig.dragTooltipActive,
                ),
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
                divergingAccessibility(
                    description = chartState.description,
                    dataList = dataList,
                    leftSeriesName = leftSeriesName,
                    rightSeriesName = rightSeriesName,
                    valueFormatter = divergingConfig.valueFormatter,
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { row -> row.label },
            yAxisConfig =
                divergingAxisConfig(
                    scales = scales,
                    sideScaling = divergingConfig.sideScaling,
                    valueFormatter = divergingConfig.valueFormatter,
                ),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            tooltipManager.clearBounds()

            drawDivergingBars(
                DivergingDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    config = divergingConfig,
                    scales = scales,
                    leftColor = leftColor,
                    rightColor = rightColor,
                    animationProgress = chartState.animationProgress.value,
                    labels = labels,
                    onBarBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    tooltipConfig = divergingConfig.tooltipConfig,
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

@Composable
private fun rememberScales(
    dataList: List<DivergingData>,
    config: DivergingBarChartConfig,
): DivergingScales =
    remember(dataList, config.sideScaling) {
        divergingScales(dataList = dataList, sideScaling = config.sideScaling)
    }
