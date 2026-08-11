package com.himanshoe.charty.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.WeekStartDay
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.calendar.internal.GridLayout
import com.himanshoe.charty.calendar.internal.computeGridLayout
import com.himanshoe.charty.calendar.internal.drawCalendarGrid
import com.himanshoe.charty.calendar.internal.drawDayLabels
import com.himanshoe.charty.calendar.internal.drawMonthLabels
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private data class CalendarDrawParams(
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
)

private const val DAY_LABEL_RIGHT_GAP = 8f
private const val MONTH_LABEL_BOTTOM_GAP = 4f
private const val DAYS_PER_WEEK = 7

private const val DOW_MON_SUNDAY_START = 1
private const val DOW_WED_SUNDAY_START = 3
private const val DOW_FRI_SUNDAY_START = 5
private const val DOW_MON_MONDAY_START = 0
private const val DOW_WED_MONDAY_START = 2
private const val DOW_FRI_MONDAY_START = 4

/**
 * A GitHub-style contribution calendar heatmap chart.
 *
 * Renders a grid of day cells, one column per week. Cell colour intensity reflects
 * [CalendarData.value] relative to the dataset maximum. The chart supports a left-to-right
 * entrance animation, configurable cell shapes, adaptive font-size-aware label padding, and
 * tap-to-show tooltips. Horizontal scrolling is opt-out via [scrollEnabled].
 *
 * @param data A lambda that returns the list of [CalendarData] to display. Days with no entry
 *   or a value ≤ 0 are shown as empty cells.
 * @param modifier Modifier applied to the outermost container.
 * @param config Chart appearance and behaviour; see [CalendarHeatmapConfig].
 * @param visibleWeeks If not `null`, only the **last** [visibleWeeks] columns of the full date
 *   range are rendered. Useful for a rolling window (e.g., 26 weeks). `null` shows all data.
 * @param scrollEnabled When `true` (default) the chart scrolls horizontally so all columns are
 *   reachable. Set to `false` to clip the chart to the available width (useful when used inside
 *   another scroll container or when [visibleWeeks] already limits the width).
 * @param onDayClick Optional callback invoked when the user taps a cell that has data.
 *
 * Example usage:
 * ```kotlin
 * CalendarHeatmapChart(
 *     data = {
 *         listOf(
 *             CalendarData(2024, 1, 1, 3f),
 *             CalendarData(2024, 6, 15, 7f),
 *         )
 *     },
 *     config = CalendarHeatmapConfig(cellShape = CellShape.Circle),
 *     visibleWeeks = 26,
 *     scrollEnabled = false,
 *     onDayClick = { data -> println("${data.day}/${data.month}: ${data.value}") },
 * )
 * ```
 */
@Composable
fun CalendarHeatmapChart(
    data: () -> List<CalendarData>,
    modifier: Modifier = Modifier,
    config: CalendarHeatmapConfig = CalendarHeatmapConfig(),
    visibleWeeks: Int? = null,
    scrollEnabled: Boolean = true,
    onDayClick: ((CalendarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    val gridLayout =
        remember(dataList, config.weekStartDay, visibleWeeks) {
            computeGridLayout(dataList, config.weekStartDay, visibleWeeks)
        }
    val maxValue = remember(dataList) { dataList.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f }
    val animationProgress = rememberChartAnimation(config.animation)

    val currentConfig by rememberUpdatedState(config)
    val currentOnDayClick by rememberUpdatedState(onDayClick)

    val measuredMonthLabels = rememberMeasuredMonthLabels(config, gridLayout, textMeasurer)
    val measuredDayLabelRows = rememberMeasuredDayLabelRows(config, textMeasurer)

    val cellSizePx = remember(config.cellSize, density) { with(density) { config.cellSize.toPx() } }
    val cellStridePx =
        remember(config.cellSize, config.cellSpacing, density) {
            cellSizePx + with(density) { config.cellSpacing.toPx() }
        }

    val leftPadding =
        measuredDayLabelRows
            .maxOfOrNull { (_, r) -> r.size.width }
            ?.let { it + DAY_LABEL_RIGHT_GAP } ?: 0f
    val topPadding =
        measuredMonthLabels.values
            .firstOrNull()
            ?.size
            ?.height
            ?.let { it + MONTH_LABEL_BOTTOM_GAP } ?: 0f

    val canvasWidth = leftPadding + gridLayout.totalWeeks * cellStridePx
    val canvasHeight = topPadding + DAYS_PER_WEEK * cellStridePx

    val cellBounds = remember { mutableListOf<Pair<Rect, CalendarData>>() }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    val outerModifier = if (scrollEnabled) modifier.horizontalScroll(scrollState) else modifier

    Box(modifier = outerModifier) {
        if (gridLayout.totalWeeks == 0) return@Box

        Canvas(
            modifier =
                Modifier
                    .size(
                        width = with(density) { canvasWidth.toDp() },
                        height = with(density) { canvasHeight.toDp() },
                    ).pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val hit = cellBounds.firstOrNull { (rect, _) -> rect.contains(offset) }
                            if (hit != null) {
                                currentOnDayClick?.invoke(hit.second)
                                tooltipState =
                                    TooltipState(
                                        content = currentConfig.tooltipFormatter(hit.second),
                                        x = hit.first.left,
                                        y = hit.first.top,
                                        barWidth = hit.first.width,
                                        position = TooltipPosition.ABOVE,
                                    )
                            } else {
                                tooltipState = null
                            }
                        }
                    },
        ) {
            drawCalendarContent(
                CalendarDrawParams(
                    config = config,
                    gridLayout = gridLayout,
                    measuredMonthLabels = measuredMonthLabels,
                    measuredDayLabelRows = measuredDayLabelRows,
                    leftPadding = leftPadding,
                    topPadding = topPadding,
                    cellSizePx = cellSizePx,
                    cellStridePx = cellStridePx,
                    maxValue = maxValue,
                    animationProgress = animationProgress.value,
                    cellBounds = cellBounds,
                    tooltipState = tooltipState,
                    textMeasurer = textMeasurer,
                ),
            )
        }
    }
}

