package com.himanshoe.charty.common.axis

import androidx.compose.runtime.Immutable

private const val YEAR_SUFFIX_MOD = 100
private const val MONTHS_PER_YEAR = 12

/**
 * How a datetime axis turns instants into text, so charts can be localized without the library
 * shipping any locale data.
 *
 * Two levels of customization:
 * - **Translate**: pass [monthNamesShort] in the target language and keep every formatter; the
 *   defaults render whatever names are supplied.
 * - **Reformat**: replace individual formatters for conventions the defaults do not cover, such as
 *   12-hour clocks, day-before-month ordering, or non-Gregorian numerals.
 *
 * Formatters receive an already-resolved month name rather than a month number, so translating and
 * reformatting stay independent. Apps needing platform-perfect localization can delegate to their
 * own date formatter inside these lambdas.
 *
 * @property monthNamesShort Abbreviated month names, index 0 = January; must hold exactly 12 entries.
 * @property formatTime Renders a time of day; defaults to a zero-padded 24-hour `"14:30"`.
 * @property formatDay Renders a day within a month; defaults to `"Aug 12"`.
 * @property formatMonth Renders a month on its own; defaults to the month name, e.g. `"Aug"`.
 * @property formatMonthWithYear Renders a month that starts a new year; defaults to `"Aug '26"`.
 * @property formatYear Renders a whole year; defaults to `"2026"`.
 */
@Immutable
data class DateTimeAxisLocale(
    val monthNamesShort: List<String> = DEFAULT_MONTH_ABBREVIATIONS,
    val formatTime: (hour: Int, minute: Int) -> String = { hour, minute -> "${pad2(hour)}:${pad2(minute)}" },
    val formatDay: (monthName: String, day: Int) -> String = { monthName, day -> "$monthName $day" },
    val formatMonth: (monthName: String) -> String = { monthName -> monthName },
    val formatMonthWithYear: (monthName: String, year: Int) -> String = { monthName, year ->
        "$monthName '${pad2(year.mod(YEAR_SUFFIX_MOD))}"
    },
    val formatYear: (year: Int) -> String = { year -> year.toString() },
) {
    init {
        require(monthNamesShort.size == MONTHS_PER_YEAR) {
            "monthNamesShort must have exactly $MONTHS_PER_YEAR entries but had ${monthNamesShort.size}"
        }
    }

    /** The abbreviated name for [month] (1–12), clamped to the valid range. */
    fun monthName(month: Int): String = monthNamesShort[(month - 1).coerceIn(0, monthNamesShort.size - 1)]
}
