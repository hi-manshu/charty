package com.himanshoe.charty.point

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData

/**
 * A composable function that displays a bubble chart.
 *
 * A bubble chart is a variation of a scatter plot where each point is represented by a circle (bubble),
 * and the size of the bubble represents a third dimension of data. This is useful for visualizing three variables at once:
 * x-position, y-position, and size.
 *
 * @param data A lambda function that returns a list of [BubbleData] to be displayed.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bubbles, defined by a [ChartyColor].
 * @param config The configuration for the bubbles' appearance, where `pointRadius` is the maximum bubble size, defined by a [PointChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param minBubbleRadius The minimum radius for a bubble in pixels.
 * @param onBubbleClick A lambda function to be invoked when a bubble is clicked, providing the corresponding [BubbleData].
 *
 * @sample
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
 */
@Composable
fun BubbleChart(
    data: () -> List<BubbleData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    config: PointChartConfig = PointChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    minBubbleRadius: Float = 10f,
    onBubbleClick: ((BubbleData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bubble chart data cannot be empty" }
    require(minBubbleRadius > 0f) { "Minimum bubble radius must be positive" }
    require(config.pointRadius > minBubbleRadius) { "Max radius must be greater than min radius" }

    val bubbleBounds = remember { mutableListOf<BubbleBounds>() }
    val sizeInfo = remember(dataList) { calculateBubbleSizeInfo(dataList) }

    val isBelowAxisMode = config.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(config.animation)

    ChartScaffold(
        modifier = modifier.then(createBubbleClickModifier(dataList, bubbleBounds, onBubbleClick)),
        xLabels = dataList.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = sizeInfo.minValue,
            maxValue = sizeInfo.maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        bubbleBounds.clear()

        dataList.fastForEachIndexed { index, bubble ->
            val bubbleProgress = index.toFloat() / dataList.size
            val bubbleAnimationProgress = ((animationProgress.value - bubbleProgress) * dataList.size).coerceIn(0f, 1f)

            val bubbleX = chartContext.calculateCenteredXPosition(index, dataList.size)
            val bubbleY = chartContext.convertValueToYPosition(bubble.yValue)

            val bubbleRadius = calculateBubbleRadius(
                bubble.size,
                sizeInfo.minSize,
                sizeInfo.sizeRange,
                minBubbleRadius,
                config.pointRadius,
            )

            val bubbleColor = when (color) {
                is ChartyColor.Solid -> color.color
                is ChartyColor.Gradient -> color.colors[index % color.colors.size]
            }

            if (bubbleAnimationProgress > 0f) {
                val center = Offset(bubbleX, bubbleY)
                val animatedRadius = bubbleRadius * bubbleAnimationProgress

                if (onBubbleClick != null) {
                    bubbleBounds.add(BubbleBounds(center, animatedRadius, bubble))
                }

                drawCircle(
                    color = bubbleColor.copy(alpha = 0.3f),
                    radius = animatedRadius,
                    center = center,
                    alpha = config.pointAlpha * bubbleAnimationProgress,
                )
                drawCircle(
                    color = bubbleColor,
                    radius = (bubbleRadius * 0.85f) * bubbleAnimationProgress,
                    center = center,
                    alpha = config.pointAlpha * bubbleAnimationProgress,
                )
            }
        }
    }
}
