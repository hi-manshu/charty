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
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import kotlin.math.sqrt

/**
 * Bubble Chart - Display data as circles with variable sizes
 *
 * A bubble chart is a variation of a scatter plot where each point is represented by
 * a circle (bubble) whose size represents a third dimension of data. Useful for
 * visualizing three variables at once: X position, Y position, and size.
 *
 * Usage:
 * ```kotlin
 * BubbleChart(
 *     data = {
 *         listOf(
 *             BubbleData("Product A", yValue = 50f, size = 100f),
 *             BubbleData("Product B", yValue = 75f, size = 200f),
 *             BubbleData("Product C", yValue = 60f, size = 150f)
 *         )
 *     },
 *     color = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     ),
 *     pointConfig = PointChartConfig(
 *         pointRadius = 30f // Maximum bubble radius
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bubble data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bubbles, Gradient for multi-color bubbles
 * @param pointConfig Configuration for bubble appearance (pointRadius is the max bubble size)
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param minBubbleRadius Minimum bubble radius in pixels
 */
@Composable
fun BubbleChart(
    data: () -> List<BubbleData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(Color.Blue),
    pointConfig: PointChartConfig = PointChartConfig(pointRadius = 30f),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    minBubbleRadius: Float = 10f,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bubble chart data cannot be empty" }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(pointConfig.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }

    data class BubbleSizeInfo(
        val minValue: Float,
        val maxValue: Float,
        val minSize: Float,
        val maxSize: Float,
        val sizeRange: Float,
    )

    val sizeInfo =
        remember(dataList) {
            val yValues = dataList.map { it.yValue }
            val sizes = dataList.map { it.size }
            val min = sizes.minOrNull() ?: 0f
            val max = sizes.maxOrNull() ?: 1f
            BubbleSizeInfo(
                calculateMinValue(yValues),
                calculateMaxValue(yValues),
                min,
                max,
                max - min,
            )
        }

    val minValue = sizeInfo.minValue
    val maxValue = sizeInfo.maxValue
    val minSize = sizeInfo.minSize
    val sizeRange = sizeInfo.sizeRange

    val isBelowAxisMode = pointConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (pointConfig.animation is Animation.Enabled) 0f else 1f)
        }

    LaunchedEffect(pointConfig.animation) {
        if (pointConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = pointConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        dataList.fastForEachIndexed { index, bubble ->
            val bubbleProgress = index.toFloat() / dataList.size
            val bubbleAnimationProgress = ((animationProgress.value - bubbleProgress) * dataList.size).coerceIn(0f, 1f)

            val bubbleX = chartContext.calculateCenteredXPosition(index, dataList.size)
            val bubbleY = chartContext.convertValueToYPosition(bubble.yValue)

            val normalizedSize =
                if (sizeRange > 0f) {
                    (bubble.size - minSize) / sizeRange
                } else {
                    0.5f
                }
            val radiusRange = pointConfig.pointRadius - minBubbleRadius
            val bubbleRadius = minBubbleRadius + (sqrt(normalizedSize) * radiusRange)

            val bubbleColor =
                when (color) {
                    is ChartyColor.Solid -> color.color
                    is ChartyColor.Gradient -> color.colors[index % color.colors.size]
                }

            if (bubbleAnimationProgress > 0f) {
                drawCircle(
                    color = bubbleColor.copy(alpha = 0.3f),
                    radius = bubbleRadius * bubbleAnimationProgress,
                    center = Offset(bubbleX, bubbleY),
                    alpha = pointConfig.pointAlpha * bubbleAnimationProgress,
                )

                // Draw inner circle (main bubble)
                drawCircle(
                    color = bubbleColor,
                    radius = (bubbleRadius * 0.85f) * bubbleAnimationProgress,
                    center = Offset(bubbleX, bubbleY),
                    alpha = pointConfig.pointAlpha * bubbleAnimationProgress,
                )
            }
        }
    }
}
