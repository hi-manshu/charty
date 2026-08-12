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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartLegend
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.generateLineGroupChartDescription
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
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
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaChartConstants
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaChartOverlays
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaDrawParams
import com.himanshoe.charty.line.internal.stackedarea.buildStackedAreaModifier
import com.himanshoe.charty.line.internal.stackedarea.calculateStackedCumulativeValues
import com.himanshoe.charty.line.internal.stackedarea.drawStackedAreaContent

/**
 * A composable function that displays a stacked area chart.
 *
 * Example:
 * ```kotlin
 * StackedAreaChart(
 *     data = {
 *         listOf(
 *             LineGroup(label = "Jan", values = listOf(10f, 20f, 5f)),
 *             LineGroup(label = "Feb", values = listOf(15f, 18f, 8f)),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors The color or color scheme for the stacked areas.
 * @param lineConfig The configuration for the lines' appearance and behavior.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param fillAlpha The alpha transparency for the filled areas, ranging from 0.0f to 1.0f.
 * @param onAreaClick A lambda function invoked when an area is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun StackedAreaChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(StackedAreaChartConstants.DEFAULT_COLOR_1),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_2),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_3),
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    fillAlpha: Float = 0.7f,
    onAreaClick: ((StackedAreaPoint) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<LineGroup>? = null,
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val effectiveLineConfig = crosshair?.let { lineConfig.copy(crosshairConfig = it.config) } ?: lineConfig
    val activeCrosshair = crosshair ?: lineConfig.crosshairConfig?.let { ChartCrosshair<LineGroup>(config = it) }
    require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1" }

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
                generateLineGroupChartDescription(data = series, chartTypeName = "stacked area")
            },
        ) { windowed, _ ->
            remember(windowed) {
                0f to calculateMaxValue(calculateStackedCumulativeValues(windowed))
            }
        }
    val dataList = chartState.data
    val maxValue = chartState.maxValue

    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val areaSegmentBounds = remember { mutableListOf<Triple<Rect, Path, StackedAreaPoint>>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, LineGroup>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineGroup>(
            enabled = effectiveLineConfig.crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
        )
    val textMeasurer = rememberTextMeasurer()

    val clickModifier =
        buildStackedAreaModifier(
            crosshairManager = crosshairManager,
            lineConfig = effectiveLineConfig,
            dataList = dataList,
            crosshairBounds = crosshairBounds,
            areaSegmentBounds = areaSegmentBounds,
            onAreaClick = onAreaClick,
            onTooltipStateChange = { state, _ -> tooltipState = state },
        )

    Column(modifier = modifier) {
        val chartModifier =
            buildInteractionModifier(
                base = Modifier.fillMaxSize().then(clickModifier),
                interactionConfig = interactionConfig,
                dataList = dataList,
            )

        Box(modifier = Modifier.weight(1f)) {
            ChartScaffold(
                accessibility =
                    ChartAccessibility(
                        contentDescription = chartState.description,
                    ),
                streaming = interactionConfig.streamingRender(chartState.streaming),
                modifier = chartModifier,
                xLabels = dataList.getLabels(),
                yAxisConfig = AxisConfig(minValue = 0f, maxValue = maxValue, steps = 6, drawAxisAtZero = false),
                config = scaffoldConfig,
            ) { chartContext ->
                updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
                areaSegmentBounds.clear()
                drawStackedAreaContent(
                    StackedAreaDrawParams(
                        dataList = dataList,
                        chartContext = chartContext,
                        colorList = colorList,
                        lineConfig = effectiveLineConfig,
                        fillAlpha = fillAlpha,
                        animationProgress = chartState.animationProgress.value,
                        onAreaClick = onAreaClick,
                        areaSegmentBounds = areaSegmentBounds,
                        crosshairBounds =
                            crosshairBounds.takeIf { crosshairManager != null },
                        crosshairManager = crosshairManager,
                        crosshairState = animatedCrosshairState?.resolve(),
                        tooltipState = tooltipState,
                        textMeasurer = textMeasurer,
                        interactionConfig = interactionConfig,
                        drawCrosshairLabel = false,
                    ),
                )
            }

            StackedAreaChartOverlays(
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
