# PieChart

Best for showing proportional breakdown of a whole into named slices.

## Pie Style

```kotlin
PieChart(
    data = {
        listOf(
            PieData(label = "Product A", value = 40f, color = Color(0xFF6650A4)),
            PieData(label = "Product B", value = 30f, color = Color(0xFFE91E63)),
            PieData(label = "Product C", value = 20f, color = Color(0xFF00BCD4)),
            PieData(label = "Other",     value = 10f, color = Color(0xFFFFB300)),
        )
    },
    modifier = Modifier.size(280.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    config = PieChartConfig(
        style = PieChartStyle.PIE,
        animation = Animation.Default,
    ),
    onSliceClick = { pieData, _ -> println("Tapped: ${pieData.label} (${pieData.value})") },
    accessibilityDescription = "Sales breakdown by product category",
)
```

## Donut Style

Use the `DONUT` style and control the hole size with `donutHoleRatio`:

```kotlin
PieChart(
    data = {
        listOf(
            PieData(label = "iOS",     value = 55f, color = Color(0xFF6650A4)),
            PieData(label = "Android", value = 35f, color = Color(0xFF00BCD4)),
            PieData(label = "Web",     value = 10f, color = Color(0xFFE91E63)),
        )
    },
    modifier = Modifier.size(280.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    config = PieChartConfig(
        style = PieChartStyle.DONUT,
        donutHoleRatio = 0.55f,
        animation = Animation.Default,
        interactionConfig = ChartInteractionConfig(),
    ),
    onSliceClick = { pieData, _ -> println("Slice: ${pieData.label}") },
    centerContent = {
        // Composable rendered inside the donut hole
        Text(text = "Platform\nShare", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
    },
    accessibilityDescription = "Platform distribution: iOS 55%, Android 35%, Web 10%",
)
```

**Key config options:**
- `style` — `PieChartStyle.PIE` (solid disc) or `PieChartStyle.DONUT` (ring with a center hole)
- `donutHoleRatio` — fraction of the chart radius cut out for the donut hole (0f–1f; only used with `DONUT` style)
- `centerContent` — a composable slot rendered inside the donut hole (ignored in `PIE` style)
