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
    colors: ChartyColor = ChartyColor.Solid(Color(0xFFE91E63)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Lollipop bar chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList) {
            val values = dataList.getValues()
            0f to calculateMaxValue(values)
        }

    val animationProgress =
        remember {
            Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
        }

    LaunchedEffect(config.animation) {
        if (config.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.animation.duration),
            )
        }
    }

    // State to track which bar is currently showing a tooltip
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    // Store lollipop positions for hit testing (storing circle center and data)
    val lollipopBounds = remember { mutableListOf<Pair<Offset, BarData>>() }

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            if (onBarClick != null) {
                Modifier.pointerInput(dataList, config, onBarClick) {
                    detectTapGestures { offset ->
                        // Find the closest lollipop circle within tap radius
                        val tapRadius = config.circleRadius * 2f
                        val clickedLollipop = lollipopBounds.minByOrNull { (position, _) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            sqrt(dx.pow(2) + dy.pow(2))
                        }

                        clickedLollipop?.let { (position, barData) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            val distance = sqrt(dx.pow(2) + dy.pow(2))

                            if (distance <= tapRadius) {
                                onBarClick.invoke(barData)
                                tooltipState = TooltipState(
                                    content = config.tooltipFormatter(barData),
                                    x = position.x - config.circleRadius,
                                    y = position.y,
                                    barWidth = config.circleRadius * 2,
                                    position = config.tooltipPosition,
                                )
                            } else {
                                tooltipState = null
                            }
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
                drawAxisAtZero = true,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        lollipopBounds.clear()

        val baselineY = chartContext.bottom

        dataList.fastForEachIndexed { index, bar ->
            val barLeft = chartContext.calculateBarLeftPosition(index, dataList.size, config.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(dataList.size, config.barWidthFraction)
            val centerX = barLeft + barWidth / 2f

            val barValueY = chartContext.convertValueToYPosition(bar.value)
            val animatedTopY = baselineY - (baselineY - barValueY) * animationProgress.value

            // Store lollipop position for hit testing
            if (onBarClick != null) {
                lollipopBounds.add(Offset(centerX, animatedTopY) to bar)
            }

            val chartyColor = bar.color ?: colors
            val circleChartyColor = config.circleColor ?: chartyColor

            val stemBrush =
                when (chartyColor) {
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

            val circleColor =
                when (circleChartyColor) {
                    is ChartyColor.Solid -> circleChartyColor.color
                    is ChartyColor.Gradient -> circleChartyColor.colors[index % circleChartyColor.colors.size]
                }

            drawLine(
                brush = stemBrush,
                start = Offset(centerX, baselineY),
                end = Offset(centerX, animatedTopY),
                strokeWidth = config.stemThickness,
            )

            if (config.circleStrokeWidth > 0f) {
                drawCircle(
                    color = circleColor,
                    radius = config.circleRadius,
                    center = Offset(centerX, animatedTopY),
                    style = Stroke(width = config.circleStrokeWidth),
                )
            } else {
                drawCircle(
                    color = circleColor,
                    radius = config.circleRadius,
                    center = Offset(centerX, animatedTopY),
                )
            }
        }

        // Draw highlight and tooltip for clicked lollipop
        tooltipState?.let { state ->
            // Find the clicked lollipop position
            val clickedPosition = lollipopBounds.find { (_, data) ->
                config.tooltipFormatter(data) == state.content
            }?.first

            clickedPosition?.let { position ->
                // Draw subtle vertical indicator line
                drawLine(
                    color = Color.Black.copy(alpha = 0.1f),
                    start = Offset(position.x, chartContext.top),
                    end = Offset(position.x, chartContext.bottom),
                    strokeWidth = 1.5f,
                )

                // Draw highlight circle around the clicked lollipop
                drawCircle(
                    color = Color.White,
                    radius = config.circleRadius + 3f,
                    center = position,
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = config.circleRadius + 2f,
                    center = position,
                )
            }

            // Draw tooltip
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
}
