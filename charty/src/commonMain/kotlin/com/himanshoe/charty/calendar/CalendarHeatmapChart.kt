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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.WeekStartDay
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.calendar.internal.computeGridLayout
import com.himanshoe.charty.calendar.internal.drawCalendarGrid
import com.himanshoe.charty.calendar.internal.drawDayLabels
import com.himanshoe.charty.calendar.internal.drawMonthLabels
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

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
@OptIn(ExperimentalTextApi::class)
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

    val gridLayout = remember(dataList, config.weekStartDay, visibleWeeks) {
        computeGridLayout(dataList, config.weekStartDay, visibleWeeks)
    }

    val maxValue = remember(dataList) {
        dataList.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    }

    val animationProgress = rememberChartAnimation(config.animation)

    // rememberUpdatedState ensures the pointerInput coroutine (which is never restarted
    // because its key is Unit) always reads the latest config and callback values.
    val currentConfig by rememberUpdatedState(config)
    val currentOnDayClick by rememberUpdatedState(onDayClick)

    // Pre-measure month labels once per style/data change so the draw path is allocation-free.
    val measuredMonthLabels: Map<String, TextLayoutResult> = remember(
        config.showMonthLabels,
        config.labelTextStyle,
        gridLayout.monthBoundaries,
    ) {
        if (!config.showMonthLabels) return@remember emptyMap()
        gridLayout.monthBoundaries.associate { (_, label) ->
            label to textMeasurer.measure(label, config.labelTextStyle)
        }
    }

    // Pre-measure day-of-week labels (Mon, Wed, Fri) including their grid row index.
    val measuredDayLabelRows: List<Pair<Int, TextLayoutResult>> = remember(
        config.showDayLabels,
        config.weekStartDay,
        config.labelTextStyle,
    ) {
        if (!config.showDayLabels) return@remember emptyList()
        val rows = if (config.weekStartDay == WeekStartDay.SUNDAY) {
            listOf(1 to "Mon", 3 to "Wed", 5 to "Fri")
        } else {
            listOf(0 to "Mon", 2 to "Wed", 4 to "Fri")
        }
        rows.map { (dayIndex, label) -> dayIndex to textMeasurer.measure(label, config.labelTextStyle) }
    }

    val cellSizePx = remember(config.cellSize, density) { with(density) { config.cellSize.toPx() } }
    val cellStridePx = remember(config.cellSize, config.cellSpacing, density) {
        cellSizePx + with(density) { config.cellSpacing.toPx() }
    }

    val leftPadding = measuredDayLabelRows.maxOfOrNull { (_, r) -> r.size.width }
        ?.let { it + 8f } ?: 0f
    val topPadding = measuredMonthLabels.values.firstOrNull()?.size?.height
        ?.let { it + 4f } ?: 0f

    val canvasWidth = leftPadding + gridLayout.totalWeeks * cellStridePx
    val canvasHeight = topPadding + 7 * cellStridePx

    val cellBounds = remember { mutableListOf<Pair<Rect, CalendarData>>() }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }

    val outerModifier = if (scrollEnabled) modifier.horizontalScroll(scrollState) else modifier

    Box(modifier = outerModifier) {
        if (gridLayout.totalWeeks == 0) return@Box

        Canvas(
            modifier = Modifier
                .size(
                    width = with(density) { canvasWidth.toDp() },
                    height = with(density) { canvasHeight.toDp() },
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hit = cellBounds.firstOrNull { (rect, _) -> rect.contains(offset) }
                        if (hit != null) {
                            currentOnDayClick?.invoke(hit.second)
                            tooltipState = TooltipState(
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
            if (config.showMonthLabels) {
                drawMonthLabels(
                    monthBoundaries = gridLayout.monthBoundaries,
                    measuredLabels = measuredMonthLabels,
                    leftPadding = leftPadding,
                    topPadding = topPadding,
                    cellStridePx = cellStridePx,
                )
            }

            if (config.showDayLabels) {
                drawDayLabels(
                    dayLabelRows = measuredDayLabelRows,
                    topPadding = topPadding,
                    leftPadding = leftPadding,
                    cellStridePx = cellStridePx,
                )
            }

            drawCalendarGrid(
                gridLayout = gridLayout,
                config = config,
                maxValue = maxValue,
                leftPadding = leftPadding,
                topPadding = topPadding,
                cellSizePx = cellSizePx,
                cellStridePx = cellStridePx,
                animationProgress = animationProgress.value,
                cellBoundsOutput = cellBounds,
            )

            tooltipState?.let { ts ->
                drawTooltip(
                    tooltipState = ts,
                    config = config.tooltipConfig,
                    textMeasurer = textMeasurer,
                    chartWidth = size.width,
                    chartTop = topPadding,
                    chartBottom = size.height,
                )
            }
        }
    }
}
