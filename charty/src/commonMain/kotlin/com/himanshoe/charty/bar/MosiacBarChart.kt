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

private const val MAX_PERCENTAGE = 100f
private const val MIN_PERCENTAGE = 0f
private const val DEFAULT_AXIS_STEPS = 5

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

    val animationProgress = rememberMosiacAnimation(config.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val segmentBounds = remember { mutableListOf<Pair<Rect, MosiacBarSegment>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createMosiacChartModifier(
        modifier = modifier,
        onSegmentClick = onSegmentClick,
        groups = groups,
        config = config,
        segmentBounds = segmentBounds,
        onTooltipUpdate = { tooltipState = it }
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = groups.map { it.label },
        yAxisConfig = createMosiacAxisConfig(),
        config = scaffoldConfig,
    ) { chartContext ->
        segmentBounds.clear()

        drawMosiacBars(
            groups = groups,
            chartContext = chartContext,
            config = config,
            animationProgress = animationProgress.value,
            onSegmentClick = onSegmentClick,
            onSegmentBoundCalculated = { segmentBounds.add(it) },
        )

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

@Composable
private fun rememberMosiacAnimation(animation: Animation): Animatable<Float, *> {
    val animationProgress = remember {
        Animatable(if (animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(animation) {
        if (animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animation.duration),
            )
        }
    }

    return animationProgress
}

@Composable
private fun createMosiacChartModifier(
    onSegmentClick: ((MosiacBarSegment) -> Unit)?,
    groups: List<BarGroup>,
    config: MosiacBarChartConfig,
    segmentBounds: List<Pair<Rect, MosiacBarSegment>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onSegmentClick != null) {
        modifier.pointerInput(groups, config, onSegmentClick) {
            detectTapGestures { offset ->
                handleMosiacSegmentClick(offset, segmentBounds, onSegmentClick, config, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

private fun handleMosiacSegmentClick(
    offset: androidx.compose.ui.geometry.Offset,
    segmentBounds: List<Pair<Rect, MosiacBarSegment>>,
    onSegmentClick: (MosiacBarSegment) -> Unit,
    config: MosiacBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit
) {
    val clickedSegment = segmentBounds.find { (rect, _) -> rect.contains(offset) }

    clickedSegment?.let { (rect, segment) ->
        onSegmentClick.invoke(segment)
        onTooltipUpdate(
            TooltipState(
                content = config.tooltipFormatter(segment),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = config.tooltipPosition,
            )
        )
    } ?: onTooltipUpdate(null)
}

private fun createMosiacAxisConfig(): AxisConfig {
    return AxisConfig(
        minValue = MIN_PERCENTAGE,
        maxValue = MAX_PERCENTAGE,
        steps = DEFAULT_AXIS_STEPS,
    )
}

private fun DrawScope.drawMosiacBars(
    groups: List<BarGroup>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    config: MosiacBarChartConfig,
    animationProgress: Float,
    onSegmentClick: ((MosiacBarSegment) -> Unit)?,
    onSegmentBoundCalculated: (Pair<Rect, MosiacBarSegment>) -> Unit,
) {
    groups.fastForEachIndexed { groupIndex, group ->
        val barX = chartContext.calculateBarLeftPosition(groupIndex, groups.size, config.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(groups.size, config.barWidthFraction)
        val total = group.values.sum().takeIf { it > 0f } ?: return@fastForEachIndexed

        drawMosiacBarSegments(
            group = group,
            barX = barX,
            barWidth = barWidth,
            chartHeight = chartContext.height,
            chartBottom = chartContext.bottom,
            total = total,
            animationProgress = animationProgress,
            onSegmentClick = onSegmentClick,
            onSegmentBoundCalculated = onSegmentBoundCalculated,
        )
    }
}

private fun DrawScope.drawMosiacBarSegments(
    group: BarGroup,
    barX: Float,
    barWidth: Float,
    chartHeight: Float,
    chartBottom: Float,
    total: Float,
    animationProgress: Float,
    onSegmentClick: ((MosiacBarSegment) -> Unit)?,
    onSegmentBoundCalculated: (Pair<Rect, MosiacBarSegment>) -> Unit,
) {
    var currentTop = chartBottom

    group.values.fastForEachIndexed { segmentIndex, value ->
        val fraction = (value / total).coerceIn(MIN_PERCENTAGE, 1f)
        val fullHeight = chartHeight * fraction
        val animatedHeight = fullHeight * animationProgress
        val top = currentTop - animatedHeight

        if (onSegmentClick != null && animatedHeight > 0) {
            onSegmentBoundCalculated(
                Rect(
                    left = barX,
                    top = top,
                    right = barX + barWidth,
                    bottom = currentTop,
                ) to MosiacBarSegment(
                    barGroup = group,
                    segmentIndex = segmentIndex,
                    segmentValue = value,
                    segmentPercentage = fraction * MAX_PERCENTAGE,
                )
            )
        }

        val chartyColor = group.colors?.getOrNull(segmentIndex)
            ?: defaultMosiacColors[segmentIndex % defaultMosiacColors.size]

        val segmentBrush = Brush.verticalGradient(
            colors = chartyColor.value,
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
