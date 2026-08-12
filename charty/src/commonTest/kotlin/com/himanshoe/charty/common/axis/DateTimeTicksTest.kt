package com.himanshoe.charty.common.axis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 3_600_000L
private const val DAY_MS = 86_400_000L

private fun millisOf(
    year: Int,
    month: Int,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
): Long = dateToEpochDay(year = year, month = month, day = day) * DAY_MS + hour * HOUR_MS + minute * MINUTE_MS

class DateTimeTicksTest {
    @Test
    fun thirtyMinuteRange_usesQuarterHourTicks() {
        val start = millisOf(year = 2024, month = 1, day = 1, hour = 10)
        val ticks =
            selectDateTimeTicks(startEpochMillis = start, endEpochMillis = start + 30 * MINUTE_MS, maxTicks = 5)
        assertEquals(expected = listOf("10:00", "10:15", "10:30"), actual = ticks.map { it.label })
        assertEquals(expected = start, actual = ticks.first().epochMillis)
    }

    @Test
    fun quarterHourTicks_snapToQuarterHourBoundaries() {
        val start = millisOf(year = 2024, month = 1, day = 1, hour = 10, minute = 7)
        val end = millisOf(year = 2024, month = 1, day = 1, hour = 10, minute = 37)
        val ticks = selectDateTimeTicks(startEpochMillis = start, endEpochMillis = end, maxTicks = 5)
        assertEquals(expected = listOf("10:15", "10:30"), actual = ticks.map { it.label })
        assertEquals(
            expected = millisOf(year = 2024, month = 1, day = 1, hour = 10, minute = 15),
            actual = ticks.first().epochMillis,
        )
    }

    @Test
    fun sixHourRange_usesHourlyTicks() {
        val start = millisOf(year = 2024, month = 1, day = 1)
        val ticks =
            selectDateTimeTicks(startEpochMillis = start, endEpochMillis = start + 6 * HOUR_MS, maxTicks = 12)
        assertEquals(
            expected = listOf("00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00"),
            actual = ticks.map { it.label },
        )
    }

    @Test
    fun threeDayRange_usesDailyTicks_snappedToMidnight() {
        val start = millisOf(year = 2024, month = 1, day = 1, hour = 7, minute = 30)
        val ticks =
            selectDateTimeTicks(startEpochMillis = start, endEpochMillis = start + 3 * DAY_MS, maxTicks = 6)
        assertEquals(expected = listOf("Jan 2", "Jan 3", "Jan 4"), actual = ticks.map { it.label })
        assertEquals(expected = millisOf(year = 2024, month = 1, day = 2), actual = ticks.first().epochMillis)
    }

    @Test
    fun threeWeekRange_usesWeeklyTicks_snappedToMonday() {
        val start = millisOf(year = 2024, month = 1, day = 1)
        val ticks =
            selectDateTimeTicks(startEpochMillis = start, endEpochMillis = start + 21 * DAY_MS, maxTicks = 5)
        assertEquals(expected = listOf("Jan 1", "Jan 8", "Jan 15", "Jan 22"), actual = ticks.map { it.label })
    }

    @Test
    fun twoMonthRange_usesMonthTicks_snappedToFirstOfMonth() {
        val start = millisOf(year = 2024, month = 1, day = 6)
        val ticks =
            selectDateTimeTicks(startEpochMillis = start, endEpochMillis = start + 61 * DAY_MS, maxTicks = 6)
        assertEquals(expected = listOf("Feb", "Mar"), actual = ticks.map { it.label })
        assertEquals(expected = millisOf(year = 2024, month = 2, day = 1), actual = ticks.first().epochMillis)
    }

    @Test
    fun monthTicks_showYearSuffix_onYearChange() {
        val start = millisOf(year = 2026, month = 11, day = 15)
        val end = millisOf(year = 2027, month = 2, day = 10)
        val ticks = selectDateTimeTicks(startEpochMillis = start, endEpochMillis = end, maxTicks = 6)
        assertEquals(expected = listOf("Dec", "Jan '27", "Feb"), actual = ticks.map { it.label })
        assertEquals(expected = millisOf(year = 2027, month = 1, day = 1), actual = ticks[1].epochMillis)
    }

    @Test
    fun fiveYearRange_usesYearTicks_snappedToJanuaryFirst() {
        val start = millisOf(year = 2020, month = 3, day = 15)
        val end = millisOf(year = 2025, month = 6, day = 1)
        val ticks = selectDateTimeTicks(startEpochMillis = start, endEpochMillis = end, maxTicks = 7)
        assertEquals(expected = listOf("2021", "2022", "2023", "2024", "2025"), actual = ticks.map { it.label })
        assertEquals(expected = millisOf(year = 2021, month = 1, day = 1), actual = ticks.first().epochMillis)
    }

    @Test
    fun multiDecadeRange_fallsBackToNiceYearSteps_withinMaxTicks() {
        val start = millisOf(year = 1995, month = 6, day = 1)
        val end = millisOf(year = 2025, month = 6, day = 1)
        val ticks = selectDateTimeTicks(startEpochMillis = start, endEpochMillis = end, maxTicks = 5)
        assertTrue(ticks.size <= 5)
        assertEquals(expected = listOf("2000", "2010", "2020"), actual = ticks.map { it.label })
    }

    @Test
    fun singleInstant_onBoundary_returnsSingleTick() {
        val instant = millisOf(year = 2024, month = 8, day = 12, hour = 10, minute = 23)
        val ticks = selectDateTimeTicks(startEpochMillis = instant, endEpochMillis = instant, maxTicks = 4)
        assertEquals(expected = listOf("10:23"), actual = ticks.map { it.label })
        assertEquals(expected = instant, actual = ticks.first().epochMillis)
    }

