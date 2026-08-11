# HorizontalBarChart

Best for long category labels or ranked lists where horizontal reading is more natural.

```kotlin
HorizontalBarChart(
    data = {
        listOf(
            BarData(label = "Category A", value = 340f),
            BarData(label = "Category B", value = 210f),
            BarData(label = "Category C", value = 480f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(250.dp),
    color = ChartyColor.Solid(Color(0xFF0288D1)),
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Medium,
        animation = Animation.Default,
    ),
    onBarClick = { barData -> println("Clicked: ${barData.label}") },
)
```

**Key config options:**
- `cornerRadius` — rounded corners on the leading (right) end of each bar
- `showDataLabels` — draws value text at the end of each bar
- `negativeValuesDrawMode` — controls how bars with negative values are rendered
