package com.himanshoe.charty.calendar.data

/**
 * Represents a single day's contribution data for the [com.himanshoe.charty.calendar.CalendarHeatmapChart].
 *
 * @property year The year of the data point (e.g., 2024).
 * @property month The month of the data point in the range 1–12.
 * @property day The day of the month in the range 1–31.
 * @property value The contribution count or intensity for this day.
 *   Values of `0` or less are treated as empty (no-activity) cells.
 *
 * Example usage:
 * ```kotlin
 * CalendarData(year = 2024, month = 6, day = 15, value = 4f)
 * ```
 */
data class CalendarData(
    val year: Int,
    val month: Int,
    val day: Int,
    val value: Float,
) {
    init {
        require(month in 1..12) { "month must be in 1..12, got $month" }
        require(day in 1..31) { "day must be in 1..31, got $day" }
    }
}
