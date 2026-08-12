package com.himanshoe.charty.common.axis

import androidx.compose.runtime.Immutable
import kotlin.math.abs

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_DAY = 86_400_000L
private const val QUARTER_HOUR_MILLIS = 15 * MILLIS_PER_MINUTE
private const val SIX_HOUR_MILLIS = 6 * MILLIS_PER_HOUR
private const val WEEK_MILLIS = 7 * MILLIS_PER_DAY
private const val MIN_MONTH_MILLIS = 28 * MILLIS_PER_DAY
private const val MIN_QUARTER_MILLIS = 90 * MILLIS_PER_DAY
private const val MIN_YEAR_MILLIS = 365 * MILLIS_PER_DAY
private const val MONTHS_PER_YEAR = 12
private const val MONTHS_PER_QUARTER = 3
private const val MONDAY_EPOCH_DAY = 4L
private const val NICE_STEP_MAGNITUDE = 10

private val NICE_YEAR_STEP_BASES = listOf(1, 2, 5)

/**
 * A single tick on a datetime axis: an instant and its ready-to-render label.
 *
 * @property epochMillis The tick's instant as milliseconds since 1970-01-01T00:00:00Z.
 * @property label The formatted label for the tick, e.g. `"14:30"`, `"Aug 12"`, `"Aug '26"`, `"2026"`.
 */
@Immutable
data class DateTimeTick(
    val epochMillis: Long,
    val label: String,
)

/**
 * The granularity a datetime axis snaps its ticks to. [minSpacingMillis] is the smallest possible
 * gap between two consecutive boundaries of the granularity (used as a conservative tick-count
 * bound), [stepMillis] is the exact step for fixed-length granularities, and [stepMonths] is the
 * calendar step for month-based granularities (`0` for fixed-length ones).
 */
private enum class DateTimeGranularity(
    val minSpacingMillis: Long,
    val stepMillis: Long,
    val stepMonths: Int,
) {
    MINUTE(minSpacingMillis = MILLIS_PER_MINUTE, stepMillis = MILLIS_PER_MINUTE, stepMonths = 0),
    QUARTER_HOUR(minSpacingMillis = QUARTER_HOUR_MILLIS, stepMillis = QUARTER_HOUR_MILLIS, stepMonths = 0),
    HOUR(minSpacingMillis = MILLIS_PER_HOUR, stepMillis = MILLIS_PER_HOUR, stepMonths = 0),
    SIX_HOUR(minSpacingMillis = SIX_HOUR_MILLIS, stepMillis = SIX_HOUR_MILLIS, stepMonths = 0),
    DAY(minSpacingMillis = MILLIS_PER_DAY, stepMillis = MILLIS_PER_DAY, stepMonths = 0),
    WEEK(minSpacingMillis = WEEK_MILLIS, stepMillis = WEEK_MILLIS, stepMonths = 0),
    MONTH(minSpacingMillis = MIN_MONTH_MILLIS, stepMillis = 0, stepMonths = 1),
    QUARTER(minSpacingMillis = MIN_QUARTER_MILLIS, stepMillis = 0, stepMonths = MONTHS_PER_QUARTER),
    YEAR(minSpacingMillis = MIN_YEAR_MILLIS, stepMillis = 0, stepMonths = MONTHS_PER_YEAR),
}

