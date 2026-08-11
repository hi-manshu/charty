package com.himanshoe.charty.calendar.internal

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.WeekStartDay
import com.himanshoe.charty.calendar.config.calendarMonthName
import com.himanshoe.charty.calendar.data.CalendarData
import kotlin.math.roundToInt

private const val JDN_MONTH_ADJUST = 14
private const val JDN_MONTH_BASE = 12
private const val JDN_YEAR_SHIFT = 4800
private const val JDN_MONTH_FACTOR = 153
private const val JDN_MONTH_DIVISOR = 5
private const val JDN_DAYS_IN_YEAR = 365
private const val JDN_4_YEAR_CYCLE = 4
private const val JDN_100_YEAR_CYCLE = 100
private const val JDN_400_YEAR_CYCLE = 400
private const val JDN_EPOCH_OFFSET = 32045

private const val JDN_REVERSE_OFFSET = 32044
private const val JDN_400Y_DAYS = 146097
private const val JDN_4Y_DAYS = 1461

private const val JDN_ALGO_OFFSET_2 = 2
private const val JDN_ALGO_OFFSET_3 = 3
private const val JDN_MONTH_TENS_DIVISOR = 10
private const val JDN_MONTH_ADJUST_M = 3
private const val PREV_YEAR_MONTH_THRESHOLD = 3

private val SAKAMOTO_TABLE = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
private const val DAYS_PER_WEEK = 7

private const val MONTH_KEY_BASE = 100L

/**
 * Converts a proleptic Gregorian date to its Julian Day Number (JDN).
 * Uses the standard astronomical algorithm valid for all dates after 1 March 200 AD.
 */
internal fun gregorianToJdn(
    year: Int,
    month: Int,
    day: Int,
): Long {
    val a = (JDN_MONTH_ADJUST - month) / JDN_MONTH_BASE
    val y = year + JDN_YEAR_SHIFT - a
    val m = month + JDN_MONTH_BASE * a - JDN_MONTH_ADJUST_M
    return day + (JDN_MONTH_FACTOR * m + JDN_ALGO_OFFSET_2) / JDN_MONTH_DIVISOR +
        JDN_DAYS_IN_YEAR.toLong() * y + y / JDN_4_YEAR_CYCLE -
        y / JDN_100_YEAR_CYCLE + y / JDN_400_YEAR_CYCLE - JDN_EPOCH_OFFSET
}

/**
 * Converts a Julian Day Number back to a Gregorian (year, month, day) triple.
 */
internal fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
    val a = jdn + JDN_REVERSE_OFFSET
    val b = (JDN_4_YEAR_CYCLE * a + JDN_ALGO_OFFSET_3) / JDN_400Y_DAYS
    val c = a - (JDN_400Y_DAYS * b) / JDN_4_YEAR_CYCLE
    val d = (JDN_4_YEAR_CYCLE * c + JDN_ALGO_OFFSET_3) / JDN_4Y_DAYS
    val e = c - (JDN_4Y_DAYS * d) / JDN_4_YEAR_CYCLE
    val m = (JDN_MONTH_DIVISOR * e + JDN_ALGO_OFFSET_2) / JDN_MONTH_FACTOR
    val day = (e - (JDN_MONTH_FACTOR * m + JDN_ALGO_OFFSET_2) / JDN_MONTH_DIVISOR + 1).toInt()
    val month = (m + JDN_MONTH_ADJUST_M - JDN_MONTH_BASE * (m / JDN_MONTH_TENS_DIVISOR)).toInt()
    val year = (JDN_100_YEAR_CYCLE * b + d - JDN_YEAR_SHIFT + m / JDN_MONTH_TENS_DIVISOR).toInt()
    return Triple(year, month, day)
}

/**
 * Returns the day of the week using Sakamoto's algorithm.
 * Result: 0 = Sunday, 1 = Monday, …, 6 = Saturday.
 */
internal fun dayOfWeek(
    year: Int,
    month: Int,
    day: Int,
): Int {
    val y =
        if (month < PREV_YEAR_MONTH_THRESHOLD) {
            year - 1
        } else {
            year
        }
    return (
        y + y / JDN_4_YEAR_CYCLE - y / JDN_100_YEAR_CYCLE +
            y / JDN_400_YEAR_CYCLE + SAKAMOTO_TABLE[month - 1] + day
    ) % DAYS_PER_WEEK
}

/** A single cell in the rendered grid that has data attached to it. */
internal data class GridCell(
    val weekIndex: Int,
    val dayIndex: Int,
    val calendarData: CalendarData,
)

/**
 * The fully computed grid layout for [com.himanshoe.charty.calendar.CalendarHeatmapChart].
 *
 * @property cells All cells that have data.
 * @property cellMap Pre-computed O(1) lookup from (weekIndex, dayIndex) to [GridCell].
 *   Avoids rebuilding a [HashMap] on every draw frame.
 * @property totalWeeks Total number of week-columns to render.
 * @property monthBoundaries Pairs of (weekIndex, monthLabel) where a new month starts.
 */
internal data class GridLayout(
    val cells: List<GridCell>,
    val cellMap: Map<Pair<Int, Int>, GridCell>,
    val totalWeeks: Int,
    val monthBoundaries: List<Pair<Int, String>>,
)

