package com.himanshoe.charty.gantt.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * One bar of a Gantt row: a half-open interval `[startValue, endValue)` on the chart's continuous
 * value axis, optionally coloured and labelled in its own right.
 *
 * The axis is generic: the values are plain floats, so they can be hours into a shift, metres along
 * a pipeline, or instants on a timeline. Nothing here is time-aware — see
 * [com.himanshoe.charty.gantt.GanttChart] for how to pair the chart with the datetime axis helpers.
 *
 * @property startValue Where the segment begins on the value axis.
 * @property endValue Where the segment ends. Must be strictly greater than [startValue]; a
 *   zero-length segment has nothing to draw and is rejected rather than silently vanishing.
 * @property color Optional colour for this segment; falls back to the chart's colour.
 * @property label Optional text drawn inside the segment when it is wide enough to fit, and used in
 *   the segment's tooltip.
 * @property progress Optional completion fraction from `0f` to `1f`. When set, the segment is drawn
 *   dimmed with its completed head painted solid, the usual "80% done" bar. `null` (default) draws a
 *   plain segment.
 */
@Immutable
data class GanttSegment(
    val startValue: Float,
    val endValue: Float,
    val color: ChartyColor? = null,
    val label: String? = null,
    val progress: Float? = null,
) {
    init {
        require(endValue > startValue) { "endValue must be greater than startValue" }
        require(progress == null || progress in 0f..1f) { "progress must be between 0 and 1" }
    }
}

/**
 * One category row of a Gantt chart: a label and the segments laid out along it.
 *
 * A row holds **many** segments, which is what separates this chart from
 * [com.himanshoe.charty.bar.SpanChart] — that draws exactly one range per row.
 *
 * Segments within a row must not overlap. Overlapping bars on one row cannot be told apart once
 * drawn (the later one simply covers the earlier), so an overlap is rejected at construction rather
 * than rendered ambiguously; give the conflicting work its own row instead. Touching segments —
 * one ending exactly where the next begins — are allowed, and segments may be given in any order.
 *
 * @property label The row label, drawn in the left gutter.
 * @property segments The row's segments, in any order, none of which may overlap another.
 */
@Immutable
data class GanttRow(
    val label: String,
    val segments: List<GanttSegment>,
) {
    init {
        val ordered = segments.sortedBy { segment -> segment.startValue }
        ordered.forEachIndexed { index, segment ->
            val next = ordered.getOrNull(index + 1)
            require(next == null || next.startValue >= segment.endValue) {
                "Segments of row '$label' must not overlap"
            }
        }
    }
}

/**
 * The segment a reader tapped, together with the row it belongs to.
 *
 * @property row The row that was tapped.
 * @property segment The segment within that row.
 */
@Immutable
data class GanttSelection(
    val row: GanttRow,
    val segment: GanttSegment,
)
