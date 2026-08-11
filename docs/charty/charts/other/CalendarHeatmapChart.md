# CalendarHeatmapChart

Best for visualizing daily activity or intensity data over weeks and months in a calendar grid layout (similar to GitHub's contribution graph).

```kotlin
CalendarHeatmapChart(
    data = {
        listOf(
            CalendarData(year = 2024, month = 1, day = 1,  value = 3f),
            CalendarData(year = 2024, month = 1, day = 2,  value = 7f),
            CalendarData(year = 2024, month = 1, day = 3,  value = 0f),
            CalendarData(year = 2024, month = 1, day = 4,  value = 12f),
            CalendarData(year = 2024, month = 1, day = 7,  value = 5f),
            // ... more dates
        )
    },
    modifier = Modifier.fillMaxWidth(),
    config = CalendarHeatmapConfig(
        animation = Animation.Default,
    ),
    visibleWeeks = 12,
    scrollEnabled = true,
    onDayClick = { calendarData -> println("${calendarData.year}-${calendarData.month}-${calendarData.day}: ${calendarData.value}") },
)
```

Each `CalendarData` entry is identified by `year`, `month` (1–12), and `day` (1–31). Data points do not need to be contiguous — missing dates simply render as empty cells.

**Key config options:**
- `visibleWeeks` — number of week columns visible at a time before horizontal scrolling is needed
- `scrollEnabled` — when `true`, the user can scroll horizontally through the full date range
- `config` — `CalendarHeatmapConfig` controls cell size, corner radius, color scale (low-intensity to high-intensity colors), and day-of-week label visibility