/**
 * Selects smart, nicely snapped ticks for a datetime range using pure epoch-millisecond math.
 *
 * The finest granularity that still fits is chosen from minute, 15-minute, hour, 6-hour, day, week,
 * month, quarter, and year so that the resulting tick count never exceeds [maxTicks]; ranges too
 * long even for yearly ticks fall back to multi-year steps (2, 5, 10, ... years). Ticks snap to
 * natural boundaries — whole minutes, the top of the hour, midnight (UTC), Monday, the 1st of the
 * month, and January 1st — and labels adapt to the granularity: times as `HH:MM`, days and weeks as
 * `"Aug 12"`, months and quarters as `"Aug"` (with a `"Aug '26"` year suffix when the year changes
 * between consecutive ticks), and years as `"2026"`. When no snapped boundary falls inside a very
 * short range, a single tick at [startEpochMillis] is returned so the axis is never label-less.
 *
 * All calendar math is UTC-based (proleptic Gregorian, via [epochDayToDate]).
 *
 * @param startEpochMillis Inclusive start of the range, in milliseconds since 1970-01-01T00:00:00Z.
 * @param endEpochMillis Inclusive end of the range, in milliseconds since 1970-01-01T00:00:00Z;
 *   must be `>=` [startEpochMillis].
 * @param maxTicks The maximum number of ticks to produce; must be positive.
 * @param locale The month names and per-granularity formatters used to render the labels; defaults
 *   to English with 24-hour times.
 * @return The selected ticks in ascending order, each carrying its instant and formatted label.
 */
fun selectDateTimeTicks(
    startEpochMillis: Long,
    endEpochMillis: Long,
    maxTicks: Int,
    locale: DateTimeAxisLocale = DateTimeAxisLocale(),
): List<DateTimeTick> {
    require(maxTicks > 0) { "maxTicks must be positive but was $maxTicks" }
    require(endEpochMillis >= startEpochMillis) {
        "endEpochMillis ($endEpochMillis) must be >= startEpochMillis ($startEpochMillis)"
    }
    val rangeMillis = endEpochMillis - startEpochMillis
    val granularity = chooseGranularity(rangeMillis = rangeMillis, maxTicks = maxTicks)
    val ticks =
        if (granularity.stepMonths > 0) {
            generateCalendarTicks(
                startEpochMillis = startEpochMillis,
                endEpochMillis = endEpochMillis,
                granularity = granularity,
                maxTicks = maxTicks,
                locale = locale,
            )
        } else {
            generateFixedStepTicks(
                startEpochMillis = startEpochMillis,
                endEpochMillis = endEpochMillis,
                granularity = granularity,
                locale = locale,
            )
        }
    return ticks.ifEmpty {
        listOf(
            DateTimeTick(
                epochMillis = startEpochMillis,
                label =
                    fallbackLabel(
                        granularity = granularity,
                        epochMillis = startEpochMillis,
                        locale = locale,
                    ),
            ),
        )
    }
}

/**
 * Maps each data point's timestamp to either a smart tick label or `""`, producing one label per
 * point for use as a chart's categorical x-axis labels (empty labels render invisibly, leaving
 * sparse snapped ticks).
 *
 * Ticks are chosen with [selectDateTimeTicks] over the values' min..max range, then each tick is
 * assigned to the data point whose timestamp is nearest to it (earliest point wins a tie). Points
 * are assumed to be roughly evenly sampled; with irregular sampling the labels still appear, but on
 * the nearest point rather than at the tick's exact position, so they may sit slightly off the true
 * boundary. Two ticks whose nearest point is the same one collapse into a single label, so sparsely
 * sampled data can yield fewer labels than there are ticks.
 *
 * @param epochMillisValues Each data point's timestamp in milliseconds since 1970-01-01T00:00:00Z,
 *   in the same order as the chart's data points.
 * @param maxLabels The maximum number of non-empty labels to produce; must be positive.
 * @param locale The month names and per-granularity formatters used to render the labels; defaults
 *   to English with 24-hour times.
 * @return A list the same size as [epochMillisValues] with tick labels at tick positions and `""`
 *   everywhere else; empty when [epochMillisValues] is empty.
 */
fun dateTimeAxisLabels(
    epochMillisValues: List<Long>,
    maxLabels: Int,
    locale: DateTimeAxisLocale = DateTimeAxisLocale(),
): List<String> {
    require(maxLabels > 0) { "maxLabels must be positive but was $maxLabels" }
    if (epochMillisValues.isEmpty()) {
        return emptyList()
    }
    val ticks =
        selectDateTimeTicks(
            startEpochMillis = epochMillisValues.min(),
            endEpochMillis = epochMillisValues.max(),
            maxTicks = maxLabels,
            locale = locale,
        )
    val labels = MutableList(epochMillisValues.size) { "" }
    ticks.forEach { tick ->
        val index = indexOfNearest(values = epochMillisValues, target = tick.epochMillis)
        if (labels[index].isEmpty()) {
            labels[index] = tick.label
        }
    }
    return labels
}

