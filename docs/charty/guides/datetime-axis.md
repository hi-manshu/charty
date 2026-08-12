# Datetime Axis and Localization

Time-series data rarely wants one label per point. Charty picks **smart, snapped
ticks** for a datetime range — whole minutes, the top of the hour, midnight,
Monday, the 1st of the month, January 1st — and formats them at a granularity that
suits the range, all from pure epoch-millisecond arithmetic with no platform date
library involved.

Two entry points, both in `com.himanshoe.charty.common.axis`:

| Function | Use it for |
|---|---|
| `selectDateTimeTicks(...)` | The ticks themselves — instants plus formatted labels. |
| `dateTimeAxisLabels(...)` | One label per data point, ready to feed a chart's categorical x-axis. |

---

## `dateTimeAxisLabels` — the common case

Your chart has one point per sample and you want sparse, sensible time labels
under it.

```kotlin
import com.himanshoe.charty.common.axis.dateTimeAxisLabels

val timestamps: List<Long> = readings.map { it.epochMillis }
val labels = dateTimeAxisLabels(epochMillisValues = timestamps, maxLabels = 6)

val data = readings.mapIndexed { index, reading ->
    LineData(label = labels[index], value = reading.value)
}

LineChart(
    data = { data },
    color = ChartyColor.Solid(ChartyColors.Blue),
)
```

The returned list is **the same size as the input**. Points that are not tick
positions get `""`, which renders invisibly — leaving exactly the sparse snapped
ticks you asked for.

`maxLabels` must be positive.

### What it assumes

Ticks are chosen over the values' `min..max` range, then each tick is assigned to
the data point whose timestamp is **nearest** to it (earliest point wins a tie).
Two consequences worth knowing:

- Points are assumed to be roughly evenly sampled. With irregular sampling the
  labels still appear, but on the nearest point rather than at the tick's exact
  position, so they may sit slightly off the true boundary.
- If two ticks resolve to the same nearest point, they collapse into a single
  label — sparsely sampled data can therefore yield fewer labels than ticks.

---

## `selectDateTimeTicks` — the ticks themselves

When you need the instants (to place ticks by value rather than by index, or to
build your own axis), ask for them directly.

```kotlin
import com.himanshoe.charty.common.axis.DateTimeTick
import com.himanshoe.charty.common.axis.selectDateTimeTicks

val ticks: List<DateTimeTick> =
    selectDateTimeTicks(
        startEpochMillis = windowStart,
        endEpochMillis = windowEnd,
        maxTicks = 8,
    )

ticks.forEach { tick ->
    println("${tick.epochMillis} -> ${tick.label}")   // 1755000000000 -> "Aug 12"
}
```

```kotlin
data class DateTimeTick(
    val epochMillis: Long,
    val label: String,
)
```

`maxTicks` must be positive and `endEpochMillis` must be `>= startEpochMillis`;
both are enforced with `require`.

---

## Adaptive granularity

The finest granularity that still fits within your tick budget is chosen from:

**minute → 15 minutes → hour → 6 hours → day → week → month → quarter → year**

Ranges too long even for yearly ticks fall back to multi-year steps on "nice"
numbers (2, 5, 10, 20, 50, …), which lands them on round years such as decades.

Labels adapt to whichever granularity was picked:

| Granularity | Snaps to | Default label |
|---|---|---|
| Minute, 15-minute, hour, 6-hour | Whole minutes / the top of the hour | `14:30` |
| Day, week | Midnight UTC; weeks on Monday | `Aug 12` |
| Month, quarter | The 1st of the month; quarters on Jan/Apr/Jul/Oct | `Aug`, or `Aug '26` when the year changes between consecutive ticks |
| Year, multi-year | January 1st | `2026` |

If the range is so short that no snapped boundary falls inside it, a single tick at
`startEpochMillis` is returned, so the axis is never label-less.

> **All calendar maths is UTC-based** (proleptic Gregorian). Timestamps are
> interpreted as instants, not local wall-clock times. If you need local-time
> boundaries, offset your epoch millis before passing them in.

---

## Localization with `DateTimeAxisLocale`

Charty ships **no locale database**, on purpose — bundling one would bloat every
consumer for a feature most of them configure in two lines. Instead, every label
goes through a `DateTimeAxisLocale`, which you supply.

