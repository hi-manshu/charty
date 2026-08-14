package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateLineChartDescription
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.area.AreaChartDrawParams
import com.himanshoe.charty.line.internal.area.AreaChartOverlays
import com.himanshoe.charty.line.internal.area.buildAreaModifier
import com.himanshoe.charty.line.internal.area.calculateBaselineY
import com.himanshoe.charty.line.internal.area.calculatePointPositions
import com.himanshoe.charty.line.internal.area.createAxisConfig
import com.himanshoe.charty.line.internal.area.drawAreaChart
import com.himanshoe.charty.line.internal.area.drawTooltipHighlightIfNeeded
import com.himanshoe.charty.line.internal.area.drawTooltipIfNeeded
import com.himanshoe.charty.line.internal.area.rememberAreaValueRange
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.rememberThemedLineStyling

/**
 * A composable function that displays an area chart.
 *
 * Example:
 * ```kotlin
 * AreaChart(
 *     data = {
 *         listOf(
 *             LineData(label = "Jan", value = 20f),
 *             LineData(label = "Feb", value = 45f),
 *             LineData(label = "Mar", value = 30f),
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineData] points to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color The color or color scheme for the filled area.
 * @param lineConfig The configuration for the line and its points.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onPointClick A lambda function invoked when a point on the line is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it. It
 *   is a drag gesture that leaves taps alone, so tap-to-tooltip and the chart's click callback
 *   keep working alongside it; streaming scrollback ([ChartInteractionConfig.streamingState])
 *   does not, because the crosshair owns the drag.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun AreaChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                ChartyColors.Blue,
                ChartyColors.BlueAlpha30,
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<LineData> = ChartTooltip.canvas(),
    crosshair: ChartCrosshair<LineData>? = null,
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val fillAlpha = lineConfig.fillAlpha
    val styling = rememberThemedLineStyling(lineConfig = lineConfig, crosshair = crosshair)

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = lineConfig.animation,
            visibleWindow = lineConfig.visibleWindow,
            downsampleThreshold = lineConfig.downsampleThreshold,
            downsampleValue = { it.value },
            describe = { series, min, max ->
                generateLineChartDescription(data = series, minValue = min, maxValue = max)
            },
        ) { windowed, _ ->
            rememberAreaValueRange(
                dataList = windowed,
                negativeValuesDrawMode = lineConfig.negativeValuesDrawMode,
            )
        }
    val dataList = chartState.data
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val tooltipManager = rememberTooltipManager<Offset, LineData>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()

    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineData>(styling.config.crosshairConfig != null, interactionConfig)

    val chartModifier =
        modifier.then(
            buildAreaModifier(
                crosshairManager = crosshairManager,
                dataList = dataList,
                tooltipManager = tooltipManager,
                lineConfig = styling.config,
                onPointClick = onPointClick,
                interactionConfig = interactionConfig,
            ),
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
                createAxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    isBelowAxisMode = isBelowAxisMode,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            tooltipManager.clearBounds()
            val pointPositions =
                calculatePointPositions(dataList = dataList, chartContext = chartContext) {
                    tooltipManager.bounds.add(it)
                }
            val baselineY =
                calculateBaselineY(
                    minValue = minValue,
                    isBelowAxisMode = isBelowAxisMode,
                    chartContext = chartContext,
                )
            drawAreaChart(
                params =
                    AreaChartDrawParams(
                        dataList = dataList,
                        pointPositions = pointPositions,
                        baselineY = baselineY,
                        config = lineConfig,
                        color = color,
                        fillAlpha = fillAlpha,
                        animationProgress = chartState.animationProgress.value,
                        chartContext = chartContext,
                        textMeasurer = textMeasurer,
                    ),
            )
            drawTooltipHighlightIfNeeded(
                tooltipState = tooltipManager.tooltipState,
                lineConfig = lineConfig,
                pointBounds = tooltipManager.bounds,
                chartContext = chartContext,
                color = color,
            )
            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    tooltipConfig = styling.tooltipConfig,
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
            animatedCrosshairState?.resolve()?.let { crosshairState ->
                styling.config.crosshairConfig?.let { crosshairConfig ->
                    drawLineChartCrosshair(
                        state = crosshairState,
                        config = crosshairConfig,
                        chartContext = chartContext,
                        textMeasurer = textMeasurer,
                        chartColor = color,
                        drawLabel = false,
                    )
                }
            }
        }

        AreaChartOverlays(
            tooltipManager = tooltipManager,
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            tooltip = tooltip,
            crosshair = styling.activeCrosshair,
        )
    }
}
