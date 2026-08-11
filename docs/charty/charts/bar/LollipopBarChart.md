# LollipopBarChart

Best for datasets where the exact value at the tip matters more than bar area — each entry is rendered as a thin stem topped with a circle.

```kotlin
LollipopBarChart(
    data = {
        listOf(
            BarData(label = "Jan", value = 120f),
            BarData(label = "Feb", value = 85f),
            BarData(label = "Mar", value = 200f),
            BarData(label = "Apr", value = 60f),
            BarData(label = "May", value = 175f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    colors = ChartyColor.Solid(Color(0xFF2196F3)),
    config = LollipopBarChartConfig(
        stemThickness = 6f,
        circleRadius = 14f,
        animation = Animation.Default,
    ),
    onBarClick = { barData -> println("Clicked: ${barData.label} = ${barData.value}") },
)
```

**Key config options (`LollipopBarChartConfig`):**
- `stemThickness` — pixel width of the vertical stem line
- `circleRadius` — pixel radius of the lollipop circle head
- `circleStrokeWidth` — set to `> 0f` to draw the circle as a ring instead of a filled disc
- `circleColor` — optional `ChartyColor` override for the circle head (defaults to the chart-level `colors`)
- `animation` — stems grow upward from the baseline on entry
