# ComparisonBarChart

Best for comparing multiple values (e.g., two metrics side by side) within the same category group — each `BarGroup` renders its values as individual bars clustered together.

```kotlin
ComparisonBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Q1",
                values = listOf(120f, 95f),
                colors = listOf(
                    ChartyColor.Solid(Color(0xFF6650A4)),
                    ChartyColor.Solid(Color(0xFFE91E63)),
                ),
            ),
            BarGroup(
                label = "Q2",
                values = listOf(180f, 140f),
                colors = listOf(
                    ChartyColor.Solid(Color(0xFF6650A4)),
                    ChartyColor.Solid(Color(0xFFE91E63)),
                ),
            ),
            BarGroup(
                label = "Q3",
                values = listOf(95f, 160f),
                colors = listOf(
                    ChartyColor.Solid(Color(0xFF6650A4)),
                    ChartyColor.Solid(Color(0xFFE91E63)),
                ),
            ),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    comparisonConfig = ComparisonBarChartConfig(
        cornerRadius = CornerRadius.Medium,
        animation = Animation.Default,
    ),
    onBarClick = { segment ->
        println("Group: ${segment.barGroup.label}, bar ${segment.barIndex} = ${segment.barValue}")
    },
)
```

Each `BarGroup` **must** have a `colors` list with one `ChartyColor` per value — the chart uses these per-bar colors and will throw at runtime if they are missing or shorter than `values`.

**Click data:** `ComparisonBarSegment(barGroup, barIndex, barValue)`

**Key config options:**
- `cornerRadius` — rounding applied to bar tops (positive bars) or bottoms (negative bars)
- `negativeValuesDrawMode` — `BELOW_AXIS` or `FROM_MIN_VALUE`
- `animation` — entrance animation duration; `Animation.Default` = 800 ms
