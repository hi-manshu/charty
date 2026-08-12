package com.himanshoe.charty.common.axis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val GERMAN_MONTHS =
    listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")

class DateTimeAxisLocaleTest {
    @Test
    fun defaultLocaleFormatsInEnglish() {
        val locale = DateTimeAxisLocale()
        assertEquals("Aug", locale.formatMonth(locale.monthName(8)))
        assertEquals("Aug 12", locale.formatDay(locale.monthName(8), 12))
        assertEquals("09:05", locale.formatTime(9, 5))
        assertEquals("2026", locale.formatYear(2026))
    }

    @Test
    fun translatedMonthNamesFlowThroughDefaultFormatters() {
        val locale = DateTimeAxisLocale(monthNamesShort = GERMAN_MONTHS)
        assertEquals("Mär", locale.monthName(3))
        assertEquals("Mär 12", locale.formatDay(locale.monthName(3), 12))
        assertEquals("Mär '26", locale.formatMonthWithYear(locale.monthName(3), 2026))
    }

    @Test
    fun customFormattersOverrideConventions() {
        val locale =
            DateTimeAxisLocale(
                formatTime = { hour, minute ->
                    val suffix = if (hour < 12) "am" else "pm"
                    val display = if (hour % 12 == 0) 12 else hour % 12
                    "$display:${minute.toString().padStart(2, '0')}$suffix"
                },
                formatDay = { monthName, day -> "$day $monthName" },
            )
        assertEquals("2:05pm", locale.formatTime(14, 5))
        assertEquals("12 Aug", locale.formatDay(locale.monthName(8), 12))
    }

    @Test
    fun ticksUseTheSuppliedLocale() {
        val marchFirst2026 = dateToEpochDay(year = 2026, month = 3, day = 1) * 86_400_000L
        val augustFirst2026 = dateToEpochDay(year = 2026, month = 8, day = 1) * 86_400_000L
        val labels =
            selectDateTimeTicks(
                startEpochMillis = marchFirst2026,
                endEpochMillis = augustFirst2026,
                maxTicks = 6,
                locale = DateTimeAxisLocale(monthNamesShort = GERMAN_MONTHS),
            ).map { it.label }
        assertTrue(labels.any { it.startsWith("Mär") }, "expected a German March label but got $labels")
        assertTrue(labels.none { it.startsWith("Mar ") }, "English month names leaked into $labels")
    }

    @Test
    fun timeAndDayTicksUseTheSuppliedFormatters() {
        val locale =
            DateTimeAxisLocale(
                monthNamesShort = GERMAN_MONTHS,
                formatTime = { hour, minute -> "${hour}h$minute" },
                formatDay = { monthName, day -> "$day. $monthName" },
            )
        val start = dateToEpochDay(year = 2026, month = 3, day = 2) * 86_400_000L
        val hourly =
            selectDateTimeTicks(
                startEpochMillis = start,
                endEpochMillis = start + 4 * 3_600_000L,
                maxTicks = 6,
                locale = locale,
            ).map { it.label }
        assertEquals(listOf("0h0", "1h0", "2h0", "3h0", "4h0"), hourly)
        val daily =
            selectDateTimeTicks(
                startEpochMillis = start,
                endEpochMillis = start + 3 * 86_400_000L,
                maxTicks = 5,
                locale = locale,
            ).map { it.label }
        assertEquals(listOf("2. Mär", "3. Mär", "4. Mär", "5. Mär"), daily)
    }

    @Test
    fun axisLabelsUseTheSuppliedLocale() {
        val start = dateToEpochDay(year = 2026, month = 3, day = 2) * 86_400_000L
        val values = List(4) { start + it * 86_400_000L }
        val labels =
            dateTimeAxisLabels(
                epochMillisValues = values,
                maxLabels = 4,
                locale = DateTimeAxisLocale(monthNamesShort = GERMAN_MONTHS),
            )
        assertEquals(listOf("Mär 2", "Mär 3", "Mär 4", "Mär 5"), labels)
    }

    @Test
    fun fallbackTickUsesTheSuppliedLocale() {
        val instant = dateToEpochDay(year = 2026, month = 3, day = 2) * 86_400_000L + 30_000L
        val ticks =
            selectDateTimeTicks(
                startEpochMillis = instant,
                endEpochMillis = instant,
                maxTicks = 3,
                locale = DateTimeAxisLocale(formatTime = { hour, minute -> "T$hour:$minute" }),
            )
        assertEquals(listOf("T0:0"), ticks.map { it.label })
    }

    @Test
    fun wrongMonthNameCountIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            DateTimeAxisLocale(monthNamesShort = listOf("Jan", "Feb"))
        }
    }
}
