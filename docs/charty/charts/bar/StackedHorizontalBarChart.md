# StackedHorizontalBarChart

Best for showing cumulative part-to-whole composition across categories in a horizontal layout.

```kotlin
StackedHorizontalBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Product A",
                values = listOf(30f, 50f, 20f),
            ),
            BarGroup(
                label = "Product B",
                values = listOf(60f, 25f, 45f),
            ),
        )
    },
    modifier = Modifier.fillMaxWidth().height(200.dp),
    colors = ChartyColor.Solid(Color(0xFF00897B)),
    config = StackedHorizontalBarChartConfig(
        barWidthFraction = 0.6f,
        rightCornerRadius = CornerRadius.Medium,
        animation = Animation.Default,
    ),
    onSegmentClick = { segment -> println("Tapped ${segment.barGroup.label} segment ${segment.segmentIndex}") },
)
```

**Key config options:**
- `rightCornerRadius` — rounds the trailing (rightmost) corners of the last segment
- `barWidthFraction` — controls the thickness of each bar row (0f–1f)
- `referenceLine` — optional vertical guide line drawn at a fixed value

> Note: All segment values must be positive; negative values throw an `IllegalArgumentException`.
