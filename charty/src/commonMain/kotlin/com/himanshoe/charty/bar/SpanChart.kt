package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Span Chart - Display ranges/spans horizontally across categories
 *
 * A span chart (also known as a range chart or Gantt-style chart) displays horizontal bars
 * showing ranges or time periods for different categories. Each span has a start and end value,
 * making it ideal for visualizing schedules, timelines, or value ranges.
 *
 * Usage:
 * ```kotlin
 * SpanChart(
 *     data = {
 *         listOf(
 *             SpanData("Category 1", startValue = 5f, endValue = 15f),
 *             SpanData("Category 2", startValue = 10f, endValue = 25f),
 *             SpanData("Category 3", startValue = 3f, endValue = 18f)
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     ),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Medium
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of span data to display
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient recommended for distinguishing multiple spans
 * @param barConfig Configuration for span bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
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
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Span chart data cannot be empty" }

    val (minValue, maxValue) = rememberSpanValueRange(dataList, colors)
    val animationProgress = rememberChartAnimation(barConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val spanBounds = remember { mutableListOf<Pair<Rect, SpanData>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createSpanChartModifier(
        modifier = modifier,
        onSpanClick = onSpanClick,
        dataList = dataList,
        barConfig = barConfig,
        spanBounds = spanBounds,
        onTooltipUpdate = { tooltipState = it }
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = createAxisConfig(minValue, maxValue),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        spanBounds.clear()
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
                onSpanBoundCalculated = { spanBounds.add(it) },
            )
        )

        drawTooltipIfNeeded(
            tooltipState = tooltipState,
            tooltipConfig = barConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartContext = chartContext,
        )
    }
}

