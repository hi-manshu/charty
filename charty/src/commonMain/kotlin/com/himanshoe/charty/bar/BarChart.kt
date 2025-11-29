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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

/**
 * Bar Chart - Display data as vertical bars
 *
 * A bar chart presents categorical data with rectangular bars. The height of each bar
 * is proportional to the value it represents. Ideal for comparing discrete categories.
 *
 * Usage:
 * ```kotlin
 * BarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         roundedTopCorners = true,
 *         topCornerRadius = CornerRadius.Large,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bars, Gradient for vertical gradient effect
 * @param barConfig Configuration for bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param leftLabelRotation Rotation for Y-axis labels. Default is LabelRotation.Straight. Use LabelRotation.Angle45Negative for -45-degree rotation.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    leftLabelRotation: LabelRotation = LabelRotation.Straight,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberAnimationProgress(barConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        barConfig = barConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
        leftLabelRotation = leftLabelRotation,
    ) { chartContext ->
        barBounds.clear()
        val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

        drawBars(
            dataList = dataList,
            chartContext = chartContext,
            barConfig = barConfig,
            baselineY = baselineY,
            animationProgress = animationProgress.value,
            color = color,
            onBarClick = onBarClick,
            barBounds = barBounds,
        )

        drawReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawTooltipIfNeeded(tooltipState, barConfig, textMeasurer, chartContext)
    }
}

/**
 * Remember the animation progress based on the bar configuration
 */
@Composable
private fun rememberAnimationProgress(animationConfig: Animation): Animatable<Float, *> {
    val animationProgress = remember {
        Animatable(if (animationConfig is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(animationConfig) {
        if (animationConfig is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationConfig.duration),
            )
        }
    }

    return animationProgress
}

/**
 * Calculate the baseline Y position for bars, considering negative values and axis position
 */
private fun calculateBaselineY(minValue: Float, isBelowAxisMode: Boolean, chartContext: ChartContext) =
    if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }

/**
 * Draw the bars on the chart
 */
private fun DrawScope.drawBars(
    dataList: List<BarData>,
    chartContext: ChartContext,
    barConfig: BarChartConfig,
    baselineY: Float,
    animationProgress: Float,
    color: ChartyColor,
    onBarClick: ((BarData) -> Unit)?,
    barBounds: MutableList<Pair<Rect, BarData>>,
) {
    dataList.fastForEachIndexed { index, bar ->
        val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)
        val barValueY = chartContext.convertValueToYPosition(bar.value)
        val isNegative = bar.value < 0f

        val (barTop, barHeight) = if (isNegative) {
            baselineY to (barValueY - baselineY) * animationProgress
        } else {
            val animatedBarHeight = (baselineY - barValueY) * animationProgress
            baselineY - animatedBarHeight to animatedBarHeight
        }

        if (onBarClick != null) {
            barBounds.add(
                Rect(
                    left = barX,
                    top = barTop,
                    right = barX + barWidth,
                    bottom = barTop + barHeight,
                ) to bar,
            )
        }

        val barColor = bar.color ?: color
        val brush = with(chartContext) { barColor.toVerticalGradientBrush() }

        drawRoundedBar(
            brush = brush,
            x = barX,
            y = barTop,
            width = barWidth,
            height = barHeight,
            isNegative = isNegative,
            isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS,
            cornerRadius = barConfig.cornerRadius.value,
        )
    }
}

/**
 * Draw the reference line if configured
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    barConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = referenceLineConfig,
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Draw the tooltip if the state is not null
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    barConfig: BarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
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
 * Helper function to create the chart modifier with tap gestures for bar clicks
 */
@Composable
private fun createChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    barConfig: BarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return modifier.then(
        if (onBarClick != null) {
            Modifier.pointerInput(dataList, barConfig, onBarClick) {
                detectTapGestures { offset ->
                    val clickedBar = barBounds.find { (rect, _) ->
                        rect.contains(offset)
                    }

                    clickedBar?.let { (rect, barData) ->
                        onBarClick.invoke(barData)
                        onTooltipUpdate(
                            TooltipState(
                                content = barConfig.tooltipFormatter(barData),
                                x = rect.left,
                                y = rect.top,
                                barWidth = rect.width,
                                position = barConfig.tooltipPosition,
                            ),
                        )
                    } ?: run {
                        onTooltipUpdate(null)
                    }
                }
            }
        } else {
            Modifier
        },
    )
}

/**
 * Helper function to remember the value range (min, max) for the Y axis
 */
@Composable
private fun rememberValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val calculatedMin = calculateMinValue(values)
        val calculatedMax = calculateMaxValue(values)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
        finalMin to calculatedMax
    }
}

/**
 * Helper function to create the axis configuration for the Y axis
 */
private fun createAxisConfig(minValue: Float, maxValue: Float, isBelowAxisMode: Boolean): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = 6,
        drawAxisAtZero = isBelowAxisMode,
    )
}

/**
 * Helper function to draw a bar with rounded corners based on bar position
 */
private fun DrawScope.drawRoundedBar(
    brush: androidx.compose.ui.graphics.Brush,
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
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            }
        }
    drawPath(path, brush)
}
