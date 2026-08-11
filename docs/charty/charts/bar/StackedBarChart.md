# StackedBarChart

Best for showing part-to-whole relationships across categories with vertical stacked bars.

```kotlin
StackedBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Q1",
                values = listOf(40f, 60f, 30f),
                colors = listOf(
                    ChartyColor.Solid(Color(0xFF6650A4)),
                    ChartyColor.Solid(Color(0xFF9C27B0)),
                    ChartyColor.Solid(Color(0xFFCE93D8)),
                )
            ),
            BarGroup(label = "Q2", values = listOf(55f, 45f, 70f)),
            BarGroup(label = "Q3", values = listOf(80f, 20f, 50f)),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    colors = ChartyColor.Solid(Color(0xFF6650A4)),
    stackedConfig = StackedBarChartConfig(
        showDataLabels = true,
        animation = Animation.Default,
    ),
    onSegmentClick = { segment -> println("Segment ${segment.segmentIndex} of ${segment.barGroup.label}") },
)
```

**Key config options:**
- `showDataLabels` — draws the total stack value above each bar
- `topCornerRadius` — applied to the top corners of the topmost segment
- `animation` — controls the grow-from-bottom entrance animation
