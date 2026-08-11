# MultipleRadarChart

Best for comparing multiple entities or time periods across the same set of axes on a single radar.

```kotlin
MultipleRadarChart(
    dataSets = {
        listOf(
            RadarDataSet(
                label = "Current Year",
                axes = listOf(
                    RadarAxisData(label = "Revenue",      value = 90f),
                    RadarAxisData(label = "Satisfaction", value = 75f),
                    RadarAxisData(label = "Growth",       value = 60f),
                    RadarAxisData(label = "Retention",    value = 85f),
                    RadarAxisData(label = "Efficiency",   value = 70f),
                )
            ),
            RadarDataSet(
                label = "Previous Year",
                axes = listOf(
                    RadarAxisData(label = "Revenue",      value = 70f),
                    RadarAxisData(label = "Satisfaction", value = 65f),
                    RadarAxisData(label = "Growth",       value = 80f),
                    RadarAxisData(label = "Retention",    value = 60f),
                    RadarAxisData(label = "Efficiency",   value = 55f),
                )
            ),
        )
    },
    modifier = Modifier.size(320.dp),
    config = MultipleRadarChartConfig(
        animation = Animation.Default,
    ),
    onDataSetClick = { label, index -> println("Tapped: $label (index $index)") },
    accessibilityDescription = "Year-over-year comparison across five business metrics",
)
```

Each `RadarDataSet` is rendered as a separate overlapping polygon with a distinct fill color. The `onDataSetClick` callback identifies which data set polygon was tapped.

**Key config options:**
- Each data set is automatically assigned a unique color from the chart's internal color palette; override via `RadarChartConfig`
- All `RadarDataSet` entries must have the same number and order of axes
- `onDataSetClick` — invoked with `(label: String, index: Int)` identifying which data set polygon was tapped
