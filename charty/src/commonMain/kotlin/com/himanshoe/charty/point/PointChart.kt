package com.himanshoe.charty.point

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.point.data.PointData

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
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of point data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform color, Gradient for multi-color points
 * @param pointConfig Configuration for point appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun PointChart(
    data: () -> List<PointData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    pointConfig: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Point chart data cannot be empty" }

    val (minValue, maxValue) = remember(dataList, pointConfig.negativeValuesDrawMode) {
        val values = dataList.getValues()
        calculateMinValue(values) to calculateMaxValue(values)
    }

    val isBelowAxisMode = pointConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = remember {
        Animatable(if (pointConfig.animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(pointConfig.animation) {
        if (pointConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = pointConfig.animation.duration)
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
        dataList.fastForEachIndexed { index, point ->
            val pointProgress = index.toFloat() / dataList.size
            val pointAnimationProgress = ((animationProgress.value - pointProgress) * dataList.size).coerceIn(0f, 1f)

            val pointX = chartContext.calculateCenteredXPosition(index, dataList.size)
            val pointY = chartContext.convertValueToYPosition(point.value)

            val pointColor = when (color) {
                is ChartyColor.Solid -> color.color
                is ChartyColor.Gradient -> color.colors[index % color.colors.size]
            }

            if (pointAnimationProgress > 0f) {
                drawCircle(
                    color = pointColor,
                    radius = pointConfig.pointRadius * pointAnimationProgress,
                    center = Offset(pointX, pointY),
                    alpha = pointConfig.pointAlpha * pointAnimationProgress
                )
            }
        }
    }
}

