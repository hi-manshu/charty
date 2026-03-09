package com.himanshoe.charty.calendar.internal

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.WeekStartDay
import com.himanshoe.charty.calendar.config.calendarMonthName
import com.himanshoe.charty.calendar.data.CalendarData
import kotlin.math.roundToInt

/**
 * Converts a proleptic Gregorian date to its Julian Day Number (JDN).
 * Uses the standard astronomical algorithm valid for all dates after 1 March 200 AD.
 */
internal fun gregorianToJdn(year: Int, month: Int, day: Int): Long {
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    return day + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045
}

/**
 * Converts a Julian Day Number back to a Gregorian (year, month, day) triple.
 */
internal fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
    val a = jdn + 32044
    val b = (4 * a + 3) / 146097
    val c = a - (146097 * b) / 4
    val d = (4 * c + 3) / 1461
    val e = c - (1461 * d) / 4
    val m = (5 * e + 2) / 153
    val day = (e - (153 * m + 2) / 5 + 1).toInt()
    val month = (m + 3 - 12 * (m / 10)).toInt()
    val year = (100 * b + d - 4800 + m / 10).toInt()
    return Triple(year, month, day)
}

/**
 * Returns the day of the week using Sakamoto's algorithm.
 * Result: 0 = Sunday, 1 = Monday, …, 6 = Saturday.
 */
internal fun dayOfWeek(year: Int, month: Int, day: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (month < 3) year - 1 else year
    return (y + y / 4 - y / 100 + y / 400 + t[month - 1] + day) % 7
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
    if (dataList.isEmpty()) return GridLayout(emptyList(), emptyMap(), 0, emptyList())

    val startOffset = if (weekStartDay == WeekStartDay.SUNDAY) 0 else 1

    val lookup = dataList.associateBy { gregorianToJdn(it.year, it.month, it.day) }

    val minJdn = lookup.keys.min()
    val maxJdn = lookup.keys.max()

    val (minYear, minMonth, minDay) = jdnToGregorian(minJdn)
    val minDow = dayOfWeek(minYear, minMonth, minDay)
    val offsetToWeekStart = ((minDow - startOffset + 7) % 7).toLong()
    val gridStartJdn = minJdn - offsetToWeekStart

    val (maxYear, maxMonth, maxDay) = jdnToGregorian(maxJdn)
    val maxDow = dayOfWeek(maxYear, maxMonth, maxDay)
    val offsetToWeekEnd = ((startOffset + 6 - maxDow + 7) % 7).toLong()
    val gridEndJdn = maxJdn + offsetToWeekEnd

    val totalWeeks = ((gridEndJdn - gridStartJdn + 1) / 7).toInt()

    val effectiveTotalWeeks: Int
    val effectiveStartJdn: Long
    if (visibleWeeks != null && visibleWeeks < totalWeeks) {
        effectiveTotalWeeks = visibleWeeks
        effectiveStartJdn = gridEndJdn - (visibleWeeks * 7L) + 1
    } else {
        effectiveTotalWeeks = totalWeeks
        effectiveStartJdn = gridStartJdn
    }

    val cells = mutableListOf<GridCell>()
    val monthBoundaries = mutableListOf<Pair<Int, String>>()
    val seenMonthKeys = mutableSetOf<Long>()

    val (firstYear, firstMonth, _) = jdnToGregorian(effectiveStartJdn)
    seenMonthKeys.add(firstYear * 100L + firstMonth)
    monthBoundaries.add(0 to calendarMonthName(firstMonth))

    for (weekIndex in 0 until effectiveTotalWeeks) {
        for (dayIndex in 0..6) {
            val jdn = effectiveStartJdn + weekIndex * 7L + dayIndex

            lookup[jdn]?.let { data ->
                cells.add(GridCell(weekIndex, dayIndex, data))
            }

            val (year, month, day) = jdnToGregorian(jdn)
            if (day == 1) {
                val key = year * 100L + month
                if (key !in seenMonthKeys) {
                    seenMonthKeys.add(key)
                    monthBoundaries.add(weekIndex to calendarMonthName(month))
                }
            }
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
internal fun CalendarHeatmapConfig.resolveColor(value: Float, maxValue: Float): Color {
    if (value <= 0f || maxValue <= 0f) return emptyColor
    val ratio = (value / maxValue).coerceIn(0f, 1f)
    val index = (ratio * (intensityColors.size - 1))
        .roundToInt()
        .coerceIn(0, intensityColors.size - 1)
    return intensityColors[index]
}
