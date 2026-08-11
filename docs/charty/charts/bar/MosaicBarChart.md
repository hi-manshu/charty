# MosaicBarChart

Best for visualizing a matrix of categorical data where cell color intensity encodes magnitude.

```kotlin
MosaicBarChart(
    data = {
        listOf(
            BarGroup(
                label = "Row 1",
                values = listOf(10f, 50f, 30f, 90f),
            ),
            BarGroup(
                label = "Row 2",
                values = listOf(70f, 20f, 80f, 40f),
            ),
        )
    },
    modifier = Modifier.fillMaxWidth().height(200.dp),
    config = MosaicBarChartConfig(
        animation = Animation.Default,
    ),
    onSegmentClick = { segment -> println("Cell: ${segment.barGroup.label}[${segment.segmentIndex}]") },
)
```

**Key config options:**
- `config` — `MosaicBarChartConfig` controls cell spacing and corner radius
- Each `BarGroup.values` list represents one row; colors can be overridden per segment via `BarGroup.colors`
