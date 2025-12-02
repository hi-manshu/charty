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

package com.himanshoe.charty.line

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.calculateMinValue
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.ext.getValues
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Line Chart - Connect data points with lines
 *
 * A line chart displays information as a series of data points connected by straight line segments.
 * It is useful for showing trends over time or continuous data.
 *
 * Usage:
 * ```kotlin
 * LineChart(
 *     data = {
 *         listOf(
 *             LineData("Mon", 20f),
 *             LineData("Tue", 45f),
 *             LineData("Wed", 30f),
 *             LineData("Thu", 70f)
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         animation = Animation.Enabled()
 *     ),
 *     onPointClick = { lineData ->
 *         println("Clicked: ${lineData.label}")
 *     }
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for line and points
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onPointClick Optional callback when a point is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((LineData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Line chart data cannot be empty" }
    val (minValue, maxValue) = remember(dataList, lineConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }
    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = remember {
        Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
    }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, LineData>>() }
    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration),
            )
        }
    }
    val textMeasurer = rememberTextMeasurer()
    ChartScaffold(
        modifier = modifier.then(
            if (onPointClick != null) {
                Modifier.pointerInput(dataList, lineConfig, onPointClick) {
                    detectTapGestures { offset ->
                        val tapRadius = lineConfig.pointRadius * 2.5f
                        val clickedPoint = pointBounds.minByOrNull { (position, _) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            sqrt(dx.pow(2) + dy.pow(2))
                        }
                        clickedPoint?.let { (position, lineData) ->
                            val dx = position.x - offset.x
                            val dy = position.y - offset.y
                            val distance = sqrt(dx.pow(2) + dy.pow(2))
                            if (distance <= tapRadius) {
                                onPointClick.invoke(lineData)
                                tooltipState = TooltipState(
                                    content = lineConfig.tooltipFormatter(lineData),
                                    x = position.x - lineConfig.pointRadius,
                                    y = position.y,
                                    barWidth = lineConfig.pointRadius * 2,
                                    position = lineConfig.tooltipPosition,
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

        val pointPositions =
            dataList.fastMapIndexed { index, point ->
                val position = Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(point.value),
                )
                if (onPointClick != null) {
                    pointBounds.add(position to point)
                }
                position
            }

        if (lineConfig.smoothCurve) {
            val path = Path()

            if (pointPositions.isNotEmpty()) {
                path.moveTo(pointPositions[0].x, pointPositions[0].y)
                for (i in 0 until pointPositions.size - 1) {
                    val current = pointPositions[i]
                    val next = pointPositions[i + 1]
                    val controlPoint1X = current.x + (next.x - current.x) / 3f
                    val controlPoint1Y = current.y
                    val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                    val controlPoint2Y = next.y
                    path.cubicTo(
                        controlPoint1X,
                        controlPoint1Y,
                        controlPoint2X,
                        controlPoint2Y,
                        next.x,
                        next.y,
                    )
                }

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(color.value),
                    style =
                        Stroke(
                            width = lineConfig.lineWidth,
                            cap = lineConfig.strokeCap,
                        ),
                    alpha = animationProgress.value,
                )
            }
        } else {
            val segmentsToDraw = ((pointPositions.size - 1) * animationProgress.value).toInt()
            val segmentProgress = ((pointPositions.size - 1) * animationProgress.value) - segmentsToDraw
            for (i in 0 until segmentsToDraw) {
                drawLine(
                    brush = Brush.linearGradient(color.value),
                    start = pointPositions[i],
                    end = pointPositions[i + 1],
                    strokeWidth = lineConfig.lineWidth,
                    cap = lineConfig.strokeCap,
                )
            }
            if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
                val start = pointPositions[segmentsToDraw]
                val end = pointPositions[segmentsToDraw + 1]
                val partialEnd =
                    Offset(
                        x = start.x + (end.x - start.x) * segmentProgress,
                        y = start.y + (end.y - start.y) * segmentProgress,
                    )
                drawLine(
                    brush = Brush.linearGradient(color.value),
                    start = start,
                    end = partialEnd,
                    strokeWidth = lineConfig.lineWidth,
                    cap = lineConfig.strokeCap,
                )
            }
        }
        if (lineConfig.showPoints) {
            pointPositions.fastForEachIndexed { index, position ->
                // Only draw points up to animation progress
                val pointProgress = index.toFloat() / (pointPositions.size - 1)
                if (pointProgress <= animationProgress.value) {
                    drawCircle(
                        brush = Brush.linearGradient(color.value),
                        radius = lineConfig.pointRadius,
                        center = position,
                        alpha = lineConfig.pointAlpha,
                    )
                }
            }
        }
        lineConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }
        tooltipState?.let { state ->
            val clickedPosition = pointBounds.find { (_, data) ->
                lineConfig.tooltipFormatter(data) == state.content
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
                    radius = lineConfig.pointRadius + 3f,
                    center = position,
                )
                drawCircle(
                    brush = Brush.linearGradient(color.value),
                    radius = lineConfig.pointRadius + 2f,
                    center = position,
                )
            }
            drawTooltip(
                tooltipState = state,
                config = lineConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}
