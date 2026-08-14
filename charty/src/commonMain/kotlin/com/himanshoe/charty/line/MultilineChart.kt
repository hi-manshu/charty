package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartLegend
import com.himanshoe.charty.common.ChartOverlays
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.generateLineGroupChartDescription
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.getAllValues
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.multiline.MultilineDrawParams
import com.himanshoe.charty.line.internal.multiline.buildMultilineModifier
import com.himanshoe.charty.line.internal.multiline.drawMultilineContent
import com.himanshoe.charty.line.internal.multiline.resolveMultilineTapHandler
import com.himanshoe.charty.line.internal.rememberThemedLineStyling

/**
 * A composable function that displays a multiline chart.
 *
 * Example:
 * ```kotlin
 * MultilineChart(
 *     data = {
 *         listOf(
 *             LineGroup(label = "Jan", values = listOf(20f, 12f)),
 *             LineGroup(label = "Feb", values = listOf(45f, 30f)),
 *             LineGroup(label = "Mar", values = listOf(30f, 25f)),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors The color or color scheme for the lines.
 * @param lineConfig The configuration for the lines' appearance and behavior.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onPointClick A lambda function invoked when a point is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it. It
 *   is a drag gesture that leaves taps alone, so tap-to-tooltip and the chart's click callback
 *   keep working alongside it; streaming scrollback ([ChartInteractionConfig.streamingState])
 *   does not, because the crosshair owns the drag.
 * @param tooltip How a tapped point is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. A tap resolves
 *   to the single nearest [MultilinePoint], so it names one series rather than every series at that
 *   x. Its text comes from [LineChartConfig.tooltipFormatter], which takes a
 *   [com.himanshoe.charty.line.data.LineData]: the point is projected onto one with its series
 *   number folded into the label, reading `Jan Line 2: 12` by default. The tooltip is shown
 *   alongside [onPointClick], never instead of it.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun MultilineChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColors.DefaultMultiline,
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((MultilinePoint) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<MultilinePoint>? = null,
    tooltip: ChartTooltip<MultilinePoint> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val styling = rememberThemedLineStyling(lineConfig = lineConfig, crosshair = crosshair)

    val colorList = remember(colors) { colors.value }
    val legendColors = remember(colorList) { colorList.map { stop -> ChartyColor.Solid(stop) } }
    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = lineConfig.animation,
            visibleWindow = lineConfig.visibleWindow,
            downsampleThreshold = lineConfig.downsampleThreshold,
            downsampleValue = { it.values.sum() },
            describe = { series, _, _ ->
                generateLineGroupChartDescription(data = series, chartTypeName = "multiline")
            },
        ) { windowed, _ ->
            remember(windowed) {
                val allValues = windowed.getAllValues()
                calculateMinValue(allValues) to calculateMaxValue(allValues)
            }
        }
    val dataList = chartState.data
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue

    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val tooltipManager = rememberTooltipManager<Offset, MultilinePoint>(dataKey = dataList)
    val tapEnabled = onPointClick != null || tooltip !is NoneTooltip
    val crosshairBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<MultilinePoint>(
            enabled = styling.config.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
            streamingState = interactionConfig.streamingState,
        )
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            ChartScaffold(
                accessibility =
                    ChartAccessibility(
                        contentDescription = chartState.description,
                    ),
                streaming = interactionConfig.streamingRender(chartState.streaming),
                modifier =
                    buildMultilineModifier(
                        base = Modifier.fillMaxSize(),
                        crosshairManager = crosshairManager,
                        dataList = dataList,
                        lineConfig = styling.config,
                        pointBounds = tooltipManager.bounds,
                        crosshairBounds = crosshairBounds,
                        onPointTap =
                            resolveMultilineTapHandler(
                                onPointClick = onPointClick,
                                tapEnabled = tapEnabled,
                            ),
                        onTooltipStateChange = tooltipManager::updateTooltip,
                        interactionConfig = interactionConfig,
                    ),
                xLabels = dataList.getLabels(),
                yAxisConfig =
                    AxisConfig(
                        minValue = minValue,
                        maxValue = maxValue,
                        steps = ChartConstants.DEFAULT_AXIS_STEPS,
                        drawAxisAtZero = isBelowAxisMode,
                    ),
                config = scaffoldConfig,
            ) { chartContext ->
                updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
                drawMultilineContent(
                    MultilineDrawParams(
                        tooltipConfig = styling.tooltipConfig,
                        dataList = dataList,
                        chartContext = chartContext,
                        colorList = colorList,
                        lineConfig = styling.config,
                        animationProgress = chartState.animationProgress.value,
                        pointBounds = tooltipManager.bounds,
                        crosshairBounds = crosshairBounds,
                        recordPointBounds = tapEnabled,
                        crosshairManager = crosshairManager,
                        crosshairState = animatedCrosshairState?.resolve(),
                        tooltipState = tooltipManager.tooltipState,
                        drawTooltipBubble = tooltip.isCanvas(),
                        textMeasurer = textMeasurer,
                        interactionConfig = interactionConfig,
                    ),
                )
            }

            ChartOverlays(
                tooltip = tooltip,
                tooltipItem = tooltipManager.selectedItem,
                tooltipAnchor = tooltipManager.tooltipState,
                crosshair = styling.activeCrosshair,
                crosshairItem = crosshairManager?.selectedItem,
                crosshairState = animatedCrosshairState?.resolve(),
            )
        }
        if (lineConfig.legendLabels.isNotEmpty()) {
            ChartLegend(
                labels = lineConfig.legendLabels,
                colors = legendColors,
                textStyle = lineConfig.legendTextStyle,
            )
        }
    }
}
