# BubbleChart

Best for three-variable data where x-position, y-position, and a third variable (bubble size) are all meaningful.

```kotlin
BubbleChart(
    data = {
        listOf(
            BubbleData(label = "Alpha",   yValue = 200f, size = 40f),
            BubbleData(label = "Beta",    yValue = 350f, size = 80f),
            BubbleData(label = "Gamma",   yValue = 150f, size = 20f),
            BubbleData(label = "Delta",   yValue = 420f, size = 60f),
            BubbleData(label = "Epsilon", yValue = 280f, size = 100f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    color = ChartyColor.Gradient(listOf(Color(0xFF6650A4), Color(0xFF03DAC5))),
    config = PointChartConfig(
        animation = Animation.Default,
    ),
    minBubbleRadius = 12f,
    onBubbleClick = { bubbleData -> println("${bubbleData.label}: y=${bubbleData.yValue}, size=${bubbleData.size}") },
)
```

The `size` field in `BubbleData` is interpolated between `minBubbleRadius` and a computed maximum radius so that relative proportions are preserved visually.

## Crosshair

```kotlin
BubbleChart(
    data = { /* ... */ },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    minBubbleRadius = 12f,
    crosshairConfig = ChartCrosshairConfig(
        tooltipConfig = TooltipConfig(),
        dismissOnRelease = true,
    ),
)
```

**Key config options:**
- `size` in `BubbleData` — maps to bubble radius; the chart interpolates between `minBubbleRadius` and the chart's computed max radius
- `minBubbleRadius` — floor for bubble rendering so that very small values still remain tappable
- `crosshairConfig` — crosshair snaps to bubble centers by x-position