/**
 * Processes a single grid cell: records data if present, and marks month boundaries on day 1.
 */
private fun processGridCell(
    jdn: Long,
    weekIndex: Int,
    dayIndex: Int,
    lookup: Map<Long, CalendarData>,
    cells: MutableList<GridCell>,
    seenMonthKeys: MutableSet<Long>,
    monthBoundaries: MutableList<Pair<Int, String>>,
) {
    lookup[jdn]?.let { data ->
        cells.add(GridCell(weekIndex = weekIndex, dayIndex = dayIndex, calendarData = data))
    }
    val (year, month, day) = jdnToGregorian(jdn)
    if (day == 1) {
        val key = year * MONTH_KEY_BASE + month
        if (key !in seenMonthKeys) {
            seenMonthKeys.add(key)
            monthBoundaries.add(weekIndex to calendarMonthName(month))
        }
    }
}

/**
 * Computes the full grid layout from a list of [CalendarData] entries.
 *
 * The grid always starts on a week boundary (Sunday or Monday, per [weekStartDay]) that
 * covers the earliest data point, and ends on the week boundary covering the latest point.
 *
 * @param dataList The raw data points. Duplicate dates use the last occurrence.
 * @param weekStartDay Whether weeks begin on Sunday or Monday.
 * @param visibleWeeks If not `null`, only the **last** [visibleWeeks] week-columns are shown.
 */
internal fun computeGridLayout(
    dataList: List<CalendarData>,
    weekStartDay: WeekStartDay,
    visibleWeeks: Int?,
): GridLayout {
    if (dataList.isEmpty()) {
        return GridLayout(cells = emptyList(), cellMap = emptyMap(), totalWeeks = 0, monthBoundaries = emptyList())
    }

    val startOffset =
        if (weekStartDay == WeekStartDay.SUNDAY) {
            0
        } else {
            1
        }

    val lookup = dataList.associateBy { gregorianToJdn(year = it.year, month = it.month, day = it.day) }

    val minJdn = lookup.keys.min()
    val maxJdn = lookup.keys.max()

    val (minYear, minMonth, minDay) = jdnToGregorian(minJdn)
    val minDow = dayOfWeek(year = minYear, month = minMonth, day = minDay)
    val offsetToWeekStart = ((minDow - startOffset + DAYS_PER_WEEK) % DAYS_PER_WEEK).toLong()
    val gridStartJdn = minJdn - offsetToWeekStart

    val (maxYear, maxMonth, maxDay) = jdnToGregorian(maxJdn)
    val maxDow = dayOfWeek(year = maxYear, month = maxMonth, day = maxDay)
    val offsetToWeekEnd = ((startOffset + DAYS_PER_WEEK - 1 - maxDow + DAYS_PER_WEEK) % DAYS_PER_WEEK).toLong()
    val gridEndJdn = maxJdn + offsetToWeekEnd

    val totalWeeks = ((gridEndJdn - gridStartJdn + 1) / DAYS_PER_WEEK).toInt()

    val effectiveTotalWeeks: Int
    val effectiveStartJdn: Long
    if (visibleWeeks != null && visibleWeeks < totalWeeks) {
        effectiveTotalWeeks = visibleWeeks
        effectiveStartJdn = gridEndJdn - (visibleWeeks * DAYS_PER_WEEK.toLong()) + 1
    } else {
        effectiveTotalWeeks = totalWeeks
        effectiveStartJdn = gridStartJdn
    }

    val cells = mutableListOf<GridCell>()
    val monthBoundaries = mutableListOf<Pair<Int, String>>()
    val seenMonthKeys = mutableSetOf<Long>()

    val (firstYear, firstMonth, _) = jdnToGregorian(effectiveStartJdn)
    seenMonthKeys.add(firstYear * MONTH_KEY_BASE + firstMonth)
    monthBoundaries.add(0 to calendarMonthName(firstMonth))

    for (weekIndex in 0 until effectiveTotalWeeks) {
        for (dayIndex in 0 until DAYS_PER_WEEK) {
            val jdn = effectiveStartJdn + weekIndex * DAYS_PER_WEEK.toLong() + dayIndex
            processGridCell(
                jdn = jdn,
                weekIndex = weekIndex,
                dayIndex = dayIndex,
                lookup = lookup,
                cells = cells,
                seenMonthKeys = seenMonthKeys,
                monthBoundaries = monthBoundaries,
            )
        }
    }

    return GridLayout(
        cells = cells,
        cellMap = cells.associateBy { it.weekIndex to it.dayIndex },
        totalWeeks = effectiveTotalWeeks,
        monthBoundaries = monthBoundaries.sortedBy { it.first },
    )
}

/**
 * Maps a [value] to one of [CalendarHeatmapConfig.intensityColors] using linear interpolation
 * across the color buckets. Values ≤ 0 return [CalendarHeatmapConfig.emptyColor].
 */
internal fun CalendarHeatmapConfig.resolveColor(
    value: Float,
    maxValue: Float,
): Color {
    if (value <= 0f || maxValue <= 0f) {
        return emptyColor
    }
    val ratio = (value / maxValue).coerceIn(0f, 1f)
    val index =
        (ratio * (intensityColors.size - 1))
            .roundToInt()
            .coerceIn(0, intensityColors.size - 1)
    return intensityColors[index]
}