/**
 * Picks the finest granularity whose conservative tick-count bound fits within [maxTicks], falling
 * back to [DateTimeGranularity.YEAR] (with a multi-year step chosen later) for very long ranges.
 */
private fun chooseGranularity(
    rangeMillis: Long,
    maxTicks: Int,
): DateTimeGranularity =
    DateTimeGranularity.entries.firstOrNull { granularity ->
        rangeMillis / granularity.minSpacingMillis + 1 <= maxTicks
    } ?: DateTimeGranularity.YEAR

/**
 * Generates ticks on a fixed-length step grid anchored at the Unix epoch (midnight UTC), so hour
 * ticks land on the top of the hour, day ticks on midnight, and week ticks on Monday (the grid is
 * shifted by [MONDAY_EPOCH_DAY] because 1970-01-01 was a Thursday).
 */
private fun generateFixedStepTicks(
    startEpochMillis: Long,
    endEpochMillis: Long,
    granularity: DateTimeGranularity,
    locale: DateTimeAxisLocale,
): List<DateTimeTick> {
    val anchorOffset =
        if (granularity == DateTimeGranularity.WEEK) {
            MONDAY_EPOCH_DAY * MILLIS_PER_DAY
        } else {
            0L
        }
    var tickMillis =
        ceilToMultiple(value = startEpochMillis - anchorOffset, step = granularity.stepMillis) + anchorOffset
    val ticks = mutableListOf<DateTimeTick>()
    while (tickMillis <= endEpochMillis) {
        ticks +=
            DateTimeTick(
                epochMillis = tickMillis,
                label = fixedStepLabel(granularity = granularity, epochMillis = tickMillis, locale = locale),
            )
        tickMillis += granularity.stepMillis
    }
    return ticks
}

/**
 * Generates month-grid ticks (month, quarter, year, or multi-year) snapped to the 1st of the month
 * at midnight UTC. The grid is aligned on absolute month index, which lands quarters on
 * Jan/Apr/Jul/Oct, years on January 1st, and multi-year steps on round years (e.g. decades).
 */
private fun generateCalendarTicks(
    startEpochMillis: Long,
    endEpochMillis: Long,
    granularity: DateTimeGranularity,
    maxTicks: Int,
    locale: DateTimeAxisLocale,
): List<DateTimeTick> {
    val stepMonths =
        if (granularity == DateTimeGranularity.YEAR) {
            MONTHS_PER_YEAR * yearStepFor(rangeMillis = endEpochMillis - startEpochMillis, maxTicks = maxTicks)
        } else {
            granularity.stepMonths
        }
    val (startYear, startMonth, _) = epochDayToDate(startEpochMillis.floorDiv(MILLIS_PER_DAY))
    var monthIndex = startYear * MONTHS_PER_YEAR + (startMonth - 1)
    monthIndex -= monthIndex.mod(stepMonths)
    while (monthStartMillis(monthIndex) < startEpochMillis) {
        monthIndex += stepMonths
    }
    val ticks = mutableListOf<DateTimeTick>()
    var previousYear: Int? = null
    while (monthStartMillis(monthIndex) <= endEpochMillis) {
        val year = monthIndex.floorDiv(MONTHS_PER_YEAR)
        val month = monthIndex.mod(MONTHS_PER_YEAR) + 1
        ticks +=
            DateTimeTick(
                epochMillis = monthStartMillis(monthIndex),
                label =
                    calendarLabel(
                        granularity = granularity,
                        year = year,
                        month = month,
                        previousYear = previousYear,
                        locale = locale,
                    ),
            )
        previousYear = year
        monthIndex += stepMonths
    }
    return ticks
}

/**
 * Picks the smallest "nice" year step (1, 2, 5, 10, 20, 50, ...) whose conservative tick-count
 * bound fits within [maxTicks].
 */
