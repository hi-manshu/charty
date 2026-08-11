# ComboChart

Best for overlaying a bar series and a line series on the same axes — ideal for showing volume alongside a trend (e.g., sales bars with a moving-average line).

```kotlin
ComboChart(
    data = {
        listOf(
            ComboChartData(label = "Jan", barValue = 120f, lineValue = 105f),
            ComboChartData(label = "Feb", barValue = 95f,  lineValue = 115f),
            ComboChartData(label = "Mar", barValue = 180f, lineValue = 130f),
            ComboChartData(label = "Apr", barValue = 140f, lineValue = 145f),
            ComboChartData(label = "May", barValue = 220f, lineValue = 160f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    barColor = ChartyColor.Solid(Color(0xFF6650A4)),
    lineColor = ChartyColor.Solid(Color(0xFFE91E63)),
    comboConfig = ComboChartConfig(
        barWidthFraction = 0.5f,
        barCornerRadius = CornerRadius.Medium,
        lineWidth = 2f,
        showPoints = true,
        animation = Animation.Default,
    ),
    onDataClick = { comboData -> println("${comboData.label}: bar=${comboData.barValue}, line=${comboData.lineValue}") },
)
```

## Crosshair

Enable a drag-to-snap crosshair that snaps to the line points:

```kotlin
ComboChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    barColor = ChartyColor.Solid(Color(0xFF6650A4)),
    lineColor = ChartyColor.Solid(Color(0xFFE91E63)),
    comboConfig = ComboChartConfig(
        crosshairConfig = ChartCrosshairConfig(
            tooltipConfig = TooltipConfig(),
            dismissOnRelease = true,
        ),
        animation = Animation.Default,
    ),
)
```

**Key config options:**
- `barWidthFraction` — width of each bar relative to its column slot (0f–1f)
- `showPoints` — renders a dot at each line data point
- `crosshairConfig` — when active, the crosshair snaps to line points and replaces click interaction
