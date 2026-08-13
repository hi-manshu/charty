package com.himanshoe.charty.calendar.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Bundles everything the calendar heatmap's draw phase needs, so no layout or measurement work
 * happens on an animation frame.
 *
 * @property config Chart appearance and behaviour configuration.
 * @property gridLayout The computed week/day grid for the current data.
 * @property measuredMonthLabels Pre-measured month labels keyed by label string.
 * @property measuredDayLabelRows Pre-measured day labels as (dayIndex, layout) pairs.
 * @property leftPadding Horizontal offset (px) reserved for day-of-week labels.
 * @property topPadding Vertical offset (px) reserved for month labels.
 * @property cellSizePx Cell width/height in pixels.
 * @property cellStridePx Cell size plus spacing (distance between adjacent cell origins).
 * @property maxValue Maximum value in the dataset; used to normalise colour intensity.
 * @property animationProgress Reveal progress in `0f..1f`.
 * @property cellBounds Sink refilled with hit-test rectangles for the visible data cells.
 * @property tooltipState The tooltip to render, or `null` when none is shown.
 * @property textMeasurer Measurer used for the tooltip text.
 * @property tooltipConfig Styling for the tooltip bubble, already resolved against the theme.
 */
internal data class CalendarDrawParams(
    val config: CalendarHeatmapConfig,
    val gridLayout: GridLayout,
    val measuredMonthLabels: Map<String, TextLayoutResult>,
    val measuredDayLabelRows: List<Pair<Int, TextLayoutResult>>,
    val leftPadding: Float,
    val topPadding: Float,
    val cellSizePx: Float,
    val cellStridePx: Float,
    val maxValue: Float,
    val animationProgress: Float,
    val cellBounds: MutableList<Pair<Rect, CalendarData>>,
    val tooltipState: TooltipState?,
    val textMeasurer: TextMeasurer,
    val tooltipConfig: TooltipConfig,
)
