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
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.horizontal.HorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.horizontal.createHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalBars
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalAnimation
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalValueRange
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.StreamingLayout
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
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
 * A composable function that displays a horizontal bar chart.
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars, where the
 * length of each bar is proportional to the value it represents. This type of chart is particularly
 * useful for comparing categories with long labels or when there are many categories to display.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The color or color scheme for the bars, defined by a [ChartyColor].
 * @param barConfig The configuration for the bars, such as width and corner radius, defined by a
 *   [BarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels,
 *   defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function invoked when a bar is clicked, providing the corresponding
 *   [BarData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val visible =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = barConfig.visibleWindow,
            animation = barConfig.animation,
        )
    val dataList = visible.data

    val (minValue, maxValue) =
        rememberHorizontalDisplayRange(dataList = dataList, barConfig = barConfig, streaming = visible.streaming)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberHorizontalAnimation(barConfig.animation)
    val displayList =
        rememberAnimatedBarValues(
            dataList = dataList,
            animation = barConfig.animation,
            enabled = barConfig.animateValueChanges,
        )
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateBarChartDescription(data = it, minValue = minValue, maxValue = maxValue)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val scrubModifier =
        if (interactionConfig.dragTooltipActive) {
            modifier.rectangularChartScrubHandler(
                dataList = dataList,
                bounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip,
                createTooltipContent = { barData, rect ->
                    createRectangularTooltipState(
                        content = barConfig.tooltipFormatter(barData),
                        rect = rect,
                        position = barConfig.tooltipPosition,
                    )
                },
            )
        } else {
            modifier
        }

    val chartModifier =
        buildInteractionModifier(
            base = scrubModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription = chartDescription,
                    dataPointDescriptions =
                        buildDataPointDescriptions(
                            labels = dataList.fastMap { it.label },
                            values = dataList.fastMap { it.value },
                        ),
                ),
            streamingLayout = visible.streaming,
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig =
                createHorizontalAxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    drawAxisAtZero = drawAxisAtZero,
                ),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val baselineX =
                calculateHorizontalBaselineX(
                    drawAxisAtZero = drawAxisAtZero,
                    minValue = minValue,
                    maxValue = maxValue,
                    chartContext = chartContext,
                )

            drawHorizontalBars(
                HorizontalBarDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    barConfig = barConfig,
                    baselineX = baselineX,
                    animationProgress = animationProgress.value,
                    color = color,
                    isBelowAxisMode = isBelowAxisMode,
                    minValue = minValue,
                    maxValue = maxValue,
                    onBarClick = onBarClick,
                    onBarBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
                    textMeasurer = textMeasurer,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawHorizontalReferenceLineIfNeeded(
                barConfig = barConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawHorizontalTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    barConfig = barConfig,
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

/**
 * The min/max the chart lays out with: the raw range of [dataList], eased through
 * [rememberAnimatedRange] while a rolling window is sliding so rescales glide instead of jumping.
 */
@Composable
private fun rememberHorizontalDisplayRange(
    dataList: List<BarData>,
    barConfig: BarChartConfig,
    streaming: StreamingLayout?,
): Pair<Float, Float> {
    val (rawMinValue, rawMaxValue) =
        rememberHorizontalValueRange(
            dataList = dataList,
            negativeValuesDrawMode = barConfig.negativeValuesDrawMode,
        )
    return rememberAnimatedRange(
        minValue = rawMinValue,
        maxValue = rawMaxValue,
        animation = barConfig.animation,
        active = streaming != null,
    )
}
