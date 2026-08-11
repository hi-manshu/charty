# StackedAreaChart

Best for showing how multiple series combine to form a cumulative total, emphasizing both individual contributions and the whole.

```kotlin
StackedAreaChart(
    data = {
        listOf(
            LineGroup(label = "Q1", values = listOf(200f, 150f, 100f)),
            LineGroup(label = "Q2", values = listOf(250f, 180f, 120f)),
            LineGroup(label = "Q3", values = listOf(300f, 200f, 140f)),
            LineGroup(label = "Q4", values = listOf(280f, 220f, 160f)),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    colors = ChartyColor.Gradient(
        listOf(Color(0xFF6650A4), Color(0xFFE91E63), Color(0xFF00BCD4))
    ),
    lineConfig = LineChartConfig(
        smoothCurve = true,
        animation = Animation.Default,
        legendLabels = listOf("Series A", "Series B", "Series C"),
        crosshairConfig = ChartCrosshairConfig(
            tooltipConfig = TooltipConfig(),
            dismissOnRelease = true,
        ),
    ),
    fillAlpha = 0.5f,
    onAreaClick = { point -> println("Series ${point.seriesIndex} in ${point.lineGroup.label}") },
)
```

Each `LineGroup.values[i]` is the value for series `i` at that x-position. Values are accumulated from bottom to top — the topmost boundary of the chart is the sum of all series values at each point.

**Key config options:**
- `fillAlpha` — opacity of the area fill; a lower value (e.g., `0.4f`) avoids color collision between stacked layers
- `legendLabels` — renders a color-dot legend below the chart, one entry per series
- `crosshairConfig` — crosshair snaps to the top cumulative position and shows a combined label for all series at that x-point