    @Test
    fun singleInstant_offBoundary_fallsBackToSingleTickAtStart() {
        val instant = millisOf(year = 2024, month = 8, day = 12, hour = 10, minute = 23) + 30_000L
        val ticks = selectDateTimeTicks(startEpochMillis = instant, endEpochMillis = instant, maxTicks = 4)
        assertEquals(expected = listOf("10:23"), actual = ticks.map { it.label })
        assertEquals(expected = instant, actual = ticks.first().epochMillis)
    }

    @Test
    fun invalidArguments_throw() {
        assertFailsWith<IllegalArgumentException> {
            selectDateTimeTicks(startEpochMillis = 0L, endEpochMillis = 1L, maxTicks = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            selectDateTimeTicks(startEpochMillis = 1L, endEpochMillis = 0L, maxTicks = 4)
        }
        assertFailsWith<IllegalArgumentException> {
            dateTimeAxisLabels(epochMillisValues = listOf(0L), maxLabels = 0)
        }
    }

    @Test
    fun axisLabels_emptyValues_returnEmptyList() {
        val labels = dateTimeAxisLabels(epochMillisValues = emptyList(), maxLabels = 5)
        assertEquals(expected = emptyList(), actual = labels)
    }

    @Test
    fun axisLabels_singleValue_returnsSingleLabel() {
        val instant = millisOf(year = 2024, month = 8, day = 12, hour = 10, minute = 23)
        val labels = dateTimeAxisLabels(epochMillisValues = listOf(instant), maxLabels = 4)
        assertEquals(expected = listOf("10:23"), actual = labels)
    }

    @Test
    fun axisLabels_hourlyDay_produceSparseSixHourLabels() {
        val base = millisOf(year = 2024, month = 1, day = 1)
        val values = List(25) { base + it * HOUR_MS }
        val labels = dateTimeAxisLabels(epochMillisValues = values, maxLabels = 5)
        assertEquals(expected = 25, actual = labels.size)
        assertEquals(expected = 5, actual = labels.count { it.isNotEmpty() })
        assertEquals(expected = "00:00", actual = labels[0])
        assertEquals(expected = "06:00", actual = labels[6])
        assertEquals(expected = "12:00", actual = labels[12])
        assertEquals(expected = "18:00", actual = labels[18])
        assertEquals(expected = "00:00", actual = labels[24])
    }

    @Test
    fun axisLabels_irregularSampling_attachToNearestPoint() {
        val base = millisOf(year = 2024, month = 1, day = 1, hour = 10)
        val values = listOf(base, base + 45 * MINUTE_MS, base + 70 * MINUTE_MS)
        val labels = dateTimeAxisLabels(epochMillisValues = values, maxLabels = 3)
        assertEquals(expected = listOf("10:00", "", "11:00"), actual = labels)
    }

    @Test
    fun tickCount_neverExceedsMaxTicks_andIsNeverEmpty() {
        val start = millisOf(year = 2024, month = 3, day = 7, hour = 13, minute = 41)
        val ranges =
            listOf(
                0L,
                1L,
                MINUTE_MS,
                7 * MINUTE_MS,
                HOUR_MS,
                5 * HOUR_MS,
                DAY_MS,
                9 * DAY_MS,
                40 * DAY_MS,
                200 * DAY_MS,
                900 * DAY_MS,
                40 * 365 * DAY_MS,
                900 * 365 * DAY_MS,
            )
        ranges.forEach { range ->
            for (maxTicks in 1..8) {
                val ticks =
                    selectDateTimeTicks(
                        startEpochMillis = start,
                        endEpochMillis = start + range,
                        maxTicks = maxTicks,
                    )
                assertTrue(ticks.isNotEmpty(), "range $range with maxTicks $maxTicks produced no ticks")
                assertTrue(
                    ticks.size <= maxTicks,
                    "range $range with maxTicks $maxTicks produced ${ticks.size} ticks",
                )
                assertTrue(
                    ticks.zipWithNext().all { (previous, next) -> next.epochMillis > previous.epochMillis },
                    "range $range with maxTicks $maxTicks produced unordered ticks",
                )
                assertTrue(
                    ticks.all { it.label.isNotEmpty() },
                    "range $range with maxTicks $maxTicks produced an empty label",
                )
            }
        }
    }

    @Test
    fun ticksBeforeTheEpoch_snapCorrectly() {
        val start = millisOf(year = 1965, month = 11, day = 20)
        val end = millisOf(year = 1966, month = 3, day = 5)
        val ticks = selectDateTimeTicks(startEpochMillis = start, endEpochMillis = end, maxTicks = 6)
        assertEquals(expected = listOf("Dec", "Jan '66", "Feb", "Mar"), actual = ticks.map { it.label })
        assertEquals(expected = millisOf(year = 1965, month = 12, day = 1), actual = ticks.first().epochMillis)
    }

    @Test
    fun dateToEpochDay_isInverseOfEpochDayToDate() {
        assertEquals(expected = 0L, actual = dateToEpochDay(year = 1970, month = 1, day = 1))
        assertEquals(expected = 19723L, actual = dateToEpochDay(year = 2024, month = 1, day = 1))
        assertEquals(expected = -1L, actual = dateToEpochDay(year = 1969, month = 12, day = 31))
        assertEquals(expected = 18321L, actual = dateToEpochDay(year = 2020, month = 2, day = 29))
    }
}
