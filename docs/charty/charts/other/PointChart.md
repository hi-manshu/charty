# PointChart

Best for scatter-plot style visualization where individual data point positions matter more than connecting trends.

```kotlin
PointChart(
    data = {
        listOf(
            PointData(label = "A", value = 42f),
            PointData(label = "B", value = 78f),
            PointData(label = "C", value = 55f),
            PointData(label = "D", value = 91f),
            PointData(label = "E", value = 33f),
            PointData(label = "F", value = 67f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(280.dp),
    color = ChartyColor.Solid(Color(0xFF00897B)),
    pointConfig = PointChartConfig(
        pointRadius = 6f,
        animation = Animation.Default,
    ),
    onPointClick = { pointData -> println("Clicked: ${pointData.label} = ${pointData.value}") },
)
```

## Crosshair

```kotlin
PointChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(280.dp),
    color = ChartyColor.Solid(Color(0xFF00897B)),
    pointConfig = PointChartConfig(pointRadius = 6f),
    crosshairConfig = ChartCrosshairConfig(
        verticalLineColor = ChartyColor.Solid(Color(0xFF00897B)),
        tooltipConfig = TooltipConfig(),
        dismissOnRelease = true,
    ),
)
```

**Key config options:**
- `pointRadius` — size of each rendered dot
- `crosshairConfig` — snaps to the nearest point by x-position and shows a tooltip
- `animation` — points scale in from zero radius on first composition
