package com.himanshoe.charty.common.axis

private const val DAYS_SHIFT = 719468
private const val ERA_DAYS = 146097
private const val ERA_NEG_ADJUST = 146096
private const val YEARS_PER_ERA = 400
private const val DAYS_PER_YEAR = 365
private const val YOE_LEAP_1 = 1460
private const val YOE_LEAP_2 = 36524
private const val YOE_LEAP_3 = 146096
private const val MP_FACTOR = 5
private const val MP_OFFSET = 2
private const val MP_DIVISOR = 153
private const val MONTH_PIVOT = 10
private const val MONTH_SHIFT_LOW = 3
private const val MONTH_SHIFT_HIGH = 9
private const val FEBRUARY = 2
private const val LEAP_DIV_4 = 4
private const val LEAP_DIV_100 = 100

/** Default three-letter English month abbreviations, index 0 = January. */
val DEFAULT_MONTH_ABBREVIATIONS: List<String> =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** Built-in layouts for a date axis label. See [dateAxisFormatter]. */
enum class DateLabelFormat {
    /** `2024-01-05` */
    ISO_DATE,

    /** `Jan 5` */
    MONTH_DAY,

    /** `5 Jan` */
    DAY_MONTH,

    /** `Jan 2024` */
    MONTH_YEAR,

    /** `2024` */
    YEAR,
}

/**
 * Converts an epoch day (days since 1970-01-01, negative before) to a `(year, month, day)` triple,
 * using Howard Hinnant's civil-from-days algorithm. Month is 1–12, day is 1–31.
 */
internal fun epochDayToDate(epochDay: Long): Triple<Int, Int, Int> {
    val z = epochDay + DAYS_SHIFT
    val era = (if (z >= 0) z else z - ERA_NEG_ADJUST) / ERA_DAYS
    val doe = z - era * ERA_DAYS
    val yoe = (doe - doe / YOE_LEAP_1 + doe / YOE_LEAP_2 - doe / YOE_LEAP_3) / DAYS_PER_YEAR
    val year = yoe + era * YEARS_PER_ERA
    val doy = doe - (DAYS_PER_YEAR * yoe + yoe / LEAP_DIV_4 - yoe / LEAP_DIV_100)
    val mp = (MP_FACTOR * doy + MP_OFFSET) / MP_DIVISOR
    val day = (doy - (MP_DIVISOR * mp + MP_OFFSET) / MP_FACTOR + 1).toInt()
    val month = (if (mp < MONTH_PIVOT) mp + MONTH_SHIFT_LOW else mp - MONTH_SHIFT_HIGH).toInt()
    val calendarYear = (if (month <= FEBRUARY) year + 1 else year).toInt()
    return Triple(calendarYear, month, day)
}

/**
 * Formats an [epochDay] as a label using a built-in [format] and [monthNames].
 */
fun dateAxisLabel(
    epochDay: Long,
    format: DateLabelFormat = DateLabelFormat.MONTH_DAY,
    monthNames: List<String> = DEFAULT_MONTH_ABBREVIATIONS,
): String {
    val (year, month, day) = epochDayToDate(epochDay)
    val monthName = monthNames[(month - 1).coerceIn(0, monthNames.size - 1)]
    return when (format) {
        DateLabelFormat.ISO_DATE -> "$year-${pad2(month)}-${pad2(day)}"
        DateLabelFormat.MONTH_DAY -> "$monthName $day"
        DateLabelFormat.DAY_MONTH -> "$day $monthName"
        DateLabelFormat.MONTH_YEAR -> "$monthName $year"
        DateLabelFormat.YEAR -> year.toString()
    }
}

/**
 * A value formatter for [AxisConfig.valueFormatter] (or for building X labels) that reads each axis
 * value as an **epoch day** (days since 1970-01-01) and formats it with a built-in [format].
 *
 * @param format The built-in label layout.
 * @param monthNames Month names (index 0 = January); override for localization.
 */
fun dateAxisFormatter(
    format: DateLabelFormat = DateLabelFormat.MONTH_DAY,
    monthNames: List<String> = DEFAULT_MONTH_ABBREVIATIONS,
): (Float) -> String = { value -> dateAxisLabel(epochDay = value.toLong(), format = format, monthNames = monthNames) }

/**
 * A value formatter that reads each axis value as an **epoch day** and lets the caller build the
 * label from the resolved `(year, month, day)` — full control over layout and localization.
 */
fun dateAxisFormatter(format: (year: Int, month: Int, day: Int) -> String): (Float) -> String =
    { value ->
        val (year, month, day) = epochDayToDate(value.toLong())
        format(year, month, day)
    }

private fun pad2(value: Int): String =
    if (value < MONTH_PIVOT) {
        "0$value"
    } else {
        value.toString()
    }
