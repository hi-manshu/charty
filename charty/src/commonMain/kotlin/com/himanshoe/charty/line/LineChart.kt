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
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.common.config.Animation

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
 *     color = ChartyColor.Solid(Color.Blue),
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         animation = Animation.Enabled()
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of line data points to display
 * @param modifier Modifier for the chart
 * @param color Color configuration for line and points
 * @param lineConfig Configuration for line appearance and behavior
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = data()
    require(dataList.isNotEmpty()) { "Line chart data cannot be empty" }

    val maxValue = calculateMaxValue(dataList.getValues())

    // Animation
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
            minValue = 0f,
            maxValue = maxValue,
            steps = 6
        ),
        config = scaffoldConfig
    ) { chartContext ->
        // Calculate all point positions using ChartContext helpers with fastMapIndexed
        val pointPositions = dataList.fastMapIndexed { index, point ->
            Offset(
                x = chartContext.calculateCenteredXPosition(index, dataList.size),
                y = chartContext.convertValueToYPosition(point.value)
            )
        }

        // Calculate how many segments to draw based on animation progress
        val segmentsToDraw = ((pointPositions.size - 1) * animationProgress.value).toInt()
        val segmentProgress = ((pointPositions.size - 1) * animationProgress.value) - segmentsToDraw

        // Draw lines connecting consecutive points with animation
        for (i in 0 until segmentsToDraw) {
            drawLine(
                brush = Brush.linearGradient(color.value),
                start = pointPositions[i],
                end = pointPositions[i + 1],
                strokeWidth = lineConfig.lineWidth,
                cap = lineConfig.strokeCap
            )
        }

        // Draw partial segment for smooth animation
        if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
            val start = pointPositions[segmentsToDraw]
            val end = pointPositions[segmentsToDraw + 1]
            val partialEnd = Offset(
                x = start.x + (end.x - start.x) * segmentProgress,
                y = start.y + (end.y - start.y) * segmentProgress
            )
            drawLine(
                brush = Brush.linearGradient(color.value),
                start = start,
                end = partialEnd,
                strokeWidth = lineConfig.lineWidth,
                cap = lineConfig.strokeCap
            )
        }

        // Optionally draw circular markers at data points with fastForEach
        if (lineConfig.showPoints) {
            pointPositions.fastForEachIndexed { index, position ->
                // Only draw points up to animation progress
                val pointProgress = index.toFloat() / (pointPositions.size - 1)
                if (pointProgress <= animationProgress.value) {
                    drawCircle(
                        brush = Brush.linearGradient(color.value),
                        radius = lineConfig.pointRadius,
                        center = position,
                        alpha = lineConfig.pointAlpha
                    )
                }
            }
        }
    }
}



