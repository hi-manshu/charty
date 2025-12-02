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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import kotlin.math.pow
import kotlin.math.sqrt

private const val DEFAULT_AXIS_STEPS = 6
private const val DEFAULT_COLOR_HEX = 0xFFE91E63
private const val TAP_RADIUS_MULTIPLIER = 2f
private const val CIRCLE_HIGHLIGHT_OUTER_PADDING = 3f
private const val CIRCLE_HIGHLIGHT_INNER_PADDING = 2f
private const val HIGHLIGHT_LINE_WIDTH = 1.5f
private const val HIGHLIGHT_LINE_ALPHA = 0.1f
private const val HIGHLIGHT_CIRCLE_ALPHA = 0.3f

/**
 * Lollipop Bar Chart - vertical line with a circular head for each value.
 *
 * This chart is similar to a traditional bar chart but uses a thin "stem" and a
 * configurable circle at the value position, making it visually lighter for
 * category comparisons.
 *
 * Configuration options allow customizing stem thickness, circle radius and circle
 * color, along with standard bar width fraction and animation.
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param colors Color configuration for stems and circles
 * @param config Configuration for lollipop chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a lollipop is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LollipopBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Solid(Color(DEFAULT_COLOR_HEX)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Lollipop bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberLollipopValueRange(dataList)
    val animationProgress = rememberLollipopAnimation(config.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val lollipopBounds = remember { mutableListOf<Pair<Offset, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createLollipopChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        config = config,
        lollipopBounds = lollipopBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue),
        config = scaffoldConfig,
    ) { chartContext ->
        lollipopBounds.clear()

        drawLollipops(
            dataList = dataList,
            chartContext = chartContext,
            config = config,
            animationProgress = animationProgress.value,
            colors = colors,
            onBarClick = onBarClick,
            lollipopBounds = lollipopBounds,
        )

        drawTooltipHighlightIfNeeded(tooltipState, config, chartContext)
        drawTooltipIfNeeded(tooltipState, config, textMeasurer, chartContext)
    }
}

@Composable
private fun rememberLollipopValueRange(dataList: List<BarData>): Pair<Float, Float> {
    return remember(dataList) {
        val values = dataList.getValues()
        0f to calculateMaxValue(values)
    }
}

@Composable
private fun rememberLollipopAnimation(animation: Animation): Animatable<Float, *> {
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
private fun createLollipopChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    config: LollipopBarChartConfig,
    lollipopBounds: List<Pair<Offset, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointerInput(dataList, config, onBarClick) {
            detectTapGestures { offset ->
                handleLollipopClick(offset, lollipopBounds, onBarClick, config, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

private fun handleLollipopClick(
    offset: Offset,
    lollipopBounds: List<Pair<Offset, BarData>>,
    onBarClick: (BarData) -> Unit,
    config: LollipopBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit,
) {
    val tapRadius = config.circleRadius * TAP_RADIUS_MULTIPLIER
    val clickedLollipop = lollipopBounds.minByOrNull { (position, _) ->
        calculateDistance(position, offset)
    }

    clickedLollipop?.let { (position, barData) ->
        val distance = calculateDistance(position, offset)

        if (distance <= tapRadius) {
            onBarClick.invoke(barData)
            onTooltipUpdate(
                TooltipState(
                    content = config.tooltipFormatter(barData),
                    x = position.x - config.circleRadius,
                    y = position.y,
                    barWidth = config.circleRadius * TAP_RADIUS_MULTIPLIER,
                    position = config.tooltipPosition,
                ),
            )
        } else {
            onTooltipUpdate(null)
        }
    } ?: onTooltipUpdate(null)
}

private fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return sqrt(dx.pow(2) + dy.pow(2))
}

private fun createAxisConfig(minValue: Float, maxValue: Float): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = true,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLollipops(
    dataList: List<BarData>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    config: LollipopBarChartConfig,
    animationProgress: Float,
    colors: ChartyColor,
    onBarClick: ((BarData) -> Unit)?,
    lollipopBounds: MutableList<Pair<Offset, BarData>>,
) {
    val baselineY = chartContext.bottom

    dataList.fastForEachIndexed { index, bar ->
        val barLeft = chartContext.calculateBarLeftPosition(index, dataList.size, config.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(dataList.size, config.barWidthFraction)
        val centerX = barLeft + barWidth / 2f

        val barValueY = chartContext.convertValueToYPosition(bar.value)
        val animatedTopY = baselineY - (baselineY - barValueY) * animationProgress

        if (onBarClick != null) {
            lollipopBounds.add(Offset(centerX, animatedTopY) to bar)
        }

        val chartyColor = bar.color ?: colors
        val circleChartyColor = config.circleColor ?: chartyColor

        val stemBrush = createStemBrush(chartyColor, baselineY, barValueY)
        val circleColor = getCircleColor(circleChartyColor, index)

        // Draw stem
        drawLine(
            brush = stemBrush,
            start = Offset(centerX, baselineY),
            end = Offset(centerX, animatedTopY),
            strokeWidth = config.stemThickness,
        )

        // Draw circle
        drawLollipopCircle(
            color = circleColor,
            center = Offset(centerX, animatedTopY),
            radius = config.circleRadius,
            strokeWidth = config.circleStrokeWidth,
        )
    }
}

private fun createStemBrush(
    chartyColor: ChartyColor,
    baselineY: Float,
    barValueY: Float,
): Brush {
    return when (chartyColor) {
        is ChartyColor.Solid ->
            Brush.verticalGradient(
                colors = listOf(chartyColor.color, chartyColor.color),
                startY = baselineY,
                endY = barValueY,
            )

        is ChartyColor.Gradient ->
            Brush.verticalGradient(
                colors = chartyColor.colors,
                startY = baselineY,
                endY = barValueY,
            )
    }
}

private fun getCircleColor(circleChartyColor: ChartyColor, index: Int): Color {
    return when (circleChartyColor) {
        is ChartyColor.Solid -> circleChartyColor.color
        is ChartyColor.Gradient -> circleChartyColor.colors[index % circleChartyColor.colors.size]
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLollipopCircle(
    color: Color,
    center: Offset,
    radius: Float,
    strokeWidth: Float,
) {
    if (strokeWidth > 0f) {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth),
        )
    } else {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipHighlightIfNeeded(
    tooltipState: TooltipState?,
    config: LollipopBarChartConfig,
    chartContext: com.himanshoe.charty.common.ChartContext,
) {
    tooltipState?.let { state ->
        val clickedPosition = Offset(
            state.x + config.circleRadius,
            state.y,
        )

        // Draw vertical highlight line
        drawLine(
            color = Color.Black.copy(alpha = HIGHLIGHT_LINE_ALPHA),
            start = Offset(clickedPosition.x, chartContext.top),
            end = Offset(clickedPosition.x, chartContext.bottom),
            strokeWidth = HIGHLIGHT_LINE_WIDTH,
        )

        // Draw highlight circles
        drawCircle(
            color = Color.White,
            radius = config.circleRadius + CIRCLE_HIGHLIGHT_OUTER_PADDING,
            center = clickedPosition,
        )
        drawCircle(
            color = Color.Black.copy(alpha = HIGHLIGHT_CIRCLE_ALPHA),
            radius = config.circleRadius + CIRCLE_HIGHLIGHT_INNER_PADDING,
            center = clickedPosition,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    config: LollipopBarChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    chartContext: com.himanshoe.charty.common.ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = config.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}
