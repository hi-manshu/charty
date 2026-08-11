package com.himanshoe.charty.calendar

import com.himanshoe.charty.calendar.internal.dayOfWeek
import com.himanshoe.charty.calendar.internal.gregorianToJdn
import com.himanshoe.charty.calendar.internal.jdnToGregorian
import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarDateMathTest {
    @Test
    fun jdnRoundTrip_returnsOriginalDate() {
        val dates =
            listOf(
                Triple(2024, 1, 1),
                Triple(2024, 2, 29),
                Triple(2023, 12, 31),
                Triple(2000, 1, 1),
                Triple(1999, 7, 15),
                Triple(2025, 6, 30),
            )
        dates.forEach { (y, m, d) ->
            val jdn = gregorianToJdn(year = y, month = m, day = d)
            assertEquals(Triple(y, m, d), jdnToGregorian(jdn), "round trip for $y-$m-$d")
        }
    }

    @Test
    fun consecutiveDays_differByOneJdn() {
        val jan1 = gregorianToJdn(year = 2024, month = 1, day = 1)
        val jan2 = gregorianToJdn(year = 2024, month = 1, day = 2)
        assertEquals(1L, jan2 - jan1)
    }

    @Test
    fun dayOfWeek_knownDates_useSundayZero() {
        // 2024-01-01 was a Monday, 2023-01-01 a Sunday, 2020-02-29 a Saturday.
        assertEquals(1, dayOfWeek(year = 2024, month = 1, day = 1))
        assertEquals(0, dayOfWeek(year = 2023, month = 1, day = 1))
        assertEquals(6, dayOfWeek(year = 2020, month = 2, day = 29))
    }
}
