@file:Suppress("FunctionNaming") // Composable functions should start with capital letter

package com.himanshoe.charty.point

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.PointData
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Point Chart (Scatter Chart) - Display data as individual points
 *
 * A point chart (also known as scatter plot) displays values for two variables as points.
 * Each point represents an observation, with position determined by the X and Y values.
 * Useful for showing correlation, distribution, or outliers in data.
 *
 * Usage:
 * ```kotlin
 * PointChart(
 *     data = {
 *         listOf(
 *             PointData("A", 17f),
 *             PointData("B", 25f),
 *             PointData("C", 44f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color.Blue),
 *     pointConfig = PointChartConfig(
 *         pointRadius = 8f,
 *         pointAlpha = 1f,
 *         animation = Animation.Enabled()
 *     ),
 *     onPointClick = { pointData ->
 *         println("Clicked: ${pointData.label}")
 *     }
 * )
 * ```
 *
 * @param data Lambda returning list of point data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform color, Gradient for multi-color points
 * @param pointConfig Configuration for point appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onPointClick Optional callback when a point is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun PointChart(
    data: () -> List<PointData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    pointConfig: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((PointData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Point chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList, pointConfig.negativeValuesDrawMode) {
            val values = dataList.getValues()
            calculateMinValue(values) to calculateMaxValue(values)
        }

    val isBelowAxisMode = pointConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (pointConfig.animation is Animation.Enabled) 0f else 1f)
        }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, PointData>>() }

    LaunchedEffect(pointConfig.animation) {
        if (pointConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = pointConfig.animation.duration),
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            if (onPointClick != null) {
                Modifier.pointerInput(dataList, pointConfig, onPointClick) {
                    detectTapGestures { offset ->
                        val tapRadius = pointConfig.pointRadius * 2.5f
                        val clickedPoint = pointBounds.minByOrNull { (position, _) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            sqrt(dx.pow(2) + dy.pow(2))
                        }

                        clickedPoint?.let { (position, pointData) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            val distance = sqrt(dx.pow(2) + dy.pow(2))

                            if (distance <= tapRadius) {
                                onPointClick.invoke(pointData)
                                tooltipState = TooltipState(
                                    content = pointConfig.tooltipFormatter(pointData),
                                    x = position.x - pointConfig.pointRadius,
                                    y = position.y,
                                    barWidth = pointConfig.pointRadius * 2,
                                    position = pointConfig.tooltipPosition,
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
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        pointBounds.clear()

        dataList.fastForEachIndexed { index, point ->
            val pointProgress = index.toFloat() / dataList.size
            val pointAnimationProgress = ((animationProgress.value - pointProgress) * dataList.size).coerceIn(0f, 1f)
            val pointX = chartContext.calculateCenteredXPosition(index, dataList.size)
            val pointY = chartContext.convertValueToYPosition(point.value)
            val position = Offset(pointX, pointY)
            if (onPointClick != null) {
                pointBounds.add(position to point)
            }

            if (pointAnimationProgress > 0f) {
                drawCircle(
                    brush = Brush.linearGradient(color.value),
                    radius = pointConfig.pointRadius * pointAnimationProgress,
                    center = position,
                    alpha = pointConfig.pointAlpha * pointAnimationProgress,
                )
            }
        }
        pointConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }
        tooltipState?.let { state ->
            val clickedPosition = pointBounds.find { (_, data) ->
                pointConfig.tooltipFormatter(data) == state.content
            }?.first

            clickedPosition?.let { position ->
                drawLine(
                    color = Color.Black.copy(alpha = 0.1f),
                    start = Offset(position.x, chartContext.top),
                    end = Offset(position.x, chartContext.bottom),
                    strokeWidth = 1.5f,
                )
                drawCircle(
                    color = Color.White,
                    radius = pointConfig.pointRadius + 3f,
                    center = position,
                )
                drawCircle(
                    brush = Brush.linearGradient(color.value),
                    radius = pointConfig.pointRadius + 2f,
                    center = position,
                )
            }
            drawTooltip(
                tooltipState = state,
                config = pointConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}
