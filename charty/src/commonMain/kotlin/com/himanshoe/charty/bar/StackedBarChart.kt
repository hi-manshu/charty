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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.draw.drawReferenceLine

/**
 * Stacked Bar Chart - Display data as stacked vertical bars showing composition
 *
 * A stacked bar chart shows multiple values stacked on top of each other,
 * displaying both individual values and the total. Useful for showing part-to-whole
 * relationships and composition over categories.
 *
 * Usage:
 * ```kotlin
 * StackedBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(20f, 30f, 15f)),
 *             BarGroup("Q2", listOf(25f, 35f, 20f)),
 *             BarGroup("Q3", listOf(30f, 25f, 25f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
 *     ),
 *     stackedConfig = StackedBarChartConfig(
 *         barWidthFraction = 0.7f,
 *         topCornerRadius = CornerRadius.Medium
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups (each group represents one stacked bar)
 * @param modifier Modifier for the chart
 * @param colors Color configuration - Gradient assigns different color to each stack segment
 * @param stackedConfig Configuration for stacked bar appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun StackedBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(0xFF2196F3),
                Color(0xFF4CAF50),
                Color(0xFFFF9800),
            ),
        ),
    stackedConfig: StackedBarChartConfig = StackedBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Stacked bar chart data cannot be empty" }
    require(dataList.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val (maxTotal, colorList) =
        remember(dataList, colors) {
            val totals = dataList.map { group -> group.values.sum() }
            (totals.maxOrNull() ?: 0f) to colors.value
        }

    val animationProgress =
        remember {
            Animatable(if (stackedConfig.animation is Animation.Enabled) 0f else 1f)
        }

    LaunchedEffect(stackedConfig.animation) {
        if (stackedConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = stackedConfig.animation.duration),
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig =
            AxisConfig(
                minValue = 0f,
                maxValue = maxTotal,
                steps = 6,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        dataList.fastForEachIndexed { groupIndex, barGroup ->
            val barX = chartContext.calculateBarLeftPosition(groupIndex, dataList.size, stackedConfig.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(dataList.size, stackedConfig.barWidthFraction)

            var cumulativeValue = 0f

            barGroup.values.fastForEachIndexed { segmentIndex, value ->
                val segmentBottomValue = cumulativeValue
                val segmentTopValue = cumulativeValue + value
                cumulativeValue = segmentTopValue

                val segmentBottomY = chartContext.convertValueToYPosition(segmentBottomValue)
                val segmentTopY = chartContext.convertValueToYPosition(segmentTopValue)

                val fullSegmentHeight = segmentBottomY - segmentTopY
                val animatedHeight = fullSegmentHeight * animationProgress.value
                val animatedTopY = segmentBottomY - animatedHeight

                // Use per-group color if available, otherwise fall back to chart colors
                val segmentChartyColor =
                    if (barGroup.colors != null && segmentIndex < barGroup.colors.size) {
                        barGroup.colors[segmentIndex]
                    } else {
                        // Create ChartyColor from the color at this index
                        ChartyColor.Solid(colorList[segmentIndex % colorList.size])
                    }
                val isTopSegment = segmentIndex == barGroup.values.size - 1

                // Create gradient brush for this segment
                val segmentBrush =
                    when (segmentChartyColor) {
                        is ChartyColor.Solid ->
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(segmentChartyColor.color, segmentChartyColor.color),
                                startY = animatedTopY,
                                endY = animatedTopY + animatedHeight,
                            )
                        is ChartyColor.Gradient ->
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = segmentChartyColor.colors,
                                startY = animatedTopY,
                                endY = animatedTopY + animatedHeight,
                            )
                    }

                drawStackedSegment(
                    brush = segmentBrush,
                    x = barX,
                    y = animatedTopY,
                    width = barWidth,
                    height = animatedHeight,
                    cornerRadius = if (isTopSegment) stackedConfig.topCornerRadius.value else 0f,
                    isTopSegment = isTopSegment,
                )
            }
        }

        // Draw reference / target line if configured
        stackedConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }
    }
}

/**
 * Helper function to draw a segment of a stacked bar with gradient support
 * Only the top segment gets rounded corners
 */
private fun DrawScope.drawStackedSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    isTopSegment: Boolean,
) {
    val path =
        Path().apply {
            if (isTopSegment && cornerRadius > 0f) {
                // Top segment with rounded corners
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            } else {
                // Regular rectangle for middle/bottom segments
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            }
        }
    drawPath(path, brush)
}