@Composable
private fun rememberMeasuredMonthLabels(
    config: CalendarHeatmapConfig,
    gridLayout: GridLayout,
    textMeasurer: TextMeasurer,
): Map<String, TextLayoutResult> {
    return remember(config.showMonthLabels, config.labelTextStyle, gridLayout.monthBoundaries) {
        if (!config.showMonthLabels) return@remember emptyMap()
        gridLayout.monthBoundaries.associate { (_, label) ->
            label to textMeasurer.measure(label, config.labelTextStyle)
        }
    }
}

@Composable
private fun rememberMeasuredDayLabelRows(
    config: CalendarHeatmapConfig,
    textMeasurer: TextMeasurer,
): List<Pair<Int, TextLayoutResult>> {
    return remember(config.showDayLabels, config.weekStartDay, config.labelTextStyle) {
        if (!config.showDayLabels) return@remember emptyList()
        val rows =
            if (config.weekStartDay == WeekStartDay.SUNDAY) {
                listOf(DOW_MON_SUNDAY_START to "Mon", DOW_WED_SUNDAY_START to "Wed", DOW_FRI_SUNDAY_START to "Fri")
            } else {
                listOf(DOW_MON_MONDAY_START to "Mon", DOW_WED_MONDAY_START to "Wed", DOW_FRI_MONDAY_START to "Fri")
            }
        rows.map { (dayIndex, label) -> dayIndex to textMeasurer.measure(label, config.labelTextStyle) }
    }
}

private fun DrawScope.drawCalendarContent(params: CalendarDrawParams) {
    if (params.config.showMonthLabels) {
        drawMonthLabels(
            monthBoundaries = params.gridLayout.monthBoundaries,
            measuredLabels = params.measuredMonthLabels,
            leftPadding = params.leftPadding,
            topPadding = params.topPadding,
            cellStridePx = params.cellStridePx,
        )
    }
    if (params.config.showDayLabels) {
        drawDayLabels(
            dayLabelRows = params.measuredDayLabelRows,
            topPadding = params.topPadding,
            leftPadding = params.leftPadding,
            cellStridePx = params.cellStridePx,
        )
    }
    drawCalendarGrid(
        gridLayout = params.gridLayout,
        config = params.config,
        maxValue = params.maxValue,
        leftPadding = params.leftPadding,
        topPadding = params.topPadding,
        cellSizePx = params.cellSizePx,
        cellStridePx = params.cellStridePx,
        animationProgress = params.animationProgress,
        cellBoundsOutput = params.cellBounds,
    )
    params.tooltipState?.let { ts ->
        drawTooltip(
            tooltipState = ts,
            config = params.config.tooltipConfig,
            textMeasurer = params.textMeasurer,
            chartWidth = size.width,
            chartTop = params.topPadding,
            chartBottom = size.height,
        )
    }
}
