package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private const val DEFAULT_COLOR_BLUE = 0xFF2196F3
private const val DEFAULT_COLOR_GREEN = 0xFF4CAF50
private const val DEFAULT_COLOR_ORANGE = 0xFFFF9800
private const val DEFAULT_AXIS_STEPS = 6
private const val AXIS_OFFSET_MULTIPLIER = 20f

/**
 * Parameters for drawing spans
 */
private data class SpanDrawParams(
    val dataList: List<SpanData>,
    val chartContext: ChartContext,
    val barConfig: BarChartConfig,
    val axisOffset: Float,
    val minValue: Float,
    val maxValue: Float,
    val animationProgress: Float,
    val colors: ChartyColor,
    val onSpanClick: ((SpanData) -> Unit)?,
    val onSpanBoundCalculated: (Pair<Rect, SpanData>) -> Unit,
)

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
    val animationProgress = rememberSpanAnimation(barConfig.animation)
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

        drawTooltipIfNeeded(tooltipState, barConfig, textMeasurer, chartContext)
    }
}

@Composable
private fun rememberSpanValueRange(
    dataList: List<SpanData>,
    colors: ChartyColor
): Pair<Float, Float> {
    return remember(dataList, colors) {
        val allValues = dataList.flatMap { listOf(it.startValue, it.endValue) }
        Pair(
            allValues.minOrNull() ?: 0f,
            allValues.maxOrNull() ?: 100f,
        )
    }
}

@Composable
private fun rememberSpanAnimation(animation: Animation): Animatable<Float, *> {
    val animationProgress = remember {
        Animatable(if (animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(animation) {
        if (animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animation.duration),
            )
        }
    }

    return animationProgress
}

@Composable
private fun createSpanChartModifier(
    onSpanClick: ((SpanData) -> Unit)?,
    dataList: List<SpanData>,
    barConfig: BarChartConfig,
    spanBounds: List<Pair<Rect, SpanData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onSpanClick != null) {
        modifier.pointerInput(dataList, barConfig, onSpanClick) {
            detectTapGestures { offset ->
                handleSpanClick(offset, spanBounds, onSpanClick, barConfig, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

private fun handleSpanClick(
    offset: androidx.compose.ui.geometry.Offset,
    spanBounds: List<Pair<Rect, SpanData>>,
    onSpanClick: (SpanData) -> Unit,
    barConfig: BarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit
) {
    val clickedSpan = spanBounds.find { (rect, _) -> rect.contains(offset) }

    clickedSpan?.let { (rect, spanData) ->
        onSpanClick.invoke(spanData)
        onTooltipUpdate(
            TooltipState(
                content = "${spanData.label}: ${spanData.startValue} - ${spanData.endValue}",
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = barConfig.tooltipPosition,
            )
        )
    } ?: onTooltipUpdate(null)
}

private fun createAxisConfig(minValue: Float, maxValue: Float): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = false,
    )
}

private fun calculateAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float {
    return if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
}

private fun DrawScope.drawSpans(params: SpanDrawParams) {
    val range = params.maxValue - params.minValue

    params.dataList.fastForEachIndexed { index, span ->
        val spanChartyColor = span.color ?: params.colors
        val barHeight = params.chartContext.height / params.dataList.size
        val barY = params.chartContext.top + (barHeight * index)
        val barThickness = barHeight * params.barConfig.barWidthFraction
        val centeredBarY = barY + (barHeight - barThickness) / 2

        val startNormalized = (span.startValue - params.minValue) / range
        val endNormalized = (span.endValue - params.minValue) / range
        val startX = params.chartContext.left +
            params.axisOffset + (startNormalized * (params.chartContext.width - params.axisOffset))
        val endX = params.chartContext.left +
            params.axisOffset + (endNormalized * (params.chartContext.width - params.axisOffset))

        val fullSpanWidth = endX - startX
        val animatedSpanWidth = fullSpanWidth * params.animationProgress

        if (params.onSpanClick != null && animatedSpanWidth > 0) {
            params.onSpanBoundCalculated(
                Rect(
                    left = startX,
                    top = centeredBarY,
                    right = startX + animatedSpanWidth,
                    bottom = centeredBarY + barThickness,
                ) to span
            )
        }

        val brush = Brush.horizontalGradient(
            colors = spanChartyColor.value,
            startX = startX,
            endX = endX,
        )

        drawRoundedSpan(
            brush = brush,
            x = startX,
            y = centeredBarY,
            width = animatedSpanWidth,
            height = barThickness,
            cornerRadius = params.barConfig.cornerRadius.value,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    barConfig: BarChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    chartContext: ChartContext
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = barConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * Helper function to draw a span (horizontal bar) with fully rounded corners
 */
private fun DrawScope.drawRoundedSpan(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
) {
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height,
                topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
            ),
        )
    }
    drawPath(path, brush)
}
