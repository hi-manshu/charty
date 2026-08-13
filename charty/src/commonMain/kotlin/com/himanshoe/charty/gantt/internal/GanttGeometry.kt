package com.himanshoe.charty.gantt.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSegment

private const val HALF = 2f
private const val EMPTY_RANGE_SPAN = 1f

/**
 * The value range the rows span: the earliest start to the latest end, so the plot's left edge is
 * the first segment's start and its right edge the last segment's end.
 *
 * A set of rows with no segments at all, or one whose segments all collapse onto a single value,
 * has no width to divide by; those cases widen to a unit range instead.
 *
 * @param rows The rows currently drawn.
 * @return The `(min, max)` axis range.
 */
internal fun ganttValueRange(rows: List<GanttRow>): Pair<Float, Float> {
    var minValue = Float.MAX_VALUE
    var maxValue = -Float.MAX_VALUE
    rows.fastForEach { row ->
        row.segments.fastForEach { segment ->
            if (segment.startValue < minValue) {
                minValue = segment.startValue
            }
            if (segment.endValue > maxValue) {
                maxValue = segment.endValue
            }
        }
    }
    return if (minValue > maxValue) {
        0f to EMPTY_RANGE_SPAN
    } else {
        minValue to maxValue
    }
}

/**
 * Maps a value onto its x-coordinate in the plot. A degenerate range collapses every value onto the
 * left edge rather than dividing by zero.
 *
 * @param value The value to place.
 * @param chartContext The chart's coordinate context.
 * @param minValue The value at the plot's left edge.
 * @param maxValue The value at the plot's right edge.
 * @return The x-coordinate in pixels.
 */
internal fun ganttValueToX(
    value: Float,
    chartContext: ChartContext,
    minValue: Float,
    maxValue: Float,
): Float {
    val range = maxValue - minValue
    return if (range <= 0f) {
        chartContext.left
    } else {
        chartContext.left + (value - minValue) / range * chartContext.width
    }
}

/**
 * The pixel rectangle of one segment, growing from its start value towards its end as the entry
 * animation runs.
 *
 * @param chartContext The chart's coordinate context.
 * @param index The row's index among the drawn rows.
 * @param rowCount The number of drawn rows.
 * @param segment The segment to place.
 * @param minValue The value at the plot's left edge.
 * @param maxValue The value at the plot's right edge.
 * @param barHeightFraction Fraction of the row's height the segment occupies.
 * @param animationProgress The entry animation's progress from `0f` to `1f`.
 * @return The segment's bounds.
 */
@Suppress("LongParameterList")
internal fun ganttSegmentRect(
    chartContext: ChartContext,
    index: Int,
    rowCount: Int,
    segment: GanttSegment,
    minValue: Float,
    maxValue: Float,
    barHeightFraction: Float,
    animationProgress: Float,
): Rect {
    val slotHeight = chartContext.calculateSlotHeight(totalItems = rowCount)
    val slotTop = chartContext.calculateSlotTopPosition(index = index, totalItems = rowCount)
    val barHeight = slotHeight * barHeightFraction
    val top = slotTop + (slotHeight - barHeight) / HALF
    val startX =
        ganttValueToX(value = segment.startValue, chartContext = chartContext, minValue = minValue, maxValue = maxValue)
    val endX =
        ganttValueToX(value = segment.endValue, chartContext = chartContext, minValue = minValue, maxValue = maxValue)
    return Rect(
        left = startX,
        top = top,
        right = startX + (endX - startX) * animationProgress,
        bottom = top + barHeight,
    )
}

/**
 * The completed head of a segment: the part of [rect] its progress fraction covers, or `null` when
 * the segment carries no progress and is drawn solid.
 *
 * @param rect The segment's bounds.
 * @param progress The segment's completion fraction, or `null`.
 * @return The completed portion's bounds, or `null`.
 */
internal fun ganttProgressRect(
    rect: Rect,
    progress: Float?,
): Rect? =
    progress?.let { fraction ->
        Rect(
            left = rect.left,
            top = rect.top,
            right = rect.left + rect.width * fraction,
            bottom = rect.bottom,
        )
    }

/**
 * Builds the value axis for a Gantt chart: a plain continuous axis with no zero line, since the
 * values are positions along a scale rather than magnitudes measured from zero.
 *
 * @param minValue The value at the plot's left edge.
 * @param maxValue The value at the plot's right edge.
 * @param steps The number of divisions on the axis.
 * @param valueFormatter Formats a tick value for display.
 * @return The axis configuration for the scaffold.
 */
internal fun ganttAxisConfig(
    minValue: Float,
    maxValue: Float,
    steps: Int,
    valueFormatter: (Float) -> String,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = steps,
        drawAxisAtZero = false,
        valueFormatter = valueFormatter,
    )
