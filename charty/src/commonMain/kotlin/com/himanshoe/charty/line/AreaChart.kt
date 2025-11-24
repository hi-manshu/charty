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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.calculateMinValue
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.ext.getValues

/**
 * Area Chart - Line chart with filled area below the line
 *
 * An area chart is similar to a line chart but the area between the line and the axis
 * is filled with color/gradient. Useful for showing cumulative trends and emphasizing
 * the magnitude of change over time.
 *
 * Usage:
 * ```kotlin
 * AreaChart(
 *     data = {
 *         listOf(
 *             LineData("Jan", 20f),
 *             LineData("Feb", 45f),
 *             LineData("Mar", 30f),
 *             LineData("Apr", 70f)
 *         )
 *     },
 *     color = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF2196F3).copy(alpha = 0.3f))
 *     ),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for area fill (gradient recommended for fade effect)
 * @param lineConfig Configuration for line and points appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param fillAlpha Alpha transparency for the filled area (0.0f - 1.0f)
 */
@Composable
fun AreaChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(0xFF2196F3),
                Color(0xFF2196F3).copy(alpha = 0.3f),
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    fillAlpha: Float = 0.3f,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Area chart data cannot be empty" }
    require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1" }

    val (minValue, maxValue) =
        remember(dataList, lineConfig.negativeValuesDrawMode) {
            val values = dataList.getValues()
            calculateMinValue(values) to calculateMaxValue(values)
        }

    val isBelowAxisMode =
        lineConfig.negativeValuesDrawMode == com.himanshoe.charty.bar.config.NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
        }

    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier,
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
        val pointPositions =
            dataList.fastMapIndexed { index, point ->
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(point.value),
                )
            }

        val baselineY =
            if (minValue < 0f && isBelowAxisMode) {
                chartContext.convertValueToYPosition(0f)
            } else {
                chartContext.bottom
            }

        if (pointPositions.isNotEmpty()) {
            val areaPath =
                Path().apply {
                    moveTo(pointPositions[0].x, baselineY)
                    lineTo(pointPositions[0].x, pointPositions[0].y)

                    if (lineConfig.smoothCurve) {
                        for (i in 0 until pointPositions.size - 1) {
                            val current = pointPositions[i]
                            val next = pointPositions[i + 1]

                            val controlPoint1X = current.x + (next.x - current.x) / 3f
                            val controlPoint1Y = current.y
                            val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                            val controlPoint2Y = next.y

                            cubicTo(
                                controlPoint1X,
                                controlPoint1Y,
                                controlPoint2X,
                                controlPoint2Y,
                                next.x,
                                next.y,
                            )
                        }
                    } else {
                        // Straight lines through points
                        for (i in 1 until pointPositions.size) {
                            lineTo(pointPositions[i].x, pointPositions[i].y)
                        }
                    }

                    // Close path by going down to baseline and back to start
                    lineTo(pointPositions.last().x, baselineY)
                    close()
                }

            // Create gradient brush for area fill
            val areaBrush =
                when (color) {
                    is ChartyColor.Solid ->
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    color.color.copy(alpha = fillAlpha),
                                    color.color.copy(alpha = fillAlpha * 0.3f),
                                ),
                            startY = chartContext.top,
                            endY = chartContext.bottom,
                        )
                    is ChartyColor.Gradient ->
                        Brush.verticalGradient(
                            colors = color.colors.map { it.copy(alpha = it.alpha * fillAlpha) },
                            startY = chartContext.top,
                            endY = chartContext.bottom,
                        )
                }

            // Draw filled area with animation
            drawPath(
                path = areaPath,
                brush = areaBrush,
                style = Fill,
                alpha = animationProgress.value,
            )

            // Draw line on top of area
            val linePath =
                Path().apply {
                    moveTo(pointPositions[0].x, pointPositions[0].y)

                    if (lineConfig.smoothCurve) {
                        for (i in 0 until pointPositions.size - 1) {
                            val current = pointPositions[i]
                            val next = pointPositions[i + 1]

                            val controlPoint1X = current.x + (next.x - current.x) / 3f
                            val controlPoint1Y = current.y
                            val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                            val controlPoint2Y = next.y

                            cubicTo(
                                controlPoint1X,
                                controlPoint1Y,
                                controlPoint2X,
                                controlPoint2Y,
                                next.x,
                                next.y,
                            )
                        }
                    } else {
                        for (i in 1 until pointPositions.size) {
                            lineTo(pointPositions[i].x, pointPositions[i].y)
                        }
                    }
                }

            val lineBrush =
                when (color) {
                    is ChartyColor.Solid -> Brush.linearGradient(listOf(color.color, color.color))
                    is ChartyColor.Gradient -> Brush.linearGradient(color.colors)
                }

            drawPath(
                path = linePath,
                brush = lineBrush,
                style =
                    Stroke(
                        width = lineConfig.lineWidth,
                        cap = lineConfig.strokeCap,
                    ),
                alpha = animationProgress.value,
            )

            // Draw points
            if (lineConfig.showPoints) {
                pointPositions.fastForEachIndexed { index, position ->
                    val pointProgress = index.toFloat() / (pointPositions.size - 1)
                    if (pointProgress <= animationProgress.value) {
                        drawCircle(
                            brush = lineBrush,
                            radius = lineConfig.pointRadius,
                            center = position,
                            alpha = lineConfig.pointAlpha,
                        )
                    }
                }
            }
        }
    }
}
