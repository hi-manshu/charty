package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.ChartScaffoldConfig

private const val CENTER_DIVISOR = 2f

/**
 * Anchor points for the persistent markers of a span chart: the centre of each span's end (its
 * higher-value edge), in the same order as the drawn data, so a marker index of `-1` lands on the
 * bottom span. Mirrors the geometry [drawSpans] uses.
 *
 * @param chartContext The chart's coordinate context.
 * @param endValues The end value of each drawn span, ordered top to bottom.
 * @param minValue The value at the left edge of the plot.
 * @param maxValue The value at the right edge of the plot.
 * @param axisOffset The left inset reserved for the axis.
 * @return One [Offset] per span, at the centre of its end edge.
 */
internal fun spanEndMarkerPositions(
    chartContext: ChartContext,
    endValues: List<Float>,
    minValue: Float,
    maxValue: Float,
    axisOffset: Float,
): List<Offset> {
    val range = maxValue - minValue
    val rowHeight = chartContext.height / endValues.size
    return List(endValues.size) { index ->
        val normalized =
            if (range == 0f) {
                0f
            } else {
                (endValues[index] - minValue) / range
            }
        Offset(
            x = chartContext.left + axisOffset + normalized * (chartContext.width - axisOffset),
            y = chartContext.top + rowHeight * index + rowHeight / CENTER_DIVISOR,
        )
    }
}

internal fun calculateAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float =
    if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
