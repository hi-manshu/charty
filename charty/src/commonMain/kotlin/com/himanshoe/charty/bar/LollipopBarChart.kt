package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.SeriesCrosshairHost
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.lollipop.createAxisConfig
import com.himanshoe.charty.bar.internal.bar.lollipop.createLollipopChartModifier
import com.himanshoe.charty.bar.internal.bar.lollipop.drawLollipopCrosshair
import com.himanshoe.charty.bar.internal.bar.lollipop.drawLollipops
import com.himanshoe.charty.bar.internal.bar.lollipop.drawTooltipHighlightIfNeeded
import com.himanshoe.charty.bar.internal.bar.lollipop.drawTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.lollipop.rememberLollipopCrosshair
import com.himanshoe.charty.bar.internal.bar.lollipop.rememberLollipopValueRange
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.seriesCrosshairHandler
import com.himanshoe.charty.bar.internal.bar.seriesStreamingPan
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

private const val DEFAULT_COLOR_HEX = 0xFF2196F3

/**
 * A composable function that displays a lollipop bar chart.
 *
 * Example:
 * ```kotlin
 * LollipopBarChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Jan", value = 40f),
 *             BarData(label = "Feb", value = 65f),
 *             BarData(label = "Mar", value = 50f),
 *         )
 *     },
 *     colors = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors The color or color scheme for the stems and circles.
 * @param config The configuration for the lollipop chart's appearance.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a lollipop is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   vertical guide line that snaps by x to the nearest lollipop and rests on the centre of its head —
 *   the same anchor the chart's persistent markers use. Its label reads that lollipop's value through
 *   [LollipopBarChartConfig.tooltipFormatter], the same text a tap shows. It is a drag gesture that
 *   leaves taps alone, so tapping a lollipop still raises its tooltip; streaming scrollback
 *   ([ChartInteractionConfig.streamingState]) does not survive it, because the crosshair owns the drag.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun LollipopBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColor.Solid(Color(DEFAULT_COLOR_HEX)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
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
            animation = config.animation,
            visibleWindow = config.visibleWindow,
            displayData = {
                rememberAnimatedBarValues(
                    dataList = it,
                    animation = config.animation,
                    enabled = config.animateValueChanges,
                )
            },
        ) { windowed, _ -> rememberLollipopValueRange(windowed) }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val animationProgress = chartState.animationProgress
    val tooltipManager = rememberTooltipManager<Offset, BarData>(dataKey = dataList)
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = config.tooltipConfig.orThemeDefault()
    val crosshairScope =
        rememberLollipopCrosshair(config = config, crosshair = crosshair, interactionConfig = interactionConfig)

    val clickModifier =
        createLollipopChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            config = config,
            lollipopBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
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
                    values = dataList.fastMap { it.value },
                    fallbackDescription = "Lollipop chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig = createAxisConfig(minValue, maxValue),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()

            drawLollipops(
                dataList = displayList,
                chartContext = chartContext,
                config = config,
                animationProgress = animationProgress.value,
                colors = colors,
                onBarClick = onBarClick,
                lollipopBounds = tooltipManager.bounds,
            )

            if (config.markers.isNotEmpty()) {
                drawPersistentMarkers(
                    chartContext = chartContext,
                    markers = config.markers,
                    pointPositions =
                        verticalBarMarkerPositions(
                            chartContext = chartContext,
                            values = displayList.fastMap { it.value },
                        ),
                    valueLabelFor = { index -> formatMarkerValue(displayList[index].value) },
                    textMeasurer = textMeasurer,
                )
            }

            drawTooltipHighlightIfNeeded(tooltipManager.tooltipState, config, chartContext)
            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(
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

            drawLollipopCrosshair(
                crosshair = crosshairScope,
                dataList = dataList,
                displayList = displayList,
                chartContext = chartContext,
                config = config,
                colors = colors,
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
