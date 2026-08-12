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
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barchart.BarDrawParams
import com.himanshoe.charty.bar.internal.bar.barchart.calculateBarBaselineY
import com.himanshoe.charty.bar.internal.bar.barchart.createBarAxisConfig
import com.himanshoe.charty.bar.internal.bar.barchart.createBarChartModifier
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBars
import com.himanshoe.charty.bar.internal.bar.barchart.rememberBarValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawReferenceBand
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

/**
 * A composable function that displays a bar chart.
 *
 * A bar chart uses rectangular bars to compare values across categories.
 * Supports positive and negative values, click interactions, tooltips,
 * data labels, and animated entry.
 *
 * Usage:
 * ```kotlin
 * BarChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Jan", value = 100f),
 *             BarData(label = "Feb", value = 150f),
 *             BarData(label = "Mar", value = 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Large,
 *         showDataLabels = true,
 *         animation = Animation.Default,
 *     )
 * )
 * ```
 *
 * @param data A lambda returning the list of [BarData] points to display.
 * @param modifier Modifier applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color Color or gradient for the bars, defined by a [ChartyColor].
 * @param barConfig Appearance and behaviour configuration — width fraction, corner radius,
 *   animation, negative-value mode, data labels, tooltip, and reference line.
 * @param scaffoldConfig Styling for the chart scaffold — axes, grid, and labels.
 * @param onBarClick Called with the [BarData] of the tapped bar. Pass `null` to disable.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun BarChart(
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

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = barConfig.animation,
            visibleWindow = barConfig.visibleWindow,
            displayData = {
                rememberAnimatedBarData(
                    dataList = it,
                    animation = barConfig.animation,
                    enabled = barConfig.animateValueChanges,
                )
            },
            describe = { series, min, max ->
                generateBarChartDescription(data = series, minValue = min, maxValue = max)
            },
        ) { windowed, _ ->
            rememberBarValueRange(
                dataList = windowed,
                negativeValuesDrawMode = barConfig.negativeValuesDrawMode,
            )
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    val clickModifier =
        createBarChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            barConfig = barConfig,
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

    val pan = interactionConfig.streamingPan(streaming = chartState.streaming, orientation = ChartOrientation.VERTICAL)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription = chartState.description,
                    dataPointDescriptions = buildDataPointDescriptions(dataList.getLabels(), dataList.getValues()),
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                createBarAxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    isBelowAxisMode = isBelowAxisMode,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val baselineY =
                calculateBarBaselineY(
                    minValue = minValue,
                    isBelowAxisMode = isBelowAxisMode,
                    chartContext = chartContext,
                )

            barConfig.referenceBand?.let { band ->
                drawReferenceBand(
                    chartContext = chartContext,
                    orientation = ChartOrientation.VERTICAL,
                    config = band,
                    textMeasurer = textMeasurer,
                )
            }

            drawBars(
                BarDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    barConfig = barConfig,
                    baselineY = baselineY,
                    animationProgress = animationProgress.value,
                    color = color,
                    barBounds = tooltipManager.bounds,
                    textMeasurer = textMeasurer,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawBarReferenceLineIfNeeded(
                barConfig = barConfig,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
            )
            if (tooltip.isCanvas()) {
                drawBarTooltipIfNeeded(
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
 * Returns [dataList] with each bar's value tweened toward its target whenever the data changes, so
 * bars glide to their new heights. When [enabled] is `false` or [animation] is disabled the list is
 * returned unchanged. See [rememberAnimatedValues].
 */
@Composable
private fun rememberAnimatedBarData(
    dataList: List<BarData>,
    animation: Animation,
    enabled: Boolean,
): List<BarData> {
    val animatedValues =
        rememberAnimatedValues(
            targetValues = dataList.fastMap { it.value },
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        dataList.fastMapIndexed { index, bar -> bar.copy(value = animatedValues[index]) }
    }
}
