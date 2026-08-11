# AreaChart

Best for emphasizing the volume or magnitude of a single series by filling the area beneath the line.

```kotlin
AreaChart(
    data = {
        listOf(
            LineData(label = "Week 1", value = 400f),
            LineData(label = "Week 2", value = 620f),
            LineData(label = "Week 3", value = 530f),
            LineData(label = "Week 4", value = 780f),
            LineData(label = "Week 5", value = 710f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Gradient(listOf(Color(0xFF6650A4), Color(0xFF03DAC5))),
    lineConfig = LineChartConfig(
        lineWidth = 2f,
        smoothCurve = true,
        animation = Animation.Default,
    ),
    fillAlpha = 0.35f,
    onPointClick = { lineData -> println("${lineData.label}: ${lineData.value}") },
)
```

**Key config options:**
- `fillAlpha` — controls the opacity of the filled area (0f = invisible, 1f = fully opaque)
- `color` — a `ChartyColor.Gradient` applies the gradient both to the line stroke and the fill
- `lineConfig.negativeValuesDrawMode` — determines how values below zero affect the filled region
