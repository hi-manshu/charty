package com.himanshoe.charty.common.accessibility

import com.himanshoe.charty.calendar.data.CalendarData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartAccessibilityTest {
    @Test
    fun calendarDescription_empty_reportsEmpty() {
        assertEquals("Empty calendar heatmap.", generateCalendarHeatmapDescription(emptyList()))
    }

    @Test
    fun calendarDescription_reportsCountAndBusiestDay() {
        val data =
            listOf(
                CalendarData(year = 2024, month = 1, day = 1, value = 3f),
                CalendarData(year = 2024, month = 6, day = 15, value = 7f),
            )
        val description = generateCalendarHeatmapDescription(data)
        assertTrue(description.contains("2 days with data"), description)
        assertTrue(description.contains("2024-6-15"), description)
        assertTrue(description.contains("7"), description)
    }

    @Test
    fun calendarDescription_singleDay_usesSingularDay() {
        val data = listOf(CalendarData(year = 2024, month = 3, day = 9, value = 1f))
        assertTrue(generateCalendarHeatmapDescription(data).contains("1 day with data"))
    }
}
