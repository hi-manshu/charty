package com.himanshoe.charty.combo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.ext.getLabels
import com.himanshoe.charty.combo.internal.ComboChartConstants
import com.himanshoe.charty.combo.internal.ComboChartOverlays
import com.himanshoe.charty.combo.internal.ComboDrawParams
import com.himanshoe.charty.combo.internal.buildComboModifier
import com.himanshoe.charty.combo.internal.comboChartAccessibility
import com.himanshoe.charty.combo.internal.comboLineRange
import com.himanshoe.charty.combo.internal.comboPrimaryRange
import com.himanshoe.charty.combo.internal.drawComboContent
import com.himanshoe.charty.combo.internal.toSecondaryAxisConfig
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateComboChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeCrosshair
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a combo chart, combining a bar chart and a line chart.
 *
 * A combo chart is useful for visualizing two different data series on the same chart, allowing for
 * easy comparison of trends and magnitudes. Both the bar values and line values share a unified
 * y-axis, so the data ranges should be comparable.
 *
 * @param data A lambda function that returns a list of [ComboChartData], each containing a bar
 *   value and a line value.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param barColor The color or color scheme for the bars, defined by a [ChartyColor].
 * @param lineColor The color or color scheme for the line and its points, defined by a [ChartyColor].
 * @param comboConfig The configuration for the combo chart's appearance and behavior, defined by a
 *   [ComboChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels,
 *   defined by a [ChartScaffoldConfig].
 * @param onDataClick A lambda function invoked when a data point (bar or line point) is clicked,
 *   providing the corresponding [ComboChartData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it. It
 *   is a drag gesture that leaves taps alone, so tap-to-tooltip and the chart's click callback
 *   keep working alongside it; streaming scrollback ([ChartInteractionConfig.streamingState])
 *   does not, because the crosshair owns the drag.
 * @param tooltip How a tapped x position is presented: the built-in canvas bubble (the default), a
 *   custom Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. A tap
 *   resolves to the whole [ComboChartData] at that x — the bar rect and the line point share one hit
 *   area, so tapping either shows one tooltip describing both series — and its text comes from
 *   [ComboChartConfig.tooltipFormatter], reading `label: Bar=…, Line=…` by default. The tooltip is
 *   shown alongside [onDataClick], never instead of it.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun ComboChart(
    data: () -> List<ComboChartData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    barColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_BAR_COLOR)),
    lineColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_LINE_COLOR)),
    comboConfig: ComboChartConfig = ComboChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onDataClick: ((ComboChartData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<ComboChartData>? = null,
    tooltip: ChartTooltip<ComboChartData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val themedCrosshairConfig = crosshair?.config.orThemeCrosshair()
    val effectiveComboConfig =
        crosshair?.let { comboConfig.copy(crosshairConfig = themedCrosshairConfig) } ?: comboConfig
    val activeCrosshair = crosshair ?: comboConfig.crosshairConfig?.let { ChartCrosshair<ComboChartData>(config = it) }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = comboConfig.animation,
            visibleWindow = comboConfig.visibleWindow,
            displayData = {
                rememberAnimatedComboData(
                    dataList = it,
                    animation = comboConfig.animation,
                    enabled = comboConfig.animateValueChanges,
                )
            },
            describe = { series, _, _ -> generateComboChartDescription(series) },
        ) { windowed, _ ->
            remember(windowed, comboConfig.negativeValuesDrawMode, comboConfig.secondaryAxisForLine) {
                comboPrimaryRange(
                    dataList = windowed,
                    negativeValuesDrawMode = comboConfig.negativeValuesDrawMode,
                    secondaryAxisForLine = comboConfig.secondaryAxisForLine,
                )
            }
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue

    val rawSecondaryLineRange =
        remember(dataList, comboConfig.secondaryAxisForLine) {
            comboLineRange(dataList = dataList, secondaryAxisForLine = comboConfig.secondaryAxisForLine)
        }
    val secondaryLineRange =
        rawSecondaryLineRange?.let { (rawLineMin, rawLineMax) ->
            rememberAnimatedRange(
                minValue = rawLineMin,
                maxValue = rawLineMax,
                animation = comboConfig.animation,
                active = chartState.streaming != null,
            )
        }

    val isBelowAxisMode = comboConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val tooltipManager = rememberTooltipManager<Rect, ComboChartData>(dataKey = dataList)
    val tapEnabled = onDataClick != null || tooltip !is NoneTooltip
    val crosshairBounds = remember { mutableListOf<Pair<Offset, ComboChartData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<ComboChartData>(
            enabled = effectiveComboConfig.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
            streamingState = interactionConfig.streamingState,
        )
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = comboConfig.tooltipConfig.orThemeDefault()

    val clickModifier =
        buildComboModifier(
            crosshairManager = crosshairManager,
            comboConfig = effectiveComboConfig,
            dataList = dataList,
            crosshairBounds = crosshairBounds,
            dataBounds = tooltipManager.bounds,
            onDataClick = onDataClick,
            onTooltipStateChange = tooltipManager::updateTooltip,
            tapEnabled = tapEnabled,
        )

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.streamingPan(streaming = chartState.streaming, orientation = ChartOrientation.VERTICAL)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility = comboChartAccessibility(chartDescription = chartState.description, dataList = dataList),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 6,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
            secondaryYAxisConfig = secondaryLineRange.toSecondaryAxisConfig(),
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            tooltipManager.clearBounds()
            drawComboContent(
                ComboDrawParams(
                    tooltipConfig = resolvedTooltipConfig,
                    dataList = displayList,
                    chartContext = chartContext,
                    comboConfig = effectiveComboConfig,
                    barColor = barColor,
                    lineColor = lineColor,
                    minValue = minValue,
                    lineRange = secondaryLineRange,
                    isBelowAxisMode = isBelowAxisMode,
                    animationProgress = chartState.animationProgress.value,
                    recordDataBounds = tapEnabled,
                    dataBounds = tooltipManager.bounds,
                    crosshairBounds =
                        crosshairBounds.takeIf { crosshairManager != null },
                    crosshairState = animatedCrosshairState?.resolve(),
                    tooltipState = tooltipManager.tooltipState,
                    drawTooltipBubble = tooltip.isCanvas(),
                    textMeasurer = textMeasurer,
                    interactionConfig = interactionConfig,
                    drawCrosshairLabel = false,
                ),
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )

        ComboChartOverlays(
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            crosshair = activeCrosshair,
        )
    }
}

/**
 * Returns [dataList] with both the bar value and the line value of every point tweened toward their
 * targets whenever the data changes. The two series share one progress, so they never drift apart
 * mid-transition. When [enabled] is `false` or [animation] is disabled the list is returned unchanged.
 */
@Composable
private fun rememberAnimatedComboData(
    dataList: List<ComboChartData>,
    animation: Animation,
    enabled: Boolean,
): List<ComboChartData> {
    val flattened = remember(dataList) { dataList.flatMap { listOf(it.barValue, it.lineValue) } }
    val animatedValues =
        rememberAnimatedValues(
            targetValues = flattened,
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        if (animatedValues.size != flattened.size) {
            dataList
        } else {
            dataList.fastMapIndexed { index, item ->
                item.copy(
                    barValue = animatedValues[index * 2],
                    lineValue = animatedValues[index * 2 + 1],
                )
            }
        }
    }
}
