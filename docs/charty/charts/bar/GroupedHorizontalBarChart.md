# GroupedHorizontalBarChart

Best for side-by-side comparison of multiple series across the same categories in a horizontal layout.

```kotlin
GroupedHorizontalBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Region A",
                values = listOf(120f, -40f, 80f),
            ),
            BarGroup(
                label = "Region B",
                values = listOf(90f, 60f, -30f),
            ),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    colors = ChartyColor.Gradient(listOf(Color(0xFF6650A4), Color(0xFF03DAC5))),
    config = GroupedHorizontalBarChartConfig(
        barWidthFraction = 0.7f,
        barSpacing = 4f,
        cornerRadius = CornerRadius.Medium,
        negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
        animation = Animation.Default,
    ),
    onBarClick = { entry -> println("Group ${entry.barGroup.label}, bar ${entry.barIndex} = ${entry.barValue}") },
)
```

**Key config options:**
- `barSpacing` — gap between bars within the same group
- `negativeValuesDrawMode` — negative bars extend left of the axis; positive bars extend right
- `cornerRadius` — applies to the trailing end of each individual bar
