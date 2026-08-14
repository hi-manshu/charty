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
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.BarChartOverlays
import com.himanshoe.charty.bar.internal.bar.barCrosshairHandler
import com.himanshoe.charty.bar.internal.bar.barStreamingPan
import com.himanshoe.charty.bar.internal.bar.drawBarCrosshair
import com.himanshoe.charty.bar.internal.bar.horizontal.HorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.horizontal.createHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalBars
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceBandIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.horizontalBarAccessibility
import com.himanshoe.charty.bar.internal.bar.horizontal.horizontalBarScrubModifier
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalValueRange
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.rememberBarCrosshair
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
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
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   horizontal guide line that snaps to the nearest bar's row centre, with a built-in or custom
 *   label drawn over it. It is a drag gesture that leaves taps alone, so tapping a bar still raises
 *   its tooltip; streaming scrollback ([ChartInteractionConfig.streamingState]) does not survive it,
 *   because the crosshair owns the drag.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
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
    crosshair: ChartCrosshair<BarData>? = null,
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
            animation = barConfig.animation,
            visibleWindow = barConfig.visibleWindow,
            displayData = {
                rememberAnimatedBarValues(
                    dataList = it,
                    animation = barConfig.animation,
                    enabled = barConfig.animateValueChanges,
                )
            },
            describe = { series, min, max ->
                generateBarChartDescription(data = series, minValue = min, maxValue = max)
            },
        ) { windowed, _ ->
            rememberHorizontalValueRange(dataList = windowed)
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, BarData>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = barConfig.tooltipConfig.orThemeDefault()
    val crosshairScope =
        rememberBarCrosshair(
            barConfig = barConfig,
            crosshair = crosshair,
            interactionConfig = interactionConfig,
            orientation = ChartOrientation.HORIZONTAL,
        )

    val scrubModifier =
        horizontalBarScrubModifier(
            base = modifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
            barConfig = barConfig,
            tooltipManager = tooltipManager,
        ).barCrosshairHandler(crosshair = crosshairScope, dataList = dataList)

    val chartModifier =
        buildInteractionModifier(
            base = scrubModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.barStreamingPan(streaming = chartState.streaming, crosshair = crosshairScope)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility = horizontalBarAccessibility(description = chartState.description, dataList = dataList),
            streaming = interactionConfig.streamingRender(chartState.streaming),
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

            drawHorizontalReferenceBandIfNeeded(
                barConfig = barConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
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
                    tooltipConfig = resolvedTooltipConfig,
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

            drawBarCrosshair(
                crosshair = crosshairScope,
                dataList = dataList,
                displayList = displayList,
                chartContext = chartContext,
                color = color,
                textMeasurer = textMeasurer,
            )
        }

        BarChartOverlays(tooltip = tooltip, tooltipManager = tooltipManager, crosshair = crosshairScope)
    }
}
