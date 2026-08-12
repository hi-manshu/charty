# MosaicBarChart

Best for comparing the *composition* of several categories: each `BarGroup` becomes one vertical column, and its values are stacked as percentages so every column fills the full plot height.

```kotlin
MosaicBarChart(
    data = {
        listOf(
            BarGroup(label = "Q1", values = listOf(10f, 50f, 30f, 90f)),
            BarGroup(label = "Q2", values = listOf(70f, 20f, 80f, 40f)),
        )
    },
    modifier = Modifier.fillMaxWidth().height(200.dp),
    config = MosaicBarChartConfig(
        barWidthFraction = 0.9f,
        animation = Animation.Default,
    ),
    onSegmentClick = { segment ->
        println("${segment.barGroup.label}[${segment.segmentIndex}] = ${segment.segmentPercentage}%")
    },
)
```

Each `BarGroup.values` list is **one column**, not one row. Values of `0f` or less are skipped. A group whose positive values sum to zero draws nothing.

`MosaicBarChart` has no chart-level colour parameter. Segments use `BarGroup.colors` when supplied, and otherwise cycle through a built-in three-colour palette.

**Click data:** `MosaicBarSegment(barGroup, segmentIndex, segmentValue, segmentPercentage)`

## Fixed 0–100% axis

The value axis is **always 0%–100%**, because every column is normalised to fill the plot height. Column totals are therefore not comparable across columns — only the internal proportions are. Use [StackedBarChart](StackedBarChart.md) when the absolute totals matter.

## Rolling window

```kotlin
config = MosaicBarChartConfig(visibleWindow = 12, animation = Animation.Fast)
```

Keeps only the last N columns on screen; `null` or at least `2`.

## Persistent markers

```kotlin
config = MosaicBarChartConfig(
    visibleWindow = 12,
    markers = listOf(PersistentMarker(dataIndex = -1, label = "Latest")),
)
```

A negative `dataIndex` counts back from the end of the drawn data, so `-1` marks the newest column.

## Animating value changes

```kotlin
config = MosaicBarChartConfig(animateValueChanges = true, animation = Animation.Fast)
```

## Tooltip

```kotlin
MosaicBarChart(
    data = { groups },
    modifier = Modifier.fillMaxWidth().height(200.dp),
    tooltip = ChartTooltip.canvas(),
    config = MosaicBarChartConfig(
        tooltipFormatter = { segment -> "${segment.barGroup.label}: ${segment.segmentPercentage.toInt()}%" },
    ),
)
```

## Accessibility

A generated summary plus one focusable node per column.

```kotlin
interactionConfig = ChartInteractionConfig(accessibilityDescription = "Traffic source mix by quarter")
```

## `MosaicBarChartConfig`

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `barWidthFraction` | `Float` | `0.9f` | Column width as a fraction of its slot; `0f..1f` |
| `animation` | `Animation` | `Animation.Default` | Grow-from-bottom entry animation |
| `animateValueChanges` | `Boolean` | `false` | Tween segment values on data change |
| `markers` | `List<PersistentMarker>` | `emptyList()` | Persistent pinned labels |
| `tooltipConfig` | `TooltipConfig` | `TooltipConfig()` | Canvas tooltip appearance |
| `tooltipPosition` | `TooltipPosition` | `AUTO` | `ABOVE`, `BELOW`, or `AUTO` |
| `tooltipFormatter` | `(MosaicBarSegment) -> String` | `"label [i]: n%"` | Tooltip text |
| `visibleWindow` | `Int?` | `null` | Rolling "show last N" window; `null` or `>= 2` |

## Limitations

- There is **no corner-radius or segment-spacing option** on `MosaicBarChartConfig`; segments are drawn flush.
- No crosshair, no reference line, and no data labels.
