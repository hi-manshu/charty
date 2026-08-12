package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedSpanValues
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_BLUE
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_GREEN
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_ORANGE
import com.himanshoe.charty.bar.internal.span.SpanDrawParams
import com.himanshoe.charty.bar.internal.span.calculateAxisOffset
import com.himanshoe.charty.bar.internal.span.createAxisConfig
import com.himanshoe.charty.bar.internal.span.createSpanChartModifier
import com.himanshoe.charty.bar.internal.span.drawSpans
import com.himanshoe.charty.bar.internal.span.rememberSpanValueRange
import com.himanshoe.charty.bar.internal.span.spanEndMarkerPositions
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * Span Chart - Display ranges/spans horizontally across categories
 *
 * Example:
 * ```kotlin
 * SpanChart(
 *     data = {
 *         listOf(
 *             SpanData("Task A", startValue = 1f, endValue = 5f),
 *             SpanData("Task B", startValue = 3f, endValue = 8f),
 *         )
 *     },
 * )
 * ```
 *
 * @param data Lambda returning list of span data to display
 * @param modifier Modifier for the chart
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors Color configuration
 * @param barConfig Configuration for span bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSpanClick Called when a span bar is tapped, providing the tapped [SpanData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun SpanChart(
    data: () -> List<SpanData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(DEFAULT_COLOR_BLUE),
                Color(DEFAULT_COLOR_GREEN),
                Color(DEFAULT_COLOR_ORANGE),
            ),
        ),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onSpanClick: ((SpanData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<SpanData> = ChartTooltip.canvas(),
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
            streamingState = interactionConfig.streamingState,
        )
    val dataList = visible.data

    val displayList =
        rememberAnimatedSpanValues(
            dataList = dataList,
            animation = barConfig.animation,
            enabled = barConfig.animateValueChanges,
        )
    val (rawMinValue, rawMaxValue) = rememberSpanValueRange(dataList = displayList, colors = colors)
    val (minValue, maxValue) =
        rememberAnimatedRange(
            minValue = rawMinValue,
            maxValue = rawMaxValue,
            animation = barConfig.animation,
            active = visible.streaming != null,
        )
    val animationProgress = rememberChartAnimation(barConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, SpanData>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createSpanChartModifier(
            modifier = modifier,
            onSpanClick = onSpanClick,
            dataList = dataList,
            barConfig = barConfig,
            spanBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = clickModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    val pan = interactionConfig.streamingPan(streaming = visible.streaming, orientation = ChartOrientation.HORIZONTAL)

    Box(modifier = chartModifier.then(pan)) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription =
                        interactionConfig.accessibilityDescription ?: "Span chart, ${fullDataList.size} spans.",
                    dataPointDescriptions =
                        dataList.fastMapIndexed { index, item ->
                            "Point ${index + 1} of ${dataList.size}: ${item.label}, " +
                                "${item.startValue} to ${item.endValue}"
                        },
                ),
            streaming = interactionConfig.streamingRender(visible.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createAxisConfig(minValue, maxValue),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            tooltipManager.clearBounds()
            val axisOffset = calculateAxisOffset(scaffoldConfig)

            drawSpans(
                SpanDrawParams(
                    dataList = displayList,
                    chartContext = chartContext,
                    barConfig = barConfig,
                    axisOffset = axisOffset,
                    minValue = minValue,
                    maxValue = maxValue,
                    animationProgress = animationProgress.value,
                    colors = colors,
                    onSpanClick = onSpanClick,
                    onSpanBoundCalculated = { tooltipManager.bounds.add(it) },
                    recordBounds = onSpanClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawPersistentMarkers(
                chartContext = chartContext,
                markers = barConfig.markers,
                pointPositions =
                    spanEndMarkerPositions(
                        chartContext = chartContext,
                        endValues = displayList.fastMap { it.endValue },
                        minValue = minValue,
                        maxValue = maxValue,
                        axisOffset = axisOffset,
                    ),
                valueLabelFor = { index -> formatMarkerValue(displayList[index].endValue) },
                textMeasurer = textMeasurer,
            )

            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    tooltipConfig = barConfig.tooltipConfig,
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
