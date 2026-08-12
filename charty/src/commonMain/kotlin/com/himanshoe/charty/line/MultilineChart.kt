package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartLegend
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.generateLineGroupChartDescription
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.getAllValues
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.multiline.MultilineChartOverlays
import com.himanshoe.charty.line.internal.multiline.MultilineDrawParams
import com.himanshoe.charty.line.internal.multiline.buildMultilineModifier
import com.himanshoe.charty.line.internal.multiline.drawMultilineContent

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
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it.
 */
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
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val effectiveLineConfig = crosshair?.let { lineConfig.copy(crosshairConfig = it.config) } ?: lineConfig
    val activeCrosshair = crosshair ?: lineConfig.crosshairConfig?.let { ChartCrosshair<MultilinePoint>(config = it) }

    val colorList = remember(colors) { colors.value }
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
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<MultilinePoint>(
            enabled = effectiveLineConfig.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
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
                        lineConfig = effectiveLineConfig,
                        pointBounds = pointBounds,
                        crosshairBounds = crosshairBounds,
                        onPointClick = onPointClick,
                        onTooltipStateChange = { state, _ -> tooltipState = state },
                        interactionConfig = interactionConfig,
                    ),
                xLabels = dataList.getLabels(),
                yAxisConfig =
                    AxisConfig(
                        minValue = minValue,
                        maxValue = maxValue,
                        steps = 6,
                        drawAxisAtZero = isBelowAxisMode,
                    ),
                config = scaffoldConfig,
            ) { chartContext ->
                updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
                drawMultilineContent(
                    MultilineDrawParams(
                        dataList = dataList,
                        chartContext = chartContext,
                        colorList = colorList,
                        lineConfig = effectiveLineConfig,
                        animationProgress = chartState.animationProgress.value,
                        pointBounds = pointBounds,
                        crosshairBounds = crosshairBounds,
                        onPointClick = onPointClick,
                        crosshairManager = crosshairManager,
                        crosshairState = animatedCrosshairState?.resolve(),
                        tooltipState = tooltipState,
                        textMeasurer = textMeasurer,
                        interactionConfig = interactionConfig,
                        drawCrosshairLabel = false,
                    ),
                )
            }

            MultilineChartOverlays(
                crosshairManager = crosshairManager,
                animatedCrosshairState = animatedCrosshairState?.resolve(),
                crosshair = activeCrosshair,
            )
        }
        if (lineConfig.legendLabels.isNotEmpty()) {
            ChartLegend(
                labels = lineConfig.legendLabels,
                colors = colorList,
                textStyle = lineConfig.legendTextStyle,
            )
        }
    }
}