private fun yearStepFor(
    rangeMillis: Long,
    maxTicks: Int,
): Int {
    val rangeYears = rangeMillis / MIN_YEAR_MILLIS
    var magnitude = 1
    while (true) {
        NICE_YEAR_STEP_BASES.forEach { base ->
            val step = base * magnitude
            if (rangeYears / step + 1 <= maxTicks) {
                return step
            }
        }
        magnitude *= NICE_STEP_MAGNITUDE
    }
}

/** The epoch millis of midnight UTC on the 1st of the month at absolute [monthIndex] (year * 12 + month - 1). */
private fun monthStartMillis(monthIndex: Int): Long =
    dateToEpochDay(
        year = monthIndex.floorDiv(MONTHS_PER_YEAR),
        month = monthIndex.mod(MONTHS_PER_YEAR) + 1,
        day = 1,
    ) * MILLIS_PER_DAY

/** Rounds [value] up to the nearest multiple of [step], correct for negative values too. */
private fun ceilToMultiple(
    value: Long,
    step: Long,
): Long = (value + step - 1).floorDiv(step) * step

/**
 * Formats a fixed-step tick through [locale]: sub-day granularities as a time of day, day and week
 * granularities as a day within its month.
 */
private fun fixedStepLabel(
    granularity: DateTimeGranularity,
    epochMillis: Long,
    locale: DateTimeAxisLocale,
): String {
    val isSubDay =
        granularity == DateTimeGranularity.MINUTE ||
            granularity == DateTimeGranularity.QUARTER_HOUR ||
            granularity == DateTimeGranularity.HOUR ||
            granularity == DateTimeGranularity.SIX_HOUR
    return if (isSubDay) {
        val millisOfDay = epochMillis.mod(MILLIS_PER_DAY)
        locale.formatTime(
            (millisOfDay / MILLIS_PER_HOUR).toInt(),
            (millisOfDay.mod(MILLIS_PER_HOUR) / MILLIS_PER_MINUTE).toInt(),
        )
    } else {
        val (_, month, day) = epochDayToDate(epochMillis.floorDiv(MILLIS_PER_DAY))
        locale.formatDay(locale.monthName(month), day)
    }
}

/**
 * Formats a calendar tick through [locale]: months and quarters as a month name, gaining a year
 * suffix when [year] differs from [previousYear]; years as a whole year.
 */
private fun calendarLabel(
    granularity: DateTimeGranularity,
    year: Int,
    month: Int,
    previousYear: Int?,
    locale: DateTimeAxisLocale,
): String =
    when {
        granularity == DateTimeGranularity.YEAR -> locale.formatYear(year)
        previousYear != null && previousYear != year -> locale.formatMonthWithYear(locale.monthName(month), year)
        else -> locale.formatMonth(locale.monthName(month))
    }

/**
 * Formats the label of the single fallback tick emitted when no snapped boundary falls inside the
 * requested range.
 */
private fun fallbackLabel(
    granularity: DateTimeGranularity,
    epochMillis: Long,
    locale: DateTimeAxisLocale,
): String {
    val (year, month, _) = epochDayToDate(epochMillis.floorDiv(MILLIS_PER_DAY))
    return when (granularity) {
        DateTimeGranularity.MONTH, DateTimeGranularity.QUARTER -> locale.formatMonth(locale.monthName(month))
        DateTimeGranularity.YEAR -> locale.formatYear(year)
        else -> fixedStepLabel(granularity = granularity, epochMillis = epochMillis, locale = locale)
    }
}

/** The index of the value in [values] nearest to [target]; the earliest index wins a tie. */
private fun indexOfNearest(
    values: List<Long>,
    target: Long,
): Int {
    var bestIndex = 0
    var bestDistance = Long.MAX_VALUE
    values.forEachIndexed { index, value ->
        val distance = abs(value - target)
        if (distance < bestDistance) {
            bestDistance = distance
            bestIndex = index
        }
    }
    return bestIndex
}
