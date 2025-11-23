package com.himanshoe.charty.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.ext.calculateMaxValue
import com.himanshoe.charty.line.ext.calculateMinValue
import com.himanshoe.charty.line.ext.getAllValues
import com.himanshoe.charty.line.ext.getLabels

/**
 * Multiline Chart - Display multiple line series on the same chart
 *
 * A multiline chart displays multiple data series as separate lines on the same chart.
 * Perfect for comparing trends of multiple metrics or categories over time.
 * Each line starts smoothly from the axis intersection point (0,0).
 *
 * Usage:
 * ```kotlin
 * MultilineChart(
 *     data = {
 *         listOf(
 *             LineGroup("Mon", listOf(20f, 35f, 15f)),
 *             LineGroup("Tue", listOf(45f, 28f, 38f)),
 *             LineGroup("Wed", listOf(30f, 52f, 25f)),
 *             LineGroup("Thu", listOf(70f, 40f, 55f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(
 *             Color(0xFFE91E63),
 *             Color(0xFF2196F3),
 *             Color(0xFF4CAF50)
 *         )
 *     ),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         smoothCurve = true
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of line groups, each containing values for all series at that X position
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient recommended for distinguishing multiple lines
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun MultilineChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Gradient(
        listOf(
            Color(0xFFE91E63),
            Color(0xFF2196F3),
            Color(0xFF4CAF50)
        )
    ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Multiline chart data cannot be empty" }

    val (minValue, maxValue, colorList) = remember(dataList, colors, lineConfig.negativeValuesDrawMode) {
        val allValues = dataList.getAllValues()
        Triple(
            calculateMinValue(allValues),
            calculateMaxValue(allValues),
            colors.value
        )
    }

    val isBelowAxisMode = lineConfig.negativeValuesDrawMode == com.himanshoe.charty.bar.config.NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = remember {
        Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
    }
    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration)
            )
        }
    }

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode
        ),
        config = scaffoldConfig
    ) { chartContext ->
        val seriesCount = dataList.firstOrNull()?.values?.size ?: 0

        for (seriesIndex in 0 until seriesCount) {
            val seriesColor = colorList[seriesIndex % colorList.size]

            val pointPositions = dataList.fastMapIndexed { index, group ->
                val value = group.values.getOrNull(seriesIndex) ?: 0f
                Offset(
                    x = chartContext.calculateCenteredXPosition(index, dataList.size),
                    y = chartContext.convertValueToYPosition(value)
                )
            }

            if (pointPositions.isNotEmpty()) {
                val path = Path()
                val startX = chartContext.left
                val startY = chartContext.bottom
                val firstPoint = pointPositions[0]

                if (lineConfig.smoothCurve) {
                    // Smooth start with cubic bezier from (0,0) to first point
                    val control1X = startX + (firstPoint.x - startX) / 3f
                    val control2X = startX + 2 * (firstPoint.x - startX) / 3f
                    val control2Y = firstPoint.y
                    path.moveTo(startX, startY)
                    path.cubicTo(control1X, startY, control2X, control2Y, firstPoint.x, firstPoint.y)

                    // Draw smooth curves between data points
                    for (i in 0 until pointPositions.size - 1) {
                        val current = pointPositions[i]
                        val next = pointPositions[i + 1]

                        val controlPoint1X = current.x + (next.x - current.x) / 3f
                        val controlPoint1Y = current.y
                        val controlPoint2X = current.x + 2 * (next.x - current.x) / 3f
                        val controlPoint2Y = next.y

                        path.cubicTo(
                            controlPoint1X, controlPoint1Y,
                            controlPoint2X, controlPoint2Y,
                            next.x, next.y
                        )
                    }
                } else {
                    // Straight line start from (0,0) to first point
                    path.moveTo(startX, startY)
                    path.lineTo(firstPoint.x, firstPoint.y)

                    // Draw straight lines between data points
                    for (i in 1 until pointPositions.size) {
                        path.lineTo(pointPositions[i].x, pointPositions[i].y)
                    }
                }

                // Draw the path with animation
                drawPath(
                    path = path,
                    color = seriesColor,
                    style = Stroke(
                        width = lineConfig.lineWidth,
                        cap = lineConfig.strokeCap
                    ),
                    alpha = animationProgress.value
                )
            }

            // Draw points if enabled
            if (lineConfig.showPoints) {
                pointPositions.fastForEachIndexed { index, position ->
                    // Only draw points up to animation progress
                    val pointProgress = if (lineConfig.animation is Animation.Enabled) {
                        ((index + 1).toFloat() / pointPositions.size).coerceAtMost(animationProgress.value * 1.2f)
                    } else {
                        1f
                    }

                    if (pointProgress > 0f) {
                        drawCircle(
                            color = seriesColor,
                            radius = lineConfig.pointRadius,
                            center = position,
                            alpha = (pointProgress.coerceIn(0f, 1f) * lineConfig.pointAlpha)
                        )
                    }
                }
            }
        }
    }
}

