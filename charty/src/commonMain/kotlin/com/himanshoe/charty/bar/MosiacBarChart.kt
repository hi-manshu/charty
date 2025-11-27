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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.MosiacBarChartConfig
import com.himanshoe.charty.bar.config.MosiacBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

/**
 * Mosiac Bar Chart - 100% stacked bar chart.
 *
 * Each bar represents a category whose segments are normalized to 100% of
 * the bar height, similar to a mosaic / 100% stacked bar chart.
 *
 * @param data Lambda returning list of bar groups to display
 * @param modifier Modifier for the chart
 * @param config Configuration for mosiac chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onSegmentClick Optional callback when a segment is clicked
 */
@Composable
fun MosiacBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    config: MosiacBarChartConfig = MosiacBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onSegmentClick: ((MosiacBarSegment) -> Unit)? = null,
) {
    val groups = remember(data) { data() }
    require(groups.isNotEmpty()) { "Mosiac bar chart data cannot be empty" }
    require(groups.all { it.values.isNotEmpty() }) { "Each bar group must have at least one value" }

    val animationProgress =
        remember {
            Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
        }

    // State to track which segment is currently showing a tooltip
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    // Store segment bounds for hit testing
    val segmentBounds = remember { mutableListOf<Pair<Rect, MosiacBarSegment>>() }

    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(config.animation) {
        if (config.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier.then(
            if (onSegmentClick != null) {
                Modifier.pointerInput(groups, config, onSegmentClick) {
                    detectTapGestures { offset ->
                        val clickedSegment = segmentBounds.find { (rect, _) ->
                            rect.contains(offset)
                        }

                        clickedSegment?.let { (rect, segment) ->
                            onSegmentClick.invoke(segment)
                            tooltipState = TooltipState(
                                content = config.tooltipFormatter(segment),
                                x = rect.left,
                                y = rect.top,
                                barWidth = rect.width,
                                position = config.tooltipPosition,
                            )
                        } ?: run {
                            tooltipState = null
                        }
                    }
                }
            } else {
                Modifier
            },
        ),
        xLabels = groups.map { it.label },
        yAxisConfig =
            AxisConfig(
                minValue = 0f,
                maxValue = 100f,
                steps = 5,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        segmentBounds.clear()

        groups.fastForEachIndexed { groupIndex, group ->
            val barX = chartContext.calculateBarLeftPosition(groupIndex, groups.size, config.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(groups.size, config.barWidthFraction)

            val total = group.values.sum().takeIf { it > 0f } ?: return@fastForEachIndexed
            var currentTop = chartContext.bottom

            group.values.fastForEachIndexed { segmentIndex, value ->
                val fraction = (value / total).coerceIn(0f, 1f)
                val fullHeight = chartContext.height * fraction
                val animatedHeight = fullHeight * animationProgress.value
                val top = currentTop - animatedHeight

                // Store segment bounds for hit testing
                if (onSegmentClick != null && animatedHeight > 0) {
                    segmentBounds.add(
                        Rect(
                            left = barX,
                            top = top,
                            right = barX + barWidth,
                            bottom = currentTop,
                        ) to MosiacBarSegment(
                            barGroup = group,
                            segmentIndex = segmentIndex,
                            segmentValue = value,
                            segmentPercentage = fraction * 100f,
                        ),
                    )
                }

                // Use per-segment color from BarGroup.colors if provided; fall back to a default palette
                val chartyColor =
                    group.colors?.getOrNull(segmentIndex)
                        ?: defaultMosiacColors[segmentIndex % defaultMosiacColors.size]

                val color =
                    when (chartyColor) {
                        is ChartyColor.Solid -> chartyColor.color
                        is ChartyColor.Gradient -> chartyColor.colors.first()
                    }

                val segmentBrush =
                    Brush.verticalGradient(
                        colors = listOf(color, color),
                        startY = top,
                        endY = currentTop,
                    )

                val isTop = segmentIndex == group.values.lastIndex

                drawMosiacSegment(
                    brush = segmentBrush,
                    x = barX,
                    y = top,
                    width = barWidth,
                    height = animatedHeight,
                    cornerRadius = if (isTop) CornerRadius(0f, 0f) else CornerRadius.Zero,
                )

                currentTop -= animatedHeight
            }
        }

        // Draw tooltip
        tooltipState?.let { state ->
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

private val defaultMosiacColors =
    listOf(
        ChartyColor.Solid(Color(0xFF0B1D3B)),
        ChartyColor.Solid(Color(0xFFD64C66)),
        ChartyColor.Solid(Color(0xFFFFA64D)),
    )

private fun DrawScope.drawMosiacSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: CornerRadius,
) {
    val path =
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = cornerRadius,
                    topRightCornerRadius = cornerRadius,
                    bottomLeftCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius.Zero,
                ),
            )
        }
    drawPath(path, brush)
}
