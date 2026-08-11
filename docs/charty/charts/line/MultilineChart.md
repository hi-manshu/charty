# MultilineChart

Best for comparing multiple data series on a single set of axes.

```kotlin
MultilineChart(
    data = {
        listOf(
            LineGroup(label = "Jan", values = listOf(100f, 80f, 60f)),
            LineGroup(label = "Feb", values = listOf(120f, 95f, 70f)),
            LineGroup(label = "Mar", values = listOf(90f,  110f, 85f)),
            LineGroup(label = "Apr", values = listOf(150f, 130f, 100f)),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    colors = ChartyColor.Gradient(
        listOf(Color(0xFF6650A4), Color(0xFFE91E63), Color(0xFF00BCD4))
    ),
    lineConfig = LineChartConfig(
        lineWidth = 2f,
        showPoints = true,
        smoothCurve = true,
        animation = Animation.Default,
    ),
    onPointClick = { point -> println("Series ${point.seriesIndex} in ${point.lineGroup.label}") },
)
```

Each `LineGroup.values` list must have the same length as the number of series, and the `colors` gradient list should contain one color per series.

## Legend

Add a color-coded legend below the chart:

```kotlin
MultilineChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    colors = ChartyColor.Gradient(
        listOf(Color(0xFF6650A4), Color(0xFFE91E63), Color(0xFF00BCD4))
    ),
    lineConfig = LineChartConfig(
        legendLabels = listOf("Revenue", "Expenses", "Profit"),
        legendTextStyle = TextStyle(fontSize = 12.sp),
        animation = Animation.Default,
    ),
)
```

## Gradient Fill Under Each Series

Draw a semi-transparent shaded area beneath each line:

```kotlin
MultilineChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    colors = ChartyColor.Gradient(
        listOf(Color(0xFF6650A4), Color(0xFFE91E63), Color(0xFF00BCD4))
    ),
    lineConfig = LineChartConfig(
        showGradientFill = true,
        gradientFillAlpha = 0.25f,
        legendLabels = listOf("Revenue", "Expenses", "Profit"),
        animation = Animation.Default,
    ),
)
```

The fill is drawn before the line so the line renders on top. Each series receives its corresponding color from the gradient list at the reduced `gradientFillAlpha` opacity.

**Key config options:**
- `legendLabels` — list of strings (one per series) that appear as color-dot legend items below the chart
- `showGradientFill` / `gradientFillAlpha` — enable and tune the shaded area under each series
- `crosshairConfig` — crosshair snaps to the nearest x-position and displays all series values in a combined tooltip label (e.g., `"L1: 100  L2: 80  L3: 60"`)
