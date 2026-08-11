# SpanChart

Best for displaying min-to-max ranges (confidence intervals, temperature highs/lows, bid-ask spreads).

```kotlin
SpanChart(
    data = {
        listOf(
            SpanData(label = "Jan", minValue = 2f, maxValue = 10f),
            SpanData(label = "Feb", minValue = 5f, maxValue = 14f),
            SpanData(label = "Mar", minValue = 8f, maxValue = 18f),
            SpanData(label = "Apr", minValue = 12f, maxValue = 22f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(280.dp),
    colors = ChartyColor.Gradient(listOf(Color(0xFF6650A4), Color(0xFF03DAC5))),
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Large,
        animation = Animation.Default,
    ),
    onSpanClick = { spanData -> println("Range: ${spanData.minValue}–${spanData.maxValue}") },
)
```

**Key config options:**
- `colors` — a `ChartyColor.Gradient` creates a gradient fill along each span bar
- `cornerRadius` — rounds both ends of the floating span bar
- `barConfig.animation` — bars grow from the min value to the max value on entry
