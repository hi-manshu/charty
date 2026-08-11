# WaterfallChart

Best for showing cumulative effects of sequential positive and negative values (profit/loss bridges, cash flow).

```kotlin
WaterfallChart(
    data = {
        listOf(
            BarData(label = "Start",    value = 500f),
            BarData(label = "Revenue",  value = 200f),
            BarData(label = "Costs",    value = -150f),
            BarData(label = "Tax",      value = -50f),
            BarData(label = "End",      value = 500f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    config = WaterfallChartConfig(
        animation = Animation.Default,
    ),
    onBarClick = { barData -> println("${barData.label}: ${barData.value}") },
)
```

**Key config options:**
- Positive values draw in the chart's accent color; negative values draw in the negative color defined in `WaterfallChartConfig`
- The first and last bars are conventionally treated as total/subtotal bars
- `scaffoldConfig` controls axis label formatting for currency or unit display