There are two levels.

### Level 1: translate

Pass month names in the target language and keep every formatter. The defaults
render whatever names they are given.

```kotlin
import com.himanshoe.charty.common.axis.DateTimeAxisLocale

val french =
    DateTimeAxisLocale(
        monthNamesShort =
            listOf("janv", "févr", "mars", "avr", "mai", "juin", "juil", "août", "sept", "oct", "nov", "déc"),
    )

val labels = dateTimeAxisLabels(epochMillisValues = timestamps, maxLabels = 6, locale = french)
```

`monthNamesShort` must hold **exactly 12** entries, index 0 = January; the `init`
block enforces it.

### Level 2: reformat

For conventions the defaults do not cover — 12-hour clocks, day-before-month
ordering, non-Gregorian numerals — replace the individual formatter lambdas.

```kotlin
val enUs =
    DateTimeAxisLocale(
        formatTime = { hour, minute ->
            val suffix = if (hour < 12) "AM" else "PM"
            val display = if (hour % 12 == 0) 12 else hour % 12
            "$display:${minute.toString().padStart(length = 2, padChar = '0')} $suffix"
        },
    )

val enGb =
    DateTimeAxisLocale(
        formatDay = { monthName, day -> "$day $monthName" },   // "12 Aug" instead of "Aug 12"
    )
```

The two levels are independent because **formatters receive an already-resolved
month name, never a month number** — so translating and reformatting never
interfere with each other.

### The formatter surface

| Property | Signature | Default output |
|---|---|---|
| `monthNamesShort` | `List<String>` (exactly 12) | `Jan`, `Feb`, … `Dec` |
| `formatTime` | `(hour: Int, minute: Int) -> String` | `14:30` (zero-padded 24-hour) |
| `formatDay` | `(monthName: String, day: Int) -> String` | `Aug 12` |
| `formatMonth` | `(monthName: String) -> String` | `Aug` |
| `formatMonthWithYear` | `(monthName: String, year: Int) -> String` | `Aug '26` |
| `formatYear` | `(year: Int) -> String` | `2026` |

### Delegating to a platform formatter

Because these are plain lambdas, an app needing platform-perfect localization can
delegate inside them — the library never gets in the way:

```kotlin
// In androidMain, or behind your own expect/actual.
val platformLocale =
    DateTimeAxisLocale(
        monthNamesShort = DateFormatSymbols.getInstance().shortMonths.take(n = 12),
        formatTime = { hour, minute -> myPlatformTimeFormatter.format(hour, minute) },
        formatYear = { year -> myPlatformYearFormatter.format(year) },
    )
```

Keep the lambdas cheap: they run once per tick, but ticks are recomputed whenever
the range changes, which on a streaming chart is every frame the window slides.

---

## Epoch *days* — the `DateAxis` helpers

When your axis values are **epoch days** rather than milliseconds — a daily series
plotted by value — use the simpler formatters in `DateAxis.kt`:

```kotlin
import com.himanshoe.charty.common.axis.DateLabelFormat
import com.himanshoe.charty.common.axis.dateAxisFormatter
import com.himanshoe.charty.common.axis.dateAxisLabel

// Built-in layouts.
val formatter = dateAxisFormatter(format = DateLabelFormat.DAY_MONTH)   // "5 Jan"

// Or take full control of the layout.
val custom = dateAxisFormatter { year, month, day -> "$day.$month.$year" }

// One-off label.
val text = dateAxisLabel(epochDay = 19723, format = DateLabelFormat.ISO_DATE)   // "2024-01-05"
```

`DateLabelFormat` offers `ISO_DATE` (`2024-01-05`), `MONTH_DAY` (`Jan 5`),
`DAY_MONTH` (`5 Jan`), `MONTH_YEAR` (`Jan 2024`), and `YEAR` (`2024`). Both
`dateAxisLabel` and the enum-based `dateAxisFormatter` take a `monthNames` list for
translation, defaulting to `DEFAULT_MONTH_ABBREVIATIONS`.

---

## Next steps

- **[Streaming](streaming.md)** — a datetime axis under a rolling live window.
- **[Common configuration](../configurations/common-config.md)** — axis and label styling via `ChartScaffoldConfig`.
