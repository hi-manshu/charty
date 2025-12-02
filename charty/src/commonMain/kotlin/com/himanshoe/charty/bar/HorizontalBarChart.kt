package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private const val DEFAULT_AXIS_STEPS = 6
private const val AXIS_OFFSET_MULTIPLIER = 20f

/**
 * Parameters for drawing horizontal bars
 */
private data class HorizontalBarDrawParams(
    val dataList: List<BarData>,
    val chartContext: ChartContext,
    val barConfig: BarChartConfig,
    val baselineX: Float,
    val axisOffset: Float,
    val animationProgress: Float,
    val color: ChartyColor,
    val isBelowAxisMode: Boolean,
    val minValue: Float,
    val maxValue: Float,
    val onBarClick: ((BarData) -> Unit)?,
    val onBarBoundCalculated: (Pair<Rect, BarData>) -> Unit,
)

/**
 * Horizontal Bar Chart - Display data as horizontal bars
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars.
 * The length of each bar is proportional to the value it represents.
 * Ideal for comparing categories with long labels or when you have many categories.
 *
 * Usage:
 * ```kotlin
 * HorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarData("Category A", 100f),
 *             BarData("Category B", 150f),
 *             BarData("Category C", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Large
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for horizontal gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Horizontal bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberHorizontalValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberHorizontalAnimation(barConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = createAxisConfig(minValue, maxValue, drawAxisAtZero),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        barBounds.clear()
        val axisOffset = calculateAxisOffset(scaffoldConfig)
        val baselineX = calculateBaselineX(drawAxisAtZero, minValue, maxValue, chartContext, axisOffset)

        drawHorizontalBars(
            HorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                barConfig = barConfig,
                baselineX = baselineX,
                axisOffset = axisOffset,
                animationProgress = animationProgress.value,
                color = color,
                isBelowAxisMode = isBelowAxisMode,
                minValue = minValue,
                maxValue = maxValue,
                onBarClick = onBarClick,
                onBarBoundCalculated = { barBounds.add(it) },
            )
        )

        drawReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawTooltipIfNeeded(tooltipState, barConfig, textMeasurer, chartContext)
    }
}

@Composable
private fun rememberHorizontalValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val calculatedMin = calculateMinValue(values)
        val calculatedMax = calculateMaxValue(values)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
        finalMin to calculatedMax
    }
}

@Composable
private fun rememberHorizontalAnimation(animation: Animation): Animatable<Float, *> {
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

private fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = drawAxisAtZero,
    )
}

private fun calculateAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float {
    return if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
}

private fun calculateBaselineX(
    drawAxisAtZero: Boolean,
    minValue: Float,
    maxValue: Float,
    chartContext: ChartContext,
    axisOffset: Float
): Float {
    return if (drawAxisAtZero) {
        val range = maxValue - minValue
        val zeroNormalized = (0f - minValue) / range
        chartContext.left + (zeroNormalized * chartContext.width)
    } else {
        chartContext.left + axisOffset
    }
}

private fun DrawScope.drawHorizontalBars(params: HorizontalBarDrawParams) {
    val range = params.maxValue - params.minValue

    params.dataList.fastForEachIndexed { index, bar ->
        val barHeight = params.chartContext.height / params.dataList.size
        val barY = params.chartContext.top + (barHeight * index)
        val barThickness = barHeight * params.barConfig.barWidthFraction
        val centeredBarY = barY + (barHeight - barThickness) / 2

        val valueNormalized = (bar.value - params.minValue) / range
        val barValueX = params.chartContext.left + params.axisOffset +
            (valueNormalized * (params.chartContext.width - params.axisOffset))
        val isNegative = bar.value < 0f

        val (barLeft, barWidth) = calculateHorizontalBarDimensions(
            isNegative = isNegative,
            isBelowAxisMode = params.isBelowAxisMode,
            baselineX = params.baselineX,
            barValueX = barValueX,
            animationProgress = params.animationProgress,
        )

        if (params.onBarClick != null) {
            params.onBarBoundCalculated(
                Rect(
                    left = barLeft,
                    top = centeredBarY,
                    right = barLeft + barWidth,
                    bottom = centeredBarY + barThickness,
                ) to bar
            )
        }

        val barColor = bar.color ?: params.color
        val brush = Brush.horizontalGradient(
            colors = barColor.value,
            startX = params.chartContext.left,
            endX = params.chartContext.right,
        )

        drawRoundedHorizontalBar(
            brush = brush,
            x = barLeft,
            y = centeredBarY,
            width = barWidth,
            height = barThickness,
            isNegative = isNegative,
            isBelowAxisMode = params.isBelowAxisMode,
            cornerRadius = params.barConfig.cornerRadius.value,
        )
    }
}

private fun calculateHorizontalBarDimensions(
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    baselineX: Float,
    barValueX: Float,
    animationProgress: Float,
): Pair<Float, Float> {
    return if (isNegative && isBelowAxisMode) {
        val fullBarWidth = baselineX - barValueX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = barValueX
        barLeft to barWidth
    } else {
        val fullBarWidth = barValueX - baselineX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = baselineX
        barLeft to barWidth
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    barConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = ChartOrientation.HORIZONTAL,
            config = referenceLineConfig,
            textMeasurer = textMeasurer,
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
 * Helper function to draw a horizontal bar with rounded corners
 */
private fun DrawScope.drawRoundedHorizontalBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            if (isNegative && isBelowAxisMode) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            }
        }

    drawPath(path, brush)
}

