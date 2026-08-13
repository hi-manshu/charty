package com.himanshoe.charty.gantt.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.gantt.config.GanttChartConfig
import com.himanshoe.charty.gantt.data.GanttRow

/**
 * The in-segment labels of a Gantt chart, measured once per data change rather than once per frame,
 * so the draw loop only decides whether text it has already laid out fits.
 *
 * @property rows One list of measured labels per row, in row and segment order; an entry is `null`
 *   where its segment carries no label.
 */
internal class GanttSegmentLabels(
    val rows: List<List<TextLayoutResult?>>,
)

/**
 * Measures the in-segment labels, or returns `null` when the chart does not show them.
 *
 * @param rows The rows currently drawn.
 * @param config Supplies the label text style and whether labels are shown at all.
 * @param textMeasurer The measurer used to lay the labels out.
 * @return The measured labels, or `null` when [GanttChartConfig.showSegmentLabels] is off.
 */
@Composable
internal fun rememberGanttSegmentLabels(
    rows: List<GanttRow>,
    config: GanttChartConfig,
    textMeasurer: TextMeasurer,
): GanttSegmentLabels? =
    remember(rows, config, textMeasurer) {
        if (!config.showSegmentLabels) {
            null
        } else {
            GanttSegmentLabels(
                rows =
                    rows.fastMap { row ->
                        row.segments.fastMap { segment ->
                            segment.label?.let { text ->
                                textMeasurer.measure(text = text, style = config.segmentLabelStyle)
                            }
                        }
                    },
            )
        }
    }
