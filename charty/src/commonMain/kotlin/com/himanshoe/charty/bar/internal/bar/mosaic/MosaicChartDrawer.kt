package com.himanshoe.charty.bar.internal.bar.mosaic

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.PERCENT_SCALE

/**
 * Draws all mosaic bars on the chart.
 */
internal fun DrawScope.drawMosaicBars(
    groups: List<BarGroup>,
    chartContext: ChartContext,
    config: MosaicBarChartConfig,
    animationProgress: Float,
    onSegmentClick: ((MosaicBarSegment) -> Unit)?,
    onSegmentBoundCalculated: (Pair<Rect, MosaicBarSegment>) -> Unit,
    recordBounds: Boolean = onSegmentClick != null,
) {
    groups.fastForEachIndexed { groupIndex, group ->
        val barX = chartContext.calculateBarLeftPosition(groupIndex, groups.size, config.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(groups.size, config.barWidthFraction)
        val total =
            group.values
                .fastFilter { it > 0f }
                .sum()
                .takeIf { it > 0f } ?: return@fastForEachIndexed

        drawMosaicBarSegments(
            group = group,
            barX = barX,
            barWidth = barWidth,
            chartHeight = chartContext.height,
            chartBottom = chartContext.bottom,
            total = total,
            animationProgress = animationProgress,
            recordBounds = recordBounds,
            onSegmentBoundCalculated = onSegmentBoundCalculated,
        )
    }
}

/**
 * Draws segments for a single mosaic bar.
 */
private fun DrawScope.drawMosaicBarSegments(
    group: BarGroup,
    barX: Float,
    barWidth: Float,
    chartHeight: Float,
    chartBottom: Float,
    total: Float,
    animationProgress: Float,
    recordBounds: Boolean,
    onSegmentBoundCalculated: (Pair<Rect, MosaicBarSegment>) -> Unit,
) {
    var currentTop = chartBottom

    group.values.fastForEachIndexed { segmentIndex, value ->
        if (value <= 0f) {
            return@fastForEachIndexed
        }
        val fraction = (value / total).coerceIn(0f, 1f)
        val fullHeight = chartHeight * fraction
        val animatedHeight = fullHeight * animationProgress
        val top = currentTop - animatedHeight

        if (recordBounds && animatedHeight > 0) {
            onSegmentBoundCalculated(
                Rect(
                    left = barX,
                    top = top,
                    right = barX + barWidth,
                    bottom = currentTop,
                ) to
                    MosaicBarSegment(
                        barGroup = group,
                        segmentIndex = segmentIndex,
                        segmentValue = value,
                        segmentPercentage = fraction * PERCENT_SCALE,
                    ),
            )
        }

        val chartyColor =
            group.colors?.getOrNull(segmentIndex)
                ?: defaultMosaicColors[segmentIndex % defaultMosaicColors.size]

        val segmentBrush =
            Brush.verticalGradient(
                colors = chartyColor.value,
                startY = top,
                endY = currentTop,
            )

        drawMosaicSegment(
            brush = segmentBrush,
            x = barX,
            y = top,
            width = barWidth,
            height = animatedHeight,
        )

        currentTop -= animatedHeight
    }
}

/**
 * Draws a single mosaic segment with rounded corners at the top.
 */
private fun DrawScope.drawMosaicSegment(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
    /*
     * Still drawn as a path with an all-zero RoundRect rather than as a plain rect.
     *
     * The two are the same rectangle but not the same pixels: swapping in drawRect moved the mosaic's
     * documentation image, because a path and a rect do not anti-alias their edges identically. The
     * corner radius that used to be a parameter is gone — both of its branches produced zero — but the
     * draw call it fed stays exactly as it was.
     */
    val path =
        Path().apply {
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
    drawPath(path, brush)
}
