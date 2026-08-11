# BubbleBarChart

Best for showing value distributions as stacked bubbles in a column — instead of a rectangular bar, each column contains a series of circles whose combined height represents the data value.

```kotlin
BubbleBarChart(
    data = {
        listOf(
            BarData(label = "Mon", value = 5f),
            BarData(label = "Tue", value = 8f),
            BarData(label = "Wed", value = 3f),
            BarData(label = "Thu", value = 10f),
            BarData(label = "Fri", value = 6f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    color = ChartyColor.Solid(ChartyColors.Blue),
    bubbleConfig = BubbleBarChartConfig(
        bubbleRadius = 100f,
        bubbleSpacing = 8f,
        animation = Animation.Default,
    ),
    onBarClick = { barData -> println("Clicked: ${barData.label} = ${barData.value}") },
)
```

The `value` field controls how many bubbles are stacked in the column (`value` is treated as a count). Non-integer values are handled by rendering a partial bubble at the top.

**Key config options (`BubbleBarChartConfig`):**
- `bubbleRadius` — pixel radius of each individual bubble
- `bubbleSpacing` — gap between adjacent bubbles in pixels
- `barWidthFraction` — fraction of the column slot occupied by each bubble column (0f–1f)
- `negativeValuesDrawMode` — `BELOW_AXIS` or `FROM_MIN_VALUE`
- `animation` — bubbles fill in from the baseline on entry
