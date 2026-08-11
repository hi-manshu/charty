package com.himanshoe.charty.common.axis

import kotlin.test.Test
import kotlin.test.assertEquals

class DateAxisTest {
    @Test
    fun epochDayZero_isUnixEpoch() {
        assertEquals(Triple(1970, 1, 1), epochDayToDate(0))
    }

    @Test
    fun epochDayNegativeOne_isDayBefore() {
        assertEquals(Triple(1969, 12, 31), epochDayToDate(-1))
    }

    @Test
    fun knownEpochDays_resolveCorrectly() {
        assertEquals(Triple(2024, 1, 1), epochDayToDate(19723))
        assertEquals(Triple(2020, 1, 1), epochDayToDate(18262))
        assertEquals(Triple(2020, 2, 29), epochDayToDate(18321))
    }

    @Test
    fun builtInFormats_render() {
        val day = 19723L // 2024-01-01
        assertEquals("2024-01-01", dateAxisLabel(day, DateLabelFormat.ISO_DATE))
        assertEquals("Jan 1", dateAxisLabel(day, DateLabelFormat.MONTH_DAY))
        assertEquals("1 Jan", dateAxisLabel(day, DateLabelFormat.DAY_MONTH))
        assertEquals("Jan 2024", dateAxisLabel(day, DateLabelFormat.MONTH_YEAR))
        assertEquals("2024", dateAxisLabel(day, DateLabelFormat.YEAR))
    }

    @Test
    fun formatterReadsValueAsEpochDay() {
        val formatter = dateAxisFormatter(DateLabelFormat.ISO_DATE)
        assertEquals("2024-01-01", formatter(19723f))
    }

    @Test
    fun customFormatterOverload_getsResolvedDate() {
        val formatter = dateAxisFormatter { year, month, day -> "$day/$month/$year" }
        assertEquals("1/1/2024", formatter(19723f))
    }

    @Test
    fun customMonthNames_areUsed() {
        val french = listOf("janv", "févr", "mars", "avr", "mai", "juin", "juil", "août", "sept", "oct", "nov", "déc")
        assertEquals("janv 1", dateAxisLabel(19723, DateLabelFormat.MONTH_DAY, french))
    }
}
