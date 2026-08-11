# LineChart

Best for visualizing a single data series over time or an ordered sequence of values.

```kotlin
LineChart(
    data = {
        listOf(
            LineData(label = "Jan", value = 120f),
            LineData(label = "Feb", value = 95f),
            LineData(label = "Mar", value = 180f),
            LineData(label = "Apr", value = 140f),
            LineData(label = "May", value = 220f),
            LineData(label = "Jun", value = 175f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    lineConfig = LineChartConfig(
        lineWidth = 2f,
        showPoints = true,
        pointRadius = 4f,
        smoothCurve = true,
        animation = Animation.Default,
    ),
    onPointClick = { lineData -> println("Clicked: ${lineData.label} = ${lineData.value}") },
)
```

**Key config options:**
- `smoothCurve` — renders a Bezier curve through points instead of straight line segments
- `showPoints` — draws a dot at each data point; `pointRadius` controls its size
- `crosshairConfig` — enables a drag-to-snap crosshair with a tooltip (see [Crosshair](#crosshair))

## Crosshair

Add a crosshair so users can drag to inspect exact values:

```kotlin
LineChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    lineConfig = LineChartConfig(
        crosshairConfig = ChartCrosshairConfig(
            verticalLineColor = ChartyColor.Solid(Color(0xFF6650A4)),
            horizontalLineColor = ChartyColor.Solid(Color(0xFFCCCCCC)),
            tooltipConfig = TooltipConfig(),
            dismissOnRelease = true,
        ),
    ),
)
```
