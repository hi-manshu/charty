# RadarChart

Best for displaying a single entity's performance across multiple axes (spider chart / star chart).

```kotlin
RadarChart(
    data = {
        listOf(
            RadarDataSet(
                label = "Team Alpha",
                axes = listOf(
                    RadarAxisData(label = "Attack",  value = 85f),
                    RadarAxisData(label = "Defense", value = 70f),
                    RadarAxisData(label = "Speed",   value = 90f),
                    RadarAxisData(label = "Stamina", value = 65f),
                    RadarAxisData(label = "Skill",   value = 80f),
                )
            )
        )
    },
    modifier = Modifier.size(300.dp),
    config = RadarChartConfig(
        animation = Animation.Default,
    ),
    accessibilityDescription = "Team Alpha radar chart showing performance across five metrics",
)
```

The number of axes is determined by the length of the `axes` list in the first `RadarDataSet`. All data sets in a `MultipleRadarChart` must have the same number of axes.

**Key config options:**
- `config` — `RadarChartConfig` controls polygon fill color, stroke width, axis label text style, and the number of concentric guide rings
- Each `RadarAxisData.value` is automatically normalized to the maximum value found across all axes for rendering
