# NormalizedHorizontalBarChart

Best for showing each group's composition as a percentage of the whole (every row always fills 100% width).

```kotlin
NormalizedHorizontalBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Market A",
                values = listOf(50f, 30f, 20f),
                colors = listOf(
                    ChartyColor.Solid(Color(0xFF6650A4)),
                    ChartyColor.Solid(Color(0xFFE91E63)),
                    ChartyColor.Solid(Color(0xFF00BCD4)),
                )
            ),
            BarGroup(label = "Market B", values = listOf(70f, 15f, 15f)),
        )
    },
    modifier = Modifier.fillMaxWidth().height(200.dp),
    colors = ChartyColor.Solid(Color(0xFF6650A4)),
    config = NormalizedHorizontalBarChartConfig(
        barWidthFraction = 0.65f,
        rightCornerRadius = CornerRadius.Small,
        animation = Animation.Default,
    ),
    onSegmentClick = { segment ->
        println("${segment.barGroup.label} segment ${segment.segmentIndex}: ${segment.segmentPercentage}%")
    },
)
```

**Key config options:**
- `rightCornerRadius` — rounds the trailing corners of the last (rightmost) segment
- `barWidthFraction` — thickness of each 100%-wide bar row
- Values ≤ 0 are silently skipped during normalization (all segment values must be positive for meaningful output)
