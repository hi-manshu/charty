# BarChart

Best for comparing discrete values across categories with vertical bars.

```kotlin
BarChart(
    data = {
        listOf(
            BarData(label = "Mon", value = 120f),
            BarData(label = "Tue", value = 85f),
            BarData(label = "Wed", value = 200f),
            BarData(label = "Thu", value = 60f),
            BarData(label = "Fri", value = 175f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Large,
        showDataLabels = true,
        animation = Animation.Default,
    ),
    onBarClick = { barData -> println("Clicked: ${barData.label} = ${barData.value}") },
)
```

**Key config options:**
- `cornerRadius` — controls bar corner rounding (`None`, `Small`, `Medium`, `Large`, `ExtraLarge`, or `Custom(radius)`)
- `showDataLabels` — draws the numeric value above each bar
- `negativeValuesDrawMode` — `BELOW_AXIS` (bars extend below zero) or `FROM_MIN_VALUE` (baseline shifts to the minimum value)
