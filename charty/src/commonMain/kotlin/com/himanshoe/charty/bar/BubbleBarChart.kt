@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "FunctionNaming",
    "CyclomaticComplexMethod",
    "WildcardImport",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UnusedImports",
)

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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import kotlin.math.ceil
import kotlin.math.max

/**
 * Bubble Bar Chart - Display data as stacked bubbles in vertical columns
 *
 * A bubble bar chart presents categorical data with circles/bubbles stacked vertically.
 * The number and size of bubbles is proportional to the value they represent.
 * Ideal for creating visually appealing comparisons between discrete categories.
 *
 * Usage:
 * ```kotlin
 * BubbleBarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     bubbleConfig = BubbleBarChartConfig(
 *         barWidthFraction = 0.6f,
 *         bubbleRadius = 8.dp,
 *         bubbleSpacing = 4.dp,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bubbles, Gradient for gradient effect
 * @param bubbleConfig Configuration for bubble appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels (includes leftLabelRotation)
 * @param onBarClick Optional callback when a bar is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BubbleBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    bubbleConfig: BubbleBarChartConfig = BubbleBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bubble bar chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList, bubbleConfig.negativeValuesDrawMode) {
            val values = dataList.getValues()
            val calculatedMin = calculateMinValue(values)
            val calculatedMax = calculateMaxValue(values)
            val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
            finalMin to calculatedMax
        }

    val isBelowAxisMode = bubbleConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (bubbleConfig.animation is Animation.Enabled) 0f else 1f)
        }

    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }

    LaunchedEffect(bubbleConfig.animation) {
        if (bubbleConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = bubbleConfig.animation.duration),
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            if (onBarClick != null) {
                Modifier.pointerInput(dataList, bubbleConfig, onBarClick) {
                    detectTapGestures { offset ->
                        val clickedBar = barBounds.find { (rect, _) ->
                            rect.contains(offset)
                        }

                        clickedBar?.let { (rect, barData) ->
                            onBarClick.invoke(barData)
                            tooltipState = TooltipState(
                                content = bubbleConfig.tooltipFormatter(barData),
                                x = rect.left,
                                y = rect.top,
                                barWidth = rect.width,
                                position = bubbleConfig.tooltipPosition,
                            )
                        } ?: run {
                            tooltipState = null
                        }
                    }
                }
            } else {
                Modifier
            },
        ),
        xLabels = dataList.getLabels(),
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
        leftLabelRotation = scaffoldConfig.leftLabelRotation,
    ) { chartContext ->
        barBounds.clear()
        val baselineY =
            if (minValue < 0f && isBelowAxisMode) {
                chartContext.convertValueToYPosition(0f)
            } else {
                chartContext.bottom
            }

        dataList.fastForEachIndexed { index, bar ->
            val barX = chartContext.calculateBarLeftPosition(index, dataList.size, bubbleConfig.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(dataList.size, bubbleConfig.barWidthFraction)
            val barValueY = chartContext.convertValueToYPosition(bar.value)
            val isNegative = bar.value < 0f

            val barTop: Float
            val barHeight: Float

            if (isNegative) {
                barTop = baselineY
                val fullBarHeight = barValueY - baselineY
                barHeight = fullBarHeight * animationProgress.value
            } else {
                val fullBarHeight = baselineY - barValueY
                val animatedBarHeight = fullBarHeight * animationProgress.value
                barTop = baselineY - animatedBarHeight
                barHeight = animatedBarHeight
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

            drawBubbleBar(
                color = barColor,
                x = barX,
                y = barTop,
                width = barWidth,
                height = barHeight,
                bubbleRadius = bubbleConfig.bubbleRadius,
                bubbleSpacing = bubbleConfig.bubbleSpacing,
            )
        }

        bubbleConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }

        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = bubbleConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBubbleBar(
    color: ChartyColor,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    bubbleRadius: Float,
    bubbleSpacing: Float,
) {
    if (height <= 0f) return

    val centerX = x + width / 2f
    val diameter = bubbleRadius * 2
    val verticalStep = diameter + bubbleSpacing
    val bubbleCount = max(1, ceil(height / verticalStep).toInt())

    for (i in 0 until bubbleCount) {
        val bubbleY = y + height - (i * verticalStep) - bubbleRadius

        if (bubbleY < y - bubbleRadius) break

        val bubbleColor = when (color) {
            is ChartyColor.Solid -> color.color
            is ChartyColor.Gradient -> {
                val colors = color.colors
                val ratio = i.toFloat() / bubbleCount.coerceAtLeast(1)
                val scaledRatio = ratio * (colors.size - 1)
                val index = scaledRatio.toInt().coerceIn(0, colors.size - 2)
                val localRatio = scaledRatio - index
                lerp(colors[index], colors[index + 1], localRatio)
            }
        }

        drawCircle(
            color = bubbleColor,
            radius = bubbleRadius,
            center = Offset(centerX, bubbleY)
        )
    }
}

