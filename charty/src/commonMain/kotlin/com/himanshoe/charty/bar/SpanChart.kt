package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_BLUE
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_GREEN
import com.himanshoe.charty.bar.internal.span.DEFAULT_COLOR_ORANGE
import com.himanshoe.charty.bar.internal.span.SpanDrawParams
import com.himanshoe.charty.bar.internal.span.calculateAxisOffset
import com.himanshoe.charty.bar.internal.span.createAxisConfig
import com.himanshoe.charty.bar.internal.span.createSpanChartModifier
import com.himanshoe.charty.bar.internal.span.drawSpans
import com.himanshoe.charty.bar.internal.span.rememberSpanValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * Span Chart - Display ranges/spans horizontally across categories
 *
 * @param data Lambda returning list of span data to display
 * @param modifier Modifier for the chart
 * @param colors Color configuration
 * @param barConfig Configuration for span bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSpanClick Called when a span bar is tapped, providing the tapped [SpanData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [SpanData].
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SpanChart(
    data: () -> List<SpanData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(DEFAULT_COLOR_BLUE),
                Color(DEFAULT_COLOR_GREEN),
                Color(DEFAULT_COLOR_ORANGE),
            ),
        ),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSpanClick: ((SpanData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (SpanData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Span chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberSpanValueRange(dataList, colors)
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

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createAxisConfig(minValue, maxValue),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Span chart, ${fullDataList.size} spans.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

            tooltipManager.clearBounds()
            val axisOffset = calculateAxisOffset(scaffoldConfig)

            drawSpans(
                SpanDrawParams(
                    dataList = dataList,
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

            if (tooltipContent == null) {
                drawTooltipIfNeeded(
                    tooltipState = tooltipManager.tooltipState,
                    tooltipConfig = barConfig.tooltipConfig,
                    textMeasurer = textMeasurer,
                    chartContext = chartContext,
                )
            }

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
        }

        if (tooltipContent != null) {
            ChartTooltipOverlay(
                item = tooltipManager.selectedItem,
                anchor = tooltipManager.tooltipState,
                config = barConfig.tooltipConfig,
                modifier = Modifier.matchParentSize(),
                content = tooltipContent,
            )
        }
    }
}
