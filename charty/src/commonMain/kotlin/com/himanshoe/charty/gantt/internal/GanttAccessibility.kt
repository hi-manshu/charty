package com.himanshoe.charty.gantt.internal

import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.gantt.data.GanttRow

/**
 * Builds the chart-level screen-reader summary: how many rows and segments are shown, and the span
 * they cover, which is what orients a reader before they traverse the rows.
 *
 * @param rows The rows currently drawn.
 * @param minValue The value at the plot's left edge.
 * @param maxValue The value at the plot's right edge.
 * @param valueFormatter Formats a value for speech.
 * @return A sentence describing the chart.
 */
internal fun generateGanttChartDescription(
    rows: List<GanttRow>,
    minValue: Float,
    maxValue: Float,
    valueFormatter: (Float) -> String,
): String {
    if (rows.isEmpty()) {
        return "Empty Gantt chart."
    }
    val segmentCount = rows.sumOf { row -> row.segments.size }
    return "Gantt chart, ${rows.size} rows, $segmentCount segments. " +
        "Spanning ${valueFormatter(minValue)} to ${valueFormatter(maxValue)}."
}

/**
 * Bundles the chart summary with one description per row, listing that row's segments in order so
 * assistive technology can walk the schedule row by row.
 *
 * @param description The chart summary, or `null` when the caller suppressed it.
 * @param rows The rows currently drawn.
 * @param valueFormatter Formats a value for speech.
 * @return The accessibility payload handed to the chart scaffold.
 */
internal fun ganttAccessibility(
    description: String?,
    rows: List<GanttRow>,
    valueFormatter: (Float) -> String,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = description,
        dataPointDescriptions =
            rows.fastMapIndexed { index, row ->
                val segments =
                    row.segments
                        .sortedBy { segment -> segment.startValue }
                        .joinToString(separator = ", ") { segment ->
                            val name = segment.label?.let { text -> "$text " }.orEmpty()
                            "$name${valueFormatter(segment.startValue)} to ${valueFormatter(segment.endValue)}"
                        }
                "Row ${index + 1} of ${rows.size}: ${row.label}, ${row.segments.size} segments: $segments"
            },
    )
