package com.himanshoe.charty.gantt.internal

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.gantt.config.GanttChartConfig
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSelection

/**
 * Everything [drawGanttRows] needs for one frame.
 *
 * @property rows The rows currently drawn, top to bottom.
 * @property chartContext The chart's coordinate context.
 * @property config The chart's appearance configuration.
 * @property color The default segment colour, unless a segment overrides it.
 * @property minValue The value at the plot's left edge.
 * @property maxValue The value at the plot's right edge.
 * @property animationProgress The entry animation's progress from `0f` to `1f`.
 * @property labels Pre-measured segment labels, or `null` when labels are off.
 * @property onSegmentBoundCalculated Receives each drawn segment's bounds paired with the selection
 *   it represents, for hit testing.
 * @property recordBounds Whether bounds are worth recording at all, i.e. whether anything consumes them.
 */
internal data class GanttDrawParams(
    val rows: List<GanttRow>,
    val chartContext: ChartContext,
    val config: GanttChartConfig,
    val color: ChartyColor,
    val minValue: Float,
    val maxValue: Float,
    val animationProgress: Float,
    val labels: GanttSegmentLabels?,
    val onSegmentBoundCalculated: (Pair<Rect, GanttSelection>) -> Unit,
    val recordBounds: Boolean,
)
