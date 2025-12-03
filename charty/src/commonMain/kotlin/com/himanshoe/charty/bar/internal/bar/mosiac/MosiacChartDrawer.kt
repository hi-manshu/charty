package com.himanshoe.charty.bar.internal.bar.mosiac

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.MosiacBarChartConfig
import com.himanshoe.charty.bar.config.MosiacBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

private const val MAX_PERCENTAGE = 100f
private const val MIN_PERCENTAGE = 0f

/**
 * Draws all mosiac bars on the chart.
 */
internal fun DrawScope.drawMosiacBars(
    groups: List<BarGroup>,
    chartContext: ChartContext,
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

/**
 * Draws segments for a single mosiac bar.
 */
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
                ),
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

/**
 * Draws a single mosiac segment with rounded corners at the top.
 */
private fun DrawScope.drawMosiacSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: CornerRadius,
) {
    val path = Path().apply {
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

