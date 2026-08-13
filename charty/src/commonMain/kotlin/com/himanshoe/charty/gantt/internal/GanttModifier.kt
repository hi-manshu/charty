package com.himanshoe.charty.gantt.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.gantt.config.GanttChartConfig
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSelection

/**
 * Builds the tap — and, when enabled, drag-to-scrub — modifier for a Gantt chart. Each segment is
 * its own hit target, so a tap reports the exact segment rather than only its row.
 *
 * @param modifier The modifier the handlers are chained onto.
 * @param onSegmentClick Called with the tapped segment, or `null` when taps are ignored.
 * @param rows The rows currently drawn, used as a gesture recomposition key.
 * @param config Supplies the tooltip's value formatting and preferred placement.
 * @param segmentBounds The bounds recorded during the last draw, paired with what they represent.
 * @param onTooltipUpdate Receives the new tooltip state and its selection.
 * @param enableScrub Whether dragging across the chart tracks the segment under the finger.
 * @return The modifier with the gesture handlers attached.
 */
@Suppress("LongParameterList")
internal fun createGanttChartModifier(
    modifier: Modifier,
    onSegmentClick: ((GanttSelection) -> Unit)?,
    rows: List<GanttRow>,
    config: GanttChartConfig,
    segmentBounds: List<Pair<Rect, GanttSelection>>,
    onTooltipUpdate: (TooltipState?, GanttSelection?) -> Unit,
    enableScrub: Boolean,
): Modifier {
    val tooltipContentBuilder = { selection: GanttSelection, rect: Rect ->
        val name = selection.segment.label ?: selection.row.label
        TooltipState(
            content =
                "$name: ${config.valueFormatter(selection.segment.startValue)} - " +
                    config.valueFormatter(selection.segment.endValue),
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = config.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = rows,
            bounds = segmentBounds,
            onItemClick = onSegmentClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = rows,
            bounds = segmentBounds,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
