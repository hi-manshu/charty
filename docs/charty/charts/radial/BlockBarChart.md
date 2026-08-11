# BlockBarChart

Best for visualizing a single categorical distribution as a row of colored blocks — a compact alternative to a horizontal stacked bar when exact proportions matter less than the visual presence of each category.

```kotlin
BlockBar(
    data = {
        listOf(
            BlockData(value = 40f, color = ChartyColor.Solid(Color(0xFF6650A4))),
            BlockData(value = 25f, color = ChartyColor.Solid(Color(0xFFE91E63))),
            BlockData(value = 20f, color = ChartyColor.Solid(Color(0xFF00BCD4))),
            BlockData(value = 15f, color = ChartyColor.Solid(Color(0xFFFFB300))),
        )
    },
    modifier = Modifier.fillMaxWidth().height(56.dp),
    blockBarConfig = BlockBarChartConfig(),
    accessibilityDescription = "Market share: Segment A 40%, B 25%, C 20%, D 15%",
)
```

Each `BlockData.value` defines the proportional width of its block. Values are normalized internally so they always sum to the full available width.

**Key config options:**
- `blockBarConfig` — `BlockBarChartConfig` controls corner radius and spacing between blocks
- `color` in `BlockData` — each block carries its own `ChartyColor`, supporting both `Solid` and `Gradient` fills
- `accessibilityDescription` — required for screen-reader users since there are no axis labels; always include a human-readable summary
